package com.edc.erp.common.enumeration;

/**
 * 订货单日志模板枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021年7月14日 11:00
 */
public enum DeliveryOrderLogEnum {

    DELIVERY_ORDER_CREATE("创建配销单{0}。", "配销单"),
    DIR_DELIVERY_ORDER_CREATE("创建配货单{0}。", "配货单"),
    DELIVERY_ORDER_CREATE_CHARGE("创建红冲配销单{0}。", "配销单"),
    DELIVERY_ORDER_STATUS_UPDATE("配销单：{0}状态由{1}更新为{2}。", "配销单"),
    DIR_DELIVERY_ORDER_STATUS_UPDATE("配货单：{0}状态由{1}更新为{2}。", "配货单"),
    DIR_DELIVERY_ORDER_SALVAGE("拆分配货-配货单：{0}存入订货周期为{1}的捞单池明细。", "配货单"),
    DIR_DELIVERY_ORDER_NO_SALVAGE("非订单流-配货单：{0}存入无主捞单池明细。", "配货单"),
    CUT_DELIVERY_ORDER_RESULT("配销单{0}截单{1}。", "要货单"),
    DELIVERY_UPDATE_DISTRIBUTION_QUANTITY("配销单{0}匹配{1}更新配货量成功。", "更新对账单配货量"),
    DELIVERY_UPDATE_DELIVERY_QUANTITY("配销单{0}匹配{1}更新发货量成功，订货订单状态改为：{2}。", "更新对账单发货量"),
    DELIVERY_TAKE_SUCCESS("配销单{0}(海鼎：{1})已收货,收货方：{2}。", "配销单收货"),
    DELIVERY_ORDER_APPROVED("配销单{0}已审核。", "配销单"),
    DIS_DELIVERY_ORDER_DELIVERED("配销单{0}已发货，实扣{1}元。", "配销单"),
    DIR_DELIVERY_ORDER_DELIVERED("配货单{0}已发货。", "配货单"),

    DELIVERY_ORDER_CHARGE("配货单{0}已红冲。", "配货单"),
    DIS_DELIVERY_ORDER_CHARGE("配销单{0}已红冲。", "配销单"),
    DIS_DELIVERY_ORDER_SALVAGE("拆分配货-配销单：{0}存入订货周期为{1}的捞单池明细。", "配销单"),
    DIS_DELIVERY_ORDER_NO_SALVAGE("非订单流-配销单：{0}存入无主捞单池明细。", "配销单"),
    INIT_ORDER_TAKE_DELIVERY_TIME("配销单{0}更新自动收货时间。", "配销单"),
    CUT_DELIVERY_ORDER("配销单{0}截单成功，状态由{1}更新为{2}。已发送至ERP。", "配销单"),
    CUT_DELIVERY_ORDER_ZK("配销单{0}截单成功，已发送至中科。", "配销单"),
    CUT_DELIVERY_ORDER_ZK_EMPTY_GOODS("配销单{0}推送中科前状态由{1}改为{2}，{3}没有映射。", "配销单"),
    CUT_DELIVERY_BEFORE_SEND_ZK("配销单{0}推送中科前状态由{1}改为{2}。", "配销单"),
    SIGNING_DELIVERY_ORDER("与司机交接签收：{0}", "配销单签收"),
    ALL_OUT_STOCK("配销单{0}整单缺货", "配销单"),
    TAKE_DELIVERY("收货成功{0}", "配销单完成收货"),
    ZK_SHIPPED_AND_CREATE_DIRECT_ORDER("配销单{0}中科发货后优店自动收货成功，并创建直送收货单{1}。", "配销单"),
    TS_DELIVERY_DIRECT_ORDER_SKU_ERROR("配销单{0}创建直送收货单时校验供应商不存在商品{1}。", "配送单"),
    ZK_DELIVERY_TO_HD("中科配销单{0}，已发送至海鼎ERP。", "配销单"),
    ZK_AUDIT_DISTRIBUTION_QUANTITY_ERROR("中科配销单{0}明细：{1}审核配货量大于要货量。", "配销单"),
    ZK_AUDIT_DELIVERY_QUANTITY_ERROR("中科配销单{0}明细：{1}实配数大于审核配货数。", "配销单"),
    SEND_HD_EMPTY_ITEMS("配销单{0}发送海鼎明细为空", "sendDeliveryOrderDetail"),
    DIS_DELIVERY_ORDER_OCCUPY_STOCK("配销单{0}占库存：{1}", "配销单"),
    DIS_DELIVERY_ORDER_STOCK_OUT_RETURN_FUND("配销单{0}缺货或少货返款{1}", "配销单"),

    DIR_DELIVERY_ORDER_OCCUPY_STOCK("配货单{0}占库存：{1}", "配货单"),
    DIR_DELIVERY_ORDER_STOCK_OUT_RETURN_FUND("配销单{0}缺货或少货返款{1}", "配销单"),
    DIS_DELIVERY_ORDER_PAY("配销单{0}已付款{1}元。", "配销单"),
    DIS_DELIVERY_ORDER_RETURN("配销单{0},{1}已退款{2}元。", "配销单"),
    DIS_DELIVERY_ZK_AUDIT_BACK_NO_FAIL("配销单{0}中科审核回传明细均无审核数。", "配销单"),

    DIS_DELIVERY_ZK_AUDIT_BACK_SUCCESS("配销单{0}中科审核已回传。", "配销单"),

    ORD_DIS_DELIVERY_EMPTY_OTHER_GOODS("配销单整单无映射", "配销单"),



    DELIVERY_INVALID_UNFROZEN("配销单：{0}解冻审核金额。", "配销单"),
    TRANSFER_DELIVERY_ORDER_STATUS_UPDATE("中转配销单采购回传，状态更新为{0}。", "配销单"),
    TRANSFER_DIR_DELIVERY_ORDER_STATUS_UPDATE("中转配货单采购回传，状态更新为{0}。", "配货单"),;

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
