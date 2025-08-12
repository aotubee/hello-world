package com.edc.erp.presale.controller;

import com.edc.erp.model.in.QueryStorePresaleOrderForAppIn;
import com.edc.erp.model.in.QueryStorePresaleOrderIn;
import com.edc.erp.model.out.DisStorePresaleOrderGetForAppOut;
import com.edc.erp.model.out.DisStorePresaleOrderInfoOut;
import com.edc.erp.model.out.DisStorePresaleOrderPageForAppOut;
import com.edc.erp.presale.entity.OrdDisPresaleOrder;
import com.edc.erp.presale.enumeration.OrdDisPresaleOrderStatusEnum;
import com.edc.erp.presale.model.in.SubmitPresaleOrderIn;
import com.edc.erp.presale.model.out.CreatePresaleOrderDataOut;
import com.edc.erp.presale.service.OrdDisPresaleOrderService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @ClassName OrdDisPresaleActivityController
 * @Author ZhangYao
 * @CreateTime 2024/8/23 8:43
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disPresaleOrder")
@Api(value = "ordDisPresaleOrderController", tags = "预售订单接口")
public class OrdDisPresaleOrderController {
    private final OrdDisPresaleOrderService ordDisPresaleOrderService;

    @ApiOperation(value = "创建预售订单", notes = "创建预售订单", httpMethod = "POST")
    @PostMapping("/createPresaleOrder")
    public Response<Long> createPresaleOrder(@RequestBody SubmitPresaleOrderIn submitPresaleOrderIn) {
        submitPresaleOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        submitPresaleOrderIn.setLoginUsername(UserUtil.getUserName());
        submitPresaleOrderIn.setOrgCode(UserUtil.getOrgCode());
        CreatePresaleOrderDataOut createPresaleOrderDataOut = ordDisPresaleOrderService.handleDisPresaleOrderData(submitPresaleOrderIn);
        return ordDisPresaleOrderService.saveDisPresaleOrder(createPresaleOrderDataOut);
    }

    @ApiOperation(value = "取消门店预售订单", notes = "取消门店预售订单", httpMethod = "GET")
    @GetMapping(value = "/cancelStorePresaleOrder")
    public Response<String> cancelStorePresaleOrder(@RequestParam @NotNull Long id) {
        boolean result = ordDisPresaleOrderService.cancelStorePresaleOrder(id);
        if (result) {
            return Response.success("预售订单取消成功");
        }
        return Response.error("预售订单取消失败");
    }

    @ApiOperation(value = "APP查询门店预售订单", notes = "APP查询门店预售订单", httpMethod = "POST")
    @PostMapping(value = "/findStorePresaleOrderForApp")
    public Response<Page<DisStorePresaleOrderPageForAppOut>> findStorePresaleOrderForApp(@RequestBody QueryStorePresaleOrderForAppIn queryStorePresaleOrderForAppIn) {
        Page<DisStorePresaleOrderPageForAppOut> page = ordDisPresaleOrderService.findStorePresaleOrderForApp(queryStorePresaleOrderForAppIn);
        return Response.data(page);
    }

    @ApiOperation(value = "APP查询门店预售订单详情", notes = "APP查询门店预售订单详情", httpMethod = "GET")
    @GetMapping(value = "/getStorePresaleOrderInfoForApp")
    public Response<DisStorePresaleOrderGetForAppOut> getStorePresaleOrderInfoForApp(@RequestParam Long id) {
        DisStorePresaleOrderGetForAppOut storePresaleOrderInfoForApp = ordDisPresaleOrderService.getStorePresaleOrderInfoForApp(id);
        return Response.data(storePresaleOrderInfoForApp);
    }

    @ApiOperation(value = "运营端分页查询门店预售订单", notes = "运营的分页查询门店预售订单", httpMethod = "POST")
    @PostMapping(value = "/findStorePresaleOrderForPage")
    public Response<Page<DisStorePresaleOrderInfoOut>> findStorePresaleOrderForPage(@RequestBody QueryStorePresaleOrderIn queryStorePresaleOrderIn) {
        queryStorePresaleOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(ordDisPresaleOrderService.findStorePresaleOrderByPage(queryStorePresaleOrderIn));
    }

