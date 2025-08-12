package com.edc.erp.common.service;

import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.stock.WarehouseInfoOut;

import java.util.Map;

/**
 * 仓位信息
 *
 * @author weichao
 */
public interface StockServer {

    /**
     * 获取仓位信息
     *
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    StockInfoOut getByCode(String stockCode, String bizOrgCode);

    /**
     * 获取仓储信息
     *
     * @param warehouseCode
     * @param bizOrgCode
     * @return
     */
    WarehouseInfoOut getWarehouseInfoByCode(String warehouseCode, String bizOrgCode);

    /**
     * 获取退货仓位状态
     * @param type
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    boolean getReturnStockStatus(String type, String stockCode, String bizOrgCode);

    /**
     * 是否发送物流
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    boolean isSendWms(String stockCode, String bizOrgCode);

    Map<String, StockInfoOut> findAll(String bizOrgCode);

    /**
     * 是否操作大库
     **/
    boolean isAbutmentWms(String stockCode, String bizOrgCode);

    void checkIsCanAbutmentWms(String stockCode, String bizOrgCode);

    Map<String, StockInfoOut> findByAuthOrg(String bizOrgCode);

    StockInfoOut getAndCheckStockInfo(String stockCode, String loginBizOrgCode, String businessOperationContent);

    StockInfoOut getTransInfo(String stockCode);

    StockInfoOut getByCodeAndAuth(String stockCode, String bizOrgCode);
}