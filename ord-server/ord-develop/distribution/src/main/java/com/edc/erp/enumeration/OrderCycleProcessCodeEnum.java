package com.edc.erp.enumeration;

/**
 * 单据生命周期流程枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021年6月28日 14:52
 */
public enum OrderCycleProcessCodeEnum {

    /**
     * 订单创建
     */
    ORDER_CREATE("P000001", "订单创建"),
    /**
     * 订单支付
     */
    ORDER_PAY("P000002", "订单支付"),
    /**
     * 合并要货
     */
    REQUEST_ORDER_CREATE("P000003", "合并要货"),
    /**
     * 拆分配货
     */
    DELIVERY_ORDER_CREATE("P000004", "拆分配货"),
    /**
     * 订单确认
     */
    DELIVERY_CONFIRM("P000005", "订单确认");

    private String code;
    private String value;

    OrderCycleProcessCodeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

}
