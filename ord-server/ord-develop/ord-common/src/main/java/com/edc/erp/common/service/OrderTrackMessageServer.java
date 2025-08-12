package com.edc.erp.common.service;

/**
 * 订单追踪处理类
 * @author wei
 */
public interface OrderTrackMessageServer {
    /**
     * 接收消息
     * @param message
     */
    void receiveMessage(String message);

}
