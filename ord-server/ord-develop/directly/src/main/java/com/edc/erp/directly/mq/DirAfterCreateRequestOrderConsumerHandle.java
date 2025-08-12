//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
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
//@Service("dirAfterCreateRequestOrderConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirAfterCreateRequestOrderConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-after-create-request-order.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-after-create-request-order.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("直营要货单拆配货单消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
////        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        String resultStr = new String(mqMessage.getBody());
//        JSONObject jsonObject = JSON.parseObject(resultStr);
//        if (null == jsonObject) {
//            return true;
//        }
//        String tag = mqMessage.getTag();
//
//        RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(jsonObject.toJSONString(), RequestOrderCreateMqIn.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.dirRequestToDelivery(requestOrderCreateMqIn);
//        } catch (Exception e) {
//            log.error("直营要货单拆配货单消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, JSONObject.toJSONString(requestOrderCreateMqIn),
//                requestOrderCreateMqIn.getBizOrgCode(), requestOrderCreateMqIn.getRequestOrderNo(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
