package com.edc.erp.directly.dirfirstorder.controller;

import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDelivery;
import com.edc.erp.directly.dirfirstorder.service.OrdDirOrderFirstDeliveryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.springframework.web.bind.annotation.*;



/**
 * <p>
 * 配货单与铺货单关联表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-10 14:09:00
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirOrderFirstDelivery")
@Api(value = "ordDirOrderFirstDelivery", tags = "配货单与铺货单关联表模块")
public class OrdDirOrderFirstDeliveryController {

    private final OrdDirOrderFirstDeliveryService ordDirOrderFirstDeliveryService;
	
}
