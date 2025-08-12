package com.edc.erp.common.enumeration;

/**
 * 资管返款场景类型枚举
 */

/**
 * @author zy
 * @description: 资管返款场景类型枚举
 * @since 2023-02-03
 */
public enum FundReturnTypeEnum {

    DIS_ORDER_INVALID("订货单作废"),

    DIS_REQUEST_ORDER_ILLEGAL_GOODS("集货单不合法商品"),

    DIS_DELIVERY_ORDER_INVALID("配销单作废"),

    DIS_DELIVERY_ORDER_CHARGE("配销单冲销"),

    DIS_DELIVERY_ORDER_STOCK_OUT("配销单缺货"),

//    DIS_RETURN_ORDER_INVALID("配销退货单作废"),

    DIS_RETURN_ORDER_CHARGE("配销退货单冲销"),

    DIS_RETURN_ORDER_RECEIVE("配销退货单收货"),

//    DIS_DIFFERENCE_ORDER_INVALID("差异单作废"),

    DIS_DIFFERENCE_ORDER_CHARGE("差异单冲销"),

    WHOLESALE_SHIPMENT_INVALID("批发出作废"),

    WHOLESALE_SHIPMENT_CHARGE("批发出冲销"),

    WHOLESALE_SHIPMENT_SHIPPED("批发出发货"),

//    WHOLESALE_RETURN_INVALID("批发退作废"),

    WHOLESALE_RETURN_CHARGE("批发退冲销"),

    DIS_FIRST_ORDER_RETURN("铺货单不合格商品退款"),

    DIS_FIRST_ORDER_INVALID("铺货单作废退款"),

    RETURN_PRESALE_ORDER("预售订单退款"),

    ;

    private final String name;

    FundReturnTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String getCode(FundReturnTypeEnum activityTypeEnum) {
        return activityTypeEnum.name;
    }


}
