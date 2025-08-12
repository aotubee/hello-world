package com.edc.erp.common.rpc;


import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.model.out.ordertypeconfig.OptOrderTypeConfigOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;

@FeignClient(name ="mdm",contextId ="orderTypeClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface OrderTypeClient {

    @ApiOperation(value = "根据ID查询订单类型配置信息")
    @GetMapping("/opt/orderTypeConfig/getById")
    Response<OrderTypeConfig> getById(@ApiParam(name = "id",value = "订单类型ID") @NotNull(message = "订单类型ID不能为空") @RequestParam("id") Integer id);
}
