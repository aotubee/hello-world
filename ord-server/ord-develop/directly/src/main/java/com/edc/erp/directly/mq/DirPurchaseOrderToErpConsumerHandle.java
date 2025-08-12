//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
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
//import java.util.List;
//
///**
// * 采购订单回传配货单消费者
// */
//@Service("dirPurchaseOrderToErpConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirPurchaseOrderToErpConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-purchase-order-to-erp.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-purchase-order-to-erp.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("采购订单回传配货单消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        List<TransferNoticePurchaseVO> transferNoticePurchaseVOList = JSON.parseArray(jsonObject.toJSONString(), TransferNoticePurchaseVO.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.dirPurchaseOrderToErp(transferNoticePurchaseVOList);
//        } catch (Exception e) {
//            log.error("采购订单回传配货单消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_PURCHASE_ORDER_TO_ERP, JSONObject.toJSONString(transferNoticePurchaseVOList),
//                transferNoticePurchaseVOList.get(0).getBizOrgCode(), transferNoticePurchaseVOList.get(0).getPurchaseOrderNo(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
