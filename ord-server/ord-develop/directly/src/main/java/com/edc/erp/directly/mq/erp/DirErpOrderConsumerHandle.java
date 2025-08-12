package com.edc.erp.directly.mq.erp;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.enumeration.ErpDirMqTagsEnum;
import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
import com.edc.plugins.jms.MessageProcessor;
import com.edc.plugins.jms.entity.MqMessage;
import com.edc.plugins.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DirErpOrderConsumerHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/4/2 8:32
 **/
@Service("dirErpOrderConsumerHandle")
@Slf4j
@RequiredArgsConstructor
public class DirErpOrderConsumerHandle implements MessageProcessor {

    private final DirAsyncTaskItemService dirAsyncTaskItemService;
    private final AsyncPushTaskService asyncPushTaskService;
    private final RedisService redisService;

    @Override
    public boolean process(MqMessage mqMessage) {
        log.info("直营Erp单据流消费入参：{},{},{},{}", mqMessage.getTopic(), mqMessage.getTag(), mqMessage.getMessageId(), new String(mqMessage.getBody()));
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
        try {
            if (ErpDirMqTagsEnum.DIR_BEFORE_CREATE_REQUEST_ORDER.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_DISTRIBUTION_TO_REQUEST;
                logPrefix = ErpDirMqTagsEnum.DIR_BEFORE_CREATE_REQUEST_ORDER.getValue();
                SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = JSON.parseObject(messageStr, SendBeforeCreateRequestOrderMqIn.class);
                List<OrdDirOrder> orderList = sendBeforeCreateRequestOrderMqIn.getOrderList();
                bizOrgCode = orderList.get(0).getBizOrgCode();
                businessNo = orderList.get(0).getOrderNo();
                Long orderId = orderList.get(0).getId();
                key = "beforeCreateRequestOrderConsumerKey:" + sendBeforeCreateRequestOrderMqIn.getBizOrgCode() + ":" + orderId;
                if (!redisService.setIfAbsent(key, orderId, 2L, TimeUnit.MINUTES)) {
                    log.error("重复消费-消费订货单{}状态更新至可创建要货单", businessNo);
                    return true;
                }
                resultFlag = dirAsyncTaskItemService.dirDistributionToRequest(sendBeforeCreateRequestOrderMqIn);
            }
            if (ErpDirMqTagsEnum.DIR_AFTER_CREATE_REQUEST_ORDER.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY;
                logPrefix = ErpDirMqTagsEnum.DIR_AFTER_CREATE_REQUEST_ORDER.getValue();
                RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(messageStr, RequestOrderCreateMqIn.class);
                bizOrgCode = requestOrderCreateMqIn.getBizOrgCode();
                businessNo = requestOrderCreateMqIn.getRequestOrderNo();
                key = DirSystemConstant.CHECK_DIR_REQUEST_ORDER_SPLIT_KEY + bizOrgCode +
                        SystemConstant.COLON + requestOrderCreateMqIn.getStoreCode() + SystemConstant.COLON + businessNo;
                if (!redisService.setIfAbsent(key, businessNo, DirSystemConstant.CHECK_DIR_REQUEST_ORDER_SPLIT_TIME_KEY, TimeUnit.MINUTES)) {
                    log.error("门店{}要货单{}重复拆单", requestOrderCreateMqIn.getStoreCode(), businessNo);
                    return true;
                }
                resultFlag = dirAsyncTaskItemService.dirRequestToDelivery(requestOrderCreateMqIn);
            }
            if (ErpDirMqTagsEnum.DIR_FIRST_TO_DELIVERY.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_FIRST_TO_DELIVERY;
                logPrefix = ErpDirMqTagsEnum.DIR_FIRST_TO_DELIVERY.getValue();
                OrdDirOrderFirst ordDirOrderFirst = JSON.parseObject(messageStr, OrdDirOrderFirst.class);
                bizOrgCode = ordDirOrderFirst.getBizOrgCode();
                businessNo = ordDirOrderFirst.getFirstOrderNo();
                key = DirSystemConstant.REDIS_DIR_FIRST_TO_DELIVERY + ordDirOrderFirst.getBizOrgCode() + SystemConstant.COLON + ordDirOrderFirst.getFirstOrderNo();
                if (!redisService.setIfAbsent(key, ordDirOrderFirst.getFirstOrderNo(), 2L, TimeUnit.MINUTES)) {
                    log.error("直营铺货单{}转配货单重复拆单", businessNo);
                    return true;
                }
                resultFlag = dirAsyncTaskItemService.dirFirstToDelivery(ordDirOrderFirst);
            }
            if (ErpDirMqTagsEnum.DIR_DISTRIBUTION_CREATE_ORDER.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_DISTRIBUTION_CREATE_ORDER;
                logPrefix = ErpDirMqTagsEnum.DIR_DISTRIBUTION_CREATE_ORDER.getValue();
                OrdDirOrderDistribution orderDistribution = JSON.parseObject(messageStr, OrdDirOrderDistribution.class);
                bizOrgCode = orderDistribution.getBizOrgCode();
                businessNo = orderDistribution.getDistributionOrderNo();
                resultFlag = dirAsyncTaskItemService.dirDistributionCreateOrder(orderDistribution);
            }
            if (ErpDirMqTagsEnum.DIR_DELIVERY_TO_DIFFERENCE.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_DELIVERY_TO_DIFFERENCE;
                logPrefix = ErpDirMqTagsEnum.DIR_DELIVERY_TO_DIFFERENCE.getValue();
                SaveDifferenceIn saveDifferenceIn = JSON.parseObject(messageStr, SaveDifferenceIn.class);
                bizOrgCode = saveDifferenceIn.getBizOrgCode();
                businessNo = saveDifferenceIn.getDeliveryOrderNo();
                key = DirSystemConstant.REDIS_DIR_SAVE_DIR_DIFFERENCE + bizOrgCode
                        + SystemConstant.COLON + businessNo + SystemConstant.COLON + saveDifferenceIn.getDifferenceType();
                if (!redisService.setIfAbsent(key, saveDifferenceIn.getDeliveryOrderNo() + saveDifferenceIn.getDifferenceType(), 2L, TimeUnit.MINUTES)) {
                    log.error("配货单收货创建差异单重复消费{}", businessNo);
                    return true;
                }
                resultFlag = dirAsyncTaskItemService.dirDeliveryToDifference(saveDifferenceIn);
            }
            if (ErpDirMqTagsEnum.DIR_PURCHASE_ORDER_TO_ERP.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_PURCHASE_ORDER_TO_ERP;
                logPrefix = ErpDirMqTagsEnum.DIR_PURCHASE_ORDER_TO_ERP.getValue();
                List<TransferNoticePurchaseVO> transferNoticePurchaseVOList = JSON.parseArray(messageStr, TransferNoticePurchaseVO.class);
                bizOrgCode = transferNoticePurchaseVOList.get(0).getBizOrgCode();
                businessNo = transferNoticePurchaseVOList.get(0).getPurchaseOrderNo();
                key = DirSystemConstant.REDIS_DIR_PURCHASE_ORDER_TO_ERP + bizOrgCode + SystemConstant.COLON + businessNo;
                if (!redisService.setIfAbsent(key, businessNo, 2L, TimeUnit.MINUTES)) {
                    log.error("采购订单{}回传直营配货单回传重复处理", businessNo);
                    return true;
                }
                resultFlag = dirAsyncTaskItemService.dirPurchaseOrderToErp(transferNoticePurchaseVOList);
            }
            if (ErpDirMqTagsEnum.ORDER_DIR_DELIVERY_DATA_FILE.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.ORDER_DIR_DELIVERY_DATA_FILE;
                logPrefix = ErpDirMqTagsEnum.ORDER_DIR_DELIVERY_DATA_FILE.getValue();
                OrdDeliveryDataFile ordDeliveryDataFile = JSON.parseObject(messageStr, OrdDeliveryDataFile.class);
                bizOrgCode = ordDeliveryDataFile.getBizOrgCode();
                resultFlag = dirAsyncTaskItemService.execOrderDeliveryDataFile(ordDeliveryDataFile);
            }
        } catch (Exception e) {
            log.error("直营{}消息{}消费异常，参数{}", logPrefix, mqMessage.getMessageId(), messageStr, e);
            resultFlag = false;
            if (StringUtils.isNotBlank(key)) {
                redisService.del(key);
            }
        } finally {
            asyncPushTaskService.submitForMQ(type, messageStr, bizOrgCode, businessNo, resultFlag, null);
        }
        return resultFlag;
    }
}
