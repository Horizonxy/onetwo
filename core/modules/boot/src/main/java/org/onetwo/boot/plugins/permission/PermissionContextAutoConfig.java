package org.onetwo.boot.plugins.permission;

import org.onetwo.boot.plugins.security.BootSecurityConfig;
import org.onetwo.ext.permission.AbstractPermissionConfig;
import org.onetwo.ext.permission.PermissionHandlerMappingListener;
import org.onetwo.ext.permission.PermissionManager;
import org.onetwo.ext.permission.entity.DefaultIPermission;
import org.onetwo.ext.permission.entity.PermisstionTreeModel;
import org.onetwo.ext.permission.parser.DefaultMenuInfoParser;
import org.onetwo.ext.permission.service.MenuItemRepository;
import org.onetwo.ext.permission.service.impl.DefaultMenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * 菜单权限管理
 * 需要：
 * 实现 {@linkplain AbstractPermissionConfig} 接口
 * 实现 {@linkplain PermissionManager} 接口
 * @author way
 *
 */
@Configuration
@ConditionalOnBean(AbstractPermissionConfig.class)
public class PermissionContextAutoConfig {
	
	@Autowired
	private BootSecurityConfig bootSecurityConfig;
	
	public PermissionContextAutoConfig(){
	}
	
	@Bean
	@Autowired
	public <T extends DefaultIPermission<T>> DefaultMenuInfoParser<T> menuInfoParser(AbstractPermissionConfig<T> permissionConfig){
		DefaultMenuInfoParser<T> parser = new DefaultMenuInfoParser<T>(permissionConfig);
		return parser;
	}
	
	@Bean
//	@ConditionalOnBean(AdminController.class)
	@ConditionalOnMissingBean(MenuItemRepository.class)
	public MenuItemRepository<PermisstionTreeModel> menuItemRepository(){
		DefaultMenuItemRepository menuItemRepository = new DefaultMenuItemRepository();
		return menuItemRepository;
	}
	
	@Bean
	@ConditionalOnMissingBean(value=PermissionHandlerMappingListener.class)
	public PermissionHandlerMappingListener permissionHandlerMappingListener(){
		PermissionHandlerMappingListener listener = new PermissionHandlerMappingListener();
		listener.setSyncPermissionData(bootSecurityConfig.getSyncPermissionData());
		return listener;
	}
	

}
