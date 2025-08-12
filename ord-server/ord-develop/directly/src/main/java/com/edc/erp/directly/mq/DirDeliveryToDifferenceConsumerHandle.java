//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
//import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
//import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
///**
// * 直营配货单收货后生成直营配货差异单消费者
// */
//@Service("dirDeliveryToDifferenceConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirDeliveryToDifferenceConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-delivery-to-difference.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-delivery-to-difference.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("直营配货单收货后生成直营配货差异单消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        SaveDifferenceIn saveDifferenceIn = JSON.parseObject(jsonObject.toJSONString(), SaveDifferenceIn.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.dirDeliveryToDifference(saveDifferenceIn);
//        } catch (Exception e) {
//            log.error("直营配货单收货后生成直营配货差异单消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_DELIVERY_TO_DIFFERENCE, JSONObject.toJSONString(saveDifferenceIn),
//                saveDifferenceIn.getBizOrgCode(), saveDifferenceIn.getDeliveryOrderNo(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
