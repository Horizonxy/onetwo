package org.onetwo.plugins.dq;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;
import org.onetwo.common.db.DataQuery;
import org.onetwo.common.db.ExtQuery.K.IfNull;
import org.onetwo.common.db.ExtQueryUtils;
import org.onetwo.common.db.QueryConfigData;
import org.onetwo.common.db.QueryContextVariable;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.proxy.AbstractMethodResolver;
import org.onetwo.common.proxy.BaseMethodParameter;
import org.onetwo.common.spring.sql.JNamedQueryKey;
import org.onetwo.common.spring.sql.ParsedSqlUtils;
import org.onetwo.common.spring.sql.ParserContext;
import org.onetwo.common.spring.sql.ParserContextFunctionSet;
import org.onetwo.common.utils.AnnotationUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.Langs;
import org.onetwo.common.utils.Page;
import org.onetwo.common.utils.ReflectUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.plugins.dq.DynamicMethod.DynamicMethodParameter;
import org.onetwo.plugins.dq.annotations.BatchObject;
import org.onetwo.plugins.dq.annotations.ExecuteUpdate;
import org.onetwo.plugins.dq.annotations.Name;
import org.onetwo.plugins.dq.annotations.QueryConfig;
import org.springframework.core.MethodParameter;

public class DynamicMethod extends AbstractMethodResolver<DynamicMethodParameter>{

	public static final List<String> EXECUTE_UPDATE_PREFIX = LangUtils.newArrayList("save", "update", "remove", "delete", "insert", "create");
	public static final List<String> BATCH_PREFIX = LangUtils.newArrayList("batch");
//	public static final String FIELD_NAME_SPERATOR = "By";
	
//	private final Method method;
//	private final List<DynamicMethodParameter> parameters;
	private final Class<?> resultClass;
	private final Class<?> componentClass;
	private final String queryName;
//	private final ExecuteUpdate executeUpdate;
	private boolean update;
	private boolean batchUpdate;
//	private List<String> parameterNames;
	
	public DynamicMethod(Method method){
		super(method);
		
		this.setupExecuteType();
		
		queryName = method.getDeclaringClass().getName()+"."+method.getName();
		Class<?> rClass = method.getReturnType();
		Class<?> compClass = ReflectUtils.getGenricType(method.getGenericReturnType(), 0);
		if(rClass==void.class){
			rClass = parameters.get(0).getParameterType();
//			rClass = parameters.remove(0).getParameterType();
			//如果返回类型为空，看第一个参数是否是page对象
			if(Page.class != rClass){
				throw new BaseException("method no return type, the first arg of method must be a Page object: " + method.toGenericString());
			}
			DynamicMethodParameter pageParamter = this.parameters.get(0);
			if(pageParamter.getParameterAnnotation(Name.class)==null)//如果page对象没有name注解，移除它
				this.parameters.remove(0);
			Type ptype = pageParamter.getGenericParameterType();
			if(ptype instanceof ParameterizedType){
				compClass = ReflectUtils.getGenricType(ptype, 0);
			}
		}else if(Page.class==rClass){
			rClass = parameters.get(0).getParameterType();
//			rClass = parameters.remove(0).getParameterType();
			if(Page.class == rClass){
				throw new BaseException("method has return Page object, the first arg can not return the Page object: " + method.toGenericString());
			}
		}else if(DataQuery.class==rClass){
			compClass = null;
		}
		
		resultClass = rClass;
		if(compClass==Object.class)
			compClass = resultClass;
		this.componentClass = compClass;
		
		LangUtils.println("resultClass: ${0}, componentClass:${1}", resultClass, compClass);
	}
	
	@Override
	protected DynamicMethodParameter createMethodParameter(Method method, int parameterIndex, Parameter parameter) {
		return new DynamicMethodParameterJ8(method, parameterIndex, parameter);
	}

	
	protected boolean judgeBatchUpdateFromParameterObjects(List<DynamicMethodParameter> mparameters){
		for(DynamicMethodParameter mp : mparameters){
			if(mp.hasParameterAnnotation(BatchObject.class)){
				return true;
			}
		}
		return false;
	}
	
	final protected void setupExecuteType(){
		ExecuteUpdate executeUpdate = method.getAnnotation(ExecuteUpdate.class);
		if(executeUpdate!=null){
			this.update = true;
			this.batchUpdate = executeUpdate.isBatch();
		}else{
			this.update = EXECUTE_UPDATE_PREFIX.contains(StringUtils.getFirstWord(this.method.getName()));
			this.batchUpdate = BATCH_PREFIX.contains(StringUtils.getFirstWord(this.method.getName()));
		}
		if(!batchUpdate){
			this.batchUpdate = judgeBatchUpdateFromParameterObjects(parameters);
		}
	}
	
	public MethodParameter remove(int index){
		return this.parameters.remove(index);
	}
	
