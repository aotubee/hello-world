package com.edc.erp.returnorder.enumeration;

/**
 *退货单类型枚举
 * @author
 */
public enum OrdReturnOrderTypeEnum {
    /**
     *特退
     */
    SPECIAL_RETURN("specialReturn", "特退"),
    /**
     * 普通
     */
    NORMAL_RETURN("normalReturn", "普通"),
    /**
     * 限量
     */
    LIMITED_RETURN("limitedReturn", "限量"),
    /*8
    后台退货
     */
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
