package com.edc.erp.common.enumeration;

/**
 * 库存增减
 * @author weichao
 */
public enum AdjustTypeEnum {
    /**
     * 增
     */
    ADD("add", "增"),
    /**
     * 减
     */
    REDUCE("reduce", "减"),;
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    AdjustTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (AdjustTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
