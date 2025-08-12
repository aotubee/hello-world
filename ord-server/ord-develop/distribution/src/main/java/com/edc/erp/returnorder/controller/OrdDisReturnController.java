package com.edc.erp.returnorder.controller;


import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.returnorder.entity.OrdDisReturn;
import com.edc.erp.returnorder.entity.OrdDisReturnDetail;
import com.edc.erp.returnorder.enumeration.OrdReturnOrderStatusEnum;
import com.edc.erp.returnorder.model.in.*;
import com.edc.erp.returnorder.model.out.*;
import com.edc.erp.returnorder.service.OrdDisReturnDetailService;
import com.edc.erp.returnorder.service.OrdDisReturnService;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 退货单 前端控制器
 * </p>
 *
 * @author yaojinpeng
 * @since 2022-10-24 15:35:29
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disReturn")
@Api(value = "disReturn", tags = "配销退货单模块")
public class OrdDisReturnController {

    private final OrdDisReturnService ordDisReturnService;

    private final OrdDisReturnDetailService ordDisReturnDetailService;
    private final StoreChannelHandle storeChannelHandle;
    private final StockServer stockServer;
    private final RedisService redisService;

    @ApiOperation(value = "保存退货单", notes = "保存退货单", httpMethod = "POST")
    @PostMapping("/save")
    public Response saveOrUpdateReturnOrder(@RequestBody OrdSaveReturnOrderIn saveReturnOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(saveReturnOrderIn.getStockCode(), loginBizOrgCode, "创建配销退货单");
        String storeChannelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(saveReturnOrderIn.getStoreCode(), loginBizOrgCode);
        saveReturnOrderIn.setBizOrgCode(loginBizOrgCode);
        saveReturnOrderIn.setCenterStockBizOrgCode(stockInfoOut.getBizOrgCode());
        return ordDisReturnService.saveOrUpdateReturnOrder(saveReturnOrderIn, true, storeChannelBizOrgCode);
    }

    @ApiOperation(value = "分页查询退货单列表", notes = "分页查询退货单列表", httpMethod = "POST")
    @PostMapping("/findForPage")
    public Response<Page<BaseReturnOrderOut>> findBaseReturnOrderForPage(@RequestBody OrdReturnOrderPageIn returnOrderPageIn) {
        returnOrderPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<BaseReturnOrderOut> resultPage = ordDisReturnService.findBaseReturnOrderForPage(returnOrderPageIn);
        return Response.data(resultPage);
    }

    @ApiOperation(value = "查询退货单明细", notes = "分页查询退货单明细", httpMethod = "POST")
    @PostMapping("/findOrdReturnDetailList")
    public Response<ReturnDetailOut> findOrdReturnDetailList(@RequestBody OrdDisReturnDetailIn pageIn) {
        pageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return ordDisReturnService.findOrdReturnDetails(pageIn);
    }

    @ApiOperation(value = "审核退货单", notes = "审核退货单", httpMethod = "POST")
    @PostMapping("/audit")
    public Response auditOrdReturn(@RequestBody OrdSaveReturnOrderIn saveReturnOrderIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        saveReturnOrderIn.setBizOrgCode(bizOrgCode);
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(saveReturnOrderIn.getStockCode(), saveReturnOrderIn.getBizOrgCode(), "审核配销退货单");
        saveReturnOrderIn.setCenterStockBizOrgCode(stockInfoOut.getBizOrgCode());
        return ordDisReturnService.auditOrdReturn(saveReturnOrderIn, stockInfoOut.getBizOrgCode());
    }

    @ApiOperation(value = "作废退货单", notes = "作废退货单", httpMethod = "GET")
    @GetMapping("/invalidated")
    public Response invalidatedOrdReturn(@RequestParam @NotNull Integer id) {
        OrdDisReturn returnOrder = ordDisReturnService.getReturnOrderById(id);
        if (null == returnOrder) {
            return Response.error("退货单不存在");
        }
        if (!OrdReturnOrderStatusEnum.SUBMITTED.getKey().equals(returnOrder.getReturnStatus()) && !OrdReturnOrderStatusEnum.SAVED.getKey().equals(returnOrder.getReturnStatus())
                && !OrdReturnOrderStatusEnum.APPROVED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("退货单只有待审核、已保存和已审核才可作废");
        }
        stockServer.getAndCheckStockInfo(returnOrder.getStockCode(), UserUtil.getBizOrgCode(), "作废配销退货单");
        String loginUsername = UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】");
        returnOrder.setUpdateTime(LocalDateTime.now());
        returnOrder.setUpdater(loginUsername);
        ordDisReturnService.invalidatedOrdReturn(returnOrder);
        return Response.success("作废成功");
    }

