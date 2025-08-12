package com.edc.erp.enumeration;

/**
 * 集货单日志模板枚举
 *
 * @author : fxw
 * @date 2022-10-24
 */
public enum RequestOrderLogEnum {
    /**
     * 创建集货单
     */
    REQUEST_ORDER_CREATE("创建集货单{0}。", "集货单"),
    /**
     * 状态由 更新为
     */
    REQUEST_ORDER_STATUS_UPDATE("集货单：{0}状态由{1}更新为{2}。", "集货单"),

    /**
     * 非法sku
     */
    ILLEGAL_SKU_NOT_CREATE_REQUEST("{0}商品状态不符合集货配货","集货单"),

    /**
     * 截单成功，状态由 更新为
     */
    CUT_ORDER_RESULT("集货单{0}截单成功，状态由{1}更新为{2}。", "集货单");

    private String key;
    private String globalType;

    RequestOrderLogEnum(String key, String globalType) {
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
