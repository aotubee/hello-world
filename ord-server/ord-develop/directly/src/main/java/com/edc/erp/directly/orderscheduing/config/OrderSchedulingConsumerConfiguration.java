//package com.edc.erp.directly.orderscheduing.config;
//
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.MqFactory;
//import com.edc.plugins.jms.entity.MqConsumerConfig;
//import com.edc.plugins.jms.mq.MqConsumer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @author fxw
// * @description: 订单流转消费者配置
// * @since 2022/10/24 17:28
// */
//@Configuration
//public class OrderSchedulingConsumerConfiguration {
//
//    /**
//     * 订货单更新状态至可创建要货单前的消费者
//     *
//     * @return
//     */
//    @Bean("dirHandleBeforeCreateRequestOrderConsumerConfig")
//    @ConfigurationProperties(prefix = "mq.consumer.handle-dir-before-create-request-order")
//    @ConditionalOnProperty(name = "mq.consumer.handle-dir-before-create-request-order.enable", havingValue = "true")
//    protected MqConsumerConfig dirHandleBeforeCreateRequestOrderConsumerConfig() {
//        return new MqConsumerConfig();
//    }
//
//    @Autowired
//    @Qualifier("dirBeforeCreateRequestOrderConsumerService")
//    MessageProcessor dirBeforeCreateRequestOrderConsumerService;
//
//
//    @Bean("dirHandleBeforeCreateRequestOrderConsumer")
//    @ConditionalOnProperty(name = "mq.consumer.handle-dir-before-create-request-order.enable", havingValue = "true")
//    protected MqConsumer handleBeforeCreateRequestOrderConsumer() {
//        return MqFactory.buildConsumer(dirHandleBeforeCreateRequestOrderConsumerConfig(), dirBeforeCreateRequestOrderConsumerService);
//    }
//}
