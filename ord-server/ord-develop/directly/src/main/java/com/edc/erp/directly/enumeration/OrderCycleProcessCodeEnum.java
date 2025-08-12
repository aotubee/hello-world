package com.edc.erp.directly.enumeration;

/**
 * 单据生命周期枚举
 *
 * @author wanglidong
 * @since 2022/11/16 15:20
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