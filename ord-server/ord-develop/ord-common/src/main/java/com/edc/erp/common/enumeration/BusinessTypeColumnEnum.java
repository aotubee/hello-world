package com.edc.erp.common.enumeration;

/**
 * 业务类型枚举
 * @author weichao
 */
public enum BusinessTypeColumnEnum {
    /**
     * 购物车
     */
    SHOP_CART("shopCart","购物车"),
    /**
     * 上下限跑货
     */
    UP_LOW_DOWN("upLowDown","上下限跑货"),
    /**
     * 分货单/配销分货单
     */
    DISTRIBUTION("distribution","分货单/配销分货单"),
    /**
     * 配货单/门店要货单
     */
    DISTRIBUTION_BILL("distributionBill","配货单/门店要货单"),
    /**
     * 配销单/集货单
     */
    DEALER_BILL("dealerBill","配销单/集货单"),
    /**
     * 退货-普通退货
     */
    RETURN_GOODS("returnGoods","退货-普通退货"),
    /**
     * 退货-特退/限量
     */
    RETURN_GOODS_FAST("returnGoodsFast","退货-特退/限量"),
    /**
     * 门店订货单/订货单
     */
    ORDER_GOODS("orderGoods","门店订货单/订货单"),
    /**
     * 直送收货
     */
    ORDER_RECEIVING("orderReceiving","直送收货"),
    /**
     * 直送退货
     */
    ORDER_RETURN("orderReturn","直送退货"),
    /**
     * 门店调拨
     */
    TRANSFER_ORDER("transferOrder", "门店调拨"),
    /**
     * 门店报损
     */
    REPORTED_LOSS("reportedLoss", "门店报损"),
    /**
     * 铺货单/配销铺货单
     */
    FIRST_ORDER("firstOrder","铺货单/配销铺货单"),

    INTELLIGENT_UP_LOW_DOWN("intelligentUpLowDown","智能上下限跑货"),

    HEAD_OFFICE_REPLENISH("headOfficeReplenish","总部补单"),
    ;
    private String type;
    private String name;
    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    BusinessTypeColumnEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }
    public static boolean getExistByType(String type) {
        for (BusinessTypeColumnEnum ele : values()) {
            if (type.equals(ele.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据来源类型获取业务类型
     * @param sourceCode
     * @return
     */
    public static String getTypeBySourceCode(String sourceCode){
        if (SourceTypeEnum.DISTRIBUTION.getKey().equals(sourceCode)) {
            return DISTRIBUTION.type;
        }
        if (SourceTypeEnum.UPLOWDOWN.getKey().equals(sourceCode)) {
            return UP_LOW_DOWN.type;
        }
        if (SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode)) {
            return SHOP_CART.type;
        }
        return ORDER_GOODS.type;
    }
}
