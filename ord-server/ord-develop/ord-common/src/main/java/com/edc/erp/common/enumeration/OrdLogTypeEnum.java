package com.edc.erp.common.enumeration;

/**
 * @author fxw
 * @description: 日志业务类型枚举类
 * @since 2022/09/17
 */
public enum OrdLogTypeEnum {
    /**
     * 品牌
     */
    BRAND("brand", "品牌"),

    /**
     * 计量单位
     */
    UNIT("unit", "计量单位"),

    /**
     * 生命周期
     */
    LIFE_CYCLE("lifeCycle", "生命周期"),

    /**
     * 商品状态
     */
    GOODS_STATUS("goodsStatus", "商品状态"),

    /**
     * 标准商品
     */
    STANDARD_GOODS("standardGoods", "标准商品"),

    /**
     * 组织商品
     */
    ORG_GOODS("orgGoods", "组织商品"),

    /**
     * 运营品类
     */
    ORG_SORT("orgSort", "运营品类"),

    /**
     * 标准品类
     */
    STANDARD_SORT("standardSort", "标准品类"),

    /**
     * 组织商品周期任务
     */
    ORG_ADJUST_STATUS("orgAdjustStatus", "组织商品周期任务"),

    /**
     * 组织商品状态调整单
     */
    ADJUST_STATUS_ORDER("orgAdjustStatusOrder", "组织商品状态调整单"),

    /**
     * 组织商品属性调整单
     */
    ADJUST_PROPERTY_ORDER("orgAdjustPropertyOrder", "组织商品属性调整单"),

    /**
     * 商品经营方案组
     */
    GOODS_BUSINESS_PLAN("goodsBusinessPlan", "商品经营方案组"),

    /**
     * 门店商品配置
     */
    STORE_GOODS_CONFIG("store_goods_config", "门店商品配置"),

    /**
     * 商品经营方案调整单
     */
    GOODS_BUSINESS_PLAN_ADJUST_ORDER("goodsBusinessAdjustOrder", "商品经营方案调整单"),

    /**
     * 商品配送方案组
     */
    GOODS_DISTRIBUTION_PLAN_GROUP("goodsDistributionPlanGroup", "商品配送方案组"),

    /**
     * 商品配送方案调整单
     */
    GOODS_DISTRIBUTION_PLAN_ADJUST_ORDER("goodsDistributionPlanAdjustOrder", "商品配送方案调整单"),

    /**
     * 商品配送价格组
     */
    GOODS_DISTRIBUTION_PRICE_GROUP("GoodsDistributionPriceGroup", "商品配送价格组"),

    /**
     * 商品订货时间设置
     */
    GOODS_ORDER_ABLE_PERIOD("goodsOrderAblePeriod", "商品订货时间设置"),

    /**
     * 订货周期
     */
    GOODS_ORDERING_CYCLE("goodsOrderingCycle", "订货周期"),

    /**
     * 添加价格组
     */
    PRICE_GROUP_SAVE("priceGroupSave", "添加价格组"),

    /**
     * 删除价格组
     */
    PRICE_GROUP_DELETE("priceGroupDelete", "删除价格组"),

    /**
     * 修改价格组
     */
    PRICE_GROUP_UPDATE("priceGroupUpdate", "修改价格组"),

    /**
     * 修改价格组商品
     */
    PRICE_GROUP_DETAIL_UPDATE("priceGroupDetailUpdate", "修改价格组商品"),

    /**
     * 添加售价调整单
     */
    PRICE_ADJUST_ORDER_SAVE("priceAdjustOrderSave", "添加售价调整单"),

    /**
     * 添加售价调整单明细
     */
    PRICE_ADJUST_ORDER_DETAIL_SAVE("priceAdjustOrderDetailSave", "添加售价调整单明细"),

    /**
     * 删除售价调整单明细
     */
    PRICE_ADJUST_ORDER_DETAIL_DELETE("priceAdjustOrderDetailDelete", "删除售价调整单明细"),

