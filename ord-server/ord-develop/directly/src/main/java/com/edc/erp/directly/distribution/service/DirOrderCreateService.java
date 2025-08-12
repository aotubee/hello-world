package com.edc.erp.directly.distribution.service;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.GoodsTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.OrderNoPreEnum;
import com.edc.erp.common.enumeration.SourceTypeEnum;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.model.in.CreateOrderInfoIn;
import com.edc.erp.directly.distribution.model.in.DataForCreateDirOrderIn;
import com.edc.erp.directly.distribution.model.in.UpdateDirOrderGoodsIn;
import com.edc.erp.directly.distribution.model.out.AfterOrderCreatedMqOut;
import com.edc.erp.directly.distribution.model.out.OrderCartGoodsOut;
import com.edc.erp.directly.distribution.model.out.OrderCycleHeaderOut;
import com.edc.erp.directly.distribution.model.out.OrderIdMessageOut;
import com.edc.erp.directly.entity.DirOrderProcessCopy;
import com.edc.erp.directly.enumeration.*;
import com.edc.erp.directly.model.out.DirOrderProcessOut;
import com.edc.erp.directly.orderscheduing.handle.DirOrderProcessSchedulingHandle;
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

/**
 * @author fxw
 * @description: 订单创建业务处理层
 * @since 2022/10/17 19:24
 */
@Service
@Slf4j
public class DirOrderCreateService {

    @Autowired
    private DirOrderProcessSchedulingHandle orderProcessSchedulingHandle;

    @Qualifier("ordDirOrderServiceImpl")
    @Autowired
    private OrdDirOrderService ordDirOrderService;

    @Qualifier("ordDirOrderDetailServiceImpl")
    @Autowired
    private OrdDirOrderDetailService ordDirOrderDetailService;

    @Autowired
    @Qualifier("ordDirOrderCycleServiceImpl")
    private OrdDirOrderCycleService ordDirOrderCycleService;

    @Autowired
    private UniqueUtils uniqueUtils;

    @Autowired
    private OrdDirOrderTrackService ordDirOrderTrackService;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderHandle orderHandle;

