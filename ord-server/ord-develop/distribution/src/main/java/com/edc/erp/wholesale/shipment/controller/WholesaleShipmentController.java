package com.edc.erp.wholesale.shipment.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.AdjustTypeEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.model.in.QueryWarehouseIn;
import com.edc.erp.model.out.StockInfoOut;
import com.edc.erp.model.out.WarehouseInfoOut;
import com.edc.erp.orderscheduing.service.AsyncTaskItemService;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.wholesale.enumeration.PushPurProgressEnum;
import com.edc.erp.wholesale.handle.PushPurWholesaleShipmentHandle;
import com.edc.erp.wholesale.model.excel.shipment.ImportShipmentOrder;
import com.edc.erp.wholesale.model.excel.shipment.ImportShipmentOrderIn;
import com.edc.erp.wholesale.model.in.PushPurShipmentIn;
import com.edc.erp.wholesale.model.in.shipment.QueryShipmentIn;
import com.edc.erp.wholesale.model.in.shipment.ShipmentOrderWriteOffIn;
import com.edc.erp.wholesale.model.in.shipment.ShipmentWithDetailIn;
import com.edc.erp.wholesale.model.in.shipment.ShipmentsIn;
import com.edc.erp.wholesale.model.listener.shipment.ImportShipmentOrderListener;
import com.edc.erp.wholesale.model.out.shipment.*;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.handle.RepairWarehouseStockHandle;
import com.edc.erp.wholesale.shipment.handle.StockHandle;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.erp.wholesale.shipment.service.impl.AsyncHandleWholesale;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.model.order.in.WholesaleBillIn;
import com.edc.sdk.dts.model.order.vo.WholesaleBillVO;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 批发出货单 前端控制器
 * </p>
 *
 * @author lx
 * @since 2022-10-18 11:59:38
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/wholesaleShipment")
@Api(value = "wholesaleShipment", tags = "批发出货单模块")
public class WholesaleShipmentController {

    private final WholesaleShipmentService wholesaleShipmentService;

    private final AsyncTaskItemService asyncTaskItemService;
    private final AsyncHandleWholesale asyncHandleWholesale;
    private final RedisService redisService;

    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;

    private final PushPurWholesaleShipmentHandle pushPurWholesaleShipmentHandle;

    private final RepairWarehouseStockHandle repairWarehouseStockHandle;

    private final StockServer stockServer;

    /**
     * 分页查询批发出货单
     * @param queryShipmentIn 批发出货单 查询入参类
     * @return
     */
    @ApiOperation(value = "分页查询批发出货单", notes = "分页查询批发出货单")
    @GetMapping("/findByPage")
    public Response<Page<ShipmentWithDetailOut>> findShipmentByPage(QueryShipmentIn queryShipmentIn) {
        //业务组织
        queryShipmentIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<ShipmentWithDetailOut> wholesaleShipmentOutPage = wholesaleShipmentService.findShipmentByPage(queryShipmentIn);
        return Response.data(wholesaleShipmentOutPage);
    }

    /**
     * 批发出货单查询
     * @param id 出货单Id
     * @return
     */
    @ApiOperation(value = "批发出货单查询", notes = "批发出货单查询")
    @GetMapping("/get")
    public Response<ShipmentWithDetailOut> getShipment(
            @ApiParam(name = "id", value = "出货单Id")
            @RequestParam("id") Long id) {

        //业务组织
        String bizOrgCode = UserUtil.getBizOrgCode();
        return Response.data(wholesaleShipmentService.getShipment(id, bizOrgCode));
    }


    /**
     * 查询允许批发的仓位信息
     * @param queryWarehouseIn 仓位信息入参
     * @return
     */
    @ApiOperation(value = "查询允许批发的仓位信息", notes = "查询允许批发的仓位信息")
    @GetMapping("/findStockInfo")
    public Response<List<StockInfoOut>> findStockInfo(QueryWarehouseIn queryWarehouseIn) {
        //添加业务组织
        queryWarehouseIn.setBizOrgCode(UserUtil.getBizOrgCode());
        //过滤已禁用
        queryWarehouseIn.setIsEnable(ModelConst.DELETE.YES);

        List<StockInfoOut> stockInfos = wholesaleShipmentService.findStockInfo(queryWarehouseIn);
        return Response.data(stockInfos);
    }

