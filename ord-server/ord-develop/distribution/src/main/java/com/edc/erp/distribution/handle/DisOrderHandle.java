package com.edc.erp.distribution.handle;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.enumeration.warning.UpLowerLimitListWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.WarningBusinessTypeEnum;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.fund.ForeignAccountFundIn;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.fund.ForeignAccountFundOut;
import com.edc.erp.common.model.out.store.StoreUnit;
import com.edc.erp.common.model.out.warning.WarningResultOut;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.WarningService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disordercart.model.in.DeleteOrderCartIn;
import com.edc.erp.disordercart.service.DisShoppingCartService;
import com.edc.erp.distribution.entity.*;
import com.edc.erp.distribution.model.in.CreateOrderInfoIn;
import com.edc.erp.distribution.model.in.CreateOrderSkuIn;
import com.edc.erp.distribution.model.in.CreatePresaleOrderGoodsIn;
import com.edc.erp.distribution.model.in.DataForCreateDisOrderIn;
import com.edc.erp.distribution.model.out.*;
import com.edc.erp.distribution.service.DisOrderPayService;
import com.edc.erp.distribution.service.OrdDisOrderDistributionRelService;
import com.edc.erp.distribution.service.OrdDisOrderDistributionResultService;
import com.edc.erp.distribution.service.OrderCreateService;
import com.edc.erp.distribution.service.impl.OrdDisOrderServiceImpl;
import com.edc.erp.enumeration.SourceTypeEnum;
import com.edc.erp.enumeration.*;
import com.edc.erp.handle.DisOrderConfigHandle;
import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.enumeration.OrdDisPresaleFlowBusinessTypeEnum;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsGoodsIn;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsIn;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
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
 * @description: 配销订货单统一处理器
 * @since 2022/10/17 17:06
 */
@Service
@Slf4j
public class DisOrderHandle extends OrdDisOrderServiceImpl {

    @Autowired
    private DisOrderDetailHandle orderDetailHandle;

    @Autowired
    private DisShoppingCartService shoppingCartService;

    @Autowired
    private OrderCreateService orderCreateService;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private DisOrderConfigHandle orderConfigHandle;

    @Autowired
    private FundServer fundServer;
    @Autowired
    private WarningService warningService;

    @Autowired
    private OrdDisOrderDistributionRelService ordDisOrderDistributionRelService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrdDisOrderDistributionResultService ordDisOrderDistributionResultService;

    @Autowired
    private OrdDisPresaleAssetsService ordDisPresaleAssetsService;

    @Autowired
    private OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;

    @Autowired
    private DisOrderPayService disOrderPayService;


//    /**
//     * 手工订货生成订货单
//     *
//     * @param storeCode
//     * @param loginUsername
//     * @param bizOrgCode
//     * @param createOrderSkuInList
//     * @return
//     */
//    public Response<AfterOrderCreatedMqOut> createManualOrderOld(String storeCode, String loginUsername, String bizOrgCode, List<CreateOrderSkuIn> createOrderSkuInList) {
//        List<DeleteOrderCartIn> deleteOrderCartInList = Lists.newArrayList();
//        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, SourceTypeEnum.INITIATIVE.getKey(), storeCode, bizOrgCode);
//        if (Objects.isNull(createOrderInfoIn)) {
//            log.info("没有可手工下单的商品");
//            return Response.success();
//        }
//        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.createOrderInfo(createOrderInfoIn);
//        createOrderSkuInList.forEach(createOrderSkuIn -> deleteOrderCartInList.add(new DeleteOrderCartIn(createOrderSkuIn.getGoodsCode(), NumberUtils.INTEGER_ZERO)));
//        if (CollectionUtils.isNotEmpty(afterOrderCreatedMqOut.getOrderIdMessageOutList())) {
//            // 移除购物车已定量
//            this.removeExistsSkuQuantityForCreateOrder(storeCode, afterOrderCreatedMqOut.getOrderIdMessageOutList());
//            //移除购物车
//            shoppingCartService.deleteOrderCart(deleteOrderCartInList, storeCode, bizOrgCode);
//        }
//        return Response.data(afterOrderCreatedMqOut, "手工叫货成功");
//    }

