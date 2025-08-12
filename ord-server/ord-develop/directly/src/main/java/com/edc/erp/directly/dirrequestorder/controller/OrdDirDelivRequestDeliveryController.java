package com.edc.erp.directly.dirrequestorder.controller;

import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDelivery;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestDeliveryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.springframework.web.bind.annotation.*;



/**
 * <p>
 * 要货单与配货单关联表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDelivRequestDelivery")
@Api(value = "ordDirDelivRequestDelivery", tags = "要货单与配货单关联表模块")
public class OrdDirDelivRequestDeliveryController {

    private final OrdDirDelivRequestDeliveryService ordDirDelivRequestDeliveryService;
	
}
