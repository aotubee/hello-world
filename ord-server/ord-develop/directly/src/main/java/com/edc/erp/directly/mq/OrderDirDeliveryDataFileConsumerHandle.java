//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.entity.OrdDeliveryDataFile;
//import com.edc.erp.common.util.MqUtil;
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
//@Service("orderDirDeliveryDataFileConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class OrderDirDeliveryDataFileConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-order-dir-delivery-data-file.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-order-dir-delivery-data-file.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("直营配货单数据文件执行消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        OrdDeliveryDataFile ordDeliveryDataFile = JSON.parseObject(jsonObject.toJSONString(), OrdDeliveryDataFile.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.execOrderDeliveryDataFile(ordDeliveryDataFile);
//        } catch (Exception e) {
//            log.error("直营配货单数据文件执行消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.ORDER_DIR_DELIVERY_DATA_FILE, JSONObject.toJSONString(ordDeliveryDataFile),
//                ordDeliveryDataFile.getBizOrgCode(), null, resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
