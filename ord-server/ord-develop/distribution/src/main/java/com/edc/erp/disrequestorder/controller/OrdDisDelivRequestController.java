package com.edc.erp.disrequestorder.controller;

import com.edc.erp.disrequestorder.model.in.BackQueryRequestOrderPageIn;
import com.edc.erp.disrequestorder.model.out.BackRequestOrderOut;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


/**
 * <p>
 * 集货单 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-20 14:54:49
 */
@Slf4j
@RestController
@RequestMapping("/ord/ordDisDelivRequest")
@Api(value = "ordDisDelivRequest", tags = "集货单模块")
public class OrdDisDelivRequestController {

    @Autowired
    @Qualifier("ordDisDelivRequestServiceImpl")
    private OrdDisDelivRequestService ordDisDelivRequestService;


    @ApiOperation(value = "运营端分页查询集货单列表", httpMethod = "POST")
    @PostMapping("/findRequestOrderListForPage")
    public Response<Page<BackRequestOrderOut>> findRequestOrderListForPage(@RequestBody BackQueryRequestOrderPageIn backQueryRequestOrderPageIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        backQueryRequestOrderPageIn.setBizOrgCode(bizOrgCode);
        Page<BackRequestOrderOut> requestOrderPage = ordDisDelivRequestService.findRequestOrderListForPage(backQueryRequestOrderPageIn);
        return Response.data(requestOrderPage);
    }

    /**
     * 集货单汇总
     *
     * @param backQueryRequestOrderPageIn
     * @return
     */
    @ApiOperation(value = "集货单汇总")
    @GetMapping(value = "/summary")
    public Response<BigDecimal> requestOrderSummary(BackQueryRequestOrderPageIn backQueryRequestOrderPageIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        backQueryRequestOrderPageIn.setBizOrgCode(bizOrgCode);
        return Response.data(ordDisDelivRequestService.requestOrderSummary(backQueryRequestOrderPageIn));
    }

    @ApiOperation(value = "查询集货单明细表头")
    @GetMapping("/getRequestOrderById")
    public Response<BackRequestOrderOut> getRequestOrderById(@RequestParam @NotNull Long requestOrderId) {
        return Response.data(ordDisDelivRequestService.getRequestOrderById(requestOrderId));
    }

    @ApiOperation(value = "运营端导出集货单列表", httpMethod = "POST")
    @PostMapping("/export")
    public Response<String> export(@RequestBody BackQueryRequestOrderPageIn backQueryRequestOrderPageIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        backQueryRequestOrderPageIn.setBizOrgCode(bizOrgCode);
        backQueryRequestOrderPageIn.setPageSize(0);
        backQueryRequestOrderPageIn.setPageNum(0);
        boolean errorFlag = StringUtils.isBlank(backQueryRequestOrderPageIn.getBeginTruncationTime()) && StringUtils.isBlank(backQueryRequestOrderPageIn.getEndTruncationTime());
        if (errorFlag) {
            return Response.error("导出集货单列表必须传提交时间段或截单日期时间段");
        }
        log.info("开始执行/ord/ordDisDelivRequest/export");
        String privateUrl = ordDisDelivRequestService.export(backQueryRequestOrderPageIn);
        log.info("结束执行/ord/ordDisDelivRequest/export");
        return Response.data(privateUrl);
    }
}
