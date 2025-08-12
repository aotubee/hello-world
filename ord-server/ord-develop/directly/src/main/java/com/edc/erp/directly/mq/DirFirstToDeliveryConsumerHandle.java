//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.util.MqUtil;
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
// * 预售订单支付后更新状态消费者
// */
//@Service("dirFirstToDeliveryConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirFirstToDeliveryConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-first-to-delivery.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-first-to-delivery.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("直营铺货单转配货单消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        OrdDirOrderFirst ordDirOrderFirst = JSON.parseObject(jsonObject.toJSONString(), OrdDirOrderFirst.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.dirFirstToDelivery(ordDirOrderFirst);
//        } catch (Exception e) {
//            log.error("直营铺货单转配货单消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_FIRST_TO_DELIVERY, JSONObject.toJSONString(ordDirOrderFirst),
//                ordDirOrderFirst.getBizOrgCode(), ordDirOrderFirst.getFirstOrderNo(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