    /**
     * 查询允许批发仓储信息
     * @param wholesaleOrderType  批发单类型(out：出货，returns：退货)
     * @return
     */
    @ApiOperation(value = "查询允许批发仓储信息", notes = "查询允许批发仓储信息")
    @GetMapping("/findWarehouseInfo")
    public Response<List<WarehouseInfoOut>> findWarehouseInfo(
            @ApiParam(name = "wholesaleOrderType", value = "批发单类型(out：出货，returns：退货)")
            @RequestParam("wholesaleOrderType") String wholesaleOrderType) {

        if (StringUtils.isBlank(wholesaleOrderType)) {
            return Response.error("批发单类型不能为空");
        }
        //业务组织
        String bizOrgCode = UserUtil.getBizOrgCode();
        List<WarehouseInfoOut> warehouseInfoOuts = wholesaleShipmentService.findWarehouseInfo(bizOrgCode, wholesaleOrderType);
        return Response.data(warehouseInfoOuts);
    }


    /***
     * 查询仓储与仓位信息(二级联动)
     * @param wholesaleOrderType 批发单类型(out：出货，returns：退货)
     * @return
     */
    @ApiOperation(value = "查询仓储与仓位信息(二级联动)", notes = "查询仓储与仓位信息(二级联动)")
    @GetMapping("/findWarehouseStockInfo")
    public Response<List<WarehouseInfoOut>> findWarehouseStockInfo(
            @ApiParam(name = "wholesaleOrderType", value = "(out：出货，returns：退货)")
            @RequestParam("wholesaleOrderType") String wholesaleOrderType) {

        if (StringUtils.isBlank(wholesaleOrderType)) {
            return Response.error("批发单类型不能为空");
        }

        //业务组织
        String bizOrgCode = UserUtil.getBizOrgCode();
        List<WarehouseInfoOut> warehouseStockInfos = wholesaleShipmentService.findWarehouseStockInfo(bizOrgCode, wholesaleOrderType);
        return Response.data(warehouseStockInfos);
    }

    /**
     * 保存或修改批发出货单
     * @param shipmentWithDetailIn 批发出货单和明细新增入参类
     * @return
     */
    @ApiOperation(value = "保存或修改批发出货单", notes = "保存或修改批发出货单")
    @PostMapping("/saveUpdate")
    public Response<ShipmentWithDetailOut> saveUpdate(@RequestBody ShipmentWithDetailIn shipmentWithDetailIn) {
        if (CollectionUtils.isEmpty(shipmentWithDetailIn.getWholesaleShipmentDetailList())) {
            return Response.error("请至少录入一条出货单明细！");
        }

        //业务组织代码
        shipmentWithDetailIn.getWholesaleShipment().setBizOrgCode(UserUtil.getBizOrgCode());
        shipmentWithDetailIn.getWholesaleShipment().setOrgCode(UserUtil.getOrgCode());
        String distributionType = shipmentWithDetailIn.getWholesaleShipment().getDistributionType();
        shipmentWithDetailIn.getWholesaleShipment().setDistributionType(DistributionWaysEnum.getTypeByName(distributionType));
        return Response.data(wholesaleShipmentService.saveUpdate(shipmentWithDetailIn));
    }

    /**
     * 出货单审核
     * @param shipmentWithDetailIn 批发出货单 和 明细新增入参类
     * @return
     */
    @ApiOperation(value = "出货单审核", notes = "出货单审核")
    @PostMapping("/audit")
    public Response<String> audit(@RequestBody ShipmentWithDetailIn shipmentWithDetailIn) {
        //审核
        ShipmentWithDetailOut shipmentWithDetailOut = wholesaleShipmentService.audit(shipmentWithDetailIn);
        if (Objects.isNull(shipmentWithDetailOut)) {
            return Response.success("整单没有审核数已作废");
        }
        return Response.success("审核成功");
    }


