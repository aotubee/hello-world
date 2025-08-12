package com.edc.erp.common.service.impl;

import com.edc.erp.common.entity.OrdStoreInventorySupplyRate;
import com.edc.erp.common.mapper.OrdStoreInventorySupplyRateMapper;
import com.edc.erp.common.service.OrdStoreInventorySupplyRateService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @ClassName OrdStoreInventorySupplyRateServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/4/30 16:42
 **/
@Service
@RequiredArgsConstructor
public class OrdStoreInventorySupplyRateServiceImpl implements OrdStoreInventorySupplyRateService {

    private final OrdStoreInventorySupplyRateMapper ordStoreInventorySupplyRateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdStoreInventorySupplyRate> storeInventorySupplyRateList) {
        if (CollectionUtils.isEmpty(storeInventorySupplyRateList)) {
            return;
        }
        ordStoreInventorySupplyRateMapper.batchSave(storeInventorySupplyRateList);
    }
}
