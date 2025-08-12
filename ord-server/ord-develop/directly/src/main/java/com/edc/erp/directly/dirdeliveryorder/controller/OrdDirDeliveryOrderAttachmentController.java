package com.edc.erp.directly.dirdeliveryorder.controller;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderAttachment;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderAttachmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.springframework.web.bind.annotation.*;



/**
 * <p>
 * 配货单签收附件表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-22 10:46:50
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDeliveryOrderAttachment")
@Api(value = "ordDirDeliveryOrderAttachment", tags = "配货单签收附件表模块")
public class OrdDirDeliveryOrderAttachmentController {

    private final OrdDirDeliveryOrderAttachmentService ordDirDeliveryOrderAttachmentService;
	
}
