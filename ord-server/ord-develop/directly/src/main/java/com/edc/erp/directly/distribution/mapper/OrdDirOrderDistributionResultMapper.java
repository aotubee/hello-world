package com.edc.erp.directly.distribution.mapper;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionResult;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdDirOrderDistributionResultMapper extends BaseMapper<OrdDirOrderDistributionResult> {

    int updateByDistributionOrderIdAndStoreCode(OrdDirOrderDistributionResult ordDirOrderDistributionResult);
}
