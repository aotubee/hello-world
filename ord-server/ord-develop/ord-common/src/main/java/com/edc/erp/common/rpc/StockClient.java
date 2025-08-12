package com.edc.erp.common.rpc;

import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.stock.WarehouseInfoOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 获取仓位信息
 * @author weichao
 */
@FeignClient(name = "mdm", contextId = "StockClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface StockClient {
    /**
     *根据组织和code获取仓位信息
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据组织和code获取仓位信息", httpMethod = "GET")
    @GetMapping("/logc/stock/getByCode")
    Response<StockInfoOut> getByCode(@RequestParam("stockCode") String stockCode, @RequestParam("bizOrgCode") String bizOrgCode);

    /**
     * 根据组织和code获取仓储信息
     *
     * @param warehouseCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据组织和code获取仓储信息", httpMethod = "GET")
    @GetMapping("/logc/warehouse/getByCode")
    Response<WarehouseInfoOut> getWarehouseInfoByCode(@RequestParam("warehouseCode") String warehouseCode, @RequestParam("bizOrgCode") String bizOrgCode);

    @ApiOperation(value = "根据组织获取仓位信息", httpMethod = "GET")
    @GetMapping("/logc/stock/findByBizOrgCode")
    Response<List<StockInfoOut>> findByBizOrgCode(@RequestParam("bizOrgCode") String bizOrgCode);

    @ApiOperation(value = "查询仓位业务是否操作ERP仓储", httpMethod = "GET")
    @GetMapping("/logc/stock/isAbutmentWrh")
    Response<Integer> isAbutmentWrh(@RequestParam("stockCode") String stockCode, @RequestParam("bizOrgCode") String bizOrgCode);

    @ApiOperation(value = "查询组织所属与可配仓位", httpMethod = "GET")
    @GetMapping("/logc/stock/findByAuthOrg")
    Response<List<StockInfoOut>> findByAuthOrg(@RequestParam("bizOrgCode") String bizOrgCode);

    @ApiOperation(value = "查询组织所属与可配仓位,仓储信息", httpMethod = "GET")
    @GetMapping("/logc/stock/findWrhAndStockTransByCodes")
    Response<List<StockInfoOut>> findWrhAndStockTransByCodes(@RequestParam("bizOrgCode") String bizOrgCode);

    @ApiOperation(value = "查询指定仓位和组织所属与可配仓位,仓储信息", httpMethod = "GET")
    @GetMapping("/logc/stock/getByCodeAndAuth")
    Response<StockInfoOut> getByCodeAndAuth(@RequestParam("stockCode") String stockCode, @RequestParam("bizOrgCode") String bizOrgCode);

}
