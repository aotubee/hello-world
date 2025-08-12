package com.edc.erp.directly.dirdifferenceorder.controller;

import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.model.in.ApprovedDirDifferenceOrderIn;
import com.edc.erp.directly.dirdifferenceorder.model.in.DirDifferenceOrderIn;
import com.edc.erp.directly.dirdifferenceorder.model.out.DirDiffOrderSummaryOut;
import com.edc.erp.directly.dirdifferenceorder.model.out.DirDifferenceOrderOut;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.util.StringUtil;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;


/**
 * <p>
 * 差异单 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-14 11:32:38
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDelivDifference")
@Api(value = "ordDirDelivDifference", tags = "差异单模块")
public class OrdDirDelivDifferenceController {

    private final OrdDirDelivDifferenceService ordDirDelivDifferenceService;

    private final StockServer stockServer;

    @ApiOperation(value = "分页获取差异单列表")
    @GetMapping(value = "/findDiffOrderByParam")
    public Response<Page<DirDifferenceOrderOut>> findDiffOrderByParam(DirDifferenceOrderIn differenceOrderIn) {
        differenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(ordDirDelivDifferenceService.findDiffOrderByParam(differenceOrderIn));
    }

    @ApiOperation(value = "直营差异单汇总", notes = "直营差异单汇总")
    @GetMapping(value = "/summary")
    public Response<DirDiffOrderSummaryOut> diffOrderSummary(DirDifferenceOrderIn differenceOrderIn) {
        differenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        DirDiffOrderSummaryOut orderSummaryOut = ordDirDelivDifferenceService.diffOrderSummary(differenceOrderIn);
        return Response.data(orderSummaryOut);
    }

    @ApiOperation(value = "运营端查询直营配货差异单表头", notes = "运营端查询直营配货差异单表头", httpMethod = "GET")
    @GetMapping("/getBackHeaderDifferenceOrderOutById")
    public Response<DirDifferenceOrderOut> getBackHeaderDifferenceOrderOutById(@RequestParam Integer differenceOrderId) {
        if (null == differenceOrderId) {
            return Response.error("直营配货差异单主键不能为空");
        }
        DirDifferenceOrderOut disDifferenceOrderOut = ordDirDelivDifferenceService.getBackHeaderDifferenceOrderOutById(differenceOrderId);
        return Response.data(disDifferenceOrderOut);
    }

    /**
     * 批量批准配货差异单
     *
     * @return
     */
    @ApiOperation(value = "批量批准配货差异单", notes = "批量批准配货差异单")
    @PostMapping("/batchApproved")
    public Response batchApprovedDirDifference(@RequestParam List<Integer> dirDifferenceOrderIds) {
        int approved = ordDirDelivDifferenceService.batchApprovedDirDifference(dirDifferenceOrderIds);
        if (approved > 0) {
            return Response.success("批准成功");
        }
        return Response.error("批准失败");
    }

    /**
     * 批准差异单
     *
     * @return
     */
    @ApiOperation(value = "批准差异单", notes = "批准差异单")
    @PostMapping("/approved")
    public Response approvedDisDifference(@RequestBody @Valid ApprovedDirDifferenceOrderIn approvedDirDifferenceOrderIn) {
        approvedDirDifferenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        int approved = ordDirDelivDifferenceService.approvedDisDifference(approvedDirDifferenceOrderIn);
        if (approved > 0) {
            return Response.success("批准成功");
        }
        return Response.error("批准失败");
    }

    /**
     * 作废差异单
     *
     * @return
     */
    @ApiOperation(value = "作废差异单", notes = "作废差异单")
    @PostMapping("/invalid")
    public Response invalidDirDifference(@RequestBody OrdDirDelivDifference ordDirDelivDifference) {
        OrdDirDelivDifference query = new OrdDirDelivDifference();
        query.setId(ordDirDelivDifference.getId());
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        OrdDirDelivDifference dirDelivDifference = ordDirDelivDifferenceService.selectOne(query);
        if (Objects.isNull(dirDelivDifference)) {
            return Response.error("找不到此差异单");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(dirDelivDifference.getStockCode(), UserUtil.getBizOrgCode(), "作废差异单");
        int invalid = ordDirDelivDifferenceService.invalidDirDifference(dirDelivDifference, stockInfoOut);
        if (invalid > 0) {
            return Response.success("作废成功");
        }
        return Response.error("作废失败");
    }

    /**
     * 冲销差异单
     *
     * @return
     */
    @ApiOperation(value = "冲销差异单", notes = "冲销差异单")
    @PostMapping("/charge")
    public Response chargeDirDifference(@RequestBody ApprovedDirDifferenceOrderIn approvedDirDifferenceOrderIn) {
        approvedDirDifferenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        int charge = ordDirDelivDifferenceService.chargeDirDifference(approvedDirDifferenceOrderIn);
        if (charge > 0) {
            return Response.success("冲销成功");
        }
        return Response.error("冲销失败");
    }

    @ApiOperation(value = "导出差异单列表", notes = "导出差异单列表")
    @PostMapping("/exportDirDifferences")
    public Response<String> exportDirDifferences(@RequestBody DirDifferenceOrderIn differenceOrderIn) {
        differenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        log.info("导出获取差异单列表入参是--{}", differenceOrderIn);
        log.info("开始执行/ord/ordDirDelivDifference/exportDirDifferences");
        String fileUrl = ordDirDelivDifferenceService.exportDirDifferences(differenceOrderIn);
        log.info("结束执行/ord/ordDirDelivDifference/exportDirDifferences");
        log.info("导出获取差异单列表返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    @ApiOperation(value = "查询配货差异单列表（库存盘点）", notes = "查询配货差异单列表（库存盘点）", httpMethod = "GET")
    @GetMapping("/findDirDifferenceOrders")
    public Response<List<DirDifferenceOrderOut>> findDirDifferenceOrders(OrdDirDelivDifference dirDelivDifference) {
        if (StringUtil.isEmpty(dirDelivDifference.getBizOrgCode())) {
            dirDelivDifference.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        List<DirDifferenceOrderOut> dirDifferenceOrderOuts = ordDirDelivDifferenceService.findDirDifferenceOrders(dirDelivDifference);
        return Response.data(dirDifferenceOrderOuts);
    }
}
