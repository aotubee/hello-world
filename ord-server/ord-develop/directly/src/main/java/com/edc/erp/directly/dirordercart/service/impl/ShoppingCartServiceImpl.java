/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.directly.dirordercart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.FlashSaleCheckOut;
import com.edc.erp.common.model.out.FlashSaleWeekOut;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreDelivery;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.model.vo.ActivityAdditionVO;
import com.edc.erp.common.model.vo.ActivityGivingGoodsVO;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.OrderLimitConfigService;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.StoreSkuMonthlySalesService;
import com.edc.erp.common.util.FlashSaleUtils;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.common.util.StoreDeliveryDateUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirordercart.entity.OrdDirOrderCart;
import com.edc.erp.directly.dirordercart.model.in.DeleteOrderCartIn;
import com.edc.erp.directly.dirordercart.model.in.DirOperationShoppingCartIn;
import com.edc.erp.directly.dirordercart.model.out.DirCalculationOrderCycleOut;
import com.edc.erp.directly.dirordercart.model.out.DirCalculationShoppingCartOut;
import com.edc.erp.directly.dirordercart.service.ShoppingCartService;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.distribution.handle.OrderDetailHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDetailMapper;
import com.edc.erp.directly.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.directly.distribution.model.out.OrderCartGoodsOut;
import com.edc.erp.directly.distribution.model.out.OrderCartOut;
import com.edc.erp.directly.distribution.model.out.OrderCycleHeaderOut;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.enumeration.OrderCycleProcessCodeEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigCodeEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigItemCodeEnum;
import com.edc.erp.directly.handle.DirOrderConfigHandle;
import com.edc.erp.directly.model.out.OrderCycleDeliveryOut;
import com.edc.erp.directly.orderscheduing.handle.DirOrderProcessSchedulingHandle;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
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
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private DirOrderConfigHandle orderConfigHandle;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;

    @Autowired
    private DirOrderProcessSchedulingHandle orderProcessSchedulingHandle;

    @Autowired
    private OrderDetailHandle orderDetailHandle;

    @Autowired
    private OrderHandle orderHandle;

    @Autowired
    private OrderLimitConfigService orderLimitConfigService;

    @Autowired
    private StoreSkuMonthlySalesService storeSkuMonthlySalesService;

    @Autowired
    private OrdDirOrderDetailMapper ordDirOrderDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrderCart(List<DeleteOrderCartIn> deleteOrderCartInList, String storeCode, String bizOrgCode) {
        String orderCartKey = DirSystemConstant.DIR_ORDER_CART + bizOrgCode + ":" + storeCode;
        Object redisCartObject = redisService.hGet(orderCartKey, storeCode);
        if (null == redisCartObject) {
            return;
        }
        JSONObject redisCartJsonObj = JSONObject.parseObject(redisCartObject.toString());
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

    /**
     * 更新购物车
     *
     * @param shoppingCartInList
     * @param appUserOut
     * @return
     */
    @Override
    public Response<List<OrderCycleHeaderOut>> updateShoppingCart(List<CreateOrderSkuIn> shoppingCartInList, AppUserOut appUserOut, Boolean isCopyOrder) {
        List<OrderCartOut> orderCartOutList = Lists.newArrayList();
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
            DirOrderTypeConfig dirOptOrderTypeConfig = orderConfigHandle.getOneByCombinationTypeValueList(appUserOut.getStoreCode(), bizOrgCode, combinationTypeCodeList);
            if (Objects.isNull(dirOptOrderTypeConfig)) {
                log.error("门店{}加购商品{}加购失败，未找到对应的订单类型配置。", appUserOut.getStoreCode(), createOrderSkuIn.getGoodsCode());
                return Response.error("商品" + createOrderSkuIn.getGoodsCode() + "加购失败，未找到对应的订单类型配置。");
            }
            String storeCode = appUserOut.getStoreCode();
            String orderCartKey = DirSystemConstant.DIR_ORDER_CART + bizOrgCode + ":" + storeCode;
            Object redisCartObject = redisService.hGet(orderCartKey, storeCode);
            int countOrderCart = 0;
            JSONObject cartObj = null;
            if (null != redisCartObject) {
                cartObj = JSON.parseObject(redisCartObject.toString());
                if (null != cartObj && cartObj.size() > 0) {
                    countOrderCart = cartObj.size();
                }
            }
            OrdDirOrderCart orderCart = this.getOrderCart(createOrderSkuIn.getGoodsCode(), cartObj);
            if (null == orderCart) {
                if (SystemConstant.MAX_ORDER_CART_COUNT < countOrderCart) {
                    log.info("门店{}购物车已满", storeCode);
                    return Response.error("您的购物车已满");
                }
                orderCart = new OrdDirOrderCart();
                BeanUtils.copy(createOrderSkuIn, orderCart);
            }
            orderCart.setStoreId(appUserOut.getStoreId());
            orderCart.setStoreCode(appUserOut.getStoreCode());
            this.addOrUpdateOrderCartGoods(orderCart, createOrderSkuIn.getPackageQuantity(), appUserOut.getStoreCode(), appUserOut.getBizOrgCode(), cartObj);

            OrderCartOut orderCartOut = new OrderCartOut();
            orderCartOut.setGoodsCode(createOrderSkuIn.getGoodsCode());
            orderCartOut.setQuantity(createOrderSkuIn.getPackageQuantity());
            orderCartOutList.add(orderCartOut);
        }
//        log.info("门店{}加购orderCartOutList长度------>{}", appUserOut.getStoreCode(), orderCartOutList.size());
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = this.initOrderCycleHeaderOutMap(appUserOut, orderCartOutList, SourceTypeEnum.INITIATIVE.getKey(), false);
        orderCycleHeaderOutList.forEach(orderCycleHeaderOut -> {
            String truncationTimeStr = DateUtils.format(orderCycleHeaderOut.getTruncationTime());
            // 获取该温层下分货单与跑货总金额
            BigDecimal distributionAndUpDownOrderPaidAmount = orderHandle.getTotalDistributionAndUpDownOrderAmount(appUserOut.getStoreCode(), truncationTimeStr,
                    orderCycleHeaderOut.getOrderTypeConfigId(), appUserOut.getBizOrgCode());
            orderCycleHeaderOut.setDistributionAndUpDownOrderPaidAmount(distributionAndUpDownOrderPaidAmount);
            this.handleWarningOrderQuantity(appUserOut, orderCartOutList, orderCycleHeaderOut, truncationTimeStr);
        });
        return Response.data(orderCycleHeaderOutList);
    }

    /**
     * 获取购物车订单周期类型
     *
     * @param storeAppUserOut
     * @param orderCartOutList
     * @param sourceCode
     * @param isSubmit
     * @return
     */
    @Override
    public List<OrderCycleHeaderOut> initOrderCycleHeaderOutMap(AppUserOut storeAppUserOut, List<OrderCartOut> orderCartOutList, String sourceCode, boolean isSubmit) {
        Map<Integer, OrderCycleHeaderOut> cycleHeaderOutMap = new HashMap<>(NumberUtil.INTEGER_TWO);
        boolean isOrderUpLimitFlag = SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode) || SourceTypeEnum.UPLOWDOWN.getKey().equals(sourceCode);
        // 门店的可订货上限
        Map<String, BigDecimal> orderUpLimitMap = null;
        if (isOrderUpLimitFlag) {
            orderUpLimitMap = orderLimitConfigService.findGoodsLimitByStoreCode(storeAppUserOut.getStoreCode(), storeAppUserOut.getBizOrgCode());
        }
        StringJoiner errorGoodsJoiner = new StringJoiner(",");
        // 获取配送周期信息
        List<StoreDelivery> storeDeliverLogicList = storeCenterService.getByStoreCode(storeAppUserOut.getStoreCode());
        if(CollectionUtils.isEmpty(storeDeliverLogicList)){
            throw new BusinessException("门店配送周期信息为空");
        }
        Map<String, StoreDelivery> deliveryTypeMap = storeDeliverLogicList.stream().collect(Collectors.toMap(StoreDelivery::getDeliveryType, Function.identity()));
        List<DeleteOrderCartIn> deleteOrderCartInList = Lists.newArrayList();
        for (OrderCartOut orderCartOut : orderCartOutList) {
            String bizOrgCode = storeAppUserOut.getBizOrgCode();
            List<OrderCartGoodsOut> goodsList;
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setStoreCode(storeAppUserOut.getStoreCode());
            orderGoodsIn.setGoodsCode(orderCartOut.getGoodsCode());
            orderGoodsIn.setBizOrgCode(bizOrgCode);
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(sourceCode));

            OrderGoodsOut orderGoodsOut = isSubmit ? orderGoodsServer.getStoreOrderGoods(orderGoodsIn) : orderGoodsServer.getStoreOrderGoodsByCache(orderGoodsIn);
            if (Objects.isNull(orderGoodsOut)) {
                // 移除购物车
                this.addRemoveGoodsList(deleteOrderCartInList, errorGoodsJoiner, orderCartOut.getGoodsCode());
//                log.info("门店{}加购商品{}------1", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
                continue;
            } else {
                if (Objects.isNull(orderGoodsOut.getDistributionSpecification())) {
                    if (isSubmit) {
                        throw new BusinessException("商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送规格为空");
                    } else {
                        log.error("门店{}" + storeAppUserOut.getStoreCode() + "加购商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送规格为空");
                        // 移除购物车
                        this.addRemoveGoodsList(deleteOrderCartInList, errorGoodsJoiner, orderGoodsOut.getGoodsCode());
                        continue;
                    }
                }
                if (Objects.isNull(orderGoodsOut.getDistributionUnitPrice())) {
                    if (isSubmit) {
                        throw new BusinessException("门店{}" + storeAppUserOut.getStoreCode() + "加购商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送价异常");
                    } else {
                        log.error("门店{}" + storeAppUserOut.getStoreCode() + "加购商品" + orderGoodsOut.getGoodsName() + "(" + orderGoodsOut.getGoodsCode() + ")配送价异常");
                        this.addRemoveGoodsList(deleteOrderCartInList, errorGoodsJoiner, orderGoodsOut.getGoodsCode());
                        continue;
                    }
                }
            }