	private boolean addAndCheckParamValue(Name name, List<Object> values, String pname, Object pvalue){
		//TODO 参数ifParamNull已过时，将来注释下面这段代码
		IfNull ifnull = name.ifParamNull();
		if(pvalue==null){
			switch (ifnull) {
				case Ignore:
					return false;
				case Throw:
					throw new BaseException("param["+pname+"]' value must not be null");
				default:
					break;
			}
		}
		//end
		values.add(pname);
		if(String.class.isInstance(pvalue) && name.isLikeQuery()){
			values.add(ExtQueryUtils.getLikeString(pvalue.toString()));
		}else{
			values.add(pvalue);
		}
		return true;
	}
	

	public Object[] toArrayByArgs(Object[] args){
		Map<Object, Object> map = toMapByArgs(args);
		return Langs.toArray(map);
//		return toArrayByArgs2(args, componentClass);
	}

	/*public Object[] toArrayByArgs2(Object[] args, Class<?> componentClass){
		List<Object> values = LangUtils.newArrayList(parameters.size()*2);
		
		Object pvalue = null;
		ParserContext parserContext = ParserContext.create();
		for(DynamicMethodParameter mp : parameters){
			pvalue = args[mp.getParameterIndex()];
			if(pvalue instanceof ParserContext){
				parserContext.putAll((ParserContext) pvalue);
			}else if(mp.hasParameterAnnotation(Name.class)){
				Name name = mp.getParameterAnnotation(Name.class);
				if(name.renamedUseIndex()){
					List<?> listValue = LangUtils.asList(pvalue);
					int index = 0;
					//parem0, value0, param1, value1, ...
					for(Object obj : listValue){
						if(addAndCheckParamValue(name, values, mp.getParameterName()+index, obj)){
							index++;
						}
					}
					values.add(mp.getParameterName());
					values.add(listValue);
				}else{
					addAndCheckParamValue(name, values, mp.getParameterName(), pvalue);
				}
					
			}else{
				values.add(mp.getParameterName());
				values.add(pvalue);
			}
		}
		
		QueryConfig queryConfig = method.getAnnotation(QueryConfig.class);
		if(queryConfig!=null){
			QueryConfigData config = new QueryConfigData(queryConfig.stateful());
			config.setLikeQueryFields(Arrays.asList(queryConfig.likeQueryFields()));
			parserContext.setQueryConfig(config);
		}else{
			parserContext.setQueryConfig(QueryConfigData.EMPTY_CONFIG);
		}

		values.add(JNamedQueryKey.ParserContext);
		values.add(parserContext);
		if(componentClass!=null){
			values.add(JNamedQueryKey.ResultClass);
			values.add(componentClass);
		}
		return values.toArray();
	}*/
	
	private Pair<String, Object> addAndCheckParamValue(Name name, String pname, Object pvalue){
		//TODO 参数ifParamNull已过时，将来注释下面这段代码
		IfNull ifnull = name.ifParamNull();
		if(pvalue==null){
			switch (ifnull) {
				case Ignore:
					return null;
				case Throw:
					throw new BaseException("param["+pname+"]' value must not be null");
				default:
					break;
			}
		}
		//end
//		values.add(pname);
		Object val = null;
		if(String.class.isInstance(pvalue) && name.isLikeQuery()){
//			values.add(ExtQueryUtils.getLikeString(pvalue.toString()));
			val = ExtQueryUtils.getLikeString(pvalue.toString());
		}else{
//			values.add(pvalue);
			/*if(pvalue!=null && pvalue.getClass().isArray()){
				val = LangUtils.asList(pvalue);
			}else{
				val = pvalue;
			}*/
			val = pvalue;
		}
		return Pair.with(pname, val);
	}
	
	protected void handleArg(Map<Object, Object> values, ParserContext parserContext, DynamicMethodParameter mp, Object pvalue){
		if(pvalue instanceof ParserContext){
			parserContext.putAll((ParserContext) pvalue);
		}else if(mp.hasParameterAnnotation(Name.class)){
			Name name = mp.getParameterAnnotation(Name.class);
			if(name.renamedUseIndex()){
				List<?> listValue = LangUtils.asList(pvalue);
				int index = 0;
				//parem0, value0, param1, value1, ...
				for(Object obj : listValue){
					Pair<String, Object> pair = addAndCheckParamValue(name, mp.getParameterName()+index, obj);
					if(pair!=null){
						values.put(pair.getValue0(), pair.getValue1());
//						if(addAndCheckParamValue(name, values, mp.getParameterName()+index, obj)){
						index++;
					}
				}
				/*values.add(mp.getParameterName());
				values.add(listValue);*/
				values.put(mp.getParameterName(), listValue);
			}else{
//				addAndCheckParamValue(name, values, mp.getParameterName(), pvalue);
				/*if(mp.hasParameterAnnotation(BatchObject.class)){
					values.put(BatchObject.class, pvalue);
				}else{
					Pair<String, Object> pair = addAndCheckParamValue(name, mp.getParameterName(), pvalue);
					if(pair!=null){
						values.put(pair.getValue0(), pair.getValue1());
					}
				}*/
				Pair<String, Object> pair = addAndCheckParamValue(name, mp.getParameterName(), pvalue);
				if(pair!=null){
					values.put(pair.getValue0(), pair.getValue1());
				}
			}
				
		}else if(mp.hasParameterAnnotation(BatchObject.class)){
			values.put(BatchObject.class, pvalue);
		}else{
			/*values.add(mp.getParameterName());
			values.add(pvalue);*/
			values.put(mp.getParameterName(), pvalue);
		}
	}
	
