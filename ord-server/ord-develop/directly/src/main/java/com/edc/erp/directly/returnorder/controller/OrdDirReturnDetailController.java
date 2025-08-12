package com.edc.erp.directly.returnorder.controller;

import com.edc.erp.directly.returnorder.entity.OrdDirReturnImage;
import com.edc.erp.directly.returnorder.model.in.OrdDirReturnDetailIn;
import com.edc.erp.directly.returnorder.service.OrdDirReturnDetailService;
import com.edc.erp.directly.returnorder.service.OrderDirReturnImageService;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>
 * 退货单详情表 前端控制器
 * </p>
 *
 * @author
 * @since 2022-11-18 18:50:07
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirReturnDetail")
@Api(value = "ordDirReturnDetail", tags = "直营退货单详情表模块")
public class OrdDirReturnDetailController {

    private final OrdDirReturnDetailService ordDirReturnDetailService;

    private final OrderDirReturnImageService orderDirReturnImageService;


    @ApiOperation(value = "导出退货单明细", notes = "导出退货单", httpMethod = "GET")
    @GetMapping("/export")
    public Response<String> exportOrdReturn(OrdDirReturnDetailIn pageIn){
        pageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        pageIn.setPageNum(0);
        pageIn.setPageSize(0);
        log.info("开始执行“/ord/dirReturnDetail/export");
        String export = ordDirReturnDetailService.export(pageIn);
        log.info("结束执行“/ord/dirReturnDetail/export");
        return Response.data(export,"导出成功");
    }

    @ApiOperation(value = "查询退货单下所有上传图片", notes = "查询退货单下所有上传图片", httpMethod = "GET")
    @GetMapping("/findAllReturnImage")
    public Response<List<OrdDirReturnImage>> findAllReturnImage(@RequestParam Integer id){
        return Response.data(orderDirReturnImageService.findAllReturnImage(id));
    }
}
