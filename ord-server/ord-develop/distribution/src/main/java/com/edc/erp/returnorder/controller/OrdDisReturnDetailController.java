package com.edc.erp.returnorder.controller;

import com.edc.erp.returnorder.entity.OrdDisReturnImage;
import com.edc.erp.returnorder.model.in.OrdDisReturnDetailIn;
import com.edc.erp.returnorder.service.OrdDisReturnDetailService;
import com.edc.erp.returnorder.service.OrderDisReturnImageService;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * <p>
 * 退货单详情表 前端控制器
 * </p>
 *
 * @author yaojinpeng
 * @since 2022-10-21 18:26:22
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disReturnDetail")
@Api(value = "disReturnDetail", tags = "配销退货单详情表模块")
public class OrdDisReturnDetailController {

    private final OrdDisReturnDetailService ordDisReturnDetailService;

    private final OrderDisReturnImageService orderDisReturnImageService;

    @ApiOperation(value = "导出退货单明细", notes = "导出退货单", httpMethod = "GET")
    @GetMapping("/export")
    public Response<String> exportOrdReturn(OrdDisReturnDetailIn pageIn){
        pageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        pageIn.setPageNum(0);
        pageIn.setPageSize(0);
        log.info("开始执行“/ord/disReturnDetail/export");
        String export = ordDisReturnDetailService.export(pageIn);
        log.info("结束执行“/ord/disReturnDetail/export");
        return Response.data(export,"导出成功");
    }


    @ApiOperation(value = "查询退货单下所有上传图片", notes = "查询退货单下所有上传图片", httpMethod = "GET")
    @GetMapping("/findAllReturnImage")
    public Response<List<OrdDisReturnImage>> findAllReturnImage(@RequestParam Integer id){
        return Response.data(orderDisReturnImageService.findAllReturnImage(id));
    }

}
