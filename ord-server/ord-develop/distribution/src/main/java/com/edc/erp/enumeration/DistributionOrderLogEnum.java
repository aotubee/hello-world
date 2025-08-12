package com.edc.erp.enumeration;

/**
 * 订货订单日志模板枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021年5月7日 09:52
 */
public enum DistributionOrderLogEnum {
    /**
     * 审核分货单
     */
    DISTRIBUTION_ORDER_SUCCESS("配销分货单审核，成功创建{0}家门店订单，失败{1}家", "审核分货单"),

    /**
     * 审核分货单
     */
    DISTRIBUTION_ORDER_ERROR("配销分货单审核，成功创建{0}家门店订单，失败{1}家，包含（{2}）", "审核分货单"),
    /**
     * 创建分货单
      */
    DISTRIBUTION_ORDER_CREATE("创建分货单{0}", "创建分货单"),
    /**
     * 作废分货单
     */
    DISTRIBUTION_ORDER_INVALID("作废分货单{0}", "作废分货单"),
    /**
     * 作废分货单关联订货单
     */
    DISTRIBUTION_ORDER_JOIN_INVALID("作废分货单关联的订货单，未成功的是：{0}", "作废分货单关联订货单"),

    DISTRIBUTION_FILTER_SKU("分货单创建订货单前校验商品异常信息：{0}", "分货单创建订货单前校验商品"),

    DISTRIBUTION_ASYNC_AUDIT("分货单已审核", "审核分货单"),
    ;

    private String key;
    private String globalType;

    DistributionOrderLogEnum(String key, String globalType) {
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