    public Response<AfterOrderCreatedMqOut> createManualOrder(String storeCode, String loginUsername, String bizOrgCode,
                                                              List<CreateOrderSkuIn> createOrderSkuInList, String orderIdentification) {
        List<DeleteOrderCartIn> deleteOrderCartInList = Lists.newArrayList();
        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, SourceTypeEnum.INITIATIVE.getKey(), storeCode, bizOrgCode);
        if (Objects.isNull(createOrderInfoIn)) {
            log.info("没有可手工下单的商品");
            return Response.success();
        }
        List<DataForCreateDisOrderIn> dataForCreateDisOrderInList = orderCreateService.handleBeforeSubmitCreateOrder(createOrderInfoIn);
//        String orderIdentification;
//        if (DistributionIdentificationEnum.NORMAL_DISTRIBUTION.getCode().equals(distributionIdentification)) {
//            orderIdentification = OrderIdentificationEnum.NORMAL_ORDER.getCode();
//        } else {
//            orderIdentification = OrderIdentificationEnum.PRESALE_ORDER.getCode();
//        }
        dataForCreateDisOrderInList.forEach(dataForCreateDisOrderIn -> dataForCreateDisOrderIn.getOrdDisOrder().setOrderIdentification(orderIdentification));
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.handleSaveOrder(dataForCreateDisOrderInList);
        createOrderSkuInList.forEach(createOrderSkuIn -> deleteOrderCartInList.add(new DeleteOrderCartIn(createOrderSkuIn.getGoodsCode(), NumberUtils.INTEGER_ZERO)));
        if (CollectionUtils.isNotEmpty(afterOrderCreatedMqOut.getOrderIdMessageOutList())) {
            // 移除购物车已定量
            dataForCreateDisOrderInList.forEach(dataForCreateDisOrderIn -> this.removeGoodsForExistsQuantity(dataForCreateDisOrderIn.getOrdDisOrder(),
                    dataForCreateDisOrderIn.getOrdDisOrderCycle(), dataForCreateDisOrderIn.getOrdDisOrderDetailList()));
            //移除购物车
            shoppingCartService.deleteOrderCart(deleteOrderCartInList, storeCode, bizOrgCode);
        }
        return Response.data(afterOrderCreatedMqOut, "手工叫货成功");
    }

