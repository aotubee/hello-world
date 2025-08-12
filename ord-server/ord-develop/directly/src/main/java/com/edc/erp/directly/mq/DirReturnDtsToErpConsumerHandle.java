//package com.edc.erp.directly.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.edc.erp.common.async.service.AsyncPushTaskService;
//import com.edc.erp.common.constant.AsyncTaskConstant;
//import com.edc.erp.common.constant.SystemConstant;
//import com.edc.erp.common.util.MqUtil;
//import com.edc.erp.directly.constant.DirSystemConstant;
//import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
//import com.edc.plugins.jms.MessageProcessor;
//import com.edc.plugins.jms.entity.MqMessage;
//import com.edc.plugins.redis.RedisService;
//import com.edc.sdk.dts.model.order.vo.UnificationReBillVO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * 预售订单支付后更新状态消费者
// */
//@Service("dirReturnDtsToErpConsumerHandle")
//@Slf4j
//@RequiredArgsConstructor
//public class DirReturnDtsToErpConsumerHandle implements MessageProcessor {
//    @Value("${mq.consumer.handle-dir-return-dts-to-erp.tags}")
//    private String tags;
//    @Value("${mq.consumer.handle-dir-return-dts-to-erp.topic}")
//    private String topic;
//    private final DirAsyncTaskItemService dirAsyncTaskItemService;
//    private final AsyncPushTaskService asyncPushTaskService;
//    private final RedisService redisService;
//
//    @Override
//    public boolean process(MqMessage mqMessage) {
//        log.info("DTS直营退货单数据回传消费入参：{},{}", mqMessage.getMessageId(), new String(mqMessage.getBody()));
//        JSONObject jsonObject = MqUtil.initJsonObject(mqMessage, topic, tags);
//        if (null == jsonObject) {
//            return true;
//        }
//        //转换入参
//        UnificationReBillVO unificationReBillVO = JSON.parseObject(jsonObject.toJSONString(), UnificationReBillVO.class);
//        String key = DirSystemConstant.REDIS_DIR_DELIVERY_DTS_TO_ERP + unificationReBillVO.getFdestorg() + SystemConstant.COLON + unificationReBillVO.getFsrcnum();
//        if (!redisService.setIfAbsent(key, unificationReBillVO.getFsrcnum(), 2L, TimeUnit.MINUTES)) {
//            log.error("DTS直营退货单{}数据回传重复消费", unificationReBillVO.getFsrcnum());
//            return true;
//        }
//        boolean resultFlag;
//        try {
//            resultFlag = dirAsyncTaskItemService.unificationReOrderCallBack(unificationReBillVO);
//        } catch (Exception e) {
//            log.error("DTS直营退货单数据回传消费异常：{}", mqMessage.getMessageId(), e);
//            resultFlag = false;
//        }
//        asyncPushTaskService.submitForMQ(AsyncTaskConstant.Type.DIR_RETURN_DTS_TO_ERP, JSONObject.toJSONString(unificationReBillVO),
//                unificationReBillVO.getFdestorg(), unificationReBillVO.getFsrcnum(), resultFlag ? AsyncTaskConstant.ExecStatus.SUCCESS : AsyncTaskConstant.ExecStatus.FAIL);
//        return resultFlag;
//    }
//}
//
//
