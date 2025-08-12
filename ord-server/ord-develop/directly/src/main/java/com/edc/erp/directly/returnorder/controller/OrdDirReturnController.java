package com.edc.erp.directly.returnorder.controller;

import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.returnorder.entity.OrdDirReturn;
import com.edc.erp.directly.returnorder.entity.OrdDirReturnDetail;
import com.edc.erp.directly.returnorder.enumeration.OrdReturnOrderStatusEnum;
import com.edc.erp.directly.returnorder.model.in.*;
import com.edc.erp.directly.returnorder.model.out.BaseReturnOrderOut;
import com.edc.erp.directly.returnorder.model.out.DirReturnOrderPrintOut;
import com.edc.erp.directly.returnorder.model.out.OrdReturnDetailOut;
import com.edc.erp.directly.returnorder.model.out.ReturnDetailOut;
import com.edc.erp.directly.returnorder.service.OrdDirReturnDetailService;
import com.edc.erp.directly.returnorder.service.OrdDirReturnService;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 退货单 前端控制器
 * </p>
 *
 * @author
 * @since 2022-11-18 18:49:59
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/ordDirReturn")
@Api(value = "ordDirReturn", tags = "直营退货单模块")
public class OrdDirReturnController {

    private final OrdDirReturnService ordDirReturnService;
    private final OrdDirReturnDetailService ordDirReturnDetailService;
    private final StoreChannelHandle storeChannelHandle;
    private final StockServer stockServer;
    private final RedisService redisService;


    @ApiOperation(value = "保存退货单", notes = "保存退货单", httpMethod = "POST")
    @PostMapping("/save")
    public Response saveOrUpdateReturnOrder(@RequestBody OrdSaveReturnOrderIn saveReturnOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(saveReturnOrderIn.getStockCode(), loginBizOrgCode, "创建退货单");
        String storeChannelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(saveReturnOrderIn.getStoreCode(), loginBizOrgCode);
        saveReturnOrderIn.setBizOrgCode(loginBizOrgCode);
        saveReturnOrderIn.setCenterStockBizOrgCode(stockInfoOut.getBizOrgCode());
        return ordDirReturnService.saveOrUpdateReturnOrder(saveReturnOrderIn, true, storeChannelBizOrgCode);
    }

    @ApiOperation(value = "分页查询退货单列表", notes = "分页查询退货单列表", httpMethod = "POST")
    @PostMapping("/findForPage")
    public Response<Page<BaseReturnOrderOut>> findBaseReturnOrderForPage(@RequestBody OrdReturnOrderPageIn returnOrderPageIn) {
        returnOrderPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        Page<BaseReturnOrderOut> resultPage = ordDirReturnService.findBaseReturnOrderForPage(returnOrderPageIn);
        return Response.data(resultPage);
    }

    @ApiOperation(value = "查询退货单明细", notes = "分页查询退货单明细", httpMethod = "POST")
    @PostMapping("/findOrdReturnDetailList")
    public Response<ReturnDetailOut> finaOrdReturnDetailList(@RequestBody OrdDirReturnDetailIn pageIn) {
        pageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return ordDirReturnService.findOrdReturnDetails(pageIn);
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
        Page<BaseReturnOrderOut> resultPage = ordDirReturnService.findBaseReturnOrderForPage(returnOrderPageIn);
        return Response.data(resultPage.getList());
    }

    @ApiOperation(value = "POS查询已审核状态退货单详情", notes = "POS查询已审核状态退货单详情")
    @PostMapping("/findReturnOrdersDetail")
    public Response<DirReturnOrderPrintOut> findReturnOrdersDetail(@RequestBody OrdReturnOrderPosDetailIn in) {
        List<DirReturnOrderPrintOut> disReturnOrderPrintOuts = ordDirReturnService.findPrintDataByIds(Collections.singletonList(in.getReturnOrderId()), in.getBizOrgCode());
        return Response.data(disReturnOrderPrintOuts.get(0));
    }

