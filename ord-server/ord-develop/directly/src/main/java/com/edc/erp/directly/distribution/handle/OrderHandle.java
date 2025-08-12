package com.edc.erp.directly.distribution.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.enumeration.warning.UpLowerLimitListWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.WarningBusinessTypeEnum;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.fund.ForeignAccountFundIn;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.store.StoreUnit;
import com.edc.erp.common.model.out.warning.WarningResultOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.WarningService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirordercart.model.in.DeleteOrderCartIn;
import com.edc.erp.directly.dirordercart.service.ShoppingCartService;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionRel;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDistributionResult;
import com.edc.erp.directly.distribution.model.in.*;
import com.edc.erp.directly.distribution.model.out.*;
import com.edc.erp.directly.distribution.service.DirOrderCreateService;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionRelService;
import com.edc.erp.directly.distribution.service.OrdDirOrderDistributionResultService;
import com.edc.erp.directly.distribution.service.impl.OrdDirOrderServiceImpl;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.enumeration.OrderLogEnum;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
import com.edc.erp.directly.handle.DirOrderConfigHandle;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.sdk.dictionary.entity.SystemDict;
import com.edc.sdk.dictionary.service.SystemDictService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 直营配货订货单统一处理器
 * @since 2022/10/17 17:06
 */
@Service
@Slf4j
public class OrderHandle extends OrdDirOrderServiceImpl {

    @Autowired
    private OrderDetailHandle orderDetailHandle;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private DirOrderCreateService orderCreateService;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private DirOrderConfigHandle orderConfigHandle;

    @Autowired
    private WarningService warningService;

    @Autowired
    private OrdDirOrderDistributionRelService ordDirOrderDistributionRelService;

    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;

    @Autowired
    private SystemDictService systemDictService;

    @Autowired
    private OrdDirOrderDistributionResultService ordDirOrderDistributionResultService;

    @Autowired
    private RedisService redisService;


