//package com.edc.erp.orderscheduing.config;
//
//import com.edc.plugins.jms.MessageSender;
//import com.edc.plugins.jms.mq.MqProducer;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * 新订单消息生产者配置
// *
// * @author : fxw
// * @date 2022/10-18
// */
//@Configuration
//public class DisOrderSchedulingMqSenderConfiguration {
//
//    /**
//     * 订货单创建成功状态更新至可创建集货单前发送消息
//     *
//     * @param mqProducer
//     * @return
//     */
//    @Bean("beforeCreateRequestOrderSender")
//    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
//    @ConfigurationProperties(prefix = "mq.producer.before-create-request-order-sender")
//    public MessageSender beforeCreateRequestOrderSender(MqProducer mqProducer) {
//        return new MessageSender(mqProducer);
//    }
//
//}
