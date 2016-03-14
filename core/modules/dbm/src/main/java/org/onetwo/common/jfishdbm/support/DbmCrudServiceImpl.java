package org.onetwo.common.jfishdbm.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.onetwo.common.db.BaseCrudServiceImpl;
import org.onetwo.common.exception.BusinessException;
import org.onetwo.common.jfishdbm.mapping.JFishMappedEntry;
import org.onetwo.common.jfishdbm.query.JFishQueryBuilder;
import org.onetwo.common.spring.SpringApplication;
import org.onetwo.common.spring.validator.ValidationBindingResult;
import org.onetwo.common.utils.Page;
import org.springframework.transaction.annotation.Transactional;

abstract public class DbmCrudServiceImpl<T, PK extends Serializable> extends BaseCrudServiceImpl<T, PK> {

	private DbmEntityManager baseEntityManager;
	
	public DbmCrudServiceImpl(){
	}
	
	protected String serviceQName(String methodName){
		return this.getClass().getSimpleName() + "." + methodName;
	}
	@Resource
	public void setBaseEntityManager(DbmEntityManager baseEntityManager) {
		this.baseEntityManager = baseEntityManager;
	}

	@Override
	public DbmEntityManager getBaseEntityManager() {
		return baseEntityManager;
	}

	@Override
	@Transactional(readOnly=true)
	public T findById(PK id) {
		return super.findById(id);
	}
	
	@Override
	@Transactional(readOnly=true)
	public Number countRecord(Map<Object, Object> properties) {
		return super.countRecord(properties);
	}

	@Override
	@Transactional(readOnly=true)
	public Number countRecord(Object... params) {
		return super.countRecord(params);
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> findAll() {
		return super.findAll();
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> findByProperties(Map<Object, Object> properties) {
		return super.findByProperties(properties);
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> findByProperties(Object... properties) {
		return super.findByProperties(properties);
	}

	@Override
	@Transactional(readOnly=true)
	public Page<T> findPage(Page<T> page, Map<Object, Object> properties) {
		return super.findPage(page, properties);
	}

	@Override
	@Transactional(readOnly=true)
	public Page<T> findPage(Page<T> page, Object... properties) {
		return super.findPage(page, properties);
	}

	@Override
	@Transactional(readOnly=true)
	public T load(PK id) {
		return super.load(id);
	}

	@Override
	@Transactional
	public T remove(T entity) {
		return super.remove(entity);
	}
	
	@Override
	@Transactional
	public void removes(Collection<T> entities) {
		super.removes(entities);
	}

	@Override
	@Transactional
	public T removeById(PK id) {
		return super.removeById(id);
	}

	@Override
	@Transactional
	public T save(T entity) {
		return super.save(entity);
	}

	/*@Override
	@Transactional
	public void persist(Object entity) {
		super.persist(entity);
	}*/

	@Override
	@Transactional(readOnly=true)
	public T findUnique(Map<Object, Object> properties) {
		return super.findUnique(properties);
	}

	@Override
	@Transactional(readOnly=true)
	public T findUnique(Object... properties) {
		return super.findUnique(properties);
	}
	
	@Transactional
	public void removeAll(){
		baseEntityManager.removeAll(entityClass);
	}
	
	protected ValidationBindingResult validate(Object obj, Class<?>... groups){
		ValidationBindingResult validations = SpringApplication.getInstance().getValidator().validate(obj);
		return validations;
	}
	
	protected void validateAndThrow(Object obj, Class<?>... groups) throws BusinessException{
		ValidationBindingResult validations = validate(obj, groups);
		if(validations.hasErrors()){
			throw new ValidationException(validations.getFieldErrorMessagesAsString());
		}
	}

	
	protected JFishMappedEntry getMappedEntry(){
		return this.getBaseEntityManager().getDbmDao().getMappedEntryManager().getEntry(entityClass);
	}
	
	protected JFishQueryBuilder createQueryBuilder(){
		return this.getBaseEntityManager().createQueryBuilder(entityClass);
	}
}
