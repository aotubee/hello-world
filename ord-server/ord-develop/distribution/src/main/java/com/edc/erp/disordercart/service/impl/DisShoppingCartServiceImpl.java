/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.disordercart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderProcessConfigItem;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.enumeration.ActivityTypeEnum;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.FlashSaleCheckOut;
import com.edc.erp.common.model.out.FlashSaleWeekOut;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreDelivery;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.model.vo.ActivityAdditionVO;
import com.edc.erp.common.model.vo.ActivityGivingGoodsVO;
import com.edc.erp.common.model.vo.ActivityOtherMainGoodsVO;
import com.edc.erp.common.model.vo.ActivityRegulationVO;
import com.edc.erp.common.service.*;
import com.edc.erp.common.util.FlashSaleUtils;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.common.util.StoreDeliveryDateUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disordercart.entity.OrdDisOrderCart;
import com.edc.erp.disordercart.model.in.DeleteOrderCartIn;
import com.edc.erp.disordercart.model.in.DisOperationShoppingCartIn;
import com.edc.erp.disordercart.model.out.CalculationOrderCycleOut;
import com.edc.erp.disordercart.model.out.CalculationShoppingCartOut;
import com.edc.erp.disordercart.service.DisShoppingCartService;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.distribution.handle.DisOrderDetailHandle;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.mapper.OrdDisOrderDetailMapper;
import com.edc.erp.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.distribution.model.out.OrderCartGoodsOut;
import com.edc.erp.distribution.model.out.OrderCartOut;
import com.edc.erp.distribution.model.out.OrderCycleHeaderOut;
import com.edc.erp.enumeration.*;
import com.edc.erp.handle.DisOrderConfigHandle;
import com.edc.erp.model.out.OrderCycleDeliveryOut;
import com.edc.erp.orderscheduing.handle.OrderProcessSchedulingHandle;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PpOrderCart业务访问实现类
 *
 * @author zhangyao
 */
@Slf4j
@Service
public class DisShoppingCartServiceImpl extends BaseServiceImpl<OrdDisOrderCart> implements DisShoppingCartService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private DisOrderConfigHandle orderConfigHandle;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private OrderProcessSchedulingHandle orderProcessSchedulingHandle;

    @Autowired
    private DisOrderHandle disOrderHandle;

    @Autowired
    private DisOrderDetailHandle disOrderDetailHandle;

    @Autowired
    private OrderLimitConfigService orderLimitConfigService;

    @Autowired
    private StoreSkuMonthlySalesService storeSkuMonthlySalesService;

    @Autowired
    private OrdDisOrderDetailMapper ordDisOrderDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrderCart(List<DeleteOrderCartIn> deleteOrderCartInList, String storeCode, String bizOrgCode) {
        String orderCartKey = DisSystemConstant.DIS_ORDER_CART + bizOrgCode + ":" + storeCode;
        Object redisCartObject = redisService.hGet(orderCartKey, storeCode);
        if (null == redisCartObject) {
            return;
        }
        JSONObject redisCartJsonObj = JSON.parseObject(redisCartObject.toString());
//        log.info("门店{}购物车删除前数据------------------{}", storeCode, redisCartJsonObj.toJSONString());
        if (null != redisCartJsonObj && redisCartJsonObj.size() > 0) {
            deleteOrderCartInList.forEach(deleteOrderCartIn -> {
                redisCartJsonObj.remove(deleteOrderCartIn.getGoodsCode());
//                log.info("门店{}移除商品{}", storeCode, deleteOrderCartIn.getGoodsCode());
            });
//            log.info("门店{}购物车删除 后 数据------------------{}", storeCode, redisCartJsonObj.toJSONString());
            redisService.hSet(orderCartKey, storeCode, redisCartJsonObj.toJSONString());
        }
//        log.info("门店{}购物车删除 后 数据------------------{}", storeCode, JSONObject.parseObject(redisService.hGet(orderCartKey, storeCode).toString()));
    }

    @Override
    public void addOrUpdateOrderCartGoods(OrdDisOrderCart ordDisOrderCart, BigDecimal quantity, String storeCode, String bizOrgCode, JSONObject redisCartObject) {
        if (null != redisCartObject && quantity.compareTo(BigDecimal.ZERO) == 0) {
            redisCartObject.remove(ordDisOrderCart.getGoodsCode());
        } else {
            if (null == redisCartObject) {
                redisCartObject = new JSONObject();
            }
            ordDisOrderCart.setQuantity(quantity);
            ordDisOrderCart.setUpdator(storeCode);
            ordDisOrderCart.setUpdateTime(LocalDateTime.now());
            ordDisOrderCart.setBizOrgCode(bizOrgCode);
            redisCartObject.put(ordDisOrderCart.getGoodsCode(), ordDisOrderCart);
        }
        String orderCartKey = DisSystemConstant.DIS_ORDER_CART + bizOrgCode + ":" + storeCode;
        redisService.hSet(orderCartKey, storeCode, redisCartObject.toJSONString());
    }

    /**
     * 获取购物车订单周期类型
     *
     * @param storeAppUserOut
     * @param orderCartOutList
     * @param sourceCode
     * @param requestType
     * @return
     */
    @Override
    public List<OrderCycleHeaderOut> initOrderCycleHeaderOutMap(AppUserOut storeAppUserOut, List<OrderCartOut> orderCartOutList, String sourceCode, int requestType) {
        Map<Integer, OrderCycleHeaderOut> cycleHeaderOutMap = new HashMap<>(NumberUtil.INTEGER_TWO);
        // 门店的可订货上限
        Map<String, BigDecimal> orderUpLimitMap = null;
        boolean isOrderUpLimitFlag = SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode) || SourceTypeEnum.UPLOWDOWN.getKey().equals(sourceCode);
        if (isOrderUpLimitFlag) {
            orderUpLimitMap = orderLimitConfigService.findGoodsLimitByStoreCode(storeAppUserOut.getStoreCode(), storeAppUserOut.getBizOrgCode());
        }
        StringJoiner errorGoodsJoiner = new StringJoiner(SystemConstant.COMMA);
        // 获取配送周期信息
        List<StoreDelivery> storeDeliverLogicList = storeCenterService.getByStoreCode(storeAppUserOut.getStoreCode());
        if(CollectionUtils.isEmpty(storeDeliverLogicList)){
            throw new BusinessException("门店配送周期信息为空");
        }
        Map<String, StoreDelivery> deliveryTypeMap = storeDeliverLogicList.stream().collect(Collectors.toMap(StoreDelivery::getDeliveryType, Function.identity()));
        List<DeleteOrderCartIn> deleteOrderCartInList = Lists.newArrayList();
        for (OrderCartOut orderCartOut : orderCartOutList) {
            List<OrderCartGoodsOut> goodsList;
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setStoreCode(storeAppUserOut.getStoreCode());
            orderGoodsIn.setGoodsCode(orderCartOut.getGoodsCode());
            orderGoodsIn.setBizOrgCode(storeAppUserOut.getBizOrgCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(sourceCode));
            OrderGoodsOut orderGoodsOut = NumberUtil.INTEGER_FOUR.equals(requestType) ? orderGoodsServer.getStoreOrderGoods(orderGoodsIn) : orderGoodsServer.getStoreOrderGoodsByCache(orderGoodsIn);
            if (Objects.isNull(orderGoodsOut)) {
                // 移除购物车
                addRemoveGoodsList(deleteOrderCartInList, errorGoodsJoiner, orderCartOut.getGoodsCode());
//                log.info("门店{}加购商品{}------1", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
                continue;
            } else {
                if (Objects.isNull(orderGoodsOut.getDistributionSpecification())) {
                    if (NumberUtil.INTEGER_FOUR.equals(requestType)) {
                        throw new BusinessException("商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送规格为空");
                    } else {
                        log.error("门店{}" + storeAppUserOut.getStoreCode() + "加购商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送规格为空");
                        // 移除购物车
                        addRemoveGoodsList(deleteOrderCartInList, errorGoodsJoiner, orderGoodsOut.getGoodsCode());
                        continue;
                    }
                }
                if (Objects.isNull(orderGoodsOut.getDistributionUnitPrice())) {
                    if (NumberUtil.INTEGER_FOUR.equals(requestType)) {
                        throw new BusinessException("商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送价异常");
                    } else {
                        log.error("门店{}" + storeAppUserOut.getStoreCode() + "加购商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送价异常");
                        addRemoveGoodsList(deleteOrderCartInList, errorGoodsJoiner, orderGoodsOut.getGoodsCode());
                        continue;
                    }
                }
            }
//            log.info("门店{}加购商品{}------2", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
            List<String> combinationTypeCodeList = Lists.newArrayList(orderGoodsOut.getStockCode(), DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay()), StringUtils.isNotBlank(GoodsTypeEnum.getCodeByName(orderGoodsOut.getGoodsType())) ? GoodsTypeEnum.getCodeByName(orderGoodsOut.getGoodsType()) : orderGoodsOut.getGoodsType());
            // 查找订单类型配置
            OrderTypeConfig orderTypeConfig = orderConfigHandle.getOneByCombinationTypeValueList(storeAppUserOut.getStoreCode(), storeAppUserOut.getBizOrgCode(), combinationTypeCodeList);
            if (Objects.isNull(orderTypeConfig)) {
                log.info("购物车-查询订单类型配置入参：仓位{}，配送方式{}，品类属性{}，组织{}不存在的订单类型配置", orderGoodsOut.getStockCode(), orderGoodsOut.getDistributionWay(), orderGoodsOut.getGoodsType(), storeAppUserOut.getBizOrgCode());
                throw new BusinessException("购物车-不存在的订单类型配置");
            }
            OrderCycleHeaderOut orderCycleHeaderOut = cycleHeaderOutMap.get(orderTypeConfig.getId());
            if (Objects.isNull(orderCycleHeaderOut)) {
//                log.info("门店{}加购商品{}------3", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
                orderCycleHeaderOut = new OrderCycleHeaderOut();
                goodsList = new ArrayList<>();
//                StoreLogisticsOut storeLogistics = storeCenterService.getStoreOnline(storeAppUserOut.getStoreId());
//                // roomDis = 0003
//                if (Objects.isNull(storeLogistics)) {
//                    throw new BusinessException("门店" + storeAppUserOut.getStoreCode() + "要货周期查找为空");
//                }
              /*  String positionCode = orderGoodsOut.getStockCode();
//               String deliveryDailyCycle = this.getDeliveryDailyCycle(storeLogistics, positionCode);*/
                StoreDelivery storeDelivery = deliveryTypeMap.get(orderTypeConfig.getOrderPeriod());
                if(Objects.isNull(storeDelivery)){
                    throw new BusinessException("门店" + storeAppUserOut.getStoreCode() + "配送信息" + orderTypeConfig.getOrderPeriod() + "为空");
                }
//                OrderCycleDeliveryOut storeDeliveryType = this.getStoreDeliveryType(storeLogistics, orderTypeConfig.getOrderPeriod());
                OrderCycleDeliveryOut storeDeliveryType = this.getStoreDeliveryInfoType(storeDelivery);
                if (Objects.isNull(storeDeliveryType)) {
                    throw new BusinessException("门店" + storeAppUserOut.getStoreCode() + "要货周期为空");
                }
                if (StringUtils.isBlank(orderTypeConfig.getTruncationTimePoint())) {
                    throw new BusinessException("门店" + storeAppUserOut.getStoreCode() + "平台提单时间为空");
                }
                String storeDeliveryTruncationTime = this.getStoreDeliveryTruncationTime(storeDeliveryType.getDistributionCycle(), storeDeliveryType.getDeliveryDailyCycle(), orderTypeConfig.getTruncationTimePoint());
                if (StringUtils.isBlank(storeDeliveryTruncationTime)) {
                    throw new BusinessException("购物车-截单时间为空");
                }
//                log.info("门店{}加购商品{}------4", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
                orderCycleHeaderOut.setOrderTypeConfigId(orderTypeConfig.getId());
                orderCycleHeaderOut.setTruncationTime(DateUtils.parseTime(storeDeliveryTruncationTime + ":00"));
                orderCycleHeaderOut.setShortOrderType(orderTypeConfig.getShortOrderType());
                orderCycleHeaderOut.setOrderTypeCode(orderTypeConfig.getOrderTypeCode());
                orderCycleHeaderOut.setMinimumOrderAmount(orderTypeConfig.getMinimumOrderAmount());
                // 查找订货周期
                OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycle(storeAppUserOut.getStoreCode(), orderCycleHeaderOut.getOrderTypeConfigId(), orderCycleHeaderOut.getTruncationTime(), storeAppUserOut.getBizOrgCode());
                if (Objects.nonNull(orderCycle)) {
                    orderCycleHeaderOut.setOrderCycleId(orderCycle.getId());
                }
                this.checkOrderSource(orderCycleHeaderOut, orderCycle, sourceCode, storeAppUserOut.getBizOrgCode());
//                log.info("门店{}加购商品{}------5", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
                // 获取起订额校验配置项
                String minAmountCheckType = this.getMinOrderCheckTypeCode(orderCycleHeaderOut, orderCycle, storeAppUserOut.getBizOrgCode());
                orderCycleHeaderOut.setMinAmountCheckType(minAmountCheckType);
//                log.info("门店{}加购商品{}------6", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
            } else {
                goodsList = orderCycleHeaderOut.getGoodsList();
//                log.info("门店{}加购商品{}------7", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
            }
            //配送方式中文转换
            orderGoodsOut.setDistributionWay(StringUtils.isNotBlank(DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay())) ? DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay()) : orderGoodsOut.getDistributionWay());
