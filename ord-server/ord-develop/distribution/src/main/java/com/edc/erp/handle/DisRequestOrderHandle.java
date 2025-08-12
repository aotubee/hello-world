package com.edc.erp.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.FundReturnTypeEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.common.service.LogService;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondService;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.disrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.disrequestorder.model.out.CheckSkuForCreateRequestOrderOut;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestService;
import com.edc.erp.disrequestorder.service.impl.OrdDisDelivRequestServiceImpl;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.model.out.AppOrderDetailOut;
import com.edc.erp.distribution.service.OrdDisOrderDetailService;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.erp.enumeration.*;
import com.edc.erp.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.erp.orderscheduing.handle.OrderProcessSchedulingHandle;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 集货单业务处理类
 * @since 2022/10/21 14:33
 */
@Service
@Slf4j
public class DisRequestOrderHandle extends OrdDisDelivRequestServiceImpl {

    @Autowired
    private DisOrderHandle orderHandle;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    @Qualifier("ordDisOrderDetailServiceImpl")
    private OrdDisOrderDetailService orderDetailService;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private DisRequestOrderConfigHandle requestOrderConfigHandle;

    @Autowired
    @Qualifier("ordDisDelivRequestServiceImpl")
    private OrdDisDelivRequestService ordDisDelivRequestService;

    @Autowired
    private OrdDisOrderTrackService ordDisOrderTrackService;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private AsyncPushTaskService asyncPushTaskService;

    @Autowired
    private OrderProcessSchedulingHandle orderProcessSchedulingHandle;
    @Autowired
    @Qualifier("disAfterCreateRequestOrderSender")
    private MessageSender disAfterCreateRequestOrderSender;
    private FundServer fundServer;

    /**
     * 处理创建集货单
     *
     * @param sendBeforeCreateRequestOrderMqIn 发送创建集货单前MQ消息入参对象
     * @param checkSkuForCreateRequestOrderOut 订货单商品创建集货单校验结果对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleCreateRequestOrder(SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn, CheckSkuForCreateRequestOrderOut checkSkuForCreateRequestOrderOut) {
        Integer orderCycleId = sendBeforeCreateRequestOrderMqIn.getOrderCycleId();
        String bizOrgCode = sendBeforeCreateRequestOrderMqIn.getBizOrgCode();
        String loginUsername = sendBeforeCreateRequestOrderMqIn.getLoginUsername();
        Boolean addPushFlag = sendBeforeCreateRequestOrderMqIn.getAddPushFlag();
        OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, bizOrgCode);
        // 查找集货单合并规则
        OrderProcessConfigItemOut orderProcessConfigItemOut = requestOrderConfigHandle.getDataMergingRule(orderCycle.getId(), orderCycle.getBizOrgCode());
        if (Objects.isNull(orderProcessConfigItemOut)) {
            log.info(orderCycle.getStoreCode() + "订货周期副本" + orderCycle.getShortOrderType() + "的" + orderCycle.getTruncationDateTime() + "配置或者配置选项为空");
        }
        String itemCode = orderProcessConfigItemOut.getItemCode();
        Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap = checkSkuForCreateRequestOrderOut.getLegalOrderMap();
        Map<String, String> illegalSkuMap = checkSkuForCreateRequestOrderOut.getIllegalSkuMap();
        if (null != legalOrderMap && legalOrderMap.size() > 0) {
            log.info("门店{}订货周期{}合法商品创建集货单", orderCycle.getStoreCode(), orderCycle.getTruncationDateTime());
            this.createRequestOrder(legalOrderMap, illegalSkuMap, orderCycle, itemCode, loginUsername, addPushFlag,
                    checkSkuForCreateRequestOrderOut.getIllegalSkuAmountMap(), sendBeforeCreateRequestOrderMqIn.getAuditType());
        }
        Map<OrdDisOrder, List<OrdDisOrderDetail>> illegalOrderMap = checkSkuForCreateRequestOrderOut.getIllegalOrderMap();
        if (null != illegalOrderMap && illegalOrderMap.size() > 0) {
            log.info("门店{}订货周期{}非法商品处理", orderCycle.getStoreCode(), orderCycle.getTruncationDateTime());
            List<OrdDisOrder> illegalOrderList = illegalOrderMap.keySet().stream().collect(Collectors.toList());
            orderHandle.changeRequestOrder(illegalOrderList, OrderLogEnum.CREATE_REQUEST_ORDER_ILLEGAL_SKU_END_ORDER.getKey(),
                    SystemConstant.SYSTEM_USER, FundReturnTypeEnum.DIS_ORDER_INVALID.getName(), null);
        }
        // 判断是否需要释放周期内全部订货单金额
        this.unfreezeAllOrder(loginUsername, DateUtils.format(orderCycle.getTruncationDateTime()), legalOrderMap, illegalOrderMap);
    }

    /**
     * 创建集货单
     *
     * @param legalOrderMap
     * @param orderCycle
     * @param itemCode
     * @param loginUsername
     */
    @Transactional(rollbackFor = Exception.class)
    public void createRequestOrder(Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap, Map<String, String> illegalSkuMap, OrdDisOrderCycle orderCycle,
                                   String itemCode, String loginUsername, Boolean addPushFlag, Map<String, BigDecimal> illegalSkuAmountMap, String auditType) {
        // 创建集货单前校验订货单下商品
        OrdDisDelivRequest requestOrder = ordDisDelivRequestService.createRequestOrder(orderCycle, legalOrderMap, itemCode, illegalSkuAmountMap);
        String requestContent = MessageFormat.format(RequestOrderLogEnum.REQUEST_ORDER_CREATE.getKey(), requestOrder.getRequestOrderNo());
        BusinessLog requestOrderLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_REQUEST_ORDER.getName(), requestOrder.getId().toString(),
                OrdLogTypeEnum.DIS_REQUEST_ORDER.getCode(), requestContent, new Date(), SystemConstant.SYSTEM_USER);
        asyncLogService.sendAsyncSaveLogByMq(requestOrderLog);

