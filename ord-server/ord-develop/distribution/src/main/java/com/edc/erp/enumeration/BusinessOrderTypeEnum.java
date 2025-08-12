package com.edc.erp.enumeration;

/**
 * 业务订单类型枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020年12月26日 10:52
 */
public enum BusinessOrderTypeEnum {
    /**
     * 订货清单
     */
    PURCHASE_LIST("12002502", "订货清单"),
    /**
     * 充值订单
     */
    RECHARGE_ORDER("12002503", "充值订单"),
    /**
     *直送订单
     */
    DIRECT_DELIVERY_ORDER("12002504", "直送订单"),
    /**
     *订货单
     */
    ORDER("12002505", "订货单");

    private String key;
    private String value;

    BusinessOrderTypeEnum(String key, String value) {
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
        for (BusinessOrderTypeEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
