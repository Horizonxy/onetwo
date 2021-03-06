package org.onetwo.plugins.security.client;

import org.onetwo.common.utils.StringUtils;
import org.onetwo.common.web.config.BaseSiteConfig;
import org.onetwo.common.web.view.jsp.TagUtils;
import org.onetwo.plugins.security.sso.SsoConfig;
import org.onetwo.plugins.security.utils.SecurityPluginUtils;

@SuppressWarnings("serial")
public class SsoClientConfig extends SsoConfig {

	@Override
	public boolean isSso() {
		return true;
	}
	@Override
	public boolean isSsoServer() {
		return false;
	}
	@Override
	public boolean isSsoClient() {
		return true;
	}
	public String getServerUrl(){
		return getAndThrowIfEmpty("server.url");
	}
	
	private boolean isCheckAll(){
		return getBoolean("check.all", false);
	}
	/***
	 * 服务器端登录地址
	 */
	public String getServerLoginUrl(){
		String url = getProperty(SERVER_LOGIN_URL);
		if(StringUtils.isBlank(url)){
			url = getServerUrl()+"/login";
			url = TagUtils.appendParam(url, SecurityPluginUtils.LOGIN_PARAM_CLIENT_CODE, getClientCode());
			url = TagUtils.appendParam(url, SecurityPluginUtils.LOGIN_PARAM_ALL, String.valueOf(isCheckAll()));
		}
		return url;
	}
	public String getServerLogoutUrl(){
		String url = getProperty(SERVER_LOGOUT_URL);
		if(StringUtils.isBlank(url)){
			url = getServerUrl()+"/logout";
			url = TagUtils.appendParam(url, SecurityPluginUtils.LOGIN_PARAM_CLIENT_CODE, getClientCode());
			url = TagUtils.appendParam(url, SecurityPluginUtils.LOGIN_PARAM_ALL, String.valueOf(isCheckAll()));
		}
		return url;
	}
	public String getSSOUserServiceUrl(){
		String url = getProperty("service.url");
		if(StringUtils.isBlank(url)){
			url = SecurityPluginUtils.getSsoUserServiceExporterDefaultUrl(getServerUrl());
		}
		return url;
	}
	
	public String getClientCode(){
		String clientCode = getProperty("client.code");
		if(StringUtils.isBlank(clientCode)){
			clientCode = BaseSiteConfig.getInstance().getAppCode();
		}
		return clientCode;
	}
}
