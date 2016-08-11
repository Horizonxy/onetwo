package org.onetwo.common.rocketmq.consumer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.google.common.collect.Lists;

//@Component
public class RocketMQPushConsumerStarter implements InitializingBean, DisposableBean {

	private final Logger logger = LoggerFactory.getLogger(RocketMQPushConsumerStarter.class);

//	@Value("${jfish.rocketmq.namesrvAddr}")
	private String namesrvAddr;
	
	@Autowired
	private ApplicationContext applicationContext;
	private List<DefaultMQPushConsumer> defaultMQPushConsumers = Lists.newArrayList();
	

	public RocketMQPushConsumerStarter() {
		super();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void afterPropertiesSet() throws Exception {
		logger.debug("mq consumer init.");
		Assert.hasText(namesrvAddr, "namesrvAddr can not be empty!");

		Map<String, AppMQConsumer> consumerBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, AppMQConsumer.class);

		Map<ConsumerMeta, List<AppMQConsumer<Object>>> consumerGroups = consumerBeans.values()
																						.stream()
																						.collect(Collectors.groupingBy(c->c.getConsumerMeta()));
		consumerGroups.entrySet().forEach(e->{
			try {
				this.initializeConsumers(e.getKey(), e.getValue());
			} catch (MQClientException | InterruptedException ex) {
				logger.error("mq consumer initialize error: " + ex.getMessage(), ex);
			}
		});
		
	}


	private void initializeConsumers(ConsumerMeta meta, List<AppMQConsumer<Object>> consumers) throws InterruptedException, MQClientException {

		Assert.hasText(meta.getGroupName(), "consumerGroup can not be empty!");
		Assert.hasText(meta.getTopic(), "topic can not be empty!");
		logger.info("create mq consumergroup: {}", meta.getGroupName());
		consumers.forEach(c->{
			logger.info("consumer: {}", c.getConsumerMeta());
		});
		DefaultMQPushConsumer defaultMQPushConsumer = this.createAndConfigMQPushConsumer(meta);
		
		defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {

			// 默认msgs里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息
			@Override
			public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

				MessageExt msg = msgs.get(0);
				logger.info("receive id: {}, topic: {}, tag: {}", msg.getMsgId(),  msg.getTopic(), msg.getTags());

				try {
					consumers.stream().forEach(consumer->{
						Object body = consumer.convertMessage(msg);
						logger.info("consume id: {}, body: {}", msg.getMsgId(), body);
						consumer.doConsume(msg, body);
					});
				} catch (Exception e) {
					String errorMsg = "consume message error. id: "+msg.getMsgId()+", topic: "+msg.getTopic()+", tag: "+msg.getTags();
					logger.error(errorMsg, e);
					return ConsumeConcurrentlyStatus.RECONSUME_LATER;
				}
				logger.info("consume finish. id: {}, topic: {}, tag: {}", msg.getMsgId(),  msg.getTopic(), msg.getTags());

				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			}
		});

		defaultMQPushConsumer.start();
		logger.info("defaultMQPushConsumer[{}] start. consumers size: {}", meta.getGroupName(), consumers.size());
		defaultMQPushConsumers.add(defaultMQPushConsumer);
	}
	
	protected DefaultMQPushConsumer createAndConfigMQPushConsumer(ConsumerMeta meta) throws InterruptedException, MQClientException {
		DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(meta.getGroupName());
		defaultMQPushConsumer.setNamesrvAddr(namesrvAddr);
		defaultMQPushConsumer.setVipChannelEnabled(false);

		if(meta.getTags()!=null && !meta.getTags().isEmpty()){
			defaultMQPushConsumer.subscribe(meta.getTopic(), StringUtils.join(meta.getTags(), " || "));
		}else{
			defaultMQPushConsumer.subscribe(meta.getTopic(), null);
		}
		
		defaultMQPushConsumer.setConsumeFromWhere(meta.getConsumeFromWhere());
		defaultMQPushConsumer.setMessageModel(meta.getMessageModel());
		
		return defaultMQPushConsumer;
	}

	@Override
	public void destroy() {
		defaultMQPushConsumers.forEach(c->{
			c.shutdown();
		});
		logger.info("DefaultMQPushConsumer shutdown.");
	}

	public void setNamesrvAddr(String namesrvAddr) {
		this.namesrvAddr = namesrvAddr;
	}

}
