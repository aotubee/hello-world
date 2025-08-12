package com.edc.erp.directly.enumeration;

/**
 * @return: 单据追踪业务类型枚举类
 * @Author: fxw
 * @Date: 2022/11/23
 */
public enum OrderTrackBusinessTypeEnum {

    /**
     * 单据追踪业务类型枚举类
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
