package com.edc.erp.directly.dirdeliveryorder.controller;

import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateDeliveryOrderHeartRateIn;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderHeartRateMonitorService;
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
 * 配货单收货心跳检测 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDeliveryOrderHeartRateMonitor")
@Api(value = "ordDirDeliveryOrderHeartRateMonitor", tags = "配货单收货心跳检测模块")
public class OrdDirDeliveryOrderHeartRateMonitorController {

    private final OrdDirDeliveryOrderHeartRateMonitorService ordDirDeliveryOrderHeartRateMonitorService;


    @ApiOperation(value = "更新收货最近一次心跳时间", notes = "更新收货最近一次心跳时间", httpMethod = "POST")
    @PostMapping("/updateTakeDeliveryHeartRate")
    public Response updateTakeDeliveryHeartRate(@RequestBody @Valid UpdateDeliveryOrderHeartRateIn updateDeliveryOrderHeartRateIn) {
        ordDirDeliveryOrderHeartRateMonitorService.updateTakeDeliveryHeartRate(updateDeliveryOrderHeartRateIn);
        return Response.success();
    }

}
