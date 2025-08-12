package com.edc.erp.common.mapper;

import com.edc.erp.common.model.out.stock.StockInfoOut;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author
 * @since 2022-08-30 17:20:59
 */
@Repository
public interface StockMapper {


    /**
     * 查询仓位此业务开关
     * @param stockCode
     * @param bizOrgCode
     * @param column
     * @return
     */
    Integer getStockBusinessSwitchStatus(@Param("stockCode") String stockCode, @Param("bizOrgCode") String bizOrgCode,
                                         @Param("column") String column);

    StockInfoOut getTransInfo(@Param("stockCode") String stockCode);

}
