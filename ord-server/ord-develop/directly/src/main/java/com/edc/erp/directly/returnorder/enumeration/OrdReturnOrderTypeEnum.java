package com.edc.erp.directly.returnorder.enumeration;

/**
 * @return: 退货单类型枚举
 * @Author: fxw
 * @Date: 2022/11/23
 */
public enum OrdReturnOrderTypeEnum {

    /**
     * 退货单类型
     */
    SPECIAL_RETURN("specialReturn", "特退"),
    NORMAL_RETURN("normalReturn", "普通"),
    LIMITED_RETURN("limitedReturn", "限量"),
    BACK_RETURN("backReturn", "后台退货"),
    ;

    private String key;
    private String values;


    OrdReturnOrderTypeEnum(String key, String values) {
        this.key = key;
        this.values = values;

    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.values;
    }



    public static String getValueByKey(String key) {
        for (OrdReturnOrderTypeEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }


}