    @ApiOperation(value = "运营端查询门店预售订单详情", notes = "运营端查询门店预售订单详情", httpMethod = "GET")
    @GetMapping(value = "/getStorePresaleOrderInfo")
    public Response<DisStorePresaleOrderInfoOut> getStorePresaleOrderInfo(@RequestParam Long id) {
        DisStorePresaleOrderInfoOut storePresaleOrderInfoOut = ordDisPresaleOrderService.getStorePresaleOrderInfo(id);
        return Response.data(storePresaleOrderInfoOut);
    }

    @ApiOperation(value = "导出预售订单列表", notes = "导出预售订单列表")
    @PostMapping("/exportPresaleOrderList")
    public Response<String> exportPresaleOrderList(@RequestBody @Valid QueryStorePresaleOrderIn queryStorePresaleOrderIn) {
        log.info("导出预售订单列表入参是--{}", queryStorePresaleOrderIn);
        log.info("开始执行/ord/disPresaleOrder/exportPresaleOrderList");
        queryStorePresaleOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        String fileUrl = ordDisPresaleOrderService.exportPresaleOrderList(queryStorePresaleOrderIn);
        log.info("结束执行/ord/disPresaleOrder/exportPresaleOrderList");
        log.info("导出预售订单列表返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    @ApiOperation(value = "导出预售订单明细", notes = "导出预售订单明细")
    @GetMapping("/exportPresaleOrderDetailList")
    public Response<String> exportPresaleOrderDetailList(@RequestParam Long id) {
        log.info("开始执行/ord/disPresaleOrder/exportPresaleOrderDetailList");
        String fileUrl = ordDisPresaleOrderService.exportPresaleOrderDetailList(id);
        log.info("结束执行/ord/disPresaleOrder/exportPresaleOrderDetailList");
        log.info("导出预售订单列表返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    @ApiOperation(value = "预售订单退款", notes = "预售订单退款")
    @GetMapping("/checkIsCanRefund")
    public Response<String> checkIsCanRefund(@RequestParam Long id) {
        OrdDisPresaleOrder ordDisPresaleOrder = ordDisPresaleOrderService.getOneByIdAndBizOrgCode(id, UserUtil.getBizOrgCode());
        if (Objects.isNull(ordDisPresaleOrder)) {
            return Response.error("不存在的预售订单");
        }
        if (!OrdDisPresaleOrderStatusEnum.PAID.getKey().equals(ordDisPresaleOrder.getStatus())) {
            return Response.error("预售订单状态不支持退款");
        }
        Response<String> checkResponse = ordDisPresaleOrderService.checkIsCanRefund(ordDisPresaleOrder);
        return checkResponse;
    }

    @ApiOperation(value = "预售订单退款", notes = "预售订单退款")
    @GetMapping("/refundPresaleOrder")
    public Response<String> refundPresaleOrder(@RequestParam Long id) {
        OrdDisPresaleOrder ordDisPresaleOrder = ordDisPresaleOrderService.getOneByIdAndBizOrgCode(id, UserUtil.getBizOrgCode());
        if (Objects.isNull(ordDisPresaleOrder)) {
            return Response.error("不存在的预售订单");
        }
        if (!OrdDisPresaleOrderStatusEnum.PAID.getKey().equals(ordDisPresaleOrder.getStatus())) {
            return Response.error("预售订单状态不支持退款");
        }
        Response<String> checkResponse = ordDisPresaleOrderService.checkIsCanRefund(ordDisPresaleOrder);
        if (!checkResponse.isSuccess()) {
            return checkResponse;
        }
        return ordDisPresaleOrderService.refundPresaleOrder(ordDisPresaleOrder, UserUtil.getUserName());
    }
}