//    /**
//     * 上下限跑货生成订货单
//     *
//     * @param storeCode
//     * @param loginUsername
//     * @param bizOrgCode
//     * @param createOrderSkuInList
//     * @return
//     */
//    public Response<AfterOrderCreatedMqOut> createUpAndDownOrderOld(String storeCode, String loginUsername, String bizOrgCode, List<CreateOrderSkuIn> createOrderSkuInList) {
//        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, BusinessTypeColumnEnum.UP_LOW_DOWN.getType(), storeCode, bizOrgCode);
//        if (Objects.isNull(createOrderInfoIn)) {
//            log.info("没有可跑货下单的商品");
//            return Response.success();
//        }
//        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.createOrderInfo(createOrderInfoIn);
//        // 跑货预警
//        afterOrderCreatedMqOut.getOrderIdMessageOutList().forEach(orderIdMessageOut -> {
//            OrdDisOrder ordDisOrder = this.selectByPrimaryKey(orderIdMessageOut.getOrderId());
//            OrderTypeConfig orderTypeConfig = orderConfigHandle.getOrderTypeConfigByIdAndBizOrgCode(orderIdMessageOut.getOrderTypeConfigId(), bizOrgCode);
//            // 常温仓计算
//            if (SystemConstant.ROOM_DISTRIBUTION_CYCLE.equals(orderTypeConfig.getOrderPeriod())) {
//                StoreUnit storeUnit = storeCenterService.getStoreUnitByStoreCode(ordDisOrder.getStoreCode(), bizOrgCode);
//                String unitStr = "";
//                if (storeUnit != null) {
//                    unitStr = "【" + storeUnit.getUnitCode() + "】" + storeUnit.getUnitName() + SystemConstant.SHORT_LINE;
//                }
//                WarningResultOut warningResultOut = this.checkOrderAmount(ordDisOrder, orderIdMessageOut.getOrderTypeConfigId(), unitStr, UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getErrorMessage());
//                if (Boolean.TRUE.equals(warningResultOut.getCheckFlag())) {
////                    warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(), UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getType(),
////                            warningResultOut.getErrorMessage(), null, unitCode, bizOrgCode);
//                    warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
//                            UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getType(),
//                            warningResultOut.getErrorMessage(), null, null, bizOrgCode);
//                }
//                WarningResultOut skuSimilarityWarningResultOut = this.checkOrderGoodsSimilarity(ordDisOrder, unitStr, UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getErrorMessage());
//                if (Boolean.TRUE.equals(skuSimilarityWarningResultOut.getCheckFlag())) {
////                    warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(), UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getType(),
////                            skuSimilarityWarningResultOut.getErrorMessage(), null, unitCode, bizOrgCode);
//                    warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
//                            UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getType(),
//                            skuSimilarityWarningResultOut.getErrorMessage(), null, null, bizOrgCode);
//                }
//            }
//        });
//        // 移除购物车已定量
//        this.removeExistsSkuQuantityForCreateOrder(storeCode, afterOrderCreatedMqOut.getOrderIdMessageOutList());
//        return Response.data(afterOrderCreatedMqOut, "系统补货成功");
//    }

    public Response<AfterOrderCreatedMqOut> createUpAndDownOrder(String storeCode, String loginUsername, String bizOrgCode, List<CreateOrderSkuIn> createOrderSkuInList, String sourceCode) {
        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, BusinessTypeColumnEnum.UP_LOW_DOWN.getType(), storeCode, bizOrgCode);
        if (Objects.isNull(createOrderInfoIn)) {
            log.info("没有可跑货下单的商品");
            return Response.success();
        }
        List<DataForCreateDisOrderIn> dataForCreateDisOrderInList = orderCreateService.handleBeforeSubmitCreateOrder(createOrderInfoIn);
        dataForCreateDisOrderInList.forEach(dataForCreateDisOrderIn -> dataForCreateDisOrderIn.getOrdDisOrder().setOrderIdentification(OrderIdentificationEnum.NORMAL_ORDER.getCode()));
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.handleSaveOrder(dataForCreateDisOrderInList);
        if (BusinessTypeColumnEnum.INTELLIGENT_UP_LOW_DOWN.getType().equals(sourceCode)) {
            List<Long> orderIdList = afterOrderCreatedMqOut.getOrderIdMessageOutList().stream().map(OrderIdMessageOut::getOrderId).collect(Collectors.toList());
            StringJoiner idJoiner = new StringJoiner(SystemConstant.COMMA);
            orderIdList.forEach(id -> idJoiner.add(id.toString()));
            orderCreateService.updateOrderSourceCode(orderIdList, BusinessTypeColumnEnum.INTELLIGENT_UP_LOW_DOWN.getType());

            String key = SystemConstant.DSS_STORE_ORDER_CREATED + bizOrgCode + SystemConstant.COLON + DateUtils.format(LocalDate.now()) + SystemConstant.COLON + storeCode;
            redisService.set(key, idJoiner.toString(), NumberUtil.INTEGER_EIGHT, TimeUnit.HOURS);
        }
        // 跑货预警
        dataForCreateDisOrderInList.forEach(dataForCreateDisOrderIn -> {
            OrdDisOrder ordDisOrder = dataForCreateDisOrderIn.getOrdDisOrder();
            Integer orderTypeConfigId = dataForCreateDisOrderIn.getOrdDisOrderCycle().getOrderTypeConfigId();
            OrderTypeConfig orderTypeConfig = orderConfigHandle.getOrderTypeConfigByIdAndBizOrgCode(orderTypeConfigId, bizOrgCode);
            // 常温仓计算
            if (SystemConstant.ROOM_DISTRIBUTION_CYCLE.equals(orderTypeConfig.getOrderPeriod())) {
                StoreUnit storeUnit = storeCenterService.getStoreUnitByStoreCode(ordDisOrder.getStoreCode(), bizOrgCode);
                String unitStr = "";
                if (storeUnit != null) {
                    unitStr = "【" + storeUnit.getUnitCode() + "】" + storeUnit.getUnitName() + SystemConstant.SHORT_LINE;
                }
                WarningResultOut warningResultOut = this.checkOrderAmount(ordDisOrder, orderTypeConfigId, unitStr,
                        UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getErrorMessage());
                if (Boolean.TRUE.equals(warningResultOut.getCheckFlag())) {
//                    warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(), UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getType(),
//                            warningResultOut.getErrorMessage(), null, unitCode, bizOrgCode);
                    warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
                            UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_AMOUNT.getType(),
                            warningResultOut.getErrorMessage(), null, null, bizOrgCode);
                }
                // 2023-09-14移除相似度预警
//                WarningResultOut skuSimilarityWarningResultOut = this.checkOrderGoodsSimilarity(ordDisOrder, unitStr, UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getErrorMessage());
//                if (Boolean.TRUE.equals(skuSimilarityWarningResultOut.getCheckFlag())) {
////                    warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(), UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getType(),
////                            skuSimilarityWarningResultOut.getErrorMessage(), null, unitCode, bizOrgCode);
//                    warningService.pushWarningMessage(WarningBusinessTypeEnum.UP_LOWER_LIMIT.getBusinessType(),
//                            UpLowerLimitListWarningTypeEnum.NEW_UP_LOWER_LIMIT_SKU_SIMILARITY.getType(),
//                            skuSimilarityWarningResultOut.getErrorMessage(), null, null, bizOrgCode);
//                }
            }
            // 移除购物车已定量
            this.removeGoodsForExistsQuantity(ordDisOrder, dataForCreateDisOrderIn.getOrdDisOrderCycle(), dataForCreateDisOrderIn.getOrdDisOrderDetailList());
        });
        return Response.data(afterOrderCreatedMqOut, "系统补货成功");
    }

    /**
     * 校验订货清单金额
     *
     * @param targetOrder
     * @param orderTypeConfigId
     * @param unitStr
     * @param msgTemplate
     * @return
     */
    private WarningResultOut checkOrderAmount(OrdDisOrder targetOrder, Integer orderTypeConfigId, String unitStr, String msgTemplate) {
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
                log.info("门店{}跑货订货单单号：{}即将发送金额过大预警", targetOrder.getStoreCode(), targetOrder.getOrderNo());
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
            OrdDisOrder order = new OrdDisOrder();
            order.setId(orderIdMessageOut.getOrderId());
            order.setOrderCycleId(orderIdMessageOut.getOrderCycleId());
            order.setBizOrgCode(orderIdMessageOut.getBizOrgCode());
            order.setStoreCode(storeCode);
            orderDetailHandle.removeSkuForExistsQuantity(order);
        });
    }

    public void removeGoodsForExistsQuantity(OrdDisOrder order, OrdDisOrderCycle orderCycle, List<OrdDisOrderDetail> orderDetailList) {
        LocalDateTime truncationTime = orderCycle.getTruncationDateTime();
        String redisTruncationTimeStr = DateUtils.format(truncationTime, "yyyy-MM-dd/HH-mm-ss");
        orderDetailList.forEach(orderDetail -> {
            String key = "storeTruncationDateTimeSku:" + order.getBizOrgCode() + ":" + order.getStoreCode() + redisTruncationTimeStr + ":" + orderDetail.getGoodsCode();
            redisService.del(key);
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
        List<OrderCartOut> orderCartOutList = createOrderSkuInList.stream().map(createOrderSkuIn -> {
            OrderCartOut orderCartOut = new OrderCartOut();
            orderCartOut.setGoodsCode(createOrderSkuIn.getGoodsCode());
            orderCartOut.setQuantity(createOrderSkuIn.getPackageQuantity());
            orderCartOut.setOptionalGiftCodeList(createOrderSkuIn.getOptionalGiftCodeList());
            return orderCartOut;
        }).collect(Collectors.toList());
        AppUserOut storeAppUserOut = new AppUserOut();
        storeAppUserOut.setStoreId(storeInfo.getStoreId());
        storeAppUserOut.setStoreCode(storeCode);
        storeAppUserOut.setBizOrgCode(storeInfo.getBizOrgCode());
        storeAppUserOut.setOrgCode(storeInfo.getOrgCode());
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = shoppingCartService.initOrderCycleHeaderOutMap(storeAppUserOut, orderCartOutList, sourceCode, NumberUtil.INTEGER_FOUR);
        if (CollectionUtils.isEmpty(orderCycleHeaderOutList)) {
            return null;
        }
        CreateOrderInfoIn createOrderInfoIn = new CreateOrderInfoIn();
        createOrderInfoIn.setOrderCycleHeaderOutList(orderCycleHeaderOutList);
        createOrderInfoIn.setLoginUsername(loginUsername);
        createOrderInfoIn.setStoreCode(storeCode);
        createOrderInfoIn.setSourceCode(sourceCode);
        createOrderInfoIn.setOrgCode(storeInfo.getOrgCode());
        createOrderInfoIn.setBizOrgCode(storeInfo.getBizOrgCode());
        return createOrderInfoIn;
    }

    /**
     * 修改集货单
     *
     * @param orderList
     * @param logPattern
     * @param updater
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeRequestOrder(List<OrdDisOrder> orderList, String logPattern, String updater, String remark, Map<String, BigDecimal> illegalSkuAmountMap) {
        for (OrdDisOrder order : orderList) {
            OrdDisOrder updateOrder = new DisOrderOut();
            updateOrder.setId(order.getId());
            updateOrder.setOrderStatusCode(OrderStatusEnum.TO_REQUEST_ORDER.getKey());
            updateOrder.setUpdater(updater);
            updateOrder.setUpdateTime(LocalDateTime.now());
            this.update(updateOrder);
            // 订单流冻结金额注释
//            boolean havePayProcess = orderConfigHandle.isHavePayProcess(order.getOrderCycleId(), order.getStoreCode(), order.getBizOrgCode());
//            BigDecimal amount = BigDecimal.ZERO;
//            if (Objects.isNull(illegalSkuAmountMap)) {
//                amount = order.getOrderAmount();
//            }
//            if (Objects.nonNull(illegalSkuAmountMap) && Objects.nonNull(illegalSkuAmountMap.get(order.getOrderNo()))) {
//                amount = illegalSkuAmountMap.get(order.getOrderNo());
//            }
//            if (havePayProcess && BigDecimal.ZERO.compareTo(amount) != 0) {
//                handleReturnFund(order, remark, amount);
//            }
            String content = MessageFormat.format(logPattern, order.getOrderNo(),
                    OrderStatusEnum.getValueByKey(order.getOrderStatusCode()), OrderStatusEnum.getValueByKey(OrderStatusEnum.TO_REQUEST_ORDER.getKey()));
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
                    OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), updater);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
    }

    private void handleReturnFund(OrdDisOrder order, String remark, BigDecimal amount) {
        // 返款
        RechargeLiquidationIn liquidationIn = new RechargeLiquidationIn();
        liquidationIn.setRecipientPrincipalCode(order.getStoreCode());
        liquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
        liquidationIn.setPayOrPrincipalCode(order.getBizOrgCode());
        liquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
        liquidationIn.setBizOrgCode(order.getBizOrgCode());
        liquidationIn.setBusinessNo(order.getOrderNo());
        liquidationIn.setOriginalBusinessNo(order.getOrderNo());
        liquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
        liquidationIn.setBusinessType(FundTypeEnum.DIS_ORDER_RETURN.getCode());
        liquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        liquidationIn.setLiquidationAmount(amount);
        liquidationIn.setRemark(remark);
        Response settlementResponse = fundServer.settlement(liquidationIn);
        if (!settlementResponse.isSuccess()) {
            log.error("门店{}订货单{}返款失败{}", order.getStoreCode(), order.getOrderNo(), settlementResponse.getMessage());
            throw new BusinessException("门店" + order.getStoreCode() + "订货单" + order.getOrderNo() + "返款失败" + settlementResponse.getMessage());
        }
    }

    /**
     * 更新订货单为待付款
     *
     * @param order
     * @param updater
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusBySubmit(OrdDisOrder order, String updater) {
        String beforeStatusCode = order.getOrderStatusCode();
        this.updateOrderStatus(order.getId(), order.getBizOrgCode(), OrderStatusEnum.WAIT_PAYMENT.getKey(), SystemConstant.SYSTEM_USER, null);
        String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), order.getOrderNo(),
                OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.WAIT_PAYMENT.getKey()));
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), updater);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 作废订单
     *
     * @param ordDisOrder
     * @param username
     * @param bizOrgCode
     */
    @Transactional(rollbackFor = Exception.class)
    public void invalidOrder(OrdDisOrder ordDisOrder, String username, String bizOrgCode) {
        //通知资管中心回退金额
        String beforeStatusCode = ordDisOrder.getOrderStatusCode();
        String orderIdentification = ordDisOrder.getOrderIdentification();
        if (OrderStatusEnum.PAID.getKey().equals(beforeStatusCode)
                && BigDecimal.ZERO.compareTo(ordDisOrder.getOrderAmount()) != 0
                && OrderIdentificationEnum.NORMAL_ORDER.getCode().equals(orderIdentification)) {
//            RechargeLiquidationIn liquidationIn = new RechargeLiquidationIn();
//            liquidationIn.setRecipientPrincipalCode(ordDisOrder.getStoreCode());
//            liquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
//            liquidationIn.setPayOrPrincipalCode(ordDisOrder.getBizOrgCode());
//            liquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
//            liquidationIn.setBizOrgCode(ordDisOrder.getBizOrgCode());
//            liquidationIn.setBusinessNo(ordDisOrder.getOrderNo());
//            liquidationIn.setBusinessType(FundTypeEnum.DIS_ORDER_RETURN.getCode());
//            liquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
//            liquidationIn.setLiquidationAmount(ordDisOrder.getOrderAmount().abs());
//            liquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
//            liquidationIn.setOriginalBusinessNo(ordDisOrder.getOrderNo());
//            liquidationIn.setRemark(FundReturnTypeEnum.DIS_ORDER_INVALID.getName());
//            fundServer.settlement(liquidationIn);
            UnFrozenIn unFrozenIn = new UnFrozenIn();
            unFrozenIn.setUnFrozenBusinessNos(Collections.singletonList(ordDisOrder.getOrderNo()));
            Response response = fundServer.unFrozen(unFrozenIn);
            if (null != response && !response.isSuccess()) {
                log.error("作废订货单{}调用资管解冻异常{}", ordDisOrder.getOrderNo(), response.getMessage());
                throw new BusinessException("作废订货单" + ordDisOrder.getOrderNo() + "调用资管解冻异常" + response.getMessage());
            }
        }
        String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), ordDisOrder.getOrderNo(),
                OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.INVALID.getKey()));
        this.updateOrderStatus(ordDisOrder.getId(), ordDisOrder.getBizOrgCode(), OrderStatusEnum.INVALID.getKey(), SystemConstant.SYSTEM_USER, null);
        // 删除存在的sku信息
        orderDetailHandle.removeSkuForExistsQuantity(ordDisOrder);
        // 预售，退资产
        if (OrderIdentificationEnum.PRESALE_ORDER.getCode().equals(orderIdentification)) {
            this.handleReturnBackStorePresaleAssets(ordDisOrder, username);
        }
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), ordDisOrder.getId().toString(),
                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), username);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        String key = SystemConstant.DSS_STORE_ORDER_CREATED + bizOrgCode + SystemConstant.COLON + DateUtils.format(ordDisOrder.getCreateTime().toLocalDate())
                + SystemConstant.COLON + ordDisOrder.getStoreCode();
        redisService.del(key);
    }

    public void handleReturnBackStorePresaleAssets(OrdDisOrder ordDisOrder, String username) {
        UpdateDisPresaleAssetsIn updateDisPresaleAssetsIn = new UpdateDisPresaleAssetsIn();
        updateDisPresaleAssetsIn.setBizOrgCode(ordDisOrder.getBizOrgCode());
        updateDisPresaleAssetsIn.setLoginUsername(username);
        String businessType = null;
        if (SourceTypeEnum.DISTRIBUTION.getKey().equals(ordDisOrder.getSourceCode())) {
            businessType = OrdDisPresaleFlowBusinessTypeEnum.INVALID_DISTRIBUTION_ORDER.getKey();
        }
        if (SourceTypeEnum.INITIATIVE.getKey().equals(ordDisOrder.getSourceCode())) {
            businessType = OrdDisPresaleFlowBusinessTypeEnum.INVALID_MANUAL_ORDER.getKey();
        }
        updateDisPresaleAssetsIn.setBusinessType(businessType);
        updateDisPresaleAssetsIn.setSourceNo(ordDisOrder.getOrderNo());
        OrdDisPresaleAssets presaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(ordDisOrder.getStoreCode());
        if (Objects.nonNull(presaleAssets)) {
            updateDisPresaleAssetsIn.setAssetsId(presaleAssets.getId());
        }
        List<OrdDisOrderDetail> orderDetailList = orderDetailHandle.findOrderDetailListByOrderId(ordDisOrder.getId());
        List<UpdateDisPresaleAssetsGoodsIn> assetsGoodsInList = orderDetailList.stream().map(ordDisOrderDetail -> {
            OrdDisPresaleAssetsDetail storeAssetsDetail = ordDisPresaleAssetsDetailService.getStoreAssetsDetail(ordDisOrder.getStoreCode(),
                    ordDisOrderDetail.getGoodsCode(), ordDisOrder.getCreateTime());
            if (Objects.isNull(storeAssetsDetail)) {
                throw new BusinessException("门店资产商品" + ordDisOrderDetail.getGoodsCode() + "不存在");
            }
            UpdateDisPresaleAssetsGoodsIn updateDisPresaleAssetsGoodsIn = new UpdateDisPresaleAssetsGoodsIn();
            updateDisPresaleAssetsGoodsIn.setPresaleActivityId(storeAssetsDetail.getPresaleActivityId());
            updateDisPresaleAssetsGoodsIn.setPresaleActivityNo(storeAssetsDetail.getPresaleActivityNo());
            updateDisPresaleAssetsGoodsIn.setStoreCode(presaleAssets.getStoreCode());
            updateDisPresaleAssetsGoodsIn.setStoreName(presaleAssets.getStoreName());
            updateDisPresaleAssetsGoodsIn.setGoodsCode(ordDisOrderDetail.getGoodsCode());
            updateDisPresaleAssetsGoodsIn.setUpdateSurplusQuantity(ordDisOrderDetail.getQuantity());
            updateDisPresaleAssetsGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
            updateDisPresaleAssetsGoodsIn.setOrderQuantity(ordDisOrderDetail.getQuantity().negate());
            updateDisPresaleAssetsGoodsIn.setUpdateSurplusQuantity(updateDisPresaleAssetsGoodsIn.getOrderQuantity().abs());
            updateDisPresaleAssetsGoodsIn.setPackageSpecificationNum(ordDisOrderDetail.getDistributionSpecificationNum());
            updateDisPresaleAssetsGoodsIn.setLoginUsername(username);
            updateDisPresaleAssetsGoodsIn.setAssetsId(storeAssetsDetail.getAssetsId());
            return updateDisPresaleAssetsGoodsIn;
        }).collect(Collectors.toList());
        updateDisPresaleAssetsIn.setAssetsGoodsInList(assetsGoodsInList);
        ordDisPresaleAssetsService.updatePresaleAssets(updateDisPresaleAssetsIn);
    }

