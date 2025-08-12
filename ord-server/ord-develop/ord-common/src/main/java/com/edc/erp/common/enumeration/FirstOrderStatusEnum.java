package com.edc.erp.common.enumeration;

/**
 * 铺货单订单状态
 *
 * @author : gusiyuan
 * @date 2020-07-14 09:17
 */
public enum FirstOrderStatusEnum {
    /**
     * 待审核
     */
    PENDING("pending", "待审核"),
    /**
     * 已审核
     */
    APPROVED("approved", "已审核"),
    /**
     * 已生效
     */
    EXECUTED("executed",  "已生效"),
    /**
     * 已作废
     */
    INVALID("invalid",  "已作废");


    private final String code;
    private final String name;

    FirstOrderStatusEnum (String code, String name) {

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
        for (FirstOrderStatusEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }

}
