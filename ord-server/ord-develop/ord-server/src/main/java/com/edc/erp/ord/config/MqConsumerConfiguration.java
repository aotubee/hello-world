package com.edc.erp.ord.config;

import com.edc.plugins.jms.MessageProcessor;
import com.edc.plugins.jms.MqFactory;
import com.edc.plugins.jms.entity.MqConsumerConfig;
import com.edc.plugins.jms.mq.MqConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName MqConsumerConfig
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/10 8:51
 **/
@Configuration
public class MqConsumerConfiguration {

//    @Bean("updateOrderPaidConsumerConfig")
//    @ConfigurationProperties(prefix = "mq.consumer.update-order-paid-consumer")
//    @ConditionalOnProperty(name = "mq.consumer.update-order-paid-consumer.enable", havingValue = "true")
//    protected MqConsumerConfig updateOrderPaidConsumerConfig() {
//        return new MqConsumerConfig();
//    }
//
//    @Autowired
//    @Qualifier("orderConsumerService")
//    protected MessageProcessor orderConsumerService;
//
//    @Bean("updateOrderPaidConsumer")
//    @ConditionalOnProperty(name = "mq.consumer.update-order-paid-consumer.enable", havingValue = "true")
//    protected MqConsumer updateOrderPaidConsumer() {
//        return MqFactory.buildConsumer(updateOrderPaidConsumerConfig(), orderConsumerService);
//    }

    @Bean("updatePresaleOrderPaidConsumerConfig")
    @ConfigurationProperties(prefix = "mq.consumer.update-presale-order-paid-consumer")
    @ConditionalOnProperty(name = "mq.consumer.update-presale-order-paid-consumer.enable", havingValue = "true")
    protected MqConsumerConfig updatePresaleOrderPaidConsumerConfig() {
        return new MqConsumerConfig();
    }

    @Autowired
    @Qualifier("presaleOrderConsumerService")
    protected MessageProcessor presaleOrderConsumerService;

    @Bean("updatePresaleOrderPaidConsumer")
    @ConditionalOnProperty(name = "mq.consumer.update-presale-order-paid-consumer.enable", havingValue = "true")
    protected MqConsumer updatePresaleOrderPaidConsumer() {
        return MqFactory.buildConsumer(updatePresaleOrderPaidConsumerConfig(), presaleOrderConsumerService);
    }
}
