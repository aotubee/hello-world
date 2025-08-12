package com.edc.erp.enumeration;

/**
 * 单据追踪状态枚举类
 * @author fxw
 */
public enum OrderTrackStatusEnum {
    /**
     * 下单成功
     */
    CREATE_ORDER("下单成功"),
    /**
     * 创建清单
     */
    CREATE_LIST("创建清单"),
    /**
     * 发起支付
     */
    REQUEST_PAY("发起支付"),
    /**
     * 已支付
     */
    LIST_PAID("已支付"),
    /**
     * 订单作废
     */
    INVALID_ORDER("订单作废"),
    /**
     * 仓库处理中
     */
    WAREHOUSE_IN_PROCESS("仓库处理中"),
    /**
     * 仓库已发货
     */
    WAREHOUSE_DELIVERED("仓库已发货"),
    /**
     * 已收货
     */
    STORE_RECEIVED("已收货"),
    /**
     * 已转单
     */
    TO_REQUEST_ORDER("已转单"),;

    private final String name;

    OrderTrackStatusEnum(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public static String getName(OrderTrackStatusEnum activityTypeEnum) {
        return activityTypeEnum.name;
    }

}