    @ApiOperation(value = "批量获取需要打印的退货单", notes = "批量获取需要打印的退货单")
    @PostMapping("/findPrintDataByIds")
    public Response<List<DirReturnOrderPrintOut>> findPrintDataByIds(@RequestBody List<Long> ids) {
        List<DirReturnOrderPrintOut> disReturnOrderPrintOuts = ordDirReturnService.findPrintDataByIds(ids, UserUtil.getBizOrgCode());
        return Response.data(disReturnOrderPrintOuts);
    }

    @ApiOperation(value = "审核退货单", notes = "审核退货单", httpMethod = "POST")
    @PostMapping("/audit")
    public Response auditOrdReturn(@RequestBody OrdSaveReturnOrderIn saveReturnOrderIn) {
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        saveReturnOrderIn.setBizOrgCode(loginBizOrgCode);
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(saveReturnOrderIn.getStockCode(), saveReturnOrderIn.getBizOrgCode(), "审核退货单");
        saveReturnOrderIn.setCenterStockBizOrgCode(stockInfoOut.getBizOrgCode());
        return ordDirReturnService.auditOrdReturn(saveReturnOrderIn, stockInfoOut.getBizOrgCode());
    }

    @ApiOperation(value = "作废退货单", notes = "作废退货单", httpMethod = "GET")
    @GetMapping("/invalidated")
    public Response invalidatedOrdReturn(@RequestParam @NotNull Integer id) {
        OrdDirReturn returnOrder = ordDirReturnService.getReturnOrderById(id);
        if (Objects.isNull(returnOrder)) {
            return Response.error("退货单不存在");
        }
        stockServer.getAndCheckStockInfo(returnOrder.getStockCode(), UserUtil.getBizOrgCode(), "作废退货单");
        return ordDirReturnService.invalidatedOrdReturn(returnOrder);
    }

    @ApiOperation(value = "收货", notes = "收货", httpMethod = "POST")
    @PostMapping("/receiving")
    public Response receiving(@RequestBody OrdSaveReturnOrderIn saveReturnOrderIn) {
        saveReturnOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        OrdDirReturn returnOrder = ordDirReturnService.getReturnOrderById(saveReturnOrderIn.getReturnOrderId());
        if (!OrdReturnOrderStatusEnum.APPROVED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("已审核状态才允许收货");
        }
        if (OrdReturnOrderStatusEnum.PROCESSED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("该退货单已收货");
        }
        returnOrder.setUpdater(UserUtil.getUserName());
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(returnOrder.getStockCode(), UserUtil.getBizOrgCode(), "退货单收货");
        return ordDirReturnService.receiving(saveReturnOrderIn.getReturnGoodsInfoInList(), returnOrder, stockInfoOut);
    }

    @ApiOperation(value = "冲销退货单", notes = "冲销退货单")
    @PostMapping("/charge")
    public Response chargeReturnOrder(@RequestBody ChargeReturnOrderIn chargeReturnOrderIn) {
        chargeReturnOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        chargeReturnOrderIn.setName(UserUtil.getNickname() + "【" + UserUtil.getJobNumber() + "】");
        int charge = ordDirReturnService.chargeReturnOrder(chargeReturnOrderIn);
        if (charge > 0) {
            return Response.success("冲销成功");
        }
        return Response.error("冲销失败");
    }

    @ApiOperation(value = "导入退货单明细", notes = "导入退货单明细", httpMethod = "POST")
    @GetMapping("/import")
    public Response importOrdReturnDetail(@RequestParam String fileId, @RequestParam String storeCode,
                                          @RequestParam String warehouseCode, @RequestParam String stockCode,
                                          @RequestParam String distributionType, @RequestParam(required = false) String deliveryOrderNo) {
//        try {
//            distributionType = URLDecoder.decode(distributionType, StandardCharsets.UTF_8.displayName());
//        } catch (Exception e) {
//            log.error("URL decode异常", e);
//            return Response.error("参数解析异常");
//        }
        String loginBizOrgCode = UserUtil.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(stockCode, loginBizOrgCode, "导入退货单明细");
        String storeChannelBizOrgCode = storeChannelHandle.getStoreChannelBizOrgCode(storeCode, loginBizOrgCode);
        return ordDirReturnService.importReturnOrderGoods(fileId, storeCode, storeChannelBizOrgCode, warehouseCode, stockCode,
                DistributionWaysEnum.getTypeByName(distributionType), deliveryOrderNo, stockInfoOut.getBizOrgCode());
    }

