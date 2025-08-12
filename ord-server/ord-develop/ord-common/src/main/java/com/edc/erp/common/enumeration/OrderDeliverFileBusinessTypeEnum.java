package com.edc.erp.common.enumeration;

/**
 * 单据数据文件业务类别
 */
public enum OrderDeliverFileBusinessTypeEnum {
    /**
     * 配货
     */
    DIR("dirDeliveryFileType", "配货"),
    /**
     * 配销
     */
    DIS("disDeliveryFileType", "配销")
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    OrderDeliverFileBusinessTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (OrderDeliverFileBusinessTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
