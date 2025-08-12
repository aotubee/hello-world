package com.edc.erp.common.enumeration.warning;

/**
 * 消息推送模板
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020年06月20日 13:56
 */
public enum MessagePushTemplateEnum {

    PENDING_PAYMENT("订单待付款通知", "您有一笔订单待支付，订货清单{0},订货额{1}元，详情前往订货记录-已提交订单中查看。"),
    PAYMENT_SUCCESSFUL("订单支付成功", "您的订单支付成功，订货清单{0}，订货额{1}，详情前往订货记录-已提交订单中查看。"),
    REPLENISHMENT_ORDER_APPROVED("上下限申请单审核结果", "{0}，{1},上下限申请单单号是{2}，申请商品数为{3}，详情请前往调整-上下限调整-历史记录，或者我的-上下限调整-历史记录中查看。"),
    INVENTORY_ADJUSTMENT_ORDER_APPROVED("库存调整单审核结果", "{0}，{1},库存调整单单号是{2}，申请商品数为{3}，详情请前往调整-库存调整-历史记录，或者我的-库存调整-历史记录中查看。"),
    REPORTED_LOSS_ORDER_APPROVED("门店报损单审核结果", "{0}，{1},门店报损单单号是{2}，报损商品数为{3}，报损金额为{4}元，详情请前往我的-门店报损中查看。"),
    FRESH_FOOD_ENABLE("鲜食目录生效通知", "您有新的鲜食目录，代码{0},名称{1}，详情请前往短保订货-鲜食中查看订货。"),
    ORDER_PAYMENT_SUCCESSFUL("订货单提交成功", "您的订货单{0}支付成功，订货额{1}。");

    private String title;
    private String value;

    MessagePushTemplateEnum(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return this.title;
    }

    public String getValue() {
        return this.value;
    }
}
