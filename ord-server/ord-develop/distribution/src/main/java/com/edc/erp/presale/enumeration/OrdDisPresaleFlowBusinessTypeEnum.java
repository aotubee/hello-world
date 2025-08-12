package com.edc.erp.presale.enumeration;

/**
 * 退货单状态枚举
 * @author lh
 */
public enum OrdDisPresaleFlowBusinessTypeEnum {

    PRESALE_ORDER("presaleOrder", "预售订单"),
    /**
     * 已付款
     */
    MANUAL_ORDER("manualOrder", "手工订货"),
    /**
     * 已取消
     */
    INVALID_MANUAL_ORDER("invalidManualOrder", "手工订单作废"),
    DISTRIBUTION_ORDER("distributionOrder", "人工分货"),
    INVALID_DISTRIBUTION_ORDER("invalidDistributionOrder", "人工分货订单作废"),
    ADJUST_ORDER_ADD("adjustOrderAdd", "调整单+"),
    ADJUST_ORDER_REDUCE("adjustOrderReduce", "调整单-"),
    CHARGE_ADJUST_ORDER_ADD("chargeAdjustOrderAdd", "调整单+冲销"),
    CHARGE_ADJUST_ORDER_REDUCE("chargeAdjustOrderReduce", "调整单-冲销"),

    PRESALE_ORDER_REFUND("presaleOrderRefund", "预售订单退款"),
    ;

    private String key;
    private String value;

    OrdDisPresaleFlowBusinessTypeEnum(String key, String value) {
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
        for (OrdDisPresaleFlowBusinessTypeEnum ele : values()) {
            if (ele.getKey().equals(key)) {
                return ele.getValue();
            }
        }
        return null;
    }
}
