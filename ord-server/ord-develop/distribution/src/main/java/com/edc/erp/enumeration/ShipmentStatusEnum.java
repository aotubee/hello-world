package com.edc.erp.enumeration;

/**
 * 出货状态 枚举
 * @author lx
 * @since 2022-10-18 15:13:04
 */
public enum ShipmentStatusEnum {
    /**
     * 待审核状态
     */
    PENDING("pending", "待审核"),
    /**
     * 已审核状态
     */
    APPROVED("approved", "已审核"),
    /**
     * 已发货状态
     */
    SHIPPED("shipped", "已发货"),
    /**
     * 已作废状态
     */
    INVALID("invalid", "已作废"),
    /**
     * 已收货状态
     */
    RECEIPT("receipt","已收货")
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    ShipmentStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code){
        for (ShipmentStatusEnum ele : values()){
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }

        return null;
    }

    public static String getCodeByName(String name){
        for (ShipmentStatusEnum ele : values()){
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }

        return null;
    }
}
