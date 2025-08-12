package com.edc.erp.disdeliveryorder.service.impl;

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
import com.edc.erp.common.model.out.warehouse.WarehouseInfoOut;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPond;
import com.edc.erp.disdeliveryorder.entity.OrdDisSalvageDelivPondDetail;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryMapper;
import com.edc.erp.disdeliveryorder.mapper.OrdDisSalvageDelivPondDetailMapper;
import com.edc.erp.disdeliveryorder.model.in.UpdateStockDisDeliveryOrderIn;
import com.edc.erp.disdeliveryorder.model.out.OrdDisSalvageDelivPondDetailOut;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderExtendService;
import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondDetailService;
import com.edc.erp.disdeliveryorder.service.OrdDisSalvageDelivPondService;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.disrequestorder.mapper.OrdDisDelivRequestMapper;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
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
public class OrdDisSalvageDelivPondDetailServiceImpl extends BaseServiceImpl<OrdDisSalvageDelivPondDetail> implements OrdDisSalvageDelivPondDetailService {

    private final OrdDisSalvageDelivPondDetailMapper ordDisSalvageDelivPondDetailMapper;

    private final StoreCenterService storeCenterService;

    private final OrdDisDeliveryOrderExtendService ordDisDeliveryOrderExtendService;

    private final OrdDisDeliveryDetailService ordDisDeliveryDetailService;

    private final StockWarehouseService stockWarehouseService;

    private final StockServer stockServer;

    private final OrdDisDeliveryMapper ordDisDeliveryMapper;

    private final AsyncLogService asyncLogService;

    private final OrdDisSalvageDelivPondService ordDisSalvageDelivPondService;

    private final OrdDisDelivRequestMapper ordDisDelivRequestMapper;

    private final OrdStoreInventorySupplyRateMapper ordStoreInventorySupplyRateMapper;

