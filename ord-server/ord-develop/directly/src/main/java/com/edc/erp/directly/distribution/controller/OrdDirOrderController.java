package com.edc.erp.directly.distribution.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.service.AppUserService;
import com.edc.erp.common.util.MqUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirrequestorder.handle.DirCutOrderHandle;
import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistribution;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.model.in.*;
import com.edc.erp.directly.distribution.model.out.*;
import com.edc.erp.directly.distribution.service.OrdDirOrderTrackService;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
import com.edc.erp.directly.enumeration.OrderTrackLogTemplateEnum;
import com.edc.erp.directly.enumeration.OrderTrackStatusEnum;
import com.edc.erp.directly.handle.DirDeliveryOrderHandle;
import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.DateUtils;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 直营订单表 前端控制器
 *
 * @author wanglidong
 * @since 2022-11-15 16:23:06
 */
@Slf4j
@RestController
@RequestMapping("/ord/ordDirOrder")
@Api(value = "ordDirOrder", tags = "直营订单表模块")
public class OrdDirOrderController {

    @Autowired
    private OrderHandle orderHandle;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private DirDeliveryOrderHandle dirDeliveryOrderHandle;

    @Autowired
    private OrdDirOrderTrackService ordDirOrderTrackService;

    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;

    @Autowired
    private DirCutOrderHandle dirCutOrderHandle;

    @ApiOperation(value = "直营订货单列表查询", notes = "直营订货单列表查询")
    @GetMapping(value = "/findByPage")
    public Response<Page<DirOrderOut>> findDisOrderByPage(OrderIn orderIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        orderIn.setBizOrgCode(bizOrgCode);
        Page<DirOrderOut> dirOrderOutPage = orderHandle.findOrderOutPage(orderIn);
        return Response.data(dirOrderOutPage);
    }

    @ApiOperation(value = "查询直营订货单明细", notes = "查询直营订货单明细")
    @GetMapping("/getOrderDetailInfoOut")
    public Response<OrderDetailInfoOut> getOrderDetailInfoOut(@RequestParam("orderId") @Valid Long orderId) {
        OrderDetailInfoOut orderDetailInfoOut = orderHandle.getOrderDetailInfoOut(orderId, UserUtil.getBizOrgCode());
        return Response.data(orderDetailInfoOut);
    }

    @ApiOperation(value = "导出直营订货单", notes = "导出直营订货单")
    @PostMapping("/exportOrdDirOrder")
    public Response exportOrdDirOrder(@RequestBody OrderIn orderIn) {
        orderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        log.info("开始执行/ord/ordDirOrder/exportOrdDirOrder");
        String privateUrl = orderHandle.exportOrdDirOrder(orderIn);
        log.info("结束执行/ord/ordDirOrder/exportOrdDirOrder");
        return StringUtils.isBlank(privateUrl) ? Response.error("导出失败，请重试") : Response.data(privateUrl);
    }

    @ApiOperation(value = "直营订货单汇总", notes = "直营订货单汇总")
    @GetMapping(value = "/summary")
    public Response<OrderSummaryOut> orderSummary(OrderIn orderIn) {
        orderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        OrderSummaryOut orderSummaryOut = orderHandle.orderSummary(orderIn);
        return Response.data(orderSummaryOut);
    }

    @ApiOperation(value = "运营端分货单批量作废直营订货单", notes = "运营端分货单批量作废直营订货单")
    @PostMapping("/batchInvalidOrder")
    public Response batchInvalidOrder(@RequestBody @Valid BatchInvalidOrderIn batchInvalidOrderIn) {
        batchInvalidOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        batchInvalidOrderIn.setUsername(UserUtil.getUserName());
        return orderHandle.batchInvalidOrder(batchInvalidOrderIn);
    }

