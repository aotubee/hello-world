package com.edc.erp.distribution.service;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderProcessCopy;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrderNoPreEnum;
import com.edc.erp.common.model.out.OrderProcessOut;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.model.in.CreateOrderInfoIn;
import com.edc.erp.distribution.model.in.DataForCreateDisOrderIn;
import com.edc.erp.distribution.model.in.UpdateDisOrderGoodsIn;
import com.edc.erp.distribution.model.out.AfterOrderCreatedMqOut;
import com.edc.erp.distribution.model.out.OrderCartGoodsOut;
import com.edc.erp.distribution.model.out.OrderCycleHeaderOut;
import com.edc.erp.distribution.model.out.OrderIdMessageOut;
import com.edc.erp.enumeration.*;
import com.edc.erp.orderscheduing.handle.OrderProcessSchedulingHandle;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fxw
 * @description: 订单创建业务处理层
 * @since 2022/10/17 19:24
 */
@Service
@Slf4j
public class OrderCreateService {

    @Autowired
    private OrderProcessSchedulingHandle orderProcessSchedulingHandle;

    @Qualifier("ordDisOrderServiceImpl")
    @Autowired
    private OrdDisOrderService ordDisOrderService;

    @Qualifier("ordDisOrderDetailServiceImpl")
    @Autowired
    private OrdDisOrderDetailService ordDisOrderDetailService;

    @Autowired
    @Qualifier("ordDisOrderCycleServiceImpl")
    private OrdDisOrderCycleService ordDisOrderCycleService;

    @Autowired
    private UniqueUtils uniqueUtils;

    @Autowired
    private OrdDisOrderTrackService ordDisOrderTrackService;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private DisOrderHandle orderHandle;

    public List<DataForCreateDisOrderIn> handleBeforeSubmitCreateOrder(CreateOrderInfoIn createOrderInfoIn) {
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = createOrderInfoIn.getOrderCycleHeaderOutList();
        String sourceCode = createOrderInfoIn.getSourceCode();
        String loginUsername = createOrderInfoIn.getLoginUsername();
        String storeCode = createOrderInfoIn.getStoreCode();
        String bizOrgCode = createOrderInfoIn.getBizOrgCode();
        String orgCode = createOrderInfoIn.getOrgCode();

//        List<OrderIdMessageOut> orderIdMessageOuts = Lists.newArrayList();
        List<DataForCreateDisOrderIn> dataForCreateDirOrderInList = Lists.newArrayList();
        for (OrderCycleHeaderOut orderCycleHeaderOut : orderCycleHeaderOutList) {
            List<OrderProcessOut> orderProcessOutList = orderProcessSchedulingHandle.findProcessAndConfigAndItemByOrderTypeConfigId(orderCycleHeaderOut.getOrderTypeConfigId(), bizOrgCode);
            if (CollectionUtils.isEmpty(orderProcessOutList)) {
                throw new BusinessException("订货周期" + orderCycleHeaderOut.getShortOrderType() + "的订单流程为空");
            }
            this.checkSku(orderCycleHeaderOut.getGoodsList(), sourceCode);
            this.checkMinAmount(orderCycleHeaderOut, sourceCode);
            OrdDisOrderCycle orderCycle = this.initOrderCycleForCreateOrder(storeCode, loginUsername, bizOrgCode, orgCode, orderCycleHeaderOut);
            DataForCreateDisOrderIn dataForCreateDisOrderIn = new DataForCreateDisOrderIn();
            OrdDisOrder ordDisOrder = this.initOrder(orderCycle, sourceCode, createOrderInfoIn.getLoginUsername());
            dataForCreateDisOrderIn.setOrdDisOrderCycle(orderCycle);
//            ordDisOrderService.save(ordDisOrder);
            List<OrdDisOrderDetail> orderDetailList = Lists.newArrayList();
            orderCycleHeaderOut.getGoodsList().forEach(orderCartGoodsOut -> this.initOrderDetail(orderCartGoodsOut, ordDisOrder, orderDetailList));
            ordDisOrder.setOrderAmount(ordDisOrder.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.CEILING));
            ordDisOrder.setPreferentialAmount(ordDisOrder.getPreferentialAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.CEILING));
            // 整单优惠金额
            BigDecimal discountAmount = BigDecimal.ZERO;
            ordDisOrder.setDiscountAmount(discountAmount);
//            ordDisOrderService.update(ordDisOrder);
            dataForCreateDisOrderIn.setOrdDisOrder(ordDisOrder);
//            ordDisOrderDetailService.batchSaveDisOrderDetail(orderDetailList);
            dataForCreateDisOrderIn.setOrdDisOrderDetailList(orderDetailList);
            // 封装订货单创建成功后向单据调度发送消息入参
//            OrderIdMessageOut orderIdMessageOut = new OrderIdMessageOut();
//            orderIdMessageOut.setOrderCycleId(orderCycle.getId());
//            orderIdMessageOut.setOrderId(ordDisOrder.getId());
//            orderIdMessageOut.setOrderNo(ordDisOrder.getOrderNo());
//            orderIdMessageOut.setBizOrgCode(bizOrgCode);
//            orderIdMessageOut.setOrgCode(orgCode);
//            orderIdMessageOut.setCurrentProgressCode(OrderCycleProcessCodeEnum.ORDER_CREATE.getCode());
//            orderIdMessageOut.setOrderTypeConfigId(orderCycle.getOrderTypeConfigId());
//            orderIdMessageOuts.add(orderIdMessageOut);
//            orderProcessSchedulingHandle.saveOrderProcessCopy(orderCycle, orderProcessOutList);
            dataForCreateDisOrderIn.setOrderProcessOutList(orderProcessOutList);
            dataForCreateDirOrderInList.add(dataForCreateDisOrderIn);
        }
