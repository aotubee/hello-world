package com.edc.erp.directly.handle;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrdStoreInventorySupplyRate;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.purchase.GoodsDtlsIn;
import com.edc.erp.common.model.in.purchase.TransferNoticePurchaseIn;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.model.out.TransferDeliveryOrderDetailOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.StandardGoodsInfoOut;
import com.edc.erp.common.model.out.stock.GoodsStockSupplyRateOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.rpc.PurchaseOrderClient;
import com.edc.erp.common.service.OrdStoreInventorySupplyRateService;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.handle.DirDeliveryOrderSalvageHandle;
import com.edc.erp.directly.dirdeliveryorder.model.in.DirSignDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.model.in.TakeDirDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDetailService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderSigningService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirSalvageDelivPondDetailService;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDetail;
import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestDeliveryService;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestDetailService;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.distribution.service.OrdDirOrderTrackService;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.enumeration.*;
import com.edc.erp.directly.model.out.TransferDirDeliveryOrderOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dts.model.order.vo.UnificationBillVO;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 直营配货单业务处理类
 * @since 2022/10/25 19:04
 */
@Service
@Slf4j
public class DirDeliveryOrderHandle {

    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;

    @Autowired
    private DirDeliveryOrderConfigHandle dirDeliveryOrderConfigHandle;

    @Autowired
    private OrdDirDelivRequestDetailService ordDirDelivRequestDetailService;

    @Autowired
    private OrdDirDelivRequestDeliveryService ordDirDelivRequestDeliveryService;

    @Autowired
    private DirRequestOrderHandle requestOrderHandle;

    @Autowired
    private UniqueUtils uniqueUtils;

    @Autowired
    private OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    @Autowired
    private WarehouseServer warehouseServer;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private OrdDirSalvageDelivPondDetailService ordDirSalvageDelivPondDetailService;

    @Autowired
    private PurchaseOrderClient purchaseOrderClient;

    @Autowired
    private DirDeliveryOrderSalvageHandle dirDeliveryOrderSalvageHandle;

    @Autowired
    private OrdDirOrderTrackService ordDirOrderTrackService;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private OrdDirDeliveryService ordDirDeliveryService;

    @Autowired
    private StockServer stockServer;

    @Autowired
    private OrdStoreInventorySupplyRateService ordStoreInventorySupplyRateService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TakeDirDeliveryHandle takeDirDeliveryHandle;

    @Autowired
    private OrdDirDeliveryOrderSigningService deliveryOrderSigningService;