	protected void buildQueryConfig(ParserContext parserContext){
		QueryConfig queryConfig = AnnotationUtils.findAnnotation(method, QueryConfig.class, true);//method.getAnnotation(QueryConfig.class);
		if(queryConfig!=null){
			QueryConfigData config = new QueryConfigData(queryConfig.stateful());
			config.setLikeQueryFields(Arrays.asList(queryConfig.likeQueryFields()));
			if(queryConfig.funcClass()==ParserContextFunctionSet.class){
				config.setVariables(ParserContextFunctionSet.getInstance());
			}else{
				QueryContextVariable func = (QueryContextVariable)ReflectUtils.newInstance(queryConfig.funcClass());
				config.setVariables(ParserContextFunctionSet.getInstance(), func);
			}
			parserContext.setQueryConfig(config);
			
		}else{
			parserContext.setQueryConfig(ParsedSqlUtils.EMPTY_CONFIG);
		}
	}

	public Map<Object, Object> toMapByArgs(Object[] args){
		Map<Object, Object> values = LangUtils.newHashMap(parameters.size());
		
		Object pvalue = null;
		ParserContext parserContext = ParserContext.create();
		for(DynamicMethodParameter mp : parameters){
			pvalue = args[mp.getParameterIndex()];
			handleArg(values, parserContext, mp, pvalue);
		}
		
		buildQueryConfig(parserContext);

		values.put(JNamedQueryKey.ParserContext, parserContext);
		if(componentClass!=null){
			values.put(JNamedQueryKey.ResultClass, componentClass);
		}
		return values;
	}
	
	public Method getMethod() {
		return method;
	}

	public List<DynamicMethodParameter> getParameters() {
		return parameters;
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public Class<?> getComponentClass() {
		return componentClass;
	}

	public String getQueryName() {
		return queryName;
	}
	
	public boolean isExecuteUpdate(){
		/*String name = StringUtils.getFirstWord(this.method.getName());
		return EXECUTE_UPDATE_PREFIX.contains(name);*/
		return update && !batchUpdate; //(executeUpdate!=null && !executeUpdate.isBatch()) || );
	}
	
	public boolean isBatch(){
		return batchUpdate;//(executeUpdate!=null && executeUpdate.isBatch()) || );
	}
	
	protected static class DynamicMethodParameter extends BaseMethodParameter {

		final protected String[] condidateParameterNames;
		final protected Name nameAnnotation;
		

		public DynamicMethodParameter(Method method, int parameterIndex) {
			this(method, parameterIndex, LangUtils.EMPTY_STRING_ARRAY);
		}
		
		public DynamicMethodParameter(Method method, int parameterIndex, String[] parameterNamesByMethodName) {
			super(method, parameterIndex);
			this.condidateParameterNames = parameterNamesByMethodName;
			nameAnnotation = getParameterAnnotation(Name.class);
		}

		/****
		 * 查询参数策略
		 * 如果有注解优先
		 * 其次是by分割符
		 * 以上皆否，则通过参数位置作为名称
		 */
		public String getParameterName() {
			Name name = getParameterAnnotation(Name.class);
			if(name!=null){
				return name.value();
			}else if(condidateParameterNames.length>getParameterIndex()){
				return StringUtils.uncapitalize(condidateParameterNames[getParameterIndex()]);
			}else{
				return String.valueOf(getParameterIndex());
			}
		}
		
	}
	
	/***
	 * for java8
	 * @author wayshall
	 *
	 */
	protected static class DynamicMethodParameterJ8 extends DynamicMethodParameter {

		public DynamicMethodParameterJ8(Method method, int parameterIndex, Parameter parameter) {
			super(method, parameterIndex, LangUtils.EMPTY_STRING_ARRAY);
		}

		@Override
		public String getParameterName() {
			Name name = getParameterAnnotation(Name.class);
			if(name!=null){
				return name.value();
			}else if(parameter!=null && parameter.isNamePresent()){
				return parameter.getName();
			}else{
				return String.valueOf(getParameterIndex());
			}
		}
	}
}
