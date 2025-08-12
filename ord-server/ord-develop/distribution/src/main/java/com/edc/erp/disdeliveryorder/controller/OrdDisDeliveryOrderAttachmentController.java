package com.edc.erp.disdeliveryorder.controller;

import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderAttachmentService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * <p>
 * 配销单签收附件表 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisDeliveryOrderAttachment")
@Api(value = "ordDisDeliveryOrderAttachment", tags = "配销单签收附件表模块")
public class OrdDisDeliveryOrderAttachmentController {

    private final OrdDisDeliveryOrderAttachmentService ordDisDeliveryOrderAttachmentService;
	
}
