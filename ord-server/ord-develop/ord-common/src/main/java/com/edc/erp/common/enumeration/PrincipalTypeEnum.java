package com.edc.erp.common.enumeration;

/**
 * 主体类型枚举
 * @author jushaofei
 * @since 2022-08-08
 */
public enum PrincipalTypeEnum {
    /**
     * 主体类型---组织
     */
    ORGANIZATION("organization", "组织"),
    /**
     * 主体类型---客户
     */
    CUSTOMER("customer", "客户"),
    /**
     * 主体类型---门店
     */
    STORE("store", "门店"),
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    PrincipalTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (PrincipalTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
