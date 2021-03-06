package org.onetwo.plugins.permission.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.web.s2.security.Authenticator;
import org.onetwo.common.web.s2.security.config.annotation.Authentic;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Authentic
public @interface ByMenuClass {

	Class<?>[] codeClass();
	Class<? extends Authenticator>[] authenticatorClass() default {};
	String redirect() default Authentic.DEFAULT_REDIRECT;
}
