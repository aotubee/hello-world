package com.edc.erp.directly.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.BusinessTypeColumnEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.dirrequestorder.model.in.RequestOrderCreateMqIn;
import com.edc.erp.directly.dirrequestorder.model.out.CheckSkuForCreateRequestOrderOut;
import com.edc.erp.directly.dirrequestorder.service.OrdDirDelivRequestService;
import com.edc.erp.directly.dirrequestorder.service.impl.OrdDirDelivRequestServiceImpl;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.model.out.AppOrderDetailOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderDetailService;
import com.edc.erp.directly.distribution.service.OrdDirOrderTrackService;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.enumeration.*;
import com.edc.erp.directly.model.in.SendBeforeCreateRequestOrderMqIn;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
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
 * 要货单业务处理类
 *
 * @author fxw
 * @since 2022/10/21 14:33
 */
@Service
@Slf4j
public class DirRequestOrderHandle extends OrdDirDelivRequestServiceImpl {

    @Autowired
    private OrderHandle orderHandle;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    @Qualifier("ordDirOrderDetailServiceImpl")
    private OrdDirOrderDetailService orderDetailService;

    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;

    @Autowired
    private DirRequestOrderConfigHandle requestOrderConfigHandle;

    @Autowired
    @Qualifier("ordDirDelivRequestServiceImpl")
    private OrdDirDelivRequestService ordDirDelivRequestService;

    @Autowired
    private OrdDirOrderTrackService ordDirOrderTrackService;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private AsyncPushTaskService asyncPushTaskService;
    @Autowired
    @Qualifier("dirAfterCreateRequestOrderSender")
    private MessageSender dirAfterCreateRequestOrderSender;


    /**
     * 处理创建要货单
     *
     * @param sendBeforeCreateRequestOrderMqIn 发送创建要货单前MQ消息入参对象
     * @param checkSkuForCreateRequestOrderOut 订货单商品创建要货单校验结果对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleCreateRequestOrder(SendBeforeCreateRequestOrderMqIn sendBeforeCreateRequestOrderMqIn, CheckSkuForCreateRequestOrderOut checkSkuForCreateRequestOrderOut) {
        Integer orderCycleId = sendBeforeCreateRequestOrderMqIn.getOrderCycleId();
        String bizOrgCode = sendBeforeCreateRequestOrderMqIn.getBizOrgCode();
        String loginUsername = sendBeforeCreateRequestOrderMqIn.getLoginUsername();
        Boolean addPushFlag = sendBeforeCreateRequestOrderMqIn.getAddPushFlag();
        OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, bizOrgCode);
        // 查找要货单合并规则
        DirOrderProcessConfigItem dirOrderProcessConfigItem = requestOrderConfigHandle.getDataMergingRule(orderCycle.getId(), orderCycle.getBizOrgCode());
        if (Objects.isNull(dirOrderProcessConfigItem)) {
            log.info(orderCycle.getStoreCode() + "订货周期副本" + orderCycle.getShortOrderType() + "的" + orderCycle.getTruncationDateTime() + "配置或者配置选项为空");
        }
        String itemCode = dirOrderProcessConfigItem.getItemCode();
        Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap = checkSkuForCreateRequestOrderOut.getLegalOrderMap();
        Map<String, String> illegalSkuMap = checkSkuForCreateRequestOrderOut.getIllegalSkuMap();
        if (null != legalOrderMap && legalOrderMap.size() > 0) {
            log.info("门店{}订货周期{}合法商品创建要货单", orderCycle.getStoreCode(), orderCycle.getTruncationDateTime());
            this.createRequestOrder(legalOrderMap, illegalSkuMap, orderCycle, itemCode, loginUsername, addPushFlag, sendBeforeCreateRequestOrderMqIn.getAuditType());
        }
        Map<OrdDirOrder, List<OrdDirOrderDetail>> illegalOrderMap = checkSkuForCreateRequestOrderOut.getIllegalOrderMap();
        if (null != illegalOrderMap && illegalOrderMap.size() > 0) {
            log.info("门店{}订货周期{}非法商品处理", orderCycle.getStoreCode(), orderCycle.getTruncationDateTime());
            List<OrdDirOrder> illegalOrderList = illegalOrderMap.keySet().stream().collect(Collectors.toList());
            orderHandle.changeRequestOrder(illegalOrderList, OrderLogEnum.CREATE_REQUEST_ORDER_ILLEGAL_SKU_END_ORDER.getKey(), SystemConstant.SYSTEM_USER);
        }
    }

    /**
     * 创建要货单
     *
     * @param legalOrderMap
     * @param orderCycle
     * @param itemCode
     * @param loginUsername
     * @param addPushFlag
     * @param auditType
     */
    @Transactional(rollbackFor = Exception.class)
    public void createRequestOrder(Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap, Map<String, String> illegalSkuMap,
                                   OrdDirOrderCycle orderCycle, String itemCode, String loginUsername, Boolean addPushFlag, String auditType) {
        // 创建要货单前校验订货单下商品
        OrdDirDelivRequest requestOrder = ordDirDelivRequestService.createRequestOrder(orderCycle, legalOrderMap, itemCode);
        String requestContent = MessageFormat.format(RequestOrderLogEnum.REQUEST_DIR_ORDER_CREATE.getKey(), requestOrder.getRequestOrderNo());
        BusinessLog requestOrderLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_REQUEST_ORDER.getName(), requestOrder.getId().toString(),
                OrdLogTypeEnum.DIR_REQUEST_ORDER.getCode(), requestContent, new Date(), SystemConstant.SYSTEM_USER);
        asyncLogService.sendAsyncSaveLogByMq(requestOrderLog);

