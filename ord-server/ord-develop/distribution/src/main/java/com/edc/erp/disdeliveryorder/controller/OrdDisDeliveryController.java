package com.edc.erp.disdeliveryorder.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.DeliveryOrderSourceCodeEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.SalvageAuditTypeEnum;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderSigning;
import com.edc.erp.disdeliveryorder.model.in.*;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleReturnSaveIn;
import com.edc.erp.disdeliveryorder.model.in.zk.TaskZKWholesaleShipmentSaveIn;
import com.edc.erp.disdeliveryorder.model.out.*;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderSigningService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disdeliveryorder.service.OrdDisOverallDeliveryService;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.enumeration.DeliveryOrderReceiveProgressEnum;
import com.edc.erp.handle.TakeDisDeliveryHandle;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.sdk.dts.model.order.vo.UnificationBillVO;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * <p>
 * 配销单 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-10-10 19:48:29
 */
@Slf4j
@RestController
@RequestMapping("/ord/ordDisDelivery")
@Api(value = "ordDisDelivery", tags = "配销单模块")
public class OrdDisDeliveryController {

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    @Autowired
    private TakeDisDeliveryHandle takeDisDeliveryHandle;

    @Autowired
    private OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    @Autowired
    private AsyncTaskItemService asyncTaskItemService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrdDisOverallDeliveryService ordDisOverallDeliveryService;

    @Autowired
    private OrdDisDeliveryOrderSigningService ordDisDeliveryOrderSigningService;

    @Autowired
    private StoreChannelHandle storeChannelHandle;

    @Autowired
    private StockServer stockServer;

    /**
     * 作废配销单
     *
     * @param ordDisDelivery
     * @return
     */
    @ApiOperation(value = "作废配销单", notes = "作废配销单")
    @PostMapping("/invalidDisDeliverOrder")
    public Response invalidDisDeliverOrder(@RequestBody OrdDisDelivery ordDisDelivery) {
        String userName = UserUtil.getUserName();
        int invalid = ordDisDeliveryService.invalidDisDeliverOrder(ordDisDelivery.getId(), userName, true);
        if (invalid > 0) {
            return Response.success("作废成功");
        }
        return Response.error("作废失败");
    }

