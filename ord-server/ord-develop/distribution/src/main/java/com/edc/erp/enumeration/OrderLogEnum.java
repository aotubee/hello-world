package com.edc.erp.enumeration;

/**
 * 订货单日志模板枚举
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021年7月14日 11:00
 */
public enum OrderLogEnum {

    /**
     * 创建订货单
     */
    ORDER_CREATE("{0}创建订货单：{1}。", "订货单"),
    /**
     *修改订货单
     */
    UPDATE_ORDER_GOODS("{0}修改订货单{1}商品：{2}。", "订货单"),
    /**
     *手动取消订货单
     */
    CANCEL_ORDER_FOR_APP("用户{0}手动取消订货单：{1}。", "订货单"),
    /**
     *状态由更新为
     */
    ORDER_STATUS_UPDATE("订货单：{0}状态由{1}更新为：{2}。", "订货单"),
    /**
     *生成集货单失败
     */
    CREATE_REQUEST_ORDER_ILLEGAL_SKU_END_ORDER("生成集货单失败，订货单明细中没有符合要货配货的商品，订货单：{0}状态由{1}更新为：{2}。", "订货单"),
    /**
     *订货单
     */
    ORDER_STATUS_UPDATE_FOR_NO_NEED_PAY("订货单：{0}订货额为0元，无需支付，状态由{1}更新为：{2}。", "订货单"),
    /**
     *单据状态改为作废
     */
    CUT_LESS_MIN_AMOUNT_INVALID("{0}截单时已付款金额小于最低起订额{1}，单据状态改为作废。", "订货单"),
    /**
     *截单时已付款金额小于最低起订额
     */
    CUT_LESS_MIN_AMOUNT_INVALID_SINGLE_MODEL("{0}截单时已付款金额小于最低起订额{1}，单据状态由{2}改为{3}。", "订货单"),
    /**
     *截单时因未付款
     */
    CUT_NO_PAY_INVALID("截单时因未付款，订货单{0}状态有{1}改为{2}。", "订货单"),
    /**
     *手动作废订货单
     */
    OPERATE_ORDER_INVALID("运营端手动作废订货单{0}。", "订货单"),
    /**
     *充值校验金额
     */
    PAY_RECHARGE_NOT_ENOUGH("{0}关联支付单号{1}海鼎充值后:账户可用余额{2}（已提交、已接单冻结总金额{3}，部分发货冻结总金额{4}），不足支付订单金额：{5}", "充值校验金额"),
    /**
     *更新对账单配货量
     */
    DELIVERY_UPDATE_DISTRIBUTION_QUANTITY("配货单{0}匹配{1}更新配货量成功。", "更新对账单配货量"),
    /**
     *更新对账单发货量
     */
    DELIVERY_UPDATE_DELIVERY_QUANTITY("配货单{0}匹配{1}更新发货量成功，订货订单状态改为：{2}。", "更新对账单发货量"),
    /**
     *配货单收货
     */
    DELIVERY_TAKE_SUCCESS("{0}对应配货单{1}(海鼎：{2})已收货,收货方：{3}。", "配货单收货"),
    /**
     *申请退货
     */
    RETURN_SEND("向海鼎发起退货请求{0}。", "申请退货"),
    /**
     *手动匹配配货单与订货订单
     */
    MANUAL_MATCHING_DELIVERY_AND_PURCHASE_ORDER("{0}将配货单{1}与订货订单{2}匹配成功，并匹配对账单配货量", "，并更新对账单配货量"),
    /**
     *截单作废单据
     */
    CUT_ONLY_MATERIEL_INVALID("{0}截单时只有911物料仓商品，单据状态全部改为作废。", "截单作废单据"),
    /**
     *运营端截单成功
     */
    BACK_MANUAL_PROCESS_CUT_PURCHASE_ORDER("{0}手动下发订货订单(接单时间由{1}改为{2})，操作结果:{3}。", "运营端截单成功"),
    /**
     *释放订单金额
     */
    DELIVERY_RELEASE_PAID_AMOUNT("订货单{0}，已成功释放冻结金额。", "释放订单金额"),
    /**
     *订货单匹配集货单
     */
    ORDER_REQUEST_ORDER("订货单{0}匹配集货单{1}。", "订货单匹配集货单"),
    /**
     *订货单加推
     */
    ADD_PUSH_ORDER("加推订货单{0}。", "订货单加推"),

    CUT_LESS_MIN_AMOUNT_INVALID_SINGLE_MODEL_MARGE("{0}截单合并要货已付款总金额小于最低起订额，单据状态由{1}改为{2}。", "订货单"),
    /**
     *订货单
     */
    SPRING_FESTIVAL_INVALID_ORDER("春节特定期间订货单{0}直接作废处理。", "订货单"),

    DIS_PRESALE_ORDER("预售订单状态由{0}改为{1}","预售订单"),
    UNFREEZE_CHECK_REQUEST_ORDER("合并集货全商品不可用，订货单{0}释放冻结金额{1}。", "订货单"),
    ORDER_UNFROZEN_FOR_DELIVERY_INVALID("{0}解冻订货额。", "订货单"),
    FIRST_ORDER_UNFROZEN_FOR_DELIVERY_INVALID("{0}解冻铺货额。", "铺货单")
    ;


    private String key;
    private String globalType;

    OrderLogEnum(String key, String globalType) {
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
