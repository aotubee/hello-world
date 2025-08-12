package com.edc.erp.handle;

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
import com.edc.erp.common.service.*;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.handle.DisDeliveryOrderSalvageHandle;
import com.edc.erp.disdeliveryorder.model.in.DisSignDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.in.TakeDisDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderSigningService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondDetailService;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDetail;
import com.edc.erp.disrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestDeliveryService;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestDetailService;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.erp.enumeration.*;
import com.edc.erp.orderscheduing.model.out.TransferDisDeliveryOrderOut;
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
 * @description: 配销单业务处理类
 * @since 2022/10/25 19:04
 */
@Service
@Slf4j
public class DisDeliveryOrderHandle {

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private DisDeliveryOrderConfigHandle disDeliveryOrderConfigHandle;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private OrdDisDelivRequestDetailService ordDisDelivRequestDetailService;

    @Autowired
    private OrdDisDelivRequestDeliveryService ordDisDelivRequestDeliveryService;

    @Autowired
    private DisRequestOrderHandle requestOrderHandle;

    @Autowired
    private UniqueUtils uniqueUtils;

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    @Autowired
    private OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    @Autowired
    private WarehouseServer warehouseServer;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private OrdDisSalvageDelivPondDetailService ordDisSalvageDelivPondDetailService;

    @Autowired
    private PurchaseOrderClient purchaseOrderClient;

    @Autowired
    private DisDeliveryOrderSalvageHandle disDeliveryOrderSalvageHandle;

    @Autowired
    private OrdDisOrderTrackService ordDisOrderTrackService;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrdStoreInventorySupplyRateService ordStoreInventorySupplyRateService;

    @Autowired
    private StockServer stockServer;

    @Autowired
    private TakeDisDeliveryHandle takeDisDeliveryHandle;

    @Autowired
    private OrdDisDeliveryOrderSigningService deliveryOrderSigningService;

    /**
     * 创建配销单
     *
     * @param requestOrderCreateMqIn 创建集货单发送MQ信息入参
     * @param configItemMap
     */
    @Transactional(rollbackFor = Exception.class)
    public List<OrdDisDelivery> createDeliveryOrder(RequestOrderCreateMqIn requestOrderCreateMqIn, StoreOut storeOut,
                                                    StoreLogisticsOut logistics, Map<String, StockInfoOut> stockMap, Map<String, List<OrderProcessConfigItemOut>> configItemMap) {
        String bizOrgCode = requestOrderCreateMqIn.getBizOrgCode();
        Integer orderCycleId = requestOrderCreateMqIn.getOrderCycleId();
        Long requestOrderId = requestOrderCreateMqIn.getRequestOrderId();
        OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, bizOrgCode);
//        //获取拆分配销单规则
//        List<OrderProcessConfigItemOut> splitRuleItemOutList = disDeliveryOrderConfigHandle.findSplitRule(orderCycleId, bizOrgCode);
//        if (CollectionUtils.isEmpty(splitRuleItemOutList)) {
//            log.info("门店" + orderCycle.getStoreCode() + "的" + orderCycle.getShortOrderType() + "（" + orderCycle.getTruncationDateTime() + "）" + "未找到配销单拆分规则配置项");
//            return null;
//        }
        List<OrdDisDelivery> deliveryOrderList;
//        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(orderCycle.getStoreCode());
        List<OrdDisDelivRequestDetail> requestOrderDetailList = ordDisDelivRequestDetailService.findByRequestOrderIdAndBizOrgCode(requestOrderId);
        // 更新要货单
        OrdDisDelivRequest requestOrder = requestOrderHandle.getRequestOrderByRequestOrderId(requestOrderId);
        // 仓位+配送方式模式拆分
        deliveryOrderList = this.splitByPositionAndDistributionType(storeOut, bizOrgCode, requestOrder, requestOrderDetailList, configItemMap, stockMap, DateUtils.format(requestOrder.getTruncationDateTime()));

