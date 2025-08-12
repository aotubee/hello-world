package com.edc.erp.directly.dirdifferenceorder.controller;

import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.model.in.OrdDirDelivDifferenceDetailIn;
import com.edc.erp.directly.dirdifferenceorder.model.out.OrdDirDelivDifferenceDetailOut;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceDetailService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * <p>
 * 差异单详细表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-14 11:32:46
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDelivDifferenceDetail")
@Api(value = "ordDirDelivDifferenceDetail", tags = "差异单详细表模块")
public class OrdDirDelivDifferenceDetailController {

    private final OrdDirDelivDifferenceDetailService ordDirDelivDifferenceDetailService;

    @ApiOperation(value = "获取差异单详情")
    @GetMapping(value = "/getDifferenceOrderDtl")
    public Response<Page<OrdDirDelivDifferenceDetailOut>> getDifferenceOrderDtl(OrdDirDelivDifferenceDetailIn dirDifferenceDetailIn) {
        return Response.data(ordDirDelivDifferenceDetailService.findDifferenceOrderDtlListByParameter(dirDifferenceDetailIn));
    }
    @ApiOperation(value = "导出获取配货差异单详情", notes = "导出获取配货差异单详情")
    @PostMapping("/exportDirDifferenceDetails")
    public Response<String> exportDirDifferenceDetails(@RequestBody @Valid OrdDirDelivDifferenceDetailIn dirDifferenceDetailIn) {
        log.info("导出获取直营配货差异单详情入参是--{}", dirDifferenceDetailIn);
        log.info("开始执行/ord/ordDirDelivDifferenceDetail/exportDirDifferenceDetails");
        String fileUrl = ordDirDelivDifferenceDetailService.exportDirDifferenceDetails(dirDifferenceDetailIn);
        log.info("结束执行/ord/ordDirDelivDifferenceDetail/exportDirDifferenceDetails");
        log.info("导出获取直营配货差异单详情返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }
}