    /**
     * 审核配销单信息
     *
     * @return
     */
    @ApiOperation(value = "审核配销单", notes = "审核配销单")
    @PostMapping("/audit")
    public Response<OrdDisDeliveryOut> audit(@RequestBody OrdDisDeliveryIn ordDisDeliveryIn) {
//        if (StringUtils.isNotBlank(ordDisDeliveryIn.getBizOrgCode())) {
//            if (!UserUtil.getBizOrgCode().equals(ordDisDeliveryIn.getBizOrgCode())) {
//                return Response.error("无此单据审核权限");
//            }
//        } else {
        ordDisDeliveryIn.setBizOrgCode(UserUtil.getBizOrgCode());
//        }
        OrdDisDelivery ordDisDelivery = ordDisDeliveryService.selectByPrimaryKey(ordDisDeliveryIn.getId());
        if (Objects.isNull(ordDisDelivery)) {
            return Response.error("配销单不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDisDelivery.getStockCode(), UserUtil.getBizOrgCode(), "审核配销单");
        return ordDisDeliveryService.audit(stockInfoOut, ordDisDelivery);
    }

    /**
     * 审核配销单信息
     *
     * @return
     */
    @ApiOperation(value = "批量审核配销单", notes = "批量审核配销单")
    @PostMapping("/bachAudit")
    public Response bachAudit(@RequestBody List<OrdDisDeliveryIn> ordDisDeliveryIns) {
        if (CollectionUtils.isEmpty(ordDisDeliveryIns)) {
            return Response.error("审核数据不能为空");
        }
        return ordDisDeliveryService.bachAudit(UserUtil.getBizOrgCode(), UserUtil.getUserName(), ordDisDeliveryIns);
    }

    /**
     * 手动触发发货
     *
     * @return
     */
    @ApiOperation(value = "发货", notes = "发货")
    @PostMapping("/shipments")
    public Response<String> shipments(@RequestBody UpdateDisDeliveryInfoIn updateDisDeliveryInfoIn) {
        OrdDisDelivery ordDisDelivery = ordDisDeliveryService.selectByPrimaryKey(updateDisDeliveryInfoIn.getId());
        if (Objects.isNull(ordDisDelivery)) {
            throw new BusinessException("此配销单不存在！");
        }
        if (!DeliveryOrderEnum.APPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
            throw new BusinessException("此配销单状态为，" + DeliveryOrderEnum.getValueByKey(ordDisDelivery.getDeliveryStatusCode()) + "不可修改");
        }
        ordDisDelivery.setUpdater(UserUtil.getUserName());
        ordDisDelivery.setUpdateTime(LocalDateTime.now());
        ordDisDeliveryService.updateDisDeliveryInfo(ordDisDelivery, updateDisDeliveryInfoIn.getUpdateDeliveryDetailInList(), UserUtil.getBizOrgCode());
        return Response.success();
    }

    @ApiOperation(value = "批量发货", notes = "批量发货")
    @PostMapping("/batchShipments")
    public Response<String> batchShipments(@RequestBody List<Long> idList) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        idList.forEach(id -> {
            OrdDisDelivery ordDisDelivery = ordDisDeliveryService.selectByPrimaryKey(id);
            try {
                if (Objects.isNull(ordDisDelivery)) {
                    errorJoiner.add("配销单id：" + id + "不存在");
                    return;
                }
                if (!DeliveryOrderEnum.APPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
                    errorJoiner.add("配销单" + ordDisDelivery.getDeliveryOrderNo() + "状态为，" + DeliveryOrderEnum.getValueByKey(ordDisDelivery.getDeliveryStatusCode()) + "不可修改");
                    return;
                }
                ordDisDelivery.setUpdater(UserUtil.getUserName());
                ordDisDelivery.setUpdateTime(LocalDateTime.now());
                List<OrdDisDeliveryDetail> disDeliveryDetailList = ordDisDeliveryDetailService.findDeliveryOrderDetails(id);
                List<UpdateDisDeliveryDetailIn> updateDisDeliveryDetailInList = disDeliveryDetailList.stream().map(disDeliveryDetail -> {
                    UpdateDisDeliveryDetailIn updateDisDeliveryDetailIn = new UpdateDisDeliveryDetailIn();
                    updateDisDeliveryDetailIn.setId(disDeliveryDetail.getId());
                    updateDisDeliveryDetailIn.setDeliveryQuantity(disDeliveryDetail.getDistributionQuantity());
                    return updateDisDeliveryDetailIn;
                }).collect(Collectors.toList());
                ordDisDeliveryService.updateDisDeliveryInfo(ordDisDelivery, updateDisDeliveryDetailInList, loginBizOrgCode);
            } catch (Exception e) {
                log.error("配销单{}发货异常", ordDisDelivery.getDeliveryOrderNo(), e);
                errorJoiner.add("配销单" + ordDisDelivery.getDeliveryOrderNo() + "发货异常");
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.success(errorJoiner.toString());
        }
        return Response.success();
    }


    /**
     * 保存配销单信息
     *
     * @param ordDisDeliveryIn
     * @return
     */
    @ApiOperation(value = "新增配销单信息", notes = "新增配销单信息")
    @PostMapping("/save")
    public Response<Long> save(@RequestBody OrdDisDeliveryIn ordDisDeliveryIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDisDeliveryIn.getStockCode(), loginBizOrgCode, "创建配货单");
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(ordDisDeliveryIn.getStoreCode(), loginBizOrgCode);
        ordDisDeliveryIn.setBizOrgCode(channelBizOrgCode);
        ordDisDeliveryIn.setSourceCode(DeliveryOrderSourceCodeEnum.MANUAL.getType());
        ordDisDeliveryIn.setAuditType(SalvageAuditTypeEnum.WAIT_MANUAL_AUDIT.getCode());
        List<OrdDisDeliveryDetail> detailList = ordDisDeliveryIn.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            return Response.error("请至少录入一条明细");
        }
        DeliveryIn deliveryIn = new DeliveryIn();
        deliveryIn.setStoreCode(ordDisDeliveryIn.getStoreCode());
        deliveryIn.setBizOrgCode(channelBizOrgCode);
        deliveryIn.setStockCode(ordDisDeliveryIn.getStockCode());
        deliveryIn.setWrhCode(ordDisDeliveryIn.getWrhCode());
        deliveryIn.setDistributionType(DistributionWaysEnum.getNameByType(ordDisDeliveryIn.getDistributionType()));
        detailList.forEach(item -> {
            deliveryIn.setGoodsCode(item.getGoodsCode());
            OrdDisDeliveryDetailOut detail = ordDisDeliveryService.checkOrderGoods(deliveryIn, stockInfoOut.getBizOrgCode());
            item.setOrderUnitPrice(detail.getDistributionUnitPrice());
            item.setDistributionUnitPrice(item.getOrderUnitPrice());
            item.setOrderAmount(item.getOrderUnitPrice().multiply(item.getOrderQuantity()));
            item.setStoreStockPrice(detail.getStoreStockPrice());
            item.setWrhPrice(detail.getWrhPrice());
            item.setInvoiceType(detail.getInvoiceType());
        });
        return Response.data(ordDisDeliveryService.saveOrUpdate(ordDisDeliveryIn));
    }

    /**
     * 配销单明细导入
     *
     * @param
     * @return
     */
    @ApiOperation(value = "配销单明细导入", notes = "配销单明细导入")
    @PostMapping("/importDeliveryDetail")
    public Response<List<DisDeliveryOrderDetailsOut>> importDeliveryDetail(@RequestBody ImportDeliveryIn importDeliveryIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(importDeliveryIn.getStockCode(), loginBizOrgCode, "配销单明细导入");
        String storeChannelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(importDeliveryIn.getStoreCode(), loginBizOrgCode);
        importDeliveryIn.setBizOrgCode(storeChannelBizOrgCode);
        return ordDisDeliveryService.importDeliveryDetail(importDeliveryIn, stockInfoOut.getBizOrgCode());
    }


    /**
     * 运营端分页查询配销单
     *
     * @param deliveryOrderIn
     * @return
     */
    @ApiOperation(value = "运营端分页查询配销单", notes = "分页查询配销单", httpMethod = "POST")
    @PostMapping("/findDeliveryOrdersByPage")
    public Response<Page<DisDeliveryOrderOut>> findDeliveryOrdersByPage(@RequestBody DisDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        deliveryOrderIn.setBizOrgCode(loginBizOrgCode);
        Page<DisDeliveryOrderOut> deliveryOrderOutPage = ordDisDeliveryService.findDeliveryOrdersByPage(deliveryOrderIn);
        return Response.data(deliveryOrderOutPage);
    }

    /**
     * 运营端配销单详情
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    @ApiOperation(value = "运营端配销单详情", notes = "运营端配销单详情", httpMethod = "POST")
    @PostMapping("/findDeliveryOrderDetailForPage")
    public Response<Page<DisDeliveryOrderDetailsOut>> findDeliveryOrderDetailForPage(@RequestBody DisDeliveryOrderDetailsIn deliveryOrderDetailsIn) {
        Page<DisDeliveryOrderDetailsOut> resultPage = ordDisDeliveryService.findDeliveryOrderDetailForPage(deliveryOrderDetailsIn, UserUtil.getBizOrgCode());
        return Response.data(resultPage);
    }

    /**
     * 运营端查询配销单表头
     *
     * @param deliveryOrderId
     * @return
     */
    @ApiOperation(value = "运营端查询配销单表头", notes = "运营端查询配销单表头", httpMethod = "GET")
    @GetMapping("/getBackHeaderDeliveryOrderOutByDeliveryOrderId")
    public Response<DisDeliveryOrderOut> getDeliveryOrderOutByDeliveryOrderId(@RequestParam Long deliveryOrderId) {
        if (null == deliveryOrderId) {
            return Response.error("配货单主键不能为空");
        }
        DisDeliveryOrderOut disDeliveryOrderOut = ordDisDeliveryService.getDeliveryOrderOutByDeliveryOrderId(deliveryOrderId);
        return Response.data(disDeliveryOrderOut);
    }

    @ApiOperation(value = "批量获取需要打印的配销单", notes = "批量获取需要打印的配销单")
    @PostMapping("/findPrintDataByIds")
    public Response<List<DisDeliveryOrderPrintOut>> findPrintDataByIds(@RequestBody List<Long> ids) {
        List<DisDeliveryOrderPrintOut> disDeliveryOrderPrintOuts = ordDisDeliveryService.findPrintDataByIds(ids);
        return Response.data(disDeliveryOrderPrintOuts);
    }

    /**
     * 导出配销单明细列表
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    @ApiOperation(value = "导出配销单明细列表", notes = "导出配销单明细列表")
    @PostMapping("/exportDeliveryOrderDetails")
    public Response<String> exportDeliveryOrderDetails(@RequestBody @Valid DisDeliveryOrderDetailsIn deliveryOrderDetailsIn) {
        log.info("导出配销单明细入参是--{}", deliveryOrderDetailsIn);
        log.info("开始执行/ord/ordDisDelivery/exportDeliveryOrderDetails");
        String fileUrl = ordDisDeliveryService.exportDeliveryOrderDetails(deliveryOrderDetailsIn, UserUtil.getBizOrgCode());
        log.info("结束执行/ord/ordDisDelivery/exportDeliveryOrderDetails");
        log.info("导出配销单明细列表返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    /**
     * 导出配销单列表
     *
     * @param deliveryOrderIn
     * @return
     */
    @ApiOperation(value = "导出配销单列表", notes = "导出配销单列表")
    @PostMapping("/exportDeliveryOrder")
    public Response<String> exportDeliveryOrder(@RequestBody DisDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        deliveryOrderIn.setBizOrgCode(loginBizOrgCode);
        log.info("导出配销单列表入参是--{}", deliveryOrderIn);
        log.info("开始执行/ord/ordDisDelivery/exportDeliveryOrder");
        String fileUrl = ordDisDeliveryService.exportDeliveryOrder(deliveryOrderIn);
        log.info("结束执行/ord/ordDisDelivery/exportDeliveryOrder");
        log.info("导出配销单列表返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    /**
     * 导出多个配销单明细
     *
     * @param deliveryOrderIn
     * @return
     */
    @ApiOperation(value = "导出多个配销单明细", notes = "导出多个配销单明细")
    @PostMapping("/exportDeliveryOrderDetailByOrder")
    public Response<String> exportDeliveryOrderDetailByOrder(@RequestBody DisDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        deliveryOrderIn.setBizOrgCode(loginBizOrgCode);
        log.info("导出多个配销单明细入参是--{}", deliveryOrderIn);
        log.info("开始执行/ord/ordDisDelivery/exportDeliveryOrderDetailByOrder");
        String fileUrl = ordDisDeliveryService.exportDeliveryOrderDetailByOrder(deliveryOrderIn);
        log.info("结束执行/ord/ordDisDelivery/exportDeliveryOrderDetailByOrder");
        log.info("导出配销单明细返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    /**
     * 运营端查询配货单收货信息
     *
     * @param disDeliveryOrderId
     * @return
     */
    @ApiOperation(value = "运营端查询配货单收货信息", notes = "运营端查询配货单收货信息")
    @GetMapping("/getTakeDeliveryInfo")
    public Response<TakeDeliveryInfoOut> getTakeDeliveryInfo(@RequestParam Long disDeliveryOrderId) {
        if (Objects.isNull(disDeliveryOrderId)) {
            return Response.error("配货单主键不能为空");
        }
        TakeDeliveryInfoOut takeDeliveryInfoOut = ordDisDeliveryService.getTakeDeliveryInfoByDeliveryOrderId(disDeliveryOrderId);
        return Response.data(takeDeliveryInfoOut);
    }

    /**
     * 收货
     *
     * @param takeDisDeliveryOrderIn
     * @return
     */
    @ApiOperation(value = "收货", notes = "收货")
    @PostMapping("/takeDisDelivery")
    public Response<String> takeDisDelivery(@RequestBody @Valid TakeDisDeliveryOrderIn takeDisDeliveryOrderIn) {
        OrdDisDelivery disDelivery = ordDisDeliveryService.selectByPrimaryKey(takeDisDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(disDelivery)) {
            return Response.error("配销单不存在");
        }
        if (!DeliveryOrderEnum.SHIPPED.getKey().equals(disDelivery.getDeliveryStatusCode())) {
            return Response.error("配销单已发货才能收货");
        }
        // 判断收货进度
        OrdDisDeliveryOrderSigning deliveryOrderSigning = ordDisDeliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(disDelivery.getId(), disDelivery.getBizOrgCode());
        if (Objects.nonNull(deliveryOrderSigning) && !DeliveryOrderReceiveProgressEnum.ONGOING.getKey().equals(disDelivery.getReceiveProgress())) {
            return Response.error("配销单已有人正在收货");
        }
        if (NumberUtils.INTEGER_ONE.equals(disDelivery.getIsReversal()) || NumberUtils.INTEGER_ONE.equals(disDelivery.getIsReversalOrder())) {
            return Response.error("配销单已冲销或是冲销单，不可收货");
        }
        String key = DisSystemConstant.CHECK_DIS_ORDER_CYCLE_REPEAT_TAKE + disDelivery.getBizOrgCode() +
                SystemConstant.COLON + disDelivery.getStoreCode() + SystemConstant.WAIT + disDelivery.getDeliveryOrderNo();
        if (!redisService.setIfAbsent(key, disDelivery.getDeliveryOrderNo(), 5L, TimeUnit.MINUTES)) {
            return Response.error("门店" + disDelivery.getStoreCode() + "短时间内异常重复收货配货单" + disDelivery.getDeliveryOrderNo() + "，故判为无效提交！");
        }
        StockInfoOut stockInfoOut = stockServer.getTransInfo(disDelivery.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            return Response.error("无此仓位信息");
        }
        takeDisDeliveryOrderIn.setLoginUsername(UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : ("【" + UserUtil.getJobNumber() + "】")));
        try {
            takeDisDeliveryHandle.takeDelivery(takeDisDeliveryOrderIn, disDelivery, stockInfoOut);
        } finally {
            redisService.del(key);
        }
        return Response.success("收货成功");
    }

    /**
     * 冲销配销单
     *
     * @return
     */
    @ApiOperation(value = "冲销配销单", notes = "冲销配销单")
    @PostMapping("/charge")
    public Response chargeDisDelivery(@RequestBody @Valid ChargeDisDeliveryOrderIn chargeDisDeliveryOrderIn) {
        chargeDisDeliveryOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        String username = UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】");
        Response response = ordDisDeliveryService.chargeDeliveryOrder(chargeDisDeliveryOrderIn, username);
        if (NumberUtil.INTEGER_ZERO.equals(response.getData())) {
            return Response.error("冲销失败");
        }
        return Response.success("冲销成功");
    }

    /**
     * 校验商品信息
     *
     * @param deliveryIn
     * @return
     */
    @ApiOperation(value = "校验商品信息", notes = "", httpMethod = "GET")
    @GetMapping("/checkOrderGoods")
    public Response<OrdDisDeliveryDetailOut> checkOrderGoods(@Valid DeliveryIn deliveryIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(deliveryIn.getStockCode(), loginBizOrgCode, "校验商品信息");
        deliveryIn.setBizOrgCode(loginBizOrgCode);
        OrdDisDeliveryDetailOut goodsOut = ordDisDeliveryService.checkOrderGoods(deliveryIn, stockInfoOut.getBizOrgCode());
        return Response.data(goodsOut);
    }

    /**
     * 运营端查询配货单金额数量汇总
     *
     * @param
     * @return
     */
    @ApiOperation(value = "运营端查询配货单金额数量汇总", notes = "运营端查询配货单金额数量汇总", httpMethod = "POST")
    @PostMapping("/getDeliveryOrderTotal")
    public Response<DisDeliveryOrderOut> getDeliveryOrderTotal(@RequestBody DisDeliveryOrderIn deliveryOrderIn) {
        //业务组织
        deliveryOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(ordDisDeliveryService.getDeliveryOrderTotal(deliveryOrderIn));
    }

    @ApiOperation(value = "查询配销单列表(库存盘点)", notes = "查询配销单列表(库存盘点)", httpMethod = "GET")
    @GetMapping("/findDisDeliveryOrder")
    public Response<List<DisDeliveryOrderOut>> findDisDeliveryOrder(DisDeliveryOrderIn deliveryOrderIn) {
        if (StringUtils.isBlank(deliveryOrderIn.getBizOrgCode())) {
            deliveryOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        List<DisDeliveryOrderOut> disDeliveryOrderOutList = ordDisDeliveryService.findDisDeliveryOrder(deliveryOrderIn);
        return Response.data(disDeliveryOrderOutList);
    }

    @ApiOperation(value = "提交收货缓存数据", notes = "提交收货缓存数据", httpMethod = "POST")
    @PostMapping("/submitTakeDisDeliveryInfoToCache")
    public Response<String> submitTakeDisDeliveryInfoToCache(@RequestBody @Valid CacheTakeDisDeliveryOrderIn cacheTakeDeliveryOrderIn) {
        return takeDisDeliveryHandle.submitTakeDisDeliveryInfoToCache(cacheTakeDeliveryOrderIn);
    }

    @ApiOperation(value = "将配货单收货进度更新至收货中", notes = "将配货单收货进度更新至收货中", httpMethod = "POST")
    @PostMapping("/updateDisDeliveryOrderTakeOngoing")
    public Response<String> updateDeliveryOrderTakeOngoing(@RequestBody @Valid UpdateDisDeliveryOrderOngoingIn updateDeliveryOrderOngoingIn, HttpServletRequest request) {
        return takeDisDeliveryHandle.updateDisDeliveryOrderTakeOngoing(updateDeliveryOrderOngoingIn, UserUtil.getUserName(request));
    }

    @ApiOperation(value = "重置指定收货", notes = "重置指定收货", httpMethod = "POST")
    @PostMapping("/resetTakeDisDelivery")
    public Response<String> resetTakeDisDelivery(@RequestBody @Valid ResetTakeDisDeliveryIn resetTakeDeliveryIn, HttpServletRequest request) {
        OrdDisDelivery deliveryOrder = ordDisDeliveryService.selectByPrimaryKey(resetTakeDeliveryIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配货单");
        }
        ordDisDeliveryService.updateResetTakeDisDeliveryByDeliveryOrderId(resetTakeDeliveryIn.getDeliveryOrderId(), resetTakeDeliveryIn.getBizOrgCode(), UserUtil.getUserName(request));
        return Response.success();
    }

    @ApiOperation(value = "清除收货缓存数据", notes = "清除收货缓存数据", httpMethod = "POST")
    @PostMapping("/removeTakeDisDeliveryCacheData")
    public Response<String> removeTakeDisDeliveryCacheData(@RequestBody @Valid ResetTakeDisDeliveryIn resetTakeDeliveryIn) {
        takeDisDeliveryHandle.removeTakeDeliveryCacheData(resetTakeDeliveryIn.getDeliveryOrderId());
        return Response.success();
    }


    @ApiOperation(value = "获取待收货商品列表", notes = "获取待收货商品列表", httpMethod = "GET")
    @GetMapping("/getWaitingForDisDeliveryInfoOut")
    public Response<WaitingForDisDeliveryInfoOut> getWaitingForDisDeliveryInfoOut(WaitingForDisDeliveryGoodsIn waitingForDeliveryGoodsIn) {
        WaitingForDisDeliveryInfoOut waitingForDisDeliveryInfoOut = ordDisDeliveryDetailService.getTakeDisDeliveryInfoOut(waitingForDeliveryGoodsIn);
        return Response.data(waitingForDisDeliveryInfoOut);
    }

    @PostMapping("/disPurchaseOrderToErp")
    @ApiOperation(value = "采购回传erp", notes = "采购回传erp")
    public void disPurchaseOrderToErp(@RequestBody JSONArray json) {
        List<TransferNoticePurchaseVO> transferNoticePurchaseVOList = JSONArray.parseArray(json.toJSONString(), TransferNoticePurchaseVO.class);
        asyncTaskItemService.disPurchaseOrderToErp(transferNoticePurchaseVOList);
    }

    @PostMapping("/unificationOrderCallBack")
    @ApiOperation(value = "DTS回传配货单发货数", notes = "DTS回传配货单发货数")
    public void unificationOrderCallBack(@RequestBody JSONObject json) {
        UnificationBillVO unificationBillVO = JSONObject.parseObject(json.toJSONString(), UnificationBillVO.class);
        asyncTaskItemService.unificationOrderCallBack(unificationBillVO);
    }

//    @PostMapping("/disDeliveryToDifference")
//    @ApiOperation(value = "创建差异单", notes = "创建差异单")
//    public void disDeliveryToDifference(@RequestBody JSONObject json) {
//        SaveDifferenceIn saveDifferenceIn = JSONObject.parseObject(json.toJSONString(), SaveDifferenceIn.class);
//        asyncTaskItemService.disDeliveryToDifference(saveDifferenceIn);
//    }

    /**
     * 批量作废配销单
     *
     * @param ids
     * @return
     */
    @ApiOperation(value = "批量作废配销单并返款", notes = "批量作废配销单并返款")
    @GetMapping("/batchInvalidDisDeliverOrderAndReFund")
    public Response batchInvalidDisDeliverOrderAndReFund(@RequestParam String ids) {
        int invalid = 0;
        for (String id : ids.split(SystemConstant.COMMA)) {
            invalid += ordDisDeliveryService.invalidDisDeliverOrder(Long.parseLong(id), "管理员手动作废", true);
        }
        if (invalid > 0) {
            return Response.success("作废成功");
        }
        return Response.error("作废失败");
    }

    /**
     * 批量作废配销单
     *
     * @param idList
     * @return
     */
    @ApiOperation(value = "批量作废配销单", notes = "批量作废配销单")
    @PostMapping("/batchInvalidDisDeliverOrder")
    public Response batchInvalidDisDeliverOrder(@RequestBody List<Long> idList) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        idList.forEach(id -> {
            OrdDisDelivery dirDelivery = ordDisDeliveryService.selectByPrimaryKey(id);
            if (Objects.isNull(dirDelivery)) {
                log.error("配销单ID{}不存在", id);
                return;
            }
            try {
                ordDisDeliveryService.invalidDisDeliverOrder(id, "管理员手动作废", true);
            } catch (NumberFormatException e) {
                log.error("配销单{}作废异常", dirDelivery.getDeliveryOrderNo(), e);
                errorJoiner.add("配销单:" + dirDelivery.getDeliveryOrderNo() + "作废失败");
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.success(errorJoiner.toString());
        }
        return Response.success();
    }

//    @ApiOperation(value = "配销单批量新增（导入）", notes = "配销单批量新增（导入）")
//    @GetMapping("/importBatchSave1")
//    public Response<List<OrdDisDeliveryOut>> importBatchSave1(@RequestParam String fileId) {
//        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
//        if (bytes == null) {
//            return Response.error("无效的Excel模板");
//        }
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
//        DisOverallDistributionAsyncListener listener = new DisOverallDistributionAsyncListener();
//        ExcelReader excelReader = EasyExcel.read(inputStream, listener).headRowNumber(0).build();
//        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
//        excelReader.read(readSheet).finish();
//
//
//        return Response.success();
////        return ordDisDeliveryService.importBatchSave(fileId, bizOrgCode);
//    }

//    @PostMapping("/zKConfirmCallBack")
//    @ApiOperation(value = "中科单据确认回传", notes = "中科单据确认回传")
//    public void zKConfirmCallBack(@RequestBody JSONObject json) {
//        asyncTaskItemService.zKConfirmCallBack(json.toJSONString());
//    }

    @PostMapping("/zKCreateWholesaleShipment")
    @ApiOperation(value = "中科单据确认回传", notes = "中科单据确认回传")
    public void zKCreateWholesaleShipment(@RequestBody JSONObject json) {
        TaskZKWholesaleShipmentSaveIn taskZKWholesaleShipmentSaveIn = JSONObject.parseObject(json.toJSONString(), TaskZKWholesaleShipmentSaveIn.class);
        asyncTaskItemService.zKCreateWholesaleShipment(taskZKWholesaleShipmentSaveIn);
    }


    @PostMapping("/zKCreateWholesaleReturn")
    @ApiOperation(value = "中科单据确认回传", notes = "中科单据确认回传")
    public void zKCreateWholesaleReturn(@RequestBody JSONObject json) {
        TaskZKWholesaleReturnSaveIn taskZKWholesaleReturnSaveIn = JSONObject.parseObject(json.toJSONString(), TaskZKWholesaleReturnSaveIn.class);
        asyncTaskItemService.zKCreateWholesaleReturn(taskZKWholesaleReturnSaveIn);
    }


//    @PostMapping("/zkAuditCallBack")
//    @ApiOperation(value = "中科退货单审核回传", notes = "中科退货单审核回传")
//    public void zkAuditCallBack(@RequestBody JSONObject json) {
//        asyncTaskItemService.zKAuditCallBack(json.toJSONString());
//    }

    @ApiOperation(value = "异步导入配销单（列表）", notes = "异步导入配销单（列表）")
    @GetMapping("/asyncImportDelivery")
    public Response<String> asyncImportDelivery(@RequestParam String fileId) {
        return ordDisDeliveryService.asyncImportDelivery(fileId, UserUtil.getUserName(), UserUtil.getBizOrgCode());
    }

    @ApiOperation(value = "异步导入统筹配销单（列表）", notes = "异步导入统筹配销单（列表.）")
    @GetMapping("/asyncImportOverallDelivery")
    public Response<String> asyncImportOverallDelivery(DisOverallDeliveryAsyncImportIn disOverallDeliveryAsyncImportIn) {
        return ordDisOverallDeliveryService.asyncImportOverallDelivery(disOverallDeliveryAsyncImportIn, UserUtil.getUserName(), UserUtil.getBizOrgCode());
    }

    @ApiOperation(value = "运营端分页查询配销单(为退货单)", notes = "运营端分页查询配销单(为退货单)", httpMethod = "POST")
    @PostMapping("/findDeliveryOrderForReturn")
    public Response<Page<QueryDisDeliveryForReturnOut>> findDeliveryOrderForReturn(@RequestBody DisDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        deliveryOrderIn.setBizOrgCode(loginBizOrgCode);
        Page<QueryDisDeliveryForReturnOut> deliveryOrderOutPage = ordDisDeliveryService.findDeliveryOrderForReturn(deliveryOrderIn);
        return Response.data(deliveryOrderOutPage);
    }

    @ApiOperation(value = "APP退货单申请退货根据配货单查询配货信息(为APP退货申请)", notes = "APP退货单申请退货根据配货单查询配货信息(为APP退货申请)", httpMethod = "GET")
    @GetMapping("/findDeliveryInfoForReturn")
    public Response<List<DisStoreDeliveryNoInfoForAppOut>> findDeliveryInfoForReturn(@RequestParam String storeCode) {
        List<DisStoreDeliveryNoInfoForAppOut> resultList = ordDisDeliveryService.findStoreDeliveryInfoList(storeCode);
        if (CollectionUtils.isEmpty(resultList)) {
            return Response.error("没有匹配的配销单");
        }
        return Response.data(resultList);
    }

    @ApiOperation(value = "运营端根据配销单号查询配销单明细", notes = "运营端根据配销单号查询配销单明细", httpMethod = "GET")
    @GetMapping("/getDeliveryOrderOutByNo")
    public Response<List<OrdDisDeliveryDetail>> getDeliveryOrderOutByNo(@RequestParam String deliveryOrderNo) {
        OrdDisDeliveryOut ordDisDeliveryOut = ordDisDeliveryService.getDeliveryOrderOutByNo(deliveryOrderNo);
        if (Objects.isNull(ordDisDeliveryOut)) {
            return Response.error("配销单不存在");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDisDeliveryOut.getIsReversal())) {
            return Response.error("配销单已红冲");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDisDeliveryOut.getIsReversalOrder())) {
            return Response.error("配销单为红冲单");
        }
        if (!DeliveryOrderEnum.RECEIVED.getKey().equals(ordDisDeliveryOut.getDeliveryStatusCode())) {
            return Response.error("必须是已收货的配销单");
        }
        return Response.data(ordDisDeliveryOut.getDetailList());
    }
}
