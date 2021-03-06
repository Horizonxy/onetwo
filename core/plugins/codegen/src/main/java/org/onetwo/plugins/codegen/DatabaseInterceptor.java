package org.onetwo.plugins.codegen;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onetwo.plugins.codegen.model.service.CodeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class DatabaseInterceptor implements HandlerInterceptor{

	
	@Autowired
	private CodeTemplateService templateService;

	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if(!templateService.isInitTemplate()){
//			ModelAndView mv = new ModelAndView("codegen-index");
//			throw new ModelAndViewDefiningException(mv);
			this.templateService.initCodegenTemplate();
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
