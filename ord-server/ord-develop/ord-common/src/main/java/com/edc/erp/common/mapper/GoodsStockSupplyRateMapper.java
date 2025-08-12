package com.edc.erp.common.mapper;

import com.edc.erp.common.model.out.stock.GoodsStockSupplyRateOut;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsStockSupplyRateMapper {

    List<GoodsStockSupplyRateOut> findGoodsStockSupplyRate(@Param("truncationDateTime") String truncationDateTime, @Param("stockCodeList") List<String> stockCodeList);
}
