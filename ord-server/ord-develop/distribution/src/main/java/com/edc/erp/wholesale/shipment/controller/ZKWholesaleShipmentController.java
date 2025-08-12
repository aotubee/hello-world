package com.edc.erp.wholesale.shipment.controller;

import com.edc.erp.wholesale.job.PushPurWholesaleShipmentScheduler;
import com.edc.plugins.common.response.Response;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName ZKWholesaleShipmentController
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/15 15:16
 **/
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/ord/zkWholesaleShipment")
public class ZKWholesaleShipmentController {

    private final PushPurWholesaleShipmentScheduler pushPurWholesaleShipmentScheduler;

//    @ApiOperation(value = "西安批发中转推送采购", notes = "西安批发中转推送采购")
//    @GetMapping("/handleXAPushShipmentDetailToPur")
//    public Response<String> handleXAPushShipmentDetailToPur() {
//        pushPurWholesaleShipmentScheduler.xaPushShipmentDetailToPur();
//        return Response.success();
//    }
//
//    @ApiOperation(value = "天岁批发中转推送采购", notes = "天岁批发中转推送采购")
//    @GetMapping("/handleTSPushShipmentDetailToPur")
//    public Response<String> handleTSShipmentDetailToPur() {
//        pushPurWholesaleShipmentScheduler.tsShipmentDetailToPur();
//        return Response.success();
//    }
//
//    @ApiOperation(value = "郑州批发中转推送采购", notes = "郑州批发中转推送采购")
//    @GetMapping("/handleZZShipmentDetailToPur")
//    public Response<String> handleZZShipmentDetailToPur() {
//        pushPurWholesaleShipmentScheduler.zzPushShipmentDetailToPur();
//        return Response.success();
//    }
}
