package com.edc.erp.presale.enumeration;

/**
 * 退货单状态枚举
 * @author lh
 */
public enum OrdDisPresaleOrderStatusEnum {

    WAIT_PAYMENT("waitPayment", "待付款"),
    /**
     * 已付款
     */
    PAID("paid", "已付款"),

    PAYING("paying", "支付中"),
    /**
     * 已取消
     */
    CANCEL("cancel", "已取消"),
    REFUND("refunded", "已退款");

    private String key;
    private String value;

    OrdDisPresaleOrderStatusEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public static String getValueByKey(String key) {
        for (OrdDisPresaleOrderStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
