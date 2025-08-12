package com.edc.erp.returnnoticeorder.enumeration;

/**
 * 允许配销退货枚举
 * @author
 */

public enum IsOrdReturnEnum {
    /**
     * 允许退货
     */
    FRANCHRETURN("is_goods_return","允许退货");


    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    IsOrdReturnEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (IsOrdReturnEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (IsOrdReturnEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
