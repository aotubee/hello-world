package com.edc.erp.directly.dirdeliveryorder.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.DeliveryOrderSourceCodeEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.SalvageAuditTypeEnum;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderSigning;
import com.edc.erp.directly.dirdeliveryorder.model.in.*;
import com.edc.erp.directly.dirdeliveryorder.model.out.*;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDetailService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderSigningService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirOverallDeliveryService;
import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.directly.enumeration.DeliveryOrderReceiveProgressEnum;
import com.edc.erp.directly.handle.TakeDirDeliveryHandle;
import com.edc.erp.directly.orderscheduing.service.DirAsyncTaskItemService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.sdk.dts.model.order.vo.DifferenceBillVO;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * <p>
 * 配货单表 前端控制器
 * </p>
 *
 * @author weichao
 * @since 2022-11-10 14:45:33
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirDelivery")
@Api(value = "ordDirDelivery", tags = "配货单表模块")
public class OrdDirDeliveryController {

    private final OrdDirDeliveryService ordDirDeliveryService;

    private final TakeDirDeliveryHandle takeDirDeliveryHandle;

    private final OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    private final DirAsyncTaskItemService dirAsyncTaskItemService;

    private final RedisService redisService;

    private final OrdDirOverallDeliveryService ordDirOverallDeliveryService;

    private final OrdDirDeliveryOrderSigningService ordDirDeliveryOrderSigningService;

    private final StoreChannelHandle storeChannelHandle;

    private final StockServer stockServer;

    /**
     * 作废配货单
     *
     * @param ordDirDelivery
     * @return
     */
    @ApiOperation(value = "作废配货单", notes = "作废配货单")
    @PostMapping("/invalidDirDeliverOrder")
    public Response invalidDisDeliverOrder(@RequestBody OrdDirDelivery ordDirDelivery) {
        String userName = UserUtil.getUserName();
        ordDirDeliveryService.invalidDirDeliverOrder(ordDirDelivery.getId(), userName);
        return Response.success();
    }

