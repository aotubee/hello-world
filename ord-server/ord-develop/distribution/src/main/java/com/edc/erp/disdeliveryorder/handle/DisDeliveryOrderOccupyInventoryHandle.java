//package com.edc.erp.disdeliveryorder.handle;
//
//import com.alibaba.fastjson.JSON;
//import com.edc.erp.common.constant.SystemConstant;
//import com.edc.erp.common.enumeration.AdjustTypeEnum;
//import com.edc.erp.common.enumeration.DeliveryOrderEnum;
//import com.edc.erp.common.enumeration.InvBusinessTypeEnum;
//import com.edc.erp.common.enumeration.StockHappenLieEnum;
//import com.edc.erp.common.model.out.stock.StockInfoOut;
//import com.edc.erp.common.service.StockServer;
//import com.edc.erp.common.util.NumberUtil;
//import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
//import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
//import com.edc.erp.disdeliveryorder.model.in.UpdateStockDisDeliveryOrderIn;
//import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
//import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
//import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondDetailService;
//import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
//import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
//import com.edc.erp.reducestock.model.out.stock.OperationStockOut;
//import com.edc.erp.reducestock.stock.service.StockWarehouseService;
//import com.edc.plugins.common.exception.BusinessException;
//import com.edc.plugins.common.response.Response;
//import com.edc.plugins.utils.bean.BeanUtils;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import tk.mybatis.mapper.util.StringUtil;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
///**
// * @ClassName DirDeliveryOrderTakeStockHandle
// * @Description TODO
// * @Author ZhangYao
// * @CreateTime 2023/9/6 14:30
// **/
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class DisDeliveryOrderOccupyInventoryHandle {
//
//    private final OrdDisDeliveryService ordDisDeliveryService;
//
//    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;
//
//    private final StockServer stockServer;
//
//    private final StockWarehouseService stockWarehouseService;
//
//    private final OrdDisSalvageDelivPondDetailService ordDisSalvageDelivPondDetailService;
//
//    private final DisDeliveryOrderSalvageHandle disDeliveryOrderSalvageHandle;
//
//    public StockFlowIn initStockFlowIn(Long deliveryOrderId, String bizOrgCode) {
//        OrdDisDelivery ordDisDelivery = ordDisDeliveryService.getOneByIdAndBizOrgCode(deliveryOrderId, bizOrgCode);
//        if (!DeliveryOrderEnum.PENDING.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
//            return null;
//        }
//        List<OrdDisDeliveryDetail> disDeliveryDetailList = ordDisDeliveryDetailService.findDeliveryOrderDetails(ordDisDelivery.getId());
//        StockFlowIn stockFlowIn = new StockFlowIn();
//        stockFlowIn.setBizOrgCode(bizOrgCode);
//        stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getCode());
//        stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getName());
//        stockFlowIn.setFlowDate(LocalDateTime.now());
//        stockFlowIn.setCreator(ordDisDelivery.getCreator());
//        stockFlowIn.setOrgCode(ordDisDelivery.getOrgCode());
//        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
//        stockFlowIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
//        // 初始化占库存参数明细
//        List<StockFlowGoodsIn> stockFlowGoodsIns = this.initStockFlowGoodsIns(bizOrgCode, ordDisDelivery, disDeliveryDetailList);
//        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
////        stockStoreFlowIns.add(stockFlowIn);
//        return stockFlowIn;
//    }
//
//    public void handleOccupyInventory(List<StockFlowIn> stockFlowIns, String bizOrgCode, String loginUsername) {
//        // 请求占用库存
//        Response<List<OperationStockOut>> response = stockWarehouseService.handleDeliveryOrderStock(stockFlowIns);
//        log.info("----------------------->" + JSON.toJSONString(response));
//        if (!response.isSuccess() || Objects.isNull(response.getData())) {
//            log.error(response.getMessage());
//            return;
//        }
//        List<OperationStockOut> operationStockOutList = response.getData();
//        if (CollectionUtils.isNotEmpty(operationStockOutList)) {
//            List<UpdateStockDisDeliveryOrderIn> updateStockDisDeliveryOrderInList = ordDisSalvageDelivPondDetailService.initUpdateStockDeliveryOrderInList(operationStockOutList, loginUsername, bizOrgCode);
//            disDeliveryOrderSalvageHandle.updateAfterHandleOccupyInventory(updateStockDisDeliveryOrderInList);
//        }
//    }
//
//    /**
//     * 初始化占库存参数明细
//     *
//     * @param bizOrgCode
//     * @param ordDisDelivery
//     * @param disDeliveryDetailList
//     * @return
//     */
//    private List<StockFlowGoodsIn> initStockFlowGoodsIns(String bizOrgCode,
//                                                         OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> disDeliveryDetailList) {
//        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
//        disDeliveryDetailList.forEach(item -> {
//            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
//            BigDecimal tar = sell.add(BigDecimal.ONE);
//            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
//            BeanUtils.copy(item, stockFlowGoodsIn);
//            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
//            // 仓储
//            stockFlowGoodsIn.setStockCode(ordDisDelivery.getStockCode());
//            StockInfoOut stockInfoOut = stockServer.getByCode(ordDisDelivery.getStockCode(), bizOrgCode);
//            if (Objects.isNull(stockInfoOut) || StringUtil.isEmpty(stockInfoOut.getWarehouseCode())) {
//                log.error("配销捞单占库存仓储{}不存在", ordDisDelivery.getStockCode());
//                throw new BusinessException("配销捞单占库存仓储" + ordDisDelivery.getStockCode() + "不存在");
//            }
//            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
//            stockFlowGoodsIn.setStoreCode(ordDisDelivery.getStoreCode());
//            stockFlowGoodsIn.setStoreName(ordDisDelivery.getStoreName());
//            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
//            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
//            // 实际增/减
//            stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.ADD.getCode());
//            // 实际数
//            stockFlowGoodsIn.setApplyQty(item.getOrderQuantity().abs());
//            //库存发生位置
//            stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
//            // 是否改变可用库存 Y是N否-统配出/配销出
//            stockFlowGoodsIn.setIsBusinessQty("Y");
//            BigDecimal stockWarehousePrice = ordDisDeliveryService.getStockWarehousePrice(stockInfoOut.getWarehouseCode(), stockInfoOut.getStockCode(), item.getGoodsCode(), bizOrgCode);
//            stockWarehousePrice = Objects.isNull(stockWarehousePrice) ? BigDecimal.ZERO : stockWarehousePrice;
//            //成本含税金额
//            stockFlowGoodsIn.setCostTaxAmount(item.getOrderQuantity().multiply(stockWarehousePrice));
//            //成本不含税金额
//            stockFlowGoodsIn.setCostNonTaxAmount(stockFlowGoodsIn.getCostTaxAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//            //成本税额
//            stockFlowGoodsIn.setCostTax(stockFlowGoodsIn.getCostTaxAmount().subtract(stockFlowGoodsIn.getCostNonTaxAmount()));
//            //含税金额
//            stockFlowGoodsIn.setTaxAmount(item.getOrderQuantity().multiply(item.getOrderUnitPrice()));
//            //不含税金额
//            stockFlowGoodsIn.setNonTaxAmount(stockFlowGoodsIn.getTaxAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//            // 税额
//            stockFlowGoodsIn.setTax(stockFlowGoodsIn.getTaxAmount().subtract(stockFlowGoodsIn.getNonTaxAmount()));
//            //单号
//            stockFlowGoodsIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
//            stockFlowGoodsIns.add(stockFlowGoodsIn);
//        });
//        return stockFlowGoodsIns;
//    }
//}
