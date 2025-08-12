package com.edc.erp.disrequestorder.controller;

import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDelivery;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestDeliveryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.springframework.web.bind.annotation.*;



/**
 * <p>
 * 集货单与配销单关联表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisDelivRequestDelivery")
@Api(value = "ordDisDelivRequestDelivery", tags = "集货单与配销单关联表模块")
public class OrdDisDelivRequestDeliveryController {

    private final OrdDisDelivRequestDeliveryService ordDisDelivRequestDeliveryService;
	
}
