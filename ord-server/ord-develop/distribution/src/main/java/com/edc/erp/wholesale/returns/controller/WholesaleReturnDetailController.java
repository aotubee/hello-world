package com.edc.erp.wholesale.returns.controller;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnCheckGoodsCodeIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnDetailFilterIn;
import com.edc.erp.wholesale.model.in.returns.WholesaleReturnsDetailIn;
import com.edc.erp.wholesale.model.out.returns.WholesaleReturnAndDetailOut;
import com.edc.erp.wholesale.returns.entity.WholesaleReturnDetail;
import com.edc.erp.wholesale.returns.entity.WholesaleReturns;
import com.edc.erp.wholesale.returns.service.WholesaleReturnDetailService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsGoodsService;
import com.edc.erp.wholesale.returns.service.WholesaleReturnsService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.uc.authority.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;


/**
 * <p>
 * 批发退货明细单 前端控制器
 * </p>
 *
 * @author lx
 * @since 2022-10-18 12:00:31
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ord/wholesaleReturnDetail")
@Api(value = "wholesaleReturnDetail", tags = "批发退货明细单模块")
public class WholesaleReturnDetailController {

    private final WholesaleReturnDetailService wholesaleReturnDetailService;

    private final WholesaleReturnsGoodsService wholesaleReturnsGoodsService;

    private final WholesaleReturnsService wholesaleReturnsService;

    @ApiOperation(value = "审核批发退货单", notes = "审核批发退货单")
    @PostMapping("/check")
    public Response checkWholesaleReturnDetail(@RequestBody WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        wholesaleReturnsDetailIn.getWholesaleReturns().setBizOrgCode(UserUtil.getBizOrgCode());
        return wholesaleReturnDetailService.auditWholesaleReturn(wholesaleReturnsDetailIn);
    }

    @ApiOperation(value = "作废批发退货单", notes = "作废批发退货单")
    @PostMapping("/invalid")
    public Response invalidWholesaleReturnDetail(@RequestBody WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        return wholesaleReturnDetailService.invalidWholesaleReturnDetail(wholesaleReturnsDetailIn);
    }

    @ApiOperation(value = "校验商品代码", notes = "校验商品代码")
    @PostMapping("/checkGoodsCode")
    public Response checkGoodsCode(@RequestBody WholesaleReturnCheckGoodsCodeIn wholesaleReturnCheckGoodsCodeIn) {
        WholesaleReturnDetail wholesaleReturnDetail = wholesaleReturnCheckGoodsCodeIn.getWholesaleReturnDetail();
        String clientCode = wholesaleReturnCheckGoodsCodeIn.getClientCode();
        String stockCode = wholesaleReturnCheckGoodsCodeIn.getStockCode();
        String warehouseCode = wholesaleReturnCheckGoodsCodeIn.getWarehouseCode();
        Integer stockId = wholesaleReturnCheckGoodsCodeIn.getStockId();
        return wholesaleReturnsGoodsService.checkWholesaleReturnDetail(wholesaleReturnDetail, clientCode, stockCode, warehouseCode, stockId, null, null);
    }

    @ApiOperation(value = "根据商品代码或者商品名称筛查订单物品", notes = "根据商品代码或者商品名称筛查订单物品")
    @PostMapping("/filter")
    public Response<WholesaleReturnAndDetailOut> filterWholesaleReturnDetail(@RequestBody WholesaleReturnDetailFilterIn wholesaleReturnDetailFilterIn) {
        return wholesaleReturnDetailService.filterWholesaleReturnDetail(wholesaleReturnDetailFilterIn);
    }

    @ApiOperation(value = "冲销批发退货单", notes = "冲销批发退货单")
    @PostMapping("/writeOff")
    public Response writeOffWholesaleReturnDetail(@RequestBody WholesaleReturnsDetailIn wholesaleReturnsDetailIn) {
        return wholesaleReturnDetailService.writeOffWholesaleReturnDetail(wholesaleReturnsDetailIn);
    }

    @ApiOperation(value = "导入批发退货单", notes = "导入批发退货单")
    @GetMapping("/import")
    public Response importWholesaleReturnDetail(@RequestParam("fileId") @NotEmpty @ApiParam(name = "文件id", value = "fileId", required = true) String fileId,
                                                @RequestParam("clientCode") @ApiParam(name = "客户代码", value = "clientCode", required = true) String clientCode,
                                                @RequestParam("stockCode") @ApiParam(name = "仓位代码", value = "stockCode", required = true) String stockCode,
                                                @RequestParam("warehouseCode") @ApiParam(name = "仓储代码", value = "warehouseCode", required = true) String warehouseCode,
                                                @RequestParam("stockId") @ApiParam(name = "仓位id", value = "stockId", required = true) Integer stockId) {
        return wholesaleReturnDetailService.importWholesaleReturnDetail(fileId, clientCode, stockCode, warehouseCode, stockId);
    }

    @ApiOperation(value = "导出批发退货单", notes = "导出批发退货单")
    @GetMapping("/export")
    public Response exportWholesaleReturnDetail(@RequestParam("wholesaleReturnsId") @ApiParam(name = "批发退货单主键id", value = "wholesaleReturnsId", required = true) Long wholesaleReturnsId) {
        String data = wholesaleReturnDetailService.exportWholesaleReturnDetail(wholesaleReturnsId);
        return Response.data(data);
    }

    @ApiOperation(value = "根据批发出货单主键生成批发退货单数据", notes = "根据批发出货单主键生成批发退货单数据")
    @GetMapping("/outToReturn")
    public Response<WholesaleReturnAndDetailOut> outToReturn(@RequestParam("id") Long id, @RequestParam("bizOrgCode") String bizOrgCode) {
        return wholesaleReturnDetailService.outToReturn(id, bizOrgCode);
    }

    @ApiOperation(value = "批量审核批发退货单", notes = "批量审核批发退货单")
    @PostMapping("/batchAuditWholesaleReturn")
    public Response<String> batchAuditWholesaleReturn(@RequestBody List<String> wholesaleReturnsIdList) {
        if (CollectionUtils.isEmpty(wholesaleReturnsIdList)) {
            return Response.error("请选择要审核的批发退单据");
        }
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        wholesaleReturnsIdList.forEach(orderNo -> {
            try {
                WholesaleReturns wholesaleReturns = wholesaleReturnsService.getOneByOrderNo(orderNo);
                if (Objects.isNull(wholesaleReturns)) {
                    throw new BusinessException(orderNo + "不存在的批发退货单");
                }
                if (!ShipmentStatusEnum.PENDING.getCode().equals(wholesaleReturns.getReturnStatus())) {
                    errorJoiner.add(orderNo + "状态不正确");
                    return;
                }
                WholesaleReturnsDetailIn wholesaleReturnsDetailIn = new WholesaleReturnsDetailIn();
                wholesaleReturnsDetailIn.setWholesaleReturns(wholesaleReturns);
                List<WholesaleReturnDetail> wholesaleReturnDetailList = wholesaleReturnDetailService.findListByWholesaleReturnId(wholesaleReturns.getId());
                wholesaleReturnsDetailIn.setWholesaleReturnDetailList(wholesaleReturnDetailList);
                wholesaleReturnsDetailIn.setStockCode(wholesaleReturns.getStorageStockCode());
                wholesaleReturnsDetailIn.setWarehouseCode(wholesaleReturns.getStorageWrh());
                Response response = wholesaleReturnDetailService.auditWholesaleReturn(wholesaleReturnsDetailIn);
                if (!response.isSuccess()) {
                    errorJoiner.add(orderNo + response.getMessage());
                }
            } catch (BusinessException e) {
                errorJoiner.add(orderNo + "审核异常");
                log.error("批发退{}审核异常", orderNo, e);
            }
        });
        if (errorJoiner.length() > 0) {
            return Response.error("以下批发退审核失败：" + errorJoiner);
        } else {
            return Response.success();
        }
    }
}
