package com.edc.erp.returnorder.enumeration;

/**
 * 退货单状态枚举
 * @author lh
 */
public enum OrdReturnOrderStatusEnum {
    /**
     * 已保存
     */
    SAVED("saved", "已保存"),
    /**
     * 待审核
     */
    SUBMITTED("submitted", "待审核"),
    /*8
    已审核
     */
    APPROVED("approved", "已审核"),
    /**
     * 已收货
     */
    PROCESSED("processed", "已收货"),
    /**
     * 已作废
     */
    INVALID("invalid","已作废"),
    RETURN_GOOD("returnGood","已完成");

    private String key;
    private String value;

    OrdReturnOrderStatusEnum(String key, String value) {
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
        for (OrdReturnOrderStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
