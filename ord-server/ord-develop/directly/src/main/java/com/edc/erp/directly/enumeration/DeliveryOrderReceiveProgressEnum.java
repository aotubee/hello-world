package com.edc.erp.directly.enumeration;

/**
 * 配货单收货进度枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020年5月26日 14:52
 */
public enum DeliveryOrderReceiveProgressEnum {

    /**
     * 配货单收货进度
     */
    CAN_BEGIN(1, "可开始收货"),
    ONGOING(2, "收货中"),
    END(3, "收货结束");

    private Integer key;
    private String value;

    DeliveryOrderReceiveProgressEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByKey(Integer key) {
        for (DeliveryOrderReceiveProgressEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
