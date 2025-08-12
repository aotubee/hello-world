package com.edc.erp.common.service;

import com.edc.erp.common.entity.OrdStoreInventorySupplyRate;

import java.util.List;

public interface OrdStoreInventorySupplyRateService {

    void batchSave(List<OrdStoreInventorySupplyRate> storeInventorySupplyRateList);
}
