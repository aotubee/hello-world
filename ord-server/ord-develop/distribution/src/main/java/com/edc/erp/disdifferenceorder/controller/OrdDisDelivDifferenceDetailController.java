package com.edc.erp.disdifferenceorder.controller;

import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import com.edc.erp.disdifferenceorder.model.in.OrdDisDelivDifferenceDetailIn;
import com.edc.erp.disdifferenceorder.model.out.OrdDisDelivDifferenceDetailOut;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceDetailService;
import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * <p>
 * 配销差异单详细表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-24 11:16:38
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisDelivDifferenceDetail")
@Api(value = "ordDisDelivDifferenceDetail", tags = "配销差异单详细表模块")
public class OrdDisDelivDifferenceDetailController {

    private final OrdDisDelivDifferenceDetailService ordDisDelivDifferenceDetailService;

    @ApiOperation(value = "获取配销差异单详情")
    @GetMapping(value = "/getDifferenceOrderDtl")
    public Response<Page<OrdDisDelivDifferenceDetailOut>> getDifferenceOrderDtl(OrdDisDelivDifferenceDetailIn disDifferenceDetailIn) {
        return Response.data(ordDisDelivDifferenceDetailService.findDifferenceOrderDtlListByParameter(disDifferenceDetailIn));
    }
    @ApiOperation(value = "导出获取配销差异单详情", notes = "导出获取配销差异单详情")
    @PostMapping("/exportDisDifferenceDetails")
    public Response<String> exportDisDifferenceDetails(@RequestBody @Valid OrdDisDelivDifferenceDetailIn disDifferenceDetailIn) {
        log.info("导出获取配销差异单详情入参是--{}", disDifferenceDetailIn);
        log.info("开始执行/ord/ordDisDelivDifferenceDetail/exportDisDifferenceDetails");
        String fileUrl = ordDisDelivDifferenceDetailService.exportDisDifferenceDetails(disDifferenceDetailIn);
        log.info("结束执行/ord/ordDisDelivDifferenceDetail/exportDisDifferenceDetails");
        log.info("导出获取配销差异单详情返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }
}
