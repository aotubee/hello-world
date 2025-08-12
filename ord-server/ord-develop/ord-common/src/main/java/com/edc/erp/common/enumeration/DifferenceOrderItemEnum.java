package com.edc.erp.common.enumeration;

/**
 * @author fxw
 * @description: 差异拆单分类
 * @since 2022/12/15 11:09
 */
public enum DifferenceOrderItemEnum {
    /**
     * 差异
     */
    DIFFERENT("different", "差异"),
    /**
     * 破损
     */
    WORN("worn", "破损");


    private final String code;
    private final String name;

    DifferenceOrderItemEnum(String code, String name) {

        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (DifferenceOrderItemEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
