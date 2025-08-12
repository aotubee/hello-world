package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.model.in.QueryPresaleAssetsPageIn;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsPageOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdDisPresaleAssetsMapper extends BaseMapper<OrdDisPresaleAssets> {

    OrdDisPresaleAssets getOrdDisPresaleAssetsByStoreCode(@Param("storeCode") String storeCode);

    List<OrdDisPresaleAssetsPageOut> findStorePresaleAssetsByPage(QueryPresaleAssetsPageIn queryPresaleAssetsPageIn);
}
