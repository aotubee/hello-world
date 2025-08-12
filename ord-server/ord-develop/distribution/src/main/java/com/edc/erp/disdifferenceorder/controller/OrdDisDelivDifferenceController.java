package com.edc.erp.disdifferenceorder.controller;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.model.in.ApprovedDisDifferenceOrderIn;
import com.edc.erp.disdifferenceorder.model.in.DisDifferenceOrderIn;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.disdifferenceorder.model.out.DisDiffOrderSummaryOut;
import com.edc.erp.disdifferenceorder.model.out.DisDifferenceOrderOut;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceService;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
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
 * 配销差异单 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-24 11:16:29
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDisDelivDifference")
@Api(value = "ordDisDelivDifference", tags = "配销差异单模块")
public class OrdDisDelivDifferenceController {

    private final OrdDisDelivDifferenceService ordDisDelivDifferenceService;

    private final AsyncTaskItemService asyncTaskItemService;

    private final StockServer stockServer;

    @ApiOperation(value = "分页获取配销差异单列表")
    @GetMapping(value = "/findDiffOrderByParam")
    public Response<Page<DisDifferenceOrderOut>> findDiffOrderByParam(DisDifferenceOrderIn differenceOrderIn) {
        differenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(ordDisDelivDifferenceService.findDiffOrderByParam(differenceOrderIn));
    }

    @ApiOperation(value = "加盟差异单汇总", notes = "加盟差异单汇总")
    @GetMapping(value = "/summary")
    public Response<DisDiffOrderSummaryOut> diffOrderSummary(DisDifferenceOrderIn differenceOrderIn) {
        differenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        DisDiffOrderSummaryOut orderSummaryOut = ordDisDelivDifferenceService.diffOrderSummary(differenceOrderIn);
        return Response.data(orderSummaryOut);
    }

    @ApiOperation(value = "运营端查询配销差异单表头", notes = "运营端查询配销差异单表头", httpMethod = "GET")
    @GetMapping("/getBackHeaderDifferenceOrderOutById")
    public Response<DisDifferenceOrderOut> getBackHeaderDifferenceOrderOutById(@RequestParam Integer differenceOrderId) {
        if (null == differenceOrderId) {
            return Response.error("配销差异单主键不能为空");
        }
        DisDifferenceOrderOut disDifferenceOrderOut = ordDisDelivDifferenceService.getBackHeaderDifferenceOrderOutById(differenceOrderId);
        return Response.data(disDifferenceOrderOut);
    }

    @ApiOperation(value = "导出配销差异单列表", notes = "导出配销差异单列表")
    @PostMapping("/exportDisDifferences")
    public Response<String> exportDisDifferences(@RequestBody DisDifferenceOrderIn differenceOrderIn) {
        differenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        log.info("导出获取配销差异单列表入参是--{}", differenceOrderIn);
        log.info("开始执行/ord/ordDisDelivDifference/exportDisDifferences");
        String fileUrl = ordDisDelivDifferenceService.exportDisDifferences(differenceOrderIn);
        log.info("结束执行/ord/ordDisDelivDifference/exportDisDifferences");
        log.info("导出获取配销差异单列表返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    /**
     * 作废配销差异单
     *
     * @return
     */
    @ApiOperation(value = "作废配销差异单", notes = "作废配销差异单")
    @PostMapping("/invalid")
    public Response invalidDisDifference(@RequestBody OrdDisDelivDifference ordDisDelivDifference) {
        OrdDisDelivDifference query = new OrdDisDelivDifference();
        query.setId(ordDisDelivDifference.getId());
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        OrdDisDelivDifference disDelivDifference = ordDisDelivDifferenceService.selectOne(query);
        if (Objects.isNull(disDelivDifference)) {
            return Response.error("找不到此配销差异单");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(disDelivDifference.getPosition(), UserUtil.getBizOrgCode(), "作废配销差异单");
        int invalid = ordDisDelivDifferenceService.invalidDisDifference(disDelivDifference, stockInfoOut);
        if (invalid > 0) {
            return Response.success("作废成功");
        }
        return Response.error("作废失败");
    }

    /**
     * 批量批准配销差异单
     *
     * @return
     */
    @ApiOperation(value = "批量批准配销差异单", notes = "批量批准配销差异单")
    @PostMapping("/batchApproved")
    public Response batchApprovedDisDifference(@RequestBody List<Integer> disDifferenceOrderIds) {
        int approved = ordDisDelivDifferenceService.batchApprovedDisDifference(disDifferenceOrderIds);
        if (approved > 0) {
            return Response.success("批准成功");
        }
        return Response.error("批准失败");
    }

    /**
     * 批准配销差异单
     *
     * @return
     */
    @ApiOperation(value = "批准配销差异单", notes = "批准配销差异单")
    @PostMapping("/approved")
    public Response approvedDisDifference(@RequestBody @Valid ApprovedDisDifferenceOrderIn approvedDisDifferenceOrderIn) {
        approvedDisDifferenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        int approved = ordDisDelivDifferenceService.approvedDisDifference(approvedDisDifferenceOrderIn);
        if (approved > 0) {
            return Response.success("批准成功");
        }
        return Response.error("批准失败");
    }

    /**
     * 冲销配销差异单
     *
     * @return
     */
    @ApiOperation(value = "冲销配销差异单", notes = "冲销配销差异单")
    @PostMapping("/charge")
    public Response chargeDisDifference(@RequestBody ApprovedDisDifferenceOrderIn approvedDisDifferenceOrderIn) {
        approvedDisDifferenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        int charge = ordDisDelivDifferenceService.chargeDisDifference(approvedDisDifferenceOrderIn);
        if (charge > 0) {
            return Response.success("冲销成功");
        }
        return Response.error("冲销失败");
    }

    /**
     * @param saveDifferenceIn
     * @return
     */
    @ApiOperation(value = "保存配销差异单(测试用)", notes = "保存配销差异单")
    @PostMapping("/saveDifferenceOrder")
    public Response saveDifferenceOrder(@RequestBody SaveDifferenceIn saveDifferenceIn) {
        return ordDisDelivDifferenceService.saveDisDifference(saveDifferenceIn);
    }

    @ApiOperation(value = "查询配销差异单列表（库存盘点）", notes = "查询配销差异单列表（库存盘点）", httpMethod = "GET")
    @PostMapping("/findDisDifferenceOrders")
    public Response<List<DisDifferenceOrderOut>> findDisDifferenceOrders(@RequestBody OrdDisDelivDifference disDelivDifference) {
        if (StringUtil.isEmpty(disDelivDifference.getBizOrgCode())) {
            disDelivDifference.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        List<DisDifferenceOrderOut> disDifferenceOrderOuts = ordDisDelivDifferenceService.findDisDifferenceOrders(disDelivDifference);
        return Response.data(disDifferenceOrderOuts);
    }

    @PostMapping("/disDeliveryToDifference")
    @ApiOperation(value = "差异单", notes = "差异单")
    public void disDeliveryToDifference(@RequestBody JSONObject json) {
        SaveDifferenceIn saveDifferenceIn = JSONObject.parseObject(json.toJSONString(), SaveDifferenceIn.class);
        asyncTaskItemService.disDeliveryToDifference(saveDifferenceIn);
    }
}
