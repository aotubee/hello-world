package com.edc.erp.common.service;

import java.math.BigDecimal;

/**
 * @author lishaobo
 * @description: app个推接口
 * @since 2023/1/7 14:42
 */
public interface AppNotificationService {

    /**
     * 保存订单成功支付个推消息
     * @param orderNo
     * @param amount
     * @param receiver
     */
    void sendPaymentSuccessFulStationMessage(String orderNo, BigDecimal amount, String receiver);

    /**
     * 推送订单待支付个推消息
     * @param orderNo
     * @param amount
     * @param receiver
     */
    void sendPendingPaymentNotificationBarMessage(String orderNo, BigDecimal amount, String receiver);
}
