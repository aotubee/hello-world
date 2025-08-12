package com.edc.erp.upperlowerlimit.server.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.warning.StoreOrderWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.UpLowerLimitListWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.WarningBusinessTypeEnum;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreDelivery;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.model.vo.InOneQtyVO;
import com.edc.erp.common.service.AppUserService;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.WarningService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.common.util.StoreDeliveryDateUtil;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disordercart.service.DisShoppingCartService;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.distribution.model.out.OrderCartOut;
import com.edc.erp.handle.DisOrderConfigHandle;
import com.edc.erp.model.out.OrderCycleDeliveryOut;
import com.edc.erp.upperlowerlimit.mapper.UpperLowerLimitMapper;
import com.edc.erp.upperlowerlimit.model.out.InvStockStoreOut;
import com.edc.erp.upperlowerlimit.model.out.ReplenishmentConfig;
import com.edc.erp.upperlowerlimit.model.out.ReplenishmentStoreDisabledRange;
import com.edc.erp.upperlowerlimit.server.UpperLowerLimitServer;
import com.edc.plugins.common.exception.BusinessException;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 上下限跑货
 *
 * @author weichao
 */
@Slf4j
@Service
public class UpperLowerLimitServerImpl implements UpperLowerLimitServer {

    @Autowired
    private UpperLowerLimitMapper upperLowerLimitMapper;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private DisOrderHandle orderHandle;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private DisOrderConfigHandle orderConfigHandle;

    @Autowired
    private DisShoppingCartService shoppingCartService;

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    @Autowired
    private WarningService warningService;

    /**
     * 线程池数
     */
    private static final int THREAD_COUNT = 10;

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 4;

    /**
     * 任务执行数量
     */
    private static final int EXECUTED_QUANTITY = 100;

    @Override
    public List<ReplenishmentConfig> findReplenishmentConfigList(String bizOrgCode, String storeCode) {
        return upperLowerLimitMapper.findReplenishmentConfigList(bizOrgCode, storeCode);
    }

