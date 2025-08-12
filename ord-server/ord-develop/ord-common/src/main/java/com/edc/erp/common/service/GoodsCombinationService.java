package com.edc.erp.common.service;

import com.edc.erp.common.entity.GoodsCombination;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 商品组合表(GoodsCombination)表服务接口
 *
 * @author fxw
 * @since 2022-10-19 15:08:49
 */
public interface GoodsCombinationService extends BaseService<GoodsCombination> {

    /**
     * 根据商品组合值查询订单类型配置信息
     *
     * @param storeCode
     * @param bizOrgCode
     * @param combinationTypeValueList
     * @return
     */
    Integer getOrderTypeConfigIdByCombinationTypeValues(String storeCode, String bizOrgCode, List<String> combinationTypeValueList);
}
