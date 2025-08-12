package com.edc.erp.orderscheduing.handle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderProcessConfigItem;
import com.edc.erp.common.entity.OrderProcessCopy;
import com.edc.erp.common.entity.OrderTypeConfig;
import com.edc.erp.common.enumeration.SourceTypeEnum;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.FrozenOrderIn;
import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.model.out.OrderProcessOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.common.service.OrderProcessConfigItemService;
import com.edc.erp.common.service.OrderProcessService;
import com.edc.erp.common.service.OrderTypeConfigService;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.disrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.disrequestorder.model.out.CheckSkuForCreateRequestOrderOut;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.service.DisOrderPayService;
import com.edc.erp.enumeration.*;
import com.edc.erp.handle.DisDeliveryOrderHandle;
import com.edc.erp.handle.DisOrderConfigHandle;
import com.edc.erp.handle.DisRequestOrderHandle;
import com.edc.erp.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.service.DisOrderProcessCopyService;
import com.edc.erp.service.impl.DisOrderProcessCopyServiceImpl;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.redis.RedisService;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 单据流程handle
 * @since 2022/10/18 16:35
 */
@Service
@Slf4j
public class OrderProcessSchedulingHandle extends DisOrderProcessCopyServiceImpl {

    @Autowired
    @Qualifier("orderTypeConfigServiceImpl")
    private OrderTypeConfigService orderTypeConfigService;

    @Autowired
    private OrderProcessService orderProcessService;

    @Autowired
    private OrderProcessConfigItemService orderProcessConfigItemService;

    @Autowired
    private DisOrderConfigHandle orderConfigHandle;

    @Autowired
    private RedisService redisService;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private DisOrderHandle orderHandle;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private DisRequestOrderHandle requestOrderHandle;

    @Autowired
    @Qualifier("disOrderProcessCopyServiceImpl")
    private DisOrderProcessCopyService orderProcessCopyService;

    @Autowired
    private DisDeliveryOrderHandle disDeliveryOrderHandle;

    @Autowired
    private AsyncPushTaskService asyncPushTaskService;

    @Autowired
    private FundServer fundServer;

    @Autowired
    private DisOrderPayService disOrderPayService;

    @Autowired
    @Qualifier("disBeforeCreateRequestOrderSender")
    private MessageSender disBeforeCreateRequestOrderSender;

