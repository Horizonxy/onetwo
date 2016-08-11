package org.onetwo.common.jfishdbm.spring;

import org.onetwo.common.jfishdbm.support.DbmDaoImplementor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class DbmDaoCreateEvent extends ApplicationEvent {

	private BeanDefinitionRegistry registry;
	
	public DbmDaoCreateEvent(Object source, BeanDefinitionRegistry registry) {
		super(source);
		this.registry = registry;
	}

	public DbmDaoImplementor getDaoImplementor(){
		return (DbmDaoImplementor)super.getSource();
	}

	public BeanDefinitionRegistry getRegistry() {
		return registry;
	}

}
