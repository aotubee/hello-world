package com.edc.erp.common.enumeration;

/**
 * 单据数据文件类型
 */
public enum OrderDeliverFileTypeEnum {
    /**
     * 配货单
     */
    DIR_DELIVERY("ordDirDelivery", "配货单"),
    /**
     * 退货单
     */
    DIR_RETURN("ordDirReturn", "配货退货单"),
    /**
     * 配销单
     */
    DIS_DELIVERY("ordDisDelivery", "配销单"),
    /**
     * 配销退货单
     */
    DIS_RETURN("ordDisReturn", "配销退货单")
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    OrderDeliverFileTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (OrderDeliverFileTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
