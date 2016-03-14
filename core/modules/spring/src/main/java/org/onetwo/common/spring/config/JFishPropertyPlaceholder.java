package org.onetwo.common.spring.config;

import java.util.Properties;

import org.onetwo.common.propconf.JFishProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/****
 * 替换spring的PropertyPlaceholder,通过JFishProfiles创建
 * @author way
 *
 */
public class JFishPropertyPlaceholder extends PropertyPlaceholderConfigurer {
//	private Properties mergedConfig;
	private JFishProperties mergedConfigWrapper;
	
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		Properties mergedConfig  = new Properties();
		mergedConfig.putAll(props);
		
		this.mergedConfigWrapper = new JFishProperties(mergedConfig);
	}

	public Properties getMergedConfig() {
		return mergedConfigWrapper;
	}

	public JFishProperties getPropertiesWraper() {
		return mergedConfigWrapper;
	}
	
}
