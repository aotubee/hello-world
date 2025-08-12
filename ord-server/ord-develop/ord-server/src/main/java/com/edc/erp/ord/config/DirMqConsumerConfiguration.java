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
public class DirMqConsumerConfiguration {

    @Autowired
    @Qualifier("dirErpOrderConsumerHandle")
    protected MessageProcessor dirErpOrderConsumerHandle;

    @Autowired
    @Qualifier("dirErpDtsConsumerHandle")
    protected MessageProcessor dirErpDtsConsumerHandle;

    @Bean("handleDirErpOrderConsumerConfig")
    @ConfigurationProperties(prefix = "mq.consumer.handle-dir-erp-order")
    @ConditionalOnProperty(name = "mq.consumer.handle-dir-erp-order.enable", havingValue = "true")
    protected MqConsumerConfig handleDirErpOrderConsumerConfig() {
        return new MqConsumerConfig();
    }

    @Bean("handleDirErpOrderConsumer")
    @ConditionalOnProperty(name = "mq.consumer.handle-dir-erp-order.enable", havingValue = "true")
    protected MqConsumer handleDirErpOrderConsumer() {
        return MqFactory.buildConsumer(handleDirErpOrderConsumerConfig(), dirErpOrderConsumerHandle);
    }


    @Bean("handleDirErpDtsConsumerConfig")
    @ConfigurationProperties(prefix = "mq.consumer.handle-dir-erp-dts")
    @ConditionalOnProperty(name = "mq.consumer.handle-dir-erp-dts.enable", havingValue = "true")
    protected MqConsumerConfig handleDirErpDtsConsumerConfig() {
        return new MqConsumerConfig();
    }


    @Bean("handleDirErpDtsConsumer")
    @ConditionalOnProperty(name = "mq.consumer.handle-dir-erp-dts.enable", havingValue = "true")
    protected MqConsumer handleDirErpDtsConsumer() {
        return MqFactory.buildConsumer(handleDirErpDtsConsumerConfig(), dirErpDtsConsumerHandle);
    }

}
