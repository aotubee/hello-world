package com.edc.erp.directly.orderscheduing.handle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.directly.dirrequestorder.model.out.CheckSkuForCreateRequestOrderOut;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.model.out.AfterOrderCreatedMqOut;
import com.edc.erp.directly.distribution.model.out.OrderIdMessageOut;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.entity.DirOrderProcessCopy;
import com.edc.erp.directly.entity.DirOrderTypeConfig;
import com.edc.erp.directly.enumeration.OrderCycleProcessCodeEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigCodeEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigItemCodeEnum;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
import com.edc.erp.directly.handle.DirDeliveryOrderHandle;
import com.edc.erp.directly.handle.DirOrderConfigHandle;
import com.edc.erp.directly.handle.DirRequestOrderHandle;
import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.directly.model.out.DirOrderProcessOut;
import com.edc.erp.directly.service.*;
import com.edc.erp.directly.service.impl.DirOrderProcessCopyServiceImpl;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 单据流程handle
 * @since 2022/10/18 16:35
 */
@Service
@Slf4j
public class DirOrderProcessSchedulingHandle extends DirOrderProcessCopyServiceImpl {

    @Autowired
    @Qualifier("dirOrderTypeConfigServiceImpl")
    private DirOrderTypeConfigService dirOrderTypeConfigService;

    @Autowired
    private DirOrderProcessService dirOrderProcessService;

    @Autowired
    private DirOrderProcessConfigService dirOrderProcessConfigService;

    @Autowired
    private DirOrderProcessConfigItemService dirOrderProcessConfigItemService;

    @Autowired
    private DirOrderConfigHandle orderConfigHandle;

    @Autowired
    private RedisService redisService;

    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;

    @Autowired
    private OrderHandle orderHandle;

    @Autowired
    private DirRequestOrderHandle requestOrderHandle;

    @Autowired
    @Qualifier("dirOrderProcessCopyServiceImpl")
    private DirOrderProcessCopyService orderProcessCopyService;

    @Autowired
    private DirDeliveryOrderHandle dirDeliveryOrderHandle;

    @Autowired
    private AsyncPushTaskService asyncPushTaskService;

    @Autowired
    private AsyncLogService asyncLogService;
    @Autowired
    @Qualifier("dirBeforeCreateRequestOrderSender")
    private MessageSender dirBeforeCreateRequestOrderSender;



