package com.edc.erp.disfirstorder.controller;

import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDeliveryService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



/**
 * <p>
 * 配销配货单与配销铺货单关联表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-10 16:17:54
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisOrderFirstDelivery")
@Api(value = "ordDisOrderFirstDelivery", tags = "配销配货单与配销铺货单关联表模块")
public class OrdDisOrderFirstDeliveryController {

    private final OrdDisOrderFirstDeliveryService ordDisOrderFirstDeliveryService;
	
}
