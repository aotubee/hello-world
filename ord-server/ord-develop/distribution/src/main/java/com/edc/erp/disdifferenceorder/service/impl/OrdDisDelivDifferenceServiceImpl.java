package com.edc.erp.disdifferenceorder.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.async.handel.SyncOrdDisOrderHandle;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.out.goods.OrgGoodsTransInfo;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.stock.StockTransInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.WarehouseServer;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifferenceDetail;
import com.edc.erp.disdifferenceorder.enumeration.DisDifferenceOrderLogEnum;
import com.edc.erp.disdifferenceorder.mapper.OrdDisDelivDifferenceMapper;
import com.edc.erp.disdifferenceorder.model.in.ApprovedDisDifferenceOrderIn;
import com.edc.erp.disdifferenceorder.model.in.DisDifferenceOrderIn;
import com.edc.erp.disdifferenceorder.model.in.OrdDisDelivDifferenceDetailIn;
import com.edc.erp.disdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.disdifferenceorder.model.out.DisDiffOrderSummaryOut;
import com.edc.erp.disdifferenceorder.model.out.DisDifferenceOrderOut;
import com.edc.erp.disdifferenceorder.model.out.ExcelDisDifferenceOrder;
import com.edc.erp.disdifferenceorder.model.out.OrdDisDelivDifferenceDetailOut;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceDetailService;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceService;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsExpiryIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
import com.edc.erp.reducestock.stock.service.StockStoreService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.export.AsyncExportExecutor;
import com.edc.plugins.export.ExportExcelByPage;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.sdk.dts.model.order.in.DifferenceBillDtlIn;
import com.edc.sdk.dts.model.order.in.DifferenceBillIn;
import com.edc.sdk.dts.model.order.vo.DifferenceBillDtlVO;
import com.edc.sdk.dts.model.order.vo.DifferenceBillVO;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 配销差异单(OrdDisDelivDifference)表服务实现类
 *
 * @author weichao
 * @since 2022-10-24 11:16:27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisDelivDifferenceServiceImpl extends BaseServiceImpl<OrdDisDelivDifference> implements OrdDisDelivDifferenceService {

    private final OrdDisDelivDifferenceMapper ordDisDelivDifferenceMapper;

    private final StoreCenterService storeCenterService;

    private final OrderGoodsServer orderGoodsServer;

    private final RedisService redisService;

    private final AsyncLogService asyncLogService;

    private final UniqueUtils uniqueUtils;

    private final OrdDisDelivDifferenceDetailService ordDisDelivDifferenceDetailService;

    private final SyncOrdDisOrderHandle syncOrdDisOrderHandle;

    private final StockServer stockServer;

    private final StockStoreService stockStoreService;

    private final WarehouseServer warehouseServer;

    private final StockFlowService stockFlowService;

    private final AsyncExportExecutor asyncExportExecutor;

    private final StoreChannelHandle storeChannelHandle;

    @Qualifier("disDifferenceOrderToDtsSender")
    private final MessageSender disDifferenceOrderToDtsSender;


    /**
     * 获取差异单列表
     * @param differenceOrderIn
     * @return
     */
    @Override
    public Page<DisDifferenceOrderOut> findDiffOrderByParam(DisDifferenceOrderIn differenceOrderIn) {
        this.handleChannelInfo(differenceOrderIn);
        if (StringUtils.isNotBlank(differenceOrderIn.getStoreArea())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(differenceOrderIn.getStoreArea(), differenceOrderIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return new Page(differenceOrderIn);
            }
            differenceOrderIn.setStoreCodeList(storeCodeList);
        }
        List<DisDifferenceOrderOut> disDifferenceOrderOuts = ordDisDelivDifferenceMapper.findDifferenceOrdersByPage(differenceOrderIn);
        disDifferenceOrderOuts.forEach(item -> {
//            List<OrdDisDelivDifferenceDetailOut> disDelivDifferenceDetails = ordDisDelivDifferenceDetailService.findDifferenceOrderDtlById(item.getId());
//            item.setDifferenceOrderDetailList(disDelivDifferenceDetails);
            StockInfoOut stockOut = stockServer.getTransInfo(item.getPosition());
            item.setPositionName(Objects.nonNull(stockOut) ? stockOut.getStockName() : "");

            item.setDifferenceStatusName(DifferenceOrderStatusEnum.getNameByCode(item.getDifferenceStatus()));
            // 配销差异类型中文
            item.setDifferenceTypeName(DifferenceOrderTypeEnum.getNameByCode(item.getDifferenceType()));
            //品项数
            Integer size = ordDisDelivDifferenceDetailService.getGoodsSize(item.getId());
            item.setGoodsSize(size);
        });
        Page<DisDifferenceOrderOut> reqPage = new Page(differenceOrderIn);
        reqPage.setList(disDifferenceOrderOuts);
        return reqPage;
    }

    private void handleChannelInfo(DisDifferenceOrderIn differenceOrderIn) {
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(differenceOrderIn.getBizOrgCode());
        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !differenceOrderIn.getBizOrgCode().equals(stockInfoOut.getBizOrgCode())).findFirst();
        if (!anyOneOptional.isPresent()) {
            differenceOrderIn.setBizOrgCode("");
        }
        List<String> stockCodeList;
        if (StringUtils.isBlank(differenceOrderIn.getPosition())) {
            stockCodeList = authOrgStockMap.keySet().stream().collect(Collectors.toList());
            differenceOrderIn.setStockCodeList(stockCodeList);
        } else {
            stockCodeList = Collections.singletonList(differenceOrderIn.getPosition());
            // 校验业务仓位是否允许操作
            storeChannelHandle.checkStockCodeListISAuth(stockCodeList, authOrgStockMap);
            differenceOrderIn.setPosition(null);
            differenceOrderIn.setStockCodeList(stockCodeList);
        }
    }

    /**
     * 获取差异单单头信息
     * @param differenceOrderId
     * @return
     */
    @Override
    public DisDifferenceOrderOut getBackHeaderDifferenceOrderOutById(Integer differenceOrderId) {
        OrdDisDelivDifference query = new OrdDisDelivDifference();
        query.setId(differenceOrderId);
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        OrdDisDelivDifference ordDisDelivDifference = ordDisDelivDifferenceMapper.selectOne(query);
        DisDifferenceOrderOut disDifferenceOrderOut = new DisDifferenceOrderOut();
        BeanUtils.copy(ordDisDelivDifference, disDifferenceOrderOut);
        // 配销差异类型中文
        disDifferenceOrderOut.setDifferenceTypeName(DifferenceOrderTypeEnum.getNameByCode(disDifferenceOrderOut.getDifferenceType()));
        disDifferenceOrderOut.setDifferenceStatusName(DifferenceOrderStatusEnum.getNameByCode(disDifferenceOrderOut.getDifferenceStatus()));
        StockInfoOut stockOut = stockServer.getTransInfo(disDifferenceOrderOut.getPosition());
        disDifferenceOrderOut.setPositionName(Objects.nonNull(stockOut) ? stockOut.getStockName() : "");
        disDifferenceOrderOut.setWrhName(Objects.nonNull(stockOut) ? stockOut.getWarehouseName() : "");
        return disDifferenceOrderOut;
    }

    /**
     * 导出配销差异单列表出参
     * @param differenceOrderIn
     * @return
     */
    @Override
    public String exportDisDifferences(DisDifferenceOrderIn differenceOrderIn) {
        // 设置每次查询条数
        differenceOrderIn.setPageSize(10000);
        String title = "配销差异单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配销差异单列表",
                        // 导出模板实体
                        ExcelDisDifferenceOrder.class,
                        // 分页查询对象
                        differenceOrderIn,
                        // 分页查询方法
                        page -> {
                            Page<DisDifferenceOrderOut> outPage = this.findDiffOrderByParam(differenceOrderIn);
                            List<ExcelDisDifferenceOrder> excelDisDifferenceOrders = parseDataToExcel(outPage.getList());
                            log.info("导出配货差异单列表集合大小是--{}", excelDisDifferenceOrders.size());
                            return excelDisDifferenceOrders;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    /**
     * 作废配销差异单
     * @param ordDisDelivDifference
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidDisDifference(OrdDisDelivDifference ordDisDelivDifference, StockInfoOut stockInfoOut) {
        if (!DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
            throw new BusinessException("已审核的差异单才可作废");
        }
        List<OrdDisDelivDifferenceDetail> details = ordDisDelivDifferenceDetailService.selectAllByDifferenceOrderId(ordDisDelivDifference.getId());
        ordDisDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.INVALID.getCode());
//        ordDisDelivDifference.setIsReversal(NumberUtils.INTEGER_ONE);
        int updateCount = ordDisDelivDifferenceMapper.updateByPrimaryKeySelective(ordDisDelivDifference);
        if (updateCount < 1) {
            return updateCount;
        }
        //库存释放
        List<StockFlowIn> stockFlowIns = this.initStockInvalid(ordDisDelivDifference, details, stockInfoOut);
        Response response = stockFlowService.checkStockFlow(stockFlowIns);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_INVALID.getName(),
                String.valueOf(ordDisDelivDifference.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                DifferenceOrderStatusEnum.INVALID.getName(), new Date(), ordDisDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return updateCount;
    }

    /**
     * 差异单作废初始化库存入参
     * @param disDelivDifference
     * @param details
     * @return
     */
    private List<StockFlowIn> initStockInvalid(OrdDisDelivDifference disDelivDifference, List<OrdDisDelivDifferenceDetail> details, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setOperationType(OrderTypeEnum.INVALID.getCode());
        // 库存发生位置
        String occurrenceLocation = null;
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(disDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(disDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getName());
            occurrenceLocation = StockHappenLieEnum.STORE.getCode();
            stockFlowIn.setBizOrgCode(disDelivDifference.getBizOrgCode());
            stockFlowIn.setOrgCode(disDelivDifference.getOrgCode());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(disDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(disDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getName());
            occurrenceLocation = StockHappenLieEnum.WAREHOUSE.getCode();
            stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
            stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        }
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(disDelivDifference.getCreator());
        stockFlowIn.setSourceNo(disDelivDifference.getDifferenceNo());
        stockFlowIn.setOccurrenceLocation(occurrenceLocation);
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        for (OrdDisDelivDifferenceDetail item : details) {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
            stockFlowGoodsIn.setApplyQty(item.getApplyDifferenceQuantity().abs());
            stockFlowGoodsIn.setStockCode(disDelivDifference.getPosition());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            stockFlowGoodsIn.setStoreCode(disDelivDifference.getStoreCode());
            stockFlowGoodsIn.setStoreName(disDelivDifference.getStoreName());
            // 单号
            stockFlowGoodsIn.setSourceNo(disDelivDifference.getDifferenceNo());
            stockFlowGoodsIn.setPosition(occurrenceLocation);
            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
            //申请差异去税金额
            BigDecimal applyDifferenceExceptTaxAmount = item.getApplyDifferenceAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //申请差异税额
            BigDecimal applyDifferenceTaxAmount = item.getApplyDifferenceAmount().subtract(applyDifferenceExceptTaxAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(applyDifferenceExceptTaxAmount);
            //含税金额
            stockFlowGoodsIn.setTaxAmount(item.getApplyDifferenceAmount());
            // 税额
            stockFlowGoodsIn.setTax(applyDifferenceTaxAmount);
            stockFlowGoodsIns.add(stockFlowGoodsIn);
        }
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return Collections.singletonList(stockFlowIn);
    }

    /**
     * 配销差异单修改
     *
     * @param ordDisDelivDifference
     * @param typeCode
     * @param typeName
     * @param name
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDisDifference(OrdDisDelivDifference ordDisDelivDifference, String typeCode, String typeName, String name) {
        return ordDisDelivDifferenceMapper.updateByPrimaryKeySelective(ordDisDelivDifference);
    }


    /**
     * 批量批准配销差异单
     *
     * @param disDifferenceOrderIds
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchApprovedDisDifference(List<Integer> disDifferenceOrderIds) {
        int approved = NumberUtil.INTEGER_ZERO;
        for (Integer item : disDifferenceOrderIds) {
            List<OrdDisDelivDifferenceDetail> details = ordDisDelivDifferenceDetailService.selectAllByDifferenceOrderId(item);
            ApprovedDisDifferenceOrderIn orderIn = new ApprovedDisDifferenceOrderIn();
            orderIn.setDifferenceDetails(details);
            orderIn.setDiffOrderId(item);
            orderIn.setBizOrgCode(UserUtil.getBizOrgCode());
            try {
                approved += this.approvedDisDifference(orderIn);
            } catch (Exception e) {
                log.error("配销差异单ID{}批准失败,错误原因是--{}", item, e.getMessage());
            }
        }
        return approved;
    }

    /**
     * 批准配销差异单
     *
     * @param approvedDisDifferenceOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approvedDisDifference(ApprovedDisDifferenceOrderIn approvedDisDifferenceOrderIn) {
        OrdDisDelivDifference disDelivDifference = ordDisDelivDifferenceMapper.selectByPrimaryKey(approvedDisDifferenceOrderIn.getDiffOrderId());
        if (Objects.isNull(disDelivDifference)) {
            throw new BusinessException("配销差异单不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(disDelivDifference.getPosition(), approvedDisDifferenceOrderIn.getBizOrgCode(), "批准直营配货差异单");
        if (!DifferenceOrderStatusEnum.AUDITED.getCode().equals(disDelivDifference.getDifferenceStatus())) {
            throw new BusinessException("已审核的配销差异单才可批准");
        }
//        if (stockServer.isSendWms(disDelivDifference.getPosition(), stockInfoOut.getBizOrgCode())) {
//            throw new BusinessException("不下发物流的单据才可批准");
//        }
        //修改差异单状态
        disDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.APPROVED.getCode());
        // 批准差异总数量
        BigDecimal totalApprovalDifferenceQuantity = BigDecimal.ZERO;
        // 批准差异总金额
        BigDecimal totalApprovalDifferenceAmount = BigDecimal.ZERO;
        //获取差异类型
        String differenceType = disDelivDifference.getDifferenceType();
        List<OrdDisDelivDifferenceDetail> differenceDetails = approvedDisDifferenceOrderIn.getDifferenceDetails();
        for (OrdDisDelivDifferenceDetail differenceDetail : differenceDetails) {
            if (Objects.isNull(differenceDetail.getApprovalDifferenceQuantity())) {
                differenceDetail.setApprovalDifferenceQuantity(differenceDetail.getApplyDifferenceQuantity());
            }
            //批准差异金额
            differenceDetail.setDifferenceAmount(differenceDetail.getDistributionUnitPrice().multiply(differenceDetail.getApprovalDifferenceQuantity().setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP)));
            //1+税率
            BigDecimal sell = Objects.isNull(differenceDetail.getSellTax()) ? BigDecimal.ZERO : differenceDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            /** 批准差异去税金额 */
            differenceDetail.setApprovalDifferenceExceptTaxAmount(differenceDetail.getDifferenceAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            /** 批准差异税额 */
            differenceDetail.setApprovalDifferenceTaxAmount(differenceDetail.getDifferenceAmount().subtract(differenceDetail.getApprovalDifferenceExceptTaxAmount()));
            //正差异，取实时仓储库存价，正差异为出库。总部财务库存减少，为负值
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(differenceType)
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(differenceType)) {
                //最新仓储库存价
                BigDecimal warehousePrice = warehouseServer.getWarehousePrice(disDelivDifference.getWrhCode(), disDelivDifference.getPosition(),
                        differenceDetail.getGoodsCode(), stockInfoOut.getBizOrgCode(), differenceDetail.getVendorCode());
                warehousePrice = Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice;
                differenceDetail.setWrhPrice(warehousePrice);
                differenceDetail.setWrhCostAmount(warehousePrice.multiply(differenceDetail.getApprovalDifferenceQuantity()).negate());
                /** 门店成本金额，正差异，门店财务库存增加，为正值 */
                differenceDetail.setStoreCostAmount(differenceDetail.getDistributionUnitPrice().multiply(differenceDetail.getApprovalDifferenceQuantity()));
            } else {
                //负差异，取原单仓储库存价，大库加库存，仓储成本金额要为正值，因为负差异原单批准差异数量为负值，此处相乘取反后为正值。
//                differenceDetail.setWrhCostAmount(differenceDetail.getWrhPrice().multiply(differenceDetail.getApprovalDifferenceQuantity()).negate());
                differenceDetail.setWrhCostAmount(differenceDetail.getDifferenceAmount().abs());
                /** 门店成本金额，负差异，门店财务库存减少，需要为负值, 因为负差异原单批准差异数量为负值，此处直接相乘，无需取反。*/
//                BigDecimal stockPrice = warehouseServer.getStockPrice(disDelivDifference.getStoreCode(), differenceDetail.getGoodsCode(), disDelivDifference.getBizOrgCode());
                differenceDetail.setStoreCostAmount(differenceDetail.getStoreStockPrice().multiply(differenceDetail.getApprovalDifferenceQuantity()));
            }
            /** 仓储成本去税金额 */
            differenceDetail.setWrhExceptTaxAmount(differenceDetail.getWrhCostAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            /** 仓储成本税额 */
            differenceDetail.setWrhTaxAmount(differenceDetail.getWrhCostAmount().subtract(differenceDetail.getWrhExceptTaxAmount()));

            /** 门店成本去税金额 */
            differenceDetail.setStoreExceptTaxAmount(differenceDetail.getStoreCostAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            /** 门店成本税额 */
            differenceDetail.setStoreTaxAmount(differenceDetail.getStoreCostAmount().subtract(differenceDetail.getStoreExceptTaxAmount()));
            //修改明细
            ordDisDelivDifferenceDetailService.updateDisDifferenceDetail(differenceDetail);
            totalApprovalDifferenceAmount = totalApprovalDifferenceAmount.add(differenceDetail.getDifferenceAmount());
            totalApprovalDifferenceQuantity = totalApprovalDifferenceQuantity.add(differenceDetail.getApprovalDifferenceQuantity());
        }
        disDelivDifference.setTotalApprovalDifferenceAmount(totalApprovalDifferenceAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        disDelivDifference.setTotalApprovalDifferenceQuantity(totalApprovalDifferenceQuantity);
        disDelivDifference.setApprovalTime(LocalDateTime.now());
        // 修改差异单
        int count = ordDisDelivDifferenceMapper.updateByPrimaryKeySelective(disDelivDifference);
        // 释放库存,资金
        this.releaseStockAndFund(disDelivDifference, differenceDetails, stockInfoOut, disDelivDifference.getApprovalTime());
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_APPROVED.getName(),
                String.valueOf(disDelivDifference.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                DifferenceOrderStatusEnum.APPROVED.getName(), new Date(), disDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

    /**
     * 冲销配销差异单
     *
     * @param approvedDisDifferenceOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int chargeDisDifference(ApprovedDisDifferenceOrderIn approvedDisDifferenceOrderIn) {
        OrdDisDelivDifference disDelivDifference = ordDisDelivDifferenceMapper.selectByPrimaryKey(approvedDisDifferenceOrderIn.getDiffOrderId());
        if (Objects.isNull(disDelivDifference)) {
            throw new BusinessException("配销差异单不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(disDelivDifference.getPosition(), approvedDisDifferenceOrderIn.getBizOrgCode(), "冲销配销差异单");
        if (NumberUtil.INTEGER_ONE.equals(disDelivDifference.getIsReversal())) {
            throw new BusinessException("该单据已被红冲");
        }
        if (NumberUtil.INTEGER_ONE.equals(disDelivDifference.getIsReversalOrder())) {
            throw new BusinessException("红冲单不能被红冲");
        }
        if (!DifferenceOrderStatusEnum.APPROVED.getCode().equals(disDelivDifference.getDifferenceStatus())) {
            throw new BusinessException("已批准的配销差异单才可冲销");
        }
//        //校验库存
//        String message = checkInv(disDelivDifference, approvedDisDifferenceOrderIn.getDifferenceDetails());
//        if (StringUtil.isNotEmpty(message)) {
//            throw new BusinessException(message);
//        }
        OrdDisDelivDifference reversalDiffOrder = new OrdDisDelivDifference();
        BeanUtils.copy(disDelivDifference, reversalDiffOrder);
        reversalDiffOrder.setTotalApplyDifferenceQuantity(Objects.isNull(disDelivDifference.getTotalApplyDifferenceQuantity()) ? BigDecimal.ZERO : disDelivDifference.getTotalApplyDifferenceQuantity().negate());
        reversalDiffOrder.setTotalApplyDifferenceAmount(Objects.isNull(disDelivDifference.getTotalApplyDifferenceAmount()) ? BigDecimal.ZERO : disDelivDifference.getTotalApplyDifferenceAmount().negate());
        reversalDiffOrder.setTotalApprovalDifferenceQuantity(Objects.isNull(disDelivDifference.getTotalApprovalDifferenceQuantity()) ? BigDecimal.ZERO : disDelivDifference.getTotalApprovalDifferenceQuantity().negate());
        reversalDiffOrder.setTotalApprovalDifferenceAmount(Objects.isNull(disDelivDifference.getTotalApprovalDifferenceAmount()) ? BigDecimal.ZERO : disDelivDifference.getTotalApprovalDifferenceAmount().negate());

        reversalDiffOrder.setId(null);
        reversalDiffOrder.setIsReversal(NumberUtil.INTEGER_ZERO);
        reversalDiffOrder.setIsReversalOrder(NumberUtil.INTEGER_ONE);
        reversalDiffOrder.setApprovalTime(LocalDateTime.now());
        reversalDiffOrder.setCreateTime(LocalDateTime.now());
        reversalDiffOrder.setUpdateTime(LocalDateTime.now());
        //原单号
        reversalDiffOrder.setSourceNo(disDelivDifference.getDifferenceNo());
        // 差异红冲单号
        reversalDiffOrder.setDifferenceNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KXC.getCode(), disDelivDifference.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR));
        //新差异增红冲单
        int count = this.insertSelective(reversalDiffOrder);

        // 添加红冲差异单日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE_SAVE.getName(),
                String.valueOf(reversalDiffOrder.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE_SAVE.getName(),
                new Date(), reversalDiffOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        //修改原差异单
        disDelivDifference.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        disDelivDifference.setIsReversal(NumberUtil.INTEGER_ONE);
        ordDisDelivDifferenceMapper.updateByPrimaryKeySelective(disDelivDifference);
        ArrayList<OrdDisDelivDifferenceDetail> list = new ArrayList<>();
        //红冲单明细
        this.charge(approvedDisDifferenceOrderIn, reversalDiffOrder, list);
        ordDisDelivDifferenceDetailService.batchSave(list);
        // 差异单冲销时调整库存
        this.adjustInv(reversalDiffOrder, list, stockInfoOut);
        //差异单冲销时 资金变动
        this.chargeDisDifferenceToFund(disDelivDifference, reversalDiffOrder.getDifferenceNo());
        // 添加日志
        BusinessLog businessLog1 = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE.getName(),
                String.valueOf(disDelivDifference.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE.getName(), new Date(), disDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog1);
        return count;
    }

    /**
     * 红冲差异单时调用资管清算
     *
     * @param ordDisDelivDifference
     * @param newDifferenceNo
     */
    private void chargeDisDifferenceToFund(OrdDisDelivDifference ordDisDelivDifference, String newDifferenceNo) {
        if (BigDecimal.ZERO.compareTo(ordDisDelivDifference.getTotalApprovalDifferenceAmount()) == 0) {
            return;
        }
        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        String businessType = FundTypeEnum.VARIANCE_SHEET.getCode();
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            rechargeLiquidationIn.setPayOrPrincipalCode(ordDisDelivDifference.getStoreCode());
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
            rechargeLiquidationIn.setRecipientPrincipalCode(ordDisDelivDifference.getBizOrgCode());
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            rechargeLiquidationIn.setRecipientPrincipalCode(ordDisDelivDifference.getStoreCode());
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
            rechargeLiquidationIn.setPayOrPrincipalCode(ordDisDelivDifference.getBizOrgCode());
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            rechargeLiquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
            rechargeLiquidationIn.setOriginalBusinessNo(ordDisDelivDifference.getDifferenceNo());
            rechargeLiquidationIn.setRemark(FundReturnTypeEnum.DIS_DIFFERENCE_ORDER_CHARGE.getName());
            rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
            businessType = FundTypeEnum.DIS_DIFFERENCE_RETURN.getCode();
        }
        rechargeLiquidationIn.setBizOrgCode(ordDisDelivDifference.getBizOrgCode());
        rechargeLiquidationIn.setBusinessNo(newDifferenceNo);
        rechargeLiquidationIn.setBusinessType(businessType);
        rechargeLiquidationIn.setLiquidationAmount(ordDisDelivDifference.getTotalApprovalDifferenceAmount().abs());

        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
        if (!response.isSuccess()) {
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE.name(),
                    String.valueOf(ordDisDelivDifference.getId()), OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                    response.getMessage(),
                    new Date(), ordDisDelivDifference.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            throw new BusinessException(response.getMessage());
        }
        String type = FundDirectionEnum.RETURN.getCode().equals(rechargeLiquidationIn.getDirection()) ?
                DisDifferenceOrderLogEnum.ORD_DIS_CHARGE_BACK_AMOUNT.getValue() : DisDifferenceOrderLogEnum.ORD_DIS_CHARGE_PAY_AMOUNT.getValue();
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE.name(),
                String.valueOf(ordDisDelivDifference.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                MessageFormat.format(type, rechargeLiquidationIn.getLiquidationAmount()),
                new Date(), ordDisDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 库存调整
     *
     * @param disDelivDifference
     * @param differenceDetails
     */
    private void adjustInv(OrdDisDelivDifference disDelivDifference, List<OrdDisDelivDifferenceDetail> differenceDetails, StockInfoOut stockInfoOut) {
        //异步调仓储库存
        StockFlowIn wareFlowIn = this.initWarehouseStockCharge(disDelivDifference, differenceDetails, stockInfoOut);
        // 门店库存
        StockFlowIn storeFlowIn = this.initStoreStockCharge(disDelivDifference, differenceDetails, stockInfoOut);
        List<StockFlowIn> stockFlowInList = Arrays.asList(wareFlowIn, storeFlowIn);
        Response response = stockFlowService.checkStockFlow(stockFlowInList);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
    }

    /**
     * 红冲单门店库存调整初始化
     *
     * @param ordDisDelivDifference
     * @param differenceDetails
     * @return
     */
    private StockFlowIn initStoreStockCharge(OrdDisDelivDifference ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> differenceDetails, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        stockFlowIn.setBizOrgCode(ordDisDelivDifference.getBizOrgCode());
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getName());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getName());
        }
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(ordDisDelivDifference.getCreator());
        stockFlowIn.setOrgCode(ordDisDelivDifference.getOrgCode());
        stockFlowIn.setSourceNo(ordDisDelivDifference.getDifferenceNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        differenceDetails.forEach(item -> {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            //实际数
            stockFlowGoodsIn.setActualQty(item.getApprovalDifferenceQuantity().abs());
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
                //实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            }
            if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
                //实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
            }

            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
            //门店库存初始化公共部分
            initStoreWrh(ordDisDelivDifference, stockInfoOut, stockFlowGoodsIns, item, tar, stockFlowGoodsIn, true);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }


    /**
     * 红冲单仓储库存调整初始化
     *
     * @param ordDisDelivDifference
     * @param differenceDetails
     * @return
     */
    private StockFlowIn initWarehouseStockCharge(OrdDisDelivDifference ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> differenceDetails, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        //负差异
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getName());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getName());
        }
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(ordDisDelivDifference.getCreator());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        stockFlowIn.setSourceNo(ordDisDelivDifference.getDifferenceNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        differenceDetails.forEach(item -> {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            //负差异冲销，减库存
            if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
                // 实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            }
            //正差异冲销，加库存
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
                // 实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
            }
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDisDelivDifference.getPosition());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            //单号
            stockFlowGoodsIn.setSourceNo(ordDisDelivDifference.getDifferenceNo());

            // 实际数
            stockFlowGoodsIn.setActualQty(item.getApprovalDifferenceQuantity().abs());
            //仓储库存公共参数初始化
            this.initWrhStock(stockFlowGoodsIns, item, tar, stockFlowGoodsIn, ordDisDelivDifference, true);

        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    private void checkInvCharge(List<OrdDisDelivDifferenceDetail> differenceDetails, StringJoiner error, String
            bizOrgCode, String storeCode) {
        if (stockStoreService.checkStockIsAllowNegative(storeCode, bizOrgCode)) {
            return;
        }
        Optional.ofNullable(differenceDetails).orElse(new ArrayList<>()).forEach(item -> {
            BigDecimal bigDecimal = this.checkStoreInv(bizOrgCode, item.getGoodsCode(), storeCode);
            BigDecimal bigDecimalTar = Objects.isNull(bigDecimal) ? BigDecimal.ZERO : bigDecimal;
            if (NumberUtil.INTEGER_ZERO > bigDecimalTar.compareTo(item.getApplyDifferenceQuantity())) {
                error.add("商品【" + item.getGoodsCode() + "】" + item.getGoodsName() + "门店可用库存不足");
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOrdDisDelivDifference(OrdDisDelivDifference ordDisDelivDifference) {
        int count = ordDisDelivDifferenceMapper.insertSelective(ordDisDelivDifference);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_SAVE.name(),
                String.valueOf(ordDisDelivDifference.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_SAVE.getName(), new Date(), ordDisDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

    @Override
    public BigDecimal checkStoreInv(String bizOrgCode, String goodsCode, String storeCode) {
        BigDecimal bigDecimal = ordDisDelivDifferenceMapper.checkStoreInv(bizOrgCode, goodsCode, storeCode);
        if (Objects.isNull(bigDecimal)) {
            return BigDecimal.ZERO;
        }
        return bigDecimal;
    }


    @Override
    public BigDecimal checkStockInv(String bizOrgCode, String goodsCode, String stockCode) {
        BigDecimal bigDecimal = ordDisDelivDifferenceMapper.checkStockInv(bizOrgCode, goodsCode, stockCode);
        if (Objects.isNull(bigDecimal)) {
            return BigDecimal.ZERO;
        }
        return bigDecimal;
    }

    /**
     * 保存配销差异单
     *
     * @param saveDifferenceIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response saveDisDifference(SaveDifferenceIn saveDifferenceIn) {
//        //校验 库存，资管
//        Response<String> response = this.verifyDisDifference(saveDifferenceIn);
//        if (!response.isSuccess()) {
//            throw new BusinessException(response.getMessage());
//        }
        OrdDisDelivDifference ordDisDelivDifference = new OrdDisDelivDifference();
        ordDisDelivDifference.setDeliveryOrderNo(saveDifferenceIn.getDeliveryOrderNo());
        ordDisDelivDifference.setDifferenceType(saveDifferenceIn.getDifferenceType());
        int count = ordDisDelivDifferenceMapper.selectCount(ordDisDelivDifference);
        if (count > 0) {
            return Response.success();
        }
        ordDisDelivDifference = this.initDisDifference(saveDifferenceIn);
        //保存差异单
        List<OrdDisDelivDifferenceDetail> details = null;
        log.info("差异单{}-----------------JSON:{}", ordDisDelivDifference.getDifferenceNo(), JSONObject.toJSONString(ordDisDelivDifference));
        int insert = ordDisDelivDifferenceMapper.insertSelective(ordDisDelivDifference);
        if (insert > NumberUtil.INTEGER_ZERO) {
            //保存明细
            details = this.saveDisDifferenceDetail(ordDisDelivDifference, saveDifferenceIn.getDifferenceDetails());
        }
        StockInfoOut stockInfoOut = stockServer.getByCodeAndAuth(ordDisDelivDifference.getPosition(), ordDisDelivDifference.getBizOrgCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("无此仓位信息");
        }
        //如果正差异差异，仓储库存占用
        if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(saveDifferenceIn.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(saveDifferenceIn.getDifferenceType())) {
            //异步调仓储库存
            StockFlowIn stockFlowIn = this.initWarehouseStock(ordDisDelivDifference, details, stockInfoOut, ordDisDelivDifference.getUpdateTime());
            Response stockFlowRes = stockFlowService.checkStockFlow(Collections.singletonList(stockFlowIn));
            if (!stockFlowRes.isSuccess()) {
                throw new BusinessException(stockFlowRes.getMessage());
            }
        }
        //负差异
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(saveDifferenceIn.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(saveDifferenceIn.getDifferenceType())) {
            //异步调门店库存(减)
            StockFlowIn storeFlowIn = this.initStoreStock(ordDisDelivDifference, details, stockInfoOut, ordDisDelivDifference.getUpdateTime());
            List<StockFlowIn> stockFlowIns = Lists.newArrayList();
            stockFlowIns.add(storeFlowIn);
            Response storeFlowRes = stockFlowService.checkStockFlow(stockFlowIns);
            if (!storeFlowRes.isSuccess()) {
                throw new BusinessException(storeFlowRes.getMessage());
            }
        }
        // 下发差异单到Dts
        if (stockServer.isSendWms(ordDisDelivDifference.getPosition(), stockInfoOut.getBizOrgCode())) {
            this.initDifferenceOrderToDts(ordDisDelivDifference, details, stockInfoOut.getBizOrgCode());
        }
        //保存日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_SAVE.name(),
                String.valueOf(ordDisDelivDifference.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_SAVE.getName(), new Date(), ordDisDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success();
    }

    /**
     * 释放库存、资金
     *
     * @param ordDisDelivDifference
     * @param ordDisDelivDifferenceDetails
     */
    @Override
    public void releaseStockAndFund(OrdDisDelivDifference ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> ordDisDelivDifferenceDetails,
                                    StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        List<StockFlowIn> stockFlowInList = Lists.newArrayList();
        // 大仓调整
        StockFlowIn warehouseStockFlowIn = this.initWarehouseStock(ordDisDelivDifference, ordDisDelivDifferenceDetails, stockInfoOut, flowDate);
        stockFlowInList.add(warehouseStockFlowIn);
        // 门店调整
        StockFlowIn storeStockFlowIn = this.initStoreStock(ordDisDelivDifference, ordDisDelivDifferenceDetails, stockInfoOut, flowDate);
        stockFlowInList.add(storeStockFlowIn);
        Response stockFlow = stockFlowService.checkStockFlow(stockFlowInList);
        if (!stockFlow.isSuccess()) {
            throw new BusinessException(stockFlow.getMessage());
        }
        //资管
        toFund(ordDisDelivDifference);
    }

    /**
     * 异步调用资管清算资金
     *
     * @param ordDisDelivDifference
     */
    private void toFund(OrdDisDelivDifference ordDisDelivDifference) {
        if (BigDecimal.ZERO.compareTo(ordDisDelivDifference.getTotalApprovalDifferenceAmount()) == 0) {
            return;
        }
        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        String businessNo = ordDisDelivDifference.getDifferenceNo();
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            rechargeLiquidationIn.setRecipientPrincipalCode(ordDisDelivDifference.getStoreCode());
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
            rechargeLiquidationIn.setPayOrPrincipalCode(ordDisDelivDifference.getBizOrgCode());
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            rechargeLiquidationIn.setBusinessType(FundTypeEnum.DIS_DIFFERENCE_RETURN.getCode());
            rechargeLiquidationIn.setOriginalBusinessNo(ordDisDelivDifference.getDifferenceNo());
            rechargeLiquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            rechargeLiquidationIn.setPayOrPrincipalCode(ordDisDelivDifference.getStoreCode());
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
            rechargeLiquidationIn.setRecipientPrincipalCode(ordDisDelivDifference.getBizOrgCode());
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            rechargeLiquidationIn.setBusinessType(FundTypeEnum.VARIANCE_SHEET.getCode());
            rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
        }
        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        rechargeLiquidationIn.setBizOrgCode(ordDisDelivDifference.getBizOrgCode());
        rechargeLiquidationIn.setBusinessNo(businessNo);
        rechargeLiquidationIn.setLiquidationAmount(ordDisDelivDifference.getTotalApprovalDifferenceAmount().abs());
        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
        if (!response.isSuccess()) {
            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE.name(),
                    String.valueOf(ordDisDelivDifference.getId()), OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                    response.getMessage(),
                    new Date(), ordDisDelivDifference.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            throw new BusinessException(response.getMessage());
        }
        String type = FundDirectionEnum.RETURN.getCode().equals(rechargeLiquidationIn.getDirection()) ?
                DisDifferenceOrderLogEnum.ORD_DIS_APPROVED_BACK_AMOUNT.getValue() : DisDifferenceOrderLogEnum.ORD_DIS_APPROVED_PAY_AMOUNT.getValue();
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE.name(),
                String.valueOf(ordDisDelivDifference.getId()), OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                MessageFormat.format(type, rechargeLiquidationIn.getLiquidationAmount()),
                new Date(), ordDisDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public List<OrdDisDelivDifferenceDetail> initDifferenceBillVO(OrdDisDelivDifference ordDisDelivDifference, List<DifferenceBillDtlVO> detail, String centerStockBizOrgCode) {
        OrdDisDelivDifferenceDetailIn ordDisDelivDifferenceDetailIn = new OrdDisDelivDifferenceDetailIn();
        String differenceType = ordDisDelivDifference.getDifferenceType();
        ordDisDelivDifferenceDetailIn.setDiffOrderId(ordDisDelivDifference.getId());
        Page<OrdDisDelivDifferenceDetailOut> pageOut = ordDisDelivDifferenceDetailService.findDifferenceOrderDtlListByParameter(ordDisDelivDifferenceDetailIn);
        Map<String, OrdDisDelivDifferenceDetailOut> detailMap = CollectionUtils.emptyIfNull(pageOut.getList()).stream()
                .collect(Collectors.toMap(ordDisDelivDifferenceDetailOut -> ordDisDelivDifferenceDetailOut.getGoodsCode()
                        + SystemConstant.SHORT_LINE + ordDisDelivDifferenceDetailOut.getLine(), Function.identity()));
        // 批准差异总数量
        BigDecimal totalApprovalDifferenceQuantity = BigDecimal.ZERO;
        // 批准差异总金额
        BigDecimal totalApprovalDifferenceAmount = BigDecimal.ZERO;
        List<OrdDisDelivDifferenceDetail> details = new ArrayList<>();
        for (DifferenceBillDtlVO item : detail) {
            OrdDisDelivDifferenceDetail differenceDetail = new OrdDisDelivDifferenceDetail();
            OrdDisDelivDifferenceDetailOut ordDisDelivDifferenceDetailOut = detailMap.get(item.getFarticlecode() + SystemConstant.SHORT_LINE + item.getLine());
            differenceDetail.setDifferenceOrderId(ordDisDelivDifference.getId());
            BeanUtils.copy(ordDisDelivDifferenceDetailOut, differenceDetail);
            //批准数量
            differenceDetail.setApprovalDifferenceQuantity(item.getFrealqty());
            //批准差异金额
            differenceDetail.setDifferenceAmount(item.getFrealqty().multiply(differenceDetail.getDistributionUnitPrice()));
            //1+税率
            BigDecimal sell = Objects.isNull(differenceDetail.getSellTax()) ? BigDecimal.ZERO : differenceDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            //批准差异去税金额
            differenceDetail.setApprovalDifferenceExceptTaxAmount(differenceDetail.getDifferenceAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            // 批准差异税额
            differenceDetail.setApprovalDifferenceTaxAmount(differenceDetail.getDifferenceAmount().subtract(differenceDetail.getApprovalDifferenceExceptTaxAmount()));

            //正差异，取实时仓储库存价，正差异为出库。总部财务库存减少，为负值
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(differenceType)
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(differenceType)) {
                //最新仓储库存价
                BigDecimal warehousePrice = warehouseServer.getWarehousePrice(ordDisDelivDifference.getWrhCode(), ordDisDelivDifference.getPosition(),
                        differenceDetail.getGoodsCode(), centerStockBizOrgCode, differenceDetail.getVendorCode());
                warehousePrice = Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice;
                differenceDetail.setWrhPrice(warehousePrice);
                differenceDetail.setWrhCostAmount(warehousePrice.multiply(differenceDetail.getApprovalDifferenceQuantity()).negate());
                /** 门店成本金额，正差异，门店财务库存增加，为正值 */
                differenceDetail.setStoreCostAmount(differenceDetail.getDistributionUnitPrice().multiply(differenceDetail.getApprovalDifferenceQuantity()));
            } else {
                //负差异，取原单仓储库存价，大库加库存，仓储成本金额要为正值，因为负差异原单批准差异数量为负值，此处相乘取反后为正值。
//                differenceDetail.setWrhCostAmount(differenceDetail.getWrhPrice().multiply(differenceDetail.getApprovalDifferenceQuantity()).negate());
                differenceDetail.setWrhCostAmount(differenceDetail.getDifferenceAmount().abs());
                /** 门店成本金额，负差异，门店财务库存减少，需要为负值, 因为负差异原单批准差异数量为负值，此处直接相乘，无需取反。*/
                differenceDetail.setStoreCostAmount(differenceDetail.getDistributionUnitPrice().multiply(differenceDetail.getApprovalDifferenceQuantity()));
            }
            /** 仓储成本去税金额 */
            differenceDetail.setWrhExceptTaxAmount(differenceDetail.getWrhCostAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            /** 仓储成本税额 */
            differenceDetail.setWrhTaxAmount(differenceDetail.getWrhCostAmount().subtract(differenceDetail.getWrhExceptTaxAmount()));

            /** 门店成本去税金额 */
            differenceDetail.setStoreExceptTaxAmount(differenceDetail.getStoreCostAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            /** 门店成本税额 */
            differenceDetail.setStoreTaxAmount(differenceDetail.getStoreCostAmount().subtract(differenceDetail.getStoreExceptTaxAmount()));

            totalApprovalDifferenceAmount = totalApprovalDifferenceAmount.add(differenceDetail.getDifferenceAmount());

            totalApprovalDifferenceQuantity = totalApprovalDifferenceQuantity.add(differenceDetail.getApprovalDifferenceQuantity());
            details.add(differenceDetail);
        }
        ordDisDelivDifference.setTotalApprovalDifferenceAmount(totalApprovalDifferenceAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDisDelivDifference.setTotalApprovalDifferenceQuantity(totalApprovalDifferenceQuantity);
        return details;
    }

    @Override
    public List<DisDifferenceOrderOut> findDisDifferenceOrders(OrdDisDelivDifference disDelivDifference) {
        disDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.AUDITED.getCode());
        List<DisDifferenceOrderOut> disDifferenceOrders = ordDisDelivDifferenceMapper.findDisDifferenceOrders(disDelivDifference);
        disDifferenceOrders.forEach(item -> {
            item.setDifferenceStatusName(DifferenceOrderStatusEnum.getNameByCode(item.getDifferenceStatus()));
        });
        return disDifferenceOrders;
    }

    /**
     * 配销差异单回传
     *
     * @param differenceBillVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean differenceOrderCallBack(DifferenceBillVO differenceBillVO) {
        OrdDisDelivDifference query = new OrdDisDelivDifference();
//        DifferenceBillVO differenceBillVO = JSON.parseObject(messageJson, DifferenceBillVO.class);
        List<DifferenceBillDtlVO> detail = differenceBillVO.getDetail();
        query.setDifferenceNo(differenceBillVO.getFsrcnum());
        OrdDisDelivDifference ordDisDelivDifference = this.selectOne(query);
        if (Objects.isNull(ordDisDelivDifference) || CollectionUtils.isEmpty(detail)) {
//            throw new BusinessException("此差异单不存在" + differenceBillVO.getFsrcnum());
            log.error("此差异单{}不存在", differenceBillVO.getFsrcnum());
            return true;
        }
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisDelivDifference.getPosition());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("无此仓位信息");
        }
        if (!DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
//            //dts回传批准差异单时，改差异单后台已批准 记录日志
//            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
//                    OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_APPROVED_SYSTEM.name(),
//                    String.valueOf(ordDisDelivDifference.getId()),
//                    OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
//                    OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_APPROVED_SYSTEM.getName(), new Date(), ordDisDelivDifference.getUpdater());
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            log.error("配销差异单{}DTS回传状态不正确", ordDisDelivDifference.getDifferenceNo());
//            return "该差异单状态不正确";
            return true;
        }
        ordDisDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.APPROVED.getCode());
        ordDisDelivDifference.setLogisticsNo(differenceBillVO.getNum());
        ordDisDelivDifference.setApprovalTime(LocalDateTime.now());
        ordDisDelivDifference.setUpdater(differenceBillVO.getFfiller());
        //初始化差异单
        List<OrdDisDelivDifferenceDetail> ordDisDelivDifferenceDetails = this.initDifferenceBillVO(ordDisDelivDifference, detail, stockInfoOut.getBizOrgCode());
        // 修改差异单
        ordDisDelivDifferenceMapper.updateByPrimaryKeySelective(ordDisDelivDifference);
        //  批量修改差异单及明细
        ordDisDelivDifferenceDetailService.batchUpdate(ordDisDelivDifferenceDetails);
        // 释放库存,资金
        this.releaseStockAndFund(ordDisDelivDifference, ordDisDelivDifferenceDetails, stockInfoOut, ordDisDelivDifference.getApprovalTime());
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_APPROVED.getName(),
                String.valueOf(ordDisDelivDifference.getId()),
                OrdLogTypeEnum.ORD_DIS_ORDER_DIFFERENCE.getCode(),
                DifferenceOrderStatusEnum.APPROVED.getName(), new Date(), ordDisDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        return "成功";
        return true;
    }

    @Override
    public String getDifferenceNoByDeliveryNo(String deliveryOrderNo, String bizOrgCode) {
        return ordDisDelivDifferenceMapper.getDifferenceNoByDeliveryNo(deliveryOrderNo, bizOrgCode);
    }

    @Override
    public String getDeliveryNoByDiffNo(String differenceOrderNo, String bizOrgCode) {
        return ordDisDelivDifferenceMapper.getDeliveryNoByDiffNo(differenceOrderNo, bizOrgCode);
    }

    @Override
    public DisDiffOrderSummaryOut diffOrderSummary(DisDifferenceOrderIn differenceOrderIn) {
        this.handleChannelInfo(differenceOrderIn);
        if (StringUtils.isNotBlank(differenceOrderIn.getStoreArea())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(differenceOrderIn.getStoreArea(), differenceOrderIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return new DisDiffOrderSummaryOut();
            }
            differenceOrderIn.setStoreCodeList(storeCodeList);
        }
        return ordDisDelivDifferenceMapper.diffOrderSummary(differenceOrderIn);
    }

    @Override
    public OrdDisDelivDifference getOneByOrderNo(String differenceNo) {
        OrdDisDelivDifference dirDelivDifference = new OrdDisDelivDifference();
        dirDelivDifference.setDifferenceNo(differenceNo);
        return ordDisDelivDifferenceMapper.selectOne(dirDelivDifference);
    }

    /**
     * 发差异单到dts
     *
     * @param ordDisDelivDifference
     * @param details
     */
    private void initDifferenceOrderToDts(OrdDisDelivDifference ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> details, String centerStockBizOrgCode) {
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDisDelivDifference.getStoreCode());
        DifferenceBillIn differenceBillIn = new DifferenceBillIn();
        differenceBillIn.setPlatform_bill_id(ordDisDelivDifference.getDifferenceNo());
        differenceBillIn.setBill_create_date(ordDisDelivDifference.getCreateTime());
        differenceBillIn.setLast_update_time(ordDisDelivDifference.getCreateTime());
        differenceBillIn.setRelation_bill_id(ordDisDelivDifference.getDeliveryOrderNo());
        //物流配单单号
        differenceBillIn.setLogistics_id(null);
        //类型
        differenceBillIn.setBill_type_id(null);
        // 仓储代码
        differenceBillIn.setWarehouse_id(ordDisDelivDifference.getWrhCode());
        //仓位代码
        differenceBillIn.setSource_stock_id(ordDisDelivDifference.getPosition());
        // 门店Id
        differenceBillIn.setShop_id(Objects.isNull(storeOut) ? "" : storeOut.getStoreId().toString());
        //门店代码
        differenceBillIn.setShop_code(ordDisDelivDifference.getStoreCode());
        //填单人
        differenceBillIn.setCreater(ordDisDelivDifference.getCreator());
        //最后修改人
        differenceBillIn.setModifier(ordDisDelivDifference.getCreator());
        differenceBillIn.setMemo(ordDisDelivDifference.getRemark());
        //渠道
        differenceBillIn.setChannel_id(null);
        //来源单位
        differenceBillIn.setSource_unit(null);
        //发送时间
        differenceBillIn.setSend_time(LocalDateTime.now());
        //来源组织
        differenceBillIn.setSource_organization(centerStockBizOrgCode);
        //目标组织
        differenceBillIn.setTarget_organization(centerStockBizOrgCode);
        //初始化发dts差异明细
        List<DifferenceBillDtlIn> dtlIns = initDifferenceBillDt(ordDisDelivDifference, details);
        //明细
        differenceBillIn.setDetail_list(dtlIns);
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_DIFFERENCE_ORDER_TO_DTS, JSONObject.toJSONString(differenceBillIn),
//                ordDisDelivDifference.getBizOrgCode(), ordDisDelivDifference.getDifferenceNo());
        SendResponse sendResponse = disDifferenceOrderToDtsSender.sendSync(JSONObject.toJSONString(differenceBillIn).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("DTS配销差异单{}下发DTS消息ID---{}", ordDisDelivDifference.getDifferenceNo(), sendResponse.getMessageId());
    }

    /**
     * 初始化发dts差异明细
     *
     * @param ordDisDelivDifference
     * @param details
     */
    private List<DifferenceBillDtlIn> initDifferenceBillDt(OrdDisDelivDifference
                                                                   ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> details) {
        List<DifferenceBillDtlIn> dtlIns = new ArrayList<>();
        details.forEach(item -> {
            OrgGoodsTransInfo goodsOut = orderGoodsServer.getGoodsOut(item.getGoodsCode(), ordDisDelivDifference.getBizOrgCode());
            DifferenceBillDtlIn billDtlIn = new DifferenceBillDtlIn();
            billDtlIn.setPlatform_bill_id(ordDisDelivDifference.getDifferenceNo());
            //行号
            billDtlIn.setLine(item.getLine());
            //  组织商品id
            billDtlIn.setSku_id(Objects.isNull(goodsOut) ? "" : goodsOut.getId().toString());
            //商品代码
            billDtlIn.setSku_code(item.getGoodsCode());
            //差异数量
            billDtlIn.setQuantity(item.getApplyDifferenceQuantity());
            //配货价格
            billDtlIn.setPrice_i(item.getDistributionUnitPrice());
            //生产日期
            billDtlIn.setProduce_date(null);
            //来源组织
            billDtlIn.setSource_organization(ordDisDelivDifference.getBizOrgCode());
            //目标组织
            billDtlIn.setTarget_organization(ordDisDelivDifference.getBizOrgCode());
            dtlIns.add(billDtlIn);
        });
        return dtlIns;
    }

    /**
     * 初始化仓储调整入参
     *
     * @param ordDisDelivDifference
     * @param details
     */
    private StockFlowIn initWarehouseStock(OrdDisDelivDifference ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> details, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getName());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getName());
        }
        stockFlowIn.setFlowDate(flowDate);
        stockFlowIn.setCreator(ordDisDelivDifference.getCreator());
        stockFlowIn.setOperationType(ordDisDelivDifference.getDifferenceStatus());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        stockFlowIn.setSourceNo(ordDisDelivDifference.getDifferenceNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        details.forEach(item -> {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            String applyLowering = null;
            BigDecimal applyQty = null;
            String actualLowering = null;
            BigDecimal actualQty = null;
            //正差异
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
                if (DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
                    //申请增/减
                    applyLowering = AdjustTypeEnum.ADD.getCode();
                    //申请数
                    applyQty = item.getApplyDifferenceQuantity().abs();
                }
                if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
                    // 实际增/减
                    actualLowering = AdjustTypeEnum.REDUCE.getCode();
                    // 实际数
                    actualQty = item.getApprovalDifferenceQuantity().abs();
                    //申请增/减
                    applyLowering = AdjustTypeEnum.REDUCE.getCode();
                    //申请数
                    applyQty = item.getApplyDifferenceQuantity().abs();
                }
                //负差异
            } else {
                if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
                    // 实际增/减
                    actualLowering = AdjustTypeEnum.ADD.getCode();
                    // 实际数
                    actualQty = item.getApprovalDifferenceQuantity().abs();
                }
            }
            stockFlowGoodsIn.setActualLowering(actualLowering);
            stockFlowGoodsIn.setActualQty(actualQty);
            stockFlowGoodsIn.setApplyLowering(applyLowering);
            stockFlowGoodsIn.setApplyQty(applyQty);
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDisDelivDifference.getPosition());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            stockFlowGoodsIn.setSourceNo(ordDisDelivDifference.getDifferenceNo());
            //库存发生位置
            this.initWrhStock(stockFlowGoodsIns, item, tar, stockFlowGoodsIn, ordDisDelivDifference, false);

        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    /**
     * 仓储库存公共参数初始化
     *
     * @param stockFlowGoodsIns
     * @param item
     * @param tar
     * @param stockFlowGoodsIn
     */
    private void initWrhStock(List<StockFlowGoodsIn> stockFlowGoodsIns, OrdDisDelivDifferenceDetail item, BigDecimal tar,
                              StockFlowGoodsIn stockFlowGoodsIn, OrdDisDelivDifference ordDisDelivDifference, Boolean isCharge) {
        stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
        //批准
        if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
            //发生价为仓储库存价
            stockFlowGoodsIn.setPrice(item.getWrhPrice());
            //批准金额
//            BigDecimal applyDifferenceAmount = BigDecimal.ZERO;
            BigDecimal differenceAmount = item.getDifferenceAmount().abs();
//            BigDecimal applyDifferenceAmount = BigDecimal.ZERO;
            BigDecimal taxAmount = AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering())
                    ? differenceAmount.negate() : differenceAmount;
            BigDecimal costTaxAmount = AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering())
                    ? item.getWrhCostAmount() : isCharge ? item.getWrhCostAmount().abs() : taxAmount;
            stockFlowGoodsIn.setCostTaxAmount(costTaxAmount);

//            //实际减，为负值(正差异批准释放业务占用库存)
//            if (AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering()) && NumberUtil.INTEGER_ZERO.equals(ordDisDelivDifference.getIsReversalOrder())) {
//                //批准金额
//                applyDifferenceAmount = stockFlowGoodsIn.getActualQty().multiply(item.getDistributionUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP).negate();
//            }
            //批准差异去税金额
            BigDecimal applyDifferenceExceptTaxAmount = taxAmount.divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //批准差异税额
            BigDecimal applyDifferenceTaxAmount = taxAmount.subtract(applyDifferenceExceptTaxAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //含税金额
            stockFlowGoodsIn.setTaxAmount(taxAmount);
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(applyDifferenceExceptTaxAmount);
            //税额
            stockFlowGoodsIn.setTax(applyDifferenceTaxAmount);
        }
        //审核，占用库存，为正值
        if (DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
            //无财务库存变更，取值原单配货单价
            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
            //申请差异去税金额
            BigDecimal applyDifferenceExceptTaxAmount = item.getApplyDifferenceAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //申请差异税额
            BigDecimal applyDifferenceTaxAmount = item.getApplyDifferenceAmount().subtract(applyDifferenceExceptTaxAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(applyDifferenceExceptTaxAmount);
            //含税金额
            stockFlowGoodsIn.setTaxAmount(item.getApplyDifferenceAmount());
            // 税额
            stockFlowGoodsIn.setTax(applyDifferenceTaxAmount);
        }
        //成本含税金额，和原单保持一致，直接取原单值
//        stockFlowGoodsIn.setCostTaxAmount(item.getWrhCostAmount());
        //成本不含税金额
        stockFlowGoodsIn.setCostNonTaxAmount(item.getWrhExceptTaxAmount());
        //成本税额
        stockFlowGoodsIn.setCostTax(item.getWrhTaxAmount());

        // 2023-06-27 增加差异单不看是否负库存
        stockFlowGoodsIn.setIsCheckNegativeStock(false);

        stockFlowGoodsIns.add(stockFlowGoodsIn);
    }

    /**
     * 初始化门店库存
     *
     * @param ordDisDelivDifference
     * @param details
     */
    private StockFlowIn initStoreStock(OrdDisDelivDifference ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> details, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(ordDisDelivDifference.getBizOrgCode());
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_LOSS.getName());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_POOR_JUST.getName());
        }
        stockFlowIn.setFlowDate(flowDate);
        stockFlowIn.setCreator(ordDisDelivDifference.getCreator());
        stockFlowIn.setOperationType(ordDisDelivDifference.getDifferenceStatus());
        stockFlowIn.setOrgCode(ordDisDelivDifference.getOrgCode());
        stockFlowIn.setSourceNo(ordDisDelivDifference.getDifferenceNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        details.forEach(item -> {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            String applyLowering = null;
            BigDecimal applyQty = null;
            String actualLowering = null;
            BigDecimal actualQty = null;
            //负差异
            if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDisDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDisDelivDifference.getDifferenceType())) {
                if (DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
                    //申请增/减
                    applyLowering = AdjustTypeEnum.ADD.getCode();
                    //申请数
                    applyQty = item.getApplyDifferenceQuantity().abs();
                }
                if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
                    //实际增/减
                    actualLowering = AdjustTypeEnum.REDUCE.getCode();
                    //实际数
                    actualQty = item.getApprovalDifferenceQuantity().abs();
                    //申请增/减
                    applyLowering = AdjustTypeEnum.REDUCE.getCode();
                    //申请数
                    applyQty = item.getApplyDifferenceQuantity().abs();
                    //负差异批准，门店库存调整取原单门店配货价
                    stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
                }
                //正差异
            } else {
                if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
                    //实际增/减
                    actualLowering = AdjustTypeEnum.ADD.getCode();
                    //实际数
                    actualQty = item.getApprovalDifferenceQuantity().abs();
                    //正差异批准，门店库存调整取原单门店配货价
                    stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
                }
            }
            //实际数
            stockFlowGoodsIn.setActualQty(actualQty);
            //实际增/减
            stockFlowGoodsIn.setActualLowering(actualLowering);
            //申请增/减
            stockFlowGoodsIn.setApplyLowering(applyLowering);
            //申请数
            stockFlowGoodsIn.setApplyQty(applyQty);
            // 封装门店库存调整数据
            this.initStoreWrh(ordDisDelivDifference, stockInfoOut, stockFlowGoodsIns, item, tar, stockFlowGoodsIn, false);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    /**
     * 门店库存初始化公共部分
     *
     * @param ordDisDelivDifference
     * @param stockInfoOut
     * @param stockFlowGoodsIns
     * @param item
     * @param tar
     * @param stockFlowGoodsIn
     */
    private void initStoreWrh(OrdDisDelivDifference ordDisDelivDifference, StockInfoOut stockInfoOut, List<StockFlowGoodsIn> stockFlowGoodsIns,
                              OrdDisDelivDifferenceDetail item, BigDecimal tar, StockFlowGoodsIn stockFlowGoodsIn, Boolean isCharge) {
        stockFlowGoodsIn.setStoreCode(ordDisDelivDifference.getStoreCode());
        stockFlowGoodsIn.setStoreName(ordDisDelivDifference.getStoreName());
        stockFlowGoodsIn.setStockCode(ordDisDelivDifference.getPosition());
        stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
        stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
        stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
        //单号
        stockFlowGoodsIn.setSourceNo(ordDisDelivDifference.getDifferenceNo());
        //库存发生位置
        stockFlowGoodsIn.setPosition(StockHappenLieEnum.STORE.getCode());
        if (Objects.isNull(stockFlowGoodsIn.getPrice())) {
            //发生价
            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
        }
        //成本金额已和原单保持一致，直接取原单值
        //成本不含税金额
        stockFlowGoodsIn.setCostNonTaxAmount(item.getStoreExceptTaxAmount());
        //成本含税金额
        stockFlowGoodsIn.setCostTaxAmount(item.getStoreCostAmount());
        //成本税额
        stockFlowGoodsIn.setCostTax(item.getStoreTaxAmount());
        //批准
        if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDisDelivDifference.getDifferenceStatus())) {
            //批准差异金额
//            item.setDifferenceAmount(item.getDistributionUnitPrice().multiply(item.getApprovalDifferenceQuantity().setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP)));
            //批准差异去税金额
//            item.setApprovalDifferenceExceptTaxAmount(item.getDifferenceAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //批准差异税额
//            item.setApprovalDifferenceTaxAmount(item.getDifferenceAmount().subtract(item.getApprovalDifferenceExceptTaxAmount()));
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(item.getApprovalDifferenceExceptTaxAmount());
            //含税金额
//            stockFlowGoodsIn.setTaxAmount(item.getApprovalDifferenceExceptTaxAmount().multiply(tar).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//            stockFlowGoodsIn.setTaxAmount(item.getDifferenceAmount());

            BigDecimal differenceAmount = item.getDifferenceAmount().abs();
            BigDecimal taxAmount = AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering())
                    ? differenceAmount.negate() : differenceAmount;
            stockFlowGoodsIn.setTaxAmount(taxAmount);
            BigDecimal costTaxAmount;
            if (AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering())) {
                BigDecimal storeStockPrice = warehouseServer.getStockPrice(ordDisDelivDifference.getStoreCode(), item.getGoodsCode(), ordDisDelivDifference.getBizOrgCode());
//                costTaxAmount = storeStockPrice.multiply(item.getApprovalDifferenceQuantity());
//                costTaxAmount = isCharge ? item.getStoreCostAmount() : storeStockPrice.multiply(item.getApprovalDifferenceQuantity());
                costTaxAmount = item.getStoreCostAmount();
            } else {
//                costTaxAmount = taxAmount;
                costTaxAmount = isCharge ? item.getStoreCostAmount() : taxAmount;
            }
            stockFlowGoodsIn.setCostTaxAmount(costTaxAmount);


            // 税额
            stockFlowGoodsIn.setTax(item.getApprovalDifferenceTaxAmount());
            if (StringUtils.isNotBlank(item.getExpiry())) {
                stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(item.getExpiry(), stockFlowGoodsIn.getActualQty())));
            }
            //审核
        } else {
            //申请差异去税金额
            BigDecimal applyDifferenceExceptTaxAmount = item.getApplyDifferenceAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //申请差异税额
            BigDecimal applyDifferenceTaxAmount = item.getApplyDifferenceAmount().subtract(applyDifferenceExceptTaxAmount).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP);
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(applyDifferenceExceptTaxAmount);
            //含税金额
            stockFlowGoodsIn.setTaxAmount(item.getApplyDifferenceAmount());
            // 税额
            stockFlowGoodsIn.setTax(applyDifferenceTaxAmount);
        }
        // 2023-06-27 增加差异单不看是否负库存
        stockFlowGoodsIn.setIsCheckNegativeStock(false);
        stockFlowGoodsIns.add(stockFlowGoodsIn);
    }

    /**
     * 保存差异单明细
     *
     * @param ordDisDelivDifference
     * @param differenceDetails
     */
    private List<OrdDisDelivDifferenceDetail> saveDisDifferenceDetail(OrdDisDelivDifference ordDisDelivDifference, List<OrdDisDelivDifferenceDetail> differenceDetails) {
        List<OrdDisDelivDifferenceDetail> details = differenceDetails.stream().map(item -> this.initDifferenceOrderDetail(ordDisDelivDifference, item)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(details)) {
            ordDisDelivDifferenceDetailService.batchSave(details);
        }
        return details;
    }

    private OrdDisDelivDifferenceDetail initDifferenceOrderDetail(OrdDisDelivDifference ordDisDelivDifference,
                                                                  OrdDisDelivDifferenceDetail item) {
        OrdDisDelivDifferenceDetail detail = new OrdDisDelivDifferenceDetail();
        BeanUtils.copy(item, detail);
        detail.setCreateTime(ordDisDelivDifference.getCreateTime());
        detail.setCreator(ordDisDelivDifference.getCreator());
        detail.setUpdateTime(ordDisDelivDifference.getUpdateTime());
        detail.setUpdater(ordDisDelivDifference.getUpdater());
        detail.setDifferenceOrderId(ordDisDelivDifference.getId());
        detail.setIsDelete(NumberUtil.INTEGER_ZERO);
        return detail;
    }

    /**
     * 初始化差异单信息
     *
     * @param saveDifferenceIn
     * @return
     */
    private OrdDisDelivDifference initDisDifference(SaveDifferenceIn saveDifferenceIn) {
        OrdDisDelivDifference ordDisDelivDifference = new OrdDisDelivDifference();
        BeanUtils.copy(saveDifferenceIn, ordDisDelivDifference);
        ordDisDelivDifference.setPosition(saveDifferenceIn.getStockCode());
        ordDisDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.AUDITED.getCode());
        ordDisDelivDifference.setIsReversal(NumberUtil.INTEGER_ZERO);
        ordDisDelivDifference.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        ordDisDelivDifference.setIsDelete(NumberUtil.INTEGER_ZERO);
        ordDisDelivDifference.setDifferenceNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KXC.getCode(), saveDifferenceIn.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR));
        return ordDisDelivDifference;
    }

    /**
     * 红冲单填充
     *
     * @param approvedDisDifferenceOrderIn
     * @param query
     * @param list
     */
    private void charge(ApprovedDisDifferenceOrderIn approvedDisDifferenceOrderIn, OrdDisDelivDifference
            query, ArrayList<OrdDisDelivDifferenceDetail> list) {
        for (OrdDisDelivDifferenceDetail item : approvedDisDifferenceOrderIn.getDifferenceDetails()) {
            OrdDisDelivDifferenceDetail ordDisDelivDifferenceDetail = new OrdDisDelivDifferenceDetail();
            BeanUtils.copy(item, ordDisDelivDifferenceDetail);
            ordDisDelivDifferenceDetail.setDifferenceOrderId(query.getId());
            ordDisDelivDifferenceDetail.setId(null);
            //发货量
            ordDisDelivDifferenceDetail.setDeliveryQuantity(ordDisDelivDifferenceDetail.getDeliveryQuantity().negate());
            //发货包装数
            ordDisDelivDifferenceDetail.setDeliveryPackageQuantity(ordDisDelivDifferenceDetail.getDeliveryPackageQuantity().negate());
            //申请差异数量
            ordDisDelivDifferenceDetail.setApplyDifferenceQuantity(ordDisDelivDifferenceDetail.getApplyDifferenceQuantity().negate());
            // 申请差异金额
            ordDisDelivDifferenceDetail.setApplyDifferenceAmount(ordDisDelivDifferenceDetail.getApplyDifferenceAmount().negate());
            //批准差异数量
            ordDisDelivDifferenceDetail.setApprovalDifferenceQuantity(ordDisDelivDifferenceDetail.getApprovalDifferenceQuantity().negate());
            //批准差异金额
            ordDisDelivDifferenceDetail.setDifferenceAmount(ordDisDelivDifferenceDetail.getDifferenceAmount().negate());
            //批准差异去税金额
            ordDisDelivDifferenceDetail.setApprovalDifferenceTaxAmount(ordDisDelivDifferenceDetail.getApprovalDifferenceTaxAmount().negate());
            //批准差异税额
            ordDisDelivDifferenceDetail.setApprovalDifferenceTaxAmount(ordDisDelivDifferenceDetail.getApprovalDifferenceTaxAmount().negate());
            //仓储成本金额
            ordDisDelivDifferenceDetail.setWrhCostAmount(ordDisDelivDifferenceDetail.getWrhCostAmount().negate());
            //仓储成本去税金额
            ordDisDelivDifferenceDetail.setWrhExceptTaxAmount(ordDisDelivDifferenceDetail.getWrhExceptTaxAmount().negate());
            //仓储成本税额
            ordDisDelivDifferenceDetail.setWrhTaxAmount(ordDisDelivDifferenceDetail.getWrhTaxAmount().negate());
            //门店成本金额
            ordDisDelivDifferenceDetail.setStoreCostAmount(ordDisDelivDifferenceDetail.getStoreCostAmount().negate());
            //门店成本去税金额
            ordDisDelivDifferenceDetail.setStoreExceptTaxAmount(ordDisDelivDifferenceDetail.getStoreExceptTaxAmount().negate());
            //门店成本税额
            ordDisDelivDifferenceDetail.setStoreTaxAmount(ordDisDelivDifferenceDetail.getStoreTaxAmount().negate());
            ordDisDelivDifferenceDetail.setCreator(query.getCreator());
            ordDisDelivDifferenceDetail.setCreateTime(LocalDateTime.now());
            list.add(ordDisDelivDifferenceDetail);
        }
    }

    /**
     * 解析数据
     *
     * @param list
     * @return
     */
    private List<ExcelDisDifferenceOrder> parseDataToExcel(List<DisDifferenceOrderOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 转化excel
     *
     * @param disDifferenceOrderOut
     * @param index
     * @return
     */
    private ExcelDisDifferenceOrder convertExcel(DisDifferenceOrderOut disDifferenceOrderOut, int index) {

        ExcelDisDifferenceOrder excelDisDifferenceOrder = new ExcelDisDifferenceOrder();
        BeanUtils.copy(disDifferenceOrderOut, excelDisDifferenceOrder);
        if (Objects.nonNull(disDifferenceOrderOut)) {
            excelDisDifferenceOrder.setIsRedRush(NumberUtil.INTEGER_ZERO.equals(disDifferenceOrderOut.getIsReversalOrder()) ? "否" : "是");
            excelDisDifferenceOrder.setDifferenceStatusName(DifferenceOrderStatusEnum.getNameByCode(disDifferenceOrderOut.getDifferenceStatus()));
            // 仓位名称
            StockTransInfoOut stockOut = orderGoodsServer.getTransInfo(disDifferenceOrderOut.getPosition(), disDifferenceOrderOut.getBizOrgCode());
            excelDisDifferenceOrder.setStockName(Objects.nonNull(stockOut) ? stockOut.getStockName() + "【" + stockOut.getStockCode() + "】" : "");
            //差异单类型
            excelDisDifferenceOrder.setDifferenceTypeName(DifferenceOrderTypeEnum.getNameByCode(disDifferenceOrderOut.getDifferenceType()));
        }
        excelDisDifferenceOrder.setIndex(index + 1);
        return excelDisDifferenceOrder;

    }


}
