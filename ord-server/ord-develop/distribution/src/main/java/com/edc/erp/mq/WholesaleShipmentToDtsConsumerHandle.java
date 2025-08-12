//package com.edc.erp.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import com.edc.sdk.dts.model.order.in.WholesaleBillIn;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * 预售订单支付后更新状态消费者
// */
//@Service("wholesaleShipmentToDtsConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class WholesaleShipmentToDtsConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-wholesale-shipment-to-dts.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-wholesale-shipment-to-dts.topic}")
//    private String topic;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("直营铺货单转配货单消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        WholesaleBillIn wholesaleBillIn = JSON.parseObject(jsonObject.toJSONString(), WholesaleBillIn.class);
//        AtomicBoolean resultFlag = new AtomicBoolean(false);
//        try {
////            resultFlag = dirAsyncTaskItemService.wholesaleShipmentToDts(wholesaleBillIn);
//        } catch (Exception e) {
//            log.error("直营铺货单转配货单消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag.set(false);
//        }
//        String businessOrderNo = wholesaleBillIn.getPlatform_bill_id();
////        OrdDirReturn ordDirReturn = ordDirReturnService.getReturnOrderByNo(businessOrderNo);
////        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_FIRST_TO_DELIVERY, JSONObject.toJSONString(wholesaleBillIn),
////                ordDirOrderFirst.getBizOrgCode(), wholesaleBillIn.getFirstOrderNo(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag.get();
//    }
//}
//
//
