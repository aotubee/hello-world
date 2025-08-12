package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.OrdStoreInventorySupplyRate;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdStoreInventorySupplyRateMapper extends BaseMapper<OrdStoreInventorySupplyRate> {

    void batchSave(@Param("storeInventorySupplyRateList") List<OrdStoreInventorySupplyRate> storeInventorySupplyRateList);
}