    public Response<AfterOrderCreatedMqOut> createManualOrder(String storeCode, String loginUsername, List<CreateOrderSkuIn> createOrderSkuInList, String bizOrgCode) {
        List<DeleteOrderCartIn> deleteOrderCartInList = Lists.newArrayList();
        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, SourceTypeEnum.INITIATIVE.getKey(), storeCode, bizOrgCode);
        if (Objects.isNull(createOrderInfoIn)) {
            log.info("没有可手工下单的商品");
            return Response.success();
        }
        List<DataForCreateDirOrderIn> dataForCreateDirOrderInList = orderCreateService.handleBeforeSubmitCreateOrder(createOrderInfoIn);
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.handleSaveOrder(dataForCreateDirOrderInList);
        createOrderSkuInList.forEach(createOrderSkuIn -> deleteOrderCartInList.add(new DeleteOrderCartIn(createOrderSkuIn.getGoodsCode(), 0)));
        if (CollectionUtils.isNotEmpty(afterOrderCreatedMqOut.getOrderIdMessageOutList())) {
            // 移除购物车已定量
            dataForCreateDirOrderInList.forEach(dataForCreateDirOrderIn -> orderDetailHandle.removeGoodsForExistsQuantity(dataForCreateDirOrderIn.getOrdDirOrder(),
                    dataForCreateDirOrderIn.getOrdDirOrderCycle(), dataForCreateDirOrderIn.getOrdDirOrderDetailList()));
            //移除购物车
            shoppingCartService.deleteOrderCart(deleteOrderCartInList, storeCode, bizOrgCode);
        }
        return Response.data(afterOrderCreatedMqOut, "手工叫货成功");
    }

    public Response<AfterOrderCreatedMqOut> createUpAndDownOrder(String storeCode, String loginUsername, List<CreateOrderSkuIn> createOrderSkuInList, String bizOrgCode, String sourceCode) {
        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, SourceTypeEnum.UPLOWDOWN.getKey(), storeCode, bizOrgCode);
        if (Objects.isNull(createOrderInfoIn)) {
            log.info("没有可跑货下单的商品");
            return Response.success();
        }
        List<DataForCreateDirOrderIn> dataForCreateDirOrderInList = orderCreateService.handleBeforeSubmitCreateOrder(createOrderInfoIn);
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.handleSaveOrder(dataForCreateDirOrderInList);
        if (BusinessTypeColumnEnum.INTELLIGENT_UP_LOW_DOWN.getType().equals(sourceCode) || BusinessTypeColumnEnum.HEAD_OFFICE_REPLENISH.getType().equals(sourceCode)) {
            List<Long> orderIdList = afterOrderCreatedMqOut.getOrderIdMessageOutList().stream().map(OrderIdMessageOut::getOrderId).collect(Collectors.toList());
            StringJoiner idJoiner = new StringJoiner(SystemConstant.COMMA);
            orderIdList.forEach(id -> idJoiner.add(id.toString()));
            orderCreateService.updateOrderSourceCode(orderIdList, sourceCode);
            if (BusinessTypeColumnEnum.INTELLIGENT_UP_LOW_DOWN.getType().equals(sourceCode)) {
                String key = SystemConstant.DSS_STORE_ORDER_CREATED + bizOrgCode + SystemConstant.COLON + DateUtils.format(LocalDate.now()) + SystemConstant.COLON + storeCode;
                redisService.set(key, idJoiner.toString(), NumberUtil.INTEGER_EIGHT, TimeUnit.HOURS);
            }
        }
        // 跑货预警
        dataForCreateDirOrderInList.forEach(dataForCreateDirOrderIn -> {
            OrdDirOrder ordDirOrder = dataForCreateDirOrderIn.getOrdDirOrder();
            Integer orderTypeConfigId = dataForCreateDirOrderIn.getOrdDirOrderCycle().getOrderTypeConfigId();
            DirOrderTypeConfig dirOrderTypeConfig = orderConfigHandle.getOrderTypeConfigByIdAndBizOrgCode(orderTypeConfigId, bizOrgCode);
            // 常温仓计算
            if (SystemConstant.ROOM_DISTRIBUTION_CYCLE.equals(dirOrderTypeConfig.getOrderPeriod())) {
                StoreUnit storeUnit = storeCenterService.getStoreUnitByStoreCode(ordDirOrder.getStoreCode(), bizOrgCode);
                String unitStr = "";
                if (storeUnit != null) {
                    unitStr = "【" + storeUnit.getUnitCode() + "】" + storeUnit.getUnitName() + "-";
                }
                WarningResultOut warningResultOut = this.checkOrderAmount(ordDirOrder, orderTypeConfigId, unitStr, UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getErrorMessage());
                if (warningResultOut.getCheckFlag()) {
//                    warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(), UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getType(),
//                            warningResultOut.getErrorMessage(), null, unitCode, bizOrgCode);
                    warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
                            UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getType(),
                            warningResultOut.getErrorMessage(), null, null, bizOrgCode);

                }
                // 2023-09-14移除相似度预警
//                WarningResultOut skuSimilarityWarningResultOut = this.checkOrderGoodsSimilarity(ordDirOrder, unitStr, UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getErrorMessage());
//                if (skuSimilarityWarningResultOut.getCheckFlag()) {
////                    warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
////                            UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getType(),
////                            skuSimilarityWarningResultOut.getErrorMessage(), null, unitCode, bizOrgCode);
//                    warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
//                            UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getType(),
//                            skuSimilarityWarningResultOut.getErrorMessage(), null, null, bizOrgCode);
//                }
            }
            // 移除购物车已定量
            orderDetailHandle.removeGoodsForExistsQuantity(ordDirOrder, dataForCreateDirOrderIn.getOrdDirOrderCycle(), dataForCreateDirOrderIn.getOrdDirOrderDetailList());
        });
        return Response.data(afterOrderCreatedMqOut, "系统补货成功");
    }

    private WarningResultOut checkOrderAmount(OrdDirOrder targetOrder, Integer orderTypeConfigId, String unitStr, String msgTemplate) {
        boolean flag = false;
        // 对比该门店上下限跑货订单金额超当天前30笔订单（所有订单类型的有效订单（不含已作废状态）平均金额的1倍
        List<BigDecimal> requestOrderTotalAmountList = this.findThirtyDaysRequestOrderTotalAmountByStoreCodeAndOrderTypeConfigId(targetOrder.getStoreCode(), orderTypeConfigId, targetOrder.getBizOrgCode());
        String errorMessage = null;
        if (CollectionUtils.isNotEmpty(requestOrderTotalAmountList)) {
            BigDecimal sumPaidAmount = requestOrderTotalAmountList.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            // 金额对比值
            BigDecimal contrastAmount = sumPaidAmount.divide(new BigDecimal(requestOrderTotalAmountList.size()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN).multiply(new BigDecimal("2"));
            if (targetOrder.getOrderAmount().compareTo(contrastAmount) >= 0) {
                errorMessage = MessageFormat.format(msgTemplate, unitStr + targetOrder.getStoreCode(),
                        targetOrder.getOrderAmount(), requestOrderTotalAmountList.size());
                log.info(errorMessage);
                log.info("门店{}跑货订货单单号：{}即将发送金额过大预警", targetOrder.getOrderNo());
                flag = true;
            }
        }
        WarningResultOut warningResultOut = new WarningResultOut();
        warningResultOut.setCheckFlag(flag);
        warningResultOut.setErrorMessage(errorMessage);
        return warningResultOut;
    }


    /**
     * 移除购物车已定量
     *
     * @param storeCode
     * @param orderIdMessageOutList
     */
    private void removeExistsSkuQuantityForCreateOrder(String storeCode, List<OrderIdMessageOut> orderIdMessageOutList) {
        orderIdMessageOutList.forEach(orderIdMessageOut -> {
            OrdDirOrder order = new OrdDirOrder();
            order.setId(orderIdMessageOut.getOrderId());
            order.setOrderCycleId(orderIdMessageOut.getOrderCycleId());
            order.setBizOrgCode(orderIdMessageOut.getBizOrgCode());
            order.setStoreCode(storeCode);
            orderDetailHandle.removeSkuForExistsQuantity(order);
        });
    }


    /**
     * 初始化创建订单信息入参
     *
     * @param createOrderSkuInList
     * @param loginUsername
     * @param sourceCode
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    public CreateOrderInfoIn initCreateOrderInfoIn(List<CreateOrderSkuIn> createOrderSkuInList, String loginUsername, String sourceCode, String storeCode, String bizOrgCode) {
        StoreInfo storeInfo = storeCenterService.getStoreByCode(storeCode, bizOrgCode);
        if (Objects.isNull(storeInfo)) {
            throw new BusinessException("门店不存在,门店代码:" + storeCode);
        }
        Map<String, List<CreateOrderSkuIn>> goodsMap = createOrderSkuInList.stream().collect(Collectors.groupingBy(CreateOrderSkuIn::getGoodsCode));
        // 查找重复出现的商品代码
        String duplicateGoodsCodsStr = goodsMap.entrySet().stream().filter(entry -> entry.getValue().size() > NumberUtil.INTEGER_ONE).map(Map.Entry::getKey).collect(Collectors.joining(SystemConstant.COMMA));
        if (StringUtils.isNotBlank(duplicateGoodsCodsStr)) {
            throw new BusinessException("门店" + storeCode + "创建订单：商品" + duplicateGoodsCodsStr + "重复，请检查");
        }
        StringJoiner goodsJoiner = new StringJoiner(",");
        List<OrderCartOut> orderCartOutList = createOrderSkuInList.stream().map(createOrderSkuIn -> {
            OrderCartOut orderCartOut = new OrderCartOut();
            orderCartOut.setGoodsCode(createOrderSkuIn.getGoodsCode());
            orderCartOut.setQuantity(createOrderSkuIn.getPackageQuantity());
            goodsJoiner.add(createOrderSkuIn.getGoodsCode());
            return orderCartOut;
        }).collect(Collectors.toList());
        AppUserOut storeAppUserOut = new AppUserOut();
        storeAppUserOut.setStoreId(storeInfo.getStoreId());
        storeAppUserOut.setStoreCode(storeCode);
        storeAppUserOut.setBizOrgCode(storeInfo.getBizOrgCode());
        storeAppUserOut.setOrgCode(storeInfo.getOrgCode());
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = shoppingCartService.initOrderCycleHeaderOutMap(storeAppUserOut, orderCartOutList, sourceCode, true);
        if (CollectionUtils.isEmpty(orderCycleHeaderOutList)) {
            return null;
        }
        CreateOrderInfoIn createOrderInfoIn = new CreateOrderInfoIn();
        createOrderInfoIn.setOrderCycleHeaderOutList(orderCycleHeaderOutList);
        createOrderInfoIn.setLoginUsername(loginUsername);
        createOrderInfoIn.setStoreCode(storeCode);
        createOrderInfoIn.setOrgCode(storeInfo.getOrgCode());
        createOrderInfoIn.setBizOrgCode(storeInfo.getBizOrgCode());
        createOrderInfoIn.setSourceCode(sourceCode);
        return createOrderInfoIn;
    }

    /**
     * 修改要货单
     *
     * @param orderList
     * @param logPattern
     * @param updater
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeRequestOrder(List<OrdDirOrder> orderList, String logPattern, String updater) {
        orderList.stream().forEach(order -> {
            String beforeOrderStatusCode = order.getOrderStatusCode();
            OrdDirOrder updateOrder = new OrdDirOrder();
            updateOrder.setId(order.getId());
            updateOrder.setOrderStatusCode(OrderStatusEnum.TO_REQUEST_ORDER.getKey());
            updateOrder.setUpdater(updater);
            updateOrder.setUpdateTime(LocalDateTime.now());
            this.update(updateOrder);
            String content = MessageFormat.format(logPattern, order.getOrderNo(),
                    OrderStatusEnum.getValueByKey(beforeOrderStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.TO_REQUEST_ORDER.getKey()));
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), String.valueOf(order.getId()),
                    OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), updater);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        });
    }

    /**
     * 更新订货单为已提交
     *
     * @param order
     * @param updater
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusBySubmit(OrdDirOrder order, String updater) {
        String beforeStatusCode = order.getOrderStatusCode();
        this.updateOrderStatus(order.getId(), order.getBizOrgCode(), OrderStatusEnum.SUBMIT.getKey(), SystemConstant.SYSTEM_USER);
        String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), order.getOrderNo(),
                OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.SUBMIT.getKey()));
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), String.valueOf(order.getId()),
                OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), updater);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }


    /**
     * 作废订单
     *
     * @param ordDirOrder
     * @param username
     * @param bizOrgCode
     */
    @Transactional(rollbackFor = Exception.class)
    public void invalidOrder(OrdDirOrder ordDirOrder, String username, String bizOrgCode) {
        String beforeStatusCode = ordDirOrder.getOrderStatusCode();
        String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), ordDirOrder.getOrderNo(),
                OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.INVALID.getKey()));
        this.updateOrderStatus(ordDirOrder.getId(), ordDirOrder.getBizOrgCode(), OrderStatusEnum.INVALID.getKey(), SystemConstant.SYSTEM_USER);
        // 删除存在的sku信息
        orderDetailHandle.removeSkuForExistsQuantity(ordDirOrder);

        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), String.valueOf(ordDirOrder.getId()),
                OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), username);
        String key = SystemConstant.DSS_STORE_ORDER_CREATED + bizOrgCode + SystemConstant.COLON + DateUtils.format(ordDirOrder.getCreateTime().toLocalDate())
                + SystemConstant.COLON + ordDirOrder.getStoreCode();
        redisService.del(key);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Transactional(rollbackFor = Exception.class)
    public Response<AfterOrderCreatedMqOut> createDistributionOrder(String storeCode, String loginUsername, Long distributionOrderId,
                                                                    List<CreateOrderSkuIn> createOrderSkuInList, String bizOrgCode) {
        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, SourceTypeEnum.DISTRIBUTION.getKey(), storeCode, bizOrgCode);
        if (Objects.isNull(createOrderInfoIn)) {
            log.info("没有可分货下单的商品");
            return Response.success();
        }
        createOrderInfoIn.setDistributionOrderId(distributionOrderId);
        List<DataForCreateDirOrderIn> dataForCreateDirOrderInList = orderCreateService.handleBeforeSubmitCreateOrder(createOrderInfoIn);
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.handleSaveOrder(dataForCreateDirOrderInList);
        List<OrdDirOrderDistributionRel> distributionJoinOrderList = Lists.newArrayList();
        afterOrderCreatedMqOut.getOrderIdMessageOutList().forEach(orderIdMessageOut -> {
            OrdDirOrderDistributionRel dirOrderDistributionRel = new OrdDirOrderDistributionRel();
            dirOrderDistributionRel.setOrderId(orderIdMessageOut.getOrderId());
            dirOrderDistributionRel.setDistributionOrderId(distributionOrderId);
            dirOrderDistributionRel.setCreator(loginUsername);
            dirOrderDistributionRel.setCreateTime(LocalDateTime.now());
            distributionJoinOrderList.add(dirOrderDistributionRel);
        });
        if (CollectionUtils.isNotEmpty(distributionJoinOrderList)) {
            //批量保存直营配货分货单与直营配货订货单关联表
            ordDirOrderDistributionRelService.batchSaveDistributionJoinOrder(distributionJoinOrderList);
            OrdDirOrderDistributionResult ordDirOrderDistributionResult = new OrdDirOrderDistributionResult();
            ordDirOrderDistributionResult.setDistributionOrderId(distributionOrderId);
            ordDirOrderDistributionResult.setStoreCode(storeCode);
            ordDirOrderDistributionResult.setIsDone(1);
            ordDirOrderDistributionResult.setUpdater(loginUsername);
            ordDirOrderDistributionResult.setUpdateTime(LocalDateTime.now());
            ordDirOrderDistributionResultService.updateByDistributionOrderIdAndStoreCode(ordDirOrderDistributionResult);
        }
        // 移除购物车已定量
        this.removeExistsSkuQuantityForCreateOrder(storeCode, afterOrderCreatedMqOut.getOrderIdMessageOutList());
        return Response.data(afterOrderCreatedMqOut, "人工分货成功");
    }


    public List<AppDirOrderCycleOut> findAppEditOrderList(AppQueryDirOrderIn appQueryOrderIn) {
        List<DirOrderCycleOrderOut> orderCycleOrderOutList = dirOrderCycleHandle.findOrderCycleOrderListByAppQueryOrderIn(appQueryOrderIn);
        if (CollectionUtils.isEmpty(orderCycleOrderOutList)) {
            return Lists.newArrayList();
        }
        Map<Integer, List<AppDirOrderOut>> orderMap = new LinkedHashMap<>();
        Map<Integer, AppDirOrderCycleOut> cycleOutMap = new LinkedHashMap<>();
        orderCycleOrderOutList.forEach(orderCycleOrderOut -> {
            this.initCycleMap(cycleOutMap, orderCycleOrderOut);
            this.initOrderMap(orderMap, orderCycleOrderOut);
        });
        List<AppDirOrderCycleOut> appOrderCycleOutList = Lists.newArrayList();
        cycleOutMap.entrySet().forEach(entry -> {
            Integer orderCycleId = entry.getKey();
            AppDirOrderCycleOut appOrderCycleOut = entry.getValue();
            List<AppDirOrderOut> appOrderOuts = orderMap.get(orderCycleId);
            BigDecimal totalAmount = this.getTotalAmount(appQueryOrderIn, orderCycleId, appOrderOuts);
            appOrderCycleOut.setTotalAmount(totalAmount);
            this.initAppOrderOutOtherData(appOrderOuts, orderCycleId, appQueryOrderIn.getBizOrgCode());
            appOrderCycleOut.setAppOrderOutList(appOrderOuts);
            appOrderCycleOutList.add(appOrderCycleOut);
        });
        return appOrderCycleOutList;
    }

    /**
     * 封装7日订货单订货周期map
     *
     * @param cycleOutMap
     * @param orderCycleOrderOut
     */
    private void initCycleMap(Map<Integer, AppDirOrderCycleOut> cycleOutMap, DirOrderCycleOrderOut orderCycleOrderOut) {
        AppDirOrderCycleOut appOrderCycleOut = cycleOutMap.get(orderCycleOrderOut.getOrderCycleId());
        if (Objects.isNull(appOrderCycleOut)) {
            appOrderCycleOut = new AppDirOrderCycleOut();
            BeanUtils.copy(orderCycleOrderOut, appOrderCycleOut);
            cycleOutMap.put(orderCycleOrderOut.getOrderCycleId(), appOrderCycleOut);
        }
    }

    /**
     * 封装7日订货单订货单map
     *
     * @param orderMap
     * @param orderCycleOrderOut
     */
    private void initOrderMap(Map<Integer, List<AppDirOrderOut>> orderMap, DirOrderCycleOrderOut orderCycleOrderOut) {
        List<AppDirOrderOut> appOrderOuts = orderMap.get(orderCycleOrderOut.getOrderCycleId());
        if (CollectionUtils.isEmpty(appOrderOuts)) {
            appOrderOuts = Lists.newArrayList();
        }
        AppDirOrderOut appOrderOut = new AppDirOrderOut();
        BeanUtils.copy(orderCycleOrderOut, appOrderOut);
        appOrderOut.setOrderId(orderCycleOrderOut.getOrderId());
        appOrderOuts.add(appOrderOut);
        orderMap.put(orderCycleOrderOut.getOrderCycleId(), appOrderOuts);
    }

    /**
     * 计算7日订单中订货周期的总额
     *
     * @param appQueryOrderIn
     * @param orderCycleId
     * @param appOrderOuts
     * @return
     */
    private BigDecimal getTotalAmount(AppQueryDirOrderIn appQueryOrderIn, Integer orderCycleId, List<AppDirOrderOut> appOrderOuts) {
        BigDecimal totalAmount = appOrderOuts.stream().filter(order -> order.getOrderStatusCode().equals(OrderStatusEnum.SUBMIT.getKey())
                        || order.getOrderStatusCode().equals(OrderStatusEnum.TO_REQUEST_ORDER.getKey()))
                .reduce(BigDecimal.ZERO, (total, o2) -> total.add(o2.getOrderAmount()), BigDecimal::add);
        return totalAmount;
    }

    private void initAppOrderOutOtherData(List<AppDirOrderOut> appOrderOutList, Integer orderCycleId, String bizOrgCode) {
        appOrderOutList.forEach(appOrderOut -> {
            SystemDict orderTypeSystemDict = systemDictService.getSystemDict(appOrderOut.getOrderStatusCode());
            if (null != orderTypeSystemDict) {
                appOrderOut.setOrderStatusCodeStr(orderTypeSystemDict.getDictValueName());
            }
            SystemDict sourceCodeSystemDict = systemDictService.getSystemDict(appOrderOut.getSourceCode());
            if (null != sourceCodeSystemDict) {
                appOrderOut.setSourceCodeStr(sourceCodeSystemDict.getDictValueName());
            }
            // top商品
            List<AppTopDirOrderGoodsOut> appTopOrderGoodsOutList = orderDetailHandle.findTopOrderDetailListByOrderId(appOrderOut.getOrderId(), 3);
            appOrderOut.setAppTopOrderGoodsOutList(appTopOrderGoodsOutList);
            // 是否能编辑
            OrdDirOrder order = new OrdDirOrder();
            order.setId(appOrderOut.getOrderId());
            order.setOrderCycleId(orderCycleId);
            order.setOrderStatusCode(appOrderOut.getOrderStatusCode());
            order.setStoreCode(appOrderOut.getStoreCode());
            order.setSourceCode(appOrderOut.getSourceCode());
            order.setBizOrgCode(bizOrgCode);
            boolean activityMatchFlag = orderConfigHandle.isCanEditOrder(order);
            appOrderOut.setIsCanEdit(activityMatchFlag ? 1 : 0);
            if (appOrderOut.getOrderStatusCode().equals(OrderStatusEnum.TO_REQUEST_ORDER.getKey())
                    || appOrderOut.getOrderStatusCode().equals(OrderStatusEnum.INVALID.getKey())
                    || SourceTypeEnum.DISTRIBUTION.getKey().equals(order.getSourceCode())) {
                appOrderOut.setIsCanInvalid(0);
            } else {
                appOrderOut.setIsCanInvalid(1);
            }
        });
    }


    public AppDirOrderDetailInfoOut getAppOrderDetailInfoOut(Long orderId, String bizOrgCode) {
        AppDirOrderDetailInfoOut appOrderDetailInfoOut = new AppDirOrderDetailInfoOut();
        AppDirOrderHeaderOut appOrderHeader = new AppDirOrderHeaderOut();
        OrdDirOrder orderOut = this.getOrderOutById(orderId, bizOrgCode);
        if (Objects.isNull(orderOut)) {
            throw new BusinessException("不存在的订货单");
        }
        BeanUtils.copy(orderOut, appOrderHeader);
        appOrderDetailInfoOut.setAppOrderHeader(appOrderHeader);
        List<OrdDirOrderDetail> orderDetailList = orderDetailHandle.findOrderDetailListByOrderId(orderId);
        Map<String, List<OrdDirOrderDetail>> giftMap = orderDetailList.stream().filter(dtl -> NumberUtils.INTEGER_ONE.equals(dtl.getIsGift())).collect(Collectors.groupingBy(OrdDirOrderDetail::getBaseGoodsCode));
        List<AppOrderDetailOut> detailOutList = new ArrayList<>();
        for (OrdDirOrderDetail item : orderDetailList) {
            if (NumberUtils.INTEGER_ZERO.equals(item.getIsGift())) {
                AppOrderDetailOut detailOut = new AppOrderDetailOut();
                BeanUtils.copy(item, detailOut);
                if (giftMap.containsKey(item.getGoodsCode())) {
                    detailOut.setGiftOutList(giftMap.get(item.getGoodsCode()));
                }
                detailOutList.add(detailOut);
            }
        }
        appOrderDetailInfoOut.setOrderDetailList(detailOutList);
        return appOrderDetailInfoOut;
    }

    /**
     * 更新订货单商品数量
     *
     * @param updateOrderIn
     * @param loginUsername
     */
    public Response<String> updateOrderSkuPackageQuantity(UpdateDirOrderIn updateOrderIn, String loginUsername) {
        OrdDirOrder order = this.getOrderByIdAndBizOrgCode(updateOrderIn.getOrderId(), updateOrderIn.getBizOrgCode());
        if (Objects.isNull(order)) {
            return Response.error("不存在的订货单");
        }
        boolean activityMatchFlag = orderConfigHandle.isCanEditOrder(order);
        if (activityMatchFlag) {
            orderCreateService.updateDirOrderGoods(updateOrderIn.getUpdateOrderGoodsInList(), order, loginUsername);
            return Response.success();
        } else {
            return Response.error("该订货单不能编辑");
        }
    }

    public Response<DirCalculationCheckSubmittedListAmountOut> initCalculationCheckSubmittedListAmount(List<Long> orderIdList, AppUserOut appUserOut) {
        DirCalculationCheckSubmittedListAmountOut calculationCheckSubmittedListAmount = new DirCalculationCheckSubmittedListAmountOut();
        BigDecimal orderAmount = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(orderIdList)) {
            orderAmount = this.sumOrderAmount(appUserOut.getStoreCode(), appUserOut.getBizOrgCode(), orderIdList);
        }
        ForeignAccountFundIn foreignAccountFundIn = new ForeignAccountFundIn();
        foreignAccountFundIn.setBizOrgCode(appUserOut.getBizOrgCode());
        foreignAccountFundIn.setPrincipalCode(appUserOut.getStoreCode());
        foreignAccountFundIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
        calculationCheckSubmittedListAmount.setOrderAmount(orderAmount);
        calculationCheckSubmittedListAmount.setCashPaymentAmount(BigDecimal.ZERO);
        calculationCheckSubmittedListAmount.setAccountBalanceTotalAmount(BigDecimal.ZERO);
        return Response.data(calculationCheckSubmittedListAmount);
    }

    public void copyOrder(Long orderId, AppUserOut appUserOut) {
        List<OrdDirOrderDetail> orderDetailList = orderDetailHandle.findOrderDetailListByOrderId(orderId);
        Map<String, CreateOrderSkuIn> skuMap = new LinkedHashMap<>();
        orderDetailList.forEach(orderDetail -> {
            if (orderDetail.getIsGift() == 1) {
                return;
            }
            CreateOrderSkuIn operationShoppingCartIn = skuMap.get(orderDetail.getGoodsCode());
            if (Objects.isNull(operationShoppingCartIn)) {
                operationShoppingCartIn = new CreateOrderSkuIn();
                operationShoppingCartIn.setPackageQuantity(orderDetail.getPackageQuantity());
                operationShoppingCartIn.setGoodsCode(orderDetail.getGoodsCode());
            } else {
                operationShoppingCartIn.setPackageQuantity(operationShoppingCartIn.getPackageQuantity().add(orderDetail.getPackageQuantity()));
            }
            skuMap.put(orderDetail.getGoodsCode(), operationShoppingCartIn);
        });
        shoppingCartService.updateShoppingCart(new ArrayList<>(skuMap.values()), appUserOut, true);
    }
}
