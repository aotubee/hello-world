package com.edc.erp.enumeration;

/**
 * 客户类型 枚举
 * @author lx
 * @since 2022-10-19 15:10:13
 */
public enum OrderIdentificationEnum {
    /*
     *批发商
     */
    NORMAL_ORDER("normalOrder","普通"),
    /**
     * 加盟商
     */
    PRESALE_ORDER("presaleOrder","预售"),
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    OrderIdentificationEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (OrderIdentificationEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (OrderIdentificationEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
