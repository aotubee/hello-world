package com.edc.erp.enumeration;

/**
 * 退货单日志模板枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022年7月13日 11:00
 */
public enum ReturnNoticeOrderLogEnum {

    /**
     * 退货通知单单
     */
    RETURN_ORDER_STATUS_UPDATE("退货通知单单{0}状态由{1}变更为{2}。", "returnOrderStatus"),
    /**
     * 创建退货单通知单
     */
    RETURN_NOTICE_CREATE("{0}创建退货单通知单：{1}。", "退货通知单"),
    ;

    private String key;
    private String globalType;

    ReturnNoticeOrderLogEnum(String key, String globalType) {
        this.key = key;
        this.globalType = globalType;
    }

    public String getKey() {
        return this.key;
    }

    public String getGlobalType() {
        return this.globalType;
    }

}