    @ApiOperation(value = "批量审核退货单", notes = "批量审核退货单", httpMethod = "POST")
    @PostMapping("/batchApproved")
    public Response batchApproved(@RequestBody List<Integer> returnOrderIds) {
        int count = ordDirReturnService.batchApproved(returnOrderIds);
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
        log.info("开始执行“/ord/dirReturnDetail/export");
        String export = ordDirReturnService.exportOrdReturn(ordReturnOrderPageIn);
        log.info("结束执行“/ord/dirReturnDetail/export");
        return Response.data(export, "导出成功");
    }

    @ApiOperation(value = "导出多个配货退货单明细", notes = "导出多个配货退货单明细", httpMethod = "GET")
    @GetMapping("/exportOrdReturnDetailByOrder")
    public Response<String> exportOrdReturnDetailByOrder(OrdReturnOrderPageIn ordReturnOrderPageIn) {
        ordReturnOrderPageIn.setBizOrgCode(UserUtil.getBizOrgCode());
        log.info("开始执行“/ord/dirReturnDetail/exportOrdReturnDetailByOrder");
        String url = ordDirReturnService.exportOrdReturnDetailByOrder(ordReturnOrderPageIn);
        log.info("结束执行“/ord/dirReturnDetail/exportOrdReturnDetailByOrder");
        return Response.data(url, "导出成功");
    }

//    @ApiOperation(value = "导入退货单列表", notes = "导入退货单列表", httpMethod = "GET")
//    @GetMapping("/importOrdReturn")
//    public Response importOrdReturn(@RequestParam String fileId) {
//        String bizOrgCode = UserUtil.getBizOrgCode();
//        return ordDirReturnService.importOrdReturn(fileId, bizOrgCode);
//    }

