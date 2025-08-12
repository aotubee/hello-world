package com.edc.erp.common.enumeration;

/**
 * 库存业务枚举
 * @author weichao
 */
public enum StockHappenLieEnum {
    /**
     * 仓储
     */
    WAREHOUSE("warehouse", "仓储"),
    /**
     * 门店
     */
    STORE("store", "门店"),;
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    StockHappenLieEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (StockHappenLieEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
