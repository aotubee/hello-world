package com.edc.erp.presale.controller;

import com.edc.erp.presale.model.in.PresaleGoodsFlowPageIn;
import com.edc.erp.presale.model.out.PresaleGoodsFlowPageOut;
import com.edc.erp.presale.service.OrdDisPresaleGoodsFlowService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disPresaleFlow")
@Api(value = "ordDisPresaleFlowController", tags = "预售流水接口")
public class OrdDisPresaleFlowController {
    private final OrdDisPresaleGoodsFlowService presaleGoodsFlowService;

    /**
     * 预售商品流水列表分页查询(运营端\APP)
     */
    @ApiOperation(value = "预售商品流水列表分页查询", notes = "预售商品流水列表分页查询", httpMethod = "POST")
    @PostMapping("/pageGoodsFlow")
    public Response<Page<PresaleGoodsFlowPageOut>> findPresaleGoodsFlowByPage(@RequestBody PresaleGoodsFlowPageIn presaleGoodsFlowPageIn) {
        return Response.data(presaleGoodsFlowService.findPresaleGoodsFlowByPage(presaleGoodsFlowPageIn));
    }

    /**
     * 导出预售商品流水列表
     */
    @ApiOperation(value = "导出预售商品流水列表", notes = "导出预售商品流水列表")
    @PostMapping("/exportGoodsFlowList")
    public Response<String> exportPresaleGoodsFlowList(@RequestBody PresaleGoodsFlowPageIn presaleGoodsFlowPageIn) {
        return Response.data(presaleGoodsFlowService.exportPresaleGoodsFlowList(presaleGoodsFlowPageIn));
    }
}