    /**
     * 修改售价调整单状态
     */
    PRICE_ADJUST_ORDER_STATUS_UPDATE("priceAdjustOrderStatusUpdate", "修改售价调整单状态"),

    /**
     * 修改售价调整单
     */
    PRICE_ADJUST_ORDER_UPDATE("priceAdjustOrderUpdate", "修改售价调整单"),

    /**
     * 删除售价调整单
     */
    PRICE_ADJUST_ORDER_DELETE("priceAdjustOrderDelete", "删除售价调整单"),

    /**
     * 商品配送价格调整单
     */
    GOODS_DISTRIBUTION_PRICE_ADJUST_ORDER("goodsDisPriceAdjOrder", "商品配送价格调整单"),

    /**
     * 商品配销价格调整单
     */
    DISTRIBUTION_PRICE_ADJUST_ORDER("distributionPriceAdjustOrder", "商品配销价格调整单"),

    /**
     * 新增订货单
     */
    ORDER_GOODS_SAVE("goodsOrderSave", "新增订货单"),

    /**
     * 修改订货单
     */
    ORDER_GOODS_UPDATE("goodsOrderUpdate", "修改订货单"),

    /**
     * 新增订货单明细
     */
    ORDER_GOODS_DETAIL_SAVE("goodsOrderDetailSave", "新增订货单明细"),

    /**
     * 修改订货单明细
     */
    ORDER_GOODS_DETAIL_UPDATE("goodsOrderDetailUpdate", "修改订货单明细"),

    /**
     * 配销分货单
     */
    ORD_DIS_ORDER_DISTRIBUTION("ordDisOrderDistribution", "配销分货单"),
    /**
     * 添加配销分货门店商品
     */
    ORD_DIS_ORDER_DISTRIBUTION_SAVE("ordDisOrderDistributionSave", "添加配销分货单"),

    /**
     * 删除分货单
     */
    ORD_DIS_ORDER_DISTRIBUTION_DELETE("ordDisOrderDistributionDelete", "删除配销分货单"),

    /**
     * 配销货单已生效
     */
    ORD_DIS_ORDER_DISTRIBUTION_EXECUTED("ordDisOrderDistributionExecuted", "配销分货单生效"),

    /**
     * 修改配销分货门店商品
     */
    ORD_DIS_ORDER_DISTRIBUTION_UPDATE("ordDisOrderDistributionUpdate", "修改配销分货单"),

    DIS_DISTRIBUTION_ORDER_DETAIL_UPDATE("配销分货单更新明细。", "修改配销分货单明细"),

    /**
     * 作废配销分货门店商品
     */
    ORD_DIS_ORDER_DISTRIBUTION_INVALID("ordDisOrderDistributionInvalid", "作废配销分货单"),


    /**
     * 审核配销分货门店商品
     */
    ORD_DIS_ORDER_DISTRIBUTION_AUDIT("ordDisOrderDistributionAudit", "审核配销分货单"),


    /**
     * 添加配销分货门店商品
     */
    ORD_DIS_ORDER_DISTRIBUTION_DETAIL_SAVE("ordDisOrderDistributionDetailSave", "添加配销分货门店商品"),

    /**
     * 修改配销分货门店商品
     */
    ORD_DIS_ORDER_DISTRIBUTION_DETAIL_UPDATE("ordDisOrderDistributionDetailSave", "修改配销分货门店商品"),

    /**
     * 删除分货单明细
     */
    ORD_DIS_ORDER_DISTRIBUTION_DETAIL_DELETE("ordDisOrderDistributionDetailDelete", "删除配销分货单明细"),

    /**
     * 删除分货单明细
     */
    DIS_ORDER_DISTRIBUTION_CREATE_ORDER("disOrderDistributionCreateOrder", "分货单生成订货单"),

    /**
     * 铺货单
     */
    FIRST_ORDER_GOODS("firstGoodsOrder", "铺货单"),

