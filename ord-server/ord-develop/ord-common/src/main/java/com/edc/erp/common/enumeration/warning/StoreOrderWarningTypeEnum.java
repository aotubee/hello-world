package com.edc.erp.common.enumeration.warning;

/**
 *
 * @author weichao
 */
public enum StoreOrderWarningTypeEnum {
    /**
     * 增
     */
    DIRECTLY("directly", "直营订货"),
    /**
     * 减
     */
    FRANCHISE("franchise", "配销管理"),;
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    StoreOrderWarningTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (StoreOrderWarningTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
