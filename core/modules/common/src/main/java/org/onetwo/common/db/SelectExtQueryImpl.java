package org.onetwo.common.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onetwo.common.db.sqlext.ExtQueryListener;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.db.sqlext.SQLSymbolManagerFactory;
import org.onetwo.common.profiling.UtilTimerStack;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.MyUtils;
import org.onetwo.common.utils.StringUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SelectExtQueryImpl extends AbstractExtQuery implements SelectExtQuery {
	

	protected StringBuilder select;
	protected StringBuilder join;
	protected StringBuilder orderBy;
	
	protected Integer firstResult = 0; 
	protected Integer maxResults = -1;
	
	protected boolean subQuery;

	private String countValue;
	
	private Map<String, Object> queryConfig;
	

	public SelectExtQueryImpl(Class<?> entityClass, String alias, Map params, SQLSymbolManager symbolManager) {
		super(entityClass, alias, params, symbolManager);
	}
	
	public SelectExtQueryImpl(Class<?> entityClass, String alias, Map params, SQLSymbolManager symbolManager, List<ExtQueryListener> listeners) {
		super(entityClass, alias, params, symbolManager, listeners);
	}


	public void initQuery(){
		this.firstResult = getValueAndRemoveKeyFromParams(K.FIRST_RESULT, firstResult);
		this.maxResults = getValueAndRemoveKeyFromParams(K.MAX_RESULTS, maxResults);
		this.countValue = getValueAndRemoveKeyFromParams(K.COUNT, countValue);
		
		//query config
		Object qc = getValueAndRemoveKeyFromParams(K.QUERY_CONFIG, queryConfig);
		if(qc instanceof Object[]){
			this.queryConfig = CUtils.asMap((Object[])qc);
		}else{
			this.queryConfig = (Map)qc;
		}
		
		super.initQuery();
	}
	
	
	public boolean needSetRange(){
		return this.firstResult>=0 && this.maxResults!=-1;
	}

	public Integer getFirstResult() {
		return firstResult;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}



	public ExtQuery build() {
		String fname = "build ext query";
		if(isDebug())
			UtilTimerStack.push(fname);
		this.buildSelect().buildJoin().buildOrderBy();
		sql = new StringBuilder();
		sql.append(select);
		if (join != null)
			sql.append(join);
		if (params.isEmpty()) {
			if (orderBy != null)
				sql.append(orderBy);
			return this;
		}
		
		this.buildWhere();
		if(where!=null)
			sql.append(where);
		if (orderBy != null)
			sql.append(orderBy);

		if (isDebug()) {
			logger.info("generated sql : " + sql);
			logger.info("params : " + (Map) this.paramsValue.getValues());
		}

		if(isDebug())
			UtilTimerStack.pop(fname);
		
		this.hasBuilt = true;
		return this;
	}

	protected SelectExtQueryImpl buildSelect() {
		select = new StringBuilder();
		
		if(getParams().containsKey(K.SQL_SELECT)){
			Object rawSqlObj = getParams().get(K.SQL_SELECT);
			if(rawSqlObj==null)
				return this;
			if(!(rawSqlObj instanceof RawSqlWrapper)){
				LangUtils.throwBaseException("it must be a sqlwrapper : " + rawSqlObj);
			}
			RawSqlWrapper wrapper = (RawSqlWrapper)rawSqlObj;
			if(!wrapper.isBlank()){
				select.append(wrapper.getRawSql());
			}
			params.remove(K.SQL_SELECT);
			return this;
		}

		Object selectValule = null;
		select.append("select ");
		if(hasParams(K.DISTINCT)){
			select.append("distinct ");
		}

		String selectKey = K.SELECT;
		selectValule = params.get(selectKey);
		if(selectValule==null){
			selectKey = K.DISTINCT;
			selectValule = params.get(selectKey);
		}
		if (selectValule != null) {
			params.remove(selectKey);
			Object[] selectList = null;
			if(selectValule instanceof String){
				selectList = StringUtils.split(selectValule.toString(), ",");
			}else if(selectValule.getClass().isArray()){
				selectList = (String[])selectValule;
			}else{
				selectList = (Object[])LangUtils.asList(selectValule).toArray();
			}
			for (int i = 0; i < selectList.length; i++) {
				if (i != 0)
					select.append(", ");

				select.append(getSelectFieldName(selectList[i].toString()));
				/*if(this.alias.equals(selectList[i]))
					select.append(selectList[i]);
				else
					select.append(getFieldName(selectList[i].toString()));*/
			}
			select.append(" ");
		} else if(StringUtils.isNotBlank(countValue)){
			select.append("count(").append(countValue).append(") ");
		} else {
			select.append(getDefaultSelectFields(entityClass, this.alias)).append(" ");
		}
		
		select.append("from ").append(getFromName(entityClass)).append(" ").append(this.alias).append(" ");
		return this;
	}

	
	public String getSelectFieldName(String f) {
		if(this.alias.equals(f)){
			return f;
		}else{
			return appendAlias(translateAt(f));
		}
	}
	
	
	protected String getDefaultSelectFields(Class<?> entityClass, String alias){
		return alias;
	}

	protected SelectExtQueryImpl buildJoin() {
		join = new StringBuilder();
		/*buildJoin(join, K.JOIN_FETCH, false);//inner
		buildJoin(join, K.FETCH, false);
		buildJoin(join, K.JOIN, false);
		buildJoin(join, K.LEFT_JOIN, false);//outer
		buildJoin(join, K.JOIN_IN, true);*/
		
		for(String key : K.JOIN_MAP.keySet()){
			if(K.JOIN_IN.equals(key)){
				buildJoin(join, key, true);
			}else if(K.SQL_JOIN.equals(key)){
				Object rawSqlObj = this.getParams().get(key);
				if(rawSqlObj==null)
					return this;
				if(!(rawSqlObj instanceof RawSqlWrapper))
					LangUtils.throwBaseException("it must a sql wrapper : " + rawSqlObj);
				RawSqlWrapper wrap = (RawSqlWrapper) rawSqlObj;
				if(!wrap.isBlank()){
					join.append(wrap.getRawSql()).append(" ");
				}
				getParams().remove(K.SQL_JOIN);
			}else{
				buildJoin(join, key, false);
			}
		}
		return this;
	}

	protected SelectExtQueryImpl buildJoin(StringBuilder joinBuf, String joinKey, boolean hasParentheses) {
		if (!hasParams(joinKey))
			return this;
		String joinWord = K.JOIN_MAP.get(joinKey);
		Object value = this.getParams().get(joinKey);
		List<String> fjoin = MyUtils.asList(value);
		if(fjoin==null)
			return this;
		
		// int index = 0;
		boolean hasComma = K.JOIN_IN.equals(joinKey);
		for (String j : fjoin) {
			String[] jstrs = StringUtils.split(j, ":");
			if(hasComma){
				joinBuf.append(", ");
			}
			if(jstrs.length>1)//alias
				joinBuf.append(joinWord).append(hasParentheses?"(":" ").append(getFieldName(jstrs[0])).append(hasParentheses?") ":" ").append(jstrs[1]).append(" ");
			else
				joinBuf.append(joinWord).append(hasParentheses?"(":" ").append(getFieldName(j)).append(hasParentheses?") ":" ");
		}
		this.getParams().remove(joinKey);
		return this;
	}
	
	

	protected ExtQuery buildOrderBy() {
		orderBy = new StringBuilder();
		/*boolean hasAsc = buildOrderby0(K.ASC);
		boolean hasDes = buildOrderby0(K.DESC);*/
		
		boolean hasOrderBy = false;
		List<String> orderbys = new ArrayList<String>(3);
		for(Map.Entry entry : (Set<Map.Entry>)this.params.entrySet()){
			if(K.ORDER_BY_MAP.containsKey(entry.getKey())){
				orderbys.add((String)entry.getKey());
			}
		}
		for(String order : orderbys){
			if(buildOrderby0(order))
				hasOrderBy = true;
		}
		
		if (!hasOrderBy) {
			this.buildDefaultOrderBy();
		}
		 
		return this;
	}
	
	protected void buildDefaultOrderBy(){
		if (!subQuery) {
			String sortField = getDefaultOrderByFieldName();
			if(StringUtils.isNotBlank(sortField))
				orderBy.append("order by ").append(getFieldName(sortField)).append(" desc "); 
		}
	}
	
	protected String getDefaultOrderByFieldName(){
//		return "id";
		return "";
	}
	
	protected boolean buildOrderby0(String order){
		boolean hasOrderBy = false;
		if(!params.keySet().contains(order))
			return false;
		
		Object ascValue = params.remove(order);
		if(ascValue==null)
			return false;
		
		Object orderValue = K.getMappedValue(order);
		Object[] orderList = null;
		if(LangUtils.isMultiple(ascValue))
			orderList = (Object[]) LangUtils.asList(ascValue).toArray();
		else
			orderList = StringUtils.split(ascValue.toString(), ",");
		String orderField = null;

		if(orderBy==null || orderBy.length()<1){
			orderBy.append("order by ");
		}else{
			orderBy.append(", ");
		}
		for (int i = 0; i < orderList.length; i++) {
			orderField = orderList[i].toString().trim();
			if (i == 0) {
				hasOrderBy = true;
			} else{
				orderBy.append(", ");
			}
			int oIndex = orderField.indexOf(':');
			if(oIndex==-1){
				if(K.ORDERBY.equals(order))
					orderBy.append(orderField).append(orderValue);
				else
					orderBy.append(getFieldName(orderField)).append(orderValue);
			}else{
				String f = orderField.substring(0, oIndex);
				String nullsOrder = orderField.substring(oIndex+1);
				nullsOrder = symbolManager.getSqlDialet().getNullsOrderby(nullsOrder);
				orderBy.append(getFieldName(f)).append(" ").append(nullsOrder).append(" ").append(orderValue);
			}
		}
		return hasOrderBy;
	}

	public StringBuilder getSelect() {
		return select;
	}


	public StringBuilder getOrderBy() {
		return orderBy;
	}
	

	protected String buildCountSql(String sql){
		String hql = sql;
		String countField = getDefaultField("id");

		if(hql.indexOf("{")!=-1 || hql.indexOf("}")!=-1)
			hql = hql.replace("{", "").replace("}", "");
		
		if(hql.indexOf(" group by ")!=-1){
//			int index = countField.lastIndexOf('.');
//			countField = countField.substring(index+1);
			if(StringUtils.isNotBlank(countValue)){
				countField = countValue;
			}else{
				countField = "count_entity." + countField;
			}
			hql = "select count("+countField+") from (" + hql + ") count_entity ";
		}else{
			hql = StringUtils.substringAfter(hql, "from ");
			hql = StringUtils.substringBefore(hql, " order by ");

			if(StringUtils.isNotBlank(countValue))
				countField = countValue;
			
			hql = "select count(" + countField + ") from " + hql;
			
			/*if(StringUtils.isNotBlank(countValue))
				countField = countValue;
			hql = ExtQueryUtils.buildCountSql(hql, countField);*/
		}
		return hql;
	}
	
	protected String getDefaultField(String name){
		String countName = getFieldName(name);
		if(StringUtils.isBlank(countName)){
			countName = "*";
		}
		return countName;
	}

	public String getCountSql() {
//		String countSql = MyUtils.getCountSql(sql.toString(), getFieldName("id"));
		String countSql = buildCountSql(sql.toString());
		if (isDebug()) {
			logger.info("generated count sql : " + countSql);
			logger.info("params : " + (Map) this.paramsValue.getValues());
		}
		return countSql;
	}

	public boolean isSubQuery() {
		return subQuery;
	}

	public void setSubQuery(boolean subQuery) {
		this.subQuery = subQuery;
	}

	public Map<String, Object> getQueryConfig() {
		if(queryConfig==null){
			return Collections.EMPTY_MAP;
		}
		return queryConfig;
	}


	public static void main(String[] args) {

		Map<Object, Object> properties = new LinkedHashMap<Object, Object>();

		properties.put("&LOWER(name)", "way");
		properties.put("&substring(name, 5, 1)", "w");

		ExtQuery q = SQLSymbolManagerFactory.getInstance().getJPA().createSelectQuery(Object.class, "mag", properties);
		q.build();
		
	}
}