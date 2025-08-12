package com.edc.erp.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * @author lishaobo
 * @description: 活动限量方式枚举
 * @since 2022/10/17 20:16
 */
public enum ActivityLimitedEnum {
    /**
     * 活动总量
     */
    TOTAL_ACTIVITY("totalActivity", "活动总量"),

    /**
     * 门店每日
     */
    STORE_EVERY_DAY("storeEveryDay", "门店每日"),
    /**
     * 门店
     */
    STORE("store", "门店"),

    /**
     * 不限量
     */
    UNLIMITED("unlimited", "不限量");

    private final String code;
    private final String name;

    ActivityLimitedEnum(String code, String value) {
        this.code = code;
        this.name = value;

    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static String getNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ActivityLimitedEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
