package com.edc.erp.common.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OptAppNotification;
import com.edc.erp.common.enumeration.warning.MessagePushTemplateEnum;
import com.edc.erp.common.model.in.unipush.OptAppNotificationIn;
import com.edc.erp.common.rpc.AppNotificationClient;
import com.edc.erp.common.service.AppNotificationService;
import com.edc.plugins.common.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;

/**
 * @author lishaobo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppNotificationServiceImpl implements AppNotificationService {

    private final AppNotificationClient appNotificationClient;

    @Override
    public void sendPaymentSuccessFulStationMessage(String orderNo, BigDecimal amount, String receiver) {
        OptAppNotification optAppNotification = new OptAppNotification();
        optAppNotification.setReceiver(receiver);
        optAppNotification.setTitle(MessagePushTemplateEnum.PAYMENT_SUCCESSFUL.getTitle());
        String content = MessageFormat.format(MessagePushTemplateEnum.PAYMENT_SUCCESSFUL.getValue(), orderNo, amount);
        optAppNotification.setContent(content);
        optAppNotification.setSendTime(LocalDateTime.now());
        optAppNotification.setCreator(SystemConstant.SYSTEM_USER);
        Response<String> response = appNotificationClient.saveNotification(optAppNotification);
        if (!response.isSuccess()) {
            log.error("发送保存个推消息失败,单号:{},接收人{},错误信息{}", orderNo, receiver, response.getMessage());
        }
    }

    @Override
    public void sendPendingPaymentNotificationBarMessage(String orderNo, BigDecimal amount, String receiver) {
        OptAppNotificationIn optAppNotificationIn = new OptAppNotificationIn();
        optAppNotificationIn.setReceiver(receiver);
        String content = MessageFormat.format(MessagePushTemplateEnum.PENDING_PAYMENT.getValue(), orderNo, amount);
        optAppNotificationIn.setContent(content);
        optAppNotificationIn.setSender(SystemConstant.SYSTEM_USER);
        optAppNotificationIn.setSendTime(LocalDateTime.now());
        optAppNotificationIn.setTitle(MessagePushTemplateEnum.PENDING_PAYMENT.getTitle());
        Response<String> response = appNotificationClient.sendMessageToSingle(optAppNotificationIn);
        if (!response.isSuccess()) {
            log.error("推送个推消息失败,单号:{},接收人{},错误信息{}", orderNo, receiver, response.getMessage());
        }
    }
}
