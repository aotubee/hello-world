package com.edc.erp.common.enumeration.warning;

/**
 * 预警业务类型枚举类型枚举类
 */
public enum WarningBusinessTypeEnum {

    UP_LOWER_LIMIT("upLowerLimit", "上下限"),
    NEW_ORDER("newOrder", "新订货订单"),
    DIR_REQUEST_ORDER("dirRequestOrder", "要货单"),
    DIS_REQUEST_ORDER("disRequestOrder", "集货单"),
    DIR_DELIVERY_ORDER("dirDeliveryOrder", "配货单"),
    DIS_DELIVERY_ORDER("disDeliveryOrder", "配销单"),
    NO_AUTO_SALVAGE("noAutoSalvage", "过点未自动捞单"),

    ;

    private final String businessType;
    private final String name;

    WarningBusinessTypeEnum(String businessType, String name) {
        this.businessType = businessType;
        this.name = name;
    }

    public String getBusinessType() {
        return businessType;
    }

    public String getName() {
        return name;
    }


}
