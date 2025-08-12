package com.edc.erp.enumeration;

/**
 * 单据追踪业务类型枚举类
 * @author fxw
 */
public enum OrderTrackBusinessTypeEnum {
    /**
     * 订货订单
     */
    ORDER("订货订单");

    private final String name;

    OrderTrackBusinessTypeEnum(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public static String getName(OrderTrackBusinessTypeEnum activityTypeEnum) {
        return activityTypeEnum.name;
    }

}
