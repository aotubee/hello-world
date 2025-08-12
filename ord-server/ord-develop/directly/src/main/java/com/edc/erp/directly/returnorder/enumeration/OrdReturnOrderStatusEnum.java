package com.edc.erp.directly.returnorder.enumeration;

/**
 * @return: 退货单状态枚举
 * @Author: fxw
 * @Date: 2022/11/23
 */
public enum OrdReturnOrderStatusEnum {

    /**
     * 退货单状态
     */
    SAVED("saved", "已保存"),
    SUBMITTED("submitted", "待审核"),
    APPROVED("approved", "已审核"),
    PROCESSED("processed", "已收货"),
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
