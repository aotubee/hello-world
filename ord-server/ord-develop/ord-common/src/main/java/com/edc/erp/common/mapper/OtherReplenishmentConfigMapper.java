package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.OtherReplenishmentConfig;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtherReplenishmentConfigMapper extends BaseMapper<OtherReplenishmentConfig> {


    List<OtherReplenishmentConfig> findOtherReplenishmentConfigList(@Param("bizOrgCode") String bizOrgCode, @Param("storeCodeList") List<String> storeCodeList);

    List<OtherReplenishmentConfig> findNeedOtherReplenishmentStoreList(@Param("bizOrgCode") String bizOrgCode, @Param("storeProperty") String storeProperty);
}
