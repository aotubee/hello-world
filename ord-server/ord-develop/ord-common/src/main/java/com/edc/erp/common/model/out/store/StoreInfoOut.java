/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.out.store;

import lombok.Data;

import java.io.Serializable;

/**
 * StoreInfo远程调用出参
 *
 * @author lishaobo
 */
@Data
public class StoreInfoOut implements Serializable {

    private static final long serialVersionUID = -7758306226668977669L;
    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 门店编码
     */
    private String storeCode;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 所属组织
     */
    private String orgCode;

    /**
     * 门店属性
     */
    private String storeProperty;

    /**
     * ERP门店编码
     */
    private String erpStoreCode;

    /**
     * 所属区域
     */
    private String belongArea;

    /**
     * 所属区域中文值
     */
    private String belongAreaStr;
    /**
     * 省
     */
    private String provinceStr;

    /**
     * 市
     */
    private String cityStr;

    /**
     * 区
     */
    private String countryStr;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 配送方案代码
     */
    private String alcSchemeCode;

    /**
     * 门店商品经营方案代码
     */
    private String saleSchemeCode;

    /**
     * 售价组代码
     */
    private String priceSchemeCode;

    /**
     * 配送方案名称
     */
    private String alcSchemeName;

    /**
     * 门店商品经营方案名称
     */
    private String saleSchemeName;

    /**
     * 售价组名称
     */
    private String priceSchemeName;
}