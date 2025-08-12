package com.edc.erp.ord.config;

import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.MqFactory;
import com.edc.plugins.jms.entity.MqProducerConfig;
import com.edc.plugins.jms.mq.MqProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息生产者配置
 *
 * @author : lee
 * @date 2022-01-19 11:50
 */
@Configuration
public class MqSenderConfiguration {

    @Bean("apacheMQProducerConfig")
    @ConfigurationProperties(prefix = "mq.producer")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    public MqProducerConfig apacheMqProducerConfig() {
        return new MqProducerConfig();
    }

    @Bean("apacheMQProducer")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    public MqProducer apacheMqProducer(MqProducerConfig mqProducerConfig) {
        return MqFactory.buildProducer(mqProducerConfig);
    }

    @Bean("updatePresaleOrderPaidSender")
    @ConditionalOnBean(MqProducer.class)
    @ConfigurationProperties(prefix = "mq.producer.update-presale-order-paid-sender")
    public MessageSender updatePresaleOrderPaidSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

//    @Bean("updateOrderPaidSender")
//    @ConditionalOnBean(MqProducer.class)
//    @ConfigurationProperties(prefix = "mq.producer.update-order-paid-sender")
//    public MessageSender updateOrderPaidSender(MqProducer mqProducer) {
//        return new MessageSender(mqProducer);
//    }

    @Bean("takeDeliveryAdjustExpirySender")
    @ConditionalOnBean(MqProducer.class)
    @ConfigurationProperties(prefix = "mq.producer.take-delivery-adjust-expiry-sender")
    public MessageSender takeDeliveryAdjustExpirySender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    @Bean("hsWholesaleDiffSender")
    @ConditionalOnBean(MqProducer.class)
    @ConfigurationProperties(prefix = "mq.producer.hs-wholesale-diff-sender")
    public MessageSender hsWholesaleDiffSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    @Bean("hsWholesaleShipmentDtsBackSender")
    @ConditionalOnBean(MqProducer.class)
    @ConfigurationProperties(prefix = "mq.producer.hs-wholesale-shipment-dts-back-sender")
    public MessageSender hsWholesaleShipmentDtsBackSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    @Bean("hsWholesaleRefuseReturnSender")
    @ConditionalOnBean(MqProducer.class)
    @ConfigurationProperties(prefix = "mq.producer.hs-wholesale-refuse-return-sender")
    public MessageSender hsWholesaleRefuseReturnSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    @Bean("hsWholesaleReturnDtsBackSender")
    @ConditionalOnBean(MqProducer.class)
    @ConfigurationProperties(prefix = "mq.producer.hs-wholesale-return-dts-back-sender")
    public MessageSender hsWholesaleReturnDtsBackSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

}
