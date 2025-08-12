package com.edc.erp.directly.enumeration;

/**
 * 直营订货单状态枚举
 *
 * @author wanglidong
 * @since 2022/11/16 12:32
 */
public enum OrderStatusEnum {

    /**
     * 直营订货单状态枚举
     */

    SUBMIT("submit", "已提交"),

    /**
     * 已付款
     */
    PAID("paid", "已付款"),

    /**
     * 已转单
     *
     */
    TO_REQUEST_ORDER("toRequestOrder", "已转单"),

    /**
     * 已作废
     *
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