    /**
     * 直营铺货单
     */
    DIR_FIRST_ORDER_GOODS("dirFirstGoodsOrder", "直营铺货单"),
    /**
     * 新增铺货单
     */
    FIRST_ORDER_GOODS_SAVE("firstGoodsOrderSave", "新增铺货单"),
    /**
     * 修改铺货单
     */
    FIRST_ORDER_GOODS_UPDATE("firstGoodsOrderUpdate", "修改铺货单"),
    /**
     * 作废铺货单
     */
    FIRST_ORDER_GOODS_INVALID("firstGoodsOrderInvalid", "作废铺货单"),

    FIRST_ORDER_GOODS_INVALID_AND_RETURN("firstGoodsOrderInvalid", "作废铺货单,返款{0}"),
    /**
     * 审核铺货单
     */
    FIRST_ORDER_GOODS_AUDIT("firstGoodsOrderAudit", "审核铺货单"),

    DIS_FIRST_ORDER_GOODS_AUDIT("disFfirstGoodsOrderAudit", "审核铺货单,支付{0}元"),
    /**
     * 生效铺货单
     */
    FIRST_ORDER_GOODS_EXECUTED("firstGoodsOrderExecuted", "生效铺货单"),

    DIS_FIRST_ORDER_EMPTY("disFirstOrderEmpty", "铺货单{0}没有可拆分的商品,整单退款"),

    DIR_FIRST_ORDER_EMPTY("dirFirstOrderEmpty", "直营铺货单{0}没有可拆分的商品"),

    DIR_FIRST_ORDER_ILLEGAL_GOODS("dirFirstOrderIllegalGoods", "直营铺货单{0}不合格商品{1}"),

    /**
     * 铺货单不合格商品退款
     */
    FIRST_ORDER_GOODS_RETURN_AMOUNT("firstGoodsReturnAmount", "铺货单不合格商品{0}退款{1}元"),
    FIRST_ORDER_ILLEGAL_GOODS("firstGoodsReturnAmount", "铺货单不合格商品{0}"),

    DIS_FIRST_ORDER_GOODS_AUDIT_FREEZE("disFfirstGoodsOrderAuditFreeze", "审核铺货单,冻结{0}元"),

    DIS_FIRST_ORDER_EMPTY_UNFREEZE("disFirstOrderEmptyUnFreeze", "铺货单{0}没有可拆分的商品,整单解冻退款"),

    FIRST_ORDER_GOODS_INVALID_AND_RETURN_UNFREEZE("firstGoodsOrderInvalidUnfreeze", "作废铺货单,解冻{0}"),

    /**
     * 配销订货单
     */
    DIS_ORDER("disOrder", "配销订货单"),

    /**
     * 直营订货单
     */
    DIR_ORDER("dirOrder", "直营订货单"),

    /**
     * 配销分货单
     */
    DIS_ORDER_DISTRIBUTION_ORDER("disOrderDistributionOrder", "配销分货单"),

    /**
     * 直营分货单
     */
    DIR_ORDER_DISTRIBUTION_ORDER("dirOrderDistributionOrder", "直营分货单"),

    /**
     * 配销差异单
     */
    ORD_DIS_ORDER_DIFFERENCE("ordDisOrderDifference", "配销差异单"),

    /**
     * 配货差异单
     */
    ORD_DIR_ORDER_DIFFERENCE("ordDirOrderDifference", "配货差异单"),

    /**
     * 作废差异单
     */
    ORD_ORDER_DIFFERENCE_INVALID("ordOrderDifferenceInvalid", "作废差异单"),
    /**
     * 批准差异单
     */
    ORD_ORDER_DIFFERENCE_APPROVED("ordOrderDifferenceInvalidApproved", "批准差异单"),
    /**
     * 后台已批准配销差异单
     */
    ORD_ORDER_DIFFERENCE_APPROVED_SYSTEM("ordDirOrderDifferenceInvalidApprovedSystem", "dts回传批准差异单时，改差异单后台已批准"),

