package com.edc.erp.distribution.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrderProcessCopy;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.FrozenOrderIn;
import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.service.AppNotificationService;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.distribution.handle.DisOrderDetailHandle;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.service.DisOrderPayService;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.erp.enumeration.*;
import com.edc.erp.enumeration.SourceTypeEnum;
import com.edc.erp.enumeration.*;
import com.edc.erp.orderscheduing.handle.OrderProcessSchedulingHandle;
import com.edc.erp.presale.entity.OrdDisPresaleAssets;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.enumeration.OrdDisPresaleFlowBusinessTypeEnum;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsGoodsIn;
import com.edc.erp.presale.model.in.UpdateDisPresaleAssetsIn;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
import com.edc.erp.presale.service.OrdDisPresaleAssetsService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.plugins.common.utils.SpringUtils;
import com.edc.plugins.redis.RedisService;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DisOrderPayServiceImpl implements DisOrderPayService {

    @Autowired
    private DisOrderHandle disOrderHandle;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private OrdDisOrderTrackService ordDisOrderTrackService;

    @Autowired
    private FundServer fundServer;

    @Autowired
    private AppNotificationService appNotificationService;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private OrderProcessSchedulingHandle orderProcessSchedulingHandle;

    @Autowired
    private DisOrderDetailHandle orderDetailHandle;

    @Autowired
    private OrdDisPresaleAssetsService ordDisPresaleAssetsService;

    @Autowired
    private OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;

//    @Autowired
//    @Qualifier("updateOrderPaidSender")
//    private MessageSender updateOrderPaidSender;

    @Autowired
    private RedisService redisService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDisOrderPaidSuccess(Long id, String operator) {
        OrdDisOrder ordDisOrder = disOrderHandle.selectByPrimaryKey(id);
        if (Objects.isNull(ordDisOrder)) {
            log.error("支付成功更新订货单状态，没有查找到指定待支付订货单ID{}", id);
            return false;
        }
        if (!OrderStatusEnum.WAIT_PAYMENT.getKey().equals(ordDisOrder.getOrderStatusCode())) {
            log.error("支付成功更新订货单状态，订货单{}状态不正确", ordDisOrder.getOrderNo());
            return false;
        }
        // 如果不存在已支付，则更新订货周期中第一笔订单支付时间
        int count = disOrderHandle.countDisOrderByOrderCycleId(ordDisOrder.getOrderCycleId(), ordDisOrder.getStoreCode(), OrderStatusEnum.PAID.getKey());
        if (count == 0) {
            disOrderCycleHandle.updateFirstOrderTime(ordDisOrder.getOrderCycleId(), LocalDateTime.now(), operator);
        }
        String beforeStatusCode = ordDisOrder.getOrderStatusCode();
        if (OrderStatusEnum.PAID.getKey().equals(beforeStatusCode)) {
            return false;
        }
        // 改订货单状态
//        disOrderHandle.updateOrderStatus(ordDisOrder.getId(), ordDisOrder.getBizOrgCode(), OrderStatusEnum.PAID.getKey(), SystemConstant.SYSTEM_USER);
        // 更新订货单支付状态，并标记以冻结
        disOrderHandle.updateOrderStatus(ordDisOrder.getId(), ordDisOrder.getBizOrgCode(), OrderStatusEnum.PAID.getKey(), SystemConstant.SYSTEM_USER, OrderFreezeEnum.FREEZE.getKey());
        String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), ordDisOrder.getOrderNo(),
                OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.PAID.getKey()));
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), ordDisOrder.getId().toString(),
                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), operator);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        // 订单支付推送订单追踪日志
        String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.ORDER_PAID.getTemplate(), ordDisOrder.getOrderNo());
        ordDisOrderTrackService.pushRedisOrderTrackMessage(ordDisOrder.getOrderNo(), ordDisOrder.getStoreCode(),
                OrderTrackStatusEnum.LIST_PAID.getName(), trackLog, ordDisOrder.getBizOrgCode(), ordDisOrder.getCreator(), ordDisOrder.getCreateTime());
        // 站内消息
        appNotificationService.sendPaymentSuccessFulStationMessage(ordDisOrder.getOrderNo(), ordDisOrder.getOrderAmount(), ordDisOrder.getStoreCode());
        // 查找下个流程
        OrdDisOrderCycle ordDisOrderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(ordDisOrder.getOrderCycleId(), ordDisOrder.getBizOrgCode());
        OrderProcessCopy nextOrderProcessCopy = orderProcessSchedulingHandle.getNextOrderProcessCopyByParameter(ordDisOrderCycle.getId(),
                ordDisOrder.getStoreCode(), OrderCycleProcessCodeEnum.ORDER_PAY.getCode(), ordDisOrder.getBizOrgCode());
        if (Objects.isNull(nextOrderProcessCopy)) {
            log.info(ordDisOrderCycle.getShortOrderType() + "下个流程不存在");
//                throw new BusinessException(ordDisOrderCycle.getShortOrderType() + "下单流程不完整");
            return false;
        }
        if (OrderCycleProcessCodeEnum.REQUEST_ORDER_CREATE.getCode().equals(nextOrderProcessCopy.getProgressCode())) {
            orderProcessSchedulingHandle.handleRequestOrderProcess(ordDisOrder, ordDisOrderCycle, SystemConstant.SYSTEM_USER);
        }
        return true;
    }

    @Override
    public Response<String> disOrderPay(List<Long> orderIdList, AppUserOut appUserOut) {
        StringJoiner errorJoiner = new StringJoiner(",");
        StoreFrozenIn storeFrozenIn = new StoreFrozenIn();
        storeFrozenIn.setPrincipalCode(appUserOut.getStoreCode());
        storeFrozenIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
        storeFrozenIn.setBizOrgCode(appUserOut.getBizOrgCode());
        orderIdList.forEach(id -> {
            String key = DisSystemConstant.DIS_ORDER_PAY + appUserOut.getBizOrgCode() + SystemConstant.COLON + id;
            if (!redisService.setIfAbsent(key, id, 1L, TimeUnit.MINUTES)) {
                log.error("配销单ID{}已进入支付处理流程", id);
                return;
            }
            OrdDisOrder order = disOrderHandle.getOrderOutById(id, appUserOut.getBizOrgCode());
            if (Objects.isNull(order)) {
                log.error("配销订单{}不存在", order.getOrderNo());
                return;
            }
            if (!OrderStatusEnum.WAIT_PAYMENT.getKey().equals(order.getOrderStatusCode())) {
                log.error("配销订单{}不需要支付", order.getOrderNo());
                errorJoiner.add(order.getOrderNo() + "不需要支付");
                return;
            }
            if (BigDecimal.ZERO.compareTo(order.getOrderAmount()) == 0) {
                log.error("配销订单{}支付金额为0", order.getOrderNo());
                return;
            }
            if (OrderIdentificationEnum.PRESALE_ORDER.getCode().equals(order.getOrderIdentification())) {
//                UpdateStoreOrderPaidForMqIn updateStoreOrderPaidForMqIn = new UpdateStoreOrderPaidForMqIn();
//                updateStoreOrderPaidForMqIn.setOrderId(id);
//                updateStoreOrderPaidForMqIn.setLoginUsername(appUserOut.getName());
//                SendResponse sendResponse = updateOrderPaidSender.sendSync(JSONObject.toJSONString(updateStoreOrderPaidForMqIn).getBytes());
//                if (Objects.nonNull(sendResponse)) {
//                    log.info("订货单支付成功更新状态发送消息ID----->{}", sendResponse.getMessageId());
//                }
                SpringUtils.getBean(this.getClass()).handleStoreDisOrderPay(id, appUserOut.getName());
            }
            if (OrderIdentificationEnum.NORMAL_ORDER.getCode().equals(order.getOrderIdentification())) {
                this.storeNormalOrderPay(appUserOut, id, order, errorJoiner);
            }
        });
        if (StringUtils.isBlank(errorJoiner.toString())) {
            return Response.success("支付成功");
        } else {
            return Response.error("配销订单:" + errorJoiner);
        }
    }

    private void storeNormalOrderPay(AppUserOut appUserOut, Long id, OrdDisOrder order, StringJoiner errorJoiner) {
//        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
//        rechargeLiquidationIn.setPayOrPrincipalCode(appUserOut.getStoreCode());
//        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
//        rechargeLiquidationIn.setRecipientPrincipalCode(appUserOut.getBizOrgCode());
//        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
//        rechargeLiquidationIn.setBizOrgCode(appUserOut.getBizOrgCode());
//        rechargeLiquidationIn.setBusinessNo(order.getOrderNo());
//        rechargeLiquidationIn.setBusinessType(FundTypeEnum.PLACE_ORDER.getCode());
//        rechargeLiquidationIn.setLiquidationAmount(order.getOrderAmount());
//        rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
//        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
        frozenOrderIn.setAmount(order.getOrderAmount());
        frozenOrderIn.setBusinessNo(order.getOrderNo());
        frozenOrderIn.setBusinessType(FundTypeEnum.PLACE_ORDER.getCode());
        List<FrozenOrderIn> frozenOrders = Collections.singletonList(frozenOrderIn);
        StoreFrozenIn storeFrozenIn = new StoreFrozenIn();
        storeFrozenIn.setPrincipalCode(appUserOut.getStoreCode());
        storeFrozenIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
        storeFrozenIn.setBizOrgCode(appUserOut.getBizOrgCode());
        storeFrozenIn.setFrozenOrders(frozenOrders);
        try {
//            Response response = fundServer.settlement(rechargeLiquidationIn);
            // 支付改冻结接口
            Response response = fundServer.frozen(storeFrozenIn);
            if (!response.isSuccess()) {
//                log.error("配销单{}支付失败:{}", order.getOrderNo(), response.getMessage());
                log.error("配销订货单{}冻结失败:{}", order.getOrderNo(), response.getMessage());
                errorJoiner.add(order.getOrderNo() + response.getMessage());
            } else {
//                disOrderHandle.updateOrderStatus(order.getId(), order.getBizOrgCode(), OrderStatusEnum.PAYING.getKey(), SystemConstant.SYSTEM_USER);
//
//                UpdateStoreOrderPaidForMqIn updateStoreOrderPaidForMqIn = new UpdateStoreOrderPaidForMqIn();
//                updateStoreOrderPaidForMqIn.setOrderId(id);
//                updateStoreOrderPaidForMqIn.setLoginUsername(appUserOut.getName());
//                SendResponse sendResponse = updateOrderPaidSender.sendSync(JSONObject.toJSONString(updateStoreOrderPaidForMqIn).getBytes());
//                if (Objects.nonNull(sendResponse)) {
//                    log.info("订货单支付成功更新状态发送消息ID----->{}", sendResponse.getMessageId());
//                }
//                String beforeStatusCode = order.getOrderStatusCode();
//                String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), order.getOrderNo(),
//                        OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.PAYING.getKey()));
//                BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
//                        OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), appUserOut.getName());
//                asyncLogService.sendAsyncSaveLogByMq(businessLog);
//                this.storePresaleOrderPay(order, appUserOut);
                SpringUtils.getBean(this.getClass()).handleStoreDisOrderPay(id, appUserOut.getName());
            }
        } catch (Exception e) {
            log.error("配销单{}支付异常{}", order.getOrderNo(), e);
        }
    }

