package com.edc.erp.common.enumeration;

/**
 *仓储运行枚举
 */
public enum OrdStockBusinessSwitchEnum {

    /**
     * 要货配货
     */
    DIR_REQUEST_DELIVERY("dirRequestDelivery","is_dis_logc"),
    /**
     * 允许配货退货
     */
     DIR_RETURN("dirReturn","is_dis_re_logc"),
    /**
     * 允许配销上下限跑货
     */
     UP_LOW_LIMIT("up_low_limit","is_up_low_limit_dis_logc"),
    /**
     * 允许集货配销
     */
    DIS_REQUEST_DELIVERY("disRequestDelivery","is_dis_dis_logc"),
    /**
     * 允许配销退货
     */
     DIS_RETURN("disReturn","is_dis_re_dis_logc");
    ;


    private String code;

    private String column;

    OrdStockBusinessSwitchEnum(String code, String column) {
        this.code = code;
        this.column = column;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public static String getColumnByCode(String code) {
        for (OrdStockBusinessSwitchEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getColumn();
            }
        }
        return null;
    }


    public static String getCodeByColumn(String column) {
        for (OrdStockBusinessSwitchEnum ele : values()) {
            if (ele.getColumn().equals(column)) {
                return ele.getCode();
            }
        }
        return null;
    }
}
