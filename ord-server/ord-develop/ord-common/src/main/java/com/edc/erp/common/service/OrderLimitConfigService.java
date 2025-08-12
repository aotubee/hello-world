/**
 * Copyright © 2010-2020 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.service;

import com.edc.erp.common.entity.OrderLimitConfig;
import com.edc.plugins.mybatis.service.BaseService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * OrderLimitConfig业务层访问接口
 *
 * @author: lishaobo
 * @date: 2023-04-26
 */
public interface OrderLimitConfigService extends BaseService<OrderLimitConfig> {

    /**
     * 查询门店下商品的订货上限
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    Map<String, BigDecimal> findGoodsLimitByStoreCode(String storeCode, String bizOrgCode);
}
