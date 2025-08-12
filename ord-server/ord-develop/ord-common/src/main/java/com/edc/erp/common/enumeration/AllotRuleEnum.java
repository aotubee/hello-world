/**
 * Copyright © 2010-2021 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.enumeration;

/**
 * 业务原因维度枚举
 *
 * @author: zhaolei
 * @date: 2021-12-15
 */
public enum AllotRuleEnum {

    /**
     * 按门店下单顺序
     */
    STORE_ORDER("storeOrder", "按门店下单顺序"),

    /**
     * 按门店销售优先级顺序
     */
    STORE_SALES("storeSales", "按门店销售优先级顺序"),

    /**
     * 按门店代码的大小顺序
     */
    STORE_CODE_SIZE("storeCodeSize", "按门店代码的大小顺序");

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    AllotRuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public static String getNameByCode(String code) {
        for (AllotRuleEnum biz : values()) {
            if (biz.getCode().equals(code)) {
                return biz.getName();
            }
        }
        return null;
    }
}
