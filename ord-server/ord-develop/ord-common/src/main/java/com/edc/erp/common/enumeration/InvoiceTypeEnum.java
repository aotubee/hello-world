/**
 * Copyright © 2010-2021 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.enumeration;

/**
 * 发票类型枚举
 *
 * @author: zhaolei
 * @date: 2024-04-09
 */
public enum InvoiceTypeEnum {

    /**
     * 按门店下单顺序
     */
    VAT("VAT", "增值税发票"),

    /**
     * 按门店销售优先级顺序
     */
    PLAIN_INVOICE("plainInvoice", "普通发票"),

    /**
     * 按门店代码的大小顺序
     */
    RECEIPT_INVOICE("receiptInvoice", "收据");

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    InvoiceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (InvoiceTypeEnum biz : values()) {
            if (biz.getCode().equals(code)) {
                return biz.getName();
            }
        }
        return null;
    }
}
