package com.edc.erp.directly.mq.dts;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceService;
import com.edc.erp.directly.enumeration.ErpDirMqTagsEnum;
import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.service.OrdDirReturnService;
import com.edc.plugins.jms.MessageProcessor;
import com.edc.plugins.jms.entity.MqMessage;
import com.edc.plugins.redis.RedisService;
import com.edc.sdk.dts.model.order.in.DifferenceBillIn;
import com.edc.sdk.dts.model.order.in.UnificationBillIn;
import com.edc.sdk.dts.model.order.in.UnificationReBillIn;
import com.edc.sdk.dts.model.order.vo.DifferenceBillVO;
import com.edc.sdk.dts.model.order.vo.UnificationBillVO;
import com.edc.sdk.dts.model.order.vo.UnificationReBillVO;
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
@Service("dirErpDtsConsumerHandle")
@Slf4j
@RequiredArgsConstructor
public class DirErpDtsConsumerHandle implements MessageProcessor {

    private final DirAsyncTaskItemService dirAsyncTaskItemService;
    private final AsyncPushTaskService asyncPushTaskService;
    private final OrdDirDeliveryService ordDirDeliveryService;
    private final RedisService redisService;
    private final OrdDirDelivDifferenceService ordDirDelivDifferenceService;
    private final OrdDirReturnService ordDirReturnService;


    @Override
    public boolean process(MqMessage mqMessage) {
        log.info("直营Erp-Dts单据流消费入参：{},{},{},{}", mqMessage.getTopic(), mqMessage.getTag(), mqMessage.getMessageId(), new String(mqMessage.getBody()));
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
            if (ErpDirMqTagsEnum.DIR_DELIVERY_TO_DTS.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS;
                logPrefix = ErpDirMqTagsEnum.DIR_DELIVERY_TO_DTS.getValue();
                UnificationBillIn unificationBillIn = JSON.parseObject(messageStr, UnificationBillIn.class);
                resultFlag = dirAsyncTaskItemService.dirDeliveryToDts(unificationBillIn);
                businessNo = unificationBillIn.getPlatform_bill_id();
                OrdDirDeliveryOut ordDirDeliveryOut = ordDirDeliveryService.getDeliveryOrderOutByNo(businessNo);
                bizOrgCode = ordDirDeliveryOut.getBizOrgCode();
            }
            if (ErpDirMqTagsEnum.DIR_DIFFERENCE_ORDER_TO_DTS.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_DIFFERENCE_ORDER_TO_DTS;
                logPrefix = ErpDirMqTagsEnum.DIR_DIFFERENCE_ORDER_TO_DTS.getValue();
                DifferenceBillIn differenceBillIn = JSON.parseObject(messageStr, DifferenceBillIn.class);
                resultFlag = dirAsyncTaskItemService.dirDifferenceOrderToDts(differenceBillIn);
                businessNo = differenceBillIn.getPlatform_bill_id();
                OrdDirDelivDifference ordDirDelivDifference = ordDirDelivDifferenceService.getOneByOrderNo(businessNo);
                bizOrgCode = ordDirDelivDifference.getBizOrgCode();
            }
            if (ErpDirMqTagsEnum.DIR_RETURN_TO_DTS.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_RETURN_TO_DTS;
                logPrefix = ErpDirMqTagsEnum.DIR_RETURN_TO_DTS.getValue();
                UnificationReBillIn unificationReBillIn = JSON.parseObject(messageStr, UnificationReBillIn.class);
                resultFlag = dirAsyncTaskItemService.dirReturnToDts(unificationReBillIn);
                businessNo = unificationReBillIn.getPlatform_bill_id();
                OrdDirReturn ordDirReturn = ordDirReturnService.getReturnOrderByNo(businessNo);
                bizOrgCode = ordDirReturn.getBizOrgCode();
            }
            if (ErpDirMqTagsEnum.DIR_DELIVERY_DTS_TO_ERP.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_DELIVERY_DTS_TO_ERP;
                logPrefix = ErpDirMqTagsEnum.DIR_DELIVERY_DTS_TO_ERP.getValue();
                UnificationBillVO unificationBillVO = JSON.parseObject(messageStr, UnificationBillVO.class);
                bizOrgCode = unificationBillVO.getFdestorg();
                businessNo = unificationBillVO.getFsrcnum();
                key = DirSystemConstant.REDIS_DIR_DELIVERY_DTS_TO_ERP + unificationBillVO.getFdestorg() + SystemConstant.COLON + unificationBillVO.getFsrcnum();
                if (!redisService.setIfAbsent(key, unificationBillVO.getFsrcnum(), 2L, TimeUnit.MINUTES)) {
                    log.error("DTS配货单{}数据回传重复消费", unificationBillVO.getFsrcnum());
                    return true;
                }
                resultFlag = dirAsyncTaskItemService.unificationOrderCallBack(unificationBillVO);
            }
            if (ErpDirMqTagsEnum.DIR_RETURN_DTS_TO_ERP.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_RETURN_DTS_TO_ERP;
                logPrefix = ErpDirMqTagsEnum.DIR_RETURN_DTS_TO_ERP.getValue();
                UnificationReBillVO unificationReBillVO = JSON.parseObject(messageStr, UnificationReBillVO.class);
                key = DirSystemConstant.REDIS_DIR_RETURN_DTS_TO_ERP + unificationReBillVO.getFdestorg() + SystemConstant.COLON + unificationReBillVO.getFsrcnum();
                if (!redisService.setIfAbsent(key, unificationReBillVO.getFsrcnum(), 2L, TimeUnit.MINUTES)) {
                    log.error("DTS直营退货单{}数据回传重复消费", unificationReBillVO.getFsrcnum());
                    return true;
                }
                bizOrgCode = unificationReBillVO.getFdestorg();
                businessNo = unificationReBillVO.getFsrcnum();
                resultFlag = dirAsyncTaskItemService.unificationReOrderCallBack(unificationReBillVO);
            }
            if (ErpDirMqTagsEnum.DIR_DIFFERENCE_DTS_TO_ERP.getTag().equals(tag)) {
                type = AsyncTaskConstant.Type.DIR_DIFFERENCE_DTS_TO_ERP;
                logPrefix = ErpDirMqTagsEnum.DIR_DIFFERENCE_DTS_TO_ERP.getValue();
                DifferenceBillVO differenceBillVO = JSON.parseObject(messageStr, DifferenceBillVO.class);
                key = DirSystemConstant.REDIS_DIR_DIFFERENCE_DTS_TO_ERP + differenceBillVO.getFdestorg() + SystemConstant.COLON + differenceBillVO.getFsrcnum();
                if (!redisService.setIfAbsent(key, differenceBillVO.getFsrcnum(), 2L, TimeUnit.MINUTES)) {
                    log.error("DTS直营差异单{}数据回传重复消费", differenceBillVO.getFsrcnum());
                    return true;
                }
                bizOrgCode = differenceBillVO.getFdestorg();
                businessNo = differenceBillVO.getFsrcnum();
                resultFlag = dirAsyncTaskItemService.differenceOrderCallBack(differenceBillVO);
            }
        } catch (Exception e) {
            log.error("直营DTS{}消息{}消费异常，参数{}", logPrefix, mqMessage.getMessageId(), messageStr, e);
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
