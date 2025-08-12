package com.edc.erp.returnnoticeorder.enumeration;

/**
 * 退货单状态枚举
 * @author lh
 */
public enum OrdDisReturnNoticeStatusEnum {

    /**
     * 待审核
     */
    SUBMITTED("submitted", "待审核"),
    /**
     * 已审核
     */
    APPROVED("approved", "已审核"),
    /**
     * 已生效
     */
    PROCESSED("processed", "已生效"),
    /**
     * 已作废
     */
    INVALID("invalid","已作废");

    private String key;
    private String value;

    OrdDisReturnNoticeStatusEnum(String key, String value) {
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
        for (OrdDisReturnNoticeStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
