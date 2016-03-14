package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.db.BaseEntityManager;
import org.onetwo.common.db.filequery.QueryProvideManager;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryProvider {

	public String value() default "";
	public Class<? extends QueryProvideManager> beanClass() default BaseEntityManager.class;
	
	
}
