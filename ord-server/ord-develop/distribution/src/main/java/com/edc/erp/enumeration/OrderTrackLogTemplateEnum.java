package com.edc.erp.enumeration;

/**
 * 单据追踪状态枚举类
 * @author fxw
 */
public enum OrderTrackLogTemplateEnum {
    /**
     * 下单成功
     */
    CREATE_ORDER("下单成功", "订货单{0}创建成功"),
    /**
     * 清单已支付
     */
    LIST_PAID("清单已支付", "订货清单{0}支付成功"),
    /**
     * 订单作废
     */
    CUT_NO_PAY("订单作废", "订货单{0}由于未付款整单作废"),

    CUT_LESS_MIN_AMOUNT_INVALID_SINGLE_MODEL_MARGE("订单作废", "订货单{0}由于已付款金额不足起送金额整单作废"),
    /**
     * 订单作废
     */
    BACK_INVALID_ORDER("订单作废", "订货单{0}整单作废"),

    APP_CANCEL_ORDER("订单作废", "配销订货单{0}已作废"),
    /**
     * 清单作废
     */
    INVALID_LIST("清单作废", "订货清单{0}未付款作废"),
    /**
     * 仓库处理中
     */
    WAREHOUSE_IN_PROCESS("仓库处理中", "订货单{0}发送海鼎成功，物流已接单"),
    /**
     * 仓库已发货
     */
    WAREHOUSE_DELIVERED("仓库已发货", "配货单{0}已发货，海鼎配货单{1}"),
    /**
     * 已收货
     */
    STORE_RECEIVED("已收货", "配货单{0}已收货，海鼎配货单{1}"),
    /**
     * 订单作废
     */
    CUT_INVALID_ONLY_MATERIEL("订单作废", "订货单{0}由于截单时只有911物料仓商品整单作废"),
    /**
     * 订货单已支付
     */
    ORDER_PAID("订货单已支付", "订货单{0}支付成功"),
    /**
     * 仓库处理中
     */
    TO_REQUEST_ORDER("仓库处理中", "订货单{0}已转集货单[{1}$${2}]，物流已接单");



    private final String template;

    OrderTrackLogTemplateEnum(String name, String template) {
        this.template = template;
    }


    public String getTemplate() {
        return template;
    }


    public static String getTemplate(OrderTrackLogTemplateEnum activityTypeEnum) {
        return activityTypeEnum.template;
    }
}
