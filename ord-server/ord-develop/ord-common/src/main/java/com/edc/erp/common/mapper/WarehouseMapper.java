package com.edc.erp.common.mapper;

import com.edc.erp.common.model.out.stock.StockWarehouseOut;
import com.edc.erp.common.model.out.warehouse.WarehouseInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author
 * @since 2022-08-30 17:20:59
 */
@Repository
public interface WarehouseMapper  {


    /**
     * 查仓储库存价
     * @param wrhCode
     * @param stockCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getWarehousePrice(@Param("wrhCode") String wrhCode,@Param("stockCode") String stockCode,
                                @Param("goodsCode") String goodsCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查门店库存价
     * @param storeCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getStockPrice(@Param("storeCode") String storeCode,
                             @Param("goodsCode") String goodsCode,
                             @Param("bizOrgCode") String bizOrgCode);

    /**
     * 校验仓位代码是否在该仓储下
     * @param warehouseCode
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    Integer checkStockIsWarehouseExist(@Param("warehouseCode") String warehouseCode, @Param("stockCode") String stockCode,@Param("bizOrgCode") String bizOrgCode);

    /**
     * 获取门店业务可用库存
     * @param storeCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getStockNum(@Param("storeCode") String storeCode,
                           @Param("goodsCode") String goodsCode,
                           @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查仓储可用库存
     * @param wrhCode
     * @param stockCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getWarehouseNum(@Param("wrhCode") String wrhCode,@Param("stockCode") String stockCode,
                               @Param("goodsCode") String goodsCode, @Param("bizOrgCode") String bizOrgCode);

    /**
     * 查询商品指定订单方的采购协议价
     * @param goodsCode
     * @param vendorCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getPurchasePriceByGoodsAndVendorCode(@Param("goodsCode") String goodsCode, @Param("vendorCode") String vendorCode,
                                                    @Param("bizOrgCode") String bizOrgCode, @Param("day") LocalDate day);

    List<StockWarehouseOut> findStockInv(@Param("stockCode") String stockCode, @Param("goodsCodeList") List<String> goodsCodeList, @Param("bizOrgCode") String bizOrgCode);
}
