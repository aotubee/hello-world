package com.edc.erp.directly.handle;

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
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.*;
import com.edc.erp.directly.dirdeliveryorder.model.in.*;
import com.edc.erp.directly.dirdeliveryorder.service.*;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.directly.enumeration.AttachmentTypeEnum;
import com.edc.erp.directly.enumeration.DeliveryOrderReceiveProgressEnum;
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
public class TakeDirDeliveryHandle {
    @Autowired
    private RedisService redisService;

    @Autowired
    private OrdDirDeliveryService ordDirDeliveryService;

    @Autowired
    private OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    @Autowired
    private AsyncLogService logService;

    @Autowired
    private OrdDirDeliveryOrderHeartRateMonitorService deliveryOrderHeartRateMonitorService;

    @Autowired
    private OrdDirDeliveryOrderAttachmentService deliveryOrderAttachmentService;

    @Autowired
    private OrdDirDeliveryOrderSigningService deliveryOrderSigningService;

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
    private DirDeliveryOrderHandle dirDeliveryOrderHandle;

    @Autowired
    @Qualifier("dirDeliveryToDifferenceSender")
    private MessageSender dirDeliveryToDifferenceSender;

    /**
     * 收货
     *
     * @param takeDirDeliveryOrderIn
     * @param ordDirDelivery
     */
    @Transactional(rollbackFor = Exception.class)
    public void takeDirDelivery(TakeDirDeliveryOrderIn takeDirDeliveryOrderIn, OrdDirDelivery ordDirDelivery, StockInfoOut stockInfoOut) {
        List<TakeDirDeliveryOrderGoodsIn> takeDirDeliveryOrderGoodsInList = takeDirDeliveryOrderIn.getTakeDeliveryOrderGoodsInList();
        if (CollectionUtils.isEmpty(takeDirDeliveryOrderGoodsInList)) {
            throw new BusinessException("无商品可收货");
        }
        List<OrdDirDeliveryDetail> dirDeliveryDetailList = ordDirDeliveryDetailService.findDeliveryOrderDetails(ordDirDelivery.getId());
        // 处理收货，返回差异数据
        Map<String, List<OrdDirDelivDifferenceDetail>> differenceTypeGoodsMap = this.optTakeGoods(takeDirDeliveryOrderIn, ordDirDelivery,
                dirDeliveryDetailList, takeDirDeliveryOrderIn.getLoginUsername());
        // 处理差异商品
        this.optDifferenceGoods(ordDirDelivery, differenceTypeGoodsMap);
        //修改配货单状态
        OrdDirDelivery updateDeliveryOrder = new OrdDirDelivery();
        updateDeliveryOrder.setId(ordDirDelivery.getId());
        updateDeliveryOrder.setDeliveryStatusCode(DeliveryOrderEnum.RECEIVED.getKey());
        updateDeliveryOrder.setUpdateTime(LocalDateTime.now());
        updateDeliveryOrder.setUpdater(takeDirDeliveryOrderIn.getLoginUsername());
        updateDeliveryOrder.setReceiveTime(LocalDateTime.now());
        updateDeliveryOrder.setReceiveProgress(DeliveryOrderReceiveProgressEnum.END.getKey());
        updateDeliveryOrder.setTakeRemark(takeDirDeliveryOrderIn.getTakeRemark());
        ordDirDeliveryService.updateByPrimaryKeySelective(updateDeliveryOrder);
        //更新收货附件信息
        this.optAttachment(takeDirDeliveryOrderIn, ordDirDelivery);
        // 收货完成后清除缓存数据
        this.removeTakeDeliveryCacheData(ordDirDelivery.getId());
        //获取收货后明细信息
        List<OrdDirDeliveryDetail> updateDirDeliveryDetailList = ordDirDeliveryDetailService.findDeliveryOrderDetails(ordDirDelivery.getId());
        // 更新库存调整
        this.handleTakeDirDeliveryStock(ordDirDelivery, updateDirDeliveryDetailList, stockInfoOut);
        String takeContent;
        if (StringUtils.isNotBlank(takeDirDeliveryOrderIn.getTakeRemark())) {
            takeContent = MessageFormat.format(DeliveryOrderLogEnum.TAKE_DELIVERY.getKey(), "，" + takeDirDeliveryOrderIn.getTakeRemark());
        } else {
            takeContent = MessageFormat.format(DeliveryOrderLogEnum.TAKE_DELIVERY.getKey(), "。");
        }
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(ordDirDelivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), takeContent, new Date(), takeDirDeliveryOrderIn.getLoginUsername());
        logService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 收货处理库存
     *
     * @param ordDirDelivery
     */
    public void handleTakeDirDeliveryStock(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> dirDeliveryDetailList, StockInfoOut stockInfoOut) {
        // 门店库存调整
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_OUT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_OUT.getName());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        stockFlowIn.setBizOrgCode(ordDirDelivery.getBizOrgCode());
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(ordDirDelivery.getCreator());
        stockFlowIn.setOperationType(ordDirDelivery.getDeliveryStatusCode());
        stockFlowIn.setOrgCode(ordDirDelivery.getOrgCode());
        stockFlowIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        dirDeliveryDetailList.forEach(item -> {
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
            stockFlowGoodsIn.setStockCode(ordDirDelivery.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            stockFlowGoodsIn.setStoreCode(ordDirDelivery.getStoreCode());
            stockFlowGoodsIn.setStoreName(ordDirDelivery.getStoreName());
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
            stockFlowGoodsIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
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
     * @param takeDirDeliveryOrderIn
     * @param ordDirDelivery
     */
    @Transactional(rollbackFor = Exception.class)
    public void optAttachment(TakeDirDeliveryOrderIn takeDirDeliveryOrderIn, OrdDirDelivery ordDirDelivery) {
        deliveryOrderHeartRateMonitorService.deleteByDeliveryOrderId(ordDirDelivery.getId(), ordDirDelivery.getBizOrgCode());
        if (CollectionUtils.isNotEmpty(takeDirDeliveryOrderIn.getAttachmentUrlList())) {
            List<OrdDirDeliveryOrderAttachment> deliveryOrderAttachmentList = Lists.newArrayList();
            takeDirDeliveryOrderIn.getAttachmentUrlList().forEach(url -> {
                OrdDirDeliveryOrderAttachment deliveryOrderAttachment = new OrdDirDeliveryOrderAttachment();
                deliveryOrderAttachment.setDirDeliveryOrderId(ordDirDelivery.getId());
                deliveryOrderAttachment.setAttachmentType(AttachmentTypeEnum.TAKE_DELIVERY.getKey());
                deliveryOrderAttachment.setAttachmentUrl(url);
                deliveryOrderAttachment.setCreator(takeDirDeliveryOrderIn.getLoginUsername());
                deliveryOrderAttachment.setCreateTime(LocalDateTime.now());
                deliveryOrderAttachment.setOrgCode(ordDirDelivery.getOrgCode());
                deliveryOrderAttachment.setBizOrgCode(ordDirDelivery.getBizOrgCode());
                deliveryOrderAttachmentList.add(deliveryOrderAttachment);
            });
            deliveryOrderAttachmentService.batchSaveDeliveryOrderAttachment(deliveryOrderAttachmentList);
        }
    }

    /**
     * 处理差异商品
     *
     * @param ordDirDelivery
     * @param differenceTypeGoodsMap
     */
    private void optDifferenceGoods(OrdDirDelivery ordDirDelivery, Map<String, List<OrdDirDelivDifferenceDetail>> differenceTypeGoodsMap) {
        if (differenceTypeGoodsMap.size() > 0) {
            differenceTypeGoodsMap.forEach((diffenenceType, ordDirDelivDifferenceDetails) -> {
                BigDecimal totalApplyDifferenceQuantity = ordDirDelivDifferenceDetails.stream().map(OrdDirDelivDifferenceDetail::getApplyDifferenceQuantity).reduce(BigDecimal::add).get();
                BigDecimal totalApplyDifferenceAmount = ordDirDelivDifferenceDetails.stream().map(OrdDirDelivDifferenceDetail::getApplyDifferenceAmount).reduce(BigDecimal::add).get();
                SaveDifferenceIn saveDifferenceIn = new SaveDifferenceIn();
                saveDifferenceIn.setStockCode(ordDirDelivery.getStockCode());
                saveDifferenceIn.setBizOrgCode(ordDirDelivery.getBizOrgCode());
                saveDifferenceIn.setStoreCode(ordDirDelivery.getStoreCode());
                saveDifferenceIn.setStoreName(ordDirDelivery.getStoreName());
                saveDifferenceIn.setDeliveryOrderNo(ordDirDelivery.getDeliveryOrderNo());
                saveDifferenceIn.setDeliveryTime(ordDirDelivery.getDeliveryTime());
                saveDifferenceIn.setReceiveTime(ordDirDelivery.getReceiveTime());
                saveDifferenceIn.setOrgCode(ordDirDelivery.getOrgCode());
                saveDifferenceIn.setTotalApplyDifferenceQuantity(totalApplyDifferenceQuantity);
                saveDifferenceIn.setTotalApplyDifferenceAmount(totalApplyDifferenceAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
                saveDifferenceIn.setDifferenceType(diffenenceType);
                saveDifferenceIn.setWrhCode(ordDirDelivery.getWrhCode());
                saveDifferenceIn.setCreator(ordDirDelivery.getUpdater());
                saveDifferenceIn.setUpdater(ordDirDelivery.getUpdater());
                saveDifferenceIn.setDifferenceDetails(ordDirDelivDifferenceDetails);
                //异步生成差异单
//                asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_DELIVERY_TO_DIFFERENCE, JSONObject.toJSONString(saveDifferenceIn),
//                        ordDirDelivery.getBizOrgCode(), ordDirDelivery.getDeliveryOrderNo());
                dirDeliveryToDifferenceSender.sendSync(JSONObject.toJSONString(saveDifferenceIn).getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_FAST_TIME);
            });
        }
    }

    /**
     * 处理收货明细，返回差异数据
     *
     * @param takeDirDeliveryOrderIn
     * @param ordDirDelivery
     * @param loginUsername
     * @return
     */
    private Map<String, List<OrdDirDelivDifferenceDetail>> optTakeGoods(TakeDirDeliveryOrderIn takeDirDeliveryOrderIn, OrdDirDelivery ordDirDelivery,
                                                                        List<OrdDirDeliveryDetail> dirDeliveryDetailList, String loginUsername) {
        Map<String, List<OrdDirDelivDifferenceDetail>> differenceTypeGoodsMap = new LinkedHashMap<>();
        Map<Long, OrdDirDeliveryDetail> detailMap = dirDeliveryDetailList.stream().collect(Collectors.toMap(OrdDirDeliveryDetail::getId, Function.identity()));
        List<AdjustStoreExpiryMqIn> adjustStoreExpiryMqInList = Lists.newArrayList();
        takeDirDeliveryOrderIn.getTakeDeliveryOrderGoodsInList().forEach(takeDirDeliveryOrderGoodsIn -> {
            OrdDirDeliveryDetail dirDeliveryDetail = detailMap.get(takeDirDeliveryOrderGoodsIn.getDeliveryOrderDetailsId());
            if (Objects.isNull(dirDeliveryDetail)) {
                log.error("配货单：" + ordDirDelivery.getDeliveryOrderNo() + ",明细主键：" + takeDirDeliveryOrderGoodsIn.getDeliveryOrderDetailsId() + "查询为空");
                throw new BusinessException("配货单:" + ordDirDelivery.getDeliveryOrderNo() + "明细ID(" + takeDirDeliveryOrderGoodsIn.getDeliveryOrderDetailsId() + ")不存在");
            }
            if (Objects.isNull(dirDeliveryDetail.getDeliveryQuantity())) {
                log.info("配货单{}明细商品{}，实配数为空", ordDirDelivery.getDeliveryOrderNo(), dirDeliveryDetail.getGoodsCode());
//                throw new BusinessException("配货单" + ordDirDelivery.getDeliveryOrderNo() + "明细商品" + deliveryOrderDetails.getGoodsCode() + "，实配数为空");
                return;
            }
            if (Objects.isNull(takeDirDeliveryOrderGoodsIn.getArrivalQuantity())) {
                String takeErrorMessage = "收货时商品(" + dirDeliveryDetail.getGoodsCode() + ")" + dirDeliveryDetail.getGoodsName() + "实收数为空";
                log.error("配货单：" + ordDirDelivery.getDeliveryOrderNo() + takeErrorMessage);
                throw new BusinessException(takeErrorMessage);
            }
            OrdDirDeliveryDetail updateDeliveryOrderDetails = new OrdDirDeliveryDetail();
            updateDeliveryOrderDetails.setId(dirDeliveryDetail.getId());
            updateDeliveryOrderDetails.setArrivalQuantity(takeDirDeliveryOrderGoodsIn.getArrivalQuantity());
            updateDeliveryOrderDetails.setArrivalPackageQuantity(takeDirDeliveryOrderGoodsIn.getArrivalQuantity().divide(dirDeliveryDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN));
            updateDeliveryOrderDetails.setArrivalAmount(takeDirDeliveryOrderGoodsIn.getArrivalQuantity().multiply(dirDeliveryDetail.getOrderUnitPrice()));
            updateDeliveryOrderDetails.setUpdateTime(LocalDateTime.now());
            updateDeliveryOrderDetails.setUpdater(loginUsername);
            if (StringUtils.isNotBlank(dirDeliveryDetail.getExpiry()) && StringUtils.isBlank(takeDirDeliveryOrderGoodsIn.getExpiry())) {
                throw new BusinessException("明细" + dirDeliveryDetail.getGoodsCode() + dirDeliveryDetail.getGoodsName() + "效期码不能为空");
            }
            if (StringUtils.isNotBlank(dirDeliveryDetail.getExpiry()) && !dirDeliveryDetail.getExpiry().equals(takeDirDeliveryOrderGoodsIn.getExpiry())) {
                AdjustStoreExpiryMqIn adjustStoreExpiryMqIn = new AdjustStoreExpiryMqIn();
                com.edc.plugins.utils.bean.BeanUtils.copy(dirDeliveryDetail, adjustStoreExpiryMqIn);
                adjustStoreExpiryMqIn.setReason("X03");
                adjustStoreExpiryMqIn.setReduceExpiry(dirDeliveryDetail.getExpiry());
                adjustStoreExpiryMqIn.setReduceExpiryQty(dirDeliveryDetail.getDeliveryQuantity().negate());
                adjustStoreExpiryMqIn.setAddExpiry(takeDirDeliveryOrderGoodsIn.getExpiry());
                adjustStoreExpiryMqIn.setAddExpiryQty(dirDeliveryDetail.getDeliveryQuantity());
                adjustStoreExpiryMqInList.add(adjustStoreExpiryMqIn);
            }
            updateDeliveryOrderDetails.setExpiry(takeDirDeliveryOrderGoodsIn.getExpiry());
            ordDirDeliveryDetailService.updateByPrimaryKeySelective(updateDeliveryOrderDetails);
            // 实配数量和实收数量不同，表示有差异
            if (dirDeliveryDetail.getDeliveryQuantity().compareTo(takeDirDeliveryOrderGoodsIn.getArrivalQuantity()) != 0
                    && StringUtils.isNotBlank(DifferenceOrderTypeEnum.getNameByCode(takeDirDeliveryOrderGoodsIn.getDiffenenceType()))) {
                List<OrdDirDelivDifferenceDetail> differenceGoodsList = differenceTypeGoodsMap.get(takeDirDeliveryOrderGoodsIn.getDiffenenceType());
                if (CollectionUtils.isEmpty(differenceGoodsList)) {
                    differenceGoodsList = Lists.newArrayList();
                }
                OrdDirDelivDifferenceDetail ordDirDelivDifferenceDetail = new OrdDirDelivDifferenceDetail();
                BeanUtils.copyProperties(dirDeliveryDetail, ordDirDelivDifferenceDetail);
                ordDirDelivDifferenceDetail.setExpiry(takeDirDeliveryOrderGoodsIn.getExpiry());
                // 单价 继承
                ordDirDelivDifferenceDetail.setDistributionUnitPrice(dirDeliveryDetail.getOrderUnitPrice());
                //申请差异数量
                BigDecimal applyDifferenceQuantity = takeDirDeliveryOrderGoodsIn.getArrivalQuantity().subtract(dirDeliveryDetail.getDeliveryQuantity());
                ordDirDelivDifferenceDetail.setApplyDifferenceQuantity(applyDifferenceQuantity);
                BigDecimal applyDifferenceAmount = applyDifferenceQuantity.multiply(dirDeliveryDetail.getOrderUnitPrice());
                ordDirDelivDifferenceDetail.setApplyDifferenceAmount(applyDifferenceAmount);
                ordDirDelivDifferenceDetail.setInvoiceType(dirDeliveryDetail.getInvoiceType());
                differenceGoodsList.add(ordDirDelivDifferenceDetail);
                differenceTypeGoodsMap.put(takeDirDeliveryOrderGoodsIn.getDiffenenceType(), differenceGoodsList);
            }
        });
        if (CollectionUtils.isNotEmpty(adjustStoreExpiryMqInList)) {
            SaveAdjustStoreExpiryOrderMqIn saveAdjustStoreExpiryOrderMqIn = new SaveAdjustStoreExpiryOrderMqIn();
            saveAdjustStoreExpiryOrderMqIn.setAdjustStoreExpiryMqInList(adjustStoreExpiryMqInList);
            saveAdjustStoreExpiryOrderMqIn.setStoreCode(ordDirDelivery.getStoreCode());
            saveAdjustStoreExpiryOrderMqIn.setBizOrgCode(ordDirDelivery.getBizOrgCode());
            saveAdjustStoreExpiryOrderMqIn.setOrgCode(ordDirDelivery.getOrgCode());
            saveAdjustStoreExpiryOrderMqIn.setRemark("由配货单" + ordDirDelivery.getDeliveryOrderNo() + "产生");
            saveAdjustStoreExpiryOrderMqIn.setLoginUsername(SystemConstant.SYSTEM_USER);
            SendResponse sendResponse = takeDeliveryAdjustExpirySender.sendSync(JSONObject.toJSONString(saveAdjustStoreExpiryOrderMqIn).getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_FAST_TIME);
            if (Objects.isNull(sendResponse)) {
                throw new BusinessException("配货单" + ordDirDelivery.getDeliveryOrderNo() + "发送效期库存调整单MQ消息异常");
            } else {
                log.info("{}发送效期库存调整单MQ消息消息ID----->{}", ordDirDelivery.getDeliveryOrderNo(), sendResponse.getMessageId());
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
        OrdDirDelivery dirDelivery = ordDirDeliveryService.selectByPrimaryKey(deliveryOrderId);
        String key = DirSystemConstant.DIR_CACHE_TAKE_DELIVERY_ORDER_KEY + ":" + dirDelivery.getBizOrgCode() + ":" + dirDelivery.getDeliveryOrderNo();
        redisService.del(key);
    }

    /**
     * 自动收货
     */
    public void autoTakeDisDelivery() {
        List<OrdDirDelivery> newDeliveryOrderList = ordDirDeliveryService.findNeedAutoTakeDirDeliveryOrderList(LocalDateTime.now().toString(), DeliveryOrderEnum.SHIPPED.getKey());
        if (CollectionUtils.isEmpty(newDeliveryOrderList)) {
            return;
        }
        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        for (OrdDirDelivery deliveryOrder : newDeliveryOrderList) {
            deliveryOrder = ordDirDeliveryService.selectByPrimaryKey(deliveryOrder.getId());
            if (NumberUtils.INTEGER_ONE.equals(deliveryOrder.getIsReversal()) || NumberUtils.INTEGER_ONE.equals(deliveryOrder.getIsReversalOrder())) {
                continue;
            }
            if (!DeliveryOrderEnum.SHIPPED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
                continue;
            }
            // 判断收货进度
//            OrdDirDeliveryOrderSigning deliveryOrderSigning = deliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(deliveryOrder.getId(), deliveryOrder.getBizOrgCode());
//            if (Objects.nonNull(deliveryOrderSigning) && !DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey().equals(deliveryOrder.getReceiveProgress())) {
            if (!DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey().equals(deliveryOrder.getReceiveProgress())) {
                continue;
            }
            List<TakeDirDeliveryOrderGoodsIn> list = ordDirDeliveryDetailService.findTakeDirDeliveryOrderGoodsListByDeliveryOrderId(deliveryOrder.getId());
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
                TakeDirDeliveryOrderIn takeDirDeliveryOrderIn = new TakeDirDeliveryOrderIn();
                takeDirDeliveryOrderIn.setDeliveryOrderId(deliveryOrder.getId());
                takeDirDeliveryOrderIn.setTakeDeliveryOrderGoodsInList(list);
                takeDirDeliveryOrderIn.setTakeRemark("系统自动收货");
                takeDirDeliveryOrderIn.setLoginUsername(com.edc.erp.common.constant.SystemConstant.SYSTEM_USER);
                dirDeliveryOrderHandle.handleAutoTakeDirDelivery(takeDirDeliveryOrderIn, deliveryOrder, stockInfoOut);
            } catch (Exception e) {
                log.error("自动收货异常" + e);
            }
        }
    }

    public Response<String> submitTakeDirDeliveryInfoToCache(CacheTakeDirDeliveryOrderIn cacheTakeDeliveryOrderIn) {
        OrdDirDelivery deliveryOrder = ordDirDeliveryService.selectByPrimaryKey(cacheTakeDeliveryOrderIn.getDeliveryOrderId());
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
            String key = DirSystemConstant.DIR_CACHE_TAKE_DELIVERY_ORDER_KEY + ":" + deliveryOrder.getBizOrgCode() + ":" + deliveryOrder.getDeliveryOrderNo();
            redisService.hSet(key, cacheTakeDeliveryOrderGoodsIn.getDeliveryOrderDetailsId().toString(), JSONObject.toJSONString(cacheTakeDeliveryOrderGoodsIn));
        });
        String beforeTakeDeliveryKey = DirSystemConstant.DIR_LOGISTICS_DELIVERY_BEFORE_TAKE_DELIVERY_ORDER_KEY + ":" + cacheTakeDeliveryOrderIn.getBizOrgCode() + ":" + deliveryOrder.getDeliveryOrderNo();
        redisService.del(beforeTakeDeliveryKey);
        return Response.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Response<String> updateDirDeliveryOrderTakeOngoing(UpdateDirDeliveryOrderOngoingIn updateDeliveryOrderOngoingIn, String loginUsername) {
        OrdDirDelivery deliveryOrder = ordDirDeliveryService.selectByPrimaryKey(updateDeliveryOrderOngoingIn.getDeliveryOrderId());
        if (Objects.isNull(deliveryOrder)) {
            return Response.error("不存在的配货单");
        }
        if (DeliveryOrderEnum.COMPLETED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
            return Response.error("配货单已经收货了");
        }
        if (!DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey().equals(deliveryOrder.getReceiveProgress())) {
            return Response.error("该配货单正在收货");
        }
        OrdDirDelivery updateDeliveryOrder = new OrdDirDelivery();
        updateDeliveryOrder.setId(deliveryOrder.getId());
        updateDeliveryOrder.setReceiveProgress(DeliveryOrderReceiveProgressEnum.ONGOING.getKey());
        updateDeliveryOrder.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        updateDeliveryOrder.setUpdater(loginUsername);
        updateDeliveryOrder.setUpdateTime(LocalDateTime.now());
        updateDeliveryOrder.setBizOrgCode(deliveryOrder.getBizOrgCode());
        updateDeliveryOrder.setOrgCode(deliveryOrder.getOrgCode());
        int updateCount = ordDirDeliveryService.updateDirDeliveryTakeOngoingById(updateDeliveryOrder);
        if (updateCount == 0) {
            throw new BusinessException("该配货单正在收货");
        }
        log.info("配货单{}更新收货进度为{}", deliveryOrder.getDeliveryOrderNo(), DeliveryOrderReceiveProgressEnum.ONGOING.getValue());
        // 注册收货心跳
        OrdDirDeliveryOrderHeartRateMonitor deliveryOrderHeartRateMonitor = new OrdDirDeliveryOrderHeartRateMonitor();
        deliveryOrderHeartRateMonitor.setDirDeliveryOrderId(deliveryOrder.getId());
        deliveryOrderHeartRateMonitor.setBizOrgCode(deliveryOrder.getBizOrgCode());
        deliveryOrderHeartRateMonitor.setOrgCode(deliveryOrder.getOrgCode());
        deliveryOrderHeartRateMonitor.setLastHeartbeatTime(LocalDateTime.now());
        deliveryOrderHeartRateMonitor.setCreator(loginUsername);
        deliveryOrderHeartRateMonitor.setCreateTime(LocalDateTime.now());
        deliveryOrderHeartRateMonitorService.saveDirDeliveryOrderHeartRateMonitor(deliveryOrderHeartRateMonitor);
        log.info("配货单{}注册心跳成功", deliveryOrder.getDeliveryOrderNo(), DeliveryOrderReceiveProgressEnum.ONGOING.getValue());
        return Response.success();
    }
}
