//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
//import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
//import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
//import com.edc.plugins.common.response.Response;
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
//@Service("dirDistributionCreateOrderConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirDistributionCreateOrderConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-distribution-create-order.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-distribution-create-order.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("直营分货单生成订货单消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        OrdDirOrderDistribution orderDistribution = JSON.parseObject(jsonObject.toJSONString(), OrdDirOrderDistribution.class);
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.dirDistributionCreateOrder(orderDistribution);
//        } catch (Exception e) {
//            log.error("直营分货单生成订货单消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_DISTRIBUTION_CREATE_ORDER, JSONObject.toJSONString(orderDistribution),
//                orderDistribution.getBizOrgCode(), orderDistribution.getDistributionOrderNo(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
