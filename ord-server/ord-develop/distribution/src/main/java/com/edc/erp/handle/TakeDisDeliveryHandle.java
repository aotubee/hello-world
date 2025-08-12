package com.edc.erp.handle;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.AdjustStoreExpiryMqIn;
import com.edc.erp.common.model.in.SaveAdjustStoreExpiryOrderMqIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderAttachment;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderHeartRateMonitor;
import com.edc.erp.disdeliveryorder.model.in.CacheTakeDisDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.in.TakeDisDeliveryOrderGoodsIn;
import com.edc.erp.disdeliveryorder.model.in.TakeDisDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.in.UpdateDisDeliveryOrderOngoingIn;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderAttachmentService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderHeartRateMonitorService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.enumeration.AttachmentTypeEnum;
import com.edc.erp.enumeration.DeliveryOrderReceiveProgressEnum;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.redis.RedisService;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fxw
 * @description: 收货处理类
 * @since 2022/10/27 15:13
 */
@Service
@Slf4j
public class TakeDisDeliveryHandle {
    @Autowired
    private RedisService redisService;

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    @Autowired
    private OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private OrdDisDeliveryOrderHeartRateMonitorService deliveryOrderHeartRateMonitorService;

    @Autowired
    private OrdDisDeliveryOrderAttachmentService deliveryOrderAttachmentService;

    @Autowired
    private AsyncPushTaskService asyncPushTaskService;

    @Autowired
    private StockServer stockServer;

    @Autowired
    private StockFlowService stockFlowService;

    @Autowired
    @Qualifier("takeDeliveryAdjustExpirySender")
    private MessageSender takeDeliveryAdjustExpirySender;

    @Autowired
    private DisDeliveryOrderHandle disDeliveryOrderHandle;

    @Autowired
    @Qualifier("disDeliveryToDifferenceSender")
    private MessageSender disDeliveryToDifferenceSender;

