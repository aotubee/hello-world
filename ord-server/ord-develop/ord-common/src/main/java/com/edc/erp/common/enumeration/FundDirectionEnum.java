package com.edc.erp.common.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * 资管交易类型枚举
 */

/**
 * @description: 资管交易类型枚举
 * @author zy
 * @since 2023-02-03
 */
public enum FundDirectionEnum {

    /** 收入 */
    PAY("in", "收入"),

    /** 退款用 */
    RETURN("out", "支出");

    private final String code;
    private final String name;

    FundDirectionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
    public static String getCode(FundDirectionEnum activityTypeEnum) {
        return activityTypeEnum.code;
    }

    public static String getTagName(FundDirectionEnum activityTypeEnum) {
        return activityTypeEnum.name;
    }

    public static String getTagNameByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (FundDirectionEnum activityTypeEnum : FundDirectionEnum.values()) {
            if (activityTypeEnum.code.equals(code)) {
                return activityTypeEnum.name;
            }
        }
        return "";
    }
}
