package org.onetwo.boot.plugins.security.config;

import org.onetwo.boot.plugins.security.DefaultUrlSecurityConfigurer;
import org.onetwo.boot.plugins.security.method.DefaultMethodSecurityConfigurer;
import org.onetwo.boot.plugins.security.method.JFishMethodSecurityMetadataSource;
import org.onetwo.boot.plugins.security.url.SecurityBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Import({RbacSecurityXmlContextConfigSupport.class})
public class UrlSecurityConfig {

	/***
	 * 如果不是基于方法拦截（即url匹配），需要用后处理器重新配置SecurityMetadataSource
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(JFishMethodSecurityMetadataSource.class)
	public SecurityBeanPostProcessor securityBeanPostProcessor(){
		return new SecurityBeanPostProcessor();
	}

	@Bean
	@ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
	public DefaultMethodSecurityConfigurer defaultSecurityConfigurer(){
		return new DefaultUrlSecurityConfigurer();
	}
}