    /**
     * 收货
     *
     * @param takeDisDeliveryOrderIn
     * @param ordDisDelivery
     */
    @Transactional(rollbackFor = Exception.class)
    public void takeDelivery(TakeDisDeliveryOrderIn takeDisDeliveryOrderIn, OrdDisDelivery ordDisDelivery, StockInfoOut stockInfoOut) {
        List<TakeDisDeliveryOrderGoodsIn> takeDisDeliveryOrderGoodsInList = takeDisDeliveryOrderIn.getTakeDeliveryOrderGoodsInList();
        if (CollectionUtils.isEmpty(takeDisDeliveryOrderGoodsInList)) {
            throw new BusinessException("无商品可收货");
        }
        List<OrdDisDeliveryDetail> disDeliveryDetailList = ordDisDeliveryDetailService.findDeliveryOrderDetails(ordDisDelivery.getId());
        // 处理收货，返回差异数据
        Map<String, List<OrdDisDelivDifferenceDetail>> differenceTypeGoodsMap = this.optTakeGoods(takeDisDeliveryOrderIn,
                ordDisDelivery, disDeliveryDetailList, takeDisDeliveryOrderIn.getLoginUsername());
        // 处理差异商品
        this.optDifferenceGoods(ordDisDelivery, differenceTypeGoodsMap);
        //修改配销单状态
        OrdDisDelivery updateDeliveryOrder = new OrdDisDelivery();
        updateDeliveryOrder.setId(ordDisDelivery.getId());
        updateDeliveryOrder.setDeliveryStatusCode(DeliveryOrderEnum.RECEIVED.getKey());
        updateDeliveryOrder.setUpdateTime(LocalDateTime.now());
        updateDeliveryOrder.setUpdater(takeDisDeliveryOrderIn.getLoginUsername());
        updateDeliveryOrder.setReceiveTime(LocalDateTime.now());
        updateDeliveryOrder.setReceiveProgress(DeliveryOrderReceiveProgressEnum.END.getKey());
        updateDeliveryOrder.setTakeRemark(takeDisDeliveryOrderIn.getTakeRemark());
        ordDisDeliveryService.updateByPrimaryKeySelective(updateDeliveryOrder);
        //更新收货附件信息
        this.optAttachment(takeDisDeliveryOrderIn, ordDisDelivery);
        // 收货完成后清除缓存数据
        this.removeTakeDeliveryCacheData(ordDisDelivery.getId());
        //获取收货后明细信息
        List<OrdDisDeliveryDetail> updateDisDeliveryDetailList = ordDisDeliveryDetailService.findDeliveryOrderDetails(ordDisDelivery.getId());
        // 更新库存调整
        this.handleTakeDeliveryStock(ordDisDelivery, updateDisDeliveryDetailList, stockInfoOut);
        String takeContent;
        if (StringUtils.isNotBlank(takeDisDeliveryOrderIn.getTakeRemark())) {
            takeContent = MessageFormat.format(DeliveryOrderLogEnum.TAKE_DELIVERY.getKey(), "，" + takeDisDeliveryOrderIn.getTakeRemark());
        } else {
            takeContent = MessageFormat.format(DeliveryOrderLogEnum.TAKE_DELIVERY.getKey(), "。");
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), takeContent, new Date(), takeDisDeliveryOrderIn.getLoginUsername());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 收货处理库存
     *
     * @param ordDisDelivery
     */
    public void handleTakeDeliveryStock(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> disDeliveryDetailList, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getName());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        stockFlowIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(ordDisDelivery.getCreator());
        stockFlowIn.setOperationType(ordDisDelivery.getDeliveryStatusCode());
        stockFlowIn.setOrgCode(ordDisDelivery.getOrgCode());
        stockFlowIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        disDeliveryDetailList.forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity())) {
                return;
            }
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            com.edc.plugins.utils.bean.BeanUtils.copy(item, stockFlowGoodsIn);
            //申请数
            stockFlowGoodsIn.setApplyQty(item.getDeliveryQuantity().abs());
            //申请增/减
            stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
            //是否改变可用库存 Y是N否-统配出/配销出
            stockFlowGoodsIn.setIsBusinessQty("Y");
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDisDelivery.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            stockFlowGoodsIn.setStoreCode(ordDisDelivery.getStoreCode());
            stockFlowGoodsIn.setStoreName(ordDisDelivery.getStoreName());
            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.STORE.getCode());
            //成本含税金额,释放按发货数量释放
            stockFlowGoodsIn.setCostTaxAmount(item.getDistributionPrice().multiply(item.getDeliveryQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate());
            //成本不含税金额
            stockFlowGoodsIn.setCostNonTaxAmount(stockFlowGoodsIn.getCostTaxAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //成本税额
            stockFlowGoodsIn.setCostTax(stockFlowGoodsIn.getCostTaxAmount().subtract(stockFlowGoodsIn.getCostNonTaxAmount()));
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(item.getArrivalAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate());
            //含税金额
            stockFlowGoodsIn.setTaxAmount(item.getArrivalAmount().negate());
            // 税额
            stockFlowGoodsIn.setTax(stockFlowGoodsIn.getTaxAmount().subtract(stockFlowGoodsIn.getNonTaxAmount()));
            //单号
            stockFlowGoodsIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
            stockFlowGoodsIns.add(stockFlowGoodsIn);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        List<StockFlowIn> stockFlowInList = Lists.newArrayList();
        stockFlowInList.add(stockFlowIn);
        Response response = stockFlowService.checkStockFlow(stockFlowInList);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
    }

    /**
     * 更新收货附件信息
     *
     * @param takeDisDeliveryOrderIn
     * @param ordDisDelivery
     */
    @Transactional(rollbackFor = Exception.class)
    public void optAttachment(TakeDisDeliveryOrderIn takeDisDeliveryOrderIn, OrdDisDelivery ordDisDelivery) {
        deliveryOrderHeartRateMonitorService.deleteByDeliveryOrderId(ordDisDelivery.getId(), ordDisDelivery.getBizOrgCode());
        if (CollectionUtils.isNotEmpty(takeDisDeliveryOrderIn.getAttachmentUrlList())) {
            List<OrdDisDeliveryOrderAttachment> deliveryOrderAttachmentList = Lists.newArrayList();
            takeDisDeliveryOrderIn.getAttachmentUrlList().forEach(url -> {
                OrdDisDeliveryOrderAttachment deliveryOrderAttachment = new OrdDisDeliveryOrderAttachment();
                deliveryOrderAttachment.setDisDeliveryOrderId(ordDisDelivery.getId());
                deliveryOrderAttachment.setAttachmentType(AttachmentTypeEnum.TAKE_DELIVERY.getKey());
                deliveryOrderAttachment.setAttachmentUrl(url);
                deliveryOrderAttachment.setCreator(takeDisDeliveryOrderIn.getLoginUsername());
                deliveryOrderAttachment.setCreateTime(LocalDateTime.now());
                deliveryOrderAttachment.setOrgCode(ordDisDelivery.getOrgCode());
                deliveryOrderAttachment.setBizOrgCode(ordDisDelivery.getBizOrgCode());
                deliveryOrderAttachmentList.add(deliveryOrderAttachment);
            });
            deliveryOrderAttachmentService.batchSaveDeliveryOrderAttachment(deliveryOrderAttachmentList);
        }
    }

    /**
     * 处理差异商品
     *
     * @param ordDisDelivery
     * @param differenceTypeGoodsMap
     */
    private void optDifferenceGoods(OrdDisDelivery ordDisDelivery, Map<String, List<OrdDisDelivDifferenceDetail>> differenceTypeGoodsMap) {
        if (differenceTypeGoodsMap.size() > 0) {
            differenceTypeGoodsMap.forEach((diffenenceType, ordDisDelivDifferenceDetails) -> {
                BigDecimal totalApplyDifferenceQuantity = ordDisDelivDifferenceDetails.stream()
                        .map(OrdDisDelivDifferenceDetail::getApplyDifferenceQuantity).reduce(BigDecimal::add).get();
                BigDecimal totalApplyDifferenceAmount = ordDisDelivDifferenceDetails.stream()
                        .map(OrdDisDelivDifferenceDetail::getApplyDifferenceAmount).reduce(BigDecimal::add).get();
                SaveDifferenceIn saveDifferenceIn = new SaveDifferenceIn();
                saveDifferenceIn.setStockCode(ordDisDelivery.getStockCode());
                saveDifferenceIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
                saveDifferenceIn.setStoreCode(ordDisDelivery.getStoreCode());
                saveDifferenceIn.setStoreName(ordDisDelivery.getStoreName());
                saveDifferenceIn.setDeliveryOrderNo(ordDisDelivery.getDeliveryOrderNo());
                saveDifferenceIn.setDeliveryTime(ordDisDelivery.getDeliveryTime());
                saveDifferenceIn.setReceiveTime(ordDisDelivery.getReceiveTime());
                saveDifferenceIn.setOrgCode(ordDisDelivery.getOrgCode());
                saveDifferenceIn.setTotalApplyDifferenceQuantity(totalApplyDifferenceQuantity);
                saveDifferenceIn.setTotalApplyDifferenceAmount(totalApplyDifferenceAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
                saveDifferenceIn.setDifferenceType(diffenenceType);
                saveDifferenceIn.setWrhCode(ordDisDelivery.getWrhCode());
                saveDifferenceIn.setDifferenceDetails(ordDisDelivDifferenceDetails);
                saveDifferenceIn.setCreator(ordDisDelivery.getUpdater());
                saveDifferenceIn.setUpdater(ordDisDelivery.getUpdater());
                //异步生成差异单
//                asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DELIVERY_TO_DIFFERENCE, JSONObject.toJSONString(saveDifferenceIn), ordDisDelivery.getBizOrgCode(), ordDisDelivery.getDeliveryOrderNo());
                disDeliveryToDifferenceSender.sendSync(JSONObject.toJSONString(saveDifferenceIn).getBytes(),System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
            });
        }
    }

    /**
     * 处理收货明细，返回差异数据
     *
     * @param takeDisDeliveryOrderIn
     * @param ordDisDelivery
     * @param disDeliveryDetailList
     * @param loginUsername
     * @return
     */
    private Map<String, List<OrdDisDelivDifferenceDetail>> optTakeGoods(TakeDisDeliveryOrderIn takeDisDeliveryOrderIn, OrdDisDelivery ordDisDelivery,
                                                                        List<OrdDisDeliveryDetail> disDeliveryDetailList, String loginUsername) {
        Map<String, List<OrdDisDelivDifferenceDetail>> differenceTypeGoodsMap = new LinkedHashMap<>();
        Map<Long, OrdDisDeliveryDetail> detailMap = disDeliveryDetailList.stream().collect(Collectors.toMap(OrdDisDeliveryDetail::getId, Function.identity()));

        List<AdjustStoreExpiryMqIn> adjustStoreExpiryMqInList = Lists.newArrayList();
        takeDisDeliveryOrderIn.getTakeDeliveryOrderGoodsInList().forEach(takeDisDeliveryOrderGoodsIn -> {
            OrdDisDeliveryDetail deliveryOrderDetails = detailMap.get(takeDisDeliveryOrderGoodsIn.getDeliveryOrderDetailsId());
            if (Objects.isNull(deliveryOrderDetails)) {
                log.error("配货单：" + ordDisDelivery.getDeliveryOrderNo() + ",明细主键：" + takeDisDeliveryOrderGoodsIn.getDeliveryOrderDetailsId() + "查询为空");
                throw new BusinessException("配货单:" + ordDisDelivery.getDeliveryOrderNo() + "明细ID(" + takeDisDeliveryOrderGoodsIn.getDeliveryOrderDetailsId() + ")不存在");
            }
            if (Objects.isNull(deliveryOrderDetails.getDeliveryQuantity())) {
                log.info("配货单{}明细商品{}，实配数为空", ordDisDelivery.getDeliveryOrderNo(), deliveryOrderDetails.getGoodsCode());
//                throw new BusinessException("配货单" + ordDisDelivery.getDeliveryOrderNo() + "明细商品" + deliveryOrderDetails.getGoodsCode() + "，实配数为空");
                return;
            }
            if (Objects.isNull(takeDisDeliveryOrderGoodsIn.getArrivalQuantity())) {
                String takeErrorMessage = "收货时商品(" + deliveryOrderDetails.getGoodsCode() + ")" + deliveryOrderDetails.getGoodsName() + "实收数为空";
                log.error("配货单：" + ordDisDelivery.getDeliveryOrderNo() + takeErrorMessage);
                throw new BusinessException(takeErrorMessage);
            }
            OrdDisDeliveryDetail updateDeliveryOrderDetails = new OrdDisDeliveryDetail();
            updateDeliveryOrderDetails.setId(deliveryOrderDetails.getId());
            updateDeliveryOrderDetails.setArrivalQuantity(takeDisDeliveryOrderGoodsIn.getArrivalQuantity());
            updateDeliveryOrderDetails.setArrivalPackageQuantity(takeDisDeliveryOrderGoodsIn.getArrivalQuantity().divide(deliveryOrderDetails.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
            updateDeliveryOrderDetails.setArrivalAmount(takeDisDeliveryOrderGoodsIn.getArrivalQuantity().multiply(deliveryOrderDetails.getOrderUnitPrice()));
            updateDeliveryOrderDetails.setUpdateTime(LocalDateTime.now());
            updateDeliveryOrderDetails.setUpdater(loginUsername);
            if (StringUtils.isNotBlank(deliveryOrderDetails.getExpiry()) && StringUtils.isBlank(takeDisDeliveryOrderGoodsIn.getExpiry())) {
                throw new BusinessException("明细" + deliveryOrderDetails.getGoodsCode() + deliveryOrderDetails.getGoodsName() + "效期码不能为空");
            }
            if (StringUtils.isNotBlank(deliveryOrderDetails.getExpiry()) && !deliveryOrderDetails.getExpiry().equals(takeDisDeliveryOrderGoodsIn.getExpiry())) {
                AdjustStoreExpiryMqIn adjustStoreExpiryMqIn = new AdjustStoreExpiryMqIn();
                com.edc.plugins.utils.bean.BeanUtils.copy(deliveryOrderDetails, adjustStoreExpiryMqIn);
                adjustStoreExpiryMqIn.setReason("X03");
                adjustStoreExpiryMqIn.setReduceExpiry(deliveryOrderDetails.getExpiry());
                adjustStoreExpiryMqIn.setReduceExpiryQty(deliveryOrderDetails.getDeliveryQuantity().negate());
                adjustStoreExpiryMqIn.setAddExpiry(takeDisDeliveryOrderGoodsIn.getExpiry());
                adjustStoreExpiryMqIn.setAddExpiryQty(deliveryOrderDetails.getDeliveryQuantity());
                adjustStoreExpiryMqInList.add(adjustStoreExpiryMqIn);
            }
            updateDeliveryOrderDetails.setExpiry(takeDisDeliveryOrderGoodsIn.getExpiry());
            ordDisDeliveryDetailService.updateByPrimaryKeySelective(updateDeliveryOrderDetails);
            // 实配数量和实收数量不同，表示有差异
            if (deliveryOrderDetails.getDeliveryQuantity().compareTo(takeDisDeliveryOrderGoodsIn.getArrivalQuantity()) != NumberUtil.INTEGER_ZERO
                    && StringUtils.isNotBlank(DifferenceOrderTypeEnum.getNameByCode(takeDisDeliveryOrderGoodsIn.getDiffenenceType()))) {
                List<OrdDisDelivDifferenceDetail> differenceGoodsList = differenceTypeGoodsMap.get(takeDisDeliveryOrderGoodsIn.getDiffenenceType());
                if (CollectionUtils.isEmpty(differenceGoodsList)) {
                    differenceGoodsList = Lists.newArrayList();
                }
                OrdDisDelivDifferenceDetail ordDisDelivDifferenceDetail = new OrdDisDelivDifferenceDetail();
                BeanUtils.copyProperties(deliveryOrderDetails, ordDisDelivDifferenceDetail);
                ordDisDelivDifferenceDetail.setExpiry(takeDisDeliveryOrderGoodsIn.getExpiry());
                // 单价 继承
                ordDisDelivDifferenceDetail.setDistributionUnitPrice(deliveryOrderDetails.getOrderUnitPrice());
                //申请差异数量
                BigDecimal applyDifferenceQuantity = takeDisDeliveryOrderGoodsIn.getArrivalQuantity().subtract(deliveryOrderDetails.getDeliveryQuantity());
                ordDisDelivDifferenceDetail.setApplyDifferenceQuantity(applyDifferenceQuantity);
                BigDecimal applyDifferenceAmount = applyDifferenceQuantity.multiply(deliveryOrderDetails.getOrderUnitPrice());
                ordDisDelivDifferenceDetail.setApplyDifferenceAmount(applyDifferenceAmount);
                ordDisDelivDifferenceDetail.setInvoiceType(deliveryOrderDetails.getInvoiceType());
                differenceGoodsList.add(ordDisDelivDifferenceDetail);
                differenceTypeGoodsMap.put(takeDisDeliveryOrderGoodsIn.getDiffenenceType(), differenceGoodsList);
            }
        });
        if (CollectionUtils.isNotEmpty(adjustStoreExpiryMqInList)) {
            SaveAdjustStoreExpiryOrderMqIn saveAdjustStoreExpiryOrderMqIn = new SaveAdjustStoreExpiryOrderMqIn();
            saveAdjustStoreExpiryOrderMqIn.setAdjustStoreExpiryMqInList(adjustStoreExpiryMqInList);
            saveAdjustStoreExpiryOrderMqIn.setStoreCode(ordDisDelivery.getStoreCode());
            saveAdjustStoreExpiryOrderMqIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
            saveAdjustStoreExpiryOrderMqIn.setRemark("由配销单" + ordDisDelivery.getDeliveryOrderNo() + "产生");
            saveAdjustStoreExpiryOrderMqIn.setLoginUsername(SystemConstant.SYSTEM_USER);
            SendResponse sendResponse = takeDeliveryAdjustExpirySender.sendSync(JSONObject.toJSONString(saveAdjustStoreExpiryOrderMqIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
            if (Objects.isNull(sendResponse)) {
                throw new BusinessException("配销单" + ordDisDelivery.getDeliveryOrderNo() + "发送效期库存调整单MQ消息异常");
            } else {
                log.info("{}发送效期库存调整单MQ消息消息ID----->{}", ordDisDelivery.getDeliveryOrderNo(), sendResponse.getMessageId());
            }
        }
        return differenceTypeGoodsMap;
    }


    /**
     * 移除收货信息缓存数据
     *
     * @param deliveryOrderId
     */
    public void removeTakeDeliveryCacheData(Long deliveryOrderId) {
        OrdDisDelivery disDelivery = ordDisDeliveryService.selectByPrimaryKey(deliveryOrderId);
        String key = DisSystemConstant.DIS_CACHE_TAKE_DELIVERY_ORDER_KEY + ":" + disDelivery.getBizOrgCode() + ":" + disDelivery.getDeliveryOrderNo();
        redisService.del(key);
    }

    /**
     * 自动收货
     */
    public void autoTakeDisDelivery() {
        List<OrdDisDelivery> newDeliveryOrderList = ordDisDeliveryService.findNeedAutoTakeDisDeliveryOrderList(LocalDateTime.now().toString(), DeliveryOrderEnum.SHIPPED.getKey());
        if (CollectionUtils.isEmpty(newDeliveryOrderList)) {
            return;
        }
        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        for (OrdDisDelivery deliveryOrder : newDeliveryOrderList) {
            deliveryOrder = ordDisDeliveryService.selectByPrimaryKey(deliveryOrder.getId());
            if (NumberUtils.INTEGER_ONE.equals(deliveryOrder.getIsReversal()) || NumberUtils.INTEGER_ONE.equals(deliveryOrder.getIsReversalOrder())) {
                continue;
            }
            if (!DeliveryOrderEnum.SHIPPED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
                continue;
            }
            // 判断是否已签收
//            OrdDisDeliveryOrderSigning deliveryOrderSigning = deliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(deliveryOrder.getId(), deliveryOrder.getBizOrgCode());
//            if (Objects.nonNull(deliveryOrderSigning) && !DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey().equals(deliveryOrder.getReceiveProgress())) {
            if (!DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey().equals(deliveryOrder.getReceiveProgress())) {
                continue;
            }
            List<TakeDisDeliveryOrderGoodsIn> list = ordDisDeliveryDetailService.findTakeDisDeliveryOrderGoodsListByDeliveryOrderId(deliveryOrder.getId());
            try {
                StockInfoOut stockInfoOut = stockInfoOutMap.get(deliveryOrder.getStockCode());
                if (Objects.isNull(stockInfoOut)) {
                    stockInfoOut = stockServer.getTransInfo(deliveryOrder.getStockCode());
                    if (Objects.isNull(stockInfoOut)) {
                        log.info("无此仓位{}信息", deliveryOrder.getStockCode());
                        continue;
                    } else {
                        stockInfoOutMap.put(deliveryOrder.getStockCode(), stockInfoOut);
                    }
                }
                TakeDisDeliveryOrderIn takeDisDeliveryOrderIn = new TakeDisDeliveryOrderIn();
                takeDisDeliveryOrderIn.setDeliveryOrderId(deliveryOrder.getId());
                takeDisDeliveryOrderIn.setTakeDeliveryOrderGoodsInList(list);
                takeDisDeliveryOrderIn.setTakeRemark("系统自动收货");
                takeDisDeliveryOrderIn.setLoginUsername(com.edc.erp.common.constant.SystemConstant.SYSTEM_USER);
                disDeliveryOrderHandle.handleAutoTakeDisDelivery(takeDisDeliveryOrderIn, deliveryOrder, stockInfoOut);
            } catch (Exception e) {
                log.error("自动收货异常" + e);
            }
        }
    }

    public Response<String> submitTakeDisDeliveryInfoToCache(CacheTakeDisDeliveryOrderIn cacheTakeDeliveryOrderIn) {
        OrdDisDelivery deliveryOrder = ordDisDeliveryService.selectByPrimaryKey(cacheTakeDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配货单");
        }
        if (DeliveryOrderEnum.COMPLETED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            return Response.error("配货单已经收货了");
        }
        if (!DeliveryOrderReceiveProgressEnum.ONGOING.getKey().equals(deliveryOrder.getReceiveProgress())) {
            return Response.error("只有收货中的配货单才能保存数据");
        }
        cacheTakeDeliveryOrderIn.getCacheTakeDeliveryOrderGoodsInList().forEach(cacheTakeDeliveryOrderGoodsIn -> {
            String key = DisSystemConstant.DIS_CACHE_TAKE_DELIVERY_ORDER_KEY + ":" + deliveryOrder.getBizOrgCode() + ":" + deliveryOrder.getDeliveryOrderNo();
            redisService.hSet(key, cacheTakeDeliveryOrderGoodsIn.getDeliveryOrderDetailsId().toString(), JSONObject.toJSONString(cacheTakeDeliveryOrderGoodsIn));
        });
        String beforeTakeDeliveryKey = DisSystemConstant.DIS_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY + ":" + cacheTakeDeliveryOrderIn.getBizOrgCode() + ":" + deliveryOrder.getDeliveryOrderNo();
        redisService.del(beforeTakeDeliveryKey);
        return Response.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Response<String> updateDisDeliveryOrderTakeOngoing(UpdateDisDeliveryOrderOngoingIn updateDeliveryOrderOngoingIn, String loginUsername) {
        OrdDisDelivery deliveryOrder = ordDisDeliveryService.selectByPrimaryKey(updateDeliveryOrderOngoingIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配货单");
        }
        if (DeliveryOrderEnum.COMPLETED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            return Response.error("配货单已经收货了");
        }
        if (!DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey().equals(deliveryOrder.getReceiveProgress())) {
            return Response.error("该配货单正在收货");
        }
        OrdDisDelivery updateDeliveryOrder = new OrdDisDelivery();
        updateDeliveryOrder.setId(deliveryOrder.getId());
        updateDeliveryOrder.setReceiveProgress(DeliveryOrderReceiveProgressEnum.ONGOING.getKey());
        updateDeliveryOrder.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        updateDeliveryOrder.setUpdater(loginUsername);
        updateDeliveryOrder.setUpdateTime(LocalDateTime.now());
        updateDeliveryOrder.setBizOrgCode(deliveryOrder.getBizOrgCode());
        updateDeliveryOrder.setOrgCode(deliveryOrder.getOrgCode());
        int updateCount = ordDisDeliveryService.updateDisDeliveryTakeOngoingById(updateDeliveryOrder);
        if (updateCount == 0) {
            throw new BusinessException("该配货单正在收货");
        }
        log.info("配货单{}更新收货进度为{}", deliveryOrder.getDeliveryOrderNo(), DeliveryOrderReceiveProgressEnum.ONGOING.getValue());
        // 注册收货心跳
        OrdDisDeliveryOrderHeartRateMonitor deliveryOrderHeartRateMonitor = new OrdDisDeliveryOrderHeartRateMonitor();
        deliveryOrderHeartRateMonitor.setDisDeliveryOrderId(deliveryOrder.getId());
        deliveryOrderHeartRateMonitor.setBizOrgCode(deliveryOrder.getBizOrgCode());
        deliveryOrderHeartRateMonitor.setOrgCode(deliveryOrder.getOrgCode());
        deliveryOrderHeartRateMonitor.setLastHeartbeatTime(LocalDateTime.now());
        deliveryOrderHeartRateMonitor.setCreator(loginUsername);
        deliveryOrderHeartRateMonitor.setCreateTime(LocalDateTime.now());
        deliveryOrderHeartRateMonitorService.saveDisDeliveryOrderHeartRateMonitor(deliveryOrderHeartRateMonitor);
        log.info("配货单{}注册心跳成功", deliveryOrder.getDeliveryOrderNo(), DeliveryOrderReceiveProgressEnum.ONGOING.getValue());
        return Response.success();
    }
}
