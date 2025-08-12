package com.edc.erp.presale.service;

import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.model.in.QueryPresaleAssetsPageIn;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsIn;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsPageOut;
import com.edc.plugins.common.model.page.Page;

public interface OrdDisPresaleAssetsService {

    OrdDisPresaleAssets getOrdDisPresaleAssetsByStoreCode(String storeCode);

    void updatePresaleAssets(UpdateDisPresaleAssetsIn updateDisPresaleAssetsIn);

    Page<OrdDisPresaleAssetsPageOut> findStorePresaleAssetsForPage(QueryPresaleAssetsPageIn queryPresaleAssetsPageIn);

    OrdDisPresaleAssets getOrdDisPresaleAssetsById(Long id);

    void saveOrdDisPresaleAssets(OrdDisPresaleAssets ordDisPresaleAssets);

    String exportStorePresaleAssets(QueryPresaleAssetsPageIn queryPresaleAssetsPageIn);
}
