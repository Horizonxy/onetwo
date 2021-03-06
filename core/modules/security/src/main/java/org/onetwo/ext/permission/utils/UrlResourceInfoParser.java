package org.onetwo.ext.permission.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.onetwo.common.expr.Expression;
import org.onetwo.common.expr.SimpleExpression;
import org.onetwo.common.expr.ValueProvider;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.StringUtils;
import org.springframework.util.Assert;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class UrlResourceInfoParser {
	
	private Expression pathParamExpression = new SimpleExpression("{", "}", new ValueProvider() {
		@Override
		public String findString(String var) {
			return "*";
		}
	});
	
	public List<UrlResourceInfo> parseToUrlResourceInfos(String source){
		if(StringUtils.isBlank(source))
			return Lists.newArrayList();
		
		Iterable<String> it = Splitter.on(",")
										.trimResults()
										.omitEmptyStrings()
										.split(source);
		
		return CUtils.iterableToList(it).stream()
				.map(str->parseToUrlResourceInfo(str))
				.collect(Collectors.toList());
	}
	
	private UrlResourceInfo parseToUrlResourceInfo(String source){
		Assert.hasText(source);
		Iterable<String> it = Splitter.on("|")
								.trimResults()
								.omitEmptyStrings()
								.split(source);
		List<String> strList = CUtils.iterableToList(it);
		if(strList.size()==1){
			return new UrlResourceInfo(strList.get(0));
		}else{
			return new UrlResourceInfo(strList.get(1), strList.get(0));//url, method
		}
	}
	
	public String parseToString(List<UrlResourceInfo> urlResourceInfo){
		if(urlResourceInfo==null)
			return "";
		
		List<String> list = urlResourceInfo.stream()
							.map(res->parseToString(res))
							.collect(Collectors.toList());
		return Joiner.on(", ").join(list);
	}

	private String parseToString(UrlResourceInfo info) {
		String url = pathParamExpression.parse(info.getUrl());
		if(StringUtils.isBlank(info.getMethod())){
			return url;
		}
		return info.getMethod() + "|" + appendStarPostfix(url);
	}

	private String appendStarPostfix(String url){
		url = StringUtils.trimEndWith(url, "/");
		if(!url.endsWith("*")){
			url += "*";
		}
		return url;
	}
	/*private String appendStarPostfix2(String url){
		url = StringUtils.trimEndWith(url, "/");
		if(!url.contains(".")){
			url += ".*";
		}
		return url;
	}*/

}
