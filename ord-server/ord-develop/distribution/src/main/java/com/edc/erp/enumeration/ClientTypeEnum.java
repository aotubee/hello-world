package com.edc.erp.enumeration;

/**
 * 客户类型 枚举
 * @author lx
 * @since 2022-10-19 15:10:13
 */
public enum ClientTypeEnum {
    /*
     *批发商
     */
    WHOLESALE_BIZ("wholesaleBiz","批发商"),
    /**
     * 加盟商
     */
    FRANCHISEE_BIZ("franchiseeBiz","加盟商"),
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    ClientTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (ClientTypeEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (ClientTypeEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
