package com.edc.erp.wholesale.shipment.handle;

import com.edc.erp.common.model.out.goods.SaleGoodsInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.enumeration.ShipmentStatusEnum;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.wholesale.model.in.shipment.QueryShipmentDetailIn;
import com.edc.erp.wholesale.model.in.shipment.ShipmentWithDetailIn;
import com.edc.erp.wholesale.model.in.shipment.WholesaleShipmentDetailIn;
import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentDetailService;
import com.edc.erp.wholesale.shipment.service.WholesaleShipmentService;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName StockHandle
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/25 17:38
 **/
@Service
@RequiredArgsConstructor
public class StockHandle {

    private final WholesaleShipmentService wholesaleShipmentService;

    private final WholesaleShipmentDetailService wholesaleShipmentDetailService;

    private final StockFlowService stockFlowService;

    private final StockServer stockServer;

    public Response<String> handleWholesaleOrderStock(Long wholesaleShipmentId) {
        WholesaleShipment wholesaleShipment = wholesaleShipmentService.selectByPrimaryKey(wholesaleShipmentId);
        //查询明细
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailIns = this.findShipmentDetailByShipmentId(wholesaleShipmentId);
        //构建初始化参数
        ShipmentWithDetailIn shipmentWithDetailIn = new ShipmentWithDetailIn();
        //添加出货单
        shipmentWithDetailIn.setWholesaleShipment(wholesaleShipment);
        //添加明细
        shipmentWithDetailIn.setWholesaleShipmentDetailList(wholesaleShipmentDetailIns);
        //初始化出货单和明细
        this.initInventory(shipmentWithDetailIn);
        //修改已发货状态
        shipmentWithDetailIn.getWholesaleShipment().setShipmentStatus(ShipmentStatusEnum.SHIPPED.getCode());
        //更新时间
        shipmentWithDetailIn.getWholesaleShipment().setUpdateTime(LocalDateTime.now());
        shipmentWithDetailIn.getWholesaleShipment().setDeliveryTime(LocalDateTime.now());
        //更新出货单和明细
        int updateCount = wholesaleShipmentService.batchUpdate(shipmentWithDetailIn, ShipmentStatusEnum.APPROVED.getCode());
        if (NumberUtil.INTEGER_ZERO.equals(updateCount)) {
            return Response.error(wholesaleShipment.getShipmentNo() + "发货失败");
        }
        //发货扣减库存
        List<StockFlowIn> stockFlowIns = wholesaleShipmentService.addOrSubStock(
                shipmentWithDetailIn.getWholesaleShipment(),
                shipmentWithDetailIn.getWholesaleShipmentDetailList(),
                ShipmentStatusEnum.SHIPPED.getCode());
        //调用库存rpc调整库存
        Response stockFlow = stockFlowService.checkStockFlow(stockFlowIns);
        if (!stockFlow.isSuccess()) {
            return Response.error("库存调整失败");
        }
        return Response.success();
    }

    private List<WholesaleShipmentDetailIn> findShipmentDetailByShipmentId(Long shipmentId) {
        //查询出货单详情
        QueryShipmentDetailIn queryShipmentDetailIn = new QueryShipmentDetailIn();
        queryShipmentDetailIn.setWholesaleShipmentId(shipmentId);
        queryShipmentDetailIn.setIsDelete(ModelConst.DELETE.NO);
        return wholesaleShipmentDetailService.findShipmentDetailList(queryShipmentDetailIn);
    }

