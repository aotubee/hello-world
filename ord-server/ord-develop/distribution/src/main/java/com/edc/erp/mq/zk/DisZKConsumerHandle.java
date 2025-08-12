package com.edc.erp.mq.zk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleReturnSaveIn;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleShipmentSaveIn;
import com.edc.erp.enumeration.ErpDisMqTagsEnum;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.plugins.jms.MessageProcessor;
import com.edc.plugins.jms.entity.MqMessage;
import com.edc.plugins.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName DirErpOrderConsumerHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/2 8:32
 **/
@Service("disZKConsumerHandle")
@Slf4j
@RequiredArgsConstructor
public class DisZKConsumerHandle implements MessageProcessor {

    private final AsyncTaskItemService disAsyncTaskItemService;
    private final AsyncPushTaskService asyncPushTaskService;
    private final RedisService redisService;

    @Override
    public boolean process(MqMessage mqMessage) {
        log.info("加盟中科单据流消费入参：{},{},{},{}", mqMessage.getTopic(), mqMessage.getTag(), mqMessage.getMessageId(), new String(mqMessage.getBody()));
        // 获取消息体内容
        String messageStr = new String(mqMessage.getBody());
//        JSONObject jsonObject = JSON.parseObject(messageStr);
        if (StringUtils.isBlank(messageStr)) {
            return true;
        }
        boolean resultFlag = true;
//        String topic = mqMessage.getTopic();
        String tag = mqMessage.getTag();
        String bizOrgCode = null;
        String businessNo = null;
        String type = null;
        String logPrefix = null;
        String key = null;
        String remark = null;
        try {
            if (ErpDisMqTagsEnum.ZK_CREATE_WHOLESALE_SHIPMENT.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.ZK_SAVE_WHOLESALE_SHIPMENT;
                logPrefix = ErpDisMqTagsEnum.ZK_CREATE_WHOLESALE_SHIPMENT.getValue();
                TaskZKWholesaleShipmentSaveIn taskZKWholesaleShipmentSaveIn = JSON.parseObject(messageStr, TaskZKWholesaleShipmentSaveIn.class);
                bizOrgCode = taskZKWholesaleShipmentSaveIn.getBizOrgCode();
                businessNo = taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo();
                remark = taskZKWholesaleShipmentSaveIn.getZkWholesaleShipmentIn().getSourceNo();
                key = DisSystemConstant.HANDLE_ZK_WHOLESALE_SHIPMENT_KEY + taskZKWholesaleShipmentSaveIn.getBizOrgCode() + SystemConstant.COLON + taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo();
                if (!redisService.setIfAbsent(key, taskZKWholesaleShipmentSaveIn.getWholesaleShipmentNo(), 5L, TimeUnit.MINUTES)) {
                    log.error("中科请求创建批发出货单{}重复消费", businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.zKCreateWholesaleShipment(taskZKWholesaleShipmentSaveIn);
            }
            if (ErpDisMqTagsEnum.ZK_CREATE_WHOLESALE_RETURN.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.ZK_SAVE_WHOLESALE_RETURN;
                logPrefix = ErpDisMqTagsEnum.ZK_CREATE_WHOLESALE_RETURN.getValue();
                TaskZKWholesaleReturnSaveIn taskZKWholesaleReturnSaveIn = JSONObject.parseObject(messageStr, TaskZKWholesaleReturnSaveIn.class);
                bizOrgCode = taskZKWholesaleReturnSaveIn.getBizOrgCode();
                businessNo = taskZKWholesaleReturnSaveIn.getWholesaleReturnNo();
                remark = taskZKWholesaleReturnSaveIn.getZkWholesaleReturnIn().getSourceNo();
                key = DisSystemConstant.HANDLE_ZK_WHOLESALE_RETURN_KEY + bizOrgCode + SystemConstant.COLON + taskZKWholesaleReturnSaveIn.getWholesaleReturnNo();
                if (!redisService.setIfAbsent(key, taskZKWholesaleReturnSaveIn.getWholesaleReturnNo(), 5L, TimeUnit.MINUTES)) {
                    log.error("中科请求创建批发退货单{}重复消费", businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.zKCreateWholesaleReturn(taskZKWholesaleReturnSaveIn);
            }
            if (ErpDisMqTagsEnum.ZK_WHOLESALE_SHIPMENT_BACK.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.ZK_WHOLESALE_SHIPMENT_BACK;
                logPrefix = ErpDisMqTagsEnum.ZK_WHOLESALE_SHIPMENT_BACK.getValue();
                WholesaleShipment wholesaleShipment = JSONObject.parseObject(messageStr, WholesaleShipment.class);
                bizOrgCode = wholesaleShipment.getBizOrgCode();
                businessNo = wholesaleShipment.getShipmentNo();
                resultFlag = disAsyncTaskItemService.zKWholesaleShipmentBack(wholesaleShipment);
            }
            if (ErpDisMqTagsEnum.ZK_WHOLESALE_RETURN_BACK.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.ZK_WHOLESALE_RETURN_BACK;
                logPrefix = ErpDisMqTagsEnum.ZK_WHOLESALE_RETURN_BACK.getValue();
                WholesaleReturns wholesaleReturns = JSONObject.parseObject(messageStr, WholesaleReturns.class);
                bizOrgCode = wholesaleReturns.getBizOrgCode();
                businessNo = wholesaleReturns.getWholesaleReturnNo();
                resultFlag = disAsyncTaskItemService.zKWholesaleReturnBack(wholesaleReturns);
            }
        } catch (Exception e) {
            log.error("中科{}消息{}消费异常，参数{}", logPrefix, mqMessage.getMessageId(), messageStr, e);
            resultFlag = false;
            if (StringUtils.isNotBlank(key)) {
                redisService.del(key);
            }
        } finally {
            asyncPushTaskService.submitForMQ(type, messageStr, bizOrgCode, businessNo, resultFlag, remark);
        }
        return resultFlag;
    }
}
