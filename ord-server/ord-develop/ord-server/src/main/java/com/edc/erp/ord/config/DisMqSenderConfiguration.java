package com.edc.erp.ord.config;

import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.mq.MqProducer;
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
public class DisMqSenderConfiguration {



    /**
     * 订货单创建成功状态更新至可创建要货单前发送消息
     *
     * @param mqProducer
     * @return
     */
    @Bean("disBeforeCreateRequestOrderSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-before-create-request-order-sender")
    public MessageSender disBeforeCreateRequestOrderSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }




    /**
     * 要货单创建成功后发送消息
     *
     * @param mqProducer
     * @return
     */
    @Bean("disAfterCreateRequestOrderSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-after-create-request-order-sender")
    public MessageSender disAfterCreateRequestOrderSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * 直营收到铺货拆配货单
     *
     * @param mqProducer
     * @return
     */
    @Bean("disFirstToDeliverySender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-first-to-delivery-sender")
    public MessageSender dirFirstToDeliverySender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * 直营分货单生成订货单
     *
     * @param mqProducer
     * @return
     */
    @Bean("disDistributionCreateOrderSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-distribution-create-order-sender")
    public MessageSender disDistributionCreateOrderSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * 直营配货单收货后生成直营配货差异单
     *
     * @param mqProducer
     * @return
     */
    @Bean("disDeliveryToDifferenceSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-delivery-to-difference-sender")
    public MessageSender disDeliveryToDifferenceSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * DTS配货单数据下发
     *
     * @param mqProducer
     * @return
     */
    @Bean("disDeliveryToDtsSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-delivery-to-dts-sender")
    public MessageSender disDeliveryToDtsSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * DTS配销差异单数据下发
     *
     * @param mqProducer
     * @return
     */
    @Bean("disDifferenceOrderToDtsSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-difference-order-to-dts-sender")
    public MessageSender disDifferenceOrderToDtsSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * DTS直营退货单数据下发
     *
     * @param mqProducer
     * @return
     */
    @Bean("disReturnToDtsSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-return-to-dts-sender")
    public MessageSender disReturnToDtsSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * DTS配货单数据回传
     *
     * @param mqProducer
     * @return
     */
    @Bean("disDeliveryDtsToErpSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-delivery-dts-to-erp-sender")
    public MessageSender disDeliveryDtsToErpSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }



    /**
     * DTS直营退货单数据回传
     *
     * @param mqProducer
     * @return
     */
    @Bean("disReturnDtsToErpSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-return-dts-to-erp-sender")
    public MessageSender disReturnDtsToErpSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }




    /**
     * DTS直营差异单数据回传
     *
     * @param mqProducer
     * @return
     */
    @Bean("disDifferenceDtsToErpSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-difference-dts-to-erp-sender")
    public MessageSender disDifferenceDtsToErpSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * 直营配货单数据文件执行
     *
     * @param mqProducer
     * @return
     */
    @Bean("orderDisDeliveryDataFileSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.order-dis-delivery-data-file-sender")
    public MessageSender orderDisDeliveryDataFileSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * 采购订单回传配货单
     *
     * @param mqProducer
     * @return
     */
    @Bean("disPurchaseOrderToErpSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.dis-purchase-order-to-erp-sender")
    public MessageSender disPurchaseOrderToErpSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * 处理批发出采购回传
     *
     * @param mqProducer
     * @return
     */
    @Bean("handleWholesaleShipmentPurchaseBackSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.handle-wholesale-shipment-purchase-back-sender")
    public MessageSender handleWholesaleShipmentPurchaseBackSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }



    /**
     * DTS批发出货单数据下发
     *
     * @param mqProducer
     * @return
     */
    @Bean("wholesaleShipmentToDtsSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.wholesale-shipment-to-dts-sender")
    public MessageSender wholesaleShipmentToDtsSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * DTS批发出货退数据下发
     *
     * @param mqProducer
     * @return
     */
    @Bean("wholesaleReturnToDtsSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.wholesale-return-to-dts-sender")
    public MessageSender wholesaleReturnToDtsSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }


    /**
     * DTS批发单数据回传
     *
     * @param mqProducer
     * @return
     */
    @Bean("wholesaleOrderCallBackSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.wholesale-order-call-back-sender")
    public MessageSender wholesaleOrderCallBackSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * DTS批发退单数据回传
     *
     * @param mqProducer
     * @return
     */
    @Bean("wholesaleReOrderCallBackSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.wholesale-re-order-call-back-sender")
    public MessageSender wholesaleReOrderCallBackSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

//    /**
//     * 中科请求创建批发出货单
//     *
//     * @param mqProducer
//     * @return
//     */
//    @Bean("zKCreateWholesaleShipmentSender")
//    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
//    @ConfigurationProperties(prefix = "mq.zk-create-wholesale-shipment-sender")
//    public MessageSender zKCreateWholesaleShipmentSender(MqProducer mqProducer) {
//        return new MessageSender(mqProducer);
//    }
//
//    /**
//     * 中科请求创建批发退货单
//     *
//     * @param mqProducer
//     * @return
//     */
//    @Bean("zKCreateWholesaleReturnSender")
//    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
//    @ConfigurationProperties(prefix = "mq.zk-create-wholesale-return-sender")
//    public MessageSender zKCreateWholesaleReturnSender(MqProducer mqProducer) {
//        return new MessageSender(mqProducer);
//    }

    /**
     * 中科请求创建批发出货单回传中科
     *
     * @param mqProducer
     * @return
     */
    @Bean("zKWholesaleShipmentBackSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.zk-wholesale-shipment-back-sender")
    public MessageSender zKWholesaleShipmentBackSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }

    /**
     * 中科请求创建批发退货单回传中科
     *
     * @param mqProducer
     * @return
     */
    @Bean("zKWholesaleReturnBackSender")
    @ConditionalOnProperty(name = "mq.producer.enable", havingValue = "true")
    @ConfigurationProperties(prefix = "mq.producer.zk-wholesale-return-back-sender")
    public MessageSender zKWholesaleReturnBackSender(MqProducer mqProducer) {
        return new MessageSender(mqProducer);
    }
}