        requestOrder.setStatusCode(RequestOrderStatusEnum.EXCRETED.getKey());
        requestOrder.setUpdater(SystemConstant.SYSTEM_USER);
        requestOrder.setUpdateTime(LocalDateTime.now());
        requestOrderHandle.updateByPrimaryKeySelective(requestOrder);
        // 创建配销单与要货单关联
        ordDisDelivRequestDeliveryService.batchSave(deliveryOrderList, requestOrderId);
        // 天岁接入ERP，不再对接中科接口
//        if (OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(requestOrderCreateMqIn.getBizOrgCode())) {
//            deliveryOrderList.forEach(ordDisDelivery -> {
//                boolean isAbutmentWms = stockServer.isAbutmentWms(ordDisDelivery.getStockCode(), ordDisDelivery.getBizOrgCode());
//                if (isAbutmentWms) {
//                    return;
//                }
//                DisDeliveryTaskIn disDeliveryTaskIn = new DisDeliveryTaskIn();
//                disDeliveryTaskIn.setDeliveryOrderNo(ordDisDelivery.getDeliveryOrderNo());
//                disDeliveryTaskIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
//                disDeliveryTaskIn.setIsNeedPay(false);
//                disDeliveryTaskIn.setLoginUsername(ordDisDelivery.getCreator());
//                asyncPushTaskService.submit(AsyncTaskConstant.Type.DELIVERY_SEND_ZK, JSONObject.toJSONString(disDeliveryTaskIn), ordDisDelivery.getBizOrgCode(), ordDisDelivery.getDeliveryOrderNo());
//            });
//        }
//        else {
//            // 不进捞单池占用库存
//            DirDeliveryOccupyInventoryIn dirDeliveryOccupyInventoryIn = new DirDeliveryOccupyInventoryIn();
//            dirDeliveryOccupyInventoryIn.setBizOrgCode(bizOrgCode);
//            dirDeliveryOccupyInventoryIn.setLoginUsername(requestOrderCreateMqIn.getLoginUsername());
//            dirDeliveryOccupyInventoryIn.setDeliveryOrderIdList(deliveryOrderList.stream().map(OrdDisDelivery::getId).collect(Collectors.toList()));
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DELIVERY_OCCUPY_INV, JSONObject.toJSONString(dirDeliveryOccupyInventoryIn), bizOrgCode);
//        }
        // 2023-09-06 捞单占库存太耗时影响物流作业，暂时弃用进入捞单池 - begin
//        else {
        // 创建捞单池明细
        List<OrdDisDelivery> salvageDeliveryOrderList = deliveryOrderList.stream()
                .filter(ordDisDelivery -> DeliveryOrderEnum.PENDING.getKey().equals(ordDisDelivery.getDeliveryStatusCode())
                        && DistributionWaysEnum.UNIFIEDDIS.getType().equals(ordDisDelivery.getDistributionType())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(salvageDeliveryOrderList)) {
            // 创建捞单池记录
            ordDisSalvageDelivPondDetailService.saveOrdDisSalvageDelivPondDetails(salvageDeliveryOrderList, orderCycle,
                    requestOrderCreateMqIn.getLoginUsername(), requestOrderCreateMqIn.getAuditType(), logistics);
            // 如果是立即自动审核，则审核占库存
            if (SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode().equals(requestOrderCreateMqIn.getAuditType())) {
                int executeCount = disDeliveryOrderSalvageHandle.handleSalvageAfterSplitDeliveryOrder(salvageDeliveryOrderList,
                        bizOrgCode, requestOrderCreateMqIn.getLoginUsername());
                log.info("待处理{}个配销单，处理成功{}个配销单审核占库存", salvageDeliveryOrderList.size(), executeCount);
            }
        }
