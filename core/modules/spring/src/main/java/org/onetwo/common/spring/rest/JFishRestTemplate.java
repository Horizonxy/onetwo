package org.onetwo.common.spring.rest;

import org.onetwo.common.jackson.JsonMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class JFishRestTemplate extends RestTemplate {
	

	public JFishRestTemplate(){
		this(null);
	}
	public JFishRestTemplate(ClientHttpRequestFactory requestFactory){
		super();
		for(HttpMessageConverter<?> converter : this.getMessageConverters()){
			if(MappingJackson2HttpMessageConverter.class.isInstance(converter)){
				((MappingJackson2HttpMessageConverter)converter).setObjectMapper(JsonMapper.IGNORE_NULL.getObjectMapper());
				break;
			}
			
		}
		if(requestFactory!=null){
			this.setRequestFactory(requestFactory);
//			this.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		}
	}

	public <T> T post(String url, Object request, Class<T> responseType){
		ResponseEntity<T> response = postForEntity(url, RestUtils.createFormEntity(request), responseType);
		if(HttpStatus.OK.equals(response.getStatusCode())){
			return response.getBody();
		}
		throw new RestClientException("invoke rest interface["+url+"] error: " + response);
	}

	public <T> T get(String url, Class<T> responseType, Object...urlVariables){
		ResponseEntity<T> response = getForEntity(url, responseType, urlVariables);
		if(HttpStatus.OK.equals(response.getStatusCode())){
			return response.getBody();
		}
		throw new RestClientException("invoke rest interface["+url+"] error: " + response);
	}

}