    @ApiOperation(value = "APP直营订货单作废")
    @GetMapping(value = "/invalidOrder")
    public Response<String> invalidOrder(@RequestParam("orderId") Long orderId) {
        String userName = UserUtil.getUserName();
        String bizOrgCode = UserUtil.getBizOrgCode();
        OrdDirOrder orderOut = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (orderOut == null) {
            return Response.error("订货单不存在");
        }
        if (BusinessTypeColumnEnum.HEAD_OFFICE_REPLENISH.getType().equals(orderOut.getSourceCode())) {
            return Response.error(BusinessTypeColumnEnum.HEAD_OFFICE_REPLENISH.getName() + "类型订单APP不能作废");
        }
        String statusCode = orderOut.getOrderStatusCode();
        if (OrderStatusEnum.TO_REQUEST_ORDER.getKey().equals(statusCode)) {
            return Response.error("已转单的订货单不可作废");
        }
        if (OrderStatusEnum.INVALID.getKey().equals(statusCode)) {
            return Response.error("不可重复作废");
        }
        orderHandle.invalidOrder(orderOut, userName, bizOrgCode);
        String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.APP_CANCEL_ORDER.getTemplate(), orderOut.getOrderNo());
        ordDirOrderTrackService.pushRedisOrderTrackMessage(orderOut.getOrderNo(), orderOut.getStoreCode(),
                OrderTrackStatusEnum.INVALID_ORDER.getName(), trackLog, orderOut.getBizOrgCode(), orderOut.getCreator(), orderOut.getCreateTime());
        return Response.success("已作废");
    }

    @ApiOperation(value = "(APP端)直营提交手工订货订货单", notes = "(APP端)直营提交手工订货订货单")
    @PostMapping("/submitOrder")
    public Response submitOrder(@RequestBody @Valid List<CreateOrderSkuIn> createOrderSkuInList) {
        String userName = UserUtil.getUserName();
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(userName);
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
        }
        Response<AfterOrderCreatedMqOut> manualOrder = orderHandle.createManualOrder(appUserOut.getStoreCode(), userName, createOrderSkuInList, UserUtil.getBizOrgCode());
        return manualOrder;
    }


    @ApiOperation(value = "计算所选订货单总金额", notes = "计算所选订货单总金额", httpMethod = "POST")
    @PostMapping("/calculationOrderListAmount")
    public Response<DirCalculationCheckSubmittedListAmountOut> calculationPurchaseListAmount(@RequestBody @Valid CalculationDirOrderIn calculationOrderIn) {
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(UserUtil.getUserName());
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
        }
        return orderHandle.initCalculationCheckSubmittedListAmount(calculationOrderIn.getOrderIdList(), appUserOut);
    }

    @ApiOperation(value = "再来一单", notes = "再来一单", httpMethod = "GET")
    @GetMapping("/copyOrder")
    public Response<String> copyOrder(@RequestParam @Valid Long orderId, @RequestParam @Valid String bizOrgCode) {
        OrdDirOrder order = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (Objects.isNull(order)) {
            return Response.error("不存在的订货单");
        }
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(UserUtil.getUserName());
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
        }
        orderHandle.copyOrder(orderId, appUserOut);
        return Response.success();
    }

    @ApiOperation(value = "查找可编辑的订货单", notes = "", httpMethod = "POST")
    @PostMapping("/findCanEditOrderList")
    public Response<List<AppDirOrderCycleOut>> findCanEditOrderList(@RequestBody @Valid AppQueryDirOrderIn appQueryOrderIn) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.SUBMIT.getKey());
        appQueryOrderIn.setOrderStatusCodeList(orderStatusCodeList);
        appQueryOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        List<AppDirOrderCycleOut> appOrderCycleOuts = orderHandle.findAppEditOrderList(appQueryOrderIn);
        return Response.data(appOrderCycleOuts, "成功");
    }

    @ApiOperation(value = "查询14日订货单列表", notes = "", httpMethod = "POST")
    @PostMapping("/findAppOrderList")
    public Response<List<AppDirOrderCycleOut>> findAppOrderList(@RequestBody @Valid AppQueryDirOrderIn appQueryOrderIn) {
        LocalDate beginDate = LocalDate.now().minusDays(14);
        appQueryOrderIn.setBeginTime(DateUtils.format(beginDate) + " 00:00:00");
        List<AppDirOrderCycleOut> appOrderCycleOuts = orderHandle.findAppEditOrderList(appQueryOrderIn);
        return Response.data(appOrderCycleOuts, "成功");
    }

    @ApiOperation(value = "查询订货单明细", notes = "", httpMethod = "GET")
    @GetMapping("/getAppDirOrderDetailInfoOut")
    public Response<AppDirOrderDetailInfoOut> getAppDirOrderDetailInfoOut(@RequestParam @Valid Long orderId, @RequestParam @Valid String bizOrgCode) {
        AppDirOrderDetailInfoOut appOrderDetailInfoOut = orderHandle.getAppOrderDetailInfoOut(orderId, bizOrgCode);
        return Response.data(appOrderDetailInfoOut);
    }

    @ApiOperation(value = "更新订货单商品明细")
    @PostMapping("/updateDirOrderGoodsDetail")
    public Response updateDirOrderGoodsDetail(@RequestBody @Valid UpdateDirOrderIn updateOrderIn) {
        return orderHandle.updateOrderSkuPackageQuantity(updateOrderIn, UserUtil.getUserName());
    }

    @Autowired
    private DirAsyncTaskItemService dirAsyncTaskItemService;

    @PostMapping("/toRequest")
    public void toRequest(@RequestBody JSONObject jsonObject) {
        com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = JSON.parseObject(jsonObject.toJSONString(), SendBeforeCreateRequestOrderMqIn.class);
        dirAsyncTaskItemService.dirDistributionToRequest(sendBeforeCreateRequestOrderMqIn);
    }

    @PostMapping("/dirRequestToDelivery")
    public void dirRequestToDelivery(@RequestBody JSONObject json) {
        RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(json.toJSONString(), RequestOrderCreateMqIn.class);
        dirAsyncTaskItemService.dirRequestToDelivery(requestOrderCreateMqIn);
    }

    @PostMapping("/dirDistributionToOrder")
    public void dirDistributionToOrder(@RequestBody JSONObject jsonObject) {
        OrdDirOrderDistribution orderDistribution = JSON.parseObject(jsonObject.toJSONString(), OrdDirOrderDistribution.class);
        dirAsyncTaskItemService.dirDistributionCreateOrder(orderDistribution);
    }


    @PostMapping("/toTransferDeliveryDirOrder")
    @ApiOperation(value = "测试加推采购单", notes = "测试加推采购单")
