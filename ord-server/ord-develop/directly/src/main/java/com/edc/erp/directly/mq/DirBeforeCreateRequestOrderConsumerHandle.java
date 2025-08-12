//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.directly.distribution.entity.OrdDirOrder;
//import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
//import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// * 预售订单支付后更新状态消费者
// */
//@Service("dirBeforeCreateRequestOrderConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirBeforeCreateRequestOrderConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-before-create-request-order.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-before-create-request-order.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("直营创建要货单前消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        log.info("订货单状态更新至可创建..要货单前入参----------------->" + jsonObject.toJSONString());
//        SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = JSON.parseObject(jsonObject.toJSONString(), SendBeforeCreateRequestOrderMqIn.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.dirDistributionToRequest(sendBeforeCreateRequestOrderMqIn);
//        } catch (Exception e) {
//            log.error("直营创建要货单前消防异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        List<OrdDirOrder> orderList = sendBeforeCreateRequestOrderMqIn.getOrderList();
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_DISTRIBUTION_TO_REQUEST, JSONObject.toJSONString(sendBeforeCreateRequestOrderMqIn),
//                sendBeforeCreateRequestOrderMqIn.getBizOrgCode(), orderList.get(0).getOrderNo(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
