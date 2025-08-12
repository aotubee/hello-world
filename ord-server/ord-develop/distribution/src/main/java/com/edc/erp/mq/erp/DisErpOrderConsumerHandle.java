package com.edc.erp.mq.erp;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrdDeliveryDataFile;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.purchase.TransferShipmentPushPurchaseBackVO;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.enumeration.ErpDisMqTagsEnum;
import com.edc.erp.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.plugins.common.response.Response;
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
@Service("disErpOrderConsumerHandle")
@Slf4j
@RequiredArgsConstructor
public class DisErpOrderConsumerHandle implements MessageProcessor {

    private final AsyncTaskItemService disAsyncTaskItemService;
    private final AsyncPushTaskService asyncPushTaskService;
    private final RedisService redisService;

    @Override
    public boolean process(MqMessage mqMessage) {
        log.info("加盟Erp单据流消费入参：{},{},{},{}", mqMessage.getTopic(), mqMessage.getTag(), mqMessage.getMessageId(), new String(mqMessage.getBody()));
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
            if (ErpDisMqTagsEnum.DIS_BEFORE_CREATE_REQUEST_ORDER.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_DISTRIBUTION_TO_REQUEST;
                logPrefix = ErpDisMqTagsEnum.DIS_BEFORE_CREATE_REQUEST_ORDER.getValue();
                SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = JSON.parseObject(messageStr, SendBeforeCreateRequestOrderMqIn.class);
                List<OrdDisOrder> orderList = sendBeforeCreateRequestOrderMqIn.getOrderList();
                bizOrgCode = orderList.get(0).getBizOrgCode();
                businessNo = orderList.get(0).getOrderNo();
                Long orderId = sendBeforeCreateRequestOrderMqIn.getOrderList().get(0).getId();
                key = "beforeCreateRequestOrderConsumerKey:" + sendBeforeCreateRequestOrderMqIn.getBizOrgCode() + ":" + orderId;
                if (!redisService.setIfAbsent(key, orderId, 2L, TimeUnit.MINUTES)) {
                    log.error("重复消费-消费订货单{}状态更新至可创建要货单", businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.disDistributionToRequest(sendBeforeCreateRequestOrderMqIn);
            }
            if (ErpDisMqTagsEnum.DIS_AFTER_CREATE_REQUEST_ORDER.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_REQUEST_TO_DELIVERY;
                logPrefix = ErpDisMqTagsEnum.DIS_AFTER_CREATE_REQUEST_ORDER.getValue();
                RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(messageStr, RequestOrderCreateMqIn.class);
                bizOrgCode = requestOrderCreateMqIn.getBizOrgCode();
                businessNo = requestOrderCreateMqIn.getRequestOrderNo();
                key = DisSystemConstant.CHECK_DIS_REQUEST_ORDER_SPLIT_KEY + requestOrderCreateMqIn.getBizOrgCode() +
                        SystemConstant.COLON + requestOrderCreateMqIn.getStoreCode() + SystemConstant.COLON + businessNo;
                if (!redisService.setIfAbsent(key, businessNo, DisSystemConstant.CHECK_DIS_REQUEST_ORDER_SPLIT_TIME_KEY, TimeUnit.MINUTES)) {
                    log.error("门店{}集货单{}重复拆单", requestOrderCreateMqIn.getStoreCode(), businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.disRequestToDelivery(requestOrderCreateMqIn);
            }
            if (ErpDisMqTagsEnum.DIS_FIRST_TO_DELIVERY.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_FIRST_TO_DELIVERY;
                logPrefix = ErpDisMqTagsEnum.DIS_FIRST_TO_DELIVERY.getValue();
                OrdDisOrderFirst ordDisOrderFirst = JSON.parseObject(messageStr, OrdDisOrderFirst.class);
                bizOrgCode = ordDisOrderFirst.getBizOrgCode();
                businessNo = ordDisOrderFirst.getFirstOrderNo();
                key = DisSystemConstant.REDIS_DIS_FIRST_TO_DELIVERY + ordDisOrderFirst.getBizOrgCode() + SystemConstant.COLON + ordDisOrderFirst.getFirstOrderNo();
                if (!redisService.setIfAbsent(key, ordDisOrderFirst.getFirstOrderNo(), 2L, TimeUnit.MINUTES)) {
                    log.error("加盟首单铺货{}拆配销单重复消费", businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.disFirstToDelivery(ordDisOrderFirst);
            }
            if (ErpDisMqTagsEnum.DIS_DISTRIBUTION_CREATE_ORDER.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_DISTRIBUTION_CREATE_ORDER;
                logPrefix = ErpDisMqTagsEnum.DIS_DISTRIBUTION_CREATE_ORDER.getValue();
                OrdDisOrderDistribution orderDistribution = JSON.parseObject(messageStr, OrdDisOrderDistribution.class);
                bizOrgCode = orderDistribution.getBizOrgCode();
                businessNo = orderDistribution.getDistributionOrderNo();
                // 分货单不能在这删除重复的key，因为多个门店独立创建订货单内部是try catch运行，如果重复消费，导致单店重复创建单据
                resultFlag = disAsyncTaskItemService.disDistributionCreateOrder(orderDistribution);
            }
            if (ErpDisMqTagsEnum.DIS_DELIVERY_TO_DIFFERENCE.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_DELIVERY_TO_DIFFERENCE;
                logPrefix = ErpDisMqTagsEnum.DIS_DELIVERY_TO_DIFFERENCE.getValue();
                SaveDifferenceIn saveDifferenceIn = JSON.parseObject(messageStr, SaveDifferenceIn.class);
                bizOrgCode = saveDifferenceIn.getBizOrgCode();
                businessNo = saveDifferenceIn.getDeliveryOrderNo();
                key = DisSystemConstant.REDIS_DIS_SAVE_DIR_DIFFERENCE + bizOrgCode
                        + SystemConstant.COLON + businessNo + SystemConstant.COLON + saveDifferenceIn.getDifferenceType();
                if (!redisService.setIfAbsent(key, businessNo + saveDifferenceIn.getDifferenceType(), 2L, TimeUnit.MINUTES)) {
                    log.error("配销单{}收货创建差异单重复消费", businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.disDeliveryToDifference(saveDifferenceIn);
            }
            if (ErpDisMqTagsEnum.DIS_PURCHASE_ORDER_TO_ERP.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_PURCHASE_ORDER_TO_ERP;
                logPrefix = ErpDisMqTagsEnum.DIS_PURCHASE_ORDER_TO_ERP.getValue();
                List<TransferNoticePurchaseVO> transferNoticePurchaseVOList = JSON.parseArray(messageStr, TransferNoticePurchaseVO.class);
                bizOrgCode = transferNoticePurchaseVOList.get(0).getBizOrgCode();
                businessNo = transferNoticePurchaseVOList.get(0).getPurchaseOrderNo();
                key = DisSystemConstant.REDIS_DIS_PURCHASE_ORDER_TO_ERP + bizOrgCode + SystemConstant.COLON + businessNo;
                if (!redisService.setIfAbsent(key, businessNo, 2L, TimeUnit.MINUTES)) {
                    log.error("采购订单{}回传加盟配销单回传重复处理", businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.disPurchaseOrderToErp(transferNoticePurchaseVOList);
            }
            if (ErpDisMqTagsEnum.ORDER_DIS_DELIVERY_DATA_FILE.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.ORDER_DIS_DELIVERY_DATA_FILE;
                logPrefix = ErpDisMqTagsEnum.ORDER_DIS_DELIVERY_DATA_FILE.getValue();
                OrdDeliveryDataFile ordDeliveryDataFile = JSON.parseObject(messageStr, OrdDeliveryDataFile.class);
                bizOrgCode = ordDeliveryDataFile.getBizOrgCode();
                resultFlag = disAsyncTaskItemService.execOrderDeliveryDataFile(ordDeliveryDataFile);
            }
            if (ErpDisMqTagsEnum.HANDLE_WHOLESALE_SHIPMENT_PURCHASE_BACK.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_PURCHASE_BACK;
                logPrefix = ErpDisMqTagsEnum.HANDLE_WHOLESALE_SHIPMENT_PURCHASE_BACK.getValue();
                List<TransferShipmentPushPurchaseBackVO> shipmentPushPurchaseBackVOList = JSON.parseArray(messageStr, TransferShipmentPushPurchaseBackVO.class);
                bizOrgCode = shipmentPushPurchaseBackVOList.get(0).getBizOrgCode();
                businessNo = shipmentPushPurchaseBackVOList.get(0).getPurchaseOrderNo();
                resultFlag = disAsyncTaskItemService.handleWholesaleShipmentPurchaseBack(shipmentPushPurchaseBackVOList);
            }
        } catch (Exception e) {
            log.error("加盟{}消息{}消费异常，参数{}", logPrefix, mqMessage.getMessageId(), messageStr, e);
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
