package com.edc.erp.directly.distribution.service;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionResult;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

public interface OrdDirOrderDistributionResultService extends BaseService<OrdDirOrderDistributionResult> {

    @Transactional(rollbackFor = Exception.class)
    void saveNotExistOne(OrdDirOrderDistribution ordDirOrderDistribution, String storeCode);

    void updateByDistributionOrderIdAndStoreCode(OrdDirOrderDistributionResult ordDirOrderDistributionResult);
}
