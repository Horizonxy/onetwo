package org.onetwo.ext.permission;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.onetwo.common.annotation.AnnotationUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.ext.permission.api.annotation.ByPermissionClass;
import org.onetwo.ext.permission.entity.DefaultIPermission;
import org.onetwo.ext.permission.utils.PermissionUtils;
import org.onetwo.ext.permission.utils.UrlResourceInfo;
import org.onetwo.ext.permission.utils.UrlResourceInfoParser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class PermissionHandlerMappingListener implements InitializingBean {
	
	private UrlResourceInfoParser urlResourceInfoParser = new UrlResourceInfoParser();
	
//	private final Logger logger = MyLoggerFactory.getLogger(this.getClass());
	
	@Resource
	private PermissionManager<?> permissionManager;
	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;
	private boolean syncMenuDataEnable;
	
	public void setSyncMenuDataEnable(boolean syncMenuDataEnable) {
		this.syncMenuDataEnable = syncMenuDataEnable;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(!syncMenuDataEnable){
			return ;
		}
		Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMappingHandlerMapping.getHandlerMethods();
		this.onHandlerMethodsInitialized(handlerMethods);

		this.permissionManager.syncMenuToDatabase();
	}

	private void setMenuUrlByRequestMappingInfo(Class<?> codeClass, DefaultIPermission<?> perm, Entry<RequestMappingInfo, HandlerMethod> entry){
		DefaultIPermission<?> menu = perm;

		String method = getFirstMethod(entry.getKey());
		if(!RequestMethod.GET.name().toLowerCase().equals(method)){
			return ;
		}
		
		menu.setMethod(method);
//		String url = entry.getKey().getPatternsCondition().getPatterns().iterator().next();
		String url = null;
		if(StringUtils.isNotBlank(menu.getUrl())){
			//如果自定义了，忽略自动解释
			url = menu.getUrl();
		}else{
			url = getFirstUrl(entry.getKey());
		}
		menu.setUrl(url);
//		Iterator<RequestMethod> it = entry.getKey().getMethodsCondition().getMethods().iterator();

		/*if(it.hasNext()){
			//取第一个映射方法
			String method = entry.getKey().getMethodsCondition().getMethods().iterator().next().toString();
			menu.setMethod(method);
		}else{
			menu.setMethod(RequestMethod.GET.name());
		}*/
	}
	
	private void setResourcePatternByRequestMappingInfo(Class<?> codeClass, DefaultIPermission<?> perm, Entry<RequestMappingInfo, HandlerMethod> entry){
		/*if(perm.getResourcesPattern()!=null){
			List<UrlResourceInfo> infos = urlResourceInfoParser.parseToUrlResourceInfos(perm.getResourcesPattern());
			//如果自定义了，忽略自动解释
			if(!infos.isEmpty()){
				String urls = this.urlResourceInfoParser.parseToString(infos);
				perm.setResourcesPattern(urls);
			}
			return ;
		}*/
		List<UrlResourceInfo> infos = urlResourceInfoParser.parseToUrlResourceInfos(perm.getResourcesPattern());
		
		Set<String> urlPattterns = entry.getKey().getPatternsCondition().getPatterns();
		
		if(urlPattterns.size()==1){
			String url = urlPattterns.stream().findFirst().orElse("");
			infos.add(new UrlResourceInfo(appendStarPostfix(url), getFirstMethod(entry.getKey())));
			
		}else{
			//超过一个url映射的，不判断方法
			urlPattterns.stream().forEach(url->infos.add(new UrlResourceInfo(appendStarPostfix(url))));
		}
		String urls = this.urlResourceInfoParser.parseToString(infos);
		perm.setResourcesPattern(urls);
	}
	
	private String appendStarPostfix(String url){
		url = StringUtils.trimEndWith(url, "/");
		if(!url.contains(".")){
			url += ".*";
		}
		return url;
	}
	
	private String getFirstMethod(RequestMappingInfo info){
		//取第一个映射方法
		return info.getMethodsCondition().getMethods().stream().findFirst().orElse(RequestMethod.GET).name();
	}
	
	private String getFirstUrl(RequestMappingInfo info){
		//取第一个映射地址
		return info.getPatternsCondition().getPatterns().stream().findFirst().orElse("");
	}
	
	public void onHandlerMethodsInitialized(Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
		this.permissionManager.build();
		for(Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()){
			ByPermissionClass permClassInst = entry.getValue().getMethodAnnotation(ByPermissionClass.class);
			if(permClassInst==null){
				continue ;
			}
			for(Class<?> codeClass : permClassInst.value()){
				this.autoConifgPermission(codeClass, entry, permClassInst);
			}
		}
	}
	
	private void autoConifgPermission(Class<?> codeClass, Entry<RequestMappingInfo, HandlerMethod> entry, ByPermissionClass permClassInst){
		if(AnnotationUtils.findAnnotationWithDeclaring(codeClass, Deprecated.class)!=null){
			return;
		}
		DefaultIPermission<?> perm = this.permissionManager.getPermission(codeClass);
		if(perm==null){
//			System.out.println("html: " + this.permissionManagerImpl.getMenuInfoParser().getRootMenu());
			throw new RuntimeException("can not find the menu code class["+ codeClass+"] in controller: " + entry.getValue());
		}
		if(PermissionUtils.isMenu(perm) && permClassInst.overrideMenuUrl()){
			this.setMenuUrlByRequestMappingInfo(codeClass, perm, entry);
		}
		this.setResourcePatternByRequestMappingInfo(codeClass, perm, entry);
	}
	
}
