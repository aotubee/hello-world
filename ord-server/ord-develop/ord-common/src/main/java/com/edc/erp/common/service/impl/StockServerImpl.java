package com.edc.erp.common.service.impl;

import com.edc.erp.common.enumeration.OrdStockBusinessSwitchEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.StoreConstant;
import com.edc.erp.common.mapper.StockMapper;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.stock.WarehouseInfoOut;
import com.edc.erp.common.rpc.StockClient;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.plugins.cache.CacheConst;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 仓位信息实现类
 *
 * @author weichao
 */
@Service
@Slf4j
public class StockServerImpl implements StockServer {

    @Autowired
    private StockClient stockClient;
    @Autowired
    private StockMapper stockMapper;

    /**
     * 获取仓位信息
     *
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public StockInfoOut getByCode(String stockCode, String bizOrgCode) {
        Response<StockInfoOut> response = stockClient.getByCode(stockCode, bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 获取仓储信息
     *
     * @param warehouseCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public WarehouseInfoOut getWarehouseInfoByCode(String warehouseCode, String bizOrgCode) {
        Response<WarehouseInfoOut> response = stockClient.getWarehouseInfoByCode(warehouseCode, bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    /**
     * 获取退货仓位状态
     *
     * @param type
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public boolean getReturnStockStatus(String type, String stockCode, String bizOrgCode) {
        String column = "";
        if (StoreConstant.StoreProperty.DIRECTLY.getMytValue().equals(type)) {
            column = OrdStockBusinessSwitchEnum.DIR_RETURN.getColumn();
        }
        if (StoreConstant.StoreProperty.FRANCHISE.getMytValue().equals(type)) {
            column = OrdStockBusinessSwitchEnum.DIS_RETURN.getColumn();
        }
        Integer status = stockMapper.getStockBusinessSwitchStatus(stockCode, bizOrgCode, column);
        return NumberUtil.INTEGER_ONE.equals(status);
    }

    @Override
    public boolean isSendWms(String stockCode, String bizOrgCode) {
        Integer status = stockMapper.getStockBusinessSwitchStatus(stockCode, bizOrgCode, "is_send_wms_logc");
        return Objects.isNull(status) || NumberUtil.INTEGER_ONE.equals(status);
    }

    @Override
    public Map<String, StockInfoOut> findAll(String bizOrgCode) {
        Response<List<StockInfoOut>> response = stockClient.findByBizOrgCode(bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData().stream().collect(Collectors.toMap(StockInfoOut::getStockCode, v -> v, (v1, v2) -> v1));
    }

    @Override
    public boolean isAbutmentWms(String stockCode, String bizOrgCode) {
        Integer status = stockMapper.getStockBusinessSwitchStatus(stockCode, bizOrgCode, "is_abutment_wrh_logc");
        return Objects.isNull(status) || NumberUtil.INTEGER_ONE.equals(status);
    }

    @Override
    public void checkIsCanAbutmentWms(String stockCode, String bizOrgCode) {
        boolean isAbutmentWms = this.isAbutmentWms(stockCode, bizOrgCode);
        if (!isAbutmentWms) {
            String message = "组织" + OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode) + "仓位" + stockCode + "不允许对接ERP仓储";
            log.error(message);
            throw new BusinessException(message);
        }
    }

    @Override
    public Map<String, StockInfoOut> findByAuthOrg(String bizOrgCode) {
        Response<List<StockInfoOut>> response = stockClient.findByAuthOrg(bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return new HashMap<>();
        }
        return response.getData().stream().collect(Collectors.toMap(StockInfoOut::getStockCode, v -> v, (v1, v2) -> v1));
    }

    @Override
    public StockInfoOut getAndCheckStockInfo(String stockCode, String loginBizOrgCode, String businessOperationContent) {
        // 获取当前登录人所属+可配仓位
        Map<String, StockInfoOut> loginStockInfoOutMap = this.findByAuthOrg(loginBizOrgCode);
        StockInfoOut stockInfoOut = loginStockInfoOutMap.get(stockCode);
        if (Objects.isNull(stockInfoOut)) {
            throw new RuntimeException("该仓位" + stockCode + "下，" + businessOperationContent + "未被授权");
        }
        return stockInfoOut;
    }

    @Override
    @Cacheable(cacheManager = CacheConst.REDIS_CACHE_MANAGER, cacheNames = "trans_info_stock",
            key = "'stockCode:' + #stockCode", unless = "#result == null")
    public StockInfoOut getTransInfo(String stockCode) {
        return stockMapper.getTransInfo(stockCode);
    }

    @Override
    public StockInfoOut getByCodeAndAuth(String stockCode,String bizOrgCode) {
        Response<StockInfoOut> response = stockClient.getByCodeAndAuth(stockCode,bizOrgCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }
}
