package org.onetwo.ext.permission.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.ext.permission.parser.MenuInfoParser;

/****
 * 一般用于子模块映射到根菜单目录
 * RootMenu{
 * }
 * 
 * {@code @MenuMapping}(parent=RootMenu)
 * SubModuleMenu {
 * 	   Menu1{
 * 		   //generate code: RootMenu_SubModuleMenu_Menu1
 * 	   }
 * }
 * @author way
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MenuMapping {

//	String code();
	Class<?> parent() default MenuInfoParser.class;
	
}