    /**
     * 创建配货单
     *
     * @param requestOrderCreateMqIn
     * @param configItemMap
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public List<OrdDirDelivery> createDeliveryOrder(RequestOrderCreateMqIn requestOrderCreateMqIn, StoreOut storeOut,
                                                    StoreLogisticsOut logistics, Map<String, StockInfoOut> stockMap, Map<String, List<OrderProcessConfigItemOut>> configItemMap) {
        String bizOrgCode = requestOrderCreateMqIn.getBizOrgCode();
        Integer orderCycleId = requestOrderCreateMqIn.getOrderCycleId();
        Long requestOrderId = requestOrderCreateMqIn.getRequestOrderId();
        OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, bizOrgCode);
//        //获取拆分配货单规则
//        List<DirOrderProcessConfigItem> splitRuleItemOutList = dirDeliveryOrderConfigHandle.findSplitRule(orderCycleId, orderCycle.getStoreCode(), bizOrgCode);
//        if (CollectionUtils.isEmpty(splitRuleItemOutList)) {
//            log.info("门店" + orderCycle.getStoreCode() + "的" + orderCycle.getShortOrderType() + "（" + orderCycle.getTruncationDateTime() + "）" + "未找到配货单拆分规则配置项");
//            return null;
//        }
        List<OrdDirDelivery> deliveryOrderList;
        List<OrdDirDelivRequestDetail> requestOrderDetailList = ordDirDelivRequestDetailService.findByRequestOrderIdAndBizOrgCode(requestOrderId);
        // 更新要货单
        OrdDirDelivRequest requestOrder = requestOrderHandle.getRequestOrderByRequestOrderId(requestOrderId);
        // 仓位+配送方式模式拆分
        deliveryOrderList = this.splitByPositionAndDistributionType(storeOut, bizOrgCode, requestOrder, requestOrderDetailList, configItemMap, stockMap, DateUtils.format(requestOrder.getTruncationDateTime()));

        requestOrder.setStatusCode(RequestOrderStatusEnum.EXCRETED.getKey());
        requestOrder.setUpdater(SystemConstant.SYSTEM_USER);
        requestOrder.setUpdateTime(LocalDateTime.now());
        requestOrderHandle.updateByPrimaryKeySelective(requestOrder);
        // 创建配货单与要货单关联
        ordDirDelivRequestDeliveryService.batchSave(deliveryOrderList, requestOrderId);
        // 创建捞单池
        List<OrdDirDelivery> salvageDeliveryOrderList = deliveryOrderList.stream()
                .filter(ordDirDelivery -> DeliveryOrderEnum.PENDING.getKey().equals(ordDirDelivery.getDeliveryStatusCode())
                        && DistributionWaysEnum.UNIFIEDDIS.getType().equals(ordDirDelivery.getDistributionType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(salvageDeliveryOrderList)) {
            log.info("要货单{}拆分配货保存直营捞单池明细开始-----------------", requestOrder.getRequestOrderNo());
            ordDirSalvageDelivPondDetailService.saveOrdDirSalvageDelivPondDetails(salvageDeliveryOrderList, orderCycle,
                    requestOrderCreateMqIn.getLoginUsername(), requestOrderCreateMqIn.getAuditType(), logistics);
            // 如果是立即自动审核，则审核占库存
            if (SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode().equals(requestOrderCreateMqIn.getAuditType())) {
                int executeCount = dirDeliveryOrderSalvageHandle.handleSalvageAfterSplitDeliveryOrder(salvageDeliveryOrderList,
                        bizOrgCode, requestOrderCreateMqIn.getLoginUsername());
                log.info("待处理{}个配货单，处理成功{}个配货单审核占库存", salvageDeliveryOrderList.size(), executeCount);
            }
            log.info("要货单{}拆分配货保存直营捞单池明细结束----------------", requestOrder.getRequestOrderNo());
        }
        return deliveryOrderList;
    }

    /**
     * 按照仓位和配送方式拆分配货单
     *  @param storeOut               门店对象
     * @param bizOrgCode             业务组织代码
     * @param requestOrderDetailList 要货单明细实体集合
     * @param configItemMap       拆分规则选项
     */
    @Transactional(rollbackFor = Exception.class)
    public List<OrdDirDelivery> splitByPositionAndDistributionType(StoreOut storeOut, String bizOrgCode, OrdDirDelivRequest requestOrder,
                                                                   List<OrdDirDelivRequestDetail> requestOrderDetailList,
                                                                   Map<String, List<OrderProcessConfigItemOut>> configItemMap,
                                                                   Map<String, StockInfoOut> stockMap, String truncationDateTime) {
        Map<String, List<OrdDirDelivRequestDetail>> requestOrderDetailsMap = new TreeMap<>();
        // 赠品集合
        Map<String, List<OrdDirDelivRequestDetail>> giftMap = new HashMap<>();
        for (OrdDirDelivRequestDetail orderDetail : requestOrderDetailList) {
            if (NumberUtils.INTEGER_ONE.equals(orderDetail.getIsGift())) {
                List<OrdDirDelivRequestDetail> giftList = giftMap.containsKey(orderDetail.getBaseGoodsCode()) ? giftMap.get(orderDetail.getBaseGoodsCode()) : new ArrayList<>();
                giftList.add(orderDetail);
                giftMap.put(orderDetail.getBaseGoodsCode(), giftList);
                continue;
            }
            String key = this.getSplitKeyBySplitItemList(configItemMap.get(OrderCycleProcessConfigCodeEnum.DELIVERY_SPLIT_CONDITION.getCode()), orderDetail.getStockCode(), orderDetail.getDistributionType(), orderDetail.getGoodsType());
            List<OrdDirDelivRequestDetail> splitList = requestOrderDetailsMap.get(key);
            if (CollectionUtils.isEmpty(splitList)) {
                splitList = Lists.newArrayList();
            }
            splitList.add(orderDetail);
            requestOrderDetailsMap.put(key, splitList);
        }
        List<OrdDirDelivery> deliveryOrderList = Lists.newArrayList();
        requestOrderDetailsMap.forEach((key, value) -> {
            String[] splits = key.split(SystemConstant.SHORT_LINE);
            OrdDirDelivery deliveryOrder = new OrdDirDelivery();
            deliveryOrder.setDeliveryOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PH.getCode(), bizOrgCode, uniqueUtils, 4));
            deliveryOrder.setStockCode(splits[0]);
            deliveryOrder.setDistributionType(splits[1]);
            if (splits.length == NumberUtil.INTEGER_THREE) {
                deliveryOrder.setGoodsType(splits[NumberUtil.INTEGER_TWO]);
            }
            deliveryOrder.setSourceCode(DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType());
            deliveryOrder.setStoreCode(storeOut.getStoreCode());
            deliveryOrder.setStoreName(storeOut.getStoreName());
            String deliveryStatusCode = "";
            if (DistributionWaysEnum.UNIFIEDDIS.getType().equals(deliveryOrder.getDistributionType())) {
                deliveryStatusCode = DeliveryOrderEnum.PENDING.getKey();
            }
            if (DistributionWaysEnum.TRANSFER.getType().equals(deliveryOrder.getDistributionType())) {
                deliveryStatusCode = DeliveryOrderEnum.PREVIEWAPPROVED.getKey();
            }
            if (StringUtils.isBlank(deliveryStatusCode)) {
                deliveryStatusCode = DeliveryOrderEnum.PENDING.getKey();
            }
            deliveryOrder.setDeliveryStatusCode(deliveryStatusCode);
//            StockInfoOut stockInfoOut = stockServer.getByCode(deliveryOrder.getStockCode(), bizOrgCode);
            StockInfoOut stockInfoOut = stockMap.get(deliveryOrder.getStockCode());
            if (Objects.nonNull(stockInfoOut)) {
                deliveryOrder.setWrhCode(stockInfoOut.getWarehouseCode());
            }
            deliveryOrder.setIsReversal(NumberUtil.INTEGER_ZERO);
            deliveryOrder.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
            deliveryOrder.setBizOrgCode(bizOrgCode);
            deliveryOrder.setCreator(SystemConstant.SYSTEM_USER);
            deliveryOrder.setUpdater(SystemConstant.SYSTEM_USER);
            deliveryOrder.setIsDelete(ModelConst.DELETE.NO);
            deliveryOrder.setOrgCode(requestOrder.getOrgCode());
            deliveryOrder.setSkuCount(value.size());
            deliveryOrder.setRequestOrderNo(requestOrder.getRequestOrderNo());
//            String orderPriority = storeCenterService.getOrderPriorityByStoreCode(storeOut.getStoreCode(), bizOrgCode, deliveryOrder.getStockCode(), deliveryOrder.getDistributionType());
            String orderPriority = configItemMap.containsKey(OrderCycleProcessConfigCodeEnum.DELIVERY_ORDER_PRIORITY.getCode()) ? configItemMap.get(OrderCycleProcessConfigCodeEnum.DELIVERY_ORDER_PRIORITY.getCode()).get(0).getItemName() : null;
            deliveryOrder.setOrderPriority(orderPriority);
            //要货数量
//            deliveryOrder.setOrderQuantity(value.stream().map(OrdDirDelivRequestDetail::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            //要货金额
//            deliveryOrder.setOrderAmount(value.stream().map(item -> item.getOriginalUnitPrice().multiply(item.getQuantity())).reduce(BigDecimal.ZERO, BigDecimal::add));
            //配销数量
            deliveryOrder.setDistributionQuantity(BigDecimal.ZERO);
            //配销金额
            deliveryOrder.setDistributionAmount(BigDecimal.ZERO);

            AtomicReference<BigDecimal> totalOrderQuantity = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> totalOrderAmount = new AtomicReference<>(BigDecimal.ZERO);
            List<OrdDirDeliveryDetail> deliveryOrderDetailsList = Lists.newArrayList();
            value.forEach(requestOrderDetail -> {
                // 赠品
                List<OrdDirDelivRequestDetail> ordDirDelivRequestDetails = giftMap.get(requestOrderDetail.getGoodsCode());
                if (CollectionUtils.isNotEmpty(ordDirDelivRequestDetails)) {
                    ordDirDelivRequestDetails.forEach(ordDirDelivRequestDetail -> {
                        totalOrderQuantity.getAndSet(totalOrderQuantity.get().add(ordDirDelivRequestDetail.getQuantity()));
                        totalOrderAmount.getAndSet(totalOrderAmount.get().add(Objects.isNull(ordDirDelivRequestDetail.getRequestOrderAmount()) ? BigDecimal.ZERO : ordDirDelivRequestDetail.getRequestOrderAmount()));
                    });
                }

                // 集货数量
//                if (Objects.isNull(deliveryOrder.getOrderQuantity())) {
//                    deliveryOrder.setOrderQuantity(requestOrderDetail.getQuantity());
//                } else {
//                    deliveryOrder.setOrderQuantity(deliveryOrder.getOrderQuantity().add(requestOrderDetail.getQuantity()));
//                }
                totalOrderQuantity.getAndSet(totalOrderQuantity.get().add(requestOrderDetail.getQuantity()));

                // 集货金额
                BigDecimal orderAmount = requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity());
//                if (Objects.isNull(deliveryOrder.getOrderAmount())) {
//                    deliveryOrder.setOrderAmount(orderAmount);
//                } else {
//                    deliveryOrder.setOrderAmount(deliveryOrder.getOrderAmount().add(orderAmount));
//                }
                totalOrderAmount.getAndSet(totalOrderAmount.get().add(orderAmount));

                OrdDirDeliveryDetail deliveryOrderDetails = this.initOrdDirDeliveryDetailByRequestDtl(deliveryOrder, requestOrderDetail, bizOrgCode, storeOut.getStoreCode(), stockInfoOut.getBizOrgCode());
                deliveryOrderDetailsList.add(deliveryOrderDetails);
                if (giftMap.containsKey(requestOrderDetail.getGoodsCode())) {
                    giftMap.get(requestOrderDetail.getGoodsCode()).forEach(item -> deliveryOrderDetailsList.add(this.initOrdDirDeliveryDetailByRequestDtl(deliveryOrder, item, bizOrgCode, storeOut.getStoreCode(), stockInfoOut.getBizOrgCode())));
                }
            });
//            deliveryOrder.setOrderAmount(deliveryOrder.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            deliveryOrder.setOrderQuantity(totalOrderQuantity.get());
            deliveryOrder.setOrderAmount(totalOrderAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            ordDirDeliveryService.insert(deliveryOrder);
            deliveryOrderList.add(deliveryOrder);
            ordDirDeliveryDetailService.save(deliveryOrderDetailsList, deliveryOrder, orderPriority);
            String goodsStockSupplyRateKey = SystemConstant.STOCK_SUPPLY_RATE_TRUNCATION_DATE_TIME_KEY + SystemConstant.COLON + truncationDateTime.replace(SystemConstant.COLON, SystemConstant.SHORT_LINE);
            List<OrdStoreInventorySupplyRate> goodsStockSupplyRateOutList = Lists.newArrayList();
            deliveryOrderDetailsList.forEach(ordDisDeliveryDetail -> {
                Object goodsStockSupplyRateObj = redisService.hGet(goodsStockSupplyRateKey, ordDisDeliveryDetail.getGoodsCode() + SystemConstant.SHORT_LINE + deliveryOrder.getStockCode());
                log.info("配货单{}截单优化占库存商品{}-------redis数据{}", deliveryOrder.getDeliveryOrderNo(), ordDisDeliveryDetail.getGoodsCode(), Objects.isNull(goodsStockSupplyRateObj) ? "---" : goodsStockSupplyRateObj.toString());
                if (Objects.nonNull(goodsStockSupplyRateObj)) {
                    GoodsStockSupplyRateOut goodsStockSupplyRateOut = JSONObject.toJavaObject(JSONObject.parseObject(goodsStockSupplyRateObj.toString()), GoodsStockSupplyRateOut.class);
                    BigDecimal supplyRate = goodsStockSupplyRateOut.getSupplyRate();
                    OrdStoreInventorySupplyRate ordStoreInventorySupplyRate = new OrdStoreInventorySupplyRate();
                    ordStoreInventorySupplyRate.setOrderCycleTime(requestOrder.getTruncationDateTime());
                    ordStoreInventorySupplyRate.setStoreCode(deliveryOrder.getStoreCode());
                    ordStoreInventorySupplyRate.setDeliveryOrderId(deliveryOrder.getId());
                    ordStoreInventorySupplyRate.setGoodsCode(ordDisDeliveryDetail.getGoodsCode());
                    ordStoreInventorySupplyRate.setSupplyRate(supplyRate);
                    if (supplyRate.compareTo(BigDecimal.ONE) >= NumberUtil.INTEGER_ZERO || supplyRate.compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO) {
                        ordStoreInventorySupplyRate.setBeginInventoryQuantity(ordDisDeliveryDetail.getOrderQuantity());
                        ordStoreInventorySupplyRate.setAllocationQuantity(ordDisDeliveryDetail.getOrderQuantity());
                    } else {
                        ordStoreInventorySupplyRate.setBeginInventoryQuantity(supplyRate.multiply(ordDisDeliveryDetail.getOrderQuantity()));
                        BigDecimal qty = ordStoreInventorySupplyRate.getBeginInventoryQuantity().setScale(NumberUtil.INTEGER_ZERO, RoundingMode.UP);
                        int roundPagckageNumber = qty.intValue() / ordDisDeliveryDetail.getDistributionSpecificationNum().intValue();
                        double remainder = qty.doubleValue() % ordDisDeliveryDetail.getDistributionSpecificationNum().intValue();
                        if (remainder != NumberUtil.INTEGER_ZERO) {
                            roundPagckageNumber++;
                        }
                        BigDecimal allocationQuantity = new BigDecimal(roundPagckageNumber).multiply(ordDisDeliveryDetail.getDistributionSpecificationNum());
                        ordStoreInventorySupplyRate.setAllocationQuantity(allocationQuantity);
                    }
                    ordStoreInventorySupplyRate.setCreator(deliveryOrder.getCreator());
                    ordStoreInventorySupplyRate.setCreateTime(LocalDateTime.now());
                    goodsStockSupplyRateOutList.add(ordStoreInventorySupplyRate);
                }
            });
            ordStoreInventorySupplyRateService.batchSave(goodsStockSupplyRateOutList);
        });
        return deliveryOrderList;
    }

    /**
     * 根据配销单获取关联要货单订货周期
     *
     * @param deliveryOrder
     */
    public void getAutoTakeDeliveryRuleByDeliveryOrder(OrdDirDelivery deliveryOrder) {
        OrdDirDelivRequest dirDelivRequest = requestOrderHandle.getRequestOrderByDeliveryOrderIdAndBizOrgCode(deliveryOrder.getId(), deliveryOrder.getBizOrgCode());
        if (Objects.nonNull(dirDelivRequest)) {
            Integer orderCycleId = dirDelivRequest.getOrderCycleId();
            this.initAutoTakeDeliveryTimeAtDelivered(orderCycleId, deliveryOrder);
        }
    }

    /**
     * 设置自动收货时间
     *
     * @param orderCycleId
     * @param deliveryOrder
     */
    public void initAutoTakeDeliveryTimeAtDelivered(Integer orderCycleId, OrdDirDelivery deliveryOrder) {
        if (!DeliveryOrderEnum.SHIPPED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            throw new BusinessException("配货单" + deliveryOrder.getDeliveryOrderNo() + "设置自动收货时间时状态不正确");
        }
        DirOrderProcessConfigItem autoTakeDeliveryRuleItem = dirDeliveryOrderConfigHandle.getAutoTakeDeliveryRule(orderCycleId, deliveryOrder.getBizOrgCode());
        if (Objects.isNull(autoTakeDeliveryRuleItem)) {
            OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, deliveryOrder.getBizOrgCode());
            log.info("配货单：{}，门店" + orderCycle.getStoreCode() + "的" + orderCycle.getShortOrderType() + "（" + orderCycle.getTruncationDateTime() + "）" + "未找到配货单是否自动收货配置项", deliveryOrder.getDeliveryOrderNo());
            return;
        }
        // 如果是非自动收货
        if (OrderCycleProcessConfigItemCodeEnum.MANUAL_RECEIVE.getCode().equals(autoTakeDeliveryRuleItem.getItemCode())) {
            return;
        }
        log.info("配货单{}即将设置自动收货时间", deliveryOrder.getDeliveryOrderNo());
        if (StringUtils.isNotBlank(autoTakeDeliveryRuleItem.getItemValue())) {
            Integer value = Integer.parseInt(autoTakeDeliveryRuleItem.getItemValue());
            LocalDateTime autoTakeDeliveryTime = deliveryOrder.getDeliveryTime().plusHours(value);
            deliveryOrder.setAutoTakeDeliveryTime(autoTakeDeliveryTime);
            log.info("配货单{}自动收货时间1------------------设置为{}", deliveryOrder.getDeliveryOrderNo(), deliveryOrder.getAutoTakeDeliveryTime());
        }
    }


    /**
     * 初始化配销单明细
     *
     * @param deliveryOrder
     * @param requestOrderDetail
     * @param bizOrgCode
     * @param storeCode
     * @return
     */
    private OrdDirDeliveryDetail initOrdDirDeliveryDetailByRequestDtl(OrdDirDelivery deliveryOrder, OrdDirDelivRequestDetail requestOrderDetail,
                                                                      String bizOrgCode, String storeCode, String centerStockBizOrgCode) {
        OrdDirDeliveryDetail deliveryOrderDetails = new OrdDirDeliveryDetail();
        BeanUtils.copy(requestOrderDetail, deliveryOrderDetails);

        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setGoodsCode(requestOrderDetail.getGoodsCode());
        orderGoodsIn.setStoreCode(storeCode);
        OrderGoodsOut orderGoods = orderGoodsServer.getOrderGoods(orderGoodsIn);
        deliveryOrderDetails.setSellTax(Objects.isNull(orderGoods) ? BigDecimal.ZERO : orderGoods.getOutTax());
        deliveryOrderDetails.setDistributionPrice(Objects.isNull(orderGoods) ? BigDecimal.ZERO : orderGoods.getDistributionUnitPrice());
        deliveryOrderDetails.setVendorCode(Objects.isNull(orderGoods) ? "" : orderGoods.getVendorCode());
        deliveryOrderDetails.setDeliveryOrderId(deliveryOrder.getId());
        deliveryOrderDetails.setOrderQuantity(requestOrderDetail.getQuantity());
        deliveryOrderDetails.setOrderPackageQuantity(requestOrderDetail.getPackageQuantity());
        deliveryOrderDetails.setOrderUnitPrice(requestOrderDetail.getOriginalUnitPrice());
        deliveryOrderDetails.setOrderAmount(requestOrderDetail.getOriginalUnitPrice().multiply(requestOrderDetail.getQuantity()));
        deliveryOrderDetails.setDistributionUnitPrice(requestOrderDetail.getOriginalUnitPrice());
        BigDecimal sellTax = null == deliveryOrderDetails.getSellTax() ? BigDecimal.ZERO : deliveryOrderDetails.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        deliveryOrderDetails.setDistributionExceptTaxAmount(deliveryOrderDetails.getOrderAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        deliveryOrderDetails.setDistributionTaxAmount(deliveryOrderDetails.getOrderAmount().subtract(deliveryOrderDetails.getDistributionExceptTaxAmount()));

        deliveryOrderDetails.setIsDelete(ModelConst.DELETE.NO);
        BigDecimal stockWarehousePrice = warehouseServer.getWarehousePrice(deliveryOrder.getWrhCode(), deliveryOrder.getStockCode(),
                deliveryOrderDetails.getGoodsCode(), centerStockBizOrgCode, deliveryOrderDetails.getVendorCode());
        deliveryOrderDetails.setWrhPrice(Objects.isNull(stockWarehousePrice) ? BigDecimal.ZERO : stockWarehousePrice);
        BigDecimal stockStorePrice = warehouseServer.getStockPrice(storeCode, deliveryOrderDetails.getGoodsCode(), bizOrgCode);
        deliveryOrderDetails.setStoreStockPrice(Objects.isNull(stockStorePrice) ? BigDecimal.ZERO : stockStorePrice);
        deliveryOrderDetails.setInvoiceType(Objects.isNull(orderGoods) ? null : orderGoods.getInvoiceType());
        return deliveryOrderDetails;
    }


    /**
     * 封装拆分的key
     *
     * @param splitItemOutList
     * @param stockCode
     * @param distributionType
     * @param goodsType
     * @return
     */
    private String getSplitKeyBySplitItemList(List<OrderProcessConfigItemOut> splitItemOutList, String stockCode, String distributionType, String goodsType) {
        StringJoiner keyJoiner = new StringJoiner(SystemConstant.SHORT_LINE);
        splitItemOutList.forEach(orderProcessConfigItemOut -> {
            String itemCode = orderProcessConfigItemOut.getItemCode();
            if (OrderCycleProcessConfigItemCodeEnum.DELIVERY_SPLIT_POSITION.getCode().equals(itemCode)) {
                keyJoiner.add(stockCode);
            }
            if (OrderCycleProcessConfigItemCodeEnum.DELIVERY_SPLIT_DISTRIBUTION_TYPE.getCode().equals(itemCode)) {
                keyJoiner.add(distributionType);
            }
            if (OrderCycleProcessConfigItemCodeEnum.GOODS_TYPE.getCode().equals(itemCode)) {
                keyJoiner.add(goodsType);
            }
        });
        return keyJoiner.toString();
    }

    /**
     * 初始化中转配货单发送采购入参
     *
     * @return
     */
    public TransferDirDeliveryOrderOut initTransferNoticePurchaseIn(List<OrdDirDelivery> dirDeliveries, String bizOrgCode) {
        List<TransferNoticePurchaseIn> transferNoticePurchaseIns = new ArrayList<>();
        List<TransferDeliveryOrderDetailOut> transferDeliveryOrderDetails = new ArrayList<>();
        String forwardCycle = bizOrgCode + DateUtil.format(LocalDateTime.now(), "yyMMdd");
        //结转周期
        String carryForwardCycle = forwardCycle + CreateCodeUtil.getCode(forwardCycle, uniqueUtils);
        if (CollectionUtils.isNotEmpty(dirDeliveries)) {
            //查询所有中转商品配货单明细
            List<Long> idList = dirDeliveries.stream().map(OrdDirDelivery::getId).collect(Collectors.toList());
            transferDeliveryOrderDetails = ordDirDeliveryDetailService.findTransferDeliveryOrderDetails(idList);
        }

        if (CollectionUtils.isNotEmpty(transferDeliveryOrderDetails)) {
            Map<String, List<TransferDeliveryOrderDetailOut>> vendorDetails = transferDeliveryOrderDetails.stream().collect(Collectors.groupingBy(item ->
                    item.getVendorCode() + "_" + item.getWarehouseCode() + "_" + item.getStockCode() + "_" + carryForwardCycle + "_" + item.getOrderPriority()
            ));
            StringJoiner stringJoiner = new StringJoiner(";");
            for (String key : vendorDetails.keySet()) {
                stringJoiner.add(key);
            }
            log.info("直营加推发送采购拆分采购单参数key" + stringJoiner + "---------------------------------------------------");
            for (List<TransferDeliveryOrderDetailOut> transferDeliveryOrderDetailOuts : vendorDetails.values()) {
                TransferNoticePurchaseIn transferNoticePurchaseIn = new TransferNoticePurchaseIn();
                if (CollectionUtils.isNotEmpty(transferDeliveryOrderDetailOuts)) {
                    TransferDeliveryOrderDetailOut transferDeliveryOrderDetailOut = transferDeliveryOrderDetailOuts.get(NumberUtil.INTEGER_ZERO);
                    BeanUtils.copy(transferDeliveryOrderDetailOut, transferNoticePurchaseIn);
                    transferNoticePurchaseIn.setCarryForwardCycle(carryForwardCycle);
                    List<GoodsDtlsIn> goodsDtlsInList = new ArrayList<>();
                    //行号
                    int lineNo = 1;
                    for (int i = 0; i < transferDeliveryOrderDetailOuts.size(); i++) {
                        String goodsCode = transferDeliveryOrderDetailOuts.get(i).getGoodsCode();
                        GoodsDtlsIn goodsMessage = goodsDtlsInList.stream().filter(e -> e.getGoodsCode().equals(goodsCode)).findFirst().orElse(null);
                        if (Objects.isNull(goodsMessage)) {
                            GoodsDtlsIn goodsDtlsIn = new GoodsDtlsIn();
                            goodsDtlsIn.setGoodsCode(goodsCode);
                            goodsDtlsIn.setLineNo(lineNo);
                            goodsDtlsIn.setTaxRate(transferDeliveryOrderDetailOuts.get(i).getSellTax().toString());
                            goodsDtlsIn.setTotalQty(transferDeliveryOrderDetailOuts.get(i).getOrderQuantity());
                            goodsDtlsInList.add(goodsDtlsIn);
                        } else {
                            goodsMessage.setTotalQty(goodsMessage.getTotalQty().add(transferDeliveryOrderDetailOuts.get(i).getOrderQuantity()));
                            goodsDtlsInList.removeIf(e -> e.getGoodsCode().equals(goodsCode));
                            goodsDtlsInList.add(goodsMessage);
                        }
                        lineNo++;
                    }
                    transferNoticePurchaseIn.setGoodsDtls(goodsDtlsInList);
                    transferNoticePurchaseIns.add(transferNoticePurchaseIn);
                }
            }
        }

        List<OrdDirDeliveryDetail> dirDeliveryDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(transferDeliveryOrderDetails)) {
            String finalCarryForwardCycle = carryForwardCycle;
            transferDeliveryOrderDetails.forEach(item -> {
                OrdDirDeliveryDetail ordDirDeliveryDetail = new OrdDirDeliveryDetail();
                BeanUtils.copy(item, ordDirDeliveryDetail);
                ordDirDeliveryDetail.setCarryForwardCycle(finalCarryForwardCycle);
                dirDeliveryDetails.add(ordDirDeliveryDetail);
            });
        }
        return TransferDirDeliveryOrderOut.builder().transferNoticePurchaseIns(transferNoticePurchaseIns)
                .carryForwardCycle(carryForwardCycle)
                .orderDirDeliveryDetails(dirDeliveryDetails).build();
    }

    /**
     * 处理配货中转采购生成采购单业务
     */
    public void transferDeliveryDirOrder(List<OrdDirDelivery> dirDeliveries, String bizOrgCode) {
        try {
            TransferDirDeliveryOrderOut TransferDirDeliveryOrderOut = this.initTransferNoticePurchaseIn(dirDeliveries, bizOrgCode);
            List<TransferNoticePurchaseIn> transferNoticePurchaseIns = TransferDirDeliveryOrderOut.getTransferNoticePurchaseIns();
            if (CollectionUtils.isEmpty(transferNoticePurchaseIns)) {
                log.info("配货加推订货单没有中转商品");
                return;
            }
            log.info("配货加推订货单中转商品条数是---{}", transferNoticePurchaseIns.size());
            log.info("调用配货加推订货单中转发采购单入参------{}", JSONArray.toJSONString(transferNoticePurchaseIns));
            // TODO lock
            Response response = purchaseOrderClient.saveDistributionOrder(transferNoticePurchaseIns);
            if (!response.isSuccess()) {
                log.error("配货加推订货单中转商品生成采购单失败" + (StringUtils.isNotBlank(response.getMessage()) ? response.getMessage() : null));
            } else {
                //发送采购成功修改订单明细的结算周期
                if (CollectionUtils.isNotEmpty(TransferDirDeliveryOrderOut.getOrderDirDeliveryDetails())) {
                    ordDirDeliveryDetailService.batchUpdate(TransferDirDeliveryOrderOut.getOrderDirDeliveryDetails());
                }
            }
        } catch (Exception e) {
            log.error("执行配销加推订货单中转商品生成采购单任务异常：", e);
        }
        log.info("配销加推订货单执行中转商品生成采购单任务结束");
    }

    /**
     * @Description: 配货单回传
     * @Author: ZhangYao
     * @Date: 2024/3/19 14:49
     * @param dirDelivery:
     * @param ordDirDeliveryDetails:
     * @param unificationBillVO:
     * @param standardGoodsMap:
     * @return: java.lang.String
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean unificationOrderCallBack(OrdDirDelivery dirDelivery, List<OrdDirDeliveryDetail> ordDirDeliveryDetails,
                                           UnificationBillVO unificationBillVO, Map<String, StandardGoodsInfoOut> standardGoodsMap) {
        StockInfoOut stockInfoOut = stockServer.getTransInfo(dirDelivery.getStockCode());
        // 更新配货单明细
        ordDirDeliveryDetailService.updateByDtsDtlList(dirDelivery, ordDirDeliveryDetails, unificationBillVO.getDetail(), standardGoodsMap, stockInfoOut.getBizOrgCode());
        BigDecimal deliveryQuantity = ordDirDeliveryDetails.stream().filter(disDeliveryDetail -> Objects.nonNull(disDeliveryDetail.getDeliveryQuantity()))
                .map(OrdDirDeliveryDetail::getDeliveryQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("配货单{}实配数量{}", dirDelivery.getDeliveryOrderNo(), deliveryQuantity);
        dirDelivery.setDeliveryQuantity(deliveryQuantity);
        BigDecimal deliveryAmount = ordDirDeliveryDetails.stream().filter(disDeliveryDetail -> Objects.nonNull(disDeliveryDetail.getDeliveryAmount()))
                .map(OrdDirDeliveryDetail::getDeliveryAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("配货单{}实配金额{}", dirDelivery.getDeliveryOrderNo(), deliveryAmount);
        dirDelivery.setDeliveryAmount(deliveryAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        //已发货状态
        dirDelivery.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        dirDelivery.setReceiveProgress(DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey());
        dirDelivery.setLogisticsNo(unificationBillVO.getNum());
        dirDelivery.setDeliveryTime(unificationBillVO.getFcreatetime());
        dirDelivery.setUpdater(SystemConstant.SYSTEM_USER);
        dirDelivery.setUpdateTime(LocalDateTime.now());
        //设置自动收货时间
        this.getAutoTakeDeliveryRuleByDeliveryOrder(dirDelivery);
        //更新配货单
        ordDirDeliveryService.updateByPrimaryKeySelective(dirDelivery);
        // 库存调整
        ordDirDeliveryService.optInvForDeliveryInfo(dirDelivery, ordDirDeliveryDetails, stockInfoOut);
        // 配销单dts回传订单追踪日志
        String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.WAREHOUSE_DELIVERED.getTemplate(), dirDelivery.getDeliveryOrderNo());
        ordDirOrderTrackService.pushRedisOrderTrackMessage(dirDelivery.getDeliveryOrderNo(), dirDelivery.getStoreCode(),
                OrderTrackStatusEnum.WAREHOUSE_DELIVERED.getName(), trackLog, dirDelivery.getBizOrgCode(), dirDelivery.getCreator(), dirDelivery.getCreateTime());
        //dts回传收货配货单时，改差异单后台已批准 记录日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIR_ORDER_SEND_DELIVERY_SYSTEM.getName(),
                String.valueOf(dirDelivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(),
                OrdLogTypeEnum.ORD_DIR_ORDER_SEND_DELIVERY_SYSTEM.getName(), new Date(), dirDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        return "成功";
        return true;
    }

    public List<OrdDirDelivery> findListByIdList(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        StringJoiner idJoiner = new StringJoiner(SystemConstant.COMMA);
        idList.forEach(id -> idJoiner.add(id.toString()));
        List<OrdDirDelivery> ordDirDeliveries = ordDirDeliveryService.selectByIds(idJoiner.toString());
        return ordDirDeliveries;
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleAutoTakeDirDelivery(TakeDirDeliveryOrderIn takeDirDeliveryOrderIn, OrdDirDelivery deliveryOrder, StockInfoOut stockInfoOut) {
        DirSignDeliveryOrderIn signDeliveryOrderIn = new DirSignDeliveryOrderIn();
        signDeliveryOrderIn.setDeliveryOrderId(deliveryOrder.getId());
        signDeliveryOrderIn.setDifferencesRemark("自动签收默认无差异");
        signDeliveryOrderIn.setOrgCode(deliveryOrder.getOrgCode());
        Response<String> signResponse = deliveryOrderSigningService.signDirDeliveryOrder(signDeliveryOrderIn, deliveryOrder, takeDirDeliveryOrderIn.getLoginUsername());
        if (Objects.nonNull(signResponse) && signResponse.isSuccess()) {
            takeDirDeliveryHandle.takeDirDelivery(takeDirDeliveryOrderIn, deliveryOrder, stockInfoOut);
        }
    }
}