    @ApiOperation(value = "批量审核出货单", notes = "批量审核出货单")
    @PostMapping("/batchAudit")
    public Response<String> batchAudit(@RequestBody List<String> shipmentIdList) {
        if (CollectionUtils.isEmpty(shipmentIdList)) {
            return Response.error("请选择要审核的批发出单据");
        }
        String bizOrgCode = UserUtil.getBizOrgCode();
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        shipmentIdList.forEach(orderNo -> {
            try {
                WholesaleShipment wholesaleShipment = wholesaleShipmentService.getOneByNo(orderNo);
                if (Objects.isNull(wholesaleShipment)) {
                    throw new BusinessException(orderNo + "不存在的批发出货单");
                }
                if (!ShipmentStatusEnum.PENDING.getCode().equals(wholesaleShipment.getShipmentStatus())) {
                    errorJoiner.add(orderNo + "状态不正确");
                    return;
                }
                ShipmentWithDetailIn shipmentWithDetailIn = wholesaleShipmentService.initShipmentWithDetailInForBatchAudit(wholesaleShipment, bizOrgCode);
                wholesaleShipmentService.audit(shipmentWithDetailIn);
            } catch (Exception e) {
                errorJoiner.add(orderNo + "审核异常");
                log.error("批发出{}审核异常", orderNo, e);
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.error("以下批发出审核失败：" + errorJoiner);
        } else {
            return Response.success();
        }
    }


    /**
     * 出货单作废
     * @param wholesaleShipment 出货单实体
     * @return
     */
    @ApiOperation(value = "出货单作废", notes = "出货单作废")
    @PostMapping("/invalid")
    public Response<String> invalid(@RequestBody WholesaleShipment wholesaleShipment) {
        //获取当前数据的业务组织
        String bizOrgCode = wholesaleShipment.getBizOrgCode();
        if (StringUtils.isNotBlank(bizOrgCode) && !UserUtil.getBizOrgCode().equals(bizOrgCode)) {
            return Response.error("此出货单无作废权限");
        }
        //作废
        int invalid = wholesaleShipmentService.invalid(wholesaleShipment);
        if (invalid == 0) {
            return Response.error("作废失败");
        }

        return Response.success("作废成功");
    }

    /**
     * 出货单冲销
     * @param shipmentOrderWriteOffIn 出货单冲销入参类
     * @return
     */
    @ApiOperation(value = "出货单冲销", notes = "出货单抽冲销")
    @PostMapping("/writeOff")
    public Response<String> writeOff(@RequestBody ShipmentOrderWriteOffIn shipmentOrderWriteOffIn) {
        //添加业务组织
        shipmentOrderWriteOffIn.setBizOrgCode(UserUtil.getBizOrgCode());
        //冲销
        WholesaleShipment wholesaleShipment = wholesaleShipmentService.writeOff(shipmentOrderWriteOffIn, UserUtil.getUserName());
        if (Objects.isNull(wholesaleShipment)) {
            return Response.error("出货单冲销失败");
        }
        return Response.success("出货单冲销成功");
    }

    /**
     * 导出批发出货单
     * @param wholesaleShipmentId 出货单详情入参
     * @return
     */
    @ApiOperation(value = "导出批发出货单", notes = "导出批发出货单")
    @GetMapping("/export")
    public Response<String> export(@RequestParam(value = "wholesaleShipmentId") Long wholesaleShipmentId) {
        return Response.data(wholesaleShipmentService.export(wholesaleShipmentId));
    }

    /**
     * 批量导入批发出货单
     * @param fileId 批发出货单批量导入 入参类
     * @return
     */
    @ApiOperation(value = "批量导入批发出货单", notes = "批量导入批发出货单")
    @GetMapping("/import")
    public Response importShipmentOrder(@RequestParam("fileId") String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return Response.error("文件id不能为空");
        }
        String bizOrgCode = UserUtil.getBizOrgCode();
        String userName = UserUtil.getUserName();
        if (StringUtils.isNotEmpty(UserUtil.getJobNumber())) {
            userName += "【" + UserUtil.getJobNumber() + "】";
        }
        String key = DisSystemConstant.CHECK_WHOLESALE_SHIPMENT_IMPORT_ONLY_ONE + bizOrgCode;
        if (!redisService.setIfAbsent(key, userName, 10L, TimeUnit.MINUTES)) {
            return Response.error("当前有用户正在异步上传批发出货单，请稍后尝试");
        }
        // 表格内校验必填，格式有不通过的则直接返回，其余继续执行
        ImportShipmentOrderListener importShipmentOrderListener = wholesaleShipmentService.importShipmentOrder(fileId);
        List<ImportShipmentOrder> importShipmentOrders = importShipmentOrderListener.getOutDetails();
        List<String> errorDate = importShipmentOrderListener.getErrorDate();
        // 同步返回
        if (CollectionUtils.isNotEmpty(errorDate)) {
            Map<String, List<String>> map = new HashMap(NumberUtil.INTEGER_TWO);
            map.put("warn", errorDate);
            redisService.del(key);
            return Response.data(map);
        }
        if (CollectionUtils.isEmpty(importShipmentOrders)) {
            redisService.del(key);
            return Response.error("无可导入的数据");
        }
        // 异步校验与入库
        asyncHandleWholesale.asyncCheckAndSaveShipment(bizOrgCode, userName, importShipmentOrders, key);
        return Response.success("异步导入中，请稍后");
    }


    /**
     * 批量导入批发出货单明细
     * @param importShipmentOrderIn 批发出货单批量导入 入参类
     * @return
     */
    @ApiOperation(value = "批量导入批发出货单明细", notes = "批量导入批发出货单明细")
    @GetMapping("/importDetail")
    public Response<List<WholesaleShipmentDetailOut>> importShipmentOrderDtl(ImportShipmentOrderIn importShipmentOrderIn) {
        if (StringUtils.isBlank(importShipmentOrderIn.getFileId())) {
            return Response.error("文件id不能为空");
        }
        //添加业务组织
        importShipmentOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return wholesaleShipmentService.importShipmentOrderDtl(importShipmentOrderIn);
    }

    /**
     * 批发出货单下发DTS手动机制
     * @param jsonObject 批发出货单下发DTS入参
     */
    @PostMapping("/wholesaleShipmentToDts")
    @ApiOperation(value = "批发出货单下发DTS手动机制", notes = "批发出货单下发DTS手动机制")
    public Response<String> wholesaleShipmentToDts(@RequestBody JSONObject jsonObject) {
        WholesaleBillIn wholesaleBillIn = JSON.parseObject(jsonObject.toJSONString(), WholesaleBillIn.class);
        asyncTaskItemService.wholesaleShipmentToDts(wholesaleBillIn);
        return Response.success();
    }

    /**
     * 批发出货单DTS回传 手动机制
     * @param json 发出货单DTS回传入参
     */
    @PostMapping("/disWholesaleDtsToErp")
    @ApiOperation(value = "批发出货单DTS回传手动机制", notes = "批发出货单DTS回传手动机制")
    public Response<String> disWholesaleDtsToErp(@RequestBody JSONObject json) {
        WholesaleBillVO wholesaleBillVO = JSON.parseObject(json.toJSONString(), WholesaleBillVO.class);
        asyncTaskItemService.wholesaleOrderCallBack(wholesaleBillVO);
        return Response.success();
    }

    /**
     * 批发出-手动发货
     * @param shipmentsIn  批发出-手动发货 入参
     * @return
     */
    @PostMapping("/sendOut")
    @ApiOperation(value = "批发出-手动发货", notes = "批发出-手动发货")
    public Response<String> shipmentSendOut(@RequestBody @Valid ShipmentsIn shipmentsIn) {
        //业务组织
        shipmentsIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return wholesaleShipmentService.shipmentSendOut(shipmentsIn);
    }

    /**
     * 批量导出批发出货单
     * @param queryShipmentIn 批发出货单查询入参
     * @return
     */
    @ApiOperation(value = "批量导出批发出货单", notes = "批量导出批发出货单")
    @PostMapping("/exportOrder")
    public Response<String> exportOrder(@RequestBody QueryShipmentIn queryShipmentIn) {
        //业务组织
        queryShipmentIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(wholesaleShipmentService.exportOrder(queryShipmentIn));
    }

    /**
     * 批量导出批发出货单明细
     * @param queryShipmentIn 批发出货单查询入参
     * @return
     */
    @ApiOperation(value = "批量导出批发出货单明细", notes = "批量导出批发出货单明细")
    @PostMapping("/exportDetail")
    public Response<String> exportDetail(@RequestBody QueryShipmentIn queryShipmentIn) {
        //业务组织
        queryShipmentIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return Response.data(wholesaleShipmentService.exportDetail(queryShipmentIn));
    }

    @ApiOperation(value = "批发出货单列表查询尾部统计", notes = "批发出货单列表查询尾部统计")
    @GetMapping("/queryShipmentReportForPage")
    public Response<QueryShipmentReportOut> queryShipmentReportForPage(QueryShipmentIn queryShipmentIn) {
        //业务组织
        queryShipmentIn.setBizOrgCode(UserUtil.getBizOrgCode());
        QueryShipmentReportOut queryShipmentReportOut = wholesaleShipmentService.queryShipmentReportForPage(queryShipmentIn);
        return Response.data(queryShipmentReportOut);
    }

    @ApiOperation(value = "手工推送中转采购至采购", notes = "手工推送中转采购至采购")
    @PostMapping("/manualPushPurShipment")
    public Response<QueryShipmentReportOut> manualPushPurShipment(@RequestBody PushPurShipmentIn pushPurShipmentIn) {
        List<TransferShipmentPushPurchaseOut> list = wholesaleShipmentDetailService.findNeedPushPurDetailListByShipmentIdList(pushPurShipmentIn.getShipmentIdList(), pushPurShipmentIn.getBizOrgCode());
        pushPurWholesaleShipmentHandle.handlePushToPur(pushPurShipmentIn.getBizOrgCode(), list);
        return Response.success();
    }

    @ApiOperation(value = "统计列表查询结果汇总数据", notes = "统计列表查询结果汇总数据")
    @GetMapping("/sumWholesaleDateInfo")
    public Response<WholesaleDateInfoOut> sumWholesaleDateInfo(QueryShipmentIn queryShipmentIn) {
        queryShipmentIn.setBizOrgCode(UserUtil.getBizOrgCode());
        WholesaleDateInfoOut wholesaleDateInfoOut = wholesaleShipmentService.sumWholesaleDateInfo(queryShipmentIn);
        return Response.data(wholesaleDateInfoOut);
    }

    /**
     * 批发出货单DTS回传 手动机制
     * @param json 发出货单DTS回传入参
     */
    @PostMapping("/zKWholesaleShipmentBack")
    @ApiOperation(value = "中科创建批发出回传任务手动机制", notes = "中科创建批发出回传任务手动机制")
    public Response<String> zKWholesaleShipmentBack(@RequestBody JSONObject json) {
        WholesaleShipment wholesaleShipment = JSONObject.parseObject(json.toJSONString(), WholesaleShipment.class);
        asyncTaskItemService.zKWholesaleShipmentBack(wholesaleShipment);
        return Response.success();
    }


    @PostMapping("/repairShipmentStock")
    @ApiOperation(value = "手动修复批发出库存", notes = "手动修复批发出库存")
    public Response<String> repairShipmentStock(@RequestBody List<String> orderNoList) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        orderNoList.forEach(orderNo -> {
            try {
                WholesaleShipment wholesaleShipment = new WholesaleShipment();
                wholesaleShipment.setShipmentNo(orderNo);
                wholesaleShipment = wholesaleShipmentService.selectOne(wholesaleShipment);
                if (Objects.isNull(wholesaleShipment)) {
                    errorJoiner.add(orderNo + "不存在");
                    return;
                }
                List<StockFlowIn> stockFlowIns = repairWarehouseStockHandle.handleWholesaleSipmentStock(wholesaleShipment, wholesaleShipment.getShipmentStatus());
                repairWarehouseStockHandle.repairStock(stockFlowIns);
            } catch (Exception e) {
                log.error("{}处理库存异常", orderNo, e);
                errorJoiner.add(orderNo + "处理库存失败");
            }
        });
        return Response.success(errorJoiner.toString());
    }

