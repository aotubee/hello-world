package com.edc.erp.common.service.impl;

import com.edc.erp.common.constant.EquipmentBusinessConstant;
import com.edc.erp.common.model.out.InvBizRsnTransOut;
import com.edc.erp.common.model.out.equipment.EquipmentBusinessReasonOut;
import com.edc.erp.common.rpc.EquipmentBusinessReasonClient;
import com.edc.erp.common.service.EquipmentBusinessReasonServer;
import com.edc.plugins.cache.CacheConst;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 库存业务原因
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Service
@Slf4j
public class EquipmentBusinessReasonServerImpl implements EquipmentBusinessReasonServer {

    @Autowired
    private EquipmentBusinessReasonClient equipmentBusinessReasonClient;


    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "warehouse_inv_biz_rsn_trans",
            key = "#bizOrgCode+ ':' + #bizRsnCode + '_' + #isDelete", unless = "#result == null")
    public InvBizRsnTransOut getWarehouseBizRsnTransByCode(String bizRsnCode, String bizOrgCode, Integer isDelete) {
        Response<InvBizRsnTransOut> response = equipmentBusinessReasonClient.getInvBizRsnTransByCode(bizRsnCode, bizOrgCode, isDelete);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "inv_biz_rsn_trans",
            key = "#bizOrgCode+ ':' + #bizRsnCode + '_' + #isDelete", unless = "#result == null")
    public InvBizRsnTransOut getStoreInvBizRsnTransByCode(String bizRsnCode, String bizOrgCode, Integer isDelete) {
//        Response<InvBizRsnTransOut> response = equipmentBusinessReasonClient.getInvBizRsnTransByCode(bizRsnCode, bizOrgCode, isDelete);
        Response<InvBizRsnTransOut> response = equipmentBusinessReasonClient.invStoreBusinessReason(bizRsnCode, bizOrgCode, isDelete);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "logc_biz_rsn_reason_code",
            key = "#bizOrgCode+ ':' + #businessReasonType + '_' + #businessReasonName", unless = "#result == null")
    public List<EquipmentBusinessReasonOut> page(String bizOrgCode, String businessReasonType, String businessReasonName,String businessReasonDimension) {
        Response<Page<EquipmentBusinessReasonOut>> response = equipmentBusinessReasonClient.page(bizOrgCode,
                1,
                100,
                businessReasonType,
                businessReasonName,
                businessReasonDimension);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return Collections.emptyList();
        }
        Page<EquipmentBusinessReasonOut> page = response.getData();
        if (null == page || CollectionUtils.isEmpty(page.getList())) {
            return Collections.emptyList();
        }
        return page.getList();
    }

    @Override
    public String getDefaultReasonCodeByName(String bizOrgCode,String businessReasonType,String businessReasonName,String businessReasonDimension) {
        List<EquipmentBusinessReasonOut> list = this.page(bizOrgCode, businessReasonType, businessReasonName, businessReasonDimension);
        if (CollectionUtils.isEmpty(list)) return "";
        Map<String, String> map = list.stream().collect(Collectors.toMap(EquipmentBusinessReasonOut::getBusinessReasonName, item -> item.getBusinessReasonCode()));
        return map.get(EquipmentBusinessConstant.DEFAULT_BUSINESS_REASON_NAME);
    }
}