//    /**
//     * 分货单创建订货单
//     *
//     * @param storeCode
//     * @param loginUsername
//     * @param distributionOrderId
//     * @param createOrderSkuInList
//     * @param bizOrgCode
//     * @return
//     */
//    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
//    public Response<AfterOrderCreatedMqOut> createDistributionOrderOld(String storeCode, String loginUsername, Long distributionOrderId, List<CreateOrderSkuIn> createOrderSkuInList, String bizOrgCode) {
//        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, BusinessTypeColumnEnum.DISTRIBUTION.getType(), storeCode, bizOrgCode);
//        if (Objects.isNull(createOrderInfoIn)) {
//            log.info("没有可分货下单的商品");
//            return Response.success();
//        }
//        createOrderInfoIn.setDistributionOrderId(distributionOrderId);
//        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.createOrderInfo(createOrderInfoIn);
//        List<OrdDisOrderDistributionRel> distributionJoinOrderList = Lists.newArrayList();
//        for (OrderIdMessageOut orderIdMessageOut : afterOrderCreatedMqOut.getOrderIdMessageOutList()) {
//            OrdDisOrderDistributionRel distributionJoinOrder = new OrdDisOrderDistributionRel();
//            distributionJoinOrder.setOrderId(orderIdMessageOut.getOrderId());
//            distributionJoinOrder.setDistributionOrderId(distributionOrderId);
//            distributionJoinOrder.setCreator(loginUsername);
//            distributionJoinOrder.setCreateTime(LocalDateTime.now());
//            distributionJoinOrderList.add(distributionJoinOrder);
//        }
//
//        if (CollectionUtils.isNotEmpty(distributionJoinOrderList)) {
//            //批量保存配销分货单与配销订货单关联表
//            ordDisOrderDistributionRelService.batchSaveDistributionJoinOrder(distributionJoinOrderList);
//        }
//        // 移除购物车已定量
//        this.removeExistsSkuQuantityForCreateOrder(storeCode, afterOrderCreatedMqOut.getOrderIdMessageOutList());
//        return Response.data(afterOrderCreatedMqOut, "人工分货成功");
//    }


    /**
     * 分货单创建订货单
     *
     * @param storeCode
     * @param loginUsername
     * @param distributionOrderId
     * @param createOrderSkuInList
     * @param bizOrgCode
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Response<AfterOrderCreatedMqOut> createDistributionOrder(String storeCode, String loginUsername, Long distributionOrderId,
                                                                    List<CreateOrderSkuIn> createOrderSkuInList, String bizOrgCode,
                                                                    String distributionIdentification) {
        CreateOrderInfoIn createOrderInfoIn = this.initCreateOrderInfoIn(createOrderSkuInList, loginUsername, BusinessTypeColumnEnum.DISTRIBUTION.getType(), storeCode, bizOrgCode);
        if (Objects.isNull(createOrderInfoIn)) {
            log.info("没有可分货下单的商品");
            return Response.success();
        }
        createOrderInfoIn.setDistributionOrderId(distributionOrderId);
        List<DataForCreateDisOrderIn> dataForCreateDisOrderInList = orderCreateService.handleBeforeSubmitCreateOrder(createOrderInfoIn);
        String orderIdentification;
        if (DistributionIdentificationEnum.NORMAL_DISTRIBUTION.getCode().equals(distributionIdentification)) {
            orderIdentification = OrderIdentificationEnum.NORMAL_ORDER.getCode();
        } else {
            orderIdentification = OrderIdentificationEnum.PRESALE_ORDER.getCode();
        }
        dataForCreateDisOrderInList.forEach(dataForCreateDisOrderIn -> dataForCreateDisOrderIn.getOrdDisOrder().setOrderIdentification(orderIdentification));
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = orderCreateService.handleSaveOrder(dataForCreateDisOrderInList);
        List<OrdDisOrderDistributionRel> distributionJoinOrderList = Lists.newArrayList();
        for (OrderIdMessageOut orderIdMessageOut : afterOrderCreatedMqOut.getOrderIdMessageOutList()) {
            OrdDisOrderDistributionRel distributionJoinOrder = new OrdDisOrderDistributionRel();
            distributionJoinOrder.setOrderId(orderIdMessageOut.getOrderId());
            distributionJoinOrder.setDistributionOrderId(distributionOrderId);
            distributionJoinOrder.setCreator(loginUsername);
            distributionJoinOrder.setCreateTime(LocalDateTime.now());
            distributionJoinOrderList.add(distributionJoinOrder);
        }
        if (CollectionUtils.isNotEmpty(distributionJoinOrderList)) {
            //批量保存配销分货单与配销订货单关联表
            ordDisOrderDistributionRelService.batchSaveDistributionJoinOrder(distributionJoinOrderList);
            OrdDisOrderDistributionResult ordDisOrderDistributionResult = new OrdDisOrderDistributionResult();
            ordDisOrderDistributionResult.setDistributionOrderId(distributionOrderId);
            ordDisOrderDistributionResult.setStoreCode(storeCode);
            ordDisOrderDistributionResult.setIsDone(1);
            ordDisOrderDistributionResult.setUpdater(loginUsername);
            ordDisOrderDistributionResult.setUpdateTime(LocalDateTime.now());
            ordDisOrderDistributionResultService.updateByDistributionOrderIdAndStoreCode(ordDisOrderDistributionResult);
        }
        // 移除购物车已定量
        this.removeExistsSkuQuantityForCreateOrder(storeCode, afterOrderCreatedMqOut.getOrderIdMessageOutList());
        return Response.data(afterOrderCreatedMqOut, "人工分货成功");
    }

    /**
     * 根据订货单id查询订货单明细信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    public OrderDetailInfoOut getOrderDetailInfoOut(Long orderId, String bizOrgCode) {
        OrderDetailInfoOut orderDetailInfoOut = new OrderDetailInfoOut();
        OrdDisOrder ordDisOrder = this.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (Objects.isNull(ordDisOrder)) {
            throw new BusinessException("不存在的订货单");
        }

        BeanUtils.copy(ordDisOrder, orderDetailInfoOut);
        List<OrdDisOrderDetail> orderDetailList = orderDetailHandle.findOrderDetailListByOrderId(orderId);
        orderDetailInfoOut.setOrderDetailList(orderDetailList);
        return orderDetailInfoOut;
    }

    public Response<DisCalculationCheckSubmittedListAmountOut> initCalculationCheckSubmittedListAmount(List<Long> orderIdList, AppUserOut appUserOut) {
        DisCalculationCheckSubmittedListAmountOut calculationCheckSubmittedListAmount = new DisCalculationCheckSubmittedListAmountOut();
        BigDecimal orderAmount = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(orderIdList)) {
            orderAmount = this.sumNeedPayAmount(appUserOut.getStoreCode(), appUserOut.getBizOrgCode(), orderIdList);
        }
        ForeignAccountFundIn foreignAccountFundIn = new ForeignAccountFundIn();
        foreignAccountFundIn.setBizOrgCode(appUserOut.getBizOrgCode());
        foreignAccountFundIn.setPrincipalCode(appUserOut.getStoreCode());
        foreignAccountFundIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
        ForeignAccountFundOut foreignAccountFundOut = fundServer.getAvailableAmount(foreignAccountFundIn);
        if (Objects.isNull(foreignAccountFundOut)) {
            log.error("门店{}查询资管账户失败", appUserOut.getStoreCode());
            return Response.error("门店" + appUserOut.getStoreCode() + "查询资管账户失败");
        }
        BigDecimal cashPaymentAmount = BigDecimal.ZERO;
        if (foreignAccountFundOut.getAvailableAmount().add(foreignAccountFundOut.getCredit()).compareTo(orderAmount) < 0) {
            cashPaymentAmount = orderAmount.subtract(foreignAccountFundOut.getAvailableAmount());
        }
        calculationCheckSubmittedListAmount.setOrderAmount(orderAmount);
        calculationCheckSubmittedListAmount.setCashPaymentAmount(cashPaymentAmount);
        calculationCheckSubmittedListAmount.setAccountBalanceTotalAmount(foreignAccountFundOut.getAvailableAmount().add(foreignAccountFundOut.getCredit()));
        return Response.data(calculationCheckSubmittedListAmount);
    }

    public void copyOrder(Long orderId, AppUserOut appUserOut) {
        List<OrdDisOrderDetail> orderDetailList = orderDetailHandle.findOrderDetailListByOrderId(orderId);
        Map<String, CreateOrderSkuIn> skuMap = new LinkedHashMap<>();
        orderDetailList.forEach(orderDetail -> {
            if (ModelConst.ENABLE.isEnable(orderDetail.getIsGift())) {
                return;
            }
            CreateOrderSkuIn disOperationShoppingCartIn = skuMap.get(orderDetail.getGoodsCode());
            if (Objects.isNull(disOperationShoppingCartIn)) {
                disOperationShoppingCartIn = new CreateOrderSkuIn();
                disOperationShoppingCartIn.setPackageQuantity(orderDetail.getPackageQuantity());
                disOperationShoppingCartIn.setGoodsCode(orderDetail.getGoodsCode());
            } else {
                disOperationShoppingCartIn.setPackageQuantity(disOperationShoppingCartIn.getPackageQuantity().add(orderDetail.getPackageQuantity()));
            }
            skuMap.put(orderDetail.getGoodsCode(), disOperationShoppingCartIn);
        });
        shoppingCartService.updateShoppingCart(new ArrayList<>(skuMap.values()), appUserOut, true);
    }


    /**
     * @Description: 通过预售资产创建订货单
     * @Author: ZhangYao
     * @Date: 2024/9/4 18:28
     * @param createPresaleOrderGoodsInList:
     * @param appUserOut:
     * @return: com.edc.plugins.common.response.Response<java.lang.String>
     **/
    public Response<String> createOrderForPresale(List<CreatePresaleOrderGoodsIn> createPresaleOrderGoodsInList, AppUserOut appUserOut) {
//        AppUserOut appUserOut = appUserService.getAppUserByUserCode(userName);
//        if (Objects.isNull(appUserOut)) {
//            return Response.error(SystemConstant.HTTP_UNAUTHORIZED, "不存在的用户");
//        }
        OrdDisPresaleAssets presaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(appUserOut.getStoreCode());
        if (Objects.isNull(presaleAssets)) {
            throw new BusinessException("资产不存在");
        }
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        List<CreateOrderSkuIn> createOrderSkuInList = Lists.newArrayList();

        createPresaleOrderGoodsInList.forEach(createPresaleOrderGoodsIn -> {
            Response<String> response = ordDisPresaleAssetsDetailService.checkGoodsIsExcessAndInOrderTime(presaleAssets, createPresaleOrderGoodsIn.getGoodsCode(), createPresaleOrderGoodsIn.getBuyPackageQuantity());
            if (!response.isSuccess()) {
                errorJoiner.add(response.getMessage());
                return;
            }
            CreateOrderSkuIn createOrderSkuIn = new CreateOrderSkuIn();
            createOrderSkuIn.setGoodsCode(createPresaleOrderGoodsIn.getGoodsCode());
            createOrderSkuIn.setPackageQuantity(createPresaleOrderGoodsIn.getBuyPackageQuantity());
            createOrderSkuInList.add(createOrderSkuIn);
        });
        if (errorJoiner.length() > 0) {
            return Response.error(errorJoiner.toString());
        }
        Response<AfterOrderCreatedMqOut> manualOrder = this.createManualOrder(appUserOut.getStoreCode(), UserUtil.getUserName(),
                appUserOut.getBizOrgCode(), createOrderSkuInList, OrderIdentificationEnum.PRESALE_ORDER.getCode());
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = manualOrder.getData();
        List<OrderIdMessageOut> orderIdMessageOutList = afterOrderCreatedMqOut.getOrderIdMessageOutList();
        List<Long> orderIdList = orderIdMessageOutList.stream().map(OrderIdMessageOut::getOrderId).collect(Collectors.toList());
        disOrderPayService.disOrderPay(orderIdList, appUserOut);
        return Response.success();
    }
}
