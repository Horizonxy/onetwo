package org.onetwo.ext.security;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.file.FileUtils;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.ext.permission.utils.UrlResourceInfo;
import org.onetwo.ext.permission.utils.UrlResourceInfoParser;
import org.onetwo.ext.security.utils.SecurityUtils;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;


public class DatabaseSecurityMetadataSource extends JdbcDaoSupport /*implements FactoryBean<FilterInvocationSecurityMetadataSource>*/ {
	//获取权限标识和url的sql语句
	private static final String AUTHORITY_RESOURCE_SQL_FILE = "/plugins/security/authority_resource.sql";

	private String resourceQuery;
//	private String appCode;// ??
	
	private LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap;
	
	private DefaultWebSecurityExpressionHandler securityExpressionHandler = new DefaultWebSecurityExpressionHandler();
	

	private FilterSecurityInterceptor filterSecurityInterceptor;
	// from WebSecurityExpressionRoot
//	private Set<String> keywords = ImmutableSet.of("permitAll", "denyAll", "is", "has");
	
	protected List<AuthorityResource> fetchAuthorityResources(){
		if(StringUtils.isBlank(resourceQuery)){
			Resource res = SpringUtils.classpath(AUTHORITY_RESOURCE_SQL_FILE);
			if(res.exists()){
				try {
					List<String> strs = FileUtils.readAsList(res.getInputStream());
					this.resourceQuery = StringUtils.join(strs, " ");
				} catch (IOException e) {
					throw new BaseException("read sql file error: "+ AUTHORITY_RESOURCE_SQL_FILE, e);
				}
			}else{
				this.resourceQuery = "SELECT "
										+ "perm.code as authority, "
										+ "perm.resources_pattern as resources_pattern, "
										+ "perm.sort "
										+ "FROM admin_permission perm "
										+ "WHERE perm.resources_pattern is not null "
//										+ "and perm.resources_pattern!='' " //oracle里是个坑
										+ "order by perm.sort";
			}
		}
		
		Assert.hasText(resourceQuery);
		ResourceMapping mapping = new ResourceMapping(getDataSource(), resourceQuery);
		
		List<AuthorityResource> authorities = mapping.execute();
		return authorities;
	}
	/****
	 * fetch all permissions from database
	 */
//	@PostConstruct
	public void buildRequestMap(){
		List<AuthorityResource> authorities = fetchAuthorityResources();
		final Map<SortableAntPathRequestMatcher, Collection<ConfigAttribute>> resouceMap = new HashMap<>(authorities.size());
		authorities.forEach(auth->{
			auth.getUrlResourceInfo().forEach(r->{
				//根据httpmethod和url映射权限标识，url拦截时也是根据这个matcher找到对应的权限SecurityConfig
//				AntPathRequestMatcher matcher = new AntPathRequestMatcher(r.getUrl(), r.getMethod());
				SortableAntPathRequestMatcher matcher = new SortableAntPathRequestMatcher(new AntPathRequestMatcher(r.getUrl(), r.getMethod()), auth.getSort());
				if(resouceMap.containsKey(matcher)){
//					resouceMap.get(matcher).add(new SecurityConfig(auth.getAuthority()));
					throw new RuntimeException("Expected a single expression attribute for " + matcher);
				}else{
					resouceMap.put(matcher, Lists.newArrayList(createSecurityConfig(auth.getAuthority())));
				}
			});
		});
		
		//first sorted by sort field, then sorted by pattern
		//数字越小，越靠前
		List<SortableAntPathRequestMatcher> keys = resouceMap.keySet()
													.stream()
													.sorted((o1, o2)->{
														int rs = o1.getSort().compareTo(o2.getSort());
														if(rs!=0)
															return rs;
														return -o1.getPathMatcher().getPattern().compareTo(o2.getPathMatcher().getPattern());
													})
													.collect(Collectors.toList());
//		Collections.reverse(keys);
		this.requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
		keys.forEach(k->{
//			System.out.println("url:"+k.getPathMatcher()+", value:"+resouceMap.get(k));
			this.requestMap.put(k.getPathMatcher(), resouceMap.get(k));
		});
	}
	
	protected SecurityConfig createSecurityConfig(String authString){
		/*if(isKeyword(authString)){
			return new SecurityConfig(authString);
		}
		StringBuilder str = new StringBuilder("hasAuthority('");
		str.append(authString).append("')");*/
		return new SecurityConfig(SecurityUtils.createSecurityExpression(authString));
	}
	
	/*protected boolean isKeyword(String authority){
		return keywords.stream().filter(key->authority.startsWith(key))
						.findAny().isPresent();
	}*/
	
	/****
	 * 基于url匹配拦截时，转换为ExpressionBasedFilterInvocationSecurityMetadataSource
	 * @param source
	 * @return
	 */
	public void buildSecurityMetadataSource(){
		Assert.notNull(filterSecurityInterceptor);
		this.buildRequestMap();
		ExpressionBasedFilterInvocationSecurityMetadataSource ms = new ExpressionBasedFilterInvocationSecurityMetadataSource(requestMap, securityExpressionHandler);
		this.filterSecurityInterceptor.setSecurityMetadataSource(ms);
	}
	
	
	public void setFilterSecurityInterceptor(FilterSecurityInterceptor filterSecurityInterceptor) {
		this.filterSecurityInterceptor = filterSecurityInterceptor;
	}
	
	/*@Override
    public FilterInvocationSecurityMetadataSource getObject() throws Exception {
//	    return new DefaultFilterInvocationSecurityMetadataSource(requestMap);
		return new ExpressionBasedFilterInvocationSecurityMetadataSource(requestMap, securityExpressionHandler);
    }

	@Override
    public Class<?> getObjectType() {
	    return DefaultFilterInvocationSecurityMetadataSource.class;
    }

	@Override
    public boolean isSingleton() {
	    return true;
    }*/
	
	public static class ResourceMapping extends MappingSqlQuery<AuthorityResource> {
		private UrlResourceInfoParser urlResourceInfoParser = new UrlResourceInfoParser();

		public ResourceMapping(DataSource ds, String sql) {
	        super(ds, sql);
        }

		@Override
        protected AuthorityResource mapRow(ResultSet rs, int rowNum) throws SQLException {
			AuthorityResource authoricty = new AuthorityResource();
			String rp = rs.getString("resources_pattern");
			List<UrlResourceInfo> urlResourceInfo = urlResourceInfoParser.parseToUrlResourceInfos(rp);
			authoricty.setUrlResourceInfo(urlResourceInfo);
			authoricty.setAuthority(rs.getString("authority"));
			authoricty.setSort(rs.getInt("sort"));
	        return authoricty;
        }
		
	}
	
	@Data
	public static class AuthorityResource {
		//权限标识
		private String authority;
		private List<UrlResourceInfo> urlResourceInfo;
		private Integer sort;
	}

	@Data
	@EqualsAndHashCode
	@AllArgsConstructor
	static class SortableAntPathRequestMatcher {
		private AntPathRequestMatcher pathMatcher;
		private Integer sort;
	}

}