    /**
     * 查找全流程
     *
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    public List<OrderProcessOut> findProcessAndConfigAndItemByOrderTypeConfigId(Integer orderTypeConfigId, String bizOrgCode) {
        OrderTypeConfig orderTypeConfig = orderTypeConfigService.getOrderTypeConfigByIdAndBizOrgCode(orderTypeConfigId, bizOrgCode);
        if (Objects.isNull(orderTypeConfig)) {
            throw new BusinessException("不存在的订单类型");
        }
        List<OrderProcessOut> orderProcessList = orderProcessService.findProcessListByOrderTypeConfigId(orderTypeConfig, bizOrgCode);
        if (CollectionUtils.isEmpty(orderProcessList)) {
            throw new BusinessException("订单类型" + orderTypeConfig.getOrderTypeName() + "未设置流程");
        }
        return orderProcessList;
    }

    /**
     * 保存流程副本
     *
     * @param orderCycle
     * @param orderProcessOutList
     */
    public void saveOrderProcessCopy(OrdDisOrderCycle orderCycle, List<OrderProcessOut> orderProcessOutList) {
        Integer orderCycleId = orderCycle.getId();
        String storeCode = orderCycle.getStoreCode();
        String bizOrgCode = orderCycle.getBizOrgCode();
        // 查找本订货周期是否已存在副本
        String businessKey = "orderProcessCopy:" + storeCode + ":" + orderCycle.getTruncationDateTime()
                + orderCycle.getShortOrderType();
        String orderProcessCopyValue = redisService.get(businessKey);
        List<OrderProcessCopy> orderProcessCopyList = null;
        if (StringUtils.isNotBlank(orderProcessCopyValue)) {
            orderProcessCopyList = this.findOrderProcessCopyListByParameter(orderCycleId, storeCode, bizOrgCode);
        }
        if (CollectionUtils.isEmpty(orderProcessCopyList)) {
            orderProcessCopyList = Lists.newArrayList();
            for (OrderProcessOut orderProcessOut : orderProcessOutList) {
                try {
                    OrderProcessCopy orderProcessCopy = new OrderProcessCopy();
                    orderProcessCopy.setStoreCode(storeCode);
                    orderProcessCopy.setOrderCycleId(orderCycleId);
                    orderProcessCopy.setProgressCode(orderProcessOut.getProcessCode());
                    orderProcessCopy.setBizOrgCode(bizOrgCode);
                    orderProcessCopy.setSerialNumber(orderProcessOut.getSerialNumber());
                    OrderProcessCopy checkOrderProcessCopy = this.selectOne(orderProcessCopy);
                    if (Objects.isNull(checkOrderProcessCopy)) {
                        orderProcessCopy.setProcessName(orderProcessOut.getProcessName());
                        orderProcessCopy.setShortOrderType(orderCycle.getShortOrderType());
                        orderProcessCopy.setProcessConfigItem(JSON.toJSONString(orderProcessOut.getConfigList()));
                        orderProcessCopy.setCreator(SystemConstant.SYSTEM_USER);
                        orderProcessCopy.setCreateTime(LocalDateTime.now());
                        this.insert(orderProcessCopy);
                    }
                    orderProcessCopyList.add(orderProcessCopy);
                } catch (Exception e) {
                    log.error("保存订单类型流程副本异常:" + e);
                }
            }
            redisService.set(businessKey, orderCycle.getTruncationDateTime() + orderCycle.getShortOrderType(), 3, TimeUnit.DAYS);
            log.info("门店{}保存订单类型流程副本参数：订货周期id-{}", storeCode, orderCycleId);
        }
    }

//    /**
//     * 处理订单创建成功后逻辑(消费创建订单后MQ内调用)
//     *
//     * @param afterOrderCreatedMqOut
//     */
//    public void handleAfterCreateOrder(AfterOrderCreatedMqOut afterOrderCreatedMqOut) {
//        List<OrderIdMessageOut> orderIdMessageOutList = afterOrderCreatedMqOut.getOrderIdMessageOutList();
//        orderIdMessageOutList.stream().forEach(afterOrderCreatedMessageOut -> {
//            OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(afterOrderCreatedMessageOut.getOrderCycleId(), afterOrderCreatedMessageOut.getBizOrgCode());
//            String storeCode = orderCycle.getStoreCode();
//            String bizOrgCode = afterOrderCreatedMessageOut.getBizOrgCode();
//            String orderNo = afterOrderCreatedMessageOut.getOrderNo();
//            // 查找下个流程
//            OrderProcessCopy nextOrderProcessCopy = this.getNextOrderProcessCopyByParameter(orderCycle.getId(), storeCode, afterOrderCreatedMessageOut.getCurrentProgressCode(), bizOrgCode);
//            if (Objects.isNull(nextOrderProcessCopy)) {
//                log.info(orderCycle.getShortOrderType() + "下个流程不存在");
//                throw new BusinessException(orderCycle.getShortOrderType() + "下单流程不完整");
//            }
//            // 待更新状态的订货单
//            OrdDisOrder order = orderHandle.getOrderByIdAndBizOrgCode(afterOrderCreatedMessageOut.getOrderId(), bizOrgCode);
//            String nextLog = "门店" + storeCode + "的订货周期" + orderCycle.getShortOrderType() + "下的订货单" + orderNo + "(" + SourceTypeEnum.getValueByKey(order.getSourceCode()) + ")";
//            // 如果是支付
//            if (OrderCycleProcessCodeEnum.ORDER_PAY.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
//                log.info(nextLog + "需要支付流程，即将流转到支付");
//                this.handlePayProcess(bizOrgCode, order);
//            }
//            // 如果是集货单
//            if (OrderCycleProcessCodeEnum.REQUEST_ORDER_CREATE.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
//                log.info(nextLog + "无支付流程，即将流转到集货单");
//                orderHandle.updateOrderStatusBySubmit(order, SystemConstant.SYSTEM_USER);
//                this.handleRequestOrderProcess(order, orderCycle, SystemConstant.SYSTEM_USER);
//            }
//        });
//    }