    /**
     * 审核配货单信息
     *
     * @return
     */
    @ApiOperation(value = "审核配货单", notes = "审核配货单")
    @PostMapping("/audit")
    public Response<OrdDirDeliveryOut> audit(@RequestBody OrdDirDeliveryIn ordDirDeliveryIn) {
//        if (StringUtils.isNotBlank(ordDirDeliveryIn.getBizOrgCode())) {
//            if (!UserUtil.getBizOrgCode().equals(ordDirDeliveryIn.getBizOrgCode())) {
//                return Response.error("无此单据审核权限");
//            }
//        } else {
        ordDirDeliveryIn.setBizOrgCode(UserUtil.getBizOrgCode());
//        }
        OrdDirDelivery ordDirDelivery = ordDirDeliveryService.selectByPrimaryKey(ordDirDeliveryIn.getId());
        if (Objects.isNull(ordDirDelivery)) {
            return Response.error("配货单不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirDelivery.getStockCode(), UserUtil.getBizOrgCode(), "审核配货单");
        return ordDirDeliveryService.audit(stockInfoOut, ordDirDelivery);
    }

    /**
     * 批量审核配货单
     *
     * @return
     */
    @ApiOperation(value = "批量审核配货单", notes = "批量审核配货单")
    @PostMapping("/bachAudit")
    public Response bachAudit(@RequestBody List<OrdDirDeliveryIn> ordDirDeliveryIns) {
        if (CollectionUtils.isEmpty(ordDirDeliveryIns)) {
            return Response.error("审核数据不能为空");
        }
        return ordDirDeliveryService.bachAudit(UserUtil.getBizOrgCode(), UserUtil.getUserName(), ordDirDeliveryIns);
    }

    /**
     * 手动触发发货
     *
     * @return
     */
    @ApiOperation(value = "发货", notes = "发货")
    @PostMapping("/shipments")
    public Response<OrdDirDeliveryOut> shipments(@RequestBody UpdateDirDeliveryInfoIn updateDirDeliveryInfoIn) {
        OrdDirDelivery ordDirDelivery = ordDirDeliveryService.selectByPrimaryKey(updateDirDeliveryInfoIn.getId());
        if (Objects.isNull(ordDirDelivery)) {
            throw new BusinessException("此配货单不存在！");
        }
        if (!DeliveryOrderEnum.APPROVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
            throw new BusinessException("此配货单状态为，" + DeliveryOrderEnum.getValueByKey(ordDirDelivery.getDeliveryStatusCode()) + "不可修改");
        }
        ordDirDelivery.setUpdater(UserUtil.getUserName());
        ordDirDeliveryService.updateDirDeliveryInfo(ordDirDelivery, updateDirDeliveryInfoIn.getUpdateDirDeliveryDetailInList(), UserUtil.getBizOrgCode());
        return Response.success();
    }

    @ApiOperation(value = "批量发货", notes = "批量发货")
    @PostMapping("/batchShipments")
    public Response<String> batchShipments(@RequestBody List<Long> idList) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        idList.forEach(id -> {
            OrdDirDelivery ordDirDelivery = ordDirDeliveryService.selectByPrimaryKey(id);
            try {
                if (Objects.isNull(ordDirDelivery)) {
                    errorJoiner.add("配货单id：" + id + "不存在");
                    return;
                }
                if (!DeliveryOrderEnum.APPROVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
                    errorJoiner.add("配货单" + ordDirDelivery.getDeliveryOrderNo() + "状态为，" + DeliveryOrderEnum.getValueByKey(ordDirDelivery.getDeliveryStatusCode()) + "不可修改");
                    return;
                }
                ordDirDelivery.setUpdater(UserUtil.getUserName());
                List<OrdDirDeliveryDetail> dirDeliveryDetailList = ordDirDeliveryDetailService.findDeliveryOrderDetails(id);
                List<UpdateDirDeliveryDetailIn> updateDirDeliveryDetailInList = dirDeliveryDetailList.stream().map(dirDeliveryDetail -> {
                    UpdateDirDeliveryDetailIn updateDirDeliveryDetailIn = new UpdateDirDeliveryDetailIn();
                    updateDirDeliveryDetailIn.setId(dirDeliveryDetail.getId());
                    updateDirDeliveryDetailIn.setDeliveryQuantity(dirDeliveryDetail.getDistributionQuantity());
                    return updateDirDeliveryDetailIn;
                }).collect(Collectors.toList());
                ordDirDeliveryService.updateDirDeliveryInfo(ordDirDelivery, updateDirDeliveryDetailInList, loginBizOrgCode);
            } catch (Exception e) {
                log.error("配货单{}发货异常", ordDirDelivery.getDeliveryOrderNo(), e);
                errorJoiner.add("配货单" + ordDirDelivery.getDeliveryOrderNo() + "发货异常");
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.error(errorJoiner.toString());
        }
        return Response.success();
    }


    /**
     * 保存配货单信息
     *
     * @param ordDirDeliveryIn
     * @return
     */
    @ApiOperation(value = "保存配货单信息", notes = "保存配货单信息")
    @PostMapping("/save")
    public Response<Long> save(@RequestBody OrdDirDeliveryIn ordDirDeliveryIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirDeliveryIn.getStockCode(), loginBizOrgCode, "创建配货单");
        String channelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(ordDirDeliveryIn.getStoreCode(), loginBizOrgCode);
        ordDirDeliveryIn.setBizOrgCode(channelBizOrgCode);
        ordDirDeliveryIn.setSourceCode(DeliveryOrderSourceCodeEnum.MANUAL.getType());
        ordDirDeliveryIn.setAuditType(SalvageAuditTypeEnum.WAIT_MANUAL_AUDIT.getCode());
        List<OrdDirDeliveryDetail> detailList = ordDirDeliveryIn.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            return Response.error("请至少录入一条明细");
        }
        DeliveryIn deliveryIn = new DeliveryIn();
        deliveryIn.setStoreCode(ordDirDeliveryIn.getStoreCode());
        deliveryIn.setBizOrgCode(channelBizOrgCode);
        deliveryIn.setStockCode(ordDirDeliveryIn.getStockCode());
        deliveryIn.setWrhCode(ordDirDeliveryIn.getWrhCode());
        deliveryIn.setDistributionType(DistributionWaysEnum.getNameByType(ordDirDeliveryIn.getDistributionType()));
        detailList.forEach(item -> {
            deliveryIn.setGoodsCode(item.getGoodsCode());
            OrdDirDeliveryDetailOut detail = ordDirDeliveryService.checkOrderGoods(deliveryIn, stockInfoOut.getBizOrgCode());
            item.setOrderUnitPrice(detail.getDistributionUnitPrice());
            item.setDistributionUnitPrice(item.getOrderUnitPrice());
            item.setOrderAmount(item.getOrderUnitPrice().multiply(item.getOrderQuantity()).setScale(NumberUtils.INTEGER_TWO, RoundingMode.UP));
            item.setStoreStockPrice(detail.getStoreStockPrice());
            item.setWrhPrice(detail.getWrhPrice());
            item.setInvoiceType(detail.getInvoiceType());
        });
        return Response.data(ordDirDeliveryService.saveOrUpdate(ordDirDeliveryIn));
    }

    /**
     * 配货单批量新增（导入）
     *
     * @param
     * @return
     */
//    @ApiOperation(value = "配货单批量新增（导入）", notes = "配货单批量新增（导入）")
//    @GetMapping("/importBatchSave")
//    public Response<List<Long>> importBatchSave(@RequestParam String fileId) {
//        String bizOrgCode = UserUtil.getBizOrgCode();
//        return ordDirDeliveryService.importBatchSave(fileId, bizOrgCode);
//    }

    /**
     * 配货单明细导入
     *
     * @param
     * @return
     */
    @ApiOperation(value = "配货单明细导入", notes = "配货单明细导入")
    @PostMapping("/importDeliveryDetail")
    public Response<List<DirDeliveryOrderDetailsOut>> importDeliveryDetail(@RequestBody ImportDeliveryIn importDeliveryIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(importDeliveryIn.getStockCode(), loginBizOrgCode, "配货单明细导入");
        String storeChannelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(importDeliveryIn.getStoreCode(), loginBizOrgCode);
        importDeliveryIn.setBizOrgCode(storeChannelBizOrgCode);
        if (StringUtils.isEmpty(importDeliveryIn.getStoreCode())) {
            throw new BusinessException("请输入门店信息");
        }
        if (StringUtils.isEmpty(importDeliveryIn.getStockCode())) {
            throw new BusinessException("请输入仓位信息");
        }
        if (StringUtils.isEmpty(importDeliveryIn.getWrhCode())) {
            throw new BusinessException("请输入仓储信息");
        }
        return ordDirDeliveryService.importDeliveryDetail(importDeliveryIn, stockInfoOut.getBizOrgCode());
    }

    /**
     * 运营端分页查询配货单
     *
     * @param deliveryOrderIn
     * @return
     */
    @ApiOperation(value = "运营端分页查询配货单", notes = "运营端分页查询配货单", httpMethod = "POST")
    @PostMapping("/findDeliveryOrdersByPage")
    public Response<Page<DirDeliveryOrderOut>> findDeliveryOrdersByPage(@RequestBody DirDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        deliveryOrderIn.setBizOrgCode(loginBizOrgCode);
        Page<DirDeliveryOrderOut> deliveryOrderOutPage = ordDirDeliveryService.findDeliveryOrdersByPage(deliveryOrderIn);
        return Response.data(deliveryOrderOutPage);
    }

    /**
     * 运营端配货单详情
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    @ApiOperation(value = "运营端配货单详情", notes = "运营端配货单详情", httpMethod = "POST")
    @PostMapping("/findDeliveryOrderDetailForPage")
    public Response<Page<DirDeliveryOrderDetailsOut>> findDeliveryOrderDetailForPage(@RequestBody DirDeliveryOrderDetailsIn deliveryOrderDetailsIn) {
        Page<DirDeliveryOrderDetailsOut> resultPage = ordDirDeliveryService.findDeliveryOrderDetailForPage(deliveryOrderDetailsIn, UserUtil.getBizOrgCode());
        return Response.data(resultPage);
    }

    /**
     * 运营端查询配货单表头
     *
     * @param deliveryOrderId
     * @return
     */
    @ApiOperation(value = "运营端查询配货单表头", notes = "运营端查询配货单表头", httpMethod = "GET")
    @GetMapping("/getBackHeaderByDeliveryOrderId")
    public Response<DirDeliveryOrderOut> getDeliveryOrderOutByDeliveryOrderId(@RequestParam Long deliveryOrderId) {
        if (null == deliveryOrderId) {
            return Response.error("配货单主键不能为空");
        }
        DirDeliveryOrderOut dirDeliveryOrderOut = ordDirDeliveryService.getDeliveryOrderOutByDeliveryOrderId(deliveryOrderId);
        return Response.data(dirDeliveryOrderOut);
    }

    @ApiOperation(value = "批量获取需要打印的配货单", notes = "批量获取需要打印的配货单")
    @PostMapping("/findPrintDataByIds")
    public Response<List<DirDeliveryOrderPrintOut>> findPrintDataByIds(@RequestBody List<Long> ids) {
        List<DirDeliveryOrderPrintOut> disDeliveryOrderPrintOuts = ordDirDeliveryService.findPrintDataByIds(ids);
        return Response.data(disDeliveryOrderPrintOuts);
    }

    /**
     * 校验商品信息
     *
     * @param deliveryIn
     * @return
     */
    @ApiOperation(value = "校验商品信息", notes = "", httpMethod = "GET")
    @GetMapping("/checkOrderGoods")
    public Response<OrdDirDeliveryDetailOut> checkOrderGoods(@Valid DeliveryIn deliveryIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(deliveryIn.getStockCode(), loginBizOrgCode, "校验商品信息");
        deliveryIn.setBizOrgCode(loginBizOrgCode);
        OrdDirDeliveryDetailOut goodsOut = ordDirDeliveryService.checkOrderGoods(deliveryIn, stockInfoOut.getBizOrgCode());
        return Response.data(goodsOut);
    }

    /**
     * 导出配货单明细列表
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    @ApiOperation(value = "导出配货单明细列表", notes = "导出配货单明细列表")
    @PostMapping("/exportDeliveryOrderDetails")
    public Response<String> exportDeliveryOrderDetails(@RequestBody @Valid DirDeliveryOrderDetailsIn deliveryOrderDetailsIn) {
        log.info("导出配货单明细入参是--{}", deliveryOrderDetailsIn);
        log.info("开始执行/ord/ordDirDelivery/exportDeliveryOrderDetails");
        String fileUrl = ordDirDeliveryService.exportDeliveryOrderDetails(deliveryOrderDetailsIn, UserUtil.getBizOrgCode());
        log.info("结束执行/ord/ordDirDelivery/exportDeliveryOrderDetails");
        log.info("导出配货单明细列表返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    /**
     * 导出配货单列表
     *
     * @param deliveryOrderIn
     * @return
     */
    @ApiOperation(value = "导出配货单列表", notes = "导出配货单列表")
    @PostMapping("/exportDeliveryOrder")
    public Response<String> exportDeliveryOrder(@RequestBody DirDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        deliveryOrderIn.setBizOrgCode(loginBizOrgCode);
        log.info("导出配货单列表入参是--{}", deliveryOrderIn);
        log.info("开始执行/ord/ordDirDelivery/exportDeliveryOrder");
        String fileUrl = ordDirDeliveryService.exportDeliveryOrder(deliveryOrderIn);
        log.info("结束执行/ord/ordDirDelivery/exportDeliveryOrder");
        log.info("导出配货单列表返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    /**
     * 导出多个配货单明细
     *
     * @param deliveryOrderIn
     * @return
     */
    @ApiOperation(value = "导出多个配货单明细", notes = "导出多个配货单明细")
    @PostMapping("/exportDeliveryOrderDetailByOrder")
    public Response<String> exportDeliveryOrderDetailByOrder(@RequestBody DirDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        deliveryOrderIn.setBizOrgCode(loginBizOrgCode);
        log.info("导出多个配货单明细入参是--{}", deliveryOrderIn);
        log.info("开始执行/ord/ordDirDelivery/exportDeliveryOrderDetailByOrder");
        String fileUrl = ordDirDeliveryService.exportDeliveryOrderDetailByOrder(deliveryOrderIn);
        log.info("结束执行/ord/ordDirDelivery/exportDeliveryOrderDetailByOrder");
        log.info("导出配货单明细返参是--{}", fileUrl);
        return Response.data(fileUrl);
    }

    /**
     * 运营端查询配货单收货信息
     *
     * @param dirDeliveryOrderId
     * @return
     */
    @ApiOperation(value = "运营端查询配货单收货信息", notes = "运营端查询配货单收货信息")
    @GetMapping("/getTakeDeliveryInfo")
    public Response<TakeDeliveryInfoOut> getTakeDeliveryInfo(@RequestParam Long dirDeliveryOrderId) {
        if (Objects.isNull(dirDeliveryOrderId)) {
            return Response.error("配货单主键不能为空");
        }
        TakeDeliveryInfoOut takeDeliveryInfoOut = ordDirDeliveryService.getTakeDeliveryInfoByDeliveryOrderId(dirDeliveryOrderId);
        return Response.data(takeDeliveryInfoOut);
    }

    /**
     * 收货
     *
     * @param takeDirDeliveryOrderIn
     * @return
     */
    @ApiOperation(value = "收货", notes = "收货")
    @PostMapping("/takeDirDelivery")
    public Response<String> takeDirDelivery(@RequestBody @Valid TakeDirDeliveryOrderIn takeDirDeliveryOrderIn) {
        OrdDirDelivery dirDelivery = ordDirDeliveryService.selectByPrimaryKey(takeDirDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(dirDelivery)) {
            return Response.error("配货单不存在");
        }
        if (!DeliveryOrderEnum.SHIPPED.getKey().equals(dirDelivery.getDeliveryStatusCode())) {
            return Response.error("配货单已发货才能收货");
        }
        StockInfoOut stockInfoOut = stockServer.getTransInfo(dirDelivery.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            return Response.error("无此仓位信息");
        }
        // 判断收货进度
        OrdDirDeliveryOrderSigning deliveryOrderSigning = ordDirDeliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(dirDelivery.getId(), dirDelivery.getBizOrgCode());
        if (Objects.nonNull(deliveryOrderSigning) && !DeliveryOrderReceiveProgressEnum.ONGOING.getKey().equals(dirDelivery.getReceiveProgress())) {
            return Response.error("配货单已有人正在收货");
        }
        if (NumberUtils.INTEGER_ONE.equals(dirDelivery.getIsReversal()) || NumberUtils.INTEGER_ONE.equals(dirDelivery.getIsReversalOrder())) {
            return Response.error("配货单已冲销或是冲销单，不可收货");
        }
        String key = DirSystemConstant.CHECK_DIR_ORDER_CYCLE_REPEAT_TAKE + dirDelivery.getBizOrgCode() +
                SystemConstant.COLON + dirDelivery.getStoreCode() + SystemConstant.WAIT + dirDelivery.getDeliveryOrderNo();
        if (!redisService.setIfAbsent(key, dirDelivery.getDeliveryOrderNo(), 5L, TimeUnit.MINUTES)) {
            return Response.error("门店" + dirDelivery.getStoreCode() + "短时间内异常重复收货配货单" + dirDelivery.getDeliveryOrderNo() + "，故判为无效提交！");
        }
        takeDirDeliveryOrderIn.setLoginUsername(UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】"));
        try {
            takeDirDeliveryHandle.takeDirDelivery(takeDirDeliveryOrderIn, dirDelivery, stockInfoOut);
        } finally {
            redisService.del(key);
        }
        return Response.success("收货成功");
    }

    /**
     * 冲销配货单
     *
     * @return
     */
    @ApiOperation(value = "冲销配货单", notes = "冲销配货单")
    @PostMapping("/charge")
    public Response chargeDirDeliveryOrder(@RequestBody @Valid ChargeDirDeliveryOrderIn chargeDirDeliveryOrderIn) {
        chargeDirDeliveryOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Response response = ordDirDeliveryService.chargeDeliveryOrder(chargeDirDeliveryOrderIn);
        if (!NumberUtil.INTEGER_ZERO.equals(response.getData())) {
            return Response.success("冲销成功");
        }
        return Response.error("冲销失败");
    }

    /**
     * 运营端查询配货单金额数量汇总
     *
     * @param
     * @return
     */
    @ApiOperation(value = "运营端查询配货单金额数量汇总", notes = "运营端查询配货单金额数量汇总", httpMethod = "POST")
    @PostMapping("/getDeliveryOrderTotal")
    public Response<DirDeliveryOrderOut> getDeliveryOrderTotal(@RequestBody DirDeliveryOrderIn deliveryOrderIn) {
        //业务组织
        deliveryOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(ordDirDeliveryService.getDeliveryOrderTotal(deliveryOrderIn));
    }

    @ApiOperation(value = "查询配货单列表(库存盘点)", notes = "查询配货单列表(库存盘点)", httpMethod = "GET")
    @GetMapping("/findDirDeliveryOrder")
    public Response<List<DirDeliveryOrderOut>> findDirDeliveryOrder(DirDeliveryOrderIn deliveryOrderIn) {
        if (StringUtils.isBlank(deliveryOrderIn.getBizOrgCode())) {
            deliveryOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        List<DirDeliveryOrderOut> dirDeliveryOrderOutList = ordDirDeliveryService.findDirDeliveryOrder(deliveryOrderIn);
        return Response.data(dirDeliveryOrderOutList);
    }

    @ApiOperation(value = "提交收货缓存数据", notes = "提交收货缓存数据", httpMethod = "POST")
    @PostMapping("/submitTakeDirDeliveryInfoToCache")
    public Response<String> submitTakeDirDeliveryInfoToCache(@RequestBody @Valid CacheTakeDirDeliveryOrderIn cacheTakeDeliveryOrderIn) {
        return takeDirDeliveryHandle.submitTakeDirDeliveryInfoToCache(cacheTakeDeliveryOrderIn);
    }

    @ApiOperation(value = "将配货单收货进度更新至收货中", notes = "将配货单收货进度更新至收货中", httpMethod = "POST")
    @PostMapping("/updateDirDeliveryOrderTakeOngoing")
    public Response<String> updateDirDeliveryOrderTakeOngoing(@RequestBody @Valid UpdateDirDeliveryOrderOngoingIn updateDeliveryOrderOngoingIn, HttpServletRequest request) {
        return takeDirDeliveryHandle.updateDirDeliveryOrderTakeOngoing(updateDeliveryOrderOngoingIn, UserUtil.getUserName(request));
    }

    @ApiOperation(value = "重置指定收货", notes = "重置指定收货", httpMethod = "POST")
    @PostMapping("/resetTakeDirDelivery")
    public Response<String> resetTakeDirDelivery(@RequestBody @Valid ResetTakeDirDeliveryIn resetTakeDeliveryIn, HttpServletRequest request) {
        OrdDirDelivery deliveryOrder = ordDirDeliveryService.selectByPrimaryKey(resetTakeDeliveryIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配货单");
        }
        ordDirDeliveryService.updateResetTakeDirDeliveryByDeliveryOrderId(resetTakeDeliveryIn.getDeliveryOrderId(), resetTakeDeliveryIn.getBizOrgCode(), UserUtil.getUserName(request));
        return Response.success();
    }

    @ApiOperation(value = "清除收货缓存数据", notes = "清除收货缓存数据", httpMethod = "POST")
    @PostMapping("/removeTakeDirDeliveryCacheData")
    public Response<String> removeTakeDirDeliveryCacheData(@RequestBody @Valid ResetTakeDirDeliveryIn resetTakeDeliveryIn) {
        takeDirDeliveryHandle.removeTakeDeliveryCacheData(resetTakeDeliveryIn.getDeliveryOrderId());
        return Response.success();
    }

    @ApiOperation(value = "获取待收货商品列表", notes = "获取待收货商品列表", httpMethod = "GET")
    @GetMapping("/getWaitingForDirDeliveryInfoOut")
    public Response<WaitingForDirDeliveryInfoOut> getWaitingForDirDeliveryInfoOut(WaitingForDirDeliveryGoodsIn waitingForDeliveryGoodsIn) {
        WaitingForDirDeliveryInfoOut waitingForDirDeliveryInfoOut = ordDirDeliveryDetailService.getTakeDirDeliveryInfoOut(waitingForDeliveryGoodsIn);
        return Response.data(waitingForDirDeliveryInfoOut);
    }

//    @PostMapping("/unificationOrderCallBack")
//    @ApiOperation(value = "DTS回传配货单发货数", notes = "DTS回传配货单发货数")
//    public void unificationOrderCallBack(@RequestBody JSONObject json) {
//        dirAsyncTaskItemService.unificationOrderCallBack(json.toJSONString());
//    }

    @PostMapping("/dirDeliveryToDifference")
    @ApiOperation(value = "直营配货单收货后生成直营配货差异单", notes = "直营配货单收货后生成直营配货差异单")
    public void dirDeliveryToDifference(@RequestBody JSONObject jsonObject) {
        SaveDifferenceIn saveDifferenceIn = JSON.parseObject(jsonObject.toJSONString(), SaveDifferenceIn.class);
        dirAsyncTaskItemService.dirDeliveryToDifference(saveDifferenceIn);
    }

//    @PostMapping("/dirPurchaseOrderToErp")
//    @ApiOperation(value = "直营配货单收货后生成直营配货差异单", notes = "直营配货单收货后生成直营配货差异单")
//    public void dirPurchaseOrderToErp(@RequestBody JSONArray json) {
//        dirAsyncTaskItemService.dirPurchaseOrderToErp(json.toJSONString());
//    }

    /**
     * 批量作废配货单
     *
     * @param idList
     * @return
     */
    @ApiOperation(value = "批量作废配货单", notes = "批量作废配货单")
    @PostMapping("/batchInvalidDirDeliverOrder")
    public Response batchInvalidDirDeliverOrder(@RequestBody List<Long> idList) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        idList.forEach(id -> {
            OrdDirDelivery dirDelivery = ordDirDeliveryService.selectByPrimaryKey(id);
            if (Objects.isNull(dirDelivery)) {
                log.error("配货单ID{}不存在", id);
                return;
            }
            try {
                ordDirDeliveryService.invalidDirDeliverOrder(id, "管理员手动作废");
            } catch (NumberFormatException e) {
                log.error("配货单{}作废异常", dirDelivery.getDeliveryOrderNo(), e);
                errorJoiner.add("配货单:" + dirDelivery.getDeliveryOrderNo() + "作废失败");
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.success(errorJoiner.toString());
        }
        return Response.success();
    }

    @ApiOperation(value = "异步导入配销单（列表）", notes = "异步导入配销单（列表）")
    @GetMapping("/asyncImportDelivery")
    public Response<String> asyncImportDelivery(@RequestParam String fileId) {
        return ordDirDeliveryService.asyncImportDelivery(fileId, UserUtil.getUserName(), UserUtil.getBizOrgCode());
    }

    @ApiOperation(value = "异步导入统筹配销单（列表）", notes = "异步导入统筹配销单（列表）")
    @GetMapping("/asyncImportOverallDelivery")
    public Response<String> asyncImportOverallDelivery(DirOverallDeliveryAsyncImportIn dirOverallDeliveryAsyncImportIn) {
        return ordDirOverallDeliveryService.asyncImportOverallDelivery(dirOverallDeliveryAsyncImportIn, UserUtil.getUserName(), UserUtil.getBizOrgCode());
    }

    @ApiOperation(value = "运营端分页查询配货单(为退货单)", notes = "运营端分页查询配货单(为退货单)", httpMethod = "POST")
    @PostMapping("/findDeliveryOrderForReturn")
    public Response<Page<DirDeliveryForReturnOut>> findDeliveryOrderForReturn(@RequestBody DirDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        deliveryOrderIn.setBizOrgCode(loginBizOrgCode);
        Page<DirDeliveryForReturnOut> deliveryOrderOutPage = ordDirDeliveryService.findDeliveryOrderForReturn(deliveryOrderIn);
        return Response.data(deliveryOrderOutPage);
    }

    @ApiOperation(value = "根据id查询配货单明细（为退货单）", notes = "根据id查询配货单明细（为退货单）")
    @GetMapping("/findDetailListForReturnOrder")
    public Response<List<DirDeliveryDetailListForReturnOut>> findDetailListForReturnOrder(@RequestParam Long id) {
        return Response.data(ordDirDeliveryDetailService.findDetailListForReturnOrder(id));
    }

    @ApiOperation(value = "APP退货单申请退货根据配货单查询配货信息(为APP退货申请)", notes = "APP退货单申请退货根据配货单查询配货信息(为APP退货申请)", httpMethod = "GET")
    @GetMapping("/findDeliveryInfoForReturn")
    public Response<List<DirStoreDeliveryNoInfoForAppOut>> findDeliveryInfoForReturn(@RequestParam String storeCode) {
        List<DirStoreDeliveryNoInfoForAppOut> resultList = ordDirDeliveryService.findStoreDeliveryInfoList(storeCode);
        if (CollectionUtils.isEmpty(resultList)) {
            return Response.error("没有匹配的配销单");
        }
        return Response.data(resultList);
    }

    @ApiOperation(value = "运营端退货单申请退货根据配货单查询配货信息", notes = "运营端退货单申请退货根据配货单查询配货信息", httpMethod = "GET")
    @GetMapping("/getDeliveryOrderOutByNo")
    public Response<List<OrdDirDeliveryDetail>> getDeliveryOrderOutByNo(@RequestParam String deliveryOrderNo) {
        OrdDirDeliveryOut ordDirDeliveryOut = ordDirDeliveryService.getDeliveryOrderOutByNo(deliveryOrderNo);
        if (Objects.isNull(ordDirDeliveryOut)) {
            return Response.error("配货单不存在");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDirDeliveryOut.getIsReversal())) {
            return Response.error("配货单已红冲");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDirDeliveryOut.getIsReversalOrder())) {
            return Response.error("配货单为红冲单");
        }
        if (!DeliveryOrderEnum.RECEIVED.getKey().equals(ordDirDeliveryOut.getDeliveryStatusCode())) {
            return Response.error("必须是已收货的配货单");
        }
        return Response.data(ordDirDeliveryOut.getDetailList());
    }

    @PostMapping("/differenceOrderCallBack")
    @ApiOperation(value = "差异回传", notes = "差异回传")
    public void differenceOrderCallBack(@RequestBody JSONObject jsonObject) {
        DifferenceBillVO saveDifferenceIn = JSON.parseObject(jsonObject.toJSONString(), DifferenceBillVO.class);
        dirAsyncTaskItemService.differenceOrderCallBack(saveDifferenceIn);
    }

}
