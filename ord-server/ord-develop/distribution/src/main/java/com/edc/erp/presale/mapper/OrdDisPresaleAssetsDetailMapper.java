package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.model.in.QueryPresaleAssetsDetailPageIn;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsGoodsIn;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailExtOut;
import com.edc.erp.presale.model.out.OrdDisPresaleAssetsDetailOut;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdDisPresaleAssetsDetailMapper extends BaseMapper<OrdDisPresaleAssetsDetail> {

    int updateSurplusAndOrderQuantity(UpdateDisPresaleAssetsGoodsIn updateDisPresaleAssetsGoodsIn);

    List<OrdDisPresaleAssetsDetailOut> findPresaleAssetsDetailByPage(QueryPresaleAssetsDetailPageIn queryPresaleAssetsDetailPageIn);

    OrdDisPresaleAssetsDetail getStoreAssetsDetail(@Param("storeCode") String storeCode, @Param("goodsCode") String goodsCode, @Param("orderCreateTime") LocalDateTime orderCreateTime);

    OrdDisPresaleAssetsDetail getStoreGoodsAssetsDetail(@Param("storeCode") String storeCode, @Param("goodsCode") String goodsCode, @Param("presaleActivityNo") String presaleActivityNo);

    List<OrdDisPresaleAssetsDetailExtOut> findStorePresaleAssetsInfo(@Param("storeCode") String storeCode);

    List<Long> findNeedUpdateStatusAssetsDetailList();

    void updateStatus(@Param("id") Long id, @Param("updateStatus") String updateStatus, @Param("nowStatus") String nowStatus, @Param("loginUsername") String loginUsername);

    List<OrdDisPresaleAssetsDetail> getStoreGoodsAssetsDetailList(@Param("storeCode") String storeCode, @Param("goodsCode") String goodsCode);

    List<OrdDisPresaleAssetsDetail> findStoreGoodsAssetsDetailList(@Param("storeCode") String storeCode);
}