    /**
     * 处理集货单流程
     *
     * @param order
     */
    public void handleRequestOrderProcess(OrdDisOrder order, OrdDisOrderCycle orderCycle, String loginUsername) {
        // 获取转单时机配置
        OrderProcessConfigItemOut configItemOut = orderConfigHandle.getRequestOrderCreateOpportunity(order.getOrderCycleId(), order.getBizOrgCode());
        if (Objects.isNull(configItemOut)) {
            log.error("订货单{}的转单时机配置查询为空", order.getOrderNo());
            throw new BusinessException("订货单" + order.getOrderNo() + "的转单时机配置查询为空");
        }
        if (OrderCycleProcessConfigItemCodeEnum.IMMEDIATELY_CREATE_REQUEST_ORDER.getCode().equals(configItemOut.getItemCode())) {
            SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = new SendBeforeCreateRequestOrderMqIn();
            sendBeforeCreateRequestOrderMqIn.setOrderList(Lists.newArrayList(order));
            sendBeforeCreateRequestOrderMqIn.setOrderCycleId(order.getOrderCycleId());
            sendBeforeCreateRequestOrderMqIn.setBizOrgCode(order.getBizOrgCode());
            sendBeforeCreateRequestOrderMqIn.setLoginUsername(loginUsername);
            sendBeforeCreateRequestOrderMqIn.setAuditType(SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode());
            sendBeforeCreateRequestOrderMqIn.setAddPushFlag(false);
            log.info("发送订货单更新状态至可创建集货单前消息{}", JSONObject.toJSONString(sendBeforeCreateRequestOrderMqIn));
//             创建捞单池记录
//            ordDisSalvageDelivPondService.saveDisSalvageDelivPond(orderCycle.getOrderTypeConfigId(), orderCycle.getTruncationDateTime(),
//                    orderCycle.getBizOrgCode(), orderCycle.getOrgCode(), loginUsername);
//            log.info("创建配销订货单订单流立即转集货单---->业务组织代码{}，订单类型ID{}，截单时间{}，配销捞单池记录已创建", orderCycle.getBizOrgCode(), orderCycle.getOrderTypeConfigId(), orderCycle.getTruncationDateTime());
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DISTRIBUTION_TO_REQUEST, JSONObject.toJSONString(sendBeforeCreateRequestOrderMqIn), order.getBizOrgCode(), order.getOrderNo());
            disBeforeCreateRequestOrderSender.sendSync(JSONObject.toJSONString(sendBeforeCreateRequestOrderMqIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
        }
    }

    /**
     * 处理创建集货单前逻辑(消费创建订货单支付成功后或者无需支付的MQ内调用)
     *
     * @param sendBeforeCreateRequestOrderMqIn 发送创建集货单前MQ消息入参对象
     */
    public void handleBeforeCreateRequestOrder(SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn) {
        //  订货单截单创建集货单验重
        OrdDisOrder order = sendBeforeCreateRequestOrderMqIn.getOrderList().get(0);
        boolean noExistFlag = requestOrderHandle.nonExistOrderRequest(order.getId());
        if (noExistFlag) {
            log.info("重复消费-消费订货单状态更新至可创建集货单,本单不处理{}，门店{}", sendBeforeCreateRequestOrderMqIn.getOrderCycleId(), order.getStoreCode());
            return;
        }
        // 创建集货单前校验订货单下商品
        CheckSkuForCreateRequestOrderOut checkSkuForCreateRequestOrderOut = requestOrderHandle.checkOrderSkuForCreateRequestOrder(sendBeforeCreateRequestOrderMqIn.getOrderList());
        log.info("门店{}订货周期id{}创建集货单合法订单{}长度，非法订单{}长度", order.getStoreCode(), order.getOrderCycleId(), checkSkuForCreateRequestOrderOut.getLegalOrderMap().size(), checkSkuForCreateRequestOrderOut.getIllegalOrderMap().size());
        requestOrderHandle.handleCreateRequestOrder(sendBeforeCreateRequestOrderMqIn, checkSkuForCreateRequestOrderOut);
    }


    /**
     * 处理支付流程
     *
     * @param order
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePayProcess(OrdDisOrder order) {
        // 如果支付金额为0
//        if (order.getOrderAmount().compareTo(BigDecimal.ZERO) == 0) {
        // 如果不存在已支付，则更新订货周期中第一笔订单支付时间
//            int count = orderHandle.countDisOrderByOrderCycleId(order.getOrderCycleId(), order.getStoreCode(), OrderStatusEnum.PAID.getKey());
//            if (count == 0) {
//                disOrderCycleHandle.updateFirstOrderTime(order.getOrderCycleId(), LocalDateTime.now(), SystemConstant.SYSTEM_USER);
//            }
//            order.setUpdater(SystemConstant.SYSTEM_USER);
//            String beforeStatusCode = order.getOrderStatusCode();
//            orderHandle.updateOrderStatus(order.getId(), order.getBizOrgCode(), OrderStatusEnum.PAID.getKey(), order.getUpdater());
//            String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE_FOR_NO_NEED_PAY.getKey(), order.getOrderNo(),
//                    OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.PAID.getKey()));
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
//                    OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), order.getUpdater());
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//            log.info("订货订单号：" + order.getOrderNo() + "由于订货额为0， 无需进行支付，状态直接修改为已付款");
//            disOrderPayService.handleStoreDisOrderPay(order.getId(), SystemConstant.SYSTEM_USER);
//        }
//        if (order.getOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
//            String beforeStatusCode = order.getOrderStatusCode();
//            orderHandle.updateOrderStatus(order.getId(), bizOrgCode, OrderStatusEnum.WAIT_PAYMENT.getKey(), SystemConstant.SYSTEM_USER);
//            String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), order.getOrderNo(),
//                    OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.WAIT_PAYMENT.getKey()));
//            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
//                    OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        this.handleAutoPay(order);
    }


    /**
     * 处理支付流程
     *
     * @param order
     */
    private void handleAutoPay(OrdDisOrder order) {
        // 调资管，大于0，普通分货产生订货单和跑货订货单,需自动发起余额支付
        boolean amountFlag = order.getOrderAmount().compareTo(BigDecimal.ZERO) > 0;
        boolean orderFlag = (order.getSourceCode().equals(SourceTypeEnum.DISTRIBUTION.getKey()) && OrderIdentificationEnum.NORMAL_ORDER.getCode().equals(order.getOrderIdentification())
                || order.getSourceCode().equals(SourceTypeEnum.UPLOWDOWN.getKey()));
        if (amountFlag && orderFlag) {
//            RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
//            rechargeLiquidationIn.setPayOrPrincipalCode(order.getStoreCode());
//            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
//            rechargeLiquidationIn.setRecipientPrincipalCode(order.getBizOrgCode());
//            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
//            rechargeLiquidationIn.setBizOrgCode(order.getBizOrgCode());
//            rechargeLiquidationIn.setBusinessNo(order.getOrderNo());
//            rechargeLiquidationIn.setBusinessType(FundTypeEnum.PLACE_ORDER.getCode());
//            rechargeLiquidationIn.setLiquidationAmount(order.getOrderAmount().abs());
//            rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
//            rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
//            Response response = fundServer.settlement(rechargeLiquidationIn);

            StoreFrozenIn storeFrozenIn = new StoreFrozenIn();
            storeFrozenIn.setPrincipalCode(order.getStoreCode());
            storeFrozenIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
            storeFrozenIn.setBizOrgCode(order.getBizOrgCode());
            FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
            frozenOrderIn.setAmount(order.getOrderAmount());
            frozenOrderIn.setBusinessNo(order.getOrderNo());
            frozenOrderIn.setBusinessType(FundTypeEnum.PLACE_ORDER.getCode());
            List<FrozenOrderIn> frozenOrders = Collections.singletonList(frozenOrderIn);
            storeFrozenIn.setFrozenOrders(frozenOrders);
            // 支付改冻结接口
            Response response = fundServer.frozen(storeFrozenIn);
            if (!response.isSuccess()) {
                log.error("订货单{}支付异常：{}", order.getOrderNo(), response.getMessage());
                return;
            }
        }
        disOrderPayService.handleStoreDisOrderPay(order.getId(), SystemConstant.SYSTEM_USER);
    }

    /**
     * 获取指定订单类型指定流程配置
     *
     * @param orderTypeConfigId     订单类型id
     * @param processCode           订单流程代码
     * @param processCodeConfigCode 流程配置代码流程配置代码
     * @param bizOrgCode            业务组织代码
     * @return
     */
    public List<OrderProcessConfigItem> getTheOrderProcessConfigOut(Integer orderTypeConfigId, String processCode, String processCodeConfigCode, String bizOrgCode) {
        OrderTypeConfig orderTypeConfig = orderTypeConfigService.getOrderTypeConfigByIdAndBizOrgCode(orderTypeConfigId, bizOrgCode);
        if (Objects.isNull(orderTypeConfig)) {
            throw new BusinessException("不存在的订单类型");
        }
        List<OrderProcessConfigItem> configItemList = orderProcessConfigItemService.findConfigItemListByParameter(orderTypeConfigId, processCode, processCodeConfigCode, bizOrgCode);
        return configItemList;
    }

    /**
     * 集货单创建成功后消息内部调用
     *
     * @param requestOrderCreateMqIn 处理集货单创建成功消费消息入参
     */
    @Transactional(rollbackFor = Exception.class)
    public Response handleAfterRequestOrderCreated(RequestOrderCreateMqIn requestOrderCreateMqIn, StoreOut storeOut,
                                                   StoreLogisticsOut logistics, Map<String, StockInfoOut> stockMap) {
//        String requestOrderSplitKey = DisSystemConstant.CHECK_DIS_REQUEST_ORDER_SPLIT_KEY + requestOrderCreateMqIn.getBizOrgCode() +
//                SystemConstant.COLON + requestOrderCreateMqIn.getStoreCode() + SystemConstant.COLON + requestOrderCreateMqIn.getRequestOrderNo();
//        if (!redisService.setIfAbsent(requestOrderSplitKey, requestOrderCreateMqIn.getRequestOrderNo(), DisSystemConstant.CHECK_DIS_REQUEST_ORDER_SPLIT_TIME_KEY, TimeUnit.MINUTES)) {
//            log.error("门店{}集货单{}重复拆单", requestOrderCreateMqIn.getStoreCode(), requestOrderCreateMqIn.getRequestOrderNo());
//            throw new BusinessException("门店" + requestOrderCreateMqIn.getStoreCode() + "集货单" + requestOrderCreateMqIn.getRequestOrderNo() + "重复拆单");
//        }
        String processCodes = OrderCycleProcessCodeEnum.DELIVERY_ORDER_CREATE.getCode() + SystemConstant.COMMA + OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String processConfigCodes = OrderCycleProcessConfigCodeEnum.DELIVERY_SPLIT_CONDITION.getCode() + SystemConstant.COMMA + OrderCycleProcessConfigCodeEnum.DELIVERY_ORDER_PRIORITY.getCode();
        Map<String, List<OrderProcessConfigItemOut>> configItemMap = orderProcessCopyService.findConfigItemMapByParam(requestOrderCreateMqIn.getOrderCycleId(), requestOrderCreateMqIn.getStoreCode(),
                requestOrderCreateMqIn.getBizOrgCode(), processCodes, processConfigCodes);

        boolean isEndFlag = !configItemMap.containsKey(OrderCycleProcessConfigCodeEnum.DELIVERY_SPLIT_CONDITION.getCode());
        // 如果终结于集货单，则由集货单发送erp,否则拆分配货单发送
        if (isEndFlag) {
            this.endOfTheRequestOrder(requestOrderCreateMqIn);
            return Response.error("门店：" + requestOrderCreateMqIn.getStoreCode() + "关联订货周期：" + requestOrderCreateMqIn.getOrderCycleId() + "未配置拆分配销业务，单据终结于集货单！");
        }
        // 配货单-拆分配货
        OrdDisDelivRequest requestOrder = requestOrderHandle.getRequestOrderByRequestOrderId(requestOrderCreateMqIn.getRequestOrderId());
        if (!RequestOrderStatusEnum.COLLECTED.getKey().equals(requestOrder.getStatusCode())) {
            log.error("集货单创建成功后重复消费创建配货单{}", requestOrderCreateMqIn.getRequestOrderNo());
            return Response.error(requestOrderCreateMqIn.getRequestOrderNo() + "集货单创建成功后重复消费创建配货单!");
        }
        List<OrdDisDelivery> deliveryOrderList = disDeliveryOrderHandle.createDeliveryOrder(requestOrderCreateMqIn, storeOut, logistics, stockMap, configItemMap);
        if (CollectionUtils.isEmpty(deliveryOrderList)) {
            log.info("集货单{}创建成功后创建配货单为空", requestOrderCreateMqIn.getRequestOrderNo());
            return Response.error("集货单" + requestOrderCreateMqIn.getRequestOrderNo() + "创建成功后创建配货单为空");
        }
        // 中转配销单加推生成采购单业务
        // 天岁接入ERP，不再对接中科接口
        if (requestOrderCreateMqIn.getAddPushFlag()) {
//        if (requestOrderCreateMqIn.getAddPushFlag() && !OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(requestOrderCreateMqIn.getBizOrgCode())) {
            List<OrdDisDelivery> disDeliveries = deliveryOrderList.stream()
                    .filter(item -> DistributionWaysEnum.TRANSFER.getType().equals(item.getDistributionType())).collect(Collectors.toList());
            disDeliveryOrderHandle.transferDeliveryDisOrder(disDeliveries, requestOrderCreateMqIn.getBizOrgCode());
        }
        deliveryOrderList.forEach(deliveryOrder -> {
            String content = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_CREATE.getKey(), deliveryOrder.getDeliveryOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrder.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), deliveryOrder.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        });
        //集货单拆单日志
        String content = MessageFormat.format(RequestOrderStatusEnum.EXCRETED.getValue(), requestOrder.getRequestOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_REQUEST_ORDER.getName(), String.valueOf(requestOrder.getId()),
                OrdLogTypeEnum.DIS_REQUEST_ORDER.getCode(), content, new Date(), requestOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("集货单创建配销单成功");
    }

    /**
     * 终结于集货单
     *
     * @param requestOrderCreateMqIn
     */
    @Transactional(rollbackFor = Exception.class)
    public void endOfTheRequestOrder(RequestOrderCreateMqIn requestOrderCreateMqIn) {
        //终结于集货单创建日志
        String content = MessageFormat.format(OrdLogTypeEnum.END_OF_DIS_REQUEST_ORDER.getName(), requestOrderCreateMqIn.getRequestOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_REQUEST_ORDER.getName(), String.valueOf(requestOrderCreateMqIn.getRequestOrderId()),
                OrdLogTypeEnum.DIS_REQUEST_ORDER.getCode(), content, new Date(), requestOrderCreateMqIn.getLoginUsername());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        List<OrdDisOrder> orderList = orderHandle.findOrderByRequestOrderIdAndBizOrgCode(requestOrderCreateMqIn.getRequestOrderId(), requestOrderCreateMqIn.getBizOrgCode());
        if (CollectionUtils.isEmpty(orderList)) {
            throw new BusinessException("集货单" + requestOrderCreateMqIn.getRequestOrderNo() + "未发现关联订货单");
        }
        BigDecimal totalOrderAmount = orderList.stream().reduce(BigDecimal.ZERO, (x, y) -> x.add(y.getOrderAmount()), BigDecimal::add);
        OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(requestOrderCreateMqIn.getOrderCycleId(), requestOrderCreateMqIn.getBizOrgCode());
        if (Objects.isNull(orderCycle)) {
            throw new BusinessException("集货单" + requestOrderCreateMqIn.getRequestOrderNo() + "关联的订货周期查询为空");
        }
        if (totalOrderAmount.compareTo(orderCycle.getMinimumOrderAmount()) == -1) {
            List<BusinessLog> businessLogList = orderHandle.batchUpdateOrder(orderList, OrderStatusEnum.INVALID.getKey(), SystemConstant.SYSTEM_USER);
            businessLogList.forEach(log -> asyncLogService.sendAsyncSaveLogByMq(log));
            log.info("门店" + orderCycle.getStoreCode() + orderCycle.getShortOrderType() + "订货周期" + orderCycle.getTruncationDateTime() +
                    "下订货单截单时周期累加校验订货额" + totalOrderAmount +
                    "不满起订额" + orderCycle.getMinimumOrderAmount() + "被作废");
        }
    }


}
