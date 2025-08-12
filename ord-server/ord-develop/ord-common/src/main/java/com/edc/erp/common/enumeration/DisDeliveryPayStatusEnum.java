package com.edc.erp.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 活配销单交易状态枚举
 */

/**
 * @description: 活配销单交易状态枚举
 * @author zy
 * @since 2023-02-16
 */
public enum DisDeliveryPayStatusEnum {

    PAID("paid", "已付款"),

    REFUNDED("refunded", "已退款");

    private final String code;
    private final String tagName;

    DisDeliveryPayStatusEnum(String code, String tagName) {
        this.code = code;
        this.tagName = tagName;
    }

    public String getCode() {
        return code;
    }

    public String getTagName() {
        return tagName;
    }
    public static String getCode(DisDeliveryPayStatusEnum activityTypeEnum) {
        return activityTypeEnum.code;
    }

    public static String getTagName(DisDeliveryPayStatusEnum activityTypeEnum) {
        return activityTypeEnum.tagName;
    }

    public static String getTagNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (DisDeliveryPayStatusEnum activityTypeEnum : DisDeliveryPayStatusEnum.values()) {
            if (activityTypeEnum.code.equals(code)) {
                return activityTypeEnum.tagName;
            }
        }
        return "";
    }
}
