package com.edc.erp.distribution.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.service.AppUserService;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disrequestorder.handle.DisCutOrderHandle;
import com.edc.erp.disrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.model.in.*;
import com.edc.erp.distribution.model.out.*;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.erp.enumeration.*;
import com.edc.erp.enumeration.OrderLogEnum;
import com.edc.erp.enumeration.OrderStatusEnum;
import com.edc.erp.enumeration.OrderTrackLogTemplateEnum;
import com.edc.erp.enumeration.OrderTrackStatusEnum;
import com.edc.erp.handle.DisDeliveryOrderHandle;
import com.edc.erp.handle.DisOrderConfigHandle;
import com.edc.erp.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.DateUtils;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * <p>
 * 配销订单表 前端控制器
 * </p>
 *
 * @author fxw
 * @since 2022-10-17 15:55:14
 */
@Slf4j
@RestController
@RequestMapping("/ord/ordDisOrder")
@Api(value = "ordDisOrder", tags = "配销订单表模块")
public class OrdDisOrderController {

    @Autowired
    private DisOrderHandle orderHandle;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private DisOrderConfigHandle orderConfigHandle;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AsyncLogService asyncLogService;
    @Autowired
    private OrdDisOrderTrackService ordDisOrderTrackService;

    @Autowired
    private DisCutOrderHandle disCutOrderHandle;

    @Autowired
    private DisDeliveryOrderHandle disDeliveryOrderHandle;

    /**
     * 订货单列表查询
     *
     * @param orderIn
     * @return
     */
    @ApiOperation(value = "订货单列表查询")
    @GetMapping(value = "/findByPage")
    public Response<Page<DisOrderOut>> findDisOrderByPage(OrderIn orderIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        orderIn.setBizOrgCode(bizOrgCode);
        Page<DisOrderOut> disOrderOutPage = orderHandle.findOrderOutPage(orderIn);
        return Response.data(disOrderOutPage);
    }

    /**
     * 订货单汇总
     *
     * @param orderIn
     * @return
     */
    @ApiOperation(value = "订货单汇总")
    @GetMapping(value = "/summary")
    public Response<OrderSummaryOut> orderSummary(OrderIn orderIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        orderIn.setBizOrgCode(bizOrgCode);
        OrderSummaryOut orderSummaryOut = orderHandle.orderSummary(orderIn);
        return Response.data(orderSummaryOut);
    }

    /**
     * 订货单列表导出
     *
     * @param orderIn
     * @return
     */
    @ApiOperation(value = "订货单列表导出")
    @PostMapping(value = "/export")
    public Response exportOrderList(@RequestBody OrderIn orderIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        orderIn.setBizOrgCode(bizOrgCode);
        boolean errorFlag = StringUtils.isBlank(orderIn.getBeginAcceptDate()) && StringUtils.isBlank(orderIn.getEndAcceptDate());
        if (errorFlag) {
            return Response.error("导出订货订单列表必须传提交时间段或截单日期时间段");
        }
        log.info("开始执行/ord/ordDisOrder/export");
        String privateUrl = orderHandle.exportOrderList(orderIn);
        log.info("结束执行/ord/ordDisOrder/export");
        return StringUtils.isBlank(privateUrl) ? Response.error("导出失败，请重试") : Response.data(privateUrl);
    }

