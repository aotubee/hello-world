package com.edc.erp.common.service;

import com.edc.erp.common.model.entity.EquipmentStockAllot;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.AsyncDeliveryImportInfoIn;
import com.edc.erp.common.model.in.StoreAreaIn;
import com.edc.erp.common.model.in.store.QueryBizOrgCodeStoreIn;
import com.edc.erp.common.model.in.store.QueryEquipmentStockAllotIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.in.store.StoreStatusInfo;
import com.edc.erp.common.model.out.store.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 门店信息
 *
 * @author weichao
 */
public interface StoreCenterService {
    /**
     * 根据门店code查询门店信息
     *
     * @param storeCode
     * @return
     */
    StoreOut getStoreInfoByErpStoreCode(String storeCode);

    /**
     * 根据门店区域代码或名称查询门店编码集合
     *
     * @param storeArea
     * @param storeName
     * @return
     */
    List<String> findStoreCodeListByStoreAreaOrName(String storeArea, String storeName);

    /**
     * 获取门店配送信息
     *
     * @param storeId
     * @return
     */
    StoreLogisticsOut getStoreOnline(Integer storeId);

    /**
     * 根据门店代码查询门店单元关联订单类型配置id集合
     *
     * @param storeCode
     * @return
     */
    List<Integer> findByStoreCode(String storeCode);


    /**
     * 根据条件查询门店信息
     *
     * @param storeInfoIn
     * @return
     */
    List<StoreInfoOut> getStoreInfoByCode(StoreInfoIn storeInfoIn);

    /**
     * 查询门店业务而控制开关
     *
     * @param storeLifeCycle
     * @param bizOrgCode
     * @return
     */
    StoreStatusBusinessSwitch getSwitch(@RequestParam String storeLifeCycle, @RequestParam String bizOrgCode);

    /**
     * 根据门店代码和门店类型查询门店信息和客户信息
     *
     * @param storeCode
     * @param storeProperty
     * @return
     */
    StoreAndClientInfoOut getStoreByStoreCodeAndType(String storeCode, String storeProperty, String bizOrgCode);

    /**
     * 获取本组织自动跑货的门店代码集合
     *
     * @param bizOrgCode
     * @param storeProperty
     * @return
     */
    List<String> findIsAutoReplenishmentStoreCodeList(String bizOrgCode, String storeProperty);


    /**
     * 根据门店区域code查名称
     */
    String getNameByCode(String storeArea, String bizOrgCode);

    StoreInfo getStoreByCode(String storeCode, String bizOrgCode);

    /**
     * 获取门店信息(门店类型,业务类型)
     *
     * @param storeStatusInfo
     * @return
     */
    StoreInfo getStatusStoreInfo(StoreStatusInfo storeStatusInfo);

    /**
     * 根据门店代码获取门店单元
     *
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    StoreUnit getStoreUnitByStoreCode(String storeCode, String bizOrgCode);

    /**
     * 根据门店代码获取转单优先级
     *
     * @return
     */
    String getOrderPriorityByStoreCode(String storeCode, String bizOrgCode, String stockCode, String distributionType);

    Map<String, EquipmentStockAllot> findByWmsCodeAndStockCode(QueryEquipmentStockAllotIn queryEquipmentStockAllotIn);

    StoreLogisticsOut getStoreLogisticsByStoreCode(String storeCode, String bizOrgCode);

    /**
     * 根据门店属性查询门店
     *
     * @param storeCodes
     * @param storeProperty
     * @param bizOrgCode
     * @return
     */
    List<StoreInfo> findStoreInfoByProperty(List<String> storeCodes, String storeProperty, String bizOrgCode);

    /**
     * 根据门店类型和组织查询已启用的APP用户的门店代码集合
     * @param storeProperty
     * @param bizOrgCode
     * @return
     */
    List<String> findEnableAppStoreCodeListByPropertyAndOrg(String storeProperty, String bizOrgCode);

    /**
     * 通过门店单元代码集合和组织代码查询下面关联的门店代码集合
     * @param unitCodeList
     * @param bizOrgCode
     * @return
     */
    List<String> findStoreCodeListByUnitCodeListAndOrgCode(List<String> unitCodeList, String bizOrgCode);

    StoreBusinessSwitchOut getStoreBusinessType(String storeCode, String bizOrgCode, String businessType);

    StoreAndStatusSwitchOut getStoreAndStatusSwitch(String storeCode, String bizOrgCode);

    List<StoreInfo> findStoreInfoByStoreCodeList(AsyncDeliveryImportInfoIn asyncDeliveryImportInfoIn);

    /**
     * @Description: 根据当前登录人批量获取可配+所属 的门店组织
     * @Author: ZhangYao
     * @Date: 2024/3/27 15:37
     * @param storeCode:
     * @param bizOrgCode:
     * @return: com.edc.erp.common.model.entity.StoreInfo
     **/
    StoreInfo getStoreInfoByStoreCode(String storeCode, String bizOrgCode);

    StoreInfo getStatusStoreInfoByAuthOrg(StoreStatusInfo storeStatusInfo);

    List<StoreDelivery> getByStoreCode(String storeCode);

    List<StoreInfo> findSimpleStoresByCodes(QueryBizOrgCodeStoreIn queryBizOrgCodeStoreIn);

    List<StoreInfoOut> findByAreaCodes(StoreInfoIn storeInfoIn);

    List<StoreInfoOut> findByAreaCodeList(StoreInfoIn storeInfoIn);

    List<String> findChildAreasByCodes(StoreAreaIn storeAreaIn);
}
