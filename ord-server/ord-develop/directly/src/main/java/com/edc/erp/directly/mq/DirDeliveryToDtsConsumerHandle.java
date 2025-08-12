//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut;
//import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
//import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import com.edc.sdk.dts.model.order.in.UnificationBillIn;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
///**
// * DTS配货单数据下发
// */
//@Service("dirDeliveryToDtsConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirDeliveryToDtsConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-delivery-to-dts.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-delivery-to-dts.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//    private final OrdDirDeliveryService ordDirDeliveryService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("DTS配货单数据下发消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        UnificationBillIn unificationBillIn = JSON.parseObject(jsonObject.toJSONString(), UnificationBillIn.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.dirDeliveryToDts(unificationBillIn);
//        } catch (Exception e) {
//            log.error("DTS配货单数据下发消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        String businessOrderNo = unificationBillIn.getPlatform_bill_id();
//        OrdDirDeliveryOut ordDirDeliveryOut = ordDirDeliveryService.getDeliveryOrderOutByNo(businessOrderNo);
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, JSONObject.toJSONString(unificationBillIn),
//                ordDirDeliveryOut.getBizOrgCode(), businessOrderNo, resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
