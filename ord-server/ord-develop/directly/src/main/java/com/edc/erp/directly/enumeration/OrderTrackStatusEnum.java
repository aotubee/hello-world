package com.edc.erp.directly.enumeration;


/**
 * @return: 单据追踪状态枚举类
 * @Author: fxw
 * @Date: 2022/11/23
 */
public enum OrderTrackStatusEnum {

    /**
     * 单据追踪状态
     */
    CREATE_ORDER("下单成功"),
    CREATE_LIST("创建清单"),
    REQUEST_PAY("发起支付"),
    LIST_PAID("已支付"),
    INVALID_ORDER("订单作废"),
    WAREHOUSE_IN_PROCESS("仓库处理中"),
    WAREHOUSE_DELIVERED("仓库已发货"),
    STORE_RECEIVED("已收货"),
    TO_REQUEST_ORDER("已转单");

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
