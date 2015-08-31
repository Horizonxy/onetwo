package org.onetwo.common.db.filequery;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;

public class JFishNamedFileQueryInfo extends NamespaceProperty {
	public static final String COUNT_POSTFIX = "-count";
	public static final String FRAGMENT_KEY = "fragment";
//	public static final String MATCHER_KEY = "matcher";
//	public static final String MATCHER_SPIT_KEY = "|";
	public static final String PROPERTY_KEY = "property";
	public static final String NAME_KEY = "name";
	public static final String FRAGMENT_DOT_KEY = FRAGMENT_KEY + DOT_KEY;

	public static boolean isCountName(String name){
		return name.endsWith(COUNT_POSTFIX);
	}
	public static String trimCountPostfix(String name){
		if(!isCountName(name))
			return name;
		return name.substring(0, name.length() - COUNT_POSTFIX.length());
	}

//	private DataBase dataBaseType;
	private String mappedEntity;
	private String countSql;
	private FileSqlParserType parser = FileSqlParserType.NONE;
	
	
	private Class<?> mappedEntityClass;
	private boolean autoGeneratedCountSql = true;
	
	final private Map<String, String> fragment = LangUtils.newHashMap();
	private List<Object> matchers = Collections.EMPTY_LIST;

	private boolean hql;

	public String getSql() {
		return getValue();
	}
	
	public String getCountName(){
		return getFullName() + COUNT_POSTFIX;
	}

	public void setSql(String sql) {
		this.setValue(sql);
	}

	public String getMappedEntity() {
		return mappedEntity;
	}

	public Class<?> getMappedEntityClass() {
		return mappedEntityClass;
	}

	public void setMappedEntity(String mappedEntity) {
		this.mappedEntity = mappedEntity;
		if(StringUtils.isNotBlank(mappedEntity)){
			this.mappedEntityClass = ReflectUtils.loadClass(mappedEntity);
		}
	}

	public String getCountSql() {
		if(!autoGeneratedCountSql){
			return countSql;
		}else{
//			throw new BaseException("countSql is null, and you shoud generated it by sql.");
			return ExtQueryUtils.buildCountSql(getSql(), null);
		}
	}

	protected String getCountSqlString() {
		if(!autoGeneratedCountSql){
			return countSql;
		}else{
			return "";
		}
	}

	public boolean isAutoGeneratedCountSql() {
		return autoGeneratedCountSql;
	}
	/*public String getCountSql2() {
		if(StringUtils.isBlank(countSql)){
			this.countSql = ExtQueryUtils.buildCountSql(this.getSql(), "");
		}
		return countSql;
	}*/

	public void setCountSql(String countSql) {
		autoGeneratedCountSql = false;
		this.countSql = countSql;
	}

	public boolean isIgnoreNull() {
		return parser==FileSqlParserType.IGNORENULL;
	}

	
	public FileSqlParserType getFileSqlParserType() {
		return parser;
	}

	public void setParser(String parser) {
		this.parser = FileSqlParserType.valueOf(parser.trim().toUpperCase());
	}
	
/*
	public boolean isNeedParseSql(){
		return isIgnoreNull();
	}*/

	public boolean isHql() {
		return hql;
	}
	public void setHql(boolean hql) {
		this.hql = hql;
	}
	public Map<String, String> getFragment() {
		return fragment;
	}
	public String getFragmentTemplateName(String attr){
		return getFullName() + DOT_KEY + FRAGMENT_DOT_KEY + attr;
	}
	
	public List<Object> getMatchers() {
		return matchers;
	}
	public void setMatchers(List<Object> matchers) {
		this.matchers = matchers;
	}
	/*public DataBase getDataBaseType() {
		return dataBaseType;
	}
	public void setDataBaseType(DataBase dataBaseType) {
		this.dataBaseType = dataBaseType;
	}*/
	public String toString() {
		return LangUtils.append("{namespace:, ", getNamespace(), ", name:", getName(), ", config:", getConfig(), ", mappedEntity:", mappedEntity, ", sql:", getSql(), ", countSql:", getCountSqlString(), "}");
	}
}