    /**
     * 后台已批准配销单
     */
    ORD_DIS_ORDER_SEND_DELIVERY_SYSTEM("ordDisOrderSendDeliverySystem", "dts回传批准配销单时，该配销单后台已发货"),


    ORD_DIS_ORDER_SEND_DELIVERY_UN_FROZEN_AND_SETTLEMENT("ordDisOrderSendDeliveryUnFrozenAndSettlement", "dts回传批准配销单时，该配销单后台已发货，实扣{0}"),
    /**
    ORD_DIS_ORDER_SEND_DELIVERY_SYSTEM("ordDisOrderSendDeliverySystem","dts回传批准配销单时，改配销单后台已发货,实扣{0}元"), /**
     * 后台已批准配货单
     */
    ORD_DIR_ORDER_SEND_DELIVERY_SYSTEM("ordDirOrderSendDeliverySystem", "dts回传批准配货单时，改配货单后台已发货"),

    /**
     * 配销集货单
     */
    DIS_REQUEST_ORDER("disRequestOrder", "集货单"),

    /**
     * 终结于集货单
     */
    END_OF_DIS_REQUEST_ORDER("endOfDisRequestOrder", "订单流未配置拆分配销，单据终结于集货单"),

    /**
     * 直营要货单
     */
    DIR_REQUEST_ORDER("dirRequestOrder", "要货单"),

    /**
     * 终结于要货单
     */
    END_OF_DIR_REQUEST_ORDER("endOfDirRequestOrder", "订单流未配置拆分配货，单据终结于要货单"),

    /**
     * 新增差异单
     */
    ORD_ORDER_DIFFERENCE_SAVE("ordOrderDifferenceSave", "新增差异单"),
    /**
     * 冲销差异单
     */
    ORD_ORDER_DIFFERENCE_CHARGE("ordOrderDifferenceCharge", "冲销差异单"),

    /**
     * 新增红冲差异单
     */
    ORD_ORDER_DIFFERENCE_CHARGE_SAVE("ordOrderDifferenceChargeSave", "新增红冲差异单"),
    /**
     * 配销单
     */
    DIS_DELIVERY_ORDER("disDeliveryOrder", "配销单"),
    /**
     * 配货单
     */
    DIR_DELIVERY_ORDER("dirDeliveryOrder", "配货单"),
    /**
     * 冲销配货单
     */
    DIR_DELIVERY_ORDER_CHARGE("dirDeliveryOrder", "冲销配货单"),

    /**
     * 批发出货单
     */
    ORD_WHOLESALE_SHIPMEN("ordWholesaleShipmen", "批发出货单"),

    /**
     * 批发出货单 - 手动发货
     */
    ORD_WHOLESALE_ARTIFICIAL("ordWholesaleArtificial", "批发出货单手动发货"),

    /**
     * 批发出货单DTS回传 - 已作废记录
     */
    ORD_WHOLESALE_SHIPMEN_DTS_INVALID("ordWholesaleShipmenDtsInvalid", "批发出货单据已作废，回传DTS失败"),

    /**
     * 批发退货单
     */
    ORD_WHOLESALE_RETURN("ordWholesaleReturn", "批发退货单"),

    /**
     * 批发出货单 - 手动发货
     */
    ORD_RETURN_ARTIFICIAL("ordReturnArtificial", "批发退货单手动收货"),

    /**
     * 批发退货单详情
     */
    ORD_WHOLESALE_RETURN_DETAIL("ordWholesaleReturnDetail", "批发退货单详情"),


    /**
     * 配销退货单
     */
    ORD_DIS_RETURN("ordDisReturn", "配销退货单"),

    ORD_DIS_RETURN_NOTICE("ordDisReturnNotice", "配销退货通知单"),
    /**
     * 新增配销退货单
     */
    ORD_DIS_RETURN_SAVE("ordDisReturnSave", "新增配销退货单"),

