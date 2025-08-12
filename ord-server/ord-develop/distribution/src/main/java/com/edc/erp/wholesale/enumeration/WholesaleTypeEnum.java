package com.edc.erp.wholesale.enumeration;

/**
 * 直营订货单状态枚举
 *
 * @author wanglidong
 * @since 2022/11/16 12:32
 */
public enum WholesaleTypeEnum {

    /**
     * 直营订货单状态枚举
     */

    ORDER("order", "批发出"),

    /**
     * 已付款
     */
    RETURN("return", "批发退"),
    ;

    private String orderType;
    private String value;

    WholesaleTypeEnum(String orderType, String value) {
        this.orderType = orderType;
        this.value = value;

    }

    public String getOrderType() {
        return this.orderType;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByKey(String orderType) {
        for (WholesaleTypeEnum ele : values()) {
            if (ele.getOrderType().equals(orderType)) {
                return ele.getValue();
            }
        }
        return null;
    }
}