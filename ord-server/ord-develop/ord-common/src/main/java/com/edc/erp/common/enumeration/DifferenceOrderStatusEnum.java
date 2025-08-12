package com.edc.erp.common.enumeration;

/**
 * 配销差异单订单状态枚举
 *
 * @author : gusiyuan
 * @date 2020-07-14 09:17
 */
public enum DifferenceOrderStatusEnum {
    /**
     * 已批准
     */
    APPROVED("approved", "已批准"),
    /**
     * 已审核
     */
    AUDITED("audited", "已审核"),
    /**
     * 已完成
     */
    FINISHED("finished",  "已完成"),
    /**
     * 已作废
     */
    INVALID("invalid",  "已作废");


    private final String code;
    private final String name;

    DifferenceOrderStatusEnum(String code, String name) {

        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (DifferenceOrderStatusEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }

}