    @Override
    public void replenishmentOrderJob(List<String> storeList, String bizOrgCode) {
//        String storeProperty = StoreConstant.StoreProperty.FRANCHISE.getMytValue();
//        List<StoreInfo> storeInfoList = storeCenterService.findStoreInfoByProperty(storeCodeList, storeProperty, bizOrgCode);
//        if (CollectionUtils.isEmpty(storeInfoList)) {
//            log.info("加盟---业务组织{}没查到可用门店", bizOrgCode);
//            return;
//        }
//        List<String> storeList = storeInfoList.stream().map(StoreInfo::getErpStoreCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(storeList)) {
            return;
        }
        if (storeList.size() < EXECUTED_QUANTITY) {
            this.executedJob(storeList, bizOrgCode);
        } else {
            // 计算每个线程处理多少条数据
            int threadDataSize = storeList.size() / CORE_POOL_SIZE;
            // 判断是否是最后一个线程
//            boolean breakFlag = storeList.size() % CORE_POOL_SIZE == 0;
            //设置线程池
            ExecutorService threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, THREAD_COUNT, 5L,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
            List<String> needHandleStoreCodeList;
            for (int i = 0; i < CORE_POOL_SIZE; i++) {
                if (i == CORE_POOL_SIZE - 1) {
//                    if (breakFlag) {
//                        break;
//                    }
                    needHandleStoreCodeList = storeList.subList(i * threadDataSize, storeList.size());
                } else {
                    needHandleStoreCodeList = storeList.subList(i * threadDataSize, threadDataSize * (i + 1));
                }
                // 开启线程
                List<String> finalNeedHandleStoreCodeList = needHandleStoreCodeList;
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        log.info("当前线程待执处理的加盟跑货有{}条数据........................", finalNeedHandleStoreCodeList.size());
                        executedJob(finalNeedHandleStoreCodeList, bizOrgCode);
                    }
                });
            }
            //关闭线程池
            MoreExecutors.shutdownAndAwaitTermination(threadPool, 6, TimeUnit.HOURS);
        }
    }

    /**
     * 执行跑货业务
     *
     * @param storeCodeList
     * @param bizOrgCode
     */
    private void executedJob(List<String> storeCodeList, String bizOrgCode) {
        StringJoiner storeCodesJoiner = new StringJoiner(SystemConstant.COMMA);
        for (String storeCode : storeCodeList) {
            try {
                // 取出门店(code)下所有商品
                List<ReplenishmentConfig> list = this.findReplenishmentConfigList(bizOrgCode, storeCode);
                log.info("门店：{} 开始跑货", storeCode);
                boolean isAllDoNotMeetConditions = this.singleStoreReplenishment(storeCode, bizOrgCode, list);
                if (isAllDoNotMeetConditions) {
                    storeCodesJoiner.add(storeCode);
                }
                log.info("门店：{} 跑货结束", storeCode);
            } catch (Exception e) {
                String msg = MessageFormat.format(UpLowerLimitListWarningTypeEnum.UP_LOWER_LIMIT_NOT_RUNNING_CARGO.getErrorMessage(), StoreOrderWarningTypeEnum.FRANCHISE.getName(), storeCode, e.getMessage());
//                warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(), UpLowerLimitListWarningTypeEnum.UP_LOWER_LIMIT_NOT_RUNNING_CARGO.getType(),
//                        msg, null, null, bizOrgCode);
                warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
                        UpLowerLimitListWarningTypeEnum.UP_LOWER_LIMIT_NOT_RUNNING_CARGO.getType(),
                        msg, null, null, bizOrgCode);
                log.error("门店：{} 跑货异常: ", storeCode, e);
            }
        }
        if (storeCodesJoiner.length() > 0) {
            String msg = MessageFormat.format(UpLowerLimitListWarningTypeEnum.UP_LOWER_LIMIT_NOT_RUNNING_CARGO.getErrorMessage(), StoreOrderWarningTypeEnum.FRANCHISE.getName(),
                    storeCodesJoiner, "上下限跑货商品均不符合条件");
//            warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(), UpLowerLimitListWarningTypeEnum.UP_LOWER_LIMIT_NOT_RUNNING_CARGO.getType(),
//                    msg, null, null, bizOrgCode);
            warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
                    UpLowerLimitListWarningTypeEnum.UP_LOWER_LIMIT_NOT_RUNNING_CARGO.getType(),
                    msg, null, null, bizOrgCode);
        }
    }


    @Override
    public List<ReplenishmentStoreDisabledRange> isReplenishmentStoreDisabled(String storeCode, String bizOrgCode) {
        ReplenishmentStoreDisabledRange storeDisabledRange = new ReplenishmentStoreDisabledRange();
        storeDisabledRange.setIsInvalid(NumberUtil.INTEGER_ZERO);
        storeDisabledRange.setIsDelete(NumberUtil.INTEGER_ZERO);
        storeDisabledRange.setStoreCode(storeCode);
        storeDisabledRange.setBizOrgCode(bizOrgCode);
        return upperLowerLimitMapper.isReplenishmentStoreDisabled(storeDisabledRange);
    }

    private boolean singleStoreReplenishment(String storeCode, String bizOrgCode, List<ReplenishmentConfig> list) {
        if (CollectionUtils.isEmpty(list)) {
            log.info(storeCode + "无跑货商品信息+++++++++++++++++++++++++++");
            return false;
        }
        Map<Integer, List<OrderCartOut>> storeOrderCartMap = new LinkedHashMap<>();
        StoreInfo storeInfo = storeCenterService.getStoreByCode(storeCode, bizOrgCode);
        if (storeInfo == null) {
            log.info("{}未获取到门店信息", storeCode);
            return false;
        }
        // 检查当天门店是否设置了不跑货
        List<ReplenishmentStoreDisabledRange> storeDisabledRanges = this.isReplenishmentStoreDisabled(storeCode, storeInfo.getBizOrgCode());
        if (CollectionUtils.isNotEmpty(storeDisabledRanges)) {
            log.info("{}设置今天不跑货", storeCode);
            return false;
        }
        // 门店下所有商品转化为购物车模式
        Integer storeId = storeInfo.getStoreId();
        StoreLogisticsOut storeLogisticsOut = storeCenterService.getStoreOnline(storeId);
        if (storeLogisticsOut == null) {
            log.info("{}未获取到门店配送信息", storeCode);
            return false;
        }
        List<String> goodsCodesList = list.stream().map(ReplenishmentConfig::getSkuCode).collect(Collectors.toList());
        List<InvStockStoreOut> invStockStoreOuts = upperLowerLimitMapper.findStoreBizInvQtyList(bizOrgCode, storeCode, goodsCodesList);
        if (CollectionUtils.isEmpty(invStockStoreOuts)) {
            throw new BusinessException("跑货时门店" + storeCode + "查询门店库存为空");
        }
        Map<String, InvStockStoreOut> invMap = invStockStoreOuts.stream().collect(Collectors.toMap(InvStockStoreOut::getGoodsCode, item -> item));

        List<OrderCartOut> orderCartOutList = new ArrayList<>();

        // 跳过商品集合
//        Map<String, String> passGoodsMap = new HashMap<>();
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        // 获取配送周期信息
        List<StoreDelivery> storeDeliverLogicList = storeCenterService.getByStoreCode(storeCode);
        if (CollectionUtils.isEmpty(storeDeliverLogicList)) {
            throw new BusinessException("门店配送信息为空");
        }
        Map<String, StoreDelivery> deliveryTypeMap = storeDeliverLogicList.stream().collect(Collectors.toMap(StoreDelivery::getDeliveryType, Function.identity()));
        // 待跑货门店下商品集合
        for (ReplenishmentConfig replenishmentConfig : list) {
            //上下限跑货门店的商品范围为：
            OrderGoodsIn query = new OrderGoodsIn();
            query.setBizOrgCode(bizOrgCode);
            query.setGoodsCode(replenishmentConfig.getSkuCode());
            query.setBusinessType(BusinessTypeColumnEnum.UP_LOW_DOWN.getType());
            query.setStoreCode(storeCode);
            OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(query);
//            if (orderGoodsOut == null) {
//                log.info("B-门店{},商品{}", storeCode, replenishmentConfig.getSkuCode());
//                errorJoiner.add("商品" + replenishmentConfig.getSkuCode() + "不允许跑货");
//                passGoodsMap.put(replenishmentConfig.getSkuCode(), replenishmentConfig.getSkuCode());
//                continue;
//            }
            if (Objects.isNull(orderGoodsOut)) {
                log.info("门店{},商品{}---{}", storeCode, replenishmentConfig.getSkuCode(), "不允许跑货");
//                this.addErrorMessage(errorJoiner, replenishmentConfig.getSkuCode(), "不允许跑货");
                continue;
            } else {
                if (Objects.isNull(orderGoodsOut.getDistributionSpecification())) {
                    log.info("门店{},商品{}---{}", storeCode, replenishmentConfig.getSkuCode(), "配送规格为空");
                    this.addErrorMessage(errorJoiner, replenishmentConfig.getSkuCode(), "配送规格为空");
                    continue;
                }
                if (Objects.isNull(orderGoodsOut.getDistributionUnitPrice())) {
                    log.info("门店{},商品{}---{}", storeCode, replenishmentConfig.getSkuCode(), "配销价为空");
                    this.addErrorMessage(errorJoiner, replenishmentConfig.getSkuCode(), "配销价为空");
                    continue;
                }
            }
            // 查找订单类型配置
            List<String> combinationTypeCodeList = Lists.newArrayList(orderGoodsOut.getStockCode(), DistributionWaysEnum.getTypeByName(orderGoodsOut.getDistributionWay()), orderGoodsOut.getGoodsType());
            OrderTypeConfig orderTypeConfig = orderConfigHandle.getOneByCombinationTypeValueList(storeInfo.getErpStoreCode(), bizOrgCode, combinationTypeCodeList);
            if (Objects.isNull(orderTypeConfig)) {
                log.info("上下限跑货-查询订单类型配置入参：仓位{}，配送方式{}，品类属性{}，组织{}不存在的订单类型配置", orderGoodsOut.getStockCode(), orderGoodsOut.getDistributionWay(), orderGoodsOut.getGoodsType(), bizOrgCode);
                throw new BusinessException("上下限跑货-不存在的订单类型配置");
            }
//            OrderCycleDeliveryOut orderCycleDeliveryOut = shoppingCartService.getStoreDeliveryType(storeLogisticsOut, orderTypeConfig.getOrderPeriod());
            StoreDelivery storeDelivery = deliveryTypeMap.get(orderTypeConfig.getOrderPeriod());
            if (Objects.isNull(storeDelivery)) {
                throw new BusinessException("门店" + storeInfo.getErpStoreCode() + "配送信息" + orderTypeConfig.getOrderPeriod() + "为空");
            }
            OrderCycleDeliveryOut orderCycleDeliveryOut = shoppingCartService.getStoreDeliveryInfoType(storeDelivery);
            if (Objects.isNull(orderCycleDeliveryOut)) {
                throw new BusinessException("门店" + storeInfo.getErpStoreCode() + "要货周期为空");
            }
            String distributionCycle = orderCycleDeliveryOut.getDistributionCycle();
            String deliveryDailyCycle = orderCycleDeliveryOut.getDeliveryDailyCycle();
            boolean delivery;
            if (SystemConstant.DELIVERY_BY_DAY.equals(distributionCycle)) {
                if (StringUtils.isBlank(deliveryDailyCycle)) {
                    throw new BusinessException("购物车-门店按周内星期勾选配送周期为空");
                }
                List<Integer> deliveryWeekList = Arrays.stream(deliveryDailyCycle.split(SystemConstant.COMMA)).map(Integer::parseInt).sorted().collect(Collectors.toList());
                delivery = StoreDeliveryDateUtil.checkStoreDeliveryByDeliveryWeekList(deliveryWeekList);
            } else {
                delivery = StoreDeliveryDateUtil.checkStoreDelivery(distributionCycle);
            }
            log.info("门店：{}配送周期：{}", storeCode, delivery);
            if (!delivery) {
                continue;
            }
            // 获取门店下商品库存
            InvStockStoreOut invStockStoreOut = invMap.get(orderGoodsOut.getGoodsCode());
            BigDecimal inventory = invStockStoreOut == null ? null : invStockStoreOut.getBusinessQty();

            log.info("C-门店{},商品{},库存{}", storeCode, replenishmentConfig.getSkuCode(), inventory);

            if (inventory == null) {
                log.info("门店" + storeCode + "中goodsCode：" + replenishmentConfig.getSkuCode() + "查询库存记录为空, 记为0");
                inventory = BigDecimal.ZERO;
//                log.info("门店" + storeCode + "中goodsCode：" + replenishmentConfig.getSkuCode() + "查询库存记录为空, 记为当前商品不跑货");
//                continue;
            }
            if (replenishmentConfig.getInventoryLowerLimit() == null || replenishmentConfig.getInventoryUpperLimit() == null) {
                continue;
            }
            //在单量
            Map<String, BigDecimal> goodsInTransitQuantityMap = null;
            String beginTime = LocalDate.now().minusDays(5) + " 00:00:00";
            String endTime = LocalDate.now().minusDays(1) + " 23:59:59";
            List<InOneQtyVO> inOneQtyVOList = ordDisDeliveryService.findInDeliveryOrder(storeCode, beginTime, endTime);
            if (CollectionUtils.isNotEmpty(inOneQtyVOList)) {
                goodsInTransitQuantityMap = inOneQtyVOList.stream().collect(Collectors.toMap(InOneQtyVO::getGoodsCode, inOneQtyVO -> Objects.isNull(inOneQtyVO.getQty()) ? BigDecimal.ZERO : inOneQtyVO.getQty()));
            }
            OrderCartOut orderCartOut = new OrderCartOut();
            //库存小于下限
            if (inventory.compareTo(replenishmentConfig.getInventoryLowerLimit()) < 1) {
                // 补货量(件)
                BigDecimal replenishmentQuantity;
                if (inventory.compareTo(BigDecimal.ZERO) < 0) {
                    replenishmentQuantity = replenishmentConfig.getInventoryUpperLimit();
                } else {
                    replenishmentQuantity = replenishmentConfig.getInventoryUpperLimit().subtract(inventory);
                }
                if (null == orderGoodsOut.getDistributionSpecification()) {
                    log.info("D-门店{},商品{}", storeCode, replenishmentConfig.getSkuCode());
                    continue;
                }
                // 获取门店商品在单量
                BigDecimal inTransitQuantity = BigDecimal.ZERO;
                if (null != goodsInTransitQuantityMap && null != goodsInTransitQuantityMap.get(replenishmentConfig.getSkuCode())) {
//                    inTransitQuantity = goodsInTransitQuantityMap.get(replenishmentConfig.getSkuCode()).divide(BigDecimal.valueOf(orderGoodsOut.getDistributionSpecification().getQpc()), 0, RoundingMode.DOWN);
                    inTransitQuantity = goodsInTransitQuantityMap.get(replenishmentConfig.getSkuCode());
                    log.info("门店{}跑货商品{}，{}至{}在单量为：{}", storeCode, replenishmentConfig.getSkuCode(), beginTime, endTime, inTransitQuantity);
                }
                // 订货数量(四舍五入) = 补货量 / 配货规格
                BigDecimal quantity = replenishmentQuantity.divide((BigDecimal.valueOf(orderGoodsOut.getDistributionSpecification().getQpc())), 0, RoundingMode.HALF_UP);
                quantity = quantity.subtract(inTransitQuantity);
                log.info("E-门店{},商品{},补货数{}", storeCode, replenishmentConfig.getSkuCode(), quantity);
                if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    // 订货数量小于等于0
                    continue;
                }
                orderCartOut.setGoodsCode(replenishmentConfig.getSkuCode());
                orderCartOut.setQuantity(quantity);
                orderCartOutList.add(orderCartOut);
            }
            // 门店下购物车集合
            if (orderCartOut.getGoodsCode() != null) {
                storeOrderCartMap.put(storeId, orderCartOutList);
            }
        }
        if (storeOrderCartMap.size() < 1) {
            return true;
        }
        storeOrderCartMap.forEach((storeInfoId, orderCartOuts) -> {
            List<CreateOrderSkuIn> createOrderSkuInList = Lists.newArrayList();
            orderCartOuts.forEach(orderCartOut -> {
                CreateOrderSkuIn createOrderSkuIn = new CreateOrderSkuIn();
                createOrderSkuIn.setPackageQuantity(orderCartOut.getQuantity());
                createOrderSkuIn.setGoodsCode(orderCartOut.getGoodsCode());
                createOrderSkuInList.add(createOrderSkuIn);
            });
            if (CollectionUtils.isEmpty(createOrderSkuInList)) {
                log.info("门店{}无跑货商品", storeCode);
                return;
            }
            orderHandle.createUpAndDownOrder(storeCode, SystemConstant.SYSTEM_USER, bizOrgCode, createOrderSkuInList, BusinessTypeColumnEnum.UP_LOW_DOWN.getType());
            // 钉钉消息通知过滤的商品
            if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
                String msg = MessageFormat.format(UpLowerLimitListWarningTypeEnum.UP_LOWER_LIMIT_FILTER_GOODS_NOTICE.getErrorMessage(),
                        StoreOrderWarningTypeEnum.FRANCHISE.getName(), storeCode, errorJoiner.toString());
                warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
                        UpLowerLimitListWarningTypeEnum.UP_LOWER_LIMIT_FILTER_GOODS_NOTICE.getType(),
                        msg, null, null, bizOrgCode);
            }
        });
        return false;
    }

    private void addErrorMessage(StringJoiner errorJoiner, String goodsCode, String errorMessage) {
        errorJoiner.add("商品" + goodsCode + errorMessage);
    }
}