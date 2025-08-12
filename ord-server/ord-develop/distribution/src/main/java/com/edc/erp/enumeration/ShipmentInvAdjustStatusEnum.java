package com.edc.erp.enumeration;

/**
 * 批发出出货单 库存调整(占用) 状态
 * @author lx
 * @since 2023-01-12 17:27:03
 */
public enum ShipmentInvAdjustStatusEnum {
    APPROVED_INVALID("approvedInvalid","审核后作废"),
    SHIPPED_RUSH_ORDER("shippedRushOrder","发货后冲单")
    ;
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    ShipmentInvAdjustStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (ShipmentInvAdjustStatusEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (ShipmentInvAdjustStatusEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