//    public void toTransferDeliveryDirOrder(@RequestBody List<OrdDirDelivery> dirDeliveries){
    public Response<String> toTransferDeliveryDirOrder(@RequestBody List<Long> idList) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        List<OrdDirDelivery> dirDeliveries = dirDeliveryOrderHandle.findListByIdList(idList);
        if (CollectionUtils.isEmpty(dirDeliveries)) {
            return Response.error("不存在的单据信息");
        }
        dirDeliveryOrderHandle.transferDeliveryDirOrder(dirDeliveries, bizOrgCode);
        return Response.success();
    }

    @GetMapping("/manualCompensationHandleCutOrder")
    public Response manualCompensationHandleCutOrder(@RequestParam String ids) {
        List<OrdDirOrderCycle> orderCycleList = dirOrderCycleHandle.findByOrderIds(ids);
        // 处理截单
        dirCutOrderHandle.handleCutOrderJob(orderCycleList);
        return Response.success();
    }

    @GetMapping("/getCycleOrderedGoodsQty")
    @ApiOperation(value = "查询此商品同一周期内非作废且来源是手工订货和上下限跑货的订单中的非赠品的订货量")
    public Response<BigDecimal> getCycleOrderedGoodsQty(@RequestParam String storeCode, @RequestParam LocalDateTime truncationDateTime,
                                                        @RequestParam Integer orderTypeConfigId, @RequestParam String goodsCode) {
        return Response.data(orderHandle.getCycleOrderedGoodsQty(storeCode, truncationDateTime, orderTypeConfigId, goodsCode, UserUtil.getBizOrgCode()));
    }
}