    @PostMapping("/repairShipmentStockWithType")
    @ApiOperation(value = "手动修复批发出库存（指定出/入）", notes = "手动修复批发出库存，Json结构：[{'shipmentNo':'xx','applyLowering':'reduce','goodsCodes':['a','b']}]")
    public Response<String> repairShipmentStockWithType(@RequestBody JSONArray jsonArray) {
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject order = jsonArray.getJSONObject(i);
            String orderNo = order.getString("shipmentNo");
            String applyLowering = order.getString("applyLowering");
            String goodsCodes = order.getString("goodsCodes");
            try {
                if (AdjustTypeEnum.getNameByCode(applyLowering) == null) {
                    errorJoiner.add(orderNo + "applyLowering不存在");
                    continue;
                }
                WholesaleShipment wholesaleShipment = new WholesaleShipment();
                wholesaleShipment.setShipmentNo(orderNo);
                wholesaleShipment = wholesaleShipmentService.selectOne(wholesaleShipment);
                if (Objects.isNull(wholesaleShipment)) {
                    errorJoiner.add(orderNo + "不存在");
                    continue;
                }
                List<StockFlowIn> stockFlowIns = repairWarehouseStockHandle.handleWholesaleSipmentStock(wholesaleShipment, wholesaleShipment.getShipmentStatus());
                List<StockFlowIn> filterStockFlowIns = new ArrayList<>();

                for (StockFlowIn flow : stockFlowIns) {
                    List<StockFlowGoodsIn> newGoods = new ArrayList<>();
                    flow.getStockStoreFlowGoodsIn().forEach(g -> {
                        // 放申请类型，增/减
                        g.setApplyLowering(applyLowering);
                        // 过滤商品
                        if (goodsCodes == null) {
                            newGoods.add(g);
                        } else {
                            if (goodsCodes.contains(g.getGoodsCode())) {
                                newGoods.add(g);
                            }
                        }
                    });
                    if (!newGoods.isEmpty()) {
                        flow.setStockStoreFlowGoodsIn(newGoods);
                        filterStockFlowIns.add(flow);
                    }
                }
                if (filterStockFlowIns.isEmpty()) {
                    errorJoiner.add(orderNo + "没有匹配的商品明细");
                    continue;
                }
                log.info("手动修复批发出库存的参数：{}", JSONObject.toJSONString(filterStockFlowIns));
                repairWarehouseStockHandle.repairStock(filterStockFlowIns);
            } catch (Exception e) {
                log.error("{}处理库存异常", orderNo, e);
                errorJoiner.add(orderNo + "处理库存失败");
            }
        }
        return Response.success(errorJoiner.toString());
    }

    private final StockHandle stockHandle;

    @GetMapping("/handleWholesaleOrderStock")
    public Response<String> handleWholesaleOrderStock(Long wholesaleShipmentId) {
        return stockHandle.handleWholesaleOrderStock(wholesaleShipmentId);
    }

    @PostMapping("/updatePushPurTime")
    @ApiOperation(value = "修改推送采购时间", notes = "修改推送采购时间")
    public Response<String> updatePushPurTime(@RequestBody WholesaleShipment updateWholesaleShipment) {
        Long id = updateWholesaleShipment.getId();
        WholesaleShipment wholesaleShipment = wholesaleShipmentService.selectByPrimaryKey(updateWholesaleShipment.getId());
        if (Objects.isNull(wholesaleShipment)) {
            return Response.error("不存在的批发单");
        }
        if (!ShipmentStatusEnum.APPROVED.getCode().equals(wholesaleShipment.getShipmentStatus())) {
            return Response.error(ShipmentStatusEnum.APPROVED.getName() + "才能修改截单时间");
        }
        if (!PushPurProgressEnum.PENDING.getProgress().equals(wholesaleShipment.getPushPurProgress())) {
            return Response.error("批发单已推送采购");
        }
        String key = SystemConstant.SHIPMENT_TRANSFER_IDS + wholesaleShipment.getBizOrgCode() + SystemConstant.COLON + id;
        if (redisService.hasKey(key)) {
            return Response.error("批发单已推送采购");
        }
        updateWholesaleShipment.setUpdater(UserUtil.getUserName());
        updateWholesaleShipment.setUpdateTime(LocalDateTime.now());
        wholesaleShipmentService.updatePushPurTime(updateWholesaleShipment);
        return Response.success();
    }
}
