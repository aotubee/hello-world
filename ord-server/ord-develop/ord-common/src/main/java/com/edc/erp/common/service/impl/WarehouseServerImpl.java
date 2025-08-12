package com.edc.erp.common.service.impl;

import com.edc.erp.common.mapper.WarehouseMapper;
import com.edc.erp.common.model.out.stock.StockWarehouseOut;
import com.edc.erp.common.model.out.warehouse.WarehouseInfoOut;
import com.edc.erp.common.rpc.WarehouseClient;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @return: 仓储业务实现
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Service
@Slf4j
public class WarehouseServerImpl implements WarehouseServer {
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private WarehouseClient warehouseClient;

    @Override
    public WarehouseInfoOut getByCode(String warehouseCode, String bizOrgCode) {

        Response<WarehouseInfoOut> response = warehouseClient.getByCode(warehouseCode, bizOrgCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

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
    @Override
    public BigDecimal getWarehousePrice(String wrhCode, String stockCode, String goodsCode, String bizOrgCode, String vendorCode) {
        BigDecimal warehousePrice = warehouseMapper.getWarehousePrice(wrhCode, stockCode, goodsCode, bizOrgCode);
        if (Objects.isNull(warehousePrice)) {
            // 采购协议价
            warehousePrice = warehouseMapper.getPurchasePriceByGoodsAndVendorCode(goodsCode, vendorCode, bizOrgCode, LocalDate.now());
        }
        return Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice;
    }

    @Override
    public BigDecimal getStockPriceForBusiness(String wrhCode, String stockCode, String goodsCode, String centerBizOrgCode, String storeCode, String storeChannelBizOrgCode) {
        BigDecimal warehousePrice = warehouseMapper.getWarehousePrice(wrhCode, stockCode, goodsCode, centerBizOrgCode);
        if (Objects.isNull(warehousePrice)) {
            warehousePrice = warehouseMapper.getStockPrice(storeCode, goodsCode, storeChannelBizOrgCode);
        }
        return warehousePrice;
    }

    /**
     * 门店库存价
     *
     * @param storeCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public BigDecimal getStockPrice(String storeCode, String goodsCode, String bizOrgCode) {
        return warehouseMapper.getStockPrice(storeCode, goodsCode, bizOrgCode);
    }

    /**
     * 校验仓位代码是否在该仓储下
     *
     * @param warehouseCode
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public boolean checkStockIsWarehouseExist(String warehouseCode, String stockCode, String bizOrgCode) {
        Integer i = warehouseMapper.checkStockIsWarehouseExist(warehouseCode, stockCode, bizOrgCode);
        return null != i;
    }

    /**
     * 门店可用库存
     *
     * @param storeCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public BigDecimal getStockNum(String storeCode, String goodsCode, String bizOrgCode) {
        return warehouseMapper.getStockNum(storeCode, goodsCode, bizOrgCode);
    }

    /**
     * 获取仓储可用库存
     *
     * @param wrhCode
     * @param stockCode
     * @param goodsCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public BigDecimal getWarehouseNum(String wrhCode, String stockCode, String goodsCode, String bizOrgCode) {
        return warehouseMapper.getWarehouseNum(wrhCode, stockCode, goodsCode, bizOrgCode);
    }

    @Override
    public Map<String, StockWarehouseOut> findStockInv(String stockCode, List<String> goodsCodeList, String bizOrgCode) {
        List<StockWarehouseOut> warehouseInfoList = warehouseMapper.findStockInv(stockCode, goodsCodeList, bizOrgCode);
        if (CollectionUtils.isEmpty(warehouseInfoList)) {
            return new HashMap<>();
        }
        return warehouseInfoList.stream().collect(Collectors.toMap(StockWarehouseOut::getGoodsCode, Function.identity()));
    }

    @Override
    public WarehouseInfoOut getWarehouseInfoByCode(String warehouseCode) {
        Response<WarehouseInfoOut> response = warehouseClient.getByCodeOrId(warehouseCode);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }
}