    @ApiOperation(value = "计算数据", notes = "计算数据", httpMethod = "POST")
    @PostMapping("/compute")
    public Response<OrdReturnDetailOut> compute(@RequestBody OrdReturnDetailIn ordReturnDetailIn) {
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordReturnDetailIn.getStockCode(), UserUtil.getBizOrgCode(), "计算退货的商品数据");
        return Response.data(ordDirReturnDetailService.compute(ordReturnDetailIn, ordReturnDetailIn.getStoreCode(),
                UserUtil.getBizOrgCode(), ordReturnDetailIn.getWrhCode(), ordReturnDetailIn.getStockCode(), stockInfoOut.getBizOrgCode()));
    }

    @ApiOperation(value = "获取退货的商品信息", notes = "商品信息")
    @PostMapping("/getGoodInfo")
    public Response<SaveReturnGoodsOut> getGoodsInfo(@RequestBody OrderGoodsIn orderGoodsIn) {
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(orderGoodsIn.getStockCode(), UserUtil.getBizOrgCode(), "获取退货的商品信息");
        Response<SaveReturnGoodsOut> saveReturnGoodsOut = ordDirReturnService.getGoodsInfo(orderGoodsIn, stockInfoOut.getBizOrgCode());
        return saveReturnGoodsOut;
    }

    @ApiOperation(value = "App保存退货单", notes = "App保存退货单", httpMethod = "POST")
    @PostMapping("/saveReturnOrderReturnNotice")
    public Response saveReturnOrderReturnNotice(@RequestBody AppDirReturnOrderSaveIn appDirReturnOrderSaveIn) {
        appDirReturnOrderSaveIn.setBizOrgCode(UserUtil.getBizOrgCode());
        if (null != appDirReturnOrderSaveIn.getReturnOrderId()) {
            OrdDirReturn ordDisReturn = ordDirReturnService.getReturnOrderById(appDirReturnOrderSaveIn.getReturnOrderId());
            if (null == ordDisReturn) {
                return Response.error("不存在的退货单");
            }
            if (!OrdReturnOrderStatusEnum.SAVED.getKey().equals(ordDisReturn.getReturnStatus())) {
                return Response.error("退货单状态不正确，当前状态：" + OrdReturnOrderStatusEnum.getValueByKey(ordDisReturn.getReturnStatus()));
            }
        }
        return ordDirReturnService.saveAndUpdateReturnOrderByReturnNotice(appDirReturnOrderSaveIn);
    }

    @ApiOperation(value = "查询直营退货单列表(库存盘点)", notes = "查询配货退货单列表(库存盘点)", httpMethod = "GET")
    @GetMapping("/findDirReturnOrder")
    public Response<List<BaseReturnOrderOut>> findDirReturnOrder(OrdDirReturn ordDirReturn) {
        if (StringUtils.isBlank(ordDirReturn.getBizOrgCode())) {
            ordDirReturn.setBizOrgCode(UserUtil.getBizOrgCode());
        }
        List<BaseReturnOrderOut> returnOrderOutList = ordDirReturnService.findDirReturnOrder(ordDirReturn);
        return Response.data(returnOrderOutList);
    }

    @ApiOperation(value = "App提交退货单", notes = "App提交退货单", httpMethod = "POST")
    @PostMapping("/submitReturnOrderForApp")
    public Response<String> submitReturnOrderForApp(@RequestBody OrdSaveReturnOrderIn saveReturnOrderIn) {
        saveReturnOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
        return ordDirReturnService.submitReturnOrderForApp(saveReturnOrderIn);
    }

    @ApiOperation(value = "App根据退货单明细主键删除一个明细", notes = "App根据退货单明细主键删除一个明细", httpMethod = "GET")
    @GetMapping("/deleteReturnOrderDetailById")
    public Response<String> deleteReturnOrderDetailById(Integer id) {
        OrdDirReturnDetail returnOrderDetail = ordDirReturnDetailService.getReturnOrderDetailById(id);
        if (null == returnOrderDetail) {
            return Response.error("不存在的退货明细");
        }
        OrdDirReturn returnOrder = ordDirReturnService.getReturnOrderById(returnOrderDetail.getReturnOrderId());
        if (!OrdReturnOrderStatusEnum.SAVED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("当前退货单不可删除，状态：" + OrdReturnOrderStatusEnum.getValueByKey(returnOrder.getReturnStatus()));
        }
        ordDirReturnDetailService.deleteReturnOrderDetailById(id);
        return Response.success("删除成功");
    }

    @ApiOperation(value = "App删除退货单", notes = "App删除退货单", httpMethod = "GET")
    @GetMapping("/deleteByReturnOrderById")
    public Response<String> deleteByReturnOrderById(Integer id) {
        OrdDirReturn returnOrder = ordDirReturnService.getReturnOrderById(id);
        if (null == returnOrder) {
            return Response.error("不存在的退货单");
        }
        if (!OrdReturnOrderStatusEnum.SAVED.getKey().equals(returnOrder.getReturnStatus())) {
            return Response.error("当前退货单不可删除，状态：" + OrdReturnOrderStatusEnum.getValueByKey(returnOrder.getReturnStatus()));
        }
        ordDirReturnService.deleteByReturnOrderById(id);
        return Response.success("删除成功");
    }


    @ApiOperation(value = "批量作废退货单", notes = "批量作废退货单", httpMethod = "POST")
    @PostMapping("/batchInvalidatedOrdReturn")
    public Response batchInvalidatedOrdReturn(@RequestBody List<Integer> idList) {
        return ordDirReturnService.batchInvalidatedOrdReturn(idList);
    }

    @ApiOperation(value = "批量收货", notes = "批量收货", httpMethod = "POST")
    @PostMapping("/batchReceiving")
    public Response batchReceiving(@RequestBody List<Integer> idList) {
        return ordDirReturnService.batchReceiving(idList);
    }

    @ApiOperation(value = "批量异步导入配货退货单", notes = "批量异步导入配货退货单", httpMethod = "GET")
    @GetMapping("/asyncImportReturn")
    public Response<String> asyncImportReturn(
            @ApiParam(name = "文件id", value = "fileId") @RequestParam(value = "fileId") String fileId) {
        return ordDirReturnService.asyncImportReturn(fileId, UserUtil.getUserName(), UserUtil.getBizOrgCode());
    }
}