    private final WarehouseServer warehouseServer;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrdDisSalvageDelivPondDetails(List<OrdDisDelivery> ordDisDeliveryList, OrdDisOrderCycle orderCycle,
                                                  String loginUsername, String auditType, StoreLogisticsOut logistics) {
        if (CollectionUtils.isEmpty(ordDisDeliveryList)) {
            log.info("订货周期ID{}------{}下拆分配销配货单没有匹配的单子", orderCycle.getId(), orderCycle.getTruncationDateTime());
            return;
        }
        Long salvagePondId = null;
        // 如果是等待自动审核（正常截单拆的配销单），创建捞单主表
        if (SalvageAuditTypeEnum.WAIT_AUTO_AUDIT.getCode().equals(auditType)) {
            LocalDateTime truncationDateTime = orderCycle.getTruncationDateTime();
            OrdDisSalvageDelivPond ordDisSalvageDelivPond = new OrdDisSalvageDelivPond();
            ordDisSalvageDelivPond.setOrderTypeConfigId(orderCycle.getOrderTypeConfigId());
            ordDisSalvageDelivPond.setTruncationDateTime(truncationDateTime);
            ordDisSalvageDelivPond.setBizOrgCode(orderCycle.getBizOrgCode());
            ordDisSalvageDelivPond = ordDisSalvageDelivPondService.getOneByParameter(orderCycle.getOrderTypeConfigId(),
                    orderCycle.getBizOrgCode(), orderCycle.getTruncationDateTime());
            if (Objects.isNull(ordDisSalvageDelivPond)) {
                // 创建捞单池记录
                ordDisSalvageDelivPond = ordDisSalvageDelivPondService.saveDisSalvageDelivPond(orderCycle.getOrderTypeConfigId(),
                        truncationDateTime, orderCycle.getBizOrgCode(), orderCycle.getOrgCode(), SystemConstant.SYSTEM_USER);
                log.info("业务组织代码{}，订单类型ID{}，截单时间{}，配销捞单池记录已创建", orderCycle.getBizOrgCode(), orderCycle.getOrderTypeConfigId(), orderCycle.getTruncationDateTime());
            }
            salvagePondId = ordDisSalvageDelivPond.getId();
        }
        List<QueryEquipmentStockAlloDetailIn> stockAlloDetailInList = ordDisDeliveryList.stream().map(ordDisDelivery -> {
            QueryEquipmentStockAlloDetailIn queryEquipmentStockAlloDetailIn = new QueryEquipmentStockAlloDetailIn();
            queryEquipmentStockAlloDetailIn.setStockCode(ordDisDelivery.getStockCode());
            queryEquipmentStockAlloDetailIn.setWarehouseCode(ordDisDelivery.getWrhCode());
            return queryEquipmentStockAlloDetailIn;
        }).collect(Collectors.toList());
        QueryEquipmentStockAllotIn queryEquipmentStockAllotIn = new QueryEquipmentStockAllotIn();
        queryEquipmentStockAllotIn.setBizOrgCode(orderCycle.getBizOrgCode());
        queryEquipmentStockAllotIn.setStockAlloDetailInList(stockAlloDetailInList);
        // 获取仓储库存分配配置
        Map<String, EquipmentStockAllot> equipmentStockAllotMap = storeCenterService.findByWmsCodeAndStockCode(queryEquipmentStockAllotIn);
        if (null == equipmentStockAllotMap) {
            equipmentStockAllotMap = new HashMap<>();
        }
        Integer roomDistPry = null;
        Integer frozenDistPry = null;
//        StoreLogisticsOut logistics = storeCenterService.getStoreLogisticsByStoreCode(orderCycle.getStoreCode(), orderCycle.getBizOrgCode());
        if (Objects.nonNull(logistics)) {
            roomDistPry = logistics.getRoomDistPry();
            frozenDistPry = logistics.getFrozenDistPry();
        }
        for (OrdDisDelivery ordDisDelivery : ordDisDeliveryList) {
            EquipmentStockAllot equipmentStockAllot = equipmentStockAllotMap.get(ordDisDelivery.getStockCode() + ordDisDelivery.getWrhCode());
            String deliveryOrderSource = DeliveryOrderSourceCodeEnum.getNameByType(ordDisDelivery.getSourceCode()) +
                    "创建" + SalvageAuditTypeEnum.getTagNameByCode(auditType) + "配销单" + ordDisDelivery.getDeliveryOrderNo();
            log.info("捞单池保存明细，来源{}", deliveryOrderSource);
            String allotRule;
            if (Objects.isNull(equipmentStockAllot)) {
                allotRule = AllotRuleEnum.STORE_ORDER.getCode();
                log.error("捞单池保存明细，配销单{}：仓位{}仓储{}未找到仓储库存分配配置，故规则默认为第一笔订单时间", ordDisDelivery.getDeliveryOrderNo(),
                        ordDisDelivery.getStockCode(), ordDisDelivery.getWrhCode());
            } else {
                allotRule = equipmentStockAllot.getAllotRule();
            }
            OrdDisSalvageDelivPondDetail ordDisSalvageDelivPondDetail = initOrdDisSalvageDelivPondDetail(ordDisDelivery, salvagePondId,
                    roomDistPry, frozenDistPry, allotRule, orderCycle.getFirstOrderTime(), auditType);
            ordDisSalvageDelivPondDetail.setCreator(loginUsername);
            ordDisSalvageDelivPondDetail.setUpdater(loginUsername);
            log.info("订货周期ID{}--------{}下拆分配销配货单{}保存至捞单池id{}明细成功", orderCycle.getId(), orderCycle.getTruncationDateTime(), ordDisDelivery.getDeliveryOrderNo(), salvagePondId);
            ordDisSalvageDelivPondDetailMapper.insertSelective(ordDisSalvageDelivPondDetail);
            String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ORDER_SALVAGE.getKey(),
                    ordDisDelivery.getDeliveryOrderNo(), DateUtils.format(orderCycle.getTruncationDateTime()));
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(ordDisDelivery.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), loginUsername);
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveForManualCreateDeliveryOrder(OrdDisDelivery ordDisDelivery, String auditType) {
        StoreLogisticsOut logistics = storeCenterService.getStoreLogisticsByStoreCode(ordDisDelivery.getStoreCode(), ordDisDelivery.getBizOrgCode());
        if (Objects.isNull(logistics)) {
            throw new BusinessException("门店" + ordDisDelivery.getStoreCode() + "未配置配送信息");
        }
        Integer roomDistPry = logistics.getRoomDistPry();
        Integer frozenDistPry = logistics.getFrozenDistPry();
        QueryEquipmentStockAlloDetailIn queryEquipmentStockAlloDetailIn = new QueryEquipmentStockAlloDetailIn();
        queryEquipmentStockAlloDetailIn.setStockCode(ordDisDelivery.getStockCode());
        queryEquipmentStockAlloDetailIn.setWarehouseCode(ordDisDelivery.getWrhCode());
        List<QueryEquipmentStockAlloDetailIn> stockAlloDetailInList = com.google.common.collect.Lists.newArrayList();
        stockAlloDetailInList.add(queryEquipmentStockAlloDetailIn);
        QueryEquipmentStockAllotIn queryEquipmentStockAllotIn = new QueryEquipmentStockAllotIn();
        queryEquipmentStockAllotIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
        queryEquipmentStockAllotIn.setStockAlloDetailInList(stockAlloDetailInList);
        Map<String, EquipmentStockAllot> equipmentStockAllotMap = storeCenterService.findByWmsCodeAndStockCode(queryEquipmentStockAllotIn);
        EquipmentStockAllot equipmentStockAllot = equipmentStockAllotMap.get(ordDisDelivery.getStockCode() + ordDisDelivery.getWrhCode());
        String allotRule;
        if (Objects.isNull(equipmentStockAllot)) {
            allotRule = AllotRuleEnum.STORE_ORDER.getCode();
            log.info("运营端手动创建捞单池明细，配货单{}：仓位{}仓储{}未找到仓储库存分配配置，故规则默认为第一笔订单时间", ordDisDelivery.getDeliveryOrderNo(),
                    ordDisDelivery.getStockCode(), ordDisDelivery.getWrhCode());
        } else {
            allotRule = equipmentStockAllot.getAllotRule();
        }
        OrdDisSalvageDelivPondDetail ordDisSalvageDelivPondDetail = initOrdDisSalvageDelivPondDetail(ordDisDelivery, null,
                roomDistPry, frozenDistPry, allotRule, ordDisDelivery.getCreateTime(), auditType);
        ordDisSalvageDelivPondDetail.setCreator(ordDisDelivery.getCreator());
        ordDisSalvageDelivPondDetail.setUpdater(ordDisDelivery.getUpdater());
        log.info("非订单流创建配销配货单{}保存至无主捞单池明细成功", ordDisDelivery.getDeliveryOrderNo());
        ordDisSalvageDelivPondDetailMapper.insert(ordDisSalvageDelivPondDetail);
        String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ORDER_NO_SALVAGE.getKey(), ordDisDelivery.getDeliveryOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), ordDisDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    private static OrdDisSalvageDelivPondDetail initOrdDisSalvageDelivPondDetail(OrdDisDelivery ordDisDelivery, Long salvagePondId, Integer roomDistPry,
                                                                                 Integer frozenDistPry, String allotRule, LocalDateTime firstOrderTime, String auditType) {
        OrdDisSalvageDelivPondDetail ordDisSalvageDelivPondDetail = new OrdDisSalvageDelivPondDetail();
        ordDisSalvageDelivPondDetail.setSalvagePondId(salvagePondId);
        ordDisSalvageDelivPondDetail.setStoreCode(ordDisDelivery.getStoreCode());
        ordDisSalvageDelivPondDetail.setFirstOrderTime(firstOrderTime);
        ordDisSalvageDelivPondDetail.setDeliveryOrderId(ordDisDelivery.getId());
        ordDisSalvageDelivPondDetail.setStockCode(ordDisDelivery.getStockCode());
        ordDisSalvageDelivPondDetail.setWrhCode(ordDisDelivery.getWrhCode());
        ordDisSalvageDelivPondDetail.setAllotRule(allotRule);
        ordDisSalvageDelivPondDetail.setRoomPry(roomDistPry);
        ordDisSalvageDelivPondDetail.setFrozenPry(frozenDistPry);
        ordDisSalvageDelivPondDetail.setAuditType(auditType);
        ordDisSalvageDelivPondDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisSalvageDelivPondDetail;
    }

