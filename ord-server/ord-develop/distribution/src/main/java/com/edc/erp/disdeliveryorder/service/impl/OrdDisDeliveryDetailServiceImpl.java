package com.edc.erp.disdeliveryorder.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.IsOutReturnEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.purchase.OrdDeliveryDetailIn;
import com.edc.erp.common.model.out.TransferDeliveryOrderDetailOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.StandardGoodsInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryDetailMapper;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryMapper;
import com.edc.erp.disdeliveryorder.model.in.CacheTakeDisDeliveryOrderGoodsIn;
import com.edc.erp.disdeliveryorder.model.in.DisDeliveryOrderDetailsIn;
import com.edc.erp.disdeliveryorder.model.in.TakeDisDeliveryOrderGoodsIn;
import com.edc.erp.disdeliveryorder.model.in.WaitingForDisDeliveryGoodsIn;
import com.edc.erp.disdeliveryorder.model.out.*;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.enumeration.DeliveryOrderReceiveProgressEnum;
import com.edc.erp.model.in.QueryWarehouseIn;
import com.edc.erp.model.out.StockInfoOut;
import com.edc.erp.model.out.WarehouseInfoOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.model.order.vo.UnificationBillDtlVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 配销单详情表(OrdDisDeliveryDetail)表服务实现类
 *
 * @author weichao
 * @since 2022-10-10 19:48:15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDisDeliveryDetailServiceImpl extends BaseServiceImpl<OrdDisDeliveryDetail> implements OrdDisDeliveryDetailService {

    private final OrdDisDeliveryDetailMapper ordDisDeliveryDetailMapper;

    private final OrderGoodsServer orderGoodsServer;

    private final RedisService redisService;

    private final OrdDisDeliveryMapper ordDisDeliveryMapper;

    private final WarehouseServer warehouseServer;

    private final int batchSize = 8000;

    /**
     * 批量新增配销单明细信息
     *
     * @param detailList
     * @param ordDisDelivery
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(List<OrdDisDeliveryDetail> detailList, OrdDisDelivery ordDisDelivery, String orderPriority) {
        Map<String, String> checkMap = new HashMap<>(2);
        //行号
        int line = 1;
        for (OrdDisDeliveryDetail disDeliveryDetail : detailList) {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
            orderGoodsIn.setGoodsCode(disDeliveryDetail.getGoodsCode());
            orderGoodsIn.setStoreCode(ordDisDelivery.getStoreCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
            OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
            if (Objects.nonNull(storeOrderGoods)) {
                disDeliveryDetail.setDistributionUnitPrice(storeOrderGoods.getDistributionUnitPrice());
                disDeliveryDetail.setDistributionPrice(storeOrderGoods.getDistributionPrice());
                disDeliveryDetail.setVendorCode(storeOrderGoods.getVendorCode());
                disDeliveryDetail.setGoodsType(storeOrderGoods.getGoodsType());
                disDeliveryDetail.setInvoiceType(storeOrderGoods.getInvoiceType());
            }
            disDeliveryDetail.setDeliveryOrderId(ordDisDelivery.getId());
            disDeliveryDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
            String key = disDeliveryDetail.getGoodsCode() + SystemConstant.SHORT_LINE + disDeliveryDetail.getIsGift() + SystemConstant.SHORT_LINE + disDeliveryDetail.getBaseGoodsCode();
            if (checkMap.containsKey(key)) {
                throw new BusinessException("此配销单明细中" + disDeliveryDetail.getGoodsCode() + "商品代码重复;");
            }
            //添加行号
            disDeliveryDetail.setLine(line);
            disDeliveryDetail.setOrderPriority(orderPriority);
            checkMap.put(key, disDeliveryDetail.getGoodsCode());
            line++;
        }

        return ordDisDeliveryDetailMapper.batchSave(detailList);
//        int pageSize = 500;
//        int pages = detailList.size() % pageSize == 0 ? detailList.size() / pageSize : detailList.size() / pageSize + 1;
//        for (int i = 0; i < pages; i++) {
//            ordDisDeliveryDetailMapper.batchSave(detailList.subList(i * pageSize, i == pages - 1 ? detailList.size() : (i + 1) * pageSize));
//        }
//        return detailList.size();
    }


    /**
     * 查询仓储与仓位信息(二级联动)
     *
     * @param bizOrgCode
     * @return
     */
    @Override
    public List<WarehouseInfoOut> findDisDeliveryStockInfo(String bizOrgCode) {
        //根据允许配货信息的仓储id查询仓储信息
        List<StockInfoOut> dbStockInfos = this.findStockInfoOut(bizOrgCode);
        //如果无仓位信息 则返回空
        if (CollectionUtils.isEmpty(dbStockInfos)) {
            return Collections.emptyList();
        }
        //查询仓储信息
        List<WarehouseInfoOut> warehouseInfoOutList = ordDisDeliveryDetailMapper.findWarehouseInfo(dbStockInfos, bizOrgCode);
        //利用仓位信息中的仓储id分组并转map
        Map<Long, List<StockInfoOut>> stockInfoMap = dbStockInfos.stream().collect(Collectors.groupingBy(StockInfoOut::getWarehouseId));
        //将仓位信息添加至对应的仓储信息内
        warehouseInfoOutList.forEach(item -> {
            //根据仓储id获取对应的仓位信息
            List<StockInfoOut> stockInfoOuts = stockInfoMap.get(item.getId());
            item.setStockInfoOuts(stockInfoOuts);
        });
        return warehouseInfoOutList;
    }

    @Override
    public List<OrdDisDeliveryDetail> findDeliveryOrderDetailByDeliveryIds(List<Long> ordDisDeliveryIds) {
        return ordDisDeliveryDetailMapper.findDeliveryOrderDetailByDeliveryIds(ordDisDeliveryIds);
    }

    @Override
    public List<OrdDisDeliveryDetail> findDeliveryOrderDetails(Long id) {
        OrdDisDeliveryDetail query = new OrdDisDeliveryDetail();
        query.setDeliveryOrderId(id);
        query.setIsDelete(ModelConst.DELETE.NO);
        return ordDisDeliveryDetailMapper.select(query);
    }

    /**
     * 查询配销单明细集合
     *
     * @param ordDisDeliveryIds
     * @return
     */
    @Override
    public List<TransferDeliveryOrderDetailOut> findTransferDeliveryOrderDetails(List<Long> ordDisDeliveryIds) {
        return ordDisDeliveryDetailMapper.findTransferDeliveryOrderDetails(ordDisDeliveryIds);
    }

    /**
     * 查询允许出货配货条件仓位信息
     *
     * @param bizOrgCode 业务组织
     * @return
     */
    public List<StockInfoOut> findStockInfoOut(String bizOrgCode) {
        //允许出货配货条件
        QueryWarehouseIn queryWarehouseIn = new QueryWarehouseIn();
        queryWarehouseIn.setBizOrgCode(bizOrgCode);
        queryWarehouseIn.setIsEnable(ModelConst.ENABLE.YES);
        //允许出货配货条件
        queryWarehouseIn.setIsOutReturn(IsOutReturnEnum.IS_DIS_DIS_LOGC.getCode() + SystemConstant.WAIT + ModelConst.ENABLE.YES);
        //查询仓位信息
        return this.findStockInfo(queryWarehouseIn);
    }

    @Override
    public OrdDisDeliveryDetail getDetail(Long deliveryOrderId) {
        return ordDisDeliveryDetailMapper.getDetail(deliveryOrderId);
    }

    @Override
    public List<DisDeliveryOrderDetailsOut> findDeliveryOrderDetailsForPage(DisDeliveryOrderDetailsIn deliveryOrderDetailsIn) {
        return ordDisDeliveryDetailMapper.findDeliveryOrderDetailsByPage(deliveryOrderDetailsIn);
    }

    /**
     * 查询允许某业务条件的仓位信息- 公共方法一定要传业务条件
     *
     * @param queryWarehouseIn 仓位信息入参
     * @return
     */
    @Override
    public List<StockInfoOut> findStockInfo(QueryWarehouseIn queryWarehouseIn) {
        return ordDisDeliveryDetailMapper.findStockInfo(queryWarehouseIn);
    }

    /**
     * 查询允许要货配货的仓位信息
     *
     * @param stockCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public StockInfoOut findStockInfoByCode(String stockCode, String bizOrgCode) {
        //查询允许配货的仓位信息
        QueryWarehouseIn queryWarehouseIn = new QueryWarehouseIn();
        queryWarehouseIn.setBizOrgCode(bizOrgCode);
        queryWarehouseIn.setStockCode(stockCode);
        //允许出货配货条件
        queryWarehouseIn.setIsOutReturn(IsOutReturnEnum.IS_DIS_DIS_LOGC.getCode() + SystemConstant.WAIT + ModelConst.ENABLE.YES);
        return ordDisDeliveryDetailMapper.findStockInfoByCode(queryWarehouseIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByDtsDtlList(OrdDisDelivery deliveryOrder, List<OrdDisDeliveryDetail> deliveryOrderDetails, List<UnificationBillDtlVO> detail,
                                  Map<String, StandardGoodsInfoOut> standardGoodsMap, String centerStockBizOrgCode) {
        // 对比明细信息
        deliveryOrderDetails.forEach(dtl -> {
            if (Objects.isNull(dtl.getDistributionQuantity())) {
                return;
            }
            StandardGoodsInfoOut standardGoodsInfoOut = standardGoodsMap.get(dtl.getGoodsCode());
            dtl.setExpirationDate(standardGoodsInfoOut.getExpirationDate());
            this.setDeliveryOrderDtlSendQty(deliveryOrder, detail, dtl, centerStockBizOrgCode);
        });
        return ordDisDeliveryDetailMapper.updateBatchByDtsDtlList(deliveryOrderDetails);
    }

    @Override
    public List<TakeDisDeliveryOrderGoodsIn> findTakeDisDeliveryOrderGoodsListByDeliveryOrderId(Long deliveryOrderId) {
        return ordDisDeliveryDetailMapper.findTakeDisDeliveryOrderGoodsListByDeliveryOrderId(deliveryOrderId);
    }

    @Override
    public WaitingForDisDeliveryInfoOut getTakeDisDeliveryInfoOut(WaitingForDisDeliveryGoodsIn waitingForDeliveryGoodsIn) {
        WaitingForDisDeliveryInfoOut waitingForDeliveryInfoOut = new WaitingForDisDeliveryInfoOut();
        List<WaitingForDisDeliveryGoodsOut> deliveryGoodsInfoList = new ArrayList<>();
        OrdDisDelivery deliveryOrder = ordDisDeliveryMapper.selectByPrimaryKey(waitingForDeliveryGoodsIn.getDeliveryOrderId());
        waitingForDeliveryGoodsIn.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        List<OrdDisDeliveryDetail> deliveryOrderDetailsList = ordDisDeliveryDetailMapper.findListByTakeDisDelivery(waitingForDeliveryGoodsIn);
        BigDecimal totalPackageQuantity = BigDecimal.ZERO;
        for (OrdDisDeliveryDetail deliveryOrderDetails : deliveryOrderDetailsList) {
//            this.updateGidAndGoodsImage(deliveryOrderDetails, deliveryOrder.getOrgCode());
            WaitingForDisDeliveryGoodsOut waitingForDeliveryGoodsOut = new WaitingForDisDeliveryGoodsOut();
            BeanUtils.copy(deliveryOrderDetails, waitingForDeliveryGoodsOut);
            waitingForDeliveryGoodsOut.setDeliveryOrderDetailsId(deliveryOrderDetails.getId());
            waitingForDeliveryGoodsOut.setArrivalQuantity(deliveryOrderDetails.getDeliveryQuantity());
            Integer isCanEdit = 1;
            if (null == deliveryOrderDetails.getDeliveryQuantity() || null == deliveryOrderDetails.getLine()) {
                isCanEdit = 0;
            }
            waitingForDeliveryGoodsOut.setIsCanEdit(isCanEdit);
            Integer statisticalType = orderGoodsServer.getGoodsLogisticsByGoodsCode(deliveryOrderDetails.getGoodsCode(), deliveryOrder.getBizOrgCode());
            if (Objects.nonNull(statisticalType)) {
                waitingForDeliveryGoodsOut.setStatisticalType(statisticalType);
            }
            Object cacheTakeData = null;
            if (DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey().equals(deliveryOrder.getReceiveProgress())) {
                cacheTakeData = redisService.hGet(DisSystemConstant.DIS_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY
                        + SystemConstant.COLON + deliveryOrder.getBizOrgCode() + SystemConstant.COLON + deliveryOrder.getDeliveryOrderNo(), deliveryOrderDetails.getId().toString());
            }
            if (DeliveryOrderReceiveProgressEnum.ONGOING.getKey().equals(deliveryOrder.getReceiveProgress())) {
                cacheTakeData = redisService.hGet(DisSystemConstant.DIS_CACHE_TAKE_DELIVERY_ORDER_KEY
                        + SystemConstant.COLON + deliveryOrder.getBizOrgCode() + SystemConstant.COLON + deliveryOrder.getDeliveryOrderNo(), deliveryOrderDetails.getId().toString());
            }
            if (null != cacheTakeData) {
                CacheTakeDisDeliveryOrderGoodsIn cacheTakeDeliveryOrderGoodsIn = JSONObject.toJavaObject(JSONObject.parseObject(cacheTakeData.toString()), CacheTakeDisDeliveryOrderGoodsIn.class);
                waitingForDeliveryGoodsOut.setCacheArrivalQuantity(cacheTakeDeliveryOrderGoodsIn.getCacheArrivalQuantity());
                waitingForDeliveryGoodsOut.setSweepTheCodeNumber(cacheTakeDeliveryOrderGoodsIn.getSweepTheCodeNumber());
            }
            deliveryGoodsInfoList.add(waitingForDeliveryGoodsOut);
            if (null != deliveryOrderDetails.getDeliveryPackageQuantity()) {
                totalPackageQuantity = totalPackageQuantity.add(deliveryOrderDetails.getDeliveryPackageQuantity());
            }
        }
        waitingForDeliveryInfoOut.setDeliveryGoodsInfoList(deliveryGoodsInfoList);
        List<DisOrderGoodsPreviewOut> orderGoodsPreviewOutList = this.findGoodsNameAndPreviewListByDisDeliveryOrderId(deliveryOrder.getId());
        waitingForDeliveryInfoOut.setSkuItemQuantity(orderGoodsPreviewOutList.size());
        waitingForDeliveryInfoOut.setTotalQuantity(deliveryOrder.getDeliveryQuantity());
        waitingForDeliveryInfoOut.setTotalPackageQuantity(totalPackageQuantity);
        return waitingForDeliveryInfoOut;
    }

    @Override
    public List<DisOrderGoodsPreviewOut> findGoodsNameAndPreviewListByDisDeliveryOrderId(Long deliveryOrderId) {
        return ordDisDeliveryDetailMapper.findGoodsNameAndPreviewListByDisDeliveryOrderId(deliveryOrderId);
    }

    /**
     * 批量修改配销单明细信息
     *
     * @param detailList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdate(List<OrdDisDeliveryDetail> detailList) {
        return ordDisDeliveryDetailMapper.batchUpdate(detailList);
    }

    @Override
    public List<OrdDisDeliveryDetail> findOrdDisDeliveryDetail(OrdDeliveryDetailIn ordDeliveryDetailIn) {
        return ordDisDeliveryDetailMapper.findOrdDisDeliveryDetail(ordDeliveryDetailIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDistributionInfo(List<OrdDisDeliveryDetail> disDeliveryDetailList) {
        return ordDisDeliveryDetailMapper.batchUpdateDistributionInfo(disDeliveryDetailList);
    }

    @Override
    public BigDecimal sumOrderAmount(Long deliveryOrderId) {
        BigDecimal sumOrderAmount = ordDisDeliveryDetailMapper.sumOrderAmount(deliveryOrderId);
        return Objects.isNull(sumOrderAmount) ? BigDecimal.ZERO : sumOrderAmount;
    }

    /**
     * 发货后批量修改陪销单明细信息
     *
     * @param detailList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDeliveryInfo(List<OrdDisDeliveryDetail> detailList) {
        return ordDisDeliveryDetailMapper.batchUpdateDeliveryInfo(detailList);
    }

    @Override
    public OrdDisDeliveryDetail getOneByIdAndDeliveryOrderId(Long id, Long deliveryOrderId) {
        OrdDisDeliveryDetail disDeliveryDetail = new OrdDisDeliveryDetail();
        disDeliveryDetail.setId(id);
        disDeliveryDetail.setDeliveryOrderId(deliveryOrderId);
        disDeliveryDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisDeliveryDetailMapper.selectOne(disDeliveryDetail);
    }

    /**
     * 统计所有配销单明细实收数量和实收金额
     * @param bizOrgCode 业务组织
     * @return
     */
    @Override
    public OrdDisDeliveryDetail countTotalQuantityAndAmount(String bizOrgCode) {
        return ordDisDeliveryDetailMapper.countTotalQuantityAndAmount(bizOrgCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateNotStock(Long orderId, String operator) {
        ordDisDeliveryDetailMapper.batchUpdateNotStock(orderId, operator);
    }

    @Override
    public void delete(OrdDisDeliveryDetail ordDisDeliveryDetail) {
        ordDisDeliveryDetailMapper.delete(ordDisDeliveryDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateForSendZk(List<OrdDisDeliveryDetail> deliveryOrderDetails) {
        return ordDisDeliveryDetailMapper.batchUpdateForSendZk(deliveryOrderDetails);
    }

    private void setDeliveryOrderDtlSendQty(OrdDisDelivery deliveryOrder, List<UnificationBillDtlVO> uniOrderDtlInList, OrdDisDeliveryDetail dtl, String centerStockBizOrgCode) {
        for (UnificationBillDtlVO uniOrderDtlIn : uniOrderDtlInList) {
            if (null == uniOrderDtlIn.getFqty()) {
                throw new BusinessException("发货时明细" + uniOrderDtlIn.getFarticlecode() + "确认数量不能为空");
            }
            boolean isEqFlag = uniOrderDtlIn.getFarticlecode().equals(dtl.getGoodsCode()) && uniOrderDtlIn.getLine().equals(dtl.getLine());
            if (isEqFlag) {
                dtl.setDeliveryQuantity(uniOrderDtlIn.getFqty());
                dtl.setDeliveryAmount(dtl.getOrderUnitPrice().multiply(dtl.getDeliveryQuantity()));
                dtl.setDeliveryPackageQuantity(dtl.getDeliveryQuantity().divide(dtl.getDistributionSpecificationNum(), NumberUtil.INTEGER_ZERO, RoundingMode.UP));
                dtl.setUpdater(SystemConstant.SYSTEM_USER);
                dtl.setUpdateTime(deliveryOrder.getUpdateTime());
                dtl.setStockoutQuantity(dtl.getDistributionQuantity().subtract(dtl.getDeliveryQuantity()));

                //税额
                BigDecimal sellTax = Objects.isNull(dtl.getSellTax()) ? BigDecimal.ZERO : dtl.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
                BigDecimal tax = sellTax.add(BigDecimal.ONE);
                //最新仓储库存价
                BigDecimal warehousePrice = warehouseServer.getWarehousePrice(deliveryOrder.getWrhCode(), deliveryOrder.getStockCode(),
                        dtl.getGoodsCode(), centerStockBizOrgCode, dtl.getVendorCode());
                dtl.setWrhPrice(warehousePrice);
                //配销出货配货单发货仓储减库存，仓储成本相关为负值
                dtl.setWrhCostAmount(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice.multiply(dtl.getDeliveryQuantity()).negate());
                dtl.setWrhExceptTaxAmount(dtl.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                dtl.setWrhTaxAmount(dtl.getWrhCostAmount().subtract(dtl.getWrhExceptTaxAmount()));

                //配销出货单发货门店加库存，门店成本为正值
                dtl.setStoreCostAmount(dtl.getDeliveryAmount());
                dtl.setStoreExceptTaxAmount(dtl.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                dtl.setStoreTaxAmount(dtl.getStoreCostAmount().subtract(dtl.getStoreExceptTaxAmount()));
                dtl.setProduceDate(uniOrderDtlIn.getFproducedate());
                break;
            }
        }
        if (null == dtl.getDeliveryQuantity()) {
            log.error("回传配销单明细数量不一致, 商品明细代码是--{}", dtl.getGoodsCode());
        }
    }

    @Override
    public DisDeliveryOrderArrivalDataOut sumArrivalData(Long deliveryOrderId) {
        DisDeliveryOrderArrivalDataOut arrivalDataOut = ordDisDeliveryDetailMapper.sumArrivalData(deliveryOrderId);
        if (Objects.nonNull(arrivalDataOut)) {
            if (null == arrivalDataOut.getTotalArrivalAmount()) {
                arrivalDataOut.setTotalArrivalAmount(BigDecimal.ZERO);
            }
            if (null == arrivalDataOut.getTotalArrivalQuantity()) {
                arrivalDataOut.setTotalArrivalQuantity(BigDecimal.ZERO);
            }
        }
        return arrivalDataOut;
    }

    @Override
    public DisDeliveryOrderArrivalDataOut sumArrivalDataByDeliveryOrderIdList(List<Long> deliveryOrderIdList) {
        if (CollectionUtils.isEmpty(deliveryOrderIdList)) {
            return null;
        }
        int listSize = deliveryOrderIdList.size();
        DisDeliveryOrderArrivalDataOut totalArrivalDataOut = new DisDeliveryOrderArrivalDataOut();
        totalArrivalDataOut.setTotalArrivalAmount(BigDecimal.ZERO);
        totalArrivalDataOut.setTotalArrivalQuantity(BigDecimal.ZERO);
        for (int i = 0; i < listSize; i += batchSize) {
            // 获取当前批次的子列表
            int endIndex = Math.min(i + batchSize, listSize);
            List<Long> subIdList = deliveryOrderIdList.subList(i, endIndex);
            DisDeliveryOrderArrivalDataOut arrivalDataOut = ordDisDeliveryDetailMapper.sumArrivalDataByDeliveryOrderIdList(subIdList);
            if (Objects.nonNull(arrivalDataOut)) {
                if (null != arrivalDataOut.getTotalArrivalAmount()) {
                    totalArrivalDataOut.setTotalArrivalAmount(totalArrivalDataOut.getTotalArrivalAmount().add(arrivalDataOut.getTotalArrivalAmount()));
                }
                if (null != arrivalDataOut.getTotalArrivalQuantity()) {
                    totalArrivalDataOut.setTotalArrivalQuantity(totalArrivalDataOut.getTotalArrivalQuantity().add(arrivalDataOut.getTotalArrivalQuantity()));
                }
            }
        }
        return totalArrivalDataOut;
    }

    @Override
    public List<DisDeliveryOrderDtlPrintOut> findPrintDtlByDeliveryId(Long deliveryOrderId) {
        return ordDisDeliveryDetailMapper.findPrintDtlByDeliveryId(deliveryOrderId);
    }

    @Override
    public List<OrdDisDeliveryDetail> findDetailSupplyRateList(Long deliveryOrderId, String storeCode, LocalDateTime orderCycleTime) {
        return ordDisDeliveryDetailMapper.findDetailSupplyRateList(deliveryOrderId, storeCode, orderCycleTime);
    }
}
