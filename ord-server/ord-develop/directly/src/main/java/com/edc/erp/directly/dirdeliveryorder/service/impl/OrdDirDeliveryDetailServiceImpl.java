package com.edc.erp.directly.dirdeliveryorder.service.impl;

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
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryDetailMapper;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryMapper;
import com.edc.erp.directly.dirdeliveryorder.model.in.*;
import com.edc.erp.directly.dirdeliveryorder.model.out.*;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDetailService;
import com.edc.erp.directly.enumeration.DeliveryOrderReceiveProgressEnum;
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


/**
 * 配货单详情表(OrdDirDeliveryDetail)表服务实现类
 *
 * @author weichao
 * @since 2022-11-10 14:45:40
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDirDeliveryDetailServiceImpl extends BaseServiceImpl<OrdDirDeliveryDetail> implements OrdDirDeliveryDetailService {

    private final OrdDirDeliveryDetailMapper ordDirDeliveryDetailMapper;

    private final OrdDirDeliveryMapper ordDirDeliveryMapper;

    private final RedisService redisService;

    private final OrderGoodsServer orderGoodsServer;

    private final WarehouseServer warehouseServer;

    private final int batchSize = 8000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(List<OrdDirDeliveryDetail> detailList, OrdDirDelivery ordDirDelivery, String orderPriority) {
        Map<String, String> checkMap = new HashMap<>(2);
        //行号
        int line = 1;
        for (OrdDirDeliveryDetail dirDeliveryDetail : detailList) {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setBizOrgCode(ordDirDelivery.getBizOrgCode());
            orderGoodsIn.setGoodsCode(dirDeliveryDetail.getGoodsCode());
            orderGoodsIn.setStoreCode(ordDirDelivery.getStoreCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION_BILL.getType());
            OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
            if (Objects.nonNull(storeOrderGoods)) {
                dirDeliveryDetail.setDistributionUnitPrice(storeOrderGoods.getDistributionUnitPrice());
                dirDeliveryDetail.setDistributionPrice(storeOrderGoods.getDistributionPrice());
                dirDeliveryDetail.setVendorCode(storeOrderGoods.getVendorCode());
                dirDeliveryDetail.setGoodsType(storeOrderGoods.getGoodsType());
                dirDeliveryDetail.setInvoiceType(storeOrderGoods.getInvoiceType());
            }
            dirDeliveryDetail.setDeliveryOrderId(ordDirDelivery.getId());
            dirDeliveryDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
            String key = dirDeliveryDetail.getGoodsCode() + SystemConstant.SHORT_LINE + dirDeliveryDetail.getIsGift() + SystemConstant.SHORT_LINE + dirDeliveryDetail.getBaseGoodsCode();
            if (checkMap.containsKey(key)) {
                throw new BusinessException("此配货单明细中" + dirDeliveryDetail.getGoodsCode() + "商品代码重复;");
            }
            //添加行号
            dirDeliveryDetail.setLine(line);
            dirDeliveryDetail.setOrderPriority(orderPriority);
            checkMap.put(key, dirDeliveryDetail.getGoodsCode());
            line++;
        }
        ordDirDeliveryDetailMapper.batchSave(detailList);
//        int pageSize = 500;
//        int pages = detailList.size() % pageSize == 0 ? detailList.size() / pageSize : detailList.size() / pageSize + 1;
//        for (int i = 0; i < pages; i++) {
//            ordDirDeliveryDetailMapper.batchSave(detailList.subList(i * pageSize, i == pages - 1 ? detailList.size() : (i + 1) * pageSize));
//        }
        return detailList.size();
    }

    @Override
    public List<OrdDirDeliveryDetail> findDeliveryOrderDetails(Long id) {
        OrdDirDeliveryDetail query = new OrdDirDeliveryDetail();
        query.setDeliveryOrderId(id);
        return ordDirDeliveryDetailMapper.select(query);
    }

    @Override
    public StockInfoOut findStockInfoByCode(String stockCode, String bizOrgCode) {
        QueryWarehouseIn queryWarehouseIn = new QueryWarehouseIn();
        queryWarehouseIn.setBizOrgCode(bizOrgCode);
        queryWarehouseIn.setStockCode(stockCode);
        //允许出货配货条件
        queryWarehouseIn.setIsOutReturn(IsOutReturnEnum.IS_DIS_LOGC.getCode() + SystemConstant.WAIT + ModelConst.ENABLE.YES);
        return ordDirDeliveryDetailMapper.findStockInfoByCode(queryWarehouseIn);
    }


    @Override
    public OrdDirDeliveryDetail getDetail(Long deliveryOrderId) {
        return ordDirDeliveryDetailMapper.getDetail(deliveryOrderId);
    }

    @Override
    public List<TransferDeliveryOrderDetailOut> findTransferDeliveryOrderDetails(List<Long> ordDirDeliveryIds) {
        return ordDirDeliveryDetailMapper.findTransferDeliveryOrderDetails(ordDirDeliveryIds);
    }

    @Override
    public List<DirDeliveryOrderDetailsOut> findDeliveryOrderDetailsForPage(DirDeliveryOrderDetailsIn deliveryOrderDetailsIn) {
        return ordDirDeliveryDetailMapper.findDeliveryOrderDetailsByPage(deliveryOrderDetailsIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByDtsDtlList(OrdDirDelivery deliveryOrder, List<OrdDirDeliveryDetail> deliveryOrderDetails, List<UnificationBillDtlVO> detail,
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
        return ordDirDeliveryDetailMapper.updateBatchByDtsDtlList(deliveryOrderDetails);
    }

    @Override
    public List<TakeDirDeliveryOrderGoodsIn> findTakeDirDeliveryOrderGoodsListByDeliveryOrderId(Long deliveryOrderId) {
        return ordDirDeliveryDetailMapper.findTakeDirDeliveryOrderGoodsListByDeliveryOrderId(deliveryOrderId);
    }

    @Override
    public WaitingForDirDeliveryInfoOut getTakeDirDeliveryInfoOut(WaitingForDirDeliveryGoodsIn waitingForDeliveryGoodsIn) {
        WaitingForDirDeliveryInfoOut waitingForDeliveryInfoOut = new WaitingForDirDeliveryInfoOut();
        List<WaitingForDirDeliveryGoodsOut> deliveryGoodsInfoList = new ArrayList<>();
        OrdDirDelivery deliveryOrder = ordDirDeliveryMapper.selectByPrimaryKey(waitingForDeliveryGoodsIn.getDeliveryOrderId());
        waitingForDeliveryGoodsIn.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        List<OrdDirDeliveryDetail> deliveryOrderDetailsList = ordDirDeliveryDetailMapper.findListByTakeDirDelivery(waitingForDeliveryGoodsIn);
        BigDecimal totalPackageQuantity = BigDecimal.ZERO;
        for (OrdDirDeliveryDetail deliveryOrderDetails : deliveryOrderDetailsList) {
//            this.updateGidAndGoodsImage(deliveryOrderDetails, deliveryOrder.getOrgCode());
            WaitingForDirDeliveryGoodsOut waitingForDeliveryGoodsOut = new WaitingForDirDeliveryGoodsOut();
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
                cacheTakeData = redisService.hGet(DirSystemConstant.DIR_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY
                        + SystemConstant.COLON + deliveryOrder.getBizOrgCode() + SystemConstant.COLON + deliveryOrder.getDeliveryOrderNo(), deliveryOrderDetails.getId().toString());
            }
            if (DeliveryOrderReceiveProgressEnum.ONGOING.getKey().equals(deliveryOrder.getReceiveProgress())) {
                cacheTakeData = redisService.hGet(DirSystemConstant.DIR_CACHE_TAKE_DELIVERY_ORDER_KEY
                        + SystemConstant.COLON + deliveryOrder.getBizOrgCode() + SystemConstant.COLON + deliveryOrder.getDeliveryOrderNo(), deliveryOrderDetails.getId().toString());
            }
            if (null != cacheTakeData) {
                CacheTakeDirDeliveryOrderGoodsIn cacheTakeDeliveryOrderGoodsIn = JSONObject.toJavaObject(JSONObject.parseObject(cacheTakeData.toString()), CacheTakeDirDeliveryOrderGoodsIn.class);
                waitingForDeliveryGoodsOut.setCacheArrivalQuantity(cacheTakeDeliveryOrderGoodsIn.getCacheArrivalQuantity());
                waitingForDeliveryGoodsOut.setSweepTheCodeNumber(cacheTakeDeliveryOrderGoodsIn.getSweepTheCodeNumber());
            }
            deliveryGoodsInfoList.add(waitingForDeliveryGoodsOut);
            if (null != deliveryOrderDetails.getDeliveryPackageQuantity()) {
                totalPackageQuantity = totalPackageQuantity.add(deliveryOrderDetails.getDeliveryPackageQuantity());
            }
        }
        waitingForDeliveryInfoOut.setDeliveryGoodsInfoList(deliveryGoodsInfoList);
        List<DirOrderGoodsPreviewOut> orderGoodsPreviewOutList = this.findGoodsNameAndPreviewListByDisDeliveryOrderId(deliveryOrder.getId());
        waitingForDeliveryInfoOut.setSkuItemQuantity(orderGoodsPreviewOutList.size());
        waitingForDeliveryInfoOut.setTotalQuantity(deliveryOrder.getDeliveryQuantity());
        waitingForDeliveryInfoOut.setTotalPackageQuantity(totalPackageQuantity);
        return waitingForDeliveryInfoOut;
    }

    @Override
    public List<DirOrderGoodsPreviewOut> findGoodsNameAndPreviewListByDisDeliveryOrderId(Long deliveryOrderId) {
        return ordDirDeliveryDetailMapper.findGoodsNameAndPreviewListByDeliveryOrderId(deliveryOrderId);
    }

    /**
     * 批量修改配货单明细信息
     *
     * @param detailList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdate(List<OrdDirDeliveryDetail> detailList) {
        return ordDirDeliveryDetailMapper.batchUpdate(detailList);
    }

    @Override
    public List<OrdDirDeliveryDetail> findOrdDirDeliveryDetail(OrdDeliveryDetailIn ordDeliveryDetailIn) {
        return ordDirDeliveryDetailMapper.findOrdDirDeliveryDetail(ordDeliveryDetailIn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDistributionInfo(List<OrdDirDeliveryDetail> dirDeliveryDetailList) {
        return ordDirDeliveryDetailMapper.batchUpdateDistributionInfo(dirDeliveryDetailList);
    }

    /**
     * 批量更新配货单明细信息
     *
     * @param dirDeliveryDetailList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDeliveryInfo(List<OrdDirDeliveryDetail> dirDeliveryDetailList) {
        return ordDirDeliveryDetailMapper.batchUpdateDeliveryInfo(dirDeliveryDetailList);
    }

    @Override
    public OrdDirDeliveryDetail getOneByIdAndDeliveryOrderId(Long id, Long deliveryOrderId) {
        OrdDirDeliveryDetail dirDeliveryDetail = new OrdDirDeliveryDetail();
        dirDeliveryDetail.setId(id);
        dirDeliveryDetail.setDeliveryOrderId(deliveryOrderId);
        dirDeliveryDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDirDeliveryDetailMapper.selectOne(dirDeliveryDetail);
    }


    /**
     * 统计所有配货单明细实收数量和实收金额
     * @param bizOrgCode 业务组织
     * @return
     */
    @Override
    public OrdDirDeliveryDetail countTotalQuantityAndAmount(String bizOrgCode) {
        return ordDirDeliveryDetailMapper.countTotalQuantityAndAmount(bizOrgCode);
    }

    @Override
    public void delete(OrdDirDeliveryDetail ordDirDeliveryDetail) {
        ordDirDeliveryDetailMapper.delete(ordDirDeliveryDetail);
    }

    private void setDeliveryOrderDtlSendQty(OrdDirDelivery deliveryOrder, List<UnificationBillDtlVO> uniOrderDtlInList, OrdDirDeliveryDetail dtl, String centerStockBizOrgCode) {
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
                BigDecimal warehousePrice = warehouseServer.getWarehousePrice(deliveryOrder.getWrhCode(), deliveryOrder.getStockCode(), dtl.getGoodsCode(),
                        centerStockBizOrgCode, dtl.getVendorCode());
                dtl.setWrhPrice(warehousePrice);
                //配销出货配货单发货仓储减库存，仓储成本相关为负值
                dtl.setWrhCostAmount(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice.multiply(dtl.getDeliveryQuantity()).negate());
                dtl.setWrhExceptTaxAmount(dtl.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                dtl.setWrhTaxAmount(dtl.getWrhCostAmount().subtract(dtl.getWrhExceptTaxAmount()));

                //配销出货配货单发货门店加库存，门店成本为正值
                dtl.setStoreCostAmount(dtl.getDeliveryAmount());
                dtl.setStoreExceptTaxAmount(dtl.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                dtl.setStoreTaxAmount(dtl.getStoreCostAmount().subtract(dtl.getStoreExceptTaxAmount()));
                dtl.setProduceDate(uniOrderDtlIn.getFproducedate());
                break;
            }
        }
        if (null == dtl.getDeliveryQuantity()) {
            log.error("回传配货单明细数量不一致, 商品明细代码是--{}", dtl.getGoodsCode());
        }
    }

    @Override
    public DirDeliveryOrderArrivalDataOut sumArrivalData(Long deliveryOrderId) {
        DirDeliveryOrderArrivalDataOut arrivalDataOut = ordDirDeliveryDetailMapper.sumArrivalData(deliveryOrderId);
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
    public DirDeliveryOrderArrivalDataOut sumArrivalDataByDeliveryOrderIdList(List<Long> deliveryOrderIdList) {
        if (CollectionUtils.isEmpty(deliveryOrderIdList)) {
            return null;
        }
        int listSize = deliveryOrderIdList.size();
        DirDeliveryOrderArrivalDataOut totalArrivalDataOut = new DirDeliveryOrderArrivalDataOut();
        totalArrivalDataOut.setTotalArrivalAmount(BigDecimal.ZERO);
        totalArrivalDataOut.setTotalArrivalQuantity(BigDecimal.ZERO);
        for (int i = 0; i < listSize; i += batchSize) {
            // 获取当前批次的子列表
            int endIndex = Math.min(i + batchSize, listSize);
            List<Long> subIdList = deliveryOrderIdList.subList(i, endIndex);
            DirDeliveryOrderArrivalDataOut arrivalDataOut = ordDirDeliveryDetailMapper.sumArrivalDataByDeliveryOrderIdList(subIdList);
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
    public List<DirDeliveryOrderDtlPrintOut> findPrintDtlByDeliveryId(Long deliveryOrderId) {
        return ordDirDeliveryDetailMapper.findPrintDtlByDeliveryId(deliveryOrderId);
    }

    @Override
    public List<OrdDirDeliveryDetail> findDetailSupplyRateList(Long deliveryOrderId, String storeCode, LocalDateTime orderCycleTime) {
        return ordDirDeliveryDetailMapper.findDetailSupplyRateList(deliveryOrderId, storeCode, orderCycleTime);
    }

    @Override
    public List<DirDeliveryDetailListForReturnOut> findDetailListForReturnOrder(Long id) {
        return ordDirDeliveryDetailMapper.findDetailListForReturnOrder(id);
    }
}
