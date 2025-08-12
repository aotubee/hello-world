package com.edc.erp.distribution.mapper;

import com.edc.erp.distribution.entity.OrdDisOrderDistributionResult;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdDisOrderDistributionResultMapper extends BaseMapper<OrdDisOrderDistributionResult> {

    int updateByDistributionOrderIdAndStoreCode(OrdDisOrderDistributionResult ordDisOrderDistributionResult);
}
