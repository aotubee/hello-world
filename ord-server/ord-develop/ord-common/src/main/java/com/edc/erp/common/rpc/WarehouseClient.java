package com.edc.erp.common.rpc;

import com.edc.erp.common.model.out.warehouse.WarehouseInfoOut;

import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 获取仓储信息
 * @author
 */

@FeignClient(name ="mdm",contextId ="WarehouseClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface WarehouseClient {

    /**
     * 根据组织和code获取仓储信息
     *
     * @param warehouseCode
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "根据组织和code获取仓储信息", httpMethod = "GET")
    @GetMapping("/logc/warehouse/getByCode")
    Response<WarehouseInfoOut> getByCode(@RequestParam String warehouseCode, @RequestParam String bizOrgCode);


    @ApiOperation(value = "根据code获取所属组织", httpMethod = "GET")
    @GetMapping("/logc/warehouse/getByCodeOrId")
    Response<WarehouseInfoOut> getByCodeOrId(@RequestParam String warehouseCode);

}
