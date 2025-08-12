package com.edc.erp.presale.mapper;

import com.edc.erp.presale.entity.OrdDisPresaleOrderDetail;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrdDisPresaleOrderDetailMapper extends BaseMapper<OrdDisPresaleOrderDetail> {

    int batchSaveDisPresaleOrderDetailList(@Param("orderDetailList") List<OrdDisPresaleOrderDetail> orderDetailList);

    List<OrdDisPresaleOrderDetail> findListByOrderId(@Param("id") Long id);

    BigDecimal sumBasePackageQuantityByActivityIdAndGoodsCode(@Param("presaleActivityId") Long presaleActiviryId, @Param("goodsCode") String goodsCode, @Param("storeCode") String storeCode);
}
