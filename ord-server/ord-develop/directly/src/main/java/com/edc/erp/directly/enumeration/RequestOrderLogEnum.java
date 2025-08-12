package com.edc.erp.directly.enumeration;

/**
 * 要货单/要货单日志模板枚举
 *
 * @author : fxw
 * @date 2022-10-24
 */
public enum RequestOrderLogEnum {

    /**
     * 要货单日志枚举
     */
    REQUEST_ORDER_CREATE("创建要货单{0}。", "要货单"),
    REQUEST_DIR_ORDER_CREATE("创建要货单{0}。", "要货单"),
    REQUEST_ORDER_STATUS_UPDATE("要货单：{0}状态由{1}更新为{2}。", "要货单"),
    REQUEST_DIR_ORDER_STATUS_UPDATE("要货单：{0}状态由{1}更新为{2}。", "要货单"),
    ILLEGAL_SKU_NOT_CREATE_REQUEST("{0}商品状态不符合要货配货","要货单"),
    ILLEGAL_SKU_DIR_NOT_CREATE_REQUEST("{0}商品状态不符合要货配货","要货单"),
    CUT_ORDER_RESULT("要货单{0}截单成功，状态由{1}更新为{2}。", "要货单"),
    CUT_DIR_ORDER_RESULT("要货单{0}截单成功，状态由{1}更新为{2}。", "要货单");

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