//            if (isInvalid) {
//                orderCartGoodsOut = this.initInvalidOrderCartGoodsOut(orderGoodsOut);
//            } else {
            // 创建购物车商品对象
            BigDecimal specNum = BigDecimal.valueOf(orderGoodsOut.getDistributionSpecification().getQpc());
            BigDecimal totalQuantity = orderCartOut.getQuantity().multiply(specNum);
            // 可订量
            BigDecimal canQty = null;
            // 存在可订货上限，需要去拿当前周期的订货数累加是否超出
            if (isOrderUpLimitFlag && orderUpLimitMap.containsKey(orderCartOut.getGoodsCode())) {
                // 已订量
                BigDecimal orderedQty = BigDecimal.ZERO;
                if (Objects.nonNull(orderCycleHeaderOut.getOrderCycleId())) {
                    orderedQty = orderedQty.add(ordDisOrderDetailMapper.getCycleOrderedGoodsQty(orderCycleHeaderOut.getOrderCycleId(), orderGoodsOut.getGoodsCode()));
                }
                // 可订量
                canQty = orderUpLimitMap.get(orderCartOut.getGoodsCode()).subtract(orderedQty);
                if (NumberUtil.INTEGER_FOUR.equals(requestType) || !SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode)) {
                    if (BigDecimal.ZERO.compareTo(canQty) > 0) {
                        log.error("门店{}来源{}商品{}可订货上限不足,可订{},当前申请量{}", storeAppUserOut.getStoreCode(), SourceTypeEnum.getValueByKey(sourceCode), orderCartOut.getGoodsCode(), canQty, totalQuantity);
                        continue;
                    }
                    if (canQty.compareTo(totalQuantity) < 0) {
                        totalQuantity = canQty.divide(specNum, NumberUtil.INTEGER_ZERO, RoundingMode.DOWN).multiply(specNum);
                    }
                    if (totalQuantity.compareTo(BigDecimal.ZERO) < 1) {
                        log.error("门店{}来源{}商品{}可订货上限不足,可订{},计算规格后不足一个包装数", storeAppUserOut.getStoreCode(), SourceTypeEnum.getValueByKey(sourceCode), orderCartOut.getGoodsCode(), canQty);
                        continue;
                    }
                }
            }
            OrderCartGoodsOut orderCartGoodsOut = this.getMainOrderCartGoodsOut(orderGoodsOut, totalQuantity,
                    storeAppUserOut, sourceCode);
//            log.info("门店{}加购商品{}------8", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
            orderCartGoodsOut.setCanQty(canQty);
            orderCartGoodsOut.setOptionalGiftCodeList(orderCartOut.getOptionalGiftCodeList());
