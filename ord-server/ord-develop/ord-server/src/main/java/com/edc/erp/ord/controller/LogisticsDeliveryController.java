package com.edc.erp.ord.controller;

import com.edc.erp.common.model.out.fl.DeliveryNumberVO;
import com.edc.erp.common.service.WmsService;
import com.edc.plugins.common.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-12-16 15:32
 */
@Api(tags = "物流配送控制器")
@RestController
@RequestMapping("/ord/logisticsDelivery")
@Slf4j
public class LogisticsDeliveryController {

    @Autowired
    private WmsService wmsService;

    @ApiOperation(value = "获取物流箱数信息", notes = "获取物流箱数信息", httpMethod = "GET")
    @GetMapping("/getDeliveryNumberByDeliveryNo")
    public Response<DeliveryNumberVO> getDeliveryNumberByDeliveryNo(@RequestParam @NotBlank String deliveryNo) {
        return wmsService.getDeliveryNumberByDeliveryNo(deliveryNo);
    }
}
