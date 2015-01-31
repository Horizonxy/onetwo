package org.onetwo.app.taskservice;

import org.onetwo.app.taskservice.service.impl.TaskQueueServiceImpl;
import org.onetwo.common.spring.cache.JFishSimpleCacheManagerImpl;
import org.onetwo.common.spring.config.JFishProfile;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@JFishProfile
@ImportResource("classpath:applicationContext.xml")
@ComponentScan(basePackageClasses=TaskService.class)
public class TasksysContextConfig {
	
	@Bean
	public TaskServerConfig taskServerConfig(){
		return TaskServerConfig.getInstance();
	}
	
	@Bean
	public CacheManager cacheManager() {
		JFishSimpleCacheManagerImpl cache = new JFishSimpleCacheManagerImpl();
		return cache;
	}
	
	@Bean
	public TaskQueueServiceImpl taskQueueService(){
		return new TaskQueueServiceImpl();
	}
}