    @Override
    public List<OrdDisSalvageDelivPondDetailOut> findListBySalvagePondId(Long salvagePondId) {
        OrdDisSalvageDelivPondDetail ordDisSalvageDelivPondDetail = new OrdDisSalvageDelivPondDetail();
        ordDisSalvageDelivPondDetail.setSalvagePondId(salvagePondId);
        ordDisSalvageDelivPondDetail.setIsDelete(ModelConst.DELETE.NO);
        List<OrdDisSalvageDelivPondDetail> list = ordDisSalvageDelivPondDetailMapper.select(ordDisSalvageDelivPondDetail);
        List<OrdDisSalvageDelivPondDetailOut> ordDisSalvageDelivPondDetailOutList = list.stream().map(detail -> {
            OrdDisSalvageDelivPondDetailOut ordDisSalvageDelivPondDetailOut = new OrdDisSalvageDelivPondDetailOut();
            BeanUtils.copy(detail, ordDisSalvageDelivPondDetailOut);
            return ordDisSalvageDelivPondDetailOut;
        }).collect(Collectors.toList());
        return ordDisSalvageDelivPondDetailOutList;
    }

    @Override
    public List<OperationStockOut> handleSalvage(String bizOrgCode, String loginUsername, Boolean isRecalculateOccupancyQty,
                                                 List<OrdDisSalvageDelivPondDetailOut> ordDisSalvageDelivPondDetailOutList) {
        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        List<OperationStockOut> allOperationStockOutList = Lists.newArrayList();
        // 按规则分组+仓位
        Map<String, List<OrdDisSalvageDelivPondDetailOut>> allotRuleMap = ordDisSalvageDelivPondDetailOutList.stream()
                .collect(Collectors.groupingBy(v -> v.getAllotRule() + SystemConstant.COLON + v.getStockCode()));
        for (Map.Entry<String, List<OrdDisSalvageDelivPondDetailOut>> entry : allotRuleMap.entrySet()) {
            String[] keyArray = entry.getKey().split(SystemConstant.COLON);
            String allotRule = keyArray[0];
            String stockCode = keyArray[1];
            List<OrdDisSalvageDelivPondDetailOut> detailList = entry.getValue();
            log.info("------------开始处理业务组织{}下仓储库存分配规则为{}的配货单占库存--------------", allotRule);
            List<OrdDisSalvageDelivPondDetailOut> disSalvageDelivPondDetailSortList = null;
            if (AllotRuleEnum.STORE_CODE_SIZE.getCode().equals(allotRule)) {
                disSalvageDelivPondDetailSortList = this.orderByStoreCode(detailList);
            }
            if (AllotRuleEnum.STORE_ORDER.getCode().equals(allotRule)) {
                disSalvageDelivPondDetailSortList = this.orderByFirstStoreOrder(detailList);
            }
            if (AllotRuleEnum.STORE_SALES.getCode().equals(allotRule)) {
                disSalvageDelivPondDetailSortList = this.orderByStoreSale(detailList, stockCode);
            }
            if (CollectionUtils.isEmpty(disSalvageDelivPondDetailSortList)) {
                continue;
            }
            List<StockFlowIn> stockStoreFlowIns = Lists.newArrayList();
            disSalvageDelivPondDetailSortList.forEach(ordDisSalvageDelivPondDetailOut -> {
                OrdDisDelivery ordDisDelivery = ordDisDeliveryOrderExtendService.getOneByIdAndBizOrgCode(ordDisSalvageDelivPondDetailOut.getDeliveryOrderId(), bizOrgCode);
                if (!DeliveryOrderEnum.PENDING.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
                    return;
                }
                List<OrdDisDeliveryDetail> disDeliveryDetailList = ordDisDeliveryDetailService.findDeliveryOrderDetails(ordDisDelivery.getId());
                if (isRecalculateOccupancyQty) {
                    OrdDisDelivRequest ordDirDelivRequest = ordDisDelivRequestMapper.getRequestOrderByDeliveryOrderIdAndBizOrgCode(ordDisDelivery.getId(), ordDisDelivery.getBizOrgCode());
                    OrdStoreInventorySupplyRate ordStoreInventorySupplyRate = new OrdStoreInventorySupplyRate();
                    ordStoreInventorySupplyRate.setStoreCode(ordDisDelivery.getStoreCode());
                    ordStoreInventorySupplyRate.setOrderCycleTime(ordDirDelivRequest.getTruncationDateTime());
                    ordStoreInventorySupplyRate.setDeliveryOrderId(ordDisDelivery.getId());
                    List<OrdStoreInventorySupplyRate> storeInventorySupplyRateList = ordStoreInventorySupplyRateMapper.select(ordStoreInventorySupplyRate);
                    if (CollectionUtils.isNotEmpty(storeInventorySupplyRateList)) {
                        Map<String, OrdStoreInventorySupplyRate> supplyRateMap = storeInventorySupplyRateList.stream().collect(Collectors.toMap(OrdStoreInventorySupplyRate::getGoodsCode, Function.identity()));
                        disDeliveryDetailList.forEach(ordDisDeliveryDetail -> {
                            OrdStoreInventorySupplyRate storeInventorySupplyRate = supplyRateMap.get(ordDisDeliveryDetail.getGoodsCode());
                            if (Objects.nonNull(storeInventorySupplyRate)) {
                                ordDisDeliveryDetail.setOrderQuantity(storeInventorySupplyRate.getAllocationQuantity());
                            }
                        });
                    }
                }
                StockFlowIn stockFlowIn = new StockFlowIn();
                stockFlowIn.setBizOrgCode(ordDisSalvageDelivPondDetailOut.getCenterStockBizOrgCode());
                stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getCode());
                stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getName());
                stockFlowIn.setFlowDate(LocalDateTime.now());
                stockFlowIn.setCreator(ordDisDelivery.getCreator());
                stockFlowIn.setOrgCode(ordDisSalvageDelivPondDetailOut.getCenterStockOrgCode());
                stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
                stockFlowIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
                // 初始化占库存参数明细
                List<StockFlowGoodsIn> stockFlowGoodsIns = this.initStockFlowGoodsIns(ordDisDelivery, disDeliveryDetailList,
                        ordDisSalvageDelivPondDetailOut.getCenterStockBizOrgCode());
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
    public List<UpdateStockDisDeliveryOrderIn> initUpdateStockDeliveryOrderInList(List<OperationStockOut> operationStockOutList, String loginUsername, String bizOrgCode) {
        if (CollectionUtils.isEmpty(operationStockOutList)) {
            return null;
        }
        List<UpdateStockDisDeliveryOrderIn> updateStockDisDeliveryOrderInList = Lists.newArrayList();
        // 处理成功占用库存后
        operationStockOutList.forEach(operationStockOut -> {
            String deliveryOrderNo = operationStockOut.getBusinessOrderNo();
            log.info("配销单{}审核占库存结果：{}", deliveryOrderNo, JSONObject.toJSONString(operationStockOut));
            OrdDisDelivery ordDisDelivery = ordDisDeliveryOrderExtendService.getOneByDeliveryOrderNo(deliveryOrderNo, DeliveryOrderEnum.PENDING.getKey(), bizOrgCode);
            if (Objects.isNull(ordDisDelivery)) {
                log.error("配销配货单{}不存在或者状态非待审核", deliveryOrderNo);
                return;
            }
            List<OperationStockDetailsOut> operationStockDetailsOutList = operationStockOut.getOperationStockDetailsOutList();
            List<OrdDisDeliveryDetail> updateDistributionInfoList = Lists.newArrayList();
            // 封装明细
            if (CollectionUtils.isNotEmpty(operationStockDetailsOutList)) {
                List<OrdDisDeliveryDetail> ordDisDeliveryDetails = ordDisDeliveryDetailService.findDeliveryOrderDetails(ordDisDelivery.getId());
                Map<String, OrdDisDeliveryDetail> detailMap = ordDisDeliveryDetails.stream()
                        .collect(Collectors.toMap(detail -> detail.getGoodsCode() + SystemConstant.SHORT_LINE + detail.getIsGift() + SystemConstant.SHORT_LINE + detail.getBaseGoodsCode(), Function.identity()));
                operationStockDetailsOutList.forEach(operationStockDetailsOut -> {
                    OrdDisDeliveryDetail dbDetail = detailMap.get(operationStockDetailsOut.getGoodsCode() + SystemConstant.SHORT_LINE + operationStockDetailsOut.getIsGift() + SystemConstant.SHORT_LINE + operationStockDetailsOut.getBaseGoodsCode());
                    if (Objects.isNull(dbDetail)) {
                        return;
                    }
                    OrdDisDeliveryDetail ordDisDeliveryDetail = new OrdDisDeliveryDetail();
                    ordDisDeliveryDetail.setId(dbDetail.getId());
                    ordDisDeliveryDetail.setLine(dbDetail.getLine());
                    ordDisDeliveryDetail.setOrderUnitPrice(dbDetail.getOrderUnitPrice());
                    ordDisDeliveryDetail.setDistributionUnitPrice(dbDetail.getDistributionUnitPrice());
                    ordDisDeliveryDetail.setGoodsCode(dbDetail.getGoodsCode());
                    ordDisDeliveryDetail.setOrgGoodsId(dbDetail.getOrgGoodsId());
                    ordDisDeliveryDetail.setDistributionQuantity(operationStockDetailsOut.getAuditsQuantity());
                    ordDisDeliveryDetail.setDistributionPackageQuantity(ordDisDeliveryDetail.getDistributionQuantity()
                            .divide(dbDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDisDeliveryDetail.setDistributionAmount(operationStockDetailsOut.getAuditsQuantity().multiply(dbDetail.getOrderUnitPrice()));
                    ordDisDeliveryDetail.setUpdater(loginUsername);
                    ordDisDeliveryDetail.setUpdateTime(LocalDateTime.now());
                    //税额
                    BigDecimal sellTax = Objects.isNull(dbDetail.getSellTax()) ? BigDecimal.ZERO : dbDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
                    BigDecimal tax = sellTax.add(BigDecimal.ONE);
                    ordDisDeliveryDetail.setDistributionExceptTaxAmount(ordDisDeliveryDetail.getDistributionAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDisDeliveryDetail.setDistributionTaxAmount(ordDisDeliveryDetail.getDistributionAmount().subtract(ordDisDeliveryDetail.getDistributionExceptTaxAmount()));
//                    BigDecimal stockWarehousePrice = warehouseServer.getWarehousePrice(ordDisDelivery.getWrhCode(), ordDisDelivery.getStockCode(), ordDisDeliveryDetail.getGoodsCode(), bizOrgCode);
//                    ordDisDeliveryDetail.setWrhPrice(Objects.isNull(stockWarehousePrice) ? BigDecimal.ZERO : stockWarehousePrice);
//                    BigDecimal stockStorePrice = warehouseServer.getStockPrice(ordDisDelivery.getStoreCode(), ordDisDeliveryDetail.getGoodsCode(), bizOrgCode);
//                    ordDisDeliveryDetail.setStoreStockPrice(Objects.isNull(stockStorePrice) ? BigDecimal.ZERO : stockStorePrice);
                    BigDecimal distributionQuantity = Objects.isNull(ordDisDeliveryDetail.getDistributionQuantity()) ? BigDecimal.ZERO : ordDisDeliveryDetail.getDistributionQuantity();
                    ordDisDeliveryDetail.setWrhCostAmount(dbDetail.getWrhPrice().multiply(distributionQuantity));
                    ordDisDeliveryDetail.setWrhExceptTaxAmount(ordDisDeliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDisDeliveryDetail.setWrhTaxAmount(ordDisDeliveryDetail.getWrhCostAmount().subtract(ordDisDeliveryDetail.getWrhExceptTaxAmount()));
                    ordDisDeliveryDetail.setStoreCostAmount(dbDetail.getStoreStockPrice().multiply(distributionQuantity));
                    ordDisDeliveryDetail.setStoreExceptTaxAmount(ordDisDeliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                    ordDisDeliveryDetail.setStoreTaxAmount(ordDisDeliveryDetail.getStoreCostAmount().subtract(ordDisDeliveryDetail.getStoreExceptTaxAmount()));
                    updateDistributionInfoList.add(ordDisDeliveryDetail);
                });
            }
            String deliveryStatusCode = ordDisDelivery.getDeliveryStatusCode();
            BigDecimal distributionQuantity = BigDecimal.ZERO;
            BigDecimal distributionAmount = BigDecimal.ZERO;
            // 是否整单缺货
            if (operationStockOut.getIsStockOutAll() == 1) {
                deliveryStatusCode = DeliveryOrderEnum.INVALID.getKey();
            }
            // 部分缺货
            if (operationStockOut.getIsStockOutAll() == 0) {
                deliveryStatusCode = DeliveryOrderEnum.APPROVED.getKey();
                distributionQuantity = updateDistributionInfoList.stream().map(OrdDisDeliveryDetail::getDistributionQuantity).reduce(BigDecimal::add).get();
                distributionAmount = updateDistributionInfoList.stream().map(OrdDisDeliveryDetail::getDistributionAmount).reduce(BigDecimal::add).get();
            }
            OrdDisDelivery updateOrdDisDelivery = new OrdDisDelivery();
            BeanUtils.copy(ordDisDelivery, updateOrdDisDelivery);
            updateOrdDisDelivery.setId(ordDisDelivery.getId());
            updateOrdDisDelivery.setDeliveryStatusCode(deliveryStatusCode);
            updateOrdDisDelivery.setDistributionQuantity(distributionQuantity);
            updateOrdDisDelivery.setDistributionAmount(distributionAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.CEILING));
            updateOrdDisDelivery.setDistributionTime(LocalDateTime.now());
            updateOrdDisDelivery.setUpdater(loginUsername);
            updateOrdDisDelivery.setUpdateTime(LocalDateTime.now());
            updateOrdDisDelivery.setBizOrgCode(bizOrgCode);
            // 配货单占库存审核更新入参
            UpdateStockDisDeliveryOrderIn updateStockDisDeliveryOrderIn = new UpdateStockDisDeliveryOrderIn();
            updateStockDisDeliveryOrderIn.setIsStockOutAll(operationStockOut.getIsStockOutAll());
            updateStockDisDeliveryOrderIn.setStockOutLog(operationStockOut.getStockOutLog());
            updateStockDisDeliveryOrderIn.setDisDeliveryDetailList(updateDistributionInfoList);
            updateStockDisDeliveryOrderIn.setBeforeDeliveryStatus(ordDisDelivery.getDeliveryStatusCode());
            updateStockDisDeliveryOrderIn.setOrdDisDelivery(updateOrdDisDelivery);
            updateStockDisDeliveryOrderInList.add(updateStockDisDeliveryOrderIn);
        });
        return updateStockDisDeliveryOrderInList;
    }

    @Override
    public OrdDisSalvageDelivPondDetail getOneByDeliveryOrderId(Long deliveryOrderId) {
        OrdDisSalvageDelivPondDetail ordDisSalvageDelivPondDetail = new OrdDisSalvageDelivPondDetail();
        ordDisSalvageDelivPondDetail.setDeliveryOrderId(deliveryOrderId);
        ordDisSalvageDelivPondDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisSalvageDelivPondDetailMapper.selectOne(ordDisSalvageDelivPondDetail);
    }

    @Override
    public List<OrdDisSalvageDelivPondDetailOut> findByDeliveryOrderIdList(List<Long> deliveryOrderIdList) {
        return ordDisSalvageDelivPondDetailMapper.findByDeliveryOrderIdList(deliveryOrderIdList);
    }

    @Override
    public List<Long> findDeliveryOrderIdListBySalvagePondId(Long salvagePondId) {
        return ordDisSalvageDelivPondDetailMapper.findDeliveryOrderIdListBySalvagePondId(salvagePondId);
    }

    private BigDecimal getAmountByDeliveryOrderStatus(OrdDisDeliveryDetail disDeliveryDetail, String deliveryOrderStatus) {
        if (DeliveryOrderEnum.INVALID.getKey().equals(deliveryOrderStatus)) {
            return disDeliveryDetail.getOrderAmount();
        }
        if (DeliveryOrderEnum.APPROVED.getKey().equals(deliveryOrderStatus)) {
            if (Objects.isNull(disDeliveryDetail.getDistributionQuantity())) {
                return disDeliveryDetail.getOrderAmount();
            } else {
                BigDecimal quantity = disDeliveryDetail.getOrderQuantity().subtract(disDeliveryDetail.getDistributionQuantity());
                return quantity.multiply(disDeliveryDetail.getOrderUnitPrice());
            }
        }
        throw new BusinessException("未知配货单类型");
    }

    /**
     * 按门店大小排序
     *
     * @param detailList
     * @return
     */
    private List<OrdDisSalvageDelivPondDetailOut> orderByStoreCode(List<OrdDisSalvageDelivPondDetailOut> detailList) {
        int day = LocalDate.now().getDayOfMonth();
        int evenNumber = 2;
        LinkedHashMap<String, List<OrdDisSalvageDelivPondDetailOut>> map;
        // 判断双日还是单日
        if (day % evenNumber == 0) {
            // 双日降序
            map = detailList.stream().sorted(Comparator.comparing(OrdDisSalvageDelivPondDetailOut::getStoreCode)
                    .reversed()).collect(Collectors.groupingBy(OrdDisSalvageDelivPondDetailOut::getStoreCode, LinkedHashMap::new, Collectors.toList()));
        } else {
            //单日升序
            map = detailList.stream().sorted(Comparator.comparing(OrdDisSalvageDelivPondDetailOut::getStoreCode))
                    .collect(Collectors.groupingBy(OrdDisSalvageDelivPondDetailOut::getStoreCode, LinkedHashMap::new, Collectors.toList()));
        }
        return this.mergeSalvageDetailAfterSort(map);
    }

    /**
     * 按第一笔下单时间排序
     *
     * @param detailList
     * @return
     */
    private List<OrdDisSalvageDelivPondDetailOut> orderByFirstStoreOrder(List<OrdDisSalvageDelivPondDetailOut> detailList) {
        LinkedHashMap<String, List<OrdDisSalvageDelivPondDetailOut>> map = detailList.stream().sorted(Comparator.comparing(OrdDisSalvageDelivPondDetailOut::getFirstOrderTime))
                .collect(Collectors.groupingBy(OrdDisSalvageDelivPondDetailOut::getStoreCode, LinkedHashMap::new, Collectors.toList()));
        return this.mergeSalvageDetailAfterSort(map);
    }

    /**
     * 合并排序后捞单明细，将有序LinkedHashMap中的value合并至一个List
     *
     * @param map
     * @return
     */
    private List<OrdDisSalvageDelivPondDetailOut> mergeSalvageDetailAfterSort(LinkedHashMap<String, List<OrdDisSalvageDelivPondDetailOut>> map) {
        List<OrdDisSalvageDelivPondDetailOut> list = Lists.newArrayList();
        for (List<OrdDisSalvageDelivPondDetailOut> ordDisSalvageDelivPondDetailOutList : map.values()) {
            list.addAll(ordDisSalvageDelivPondDetailOutList);
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
    private List<OrdDisSalvageDelivPondDetailOut> orderByStoreSale(List<OrdDisSalvageDelivPondDetailOut> detailList, String stockCode) {
        // -------------------开始处理未维护销售优先级的--------------------
        // 查找优先级为空的捞单池明细
        List<OrdDisSalvageDelivPondDetailOut> emptyDistPryList = detailList.stream()
                .filter(ordDisSalvageDelivPondDetailOut -> this.checkDistPryEmpty(ordDisSalvageDelivPondDetailOut, stockCode, true)).collect(Collectors.toList());
        // -------------------结束处理未维护销售优先级的---------------------

        // -------------------开始处理已维护销售优先级的   --------------------
        // 重复的优先级，多个门店的销售优先级顺序一致时，按门店代码大小顺序排序,调用 orderByStoreOrder
        List<OrdDisSalvageDelivPondDetailOut> repeatDistPryList = Lists.newArrayList();
        // 不重复的优先级
        List<OrdDisSalvageDelivPondDetailOut> noRepeatDistPryList = Lists.newArrayList();
        // 查找优先级非空的捞单池明细, 赋值销售优先级
        List<OrdDisSalvageDelivPondDetailOut> noEmptyDistPryList = detailList.stream()
                .filter(ordDisSalvageDelivPondDetailOut -> this.checkDistPryEmpty(ordDisSalvageDelivPondDetailOut, stockCode, false))
                .collect(Collectors.toList());
        // 按优先级将明细分组
        LinkedHashMap<Integer, List<OrdDisSalvageDelivPondDetailOut>> distPrySalvageDetailMap = noEmptyDistPryList.stream()
                .collect(Collectors.groupingBy(OrdDisSalvageDelivPondDetailOut::getSalesPriority, LinkedHashMap::new, Collectors.toList()));
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
     * @Description: 初始化占库存参数明细
     * @Author: ZhangYao
     * @Date: 2024/4/18 15:24
     * @param ordDisDelivery:
     * @param disDeliveryDetailList:
     * @param centerStockBizOrgCode:
     * @return: java.util.List<com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn>
     **/
    private List<StockFlowGoodsIn> initStockFlowGoodsIns(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> disDeliveryDetailList, String centerStockBizOrgCode) {
        Map<String, StockInfoOut> stockInfoOutMap = new HashMap<>();
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        disDeliveryDetailList.forEach(item -> {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDisDelivery.getStockCode());
            StockInfoOut stockInfoOut = stockInfoOutMap.get(ordDisDelivery.getStockCode());
            if (Objects.isNull(stockInfoOut)) {
                stockInfoOut = stockServer.getTransInfo(ordDisDelivery.getStockCode());
                if (Objects.nonNull(stockInfoOut)) {
                    stockInfoOutMap.put(ordDisDelivery.getStockCode(), stockInfoOut);
                }
            }
            if (Objects.isNull(stockInfoOut) || StringUtil.isEmpty(stockInfoOut.getWarehouseCode())) {
                log.error("配销捞单占库存仓储{}不存在", ordDisDelivery.getStockCode());
                throw new BusinessException("配销捞单占库存仓储" + ordDisDelivery.getStockCode() + "不存在");
            }
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setStoreCode(ordDisDelivery.getStoreCode());
            stockFlowGoodsIn.setStoreName(ordDisDelivery.getStoreName());
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
            BigDecimal stockWarehousePrice = ordDisDeliveryMapper.getStockWarehousePrice(stockInfoOut.getWarehouseCode(),
                    stockInfoOut.getStockCode(), item.getGoodsCode(), centerStockBizOrgCode);
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
            stockFlowGoodsIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
            stockFlowGoodsIns.add(stockFlowGoodsIn);
        });
        return stockFlowGoodsIns;
    }

    /**
     * 根据仓位（温层）判断销售优先级是否为空
     *
     * @param ordDisSalvageDelivPondDetailOut 捞单池明细
     * @param stockCode                       仓位
     * @param optFlag                         操作Flag（true:正向操作，获取为空的；false:反向操作,获取非空的）
     * @return
     */
    private boolean checkDistPryEmpty(OrdDisSalvageDelivPondDetailOut ordDisSalvageDelivPondDetailOut, String stockCode, boolean optFlag) {
        // 郑州上线暂时写死常温
        String temperature = SystemConstant.ROOM_DISTRIBUTION_CYCLE;
//        StockInfoOut stockInfoOut = stockServer.getByCode(stockCode, bizOrgCode);
//        if (Objects.nonNull(stockInfoOut)) {
//            temperature = stockInfoOut.getDistributionCycleType();
//        }
        Integer salesPriority;
        // 低温
        if (SystemConstant.FROZEN_DISTRIBUTION_CYCLE.equals(temperature)) {
            // 正向操作
            if (optFlag) {
                return Objects.isNull(ordDisSalvageDelivPondDetailOut.getFrozenPry());
            } else {
                salesPriority = ordDisSalvageDelivPondDetailOut.getFrozenPry();
                ordDisSalvageDelivPondDetailOut.setSalesPriority(salesPriority);
                return Objects.nonNull(ordDisSalvageDelivPondDetailOut.getFrozenPry());
            }
        }
        //常温
        if (SystemConstant.ROOM_DISTRIBUTION_CYCLE.equals(temperature)) {
            // 正向操作
            if (optFlag) {
                return Objects.isNull(ordDisSalvageDelivPondDetailOut.getRoomPry());
            } else {
                salesPriority = ordDisSalvageDelivPondDetailOut.getRoomPry();
                ordDisSalvageDelivPondDetailOut.setSalesPriority(salesPriority);
                return Objects.nonNull(ordDisSalvageDelivPondDetailOut.getRoomPry());
            }
        }
        return !optFlag;
    }


}
