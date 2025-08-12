package com.edc.erp.directly.dirdeliveryorder.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.DeliveryOrderLogEnum;
import com.edc.erp.common.enumeration.DistributionWaysEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPondDetail;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryDetailMapper;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryMapper;
import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateStockDirDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirSalvageDelivPondDetailOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirSalvageDelivPondDetailService;
import com.edc.erp.reducestock.model.out.stock.OperationStockOut;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
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
public class DirDeliveryOrderSalvageHandle {

    private final StoreCenterService storeCenterService;

    private final OrdDirDeliveryMapper ordDirDeliveryMapper;

    private final AsyncLogService asyncLogService;

    private final OrdDirDeliveryDetailMapper ordDirDeliveryDetailMapper;

    private final OrdDirSalvageDelivPondDetailService ordDirSalvageDelivPondDetailService;

    private final AsyncPushTaskService asyncPushTaskService;

    private final SystemDictService systemDictService;

    private final StockServer stockServer;

    private final OrderGoodsServer orderGoodsServer;

    @Qualifier("dirDeliveryToDtsSender")
    private final MessageSender dirDeliveryToDtsSender;

    @Transactional(rollbackFor = Exception.class)
    public int handleSalvageDelivPondDetailList(String channelBizOrgCode, String loginUsername, Boolean isRecalculateOccupancyQty, List<OrdDirSalvageDelivPondDetailOut> ordDirSalvageDelivPondDetailOutList) {
        // 处理捞单,并占用库存
        List<OperationStockOut> operationStockOutList = ordDirSalvageDelivPondDetailService.handleSalvage(channelBizOrgCode, loginUsername, isRecalculateOccupancyQty, ordDirSalvageDelivPondDetailOutList);
        if (CollectionUtils.isEmpty(operationStockOutList)) {
            return 0;
        }
        // 封装配货单占库存后,更新配货单明细数据
        List<UpdateStockDirDeliveryOrderIn> updateStockDirDeliveryOrderInList = ordDirSalvageDelivPondDetailService.initUpdateStockDeliveryOrderInList(operationStockOutList, loginUsername, channelBizOrgCode);
        AtomicInteger successTotal = new AtomicInteger();
        updateStockDirDeliveryOrderInList.forEach(updateStockDeliveryOrderIn -> {
            OrdDirDelivery ordDirDelivery = updateStockDeliveryOrderIn.getOrdDirDelivery();
            String stockLog = StringUtils.isBlank(updateStockDeliveryOrderIn.getStockOutLog()) ? "成功" : updateStockDeliveryOrderIn.getStockOutLog();
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDirDelivery.getStoreCode());
            // 更新配货单状态和明细的审核数
            OrdDirDeliveryOut ordDirDeliveryOut = new OrdDirDeliveryOut();
            BeanUtils.copy(ordDirDelivery, ordDirDeliveryOut);
            this.handleDeliveryOrderAfterStock(ordDirDeliveryOut, updateStockDeliveryOrderIn.getDirDeliveryDetailList(), storeOut.getStoreId());
            String content = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_OCCUPY_STOCK.getKey(),
                    ordDirDelivery.getDeliveryOrderNo(), stockLog);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDirDelivery.getId()),
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(),
                    content, new Date(),
                    ordDirDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            String statusContent = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_STATUS_UPDATE.getKey(), ordDirDelivery.getDeliveryOrderNo(),
                    DeliveryOrderEnum.getValueByKey(updateStockDeliveryOrderIn.getBeforeDeliveryStatus()), DeliveryOrderEnum.getValueByKey(ordDirDelivery.getDeliveryStatusCode()));
            BusinessLog statusBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDirDelivery.getId()),
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(),
                    statusContent, new Date(),
                    ordDirDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(statusBusinessLog);
            successTotal.getAndIncrement();
        });
        return successTotal.get();
    }

    public void saveForManualCreateDeliveryOrder(OrdDirDelivery ordDirDelivery, String auditType) {
        ordDirSalvageDelivPondDetailService.saveForManualCreateDeliveryOrder(ordDirDelivery, auditType);
    }


    /**
     * 审核后占库存捞单
     *
     * @param ordDirDelivery
     * @param ordDirSalvageDelivPondDetail
     */
    @Transactional(rollbackFor = Exception.class)
    public int handleSalvageAfterAuditDeliveryOrder(OrdDirDelivery ordDirDelivery, StockInfoOut stockInfoOut, OrdDirSalvageDelivPondDetail ordDirSalvageDelivPondDetail) {
        OrdDirSalvageDelivPondDetailOut ordDirSalvageDelivPondDetailOut = new OrdDirSalvageDelivPondDetailOut();
        BeanUtils.copy(ordDirSalvageDelivPondDetail, ordDirSalvageDelivPondDetailOut);
        List<OrdDirSalvageDelivPondDetailOut> ordDirSalvageDelivPondDetailOutList = Lists.newArrayList();
        ordDirSalvageDelivPondDetailOut.setCenterStockBizOrgCode(stockInfoOut.getBizOrgCode());
        ordDirSalvageDelivPondDetailOut.setCenterStockOrgCode(stockInfoOut.getOrgCode());
        ordDirSalvageDelivPondDetailOutList.add(ordDirSalvageDelivPondDetailOut);
        return this.handleSalvageDelivPondDetailList(ordDirDelivery.getBizOrgCode(), ordDirDelivery.getCreator(), false, ordDirSalvageDelivPondDetailOutList);
    }

    /**
     * 库存调整完毕配货单处理逻辑
     *
     * @param ordDirDelivery
     * @param dirDeliveryDetailList
     * @param storeId
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleDeliveryOrderAfterStock(OrdDirDeliveryOut ordDirDelivery, List<OrdDirDeliveryDetail> dirDeliveryDetailList, Integer storeId) {
        ordDirDeliveryMapper.updateByPrimaryKeySelective(ordDirDelivery);
        if (CollectionUtils.isNotEmpty(dirDeliveryDetailList)) {
            ordDirDeliveryDetailMapper.batchUpdateDistributionInfo(dirDeliveryDetailList);
            ordDirDeliveryDetailMapper.batchUpdateNotStock(ordDirDelivery.getId(), SystemConstant.SYSTEM_USER);
            if (DeliveryOrderEnum.APPROVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
                StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirDelivery.getStockCode());
                if (stockServer.isSendWms(ordDirDelivery.getStockCode(), stockInfoOut.getBizOrgCode())) {
                    this.initUnificationBill(ordDirDelivery, dirDeliveryDetailList, storeId, stockInfoOut.getBizOrgCode());
                }
            }
        }
    }

    /**
     * 初始化下发dts入参
     *
     * @param ordDirDelivery
     * @param detailList
     * @param storeId
     */
    public void initUnificationBill(OrdDirDeliveryOut ordDirDelivery, List<OrdDirDeliveryDetail> detailList, Integer storeId, String centerStockBizOrgCode) {
        UnificationBillIn unificationBillIn = new UnificationBillIn();
        unificationBillIn.setPlatform_bill_id(ordDirDelivery.getDeliveryOrderNo());
        unificationBillIn.setSource_stock_id(ordDirDelivery.getStockCode());
        unificationBillIn.setAlc(StringUtils.isBlank(ordDirDelivery.getDistributionType()) ? null : systemDictService.getSystemDictName(ordDirDelivery.getDistributionType()));
        BigDecimal requestOrderAmount = detailList.stream().map(detail -> checkRequestOrderAmount(detail, ordDirDelivery.getDistributionType(), ordDirDelivery.getDeliveryOrderNo())).reduce(BigDecimal.ZERO, BigDecimal::add);
//        BigDecimal requestOrderAmount = detailList.stream().map(this::checkRequestOrderAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        unificationBillIn.setAmount_i(requestOrderAmount);
        unificationBillIn.setCount(detailList.size());
        unificationBillIn.setWarehouse_id(ordDirDelivery.getWrhCode());
        unificationBillIn.setCreater(ordDirDelivery.getCreator());
        unificationBillIn.setDistribution_time(ordDirDelivery.getDistributionTime());
        unificationBillIn.setGenerate_time(ordDirDelivery.getCreateTime());
        unificationBillIn.setMemo(ordDirDelivery.getOrderPriority());
        unificationBillIn.setMemo_id("配货原因");
        unificationBillIn.setShop_code(ordDirDelivery.getStoreCode());
        unificationBillIn.setShop_id(storeId.toString());
        unificationBillIn.setSource_organization(centerStockBizOrgCode);
        unificationBillIn.setTarget_organization(centerStockBizOrgCode);
        List<UnificationBillDtlIn> unificationBillDtlIns = this.initUnificationBillDtl(ordDirDelivery, unificationBillIn, detailList);
        unificationBillIn.setDetail_list(unificationBillDtlIns);

//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_DELIVERY_TO_DTS, JSONObject.toJSONString(unificationBillIn), ordDirDelivery.getBizOrgCode(), ordDirDelivery.getDeliveryOrderNo());
        SendResponse sendResponse = dirDeliveryToDtsSender.sendSync(JSONObject.toJSONString(unificationBillIn).getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("配销单{}下发DTS消息ID---{}", ordDirDelivery.getDeliveryOrderNo(), sendResponse.getMessageId());
    }

    /**
     * 要货金额校验
     *
     * @param item
     * @param deliveryType
     * @param deliveryOrderNo
     * @return
     */
    private BigDecimal checkRequestOrderAmount(OrdDirDeliveryDetail item, String deliveryType, String deliveryOrderNo) {
        if (DistributionWaysEnum.UNIFIEDDIS.getType().equals(deliveryType)) {
            if (Objects.isNull(item.getDistributionAmount())) {
                log.error("配货单{}明细{}审核金额为空");
                throw new BusinessException("配货单" + deliveryOrderNo + "明细" + item.getGoodsCode() + "审核金额为空");
            }
            return item.getDistributionAmount();
        }
        if (DistributionWaysEnum.TRANSFER.getType().equals(deliveryType)) {
            if (Objects.isNull(item.getOrderAmount())) {
                if (Objects.isNull(item.getOrderAmount())) {
                    log.error("配货单{}明细{}要货金额为空");
                    throw new BusinessException("配货单" + deliveryOrderNo + "明细" + item.getGoodsCode() + "要货金额为空");
                }
            }
            return item.getOrderAmount();
        }
        return item.getOrderAmount();
    }

    public int handleSalvageAfterSplitDeliveryOrder(List<OrdDirDelivery> ordDirDeliveryList, String bizOrgCode, String loginUsername) {
        List<Long> idList = ordDirDeliveryList.stream().map(OrdDirDelivery::getId).collect(Collectors.toList());
        List<OrdDirSalvageDelivPondDetailOut> ordDirSalvageDelivPondDetailOutList = ordDirSalvageDelivPondDetailService.findByDeliveryOrderIdList(idList);
        Map<String, StockInfoOut> stockMap = new HashMap<>();
        ordDirSalvageDelivPondDetailOutList.forEach(ordDirSalvageDelivPondDetailOut -> {
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
        return this.handleSalvageDelivPondDetailList(bizOrgCode, loginUsername, true, ordDirSalvageDelivPondDetailOutList);
    }

    /**
     * 初始化下发DTS明细
     *
     * @param ordDirDelivery
     * @param unificationBillIn
     * @param detailList
     * @return
     */
    private List<UnificationBillDtlIn> initUnificationBillDtl(OrdDirDelivery ordDirDelivery, UnificationBillIn unificationBillIn, List<OrdDirDeliveryDetail> detailList) {
        List<UnificationBillDtlIn> unificationBillIns = new ArrayList<>();
        for (OrdDirDeliveryDetail item : detailList) {
            UnificationBillDtlIn unificationBillDtlIn = new UnificationBillDtlIn();
            unificationBillDtlIn.setLine(Objects.isNull(item.getLine()) ? NumberUtil.INTEGER_ZERO : item.getLine());
            unificationBillDtlIn.setMemo(ordDirDelivery.getRemark());
            unificationBillDtlIn.setOrder_pattern(NumberUtil.INTEGER_ZERO);
            unificationBillDtlIn.setPlatform_bill_id(unificationBillIn.getPlatform_bill_id());
            // 售价
            BigDecimal sellPrice = orderGoodsServer.getSellPrice(ordDirDelivery.getBizOrgCode(), ordDirDelivery.getStoreCode(), item.getGoodsCode());
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
     * 审核手动创建的非天岁中转配销单
     * @param ordDirDelivery
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int handleDeliveryOrderByManualTransfer(OrdDirDelivery ordDirDelivery, String centerStockBizOrgCode) {
        String baseStatusCode = ordDirDelivery.getDeliveryStatusCode();
        ordDirDelivery.setDeliveryStatusCode(DeliveryOrderEnum.APPROVED.getKey());
        ordDirDelivery.setDistributionQuantity(ordDirDelivery.getOrderQuantity());
        ordDirDelivery.setDistributionAmount(ordDirDelivery.getOrderAmount());
        ordDirDelivery.setUpdateTime(LocalDateTime.now());
        int count = ordDirDeliveryMapper.updateByPrimaryKeySelective(ordDirDelivery);
        if (count < 1) {
            return count;
        }
        List<OrdDirDeliveryDetail> ordDirDeliveryDetails = ordDirDeliveryDetailMapper.select(OrdDirDeliveryDetail.builder().deliveryOrderId(ordDirDelivery.getId()).build());
        ordDirDeliveryDetails.forEach(ordDisDeliveryDetail -> {
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
        ordDirDeliveryDetailMapper.batchUpdateDistributionInfo(ordDirDeliveryDetails);

        if (stockServer.isSendWms(ordDirDelivery.getStockCode(), centerStockBizOrgCode)) {
            OrdDirDeliveryOut ordDirDeliveryOut = new OrdDirDeliveryOut();
            BeanUtils.copy(ordDirDelivery, ordDirDeliveryOut);
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDirDelivery.getStoreCode());
            this.initUnificationBill(ordDirDeliveryOut, ordDirDeliveryDetails, storeOut.getStoreId(), centerStockBizOrgCode);
        }
        String statusContent = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_STATUS_UPDATE.getKey(), ordDirDelivery.getDeliveryOrderNo(),
                DeliveryOrderEnum.getValueByKey(baseStatusCode), DeliveryOrderEnum.getValueByKey(ordDirDelivery.getDeliveryStatusCode()));
        BusinessLog statusBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(),
                String.valueOf(ordDirDelivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(),
                statusContent, new Date(),
                ordDirDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(statusBusinessLog);
        return count;
    }
}