    public void initInventory(ShipmentWithDetailIn shipmentWithDetailIn) {
        //获取批发出货单
        WholesaleShipment wholesaleShipment = shipmentWithDetailIn.getWholesaleShipment();
        //获取明细
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList = shipmentWithDetailIn.getWholesaleShipmentDetailList();
        for (WholesaleShipmentDetailIn detailIn : wholesaleShipmentDetailList) {
            //查询商品信息入参初始化
            this.initSaleGoodsInfo(shipmentWithDetailIn.getWholesaleShipment(), detailIn);
            //查询商品信息
            SaleGoodsInfoOut saleGoodsInfo = wholesaleShipmentService.getSaleGoodsInfo(detailIn, wholesaleShipment.getBizOrgCode(), null);
            //获取审核数量
            Integer auditQuantity = detailIn.getAuditQuantity();
            //销项税率 如果为null 则为 1 + 0 否则 税率 + 1
            BigDecimal outTaxAddOne = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax().add(BigDecimal.ONE) : BigDecimal.ZERO.add(BigDecimal.ONE);
            //税率取消 + 1
            BigDecimal outTax = Objects.nonNull(saleGoodsInfo) && Objects.nonNull(saleGoodsInfo.getOutTax()) ? saleGoodsInfo.getOutTax() : BigDecimal.ZERO;
            //最新库存价
            BigDecimal inventoryPrice = Objects.isNull(saleGoodsInfo) || Objects.isNull(saleGoodsInfo.getInventoryPrice()) ? BigDecimal.ZERO : saleGoodsInfo.getInventoryPrice();
            detailIn.setInventoryPrice(inventoryPrice);
            //出库数量即审核数量
            detailIn.setShipmentQuantity(auditQuantity);
            //出库包装数
            String shipmentPackageQuantity = this.getShipmentPackageQuantity(saleGoodsInfo, auditQuantity);
            detailIn.setShipmentPackageQuantity(shipmentPackageQuantity);
            //计算实际出库金额
            BigDecimal practicalShipmentAmount = detailIn.getUnitPrice().multiply(BigDecimal.valueOf(auditQuantity));
            detailIn.setPracticalShipmentAmount(practicalShipmentAmount);
            //出库去税金额 四舍五入保留两位
            BigDecimal shipmentNetProfit = practicalShipmentAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            detailIn.setShipmentNetProfit(shipmentNetProfit);
            //出库税额 四舍五入保留两位
            detailIn.setShipmentTax(practicalShipmentAmount.subtract(shipmentNetProfit).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //成本金额 四舍五入保留两位
            BigDecimal costAmount = inventoryPrice.multiply(BigDecimal.valueOf(auditQuantity)).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate();
            detailIn.setCostAmount(costAmount);
            //成本去税金额 四舍五入保留两位
            BigDecimal costNetProfitAmount = costAmount.divide(outTaxAddOne, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            detailIn.setCostNetProfitAmount(costNetProfitAmount);
            //成本税额 四舍五入保留两位
            detailIn.setCostTax(costAmount.subtract(costNetProfitAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //更新时间
            detailIn.setUpdateTime(LocalDateTime.now());
        }
        //根据处理之后的明细初始化出货单
        this.initShipmentOrder(shipmentWithDetailIn);
    }

    private void initSaleGoodsInfo(WholesaleShipment dbShipment, WholesaleShipmentDetailIn detailIn) {
        //查询仓位信息
        com.edc.erp.common.model.out.stock.StockInfoOut stockInfo = stockServer.getByCode(
                dbShipment.getShipmentStockCode(), dbShipment.getBizOrgCode());
        //仓位id
        detailIn.setStockId(Objects.isNull(stockInfo) ? null : stockInfo.getId());
        //仓位code
        detailIn.setStockCode(dbShipment.getShipmentStockCode());
        //仓储code
        detailIn.setShipmentWrh(dbShipment.getShipmentWrh());
        //客户代码
        detailIn.setClientCode(dbShipment.getClientCode());
    }

    public void initShipmentOrder(ShipmentWithDetailIn shipmentWithDetailIn) {
        //获取明细
        List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList = shipmentWithDetailIn.getWholesaleShipmentDetailList();
        //统计出库数量
        shipmentWithDetailIn.getWholesaleShipment().setShipmentQuantity(wholesaleShipmentDetailList.stream().filter(detail -> Objects.nonNull(detail.getShipmentQuantity())).mapToInt(WholesaleShipmentDetailIn::getShipmentQuantity).sum());
        //实际出库金额
        shipmentWithDetailIn.getWholesaleShipment().setPracticalShipmentAmount(wholesaleShipmentDetailList.stream().filter(detail -> Objects.nonNull(detail.getShipmentQuantity())).map(WholesaleShipmentDetailIn::getPracticalShipmentAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static String getShipmentPackageQuantity(SaleGoodsInfoOut saleGoodsInfo, int shipmentQuantity) {
        String shipmentPackageQuantity;
        if (Objects.isNull(saleGoodsInfo) || Objects.isNull(saleGoodsInfo.getPackageSpecification())) {
            shipmentPackageQuantity = NumberUtil.INTEGER_ZERO.toString();
        } else {
            if (shipmentQuantity % saleGoodsInfo.getQpc() != NumberUtil.INTEGER_ZERO) {
                shipmentPackageQuantity = shipmentQuantity / saleGoodsInfo.getQpc() + "+" + shipmentQuantity % saleGoodsInfo.getQpc();
            } else {
                shipmentPackageQuantity = String.valueOf(shipmentQuantity / saleGoodsInfo.getQpc());
            }
        }
        return shipmentPackageQuantity;
    }
}
