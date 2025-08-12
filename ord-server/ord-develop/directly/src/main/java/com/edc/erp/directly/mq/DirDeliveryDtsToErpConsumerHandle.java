//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import com.edc.sdk.dts.model.order.vo.UnificationBillVO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
///**
// * 预售订单支付后更新状态消费者
// */
//@Service("dirDeliveryDtsToErpConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirDeliveryDtsToErpConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-delivery-dts-to-erp.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-delivery-dts-to-erp.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("DTS配货单数据回传消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        UnificationBillVO unificationBillVO = JSON.parseObject(jsonObject.toJSONString(), UnificationBillVO.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.unificationOrderCallBack(unificationBillVO);
//        } catch (Exception e) {
//            log.error("DTS配货单数据回传消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP, JSONObject.toJSONString(unificationBillVO),
//                unificationBillVO.getFdestorg(), unificationBillVO.getFsrcnum(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
