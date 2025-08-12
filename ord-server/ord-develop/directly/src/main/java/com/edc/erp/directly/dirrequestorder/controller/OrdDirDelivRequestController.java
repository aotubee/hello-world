package com.edc.erp.directly.dirrequestorder.controller;

import com.edc.erp.directly.dirrequestorder.model.in.DirRequestOrderPageIn;
import com.edc.erp.directly.dirrequestorder.model.out.DirRequestOrderOut;
import com.edc.erp.directly.handle.DirRequestOrderHandle;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


/**
 * <p>
 * 要货单 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDelivRequest")
@Api(value = "ordDirDelivRequest", tags = "要货单模块")
public class OrdDirDelivRequestController {

    private final DirRequestOrderHandle requestOrderHandle;

    @ApiOperation(value = "运营端分页查询要货单列表", httpMethod = "POST")
    @PostMapping("/findRequestOrderListForPage")
    public Response<Page<DirRequestOrderOut>> findRequestOrderListForPage(@RequestBody DirRequestOrderPageIn dirRequestOrderPageIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        dirRequestOrderPageIn.setBizOrgCode(bizOrgCode);
        Page<DirRequestOrderOut> requestOrderPage = requestOrderHandle.findRequestOrderListForPage(dirRequestOrderPageIn);
        return Response.data(requestOrderPage);
    }

    /**
     * 要货单汇总
     *
     * @param dirRequestOrderPageIn
     * @return
     */
    @ApiOperation(value = "要货单汇总")
    @GetMapping(value = "/summary")
    public Response<BigDecimal> requestOrderSummary(DirRequestOrderPageIn dirRequestOrderPageIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        dirRequestOrderPageIn.setBizOrgCode(bizOrgCode);
        return Response.data(requestOrderHandle.requestOrderSummary(dirRequestOrderPageIn));
    }

    @ApiOperation(value = "查询要货单明细表头")
    @GetMapping("/getRequestOrderById")
    public Response<DirRequestOrderOut> getRequestOrderById(@RequestParam @NotNull Long requestOrderId) {
        return Response.data(requestOrderHandle.getRequestOrderById(requestOrderId));
    }

    @ApiOperation(value = "运营端导出要货单列表", httpMethod = "POST")
    @PostMapping("/export")
    public Response<String> export(@RequestBody DirRequestOrderPageIn dirRequestOrderPageIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        dirRequestOrderPageIn.setBizOrgCode(bizOrgCode);
        dirRequestOrderPageIn.setPageSize(0);
        dirRequestOrderPageIn.setPageNum(0);
        boolean errorFlag = StringUtils.isBlank(dirRequestOrderPageIn.getBeginTruncationTime()) && StringUtils.isBlank(dirRequestOrderPageIn.getEndTruncationTime());
        if (errorFlag) {
            return Response.error("导出要货单列表必须传提交时间段或截单日期时间段");
        }
        log.info("开始执行/ord/ordDirDelivRequest/export");
        String privateUrl = requestOrderHandle.export(dirRequestOrderPageIn);
        log.info("结束执行/ord/ordDirDelivRequest/export");
        return Response.data(privateUrl);
    }
}
