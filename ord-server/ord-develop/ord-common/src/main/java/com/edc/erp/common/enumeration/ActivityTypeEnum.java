package com.edc.erp.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 活动类型枚举类，包含标签
 */
/**
 * @description: 活动类型枚举类，包含标签
 * @author fxw
 * @since 2022-11-22
 */
public enum ActivityTypeEnum {

    /** 买赠 */
    FREE_GIFT("freeGift", "买赠"),

    /** 特价 */
    SPECIAL_PRICE("specialPrice", "特价");

    private final String code;
    private final String tagName;

    ActivityTypeEnum(String code, String tagName) {
        this.code = code;
        this.tagName = tagName;
    }

    public String getCode() {
        return code;
    }

    public String getTagName() {
        return tagName;
    }
    public static String getCode(ActivityTypeEnum activityTypeEnum) {
        return activityTypeEnum.code;
    }

    public static String getTagName(ActivityTypeEnum activityTypeEnum) {
        return activityTypeEnum.tagName;
    }

    public static String getTagNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ActivityTypeEnum activityTypeEnum : ActivityTypeEnum.values()) {
            if (activityTypeEnum.code.equals(code)) {
                return activityTypeEnum.tagName;
            }
        }
        return "";
    }
}
