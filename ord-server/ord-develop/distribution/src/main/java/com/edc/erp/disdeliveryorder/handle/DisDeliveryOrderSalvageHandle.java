package com.edc.erp.disdeliveryorder.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.FrozenOrderIn;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPondDetail;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryMapper;
import com.edc.erp.disdeliveryorder.model.in.UpdateStockDisDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.out.OrdDisDeliveryOut;
import com.edc.erp.disdeliveryorder.model.out.OrdDisSalvageDelivPondDetailOut;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryPayService;
import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondDetailService;
import com.edc.erp.reducestock.model.out.stock.OperationStockOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.sdk.dictionary.service.SystemDictService;
import com.edc.sdk.dts.model.order.in.UnificationBillDtlIn;
import com.edc.sdk.dts.model.order.in.UnificationBillIn;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisDeliveryOrderSalvageHandle {

    private final StoreCenterService storeCenterService;

    private final AsyncLogService asyncLogService;

    private final OrdDisDeliveryMapper ordDisDeliveryMapper;

    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    private final OrdDisSalvageDelivPondDetailService ordDisSalvageDelivPondDetailService;

    private final FundServer fundServer;

    private final UniqueUtils uniqueUtils;

    private final AsyncPushTaskService asyncPushTaskService;

    private final SystemDictService systemDictService;

    private final StockServer stockServer;

    private final OrderGoodsServer orderGoodsServer;

    private final DisDeliveryInvalidUnFreezeHandle disDeliveryInvalidUnFreezeHandle;

    @Qualifier("disDeliveryToDtsSender")
    private final MessageSender disDeliveryToDtsSender;

    private final OrdDisDeliveryPayService ordDisDeliveryPayService;

    @Transactional(rollbackFor = Exception.class)
    public int handleSalvageDelivPondDetailList(String bizOrgCode, String loginUsername, Boolean isRecalculateOccupancyQty,
                                                List<OrdDisSalvageDelivPondDetailOut> ordDisSalvageDelivPondDetailOutList) {
        // 处理捞单,并占用库存
        List<OperationStockOut> operationStockOutList = ordDisSalvageDelivPondDetailService.handleSalvage(bizOrgCode, loginUsername, isRecalculateOccupancyQty, ordDisSalvageDelivPondDetailOutList);
        if (CollectionUtils.isEmpty(operationStockOutList)) {
            return 0;
        }
        // 封装配货单占库存后,更新配货单明细数据
        List<UpdateStockDisDeliveryOrderIn> updateStockDisDeliveryOrderInList = ordDisSalvageDelivPondDetailService.initUpdateStockDeliveryOrderInList(operationStockOutList, loginUsername, bizOrgCode);
        Integer successTotal = this.updateAfterHandleOccupyInventory(updateStockDisDeliveryOrderInList);
        return successTotal;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer updateAfterHandleOccupyInventory(List<UpdateStockDisDeliveryOrderIn> updateStockDisDeliveryOrderInList) {
        AtomicInteger successTotal = new AtomicInteger();
        updateStockDisDeliveryOrderInList.forEach(updateStockDeliveryOrderIn -> {
            OrdDisDelivery ordDisDelivery = updateStockDeliveryOrderIn.getOrdDisDelivery();
            String stockLog = StringUtils.isBlank(updateStockDeliveryOrderIn.getStockOutLog()) ? "成功" : updateStockDeliveryOrderIn.getStockOutLog();
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDisDelivery.getStoreCode());
            // 更新配货单状态和明细的审核数
            OrdDisDeliveryOut ordDisDeliveryOut = new OrdDisDeliveryOut();
            BeanUtils.copy(ordDisDelivery, ordDisDeliveryOut);
            // 更新数据
            this.handleDeliveryOrderAfterStock(ordDisDeliveryOut, updateStockDeliveryOrderIn.getDisDeliveryDetailList(),
                    storeOut.getStoreId(), updateStockDeliveryOrderIn.getBeforeDeliveryStatus());
            String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ORDER_OCCUPY_STOCK.getKey(),
                    ordDisDelivery.getDeliveryOrderNo(), stockLog);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDisDelivery.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                    content, new Date(),
                    ordDisDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            String statusContent = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_STATUS_UPDATE.getKey(), ordDisDelivery.getDeliveryOrderNo(),
                    DeliveryOrderEnum.getValueByKey(updateStockDeliveryOrderIn.getBeforeDeliveryStatus()), DeliveryOrderEnum.getValueByKey(ordDisDelivery.getDeliveryStatusCode()));
            BusinessLog statusBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDisDelivery.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                    statusContent, new Date(),
                    ordDisDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(statusBusinessLog);
            // 2023-2-3号资管调整取消ERP审核返款
//            this.deliveryOrderStockOutRefundFund(ordDisDelivery);
            successTotal.getAndIncrement();
        });
        return successTotal.get();
    }

    public void saveForManualCreateDeliveryOrder(OrdDisDelivery ordDisDelivery, String auditType) {
        ordDisSalvageDelivPondDetailService.saveForManualCreateDeliveryOrder(ordDisDelivery, auditType);
    }


    /**
     * 审核后占库存捞单
     *
     * @param ordDisDelivery
     * @param ordDisSalvageDelivPondDetail
     */
    @Transactional(rollbackFor = Exception.class)
    public int handleSalvageAfterAuditDeliveryOrder(OrdDisDelivery ordDisDelivery, OrdDisSalvageDelivPondDetail ordDisSalvageDelivPondDetail, StockInfoOut stockInfoOut) {
        OrdDisSalvageDelivPondDetailOut ordDisSalvageDelivPondDetailOut = new OrdDisSalvageDelivPondDetailOut();
        BeanUtils.copy(ordDisSalvageDelivPondDetail, ordDisSalvageDelivPondDetailOut);
        List<OrdDisSalvageDelivPondDetailOut> ordDisSalvageDelivPondDetailOutList = Lists.newArrayList();
        ordDisSalvageDelivPondDetailOut.setCenterStockBizOrgCode(stockInfoOut.getBizOrgCode());
        ordDisSalvageDelivPondDetailOut.setCenterStockOrgCode(stockInfoOut.getOrgCode());
        ordDisSalvageDelivPondDetailOutList.add(ordDisSalvageDelivPondDetailOut);
        return this.handleSalvageDelivPondDetailList(ordDisDelivery.getBizOrgCode(), ordDisDelivery.getCreator(), false, ordDisSalvageDelivPondDetailOutList);
    }


    @Transactional(rollbackFor = Exception.class)
    public void handleDeliveryOrderAfterStock(OrdDisDeliveryOut ordDisDelivery, List<OrdDisDeliveryDetail> disDeliveryDetailList, Integer storeId, String beforeDeliveryStatus) {
        ordDisDeliveryMapper.updateByPrimaryKeySelective(ordDisDelivery);
        if (DeliveryOrderEnum.INVALID.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
            if (DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDisDelivery.getSourceCode())
                    || DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDisDelivery.getSourceCode())) {
                List<BusinessLog> businessLogList = disDeliveryInvalidUnFreezeHandle.deliveryFreezeForBusinessOrder(ordDisDelivery.getId(), ordDisDelivery.getUpdater());
                if (CollectionUtils.isNotEmpty(businessLogList)) {
                    businessLogList.forEach(businessLog -> asyncLogService.sendAsyncSaveLogByMq(businessLog));
                }
            }
            // 订单流冻结金额注释整单作废退款逻辑
