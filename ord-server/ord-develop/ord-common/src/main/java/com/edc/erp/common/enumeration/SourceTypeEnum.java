package com.edc.erp.common.enumeration;

/**
 * 清单状态枚举
 *
 * @author : fxw
 * @date 2022-10-17
 */
public enum SourceTypeEnum {

    /**
     * 手工订货
     */
    INITIATIVE("manual", "手工订货"),

    /**
     * 人工分货
     *
     */
    DISTRIBUTION("distribution", "人工分货"),

    /**
     * 上下限跑货
     */
    UPLOWDOWN("upLowDown","上下限跑货"),

    HEAD_OFFICE_REPLENISH("headOfficeReplenish","总部补货");


    private String key;
    private String value;

    SourceTypeEnum(String key, String value) {
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
        for (SourceTypeEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
