package com.edc.erp.returnnoticeorder.controller;

import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNotice;
import com.edc.erp.returnnoticeorder.enumeration.OrdDisReturnNoticeStatusEnum;
import com.edc.erp.returnnoticeorder.model.in.*;
import com.edc.erp.returnnoticeorder.model.out.*;
import com.edc.erp.returnnoticeorder.service.OrdReturnNoticeGoodsService;
import com.edc.erp.returnnoticeorder.service.OrdDisReturnNoticeService;
import com.edc.erp.returnnoticeorder.service.OrdReturnNoticeStoreService;
import com.edc.plugins.common.model.page.Page;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.edc.plugins.common.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;


/**
 * <p>
 * 退货通知单表 前端控制器
 * </p>
 *
 * @author yaojinpeng
 * @since 2022-10-21 10:55:49
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/disReturnNotice")
@Api(value = "disReturnNotice", tags = "配销退货通知单表模块")
public class OrdReturnNoticeController {

    private final OrdDisReturnNoticeService ordDisReturnNoticeService;

    private final OrdReturnNoticeGoodsService ordReturnNoticeGoodsService;

    private final OrdReturnNoticeStoreService ordReturnNoticeStoreService;

    private final OrderGoodsServer orderGoodsServer;

    @ApiOperation(value = "分页查询退货通知单列表", notes = "分页查询退货通知单列表", httpMethod = "POST")
    @PostMapping("/findForPage")
    public Response<Page<OrdReturnNoticeOrderOut>> findReturnNoticeOrderOutForPage(@RequestBody OrdReturnNoticeIn ordReturnNoticeIn) {
        String bizOrgCode = UserUtil.getBizOrgCode();
        ordReturnNoticeIn.setBizOrgCode(bizOrgCode);
        Page<OrdReturnNoticeOrderOut> resultPage = ordDisReturnNoticeService.findReturnNoticeOrderOutForPage(ordReturnNoticeIn);
        return Response.data(resultPage);
    }

    @ApiOperation(value = "查询退货通知单详情", notes = "分页查询退货通知单列表", httpMethod = "POST")
    @PostMapping ("/findReturnNoticeOrderDetailOutForPage")
    public Response<OrdBackHeaderReturnNoticeOrderOut> findReturnNoticeOrderDetailOutForPage(@RequestBody OrdReturnNoticeDetailIn ordReturnNoticeIn){
        OrdBackHeaderReturnNoticeOrderOut ordBackHeaderReturnNoticeOrderOuts = ordDisReturnNoticeService.findReturnNoticeOrderDetailOutForPage(ordReturnNoticeIn);
        return  Response.data(ordBackHeaderReturnNoticeOrderOuts);
    }

/*    @ApiOperation(value = "退货通知单下门店信息列表", notes = "退货通知单下门店信息列表", httpMethod = "POST")
    @PostMapping("/findStorePage")
    public Response<Page<OrdReturnNoticeStoreOut>> findReturnNoticeStoreOutForPage(@RequestBody OrdReturnNoticeStoreIn returnNoticeStoreIn) {
        Page<OrdReturnNoticeStoreOut> resultPage = ordReturnNoticeStoreService.findReturnNoticeStoreOutForPage(returnNoticeStoreIn);
        return Response.data(resultPage);
    }

    @ApiOperation(value = "退货通知单商品列表", notes = "退货通知单商品列表", httpMethod = "POST")
    @PostMapping("/findGoodsPage")
    public Response<Page<OrdReturnNoticeGoodsOut>> findReturnNoticeGoodsOutForPage(@RequestBody ReturnNoticeGoodsIn returnNoticeGoodsIn) {
        Page<OrdReturnNoticeGoodsOut> resultPage = ordReturnNoticeGoodsService.findReturnNoticeGoodsOutForPage(returnNoticeGoodsIn);
        return Response.data(resultPage);
    }*/

    @ApiOperation(value = "保存退货通知单明细", notes = "保存退货通知单明细", httpMethod = "POST")
    @PostMapping("/save")
    public Response saveOrdReturnNotice(@RequestBody SaveOrdReturnNotice saveOrdReturnNotice){
        String bizOrgCode = UserUtil.getBizOrgCode();
        saveOrdReturnNotice.setBizOrgCode(bizOrgCode);
        return ordDisReturnNoticeService.saveOrdReturnNotice(saveOrdReturnNotice);
    }



    @ApiOperation(value = "作废退货通知单", notes = "作废退货通知单", httpMethod = "GET")
    @GetMapping("/invalidated")
    public Response invalidatedOrdReturnNotice(@RequestParam @NotNull Integer id){
        OrdDisReturnNotice ordDisReturnNotice = ordDisReturnNoticeService.getReturnNoticeOrderById(id);
        if(null == ordDisReturnNotice){
            return Response.error("无效的退货通知单");
        }
        if (OrdDisReturnNoticeStatusEnum.INVALID.getKey().equals(ordDisReturnNotice.getStatus())){
            return Response.error("退货通知单已作废");
        }
        if (OrdDisReturnNoticeStatusEnum.PROCESSED.getKey().equals(ordDisReturnNotice.getStatus())){
            return Response.error("退货通知单已生效");
        }
        return Response.data(ordDisReturnNoticeService.invalidatedOrdReturnNotice(ordDisReturnNotice),"作废成功");
    }


    @ApiOperation(value = "审核退货通知单", notes = "审核退货通知单", httpMethod = "POST")
    @PostMapping("/audit")
    public Response auditOrdDisReturnNotice(@RequestBody SaveOrdReturnNotice saveOrdReturnNotice){
        String bizOrgCode = UserUtil.getBizOrgCode();
        saveOrdReturnNotice.setBizOrgCode(bizOrgCode);
        return ordDisReturnNoticeService.auditOrdDisReturnNotice(saveOrdReturnNotice);
    }

    @ApiOperation(value = "导出退货通知单商品明细", notes = "导出退货通知单商品明细", httpMethod = "GET")
    @GetMapping("/exportGoods")
    public Response<String> exportOrdReturnGoods( ReturnNoticeGoodsIn returnNoticeGoodsIn){
        returnNoticeGoodsIn.setBizOrgCode(UserUtil.getBizOrgCode());
        returnNoticeGoodsIn.setPageNum(NumberUtil.INTEGER_ZERO);
        returnNoticeGoodsIn.setPageSize(NumberUtil.INTEGER_ZERO);
        log.info("开始执行“/ord/disReturnNotice/exportGoods");
        String export =ordReturnNoticeGoodsService.exportOrdReturnGoods(returnNoticeGoodsIn);
        log.info("结束执行“/ord/disReturnNotice/exportGoods");
        return Response.data(export,"导出成功");
    }

    @ApiOperation(value = "导出退货通知单门店明细", notes = "导出退货通知单门店明细", httpMethod = "GET")
    @GetMapping("/exportStore")
    public Response<String> exportOrdReturnStore(OrdReturnNoticeStoreIn returnNoticeStoreIn){
        returnNoticeStoreIn.setBizOrgCode(UserUtil.getBizOrgCode());
        returnNoticeStoreIn.setPageNum(NumberUtil.INTEGER_ZERO);
        returnNoticeStoreIn.setPageSize(NumberUtil.INTEGER_ZERO);
        log.info("开始执行“/ord/disReturnNotice/exportStore");
        String export =ordReturnNoticeStoreService.exportOrdReturnStore(returnNoticeStoreIn);
        log.info("结束执行“/ord/disReturnNotice/exportStore");
        return Response.data(export,"导出成功");
    }

    @ApiOperation(value = "退货通知单导入商品信息", notes = "退货通知单导入商品信息", httpMethod = "GET")
    @GetMapping("/importGoods")
    public Response<List<OrdReturnNoticeGoodsOut>> importReturnNoticeGoods(@RequestParam("fileId") String fileId,
                                                                           @RequestParam("goodsCodes") List<String> goodsCodes){
        String bizOrgCode = UserUtil.getBizOrgCode();
        return ordReturnNoticeGoodsService.importReturnNoticeGoods(fileId,goodsCodes,bizOrgCode);
    }

    @ApiOperation(value = "退货通知单导入门店信息", notes = "退货通知单导入门店信息", httpMethod = "GET")
    @GetMapping("/importStore")
    public Response<List<OrdReturnNoticeStoreOut>> importReturnNoticeStore(@RequestParam("fileId") String fileId,
                                                                           @RequestParam("returnType") String returnType){
        String bizOrgCode = UserUtil.getBizOrgCode();
        return ordReturnNoticeStoreService.importReturnNoticeStore(fileId,bizOrgCode,returnType);
    }

    @ApiOperation(value = "校验商品状态是否可以退货", notes = "校验商品状态是否可以退货", httpMethod = "GET")
    @GetMapping("/checkGoodsCode")
    public Response<OrdReturnNoticeGoodsOut> checkGoodsCode(@RequestParam("goodsCode") String goodsCode){
        return ordReturnNoticeGoodsService.checkGoodsCode(goodsCode);
    }

    @ApiOperation(value = "查商品信息", notes = "查商品信息", httpMethod = "GET")
    @GetMapping("/getGoodsInfo")
    public Response<SaveOrderGoodsOut> getGoodsInfo(@RequestParam String goodsCode){
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(goodsCode);
        orderGoodsIn.setBizOrgCode(UserUtil.getBizOrgCode());
        OrderGoodsOut orderGoods = orderGoodsServer.getOrderGoods(orderGoodsIn);
        if(Objects.isNull(orderGoods)){
            return Response.error("商品不存在");
        }
        SaveOrderGoodsOut saveOrdergoodsOut = new SaveOrderGoodsOut();
        saveOrdergoodsOut.setGoodsName(orderGoods.getGoodsName());
        saveOrdergoodsOut.setGoodsCode(orderGoods.getGoodsCode());
        saveOrdergoodsOut.setBarCode(orderGoods.getBarCode());
        saveOrdergoodsOut.setGoodsType(orderGoods.getGoodsType());
        saveOrdergoodsOut.setGoodsTypeStr(orderGoods.getGoodsTypeStr());
        saveOrdergoodsOut.setSpecification(Objects.isNull(orderGoods.getDistributionSpecification())?"":orderGoods.getDistributionSpecification().getQpcStr());
        saveOrdergoodsOut.setSort(orderGoods.getSort());
        saveOrdergoodsOut.setSortName(orderGoods.getSortName());
        saveOrdergoodsOut.setBrandName(orderGoods.getBrandName());
        saveOrdergoodsOut.setOrgGoodsId(orderGoods.getOrgGoodsId());
        return Response.data(saveOrdergoodsOut);
    }

    @ApiOperation(value = "根据单号校验退货通知单是否存在以及是否已生效状态(退货单)", notes = "根据单号校验退货通知单是否存在以及是否已生效状态", httpMethod = "GET")
    @GetMapping("/checkReturnNoticeNo")
    public Response checkReturnNoticeNo(@RequestParam("returnNoticeOrderNo") String returnNoticeOrderNo){
        String bizOrgCode = UserUtil.getBizOrgCode();
        return ordDisReturnNoticeService.checkReturnNoticeNo(returnNoticeOrderNo,bizOrgCode);
    }

    @ApiOperation(value = "根据退货通知单查询可退商品信息(App)", notes = "根据退货通知单查询可退商品信息", httpMethod = "GET")
    @GetMapping("/getReturnNoticeOrderDetailById")
    public Response<OrdBackHeaderReturnNoticeOrderOut> getReturnNoticeOrderDetailById(OrdReturnNoticeDetailIn ordReturnNoticeIn) {
        ordReturnNoticeIn.setBizOrgCode(UserUtil.getBizOrgCode());
        OrdBackHeaderReturnNoticeOrderOut returnNoticeOrderDetailOut = ordDisReturnNoticeService.getReturnNoticeOrderDetailById(ordReturnNoticeIn);
        return Response.data(returnNoticeOrderDetailOut);
    }

    @ApiOperation(value = "退货通知单导入明细信息", notes = "退货通知单导入明细信息")
    @PostMapping("/importDetail")
    public Response<String> importDetail(@RequestBody ImportNoticeDetailIn importNoticeDetailIn) {
        if (Objects.isNull(importNoticeDetailIn.getNoticeOrderId()) && Objects.isNull(importNoticeDetailIn.getReturnDeadline())) {
            return Response.error("参数不正确");
        }
        importNoticeDetailIn.setBizOrgCode(UserUtil.getBizOrgCode());
        importNoticeDetailIn.setOperator(UserUtil.getNickname() + (StringUtils.isBlank(UserUtil.getJobNumber()) ? "" : "【" + UserUtil.getJobNumber() + "】"));
        ordDisReturnNoticeService.importDetail(importNoticeDetailIn);
        return Response.success("开始导入明细信息，请等待10分钟。切勿重复操作");
    }

    @ApiOperation(value = "导出退货通知单明细", notes = "导出退货通知单明细", httpMethod = "GET")
    @GetMapping("/export")
    public Response<String> export(@RequestParam Integer returnNoticeOrderId) {
        if (Objects.isNull(returnNoticeOrderId)) {
            return Response.error("退货通知单ID不能为空");
        }
        log.info("开始执行“/ord/disReturnNotice/export");
        String export =ordDisReturnNoticeService.export(returnNoticeOrderId, UserUtil.getBizOrgCode());
        log.info("结束执行“/ord/disReturnNotice/export");
        return Response.data(export,"导出成功");
    }

    @ApiOperation(value = "查询未退生效通知单的数量", notes = "查询未退生效通知单的数量", httpMethod = "GET")
    @GetMapping("/getUnreturnedCount")
    public Response<Integer> getUnreturnedCount(@RequestParam @NotEmpty(message = "门店代码不能为空") String storeCode){
        return Response.data(ordDisReturnNoticeService.getUnreturnedCount(storeCode, UserUtil.getBizOrgCode()));
    }
}
