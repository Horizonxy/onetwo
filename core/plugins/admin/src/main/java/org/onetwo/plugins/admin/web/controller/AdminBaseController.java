package org.onetwo.plugins.admin.web.controller;

import java.util.Map;

import org.onetwo.common.fish.plugin.PluginSupportedController;
import org.onetwo.common.spring.web.mvc.view.ControllerJsonFilter;
import org.onetwo.common.web.s2.security.config.annotation.Authentic;

@Authentic(checkTimeout=false)
abstract public class AdminBaseController extends PluginSupportedController implements ControllerJsonFilter {

	@Override
	public void filterModel(Map<String, Object> model) {
	}
}
