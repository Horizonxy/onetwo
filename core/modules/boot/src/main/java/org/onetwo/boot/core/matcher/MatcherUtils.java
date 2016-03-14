package org.onetwo.boot.core.matcher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.onetwo.boot.core.matcher.MutipleRequestMatcher.NotRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public final class MatcherUtils {

	public static MutipleRequestMatcher matchAntPaths(String...paths){
		List<RequestMatcher> matchers = Stream.of(paths).map(path->new AntPathRequestMatcher(path))
						.collect(Collectors.toList());
		MutipleRequestMatcher m = new MutipleRequestMatcher(matchers);
		return m;
	}
	
	public static RequestMatcher notMatchAntPaths(String...paths){
		RequestMatcher m = matchAntPaths(paths);
		return notMatcher(m);
	}
	
	public static RequestMatcher notMatcher(RequestMatcher matcher){
		return new NotRequestMatcher(matcher);
	}
	
	private MatcherUtils(){}
}
