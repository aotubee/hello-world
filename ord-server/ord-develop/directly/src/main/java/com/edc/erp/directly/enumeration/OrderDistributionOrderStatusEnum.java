package com.edc.erp.directly.enumeration;
/**
 * @return: 配货单状态枚举
 * @Author: fxw
 * @Date: 2022/11/23
 */
public enum OrderDistributionOrderStatusEnum {

    /**
     * 已审核
     */
    APPROVAL("approved", "已审核"),
    /**
     * 待审核
     */
    PENDING_APPROVAL("pending", "待审核"),
    /**
     * 已作废
     */
    INVALID("invalid", "已作废"),

    /**
     * 已生效
     */
    EXECUTED("executed", "已生效");




    private String key;
    private String value;

    OrderDistributionOrderStatusEnum(String key, String value) {
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
        for (OrderDistributionOrderStatusEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
