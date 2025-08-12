package com.edc.erp.enumeration;

/**
 * 分货导入模板错误枚举
 * @author
 */
public enum DistributionStoreGoodsColumnEnum {
    /**
     *  门店代码
     */
    DISTRIBUTION_STORE__CODE(1, "门店代码"),
    /**
     *  商品SKU代码
     */
    DISTRIBUTION_SKU_CODE(2, "商品SKU代码"),
    /**
     *  分货数量
     */
    DISTRIBUTION_QUANTITY(3, "分货数量"),
    /**
     *  账号
     */
    ACCOUNT_USER_ERROR(1, "账号");

    private Integer columnNumber;

    private String value;

    DistributionStoreGoodsColumnEnum(Integer columnNumber, String value) {
        this.columnNumber = columnNumber;
        this.value = value;
    }

    public Integer getColumnNumber() {
        return this.columnNumber;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByColumnNumber(Integer columnNumber) {
        for (DistributionStoreGoodsColumnEnum ele : values()) {
            if (ele.getColumnNumber().equals(columnNumber)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