    public List<DataForCreateDirOrderIn> handleBeforeSubmitCreateOrder(CreateOrderInfoIn createOrderInfoIn) {
        List<OrderCycleHeaderOut> orderCycleHeaderOutList = createOrderInfoIn.getOrderCycleHeaderOutList();
        String sourceCode = createOrderInfoIn.getSourceCode();
        String loginUsername = createOrderInfoIn.getLoginUsername();
        String storeCode = createOrderInfoIn.getStoreCode();
        String bizOrgCode = createOrderInfoIn.getBizOrgCode();
        String orgCode = createOrderInfoIn.getOrgCode();

//        List<OrderIdMessageOut> orderIdMessageOuts = Lists.newArrayList();
        long isContainsNeedPayOrder = 0;
        List<DataForCreateDirOrderIn> dataForCreateDirOrderInList = Lists.newArrayList();
        for (OrderCycleHeaderOut orderCycleHeaderOut : orderCycleHeaderOutList) {
            List<DirOrderProcessOut> orderProcessOutList = orderProcessSchedulingHandle.findProcessAndConfigAndItemByOrderTypeConfigId(orderCycleHeaderOut.getOrderTypeConfigId(), bizOrgCode);
            if (CollectionUtils.isEmpty(orderProcessOutList)) {
                throw new BusinessException("订货周期" + orderCycleHeaderOut.getShortOrderType() + "的订单流程为空");
            }
//            if (isContainsNeedPayOrder == 0) {
//                isContainsNeedPayOrder = orderProcessOutList.stream().filter(orderProcessOut -> orderProcessOut.getProcessCode().equals(OrderCycleProcessCodeEnum.ORDER_PAY.getCode())).count();
//            }
            this.checkSku(orderCycleHeaderOut.getGoodsList(), sourceCode);
            this.checkMinAmount(orderCycleHeaderOut, sourceCode);
            OrdDirOrderCycle orderCycle = this.initOrderCycleForCreateOrder(storeCode, loginUsername, orgCode, bizOrgCode, orderCycleHeaderOut);
            DataForCreateDirOrderIn dataForCreateDirOrderIn = new DataForCreateDirOrderIn();
            OrdDirOrder ordDirOrder = this.initOrder(orderCycle, sourceCode, createOrderInfoIn.getLoginUsername());
//            ordDirOrderService.save(ordDirOrder);
            dataForCreateDirOrderIn.setOrdDirOrderCycle(orderCycle);
            List<OrdDirOrderDetail> ordDirOrderDetailList = Lists.newArrayList();
            orderCycleHeaderOut.getGoodsList().forEach(orderCartGoodsOut -> this.initOrderDetail(orderCartGoodsOut, ordDirOrder, ordDirOrderDetailList));
            ordDirOrder.setOrderAmount(ordDirOrder.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            ordDirOrder.setPreferentialAmount(ordDirOrder.getPreferentialAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            // 整单优惠金额
            BigDecimal discountAmount = BigDecimal.ZERO;
            ordDirOrder.setDiscountAmount(discountAmount);
            dataForCreateDirOrderIn.setOrdDirOrder(ordDirOrder);
//            ordDirOrderService.update(ordDirOrder);
//            ordDirOrderDetailService.batchSaveDirOrderDetail(ordDirOrderDetailList);
            dataForCreateDirOrderIn.setOrdDirOrderDetailList(ordDirOrderDetailList);
            dataForCreateDirOrderIn.setOrderProcessOutList(orderProcessOutList);
//            dataForCreateOrderIn.setOrderIdMessageOuts(orderIdMessageOuts);
            dataForCreateDirOrderInList.add(dataForCreateDirOrderIn);
//            // 订单追踪
//            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.CREATE_ORDER.getTemplate(), ordDirOrder.getOrderNo());
//            ordDirOrderTrackService.pushRedisOrderTrackMessage(ordDirOrder.getOrderNo(), ordDirOrder.getStoreCode(),
//                    OrderTrackStatusEnum.CREATE_ORDER.getName(), trackLog, ordDirOrder.getBizOrgCode(), ordDirOrder.getCreator(), ordDirOrder.getCreateTime());
        }
//        AfterOrderCreatedMqOut afterOrderCreatedMqOut = new AfterOrderCreatedMqOut();
//        afterOrderCreatedMqOut.setOrderIdMessageOutList(orderIdMessageOuts);
//        afterOrderCreatedMqOut.setIsContainsNeedPayOrder(isContainsNeedPayOrder);
//        orderProcessSchedulingHandle.handleAfterCreateOrder(afterOrderCreatedMqOut);
        return dataForCreateDirOrderInList;
    }

    @Transactional(rollbackFor = Exception.class)
    public AfterOrderCreatedMqOut handleSaveOrder(List<DataForCreateDirOrderIn> dataForCreateDirOrderInList) {
        List<OrderIdMessageOut> orderIdMessageOutList = Lists.newArrayList();
        dataForCreateDirOrderInList.forEach(dataForCreateDirOrderIn -> {
            OrdDirOrderCycle ordDirOrderCycle = dataForCreateDirOrderIn.getOrdDirOrderCycle();
            String storeCode = ordDirOrderCycle.getStoreCode();
            String bizOrgCode = ordDirOrderCycle.getBizOrgCode();
            ordDirOrderCycleService.createOrderCycle(ordDirOrderCycle);
            String sourceCode = dataForCreateDirOrderIn.getOrdDirOrder().getSourceCode();
            // 校验该订货周期下是否短时间内重复下单
            boolean checkDirOrderCycleRepeatSubmitFlag = this.checkDirOrderCycleSubmit(ordDirOrderCycle, sourceCode);
            if (checkDirOrderCycleRepeatSubmitFlag) {
                log.error("门店{}订单类型{}，截单时间{}，短时间内异常重复提交订单，故判为无效提交！", storeCode, ordDirOrderCycle.getShortOrderType(), ordDirOrderCycle.getTruncationDateTime());
                return;
            }

            OrdDirOrder ordDirOrder = dataForCreateDirOrderIn.getOrdDirOrder();
            ordDirOrder.setOrderCycleId(ordDirOrderCycle.getId());
            ordDirOrderService.save(ordDirOrder);
            this.setDirOrderCycleKey(ordDirOrderCycle, sourceCode);
            List<OrdDirOrderDetail> ordDirOrderDetailList = dataForCreateDirOrderIn.getOrdDirOrderDetailList();
            ordDirOrderDetailList.forEach(ordDirOrderDetail -> ordDirOrderDetail.setOrderId(ordDirOrder.getId()));
            ordDirOrderDetailService.batchSaveDirOrderDetail(ordDirOrderDetailList);
            List<DirOrderProcessOut> orderProcessOutList = dataForCreateDirOrderIn.getOrderProcessOutList();
            orderProcessSchedulingHandle.saveOrderProcessCopy(ordDirOrderCycle, orderProcessOutList);
            // 查找下个流程
            DirOrderProcessCopy nextOrderProcessCopy = orderProcessSchedulingHandle.getNextOrderProcessCopyByParameter(ordDirOrderCycle.getId(),
                    storeCode, OrderCycleProcessCodeEnum.ORDER_CREATE.getCode(), bizOrgCode);
            if (Objects.isNull(nextOrderProcessCopy)) {
                log.info(ordDirOrderCycle.getShortOrderType() + "下个流程不存在");
                throw new BusinessException(ordDirOrderCycle.getShortOrderType() + "下单流程不完整");
            }
            // 待更新状态的订货单
            String nextLog = "门店" + storeCode + "的订货周期" + ordDirOrderCycle.getShortOrderType() + "下的订货单" + ordDirOrder.getOrderNo() + "(" + SourceTypeEnum.getValueByKey(ordDirOrder.getSourceCode()) + ")";
            // 如果是支付
//            if (OrderCycleProcessCodeEnum.ORDER_PAY.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
//                log.info(nextLog + "需要支付流程，即将流转到支付");
//                this.handlePayProcess(bizOrgCode, ordDirOrder);
//            }
            // 如果是要货单
            if (OrderCycleProcessCodeEnum.REQUEST_ORDER_CREATE.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
                log.info(nextLog + "订货单{}无支付流程，即将流转到要货单", ordDirOrder.getOrderNo());
                orderHandle.updateOrderStatusBySubmit(ordDirOrder, SystemConstant.SYSTEM_USER);
                orderProcessSchedulingHandle.handleRequestOrderProcess(ordDirOrder, ordDirOrderCycle, SystemConstant.SYSTEM_USER);
            }
            // 订单追踪
            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.CREATE_ORDER.getTemplate(), ordDirOrder.getOrderNo());
            ordDirOrderTrackService.pushRedisOrderTrackMessage(ordDirOrder.getOrderNo(), ordDirOrder.getStoreCode(),
                    OrderTrackStatusEnum.CREATE_ORDER.getName(), trackLog, ordDirOrder.getBizOrgCode(), ordDirOrder.getCreator(), ordDirOrder.getCreateTime());
            // 封装出参需要对象
            OrderIdMessageOut orderIdMessageOut = new OrderIdMessageOut();
            orderIdMessageOut.setOrderCycleId(ordDirOrderCycle.getId());
            orderIdMessageOut.setOrderId(ordDirOrder.getId());
            orderIdMessageOut.setOrderNo(ordDirOrder.getOrderNo());
            orderIdMessageOut.setOrgCode(ordDirOrder.getOrgCode());
            orderIdMessageOut.setBizOrgCode(bizOrgCode);
            orderIdMessageOut.setCurrentProgressCode(OrderCycleProcessCodeEnum.ORDER_CREATE.getCode());
            orderIdMessageOut.setOrderTypeConfigId(ordDirOrderCycle.getOrderTypeConfigId());
            orderIdMessageOutList.add(orderIdMessageOut);
        });
        AfterOrderCreatedMqOut afterOrderCreatedMqOut = new AfterOrderCreatedMqOut();
        afterOrderCreatedMqOut.setOrderIdMessageOutList(orderIdMessageOutList);
        afterOrderCreatedMqOut.setIsContainsNeedPayOrder(0L);
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
//
//        List<OrderIdMessageOut> orderIdMessageOuts = Lists.newArrayList();
//        long isContainsNeedPayOrder = 0;
//        for (OrderCycleHeaderOut orderCycleHeaderOut : orderCycleHeaderOutList) {
//            List<DirOrderProcessOut> orderProcessOutList = orderProcessSchedulingHandle.findProcessAndConfigAndItemByOrderTypeConfigId(orderCycleHeaderOut.getOrderTypeConfigId(), bizOrgCode);
//            if (CollectionUtils.isEmpty(orderProcessOutList)) {
//                throw new BusinessException("订货周期" + orderCycleHeaderOut.getShortOrderType() + "的订单流程为空");
//            }
////            if (isContainsNeedPayOrder == 0) {
////                isContainsNeedPayOrder = orderProcessOutList.stream().filter(orderProcessOut -> orderProcessOut.getProcessCode().equals(OrderCycleProcessCodeEnum.ORDER_PAY.getCode())).count();
////            }
//            this.checkSku(orderCycleHeaderOut.getGoodsList(), sourceCode);
//            this.checkMinAmount(orderCycleHeaderOut, sourceCode);
//            OrdDirOrderCycle orderCycle = this.getOrderCycleForCreateOrder(storeCode, loginUsername, orgCode, bizOrgCode, orderCycleHeaderOut);
//            // 校验该订货周期下是否短时间内重复下单
//            boolean checkDirOrderCycleRepeatSubmitFlag = this.checkDirOrderCycleSubmit(orderCycle, sourceCode);
//            if (checkDirOrderCycleRepeatSubmitFlag) {
//                log.error("门店{}订单类型{}，截单时间{}，短时间内异常重复提交订单，故判为无效提交！", storeCode, orderCycle.getShortOrderType(), orderCycle.getTruncationDateTime());
//                continue;
//            }
//            OrdDirOrder ordDirOrder = this.initOrder(orderCycle, sourceCode, createOrderInfoIn.getLoginUsername());
//            ordDirOrderService.save(ordDirOrder);
//            this.setDirOrderCycleKey(orderCycle, sourceCode);
//            List<OrdDirOrderDetail> ordDirOrderDetailList = Lists.newArrayList();
//            orderCycleHeaderOut.getGoodsList().forEach(orderCartGoodsOut -> this.initOrderDetail(orderCartGoodsOut, ordDirOrder, ordDirOrderDetailList));
//            ordDirOrder.setOrderAmount(ordDirOrder.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
//            ordDirOrder.setPreferentialAmount(ordDirOrder.getPreferentialAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
//            // 整单优惠金额
//            BigDecimal discountAmount = BigDecimal.ZERO;
//            ordDirOrder.setDiscountAmount(discountAmount);
//            ordDirOrderService.update(ordDirOrder);
//            ordDirOrderDetailService.batchSaveDirOrderDetail(ordDirOrderDetailList);
//            // 封装订货单创建成功后向单据调度发送消息入参
//            OrderIdMessageOut orderIdMessageOut = new OrderIdMessageOut();
//            orderIdMessageOut.setOrderCycleId(orderCycle.getId());
//            orderIdMessageOut.setOrderId(ordDirOrder.getId());
//            orderIdMessageOut.setOrderNo(ordDirOrder.getOrderNo());
//            orderIdMessageOut.setOrgCode(orgCode);
//            orderIdMessageOut.setBizOrgCode(bizOrgCode);
//            orderIdMessageOut.setCurrentProgressCode(OrderCycleProcessCodeEnum.ORDER_CREATE.getCode());
//            orderIdMessageOut.setOrderTypeConfigId(orderCycle.getOrderTypeConfigId());
//            orderIdMessageOuts.add(orderIdMessageOut);
//            orderProcessSchedulingHandle.saveOrderProcessCopy(orderCycle, orderProcessOutList);
//            // 订单追踪
//            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.CREATE_ORDER.getTemplate(), ordDirOrder.getOrderNo());
//            ordDirOrderTrackService.pushRedisOrderTrackMessage(ordDirOrder.getOrderNo(), ordDirOrder.getStoreCode(), OrderTrackStatusEnum.CREATE_ORDER.getName(), trackLog, ordDirOrder.getBizOrgCode(), ordDirOrder.getCreator(), ordDirOrder.getCreateTime());
//        }
//        AfterOrderCreatedMqOut afterOrderCreatedMqOut = new AfterOrderCreatedMqOut();
//        afterOrderCreatedMqOut.setOrderIdMessageOutList(orderIdMessageOuts);
//        afterOrderCreatedMqOut.setIsContainsNeedPayOrder(isContainsNeedPayOrder);
//        orderProcessSchedulingHandle.handleAfterCreateOrder(afterOrderCreatedMqOut);
//        return afterOrderCreatedMqOut;
//    }

    @Transactional
    public void updateDirOrderGoods(List<UpdateDirOrderGoodsIn> updateOrderGoodsInList, OrdDirOrder order, String loginUsername) {
        List<OrdDirOrderDetail> orderDetailList = Lists.newArrayList();
        StringJoiner goodsJoiner = new StringJoiner(";");
        updateOrderGoodsInList.forEach(updateOrderGoodsIn -> {
            OrdDirOrderDetail orderDetail = ordDirOrderDetailService.getOrderDetailByIdAndOrderId(updateOrderGoodsIn.getOrderDetailId(), order.getId());
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
                ordDirOrderDetailService.deleteByPrimaryKey(updateOrderGoodsIn.getOrderDetailId());
            } else {
                ordDirOrderDetailService.updateByPrimaryKeySelective(orderDetail);
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
        ordDirOrderService.update(order);
        String content = MessageFormat.format(OrderLogEnum.UPDATE_ORDER_GOODS.getKey(), loginUsername, String.valueOf(order.getId()), goodsJoiner.toString());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), String.valueOf(order.getId()),
                OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), loginUsername);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateOrderSourceCode(List<Long> orderIdList, String sourceCode) {
        orderHandle.updateOrderSourceCode(orderIdList, sourceCode);
    }

    /**
     * @param orderCartGoodsOut:
     * @param ordDirOrder:
     * @param ordDirOrderDetailList:
     * @Description: 创建订货单明细
     * @Author: ZhangYao
     * @Date: 2023/8/25 13:50
     * @return: void
     **/
    private void initOrderDetail(OrderCartGoodsOut orderCartGoodsOut, OrdDirOrder ordDirOrder, List<OrdDirOrderDetail> ordDirOrderDetailList) {
        OrdDirOrderDetail orderDetail = new OrdDirOrderDetail();
        BeanUtils.copy(orderCartGoodsOut, orderDetail);
        orderDetail.setAllowDistributionReturn(Integer.parseInt(orderCartGoodsOut.getAllowDistributionReturn()));
        orderDetail.setOrderId(ordDirOrder.getId());
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
        orderDetail.setCreator(ordDirOrder.getCreator());
        orderDetail.setCreateTime(LocalDateTime.now());
        orderDetail.setUpdater(ordDirOrder.getUpdater());
        orderDetail.setUpdateTime(LocalDateTime.now());
//        ordDirOrderDetailService.save(orderDetail);
        ordDirOrderDetailList.add(orderDetail);
//        log.info("门店{}封装订货单明细商品{}----", ordDirOrder.getStoreCode(), orderDetail.getGoodsCode());
        ordDirOrder.setOrderAmount(ordDirOrder.getOrderAmount().add(orderDetail.getOrderAmount()));
        // 累计计算订单优惠金额总额
        BigDecimal preferentialAmount = orderDetail.getOriginaAmount().subtract(orderDetail.getOrderAmount());
        ordDirOrder.setPreferentialAmount(ordDirOrder.getPreferentialAmount().add(preferentialAmount));
        if (CollectionUtils.isNotEmpty(orderCartGoodsOut.getGiftOutList())) {
            orderCartGoodsOut.getGiftOutList().forEach(giftGoods -> {
                OrdDirOrderDetail giftOrderDetail = new OrdDirOrderDetail();
                BeanUtils.copy(giftGoods, giftOrderDetail);
                giftOrderDetail.setOrderId(ordDirOrder.getId());
                giftOrderDetail.setBaseGoodsCode(orderDetail.getGoodsCode());
                giftOrderDetail.setIsGift(ModelConst.ENABLE.YES);
                giftOrderDetail.setActivityNo(orderCartGoodsOut.getActivityCode());
//                orderDetail.setPreferentialAllocationUnitPrice(BigDecimal.ZERO);
                giftOrderDetail.setOrderUnitPrice(giftGoods.getPayUnitPrice());
                giftOrderDetail.setOrderAmount(giftOrderDetail.getOrderUnitPrice().multiply(giftOrderDetail.getQuantity()));
                giftOrderDetail.setRealUnitPrice(BigDecimal.ZERO);
                giftOrderDetail.setIsDelete(ModelConst.DELETE.NO);
                giftOrderDetail.setCreator(ordDirOrder.getCreator());
                giftOrderDetail.setCreateTime(LocalDateTime.now());
                giftOrderDetail.setUpdater(ordDirOrder.getUpdater());
                giftOrderDetail.setUpdateTime(LocalDateTime.now());
//                ordDirOrderDetailService.save(giftOrderDetail);
                ordDirOrderDetailList.add(giftOrderDetail);
//                log.info("门店{}封装订货单赠品明细商品{}----", ordDirOrder.getStoreCode(), giftOrderDetail.getGoodsCode());
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
        orderCartGoodsOutList.forEach(orderCartGoodsOut -> {
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
        });
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
        if (OrderCycleProcessConfigItemCodeEnum.ORDER_SINGLE.getCode().equals(minAmountCheckItemCode) && noMaterielAmount.compareTo(orderCycleHeaderOut.getMinimumOrderAmount()) == -1) {
            throw new BusinessException(orderCycleHeaderOut.getShortOrderType() + "不足起订额" + orderCycleHeaderOut.getMinimumOrderAmount());
        }
    }

    private boolean checkDirOrderCycleSubmit(OrdDirOrderCycle orderCycle, String sourceCode) {
        if (!SourceTypeEnum.INITIATIVE.getKey().equals(sourceCode)) {
            return false;
        }
        String key = DirSystemConstant.CHECK_DIR_ORDER_CYCLE_REPEAT_SUBMIT + orderCycle.getBizOrgCode() +
                SystemConstant.COLON + orderCycle.getStoreCode() + SystemConstant.COLON + orderCycle.getId() + SystemConstant.WAIT + sourceCode;
        return redisService.hasKey(key);
    }

    private void setDirOrderCycleKey(OrdDirOrderCycle orderCycle, String sourceCode) {
        String key = DirSystemConstant.CHECK_DIR_ORDER_CYCLE_REPEAT_SUBMIT + orderCycle.getBizOrgCode() +
                SystemConstant.COLON + orderCycle.getStoreCode() + SystemConstant.COLON + orderCycle.getId() + SystemConstant.WAIT + sourceCode;
        redisService.set(key, orderCycle.getId(), 1, TimeUnit.MINUTES);
    }

    /**
     * 创建订货单时获取订货周期
     *
     * @param storeCode           门店代码
     * @param loginUsername       登录人
     * @param orgCode             组织代码
     * @param bizOrgCode          业务组织代码
     * @param orderCycleHeaderOut 购物车订货周期类型
     * @return
     */
    private OrdDirOrderCycle getOrderCycleForCreateOrder(String storeCode, String loginUsername, String orgCode, String bizOrgCode, OrderCycleHeaderOut orderCycleHeaderOut) {
        OrdDirOrderCycle orderCycle = ordDirOrderCycleService.getOrderCycle(storeCode, orderCycleHeaderOut.getOrderTypeConfigId(), orderCycleHeaderOut.getTruncationTime(), bizOrgCode);
        if (Objects.isNull(orderCycle)) {
            OrdDirOrderCycle ordDirOrderCycle = new OrdDirOrderCycle();
            BeanUtils.copy(orderCycleHeaderOut, ordDirOrderCycle);
            ordDirOrderCycle.setStoreCode(storeCode);
            ordDirOrderCycle.setTruncationDateTime(orderCycleHeaderOut.getTruncationTime());
            ordDirOrderCycle.setFirstOrderTime(LocalDateTime.now());
            ordDirOrderCycle.setOrgCode(orgCode);
            ordDirOrderCycle.setBizOrgCode(bizOrgCode);
            ordDirOrderCycle.setCreator(loginUsername);
            ordDirOrderCycle.setCreateTime(LocalDateTime.now());
            ordDirOrderCycle.setUpdater(loginUsername);
            ordDirOrderCycle.setUpdateTime(LocalDateTime.now());
            ordDirOrderCycle.setIsDelete(ModelConst.DELETE.NO);
            return ordDirOrderCycleService.createOrderCycle(ordDirOrderCycle);
        }
        return orderCycle;
    }

    private OrdDirOrderCycle initOrderCycleForCreateOrder(String storeCode, String loginUsername, String orgCode, String bizOrgCode, OrderCycleHeaderOut orderCycleHeaderOut) {
        OrdDirOrderCycle orderCycle = ordDirOrderCycleService.getOrderCycle(storeCode, orderCycleHeaderOut.getOrderTypeConfigId(), orderCycleHeaderOut.getTruncationTime(), bizOrgCode);
        if (Objects.isNull(orderCycle)) {
            OrdDirOrderCycle ordDirOrderCycle = new OrdDirOrderCycle();
            BeanUtils.copy(orderCycleHeaderOut, ordDirOrderCycle);
            ordDirOrderCycle.setStoreCode(storeCode);
            ordDirOrderCycle.setTruncationDateTime(orderCycleHeaderOut.getTruncationTime());
            ordDirOrderCycle.setFirstOrderTime(LocalDateTime.now());
            ordDirOrderCycle.setOrgCode(orgCode);
            ordDirOrderCycle.setBizOrgCode(bizOrgCode);
            ordDirOrderCycle.setCreator(loginUsername);
            ordDirOrderCycle.setCreateTime(LocalDateTime.now());
            ordDirOrderCycle.setUpdater(loginUsername);
            ordDirOrderCycle.setUpdateTime(LocalDateTime.now());
            ordDirOrderCycle.setIsDelete(ModelConst.DELETE.NO);
            return ordDirOrderCycle;
        }
        return orderCycle;
    }

    /**
     * 初始化订单信息
     *
     * @param ordDirOrderCycle
     * @param sourceCode
     * @param loginUsername
     * @return
     */
    private OrdDirOrder initOrder(OrdDirOrderCycle ordDirOrderCycle, String sourceCode, String loginUsername) {
        OrdDirOrder order = new OrdDirOrder();
        order.setOrderCycleId(ordDirOrderCycle.getId());
        order.setStoreCode(ordDirOrderCycle.getStoreCode());
        order.setOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.MB.getCode(), ordDirOrderCycle.getBizOrgCode(), uniqueUtils, 4));
        order.setOrderStatusCode(OrderStatusEnum.SUBMIT.getKey());
        order.setOrderAmount(BigDecimal.ZERO);
        order.setOrderAmount(BigDecimal.ZERO);
        order.setPreferentialAmount(BigDecimal.ZERO);
        order.setSourceCode(sourceCode);
        order.setOrgCode(ordDirOrderCycle.getOrgCode());
        order.setBizOrgCode(ordDirOrderCycle.getBizOrgCode());
        order.setCreator(loginUsername);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdater(loginUsername);
        order.setUpdateTime(LocalDateTime.now());
        order.setSubmitter(loginUsername);
        order.setSumbitTime(LocalDateTime.now());
        order.setIsDelete(ModelConst.DELETE.NO);
        return order;
    }
}
