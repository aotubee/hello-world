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
public class DirMqSenderConfiguration {



    /**
     * 订货单创建成功状态更新至可创建要货单前发送消息
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirBeforeCreateRequestOrderSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-before-create-request-order-sender")
    public MessageSender dirBeforeCreateRequestOrderSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * 要货单创建成功后发送消息
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirAfterCreateRequestOrderSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-after-create-request-order-sender")
    public MessageSender dirAfterCreateRequestOrderSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * 直营收到铺货拆配货单
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirFirstToDeliverySender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-first-to-delivery-sender")
    public MessageSender dirFirstToDeliverySender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * 直营分货单生成订货单
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirDistributionCreateOrderSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-distribution-create-order-sender")
    public MessageSender dirDistributionCreateOrderSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * 直营配货单收货后生成直营配货差异单
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirDeliveryToDifferenceSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-delivery-to-difference-sender")
    public MessageSender dirDeliveryToDifferenceSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * DTS配货单数据下发
     *
     * @param mqProducer
     * @return
     */
//    @Bean("dirDeliveryToDtsSender")
//    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
//    @ConfigurationProperties(prefix = "mq.dir-delivery-to-dts-sender")
//    public MessageSender dirDeliveryToDtsSender(MqProducer mqProducer) {
//        return new MessageSender(mqProducer);
//    }

    @Bean("dirDeliveryToDtsSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-delivery-to-dts-sender")
    public MessageSender dirDeliveryToDtsSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * DTS配销差异单数据下发
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirDifferenceOrderToDtsSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-difference-order-to-dts-sender")
    public MessageSender dirDifferenceOrderToDtsSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }



    /**
     * DTS直营退货单数据下发
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirReturnToDtsSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-return-to-dts-sender")
    public MessageSender dirReturnToDtsSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }



    /**
     * DTS配货单数据回传
     *
     * @param mqProducer
     * @return  dir-return-to-dts-sender:
     */
    @Bean("dirDeliveryDtsToErpSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-delivery-dts-to-erp-sender")
    public MessageSender dirDeliveryDtsToErpSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }




    /**
     * DTS直营退货单数据回传
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirReturnDtsToErpSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-return-dts-to-erp-sender")
    public MessageSender dirReturnDtsToErpSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }




    /**
     * DTS直营差异单数据回传
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirDifferenceDtsToErpSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-difference-dts-to-erp-sender")
    public MessageSender dirDifferenceDtsToErpSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * 直营配货单数据文件执行
     *
     * @param mqProducer
     * @return
     */
    @Bean("orderDirDeliveryDataFileSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.order-dir-delivery-data-file-sender")
    public MessageSender orderDirDeliveryDataFileSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * 采购订单回传配货单
     *
     * @param mqProducer
     * @return
     */
    @Bean("dirPurchaseOrderToErpSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dir-purchase-order-to-erp-sender")
    public MessageSender dirPurchaseOrderToErpSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }




}