        //非法sku日志记录
        List<String> skuList = illegalSkuMap.keySet().stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(skuList)) {
            String skus = skuList.stream().collect(Collectors.joining(SystemConstant.COMMA));
            String content = MessageFormat.format(RequestOrderLogEnum.ILLEGAL_SKU_NOT_CREATE_REQUEST.getKey(), skus);
            BusinessLog illegalSkuLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_REQUEST_ORDER.getName(), requestOrder.getId().toString(),
                    OrdLogTypeEnum.DIS_REQUEST_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
            asyncLogService.sendAsyncSaveLogByMq(illegalSkuLog);
        }

        List<OrdDisOrder> orderList = legalOrderMap.keySet().stream().collect(Collectors.toList());
        orderList.forEach(order -> {
            //记录日志
            String orderContent = MessageFormat.format(OrderLogEnum.ORDER_REQUEST_ORDER.getKey(), order.getOrderNo(), requestOrder.getRequestOrderNo());
            BusinessLog orderLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
                    OrdLogTypeEnum.DIS_ORDER.getCode(), orderContent, new Date(), SystemConstant.SYSTEM_USER);

            asyncLogService.sendAsyncSaveLogByMq(orderLog);
            //订单追踪 创建集货单转单物流
            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.TO_REQUEST_ORDER.getTemplate(), order.getOrderNo(), requestOrder.getId().toString(), requestOrder.getRequestOrderNo());
            ordDisOrderTrackService.pushRedisOrderTrackMessage(order.getOrderNo(), order.getStoreCode(), OrderTrackStatusEnum.TO_REQUEST_ORDER.getName(),
                    trackLog, order.getBizOrgCode(), order.getUpdater(), LocalDateTime.now());
        });
        RequestOrderCreateMqIn requestOrderCreateMqIn = new RequestOrderCreateMqIn(orderCycle.getStoreCode(), orderCycle.getId(),
                requestOrder.getId(), orderCycle.getBizOrgCode(), requestOrder.getRequestOrderNo(), loginUsername, addPushFlag, auditType);
        log.info("向调度模块发送集货单创建完成消息{}", JSONObject.toJSONString(requestOrderCreateMqIn));
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_REQUEST_TO_DELIVERY, JSONObject.toJSONString(requestOrderCreateMqIn), orderCycle.getBizOrgCode(), requestOrder.getRequestOrderNo());
        SendResponse sendResponse = disAfterCreateRequestOrderSender.sendSync(JSONObject.toJSONString(requestOrderCreateMqIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
        log.info("集货单生成配销单{}消息ID---{}", requestOrder.getRequestOrderNo(), sendResponse.getMessageId());

    }

    /**
     * 作废未付款订货单
     *
     * @param orderList
     */
    @Transactional(rollbackFor = Exception.class)
    public void invalidNoPayOrderListForCutOrder(List<OrdDisOrder> orderList) {
        // 未付款的订货单
        List<OrdDisOrder> noPayOrderList = orderList.stream().filter(order -> OrderStatusEnum.WAIT_PAYMENT.getKey().equals(order.getOrderStatusCode())).collect(Collectors.toList());
        noPayOrderList.forEach(order -> {
            String beforeStatusCode = order.getOrderStatusCode();
            order.setOrderStatusCode(OrderStatusEnum.INVALID.getKey());
            order.setUpdater(SystemConstant.SYSTEM_USER);
            order.setUpdateTime(LocalDateTime.now());
            int count = orderHandle.updateForTruncationOrderByNoPay(order);
            if (count > 0) {
                String content = MessageFormat.format(OrderLogEnum.CUT_NO_PAY_INVALID.getKey(), order.getOrderNo(),
                        OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.INVALID.getKey()));
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
                        OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
                // 不足起订额整单作废推送订单追踪日志
                String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.CUT_NO_PAY.getTemplate(), order.getOrderNo());
                ordDisOrderTrackService.pushRedisOrderTrackMessage(order.getOrderNo(), order.getStoreCode(),
                        OrderTrackStatusEnum.INVALID_ORDER.getName(), trackLog, order.getBizOrgCode(), order.getCreator(), order.getCreateTime());
            }
        });
    }

    /**
     * 合并要货
     *
     * @param orderCycle
     * @param minAmountRuleCode
     * @param orderList
     * @param loginUsername
     * @param checkRuleFlag
     * @param addPushFlag
     * @param auditType
     */
    @Transactional(rollbackFor = Exception.class)
    public void mergeDisOrder(OrdDisOrderCycle orderCycle, String minAmountRuleCode, List<OrdDisOrder> orderList,
                              String loginUsername, boolean checkRuleFlag, boolean addPushFlag, String auditType) {
        this.invalidNoPayOrderListForCutOrder(orderList);
        List<OrdDisOrder> waitPushOrderList = orderList.stream().filter(order -> OrderStatusEnum.PAID.getKey().equals(order.getOrderStatusCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(waitPushOrderList)) {
            log.info("门店" + orderCycle.getStoreCode() + "订货周期" + orderCycle.getTruncationDateTime() +
                    orderCycle.getShortOrderType() + "没有可处理的已支付配销订货单");
            return;
        }
        if (OrderCycleProcessConfigItemCodeEnum.ORDER_CUMULATIVE.getCode().equals(minAmountRuleCode) && checkRuleFlag) {
            BigDecimal totalOrderAmount = waitPushOrderList.stream().reduce(BigDecimal.ZERO, (x, y) -> x.add(y.getOrderAmount()), BigDecimal::add);
            if (totalOrderAmount.compareTo(orderCycle.getMinimumOrderAmount()) == -1) {
                orderHandle.batchInvalidForCutOrder(waitPushOrderList, OrderStatusEnum.INVALID.getKey(), SystemConstant.SYSTEM_USER);
                log.info("门店" + orderCycle.getStoreCode() + orderCycle.getShortOrderType() + "订货周期" + orderCycle.getTruncationDateTime() +
                        "下订货单截单时周期累加校验订货额" + totalOrderAmount +
                        "不满起订额" + orderCycle.getMinimumOrderAmount() + "被作废");
                return;
            }
        }
        this.sendBeforeCreateRequestOrderMqForCutOrder(waitPushOrderList, orderCycle, loginUsername, addPushFlag, auditType);
    }

    /**
     * 处理截单-发送订货单更新状态至可创建集货单前消息
     *
     * @param orderList
     * @param orderCycle
     * @param loginUsername
     * @param addPushFlag
     * @param auditType
     */
    public void sendBeforeCreateRequestOrderMqForCutOrder(List<OrdDisOrder> orderList, OrdDisOrderCycle orderCycle,
                                                          String loginUsername, Boolean addPushFlag, String auditType) {
        SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn = new SendBeforeCreateRequestOrderMqIn();
        sendBeforeCreateRequestOrderMqIn.setOrderCycleId(orderCycle.getId());
        sendBeforeCreateRequestOrderMqIn.setBizOrgCode(orderCycle.getBizOrgCode());
        sendBeforeCreateRequestOrderMqIn.setOrderList(orderList);
        sendBeforeCreateRequestOrderMqIn.setLoginUsername(loginUsername);
        sendBeforeCreateRequestOrderMqIn.setAddPushFlag(addPushFlag);
        sendBeforeCreateRequestOrderMqIn.setAuditType(auditType);
        log.info("门店" + orderCycle.getStoreCode() + "订货周期" + orderCycle.getTruncationDateTime() +
                orderCycle.getShortOrderType() + "即将发起处理创建集货单");
        orderProcessSchedulingHandle.handleBeforeCreateRequestOrder(sendBeforeCreateRequestOrderMqIn);
    }

    /**
     * 集货单与订货单是否关联
     *
     * @param id
     * @return
     */
    public boolean nonExistOrderRequest(Long id) {
        OrdDisOrder ordDisOrder = orderHandle.selectByPrimaryKey(id);
        if (Objects.nonNull(ordDisOrder) && Objects.nonNull(ordDisOrder.getRequestOrderId())) {
            return true;
        }
        return false;
    }

    /**
     * 创建要货单前检验订货单商品
     *
     * @param orderList
     * @return
     */
    public CheckSkuForCreateRequestOrderOut checkOrderSkuForCreateRequestOrder(List<OrdDisOrder> orderList) {
        // 非法订货单
        Map<OrdDisOrder, List<OrdDisOrderDetail>> illegalOrderMap = new HashMap<>(NumberUtil.INTEGER_TWO);
        // 合法订货单
        Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap = new HashMap<>(NumberUtil.INTEGER_TWO);
        // 非法sku 金额集合
        Map<String, BigDecimal> legalOrderIllegalSkuAmountMap = new HashMap<>(NumberUtil.INTEGER_TWO);
        // 非法sku map
        Map<String, String> illegalSkuMap = new HashMap<>();
        orderList.forEach(order -> {
            OrdDisOrder dbOrder = orderHandle.selectByPrimaryKey(order.getId());
            if (!OrderStatusEnum.PAID.getKey().equals(dbOrder.getOrderStatusCode())) {
                return;
            }
            // 单据非法sku总金额
            BigDecimal orderReturnAmount = BigDecimal.ZERO;
            List<OrdDisOrderDetail> orderDetailList = orderDetailService.findOrderDetailListByOrderId(order.getId());
            Map<String, List<OrdDisOrderDetail>> giftMap = orderDetailList.stream().filter(detail -> NumberUtils.INTEGER_ONE.equals(detail.getIsGift())).collect(Collectors.groupingBy(OrdDisOrderDetail::getBaseGoodsCode));
            Map<String, AppOrderDetailOut> skuOrderDetailMap = new HashMap<>(orderDetailList.size());
            List<String> skuList = new ArrayList<>();
            for (OrdDisOrderDetail detail : orderDetailList) {
                if (NumberUtils.INTEGER_ZERO.equals(detail.getIsGift())) {
                    skuOrderDetailMap.put(detail.getGoodsCode(), getAppOrderDetailOutByCopy(detail));
                }
                if (NumberUtils.INTEGER_ZERO.equals(detail.getIsGift()) && Objects.isNull(illegalSkuMap.get(detail.getGoodsCode()))) {
                    skuList.add(detail.getGoodsCode());
                }
            }
            if (CollectionUtils.isEmpty(skuList)) {
                illegalOrderMap.put(order, null);
                return;
            }
            OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
            orderGoodsIn.setStoreCode(order.getStoreCode());
            orderGoodsIn.setGoodsCodeList(skuList);
            orderGoodsIn.setBizOrgCode(order.getBizOrgCode());
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
            List<String> requestOrderCodeList = orderGoodsServer.findBusinessGoodsCodeList(orderGoodsIn);
            // 查找非法sku
            if (CollectionUtils.isEmpty(requestOrderCodeList)) {
                illegalOrderMap.put(order, null);
                return;
            }
            // 可转要货单待查sku map
            Map<String, String> waitQueryRequestOrderSkuMap = requestOrderCodeList.stream().collect(Collectors.toMap(s -> s, Function.identity()));
            List<AppOrderDetailOut> legalOrderDetailList = Lists.newArrayList();
            for (String sku : skuList) {
                if (null == waitQueryRequestOrderSkuMap.get(sku)) {
                    illegalSkuMap.put(sku, sku);
                    AppOrderDetailOut orderDetailOut = skuOrderDetailMap.get(sku);
                    orderReturnAmount = orderReturnAmount.add(orderDetailOut.getOrderAmount());
                } else {
                    AppOrderDetailOut out = skuOrderDetailMap.get(sku);
                    if (giftMap.containsKey(sku)) {
                        out.setGiftOutList(giftMap.get(sku));
                    }
                    legalOrderDetailList.add(out);
                }
            }
            legalOrderMap.put(order, legalOrderDetailList);
            if (BigDecimal.ZERO.compareTo(orderReturnAmount) != 0) {
                legalOrderIllegalSkuAmountMap.put(order.getOrderNo(), orderReturnAmount);
            }
        });
        CheckSkuForCreateRequestOrderOut checkSkuForCreateRequestOrderOut = new CheckSkuForCreateRequestOrderOut();
        checkSkuForCreateRequestOrderOut.setIllegalOrderMap(illegalOrderMap);
        checkSkuForCreateRequestOrderOut.setLegalOrderMap(legalOrderMap);
        checkSkuForCreateRequestOrderOut.setIllegalSkuMap(illegalSkuMap);
        checkSkuForCreateRequestOrderOut.setIllegalSkuAmountMap(legalOrderIllegalSkuAmountMap);
        return checkSkuForCreateRequestOrderOut;
    }

    private AppOrderDetailOut getAppOrderDetailOutByCopy(OrdDisOrderDetail item) {
        AppOrderDetailOut out = new AppOrderDetailOut();
        BeanUtils.copy(item, out);
        return out;
    }

    /**
     * @Description: 检查是否需要释放周期内订单金额
     * @Author: ZhangYao
     * @Date: 2023/7/20 14:57
     * @param loginUsername:
     * @param truncationDateTime:
     * @param legalOrderMap:
     * @param illegalOrderMap:
     * @return: void
     **/
    private void unfreezeAllOrder(String loginUsername, String truncationDateTime, Map<OrdDisOrder, List<AppOrderDetailOut>> legalOrderMap,
                                  Map<OrdDisOrder, List<OrdDisOrderDetail>> illegalOrderMap) {
        if (null != legalOrderMap && legalOrderMap.size() == 0 && null != illegalOrderMap && illegalOrderMap.size() > 0) {
            illegalOrderMap.keySet().forEach(order -> {
                UnFrozenIn unFrozenIn = new UnFrozenIn();
                unFrozenIn.setUnFrozenBusinessNos(Collections.singletonList(order.getOrderNo()));
                Response response = fundServer.unFrozen(unFrozenIn);
                if (!response.isSuccess()) {
                    String errorMsg = "门店" + order.getStoreCode() + "订货周期" + truncationDateTime + "合并集货中全商品不可用，本单金额调用资管释放冻结异常：" + response.getMessage();
                    log.error(errorMsg);
                    throw new BusinessException(errorMsg);
                } else {
                    String orderContent = MessageFormat.format(OrderLogEnum.UNFREEZE_CHECK_REQUEST_ORDER.getKey(), order.getOrderNo(), order.getOrderAmount());
                    BusinessLog orderLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
                            OrdLogTypeEnum.DIS_ORDER.getCode(), orderContent, new Date(), loginUsername);
                    asyncLogService.sendAsyncSaveLogByMq(orderLog);
                }
            });
        }
    }
}
