package com.edc.erp.enumeration;

/**
 * @author fxw
 * @description: 订货单状态枚举
 * @since 2022/10/17 20:16
 */
public enum OrderStatusEnum {
    /**
     * 待付款
     */
    WAIT_PAYMENT("waitPayment", "待付款"),
    /**
     * 已付款
     */
    PAID("paid", "已付款"),
    /**
     * 已转单
     */
    TO_REQUEST_ORDER("toRequestOrder", "已转单"),
    /**
     * 支付中
     */
    PAYING("paying", "支付中"),
    /**
     * 已作废
     */
    INVALID("invalid", "已作废");

    private String key;
    private String value;

    OrderStatusEnum(String key, String value) {
        this.key = key;
        this.value = value;

    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByKey(String key) {
        for (OrderStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