    ORD_DIS_RETURN_UPDATE("ordDisReturnUpdate", "修改配销退货单"),
    /**
     * 冲销配销退货单
     */
    ORD_DIS_RETURN_CHARGE("ordDisReturnCharge", "冲销配销退货单"),

    ORD_DIS_RETURN_APPROVED("ordDisReturnApproved", "审核配销退货单"),

    ORD_DIS_RETURN_INVALID("ordDisReturnInvalid", "作废配销退货单"),

    ORD_DIS_RETURN_PROCESSED("ordDisReturnProcessed", "配销退货单已收货"),

    ORD_DIS_RETURN_PROCESSED_SYSTEM("ordDisReturnProcessedSystem", "dts回传已收货的退货单时，该退货单为已收货"),

    ORD_DIS_RETURN_INVALID_SYSTEM("ordDisReturnProcessedSystem", "dts回传已收货的退货单时，该退货单为已作废"),

    ORD_DIR_RETURN_INVALID_SYSTEM("ordDirReturnProcessedSystem", "dts回传已收货的退货单时，该退货单为已作废"),

    ORD_DIS_RETURN_NOTICE_SAVE("ordDisReturnNoticeSave", "新增配销退货通知单"),

    ORD_DIS_RETURN_NOTICE_UPDATE("ordDisReturnNoticeUpdate", "修改配销退货通知单"),


    ORD_DIS_RETURN_NOTICE_APPROVED("ordDisReturnNoticeApproved", "审核配销退货通知单"),

    ORD_DIS_RETURN_NOTICE_INVALID("ordDisReturnNoticeInvalid", "作废配销退货通知单"),

    ORD_DIS_RETURN_NOTICE_PROCESSED("ordDisReturnNoticeProcessed", "已生效配销退货通知单"),

    /**
     * 直营分货单
     */
    ORD_DIR_ORDER_DISTRIBUTION("ordDirOrderDistribution", "直营分货单"),
    /**
     * 添加直营分货门店商品
     */
    ORD_DIR_ORDER_DISTRIBUTION_SAVE("ordDirOrderDistributionSave", "添加直营分货单"),

    /**
     * 删除直营分货单
     */
    ORD_DIR_ORDER_DISTRIBUTION_DELETE("ordDirOrderDistributionDelete", "删除直营分货单"),

    /**
     * 修改直营分货门店商品
     */
    ORD_DIR_ORDER_DISTRIBUTION_UPDATE("ordDirOrderDistributionUpdate", "修改直营分货单"),

    DIR_DISTRIBUTION_ORDER_DETAIL_UPDATE("直营分货单更新明细。", "修改直营分货单明细"),

    /**
     * 作废直营分货门店商品
     */
    ORD_DIR_ORDER_DISTRIBUTION_INVALID("ordDirOrderDistributionInvalid", "作废直营分货单"),


    /**
     * 审核直营分货门店商品
     */
    ORD_DIR_ORDER_DISTRIBUTION_AUDIT("ordDirOrderDistributionAudit", "审核直营分货单"),

    /**
     * 直营分货单生效
     */
    ORD_DIR_ORDER_DISTRIBUTION_EXECUTED("ordDirOrderDistributionExecuted", "直营分货单生效"),

    /**
     * 添加直营分货门店商品
     */
    ORD_DIR_ORDER_DISTRIBUTION_DETAIL_SAVE("ordDirOrderDistributionDetailSave", "添加直营分货门店商品"),

    /**
     * 修改直营分货门店商品
     */
    ORD_DIR_ORDER_DISTRIBUTION_DETAIL_UPDATE("ordDirOrderDistributionDetailSave", "修改直营分货门店商品"),

    /**
     * 删除直营分货单明细
     */
    ORD_DIR_ORDER_DISTRIBUTION_DETAIL_DELETE("ordDirOrderDistributionDetailDelete", "删除直营分货单明细"),