    @ApiOperation(value = "收货", notes = "收货", httpMethod = "POST")
    @PostMapping("/receiving")
    public Response receiving(@RequestBody OrdSaveReturnOrderIn saveReturnOrderIn) {
        saveReturnOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        OrdDisReturn returnOrder = ordDisReturnService.getReturnOrderById(saveReturnOrderIn.getReturnOrderId());
        if (!OrdReturnOrderStatusEnum.APPROVED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("已审核状态才允许收货");
        }
        if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("该退货单已收货");
        }
        returnOrder.setUpdater(UserUtil.getUserName());
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(returnOrder.getStockCode(), UserUtil.getBizOrgCode(), "配销退货单收货");
        return ordDisReturnService.receiving(saveReturnOrderIn.getReturnGoodsInfoInList(), returnOrder, stockInfoOut);
    }


    @ApiOperation(value = "导入退货单明细", notes = "导入退货单明细", httpMethod = "POST")
    @GetMapping("/import")
    public Response importOrdReturnDetail(@RequestParam String fileId, @RequestParam String storeCode,
                                          @RequestParam String warehouseCode, @RequestParam String stockCode,
                                          @RequestParam String distributionType, @RequestParam(required = false) String deliveryOrderNo) {
//        try {
//            log.info("distributionType------->{}", distributionType);
//            distributionType = URLDecoder.decode(distributionType, StandardCharsets.UTF_8.displayName());
//            log.info("distributionType----解码->{}", distributionType);
//        } catch (Exception e) {
//            log.error("URL decode异常", e);
//            return Response.error("参数解析异常");
//        }
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(stockCode, loginBizOrgCode, "导入配销退货单明细");
        String storeChannelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(storeCode, loginBizOrgCode);
        return ordDisReturnService.importReturnOrderGoods(fileId, storeCode, storeChannelBizOrgCode, warehouseCode, stockCode,
                DistributionWaysEnum.getTypeByName(distributionType), deliveryOrderNo, stockInfoOut.getBizOrgCode());
    }

    @ApiOperation(value = "冲销退货单", notes = "冲销退货单")
    @PostMapping("/charge")
    public Response chargeReturnOrder(@RequestBody ChargeReturnOrderIn chargeReturnOrderIn) {
        chargeReturnOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        chargeReturnOrderIn.setName(UserUtil.getNickname() + "【" + UserUtil.getJobNumber() + "】");
        int charge = ordDisReturnService.chargeReturnOrder(chargeReturnOrderIn);
        if (charge > 0) {
            return Response.success("冲销成功");
        }
        return Response.error("冲销失败");
    }

    @ApiOperation(value = "获取退货的商品信息", notes = "商品信息")
    @PostMapping("/getGoodInfo")
    public Response<SaveReturnGoodsOut> getGoodInfo(@RequestBody OrderGoodsIn orderGoodsIn) {
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(orderGoodsIn.getStockCode(), UserUtil.getBizOrgCode(), "获取退货的商品信息");
        return ordDisReturnService.getGoodInfo(orderGoodsIn, stockInfoOut.getBizOrgCode());
    }

    @ApiOperation(value = "POS查询已审核状态退货单列表", notes = "POS查询已审核状态退货单列表", httpMethod = "POST")
    @PostMapping("/findReturnOrders")
    public Response<List<BaseReturnOrderOut>> findReturnOrders(@RequestBody OrdReturnOrderPosIn ordReturnOrderPosIn) {
        OrdReturnOrderPageIn returnOrderPageIn = new OrdReturnOrderPageIn();
        returnOrderPageIn.setBizOrgCode(ordReturnOrderPosIn.getBizOrgCode());
        returnOrderPageIn.setEndTime(ordReturnOrderPosIn.getEndTime());
        returnOrderPageIn.setStartTime(ordReturnOrderPosIn.getStartTime());
        returnOrderPageIn.setOrderBy("create_time desc");
        returnOrderPageIn.setStoreCode(ordReturnOrderPosIn.getStoreCode());
        returnOrderPageIn.setReturnStatus(OrdReturnOrderStatusEnum.APPROVED.getKey());
        Page<BaseReturnOrderOut> resultPage = ordDisReturnService.findBaseReturnOrderForPage(returnOrderPageIn);
        return Response.data(resultPage.getList());
    }

    @ApiOperation(value = "POS查询已审核状态退货单详情", notes = "POS查询已审核状态退货单详情")
    @PostMapping("/findReturnOrdersDetail")
    public Response<DisReturnOrderPrintOut> findReturnOrdersDetail(@RequestBody OrdReturnOrderPosDetailIn in) {
        List<DisReturnOrderPrintOut> disReturnOrderPrintOuts = ordDisReturnService.findPrintDataByIds(Collections.singletonList(in.getReturnOrderId()), in.getBizOrgCode());
        return Response.data(disReturnOrderPrintOuts.get(0));
    }

    @ApiOperation(value = "批量获取需要打印的退货单", notes = "批量获取需要打印的退货单")
    @PostMapping("/findPrintDataByIds")
    public Response<List<DisReturnOrderPrintOut>> findPrintDataByIds(@RequestBody List<Long> ids) {
        List<DisReturnOrderPrintOut> disReturnOrderPrintOuts = ordDisReturnService.findPrintDataByIds(ids, UserUtil.getBizOrgCode());
        return Response.data(disReturnOrderPrintOuts);
    }

    @ApiOperation(value = "App保存退货单", notes = "App保存退货单", httpMethod = "POST")
    @PostMapping("/saveReturnOrderReturnNotice")
    public Response saveReturnOrderReturnNotice(@RequestBody AppDisReturnOrderSaveIn appDisReturnOrderSaveIn) {
        appDisReturnOrderSaveIn.setBizOrgCode(UserUtil.getBizOrgCode());
        if (null != appDisReturnOrderSaveIn.getReturnOrderId()) {
            OrdDisReturn ordDisReturn = ordDisReturnService.getReturnOrderById(appDisReturnOrderSaveIn.getReturnOrderId());
            if (null == ordDisReturn) {
                return Response.error("不存在的退货单");
            }
            if (!OrdReturnOrderStatusEnum.SAVED.getKey().equals(ordDisReturn.getReturnStatus())) {
                return Response.error("退货单状态不正确，当前状态：" + OrdReturnOrderStatusEnum.getValueByKey(ordDisReturn.getReturnStatus()));
            }
        }
        return ordDisReturnService.saveReturnOrderByReturnNotice(appDisReturnOrderSaveIn);
    }

    @ApiOperation(value = "计算数据", notes = "计算数据", httpMethod = "POST")
    @PostMapping("/compute")
    public Response<OrdReturnDetailOut> compute(@RequestBody OrdReturnDetailIn ordReturnDetailIn) {
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordReturnDetailIn.getStockCode(), UserUtil.getBizOrgCode(), "计算退货的商品数据");
        return Response.data(ordDisReturnDetailService.compute(ordReturnDetailIn, ordReturnDetailIn.getStoreCode(),
                UserUtil.getBizOrgCode(), ordReturnDetailIn.getWrhCode(), ordReturnDetailIn.getStockCode(), stockInfoOut.getBizOrgCode()));
    }

    @ApiOperation(value = "批量审核退货单", notes = "批量审核退货单", httpMethod = "POST")
    @PostMapping("/batchApproved")
    public Response batchApproved(@RequestBody List<Integer> returnOrderIds) {
        int count = ordDisReturnService.batchApproved(returnOrderIds);
        if (count > 0) {
            return Response.success("审核成功");
        }
        return Response.error("审核失败");
    }

    @ApiOperation(value = "导出退货单列表", notes = "导出退货单列表", httpMethod = "GET")
    @GetMapping("/exportOrdReturn")
    public Response<String> exportOrdReturn(OrdReturnOrderPageIn ordReturnOrderPageIn) {
        ordReturnOrderPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        ordReturnOrderPageIn.setPageNum(0);
        ordReturnOrderPageIn.setPageSize(0);
        log.info("开始执行“/ord/disReturnDetail/export");
        String export = ordDisReturnService.exportOrdReturn(ordReturnOrderPageIn);
        log.info("结束执行“/ord/disReturnDetail/export");
        return Response.data(export, "导出成功");
    }

    @ApiOperation(value = "导出多个配销退货单明细", notes = "导出多个配销退货单明细", httpMethod = "GET")
    @GetMapping("/exportOrdReturnDetailByOrder")
    public Response<String> exportOrdReturnDetailByOrder(OrdReturnOrderPageIn ordReturnOrderPageIn) {
        ordReturnOrderPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        log.info("开始执行“/ord/disReturnDetail/exportOrdReturnDetailByOrder");
        String url = ordDisReturnService.exportOrdReturnDetailByOrder(ordReturnOrderPageIn);
        log.info("结束执行“/ord/disReturnDetail/exportOrdReturnDetailByOrder");
        return Response.data(url, "导出成功");
    }

