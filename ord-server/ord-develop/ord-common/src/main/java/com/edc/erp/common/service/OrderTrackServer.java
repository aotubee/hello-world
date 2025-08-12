package com.edc.erp.common.service;

/**
 * 订单追踪处理
 *
 * @author wei
 */
public interface OrderTrackServer {
    /**
     * 直营接收消息的方法
     * @param message
     */
    void dirReceiveMessage(String message);

    /**
     * 配销接收消息的方法
     * @param message
     */
    void disReceiveMessage(String message);
}
