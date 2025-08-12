package com.edc.erp.directly.dirdifferenceorder.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
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
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifferenceDetail;
import com.edc.erp.directly.dirdifferenceorder.mapper.OrdDirDelivDifferenceMapper;
import com.edc.erp.directly.dirdifferenceorder.model.in.ApprovedDirDifferenceOrderIn;
import com.edc.erp.directly.dirdifferenceorder.model.in.DirDifferenceOrderIn;
import com.edc.erp.directly.dirdifferenceorder.model.in.OrdDirDelivDifferenceDetailIn;
import com.edc.erp.directly.dirdifferenceorder.model.in.SaveDifferenceIn;
import com.edc.erp.directly.dirdifferenceorder.model.out.DirDiffOrderSummaryOut;
import com.edc.erp.directly.dirdifferenceorder.model.out.DirDifferenceOrderOut;
import com.edc.erp.directly.dirdifferenceorder.model.out.ExcelDirDifferenceOrder;
import com.edc.erp.directly.dirdifferenceorder.model.out.OrdDirDelivDifferenceDetailOut;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceDetailService;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceService;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsExpiryIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowGoodsIn;
import com.edc.erp.reducestock.model.in.stock.StockFlowIn;
import com.edc.erp.reducestock.stock.service.StockFlowService;
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
import com.edc.plugins.utils.bean.ModelConst;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 差异单(OrdDirDelivDifference)表服务实现类
 *
 * @author weichao
 * @since 2022-11-14 11:32:36
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdDirDelivDifferenceServiceImpl extends BaseServiceImpl<OrdDirDelivDifference> implements OrdDirDelivDifferenceService {

    private final OrdDirDelivDifferenceMapper ordDirDelivDifferenceMapper;

    private final StoreCenterService storeCenterService;

    private final StockServer stockServer;

    private final OrdDirDelivDifferenceDetailService ordDirDelivDifferenceDetailService;

    private final AsyncLogService asyncLogService;

    private final OrderGoodsServer orderGoodsServer;

    private final UniqueUtils uniqueUtils;

    private final RedisService redisService;

    private final WarehouseServer warehouseServer;

    private final StockFlowService stockFlowService;

    private final AsyncExportExecutor asyncExportExecutor;

    private final StoreChannelHandle storeChannelHandle;

    @Qualifier("dirDifferenceOrderToDtsSender")
    private final MessageSender dirDifferenceOrderToDtsSender;

    /**
     * 分页获取差异单列表
     * @param differenceOrderIn
     * @return
     */
    @Override
    public Page<DirDifferenceOrderOut> findDiffOrderByParam(DirDifferenceOrderIn differenceOrderIn) {
        this.handleChannelInfo(differenceOrderIn);
        if (StringUtils.isNotBlank(differenceOrderIn.getStoreArea())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(differenceOrderIn.getStoreArea(), differenceOrderIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return new Page(differenceOrderIn);
            }
            differenceOrderIn.setStoreCodeList(storeCodeList);
        }
        List<DirDifferenceOrderOut> dirDifferenceOrderOuts = ordDirDelivDifferenceMapper.findDifferenceOrdersByPage(differenceOrderIn);
        dirDifferenceOrderOuts.forEach(item -> {
//         List<OrdDirDelivDifferenceDetailOut> dirDelivDifferenceDetails= ordDirDelivDifferenceDetailService.findDifferenceOrderDtlById(item.getId());
//            item.setDifferenceOrderDetailList(dirDelivDifferenceDetails);
            StockInfoOut stockOut = stockServer.getTransInfo(item.getStockCode());
            item.setStockName(Objects.nonNull(stockOut) ? stockOut.getStockName() : "");
            item.setWrhName(Objects.nonNull(stockOut) ? stockOut.getWarehouseName() : "");
            item.setDifferenceStatusName(DifferenceOrderStatusEnum.getNameByCode(item.getDifferenceStatus()));
            // 差异类型中文
            item.setDifferenceTypeName(DifferenceOrderTypeEnum.getNameByCode(item.getDifferenceType()));
            //品项数
            Integer size = ordDirDelivDifferenceDetailService.getGoodsSize(item.getId());
            item.setGoodsSize(size);
        });
        Page<DirDifferenceOrderOut> reqPage = new Page(differenceOrderIn);
        reqPage.setList(dirDifferenceOrderOuts);
        return reqPage;
    }

    private void handleChannelInfo(DirDifferenceOrderIn differenceOrderIn) {
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(differenceOrderIn.getBizOrgCode());
        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !differenceOrderIn.getBizOrgCode().equals(stockInfoOut.getBizOrgCode())).findFirst();
        if (!anyOneOptional.isPresent()) {
            differenceOrderIn.setBizOrgCode("");
        }
        List<String> stockCodeList;
        if (StringUtils.isBlank(differenceOrderIn.getStockCode())) {
            stockCodeList = authOrgStockMap.keySet().stream().collect(Collectors.toList());
            differenceOrderIn.setStockCodeList(stockCodeList);
        } else {
            stockCodeList = Collections.singletonList(differenceOrderIn.getStockCode());
            // 校验业务仓位是否允许操作
            storeChannelHandle.checkStockCodeListISAuth(stockCodeList, authOrgStockMap);
            differenceOrderIn.setStockCode(null);
            differenceOrderIn.setStockCodeList(stockCodeList);
        }
    }

    /**
     * 获取差异单表头信息
     * @param differenceOrderId
     * @return
     */
    @Override
    public DirDifferenceOrderOut getBackHeaderDifferenceOrderOutById(Integer differenceOrderId) {
        OrdDirDelivDifference query = new OrdDirDelivDifference();
        query.setId(differenceOrderId);
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        OrdDirDelivDifference ordDirDelivDifference = ordDirDelivDifferenceMapper.selectOne(query);
        if (Objects.isNull(ordDirDelivDifference)) {
            throw new BusinessException("配货差异单不存在");
        }
        DirDifferenceOrderOut dirDifferenceOrderOut = new DirDifferenceOrderOut();
        BeanUtils.copy(ordDirDelivDifference, dirDifferenceOrderOut);
        // 直营配货差异类型中文
        dirDifferenceOrderOut.setDifferenceTypeName(DifferenceOrderTypeEnum.getNameByCode(dirDifferenceOrderOut.getDifferenceType()));
        dirDifferenceOrderOut.setDifferenceStatusName(DifferenceOrderStatusEnum.getNameByCode(dirDifferenceOrderOut.getDifferenceStatus()));
        StockInfoOut stockOut = stockServer.getTransInfo(dirDifferenceOrderOut.getStockCode());
        dirDifferenceOrderOut.setStockName(Objects.nonNull(stockOut) ? stockOut.getStockName() : "");
        dirDifferenceOrderOut.setWrhName(Objects.nonNull(stockOut) ? stockOut.getWarehouseName() : "");
        return dirDifferenceOrderOut;
    }

    /**
     * 导出差异单列表
     * @param differenceOrderIn
     * @return
     */
    @Override
    public String exportDirDifferences(DirDifferenceOrderIn differenceOrderIn) {
        // 设置每次查询条数
        differenceOrderIn.setPageSize(10000);
        String title = "配货差异单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配货差异单列表",
                        // 导出模板实体
                        ExcelDirDifferenceOrder.class,
                        // 分页查询对象
                        differenceOrderIn,
                        // 分页查询方法
                        page -> {
                            Page<DirDifferenceOrderOut> outPage = this.findDiffOrderByParam(differenceOrderIn);
                            List<ExcelDirDifferenceOrder> excelDirDifferenceOrders = parseDataToExcel(outPage.getList());
                            log.info("导出配货差异单列表集合大小是--{}", excelDirDifferenceOrders.size());
                            return excelDirDifferenceOrders;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    /**
     * 保存差异单
     *
     * @param saveDifferenceIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response saveDirDifference(SaveDifferenceIn saveDifferenceIn) {
//        //校验 库存
//        Response<String> response = this.verifyDisDifference(saveDifferenceIn);
//        if (!response.isSuccess()) {
//            throw new BusinessException(response.getMessage());
//        }
        OrdDirDelivDifference ordDirDelivDifference = new OrdDirDelivDifference();
        ordDirDelivDifference.setDeliveryOrderNo(saveDifferenceIn.getDeliveryOrderNo());
        ordDirDelivDifference.setDifferenceType(saveDifferenceIn.getDifferenceType());
        int count = ordDirDelivDifferenceMapper.selectCount(ordDirDelivDifference);
        if (count > 0) {
            return Response.success();
        }
        ordDirDelivDifference = this.initDisDifference(saveDifferenceIn);
        //保存差异单
        List<OrdDirDelivDifferenceDetail> details = null;
        int insert = ordDirDelivDifferenceMapper.insertSelective(ordDirDelivDifference);
        if (insert > NumberUtil.INTEGER_ZERO) {
            //保存明细
            details = this.saveDirDifferenceDetail(ordDirDelivDifference, saveDifferenceIn.getDifferenceDetails());
        }
        StockInfoOut stockInfoOut = stockServer.getByCodeAndAuth(ordDirDelivDifference.getStockCode(), ordDirDelivDifference.getBizOrgCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("无此仓位信息");
        }
        //如果正差异差异，仓储库存占用
        if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(saveDifferenceIn.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(saveDifferenceIn.getDifferenceType())) {
            //异步调仓储库存
            StockFlowIn stockFlowIn = this.initWarehouseStock(ordDirDelivDifference, details, stockInfoOut, ordDirDelivDifference.getUpdateTime());
            Response stockRes = stockFlowService.checkStockFlow(Collections.singletonList(stockFlowIn));
            if (!stockRes.isSuccess()) {
                throw new BusinessException(stockRes.getMessage());
            }
        }
        //负差异
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(saveDifferenceIn.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(saveDifferenceIn.getDifferenceType())) {
            //异步调门店库存
            StockFlowIn stockFlowIn = this.initStoreStock(ordDirDelivDifference, details, stockInfoOut, ordDirDelivDifference.getUpdateTime());
            Response storeRes = stockFlowService.checkStockFlow(Collections.singletonList(stockFlowIn));
            if (!storeRes.isSuccess()) {
                throw new BusinessException(storeRes.getMessage());
            }
        }
        // 下发差异单到Dts
        if (stockServer.isSendWms(ordDirDelivDifference.getStockCode(), stockInfoOut.getBizOrgCode())) {
            this.initDifferenceOrderToDts(ordDirDelivDifference, details, stockInfoOut.getBizOrgCode());
        }
        //保存日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_SAVE.name(),
                String.valueOf(ordDirDelivDifference.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DIFFERENCE.getCode(),
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_SAVE.getName(), new Date(), ordDirDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success();
    }

    /**
     * 异步调门店库存
     *
     * @param ordDirDelivDifference
     * @param details
     * @return
     */
    private StockFlowIn initStoreStock(OrdDirDelivDifference ordDirDelivDifference, List<OrdDirDelivDifferenceDetail> details, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(ordDirDelivDifference.getBizOrgCode());
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getName());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getName());
        }
        stockFlowIn.setFlowDate(flowDate);
        stockFlowIn.setCreator(ordDirDelivDifference.getCreator());
        stockFlowIn.setOperationType(ordDirDelivDifference.getDifferenceStatus());
        stockFlowIn.setOrgCode(ordDirDelivDifference.getOrgCode());
        stockFlowIn.setSourceNo(ordDirDelivDifference.getDifferenceNo());
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
            BigDecimal actualQty;
            //负差异
            if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDirDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDirDelivDifference.getDifferenceType())) {
                if (DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
                    //申请增/减
                    applyLowering = AdjustTypeEnum.ADD.getCode();
                    //申请数
                    applyQty = item.getApplyDifferenceQuantity().abs();
                }
                if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
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
                    //实际数
                    stockFlowGoodsIn.setActualQty(actualQty);
//                    stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(item.getExpiry(), stockFlowGoodsIn.getActualQty())));
                }
                //正差异
            } else {
                if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
                    //实际增/减
                    actualLowering = AdjustTypeEnum.ADD.getCode();
                    //实际数
                    actualQty = item.getApprovalDifferenceQuantity().abs();
                    //正差异批准，门店库存调整取原单门店配货价
                    stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
                    //实际数
                    stockFlowGoodsIn.setActualQty(actualQty);
//                    stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(item.getExpiry(), stockFlowGoodsIn.getActualQty())));
                }
            }
            //实际增/减
            stockFlowGoodsIn.setActualLowering(actualLowering);
            //申请增/减
            stockFlowGoodsIn.setApplyLowering(applyLowering);
            //申请数
            stockFlowGoodsIn.setApplyQty(applyQty);
            // 门店库存初始化公共部分
            this.initStoreWrh(ordDirDelivDifference, stockInfoOut, stockFlowGoodsIns, item, tar, stockFlowGoodsIn, false);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    /**
     * 门店库存初始化公共部分
     *
     * @param ordDirDelivDifference
     * @param stockInfoOut
     * @param stockFlowGoodsIns
     * @param item
     * @param tar
     * @param stockFlowGoodsIn
     */
    private void initStoreWrh(OrdDirDelivDifference ordDirDelivDifference, StockInfoOut stockInfoOut, List<StockFlowGoodsIn> stockFlowGoodsIns,
                              OrdDirDelivDifferenceDetail item, BigDecimal tar, StockFlowGoodsIn stockFlowGoodsIn, Boolean isCharge) {
        stockFlowGoodsIn.setStockCode(ordDirDelivDifference.getStockCode());
        stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
        stockFlowGoodsIn.setStoreCode(ordDirDelivDifference.getStoreCode());
        stockFlowGoodsIn.setStoreName(ordDirDelivDifference.getStoreName());
        stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
        stockFlowGoodsIn.setWarehouseCode(ordDirDelivDifference.getWrhCode());
        //库存发生位置
        stockFlowGoodsIn.setPosition(StockHappenLieEnum.STORE.getCode());
        // 单号
        stockFlowGoodsIn.setSourceNo(ordDirDelivDifference.getDifferenceNo());
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
        if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(item.getApprovalDifferenceExceptTaxAmount());
            //含税金额
//            stockFlowGoodsIn.setTaxAmount(Objects.isNull(item.getApprovalDifferenceExceptTaxAmount()) ? BigDecimal.ZERO : item.getApprovalDifferenceExceptTaxAmount().multiply(tar).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
//            stockFlowGoodsIn.setTaxAmount(item.getDifferenceAmount());

            BigDecimal differenceAmount = item.getDifferenceAmount().abs();
            BigDecimal taxAmount = AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering())
                    ? differenceAmount.negate() : differenceAmount;
            stockFlowGoodsIn.setTaxAmount(taxAmount);
            BigDecimal costTaxAmount;
            if (AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering())) {
                BigDecimal storeStockPrice = warehouseServer.getStockPrice(ordDirDelivDifference.getStoreCode(), item.getGoodsCode(), ordDirDelivDifference.getBizOrgCode());
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
     * 初始化仓储调整入参
     *
     * @param ordDirDelivDifference
     * @param details
     * @return
     */
    private StockFlowIn initWarehouseStock(OrdDirDelivDifference ordDirDelivDifference, List<OrdDirDelivDifferenceDetail> details, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(ordDirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(ordDirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getName());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getName());
        }
        stockFlowIn.setFlowDate(flowDate);
        stockFlowIn.setCreator(ordDirDelivDifference.getCreator());
        stockFlowIn.setOperationType(ordDirDelivDifference.getDifferenceStatus());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        stockFlowIn.setSourceNo(ordDirDelivDifference.getDifferenceNo());
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
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(ordDirDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(ordDirDelivDifference.getDifferenceType())) {
                if (DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
                    //申请增/减
                    applyLowering = AdjustTypeEnum.ADD.getCode();
                    //申请数
                    applyQty = item.getApplyDifferenceQuantity().abs();
                }
                if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
                    // 实际增/减
                    actualLowering = AdjustTypeEnum.REDUCE.getCode();
                    // 实际数
                    actualQty = item.getApprovalDifferenceQuantity().abs();
                    //申请增/减
                    applyLowering = AdjustTypeEnum.REDUCE.getCode();
                    //申请数
                    applyQty = item.getApplyDifferenceQuantity().abs();
                }
            } else {
                if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
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
            stockFlowGoodsIn.setStockCode(ordDirDelivDifference.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            //单号
            stockFlowGoodsIn.setSourceNo(ordDirDelivDifference.getDifferenceNo());
            //库存发生位置
            this.initWrhStock(stockFlowGoodsIns, item, tar, stockFlowGoodsIn, ordDirDelivDifference, false);
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
    private void initWrhStock(List<StockFlowGoodsIn> stockFlowGoodsIns, OrdDirDelivDifferenceDetail item, BigDecimal tar,
                              StockFlowGoodsIn stockFlowGoodsIn, OrdDirDelivDifference ordDirDelivDifference, Boolean isCharge) {
        stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
        // todo 正差异最新大仓库存价 负差异原配货单大仓库存价
        stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
        //批准
        if (DifferenceOrderStatusEnum.APPROVED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
            //发生价为仓储库存价
            stockFlowGoodsIn.setPrice(item.getWrhPrice());
            //批准金额
            BigDecimal differenceAmount = item.getDifferenceAmount().abs();
//            BigDecimal applyDifferenceAmount = BigDecimal.ZERO;
            BigDecimal taxAmount = AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering())
                    ? differenceAmount.negate() : differenceAmount;
            BigDecimal costTaxAmount = AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering())
                    ? item.getWrhCostAmount() : isCharge ? item.getWrhCostAmount().abs() : taxAmount;
            stockFlowGoodsIn.setCostTaxAmount(costTaxAmount);
            //实际减，为负值(正差异批准释放业务占用库存)
//            if (AdjustTypeEnum.REDUCE.getCode().equals(stockFlowGoodsIn.getActualLowering()) && NumberUtil.INTEGER_ZERO.equals(ordDirDelivDifference.getIsReversalOrder())) {
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
        if (DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
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
            //税额
            stockFlowGoodsIn.setTax(applyDifferenceTaxAmount);
        }

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
     * 初始化下发dts入参
     *
     * @param ordDirDelivDifference
     * @param details
     */
    private void initDifferenceOrderToDts(OrdDirDelivDifference ordDirDelivDifference, List<OrdDirDelivDifferenceDetail> details, String centerStockBizOrgCode) {
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDirDelivDifference.getStoreCode());
        DifferenceBillIn differenceBillIn = new DifferenceBillIn();
        differenceBillIn.setPlatform_bill_id(ordDirDelivDifference.getDifferenceNo());
        differenceBillIn.setBill_create_date(ordDirDelivDifference.getCreateTime());
        differenceBillIn.setLast_update_time(ordDirDelivDifference.getCreateTime());
        differenceBillIn.setRelation_bill_id(ordDirDelivDifference.getDeliveryOrderNo());
        //物流配单单号
        differenceBillIn.setLogistics_id(null);
        //类型
        differenceBillIn.setBill_type_id(null);
        // 仓储代码
        differenceBillIn.setWarehouse_id(ordDirDelivDifference.getWrhCode());
        //仓位代码
        differenceBillIn.setSource_stock_id(ordDirDelivDifference.getStockCode());
        // 门店Id
        differenceBillIn.setShop_id(Objects.isNull(storeOut) ? "" : storeOut.getStoreId().toString());
        //门店代码
        differenceBillIn.setShop_code(ordDirDelivDifference.getStoreCode());
        //填单人
        differenceBillIn.setCreater(ordDirDelivDifference.getCreator());
        //最后修改人
        differenceBillIn.setModifier(ordDirDelivDifference.getCreator());
        differenceBillIn.setMemo(ordDirDelivDifference.getRemark());
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
        List<DifferenceBillDtlIn> dtlIns = initDifferenceBillDt(ordDirDelivDifference, details);
        //明细
        differenceBillIn.setDetail_list(dtlIns);
        // 发dts
//       syncOrdDisOrderHandle.syncDifferenceOrderToDts(differenceBillIn);
        //  2022/11/2  下发dts
//        asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_DIFFERENCE_ORDER_TO_DTS, JSONObject.toJSONString(differenceBillIn), ordDirDelivDifference.getBizOrgCode(), ordDirDelivDifference.getDifferenceNo());
        SendResponse sendResponse = dirDifferenceOrderToDtsSender.sendSync(JSONObject.toJSONString(differenceBillIn).getBytes(), System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_FAST_TIME);
        log.info("DTS配销差异单数据下发DTS{}消息ID---{}", ordDirDelivDifference.getDifferenceNo(), sendResponse.getMessageId());
    }

    /**
     * 初始化发dts差异明细
     *
     * @param ordDirDelivDifference
     * @param details
     * @return
     */
    private List<DifferenceBillDtlIn> initDifferenceBillDt(OrdDirDelivDifference ordDirDelivDifference, List<OrdDirDelivDifferenceDetail> details) {
        List<DifferenceBillDtlIn> dtlIns = new ArrayList<>();
        details.forEach(item -> {
            OrgGoodsTransInfo goodsOut = orderGoodsServer.getGoodsOut(item.getGoodsCode(), ordDirDelivDifference.getBizOrgCode());
            DifferenceBillDtlIn billDtlIn = new DifferenceBillDtlIn();
            billDtlIn.setPlatform_bill_id(ordDirDelivDifference.getDifferenceNo());
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
            billDtlIn.setSource_organization(ordDirDelivDifference.getBizOrgCode());
            //目标组织
            billDtlIn.setTarget_organization(ordDirDelivDifference.getBizOrgCode());
            dtlIns.add(billDtlIn);
        });
        return dtlIns;
    }

    /**
     * 保存差异单明细
     *
     * @param ordDirDelivDifference
     * @param differenceDetails
     * @return
     */
    private List<OrdDirDelivDifferenceDetail> saveDirDifferenceDetail(OrdDirDelivDifference ordDirDelivDifference, List<OrdDirDelivDifferenceDetail> differenceDetails) {
        List<OrdDirDelivDifferenceDetail> details = differenceDetails.stream().map(item -> initDifferenceOrderDetail(ordDirDelivDifference, item)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(details)) {
            ordDirDelivDifferenceDetailService.batchSave(details);
        }
        return details;
    }

    /**
     * 初始化差异单明细
     *
     * @param ordDirDelivDifference
     * @param item
     * @return
     */
    private OrdDirDelivDifferenceDetail initDifferenceOrderDetail(OrdDirDelivDifference ordDirDelivDifference, OrdDirDelivDifferenceDetail item) {
        OrdDirDelivDifferenceDetail detail = new OrdDirDelivDifferenceDetail();
        BeanUtils.copy(item, detail);
        detail.setCreateTime(ordDirDelivDifference.getCreateTime());
        detail.setCreator(ordDirDelivDifference.getCreator());
        detail.setUpdateTime(ordDirDelivDifference.getUpdateTime());
        detail.setUpdater(ordDirDelivDifference.getUpdater());
        detail.setDifferenceOrderId(ordDirDelivDifference.getId());
        detail.setIsDelete(NumberUtil.INTEGER_ZERO);
        return detail;
    }


    /**
     * 校验仓储库存
     *
     * @param bizOrgCode
     * @param goodsCode
     * @param stockCode
     * @return
     */
    @Override
    public BigDecimal checkStockInv(String bizOrgCode, String goodsCode, String stockCode) {
        return ordDirDelivDifferenceMapper.checkStockInv(bizOrgCode, goodsCode, stockCode);
    }

    /**
     * 校验门店库存
     *
     * @param bizOrgCode
     * @param goodsCode
     * @param storeCode
     * @return
     */
    @Override
    public BigDecimal checkStoreInv(String bizOrgCode, String goodsCode, String storeCode) {
        return ordDirDelivDifferenceMapper.checkStoreInv(bizOrgCode, goodsCode, storeCode);
    }

    /**
     * 批准配货差异单
     *
     * @param approvedDirDifferenceOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approvedDisDifference(ApprovedDirDifferenceOrderIn approvedDirDifferenceOrderIn) {
        OrdDirDelivDifference dirDelivDifference = ordDirDelivDifferenceMapper.selectByPrimaryKey(approvedDirDifferenceOrderIn.getDiffOrderId());
        if (Objects.isNull(dirDelivDifference)) {
            throw new BusinessException("直营配货差异单不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(dirDelivDifference.getStockCode(), approvedDirDifferenceOrderIn.getBizOrgCode(), "批准直营配货差异单");
        if (!DifferenceOrderStatusEnum.AUDITED.getCode().equals(dirDelivDifference.getDifferenceStatus())) {
            throw new BusinessException("已审核的直营配货差异单才可批准");
        }
//        if (stockServer.isSendWms(dirDelivDifference.getStockCode(), stockInfoOut.getBizOrgCode())) {
//            throw new BusinessException("不下发物流的单据才可批准");
//        }
        //修改差异单状态
        dirDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.APPROVED.getCode());
        // 批准差异总数量
        BigDecimal totalApprovalDifferenceQuantity = BigDecimal.ZERO;
        // 批准差异总金额
        BigDecimal totalApprovalDifferenceAmount = BigDecimal.ZERO;

        //获取差异类型
        String differenceType = dirDelivDifference.getDifferenceType();
        List<OrdDirDelivDifferenceDetail> differenceDetails = approvedDirDifferenceOrderIn.getDifferenceDetails();
        for (OrdDirDelivDifferenceDetail differenceDetail : differenceDetails) {
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
            //正差异，取实时仓储库存价,正差异为出库。总部财务库存减少，为负值
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(differenceType)
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(differenceType)) {
                //最新仓储库存价
                BigDecimal warehousePrice = warehouseServer.getWarehousePrice(dirDelivDifference.getWrhCode(), dirDelivDifference.getStockCode(),
                        differenceDetail.getGoodsCode(), stockInfoOut.getBizOrgCode(), differenceDetail.getVendorCode());
                warehousePrice = Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice;
                differenceDetail.setWrhPrice(warehousePrice);
                differenceDetail.setWrhCostAmount(warehousePrice.multiply(differenceDetail.getApprovalDifferenceQuantity()).negate());
                /** 门店成本金额，正差异，门店财务库存增加，为正值 */
                differenceDetail.setStoreCostAmount(differenceDetail.getDistributionUnitPrice().multiply(differenceDetail.getApprovalDifferenceQuantity()));
            } else {
                //负差异，取原单仓储库存价，大库加库存，仓储成本金额要为正值，因为负差异原单批准差异数量为负值，此处相乘取反后为正值。
                differenceDetail.setWrhCostAmount(differenceDetail.getDifferenceAmount().abs());
                /** 门店成本金额，负差异，门店财务库存减少，需要为负值, 因为负差异原单批准差异数量为负值，此处直接相乘，无需取反。*/
//                BigDecimal stockPrice = warehouseServer.getStockPrice(dirDelivDifference.getStoreCode(), differenceDetail.getGoodsCode(), dirDelivDifference.getBizOrgCode());
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
            ordDirDelivDifferenceDetailService.updateDirDifferenceDetail(differenceDetail);
            totalApprovalDifferenceAmount = totalApprovalDifferenceAmount.add(differenceDetail.getDifferenceAmount());
            totalApprovalDifferenceQuantity = totalApprovalDifferenceQuantity.add(differenceDetail.getApprovalDifferenceQuantity());
        }
        dirDelivDifference.setTotalApprovalDifferenceAmount(totalApprovalDifferenceAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        dirDelivDifference.setTotalApprovalDifferenceQuantity(totalApprovalDifferenceQuantity);
        dirDelivDifference.setApprovalTime(LocalDateTime.now());
        // 修改差异单
        int count = this.updateDirDifference(dirDelivDifference, OrdLogTypeEnum.ORD_DIR_ORDER_DIFFERENCE.getCode(), OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_APPROVED.getName(),
                DifferenceOrderStatusEnum.APPROVED.getName());
        // 释放库存
        this.releaseStock(dirDelivDifference, differenceDetails, stockInfoOut, dirDelivDifference.getApprovalTime());
        return count;

    }

    /**
     * 修改配货差异单
     *
     * @param ordDirDelivDifference
     * @param typeCode
     * @param typeName
     * @param name
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDirDifference(OrdDirDelivDifference ordDirDelivDifference, String typeCode, String typeName, String name) {
        int count = ordDirDelivDifferenceMapper.updateByPrimaryKeySelective(ordDirDelivDifference);
        // 添加日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                typeName,
                String.valueOf(ordDirDelivDifference.getId()),
                typeCode,
                name, new Date(), ordDirDelivDifference.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

    /**
     * 作废配货差异单
     *
     * @param delivDifference
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidDirDifference(OrdDirDelivDifference delivDifference, StockInfoOut stockInfoOut) {
        if (!DifferenceOrderStatusEnum.AUDITED.getCode().equals(delivDifference.getDifferenceStatus())) {
            throw new BusinessException("已审核的差异单才可作废");
        }
        List<OrdDirDelivDifferenceDetailOut> detailOuts = ordDirDelivDifferenceDetailService.findDifferenceOrderDtlById(delivDifference.getId());
        delivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.INVALID.getCode());
//        delivDifference.setIsReversal(NumberUtils.INTEGER_ONE);
        int updateCount = this.updateDirDifference(delivDifference, OrdLogTypeEnum.ORD_DIR_ORDER_DIFFERENCE.getCode(),
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_INVALID.getName(), DifferenceOrderStatusEnum.INVALID.getName());
        if (updateCount < 1) {
            return updateCount;
        }
        //仓储库存释放
        List<StockFlowIn> stockFlowIns = this.initStockInvalid(delivDifference, detailOuts, stockInfoOut);
        Response response = stockFlowService.checkStockFlow(stockFlowIns);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
        return updateCount;
    }

    /**
     * 差异单作废初始化参数
     * @param dirDelivDifference
     * @param detailOuts
     * @return
     */
    private List<StockFlowIn> initStockInvalid(OrdDirDelivDifference dirDelivDifference, List<OrdDirDelivDifferenceDetailOut> detailOuts, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setOperationType(OrderTypeEnum.INVALID.getCode());
        // 库存发生位置
        String occurrenceLocation = null;
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(dirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getName());
            occurrenceLocation = StockHappenLieEnum.STORE.getCode();
            stockFlowIn.setBizOrgCode(dirDelivDifference.getBizOrgCode());
            stockFlowIn.setOrgCode(dirDelivDifference.getOrgCode());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(dirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getName());
            occurrenceLocation = StockHappenLieEnum.WAREHOUSE.getCode();
            stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
            stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        }
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(dirDelivDifference.getCreator());
        stockFlowIn.setSourceNo(dirDelivDifference.getDifferenceNo());
        stockFlowIn.setOccurrenceLocation(occurrenceLocation);
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        for (OrdDirDelivDifferenceDetailOut item : detailOuts) {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
            stockFlowGoodsIn.setApplyQty(item.getApplyDifferenceQuantity().abs());
            // 仓储
            stockFlowGoodsIn.setStockCode(dirDelivDifference.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(dirDelivDifference.getWrhCode());
            stockFlowGoodsIn.setStoreCode(dirDelivDifference.getStoreCode());
            stockFlowGoodsIn.setStoreName(dirDelivDifference.getStoreName());
            // 单号
            stockFlowGoodsIn.setSourceNo(dirDelivDifference.getDifferenceNo());
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
     * 冲销配货差异单
     *
     * @param approvedDirDifferenceOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int chargeDirDifference(ApprovedDirDifferenceOrderIn approvedDirDifferenceOrderIn) {
        OrdDirDelivDifference dirDelivDifference = ordDirDelivDifferenceMapper.selectByPrimaryKey(approvedDirDifferenceOrderIn.getDiffOrderId());
        if (Objects.isNull(dirDelivDifference)) {
            throw new BusinessException("差异单不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(dirDelivDifference.getStockCode(), approvedDirDifferenceOrderIn.getBizOrgCode(), "冲销配货差异单");
        if (NumberUtil.INTEGER_ONE.equals(dirDelivDifference.getIsReversal())) {
            throw new BusinessException("该单据已被红冲");
        }
        if (NumberUtil.INTEGER_ONE.equals(dirDelivDifference.getIsReversalOrder())) {
            throw new BusinessException("红冲单不能被红冲");
        }
        if (!DifferenceOrderStatusEnum.APPROVED.getCode().equals(dirDelivDifference.getDifferenceStatus())) {
            throw new BusinessException("已批准的差异单才可冲销");
        }
//        //校验库存
//        String message = checkInv(dirDelivDifference, approvedDirDifferenceOrderIn.getDifferenceDetails());
//        if (StringUtil.isNotEmpty(message)) {
//            throw new BusinessException(message);
//        }

        OrdDirDelivDifference diffReversalOrder = new OrdDirDelivDifference();
        BeanUtils.copy(dirDelivDifference, diffReversalOrder);
        diffReversalOrder.setTotalApplyDifferenceQuantity(Objects.isNull(dirDelivDifference.getTotalApplyDifferenceQuantity()) ? BigDecimal.ZERO : dirDelivDifference.getTotalApplyDifferenceQuantity().negate());
        diffReversalOrder.setTotalApplyDifferenceAmount(Objects.isNull(dirDelivDifference.getTotalApplyDifferenceAmount()) ? BigDecimal.ZERO : dirDelivDifference.getTotalApplyDifferenceAmount().negate());
        diffReversalOrder.setTotalApprovalDifferenceQuantity(Objects.isNull(dirDelivDifference.getTotalApprovalDifferenceQuantity()) ? BigDecimal.ZERO : dirDelivDifference.getTotalApprovalDifferenceQuantity().negate());
        diffReversalOrder.setTotalApprovalDifferenceAmount(Objects.isNull(dirDelivDifference.getTotalApprovalDifferenceAmount()) ? BigDecimal.ZERO : dirDelivDifference.getTotalApprovalDifferenceAmount().negate());

        diffReversalOrder.setId(null);
        diffReversalOrder.setIsReversal(NumberUtil.INTEGER_ZERO);
        diffReversalOrder.setIsReversalOrder(NumberUtil.INTEGER_ONE);
        diffReversalOrder.setCreateTime(LocalDateTime.now());
        diffReversalOrder.setUpdateTime(LocalDateTime.now());
        diffReversalOrder.setApprovalTime(LocalDateTime.now());
        //原单号
        diffReversalOrder.setSourceNo(dirDelivDifference.getDifferenceNo());
        // 差异红冲单号
        diffReversalOrder.setDifferenceNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KPC.getCode(), dirDelivDifference.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR));
        //新差异增红冲单
        int count = this.insertSelective(diffReversalOrder);

        // 添加红冲差异单日志
        BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE_SAVE.getName(),
                String.valueOf(diffReversalOrder.getId()),
                OrdLogTypeEnum.ORD_DIR_ORDER_DIFFERENCE.getCode(),
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE_SAVE.getName(),
                new Date(), diffReversalOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        //修改原差异单
        dirDelivDifference.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        dirDelivDifference.setIsReversal(NumberUtil.INTEGER_ONE);
        updateDirDifference(dirDelivDifference, OrdLogTypeEnum.ORD_DIR_ORDER_DIFFERENCE.getCode(), OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE.getName(),
                OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_CHARGE.getName());
        List<OrdDirDelivDifferenceDetail> list = new ArrayList<>();
        //红冲单明细
        this.charge(approvedDirDifferenceOrderIn, diffReversalOrder, list);
        ordDirDelivDifferenceDetailService.batchSave(list);

        // 差异单冲销时调整库存
        this.adjustInv(diffReversalOrder, list, stockInfoOut);
        return count;
    }

    /**
     * 初始化差异单明细信息
     *
     * @param ordDirDelivDifference
     * @param detail
     * @return
     */
    @Override
    public List<OrdDirDelivDifferenceDetail> initDifferenceBillVO(OrdDirDelivDifference ordDirDelivDifference, List<DifferenceBillDtlVO> detail, String centerStockBizOrgCode) {
        OrdDirDelivDifferenceDetailIn ordDisDelivDifferenceDetailIn = new OrdDirDelivDifferenceDetailIn();
        String differenceType = ordDirDelivDifference.getDifferenceType();
        ordDisDelivDifferenceDetailIn.setDiffOrderId(ordDirDelivDifference.getId());
        Page<OrdDirDelivDifferenceDetailOut> pageOut = ordDirDelivDifferenceDetailService.findDifferenceOrderDtlListByParameter(ordDisDelivDifferenceDetailIn);
        if (CollectionUtils.isEmpty(pageOut.getList())) {
            return null;
        }
        Map<String, OrdDirDelivDifferenceDetailOut> detailMap = pageOut.getList().stream().collect(Collectors.toMap(ordDirDelivDifferenceDetailOut -> ordDirDelivDifferenceDetailOut.getGoodsCode()
                + SystemConstant.SHORT_LINE + ordDirDelivDifferenceDetailOut.getLine(), Function.identity()));
        // 批准差异总数量
        BigDecimal totalApprovalDifferenceQuantity = BigDecimal.ZERO;
        // 批准差异总金额
        BigDecimal totalApprovalDifferenceAmount = BigDecimal.ZERO;
        List<OrdDirDelivDifferenceDetail> details = new ArrayList<>();
        for (DifferenceBillDtlVO item : detail) {
            OrdDirDelivDifferenceDetail differenceDetail = new OrdDirDelivDifferenceDetail();
            OrdDirDelivDifferenceDetailOut ordDirDelivDifferenceDetailOut = detailMap.get(item.getFarticlecode() + SystemConstant.SHORT_LINE + item.getLine());
            differenceDetail.setDifferenceOrderId(ordDirDelivDifference.getId());
            BeanUtils.copy(ordDirDelivDifferenceDetailOut, differenceDetail);
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
                BigDecimal warehousePrice = warehouseServer.getWarehousePrice(ordDirDelivDifference.getWrhCode(), ordDirDelivDifference.getStockCode(),
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
        ordDirDelivDifference.setTotalApprovalDifferenceAmount(totalApprovalDifferenceAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDirDelivDifference.setTotalApprovalDifferenceQuantity(totalApprovalDifferenceQuantity);
        return details;
    }

    /**
     * 解析数据
     *
     * @param list
     * @return
     */
    private List<ExcelDirDifferenceOrder> parseDataToExcel(List<DirDifferenceOrderOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 转化excel
     *
     * @param dirDifferenceOrderOut
     * @param index
     * @return
     */
    private ExcelDirDifferenceOrder convertExcel(DirDifferenceOrderOut dirDifferenceOrderOut, int index) {
        ExcelDirDifferenceOrder excelDirDifferenceOrder = new ExcelDirDifferenceOrder();
        BeanUtils.copy(dirDifferenceOrderOut, excelDirDifferenceOrder);
        if (Objects.nonNull(dirDifferenceOrderOut)) {
            excelDirDifferenceOrder.setIsRedRush(NumberUtil.INTEGER_ZERO.equals(dirDifferenceOrderOut.getIsReversalOrder()) ? "否" : "是");
            excelDirDifferenceOrder.setIsReversalOrder(NumberUtil.INTEGER_ZERO.equals(dirDifferenceOrderOut.getIsReversalOrder()) ? "否" : "是");
            excelDirDifferenceOrder.setDifferenceStatusName(DifferenceOrderStatusEnum.getNameByCode(dirDifferenceOrderOut.getDifferenceStatus()));
            // 仓位名称
            StockTransInfoOut stockOut = orderGoodsServer.getTransInfo(dirDifferenceOrderOut.getStockCode(), dirDifferenceOrderOut.getBizOrgCode());
            excelDirDifferenceOrder.setStockName(Objects.nonNull(stockOut) ? stockOut.getStockName() + "【" + stockOut.getStockCode() + "】" : "");
            //差异单类型
            excelDirDifferenceOrder.setDifferenceTypeName(DifferenceOrderTypeEnum.getNameByCode(dirDifferenceOrderOut.getDifferenceType()));
        }
        excelDirDifferenceOrder.setIndex(index + 1);
        return excelDirDifferenceOrder;
    }

    /**
     * 明细红冲
     *
     * @param approvedDirDifferenceOrderIn
     * @param query
     * @param list
     */
    private void charge(ApprovedDirDifferenceOrderIn approvedDirDifferenceOrderIn, OrdDirDelivDifference query, List<OrdDirDelivDifferenceDetail> list) {
        for (OrdDirDelivDifferenceDetail item : approvedDirDifferenceOrderIn.getDifferenceDetails()) {
            OrdDirDelivDifferenceDetail ordDisDelivDifferenceDetail = new OrdDirDelivDifferenceDetail();
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
     * 库存调整
     *
     * @param dirDelivDifference
     * @param differenceDetails
     */
    private void adjustInv(OrdDirDelivDifference dirDelivDifference, List<OrdDirDelivDifferenceDetail> differenceDetails, StockInfoOut stockInfoOut) {
        //异步调仓储库存
        StockFlowIn wareFlowIn = this.initWarehouseStockCharge(dirDelivDifference, differenceDetails, stockInfoOut);
        // 门店库存
        StockFlowIn storeFlowIn = this.initStoreStockCharge(dirDelivDifference, differenceDetails, stockInfoOut);
        List<StockFlowIn> stockFlowInList = Arrays.asList(storeFlowIn, wareFlowIn);
        Response response = stockFlowService.checkStockFlow(stockFlowInList);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
    }

    /**
     * 红冲单门店库存调整初始化
     *
     * @param dirDelivDifference
     * @param differenceDetails
     * @return
     */
    private StockFlowIn initStoreStockCharge(OrdDirDelivDifference dirDelivDifference, List<OrdDirDelivDifferenceDetail> differenceDetails, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        stockFlowIn.setBizOrgCode(dirDelivDifference.getBizOrgCode());
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(dirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getName());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(dirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getName());
        }
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(dirDelivDifference.getCreator());
        stockFlowIn.setOrgCode(dirDelivDifference.getOrgCode());
        stockFlowIn.setSourceNo(dirDelivDifference.getDifferenceNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        differenceDetails.forEach(item -> {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            //实际数
            stockFlowGoodsIn.setActualQty(item.getApprovalDifferenceQuantity().abs());
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(dirDelivDifference.getDifferenceType())) {
                //实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            }
            if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(dirDelivDifference.getDifferenceType())) {
                //实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
            }
            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
            //门店库存初始化公共部分
            this.initStoreWrh(dirDelivDifference, stockInfoOut, stockFlowGoodsIns, item, tar, stockFlowGoodsIn, true);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    /**
     * 红冲单仓储库存调整初始化
     *
     * @param dirDelivDifference
     * @param differenceDetails
     * @return
     */
    private StockFlowIn initWarehouseStockCharge(OrdDirDelivDifference dirDelivDifference, List<OrdDirDelivDifferenceDetail> differenceDetails, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        //负差异
        if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(dirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_LOSS.getName());
        }
        if (DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(dirDelivDifference.getDifferenceType())
                || DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())) {
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_POOR_JUST.getName());
        }
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(dirDelivDifference.getCreator());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        stockFlowIn.setSourceNo(dirDelivDifference.getDifferenceNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        differenceDetails.forEach(item -> {
            BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tar = sell.add(BigDecimal.ONE);
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            //负差异冲销，减库存
            if (DifferenceOrderTypeEnum.MINUS_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.MINUS_WORN.getCode().equals(dirDelivDifference.getDifferenceType())) {
                // 实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            }
            //正差异冲销，加库存
            if (DifferenceOrderTypeEnum.POSITIVE_DIFFERENT.getCode().equals(dirDelivDifference.getDifferenceType())
                    || DifferenceOrderTypeEnum.POSITIVE_WORN.getCode().equals(dirDelivDifference.getDifferenceType())) {
                // 实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
            }
            // 仓储
            stockFlowGoodsIn.setStockCode(dirDelivDifference.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(dirDelivDifference.getWrhCode());
            // 实际数
            stockFlowGoodsIn.setActualQty(item.getApprovalDifferenceQuantity().abs());
            // 单号
            stockFlowGoodsIn.setSourceNo(dirDelivDifference.getDifferenceNo());
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(item.getApprovalDifferenceExceptTaxAmount());
            //含税金额
            stockFlowGoodsIn.setTaxAmount(item.getApprovalDifferenceExceptTaxAmount().multiply(tar).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            //税额
            stockFlowGoodsIn.setTax(item.getApprovalDifferenceTaxAmount());
            //仓储库存公共参数初始化
            this.initWrhStock(stockFlowGoodsIns, item, tar, stockFlowGoodsIn, dirDelivDifference, true);

        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    /**
     * 释放库存
     *
     * @param dirDelivDifference
     * @param differenceDetails
     */
    @Override
    public void releaseStock(OrdDirDelivDifference dirDelivDifference, List<OrdDirDelivDifferenceDetail> differenceDetails, StockInfoOut stockInfoOut, LocalDateTime flowDate) {
        //异步调仓储库存
        StockFlowIn warehouseStockFlowIn = this.initWarehouseStock(dirDelivDifference, differenceDetails, stockInfoOut, dirDelivDifference.getApprovalTime());
        //异步调门店库存
        StockFlowIn storeStockFlowIn = this.initStoreStock(dirDelivDifference, differenceDetails, stockInfoOut, flowDate);
        List<StockFlowIn> stockFlowInList = Arrays.asList(warehouseStockFlowIn, storeStockFlowIn);
        Response wareResult = stockFlowService.checkStockFlow(stockFlowInList);
        if (!wareResult.isSuccess()) {
            throw new BusinessException(wareResult.getMessage());
        }
    }

    @Override
    public List<DirDifferenceOrderOut> findDirDifferenceOrders(OrdDirDelivDifference dirDelivDifference) {
        dirDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.AUDITED.getCode());
        List<DirDifferenceOrderOut> dirDifferenceOrders = ordDirDelivDifferenceMapper.findDirDifferenceOrders(dirDelivDifference);
        dirDifferenceOrders.forEach(item -> item.setDifferenceStatusName(DifferenceOrderStatusEnum.getNameByCode(item.getDifferenceStatus())));
        return dirDifferenceOrders;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchApprovedDirDifference(List<Integer> dirDifferenceOrderIds) {
        int approved = NumberUtil.INTEGER_ZERO;
        for (Integer item : dirDifferenceOrderIds) {
            OrdDirDelivDifferenceDetail dtlQuery = new OrdDirDelivDifferenceDetail();
            dtlQuery.setDifferenceOrderId(item);
            dtlQuery.setIsDelete(ModelConst.DELETE.NO);
            List<OrdDirDelivDifferenceDetail> details = ordDirDelivDifferenceDetailService.list(dtlQuery);
            ApprovedDirDifferenceOrderIn approvedDirDifferenceOrderIn = new ApprovedDirDifferenceOrderIn();
            approvedDirDifferenceOrderIn.setDifferenceDetails(details);
            approvedDirDifferenceOrderIn.setDiffOrderId(item);
            approvedDirDifferenceOrderIn.setBizOrgCode(UserUtil.getBizOrgCode());
            try {
                approved += this.approvedDisDifference(approvedDirDifferenceOrderIn);
            } catch (Exception e) {
                log.error("配货差异单ID{}批准失败,错误原因是--{}", item, e.getMessage());
            }
        }
        return approved;
    }

    /**
     * 差异单回传
     *
     * @param differenceBillVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean differenceOrderCallBack(DifferenceBillVO differenceBillVO) {
        //转换入参
//        DifferenceBillVO differenceBillVO = JSON.parseObject(messageJson, DifferenceBillVO.class);
        OrdDirDelivDifference query = new OrdDirDelivDifference();
        List<DifferenceBillDtlVO> detail = differenceBillVO.getDetail();
        query.setDifferenceNo(differenceBillVO.getFsrcnum());
        OrdDirDelivDifference ordDirDelivDifference = this.selectOne(query);
        if (Objects.isNull(ordDirDelivDifference) || CollectionUtils.isEmpty(detail)) {
//            throw new BusinessException("此差异单不存在" + differenceBillVO.getFsrcnum());
            log.error("此差异单{}不存在", differenceBillVO.getFsrcnum());
            return true;
        }
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirDelivDifference.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("无此仓位信息");
        }
        if (!DifferenceOrderStatusEnum.AUDITED.getCode().equals(ordDirDelivDifference.getDifferenceStatus())) {
            //dts回传批准差异单时，改差异单后台已批准 记录日志
//            BusinessLog businessLog = new BusinessLog(OrdSystemConstant.SYSTEM_CODE,
//
//                    OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_APPROVED_SYSTEM.name(),
//                    String.valueOf(ordDirDelivDifference.getId()),
//                    OrdLogTypeEnum.ORD_DIR_ORDER_DIFFERENCE.getCode(),
//                    OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_APPROVED_SYSTEM.getName(), new Date(), ordDirDelivDifference.getUpdater());
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            log.error("配货差异单{}DTS回传状态不正确", ordDirDelivDifference.getDifferenceNo());
//            return "该差异单状态不正确";
            return true;
        }
        ordDirDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.APPROVED.getCode());
        ordDirDelivDifference.setLogisticsNo(differenceBillVO.getNum());
        ordDirDelivDifference.setApprovalTime(LocalDateTime.now());
        ordDirDelivDifference.setUpdater(differenceBillVO.getFfiller());
        //初始化差异单
        List<OrdDirDelivDifferenceDetail> ordDirDelivDifferenceDetails = this.initDifferenceBillVO(ordDirDelivDifference, detail, stockInfoOut.getBizOrgCode());
        if (CollectionUtils.isEmpty(ordDirDelivDifferenceDetails)) {
            throw new BusinessException("此差异单明细不存在");
        }
        // 修改差异单
        this.updateDirDifference(ordDirDelivDifference, OrdLogTypeEnum.ORD_DIR_ORDER_DIFFERENCE.getCode(), OrdLogTypeEnum.ORD_ORDER_DIFFERENCE_APPROVED.getName(),
                DifferenceOrderStatusEnum.APPROVED.getName());
        //  批量修改差异单及明细
        ordDirDelivDifferenceDetailService.batchUpdate(ordDirDelivDifferenceDetails);
        // 释放库存
        this.releaseStock(ordDirDelivDifference, ordDirDelivDifferenceDetails, stockInfoOut, ordDirDelivDifference.getApprovalTime());
//        return "成功";
        return true;
    }

    @Override
    public String getDifferenceNoByDeliveryNo(String deliveryOrderNo, String bizOrgCode) {
        return ordDirDelivDifferenceMapper.getDifferenceNoByDeliveryNo(deliveryOrderNo, bizOrgCode);
    }

    @Override
    public String getDeliveryNoByDiffNo(String differenceOrderNo, String bizOrgCode) {
        return ordDirDelivDifferenceMapper.getDeliveryNoByDiffNo(differenceOrderNo, bizOrgCode);
    }

    @Override
    public DirDiffOrderSummaryOut diffOrderSummary(DirDifferenceOrderIn differenceOrderIn) {
        this.handleChannelInfo(differenceOrderIn);
        if (StringUtils.isNotBlank(differenceOrderIn.getStoreArea())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(differenceOrderIn.getStoreArea(), differenceOrderIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return new DirDiffOrderSummaryOut();
            }
            differenceOrderIn.setStoreCodeList(storeCodeList);
        }
        return ordDirDelivDifferenceMapper.diffOrderSummary(differenceOrderIn);
    }

    @Override
    public OrdDirDelivDifference getOneByOrderNo(String differenceNo) {
        OrdDirDelivDifference dirDelivDifference = new OrdDirDelivDifference();
        dirDelivDifference.setDifferenceNo(differenceNo);
        return ordDirDelivDifferenceMapper.selectOne(dirDelivDifference);
    }

    /**
     * 初始化差异单
     *
     * @param saveDifferenceIn
     * @return
     */
    private OrdDirDelivDifference initDisDifference(SaveDifferenceIn saveDifferenceIn) {
        OrdDirDelivDifference ordDirDelivDifference = new OrdDirDelivDifference();
        BeanUtils.copy(saveDifferenceIn, ordDirDelivDifference);
        ordDirDelivDifference.setDifferenceStatus(DifferenceOrderStatusEnum.AUDITED.getCode());
        ordDirDelivDifference.setIsReversal(NumberUtil.INTEGER_ZERO);
        ordDirDelivDifference.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        ordDirDelivDifference.setIsDelete(NumberUtil.INTEGER_ZERO);
        ordDirDelivDifference.setDifferenceNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KPC.getCode(), saveDifferenceIn.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR));
        return ordDirDelivDifference;
    }
}
