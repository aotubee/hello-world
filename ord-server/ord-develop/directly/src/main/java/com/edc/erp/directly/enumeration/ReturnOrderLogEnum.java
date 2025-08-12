package com.edc.erp.directly.enumeration;

/**
 * 退货单日志模板枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022年7月13日 11:00
 */
public enum ReturnOrderLogEnum {

    /**
     * 退货单日志
     */
    RETURN_ORDER_STATUS_UPDATE("退货单{0}状态由{1}变更为{2}。", "returnOrderStatus"),
    ZK_RETURN_ORDER_AUDIT_RATIFY_ERROR("中科退货单{0}明细：{1}审批数量大于申请数量。", "returnRatifyQuantity"),
    RETURN_ORDER_SEND_TO_ZK("退货单{0}成功推送中科。", "returnRatifyQuantity"),
    ZK_RETURN_ORDER_AUDIT_ACTUAL_ERROR("中科退货单{0}明细：{1}实际退货数量大于比准退货数量。", "returnActualQuantity"),
    SEND_HD_EMPTY_ITEMS("退货单{0}发送海鼎明细为空", "sendReturnOrderDetail"),
    RETURN_CREATE("{0}创建退货单：{1}。", "退货单");

    private String key;
    private String globalType;

    ReturnOrderLogEnum(String key, String globalType) {
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
