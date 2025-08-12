package com.edc.erp.wholesale.shipment.handle;

import com.edc.erp.common.enumeration.AdjustTypeEnum;
import com.edc.erp.common.enumeration.InvBusinessTypeEnum;
import com.edc.erp.common.enumeration.OrderTypeEnum;
import com.edc.erp.common.enumeration.StockHappenLieEnum;
import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.ShipmentInvAdjustStatusEnum;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.wholesale.model.in.shipment.QueryShipmentDetailIn;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName RepirWStockHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/7/31 9:34
 **/
@Service
@RequiredArgsConstructor
public class RepairWarehouseStockHandle {

    private final StockServer stockServer;

    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;

    private final StockFlowService stockFlowService;

    private final WholesaleShipmentService wholesaleShipmentService;

    public List<StockFlowIn> handleWholesaleSipmentStock(WholesaleShipment wholesaleShipment, String shipmentStatus) {
        //查询出货单详情
        QueryShipmentDetailIn queryShipmentDetailIn = new QueryShipmentDetailIn();
        queryShipmentDetailIn.setWholesaleShipmentId(wholesaleShipment.getId());
        queryShipmentDetailIn.setIsDelete(ModelConst.DELETE.NO);
        List<WholesaleShipmentDetailIn> shipmentDetails = wholesaleShipmentDetailService.findShipmentDetailList(queryShipmentDetailIn);
        //库存流水入参
        StockFlowIn stockFlowIn = new StockFlowIn();
        //添加业务组织
        stockFlowIn.setBizOrgCode(wholesaleShipment.getBizOrgCode());
        //公司业务组织
        stockFlowIn.setOrgCode(wholesaleShipment.getOrgCode());
        //公司业务类型
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.WHOLESALE_OUT.getCode());
        //公司业务类型名称
        stockFlowIn.setSourceName(InvBusinessTypeEnum.WHOLESALE_OUT.getName());
        //业务发生日期
        stockFlowIn.setFlowDate(wholesaleShipment.getUpdateTime());
        //创建人
        stockFlowIn.setCreator(wholesaleShipment.getUpdater());

