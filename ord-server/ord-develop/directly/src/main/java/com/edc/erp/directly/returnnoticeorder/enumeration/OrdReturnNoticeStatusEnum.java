package com.edc.erp.directly.returnnoticeorder.enumeration;

/**
 * @return: 退货通知单状态枚举
 * @Author: fxw
 * @Date: 2022/11/23
 */
public enum OrdReturnNoticeStatusEnum {

    /**
     * 退货通知单状态
     */
    SUBMITTED("submitted", "待审核"),
    APPROVED("approved", "已审核"),
    PROCESSED("processed", "已生效"),
    INVALID("invalid","已作废");

    private String key;
    private String value;

    OrdReturnNoticeStatusEnum(String key, String value) {
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
        for (OrdReturnNoticeStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
