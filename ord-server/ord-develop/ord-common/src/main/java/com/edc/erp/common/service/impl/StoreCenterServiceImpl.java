package com.edc.erp.common.service.impl;

import com.edc.erp.common.mapper.StoreCenterMapper;
import com.edc.erp.common.model.entity.EquipmentStockAllot;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.AsyncDeliveryImportInfoIn;
import com.edc.erp.common.model.in.StoreAreaIn;
import com.edc.erp.common.model.in.store.QueryBizOrgCodeStoreIn;
import com.edc.erp.common.model.in.store.QueryEquipmentStockAllotIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.in.store.StoreStatusInfo;
import com.edc.erp.common.model.out.store.*;
import com.edc.erp.common.rpc.StoreCenterClient;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.plugins.cache.CacheConst;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 门店信息实现类
 *
 * @author weichao
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StoreCenterServiceImpl implements StoreCenterService {

    private final StoreCenterClient storeCenterClient;

    private final StoreCenterMapper storeCenterMapper;

    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "ord_old_store_info",
            key = "'ordstore:' + #storeCode", unless = "#result == null")
    public StoreOut getStoreInfoByErpStoreCode(String storeCode) {
        Response<StoreOut> response = storeCenterClient.getStoreInfoByErpStoreCode(storeCode);
        if (!response.isSuccess() || null == response.getData().getStoreId()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 根据门店区域代码或名称查询门店编码集合
     *
     * @param storeArea
     * @return
     */
    @Override
    public List<String> findStoreCodeListByStoreAreaOrName(String storeArea, String storeName) {
        Response<List<String>> response = storeCenterClient.findStoreCodeList(storeArea, storeName);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 根据门店代码查询门店单元关联订单类型配置id集合
     *
     * @param storeCode
     * @return
     */
    @Override
    public List<Integer> findByStoreCode(String storeCode) {
        Response<List<Integer>> response = storeCenterClient.findOrderTypeIdByStoreCode(storeCode);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<StoreInfoOut> getStoreInfoByCode(StoreInfoIn storeInfoIn) {
        Response<Page<StoreInfoOut>> pageResponse = storeCenterClient.findStoreInfo(storeInfoIn);
        if (!pageResponse.isSuccess() || null == pageResponse.getData()) {
            log.error(pageResponse.getMessage());
            return Collections.emptyList();
        }
        return pageResponse.getData().getList();
    }

    @Override
    public StoreStatusBusinessSwitch getSwitch(String storeLifeCycle, String bizOrgCode) {
        Response<StoreStatusBusinessSwitch> response = storeCenterClient.getSwitch(storeLifeCycle, bizOrgCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 根据门店代码和门店类型查询门店信息和客户信息
     *
     * @param storeCode
     * @param storeProperty
     * @return
     */
    @Override
    public StoreAndClientInfoOut getStoreByStoreCodeAndType(String storeCode, String storeProperty, String bizOrgCode) {
        Response<StoreAndClientInfoOut> response = storeCenterClient.getStoreByStoreCodeAndType(storeCode, storeProperty, bizOrgCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * @param bizOrgCode
     * @param storeProperty
     * @return
     */
    @Override
    public List<String> findIsAutoReplenishmentStoreCodeList(String bizOrgCode, String storeProperty) {
        Response<List<String>> response = storeCenterClient.findIsAutoReplenishmentStoreCodeListByProperty(bizOrgCode, storeProperty);
        if (!response.isSuccess() || CollectionUtils.isEmpty(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 根据门店区域code查名称
     *
     * @param storeArea
     */
    @Override
    public String getNameByCode(String storeArea, String bizOrgCode) {
        return storeCenterMapper.getNameByCode(storeArea, bizOrgCode);
    }

    @Override
    public StoreLogisticsOut getStoreOnline(Integer storeId) {
        Response<StoreLogisticsOut> response = storeCenterClient.findStoreOnline(storeId);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }


    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "ord_store_info",
            key = "#bizOrgCode+ ':' + #storeCode", unless = "#result == null")
    public StoreInfo getStoreByCode(String storeCode, String bizOrgCode) {
        Response<StoreInfo> response = storeCenterClient.getStoreInfo(storeCode, bizOrgCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error("查询组织{}下---------门店{}失败，返回消息{}", bizOrgCode, storeCode, response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 获取门店信息(门店类型,业务类型)
     *
     * @param storeStatusInfo
     * @return
     */
    @Override
    public StoreInfo getStatusStoreInfo(StoreStatusInfo storeStatusInfo) {
        Response<StoreInfo> response = storeCenterClient.getStatusStoreInfo(storeStatusInfo);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 根据门店代码获取门店单元
     *
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public StoreUnit getStoreUnitByStoreCode(String storeCode, String bizOrgCode) {
        Response<StoreUnit> response = storeCenterClient.getStoreUnitByStoreCode(storeCode, bizOrgCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 根据门店代码获取转单优先级
     *
     * @param storeCode
     * @param stockCode
     * @param distributionType
     * @return
     */
    @Override
    public String getOrderPriorityByStoreCode(String storeCode, String bizOrgCode, String stockCode, String distributionType) {
        Response<String> response = storeCenterClient.getOrderPriorityByStoreCode(storeCode, bizOrgCode, stockCode, distributionType);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public Map<String, EquipmentStockAllot> findByWmsCodeAndStockCode(QueryEquipmentStockAllotIn queryEquipmentStockAllotIn) {
        Response<Map<String, EquipmentStockAllot>> response = storeCenterClient.findByWmsCodeAndStockCode(queryEquipmentStockAllotIn);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public StoreLogisticsOut getStoreLogisticsByStoreCode(String storeCode, String bizOrgCode) {
        Response<StoreLogisticsOut> response = storeCenterClient.getStoreLogisticsByStoreCode(storeCode, bizOrgCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<StoreInfo> findStoreInfoByProperty(List<String> storeCodes, String storeProperty, String bizOrgCode) {
        Response<List<StoreInfo>> response = storeCenterClient.findStoreInfoByProperty(storeCodes, storeProperty, bizOrgCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<String> findEnableAppStoreCodeListByPropertyAndOrg(String storeProperty, String bizOrgCode) {
        return storeCenterMapper.findEnableAppStoreCodeListByPropertyAndOrg(storeProperty, bizOrgCode);
    }

    @Override
    public List<String> findStoreCodeListByUnitCodeListAndOrgCode(List<String> unitCodeList, String bizOrgCode) {
        Response<List<String>> response = storeCenterClient.findStoreCodeListByUnitCodeListAndOrgCode(unitCodeList, bizOrgCode);
        if (!response.isSuccess()) {
            throw new BusinessException("获取门店代码集合错误--" + response.getMessage());
        }
        return response.getData();
    }

    @Override
    public StoreBusinessSwitchOut getStoreBusinessType(String storeCode, String bizOrgCode, String businessType) {
        Response<StoreBusinessSwitchOut> response = storeCenterClient.getStoreBusinessType(storeCode, bizOrgCode, businessType);
        if (!response.isSuccess()) {
            throw new BusinessException("获取门店是否可做库存调整业务错误--" + response.getMessage());
        }
        return response.getData();
    }

    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "ord_store_status_switch",
            key = "#storeCode+ ':' + #bizOrgCode", unless = "#result == null")
    @Override
    public StoreAndStatusSwitchOut getStoreAndStatusSwitch(String storeCode, String bizOrgCode) {
        Response<StoreAndStatusSwitchOut> response = storeCenterClient.getStoreAndStatusSwitch(storeCode, bizOrgCode);
        if (!response.isSuccess()) {
            throw new BusinessException("获取门店基础信息与业务开关信息错误--" + response.getMessage());
        }
        return response.getData();
    }

    @Override
    public List<StoreInfo> findStoreInfoByStoreCodeList(AsyncDeliveryImportInfoIn asyncDeliveryImportInfoIn) {
        Response<List<StoreInfo>> response = storeCenterClient.findStoreInfoByStoreCodeList(asyncDeliveryImportInfoIn);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public StoreInfo getStoreInfoByStoreCode(String storeCode, String bizOrgCode) {
        AsyncDeliveryImportInfoIn asyncDeliveryImportInfoIn = new AsyncDeliveryImportInfoIn();
        asyncDeliveryImportInfoIn.setStoreCodeList(Collections.singletonList(storeCode));
        asyncDeliveryImportInfoIn.setBizOrgCode(bizOrgCode);
        Response<List<StoreInfo>> response = storeCenterClient.findStoreInfoByStoreCodeList(asyncDeliveryImportInfoIn);
        if (!response.isSuccess() || null == response.getData() || response.getData().size() == 0) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData().get(NumberUtil.INTEGER_ZERO);
    }

    @Override
    public StoreInfo getStatusStoreInfoByAuthOrg(StoreStatusInfo storeStatusInfo) {
        Response<StoreInfo> response = storeCenterClient.getStatusStoreInfoByAuthOrg(storeStatusInfo);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    //    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "ord_store_delivery_logic",
//            key = "#storeCode", unless = "#result == null")
    @Override
    public List<StoreDelivery> getByStoreCode(String storeCode) {
        Response<List<StoreDelivery>> response = storeCenterClient.getByStoreCode(storeCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<StoreInfo> findSimpleStoresByCodes(QueryBizOrgCodeStoreIn queryBizOrgCodeStoreIn) {
        Response<List<StoreInfo>> response = storeCenterClient.findSimpleStoresByCodes(queryBizOrgCodeStoreIn);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public List<StoreInfoOut> findByAreaCodes(StoreInfoIn storeInfoIn) {
        Response<List<StoreInfoOut>> response = storeCenterClient.findByAreaCodes(storeInfoIn);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return Collections.emptyList();
        }
        return response.getData();
    }

    @Override
    public List<StoreInfoOut> findByAreaCodeList(StoreInfoIn storeInfoIn) {
        Response<List<StoreInfoOut>> response = storeCenterClient.findByAreaCodeList(storeInfoIn);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return Collections.emptyList();
        }
        return response.getData();
    }

    @Override
    public List<String> findChildAreasByCodes(StoreAreaIn storeAreaIn) {
        Response<List<String>> response = storeCenterClient.findChildAreasByCodes(storeAreaIn);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return Collections.emptyList();
        }
        return response.getData();
    }
}