    /**
     * 查找全流程
     *
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    public List<DirOrderProcessOut> findProcessAndConfigAndItemByOrderTypeConfigId(Integer orderTypeConfigId, String bizOrgCode) {
        DirOrderTypeConfig orderTypeConfig = dirOrderTypeConfigService.getOrderTypeConfigByIdAndBizOrgCode(orderTypeConfigId, bizOrgCode);
        if (Objects.isNull(orderTypeConfig)) {
            throw new BusinessException("不存在的订单类型");
        }
        List<DirOrderProcessOut> orderProcessList = dirOrderProcessService.findProcessListByOrderTypeConfigId(orderTypeConfig, bizOrgCode);
        if (CollectionUtils.isEmpty(orderProcessList)) {
            throw new BusinessException("订单类型" + orderTypeConfig.getOrderTypeName() + "未设置流程");
        }
        return orderProcessList;
    }

    /**
     * 保存流程副本
     *
     * @param orderCycle
     * @param dirOrderProcessOutList
     */
    public void saveOrderProcessCopy(OrdDirOrderCycle orderCycle, List<DirOrderProcessOut> dirOrderProcessOutList) {
        Integer orderCycleId = orderCycle.getId();
        String storeCode = orderCycle.getStoreCode();
        String bizOrgCode = orderCycle.getBizOrgCode();
        // 查找本订货周期是否已存在副本
        String businessKey = "orderProcessCopy:" + storeCode + ":" + orderCycle.getTruncationDateTime()
                + orderCycle.getShortOrderType();
        String orderProcessCopyValue = redisService.get(businessKey);
        List<DirOrderProcessCopy> orderProcessCopyList = null;
        if (StringUtils.isNotBlank(orderProcessCopyValue)) {
            orderProcessCopyList = this.findOrderProcessCopyListByParameter(orderCycleId, storeCode, bizOrgCode);
        }
        if (CollectionUtils.isEmpty(orderProcessCopyList)) {
            orderProcessCopyList = Lists.newArrayList();
            for (DirOrderProcessOut dirOrderProcessOut : dirOrderProcessOutList) {
                try {
                    DirOrderProcessCopy dirOrderProcessCopy = new DirOrderProcessCopy();
                    dirOrderProcessCopy.setStoreCode(storeCode);
                    dirOrderProcessCopy.setOrderCycleId(orderCycleId);
                    dirOrderProcessCopy.setProgressCode(dirOrderProcessOut.getProcessCode());
                    dirOrderProcessCopy.setSerialNumber(dirOrderProcessOut.getSerialNumber());
                    dirOrderProcessCopy.setBizOrgCode(bizOrgCode);
                    DirOrderProcessCopy checkDirOrderProcessCopy = this.selectOne(dirOrderProcessCopy);
                    if (Objects.isNull(checkDirOrderProcessCopy)) {
                        dirOrderProcessCopy.setProcessName(dirOrderProcessOut.getProcessName());
                        dirOrderProcessCopy.setShortOrderType(orderCycle.getShortOrderType());
                        dirOrderProcessCopy.setProcessConfigItem(JSON.toJSONString(dirOrderProcessOut.getConfigList()));
                        dirOrderProcessCopy.setCreator(SystemConstant.SYSTEM_USER);
                        dirOrderProcessCopy.setCreateTime(LocalDateTime.now());
                        this.insert(dirOrderProcessCopy);
                    }
                    orderProcessCopyList.add(dirOrderProcessCopy);
                } catch (Exception e) {
                    log.error("保存订单类型流程副本异常{}", e);
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
//    @Transactional(rollbackFor = Exception.class)
//    public void handleAfterCreateOrder(AfterOrderCreatedMqOut afterOrderCreatedMqOut) {
//        List<OrderIdMessageOut> orderIdMessageOutList = afterOrderCreatedMqOut.getOrderIdMessageOutList();
//        orderIdMessageOutList.stream().forEach(afterOrderCreatedMessageOut -> {
//            OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(afterOrderCreatedMessageOut.getOrderCycleId(), afterOrderCreatedMessageOut.getBizOrgCode());
//            String storeCode = orderCycle.getStoreCode();
//            String bizOrgCode = afterOrderCreatedMessageOut.getBizOrgCode();
//            String orderNo = afterOrderCreatedMessageOut.getOrderNo();
//            // 查找下个流程
//            DirOrderProcessCopy nextOrderProcessCopy = this.getNextOrderProcessCopyByParameter(orderCycle.getId(), storeCode, afterOrderCreatedMessageOut.getCurrentProgressCode(), bizOrgCode);
//            if (Objects.isNull(nextOrderProcessCopy)) {
//                log.info(orderCycle.getShortOrderType() + "下个流程不存在");
//                throw new BusinessException(orderCycle.getShortOrderType() + "下单流程不完整");
//            }
//            // 待更新状态的订货单
//            OrdDirOrder order = orderHandle.getOrderByIdAndBizOrgCode(afterOrderCreatedMessageOut.getOrderId(), bizOrgCode);
//            String nextLog = "门店" + storeCode + "的订货周期" + orderCycle.getShortOrderType() + "下的订货单" + orderNo + "(" + SourceTypeEnum.getValueByKey(order.getSourceCode()) + ")";
//            // 如果是支付
//            if (OrderCycleProcessCodeEnum.ORDER_PAY.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
//                log.info(nextLog + "需要支付流程，即将流转到支付");
//                this.handlePayProcess(bizOrgCode, order);
//            }
//            // 如果是要货单
//            if (OrderCycleProcessCodeEnum.REQUEST_ORDER_CREATE.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
//                log.info(nextLog + "无支付流程，即将流转到要货单");
//                orderHandle.updateOrderStatusBySubmit(order, SystemConstant.SYSTEM_USER);
//                this.handleRequestOrderProcess(order, orderCycle, SystemConstant.SYSTEM_USER);
//            }
//        });
//    }

    /**
     * 处理要货单流程
     *
     * @param order
     */
    public void handleRequestOrderProcess(OrdDirOrder order, OrdDirOrderCycle orderCycle, String loginUsername) {
        // 获取转单时机配置
        DirOrderProcessConfigItem configItem = orderConfigHandle.getRequestOrderCreateOpportunity(order.getOrderCycleId(), order.getBizOrgCode());
        if (Objects.isNull(configItem)) {
            log.error("订货单{}的转单时机配置查询为空", order.getOrderNo());
            throw new BusinessException("订货单" + order.getOrderNo() + "的转单时机配置查询为空");
        }
        if (OrderCycleProcessConfigItemCodeEnum.IMMEDIATELY_CREATE_REQUEST_ORDER.getCode().equals(configItem.getItemCode())) {
            SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = new SendBeforeCreateRequestOrderMqIn();
            sendBeforeCreateRequestOrderMqIn.setOrderList(Lists.newArrayList(order));
            sendBeforeCreateRequestOrderMqIn.setOrderCycleId(order.getOrderCycleId());
            sendBeforeCreateRequestOrderMqIn.setBizOrgCode(order.getBizOrgCode());
            sendBeforeCreateRequestOrderMqIn.setLoginUsername(loginUsername);
            sendBeforeCreateRequestOrderMqIn.setAuditType(SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode());
            sendBeforeCreateRequestOrderMqIn.setAddPushFlag(false);
            log.info("发送订货单更新状态至可创建要货单前消息{}", JSONObject.toJSONString(sendBeforeCreateRequestOrderMqIn));
//            // 创建捞单池记录
//            ordDirSalvageDelivPondService.saveDirSalvageDelivPond(orderCycle.getOrderTypeConfigId(), orderCycle.getTruncationDateTime(),
//                    orderCycle.getBizOrgCode(), orderCycle.getOrgCode(), loginUsername);
//            log.info("创建直营订货单订单流立即转要货单---->业务组织代码{}，订单类型ID{}，截单时间{}，配货捞单池记录已创建", orderCycle.getBizOrgCode(), orderCycle.getOrderTypeConfigId(), orderCycle.getTruncationDateTime());
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_DISTRIBUTION_TO_REQUEST, JSONObject.toJSONString(sendBeforeCreateRequestOrderMqIn), order.getBizOrgCode(), order.getOrderNo());
            dirBeforeCreateRequestOrderSender.sendSync(JSONObject.toJSONString(sendBeforeCreateRequestOrderMqIn).getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_TIME);
        }
    }

    /**
     * 处理创建要货单前逻辑(消费创建订货单支付成功后或者无需支付的MQ内调用)
     *
     * @param sendBeforeCreateRequestOrderMqIn 发送创建要货单前MQ消息入参对象
     */
    public void handleBeforeCreateRequestOrder(SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn) {
        //  订货单截单创建要货单验重
        OrdDirOrder order = sendBeforeCreateRequestOrderMqIn.getOrderList().get(0);
        boolean noExistFlag = requestOrderHandle.nonExistOrderRequest(order.getId());
        if (noExistFlag) {
            log.info("重复消费-消费订货单状态更新至可创建要货单,本单不处理{}，门店{}", sendBeforeCreateRequestOrderMqIn.getOrderCycleId(), order.getStoreCode());
            return;
        }
        // 创建要货单前校验订货单下商品
        CheckSkuForCreateRequestOrderOut checkSkuForCreateRequestOrderOut = requestOrderHandle.checkOrderSkuForCreateRequestOrder(sendBeforeCreateRequestOrderMqIn.getOrderList());
        log.info("门店{}订货周期id{}创建要货单合法订单{}长度，非法订单{}长度", order.getStoreCode(), order.getOrderCycleId(), checkSkuForCreateRequestOrderOut.getLegalOrderMap().size(), checkSkuForCreateRequestOrderOut.getIllegalOrderMap().size());
        requestOrderHandle.handleCreateRequestOrder(sendBeforeCreateRequestOrderMqIn, checkSkuForCreateRequestOrderOut);
    }


    /**
     * 处理支付流程
     *
     * @param bizOrgCode
     * @param order
     */
    private void handlePayProcess(String bizOrgCode, OrdDirOrder order) {

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
    public List<DirOrderProcessConfigItem> getTheOrderProcessConfigOut(Integer orderTypeConfigId, String processCode, String processCodeConfigCode, String bizOrgCode) {
        DirOrderTypeConfig orderTypeConfig = dirOrderTypeConfigService.getOrderTypeConfigByIdAndBizOrgCode(orderTypeConfigId, bizOrgCode);
        if (Objects.isNull(orderTypeConfig)) {
            throw new BusinessException("不存在的订单类型");
        }
        List<DirOrderProcessConfigItem> configItemList = dirOrderProcessConfigItemService.findConfigItemListByParameter(orderTypeConfigId, processCode, processCodeConfigCode, bizOrgCode);
        return configItemList;
    }

    /**
     * 要货单创建成功后消息内部调用
     *
     * @param requestOrderCreateMqIn 处理要货单创建成功消费消息入参
     */
    @Transactional(rollbackFor = Exception.class)
    public Response<String> handleAfterRequestOrderCreated(RequestOrderCreateMqIn requestOrderCreateMqIn, StoreOut storeOut,
                                                   StoreLogisticsOut logistics, Map<String, StockInfoOut> stockMap) {
//        String requestOrderSplitKey = DirSystemConstant.CHECK_DIR_REQUEST_ORDER_SPLIT_KEY + requestOrderCreateMqIn.getBizOrgCode() +
//                SystemConstant.COLON + requestOrderCreateMqIn.getStoreCode() + SystemConstant.COLON + requestOrderCreateMqIn.getRequestOrderNo();
//        if (!redisService.setIfAbsent(requestOrderSplitKey, requestOrderCreateMqIn.getRequestOrderNo(), DirSystemConstant.CHECK_DIR_REQUEST_ORDER_SPLIT_TIME_KEY, TimeUnit.MINUTES)) {
//            log.error("门店{}要货单{}重复拆单", requestOrderCreateMqIn.getStoreCode(), requestOrderCreateMqIn.getRequestOrderNo());
////            throw new BusinessException("门店" + requestOrderCreateMqIn.getStoreCode() + "要货单" + requestOrderCreateMqIn.getRequestOrderNo() + "重复拆单");
//            return Response.success();
//        }
        String processCodes = OrderCycleProcessCodeEnum.DELIVERY_ORDER_CREATE.getCode() + SystemConstant.COMMA + OrderCycleProcessCodeEnum.ORDER_CREATE.getCode();
        String processConfigCodes = OrderCycleProcessConfigCodeEnum.DELIVERY_SPLIT_CONDITION.getCode() + SystemConstant.COMMA + OrderCycleProcessConfigCodeEnum.DELIVERY_ORDER_PRIORITY.getCode();
        Map<String, List<OrderProcessConfigItemOut>> configItemMap = orderProcessCopyService.findConfigItemMapByParam(requestOrderCreateMqIn.getOrderCycleId(), requestOrderCreateMqIn.getStoreCode(),
                requestOrderCreateMqIn.getBizOrgCode(), processCodes, processConfigCodes);

        boolean isEndFlag = !configItemMap.containsKey(OrderCycleProcessConfigCodeEnum.DELIVERY_SPLIT_CONDITION.getCode());
        // 如果终结于要货单，则由要货单发送erp,否则拆分配货单发送
        if (isEndFlag) {
            this.endOfTheRequestOrder(requestOrderCreateMqIn);
            return Response.success("门店：" + requestOrderCreateMqIn.getStoreCode() + "关联订货周期：" + requestOrderCreateMqIn.getOrderCycleId() + "未配置拆分配销业务，单据终结于要货单！");
        }
        // 配货单-拆分配货
        OrdDirDelivRequest requestOrder = requestOrderHandle.getRequestOrderByRequestOrderId(requestOrderCreateMqIn.getRequestOrderId());
        if (!RequestOrderStatusEnum.COLLECTED.getKey().equals(requestOrder.getStatusCode())) {
            log.error("要货单创建成功后重复消费创建配货单{}", requestOrderCreateMqIn.getRequestOrderNo());
            return Response.success(requestOrderCreateMqIn.getRequestOrderNo() + "要货单创建成功后重复消费创建配货单!");
        }
        List<OrdDirDelivery> deliveryOrderList = dirDeliveryOrderHandle.createDeliveryOrder(requestOrderCreateMqIn, storeOut, logistics, stockMap, configItemMap);
        if (CollectionUtils.isEmpty(deliveryOrderList)) {
            log.info("要货单{}创建成功后创建配货单为空", requestOrderCreateMqIn.getRequestOrderNo());
//            return Response.error("要货单" + requestOrderCreateMqIn.getRequestOrderNo() + "创建成功后创建配货单为空");
            throw new BusinessException("要货单" + requestOrderCreateMqIn.getRequestOrderNo() + "创建成功后创建配货单为空");
        }
        // 中转配销单加推生成采购单业务
        if (requestOrderCreateMqIn.getAddPushFlag()) {
            List<OrdDirDelivery> dirDeliveries = deliveryOrderList.stream()
                    .filter(item -> DistributionWaysEnum.TRANSFER.getType().equals(item.getDistributionType())).collect(Collectors.toList());
            dirDeliveryOrderHandle.transferDeliveryDirOrder(dirDeliveries, requestOrderCreateMqIn.getBizOrgCode());
        }
        deliveryOrderList.forEach(deliveryOrder -> {
            String content = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_CREATE.getKey(), deliveryOrder.getDeliveryOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(deliveryOrder.getId()),
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), content, new Date(), deliveryOrder.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        });
        //要货单拆单日志
        String content = MessageFormat.format(RequestOrderStatusEnum.EXCRETED.getValue(), requestOrder.getRequestOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_REQUEST_ORDER.getName(), String.valueOf(requestOrder.getId()),
                OrdLogTypeEnum.DIR_REQUEST_ORDER.getCode(), content, new Date(), requestOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("要货单创建配货单成功");
    }

    /**
     * 终结于要货单
     *
     * @param requestOrderCreateMqIn
     */
    @Transactional(rollbackFor = Exception.class)
    public void endOfTheRequestOrder(RequestOrderCreateMqIn requestOrderCreateMqIn) {
        //终结于要货单创建日志
        String content = MessageFormat.format(OrdLogTypeEnum.END_OF_DIR_REQUEST_ORDER.getName(), requestOrderCreateMqIn.getRequestOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_REQUEST_ORDER.getName(), String.valueOf(requestOrderCreateMqIn.getRequestOrderId()),
                OrdLogTypeEnum.DIR_REQUEST_ORDER.getCode(), content, new Date(), requestOrderCreateMqIn.getLoginUsername());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        List<OrdDirOrder> orderList = orderHandle.findOrderByRequestOrderIdAndBizOrgCode(requestOrderCreateMqIn.getRequestOrderId(), requestOrderCreateMqIn.getBizOrgCode());
        if (CollectionUtils.isEmpty(orderList)) {
            throw new BusinessException("要货单" + requestOrderCreateMqIn.getRequestOrderNo() + "未发现关联订货单");
        }
        BigDecimal totalOrderAmount = orderList.stream().reduce(BigDecimal.ZERO, (x, y) -> x.add(y.getOrderAmount()), BigDecimal::add);
        OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(requestOrderCreateMqIn.getOrderCycleId(), requestOrderCreateMqIn.getBizOrgCode());
        if (Objects.isNull(orderCycle)) {
            throw new BusinessException("要货单" + requestOrderCreateMqIn.getRequestOrderNo() + "关联的订货周期查询为空");
        }
        if (totalOrderAmount.compareTo(orderCycle.getMinimumOrderAmount()) == -1) {
            orderHandle.batchUpdateOrder(orderList, OrderStatusEnum.INVALID.getKey(), SystemConstant.SYSTEM_USER);
            log.info("门店" + orderCycle.getStoreCode() + orderCycle.getShortOrderType() + "订货周期" + orderCycle.getTruncationDateTime() +
                    "下订货单截单时周期累加校验订货额" + totalOrderAmount +
                    "不满起订额" + orderCycle.getMinimumOrderAmount() + "被作废");
        }
    }
}
