/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.in.store;

import com.edc.plugins.common.model.page.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * StoreInfo远程调用入参
 *
 * @author weichao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfoIn extends Page {

    private static final long serialVersionUID = 720968237532071707L;
    /**
     * 门店编码
     */
    private String storeCode;

    /**
     * 海鼎门店编码（与storeCode一样，为了与门店中心一致）
     */
    private String erpStoreCode;

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
     * 门店状态
     */
    private String storeStatus;

    /**
     * 门店业态
     */
    private String storeType;

    /**
     * 门店业态
     */
    private String bizOrgCode;


    /**
     * 所属区域
     */
    private String belongArea;

    private List<String> areaCodes;

    public StoreInfoIn(String belongArea) {
        this.belongArea = belongArea;
    }
}
