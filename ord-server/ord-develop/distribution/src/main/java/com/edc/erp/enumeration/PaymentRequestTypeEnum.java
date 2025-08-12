package com.edc.erp.enumeration;

/**
 * 支付请求类型枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021年07月29日 10:52
 */
public enum PaymentRequestTypeEnum {


    /**
     * 手动请求
     */
    MANUAL(1, "手动请求"),
    /**
     * 自动请求
     */
    AUTO(2, "自动请求");

    private Integer value;
    private String remark;

    PaymentRequestTypeEnum(Integer value, String remark) {
        this.value = value;
        this.remark = remark;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getRemark() {
        return this.remark;
    }


}