//        }
        // 2023-09-06 捞单占库存太耗时影响物流作业，暂时弃用进入捞单池 - end
        return deliveryOrderList;
    }


    /**
     * 根据配销单获取关联集货单订货周期
     *
     * @param deliveryOrder
     */
    public void getAutoTakeDeliveryRuleByDeliveryOrder(OrdDisDelivery deliveryOrder) {
        OrdDisDelivRequest disDelivRequest = requestOrderHandle.getRequestOrderByDeliveryOrderIdAndBizOrgCode(deliveryOrder.getId(), deliveryOrder.getBizOrgCode());
        if (Objects.nonNull(disDelivRequest)) {
            Integer orderCycleId = disDelivRequest.getOrderCycleId();
            this.initAutoTakeDeliveryTimeAtDelivered(orderCycleId, deliveryOrder);
        }
    }

    /**
     * 设置自动收货时间
     *
     * @param orderCycleId
     * @param deliveryOrder
     */
    public void initAutoTakeDeliveryTimeAtDelivered(Integer orderCycleId, OrdDisDelivery deliveryOrder) {
        if (!DeliveryOrderEnum.SHIPPED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            throw new BusinessException("配销单" + deliveryOrder.getDeliveryOrderNo() + "设置自动收货时间时状态不正确");
        }
        OrderProcessConfigItemOut autoTakeDeliveryRuleItem = disDeliveryOrderConfigHandle.getAutoTakeDeliveryRule(orderCycleId, deliveryOrder.getBizOrgCode());
        if (Objects.isNull(autoTakeDeliveryRuleItem)) {
            OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, deliveryOrder.getBizOrgCode());
            log.info("配销单：{}，门店" + orderCycle.getStoreCode() + "的" + orderCycle.getShortOrderType() + "（" + orderCycle.getTruncationDateTime() + "）" + "未找到配销单是否自动收货配置项", deliveryOrder.getDeliveryOrderNo());
            return;
        }
        // 如果是非自动收货
        if (OrderCycleProcessConfigItemCodeEnum.MANUAL_RECEIVE.getCode().equals(autoTakeDeliveryRuleItem.getItemCode())) {
            return;
        }
        log.info("配销单{}即将设置自动收货时间", deliveryOrder.getDeliveryOrderNo());
        if (StringUtils.isNotBlank(autoTakeDeliveryRuleItem.getItemValue())) {
            Integer value = Integer.parseInt(autoTakeDeliveryRuleItem.getItemValue());
            LocalDateTime autoTakeDeliveryTime = deliveryOrder.getDeliveryTime().plusHours(value);
            deliveryOrder.setAutoTakeDeliveryTime(autoTakeDeliveryTime);
            log.info("配销单{}自动收货时间1------------------设置为{}", deliveryOrder.getDeliveryOrderNo(), deliveryOrder.getAutoTakeDeliveryTime());
        }
    }


    /**
     * 按照仓位和配送方式拆分配销单
     *
     * @param storeOut               门店对象
     * @param bizOrgCode             业务组织代码
     * @param requestOrderDetailList 要货单明细实体集合
     * @param configItemMap          拆分规则选项
     */
    private List<OrdDisDelivery> splitByPositionAndDistributionType(StoreOut storeOut, String bizOrgCode, OrdDisDelivRequest disDelivRequest,
                                                                    List<OrdDisDelivRequestDetail> requestOrderDetailList,
                                                                    Map<String, List<OrderProcessConfigItemOut>> configItemMap,
                                                                    Map<String, StockInfoOut> stockMap, String truncationDateTime) {
        Map<String, List<OrdDisDelivRequestDetail>> requestOrderDetailsMap = new LinkedHashMap<>();
        // 赠品集合
        Map<String, List<OrdDisDelivRequestDetail>> giftMap = new HashMap<>();
        for (OrdDisDelivRequestDetail orderDetail : requestOrderDetailList) {
            if (NumberUtils.INTEGER_ONE.equals(orderDetail.getIsGift())) {
                List<OrdDisDelivRequestDetail> giftList = giftMap.containsKey(orderDetail.getBaseGoodsCode()) ? giftMap.get(orderDetail.getBaseGoodsCode()) : new ArrayList<>();
                giftList.add(orderDetail);
                giftMap.put(orderDetail.getBaseGoodsCode(), giftList);
                continue;
            }
            String key = getSplitKeyBySplitItemList(configItemMap.get(OrderCycleProcessConfigCodeEnum.DELIVERY_SPLIT_CONDITION.getCode()), orderDetail.getStockCode(), orderDetail.getDistributionType(), orderDetail.getGoodsType());
            List<OrdDisDelivRequestDetail> splitList = requestOrderDetailsMap.get(key);
            if (CollectionUtils.isEmpty(splitList)) {
                splitList = Lists.newArrayList();
            }
            splitList.add(orderDetail);
            requestOrderDetailsMap.put(key, splitList);
        }
        List<OrdDisDelivery> deliveryOrderList = Lists.newArrayList();
        requestOrderDetailsMap.forEach((key, value) -> {
            String[] splits = key.split(SystemConstant.SHORT_LINE);
            OrdDisDelivery deliveryOrder = new OrdDisDelivery();
            deliveryOrder.setDeliveryOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PX.getCode(), bizOrgCode, uniqueUtils, 4));
            deliveryOrder.setStockCode(splits[0]);
            deliveryOrder.setDistributionType(splits[1]);
            if (splits.length == NumberUtil.INTEGER_THREE) {
                deliveryOrder.setGoodsType(splits[NumberUtil.INTEGER_TWO]);
            }
            deliveryOrder.setStoreCode(storeOut.getStoreCode());
            deliveryOrder.setStoreName(storeOut.getStoreName());
            deliveryOrder.setSourceCode(DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType());
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
            deliveryOrder.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(bizOrgCode));
            deliveryOrder.setSkuCount(value.size());
            deliveryOrder.setRequestOrderNo(disDelivRequest.getRequestOrderNo());
