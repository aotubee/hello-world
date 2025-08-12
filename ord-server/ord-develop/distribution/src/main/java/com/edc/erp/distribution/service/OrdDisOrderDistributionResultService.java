package com.edc.erp.distribution.service;

import com.edc.erp.distribution.entity.OrdDisOrderDistribution;
import com.edc.erp.distribution.entity.OrdDisOrderDistributionResult;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

public interface OrdDisOrderDistributionResultService extends BaseService<OrdDisOrderDistributionResult> {

    @Transactional(rollbackFor = Exception.class)
    void saveNotExistOne(OrdDisOrderDistribution ordDisOrderDistribution, String storeCode);

    void updateByDistributionOrderIdAndStoreCode(OrdDisOrderDistributionResult ordDisOrderDistributionResult);
}