    /**
     * 直营分货单生成订货单
     */
    DIR_ORDER_DISTRIBUTION_CREATE_ORDER("dirOrderDistributionCreateOrder", "直营分货单生成订货单"),

    /**
     * 直营退货单
     */
    ORD_DIR_RETURN("ordDirReturn", "直营退货单"),

    ORD_DIR_RETURN_NOTICE("ordDirReturnNotice", "直营退货通知单"),

    ORD_DIR_RETURN_SAVE("ordDirReturnSave", "新增直营退货单"),

    ORD_DIR_RETURN_UPDATE("ordDirReturnUpdate", "修改直营退货单"),

    ORD_DIR_RETURN_CHARGE("ordDirReturnCharge", "冲销直营退货单"),

    ORD_DIR_RETURN_APPROVED("ordDirReturnApproved", "审核直营退货单"),

    ORD_DIR_RETURN_INVALID("ordDirReturnInvalid", "作废直营退货单"),
    ORD_DIR_RETURN_PROCESSED("ordDisReturnProcessed", "直营退货单已收货"),

    ORD_DIR_RETURN_PROCESSED_SYSTEM("ordDirReturnProcessedSystem", "dts回传已收货的退货单时，改退货单为已收货"),

    ORD_DIR_RETURN_NOTICE_SAVE("ordDirReturnNoticeSave", "新增直营退货通知单"),

    ORD_DIR_RETURN_NOTICE_UPDATE("ordDirReturnNoticeUpdate", "修改直营退货通知单"),
    ORD_DIR_RETURN_NOTICE_APPROVED("ordDirReturnNoticeApproved", "审核直营退货通知单"),

    ORD_DIR_RETURN_NOTICE_INVALID("ordDirReturnNoticeInvlid", "作废直营退货通知单"),

    ORD_DIR_RETURN_NOTICE_PROCESSED("ordDirReturnNoticeProcessed", "已生效直营退货通知单"),

    ORD_DIS_PRESALE_ACTIVITY("ordDisPresaleActivity", "预售活动"),

    ORD_DIS_PRESALE_ACTIVITY_SAVE("ordDisPresaleActivitySave", "保存预售活动"),

    ORD_DIS_PRESALE_ACTIVITY_AUDIT("ordDisPresaleActivityAudit", "审核预售活动"),

    ORD_DIS_PRESALE_ACTIVITY_EXECUTED("ordDisPresaleActivityExecuted", "生效预售活动"),

    ORD_DIS_PRESALE_ACTIVITY_INVALID("ordDisPresaleActivityInvalid", "作废预售活动"),

    ORD_DIS_PRESALE_ACTIVITY_STOPPED("ordDisPresaleActivityStopped", "中止预售活动"),

    ORD_DIS_PRESALE_ACTIVITY_TERMINATED("ordDisPresaleActivityTerminated", "终止预售活动"),

    ORD_DIS_PRESALE_ORDER("ordDisPresaleOrder", "预售订单"),

    ORD_DIS_PRESALE_ORDER_SAVE("ordDisPresaleOrderSave", "保存预售订单"),

    ORD_DIS_PRESALE_ORDER_PAY("ordDisPresaleOrderPay", "支付预售订单"),

    ORD_DIS_PRESALE_ADJUST_ORDER("ordDisPresaleAdjustOrder", "预售调整单"),

    ORD_DIS_PRESALE_ACTIVITY_EXTEND_END_ORDER_DATE("ordDisPresaleActivityExtendEndOrderDate", "延长预售活动订货结束时间"),

    SHIPMENT_AUDIT_DELAY_PUSH_PUR("shipmentAuditDelayPushPur","已审核等待推送采购")
    ;


    private final String code;
    private final String name;

    OrdLogTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static void main(String[] args) {
        System.out.println("ordWholesaleShipmenDtsInvalid".length());
    }
}