        //非法sku日志记录
        List<String> skuList = illegalSkuMap.keySet().stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(skuList)) {
            String skus = skuList.stream().collect(Collectors.joining(SystemConstant.COMMA));
            String content = MessageFormat.format(RequestOrderLogEnum.ILLEGAL_SKU_DIR_NOT_CREATE_REQUEST.getKey(), skus);
            BusinessLog illegalSkuLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_REQUEST_ORDER.getName(), requestOrder.getId().toString(),
                    OrdLogTypeEnum.DIR_REQUEST_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
            asyncLogService.sendAsyncSaveLogByMq(illegalSkuLog);
        }

        List<OrdDirOrder> orderList = legalOrderMap.keySet().stream().collect(Collectors.toList());
        orderList.forEach(order -> {
            //记录日志
            String orderContent = MessageFormat.format(OrderLogEnum.ORDER_REQUEST_ORDER.getKey(), order.getOrderNo(), requestOrder.getRequestOrderNo());
            BusinessLog orderLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), order.getId().toString(),
                    OrdLogTypeEnum.DIR_ORDER.getCode(), orderContent, new Date(), SystemConstant.SYSTEM_USER);

            asyncLogService.sendAsyncSaveLogByMq(orderLog);
            //订单追踪 创建要货单转单物流
            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.TO_REQUEST_ORDER.getTemplate(), order.getOrderNo(), requestOrder.getId().toString(), requestOrder.getRequestOrderNo());
            ordDirOrderTrackService.pushRedisOrderTrackMessage(order.getOrderNo(), order.getStoreCode(), OrderTrackStatusEnum.TO_REQUEST_ORDER.getName(),
                    trackLog, order.getBizOrgCode(), order.getUpdater(), LocalDateTime.now());
        });
        RequestOrderCreateMqIn requestOrderCreateMqIn = new RequestOrderCreateMqIn(orderCycle.getStoreCode(), orderCycle.getId(),
                requestOrder.getId(), orderCycle.getBizOrgCode(), requestOrder.getRequestOrderNo(), loginUsername, addPushFlag, auditType);
        log.info("向调度模块发送要货单创建完成消息{}", JSONObject.toJSONString(requestOrderCreateMqIn));
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_REQUEST_TO_DELIVERY, JSONObject.toJSONString(requestOrderCreateMqIn), orderCycle.getBizOrgCode(), requestOrder.getRequestOrderNo());
        SendResponse sendResponse = dirAfterCreateRequestOrderSender.sendSync(JSONObject.toJSONString(requestOrderCreateMqIn).getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_TIME);
        log.info("要货单生成配货单{}消息ID---{}", requestOrder.getRequestOrderNo(), sendResponse.getMessageId());
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
    public void mergeDirOrder(OrdDirOrderCycle orderCycle, String minAmountRuleCode, List<OrdDirOrder> orderList, String loginUsername,
                              boolean checkRuleFlag, boolean addPushFlag, String auditType) {
        List<OrdDirOrder> waitPushOrderList = orderList.stream().filter(order -> OrderStatusEnum.SUBMIT.getKey().equals(order.getOrderStatusCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(waitPushOrderList)) {
            log.info("门店" + orderCycle.getStoreCode() + "订货周期" + orderCycle.getTruncationDateTime() +
                    orderCycle.getShortOrderType() + "没有可处理的已提交订货单");
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
     * 要货单与订货单是否关联
     *
     * @param id
     * @return
     */
    public boolean nonExistOrderRequest(Long id) {
        OrdDirOrder ordDisOrder = orderHandle.selectByPrimaryKey(id);
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
    public CheckSkuForCreateRequestOrderOut checkOrderSkuForCreateRequestOrder(List<OrdDirOrder> orderList) {
        // 非法订货单
        Map<OrdDirOrder, List<OrdDirOrderDetail>> illegalOrderMap = new HashMap<>();
        // 合法订货单
        Map<OrdDirOrder, List<AppOrderDetailOut>> legalOrderMap = new HashMap<>();
        // 非法sku map
        Map<String, String> illegalSkuMap = new HashMap<>();
        orderList.forEach(order -> {
            OrdDirOrder dbOrder = orderHandle.selectByPrimaryKey(order.getId());
            if (!OrderStatusEnum.SUBMIT.getKey().equals(dbOrder.getOrderStatusCode())) {
                return;
            }
            List<OrdDirOrderDetail> orderDetailList = orderDetailService.findOrderDetailListByOrderId(order.getId());
            Map<String, List<OrdDirOrderDetail>> giftMap = orderDetailList.stream().filter(detail -> NumberUtils.INTEGER_ONE.equals(detail.getIsGift())).collect(Collectors.groupingBy(OrdDirOrderDetail::getBaseGoodsCode));
            Map<String, AppOrderDetailOut> skuOrderDetailMap = new HashMap<>();
            List<String> skuList = new ArrayList<>();
            for (OrdDirOrderDetail detail : orderDetailList) {
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
            orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION_BILL.getType());
            List<String> goodsCodeList = orderGoodsServer.findBusinessGoodsCodeList(orderGoodsIn);
            // 查找非法sku
            if (CollectionUtils.isEmpty(goodsCodeList)) {
                illegalOrderMap.put(order, null);
                return;
            }
            // 可转要货单待查sku map
            Map<String, String> waitQueryRequestOrderSkuMap = goodsCodeList.stream().collect(Collectors.toMap(s -> s, Function.identity()));
            List<AppOrderDetailOut> legalOrderDetailList = Lists.newArrayList();
            skuList.forEach(sku -> {
                if (null == waitQueryRequestOrderSkuMap.get(sku)) {
                    illegalSkuMap.put(sku, sku);
                } else {
                    AppOrderDetailOut out = skuOrderDetailMap.get(sku);
                    if (giftMap.containsKey(sku)) {
                        out.setGiftOutList(giftMap.get(sku));
                    }
                    legalOrderDetailList.add(out);
                }
            });
            legalOrderMap.put(order, legalOrderDetailList);
        });
        CheckSkuForCreateRequestOrderOut checkSkuForCreateRequestOrderOut = new CheckSkuForCreateRequestOrderOut();
        checkSkuForCreateRequestOrderOut.setIllegalOrderMap(illegalOrderMap);
        checkSkuForCreateRequestOrderOut.setLegalOrderMap(legalOrderMap);
        checkSkuForCreateRequestOrderOut.setIllegalSkuMap(illegalSkuMap);
        return checkSkuForCreateRequestOrderOut;
    }

    private AppOrderDetailOut getAppOrderDetailOutByCopy(OrdDirOrderDetail item) {
        AppOrderDetailOut out = new AppOrderDetailOut();
        BeanUtils.copy(item, out);
        return out;
    }
}
