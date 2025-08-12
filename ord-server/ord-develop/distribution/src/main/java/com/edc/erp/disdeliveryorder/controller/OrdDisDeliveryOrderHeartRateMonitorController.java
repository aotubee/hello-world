package com.edc.erp.disdeliveryorder.controller;

import com.edc.erp.disdeliveryorder.model.in.UpdateDisDeliveryOrderHeartRateIn;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderHeartRateMonitorService;
import com.edc.plugins.common.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/**
 * <p>
 * 配销单收货心跳检测 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisDeliveryOrderHeartRateMonitor")
@Api(value = "ordDisDeliveryOrderHeartRateMonitor", tags = "配销单收货心跳检测模块")
public class OrdDisDeliveryOrderHeartRateMonitorController {

    private final OrdDisDeliveryOrderHeartRateMonitorService ordDisDeliveryOrderHeartRateMonitorService;

    @ApiOperation(value = "更新收货最近一次心跳时间", notes = "更新收货最近一次心跳时间", httpMethod = "POST")
    @PostMapping("/updateTakeDeliveryHeartRate")
    public Response updateTakeDeliveryHeartRate(@RequestBody @Valid UpdateDisDeliveryOrderHeartRateIn updateDisDeliveryOrderHeartRateIn) {
        ordDisDeliveryOrderHeartRateMonitorService.updateTakeDeliveryHeartRate(updateDisDeliveryOrderHeartRateIn);
        return Response.success();
    }

}
