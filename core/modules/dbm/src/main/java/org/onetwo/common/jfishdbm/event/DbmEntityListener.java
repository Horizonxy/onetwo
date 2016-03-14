package org.onetwo.common.jfishdbm.event;

public interface DbmEntityListener {
	
	public void beforeInsert(Object entity);
	
	public void afterInsert(Object entity);
	
	public void beforeUpdate(Object entity);
	
	public void afterUpdate(Object entity);

}
