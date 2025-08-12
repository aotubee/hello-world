package com.edc.erp.common.service;

/**
 * 预警业务层
 * @author weichao
 */
public interface WarningService {

    /**
     * 推送预警消息
     *  @param businessType
     * @param errorCode
     * @param placeholderErrorMessage
     * @param originalException
     * @param businessParam
     * @param bizOrgCode
     */
    void pushWarningMessage(String businessType, String errorCode, String placeholderErrorMessage, String originalException, String businessParam, String bizOrgCode);

    void pushDBIndexWarningMessage(Integer dbIndex, String topic, String businessType, String errorCode, String placeholderErrorMessage,
                                   String originalException, String businessParam, String bizOrgCode);
}