//            String orderPriority = storeCenterService.getOrderPriorityByStoreCode(storeOut.getStoreCode(), bizOrgCode, deliveryOrder.getStockCode(), deliveryOrder.getDistributionType());
            String orderPriority = configItemMap.containsKey(OrderCycleProcessConfigCodeEnum.DELIVERY_ORDER_PRIORITY.getCode()) ? configItemMap.get(OrderCycleProcessConfigCodeEnum.DELIVERY_ORDER_PRIORITY.getCode()).get(0).getItemName() : null;
            deliveryOrder.setOrderPriority(orderPriority);
            //集货数量
//            deliveryOrder.setOrderQuantity(value.stream().map(OrdDisDelivRequestDetail::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
            //集货金额
//            deliveryOrder.setOrderAmount(value.stream().map(item -> item.getOriginalUnitPrice().multiply(item.getQuantity())).reduce(BigDecimal.ZERO, BigDecimal::add));
            //配销数量
            deliveryOrder.setDistributionQuantity(BigDecimal.ZERO);
            //配销金额
            deliveryOrder.setDistributionAmount(BigDecimal.ZERO);
            AtomicReference<BigDecimal> totalOrderQuantity = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> totalOrderAmount = new AtomicReference<>(BigDecimal.ZERO);
            List<OrdDisDeliveryDetail> deliveryOrderDetailsList = Lists.newArrayList();
            value.forEach(requestOrderDetail -> {
                // 赠品
                List<OrdDisDelivRequestDetail> ordDisDelivRequestDetails = giftMap.get(requestOrderDetail.getGoodsCode());
                if (CollectionUtils.isNotEmpty(ordDisDelivRequestDetails)) {
                    ordDisDelivRequestDetails.forEach(ordDisDelivRequestDetail -> {
                        totalOrderQuantity.getAndSet(totalOrderQuantity.get().add(ordDisDelivRequestDetail.getQuantity()));
                        totalOrderAmount.getAndSet(totalOrderAmount.get().add(Objects.isNull(ordDisDelivRequestDetail.getRequestOrderAmount()) ? BigDecimal.ZERO : ordDisDelivRequestDetail.getRequestOrderAmount()));
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

                OrdDisDeliveryDetail deliveryOrderDetails = this.initOrdDisDeliveryDetailByRequestDtl(deliveryOrder, requestOrderDetail, bizOrgCode, storeOut.getStoreCode(), stockInfoOut.getBizOrgCode());
                deliveryOrderDetailsList.add(deliveryOrderDetails);
                if (giftMap.containsKey(requestOrderDetail.getGoodsCode())) {
                    giftMap.get(requestOrderDetail.getGoodsCode()).forEach(item -> deliveryOrderDetailsList.add(this.initOrdDisDeliveryDetailByRequestDtl(deliveryOrder, item, bizOrgCode, storeOut.getStoreCode(), stockInfoOut.getBizOrgCode())));
                }
            });
//            deliveryOrder.setOrderAmount(deliveryOrder.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            deliveryOrder.setOrderQuantity(totalOrderQuantity.get());
            deliveryOrder.setOrderAmount(totalOrderAmount.get().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            deliveryOrder.setOrderAmount(deliveryOrder.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            deliveryOrder.setFreezeStatus(DeliveryOrderFreezeEnum.UN_FREEZE.getKey());
            ordDisDeliveryService.insert(deliveryOrder);
            deliveryOrderList.add(deliveryOrder);
            ordDisDeliveryDetailService.save(deliveryOrderDetailsList, deliveryOrder, orderPriority);
            String goodsStockSupplyRateKey = SystemConstant.STOCK_SUPPLY_RATE_TRUNCATION_DATE_TIME_KEY + SystemConstant.COLON + truncationDateTime.replace(SystemConstant.COLON, SystemConstant.SHORT_LINE);
            List<OrdStoreInventorySupplyRate> goodsStockSupplyRateOutList = Lists.newArrayList();
            for (OrdDisDeliveryDetail ordDisDeliveryDetail : deliveryOrderDetailsList) {
                Object goodsStockSupplyRateObj = redisService.hGet(goodsStockSupplyRateKey, ordDisDeliveryDetail.getGoodsCode() + SystemConstant.SHORT_LINE + deliveryOrder.getStockCode());
                if (Objects.nonNull(goodsStockSupplyRateObj)) {
                    GoodsStockSupplyRateOut goodsStockSupplyRateOut = JSONObject.toJavaObject(JSONObject.parseObject(goodsStockSupplyRateObj.toString()), GoodsStockSupplyRateOut.class);
                    BigDecimal supplyRate = goodsStockSupplyRateOut.getSupplyRate();
                    OrdStoreInventorySupplyRate ordStoreInventorySupplyRate = new OrdStoreInventorySupplyRate();
                    ordStoreInventorySupplyRate.setOrderCycleTime(disDelivRequest.getTruncationDateTime());
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
            }
            ordStoreInventorySupplyRateService.batchSave(goodsStockSupplyRateOutList);
        });
        return deliveryOrderList;
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
    private OrdDisDeliveryDetail initOrdDisDeliveryDetailByRequestDtl(OrdDisDelivery deliveryOrder, OrdDisDelivRequestDetail requestOrderDetail, String bizOrgCode, String storeCode, String centerStockBizOrgCode) {
        OrdDisDeliveryDetail deliveryOrderDetails = new OrdDisDeliveryDetail();
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
     * 初始化中转配销单发送采购入参
     *
     * @return
     */
    public TransferDisDeliveryOrderOut initTransferNoticePurchaseIn(List<OrdDisDelivery> disDeliveries, String bizOrgCode) {
        List<TransferNoticePurchaseIn> transferNoticePurchaseIns = new ArrayList<>();
        List<TransferDeliveryOrderDetailOut> transferDeliveryOrderDetails = new ArrayList<>();
        String forwardCycle = bizOrgCode + DateUtil.format(LocalDateTime.now(), "yyMMdd");
        String carryForwardCycle = forwardCycle + CreateCodeUtil.getCode(forwardCycle, uniqueUtils);

        if (CollectionUtils.isNotEmpty(disDeliveries)) {
            //查询所有中转商品配销单明细
            List<Long> idList = disDeliveries.stream().map(OrdDisDelivery::getId).collect(Collectors.toList());
            transferDeliveryOrderDetails = ordDisDeliveryDetailService.findTransferDeliveryOrderDetails(idList);
        }

        if (CollectionUtils.isNotEmpty(transferDeliveryOrderDetails)) {
            Map<String, List<TransferDeliveryOrderDetailOut>> vendorDetails = transferDeliveryOrderDetails.stream().collect(Collectors.groupingBy(item ->
                    item.getVendorCode() + "_" + item.getWarehouseCode() + "_" + item.getStockCode() + "_" + carryForwardCycle + "_" + item.getOrderPriority()
            ));
            StringJoiner stringJoiner = new StringJoiner(";");
            for (String key : vendorDetails.keySet()) {
                stringJoiner.add(key);
            }
            log.info("配销加推发送采购拆分采购单参数key" + stringJoiner + "---------------------------------------------------");
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

        List<OrdDisDeliveryDetail> disDeliveryDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(transferDeliveryOrderDetails)) {
            String finalCarryForwardCycle = carryForwardCycle;
            transferDeliveryOrderDetails.forEach(item -> {
                OrdDisDeliveryDetail ordDisDeliveryDetail = new OrdDisDeliveryDetail();
                BeanUtils.copy(item, ordDisDeliveryDetail);
                ordDisDeliveryDetail.setCarryForwardCycle(finalCarryForwardCycle);
                disDeliveryDetails.add(ordDisDeliveryDetail);
            });
        }
        return TransferDisDeliveryOrderOut.builder().transferNoticePurchaseIns(transferNoticePurchaseIns)
                .carryForwardCycle(carryForwardCycle)
                .orderDisDeliveryDetails(disDeliveryDetails).build();
    }

    /**
     * 处理配销中转采购生成采购单业务
     */
    @Transactional(rollbackFor = Exception.class)
    public void transferDeliveryDisOrder(List<OrdDisDelivery> disDeliveries, String bizOrgCode) {
        try {
            TransferDisDeliveryOrderOut transferDisDeliveryOrderOut = this.initTransferNoticePurchaseIn(disDeliveries, bizOrgCode);
            List<TransferNoticePurchaseIn> transferNoticePurchaseIns = transferDisDeliveryOrderOut.getTransferNoticePurchaseIns();
            if (CollectionUtils.isEmpty(transferNoticePurchaseIns)) {
                log.info("配销加推订货单没有中转商品");
                return;
            }
            log.info("配销加推订货单中转商品条数是---{}", transferNoticePurchaseIns.size());
            log.info("调用配销加推订货单中转发采购单入参------{}", JSONArray.toJSONString(transferNoticePurchaseIns));
            Response response = purchaseOrderClient.saveDistributionOrder(transferNoticePurchaseIns);
            if (!response.isSuccess()) {
                log.error("配销加推订货单中转商品生成采购单失败" + (StringUtils.isNotBlank(response.getMessage()) ? response.getMessage() : null));
            } else {
                //发送采购成功修改订单明细的结算周期
                if (CollectionUtils.isNotEmpty(transferDisDeliveryOrderOut.getOrderDisDeliveryDetails())) {
                    ordDisDeliveryDetailService.batchUpdate(transferDisDeliveryOrderOut.getOrderDisDeliveryDetails());
                }
            }
        } catch (Exception e) {
            log.error("执行配销加推订货单中转商品生成采购单任务异常：", e);
        }
        log.info("配销加推订货单执行中转商品生成采购单任务结束");
    }

    /**
     * @Description: 配销单回传
     * @Author: ZhangYao
     * @Date: 2024/3/19 14:49
     * @param disDelivery:
     * @param deliveryOrderDetails:
     * @param unificationBillVO:
     * @param standardGoodsMap:
     * @return: java.lang.String
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean unificationOrderCallBack(OrdDisDelivery disDelivery, List<OrdDisDeliveryDetail> deliveryOrderDetails,
                                            UnificationBillVO unificationBillVO, Map<String, StandardGoodsInfoOut> standardGoodsMap) {
        StockInfoOut stockInfoOut = stockServer.getTransInfo(disDelivery.getStockCode());
        // 更新明细
        ordDisDeliveryDetailService.updateByDtsDtlList(disDelivery, deliveryOrderDetails, unificationBillVO.getDetail(), standardGoodsMap, stockInfoOut.getBizOrgCode());
        BigDecimal deliveryQuantity = deliveryOrderDetails.stream().filter(disDeliveryDetail -> Objects.nonNull(disDeliveryDetail.getDeliveryQuantity()))
                .map(OrdDisDeliveryDetail::getDeliveryQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        disDelivery.setDeliveryQuantity(deliveryQuantity);
        BigDecimal deliveryAmount = deliveryOrderDetails.stream().filter(disDeliveryDetail -> Objects.nonNull(disDeliveryDetail.getDeliveryAmount()))
                .map(OrdDisDeliveryDetail::getDeliveryAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        disDelivery.setDeliveryAmount(deliveryAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.CEILING));
        //已发货状态
        disDelivery.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        disDelivery.setReceiveProgress(DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey());
        disDelivery.setLogisticsNo(unificationBillVO.getNum());
        disDelivery.setDeliveryTime(unificationBillVO.getFcreatetime());
        disDelivery.setUpdater(SystemConstant.SYSTEM_USER);
        disDelivery.setUpdateTime(LocalDateTime.now());

        //保存自动收货时间
        this.getAutoTakeDeliveryRuleByDeliveryOrder(disDelivery);

        //更新配销单
        ordDisDeliveryService.updateByPrimaryKeySelective(disDelivery);
        // 库存调整
        ordDisDeliveryService.optInvForDeliveryInfo(disDelivery, deliveryOrderDetails, stockInfoOut);

        // 资金调整
        if (Objects.isNull(disDelivery.getFreezeStatus())) {
            ordDisDeliveryService.shipmentsOrdDisDeliveryFund(disDelivery, deliveryOrderDetails);
        } else {
            disDelivery.setFreezeStatus(DeliveryOrderFreezeEnum.RELEASE.getKey());
            // 发货前解冻冻结
            Response response = ordDisDeliveryService.payBeforeShipments(disDelivery.getId(), disDelivery.getDeliveryAmount());
            if (!response.isSuccess()) {
                throw new BusinessException(response.getMessage());
            }
        }

        // 配销单dts回传订单追踪日志
        String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.WAREHOUSE_DELIVERED.getTemplate(), disDelivery.getDeliveryOrderNo());
        ordDisOrderTrackService.pushRedisOrderTrackMessage(disDelivery.getDeliveryOrderNo(), disDelivery.getStoreCode(),
                OrderTrackStatusEnum.WAREHOUSE_DELIVERED.getName(), trackLog, disDelivery.getBizOrgCode(), disDelivery.getCreator(), disDelivery.getCreateTime());
        String content = MessageFormat.format(OrdLogTypeEnum.ORD_DIS_ORDER_SEND_DELIVERY_UN_FROZEN_AND_SETTLEMENT.getName(), disDelivery.getDeliveryAmount().abs());
        //dts回传收货配销单时，改差异单后台已批准 记录日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_DIS_ORDER_SEND_DELIVERY_UN_FROZEN_AND_SETTLEMENT.getCode(),
                String.valueOf(disDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                content, new Date(), disDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        return "成功";
        return true;
    }

    public List<OrdDisDelivery> findListByIdList(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        StringJoiner idJoiner = new StringJoiner(SystemConstant.COMMA);
        idList.forEach(id -> idJoiner.add(id.toString()));
        List<OrdDisDelivery> ordDirDeliveries = ordDisDeliveryService.selectByIds(idJoiner.toString());
        return ordDirDeliveries;
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleAutoTakeDisDelivery(TakeDisDeliveryOrderIn takeDisDeliveryOrderIn, OrdDisDelivery deliveryOrder, StockInfoOut stockInfoOut) {
        DisSignDeliveryOrderIn signDeliveryOrderIn = new DisSignDeliveryOrderIn();
        signDeliveryOrderIn.setDeliveryOrderId(deliveryOrder.getId());
        signDeliveryOrderIn.setDifferencesRemark("自动签收默认无差异");
        signDeliveryOrderIn.setOrgCode(deliveryOrder.getOrgCode());
        Response<String> signResponse = deliveryOrderSigningService.signDisDeliveryOrder(signDeliveryOrderIn, deliveryOrder, takeDisDeliveryOrderIn.getLoginUsername());
        if (Objects.nonNull(signResponse) && signResponse.isSuccess()) {
            takeDisDeliveryHandle.takeDelivery(takeDisDeliveryOrderIn, deliveryOrder, stockInfoOut);
        }
//        String content = MessageFormat.format(OrdLogTypeEnum.ORD_DIS_ORDER_SEND_DELIVERY_SYSTEM.getName(), deliveryOrder.getDeliveryOrderNo(), deliveryOrder.getDeliveryAmount());
//        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_ORDER_SEND_DELIVERY_SYSTEM.getCode(),
//                String.valueOf(deliveryOrder.getId()), OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), deliveryOrder.getUpdater());
//        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        return "成功";
    }
}
