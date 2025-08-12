package com.edc.erp.common.enumeration;

/**
 * 标准商品类型
 * @author lee
 */
public enum GoodsTypeEnum {
    /**
     * 商品
     */
    GOODS("goods", "商品"),

    /**
     * 原料
     */
    RAW_MATERIAL("rawMaterial", "原料"),

    /**
     * 物料
     */
    MATERIAL("material", "物料"),
    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    GoodsTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getCodeByName(String name) {
        for (GoodsTypeEnum ele : values()) {
            if (ele.getName().equals(name)) {
                return ele.getCode();
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {
        for (GoodsTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
