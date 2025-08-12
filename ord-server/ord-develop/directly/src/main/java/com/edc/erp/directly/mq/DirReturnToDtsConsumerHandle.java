//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
//import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
//import com.edc.erp.directly.returnorder.service.OrdDirReturnService;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import com.edc.sdk.dts.model.order.in.UnificationReBillIn;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
///**
// * DTS直营退货单数据下发
// */
//@Service("dirReturnToDtsConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirReturnToDtsConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-return-to-dts.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-return-to-dts.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//    private final OrdDirReturnService ordDirReturnService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("DTS直营退货单数据下发消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        UnificationReBillIn unificationReBillIn = JSON.parseObject(jsonObject.toJSONString(), UnificationReBillIn.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.dirReturnToDts(unificationReBillIn);
//        } catch (Exception e) {
//            log.error("DTS直营退货单数据下发消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        String businessOrderNo = unificationReBillIn.getPlatform_bill_id();
//        OrdDirReturn ordDirReturn = ordDirReturnService.getReturnOrderByNo(businessOrderNo);
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_RETURN_TO_DTS, JSONObject.toJSONString(unificationReBillIn),
//                ordDirReturn.getBizOrgCode(), businessOrderNo, resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
