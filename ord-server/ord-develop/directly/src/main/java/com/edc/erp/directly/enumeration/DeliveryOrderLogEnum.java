package com.edc.erp.directly.enumeration;

/**
 * 订货单日志模板枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021年7月14日 11:00
 */
public enum DeliveryOrderLogEnum {

    /**
     * 订货单日志模板
     */
    DELIVERY_ORDER_CREATE("创建直营配货单{0}。", "直营配货单"),
    DELIVERY_ORDER_CREATE_CHARGE("创建红冲直营配货单{0}。", "直营配货单"),
    DELIVERY_ORDER_STATUS_UPDATE("直营配货单：{0}状态由{1}更新为{2}。", "直营配货单"),
    CUT_DELIVERY_ORDER_RESULT("直营配货单{0}截单{1}。", "要货单"),
    DELIVERY_UPDATE_DISTRIBUTION_QUANTITY("直营配货单{0}匹配{1}更新配货量成功。", "更新对账单配货量"),
    DELIVERY_UPDATE_DELIVERY_QUANTITY("直营配货单{0}匹配{1}更新发货量成功，订货订单状态改为：{2}。", "更新对账单发货量"),
    DELIVERY_TAKE_SUCCESS("直营配货单{0}(海鼎：{1})已收货,收货方：{2}。", "直营配货单收货"),
    DELIVERY_ORDER_APPROVED("直营配货单{0}已审核。", "直营配货单"),
    DELIVERY_ORDER_DELIVERED("直营配货单{0}已发货。", "直营配货单"),
    DELIVERY_ORDER_CHARGE("直营配货单{0}已红冲。", "直营配货单"),
    INIT_ORDER_TAKE_DELIVERY_TIME("直营配货单{0}更新自动收货时间。", "直营配货单"),
    CUT_DELIVERY_ORDER("直营配货单{0}截单成功，状态由{1}更新为{2}。已发送至ERP。", "直营配货单"),
    CUT_DELIVERY_ORDER_ZK("直营配货单{0}截单成功，已发送至中科。", "直营配货单"),
    SIGNING_DELIVERY_ORDER("与司机交接签收：{0}", "直营配货单签收"),
    ALL_OUT_STOCK("直营配货单{0}整单缺货", "直营配货单"),
    TAKE_DELIVERY("收货成功{0}", "直营配货单完成收货"),
    ZK_SHIPPED_AND_CREATE_DIRECT_ORDER("直营配货单{0}中科发货后优店自动收货成功，并创建直送收货单{1}。", "直营配货单"),
    TS_DELIVERY_DIRECT_ORDER_SKU_ERROR("直营配货单{0}创建直送收货单时校验供应商不存在商品{1}。", "配送单"),
    ZK_DELIVERY_TO_HD("中科直营配货单{0}，已发送至海鼎ERP。", "直营配货单"),
    ZK_AUDIT_DISTRIBUTION_QUANTITY_ERROR("中科直营配货单{0}明细：{1}审核配货量大于要货量。", "直营配货单"),
    ZK_AUDIT_DELIVERY_QUANTITY_ERROR("中科直营配货单{0}明细：{1}实配数大于审核配货数。", "直营配货单"),
    SEND_HD_EMPTY_ITEMS("直营配货单{0}发送海鼎明细为空", "sendDeliveryOrderDetail");

    private String key;
    private String globalType;

    DeliveryOrderLogEnum(String key, String globalType) {
        this.key = key;
        this.globalType = globalType;
    }

    public String getKey() {
        return this.key;
    }

    public String getGlobalType() {
        return this.globalType;
    }

}
