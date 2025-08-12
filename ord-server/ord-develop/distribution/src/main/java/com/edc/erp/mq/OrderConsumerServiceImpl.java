//package com.edc.erp.mq;
//
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.distribution.service.DisOrderPayService;
//import com.edc.erp.presale.model.in.UpdateStoreOrderPaidForMqIn;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
///**
// * 预售订单支付后更新状态消费者
// */
//@Service("orderConsumerService")
//@Slf4j
//@RequiredArgsConstructor
//public class OrderConsumerServiceImpl implements MessageProcessor {
//    @Value("${mq.consumer.update-order-paid-consumer.tags}")
//    private String tags;
//    @Value("${mq.consumer.update-order-paid-consumer.topic}")
//    private String topic;
//    @Autowired
//    private DisOrderPayService disOrderPayService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        log.info("配销订货单支付后更新状态消息：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        UpdateStoreOrderPaidForMqIn updateStoreOrderPaidForMqIn = JSONObject.toJavaObject(jsonObject, UpdateStoreOrderPaidForMqIn.class);
//        try {
//            disOrderPayService.handleStoreDisOrderPay(updateStoreOrderPaidForMqIn.getOrderId(), updateStoreOrderPaidForMqIn.getLoginUsername());
//        } catch (Exception e) {
//            log.error("配销订货单支付后更新状态异常：{},{}", mqMessage.getMessageId(), updateStoreOrderPaidForMqIn.getOrderId(), e);
//            return false;
//        }
//        return true;
//    }
//}
//
//
