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
public class DisMqConsumerConfiguration {

    @Autowired
    @Qualifier("disErpOrderConsumerHandle")
    protected MessageProcessor disErpOrderConsumerHandle;


    @Autowired
    @Qualifier("disErpDtsConsumerHandle")
    protected MessageProcessor disErpDtsConsumerHandle;

    @Autowired
    @Qualifier("disZKConsumerHandle")
    protected MessageProcessor disZKConsumerHandle;

    @Bean("handleDisErpOrderConsumerConfig")
    @ConfigurationProperties(prefix = "mq.consumer.handle-dis-erp-order")
    @ConditionalOnProperty(name = "mq.consumer.handle-dis-erp-order.enable", havingValue = "true")
    protected MqConsumerConfig handleDisErpOrderConsumerConfig() {
        return new MqConsumerConfig();
    }

    @Bean("handleDisBeforeCreateRequestOrderConsumer")
    @ConditionalOnProperty(name = "mq.consumer.handle-dis-erp-order.enable", havingValue = "true")
    protected MqConsumer handleDisErpOrderConsumer() {
        return MqFactory.buildConsumer(handleDisErpOrderConsumerConfig(), disErpOrderConsumerHandle);
    }


    @Bean("handleDisErpDtsConsumerConfig")
    @ConfigurationProperties(prefix = "mq.consumer.handle-dis-erp-dts")
    @ConditionalOnProperty(name = "mq.consumer.handle-dis-erp-dts.enable", havingValue = "true")
    protected MqConsumerConfig handleDisErpDtsConsumerConfig() {
        return new MqConsumerConfig();
    }

    @Bean("handleDisErpDtsConsumer")
    @ConditionalOnProperty(name = "mq.consumer.handle-dis-erp-dts.enable", havingValue = "true")
    protected MqConsumer handleDisErpDtsConsumer() {
        return MqFactory.buildConsumer(handleDisErpDtsConsumerConfig(), disErpDtsConsumerHandle);
    }

    @Bean("handleZkWholesaleConsumerConfig")
    @ConfigurationProperties(prefix = "mq.consumer.handle-zk-wholesale")
    @ConditionalOnProperty(name = "mq.consumer.handle-zk-wholesale.enable", havingValue = "true")
    protected MqConsumerConfig handleZkWholesaleConsumerConfig() {
        return new MqConsumerConfig();
    }


    @Bean("handleZkWholesaleConsumer")
    @ConditionalOnProperty(name = "mq.consumer.handle-zk-wholesale.enable", havingValue = "true")
    protected MqConsumer handleZkWholesaleConsumer() {
        return MqFactory.buildConsumer(handleZkWholesaleConsumerConfig(), disZKConsumerHandle);
    }

}
