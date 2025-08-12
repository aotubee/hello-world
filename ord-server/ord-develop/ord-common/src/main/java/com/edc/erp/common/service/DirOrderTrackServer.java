package com.edc.erp.common.service;

/**
 * 订单追踪处理
 *
 * @author wei
 */
public interface DirOrderTrackServer {
    /**
     * 直营接收消息的方法
     * @param message
     */
    void dirReceiveMessage(String message);


}
