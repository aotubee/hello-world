package com.edc.erp.common.mapper;

import com.edc.erp.common.entity.StoreSkuMonthlySales;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreSkuMonthlySalesMapper extends BaseMapper<StoreSkuMonthlySales> {

    /**
     * 根据门店代码和SKU集合获取门店商品月销量
     *
     * @param skuList   sku集合
     * @param storeCode 门店代码
     * @return
     */
    List<StoreSkuMonthlySales> findSkuAverageDailySalesDaysListByStoreCode(@Param("skuList") List<String> skuList, @Param("storeCode") String storeCode,
                                                                           @Param("bizOrgCode") String bizOrgCode);
}
