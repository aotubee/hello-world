package com.edc.erp.common.enumeration;

/**
 * 配销单状态枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020年5月26日 14:52
 */
public enum DeliveryOrderEnum {
    /** 待审核 */
    PENDING("pending", "待审核"),

    /** 已预审 */
    PREVIEWAPPROVED("previewApproved","已预审"),

    /** 已审核 */
    APPROVED("approved", "已审核"),

    /** 已作废 */
    INVALID("invalid","已作废"),

    /** 已完成 */
    COMPLETED("completed", "已完成"),

    /** 已收货 */
    RECEIVED("received", "已收货"),

    /** 已发货 */
    SHIPPED("shipped", "已发货");

    private String key;
    private String value;

    DeliveryOrderEnum(String key, String value) {
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
        for (DeliveryOrderEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }

    public enum DeliveryOrderTypeEnum {

        /** 已收货 */
        ORDER_RECEIVED("RECEIVED", "已收货"),

        /** 已发货 */
        ORDER_SHIPPED("SHIPPED", "已发货");


        private String key;
        private String value;

        DeliveryOrderTypeEnum(String key, String value) {
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
            for (DeliveryOrderTypeEnum ele : values()) {
                if (ele.getKey().equals(key)) {
                    return ele.getValue();
                }
            }
            return null;
        }
    }
}