//            BigDecimal returnAmount = ordDisDeliveryPayService.calculationInvalidDisDeliverAmount(ordDisDelivery, beforeDeliveryStatus);
//            Response returnResponse = ordDisDeliveryPayService.returnAmountByDeliveryOrder(ordDisDelivery, ordDisDelivery.getDeliveryOrderNo(),
//                    FundReturnTypeEnum.DIS_DELIVERY_ORDER_INVALID.getName(), returnAmount, ordDisDelivery.getId());
//            if (!returnResponse.isSuccess()) {
//                log.error("审核占库存配销单{}整单作废退款失败，{}", ordDisDelivery.getDeliveryOrderNo(), returnResponse.getMessage());
//                throw new BusinessException(returnResponse.getMessage());
//            }
        }
        if (CollectionUtils.isNotEmpty(disDeliveryDetailList)) {
            ordDisDeliveryDetailService.batchUpdateDistributionInfo(disDeliveryDetailList);
            ordDisDeliveryDetailService.batchUpdateNotStock(ordDisDelivery.getId(), SystemConstant.SYSTEM_USER);
            if (DeliveryOrderEnum.APPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
                // 订单流冻结金额注释掉支付逻辑
//                if (DeliveryOrderSourceCodeEnum.MANUAL.getType().equals(ordDisDelivery.getSourceCode())) {
//                    BigDecimal distributionAmount = disDeliveryDetailList.stream().map(OrdDisDeliveryDetail::getDistributionAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
//                    distributionAmount = distributionAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP);
//                    // 资管余额支付
//                    Response<String> response = ordDisDeliveryPayService.payDeliveryOrder(ordDisDelivery, distributionAmount);
//                    if (!response.isSuccess()) {
//                        log.error("创建配销单{}支付异常:{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
//                        throw new BusinessException(response.getMessage());
//                    }
//                }
                // 如果是手工创建配销单，同步冻结配销单
                this.auditFrozenManualDeliveryOrder(ordDisDelivery);
                StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisDelivery.getStockCode());
                if (stockServer.isSendWms(ordDisDelivery.getStockCode(), stockInfoOut.getBizOrgCode())) {
                    this.initUnificationBill(ordDisDelivery, disDeliveryDetailList, storeId, stockInfoOut.getBizOrgCode());
                }
            }
        }
    }

    public void auditFrozenManualDeliveryOrder(OrdDisDelivery ordDisDelivery) {
        if (!DeliveryOrderSourceCodeEnum.MANUAL.getType().equals(ordDisDelivery.getSourceCode())) {
            return;
        }
//                    BigDecimal distributionAmount = disDeliveryDetailList.stream().map(OrdDisDeliveryDetail::getDistributionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//                    distributionAmount = distributionAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP);
//                    // 资管余额支付
//                    Response<String> response = ordDisDeliveryPayService.payDeliveryOrder(ordDisDelivery, distributionAmount);
//                    if (!response.isSuccess()) {
//                        log.error("创建配销单{}支付异常:{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
//                        throw new BusinessException(response.getMessage());
//                    }
        List<FrozenOrderIn> frozenOrderIns = Lists.newArrayList();
        FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
        frozenOrderIn.setBusinessNo(ordDisDelivery.getDeliveryOrderNo());
        frozenOrderIn.setAmount(ordDisDelivery.getDistributionAmount());
        frozenOrderIn.setBusinessType(FundTypeEnum.DISTRIBUTION_AUDIT.getCode());
        frozenOrderIns.add(frozenOrderIn);
        StoreFrozenIn storeFrozenIn = new StoreFrozenIn();
        storeFrozenIn.setFrozenOrders(frozenOrderIns);
        storeFrozenIn.setPrincipalCode(ordDisDelivery.getStoreCode());
        storeFrozenIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
        storeFrozenIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
        Response response = fundServer.frozen(storeFrozenIn);
        if (!response.isSuccess() && !response.getResultCode().equals("5001")) {
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDisDelivery.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                    "手动创建配销单冻结失败", new Date(),
                    ordDisDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            log.error("手动创建配销单{}冻结调用资管异常{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
            throw new BusinessException(response.getMessage());
        } else {
            ordDisDeliveryMapper.batchUpdateFreeze(Collections.singletonList(ordDisDelivery.getId()),
                    ordDisDelivery.getUpdater(), ordDisDelivery.getBizOrgCode());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDisDelivery.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                    "手动创建配销单审核冻结成功", new Date(),
                    ordDisDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
    }

    /**
     * 初始化发DTS配销单入参
     *
     * @return
     */
    public void initUnificationBill(OrdDisDeliveryOut
                                            ordDisDelivery, List<OrdDisDeliveryDetail> ordDisDeliveryDetails, Integer storeId, String
                                            centerStockBizOrgCode) {
        UnificationBillIn unificationBillIn = new UnificationBillIn();
        unificationBillIn.setPlatform_bill_id(ordDisDelivery.getDeliveryOrderNo());
        unificationBillIn.setSource_stock_id(ordDisDelivery.getStockCode());
        unificationBillIn.setAlc(StringUtils.isBlank(ordDisDelivery.getDistributionType()) ? null : systemDictService.getSystemDictName(ordDisDelivery.getDistributionType()));
        BigDecimal requestOrderAmount = ordDisDeliveryDetails.stream().map(detail -> checkRequestOrderAmount(detail, ordDisDelivery.getDistributionType(), ordDisDelivery.getDeliveryOrderNo())).reduce(BigDecimal.ZERO, BigDecimal::add);
//        BigDecimal requestOrderAmount = ordDisDeliveryDetails.stream().map(this::checkRequestOrderAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        unificationBillIn.setAmount_i(requestOrderAmount);
        unificationBillIn.setCount(ordDisDeliveryDetails.size());
        unificationBillIn.setWarehouse_id(ordDisDelivery.getWrhCode());
        unificationBillIn.setCreater(ordDisDelivery.getCreator());
        unificationBillIn.setDistribution_time(ordDisDelivery.getDistributionTime());
        unificationBillIn.setGenerate_time(ordDisDelivery.getCreateTime());
        unificationBillIn.setMemo(ordDisDelivery.getOrderPriority());
        unificationBillIn.setMemo_id("配货原因");
        unificationBillIn.setShop_code(ordDisDelivery.getStoreCode());
        unificationBillIn.setShop_id(storeId.toString());
        unificationBillIn.setSource_organization(centerStockBizOrgCode);
        unificationBillIn.setTarget_organization(centerStockBizOrgCode);
        List<UnificationBillDtlIn> unificationBillDtlIns = this.initUnificationBillDtl(ordDisDelivery, unificationBillIn, ordDisDeliveryDetails);
        unificationBillIn.setDetail_list(unificationBillDtlIns);

//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DELIVERY_TO_DTS, JSONObject.toJSONString(unificationBillIn), ordDisDelivery.getBizOrgCode(), ordDisDelivery.getDeliveryOrderNo());
        SendResponse sendResponse = disDeliveryToDtsSender.sendSync(JSONObject.toJSONString(unificationBillIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("配销单{}下发DTS消息ID---{}", ordDisDelivery.getDeliveryOrderNo(), sendResponse.getMessageId());
    }

    /**
     * 校验要货单金额
     *
     * @param item
     * @param deliveryType
     * @param deliveryOrderNo
     * @return
     */
    private BigDecimal checkRequestOrderAmount(OrdDisDeliveryDetail item, String deliveryType, String deliveryOrderNo) {
        if (DistributionWaysEnum.UNIFIEDDIS.getType().equals(deliveryType)) {
            if (Objects.isNull(item.getDistributionAmount())) {
                log.error("配货单{}明细{}审核金额为空");
                throw new BusinessException("配货单" + deliveryOrderNo + "明细" + item.getGoodsCode() + "审核金额为空");
            }
            return item.getDistributionAmount();
        }
        if (DistributionWaysEnum.TRANSFER.getType().equals(deliveryType)) {
            if (Objects.isNull(item.getOrderAmount())) {
                log.error("配货单{}明细{}要货金额为空");
                throw new BusinessException("配货单" + deliveryOrderNo + "明细" + item.getGoodsCode() + "要货金额为空");
            }
            return item.getOrderAmount();
        }
        return item.getOrderAmount();
    }

    /**
     * 初始化发DTS配销单明细
     *
     * @return
     */
    private List<UnificationBillDtlIn> initUnificationBillDtl(
            OrdDisDelivery ordDisDelivery,
            UnificationBillIn unificationBillIn,
            List<OrdDisDeliveryDetail> ordDisDeliveryDetails) {
        List<UnificationBillDtlIn> unificationBillIns = new ArrayList<>();
        for (OrdDisDeliveryDetail item : ordDisDeliveryDetails) {
            UnificationBillDtlIn unificationBillDtlIn = new UnificationBillDtlIn();
            unificationBillDtlIn.setLine(Objects.isNull(item.getLine()) ? NumberUtil.INTEGER_ZERO : item.getLine());
            unificationBillDtlIn.setMemo(ordDisDelivery.getRemark());
            unificationBillDtlIn.setOrder_pattern(NumberUtil.INTEGER_ZERO);
            unificationBillDtlIn.setPlatform_bill_id(unificationBillIn.getPlatform_bill_id());
            BigDecimal sellPrice = orderGoodsServer.getSellPrice(ordDisDelivery.getBizOrgCode(), ordDisDelivery.getStoreCode(), item.getGoodsCode());
            unificationBillDtlIn.setPrice_i(Objects.isNull(sellPrice) ? BigDecimal.ZERO : sellPrice);
            unificationBillDtlIn.setQuantity(item.getDistributionQuantity());
            unificationBillDtlIn.setSku_code(item.getGoodsCode());
            unificationBillDtlIn.setSku_id(String.valueOf(Objects.isNull(item.getOrgGoodsId()) ? NumberUtil.INTEGER_ZERO : item.getOrgGoodsId()));
            unificationBillDtlIn.setSource_organization(unificationBillIn.getSource_organization());
            unificationBillDtlIn.setSource_stock_id(unificationBillIn.getSource_stock_id());
            unificationBillDtlIn.setStorePrice(item.getOrderUnitPrice());
            unificationBillDtlIn.setTarget_organization(unificationBillIn.getTarget_organization());
            unificationBillDtlIn.setOrdNum(item.getPurchaseNo());
            unificationBillIns.add(unificationBillDtlIn);
        }
        return unificationBillIns;
    }


    /**
     * 配货单缺货或者少货返款
     *
     * @param ordDisDelivery
     */
    public void deliveryOrderStockOutRefundFund(OrdDisDelivery ordDisDelivery) {
        List<OrdDisDeliveryDetail> ordDisDeliveryDetails = ordDisDeliveryDetailService.findDeliveryOrderDetails(ordDisDelivery.getId());
        // 返款
        BigDecimal liquidationAmount = ordDisDeliveryDetails.stream().map(disDeliveryDetail -> getAmountByDeliveryOrderStatus(disDeliveryDetail, ordDisDelivery.getDeliveryStatusCode())).reduce(BigDecimal::add).get();
        if (BigDecimal.ZERO.compareTo(liquidationAmount) == 0) {
            return;
        }
        RechargeLiquidationIn liquidationIn = new RechargeLiquidationIn();
        liquidationIn.setRecipientPrincipalCode(ordDisDelivery.getStoreCode());
        liquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
        liquidationIn.setPayOrPrincipalCode(ordDisDelivery.getBizOrgCode());
        liquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
        liquidationIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
        liquidationIn.setBusinessNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PXY.getCode(), ordDisDelivery.getBizOrgCode(), uniqueUtils, 4));
        liquidationIn.setBusinessType(FundTypeEnum.DIS_DELIVERY_ORDER_RETURN.getCode());
        liquidationIn.setOriginalBusinessNo(ordDisDelivery.getDeliveryOrderNo());
        liquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        liquidationIn.setLiquidationAmount(liquidationAmount.abs());
        liquidationIn.setRemark(FundReturnTypeEnum.DIS_DELIVERY_ORDER_STOCK_OUT.getName());
        liquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
        Response settlementResponse = fundServer.settlement(liquidationIn);
        if (!settlementResponse.isSuccess()) {
            log.error("门店{}配销单{}返款失败{}", ordDisDelivery.getStoreCode(), ordDisDelivery.getDeliveryOrderNo(), settlementResponse.getMessage());
        }
        String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ORDER_STOCK_OUT_RETURN_FUND.getKey(),
                ordDisDelivery.getDeliveryOrderNo(), liquidationAmount);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                content, new Date(),
                ordDisDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    public int handleSalvageAfterSplitDeliveryOrder(List<OrdDisDelivery> ordDisDeliveryList, String
            bizOrgCode, String loginUsername) {
        List<Long> idList = ordDisDeliveryList.stream().map(OrdDisDelivery::getId).collect(Collectors.toList());
        List<OrdDisSalvageDelivPondDetailOut> ordDisSalvageDelivPondDetailOutList = ordDisSalvageDelivPondDetailService.findByDeliveryOrderIdList(idList);
        Map<String, StockInfoOut> stockMap = new HashMap<>();
        ordDisSalvageDelivPondDetailOutList.forEach(ordDirSalvageDelivPondDetailOut -> {
            StockInfoOut stockInfoOut = stockMap.get(ordDirSalvageDelivPondDetailOut.getStockCode());
            if (Objects.isNull(stockInfoOut)) {
                stockInfoOut = stockServer.getTransInfo(ordDirSalvageDelivPondDetailOut.getStockCode());
                if (Objects.nonNull(stockInfoOut)) {
                    stockMap.put(stockInfoOut.getStockCode(), stockInfoOut);
                }
            }
            ordDirSalvageDelivPondDetailOut.setCenterStockBizOrgCode(stockInfoOut.getBizOrgCode());
            ordDirSalvageDelivPondDetailOut.setCenterStockOrgCode(stockInfoOut.getOrgCode());
        });
        return this.handleSalvageDelivPondDetailList(bizOrgCode, loginUsername, true, ordDisSalvageDelivPondDetailOutList);
    }


    /**
     * 根据配销单状态获取返款金额
     *
     * @param disDeliveryDetail
     * @param deliveryOrderStatus
     * @return
     */
    private BigDecimal getAmountByDeliveryOrderStatus(OrdDisDeliveryDetail disDeliveryDetail, String
            deliveryOrderStatus) {
        if (DeliveryOrderEnum.INVALID.getKey().equals(deliveryOrderStatus)) {
            return disDeliveryDetail.getOrderAmount();
        }
        if (DeliveryOrderEnum.APPROVED.getKey().equals(deliveryOrderStatus)) {
            if (Objects.isNull(disDeliveryDetail.getDistributionQuantity()) || BigDecimal.ZERO.compareTo(disDeliveryDetail.getDistributionQuantity()) == NumberUtil.INTEGER_ZERO) {
                return disDeliveryDetail.getOrderAmount();
            } else {
                BigDecimal quantity = disDeliveryDetail.getOrderQuantity().subtract(disDeliveryDetail.getDistributionQuantity());
                return quantity.multiply(disDeliveryDetail.getOrderUnitPrice());
            }
        }
        throw new BusinessException("未知配货单类型");
    }

    /**
     * 审核手动创建的非天岁中转配销单
     * @param ordDisDelivery
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int handleDeliveryOrderByManualTransfer(OrdDisDelivery ordDisDelivery, String centerStockBizOrgCode) {
        String baseStatusCode = ordDisDelivery.getDeliveryStatusCode();
        ordDisDelivery.setDeliveryStatusCode(DeliveryOrderEnum.APPROVED.getKey());
        ordDisDelivery.setDistributionQuantity(ordDisDelivery.getOrderQuantity());
        ordDisDelivery.setDistributionAmount(ordDisDelivery.getOrderAmount());
        ordDisDelivery.setUpdateTime(LocalDateTime.now());
        int count = ordDisDeliveryMapper.updateByPrimaryKeySelective(ordDisDelivery);
        if (count < 1) {
            return count;
        }
        List<OrdDisDeliveryDetail> ordDisDeliveryDetails = ordDisDeliveryDetailService.findDeliveryOrderDetails(ordDisDelivery.getId());
        ordDisDeliveryDetails.forEach(ordDisDeliveryDetail -> {
            ordDisDeliveryDetail.setDistributionUnitPrice(ordDisDeliveryDetail.getOrderUnitPrice());
            ordDisDeliveryDetail.setDistributionQuantity(ordDisDeliveryDetail.getOrderQuantity());
            ordDisDeliveryDetail.setDistributionPackageQuantity(ordDisDeliveryDetail.getOrderPackageQuantity());
            ordDisDeliveryDetail.setDistributionAmount(ordDisDeliveryDetail.getOrderAmount());
            ordDisDeliveryDetail.setUpdateTime(LocalDateTime.now());
            //税额
            BigDecimal sellTax = Objects.isNull(ordDisDeliveryDetail.getSellTax()) ? BigDecimal.ZERO : ordDisDeliveryDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tax = sellTax.add(BigDecimal.ONE);
            ordDisDeliveryDetail.setDistributionExceptTaxAmount(ordDisDeliveryDetail.getDistributionAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            ordDisDeliveryDetail.setDistributionTaxAmount(ordDisDeliveryDetail.getDistributionAmount().subtract(ordDisDeliveryDetail.getDistributionExceptTaxAmount()));
            BigDecimal distributionQuantity = Objects.isNull(ordDisDeliveryDetail.getDistributionQuantity()) ? BigDecimal.ZERO : ordDisDeliveryDetail.getDistributionQuantity();
            ordDisDeliveryDetail.setWrhCostAmount(ordDisDeliveryDetail.getWrhPrice().multiply(distributionQuantity));
            ordDisDeliveryDetail.setWrhExceptTaxAmount(ordDisDeliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            ordDisDeliveryDetail.setWrhTaxAmount(ordDisDeliveryDetail.getWrhCostAmount().subtract(ordDisDeliveryDetail.getWrhExceptTaxAmount()));
            ordDisDeliveryDetail.setStoreCostAmount(ordDisDeliveryDetail.getStoreStockPrice().multiply(distributionQuantity));
            ordDisDeliveryDetail.setStoreExceptTaxAmount(ordDisDeliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            ordDisDeliveryDetail.setStoreTaxAmount(ordDisDeliveryDetail.getStoreCostAmount().subtract(ordDisDeliveryDetail.getStoreExceptTaxAmount()));
        });
        ordDisDeliveryDetailService.batchUpdateDistributionInfo(ordDisDeliveryDetails);
        BigDecimal distributionAmount = ordDisDeliveryDetails.stream().map(OrdDisDeliveryDetail::getDistributionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        distributionAmount = distributionAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP);
        // 资管余额支付
//        Response<String> response = ordDisDeliveryPayService.payDeliveryOrder(ordDisDelivery, distributionAmount);
//        if (!response.isSuccess()) {
//            log.error("创建配销单{}支付异常:{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
//            throw new BusinessException(response.getMessage());
//        }
        this.auditFrozenManualDeliveryOrder(ordDisDelivery);
        if (stockServer.isSendWms(ordDisDelivery.getStockCode(), centerStockBizOrgCode)) {
            OrdDisDeliveryOut ordDisDeliveryOut = new OrdDisDeliveryOut();
            BeanUtils.copy(ordDisDelivery, ordDisDeliveryOut);
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDisDelivery.getStoreCode());
            this.initUnificationBill(ordDisDeliveryOut, ordDisDeliveryDetails, storeOut.getStoreId(), centerStockBizOrgCode);
        }
        String statusContent = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_STATUS_UPDATE.getKey(), ordDisDelivery.getDeliveryOrderNo(),
                DeliveryOrderEnum.getValueByKey(baseStatusCode), DeliveryOrderEnum.getValueByKey(ordDisDelivery.getDeliveryStatusCode()));
        BusinessLog statusBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                statusContent, new Date(),
                ordDisDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(statusBusinessLog);
        return count;
    }
}
