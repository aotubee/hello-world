package com.edc.erp.ord.handle;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.warning.OrderWarningTypeEnum;
import com.edc.erp.common.model.in.purchase.TransferNoticePurchaseIn;
import com.edc.erp.common.model.in.warning.RobotMarkDownMessageIn;
import com.edc.erp.common.rpc.PurchaseOrderClient;
import com.edc.erp.common.service.DingTalkService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.ord.model.out.TransferDirDeliveryOrderOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DtsErpService
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/3 19:52
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class DtsErpBusinessOrderHandle {

    private final RedisService redisService;

    private final OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    private final PurchaseOrderClient purchaseOrderClient;


    @Value("${dingTalk.zzTransferOrderErrorNoticeUrl}")
    private String zzTransferOrderErrorNoticeUrl;

    @Value("${dingTalk.xaTransferOrderErrorNoticeUrl}")
    private String xaTransferOrderErrorNoticeUrl;

    private final DingTalkService dingTalkService;

    @Transactional(rollbackFor = Exception.class)
    public void updateDtlsCycleAndSendPur(TransferDirDeliveryOrderOut transferDirDeliveryOrderOut, List<TransferNoticePurchaseIn> transferNoticePurchaseIns, String bizOrgCode) {
        redisService.setIfAbsent(transferDirDeliveryOrderOut.getTruncationDateTimeKey(), StringUtils.join(transferDirDeliveryOrderOut.getTruncationDateTime().toArray(), ","), 3, TimeUnit.DAYS);
        //发送采购成功修改订单明细的结算周期
        if (CollectionUtils.isNotEmpty(transferDirDeliveryOrderOut.getOrderDirDeliveryDetails())){
            ordDirDeliveryDetailService.batchUpdate(transferDirDeliveryOrderOut.getOrderDirDeliveryDetails());
        }
        if (CollectionUtils.isNotEmpty(transferDirDeliveryOrderOut.getOrderDisDeliveryDetails())){
            ordDisDeliveryDetailService.batchUpdate(transferDirDeliveryOrderOut.getOrderDisDeliveryDetails());
        }
        Response response = purchaseOrderClient.saveDistributionOrder(transferNoticePurchaseIns);
        if (!response.isSuccess()) {
            redisService.del(transferDirDeliveryOrderOut.getTruncationDateTimeKey());
            String msg = bizOrgCode + "组织中转商品生成采购单失败" + (StringUtils.isNotBlank(response.getMessage()) ? response.getMessage() : null);
            sendDingTalkErrorNotice(bizOrgCode,"中转数据异常:" + msg);
            log.error(msg);
            throw new BusinessException(msg);
        }
    }

    private void sendDingTalkErrorNotice(String bizOrgCode, String resultMsg) {
        String noticeUrl = null;
        if (OrgCodeConvertEnum.ZHENGZHOU_MYT.getBizOrgCode().equals(bizOrgCode)) {
            noticeUrl = zzTransferOrderErrorNoticeUrl;
        } else if (OrgCodeConvertEnum.XIAN_MYT.getBizOrgCode().equals(bizOrgCode)){
            noticeUrl = xaTransferOrderErrorNoticeUrl;
        }
        String content = MessageFormat.format(OrderWarningTypeEnum.TRANSFER_ORDER_ERROR_CARD.getErrorMessage(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), resultMsg);
        RobotMarkDownMessageIn robotMarkDownMessageIn = new RobotMarkDownMessageIn();
        robotMarkDownMessageIn.setTitle(OrderWarningTypeEnum.TRANSFER_ORDER_ERROR_CARD.getType());
        robotMarkDownMessageIn.setText(content);
        robotMarkDownMessageIn.setIsAtAll(true);
        try {
            if (StringUtils.isNotEmpty(noticeUrl)) {
                robotMarkDownMessageIn.setUrl(noticeUrl);
                Response response = dingTalkService.sendDingTalkRobotMarkDownMessage(robotMarkDownMessageIn);
                if (null != response && response.isSuccess()) {
                    log.info("钉钉发送中转数据异常信息" + JSON.toJSONString(response));
                }
            }
        } catch (Exception e) {
            log.error("钉钉发送中转数据异常信息", e);
        }
    }
}