//            }
            goodsList.add(orderCartGoodsOut);
            orderCycleHeaderOut.setGoodsList(goodsList);
            orderCycleHeaderOut.setOrderTypeConfigId(orderTypeConfig.getId());
            orderCycleHeaderOut.setDistributionAndUpDownOrderPaidAmount(BigDecimal.ZERO);
            cycleHeaderOutMap.put(orderTypeConfig.getId(), orderCycleHeaderOut);
        }
        this.handleOrderCartGoods(cycleHeaderOutMap, storeAppUserOut, sourceCode, requestType);
        if (CollectionUtils.isNotEmpty(deleteOrderCartInList)) {
            log.error("配销门店{}获取购物车列表移除不可用商品：{}", storeAppUserOut.getStoreCode(), errorGoodsJoiner);
            this.deleteOrderCart(deleteOrderCartInList, storeAppUserOut.getStoreCode(), storeAppUserOut.getBizOrgCode());
        }
//        log.info("门店{}加购商品长度{}------9", storeAppUserOut.getStoreCode(), cycleHeaderOutMap.values().size());
        return new ArrayList<>(cycleHeaderOutMap.values());
    }

    private void addRemoveGoodsList(List<DeleteOrderCartIn> deleteOrderCartInList, StringJoiner errorGoodsJoiner, String goodsCode) {
        // 移除购物车
        DeleteOrderCartIn deleteOrderCartIn = new DeleteOrderCartIn();
        deleteOrderCartIn.setGoodsCode(goodsCode);
        deleteOrderCartInList.add(deleteOrderCartIn);
        errorGoodsJoiner.add(goodsCode);
    }

    private OrderCartGoodsOut initInvalidOrderCartGoodsOut(OrderGoodsOut orderGoodsOut) {
        OrderCartGoodsOut orderCartGoodsOut = new OrderCartGoodsOut();
        orderCartGoodsOut.setSuggestedRetailPrice(BigDecimal.ZERO);
        orderCartGoodsOut.setPackageQuantity(BigDecimal.ZERO);
        orderCartGoodsOut.setQuantity(BigDecimal.ZERO);
        orderCartGoodsOut.setGoodsCode(orderGoodsOut.getGoodsCode());
        orderCartGoodsOut.setPosition(orderGoodsOut.getStockCode());
        orderCartGoodsOut.setPositionStr(orderGoodsOut.getStockName());
        orderCartGoodsOut.setGoodsName(orderGoodsOut.getGoodsName());
        orderCartGoodsOut.setGoodsImage(orderGoodsOut.getImgUrl());
        orderCartGoodsOut.setBarCode(orderGoodsOut.getBarCode());
        orderCartGoodsOut.setBrand(orderGoodsOut.getBrand());
        orderCartGoodsOut.setAllowDistributionReturn(orderGoodsOut.getAllowDistributionReturn());
        orderCartGoodsOut.setIsShelves(orderGoodsOut.getIsShelves());
        orderCartGoodsOut.setDistributionType(orderGoodsOut.getDistributionWay());
        orderCartGoodsOut.setIsGift(NumberUtils.INTEGER_ZERO);
        orderCartGoodsOut.setSmallSort(orderGoodsOut.getSort());
        orderCartGoodsOut.setIsActivity(NumberUtils.INTEGER_ZERO);
        // 封装价格
        orderCartGoodsOut.setPayUnitPrice(BigDecimal.ZERO);
        orderCartGoodsOut.setPaySpecificationsPrice(BigDecimal.ZERO);
        orderCartGoodsOut.setOriginalUnitPrice(BigDecimal.ZERO);
        orderCartGoodsOut.setOriginalSpecificationsPrice(BigDecimal.ZERO);
        orderCartGoodsOut.setIsCanBuyFlashSale(NumberUtils.INTEGER_ZERO);
        orderCartGoodsOut.setIsEnable(NumberUtils.INTEGER_ZERO);
        return orderCartGoodsOut;
    }

    /**
     * 获取门店运送截止时间
     *
     * @param storeDeliveryType
     * @param deliveryDailyCycle
     * @param truncationTimePoint
     * @return
     */
    @Override
    public String getStoreDeliveryTruncationTime(String storeDeliveryType, String deliveryDailyCycle, String truncationTimePoint) {
        String truncationTime;
        if (SystemConstant.DELIVERY_BY_DAY.equals(storeDeliveryType)) {
            if (StringUtils.isBlank(deliveryDailyCycle)) {
                throw new BusinessException("购物车-门店按日配送勾选范围为空");
            }
            List<Integer> deliveryWeekList = Arrays.asList(deliveryDailyCycle.split(",")).stream().map(s -> Integer.parseInt(s)).sorted().collect(Collectors.toList());
            truncationTime = StoreDeliveryDateUtil.getTruncationDateTimeByDeliveryCycle(deliveryWeekList, truncationTimePoint);
        } else {
            truncationTime = StoreDeliveryDateUtil.getStoreDeliveryDate(storeDeliveryType, truncationTimePoint);
        }
        return truncationTime;
    }

    /**
     * 判断是否可以加购
     *
     * @param skuCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public FlashSaleCheckOut isCanBuyFlashSaleGoods(String skuCode, String bizOrgCode) {
        List<FlashSaleWeekOut> flashSaleWeekOutList = orderGoodsServer.findFlashSaleWeekOutList(skuCode, bizOrgCode);
        return FlashSaleUtils.getIsCanBuyFlashSaleGoods(flashSaleWeekOutList, null);
    }

    /**
     * 获取门店配送周期类型
     *
     * @param storeLogistics
     * @param orderPeriod
     * @return
     */
    @Override
    public OrderCycleDeliveryOut getStoreDeliveryType(StoreLogisticsOut storeLogistics, String orderPeriod) {
        OrderCycleDeliveryOut orderCycleDeliveryOut = new OrderCycleDeliveryOut();
        switch (orderPeriod) {
            case SystemConstant.ROOM_DISTRIBUTION_CYCLE:
                String roomDistributionCycle = storeLogistics.getRoomDistributionCycle();
                String roomDeliveryDailyCycle = storeLogistics.getRoomDeliveryDailyCycle();
                orderCycleDeliveryOut.setDeliveryDailyCycle(roomDeliveryDailyCycle);
                orderCycleDeliveryOut.setDistributionCycle(roomDistributionCycle);
                break;
            case SystemConstant.FROZEN_DISTRIBUTION_CYCLE:
                String frozenDistributionCycle = storeLogistics.getFrozenDistributionCycle();
                String frozenDeliveryDailyCycle = storeLogistics.getFrozenDeliveryDailyCycle();
                orderCycleDeliveryOut.setDeliveryDailyCycle(frozenDeliveryDailyCycle);
                orderCycleDeliveryOut.setDistributionCycle(frozenDistributionCycle);
                break;
            default:
                break;
        }
        return orderCycleDeliveryOut;
    }

    @Override
    public OrderCycleDeliveryOut getStoreDeliveryInfoType(StoreDelivery storeDelivery){
        OrderCycleDeliveryOut orderCycleDeliveryOut = new OrderCycleDeliveryOut();
        orderCycleDeliveryOut.setDeliveryDailyCycle(storeDelivery.getDeliveryDailyCycle());
        orderCycleDeliveryOut.setDistributionCycle(storeDelivery.getDeliveryCycle());
        return orderCycleDeliveryOut;
    }


    /**
     * 核算购物车金额
     *
     * @param disOperationShoppingCartInList
     * @param appUserOut
     * @return
     */
    @Override
    public CalculationShoppingCartOut calculationShoppingCartAmount(List<DisOperationShoppingCartIn> disOperationShoppingCartInList, AppUserOut appUserOut) {
        String storeCode = appUserOut.getStoreCode();
        // 初始化redis中购物车集合数据
        List<OrderCartOut> orderCartOutList = Lists.newArrayList();
        disOperationShoppingCartInList.forEach(disOperationShoppingCartIn -> {
            OrderCartOut orderCartOut = new OrderCartOut();
            orderCartOut.setQuantity(disOperationShoppingCartIn.getPackageQuantity());
            orderCartOut.setGoodsCode(disOperationShoppingCartIn.getGoodsCode());
            orderCartOutList.add(orderCartOut);
        });
        // 将购物车中商品以Map方式封装到订货周期
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = this.initOrderCycleHeaderOutMap(appUserOut, orderCartOutList, SourceTypeEnum.INITIATIVE.getKey(), NumberUtil.INTEGER_THREE);
        List<CalculationOrderCycleOut> calculationShoppingCartOrderTypeList = new ArrayList<>();
        // 订货总金额
        BigDecimal orderAmount = BigDecimal.ZERO;
        // 优惠总金额
        BigDecimal preferentialAmount = BigDecimal.ZERO;
        for (OrderCycleHeaderOut cycleHeaderOut : orderCycleHeaderOutList) {
            // 该订货周期下所勾选的商品的总金额
            BigDecimal totalOrderCycleAmount = BigDecimal.ZERO;
            // 该订货周期下所勾选的物料总金额
            BigDecimal totalMaterielAmount = BigDecimal.ZERO;
            // 获取该订货周期下分货清单总金额
            BigDecimal totalDistributionAndUpDownAmount = BigDecimal.ZERO;
            if (OrderCycleProcessConfigItemCodeEnum.ORDER_SINGLE.getCode().equals(cycleHeaderOut.getMinAmountCheckType())) {
                // 获取门店某个订货周期下指定订单类型下已付款跑货和分货金额
                totalDistributionAndUpDownAmount = disOrderHandle.getTotalDistributionAndUpDownOrderAmount(storeCode, DateUtils.format(cycleHeaderOut.getTruncationTime()),
                        cycleHeaderOut.getOrderTypeConfigId(), appUserOut.getBizOrgCode());
            }
            for (OrderCartGoodsOut cartGoodsOut : cycleHeaderOut.getGoodsList()) {
                if (GoodsTypeEnum.MATERIAL.getCode().equals(cartGoodsOut.getGoodsType())) {
                    totalMaterielAmount = totalMaterielAmount.add(cartGoodsOut.getPayUnitPrice().multiply(cartGoodsOut.getQuantity()));
                }
                totalOrderCycleAmount = totalOrderCycleAmount.add(cartGoodsOut.getPayUnitPrice().multiply(cartGoodsOut.getQuantity()));
                // 海鼎优惠不返款
                if (ActivityTypeEnum.SPECIAL_PRICE.getTagName().equals(cartGoodsOut.getActivityType())) {
                    preferentialAmount = preferentialAmount.add((cartGoodsOut.getOriginalUnitPrice().subtract(cartGoodsOut.getPayUnitPrice())).multiply(cartGoodsOut.getQuantity()));
                }
            }
            orderAmount = orderAmount.add(totalOrderCycleAmount);
            BigDecimal listAmount = totalOrderCycleAmount.add(totalDistributionAndUpDownAmount).subtract(totalMaterielAmount).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP);
            CalculationOrderCycleOut calculationOrderCycleOut = new CalculationOrderCycleOut();
            calculationOrderCycleOut.setOrderTypeConfigId(cycleHeaderOut.getOrderTypeConfigId());
            calculationOrderCycleOut.setShortOrderType(cycleHeaderOut.getShortOrderType());
            calculationOrderCycleOut.setListAmount(listAmount);
            calculationOrderCycleOut.setIsHaveDistributionOrder(totalDistributionAndUpDownAmount.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
            calculationShoppingCartOrderTypeList.add(calculationOrderCycleOut);
        }
        CalculationShoppingCartOut calculationShoppingCartOut = new CalculationShoppingCartOut();
        calculationShoppingCartOut.setCalculationOrderCycleOutList(calculationShoppingCartOrderTypeList);
        calculationShoppingCartOut.setOrderAmount(orderAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        calculationShoppingCartOut.setPreferentialAmount(preferentialAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        return calculationShoppingCartOut;
    }

    /**
     * 更新购物车
     *
     * @param shoppingCartInList
     * @param appUserOut
     * @return
     */
    @Override
    public Response<List<OrderCycleHeaderOut>> updateShoppingCart(List<CreateOrderSkuIn> shoppingCartInList, AppUserOut appUserOut, Boolean isCopyOrder) {
        List<OrderCartOut> orderCartOutList = new ArrayList<>(shoppingCartInList.size());
        for (CreateOrderSkuIn createOrderSkuIn : shoppingCartInList) {
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setStoreCode(appUserOut.getStoreCode());
            orderGoodsIn.setGoodsCode(createOrderSkuIn.getGoodsCode());
            orderGoodsIn.setBizOrgCode(appUserOut.getBizOrgCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(SourceTypeEnum.INITIATIVE.getKey()));
            OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
            if (Objects.isNull(orderGoodsOut)) {
                log.error("门店{}加购商品{}查询为空----{}", appUserOut.getStoreCode(), createOrderSkuIn.getGoodsCode());
                if (isCopyOrder) {
                    continue;
                }
                return Response.error("不能操作不存在的商品" + createOrderSkuIn.getGoodsCode());
            }
            Objects.requireNonNull(orderGoodsOut.getStockCode(), createOrderSkuIn.getGoodsCode() + "仓位代码为空");
            List<String> combinationTypeCodeList = Lists.newArrayList(orderGoodsOut.getStockCode(), DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay()), orderGoodsOut.getGoodsType());
            String bizOrgCode = appUserOut.getBizOrgCode();
            //rpc 查询订单类型配置
            OrderTypeConfig optOrderTypeConfig = orderConfigHandle.getOneByCombinationTypeValueList(appUserOut.getStoreCode(), bizOrgCode, combinationTypeCodeList);
            if (Objects.isNull(optOrderTypeConfig)) {
                log.error("门店{}加购商品{}加购失败，未找到对应的订单类型配置。", appUserOut.getStoreCode(), createOrderSkuIn.getGoodsCode());
                return Response.error("商品" + createOrderSkuIn.getGoodsCode() + "加购失败，未找到对应的订单类型配置。");
            }
            String storeCode = appUserOut.getStoreCode();
            String orderCartKey = DisSystemConstant.DIS_ORDER_CART + bizOrgCode + ":" + storeCode;
            Object redisCartObject = redisService.hGet(orderCartKey, storeCode);
            int countOrderCart = 0;
            JSONObject cartObj = null;
            if (null != redisCartObject) {
                cartObj = JSON.parseObject(redisCartObject.toString());
                if (null != cartObj && cartObj.size() > 0) {
                    countOrderCart = cartObj.size();
                }
            }
            OrdDisOrderCart orderCart = this.getOrderCart(createOrderSkuIn.getGoodsCode(), cartObj);
            if (null == orderCart) {
                if (SystemConstant.MAX_ORDER_CART_COUNT < countOrderCart) {
                    log.info("门店{}购物车已满", storeCode);
                    return Response.error("您的购物车已满");
                }
                orderCart = new OrdDisOrderCart();
                BeanUtils.copy(createOrderSkuIn, orderCart);
            }
            orderCart.setStoreId(appUserOut.getStoreId());
            orderCart.setStoreCode(appUserOut.getStoreCode());
            this.addOrUpdateOrderCartGoods(orderCart, createOrderSkuIn.getPackageQuantity(), appUserOut.getStoreCode(), appUserOut.getBizOrgCode(), cartObj);

            OrderCartOut orderCartOut = new OrderCartOut();
            orderCartOut.setGoodsCode(createOrderSkuIn.getGoodsCode());
            orderCartOut.setQuantity(createOrderSkuIn.getPackageQuantity());
            orderCartOut.setOptionalGiftCodeList(createOrderSkuIn.getOptionalGiftCodeList());
            orderCartOutList.add(orderCartOut);
        }
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = this.initOrderCycleHeaderOutMap(appUserOut, orderCartOutList, SourceTypeEnum.INITIATIVE.getKey(), NumberUtil.INTEGER_TWO);
        orderCycleHeaderOutList.forEach(orderCycleHeaderOut -> {
            String truncationTimeStr = DateUtils.format(orderCycleHeaderOut.getTruncationTime());
            // 获取该温层下分货单与跑货总金额
            BigDecimal distributionAndUpDownOrderPaidAmount = disOrderHandle.getTotalDistributionAndUpDownOrderAmount(appUserOut.getStoreCode(), truncationTimeStr,
                    orderCycleHeaderOut.getOrderTypeConfigId(), appUserOut.getBizOrgCode());
            orderCycleHeaderOut.setDistributionAndUpDownOrderPaidAmount(distributionAndUpDownOrderPaidAmount);
            this.handleWarningOrderQuantity(appUserOut, orderCartOutList, orderCycleHeaderOut, truncationTimeStr);
        });
        return Response.data(orderCycleHeaderOutList);
    }

    @Override
    public List<OrderCycleHeaderOut> findMyOrderCartList(AppUserOut appUserOut) {
        // 初始化redis中购物车集合数据
        List<OrderCartOut> orderCartOutList = this.initOrderCartOutList(appUserOut.getStoreCode(), appUserOut.getBizOrgCode());
        // 按更新时间倒序排列
        Collections.sort(orderCartOutList, (o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()));
        // 将购物车中商品以Map方式封装到订货周期
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = this.initOrderCycleHeaderOutMap(appUserOut, orderCartOutList, SourceTypeEnum.INITIATIVE.getKey(), NumberUtil.INTEGER_ONE);
        orderCycleHeaderOutList.stream().forEach(orderCycleHeaderOut -> {
            String truncationTimeStr = DateUtils.format(orderCycleHeaderOut.getTruncationTime());
            // 获取该温层下分货单与跑货总金额
            BigDecimal distributionAndUpDownOrderPaidAmount = disOrderHandle.getTotalDistributionAndUpDownOrderAmount(appUserOut.getStoreCode(), truncationTimeStr,
                    orderCycleHeaderOut.getOrderTypeConfigId(), appUserOut.getBizOrgCode());
            orderCycleHeaderOut.setDistributionAndUpDownOrderPaidAmount(distributionAndUpDownOrderPaidAmount);
            this.handleWarningOrderQuantity(appUserOut, orderCartOutList, orderCycleHeaderOut, truncationTimeStr);
        });
        return orderCycleHeaderOutList;
    }

    @Override
    public FlashSaleCheckOut isCanBuyByDistributionOrder(String skuCode, String bizOrgCode, LocalDateTime targetDateTime) {
        List<FlashSaleWeekOut> flashSaleWeekOutList = orderGoodsServer.findFlashSaleWeekOutList(skuCode, bizOrgCode);
        return FlashSaleUtils.getIsCanBuyFlashSaleGoods(flashSaleWeekOutList, targetDateTime);
    }


    /**
     * 校验允许订货类型配置
     *
     * @param orderCycleHeaderOut
     * @param orderCycle
     * @param sourceCode
     * @param bizOrgCode
     */
    public void checkOrderSource(OrderCycleHeaderOut orderCycleHeaderOut, OrdDisOrderCycle orderCycle, String sourceCode, String bizOrgCode) {
        String sourceConfigItemCode = null;
        if (SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode)) {
            sourceConfigItemCode = OrderCycleProcessConfigItemCodeEnum.MANUAL.getCode();
        }
        if (SourceTypeEnum.DISTRIBUTION.getKey().equals(sourceCode)) {
            sourceConfigItemCode = OrderCycleProcessConfigItemCodeEnum.DISTRIBUTION.getCode();
        }
        if (BusinessTypeColumnEnum.UP_LOW_DOWN.getType().equals(sourceCode)) {
            sourceConfigItemCode = OrderCycleProcessConfigItemCodeEnum.UP_AND_DOWN.getCode();
        }
        if (StringUtils.isBlank(sourceConfigItemCode)) {
            throw new BusinessException("无效的下单来源");
        }
        String prefixErrorMsg = orderCycleHeaderOut.getShortOrderType() + "(订单类型代码：" + orderCycleHeaderOut.getOrderTypeCode() + ")";
        Map<String, String> allowOrderSourceMap;
        if (Objects.isNull(orderCycle)) {
            //获取配置流程选项
            List<OrderProcessConfigItem> configItemList = orderProcessSchedulingHandle.getTheOrderProcessConfigOut(orderCycleHeaderOut.getOrderTypeConfigId(),
                    OrderCycleProcessCodeEnum.ORDER_CREATE.getCode(), OrderCycleProcessConfigCodeEnum.ORDER_SOURCE_CODE.getCode(), bizOrgCode);
            if (CollectionUtils.isEmpty(configItemList)) {
                throw new BusinessException("订单类型代码为" + orderCycleHeaderOut.getOrderTypeCode() + "的允许订货类型配置异常");
            }
            allowOrderSourceMap = configItemList.stream().collect(Collectors.toMap(OrderProcessConfigItem::getItemCode, OrderProcessConfigItem::getItemName));
        } else {
            prefixErrorMsg = prefixErrorMsg + "截单周期" + orderCycle.getTruncationDateTime().toLocalDate();
            List<OrderProcessConfigItemOut> allowOrderSourceList = orderConfigHandle.findAllowOrderSource(orderCycle.getId(), orderCycle.getBizOrgCode());
            if (CollectionUtils.isEmpty(allowOrderSourceList)) {
                throw new BusinessException(prefixErrorMsg + "的允许订货类型配置异常");
            }
            allowOrderSourceMap = allowOrderSourceList.stream().collect(Collectors.toMap(OrderProcessConfigItemOut::getItemCode, OrderProcessConfigItemOut::getItemName));
        }
        if (Objects.isNull(allowOrderSourceMap.get(sourceConfigItemCode))) {
            throw new BusinessException(prefixErrorMsg + "不允许" + SourceTypeEnum.getValueByKey(sourceCode));
        }
    }

    /**
     * 获取订货周期内的起订额校验规则代码
     *
     * @param orderCycleHeaderOut 购物车订货周期类型
     * @param orderCycle          订货周期
     * @param bizOrgCode          业务组织代码
     * @return
     */
    private String getMinOrderCheckTypeCode(OrderCycleHeaderOut orderCycleHeaderOut, OrdDisOrderCycle orderCycle, String bizOrgCode) {
        OrderProcessConfigItemOut minOrderAmountConfigItemOut;
        String minOrderCheckTypeCode;
        String prefixErrorMsg = orderCycleHeaderOut.getShortOrderType() + "(订单类型代码：" + orderCycleHeaderOut.getOrderTypeCode() + ")";
        if (Objects.isNull(orderCycle)) {
            //获取配置流程选项
            List<OrderProcessConfigItem> configItemList = orderProcessSchedulingHandle.getTheOrderProcessConfigOut(orderCycleHeaderOut.getOrderTypeConfigId(),
                    OrderCycleProcessCodeEnum.ORDER_CREATE.getCode(), OrderCycleProcessConfigCodeEnum.MIN_AMOUNT.getCode(), bizOrgCode);
            if (CollectionUtils.isEmpty(configItemList)) {
                throw new BusinessException(prefixErrorMsg + "的起订额配置异常");
            }
            minOrderCheckTypeCode = configItemList.get(0).getItemCode();
        } else {
            minOrderAmountConfigItemOut = orderConfigHandle.minAmountCheckType(orderCycle.getId(), orderCycle.getBizOrgCode());
            if (Objects.isNull(minOrderAmountConfigItemOut)) {
                throw new BusinessException(prefixErrorMsg + "截单周期" + orderCycle.getTruncationDateTime().toLocalDate() + "的起订额副本配置异常");
            }
            minOrderCheckTypeCode = minOrderAmountConfigItemOut.getItemCode();
        }
        return minOrderCheckTypeCode;
    }

    /**
     * 是否需要预警打标
     *
     * @param appUserOut
     * @param orderCartOutList
     * @param orderCycleHeaderOut
     * @param truncationTimeStr
     */
    private void handleWarningOrderQuantity(AppUserOut appUserOut, List<OrderCartOut> orderCartOutList,
                                            OrderCycleHeaderOut orderCycleHeaderOut, String truncationTimeStr) {
        List<String> skuList = Lists.newArrayList();
        orderCartOutList.forEach(orderCartOut -> skuList.add(orderCartOut.getGoodsCode()));
        // 当前购物车中商品月销量
        Map<String, BigDecimal> averageDailySalesDaysVOMap = storeSkuMonthlySalesService.getSkuAverageDailySalesDaysListByStoreCode(skuList,
                appUserOut.getStoreCode(), appUserOut.getBizOrgCode());
        // 获取本次截单下已下单商品
        List<OrdDisOrder> noInvalidOrderList = disOrderHandle.findNoInvalidOrderListByParameter(appUserOut.getStoreCode(), truncationTimeStr,
                orderCycleHeaderOut.getOrderTypeConfigId(), appUserOut.getOrgCode());
        List<Long> idList = noInvalidOrderList.stream().map(OrdDisOrder::getId).collect(Collectors.toList());
        orderCycleHeaderOut.getGoodsList().stream().forEach(orderCartGoodsOut -> {
            if (0 == orderCartGoodsOut.getIsGift()) {
                Integer isNeedWarningTag = 0;
                BigDecimal sumExistsPackageQuantity = disOrderDetailHandle.sumPackageByOrderIdList(orderCartGoodsOut.getGoodsCode(),
                        idList, appUserOut, truncationTimeStr);
                orderCartGoodsOut.setExistsQuantity(sumExistsPackageQuantity);
                BigDecimal qty = averageDailySalesDaysVOMap.get(orderCartGoodsOut.getGoodsCode());
                if (null != qty && null != orderCartGoodsOut.getIsEnable() && NumberUtils.INTEGER_ONE.equals(orderCartGoodsOut.getIsEnable())) {
                    BigDecimal packageQty = qty.divide(orderCartGoodsOut.getDistributionSpecificationNum(), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN);
                    BigDecimal targetOrderQuantity = orderCartGoodsOut.getExistsQuantity().add(orderCartGoodsOut.getPackageQuantity());
                    isNeedWarningTag = disOrderDetailHandle.getIsNeedOrderQuantityWarning(targetOrderQuantity, packageQty);
                }
                orderCartGoodsOut.setIsNeedWarningTag(isNeedWarningTag);
            }
        });
        Collections.sort(orderCycleHeaderOut.getGoodsList(), (o1, o2) -> o2.getIsNeedWarningTag().compareTo(o1.getIsNeedWarningTag()));
    }

    /**
     * 封装购物车主商品
     *
     * @param orgGoodsAdditionInfoOut
     * @param quantity
     * @param appUserOut
     * @param sourceCode
     * @return
     */
    private OrderCartGoodsOut getMainOrderCartGoodsOut(OrderGoodsOut orgGoodsAdditionInfoOut, BigDecimal quantity,
                                                       AppUserOut appUserOut, String sourceCode) {
        // 处理基础信息
        OrderCartGoodsOut orderCartGoodsOut = this.initBaseOrderCartGoodsOut(orgGoodsAdditionInfoOut, quantity, 0);
        // 判断这个商品的状态是否合法
        boolean busGateFlag = orderGoodsServer.countForBusGateBySkuCode(orgGoodsAdditionInfoOut.getGoodsCode(), appUserOut.getBizOrgCode(), appUserOut.getStoreCode(), sourceCode);
        if (busGateFlag) {
            orderCartGoodsOut.setIsEnable(ModelConst.ENABLE.YES);
        } else {
            orderCartGoodsOut.setIsEnable(ModelConst.ENABLE.NO);
        }
        // 是否可以加购
        FlashSaleCheckOut flashSaleCheckOut = this.isCanBuyFlashSaleGoods(orgGoodsAdditionInfoOut.getGoodsCode(), appUserOut.getBizOrgCode());
        int isCanBuyFlashSale = flashSaleCheckOut.getIsCanBuyFlashSale();
        orderCartGoodsOut.setIsCanBuyFlashSale(isCanBuyFlashSale);
        return orderCartGoodsOut;
    }

    /**
     * 封装购物车商品基础信息
     *
     * @param orgGoodsAdditionInfoOut
     * @param quantity
     * @param isGift
     * @return
     */
    private OrderCartGoodsOut initBaseOrderCartGoodsOut(OrderGoodsOut orgGoodsAdditionInfoOut, BigDecimal quantity, Integer isGift) {
        OrderCartGoodsOut orderCartGoodsOut = new OrderCartGoodsOut();
        orderCartGoodsOut.setSuggestedRetailPrice(orgGoodsAdditionInfoOut.getAdviceSalePrice());
        BigDecimal packageQuantity = quantity.divide(BigDecimal.valueOf(orgGoodsAdditionInfoOut.getDistributionSpecification().getQpc()), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN);
        orderCartGoodsOut.setPackageQuantity(packageQuantity);
        orderCartGoodsOut.setQuantity(quantity);
        if (null != orgGoodsAdditionInfoOut.getRetailSpecification()) {
            orderCartGoodsOut.setSpecificationUnit(orgGoodsAdditionInfoOut.getRetailSpecification().getUnitName());
        }
        orderCartGoodsOut.setGoodsCode(orgGoodsAdditionInfoOut.getGoodsCode());
        orderCartGoodsOut.setPosition(orgGoodsAdditionInfoOut.getStockCode());
        orderCartGoodsOut.setPositionStr(orgGoodsAdditionInfoOut.getStockName());
        orderCartGoodsOut.setGoodsName(orgGoodsAdditionInfoOut.getGoodsName());
        orderCartGoodsOut.setGoodsImage(orgGoodsAdditionInfoOut.getImgUrl());
        orderCartGoodsOut.setBarCode(orgGoodsAdditionInfoOut.getBarCode());
        orderCartGoodsOut.setBrand(orgGoodsAdditionInfoOut.getBrand());
        StringJoiner joiner = new StringJoiner(",");
        orgGoodsAdditionInfoOut.getStoreRecommendOutList().forEach(ppGoodsTagOut -> joiner.add(ppGoodsTagOut.getTagName()));
        orderCartGoodsOut.setTagName(joiner.toString());
        orderCartGoodsOut.setAllowDistributionReturn(orgGoodsAdditionInfoOut.getAllowDistributionReturn());
        orderCartGoodsOut.setIsShelves(orgGoodsAdditionInfoOut.getIsShelves());
        orderCartGoodsOut.setDistributionType(orgGoodsAdditionInfoOut.getDistributionWay());
        orderCartGoodsOut.setIsGift(isGift);
        orderCartGoodsOut.setDistributionSpecificationNum(BigDecimal.valueOf(orgGoodsAdditionInfoOut.getDistributionSpecification().getQpc()));
        orderCartGoodsOut.setDistributionSpecification(orgGoodsAdditionInfoOut.getDistributionSpecification().getQpcStr());
        orderCartGoodsOut.setDistributionSpecificationUnit(orgGoodsAdditionInfoOut.getDistributionSpecification().getUnitName());
        orderCartGoodsOut.setSmallSort(orgGoodsAdditionInfoOut.getSort());
        orderCartGoodsOut.setIsActivity(0);
        // 封装价格
        BigDecimal payUnitPrice = null;
        BigDecimal paySpecificationsPrice = null;
        BigDecimal originalUnitPrice = null;
        BigDecimal originalSpecificationsPrice = null;
        if (null != orgGoodsAdditionInfoOut.getDistributionUnitPrice()) {
            payUnitPrice = isGift.compareTo(0) == 0 ? orgGoodsAdditionInfoOut.getDistributionUnitPrice() : BigDecimal.ZERO;
            paySpecificationsPrice = isGift.compareTo(0) == 0 ? orgGoodsAdditionInfoOut.getDistributionPrice() : BigDecimal.ZERO;
            originalUnitPrice = orgGoodsAdditionInfoOut.getDistributionUnitPrice();
            originalSpecificationsPrice = orgGoodsAdditionInfoOut.getDistributionPrice();
        }
        orderCartGoodsOut.setPayUnitPrice(payUnitPrice);
        orderCartGoodsOut.setPaySpecificationsPrice(paySpecificationsPrice);
        orderCartGoodsOut.setOriginalUnitPrice(originalUnitPrice);
        orderCartGoodsOut.setOriginalSpecificationsPrice(originalSpecificationsPrice);
        orderCartGoodsOut.setGoodsType(orgGoodsAdditionInfoOut.getGoodsType());
        orderCartGoodsOut.setInvoiceType(orgGoodsAdditionInfoOut.getInvoiceType());
        return orderCartGoodsOut;
    }

    /**
     * 获取订货周期内的起订额校验规则代码
     *
     * @param orderCycleHeaderOut 购物车订货周期类型
     * @param storeCode           门店代码
     * @param bizOrgCode          业务组织代码
     * @return
     */
    private boolean isMatchActivityForShoppingCart(OrderCycleHeaderOut orderCycleHeaderOut, String storeCode, String bizOrgCode) {
        OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycle(storeCode, orderCycleHeaderOut.getOrderTypeConfigId(), orderCycleHeaderOut.getTruncationTime(), bizOrgCode);
        String minOrderCheckTypeCode;
        if (Objects.isNull(orderCycle)) {
            //获取配置流程选项
            List<OrderProcessConfigItem> configItemList = orderProcessSchedulingHandle.getTheOrderProcessConfigOut(orderCycleHeaderOut.getOrderTypeConfigId(),
                    OrderCycleProcessCodeEnum.ORDER_CREATE.getCode(), OrderCycleProcessConfigCodeEnum.CHECK_ACTIVITY.getCode(), bizOrgCode);
            if (CollectionUtils.isEmpty(configItemList)) {
                throw new BusinessException("订货周期" + orderCycleHeaderOut.getShortOrderType() + "的起订额配置异常");
            }
            minOrderCheckTypeCode = configItemList.get(0).getItemCode();
            if (OrderCycleProcessConfigItemCodeEnum.NO_CHECK_ACTIVITY.getCode().equals(minOrderCheckTypeCode)) {
                return false;
            }
            return OrderCycleProcessConfigItemCodeEnum.CHECK_ACTIVITY.getCode().equals(minOrderCheckTypeCode);
        } else {
            return orderConfigHandle.isMatchActivity(orderCycle.getId(), orderCycle.getBizOrgCode());
        }
    }

    /**
     * 根据条件查询购物车中某个商品记录
     *
     * @param goodsCode 商品code
     * @return
     */
    private OrdDisOrderCart getOrderCart(String goodsCode, JSONObject redisCartObject) {
        if (null != redisCartObject && null != redisCartObject.get(goodsCode)) {
            return JSON.toJavaObject(JSON.parseObject(redisCartObject.get(goodsCode).toString()), OrdDisOrderCart.class);
        }
        return null;
    }

    /**
     * 处理购物车中商品活动
     *
     * @param activity
     * @param giftOutList
     * @param otherGoodsSize
     * @param orderCartGoodsOut
     */
    private void handleOrderCartGoodsActivityInfo(ActivityAdditionVO activity, List<OrderCartGoodsOut> giftOutList, OrderCartGoodsOut orderCartGoodsOut,
                                                  int otherGoodsSize, String activityNote) {
        BigDecimal payUnitPrice = orderCartGoodsOut.getPayUnitPrice();
        BigDecimal paySpecificationsPrice = orderCartGoodsOut.getPaySpecificationsPrice();
        BigDecimal activityUnitPrice = activity.getActivityUnitPrice();
        BigDecimal activitySpecificationsPrice = (Objects.isNull(activityUnitPrice) ? payUnitPrice : activityUnitPrice).multiply(orderCartGoodsOut.getDistributionSpecificationNum());
        if (ActivityTypeEnum.FREE_GIFT.getCode().equals(activity.getActivityType())) {
            orderCartGoodsOut.setGiftOutList(giftOutList);
            orderCartGoodsOut.setActivityNote(otherGoodsSize > 0 ? "" : activityNote);
            activity.setActivityName(ActivityTypeEnum.FREE_GIFT.getTagName());
        } else {
            // 匹配分段规则
            if (CollectionUtils.isNotEmpty(activity.getActivityRegulations())) {
                int qty = orderCartGoodsOut.getQuantity().intValue();
                for (ActivityRegulationVO activityRegulation : activity.getActivityRegulations()) {
                    boolean isMatch = (Objects.isNull(activityRegulation.getDownQuantity()) && Objects.isNull(activityRegulation.getUpQuantity()))
                            || (activityRegulation.getDownQuantity() < qty && qty <= activityRegulation.getUpQuantity());
                    if (isMatch) {
                        payUnitPrice = Objects.isNull(activityRegulation.getDis()) ? activityRegulation.getPromotionPrice() : activityRegulation.getDis().multiply(orderCartGoodsOut.getPayUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
                        break;
                    }
                }
            } else {
                payUnitPrice = activity.getActivityUnitPrice();
            }
            paySpecificationsPrice = payUnitPrice.multiply(orderCartGoodsOut.getDistributionSpecificationNum()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            activityUnitPrice = activity.getActivityUnitPrice();
            activitySpecificationsPrice = (Objects.isNull(activityUnitPrice) ? payUnitPrice : activityUnitPrice).multiply(orderCartGoodsOut.getDistributionSpecificationNum());
            if (StringUtils.isBlank(activity.getActivityName())) {
                activity.setActivityName(activity.getActivityTypeName());
            }
        }
        orderCartGoodsOut.setActivityId(activity.getId());
        orderCartGoodsOut.setIsActivity(1);
        orderCartGoodsOut.setActivityType(activity.getActivityType());
        orderCartGoodsOut.setActivityCode(activity.getActivityNo());
        orderCartGoodsOut.setActivityName(activity.getActivityName());
        orderCartGoodsOut.setPayUnitPrice(payUnitPrice);
        orderCartGoodsOut.setPaySpecificationsPrice(paySpecificationsPrice);
        orderCartGoodsOut.setActivityUnitPrice(activityUnitPrice);
        orderCartGoodsOut.setActivitySpecificationsPrice(activitySpecificationsPrice);
    }

    /**
     * 校验买赠的赠品信息及封装赠品对象
     *
     * @param activity
     * @param copies
     * @param appUserOut
     * @param orderCartGoodsOut
     * @param giftOutList
     * @param builder
     * @param sourceCode
     */
    private void checkGivingGoodsAndInitGiftGoodsList(ActivityAdditionVO activity, BigDecimal copies, AppUserOut appUserOut, OrderCartGoodsOut orderCartGoodsOut,
                                                      List<OrderCartGoodsOut> giftOutList, StringBuilder builder, String sourceCode) {
        // 匹配商品级活动规则
        List<ActivityGivingGoodsVO> givingGoodsList = activity.getGivingGoodsList();
        builder.append("每买").append(activity.getActivityNum());
        if (CollectionUtils.isEmpty(orderCartGoodsOut.getOptionalGiftCodeList())) {
            // 要封装getOptionalGiftCodeList  处理选中赠品
            if ("or".equals(givingGoodsList.get(0).getCombination())) {
                orderCartGoodsOut.setOptionalGiftCodeList(Collections.singletonList(givingGoodsList.get(0).getGiftsCode()));
            } else {
                orderCartGoodsOut.setOptionalGiftCodeList(givingGoodsList.stream().map(ActivityGivingGoodsVO::getGiftsCode).collect(Collectors.toList()));
            }
        }
        for (ActivityGivingGoodsVO activityGivingGoodsVO : givingGoodsList) {
            if (orderCartGoodsOut.getOptionalGiftCodeList().contains(activityGivingGoodsVO.getGiftsCode())) {
                OrderCartGoodsOut giftGoods = this.initGiftOrderCartGoodsOut(activityGivingGoodsVO, appUserOut, copies, builder, sourceCode);
                if (Objects.nonNull(giftGoods)) {
                    giftOutList.add(giftGoods);
                }
            }
        }
    }


    /**
     * 匹配活动
     *
     * @param cycleHeaderOutMap
     * @param storeAppUserOut
     * @param sourceCode
     * @param requestType
     */
    private void handleOrderCartGoods(Map<Integer, OrderCycleHeaderOut> cycleHeaderOutMap, AppUserOut storeAppUserOut, String sourceCode, int requestType) {
        for (Map.Entry<Integer, OrderCycleHeaderOut> entry : cycleHeaderOutMap.entrySet()) {
            OrderCycleHeaderOut orderCycleHeaderOut = entry.getValue();
            // 校验是否需要匹配促销活动
            boolean isMatchActivityFlag = this.isMatchActivityForShoppingCart(orderCycleHeaderOut, storeAppUserOut.getStoreCode(), storeAppUserOut.getBizOrgCode());
            if (!isMatchActivityFlag) {
                continue;
            }
            Map<String, OrderCartGoodsOut> goodsQtyMap = orderCycleHeaderOut.getGoodsList().stream().collect(Collectors.toMap(OrderCartGoodsOut::getGoodsCode, item -> item));
            goodsQtyFor:
            for (Map.Entry<String, OrderCartGoodsOut> orderCartGoodsOutEntry : goodsQtyMap.entrySet()) {
                OrderCartGoodsOut orderCartGoodsOut = orderCartGoodsOutEntry.getValue();
                // 不可用商品不需要后续逻辑
                if (ModelConst.ENABLE.NO.equals(orderCartGoodsOut.getIsEnable())) {
                    continue;
                }
                // 已有活动商品不需要后续逻辑
                if (ModelConst.ENABLE.YES.equals(orderCartGoodsOut.getIsActivity())) {
                    continue;
                }
                // 查询活动
                ActivityAdditionVO additionVO = orderGoodsServer.getActivityByGoodsAndStore(storeAppUserOut.getBizOrgCode(), storeAppUserOut.getStoreCode(), orderCartGoodsOut.getGoodsCode());
                if (Objects.isNull(additionVO)) {
                    continue;
                }
                // 校验活动
                Map<String, OrderCartGoodsOut> otherCartGoodsMap = new HashMap<>();
                boolean isNotMatch = ActivityTypeEnum.FREE_GIFT.getCode().equals(additionVO.getActivityType()) && orderCartGoodsOut.getQuantity().compareTo(additionVO.getActivityNum()) < 0;
                if (isNotMatch) {
                    continue;
                }
                BigDecimal copies = ActivityTypeEnum.FREE_GIFT.getCode().equals(additionVO.getActivityType()) ? orderCartGoodsOut.getQuantity().divide(additionVO.getActivityNum(), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN) : BigDecimal.ZERO;
                if (ActivityTypeEnum.FREE_GIFT.getCode().equals(additionVO.getActivityType()) && CollectionUtils.isNotEmpty(additionVO.getOtherMainGoodsList())) {
                    if (copies.compareTo(BigDecimal.ONE) < 0) {
                        continue;
                    }
                    for (ActivityOtherMainGoodsVO activityOtherMainGoodsVO : additionVO.getOtherMainGoodsList()) {
                        if (!goodsQtyMap.containsKey(activityOtherMainGoodsVO.getOtherGoodsCode())) {
                            continue goodsQtyFor;
                        }
                        isNotMatch = goodsQtyMap.get(activityOtherMainGoodsVO.getOtherGoodsCode()).getQuantity().compareTo(activityOtherMainGoodsVO.getGoodsNum()) < 0;
                        if (isNotMatch) {
                            continue goodsQtyFor;
                        }
                        BigDecimal otherCopies = goodsQtyMap.get(activityOtherMainGoodsVO.getOtherGoodsCode()).getQuantity().divide(activityOtherMainGoodsVO.getGoodsNum(), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN);
                        if (otherCopies.compareTo(BigDecimal.ONE) < 0) {
                            continue goodsQtyFor;
                        }
                        copies = copies.compareTo(otherCopies) < 0 ? copies : otherCopies;
                        otherCartGoodsMap.put(activityOtherMainGoodsVO.getOtherGoodsCode(), goodsQtyMap.get(activityOtherMainGoodsVO.getOtherGoodsCode()));
                    }
                }
                if (ActivityTypeEnum.FREE_GIFT.getCode().equals(additionVO.getActivityType()) && copies.compareTo(BigDecimal.ONE) < 0) {
                    continue;
                }
                if (ActivityTypeEnum.FREE_GIFT.getCode().equals(additionVO.getActivityType())) {
                    StringBuilder builder = new StringBuilder();
                    List<OrderCartGoodsOut> giftOutList = Lists.list();
                    this.checkGivingGoodsAndInitGiftGoodsList(additionVO, copies, storeAppUserOut, orderCartGoodsOut, giftOutList, builder, sourceCode);
                    if (CollectionUtils.isNotEmpty(giftOutList)) {
                        this.handleOrderCartGoodsActivityInfo(additionVO, giftOutList, orderCartGoodsOut, otherCartGoodsMap.size(), builder.toString());
                    } else {
                        continue;
                    }
                } else {
                    this.handleOrderCartGoodsActivityInfo(additionVO, null, orderCartGoodsOut, 0, null);
                }
                if (otherCartGoodsMap.size() > 0) {
                    otherCartGoodsMap.forEach((key, value) -> {
                        value.setActivityId(additionVO.getId());
                        value.setIsActivity(1);
                        value.setActivityType(additionVO.getActivityType());
                        value.setActivityCode(additionVO.getActivityNo());
                        value.setActivityName(ActivityTypeEnum.FREE_GIFT.getTagName());
                        goodsQtyMap.put(key, value);
                    });
                }
            }
        }
    }

    /**
     * 封装购物车赠品
     *
     * @param activityGivingGoodsVO
     * @param appUserOut
     * @param copies
     * @param builder
     * @param sourceCode
     * @return
     */
    private OrderCartGoodsOut initGiftOrderCartGoodsOut(ActivityGivingGoodsVO activityGivingGoodsVO, AppUserOut appUserOut, BigDecimal copies, StringBuilder builder, String sourceCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setStoreCode(appUserOut.getStoreCode());
        orderGoodsIn.setGoodsCode(activityGivingGoodsVO.getGiftsCode());
        orderGoodsIn.setBizOrgCode(appUserOut.getBizOrgCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(sourceCode));
        OrderGoodsOut giftGoodsInfoOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        // 赠品不符合规则则没有赠品
        if (Objects.isNull(giftGoodsInfoOut)) {
            log.error("赠品" + activityGivingGoodsVO.getGiftsCode() + "不存在");
            return null;
        }
        if (null == giftGoodsInfoOut.getDistributionSpecification()) {
            log.error("赠品" + giftGoodsInfoOut.getGoodsName() + "(" + giftGoodsInfoOut.getGoodsCode() + ")配送规格为空");
            return null;
        }
        // 赠品价格
        if (null != activityGivingGoodsVO.getGiftsSpecialOffer() && activityGivingGoodsVO.getGiftsSpecialOffer().compareTo(BigDecimal.ZERO) > 0) {
            giftGoodsInfoOut.setDistributionUnitPrice(activityGivingGoodsVO.getGiftsSpecialOffer());
        }
        if (null == giftGoodsInfoOut.getDistributionUnitPrice()) {
            log.error("商品" + giftGoodsInfoOut.getGoodsName() + "(" + giftGoodsInfoOut.getGoodsCode() + ")赠品配送价异常");
            return null;
        }
        // 提示
        builder.append("送").append(activityGivingGoodsVO.getGiftsNum());
        BigDecimal quantity = copies.multiply(BigDecimal.valueOf(activityGivingGoodsVO.getGiftsNum()));
        giftGoodsInfoOut.setDistributionWay(StringUtils.isNotBlank(DistributionWaysEnum.getTypeByName(giftGoodsInfoOut.getDistributionWay())) ? DistributionWaysEnum.getTypeByName(giftGoodsInfoOut.getDistributionWay()) : giftGoodsInfoOut.getDistributionWay());
        OrderCartGoodsOut orderCartGoodsOut = this.initBaseOrderCartGoodsOut(giftGoodsInfoOut, quantity, 1);
        orderCartGoodsOut.setIsEnable(1);
        orderCartGoodsOut.setIsCanBuyFlashSale(1);
        orderCartGoodsOut.setIsGift(1);
        return orderCartGoodsOut;
    }

    private List<OrderCartOut> initOrderCartOutList(String storeCode, String bizOrgCode) {
        String orderCartKey = DisSystemConstant.DIS_ORDER_CART + bizOrgCode + ":" + storeCode;
        Object redisCartObject = redisService.hGet(orderCartKey, storeCode);
        Map<String, OrderCartOut> resultMap = new HashMap<>();
        if (null != redisCartObject) {
            JSONObject cartObj = JSON.parseObject(redisCartObject.toString());
            for (Map.Entry<String, Object> entry : cartObj.entrySet()) {
                OrderCartOut orderCartOut = JSON.toJavaObject(JSON.parseObject(entry.getValue().toString()), OrderCartOut.class);
                OrderCartOut resultOrderCartOut = resultMap.get(orderCartOut.getGoodsCode());
                BigDecimal quantity;
                if (Objects.isNull(resultOrderCartOut)) {
                    quantity = orderCartOut.getQuantity();
                } else {
                    quantity = resultOrderCartOut.getQuantity().add(orderCartOut.getQuantity());
                }
                orderCartOut.setQuantity(quantity);
                resultMap.put(orderCartOut.getGoodsCode(), orderCartOut);
            }
        }
        return new ArrayList<>(resultMap.values());
    }
}