//    @ApiOperation(value = "导入退货单列表", notes = "导入退货单列表", httpMethod = "GET")
//    @GetMapping("/importOrdReturn")
//    public Response importOrdReturn(@RequestParam String fileId) {
//        String bizOrgCode = UserUtil.getBizOrgCode();
//        return ordDisReturnService.importOrdReturn(fileId, bizOrgCode);
//    }

    @ApiOperation(value = "查询配销退货单列表(库存盘点)", notes = "查询配销退货单列表(库存盘点)", httpMethod = "GET")
    @GetMapping("/findDisReturnOrder")
    public Response<List<BaseReturnOrderOut>> findDisReturnOrder(OrdDisReturn ordDisReturn) {
        if (StringUtils.isBlank(ordDisReturn.getBizOrgCode())) {
            ordDisReturn.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        List<BaseReturnOrderOut> returnOrderOutList = ordDisReturnService.findDisReturnOrder(ordDisReturn);
        return Response.data(returnOrderOutList);
    }

    @ApiOperation(value = "App提交退货单", notes = "App提交退货单", httpMethod = "POST")
    @PostMapping("/submitReturnOrderForApp")
    public Response<String> submitReturnOrderForApp(@RequestBody OrdSaveReturnOrderIn saveReturnOrderIn) {
        saveReturnOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return ordDisReturnService.submitReturnOrderForApp(saveReturnOrderIn);
    }

    @ApiOperation(value = "App根据退货单明细主键删除一个明细", notes = "App根据退货单明细主键删除一个明细", httpMethod = "GET")
    @GetMapping("/deleteReturnOrderDetailById")
    public Response<String> deleteReturnOrderDetailById(Integer id) {
        OrdDisReturnDetail returnOrderDetail = ordDisReturnDetailService.getReturnOrderDetailById(id);
        if (null == returnOrderDetail) {
            return Response.error("不存在的退货明细");
        }
        OrdDisReturn returnOrder = ordDisReturnService.getReturnOrderById(returnOrderDetail.getReturnOrderId());
        if (!OrdReturnOrderStatusEnum.SAVED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("当前退货单不可删除，状态：" + OrdReturnOrderStatusEnum.getValueByKey(returnOrder.getReturnStatus()));
        }
        ordDisReturnDetailService.deleteReturnOrderDetailById(id);
        return Response.success("删除成功");
    }

    @ApiOperation(value = "App删除退货单", notes = "App删除退货单", httpMethod = "GET")
    @GetMapping("/deleteByReturnOrderById")
    public Response<String> deleteByReturnOrderById(Integer id) {
        OrdDisReturn returnOrder = ordDisReturnService.getReturnOrderById(id);
        if (null == returnOrder) {
            return Response.error("不存在的退货单");
        }
        if (!OrdReturnOrderStatusEnum.SAVED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("当前退货单不可删除，状态：" + OrdReturnOrderStatusEnum.getValueByKey(returnOrder.getReturnStatus()));
        }
        ordDisReturnService.deleteByReturnOrderById(id);
        return Response.success("删除成功");
    }

    @ApiOperation(value = "批量作废", notes = "批量作废", httpMethod = "POST")
    @PostMapping("/batchInvalidatedOrdReturn")
    public Response batchInvalidatedOrdReturn(@RequestBody List<Integer> idList) {
        return ordDisReturnService.batchInvalidatedOrdReturn(idList);
    }

    @ApiOperation(value = "批量收货", notes = "批量收货", httpMethod = "POST")
    @PostMapping("/batchReceiving")
    public Response batchReceiving(@RequestBody List<Integer> idList) {
        return ordDisReturnService.batchReceiving(idList, UserUtil.getUserName());
    }

    @ApiOperation(value = "批量异步导入配销退货单", notes = "批量异步导入配销退货单", httpMethod = "GET")
    @GetMapping("/asyncImportReturn")
    public Response<String> asyncImportReturn(
            @ApiParam(name = "文件id", value = "fileId") @RequestParam(value = "fileId") String fileId) {
        return ordDisReturnService.asyncImportReturn(fileId, UserUtil.getUserName(), UserUtil.getBizOrgCode());
    }

}
