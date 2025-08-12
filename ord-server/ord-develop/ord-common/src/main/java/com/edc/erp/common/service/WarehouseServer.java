package com.edc.erp.common.service;

import com.edc.erp.common.model.out.stock.StockWarehouseOut;
import com.edc.erp.common.model.out.warehouse.WarehouseInfoOut;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 仓位信息
 *
 * @author weichao
 */
public interface WarehouseServer {

    /**
     * 查询仓储信息
     *
     * @param warehouseCode
     * @param bizOrgCode
     * @return
     */
    WarehouseInfoOut getByCode(String warehouseCode, String bizOrgCode);


    /**
     * 获取仓储库存价
     *
     * @param wrhCode
     * @param stockCode
     * @param goodsCode
     * @param bizOrgCode
     * @param vendorCode
     * @return
     */
    BigDecimal getWarehousePrice(String wrhCode, String stockCode, String goodsCode, String bizOrgCode, String vendorCode);

    BigDecimal getStockPriceForBusiness(String wrhCode, String stockCode, String goodsCode, String centerBizOrgCode, String storeCode, String storeChannelBizOrgCode);

    /**
     *门店库存价
     * @param storeCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getStockPrice(String storeCode, String goodsCode, String bizOrgCode);

    /**
     * 校验仓位代码是否在该仓储下
     * @param warehouseCode
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    boolean checkStockIsWarehouseExist(String warehouseCode, String stockCode, String bizOrgCode);


    /**
     *门店可用库存
     * @param storeCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getStockNum(String storeCode, String goodsCode, String bizOrgCode);

    /**
     * 获取仓储可用库存
     * @param wrhCode
     * @param stockCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    BigDecimal getWarehouseNum(String wrhCode, String stockCode, String goodsCode, String bizOrgCode);

    Map<String, StockWarehouseOut> findStockInv(String stockCode, List<String> goodsCodeList, String bizOrgCode);

    WarehouseInfoOut getWarehouseInfoByCode(String warehouseCode);
}