//    @Transactional(rollbackFor = Exception.class)
//    public void updateDisOrderListPaidSuccess(List<Long> orderIdList, String loginUsername) {
//        orderIdList.forEach(id -> this.updateDisOrderPaidSuccess(id, loginUsername));
//    }
//    @Transactional(rollbackFor = Exception.class)
//    public void updateDisOrderListPaidSuccess(List<Long> orderIdList, String loginUsername) {
//        orderIdList.forEach(id -> this.updateDisOrderPaidSuccess(id, loginUsername));
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleStoreDisOrderPay(Long orderId, String loginUsername) {
        // 改状态，找订单流下个流程
        boolean updateFlag = this.updateDisOrderPaidSuccess(orderId, loginUsername);
        if (!updateFlag) {
            return;
        }
        OrdDisOrder order = disOrderHandle.selectByPrimaryKey(orderId);
        // 扣资产库存
        if (OrderIdentificationEnum.PRESALE_ORDER.getCode().equals(order.getOrderIdentification())) {
            UpdateDisPresaleAssetsIn updateDisPresaleAssetsIn = new UpdateDisPresaleAssetsIn();
            updateDisPresaleAssetsIn.setBizOrgCode(order.getBizOrgCode());
            updateDisPresaleAssetsIn.setLoginUsername(loginUsername);
            String businessType = null;
            if (SourceTypeEnum.DISTRIBUTION.getKey().equals(order.getSourceCode())) {
                businessType = OrdDisPresaleFlowBusinessTypeEnum.DISTRIBUTION_ORDER.getKey();
            }
            if (SourceTypeEnum.INITIATIVE.getKey().equals(order.getSourceCode())) {
                businessType = OrdDisPresaleFlowBusinessTypeEnum.MANUAL_ORDER.getKey();
            }
            updateDisPresaleAssetsIn.setBusinessType(businessType);
            updateDisPresaleAssetsIn.setSourceNo(order.getOrderNo());
            OrdDisPresaleAssets presaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(order.getStoreCode());
            if (Objects.nonNull(presaleAssets)) {
                updateDisPresaleAssetsIn.setAssetsId(presaleAssets.getId());
            }
            List<OrdDisOrderDetail> orderDetailList = orderDetailHandle.findOrderDetailListByOrderId(order.getId());
            List<UpdateDisPresaleAssetsGoodsIn> assetsGoodsInList = orderDetailList.stream().map(ordDisOrderDetail -> {
                OrdDisPresaleAssetsDetail storeAssetsDetail = ordDisPresaleAssetsDetailService.getStoreAssetsDetail(order.getStoreCode(), ordDisOrderDetail.getGoodsCode(), order.getCreateTime());
                if (Objects.isNull(storeAssetsDetail)) {
                    throw new BusinessException("门店资产商品" + ordDisOrderDetail.getGoodsCode() + "不存在");
                }
                UpdateDisPresaleAssetsGoodsIn updateDisPresaleAssetsGoodsIn = new UpdateDisPresaleAssetsGoodsIn();
                updateDisPresaleAssetsGoodsIn.setPresaleActivityId(storeAssetsDetail.getPresaleActivityId());
                updateDisPresaleAssetsGoodsIn.setPresaleActivityNo(storeAssetsDetail.getPresaleActivityNo());
                updateDisPresaleAssetsGoodsIn.setStoreCode(presaleAssets.getStoreCode());
                updateDisPresaleAssetsGoodsIn.setStoreName(presaleAssets.getStoreName());
                updateDisPresaleAssetsGoodsIn.setGoodsCode(ordDisOrderDetail.getGoodsCode());
                updateDisPresaleAssetsGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
                updateDisPresaleAssetsGoodsIn.setOrderQuantity(ordDisOrderDetail.getQuantity().abs());
                updateDisPresaleAssetsGoodsIn.setUpdateSurplusQuantity(updateDisPresaleAssetsGoodsIn.getOrderQuantity().negate());
                updateDisPresaleAssetsGoodsIn.setPackageSpecificationNum(ordDisOrderDetail.getDistributionSpecificationNum());
                updateDisPresaleAssetsGoodsIn.setLoginUsername(loginUsername);
                updateDisPresaleAssetsGoodsIn.setAssetsId(storeAssetsDetail.getAssetsId());
                return updateDisPresaleAssetsGoodsIn;
            }).collect(Collectors.toList());
            updateDisPresaleAssetsIn.setAssetsGoodsInList(assetsGoodsInList);
            ordDisPresaleAssetsService.updatePresaleAssets(updateDisPresaleAssetsIn);
        }
    }
}