    /**
     * 提交手工订货订货单
     *
     * @param createOrderSkuInList
     * @return
     */
    @ApiOperation(value = "(APP端)提交手工订货订货单")
    @PostMapping("/submitOrder")
    public Response submitOrder(@RequestBody @Valid List<CreateOrderSkuIn> createOrderSkuInList) {
        String userName = UserUtil.getUserName();
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(userName);
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
        }
        Response<AfterOrderCreatedMqOut> manualOrder = orderHandle.createManualOrder(appUserOut.getStoreCode(), userName,
                appUserOut.getBizOrgCode(), createOrderSkuInList, OrderIdentificationEnum.NORMAL_ORDER.getCode());
        return manualOrder;
    }

    /**
     * 批量作废订货单
     *
     * @param batchInvalidOrderIn
     * @return
     */
    @ApiOperation(value = "运营端分货单批量作废订货单")
    @PostMapping("/batchInvalidOrder")
    public Response batchInvalidOrder(@RequestBody @Valid BatchInvalidOrderIn batchInvalidOrderIn) {
        batchInvalidOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        batchInvalidOrderIn.setUsername(UserUtil.getUserName());
        return orderHandle.batchInvalidOrder(batchInvalidOrderIn);
    }

    @ApiOperation(value = "APP配销订货单作废")
    @GetMapping(value = "/invalidOrder")
    public Response<String> invalidOrder(@RequestParam("orderId") Long orderId) {
        String userName = UserUtil.getUserName();
        String bizOrgCode = UserUtil.getBizOrgCode();
        OrdDisOrder ordDisOrder = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (ordDisOrder == null) {
            return Response.error("订货单不存在");
        }
        String statusCode = ordDisOrder.getOrderStatusCode();
        if (OrderStatusEnum.TO_REQUEST_ORDER.getKey().equals(statusCode)) {
            return Response.error("已转单的订货单不可作废");
        }
        if (OrderStatusEnum.INVALID.getKey().equals(statusCode)) {
            return Response.error("不可重复作废");
        }
        orderHandle.invalidOrder(ordDisOrder, userName, bizOrgCode);
        String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.APP_CANCEL_ORDER.getTemplate(), ordDisOrder.getOrderNo());
        ordDisOrderTrackService.pushRedisOrderTrackMessage(ordDisOrder.getOrderNo(), ordDisOrder.getStoreCode(),
                OrderTrackStatusEnum.INVALID_ORDER.getName(), trackLog, ordDisOrder.getBizOrgCode(), ordDisOrder.getCreator(), LocalDateTime.now());
        return Response.success("已作废");
    }

    /**
     * 根据订货单id查询订货单明细信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "(app端)查询订货单明细", notes = "(app端)查询订货单明细", httpMethod = "GET")
    @GetMapping("/getOrderDetailInfoOut")
    public Response<OrderDetailInfoOut> getOrderDetailInfoOut(@RequestParam @Valid Long orderId, @RequestParam @Valid String bizOrgCode) {
        OrderDetailInfoOut orderDetailInfoOut = orderHandle.getOrderDetailInfoOut(orderId, bizOrgCode);
        return Response.data(orderDetailInfoOut);
    }

    /**
     * 运营端订货单作废
     *
     * @param orderId
     * @return
     */
    @ApiOperation(value = "运营端订货单作废")
    @GetMapping(value = "/backInvalid")
    public Response<String> backInvalidOrder(@RequestParam("orderId") Long orderId) {
        String userName = UserUtil.getUserName();
        String bizOrgCode = UserUtil.getBizOrgCode();
        OrdDisOrder orderOut = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (orderOut == null) {
            return Response.error("订货单不存在");
        }
        String statusCode = orderOut.getOrderStatusCode();
        if (OrderStatusEnum.TO_REQUEST_ORDER.getKey().equals(statusCode)) {
            return Response.error("已转单的订货单不可作废");
        }
        if (OrderStatusEnum.INVALID.getKey().equals(statusCode)) {
            return Response.error("不可重复作废");
        }
        orderHandle.invalidOrder(orderOut, userName, bizOrgCode);
        // 手动作废整单推送订单追踪日志
        String content = MessageFormat.format(OrderLogEnum.OPERATE_ORDER_INVALID.getKey(), orderOut.getOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), orderId.toString(),
                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), userName);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("已作废");
    }

    /**
     * 加推订货单
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "加推订货单")
    @GetMapping("/addPushOrder")
    public Response addPushOrder(@RequestParam("orderId") Long orderId,
                                 @RequestParam(value = "bizOrgCode", required = false) String bizOrgCode) {
        if (StringUtils.isBlank(bizOrgCode)) {
            bizOrgCode = UserUtil.getBizOrgCode();
        }
        OrdDisOrder ordDisOrder = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (Objects.isNull(ordDisOrder)) {
            return Response.error("无效订货单");
        }

        OrdDisOrderCycle ordDisOrderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(ordDisOrder.getOrderCycleId(), bizOrgCode);
        OrderTypeConfig orderTypeConfig = orderConfigHandle.getOrderTypeConfigByIdAndBizOrgCode(ordDisOrderCycle.getOrderTypeConfigId(), bizOrgCode);
        if (SystemConstant.FROZEN_DISTRIBUTION_CYCLE.equals(orderTypeConfig.getOrderPeriod())) {
            LocalDateTime beginTime = DateUtils.parseTime(LocalDate.now() + " 09:30:00");
            LocalDateTime endTime = DateUtils.parseTime(LocalDate.now() + " 12:00:00");
            if (LocalDateTime.now().isAfter(beginTime) && LocalDateTime.now().isBefore(endTime)) {
                //return Response.error("低温订货单09:30~12:00不允许加推");
                // 2021-12-07 应产品要求将限制去除，打印日志
                log.warn("低温订货单09:30~12:00不允许加推");
            }
        }
        String userName = UserUtil.getNickname() + "【" + UserUtil.getJobNumber() + "】";
        return orderHandle.addPushOrder(ordDisOrder, userName);
    }

    @ApiOperation(value = "APP根据单号查询配销订货单", notes = "APP根据单号查询配销订货单")
    @GetMapping("/getOrderOut")
    public Response<DisOrderOut> getOrderOut(@RequestParam("orderNo") @Valid String orderNo) {
        DisOrderOut disOrderOut = orderHandle.getOrderOut(orderNo, UserUtil.getBizOrgCode());
        return Response.data(disOrderOut);
    }


    @ApiOperation(value = "计算所选订货单所需支付总金额", notes = "计算所选订货单所需支付总金额", httpMethod = "POST")
    @PostMapping("/calculationDisOrderListAmount")
    public Response<DisCalculationCheckSubmittedListAmountOut> calculationDisOrderListAmount(@RequestBody @Valid CalculationDisOrderIn calculationOrderIn) {
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(UserUtil.getUserName());
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
        }
        return orderHandle.initCalculationCheckSubmittedListAmount(calculationOrderIn.getOrderIdList(), appUserOut);
    }

    @ApiOperation(value = "再来一单", notes = "再来一单", httpMethod = "GET")
    @GetMapping("/copyDisOrder")
    public Response<String> copyOrder(@RequestParam @Valid Long orderId, @RequestParam @Valid String bizOrgCode) {
        OrdDisOrder order = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
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

    @ApiOperation(value = "查找待付款的订货单", notes = "", httpMethod = "POST")
    @PostMapping("/findAppNeedPayOrderList")
    public Response<List<AppDisOrderCycleOut>> findAppNeedPayOrderList(@RequestBody @Valid AppQueryDisOrderIn appQueryOrderIn) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.WAIT_PAYMENT.getKey());
        appQueryOrderIn.setOrderStatusCodeList(orderStatusCodeList);
        appQueryOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        List<AppDisOrderCycleOut> appOrderCycleOuts = orderHandle.findAppOrderList(appQueryOrderIn);
        return Response.data(appOrderCycleOuts, "成功");
    }

    @ApiOperation(value = "查询14日订货单列表", notes = "", httpMethod = "POST")
    @PostMapping("/findAppDisOrderList")
    public Response<List<AppDisOrderCycleOut>> findAppOrderList(@RequestBody @Valid AppQueryDisOrderIn appQueryOrderIn) {
        LocalDate beginDate = LocalDate.now().minusDays(14);
        appQueryOrderIn.setBeginTime(DateUtils.format(beginDate) + " 00:00:00");
        List<AppDisOrderCycleOut> appOrderCycleOuts = orderHandle.findAppOrderList(appQueryOrderIn);
        return Response.data(appOrderCycleOuts, "成功");
    }

    @ApiOperation(value = "查询订货单明细", notes = "", httpMethod = "GET")
    @GetMapping("/getAppDisOrderDetailInfoOut")
    public Response<AppDisOrderDetailInfoOut> getAppOrderDetailInfoOut(@RequestParam @Valid Long orderId, @RequestParam @Valid String bizOrgCode) {
        AppDisOrderDetailInfoOut appOrderDetailInfoOut = orderHandle.getAppOrderDetailInfoOut(orderId, bizOrgCode);
        return Response.data(appOrderDetailInfoOut);
    }

    @ApiOperation(value = "更新订货单商品明细")
    @PostMapping("/updateDisOrderGoodsDetail")
    public Response updateDisOrderGoodsDetail(@RequestBody @Valid UpdateDisOrderIn updateOrderIn) {
        return orderHandle.updateOrderSkuPackageQuantity(updateOrderIn, UserUtil.getUserName());
    }


    @Autowired
    private AsyncTaskItemService asyncTaskItemService;

    @PostMapping("/toRequest")
    public void toRequest(@RequestBody JSONObject json) {
        SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = JSON.parseObject(json.toJSONString(), SendBeforeCreateRequestOrderMqIn.class);
        asyncTaskItemService.disDistributionToRequest(sendBeforeCreateRequestOrderMqIn);
    }

    @PostMapping("/disRequestToDelivery")
    @ApiOperation(value = "集货单生成配销单", notes = "集货单生成配销单")
    public void disRequestToDelivery(@RequestBody JSONObject json) {
        RequestOrderCreateMqIn requestOrderCreateMqIn = JSON.parseObject(json.toJSONString(), RequestOrderCreateMqIn.class);
        asyncTaskItemService.disRequestToDelivery(requestOrderCreateMqIn);
    }

//    @PostMapping("/disDistributionToOrder")
//    @ApiOperation(value = "分货单生成订货单", notes = "分货单生成订货单")
//    public void disDistributionToOrder(@RequestBody JSONObject json){
//        asyncTaskItemService.disDistributionToOrder(json.toJSONString());
//    }

    @GetMapping("/manualCompensationHandleCutOrder")
    public Response manualCompensationHandleCutOrder(@RequestParam String ids) {
        List<OrdDisOrderCycle> orderCycleList = disOrderCycleHandle.findByOrderIds(ids);
        // 处理截单
        disCutOrderHandle.handleCutOrderJob(orderCycleList);
        return Response.success();
    }

    @GetMapping("/getCycleOrderedGoodsQty")
    @ApiOperation(value = "查询此商品同一周期内非作废且来源是手工订货和上下限跑货的订单中的非赠品的订货量")
    public Response<BigDecimal> getCycleOrderedGoodsQty(@RequestParam String storeCode, @RequestParam LocalDateTime truncationDateTime,
                                                        @RequestParam Integer orderTypeConfigId, @RequestParam String goodsCode) {
        return Response.data(orderHandle.getCycleOrderedGoodsQty(storeCode, truncationDateTime, orderTypeConfigId, goodsCode, UserUtil.getBizOrgCode()));
    }

    /**
     * 批量加推订货单
     *
     * @param orderIds
     * @param bizOrgCode
     * @return
     */
    @ApiOperation(value = "批量加推订货单")
    @GetMapping("/batchAddPushOrder")
    public Response batchAddPushOrder(@RequestParam("orderIds") List<Long> orderIds,
                                      @RequestParam(value = "bizOrgCode", required = false) String bizOrgCode) {
        if (StringUtils.isBlank(bizOrgCode)) {
            bizOrgCode = UserUtil.getBizOrgCode();
        }
        StringBuilder msgBuilder = new StringBuilder();
        for (Long orderId : orderIds) {
            OrdDisOrder ordDisOrder = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
            if (Objects.isNull(ordDisOrder)) {
                msgBuilder.append(orderId).append("无效订货单;");
                continue;
            }
            OrdDisOrderCycle ordDisOrderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(ordDisOrder.getOrderCycleId(), bizOrgCode);
            OrderTypeConfig orderTypeConfig = orderConfigHandle.getOrderTypeConfigByIdAndBizOrgCode(ordDisOrderCycle.getOrderTypeConfigId(), bizOrgCode);
            if (SystemConstant.FROZEN_DISTRIBUTION_CYCLE.equals(orderTypeConfig.getOrderPeriod())) {
                LocalDateTime beginTime = DateUtils.parseTime(LocalDate.now() + " 09:30:00");
                LocalDateTime endTime = DateUtils.parseTime(LocalDate.now() + " 12:00:00");
                if (LocalDateTime.now().isAfter(beginTime) && LocalDateTime.now().isBefore(endTime)) {
                    //return Response.error("低温订货单09:30~12:00不允许加推");
                    // 2021-12-07 应产品要求将限制去除，打印日志
                    log.warn("低温订货单09:30~12:00不允许加推");
                }
            }
            String userName = UserUtil.getNickname() + "【" + UserUtil.getJobNumber() + "】";
            Response<String> response;
            try {
                response = orderHandle.addPushOrder(ordDisOrder, userName);
                if (!response.isSuccess()) {
                    msgBuilder.append(ordDisOrder.getOrderNo()).append(response.getMessage()).append(";");
                }
            } catch (Exception e) {
                msgBuilder.append(ordDisOrder.getOrderNo()).append(e.getMessage()).append(";");
            }
        }
        return msgBuilder.length() > 0 ? Response.error(msgBuilder.toString()) : Response.success("加推成功");
    }

    /**
     * 运营端批量加盟订货单作废
     *
     * @param orderIds
     * @return
     */
    @ApiOperation(value = "运营端批量加盟订货单作废")
    @GetMapping(value = "/batchBackInvalid")
    public Response<String> batchBackInvalidOrder(@RequestParam("orderIds") List<Long> orderIds) {
        String userName = UserUtil.getUserName();
        String bizOrgCode = UserUtil.getBizOrgCode();
        StringBuilder msgBuilder = new StringBuilder();
        for (Long orderId : orderIds) {
            OrdDisOrder orderOut = orderHandle.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
            if (orderOut == null) {
                msgBuilder.append(orderId).append("订货单不存在;");
                continue;
            }
            String statusCode = orderOut.getOrderStatusCode();
            if (OrderStatusEnum.TO_REQUEST_ORDER.getKey().equals(statusCode)) {
                msgBuilder.append(orderOut.getOrderNo()).append("已转单的订货单不可作废;");
                continue;
            }
            if (OrderStatusEnum.INVALID.getKey().equals(statusCode)) {
                msgBuilder.append(orderOut.getOrderNo()).append("不可重复作废;");
                continue;
            }
            orderHandle.invalidOrder(orderOut, userName, bizOrgCode);
            // 手动作废整单推送订单追踪日志
            String content = MessageFormat.format(OrderLogEnum.OPERATE_ORDER_INVALID.getKey(), orderOut.getOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), orderId.toString(),
                    OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), userName);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return msgBuilder.length() > 0 ? Response.error(msgBuilder.toString()) : Response.success("作废成功");
    }

    @ApiOperation(value = "(APP端)资产创建配销订货单")
    @PostMapping("/submitPresaleOrder")
    public Response submitPresaleOrder(@RequestBody @Valid List<CreatePresaleOrderGoodsIn> createPresaleOrderGoodsInList) {
        String userName = UserUtil.getUserName();
        AppUserOut appUserOut = appUserService.getAppUserByUserCode(userName);
        if (Objects.isNull(appUserOut)) {
            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
        }
        return orderHandle.createOrderForPresale(createPresaleOrderGoodsInList, appUserOut);
    }


    @PostMapping("/toTransferDeliveryDisOrder")
    @ApiOperation(value = "测试加推采购单", notes = "测试加推采购单")
//    public void toTransferDeliveryDirOrder(@RequestBody List<OrdDirDelivery> dirDeliveries){
    public Response<String> toTransferDeliveryDisOrder(@RequestBody List<Long> idList) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        List<OrdDisDelivery> disDeliveries = disDeliveryOrderHandle.findListByIdList(idList);
        if (CollectionUtils.isEmpty(disDeliveries)) {
            return Response.error("不存在的单据信息");
        }
        disDeliveryOrderHandle.transferDeliveryDisOrder(disDeliveries, bizOrgCode);
        return Response.success();
    }
}
