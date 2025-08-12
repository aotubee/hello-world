package com.edc.erp.directly.dirdeliveryorder.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.entity.OrdStoreInventorySupplyRate;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.mapper.OrdStoreInventorySupplyRateMapper;
import com.edc.erp.common.model.entity.EquipmentStockAllot;
import com.edc.erp.common.model.in.store.QueryEquipmentStockAlloDetailIn;
import com.edc.erp.common.model.in.store.QueryEquipmentStockAllotIn;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreLogisticsOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPond;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPondDetail;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirSalvageDelivPondDetailMapper;
import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateStockDirDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirSalvageDelivPondDetailOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryDetailService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderExtendService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirSalvageDelivPondDetailService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirSalvageDelivPondService;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.dirrequestorder.mapper.OrdDirDelivRequestMapper;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.model.out.stock.OperationStockDetailsOut;
import com.edc.erp.reducestock.model.out.stock.OperationStockOut;
import com.edc.erp.reducestock.stock.service.StockWarehouseService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.util.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDirSalvageDelivPondDetailServiceImpl extends BaseServiceImpl<OrdDirSalvageDelivPondDetail> implements OrdDirSalvageDelivPondDetailService {

    private final OrdDirSalvageDelivPondDetailMapper ordDirSalvageDelivPondDetailMapper;

    private final StoreCenterService storeCenterService;

    private final OrdDirDeliveryOrderExtendService ordDirDeliveryOrderExtendService;

    private final OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    private final StockWarehouseService stockWarehouseService;

    private final StockServer stockServer;

    private final WarehouseServer warehouseServer;

    private final AsyncLogService asyncLogService;

    private final OrdDirSalvageDelivPondService ordDirSalvageDelivPondService;

    private final OrdDirDelivRequestMapper ordDirDelivRequestMapper;

    private final OrdStoreInventorySupplyRateMapper ordStoreInventorySupplyRateMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrdDirSalvageDelivPondDetails(List<OrdDirDelivery> ordDirDeliveryList, OrdDirOrderCycle orderCycle,
                                                  String loginUsername, String auditType, StoreLogisticsOut logistics) {
        if (CollectionUtils.isEmpty(ordDirDeliveryList)) {
            log.info("订货周期ID{}---{}下拆分配货配货单没有匹配的单子", orderCycle.getId(), orderCycle.getTruncationDateTime());
            return;
        }
        Long salvagePondId = null;
        // 如果是等待自动审核（正常截单拆的配销单），创建捞单主表
        if (SalvageAuditTypeEnum.WAIT_AUTO_AUDIT.getCode().equals(auditType)) {
            LocalDateTime truncationDateTime = orderCycle.getTruncationDateTime();
            OrdDirSalvageDelivPond ordDirSalvageDelivPond = new OrdDirSalvageDelivPond();
            ordDirSalvageDelivPond.setOrderTypeConfigId(orderCycle.getOrderTypeConfigId());
            ordDirSalvageDelivPond.setTruncationDateTime(truncationDateTime);
            ordDirSalvageDelivPond.setBizOrgCode(orderCycle.getBizOrgCode());
            ordDirSalvageDelivPond = ordDirSalvageDelivPondService.getOneByParameter(orderCycle.getOrderTypeConfigId(),
                    orderCycle.getBizOrgCode(), orderCycle.getTruncationDateTime());
            if (Objects.isNull(ordDirSalvageDelivPond)) {
                // 创建捞单池记录
                ordDirSalvageDelivPond = ordDirSalvageDelivPondService.saveDirSalvageDelivPond(orderCycle.getOrderTypeConfigId(), truncationDateTime, orderCycle.getBizOrgCode(), orderCycle.getOrgCode(), SystemConstant.SYSTEM_USER);
                log.info("业务组织代码{}，订单类型ID{}，截单时间{}，配货捞单池记录已创建", orderCycle.getBizOrgCode(), orderCycle.getOrderTypeConfigId(), orderCycle.getTruncationDateTime());
            }
            salvagePondId = ordDirSalvageDelivPond.getId();
        }
        List<QueryEquipmentStockAlloDetailIn> stockAlloDetailInList = ordDirDeliveryList.stream().map(ordDirDelivery -> {
            QueryEquipmentStockAlloDetailIn queryEquipmentStockAlloDetailIn = new QueryEquipmentStockAlloDetailIn();
            queryEquipmentStockAlloDetailIn.setStockCode(ordDirDelivery.getStockCode());
            queryEquipmentStockAlloDetailIn.setWarehouseCode(ordDirDelivery.getWrhCode());
            return queryEquipmentStockAlloDetailIn;
        }).collect(Collectors.toList());
        QueryEquipmentStockAllotIn queryEquipmentStockAllotIn = new QueryEquipmentStockAllotIn();
        queryEquipmentStockAllotIn.setBizOrgCode(orderCycle.getBizOrgCode());
        queryEquipmentStockAllotIn.setStockAlloDetailInList(stockAlloDetailInList);
        // 获取仓储库存分配配置
        // TODO lock
        Map<String, EquipmentStockAllot> equipmentStockAllotMap = storeCenterService.findByWmsCodeAndStockCode(queryEquipmentStockAllotIn);
        if (null == equipmentStockAllotMap) {
            equipmentStockAllotMap = new HashMap<>();
        }
        Integer roomDistPry = null;
        Integer frozenDistPry = null;
        if (Objects.nonNull(logistics)) {
            roomDistPry = logistics.getRoomDistPry();
            frozenDistPry = logistics.getFrozenDistPry();
        }
        for (OrdDirDelivery ordDirDelivery : ordDirDeliveryList) {
            EquipmentStockAllot equipmentStockAllot = equipmentStockAllotMap.get(ordDirDelivery.getStockCode() + ordDirDelivery.getWrhCode());
            String deliveryOrderSource = DeliveryOrderSourceCodeEnum.getNameByType(ordDirDelivery.getSourceCode()) +
                    "创建" + SalvageAuditTypeEnum.getTagNameByCode(auditType) + "配货单" + ordDirDelivery.getDeliveryOrderNo();
            log.info("捞单池保存明细，来源{}", deliveryOrderSource);
            String allotRule;
            if (Objects.isNull(equipmentStockAllot)) {
                allotRule = AllotRuleEnum.STORE_ORDER.getCode();
                log.error("捞单池保存明细，配货单{}：仓位{}仓储{}未找到仓储库存分配配置，故规则默认为第一笔订单时间", ordDirDelivery.getDeliveryOrderNo(),
                        ordDirDelivery.getStockCode(), ordDirDelivery.getWrhCode());
            } else {
                allotRule = equipmentStockAllot.getAllotRule();
            }
            OrdDirSalvageDelivPondDetail ordDirSalvageDelivPondDetail = initOrdDisSalvageDelivPondDetail(ordDirDelivery, salvagePondId,
                    roomDistPry, frozenDistPry, allotRule, orderCycle.getFirstOrderTime(), auditType);
            ordDirSalvageDelivPondDetail.setCreator(loginUsername);
            ordDirSalvageDelivPondDetail.setUpdater(loginUsername);
            log.info("订货周期ID{}-----{}下拆分配货配货单{}保存至捞单池id{}明细成功", orderCycle.getId(), orderCycle.getTruncationDateTime(), ordDirDelivery.getDeliveryOrderNo(), salvagePondId);
            ordDirSalvageDelivPondDetailMapper.insertSelective(ordDirSalvageDelivPondDetail);
            String content = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_SALVAGE.getKey(),
                    ordDirDelivery.getDeliveryOrderNo(), DateUtils.format(orderCycle.getTruncationDateTime()));
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(ordDirDelivery.getId()),
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), content, new Date(), loginUsername);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
    }

    @Override
    public List<OrdDirSalvageDelivPondDetailOut> findListBySalvagePondId(Long salvagePondId) {
        OrdDirSalvageDelivPondDetail ordDirSalvageDelivPondDetail = new OrdDirSalvageDelivPondDetail();
        ordDirSalvageDelivPondDetail.setSalvagePondId(salvagePondId);
        ordDirSalvageDelivPondDetail.setIsDelete(ModelConst.DELETE.NO);
        List<OrdDirSalvageDelivPondDetail> list = ordDirSalvageDelivPondDetailMapper.select(ordDirSalvageDelivPondDetail);
        Map<String, StockInfoOut> stockMap = new HashMap<>();
        List<OrdDirSalvageDelivPondDetailOut> ordDirSalvageDelivPondDetailOutList = list.stream().map(detail -> {
            OrdDirSalvageDelivPondDetailOut ordDirSalvageDelivPondDetailOut = new OrdDirSalvageDelivPondDetailOut();
            BeanUtils.copy(detail, ordDirSalvageDelivPondDetailOut);
            StockInfoOut stockInfoOut = stockMap.get(ordDirSalvageDelivPondDetailOut.getStockCode());
            if (Objects.isNull(stockInfoOut)) {
                stockInfoOut = stockServer.getTransInfo(ordDirSalvageDelivPondDetailOut.getStockCode());
                if (Objects.nonNull(stockInfoOut)) {
                    stockMap.put(stockInfoOut.getStockCode(), stockInfoOut);
                }
            }
            ordDirSalvageDelivPondDetailOut.setCenterStockBizOrgCode(stockInfoOut.getBizOrgCode());
            ordDirSalvageDelivPondDetailOut.setCenterStockOrgCode(stockInfoOut.getOrgCode());
            return ordDirSalvageDelivPondDetailOut;
        }).collect(Collectors.toList());
        return ordDirSalvageDelivPondDetailOutList;
    }

    @Override
    public List<OperationStockOut> handleSalvage(String channelBizOrgCode, String loginUsername, Boolean isRecalculateOccupancyQty, List<OrdDirSalvageDelivPondDetailOut> ordDirSalvageDelivPondDetailOutList) {
        List<OperationStockOut> allOperationStockOutList = Lists.newArrayList();
        // 按规则分组+仓位
        Map<String, List<OrdDirSalvageDelivPondDetailOut>> allotRuleMap = ordDirSalvageDelivPondDetailOutList.stream()
                .collect(Collectors.groupingBy(v -> v.getAllotRule() + SystemConstant.COLON + v.getStockCode()));
        for (Map.Entry<String, List<OrdDirSalvageDelivPondDetailOut>> entry : allotRuleMap.entrySet()) {
            String[] keyArray = entry.getKey().split(SystemConstant.COLON);
            String allotRule = keyArray[0];
            String stockCode = keyArray[1];
            List<OrdDirSalvageDelivPondDetailOut> detailList = entry.getValue();
            log.info("------------开始处理业务组织{}下仓储库存分配规则为{}的配货单占库存--------------", allotRule);
            List<OrdDirSalvageDelivPondDetailOut> dirSalvageDelivPondDetailSortList = null;
            if (AllotRuleEnum.STORE_CODE_SIZE.getCode().equals(allotRule)) {
                dirSalvageDelivPondDetailSortList = this.orderByStoreCode(detailList);
            }
            if (AllotRuleEnum.STORE_ORDER.getCode().equals(allotRule)) {
                dirSalvageDelivPondDetailSortList = this.orderByFirstStoreOrder(detailList);
            }
            if (AllotRuleEnum.STORE_SALES.getCode().equals(allotRule)) {
                dirSalvageDelivPondDetailSortList = this.orderByStoreSale(detailList, stockCode);
            }
            if (CollectionUtils.isEmpty(dirSalvageDelivPondDetailSortList)) {
                continue;
            }
            List<StockFlowIn> stockStoreFlowIns = Lists.newArrayList();
            dirSalvageDelivPondDetailSortList.forEach(ordDirSalvageDelivPondDetailOut -> {
                OrdDirDelivery ordDirDelivery = ordDirDeliveryOrderExtendService.getOneByIdAndBizOrgCode(ordDirSalvageDelivPondDetailOut.getDeliveryOrderId(), channelBizOrgCode);
                if (!DeliveryOrderEnum.PENDING.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
                    return;
                }
                List<OrdDirDeliveryDetail> dirDeliveryDetailList = ordDirDeliveryDetailService.findDeliveryOrderDetails(ordDirDelivery.getId());
                if (isRecalculateOccupancyQty) {
                    OrdDirDelivRequest ordDirDelivRequest = ordDirDelivRequestMapper.getRequestOrderByDeliveryOrderIdAndBizOrgCode(ordDirDelivery.getId(), ordDirDelivery.getBizOrgCode());
                    OrdStoreInventorySupplyRate ordStoreInventorySupplyRate = new OrdStoreInventorySupplyRate();
                    ordStoreInventorySupplyRate.setStoreCode(ordDirDelivery.getStoreCode());
                    ordStoreInventorySupplyRate.setOrderCycleTime(ordDirDelivRequest.getTruncationDateTime());
                    ordStoreInventorySupplyRate.setDeliveryOrderId(ordDirDelivery.getId());
                    List<OrdStoreInventorySupplyRate> storeInventorySupplyRateList = ordStoreInventorySupplyRateMapper.select(ordStoreInventorySupplyRate);
                    if (CollectionUtils.isNotEmpty(storeInventorySupplyRateList)) {
                        Map<String, OrdStoreInventorySupplyRate> supplyRateMap = storeInventorySupplyRateList.stream().collect(Collectors.toMap(OrdStoreInventorySupplyRate::getGoodsCode, Function.identity()));
                        dirDeliveryDetailList.forEach(ordDisDeliveryDetail -> {
                            OrdStoreInventorySupplyRate storeInventorySupplyRate = supplyRateMap.get(ordDisDeliveryDetail.getGoodsCode());
                            if (Objects.nonNull(storeInventorySupplyRate)) {
                                ordDisDeliveryDetail.setOrderQuantity(storeInventorySupplyRate.getAllocationQuantity());
                            }
                        });
                    }
                }
                StockFlowIn stockFlowIn = new StockFlowIn();
                stockFlowIn.setBizOrgCode(ordDirSalvageDelivPondDetailOut.getCenterStockBizOrgCode());
                stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_OUT.getCode());
                stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_OUT.getName());
                stockFlowIn.setFlowDate(LocalDateTime.now());
                stockFlowIn.setCreator(ordDirDelivery.getCreator());
                stockFlowIn.setOrgCode(ordDirSalvageDelivPondDetailOut.getCenterStockOrgCode());
                stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
                stockFlowIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
                // 初始化占库存参数明细
                List<StockFlowGoodsIn> stockFlowGoodsIns = this.initStockFlowGoodsIns(ordDirDelivery, dirDeliveryDetailList,
                        ordDirSalvageDelivPondDetailOut.getCenterStockBizOrgCode());
                stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
                stockStoreFlowIns.add(stockFlowIn);
            });
            // 请求占用库存
            Response<List<OperationStockOut>> response = stockWarehouseService.handleDeliveryOrderStock(stockStoreFlowIns);
            log.info("----------------------->" + JSON.toJSONString(response));
            if (!response.isSuccess() || Objects.isNull(response.getData())) {
                log.error(response.getMessage());
                return null;
            }
            List<OperationStockOut> operationStockOutList = response.getData();
            if (CollectionUtils.isNotEmpty(operationStockOutList)) {
                allOperationStockOutList.addAll(operationStockOutList);
            }
            log.info("------------结束处理业务组织{}下仓储库存分配规则为{}的配货单占库存--------------", allotRule);
        }
        return allOperationStockOutList;
    }


    /**
     * 封装配货单占库存后数据
     *
     * @param operationStockOutList
     * @param loginUsername
     * @param bizOrgCode
     * @return
     */
    @Override
    public List<UpdateStockDirDeliveryOrderIn> initUpdateStockDeliveryOrderInList(List<OperationStockOut> operationStockOutList, String loginUsername, String bizOrgCode) {
        if (CollectionUtils.isEmpty(operationStockOutList)) {
            return null;
        }
        List<UpdateStockDirDeliveryOrderIn> updateStockDirDeliveryOrderInList = Lists.newArrayList();
        // 处理成功占用库存后
        operationStockOutList.forEach(operationStockOut -> {
            String deliveryOrderNo = operationStockOut.getBusinessOrderNo();
            log.info("配货单{}审核占库存结果：{}", deliveryOrderNo, JSONObject.toJSONString(operationStockOut));
            OrdDirDelivery ordDirDelivery = ordDirDeliveryOrderExtendService.getOneByDeliveryOrderNo(deliveryOrderNo, DeliveryOrderEnum.PENDING.getKey(), bizOrgCode);
            if (Objects.isNull(ordDirDelivery)) {
                log.error("直营配货配货单{}不存在或者状态非待审核", deliveryOrderNo);
                return;
            }
            List<OrdDirDeliveryDetail> updateDirtributionInfoList = Lists.newArrayList();
            List<OperationStockDetailsOut> operationStockDetailsOutList = operationStockOut.getOperationStockDetailsOutList();
            // 封装明细
            if (CollectionUtils.isNotEmpty(operationStockDetailsOutList)) {
                List<OrdDirDeliveryDetail> ordDirDeliveryDetails = ordDirDeliveryDetailService.findDeliveryOrderDetails(ordDirDelivery.getId());
                Map<String, OrdDirDeliveryDetail> detailMap = ordDirDeliveryDetails.stream()
                        .collect(Collectors.toMap(detail -> detail.getGoodsCode() + SystemConstant.SHORT_LINE + detail.getIsGift() + SystemConstant.SHORT_LINE + detail.getBaseGoodsCode(), Function.identity()));
                operationStockDetailsOutList.forEach(operationStockDetailsOut -> {
                    OrdDirDeliveryDetail dbDetail = detailMap.get(operationStockDetailsOut.getGoodsCode() + SystemConstant.SHORT_LINE + operationStockDetailsOut.getIsGift() + SystemConstant.SHORT_LINE + operationStockDetailsOut.getBaseGoodsCode());
                    if (Objects.isNull(dbDetail)) {
                        return;
                    }
                    OrdDirDeliveryDetail ordDirDeliveryDetail = new OrdDirDeliveryDetail();
                    ordDirDeliveryDetail.setId(dbDetail.getId());
                    ordDirDeliveryDetail.setLine(dbDetail.getLine());
                    ordDirDeliveryDetail.setOrderUnitPrice(dbDetail.getOrderUnitPrice());
                    ordDirDeliveryDetail.setDistributionUnitPrice(dbDetail.getDistributionUnitPrice());
                    ordDirDeliveryDetail.setGoodsCode(dbDetail.getGoodsCode());
                    ordDirDeliveryDetail.setStoreCostAmount(dbDetail.getStoreCostAmount());
                    ordDirDeliveryDetail.setOrgGoodsId(dbDetail.getOrgGoodsId());
                    ordDirDeliveryDetail.setDistributionQuantity(operationStockDetailsOut.getAuditsQuantity());
                    ordDirDeliveryDetail.setDistributionPackageQuantity(ordDirDeliveryDetail.getDistributionQuantity()
                            .divide(dbDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDirDeliveryDetail.setDistributionAmount(operationStockDetailsOut.getAuditsQuantity().multiply(dbDetail.getOrderUnitPrice()));
                    ordDirDeliveryDetail.setUpdater(loginUsername);
                    ordDirDeliveryDetail.setUpdateTime(LocalDateTime.now());
                    //税额
                    BigDecimal sellTax = Objects.isNull(dbDetail.getSellTax()) ? BigDecimal.ZERO : dbDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
                    BigDecimal tax = sellTax.add(BigDecimal.ONE);
                    ordDirDeliveryDetail.setDistributionExceptTaxAmount(ordDirDeliveryDetail.getDistributionAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDirDeliveryDetail.setDistributionTaxAmount(ordDirDeliveryDetail.getDistributionAmount().subtract(ordDirDeliveryDetail.getDistributionExceptTaxAmount()));
                    BigDecimal distributionQuantity = Objects.isNull(ordDirDeliveryDetail.getDistributionQuantity()) ? BigDecimal.ZERO : ordDirDeliveryDetail.getDistributionQuantity();
                    ordDirDeliveryDetail.setWrhCostAmount(dbDetail.getWrhPrice().multiply(distributionQuantity));
                    ordDirDeliveryDetail.setWrhExceptTaxAmount(ordDirDeliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDirDeliveryDetail.setWrhTaxAmount(ordDirDeliveryDetail.getWrhCostAmount().subtract(ordDirDeliveryDetail.getWrhExceptTaxAmount()));
                    ordDirDeliveryDetail.setStoreCostAmount(dbDetail.getStoreStockPrice().multiply(distributionQuantity));
                    ordDirDeliveryDetail.setStoreExceptTaxAmount(ordDirDeliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDirDeliveryDetail.setStoreTaxAmount(ordDirDeliveryDetail.getStoreCostAmount().subtract(ordDirDeliveryDetail.getStoreExceptTaxAmount()));
                    updateDirtributionInfoList.add(ordDirDeliveryDetail);
                });
            }
            String deliveryStatusCode = ordDirDelivery.getDeliveryStatusCode();
            BigDecimal distributionQuantity = BigDecimal.ZERO;
            BigDecimal distributionAmount = BigDecimal.ZERO;
            // 是否整单缺货
            if (operationStockOut.getIsStockOutAll() == 1) {
                deliveryStatusCode = DeliveryOrderEnum.INVALID.getKey();
            }
            // 部分缺货
            if (operationStockOut.getIsStockOutAll() == 0) {
                deliveryStatusCode = DeliveryOrderEnum.APPROVED.getKey();
                distributionQuantity = updateDirtributionInfoList.stream().map(OrdDirDeliveryDetail::getDistributionQuantity).reduce(BigDecimal::add).get();
                distributionAmount = updateDirtributionInfoList.stream().map(OrdDirDeliveryDetail::getDistributionAmount).reduce(BigDecimal::add).get();
            }
            OrdDirDelivery updateOrdDirDelivery = new OrdDirDelivery();
            BeanUtils.copy(ordDirDelivery, updateOrdDirDelivery);
            updateOrdDirDelivery.setId(ordDirDelivery.getId());
            updateOrdDirDelivery.setDeliveryStatusCode(deliveryStatusCode);
            updateOrdDirDelivery.setUpdater(loginUsername);
            updateOrdDirDelivery.setDistributionQuantity(distributionQuantity);
            updateOrdDirDelivery.setDistributionAmount(distributionAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
            updateOrdDirDelivery.setDistributionTime(LocalDateTime.now());
            updateOrdDirDelivery.setUpdateTime(LocalDateTime.now());
            // 配货单占库存审核更新入参
            UpdateStockDirDeliveryOrderIn updateStockDirDeliveryOrderIn = new UpdateStockDirDeliveryOrderIn();
            updateStockDirDeliveryOrderIn.setIsStockOutAll(operationStockOut.getIsStockOutAll());
            updateStockDirDeliveryOrderIn.setOrdDirDelivery(updateOrdDirDelivery);
            updateStockDirDeliveryOrderIn.setBeforeDeliveryStatus(ordDirDelivery.getDeliveryStatusCode());
            updateStockDirDeliveryOrderIn.setStockOutLog(operationStockOut.getStockOutLog());
            updateStockDirDeliveryOrderIn.setDirDeliveryDetailList(updateDirtributionInfoList);
            updateStockDirDeliveryOrderInList.add(updateStockDirDeliveryOrderIn);
        });
        return updateStockDirDeliveryOrderInList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveForManualCreateDeliveryOrder(OrdDirDelivery ordDirDelivery, String auditType) {
        StoreLogisticsOut logistics = storeCenterService.getStoreLogisticsByStoreCode(ordDirDelivery.getStoreCode(), ordDirDelivery.getBizOrgCode());
        if (Objects.isNull(logistics)) {
            throw new BusinessException("门店" + ordDirDelivery.getStoreCode() + "未配置配送信息");
        }
        Integer roomDistPry = logistics.getRoomDistPry();
        Integer frozenDistPry = logistics.getFrozenDistPry();
        QueryEquipmentStockAlloDetailIn queryEquipmentStockAlloDetailIn = new QueryEquipmentStockAlloDetailIn();
        queryEquipmentStockAlloDetailIn.setStockCode(ordDirDelivery.getStockCode());
        queryEquipmentStockAlloDetailIn.setWarehouseCode(ordDirDelivery.getWrhCode());
        List<QueryEquipmentStockAlloDetailIn> stockAlloDetailInList = com.google.common.collect.Lists.newArrayList();
        stockAlloDetailInList.add(queryEquipmentStockAlloDetailIn);
        QueryEquipmentStockAllotIn queryEquipmentStockAllotIn = new QueryEquipmentStockAllotIn();
        queryEquipmentStockAllotIn.setBizOrgCode(ordDirDelivery.getBizOrgCode());
        queryEquipmentStockAllotIn.setStockAlloDetailInList(stockAlloDetailInList);
        Map<String, EquipmentStockAllot> equipmentStockAllotMap = storeCenterService.findByWmsCodeAndStockCode(queryEquipmentStockAllotIn);
        EquipmentStockAllot equipmentStockAllot = equipmentStockAllotMap.get(ordDirDelivery.getStockCode() + ordDirDelivery.getWrhCode());
        String allotRule;
        if (Objects.isNull(equipmentStockAllot)) {
            allotRule = AllotRuleEnum.STORE_ORDER.getCode();
            log.error("运营端手动创建捞单池明细，配货单{}：仓位{}仓储{}未找到仓储库存分配配置，故规则默认为第一笔订单时间", ordDirDelivery.getDeliveryOrderNo(),
                    ordDirDelivery.getStockCode(), ordDirDelivery.getWrhCode());
        } else {
            allotRule = equipmentStockAllot.getAllotRule();
        }
        OrdDirSalvageDelivPondDetail ordDirSalvageDelivPondDetail = initOrdDisSalvageDelivPondDetail(ordDirDelivery, null,
                roomDistPry, frozenDistPry, allotRule, ordDirDelivery.getCreateTime(), auditType);
        ordDirSalvageDelivPondDetail.setCreator(ordDirDelivery.getCreator());
        ordDirSalvageDelivPondDetail.setUpdater(ordDirDelivery.getUpdater());
        log.info("非订单流创建配货配货单{}保存至无主捞单池明细成功", ordDirDelivery.getDeliveryOrderNo());
        ordDirSalvageDelivPondDetailMapper.insert(ordDirSalvageDelivPondDetail);
        String content = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_NO_SALVAGE.getKey(), ordDirDelivery.getDeliveryOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(ordDirDelivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), content, new Date(), ordDirDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public OrdDirSalvageDelivPondDetail getOneByDeliveryOrderId(Long deliveryOrderId) {
        OrdDirSalvageDelivPondDetail ordDirSalvageDelivPondDetail = new OrdDirSalvageDelivPondDetail();
        ordDirSalvageDelivPondDetail.setDeliveryOrderId(deliveryOrderId);
        ordDirSalvageDelivPondDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDirSalvageDelivPondDetailMapper.selectOne(ordDirSalvageDelivPondDetail);
    }

    @Override
    public List<OrdDirSalvageDelivPondDetailOut> findByDeliveryOrderIdList(List<Long> deliveryOrderIdList) {
        return ordDirSalvageDelivPondDetailMapper.findByDeliveryOrderIdList(deliveryOrderIdList);
    }

    @Override
    public List<Long> findDeliveryOrderIdListBySalvagePondId(Long salvagePondId) {
        return ordDirSalvageDelivPondDetailMapper.findDeliveryOrderIdListBySalvagePondId(salvagePondId);
    }

    private OrdDirSalvageDelivPondDetail initOrdDisSalvageDelivPondDetail(OrdDirDelivery ordDirDelivery, Long salvagePondId, Integer roomDistPry,
                                                                          Integer frozenDistPry, String allotRule, LocalDateTime firstOrderTime, String auditType) {
        OrdDirSalvageDelivPondDetail ordDirSalvageDelivPondDetail = new OrdDirSalvageDelivPondDetail();
        ordDirSalvageDelivPondDetail.setSalvagePondId(salvagePondId);
        ordDirSalvageDelivPondDetail.setStoreCode(ordDirDelivery.getStoreCode());
        ordDirSalvageDelivPondDetail.setFirstOrderTime(firstOrderTime);
        ordDirSalvageDelivPondDetail.setDeliveryOrderId(ordDirDelivery.getId());
        ordDirSalvageDelivPondDetail.setStockCode(ordDirDelivery.getStockCode());
        ordDirSalvageDelivPondDetail.setWrhCode(ordDirDelivery.getWrhCode());
        ordDirSalvageDelivPondDetail.setAllotRule(allotRule);
        ordDirSalvageDelivPondDetail.setRoomPry(roomDistPry);
        ordDirSalvageDelivPondDetail.setAuditType(auditType);
        ordDirSalvageDelivPondDetail.setFrozenPry(frozenDistPry);
        ordDirSalvageDelivPondDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDirSalvageDelivPondDetail;
    }

    /**
     * 按门店大小排序
     *
     * @param detailList
     * @return
     */
    private List<OrdDirSalvageDelivPondDetailOut> orderByStoreCode(List<OrdDirSalvageDelivPondDetailOut> detailList) {
        int day = LocalDate.now().getDayOfMonth();
        int evenNumber = 2;
        LinkedHashMap<String, List<OrdDirSalvageDelivPondDetailOut>> map;
        // 判断双日还是单日
        if (day % evenNumber == 0) {
            // 双日降序
            map = detailList.stream().sorted(Comparator.comparing(OrdDirSalvageDelivPondDetailOut::getStoreCode)
                    .reversed()).collect(Collectors.groupingBy(OrdDirSalvageDelivPondDetailOut::getStoreCode, LinkedHashMap::new, Collectors.toList()));
        } else {
            //单日升序
            map = detailList.stream().sorted(Comparator.comparing(OrdDirSalvageDelivPondDetailOut::getStoreCode))
                    .collect(Collectors.groupingBy(OrdDirSalvageDelivPondDetailOut::getStoreCode, LinkedHashMap::new, Collectors.toList()));
        }
        return this.mergeSalvageDetailAfterSort(map);
    }

    /**
     * 按第一笔下单时间排序
     *
     * @param detailList
     * @return
     */
    private List<OrdDirSalvageDelivPondDetailOut> orderByFirstStoreOrder(List<OrdDirSalvageDelivPondDetailOut> detailList) {
        LinkedHashMap<String, List<OrdDirSalvageDelivPondDetailOut>> map = detailList.stream().sorted(Comparator.comparing(OrdDirSalvageDelivPondDetailOut::getFirstOrderTime))
                .collect(Collectors.groupingBy(OrdDirSalvageDelivPondDetailOut::getStoreCode, LinkedHashMap::new, Collectors.toList()));
        return this.mergeSalvageDetailAfterSort(map);
    }

    /**
     * 合并排序后捞单明细，将有序LinkedHashMap中的value合并至一个List
     *
     * @param map
     * @return
     */
    private List<OrdDirSalvageDelivPondDetailOut> mergeSalvageDetailAfterSort(LinkedHashMap<String, List<OrdDirSalvageDelivPondDetailOut>> map) {
        List<OrdDirSalvageDelivPondDetailOut> list = Lists.newArrayList();
        for (List<OrdDirSalvageDelivPondDetailOut> ordDirSalvageDelivPondDetailOutList : map.values()) {
            list.addAll(ordDirSalvageDelivPondDetailOutList);
        }
        return list;
    }

    /**
     * 按照销售优先级排序
     *
     * @param detailList
     * @param stockCode
     * @return
     */
    private List<OrdDirSalvageDelivPondDetailOut> orderByStoreSale(List<OrdDirSalvageDelivPondDetailOut> detailList, String stockCode) {
        // -------------------开始处理未维护销售优先级的--------------------
        // 查找优先级为空的捞单池明细
        List<OrdDirSalvageDelivPondDetailOut> emptyDistPryList = detailList.stream()
                .filter(ordDirSalvageDelivPondDetailOut -> this.checkDistPryEmpty(ordDirSalvageDelivPondDetailOut, stockCode, true)).collect(Collectors.toList());
        // -------------------结束处理未维护销售优先级的---------------------

        // -------------------开始处理已维护销售优先级的   --------------------
        // 重复的优先级，多个门店的销售优先级顺序一致时，按门店代码大小顺序排序,调用 orderByStoreOrder
        List<OrdDirSalvageDelivPondDetailOut> repeatDistPryList = Lists.newArrayList();
        // 不重复的优先级
        List<OrdDirSalvageDelivPondDetailOut> noRepeatDistPryList = Lists.newArrayList();
        // 查找优先级非空的捞单池明细, 赋值销售优先级
        List<OrdDirSalvageDelivPondDetailOut> noEmptyDistPryList = detailList.stream()
                .filter(ordDirSalvageDelivPondDetailOut -> this.checkDistPryEmpty(ordDirSalvageDelivPondDetailOut, stockCode, false))
                .collect(Collectors.toList());
        // 按优先级将明细分组
        LinkedHashMap<Integer, List<OrdDirSalvageDelivPondDetailOut>> distPrySalvageDetailMap = noEmptyDistPryList.stream()
                .collect(Collectors.groupingBy(OrdDirSalvageDelivPondDetailOut::getSalesPriority, LinkedHashMap::new, Collectors.toList()));
        // 按优先级分组：key=优先级，value=优先级出现的次数
        Map<Integer, Long> repeatDistPryOccurrencesMap = noEmptyDistPryList.stream().collect(Collectors.groupingBy(sp -> sp.getSalesPriority(), Collectors.counting()));
        // 按优先级升序排序
        List<Integer> sortDistPryList = repeatDistPryOccurrencesMap.keySet().stream().sorted().collect(Collectors.toList());
        // 根据优先级出现次数分别存放list
        sortDistPryList.forEach(salesPriority -> {
            // 获取优先级出现次数大于1的
            if (repeatDistPryOccurrencesMap.get(salesPriority) > 1) {
                repeatDistPryList.addAll(distPrySalvageDetailMap.get(salesPriority));
            } else {
                noRepeatDistPryList.addAll(distPrySalvageDetailMap.get(salesPriority));
            }
        });
        // 处理不重复优先级的数据
        if (CollectionUtils.isNotEmpty(repeatDistPryList)) {
            //多个门店的销售优先级顺序一致时，按门店代码大小顺序排序
            noRepeatDistPryList.addAll(this.orderByStoreCode(repeatDistPryList));
        }
        // 处理未维护销售优先级排序
        if (CollectionUtils.isNotEmpty(emptyDistPryList)) {
            if (emptyDistPryList.size() == 1) {
                // 当门店未维护销售优先级字段的值时，放在最后审核
                noRepeatDistPryList.add(emptyDistPryList.get(0));
            } else {
                // 当多个门店未设置时，按门店代码大小顺序排序
                noRepeatDistPryList.addAll(this.orderByStoreCode(emptyDistPryList));
            }
        }
        // -------------------结束处理已维护销售优先级的   --------------------
        return noRepeatDistPryList;
    }

    /**
     * 初始化占库存参数明细
     *
     * @param stockInfoOutMap
     * @param ordDirDelivery
     * @param dirDeliveryDetailList
     * @return
     */
    private List<StockFlowGoodsIn> initStockFlowGoodsIns(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> dirDeliveryDetailList, String centerStockBizOrgCode) {
        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        dirDeliveryDetailList.forEach(item -> {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            stockFlowGoodsIn.setPrice(item.getWrhPrice());
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDirDelivery.getStockCode());
            StockInfoOut stockInfoOut = stockInfoOutMap.get(ordDirDelivery.getStockCode());
            if (Objects.isNull(stockInfoOut)) {
                stockInfoOut = stockServer.getTransInfo(ordDirDelivery.getStockCode());
                if (Objects.nonNull(stockInfoOut)) {
                    stockInfoOutMap.put(ordDirDelivery.getStockCode(), stockInfoOut);
                }
            }
            if (Objects.isNull(stockInfoOut) || StringUtil.isEmpty(stockInfoOut.getWarehouseCode())) {
                log.error("配销捞单占库存仓储{}不存在", ordDirDelivery.getStockCode());
                throw new BusinessException("配销捞单占库存仓储" + ordDirDelivery.getStockCode() + "不存在");
            }
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setStoreCode(ordDirDelivery.getStoreCode());
            stockFlowGoodsIn.setStoreName(ordDirDelivery.getStoreName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            // 实际增/减
            stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.ADD.getCode());
            // 实际数
            stockFlowGoodsIn.setApplyQty(item.getOrderQuantity().abs());
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
            // 是否改变可用库存 Y是N否-统配出/配销出
            stockFlowGoodsIn.setIsBusinessQty("Y");

            BigDecimal stockWarehousePrice = warehouseServer.getWarehousePrice(stockInfoOut.getWarehouseCode(), stockInfoOut.getStockCode(),
                    item.getGoodsCode(), centerStockBizOrgCode, item.getVendorCode());
            stockWarehousePrice = Objects.isNull(stockWarehousePrice) ? BigDecimal.ZERO : stockWarehousePrice;
            //成本含税金额
            stockFlowGoodsIn.setCostTaxAmount(item.getOrderQuantity().multiply(stockWarehousePrice));
            //成本不含税金额
            stockFlowGoodsIn.setCostNonTaxAmount(stockFlowGoodsIn.getCostTaxAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //成本税额
            stockFlowGoodsIn.setCostTax(stockFlowGoodsIn.getCostTaxAmount().subtract(stockFlowGoodsIn.getCostNonTaxAmount()));
            //含税金额
            stockFlowGoodsIn.setTaxAmount(item.getOrderQuantity().multiply(item.getOrderUnitPrice()));
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(stockFlowGoodsIn.getTaxAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            // 税额
            stockFlowGoodsIn.setTax(stockFlowGoodsIn.getTaxAmount().subtract(stockFlowGoodsIn.getNonTaxAmount()));
            //单号
            stockFlowGoodsIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
            stockFlowGoodsIns.add(stockFlowGoodsIn);
        });
        return stockFlowGoodsIns;
    }

    /**
     * 根据仓位（温层）判断销售优先级是否为空
     *
     * @param ordDirSalvageDelivPondDetailOut 捞单池明细
     * @param stockCode                       仓位
     * @param optFlag                         操作Flag（true:正向操作，获取为空的；false:反向操作,获取非空的）
     * @return
     */
    private boolean checkDistPryEmpty(OrdDirSalvageDelivPondDetailOut ordDirSalvageDelivPondDetailOut, String stockCode, boolean optFlag) {
        // 郑州上线暂时写死常温
        String temperature = SystemConstant.ROOM_DISTRIBUTION_CYCLE;
//        StockInfoOut stockInfoOut = stockServer.getByCode(stockCode, bizOrgCode);
//        if(Objects.nonNull(stockInfoOut)){
//            temperature = stockInfoOut.getDistributionCycleType();
//        }
        Integer salesPriority;
        // 低温
        if (SystemConstant.FROZEN_DISTRIBUTION_CYCLE.equals(temperature)) {
            // 正向操作
            if (optFlag) {
                return Objects.isNull(ordDirSalvageDelivPondDetailOut.getFrozenPry());
            } else {
                salesPriority = ordDirSalvageDelivPondDetailOut.getFrozenPry();
                ordDirSalvageDelivPondDetailOut.setSalesPriority(salesPriority);
                return Objects.nonNull(ordDirSalvageDelivPondDetailOut.getFrozenPry());
            }
        }
        //常温
        if (SystemConstant.ROOM_DISTRIBUTION_CYCLE.equals(temperature)) {
            // 正向操作
            if (optFlag) {
                return Objects.isNull(ordDirSalvageDelivPondDetailOut.getRoomPry());
            } else {
                salesPriority = ordDirSalvageDelivPondDetailOut.getRoomPry();
                ordDirSalvageDelivPondDetailOut.setSalesPriority(salesPriority);
                return Objects.nonNull(ordDirSalvageDelivPondDetailOut.getRoomPry());
            }
        }
        return !optFlag;
    }


}
