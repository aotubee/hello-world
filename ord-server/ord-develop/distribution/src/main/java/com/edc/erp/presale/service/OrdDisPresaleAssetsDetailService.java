package com.edc.erp.presale.service;

import com.edc.erp.presale.entity.OrdDisPresaleActivity;
import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.model.in.QueryPresaleAssetsDetailPageIn;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsGoodsIn;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailOut;
import com.edc.erp.presale.model.out.PresaleAssetsForAppOut;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrdDisPresaleAssetsDetailService extends BaseService<OrdDisPresaleAssetsDetail> {
    @Transactional(rollbackFor = Exception.class)
    void updatePresaleAssetsDetail(UpdateDisPresaleAssetsGoodsIn updateDisPresaleAssetsGoodsIn, Long assetsId, String loginUsername, String businessType, String sourceNo);

    Page<OrdDisPresaleAssetsDetailOut> findPresaleAssetsDetailForPage(QueryPresaleAssetsDetailPageIn queryPresaleAssetsDetailPageIn);

    Boolean checkGoodsIsExcess(String storeCode, String goodsCode, BigDecimal quantity);

    Response<String> checkGoodsIsExcessAndInOrderTime(OrdDisPresaleAssets ordDisPresaleAssets, String goodsCode, BigDecimal quantity);

    OrdDisPresaleAssetsDetail getStoreAssetsDetail(String storeCode, String goodsCode, LocalDateTime orderCreateTime);

    OrdDisPresaleAssetsDetail getStoreGoodsAssetsDetail(String storeCode, String goodsCode, String presaleActivityNo);

    List<OrdDisPresaleAssetsDetail> getStoreGoodsAssetsDetailList(String storeCode, String goodsCode);

    List<PresaleAssetsForAppOut> findStorePresaleAssetsInfo(String storeCode);

    List<Long> findNeedUpdateStatusAssetsDetailList();

    void updateStatus(Long id, String updateStatus, String nowStatus, String loginUsername);

    OrdDisPresaleAssetsDetail getOneById(Long id);

    // String exportPresaleAssetsDetail(QueryPresaleAssetsDetailPageIn queryPresaleAssetsDetailPageIn);

    List<OrdDisPresaleAssetsDetail> findStoreGoodsAssetsDetailList(String storeCode);

    @Transactional(rollbackFor = Exception.class)
    void resetAssetsDetailList(OrdDisPresaleActivity presaleActivity);
}
