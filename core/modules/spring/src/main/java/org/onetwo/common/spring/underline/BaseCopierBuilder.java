package org.onetwo.common.spring.underline;

import java.util.Arrays;
import java.util.List;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.LangUtils;

import com.google.common.collect.Lists;



@SuppressWarnings("unchecked")
abstract public class BaseCopierBuilder<B extends BaseCopierBuilder<B>> {

	private CompositePropertyFilter<?> propertyFilters;
//	private CompositePropertyFilter<?> currentFilters;
	private PropertyNameConvertor propertyNameConvertor;
	private List<String> ignoreFields;
	private List<String> includeFields;

	public BaseCopierBuilder(){
		andMode();
	}

	final public B orMode(){
		this.checkMode();
		this.propertyFilters = new OrCompositePropertyFilter();
//		or();
		return (B)this;
	}
	final public B andMode(){
		this.checkMode();
		this.propertyFilters = new AndCompositePropertyFilter();
//		and();
		return (B)this;
	}

	/*final public B or(){
		this.currentFilters = new OrCompositePropertyFilter();
		this.propertyFilters.add(currentFilters);
		return (B)this;
	}

	final public B and(){
		this.currentFilters = new AndCompositePropertyFilter();
		this.propertyFilters.add(currentFilters);
		return (B)this;
	}*/
	
	private void checkMode(){
		if(propertyFilters!=null && !this.propertyFilters.isEmpty()){
			throw new BaseException("invoke model before add any fileter!");
		}
	}
	
	/****
	 * 添加filter
	 * 最终会组合这些filter，所有filter都通过才会被copy
	 * @param propertyFilter
	 * @return
	 */
	final public B filter(PropertyFilter propertyFilter){
//		this.checkPropertyFilterNotNull();
		this.propertyFilters.add(propertyFilter);
		return (B)this;
	}

	final public B ignoreNullValue(){
		return filter(SimplePropertyFilters.IGNORE_NULL);
	}

	final public B ignoreBlankString(){
		return filter(SimplePropertyFilters.IGNORE_BLANK_STRING);
	}

	final public B ignoreFields(String...fieldNames){
		if(ignoreFields==null){
			ignoreFields = Lists.newArrayList();
		}
		ignoreFields.addAll(Arrays.asList(fieldNames));
		return getSelf();
		/*return filter((prop, fromValue)->{
			System.out.println("fieldNames:"+LangUtils.toString(fieldNames) + ", rs:" + !ArrayUtils.contains(fieldNames, prop.getName()));
			return !ArrayUtils.contains(fieldNames, prop.getName());
		});*/
	}
	
	protected B getSelf(){
		return (B)this;
	}

	final public B includeFields(String...fieldNames){
		if(includeFields==null){
			includeFields = Lists.newArrayList();
		}
		includeFields.addAll(Arrays.asList(fieldNames));
		return getSelf();
	}

	final public B clearFilters(){
		propertyFilters.clear();
		return (B)this;
	}

	final public B propertyNameConvertor(PropertyNameConvertor propertyNameConvertor){
		this.checkPropertyNameConvertorNotNull();
		this.propertyNameConvertor = propertyNameConvertor;
		return (B)this;
	}

	final public B nameWithSeperator(String seperator){
		this.checkPropertyNameConvertorNotNull();
		this.propertyNameConvertor = new SeperatorNamedConvertor(seperator);
		return (B)this;
	}
	
	private void checkPropertyNameConvertorNotNull(){
		if(this.propertyNameConvertor!=null)
			throw new BaseException("propertyNameConvertor is not null, you can't override it a not null PropertyNameConvertor!");
	}
	
	/*private void checkPropertyFilterNotNull(){
		if(this.propertyFilter!=null)
			throw new BaseException("propertyFilter is not null, you can't override it a not null propertyFilter!");
	}*/

	final public B nameWithUnderline(){
		this.checkPropertyNameConvertorNotNull();
		this.propertyNameConvertor = SeperatorNamedConvertor.UNDERLINE_CONVERTOR;
		return (B)this;
	}
	
	protected SimpleBeanCopier newCopier(){
		if(LangUtils.isNotEmpty(ignoreFields)){
			this.propertyFilters.add((prop, fromValue)->!ignoreFields.contains(prop.getName()));
		}
		if(LangUtils.isNotEmpty(includeFields)){
			this.propertyFilters.add((prop, fromValue)->includeFields.contains(prop.getName()));
		}
		SimpleBeanCopier copier = new SimpleBeanCopier();
		copier.setPropertyFilter(this.propertyFilters);
		copier.setPropertyNameConvertor(getPropertyNameConvertor());
		return copier;
	}
	
	/*
	protected PropertyFilter getPropertyFilter() {
		return propertyFilter;
	}*/

	protected PropertyNameConvertor getPropertyNameConvertor() {
		return propertyNameConvertor;
	}

	public static class SimpleCopierBuilder<T, B extends SimpleCopierBuilder<T, B>> extends BaseCopierBuilder<SimpleCopierBuilder<T, B>>{
		
		private T fromObject;

		public B copy(T from){
			this.fromObject = from;
			return (B)this;
		}
		/*public T to(Class<T> targetClass){
			T targetObject = ReflectUtils.newInstance(targetClass);
			to(targetObject);
			return targetObject;
		}*/
		public T toNewInstance(){
			T targetObject = (T)ReflectUtils.newInstance(fromObject.getClass());
			to(targetObject);
			return targetObject;
		}
		public <E> E toClass(Class<E> targetClass){
			E targetObject = ReflectUtils.newInstance(targetClass);
			newCopier().fromObject(fromObject, targetObject);
			return targetObject;
		}
		
		public void to(T target){
			newCopier().fromObject(fromObject, target);
		}

	}

}
