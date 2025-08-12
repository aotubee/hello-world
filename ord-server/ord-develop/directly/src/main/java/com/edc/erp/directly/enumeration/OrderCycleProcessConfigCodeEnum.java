package com.edc.erp.directly.enumeration;

/**
 * 单据生命周期流程配置枚举
 *
 * @author wanglidong
 * @since 2022/11/16 15:46
 */
public enum OrderCycleProcessConfigCodeEnum {

    /**
     * 单据生命周期流程配置
     */
    ORDER_SOURCE_CODE("C000001", "允许的订货类型"),
    MIN_AMOUNT("C000002", "起送金额校验"),
    CHECK_ACTIVITY("C000003", "校验促销"),
    REQUEST_ORDER_CREATE_OPPORTUNITY("C000005", "转单时机"),
    PAY_TYPE("C000006", "允许的支付方式"),
    CAPITAL_ACCOUNT("C000007", "资金账户"),
    REQUEST_ORDER_CREATE_APPROACHES("C000008", "要货数取值"),
    DELIVERY_SPLIT_CONDITION("C000009", "拆单条件"),
    DELIVERY_IS_AUTO_RECEIVE("C000010", "是否自动收货"),
    DELIVERY_ORDER_PRIORITY("C000011", "转单优先级"),
    ;

    private String code;
    private String value;

    OrderCycleProcessConfigCodeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

}