//        AfterOrderCreatedMqOut afterOrderCreatedMqOut = new AfterOrderCreatedMqOut();
//        afterOrderCreatedMqOut.setOrderIdMessageOutList(orderIdMessageOuts);
//        afterOrderCreatedMqOut.setIsContainsNeedPayOrder(isContainsNeedPayOrder);
//        orderProcessSchedulingHandle.handleAfterCreateOrder(afterOrderCreatedMqOut);
        return dataForCreateDirOrderInList;
    }

    @Transactional(rollbackFor = Exception.class)
    public AfterOrderCreatedMqOut handleSaveOrder(List<DataForCreateDisOrderIn> dataForCreateDisOrderInList) {
        List<OrderIdMessageOut> orderIdMessageOutList = Lists.newArrayList();
        AtomicLong isContainsNeedPayOrderAtomicLong = new AtomicLong(0L);
        dataForCreateDisOrderInList.forEach(dataForCreateDirOrderIn -> {
            OrdDisOrderCycle ordDisOrderCycle = dataForCreateDirOrderIn.getOrdDisOrderCycle();
            String storeCode = ordDisOrderCycle.getStoreCode();
            String bizOrgCode = ordDisOrderCycle.getBizOrgCode();
            ordDisOrderCycleService.createOrderCycle(ordDisOrderCycle);
            String sourceCode = dataForCreateDirOrderIn.getOrdDisOrder().getSourceCode();
            // 校验该订货周期下是否短时间内重复下单
            boolean checkDisOrderCycleRepeatSubmitFlag = this.checkDisOrderCycleSubmit(ordDisOrderCycle, sourceCode);
            if (checkDisOrderCycleRepeatSubmitFlag) {
                log.error("门店{}订单类型{}，截单时间{}，短时间内异常重复提交订单，故判为无效提交！", storeCode, ordDisOrderCycle.getShortOrderType(), ordDisOrderCycle.getTruncationDateTime());
                return;
            }
            OrdDisOrder ordDisOrder = dataForCreateDirOrderIn.getOrdDisOrder();
            ordDisOrder.setOrderCycleId(ordDisOrderCycle.getId());
            ordDisOrderService.save(ordDisOrder);
            this.setDisOrderCycleKey(ordDisOrderCycle, sourceCode);
            List<OrdDisOrderDetail> ordDisOrderDetailList = dataForCreateDirOrderIn.getOrdDisOrderDetailList();
            ordDisOrderDetailList.forEach(ordDisOrderDetail -> ordDisOrderDetail.setOrderId(ordDisOrder.getId()));
            ordDisOrderDetailService.batchSaveDisOrderDetail(ordDisOrderDetailList);
            List<OrderProcessOut> orderProcessOutList = dataForCreateDirOrderIn.getOrderProcessOutList();
            orderProcessSchedulingHandle.saveOrderProcessCopy(ordDisOrderCycle, orderProcessOutList);
            if (isContainsNeedPayOrderAtomicLong.get() == 0) {
                long isContainsNeedPayOrder = orderProcessOutList.stream().filter(orderProcessOut -> orderProcessOut.getProcessCode().equals(OrderCycleProcessCodeEnum.ORDER_PAY.getCode())).count();
                isContainsNeedPayOrderAtomicLong.set(isContainsNeedPayOrder);
            }
            // 查找下个流程
            OrderProcessCopy nextOrderProcessCopy = orderProcessSchedulingHandle.getNextOrderProcessCopyByParameter(ordDisOrderCycle.getId(),
                    storeCode, OrderCycleProcessCodeEnum.ORDER_CREATE.getCode(), bizOrgCode);
            if (Objects.isNull(nextOrderProcessCopy)) {
                log.info(ordDisOrderCycle.getShortOrderType() + "下个流程不存在");
                throw new BusinessException(ordDisOrderCycle.getShortOrderType() + "下单流程不完整");
            }
            String nextLog = "门店" + storeCode + "的订货周期" + ordDisOrderCycle.getShortOrderType() + "下的订货单" +
                    ordDisOrder.getOrderNo() + "(" + com.edc.erp.common.enumeration.SourceTypeEnum.getValueByKey(sourceCode) + ")";
            // 如果是支付
            if (!SourceTypeEnum.INITIATIVE.getKey().equals(ordDisOrder.getSourceCode()) && OrderCycleProcessCodeEnum.ORDER_PAY.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
                log.info(nextLog + "需要支付流程，即将流转到支付");
                orderProcessSchedulingHandle.handlePayProcess(ordDisOrder);
            }
            // 如果是集货单
//            if (OrderCycleProcessCodeEnum.REQUEST_ORDER_CREATE.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
//                log.info(nextLog + "无支付流程，即将流转到集货单");
//                orderHandle.updateOrderStatusBySubmit(ordDisOrder, SystemConstant.SYSTEM_USER);
//                orderProcessSchedulingHandle.handleRequestOrderProcess(ordDisOrder, ordDisOrderCycle, SystemConstant.SYSTEM_USER);
//            }
            // 订单追踪
            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.CREATE_ORDER.getTemplate(), ordDisOrder.getOrderNo());
            ordDisOrderTrackService.pushRedisOrderTrackMessage(ordDisOrder.getOrderNo(), ordDisOrder.getStoreCode(),
                    OrderTrackStatusEnum.CREATE_ORDER.getName(), trackLog, ordDisOrder.getBizOrgCode(), ordDisOrder.getCreator(), ordDisOrder.getCreateTime());
            // 封装出参需要对象
            OrderIdMessageOut orderIdMessageOut = new OrderIdMessageOut();
            orderIdMessageOut.setOrderCycleId(ordDisOrderCycle.getId());
            orderIdMessageOut.setOrderId(ordDisOrder.getId());
            orderIdMessageOut.setOrderNo(ordDisOrder.getOrderNo());
            orderIdMessageOut.setOrgCode(ordDisOrder.getOrgCode());
            orderIdMessageOut.setBizOrgCode(bizOrgCode);
            orderIdMessageOut.setCurrentProgressCode(OrderCycleProcessCodeEnum.ORDER_CREATE.getCode());
            orderIdMessageOut.setOrderTypeConfigId(ordDisOrderCycle.getOrderTypeConfigId());
            orderIdMessageOutList.add(orderIdMessageOut);
        });
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = new AfterOrderCreatedMqOut();
        afterOrderCreatedMqOut.setOrderIdMessageOutList(orderIdMessageOutList);
        afterOrderCreatedMqOut.setIsContainsNeedPayOrder(isContainsNeedPayOrderAtomicLong.get());
        return afterOrderCreatedMqOut;
    }


