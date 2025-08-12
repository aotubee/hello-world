package com.edc.erp.presale.enumeration;

/**
 * 退货单状态枚举
 * @author lh
 */
public enum OrdDisPresaleActivityStatusEnum {
    /**
     * 已保存
     */
    // SAVED("saved", "已保存"),
    /**
     * 待审核
     */
    SUBMITTED("submitted", "待审核"),
    /*
    已审核
     */
    APPROVED("approved", "已审核"),
    /**
     * 已收货
     */
    EXECUTED("executed", "已生效"),

    STOPPED("stopped", "已中止"),
    /**
     * 已作废
     */
    INVALID("invalid","已作废"),
    TERMINATED("terminated","已终止");

    private String key;
    private String value;

    OrdDisPresaleActivityStatusEnum(String key, String value) {
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
        for (OrdDisPresaleActivityStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
