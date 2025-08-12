package com.edc.erp.directly.returnorder.enumeration;

/**
 * 退货导入模板错误枚举
 *
 * @author yaojinpeng
 * @since 2022/10/26 17:51
 */
public enum OrdReturnGoodsColumnEnum {

    /**
     * 商品SKU代码
     */
    DISTRIBUTION_SKU_CODE(2, "商品代码"),
    /**
     * 申请退货数量
     */
    DISTRIBUTION_QUANTITY(3, "申请退货数量"),
    /**
     * 账号
     */
    ACCOUNT_USER_ERROR(1, "账号");

    private Integer columnNumber;

    private String value;


    OrdReturnGoodsColumnEnum(Integer columnNumber, String value) {
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
        for (OrdReturnGoodsColumnEnum ele : values()) {
            if (ele.getColumnNumber().equals(columnNumber)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
