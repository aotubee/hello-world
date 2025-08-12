package com.edc.erp.common.enumeration;

/**
 * 清算业务类型枚举
 * @author jushaofei
 * @since 2022-08-08
 */
public enum FundTypeEnum {
    /**
     * 清算业务类型---费用缴款
     */
    FEE_CONTRIBUTIONS("feeContributions", "费用缴款"),
    /**
     * 清算业务类型---订货下单
     */
    PLACE_ORDER("placeOrder", "订货下单"),
    /**
     * 清算业务类型---批发下单
     */
    WHOLESALE_ORDER("wholesaleOrder", "批发下单"),
    /**
     * 清算业务类型---配销补款
     */
    DISTRIBUTION_SUPPLEMENT("distributionSupplement", "配销补款"),
    /**
     * 清算业务类型---差异单
     */
    VARIANCE_SHEET("varianceSheet", "差异单"),
    /**
     * 清算业务类型---配销退货
     */
    DISTRIBUTION_RETURN("distributionReturn", "配销退货"),
    /**
     * 清算业务类型---门店调拨
     */
    STORE_ALLOCATION("storeAllocation", "门店调拨"),
    /**
     * 清算业务类型---采购收货
     */
    PURCHASE_RECEIVING("purchaseReceiving ", "采购收货"),
    /**
     * 清算业务类型---采购退货
     */
    PURCHASE_RETURN("purchaseReturn", "采购退货"),
    /**
     * 清算业务类型---门店消费
     */
    STORE_PAY("storePay", "门店消费"),
    /**
     * 清算业务类型---分润
     */
    PROFIT("profit", "分润"),
    /**
     * 清算业务类型---费用单据
     */
    FEE_BILLS("feeBills", "费用单据"),
    /**
     * 清算业务类型---资金划转
     */
    FEE_TRANSFER("feeTransfer", "资金划转"),
    /**
     * 清算业务类型---授信补偿
     */
    CREDIT_COMPENSATE("creditCompensate","授信补偿"),
    /***
     *清算业务类型---批发出货
     */
    WHOLESALE_SHIPMENT("wholesaleShipment","批发出货"),
    /***
     *清算业务类型---批发退货
     */
    WHOLESALE_RETURNS("wholesaleReturns","批发退货"),

    /***
     *清算业务类型---铺货下单
     */
    FIRST_ORDER("firstOrder","铺货下单"),

    DIS_DELIVERY_ORDER_RETURN("disDeliveryOrderReturn","配销返款"),

    DIS_FIRST_ORDER_RETURN("disFirstOrderReturn","铺货返款"),

    DIS_ORDER_RETURN("disOrderReturn","订货返款"),

    DIS_WHOLESALE_RETURN("disWholesaleReturn","批发返款"),

    DIS_WHOLESALE_PAY("disWholesalePay","批发补款"),

    DIS_DIFFERENCE_RETURN("disDifferenceReturn","差异单返款"),

    DIS_RETURN_ORDER_PAY("disReturnOrderPay","退货单补款"),

    DIS_RETURN_ORDER_RETURN("disReturnOrderReturn","退货单返款"),

    WHOLESALE_SHIPMENT_AUDIT("wholesaleShipmentAudit","批发出货单审核"),

    PAY_PRESALE_ORDER("payPresaleOrder","预售付款"),

    RETURN_PRESALE_ORDER("returnPresaleOrder","预售返款"),



    DISTRIBUTION_AUDIT("distributionAudit","配销单审核"),

    DISTRIBUTION_SHIPMENTS("distributionShipments","配销单发货"),

    ;

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    FundTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (FundTypeEnum ele : values()) {
            if (ele.getCode().equals(code)) {
                return ele.getName();
            }
        }
        return null;
    }
}
