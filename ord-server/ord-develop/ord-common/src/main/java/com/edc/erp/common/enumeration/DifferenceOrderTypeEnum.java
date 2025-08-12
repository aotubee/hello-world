package com.edc.erp.common.enumeration;

/**
 * 差异类型
 *
 * @author weichao
 */
public enum DifferenceOrderTypeEnum {
    /**
     * 正差异差异
     */
    POSITIVE_DIFFERENT("positiveDifferent", "正差异差异"),
    /**
     * 正差异破损
     */
    POSITIVE_WORN("positiveWorn", "正差异破损"),
    /**
     * 负差异差异
     */
    MINUS_DIFFERENT("minusDifferent",  "负差异差异"),
    /**
     * 负差异破损
     */
    MINUS_WORN("minusWorn",  "负差异破损");


    private final String code;
    private final String name;

    DifferenceOrderTypeEnum(String code, String name) {

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
        for (DifferenceOrderTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