//    /**
//     * 创建订单信息
//     *
//     * @param createOrderInfoIn
//     * @return
//     */
//    @Transactional(rollbackFor = Exception.class)
//    public AfterOrderCreatedMqOut createOrderInfo(CreateOrderInfoIn createOrderInfoIn) {
//        List<OrderCycleHeaderOut> orderCycleHeaderOutList = createOrderInfoIn.getOrderCycleHeaderOutList();
//        String sourceCode = createOrderInfoIn.getSourceCode();
//        String loginUsername = createOrderInfoIn.getLoginUsername();
//        String storeCode = createOrderInfoIn.getStoreCode();
//        String bizOrgCode = createOrderInfoIn.getBizOrgCode();
//        String orgCode = createOrderInfoIn.getOrgCode();
//        List<OrderIdMessageOut> orderIdMessageOuts = Lists.newArrayList();
//        long isContainsNeedPayOrder = 0;
//        for (OrderCycleHeaderOut orderCycleHeaderOut : orderCycleHeaderOutList) {
//            List<OrderProcessOut> orderProcessOutList = orderProcessSchedulingHandle.findProcessAndConfigAndItemByOrderTypeConfigId(orderCycleHeaderOut.getOrderTypeConfigId(), bizOrgCode);
//            if (CollectionUtils.isEmpty(orderProcessOutList)) {
//                throw new BusinessException("订货周期" + orderCycleHeaderOut.getShortOrderType() + "的订单流程为空");
//            }
//            if (isContainsNeedPayOrder == 0) {
//                isContainsNeedPayOrder = orderProcessOutList.stream().filter(orderProcessOut -> orderProcessOut.getProcessCode().equals(OrderCycleProcessCodeEnum.ORDER_PAY.getCode())).count();
//            }
//            this.checkSku(orderCycleHeaderOut.getGoodsList(), sourceCode);
//            this.checkMinAmount(orderCycleHeaderOut, sourceCode);
//            OrdDisOrderCycle orderCycle = this.getOrderCycleForCreateOrder(storeCode, loginUsername, bizOrgCode, orgCode, orderCycleHeaderOut);
//            // 校验该订货周期下是否短时间内重复下单
//            boolean checkDisOrderCycleRepeatSubmitFlag = this.checkDisOrderCycleSubmit(orderCycle, sourceCode);
//            if (checkDisOrderCycleRepeatSubmitFlag) {
//                log.error("门店{}订单类型{}，截单时间{}，短时间内异常重复提交订单，故判为无效提交！", storeCode, orderCycle.getShortOrderType(), orderCycle.getTruncationDateTime());
//                continue;
//            }
//            OrdDisOrder ordDisOrder = this.initOrder(orderCycle, sourceCode, createOrderInfoIn.getLoginUsername());
//            ordDisOrderService.save(ordDisOrder);
//            this.setDisOrderCycleKey(orderCycle, sourceCode);
//            List<OrdDisOrderDetail> orderDetailList = Lists.newArrayList();
//            orderCycleHeaderOut.getGoodsList().forEach(orderCartGoodsOut -> this.initOrderDetail(orderCartGoodsOut, ordDisOrder, orderDetailList));
//            ordDisOrder.setOrderAmount(ordDisOrder.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
//            ordDisOrder.setPreferentialAmount(ordDisOrder.getPreferentialAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
//            // 整单优惠金额
//            BigDecimal discountAmount = BigDecimal.ZERO;
//            ordDisOrder.setDiscountAmount(discountAmount);
//            ordDisOrderService.update(ordDisOrder);
//            ordDisOrderDetailService.batchSaveDisOrderDetail(orderDetailList);
//            // 封装订货单创建成功后向单据调度发送消息入参
//            OrderIdMessageOut orderIdMessageOut = new OrderIdMessageOut();
//            orderIdMessageOut.setOrderCycleId(orderCycle.getId());
//            orderIdMessageOut.setOrderId(ordDisOrder.getId());
//            orderIdMessageOut.setOrderNo(ordDisOrder.getOrderNo());
//            orderIdMessageOut.setBizOrgCode(bizOrgCode);
//            orderIdMessageOut.setOrgCode(orgCode);
//            orderIdMessageOut.setCurrentProgressCode(OrderCycleProcessCodeEnum.ORDER_CREATE.getCode());
//            orderIdMessageOut.setOrderTypeConfigId(orderCycle.getOrderTypeConfigId());
//            orderIdMessageOuts.add(orderIdMessageOut);
//            orderProcessSchedulingHandle.saveOrderProcessCopy(orderCycle, orderProcessOutList);
//            // 订单追踪
//            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.CREATE_ORDER.getTemplate(), ordDisOrder.getOrderNo());
//            ordDisOrderTrackService.pushRedisOrderTrackMessage(ordDisOrder.getOrderNo(), ordDisOrder.getStoreCode(),
//                    OrderTrackStatusEnum.CREATE_ORDER.getName(), trackLog, ordDisOrder.getBizOrgCode(), ordDisOrder.getCreator(), ordDisOrder.getCreateTime());
//        }
//        AfterOrderCreatedMqOut afterOrderCreatedMqOut = new AfterOrderCreatedMqOut();
//        afterOrderCreatedMqOut.setOrderIdMessageOutList(orderIdMessageOuts);
//        afterOrderCreatedMqOut.setIsContainsNeedPayOrder(isContainsNeedPayOrder);
//        orderProcessSchedulingHandle.handleAfterCreateOrder(afterOrderCreatedMqOut);
//        return afterOrderCreatedMqOut;
//    }

    /**
     * @param orderCartGoodsOut:
     * @param ordDisOrder:
     * @param orderDetailList:
     * @Description: 创建订货单明细
     * @Author: ZhangYao
     * @Date: 2023/8/25 13:52
     * @return: void
     **/
    private void initOrderDetail(OrderCartGoodsOut orderCartGoodsOut, OrdDisOrder ordDisOrder, List<OrdDisOrderDetail> orderDetailList) {
        OrdDisOrderDetail orderDetail = new OrdDisOrderDetail();
        BeanUtils.copy(orderCartGoodsOut, orderDetail);
        orderDetail.setOrderId(ordDisOrder.getId());
        orderDetail.setAllowDistributionReturn(Integer.parseInt(orderCartGoodsOut.getAllowDistributionReturn()));
        orderDetail.setOrderUnitPrice(orderCartGoodsOut.getPayUnitPrice());
        orderDetail.setOrderAmount(orderDetail.getOrderUnitPrice().multiply(orderDetail.getQuantity()));
        orderDetail.setOriginalPrice(orderCartGoodsOut.getOriginalUnitPrice());
        orderDetail.setOriginaAmount(orderDetail.getOriginalPrice().multiply(orderDetail.getQuantity()));
        orderDetail.setActivityNo(orderCartGoodsOut.getActivityCode());

        if (!ModelConst.ENABLE.isEnable(orderCartGoodsOut.getIsGift())) {
            orderDetail.setRealUnitPrice(orderDetail.getOrderUnitPrice());
        }
        if (ModelConst.ENABLE.isEnable(orderCartGoodsOut.getIsGift())) {
            orderDetail.setRealUnitPrice(BigDecimal.ZERO);
        }
        orderDetail.setRealAmount(orderDetail.getRealUnitPrice().multiply(orderDetail.getQuantity()));
        orderDetail.setIsDelete(ModelConst.DELETE.NO);
        orderDetail.setCreator(ordDisOrder.getCreator());
        orderDetail.setCreateTime(LocalDateTime.now());
        orderDetail.setUpdater(ordDisOrder.getUpdater());
        orderDetail.setUpdateTime(LocalDateTime.now());
//        ordDisOrderDetailService.save(orderDetail);
        orderDetailList.add(orderDetail);
        ordDisOrder.setOrderAmount(ordDisOrder.getOrderAmount().add(orderDetail.getOrderAmount()));
        // 累计计算订单优惠金额总额
        BigDecimal preferentialAmount = orderDetail.getOriginaAmount().subtract(orderDetail.getOrderAmount());
        ordDisOrder.setPreferentialAmount(ordDisOrder.getPreferentialAmount().add(preferentialAmount));
        if (CollectionUtils.isNotEmpty(orderCartGoodsOut.getGiftOutList())) {
            orderCartGoodsOut.getGiftOutList().forEach(giftGoods -> {
                OrdDisOrderDetail giftOrderDetail = new OrdDisOrderDetail();
                BeanUtils.copy(giftGoods, giftOrderDetail);
                giftOrderDetail.setOrderId(ordDisOrder.getId());
                giftOrderDetail.setBaseGoodsCode(orderDetail.getGoodsCode());
                giftOrderDetail.setIsGift(ModelConst.ENABLE.YES);
                giftOrderDetail.setActivityNo(orderCartGoodsOut.getActivityCode());
                giftOrderDetail.setOrderUnitPrice(giftGoods.getPayUnitPrice());
                giftOrderDetail.setOrderAmount(giftOrderDetail.getOrderUnitPrice().multiply(giftOrderDetail.getQuantity()));
                giftOrderDetail.setRealUnitPrice(BigDecimal.ZERO);
                giftOrderDetail.setIsDelete(ModelConst.DELETE.NO);
                giftOrderDetail.setCreator(ordDisOrder.getCreator());
                giftOrderDetail.setCreateTime(LocalDateTime.now());
                giftOrderDetail.setUpdater(ordDisOrder.getUpdater());
                giftOrderDetail.setUpdateTime(LocalDateTime.now());
//                ordDisOrderDetailService.save(giftOrderDetail);
                orderDetailList.add(giftOrderDetail);
            });
        }
    }

    /**
     * 校验sku
     *
     * @param orderCartGoodsOutList
     * @param sourceCode
     */
    private void checkSku(List<OrderCartGoodsOut> orderCartGoodsOutList, String sourceCode) {
        for (OrderCartGoodsOut orderCartGoodsOut : orderCartGoodsOutList) {
            if (0 == orderCartGoodsOut.getIsEnable()) {
                log.error(MessageFormat.format(ErrorMessageEnum.SKU_BUS_GATE.getValue(), orderCartGoodsOut.getGoodsCode()));
                throw new BusinessException(MessageFormat.format(ErrorMessageEnum.SKU_BUS_GATE.getValue(), orderCartGoodsOut.getGoodsCode()));
            }
            if (1 == orderCartGoodsOut.getIsShelves() && !SourceTypeEnum.DISTRIBUTION.getKey().equals(sourceCode)) {
                log.error(MessageFormat.format(ErrorMessageEnum.SKU_SHELVES.getValue(), orderCartGoodsOut.getGoodsCode()));
                throw new BusinessException(MessageFormat.format(ErrorMessageEnum.SKU_SHELVES.getValue(), orderCartGoodsOut.getGoodsCode()));
            }
            if (0 == orderCartGoodsOut.getIsCanBuyFlashSale()) {
                log.error(MessageFormat.format(ErrorMessageEnum.FLASH_SALE.getValue(), orderCartGoodsOut.getGoodsCode()));
                throw new BusinessException(MessageFormat.format(ErrorMessageEnum.FLASH_SALE.getValue(), orderCartGoodsOut.getGoodsCode()));
            }
        }
    }

    /**
     * 校验起订额
     *
     * @param orderCycleHeaderOut 购物车订货周期类型
     * @param sourceCode          下单来源
     */
    private void checkMinAmount(OrderCycleHeaderOut orderCycleHeaderOut, String sourceCode) {
        // 非手工订货不校验起订额
        if (!SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode)) {
            return;
        }
        String minAmountCheckItemCode = orderCycleHeaderOut.getMinAmountCheckType();
        // 非物料总金额
        BigDecimal noMaterielAmount = orderCycleHeaderOut.getGoodsList().stream()
                .filter(orderCartGoodsOut -> !GoodsTypeEnum.MATERIAL.getCode().equals(orderCartGoodsOut.getGoodsType()))
                .reduce(BigDecimal.ZERO, (x, y) -> x.add(y.getQuantity().multiply(y.getOriginalUnitPrice())), BigDecimal::add);
        noMaterielAmount = noMaterielAmount.add(orderCycleHeaderOut.getDistributionAndUpDownOrderPaidAmount());
        if (OrderCycleProcessConfigItemCodeEnum.ORDER_SINGLE.getCode().equals(minAmountCheckItemCode)
                && noMaterielAmount.compareTo(orderCycleHeaderOut.getMinimumOrderAmount()) == -1) {
            throw new BusinessException(orderCycleHeaderOut.getShortOrderType() + "不足起订额" + orderCycleHeaderOut.getMinimumOrderAmount());
        }
    }

    private boolean checkDisOrderCycleSubmit(OrdDisOrderCycle orderCycle, String sourceCode) {
        if (!SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode)) {
            return false;
        }
        String key = DisSystemConstant.CHECK_DIS_ORDER_CYCLE_REPEAT_SUBMIT + orderCycle.getBizOrgCode() +
                SystemConstant.COLON + orderCycle.getStoreCode() + SystemConstant.COLON + orderCycle.getId() + SystemConstant.WAIT + sourceCode;
        return redisService.hasKey(key);
    }

    private void setDisOrderCycleKey(OrdDisOrderCycle orderCycle, String sourceCode) {
        String key = DisSystemConstant.CHECK_DIS_ORDER_CYCLE_REPEAT_SUBMIT + orderCycle.getBizOrgCode() +
                SystemConstant.COLON + orderCycle.getStoreCode() + SystemConstant.COLON + orderCycle.getId() + SystemConstant.WAIT + sourceCode;
        redisService.set(key, orderCycle.getId(), 1, TimeUnit.MINUTES);
    }

    /**
     * 创建订货单时获取订货周期
     *
     * @param storeCode           门店代码
     * @param loginUsername       登录人
     * @param bizOrgCode          业务组织代码
     * @param orgCode             组织代码
     * @param orderCycleHeaderOut 购物车订货周期类型
     * @return
     */
    private OrdDisOrderCycle initOrderCycleForCreateOrder(String storeCode, String loginUsername, String bizOrgCode, String orgCode,
                                                          OrderCycleHeaderOut orderCycleHeaderOut) {
        OrdDisOrderCycle orderCycle = ordDisOrderCycleService.getOrderCycle(storeCode, orderCycleHeaderOut.getOrderTypeConfigId(),
                orderCycleHeaderOut.getTruncationTime(), bizOrgCode);
        if (Objects.isNull(orderCycle)) {
            OrdDisOrderCycle ordDisOrderCycle = new OrdDisOrderCycle();
            BeanUtils.copy(orderCycleHeaderOut, ordDisOrderCycle);
            ordDisOrderCycle.setStoreCode(storeCode);
            ordDisOrderCycle.setTruncationDateTime(orderCycleHeaderOut.getTruncationTime());
            ordDisOrderCycle.setOrgCode(orgCode);
            ordDisOrderCycle.setBizOrgCode(bizOrgCode);
            ordDisOrderCycle.setCreator(loginUsername);
            ordDisOrderCycle.setCreateTime(LocalDateTime.now());
            ordDisOrderCycle.setUpdateTime(LocalDateTime.now());
            ordDisOrderCycle.setUpdater(loginUsername);
            ordDisOrderCycle.setIsDelete(ModelConst.DELETE.NO);
            return ordDisOrderCycle;
        }
        return orderCycle;
    }

    private OrdDisOrderCycle getOrderCycleForCreateOrder(String storeCode, String loginUsername, String bizOrgCode, String orgCode,
                                                         OrderCycleHeaderOut orderCycleHeaderOut) {
        OrdDisOrderCycle orderCycle = ordDisOrderCycleService.getOrderCycle(storeCode, orderCycleHeaderOut.getOrderTypeConfigId(),
                orderCycleHeaderOut.getTruncationTime(), bizOrgCode);
        if (Objects.isNull(orderCycle)) {
            OrdDisOrderCycle ordDisOrderCycle = new OrdDisOrderCycle();
            BeanUtils.copy(orderCycleHeaderOut, ordDisOrderCycle);
            ordDisOrderCycle.setStoreCode(storeCode);
            ordDisOrderCycle.setTruncationDateTime(orderCycleHeaderOut.getTruncationTime());
            ordDisOrderCycle.setOrgCode(orgCode);
            ordDisOrderCycle.setBizOrgCode(bizOrgCode);
            ordDisOrderCycle.setCreator(loginUsername);
            ordDisOrderCycle.setCreateTime(LocalDateTime.now());
            ordDisOrderCycle.setUpdateTime(LocalDateTime.now());
            ordDisOrderCycle.setUpdater(loginUsername);
            ordDisOrderCycle.setIsDelete(ModelConst.DELETE.NO);
            return ordDisOrderCycleService.createOrderCycle(ordDisOrderCycle);
        }
        return orderCycle;
    }

    /**
     * 初始化订单信息
     *
     * @param orderCycle
     * @param sourceCode
     * @param loginUsername
     * @return
     */
    private OrdDisOrder initOrder(OrdDisOrderCycle orderCycle, String sourceCode, String loginUsername) {
        OrdDisOrder order = new OrdDisOrder();
        order.setOrderCycleId(orderCycle.getId());
        order.setStoreCode(orderCycle.getStoreCode());
        order.setOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KDH.getCode(), orderCycle.getBizOrgCode(), uniqueUtils, 4));
        order.setOrderStatusCode(OrderStatusEnum.WAIT_PAYMENT.getKey());
        order.setOrderAmount(BigDecimal.ZERO);
        order.setPreferentialAmount(BigDecimal.ZERO);
        order.setSourceCode(sourceCode);
        order.setFreezeStatus(OrderFreezeEnum.UN_FREEZE.getKey());
        order.setOrgCode(orderCycle.getOrgCode());
        order.setBizOrgCode(orderCycle.getBizOrgCode());
        order.setCreator(loginUsername);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdater(loginUsername);
        order.setUpdateTime(LocalDateTime.now());
        order.setSubmitter(loginUsername);
        order.setSumbitTime(LocalDateTime.now());
        order.setIsDelete(ModelConst.DELETE.NO);
        return order;
    }

    @Transactional
    public void updateDisOrderGoods(List<UpdateDisOrderGoodsIn> updateOrderGoodsInList, OrdDisOrder order, String loginUsername) {
        List<OrdDisOrderDetail> orderDetailList = Lists.newArrayList();
        StringJoiner goodsJoiner = new StringJoiner(";");
        updateOrderGoodsInList.forEach(updateOrderGoodsIn -> {
            OrdDisOrderDetail orderDetail = ordDisOrderDetailService.getOrderDetailByIdAndOrderId(updateOrderGoodsIn.getOrderDetailId(), order.getId());
            if (Objects.isNull(orderDetail)) {
                throw new BusinessException("商品" + updateOrderGoodsIn.getGoodsCode() + "查询异常");
            }
            goodsJoiner.add("商品" + updateOrderGoodsIn + "包装数量由" + orderDetail.getPackageQuantity() + "修改为" + updateOrderGoodsIn.getPackageQuantity());
            orderDetail.setPackageQuantity(updateOrderGoodsIn.getPackageQuantity());
            orderDetail.setQuantity(updateOrderGoodsIn.getPackageQuantity().multiply(orderDetail.getDistributionSpecificationNum()));
            orderDetail.setUpdater(loginUsername);
            orderDetail.setUpdateTime(LocalDateTime.now());
            // 删除商品
            if (updateOrderGoodsIn.getPackageQuantity().compareTo(BigDecimal.ZERO) == 0) {
                ordDisOrderDetailService.deleteByPrimaryKey(updateOrderGoodsIn.getOrderDetailId());
            } else {
                ordDisOrderDetailService.updateByPrimaryKeySelective(orderDetail);
                orderDetailList.add(orderDetail);
            }
        });
        BigDecimal orderAmount = orderDetailList.stream().reduce(BigDecimal.ZERO, (x, y) -> x.add(y.getOriginalPrice().multiply(y.getQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP)), BigDecimal::add);
//        BigDecimal payableAmount = orderDetailList.stream().reduce(BigDecimal.ZERO, (x, y) -> x.add(y.getPayUnitPrice().multiply(y.getQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP)), BigDecimal::add);
        order.setOrderAmount(orderAmount);
//        order.setPayableAmount(payableAmount);
        // 整单优惠金额（整单优惠金额：满减金额/优惠券金额）
        BigDecimal orderPreferentialAmount = BigDecimal.ZERO;
        order.setPreferentialAmount(order.getOrderAmount().subtract(order.getOrderAmount()).add(orderPreferentialAmount));
        order.setUpdater(loginUsername);
        order.setUpdateTime(LocalDateTime.now());
        ordDisOrderService.update(order);
        String content = MessageFormat.format(OrderLogEnum.UPDATE_ORDER_GOODS.getKey(), loginUsername, order.getOrderNo(), goodsJoiner.toString());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), loginUsername);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateOrderSourceCode(List<Long> orderIdList, String sourceCode) {
        orderHandle.updateOrderSourceCode(orderIdList, sourceCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrderOrderIdentification(List<Long> orderIdList, String orderIdentification) {
        orderHandle.updateOrderOrderIdentification(orderIdList, orderIdentification);
    }
}