//            log.info("门店{}加购商品{}------2", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
            List<String> combinationTypeCodeList = Lists.newArrayList(orderGoodsOut.getStockCode(), DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay()), StringUtils.isNotBlank(GoodsTypeEnum.getCodeByName(orderGoodsOut.getGoodsType())) ? GoodsTypeEnum.getCodeByName(orderGoodsOut.getGoodsType()) : orderGoodsOut.getGoodsType());
            // 查找订单类型配置
            DirOrderTypeConfig dirOrderTypeConfig = orderConfigHandle.getOneByCombinationTypeValueList(storeAppUserOut.getStoreCode(), bizOrgCode, combinationTypeCodeList);
            if (Objects.isNull(dirOrderTypeConfig)) {
                log.info("门店{}购物车-查询订单类型配置入参：仓位{}，配送方式{}，品类属性{}，组织{}不存在的订单类型配置", storeAppUserOut.getStoreCode(), orderGoodsOut.getStockCode(), orderGoodsOut.getDistributionWay(), orderGoodsOut.getGoodsType(), bizOrgCode);
                throw new BusinessException("购物车-不存在的订单类型配置");
            }
            OrderCycleHeaderOut orderCycleHeaderOut = cycleHeaderOutMap.get(dirOrderTypeConfig.getId());
            if (Objects.isNull(orderCycleHeaderOut)) {
//                log.info("门店{}加购商品{}------3", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
                orderCycleHeaderOut = new OrderCycleHeaderOut();
                goodsList = new ArrayList<>();
//                StoreLogisticsOut storeLogistics = storeCenterService.getStoreOnline(storeAppUserOut.getStoreId());
//                // roomDis = 0003
//                if (Objects.isNull(storeLogistics)) {
//                    throw new BusinessException("门店" + storeAppUserOut.getStoreCode() + "要货周期查找为空");
//                }

                StoreDelivery storeDelivery = deliveryTypeMap.get(dirOrderTypeConfig.getOrderPeriod());
                if(Objects.isNull(storeDelivery)){
                    throw new BusinessException("门店" + storeAppUserOut.getStoreCode() + "配送信息" + dirOrderTypeConfig.getOrderPeriod() + "为空");
                }

//                OrderCycleDeliveryOut storeDeliveryType = this.getStoreDeliveryType(storeLogistics, dirOrderTypeConfig.getOrderPeriod());
                OrderCycleDeliveryOut storeDeliveryType = this.getStoreDeliveryInfoType(storeDelivery);
                if (Objects.isNull(storeDeliveryType)) {
                    throw new BusinessException("门店" + storeAppUserOut.getStoreCode() + "要货周期为空");
                }
                if (StringUtils.isBlank(dirOrderTypeConfig.getTruncationTimePoint())) {
                    throw new BusinessException("门店" + storeAppUserOut.getStoreCode() + "平台提单时间为空");
                }
                String storeDeliveryTruncationTime = this.getStoreDeliveryTruncationTime(storeDeliveryType.getDistributionCycle(), storeDeliveryType.getDeliveryDailyCycle(), dirOrderTypeConfig.getTruncationTimePoint());
                if (StringUtils.isBlank(storeDeliveryTruncationTime)) {
                    throw new BusinessException("购物车-截单时间为空");
                }
//                log.info("门店{}加购商品{}------4", storeAppUserOut.getStoreCode(), orderCartOut.getGoodsCode());
                orderCycleHeaderOut.setOrderTypeConfigId(dirOrderTypeConfig.getId());
                orderCycleHeaderOut.setTruncationTime(DateUtils.parseTime(storeDeliveryTruncationTime + ":00"));
                orderCycleHeaderOut.setShortOrderType(dirOrderTypeConfig.getShortOrderType());
                orderCycleHeaderOut.setOrderTypeCode(dirOrderTypeConfig.getOrderTypeCode());
                orderCycleHeaderOut.setMinimumOrderAmount(dirOrderTypeConfig.getMinimumOrderAmount());
                // 查找订货周期
                OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycle(storeAppUserOut.getStoreCode(), orderCycleHeaderOut.getOrderTypeConfigId(), orderCycleHeaderOut.getTruncationTime(), bizOrgCode);
                if (Objects.nonNull(orderCycle)) {
                    orderCycleHeaderOut.setOrderCycleId(orderCycle.getId());
                }
                this.checkOrderSource(orderCycleHeaderOut, orderCycle, sourceCode, bizOrgCode);
                // 获取起订额校验配置项
                String minAmountCheckType = this.getMinOrderCheckTypeCode(orderCycleHeaderOut, orderCycle, bizOrgCode);
                orderCycleHeaderOut.setMinAmountCheckType(minAmountCheckType);
            } else {
                goodsList = orderCycleHeaderOut.getGoodsList();
            }

            //配送方式中文转换
            orderGoodsOut.setDistributionWay(StringUtils.isNotBlank(DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay())) ? DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay()) : orderGoodsOut.getDistributionWay());

            // 创建购物车商品对象
            BigDecimal specNum = BigDecimal.valueOf(orderGoodsOut.getDistributionSpecification().getQpc());
            BigDecimal totalQuantity = orderCartOut.getQuantity().multiply(specNum);
            BigDecimal canQty = null;
            // 存在可订货上限，需要去拿当前周期的订货数累加是否超出
            if (isOrderUpLimitFlag && orderUpLimitMap.containsKey(orderCartOut.getGoodsCode())) {
                // 已订量
                BigDecimal orderedQty = BigDecimal.ZERO;
                if (Objects.nonNull(orderCycleHeaderOut.getOrderCycleId())) {
                    orderedQty = orderedQty.add(ordDirOrderDetailMapper.getCycleOrderedGoodsQty(orderCycleHeaderOut.getOrderCycleId(), orderGoodsOut.getGoodsCode()));
                }
                // 可订量
                canQty = orderUpLimitMap.get(orderCartOut.getGoodsCode()).subtract(orderedQty);
                if (isSubmit || !SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode)) {
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
                    storeAppUserOut, orderCycleHeaderOut, sourceCode);
            orderCartGoodsOut.setCanQty(canQty);
            goodsList.add(orderCartGoodsOut);
            orderCycleHeaderOut.setGoodsList(goodsList);
            orderCycleHeaderOut.setOrderTypeConfigId(dirOrderTypeConfig.getId());
            orderCycleHeaderOut.setDistributionAndUpDownOrderPaidAmount(BigDecimal.ZERO);
            cycleHeaderOutMap.put(dirOrderTypeConfig.getId(), orderCycleHeaderOut);
        }
        if (CollectionUtils.isNotEmpty(deleteOrderCartInList)) {
            log.error("直营门店{}获取购物车列表移除不可用商品：{}", storeAppUserOut.getStoreCode(), errorGoodsJoiner);
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

    private OrderCartGoodsOut initDirInvalidOrderCartGoodsOut(OrderGoodsOut orderGoodsOut) {
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
     * 校验允许订货类型配置
     *
     * @param orderCycleHeaderOut
     * @param orderCycle
     * @param sourceCode
     * @param bizOrgCode
     */
    public void checkOrderSource(OrderCycleHeaderOut orderCycleHeaderOut, OrdDirOrderCycle orderCycle, String sourceCode, String bizOrgCode) {
        String sourceConfigItemCode = null;
        if (SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode)) {
            sourceConfigItemCode = OrderCycleProcessConfigItemCodeEnum.MANUAL.getCode();
        }
        if (SourceTypeEnum.DISTRIBUTION.getKey().equals(sourceCode)) {
            sourceConfigItemCode = OrderCycleProcessConfigItemCodeEnum.DISTRIBUTION.getCode();
        }
        if (SourceTypeEnum.UPLOWDOWN.getKey().equals(sourceCode)) {
            sourceConfigItemCode = OrderCycleProcessConfigItemCodeEnum.UP_AND_DOWN.getCode();
        }
        if (StringUtils.isBlank(sourceConfigItemCode)) {
            throw new BusinessException("无效的下单来源");
        }
        String prefixErrorMsg = orderCycleHeaderOut.getShortOrderType() + "(订单类型代码：" + orderCycleHeaderOut.getOrderTypeCode() + ")";
        Map<String, String> allowOrderSourceMap;
        if (Objects.isNull(orderCycle)) {
            //获取配置流程选项
            List<DirOrderProcessConfigItem> configItemList = orderProcessSchedulingHandle.getTheOrderProcessConfigOut(orderCycleHeaderOut.getOrderTypeConfigId(),
                    OrderCycleProcessCodeEnum.ORDER_CREATE.getCode(), OrderCycleProcessConfigCodeEnum.ORDER_SOURCE_CODE.getCode(), bizOrgCode);
            if (CollectionUtils.isEmpty(configItemList)) {
                throw new BusinessException("订单类型代码为" + orderCycleHeaderOut.getOrderTypeCode() + "的允许订货类型配置异常");
            }
            allowOrderSourceMap = configItemList.stream().collect(Collectors.toMap(DirOrderProcessConfigItem::getItemCode, DirOrderProcessConfigItem::getItemName));
        } else {
            prefixErrorMsg = prefixErrorMsg + "截单周期" + orderCycle.getTruncationDateTime().toLocalDate();
            List<DirOrderProcessConfigItem> allowOrderSourceList = orderConfigHandle.findAllowOrderSource(orderCycle.getId(), orderCycle.getStoreCode(), orderCycle.getBizOrgCode());
            if (CollectionUtils.isEmpty(allowOrderSourceList)) {
                throw new BusinessException(prefixErrorMsg + "的允许订货类型配置异常");
            }
            allowOrderSourceMap = allowOrderSourceList.stream().collect(Collectors.toMap(DirOrderProcessConfigItem::getItemCode, DirOrderProcessConfigItem::getItemName));
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
    private String getMinOrderCheckTypeCode(OrderCycleHeaderOut orderCycleHeaderOut, OrdDirOrderCycle orderCycle, String bizOrgCode) {
        String minOrderCheckTypeCode;
        String prefixErrorMsg = orderCycleHeaderOut.getShortOrderType() + "(订单类型代码：" + orderCycleHeaderOut.getOrderTypeCode() + ")";
        if (Objects.isNull(orderCycle)) {
            //获取配置流程选项
            List<DirOrderProcessConfigItem> configItemList = orderProcessSchedulingHandle.getTheOrderProcessConfigOut(orderCycleHeaderOut.getOrderTypeConfigId(),
                    OrderCycleProcessCodeEnum.ORDER_CREATE.getCode(), OrderCycleProcessConfigCodeEnum.MIN_AMOUNT.getCode(), bizOrgCode);
            if (CollectionUtils.isEmpty(configItemList)) {
                throw new BusinessException(prefixErrorMsg + "的起订额配置异常");
            }
            minOrderCheckTypeCode = configItemList.get(0).getItemCode();
        } else {
            DirOrderProcessConfigItem dirOrderProcessConfigItem = orderConfigHandle.minAmountCheckType(orderCycle.getId(), orderCycle.getBizOrgCode());
            if (Objects.isNull(dirOrderProcessConfigItem)) {
                throw new BusinessException(prefixErrorMsg + "截单周期" + orderCycle.getTruncationDateTime().toLocalDate() + "的起订额副本配置异常");
            }
            minOrderCheckTypeCode = dirOrderProcessConfigItem.getItemCode();
        }
        return minOrderCheckTypeCode;
    }

    /**
     * 封装购物车主商品
     *
     * @param orgGoodsAdditionInfoOut
     * @param quantity
     * @param appUserOut
     * @param orderCycleHeaderOut
     * @param sourceCode
     * @return
     */
    private OrderCartGoodsOut getMainOrderCartGoodsOut(OrderGoodsOut orgGoodsAdditionInfoOut, BigDecimal quantity,
                                                       AppUserOut appUserOut, OrderCycleHeaderOut orderCycleHeaderOut, String sourceCode) {
        // 处理基础信息
        OrderCartGoodsOut orderCartGoodsOut = this.initBaseOrderCartGoodsOut(orgGoodsAdditionInfoOut, quantity, 0);
        // 判断这个商品的状态是否合法
        boolean busGateFlag = orderGoodsServer.countForBusGateBySkuCode(orgGoodsAdditionInfoOut.getGoodsCode(), appUserOut.getBizOrgCode(), appUserOut.getStoreCode(), sourceCode);
        if (busGateFlag) {
            orderCartGoodsOut.setIsEnable(1);
        } else {
            orderCartGoodsOut.setIsEnable(0);
        }
        // 校验是否需要匹配促销活动
        boolean isMatchActivityFlag = this.isMatchActivityForShoppingCart(orderCycleHeaderOut, appUserOut.getStoreCode(), orgGoodsAdditionInfoOut.getBizOrgCode());
        if (isMatchActivityFlag) {
            // 处理活动
            ActivityAdditionVO activity = orderGoodsServer.getActivityByGoodsAndStore(appUserOut.getBizOrgCode(), appUserOut.getStoreCode(), orgGoodsAdditionInfoOut.getGoodsCode());
            if (null != activity) {
                this.handleOrderCartGoodsActivityInfo(orgGoodsAdditionInfoOut, activity, quantity, appUserOut, orderCartGoodsOut, sourceCode);
            }
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
//        OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycle(storeCode, orderCycleHeaderOut.getOrderTypeConfigId(), orderCycleHeaderOut.getTruncationTime(), bizOrgCode);
//        if (Objects.isNull(orderCycle)) {
//            //直营订单类型配值不校验促销
//            return false;
//        } else {
//            return orderConfigHandle.isMatchActivity(orderCycle.getId(), orderCycle.getBizOrgCode());
//        }
        // 直营直接匹配活动 不再校验是否匹配 2023-01-31
        return true;
    }

    /**
     * 处理购物车中商品活动
     *
     * @param orgGoodsAdditionInfoOut
     * @param activity
     * @param quantity
     * @param appUserOut
     * @param orderCartGoodsOut
     */
    private void handleOrderCartGoodsActivityInfo(OrderGoodsOut orgGoodsAdditionInfoOut, ActivityAdditionVO activity,
                                                  BigDecimal quantity, AppUserOut appUserOut, OrderCartGoodsOut orderCartGoodsOut, String sourceCode) {
        BigDecimal payUnitPrice;
        BigDecimal paySpecificationsPrice;
        BigDecimal activityUnitPrice;
        BigDecimal activitySpecificationsPrice;
        if (ActivityTypeEnum.FREE_GIFT.getCode().equals(activity.getActivityType())) {
            if (Objects.isNull(orgGoodsAdditionInfoOut.getDistributionSpecification())) {
                throw new BusinessException("商品" + orgGoodsAdditionInfoOut.getGoodsCode() + "配货规格异常");
            }
            String retailUnitName = Objects.nonNull(orgGoodsAdditionInfoOut.getRetailSpecification()) ? orgGoodsAdditionInfoOut.getRetailSpecification().getUnitName() : "";
            OrderCartGoodsOut giftOut = this.initGiftOrderCartGoodsOut(activity, quantity, appUserOut, sourceCode, retailUnitName);
            orderCartGoodsOut.setOptionalGiftCodeList(Collections.singletonList(giftOut.getGoodsCode()));
            orderCartGoodsOut.setGiftOutList(Collections.singletonList(giftOut));
            payUnitPrice = orgGoodsAdditionInfoOut.getDistributionUnitPrice();
            paySpecificationsPrice = payUnitPrice.multiply(orderCartGoodsOut.getDistributionSpecificationNum());
            activityUnitPrice = activity.getActivityUnitPrice();
            activitySpecificationsPrice = paySpecificationsPrice;
            activity.setActivityName(ActivityTypeEnum.FREE_GIFT.getTagName());
        } else {
            payUnitPrice = activity.getActivityUnitPrice();
            paySpecificationsPrice = payUnitPrice.multiply(orderCartGoodsOut.getDistributionSpecificationNum());
            activityUnitPrice = activity.getActivityUnitPrice();
            activitySpecificationsPrice = paySpecificationsPrice;
            if (StringUtils.isBlank(activity.getActivityName())) {
                activity.setActivityName(activity.getActivityTypeName());
            }
        }
        orderCartGoodsOut.setActivityId(activity.getId());
        orderCartGoodsOut.setIsActivity(ModelConst.ENABLE.YES);
        orderCartGoodsOut.setActivityType(activity.getActivityType());
        orderCartGoodsOut.setActivityCode(activity.getActivityNo());
        orderCartGoodsOut.setActivityName(activity.getActivityName());
        orderCartGoodsOut.setPayUnitPrice(payUnitPrice);
        orderCartGoodsOut.setPaySpecificationsPrice(paySpecificationsPrice);
        orderCartGoodsOut.setActivityUnitPrice(activityUnitPrice);
        orderCartGoodsOut.setActivitySpecificationsPrice(activitySpecificationsPrice);
    }

    @Override
    public List<OrderCycleHeaderOut> findMyOrderCartList(AppUserOut appUserOut) {
        // 初始化redis中购物车集合数据
        List<OrderCartOut> orderCartOutList = this.initOrderCartOutList(appUserOut.getStoreCode(), appUserOut.getBizOrgCode());
        // 按更新时间倒序排列
        Collections.sort(orderCartOutList, (o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()));
        // 将购物车中商品以Map方式封装到订货周期
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = this.initOrderCycleHeaderOutMap(appUserOut, orderCartOutList, SourceTypeEnum.INITIATIVE.getKey(), false);
        orderCycleHeaderOutList.stream().forEach(orderCycleHeaderOut -> {
            String truncationTimeStr = DateUtils.format(orderCycleHeaderOut.getTruncationTime());
            // 获取该温层下分货单与跑货总金额
            BigDecimal distributionAndUpDownOrderPaidAmount = orderHandle.getTotalDistributionAndUpDownOrderAmount(appUserOut.getStoreCode(), truncationTimeStr,
                    orderCycleHeaderOut.getOrderTypeConfigId(), appUserOut.getBizOrgCode());
            orderCycleHeaderOut.setDistributionAndUpDownOrderPaidAmount(distributionAndUpDownOrderPaidAmount);
            this.handleWarningOrderQuantity(appUserOut, orderCartOutList, orderCycleHeaderOut, truncationTimeStr);
        });
        return orderCycleHeaderOutList;
    }


    private List<OrderCartOut> initOrderCartOutList(String storeCode, String bizOrgCode) {
        String orderCartKey = DirSystemConstant.DIR_ORDER_CART + bizOrgCode + ":" + storeCode;
        Object redisCartObject = redisService.hGet(orderCartKey, storeCode);
        Map<String, OrderCartOut> resultMap = new HashMap<>();
        if (null != redisCartObject) {
            JSONObject cartObj = JSONObject.parseObject(redisCartObject.toString());
            for (Map.Entry<String, Object> entry : cartObj.entrySet()) {
                OrderCartOut orderCartOut = JSONObject.toJavaObject(JSON.parseObject(entry.getValue().toString()), OrderCartOut.class);
                OrderCartOut resultOrderCartOut = resultMap.get(orderCartOut.getGoodsCode());
                BigDecimal quantity = BigDecimal.ZERO;
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

    private void handleWarningOrderQuantity(AppUserOut appUserOut, List<OrderCartOut> orderCartOutList,
                                            OrderCycleHeaderOut orderCycleHeaderOut, String truncationTimeStr) {
        List<String> skuList = Lists.newArrayList();
        orderCartOutList.forEach(orderCartOut -> skuList.add(orderCartOut.getGoodsCode()));
        // 当前购物车中商品月销量
        Map<String, BigDecimal> averageDailySalesDaysVOMap = storeSkuMonthlySalesService.getSkuAverageDailySalesDaysListByStoreCode(skuList,
                appUserOut.getStoreCode(), appUserOut.getBizOrgCode());
        // 获取本次截单下已下单商品
        List<OrdDirOrder> noInvalidOrderList = orderHandle.findNoInvalidOrderListByParameter(appUserOut.getStoreCode(), truncationTimeStr,
                orderCycleHeaderOut.getOrderTypeConfigId(), appUserOut.getOrgCode());
        List<Long> idList = noInvalidOrderList.stream().map(OrdDirOrder::getId).collect(Collectors.toList());
        orderCycleHeaderOut.getGoodsList().stream().forEach(orderCartGoodsOut -> {
            if (0 == orderCartGoodsOut.getIsGift()) {
                Integer isNeedWarningTag = 0;
                BigDecimal sumExistsPackageQuantity = orderDetailHandle.sumPackageByOrderIdList(orderCartGoodsOut.getGoodsCode(),
                        idList, appUserOut, truncationTimeStr);
                orderCartGoodsOut.setExistsQuantity(sumExistsPackageQuantity);
                BigDecimal qty = averageDailySalesDaysVOMap.get(orderCartGoodsOut.getGoodsCode());
                if (null != qty && null != orderCartGoodsOut.getIsEnable() && NumberUtils.INTEGER_ONE.equals(orderCartGoodsOut.getIsEnable())) {
                    BigDecimal packageQty = qty.divide(orderCartGoodsOut.getDistributionSpecificationNum(), 0, RoundingMode.DOWN);
                    BigDecimal targetOrderQuantity = orderCartGoodsOut.getExistsQuantity().add(orderCartGoodsOut.getPackageQuantity());
                    isNeedWarningTag = orderDetailHandle.getIsNeedOrderQuantityWarning(targetOrderQuantity, packageQty);
                }
                orderCartGoodsOut.setIsNeedWarningTag(isNeedWarningTag);
            }
        });
        Collections.sort(orderCycleHeaderOut.getGoodsList(), (o1, o2) -> o2.getIsNeedWarningTag().compareTo(o1.getIsNeedWarningTag()));
    }

    @Override
    public void addOrUpdateOrderCartGoods(OrdDirOrderCart ordDirOrderCart, BigDecimal quantity, String storeCode, String bizOrgCode, JSONObject redisCartObject) {
        if (null != redisCartObject && quantity.compareTo(BigDecimal.ZERO) == 0) {
            redisCartObject.remove(ordDirOrderCart.getGoodsCode());
        } else {
            if (null == redisCartObject) {
                redisCartObject = new JSONObject();
            }
            ordDirOrderCart.setQuantity(quantity);
            ordDirOrderCart.setUpdator(storeCode);
            ordDirOrderCart.setUpdateTime(LocalDateTime.now());
            ordDirOrderCart.setBizOrgCode(bizOrgCode);
            redisCartObject.put(ordDirOrderCart.getGoodsCode(), ordDirOrderCart);
        }
        String orderCartKey = DirSystemConstant.DIR_ORDER_CART + bizOrgCode + ":" + storeCode;
        redisService.hSet(orderCartKey, storeCode, redisCartObject.toJSONString());
    }

    /**
     * 根据条件查询购物车中某个商品记录
     *
     * @param goodsCode 商品code
     * @return
     */
    private OrdDirOrderCart getOrderCart(String goodsCode, JSONObject redisCartObject) {
        if (null != redisCartObject && null != redisCartObject.get(goodsCode)) {
            OrdDirOrderCart orderCart = JSONObject.toJavaObject(JSON.parseObject(redisCartObject.get(goodsCode).toString()), OrdDirOrderCart.class);
            return orderCart;
        }
        return null;
    }


    /**
     * 核算购物车金额
     *
     * @param dirOperationShoppingCartInList
     * @param appUserOut
     * @return
     */
    @Override
    public DirCalculationShoppingCartOut calculationShoppingCartAmount(List<DirOperationShoppingCartIn> dirOperationShoppingCartInList, AppUserOut appUserOut) {
        String storeCode = appUserOut.getStoreCode();
        // 初始化redis中购物车集合数据
        List<OrderCartOut> orderCartOutList = Lists.newArrayList();
        dirOperationShoppingCartInList.forEach(dirOperationShoppingCartIn -> {
            OrderCartOut orderCartOut = new OrderCartOut();
            orderCartOut.setQuantity(dirOperationShoppingCartIn.getPackageQuantity());
            orderCartOut.setGoodsCode(dirOperationShoppingCartIn.getGoodsCode());
            orderCartOutList.add(orderCartOut);
        });
        // 将购物车中商品以Map方式封装到订货周期
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = this.initOrderCycleHeaderOutMap(appUserOut, orderCartOutList, SourceTypeEnum.INITIATIVE.getKey(), false);
        List<DirCalculationOrderCycleOut> calculationShoppingCartOrderTypeList = new ArrayList<>();
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
                totalDistributionAndUpDownAmount = orderHandle.getTotalDistributionAndUpDownOrderAmount(storeCode, DateUtils.format(cycleHeaderOut.getTruncationTime()),
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
            DirCalculationOrderCycleOut dirCalculationOrderCycleOut = new DirCalculationOrderCycleOut();
            dirCalculationOrderCycleOut.setOrderTypeConfigId(cycleHeaderOut.getOrderTypeConfigId());
            dirCalculationOrderCycleOut.setShortOrderType(cycleHeaderOut.getShortOrderType());
            dirCalculationOrderCycleOut.setListAmount(listAmount);
            dirCalculationOrderCycleOut.setIsHaveDistributionOrder(totalDistributionAndUpDownAmount.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
            calculationShoppingCartOrderTypeList.add(dirCalculationOrderCycleOut);
        }
        DirCalculationShoppingCartOut calculationShoppingCartOut = new DirCalculationShoppingCartOut();
        calculationShoppingCartOut.setCalculationOrderCycleOutList(calculationShoppingCartOrderTypeList);
        calculationShoppingCartOut.setOrderAmount(orderAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        calculationShoppingCartOut.setPreferentialAmount(preferentialAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        return calculationShoppingCartOut;
    }

    @Override
    public FlashSaleCheckOut isCanBuyByDistributionOrder(String skuCode, String bizOrgCode, LocalDateTime targetDateTime) {
        List<FlashSaleWeekOut> flashSaleWeekOutList = orderGoodsServer.findFlashSaleWeekOutList(skuCode, bizOrgCode);
        return FlashSaleUtils.getIsCanBuyFlashSaleGoods(flashSaleWeekOutList, targetDateTime);
    }

    /**
     * 封装购物车赠品
     *
     * @param activity
     * @param buyQty
     * @return
     */
    private OrderCartGoodsOut initGiftOrderCartGoodsOut(ActivityAdditionVO activity, BigDecimal buyQty, AppUserOut appUserOut, String sourceCode, String retailUnitName) {
        ActivityGivingGoodsVO activityGivingGoodsVO = activity.getGivingGoodsList().get(0);
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(activityGivingGoodsVO.getGiftsCode());
        orderGoodsIn.setBizOrgCode(appUserOut.getBizOrgCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(sourceCode));
        orderGoodsIn.setStoreCode(appUserOut.getStoreCode());
        OrderGoodsOut giftGoodsInfoOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(giftGoodsInfoOut)) {
            throw new BusinessException("商品(" + activityGivingGoodsVO.getGiftsCode() + ")不存在");
        }
        if (null == giftGoodsInfoOut.getDistributionSpecification()) {
            throw new BusinessException("商品" + giftGoodsInfoOut.getGoodsName() + "(" + giftGoodsInfoOut.getGoodsCode() + ")配送规格为空");
        }
        BigDecimal quantity = buyQty.divide(activity.getActivityNum(), 0, RoundingMode.DOWN).multiply(BigDecimal.valueOf(activityGivingGoodsVO.getGiftsNum()));
        giftGoodsInfoOut.setDistributionWay(StringUtils.isNotBlank(DistributionWaysEnum.getTypeByName(giftGoodsInfoOut.getDistributionWay())) ? DistributionWaysEnum.getTypeByName(giftGoodsInfoOut.getDistributionWay()) : giftGoodsInfoOut.getDistributionWay());

        OrderCartGoodsOut orderCartGoodsOut = this.initBaseOrderCartGoodsOut(giftGoodsInfoOut, quantity, 1);
        orderCartGoodsOut.setIsEnable(ModelConst.ENABLE.YES);
        orderCartGoodsOut.setIsCanBuyFlashSale(ModelConst.ENABLE.YES);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("每买" + activity.getActivityNum() + retailUnitName + "送" + activityGivingGoodsVO.getGiftsNum());
        if (null != giftGoodsInfoOut.getRetailSpecification()) {
            stringBuilder.append(giftGoodsInfoOut.getRetailSpecification().getUnitName());
        }
        orderCartGoodsOut.setActivityNote(stringBuilder.toString());
        return orderCartGoodsOut;
    }
}