        if (ShipmentInvAdjustStatusEnum.APPROVED_INVALID.getCode().equals(shipmentStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.INVALID.getCode());
        }
        if (ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode().equals(shipmentStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        }
        if (ShipmentStatusEnum.APPROVED.getCode().equals(shipmentStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.APPROVED.getCode());
        }
        if (ShipmentStatusEnum.SHIPPED.getCode().equals(shipmentStatus)) {
            stockFlowIn.setOperationType(OrderTypeEnum.SHIPPED.getCode());
        }
        //单号/流水号
        stockFlowIn.setSourceNo(wholesaleShipment.getShipmentNo());
        // 外部三方单号
        stockFlowIn.setOtherOrderNo(wholesaleShipment.getSourceNo());
        //库存流水详情入参
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        //处理出货单详情
        for (WholesaleShipmentDetailIn shipmentDetail : shipmentDetails) {
            //流水详情入参
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            //copy 同属性字段
            BeanUtils.copy(shipmentDetail, stockFlowGoodsIn);
            //添加仓位code
            stockFlowGoodsIn.setStockCode(wholesaleShipment.getShipmentStockCode());
            //添加仓储code
            stockFlowGoodsIn.setWarehouseCode(wholesaleShipment.getShipmentWrh());
            //根据仓位code查询仓位信息和仓储信息
            com.edc.erp.common.model.out.stock.StockInfoOut stockInfoOut = stockServer.getByCode(
                    wholesaleShipment.getShipmentStockCode(), wholesaleShipment.getBizOrgCode());

            //添加仓位名称
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            //添加仓储名称
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            //审核数量
            BigDecimal auditQuantity = BigDecimal.valueOf(shipmentDetail.getAuditQuantity()).abs();
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
            //单号
            stockFlowGoodsIn.setSourceNo(wholesaleShipment.getShipmentNo());
            //审核状态
//            if (ShipmentStatusEnum.APPROVED.getCode().equals(shipmentStatus)) {
//                if (auditQuantity.compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO) {
//                    continue;
//                }
//                //申请数占用
//                stockFlowGoodsIn.setApplyQty(auditQuantity);
//                //申请增
//                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.ADD.getCode());
//                //成本不含税金额
//                stockFlowGoodsIn.setCostNonTaxAmount(shipmentDetail.getCostNetProfitAmount());
//                //成本含税金额
//                stockFlowGoodsIn.setCostTaxAmount(shipmentDetail.getCostAmount());
//                //成本税额
//                stockFlowGoodsIn.setCostTax(shipmentDetail.getCostTax());
//                //不含税金额
//                stockFlowGoodsIn.setNonTaxAmount(shipmentDetail.getShipmentNetProfit());
//                //含税金额
//                stockFlowGoodsIn.setTaxAmount(shipmentDetail.getPracticalShipmentAmount());
//                //税额
//                stockFlowGoodsIn.setTax(shipmentDetail.getShipmentTax());
//                //发生价
//                stockFlowGoodsIn.setPrice(shipmentDetail.getUnitPrice());
//            }
            //已发货状态
//            if (ShipmentStatusEnum.SHIPPED.getCode().equals(shipmentStatus)) {
            if (auditQuantity.compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO) {
                continue;
            }
            //申请数释放（回传数量不管多少 全部释放）
            stockFlowGoodsIn.setApplyQty(auditQuantity);
            //申请减
            stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
            //实际减
//            stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            //实际数
//            stockFlowGoodsIn.setActualQty(BigDecimal.valueOf(shipmentDetail.getAuditQuantity()).abs());
            //成本不含税金额
            stockFlowGoodsIn.setCostNonTaxAmount(shipmentDetail.getCostNetProfitAmount());
            //成本含税金额
            stockFlowGoodsIn.setCostTaxAmount(shipmentDetail.getCostAmount());
            //成本税额
            stockFlowGoodsIn.setCostTax(shipmentDetail.getCostTax());
            //不含税金额
            WholesaleShipmentDetailIn wholesaleShipmentDetailIn = new WholesaleShipmentDetailIn();
            wholesaleShipmentDetailIn.setClientCode(wholesaleShipment.getClientCode());
            wholesaleShipmentDetailIn.setStockCode(wholesaleShipment.getShipmentStockCode());
            wholesaleShipmentDetailIn.setShipmentWrh(wholesaleShipment.getShipmentWrh());
            wholesaleShipmentDetailIn.setStockId(stockInfoOut.getId());
            wholesaleShipmentDetailIn.setGoodsCode(shipmentDetail.getGoodsCode());
            SaleGoodsInfoOut saleGoodsInfo = wholesaleShipmentService.getSaleGoodsInfo(wholesaleShipmentDetailIn, wholesaleShipment.getBizOrgCode(), null);
            //销项税率 如果为null 则为 1 + 0 否则 税率 + 1
            BigDecimal outTaxAddOne = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax().add(BigDecimal.ONE) : BigDecimal.ZERO.add(BigDecimal.ONE);

            //计算实际出库金额
            BigDecimal practicalShipmentAmount = shipmentDetail.getUnitPrice().multiply(stockFlowGoodsIn.getActualQty());
            //出库去税金额 四舍五入保留两位
            BigDecimal shipmentNetProfit = practicalShipmentAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
//            BigDecimal nonTaxAmount = shipmentDetail.getUnitPrice().multiply(stockFlowGoodsIn.getActualQty()).divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            stockFlowGoodsIn.setNonTaxAmount(shipmentNetProfit.negate());
            //含税金额
            stockFlowGoodsIn.setTaxAmount(practicalShipmentAmount.negate());
            //税额
            BigDecimal shipmentTax = practicalShipmentAmount.subtract(shipmentNetProfit).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            stockFlowGoodsIn.setTax(shipmentTax.negate());
            //发生价
            stockFlowGoodsIn.setPrice(shipmentDetail.getInventoryPrice());
//            }
            //冲单
//            if (ShipmentInvAdjustStatusEnum.SHIPPED_RUSH_ORDER.getCode().equals(shipmentStatus)) {
//                //实际增
//                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
//                //实际数
//                stockFlowGoodsIn.setActualQty(BigDecimal.valueOf(shipmentDetail.getShipmentQuantity()).abs());
//                //成本不含税金额
//                stockFlowGoodsIn.setCostNonTaxAmount(shipmentDetail.getCostNetProfitAmount().abs());
//                //成本含税金额
//                stockFlowGoodsIn.setCostTaxAmount(shipmentDetail.getCostAmount().abs());
//                //成本税额
//                stockFlowGoodsIn.setCostTax(shipmentDetail.getCostTax().abs());
//                //不含税金额
//                stockFlowGoodsIn.setNonTaxAmount(shipmentDetail.getShipmentNetProfit().abs());
//                //含税金额
//                stockFlowGoodsIn.setTaxAmount(shipmentDetail.getPracticalShipmentAmount().abs());
//                //税额
//                stockFlowGoodsIn.setTax(shipmentDetail.getShipmentTax().abs());
//                //发生价
//                stockFlowGoodsIn.setPrice(shipmentDetail.getInventoryPrice().abs());
//            }
//            //审核后作废
//            if (ShipmentInvAdjustStatusEnum.APPROVED_INVALID.getCode().equals(shipmentStatus)) {
//                //申请数冲单(释放)
//                stockFlowGoodsIn.setApplyQty(auditQuantity);
//                //申请减
//                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
//                //成本不含税金额
//                stockFlowGoodsIn.setCostNonTaxAmount(shipmentDetail.getCostNetProfitAmount());
//                //成本含税金额
//                stockFlowGoodsIn.setCostTaxAmount(shipmentDetail.getCostAmount());
//                //成本税额
//                stockFlowGoodsIn.setCostTax(shipmentDetail.getCostTax());
//                //不含税金额
//                stockFlowGoodsIn.setNonTaxAmount(shipmentDetail.getShipmentNetProfit());
//                //含税金额
//                stockFlowGoodsIn.setTaxAmount(shipmentDetail.getPracticalShipmentAmount());
//                //税额
//                stockFlowGoodsIn.setTax(shipmentDetail.getShipmentTax());
//                //发生价
//                stockFlowGoodsIn.setPrice(shipmentDetail.getUnitPrice());
//            }
            //添加结果集
            stockFlowGoodsIns.add(stockFlowGoodsIn);
        }
        //库存发生位置
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        //添加结果集
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        //转换list
        return Arrays.asList(stockFlowIn);
    }

    @Transactional(rollbackFor = Exception.class)
    public void repairStock(List<StockFlowIn> stockFlowIns) {
        long effectiveCount = stockFlowService.getEffectiveStockFlowCount(stockFlowIns);
        if (effectiveCount > NumberUtil.INTEGER_ZERO) {
            //调用库存rpc调整库存
            Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
            if (!stockFlow.isSuccess()) {
                throw new BusinessException(stockFlow.getMessage());
            }
        }
    }
}
