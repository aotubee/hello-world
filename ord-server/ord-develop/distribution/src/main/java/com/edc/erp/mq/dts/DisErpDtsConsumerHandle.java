package com.edc.erp.mq.dts;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryOut;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceService;
import com.edc.erp.enumeration.ErpDisMqTagsEnum;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.service.OrdDisReturnService;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.jms.MessageProcessor;
import com.edc.plugins.jms.entity.MqMessage;
import com.edc.plugins.redis.RedisService;
import com.edc.sdk.dts.model.order.in.*;
import com.edc.sdk.dts.model.order.vo.*;
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
@Service("disErpDtsConsumerHandle")
@Slf4j
@RequiredArgsConstructor
public class DisErpDtsConsumerHandle implements MessageProcessor {

    private final AsyncTaskItemService disAsyncTaskItemService;
    private final AsyncPushTaskService asyncPushTaskService;
    private final OrdDisDeliveryService ordDisDeliveryService;
    private final RedisService redisService;
    private final OrdDisDelivDifferenceService ordDisDelivDifferenceService;
    private final OrdDisReturnService ordDisReturnService;
    private final WholesaleShipmentService wholesaleShipmentService;
    private final WholesaleReturnsService wholesaleReturnsService;


    @Override
    public boolean process(MqMessage mqMessage) {
        log.info("加盟Erp-Dts单据流消费入参：{},{},{},{}", mqMessage.getTopic(), mqMessage.getTag(), mqMessage.getMessageId(), new String(mqMessage.getBody()));
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
            if (ErpDisMqTagsEnum.DIS_DELIVERY_TO_DTS.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_DELIVERY_TO_DTS;
                logPrefix = ErpDisMqTagsEnum.DIS_DELIVERY_TO_DTS.getValue();
                UnificationBillIn unificationBillIn = JSON.parseObject(messageStr, UnificationBillIn.class);
                resultFlag = disAsyncTaskItemService.disDeliveryToDts(unificationBillIn);
                businessNo = unificationBillIn.getPlatform_bill_id();
                OrdDisDeliveryOut ordDisDeliveryOut = ordDisDeliveryService.getDeliveryOrderOutByNo(businessNo);
                bizOrgCode = ordDisDeliveryOut.getBizOrgCode();
            }
            if (ErpDisMqTagsEnum.DIS_DIFFERENCE_ORDER_TO_DTS.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_DIFFERENCE_ORDER_TO_DTS;
                logPrefix = ErpDisMqTagsEnum.DIS_DIFFERENCE_ORDER_TO_DTS.getValue();
                DifferenceBillIn differenceBillIn = JSON.parseObject(messageStr, DifferenceBillIn.class);
                resultFlag = disAsyncTaskItemService.disDifferenceOrderToDts(differenceBillIn);
                businessNo = differenceBillIn.getPlatform_bill_id();
                OrdDisDelivDifference ordDisDelivDifference = ordDisDelivDifferenceService.getOneByOrderNo(businessNo);
                bizOrgCode = ordDisDelivDifference.getBizOrgCode();
            }
            if (ErpDisMqTagsEnum.DIS_RETURN_TO_DTS.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_RETURN_TO_DTS;
                logPrefix = ErpDisMqTagsEnum.DIS_RETURN_TO_DTS.getValue();
                UnificationReBillIn unificationReBillIn = JSON.parseObject(messageStr, UnificationReBillIn.class);
                resultFlag = disAsyncTaskItemService.disReturnToDts(unificationReBillIn);
                businessNo = unificationReBillIn.getPlatform_bill_id();
                OrdDisReturn ordDisReturn = ordDisReturnService.getReturnOrderByNo(businessNo);
                bizOrgCode = ordDisReturn.getBizOrgCode();
            }
            if (ErpDisMqTagsEnum.WHOLESALE_SHIPMENT_TO_DTS.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.WHOLESALE_SHIPMENT_TO_DTS;
                logPrefix = ErpDisMqTagsEnum.WHOLESALE_SHIPMENT_TO_DTS.getValue();
                WholesaleBillIn wholesaleBillIn = JSON.parseObject(messageStr, WholesaleBillIn.class);
                resultFlag = disAsyncTaskItemService.wholesaleShipmentToDts(wholesaleBillIn);
                businessNo = wholesaleBillIn.getPlatform_bill_id();
                WholesaleShipment wholesaleShipment = wholesaleShipmentService.getOneByNo(businessNo);
                bizOrgCode = wholesaleShipment.getBizOrgCode();
            }
            if (ErpDisMqTagsEnum.WHOLESALE_RETURN_TO_DTS.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.WHOLESALE_RETURN_TO_DTS;
                logPrefix = ErpDisMqTagsEnum.WHOLESALE_RETURN_TO_DTS.getValue();
                WholesaleReBillIn wholesaleReBillIn = JSON.parseObject(messageStr, WholesaleReBillIn.class);
                resultFlag = disAsyncTaskItemService.wholesaleReturnToDts(wholesaleReBillIn);
                businessNo = wholesaleReBillIn.getPlatform_bill_id();
                WholesaleReturns wholesaleReturns = wholesaleReturnsService.getOneByOrderNo(businessNo);
                bizOrgCode = wholesaleReturns.getBizOrgCode();
            }
            if (ErpDisMqTagsEnum.DIS_DELIVERY_DTS_TO_ERP.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_DELIVERY_DTS_TO_ERP;
                logPrefix = ErpDisMqTagsEnum.DIS_DELIVERY_DTS_TO_ERP.getValue();
                UnificationBillVO unificationBillVO = JSON.parseObject(messageStr, UnificationBillVO.class);
                bizOrgCode = unificationBillVO.getFdestorg();
                businessNo = unificationBillVO.getFsrcnum();
                key = DisSystemConstant.REDIS_DIS_DELIVERY_DTS_TO_ERP + unificationBillVO.getFdestorg() + SystemConstant.COLON + unificationBillVO.getFsrcnum();
                if (!redisService.setIfAbsent(key, unificationBillVO.getFsrcnum(), 2L, TimeUnit.MINUTES)) {
                    log.error("DTS配货单{}数据回传重复消费", unificationBillVO.getFsrcnum());
                    return true;
                }
                resultFlag = disAsyncTaskItemService.unificationOrderCallBack(unificationBillVO);
            }
            if (ErpDisMqTagsEnum.DIS_RETURN_DTS_TO_ERP.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_RETURN_DTS_TO_ERP;
                logPrefix = ErpDisMqTagsEnum.DIS_RETURN_DTS_TO_ERP.getValue();
                UnificationReBillVO unificationReBillVO = JSON.parseObject(messageStr, UnificationReBillVO.class);
                key = DisSystemConstant.REDIS_DIS_RETURN_DTS_TO_ERP + unificationReBillVO.getFdestorg() + SystemConstant.COLON + unificationReBillVO.getFsrcnum();
                if (!redisService.setIfAbsent(key, unificationReBillVO.getFsrcnum(), 2L, TimeUnit.MINUTES)) {
                    log.error("DTS加盟退货单{}数据回传重复消费", unificationReBillVO.getFsrcnum());
                    return true;
                }
                bizOrgCode = unificationReBillVO.getFdestorg();
                businessNo = unificationReBillVO.getFsrcnum();
                resultFlag = disAsyncTaskItemService.unificationReOrderCallBack(unificationReBillVO);
            }
            if (ErpDisMqTagsEnum.DIS_DIFFERENCE_DTS_TO_ERP.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_DIFFERENCE_DTS_TO_ERP;
                logPrefix = ErpDisMqTagsEnum.DIS_DIFFERENCE_DTS_TO_ERP.getValue();
                DifferenceBillVO differenceBillVO = JSON.parseObject(messageStr, DifferenceBillVO.class);
                key = DisSystemConstant.REDIS_DIS_DIFFERENCE_DTS_TO_ERP + differenceBillVO.getFdestorg() + SystemConstant.COLON + differenceBillVO.getFsrcnum();
                if (!redisService.setIfAbsent(key, differenceBillVO.getFsrcnum(), 2L, TimeUnit.MINUTES)) {
                    log.error("DTS加盟差异单{}数据回传重复消费", differenceBillVO.getFsrcnum());
                    return true;
                }
                bizOrgCode = differenceBillVO.getFdestorg();
                businessNo = differenceBillVO.getFsrcnum();
                resultFlag = disAsyncTaskItemService.differenceOrderCallBack(differenceBillVO);
            }
            if (ErpDisMqTagsEnum.WHOLESALE_ORDER_CALL_BACK.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_WHOLESALE_DTS_TO_ERP;
                logPrefix = ErpDisMqTagsEnum.WHOLESALE_ORDER_CALL_BACK.getValue();
                WholesaleBillVO wholesaleBillVO = JSON.parseObject(messageStr, WholesaleBillVO.class);
                bizOrgCode = wholesaleBillVO.getFdestorg();
                businessNo = wholesaleBillVO.getFsrcnum();
                key = SystemConstant.ORD_WHOLESALE_SHIPMENT_DTS_BACK_SHIPPED + SystemConstant.COLON + bizOrgCode
                        + SystemConstant.COLON + businessNo;
                if (!redisService.setIfAbsent(key, businessNo, 1l, TimeUnit.HOURS)) {
                    log.info("批发出货单{}重复处理DTS回传发货", businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.wholesaleOrderCallBack(wholesaleBillVO);
            }
            if (ErpDisMqTagsEnum.WHOLESALE_RE_ORDER_CALL_BACK.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIS_WHOLESALE_RE_DTS_TO_ERP;
                logPrefix = ErpDisMqTagsEnum.WHOLESALE_RE_ORDER_CALL_BACK.getValue();
                WholesaleReBillVO wholesaleReBillVO = JSON.parseObject(messageStr, WholesaleReBillVO.class);
                bizOrgCode = wholesaleReBillVO.getFdestorg();
                businessNo = wholesaleReBillVO.getFsrcnum();
                key = SystemConstant.ORD_WHOLESALE_RETURN_DTS_BACK_SHIPPED + SystemConstant.COLON + bizOrgCode
                        + SystemConstant.COLON + businessNo;
                if (!redisService.setIfAbsent(key, businessNo, 1l, TimeUnit.HOURS)) {
                    log.info("批发退货单{}重复处理DTS回传发货", businessNo);
                    return true;
                }
                resultFlag = disAsyncTaskItemService.wholesaleReOrderCallBack(wholesaleReBillVO);
            }
        } catch (Exception e) {
            log.error("加盟DTS{}消息{}消费异常，参数{}", logPrefix, mqMessage.getMessageId(), messageStr, e);
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
