package com.edc.erp.enumeration;

/**
 * @author lx
 * @since 2022-10-28 12:31:42
 */
public enum WholesaleOrderTypeEnum {
    /**
     * 出货
     */
    OUT("out","出货"),
    /**
     * 退货
     */
    RETURNS("returns","退货"),
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    WholesaleOrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (WholesaleOrderTypeEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (WholesaleOrderTypeEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
