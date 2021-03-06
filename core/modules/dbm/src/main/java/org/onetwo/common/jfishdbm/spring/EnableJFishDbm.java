package org.onetwo.common.jfishdbm.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.db.dquery.DynamicQueryContextConfig;
import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({JFishdbmSpringConfiguration.class, DynamicQueryContextConfig.class})
public @interface EnableJFishDbm {

}
