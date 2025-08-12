package com.edc.erp.directly.dirdeliveryorder.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.async.handel.StoreChannelHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.enumeration.warning.OrderWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.StoreOrderWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.WarningBusinessTypeEnum;
import com.edc.erp.common.model.entity.StoreInfo;
import com.edc.erp.common.model.in.QueryPurchaseIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.purchase.OrdDeliveryDetailIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.QueryPurchaseOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.OrgSortOut;
import com.edc.erp.common.model.out.purchase.GoodsDtlsVO;
import com.edc.erp.common.model.out.purchase.TransferNoticePurchaseVO;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.stock.StockTransInfoOut;
import com.edc.erp.common.model.out.store.StoreInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.model.out.store.StoreUnit;
import com.edc.erp.common.model.out.warehouse.WarehouseInfoOut;
import com.edc.erp.common.model.out.warning.WarningResultOut;
import com.edc.erp.common.model.vo.InOneQtyVO;
import com.edc.erp.common.rpc.PurchaseOrderClient;
import com.edc.erp.common.service.*;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirdeliveryorder.entity.*;
import com.edc.erp.directly.dirdeliveryorder.handle.DirDeliveryImportHandle;
import com.edc.erp.directly.dirdeliveryorder.handle.DirDeliveryOrderSalvageHandle;
import com.edc.erp.directly.dirdeliveryorder.listener.DirDeliveryAsyncImportListener;
import com.edc.erp.directly.dirdeliveryorder.listener.DirDetailImportListener;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryMapper;
import com.edc.erp.directly.dirdeliveryorder.model.in.*;
import com.edc.erp.directly.dirdeliveryorder.model.out.*;
import com.edc.erp.directly.dirdeliveryorder.service.*;
import com.edc.erp.directly.dirdifferenceorder.entity.OrdDirDelivDifference;
import com.edc.erp.directly.dirdifferenceorder.service.OrdDirDelivDifferenceService;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequest;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.enumeration.DeliveryOrderReceiveProgressEnum;
import com.edc.erp.directly.enumeration.OrderCycleProcessConfigItemCodeEnum;
import com.edc.erp.directly.handle.DirDeliveryOrderConfigHandle;
import com.edc.erp.directly.handle.DirRequestOrderHandle;
import com.edc.erp.directly.util.FileExportUtil;
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
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.dictionary.service.SystemDictService;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 配货单表(OrdDirDelivery)表服务实现类
 *
 * @author weichao
 * @since 2022-11-10 14:45:30
 */
@Slf4j
@Service
public class OrdDirDeliveryServiceImpl extends BaseServiceImpl<OrdDirDelivery> implements OrdDirDeliveryService {

    @Autowired
    private OrdDirDeliveryMapper ordDirDeliveryMapper;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private UniqueUtils uniqueUtils;

    @Autowired
    private OrdDirDeliveryDetailService ordDirDeliveryDetailService;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private SystemDictService systemDictService;

    @Autowired
    private WarehouseServer warehouseServer;

    @Autowired
    private StockServer stockServer;

    @Autowired
    private FileService fileService;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private OrdDirDeliveryOrderAttachmentService ordDirDeliveryOrderAttachmentService;

    @Autowired
    private OrdDirDeliveryOrderSigningService ordDirDeliveryOrderSigningService;

    @Autowired
    private OrdDirDelivDifferenceService ordDirDelivDifferenceService;

    @Autowired
    private OrdDirDeliveryOrderHeartRateMonitorService ordDirDeliveryOrderHeartRateMonitorService;

    @Autowired
    private OrdDirSalvageDelivPondDetailService ordDirSalvageDelivPondDetailService;

    @Autowired
    private DirDeliveryOrderSalvageHandle dirDeliveryOrderSalvageHandle;

    @Autowired
    private StockStoreService stockStoreService;

    @Autowired
    private AsyncExportExecutor asyncExportExecutor;

    @Autowired
    private StockFlowService stockFlowService;

    @Autowired
    private DirRequestOrderHandle requestOrderHandle;

    @Autowired
    private DirDeliveryOrderConfigHandle dirDeliveryOrderConfigHandle;

    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;

    @Autowired
    private WarningService warningService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private DirDeliveryImportHandle dirDeliveryImportHandle;

    @Autowired
    private StoreChannelHandle storeChannelHandle;

    @Autowired
    private PurchaseOrderClient purchaseOrderClient;

    /**
     * 作废配货单
     *
     * @param deliveryOrderId
     * @param userName
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidDirDeliverOrder(Long deliveryOrderId, String userName) {
        OrdDirDelivery ordDirDelivery = ordDirDeliveryMapper.selectByPrimaryKey(deliveryOrderId);
        if (Objects.isNull(ordDirDelivery)) {
            throw new BusinessException("配货单不存在，无法作废！");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirDelivery.getStockCode(), UserUtil.getBizOrgCode(), "作废配货单");
        // 23-02-24 产品要求中转自主随意作废
//        if (DistributionWaysEnum.TRANSFER.getType().equals(ordDirDelivery.getDistributionType())) {
//            throw new BusinessException("中转配货单不能作废！");
//        }
        String beforeStatusCode = ordDirDelivery.getDeliveryStatusCode();
        boolean isInvalid = DeliveryOrderEnum.PENDING.getKey().equals(beforeStatusCode) || DeliveryOrderEnum.APPROVED.getKey().equals(beforeStatusCode)
                || (DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(beforeStatusCode) && DeliveryOrderSourceCodeEnum.MANUAL.getType().equals(ordDirDelivery.getSourceCode()));
        if (!isInvalid) {
            throw new BusinessException("配货单状态不允许作废");
        }
        ordDirDelivery.setDeliveryStatusCode(DeliveryOrderEnum.INVALID.getKey());
        ordDirDelivery.setUpdater(userName);
        ordDirDelivery.setUpdateTime(LocalDateTime.now());
        int count = ordDirDeliveryMapper.updateByPrimaryKeySelective(ordDirDelivery);
        List<OrdDirDeliveryDetail> deliveryOrderDetails = ordDirDeliveryDetailService.findDeliveryOrderDetails(deliveryOrderId);
        if (DeliveryOrderEnum.APPROVED.getKey().equals(beforeStatusCode) && DistributionWaysEnum.UNIFIEDDIS.getType().equals(ordDirDelivery.getDistributionType())) {
            //仓储库存释放
            List<StockFlowIn> stockFlowIns = this.initWarehouseStockInvalid(ordDirDelivery, deliveryOrderDetails, stockInfoOut);
            Response response = stockFlowService.checkStockFlow(stockFlowIns);
            if (!response.isSuccess()) {
                throw new BusinessException(response.getMessage());
            }
        }
        String content = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_STATUS_UPDATE.getKey(), ordDirDelivery.getDeliveryOrderNo(),
                DeliveryOrderEnum.getValueByKey(beforeStatusCode), DeliveryOrderEnum.getValueByKey(DeliveryOrderEnum.INVALID.getKey()));
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(ordDirDelivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), content, new Date(), userName);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;

    }

    /**
     * 作废中转配货单
     *
     * @param delivery
     * @param deliveryDetails
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidTransferOrder(OrdDirDelivery delivery, List<OrdDirDeliveryDetail> deliveryDetails) {
        String beforeStatusCode = delivery.getDeliveryStatusCode();
        delivery.setDeliveryStatusCode(DeliveryOrderEnum.INVALID.getKey());
        int count = ordDirDeliveryMapper.updateByPrimaryKeySelective(delivery);

        String content = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_STATUS_UPDATE.getKey(), delivery.getDeliveryOrderNo(),
                DeliveryOrderEnum.getValueByKey(beforeStatusCode), DeliveryOrderEnum.getValueByKey(DeliveryOrderEnum.INVALID.getKey()));
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(delivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), content, new Date(), delivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }


    /**
     * 作废配货单仓储库存调整初始化入参
     *
     * @param ordDirDelivery
     * @param detailList
     * @return
     */
    private List<StockFlowIn> initWarehouseStockInvalid(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> detailList, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_OUT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_OUT.getName());
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(ordDirDelivery.getCreator());
        stockFlowIn.setOperationType(OrderTypeEnum.INVALID.getCode());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        stockFlowIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        detailList.forEach(item -> {
            //库存不足，没有占用库存，无需释放
            if (Objects.isNull(item.getDistributionQuantity())) {
                return;
            }
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDirDelivery.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());

            //申请
            stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
            stockFlowGoodsIn.setApplyQty(item.getDistributionQuantity().abs());
            // 是否改变可用库存 Y是N否-统配出/配销出
            stockFlowGoodsIn.setIsBusinessQty("Y");
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
            //成本含税金额
            stockFlowGoodsIn.setCostTaxAmount(Objects.isNull(item.getDistributionAmount()) ? BigDecimal.ZERO : item.getDistributionAmount().negate());
            //成本不含税金额
            stockFlowGoodsIn.setCostNonTaxAmount(Objects.isNull(item.getDistributionExceptTaxAmount()) ? BigDecimal.ZERO : item.getDistributionExceptTaxAmount().negate());
            //成本税额
            stockFlowGoodsIn.setCostTax(Objects.isNull(item.getDistributionTaxAmount()) ? BigDecimal.ZERO : item.getDistributionTaxAmount().negate());
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(Objects.isNull(item.getDistributionExceptTaxAmount()) ? BigDecimal.ZERO : item.getDistributionExceptTaxAmount().negate());
            //含税金额
            stockFlowGoodsIn.setTaxAmount(Objects.isNull(item.getDistributionAmount()) ? BigDecimal.ZERO : item.getDistributionAmount().negate());
            // 税额
            stockFlowGoodsIn.setTax(Objects.isNull(item.getDistributionTaxAmount()) ? BigDecimal.ZERO : item.getDistributionTaxAmount().negate());
            //单号
            stockFlowGoodsIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
            stockFlowGoodsIns.add(stockFlowGoodsIn);

        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return Collections.singletonList(stockFlowIn);
    }

    /**
     * @Description: 配货单审核
     * @Author: ZhangYao
     * @Date: 2024/7/30 10:06
     * @param stockInfoOut:
     * @param ordDirDelivery:
     * @return: com.edc.plugins.common.response.Response<com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirDeliveryOut>
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<OrdDirDeliveryOut> audit(StockInfoOut stockInfoOut, OrdDirDelivery ordDirDelivery) {
        String key = DirSystemConstant.CHECK_DIR_DELIVERY_ORDER_AUDIT + ordDirDelivery.getBizOrgCode() +
                SystemConstant.COLON + ordDirDelivery.getStoreCode() + SystemConstant.WAIT + ordDirDelivery.getDeliveryOrderNo();
        if (!redisService.setIfAbsent(key, ordDirDelivery.getDeliveryOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("配货单短时间内请勿重复审核");
        }
        if (!DeliveryOrderEnum.PENDING.getKey().equals(ordDirDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
            throw new BusinessException("配货单状态不允许审核");
        }
        int executionCount;
        // 审核手动创建的非天岁中转配销单
        if (DistributionWaysEnum.TRANSFER.getType().equals(ordDirDelivery.getDistributionType())) {
            executionCount = dirDeliveryOrderSalvageHandle.handleDeliveryOrderByManualTransfer(ordDirDelivery, stockInfoOut.getBizOrgCode());
        } else {
            OrdDirSalvageDelivPondDetail ordDirSalvageDelivPondDetail = ordDirSalvageDelivPondDetailService.getOneByDeliveryOrderId(ordDirDelivery.getId());
            if (Objects.isNull(ordDirSalvageDelivPondDetail)) {
                return Response.error("配货单未找到对应的配货池明细");
            }
            // 占库存
            executionCount = dirDeliveryOrderSalvageHandle.handleSalvageAfterAuditDeliveryOrder(ordDirDelivery, stockInfoOut, ordDirSalvageDelivPondDetail);
        }
        if (executionCount == 0) {
            return Response.error("配销单未审核中占库存失败");
        }
        OrdDirDeliveryOut ordDirDeliveryOut = new OrdDirDeliveryOut();
        BeanUtils.copy(ordDirDelivery, ordDirDeliveryOut);
        return Response.data(ordDirDeliveryOut, "审核并占库存成功");
    }

    /**
     * 批量审核
     *
     * @param bizOrgCode
     * @param loginUsername
     * @param ordDirDeliveryIns
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response bachAudit(String bizOrgCode, String loginUsername, List<OrdDirDeliveryIn> ordDirDeliveryIns) {
        AtomicInteger successTotal = new AtomicInteger();
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        ordDirDeliveryIns.forEach(ordDirDeliveryIn -> {
            try {
                OrdDirDelivery ordDirDelivery = ordDirDeliveryMapper.selectByPrimaryKey(ordDirDeliveryIn.getId());
                if (Objects.isNull(ordDirDelivery)) {
                    throw new BusinessException("不存在的配货单");
                }
                StockInfoOut stockInfoOut;
                if (DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDirDelivery.getSourceCode())) {
                    stockInfoOut = stockServer.getTransInfo(ordDirDelivery.getStockCode());
                } else {
                    stockInfoOut = stockServer.getAndCheckStockInfo(ordDirDelivery.getStockCode(), bizOrgCode, "批量审核配货单");
                }
                Response<OrdDirDeliveryOut> response = this.audit(stockInfoOut, ordDirDelivery);
                if (response.isSuccess()) {
                    successTotal.getAndIncrement();
                }
            } catch (Exception e) {
                log.error("配货单{}审核异常{}", ordDirDeliveryIn.getDeliveryOrderNo(), e);
                errorJoiner.add("配货单" + ordDirDeliveryIn.getDeliveryOrderNo() + e.getMessage());
            }
        });
        return Response.success("成功审核" + successTotal.get() + "笔配货单;" + errorJoiner);
    }


    /**
     * 发货
     *
     * @param ordDirDelivery
     * @param updateDirDeliveryDetailInList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDirDeliveryInfo(OrdDirDelivery ordDirDelivery, List<UpdateDirDeliveryDetailIn> updateDirDeliveryDetailInList, String loginBizOrgCode) {
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirDelivery.getStockCode(), loginBizOrgCode, "配货单发货");
        List<OrdDirDeliveryDetail> ordDirDeliveryDetails = Lists.newArrayList();
        updateDirDeliveryDetailInList.forEach(updateDeliveryDetailIn -> {
            OrdDirDeliveryDetail dirDeliveryDetail = ordDirDeliveryDetailService.getOneByIdAndDeliveryOrderId(updateDeliveryDetailIn.getId(), ordDirDelivery.getId());
            if (Objects.isNull(dirDeliveryDetail)) {
                throw new BusinessException("未找到指定配货单明细");
            }
            if (Objects.isNull(dirDeliveryDetail.getDistributionQuantity()) || 0 == BigDecimal.ZERO.compareTo(dirDeliveryDetail.getDistributionQuantity())) {
                log.info("配货单{}商品{}没有审核数，故不处理发货数", ordDirDelivery.getDeliveryOrderNo(), dirDeliveryDetail.getDistributionQuantity());
                return;
            }
            dirDeliveryDetail.setDeliveryQuantity(updateDeliveryDetailIn.getDeliveryQuantity());
            dirDeliveryDetail.setDeliveryPackageQuantity(dirDeliveryDetail.getDeliveryQuantity().divide(dirDeliveryDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_ZERO, RoundingMode.UP));
            dirDeliveryDetail.setDeliveryAmount(dirDeliveryDetail.getOrderUnitPrice().multiply(dirDeliveryDetail.getDeliveryQuantity()));
            dirDeliveryDetail.setStockoutQuantity(dirDeliveryDetail.getDistributionQuantity().subtract(dirDeliveryDetail.getDeliveryQuantity()));
            dirDeliveryDetail.setUpdateTime(LocalDateTime.now());
            dirDeliveryDetail.setUpdater(ordDirDelivery.getUpdater());

            //税额
            BigDecimal sellTax = Objects.isNull(dirDeliveryDetail.getSellTax()) ? BigDecimal.ZERO : dirDeliveryDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tax = sellTax.add(BigDecimal.ONE);
            //最新仓储库存价
            BigDecimal warehousePrice = warehouseServer.getWarehousePrice(ordDirDelivery.getWrhCode(), ordDirDelivery.getStockCode(),
                    dirDeliveryDetail.getGoodsCode(), stockInfoOut.getBizOrgCode(), dirDeliveryDetail.getVendorCode());
            dirDeliveryDetail.setWrhPrice(warehousePrice);
            //统配出货配货单发货仓储减库存，仓储成本相关为负值
            dirDeliveryDetail.setWrhCostAmount(warehousePrice.multiply(updateDeliveryDetailIn.getDeliveryQuantity()).negate());
            dirDeliveryDetail.setWrhExceptTaxAmount(dirDeliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            dirDeliveryDetail.setWrhTaxAmount(dirDeliveryDetail.getWrhCostAmount().subtract(dirDeliveryDetail.getWrhExceptTaxAmount()));

            //统配出货配货单发货门店加库存，门店成本为正值
            dirDeliveryDetail.setStoreCostAmount(dirDeliveryDetail.getDeliveryAmount());
            dirDeliveryDetail.setStoreExceptTaxAmount(dirDeliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            dirDeliveryDetail.setStoreTaxAmount(dirDeliveryDetail.getStoreCostAmount().subtract(dirDeliveryDetail.getStoreExceptTaxAmount()));
            ordDirDeliveryDetails.add(dirDeliveryDetail);
            ordDirDelivery.setDeliveryAmount(Objects.isNull(ordDirDelivery.getDeliveryAmount()) ? dirDeliveryDetail.getDeliveryAmount() : ordDirDelivery.getDeliveryAmount().add(dirDeliveryDetail.getDeliveryAmount()));
            ordDirDelivery.setDeliveryQuantity(Objects.isNull(ordDirDelivery.getDeliveryQuantity()) ? dirDeliveryDetail.getDeliveryQuantity() : ordDirDelivery.getDeliveryQuantity().add(dirDeliveryDetail.getDeliveryQuantity()));
        });
        ordDirDeliveryDetailService.batchUpdateDeliveryInfo(ordDirDeliveryDetails);
        ordDirDelivery.setDeliveryAmount(ordDirDelivery.getDeliveryAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDirDelivery.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        ordDirDelivery.setReceiveProgress(DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey());
        ordDirDelivery.setDeliveryTime(LocalDateTime.now());
        ordDirDelivery.setUpdateTime(LocalDateTime.now());
        //设置自动收货时间
        this.setAutoTakeTime(ordDirDelivery);
        ordDirDeliveryMapper.updateByPrimaryKeySelective(ordDirDelivery);
        this.optInvForDeliveryInfo(ordDirDelivery, ordDirDeliveryDetails, stockInfoOut);
        String content = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_DELIVERED.getKey(), ordDirDelivery.getDeliveryOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(),
                String.valueOf(ordDirDelivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(),
                content, new Date(),
                ordDirDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 发货调整库存
     *
     * @param ordDirDelivery
     * @param ordDirDeliveryDetails
     */
    @Override
    public void optInvForDeliveryInfo(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> ordDirDeliveryDetails, StockInfoOut stockInfoOut) {
        List<OperatorInvIn> operatorInvIns = Lists.newArrayList();
        // 大库操作
        OperatorInvIn wmsOperatorInvIn = new OperatorInvIn();
        wmsOperatorInvIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        wmsOperatorInvIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
        if (!DistributionWaysEnum.TRANSFER.getType().equals(ordDirDelivery.getDistributionType())) {
            wmsOperatorInvIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
        }
        wmsOperatorInvIn.setIsBusinessQty("Y");
        operatorInvIns.add(wmsOperatorInvIn);

        // 门店操作
        OperatorInvIn storeOperatorInvIn = new OperatorInvIn();
        storeOperatorInvIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        storeOperatorInvIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
        storeOperatorInvIn.setApplyLowering(AdjustTypeEnum.ADD.getCode());
        storeOperatorInvIn.setIsBusinessQty("N");
        operatorInvIns.add(storeOperatorInvIn);
        List<StockFlowIn> stockFlowInList = this.dirDeliveryOrderOptInv(ordDirDelivery, ordDirDeliveryDetails, operatorInvIns, stockInfoOut);
        //调用库存rpc调整库存
        Response response = stockFlowService.checkStockFlow(stockFlowInList);
        if (!response.isSuccess()) {
            log.error("配货单{}发货调整库存异常{}", ordDirDelivery.getDeliveryOrderNo(), response.getMessage());
            throw new BusinessException(response.getMessage());
        }
    }

    /**
     * 初始化库存调整操作入参(发货)
     *
     * @param ordDirDelivery
     * @param detailList
     * @param operatorInvIns
     * @return
     */
    @Override
    public List<StockFlowIn> dirDeliveryOrderOptInv(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> detailList, List<OperatorInvIn> operatorInvIns, StockInfoOut stockInfoOut) {
        List<StockFlowIn> stockFlowInList = Lists.newArrayList();
        for (OperatorInvIn operatorInvIn : operatorInvIns) {
            StockFlowIn stockFlowIn = new StockFlowIn();
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_OUT.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_OUT.getName());
            stockFlowIn.setOccurrenceLocation(operatorInvIn.getOccurrenceLocation());
            stockFlowIn.setFlowDate(ordDirDelivery.getDeliveryTime());
            stockFlowIn.setCreator(ordDirDelivery.getCreator());
            stockFlowIn.setOperationType(OrderTypeEnum.SHIPPED.getCode());
            stockFlowIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
            List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
            detailList.forEach(item -> {
                if (Objects.isNull(item.getDeliveryQuantity())) {
                    return;
                }
                BigDecimal sell = Objects.isNull(item.getSellTax()) ? BigDecimal.ZERO : item.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
                BigDecimal tar = sell.add(BigDecimal.ONE);
                StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
                BeanUtils.copy(item, stockFlowGoodsIn);
                //实际数
                stockFlowGoodsIn.setActualQty(item.getDeliveryQuantity().abs());
                //实际增/减
                stockFlowGoodsIn.setActualLowering(operatorInvIn.getActualLowering());
                //申请数
                BigDecimal applyQty = BigDecimal.ZERO;
                if (StockHappenLieEnum.STORE.getCode().equals(stockFlowIn.getOccurrenceLocation())) {
                    applyQty = item.getDeliveryQuantity().abs();
                    //配货出-发货门店库存调整发生价取最新门店配货价 2023-06-09改为继承价
                    stockFlowGoodsIn.setPrice(item.getOrderUnitPrice());
                    stockFlowIn.setBizOrgCode(ordDirDelivery.getBizOrgCode());
                    stockFlowIn.setOrgCode(ordDirDelivery.getOrgCode());
                    if (StringUtils.isNotBlank(item.getExpiry())) {
                        stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(item.getExpiry(), applyQty)));
                    }
                }
                if (StockHappenLieEnum.WAREHOUSE.getCode().equals(stockFlowIn.getOccurrenceLocation())) {
                    applyQty = item.getDistributionQuantity().abs();
                    //统配出-发货大库库存调整发生价取最新仓储库存价
                    stockFlowGoodsIn.setPrice(item.getWrhPrice());
                    stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
                    stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
                }
                stockFlowGoodsIn.setApplyQty(applyQty);
                //申请增/减
                stockFlowGoodsIn.setApplyLowering(operatorInvIn.getApplyLowering());
                //是否改变可用库存 Y是N否-统配出/配销出
                stockFlowGoodsIn.setIsBusinessQty(operatorInvIn.getIsBusinessQty());

                // 仓储
                stockFlowGoodsIn.setStockCode(ordDirDelivery.getStockCode());
                stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
                stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
                stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
                stockFlowGoodsIn.setStoreCode(ordDirDelivery.getStoreCode());
                stockFlowGoodsIn.setStoreName(ordDirDelivery.getStoreName());
                //库存发生位置
                stockFlowGoodsIn.setPosition(operatorInvIn.getOccurrenceLocation());
                //成本含税金额
                BigDecimal costTaxAmount = BigDecimal.ZERO;
                if (StockHappenLieEnum.STORE.getCode().equals(operatorInvIn.getOccurrenceLocation())) {
                    //门店成本含税金额为正值
                    costTaxAmount = item.getStoreCostAmount();
                    //含税金额
                    stockFlowGoodsIn.setTaxAmount(item.getDeliveryAmount());
                    //不含税金额
                    stockFlowGoodsIn.setNonTaxAmount(item.getDeliveryAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                }
                if (StockHappenLieEnum.WAREHOUSE.getCode().equals(operatorInvIn.getOccurrenceLocation())) {
                    //大库减库存，成本含税金额为负值
                    costTaxAmount = item.getWrhCostAmount();
                    //含税金额
                    stockFlowGoodsIn.setTaxAmount(item.getDeliveryAmount().negate());
                    //不含税金额
                    stockFlowGoodsIn.setNonTaxAmount(item.getDeliveryAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                }
                // 税额
                stockFlowGoodsIn.setTax(stockFlowGoodsIn.getTaxAmount().subtract(stockFlowGoodsIn.getNonTaxAmount()));
                //成本含税金额
                stockFlowGoodsIn.setCostTaxAmount(costTaxAmount);
                //成本不含税金额
                stockFlowGoodsIn.setCostNonTaxAmount(stockFlowGoodsIn.getCostTaxAmount().divide(tar, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                //成本税额
                stockFlowGoodsIn.setCostTax(stockFlowGoodsIn.getCostTaxAmount().subtract(stockFlowGoodsIn.getCostNonTaxAmount()));
                //单号
                stockFlowGoodsIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
                stockFlowGoodsIns.add(stockFlowGoodsIn);
            });
            stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
            stockFlowInList.add(stockFlowIn);
        }
        return stockFlowInList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdate(OrdDirDeliveryIn ordDirDeliveryIn) {
        ordDirDeliveryIn.setDeliveryStatusCode(DistributionWaysEnum.TRANSFER.getType().equals(ordDirDeliveryIn.getDistributionType()) ? DeliveryOrderEnum.PREVIEWAPPROVED.getKey() : DeliveryOrderEnum.PENDING.getKey());
        return this.saveOrUpdateDeliverOrder(ordDirDeliveryIn);
    }

    @Override
    public OrdDirDeliveryDetailOut checkOrderGoods(DeliveryIn deliveryIn, String centerStockBizOrgCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBizOrgCode(deliveryIn.getBizOrgCode());
        orderGoodsIn.setGoodsCode(deliveryIn.getGoodsCode());
        orderGoodsIn.setStoreCode(deliveryIn.getStoreCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION_BILL.getType());
        OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(storeOrderGoods)) {
            return null;
        }
        if (Objects.isNull(storeOrderGoods.getDistributionUnitPrice())) {
            throw new BusinessException(deliveryIn.getGoodsCode() + "商品没有配销价;");
        }
        if (!deliveryIn.getWrhCode().equals(storeOrderGoods.getWarehouseCode())) {
            throw new BusinessException(deliveryIn.getGoodsCode() + "仓储代码与所选仓储代码不匹配;");
        }
        if (!deliveryIn.getStockCode().equals(storeOrderGoods.getStockCode())) {
            throw new BusinessException(deliveryIn.getGoodsCode() + "仓位代码与所选仓位代码不匹配;");
        }
        if (!deliveryIn.getDistributionType().equals(storeOrderGoods.getDistributionWay())) {
            throw new BusinessException(deliveryIn.getGoodsCode() + "配货方式与所选配货方式不匹配;");
        }
        OrdDirDeliveryDetailOut ordDirDeliveryDetail = new OrdDirDeliveryDetailOut();
        ordDirDeliveryDetail.setGoodsCode(storeOrderGoods.getGoodsCode());
        ordDirDeliveryDetail.setGoodsName(storeOrderGoods.getGoodsName());
        ordDirDeliveryDetail.setBarCode(storeOrderGoods.getBarCode());
        ordDirDeliveryDetail.setGoodsType(storeOrderGoods.getGoodsType());
        ordDirDeliveryDetail.setDistributionSpecification(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? "" : storeOrderGoods.getDistributionSpecification().getQpcStr());
        ordDirDeliveryDetail.setDistributionSpecificationUnit(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? "" : storeOrderGoods.getDistributionSpecification().getUnitName());
        ordDirDeliveryDetail.setDistributionSpecificationNum(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : BigDecimal.valueOf(storeOrderGoods.getDistributionSpecification().getQpc()));
        ordDirDeliveryDetail.setSellTax(storeOrderGoods.getOutTax());
        ordDirDeliveryDetail.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(ordDirDeliveryDetail.getGoodsType()));
        ordDirDeliveryDetail.setDistributionPrice(Objects.isNull(storeOrderGoods.getDistributionPrice()) ? BigDecimal.ZERO : storeOrderGoods.getDistributionPrice());
        ordDirDeliveryDetail.setDistributionUnitPrice(Objects.isNull(storeOrderGoods.getDistributionUnitPrice()) ? BigDecimal.ZERO : storeOrderGoods.getDistributionUnitPrice());
        ordDirDeliveryDetail.setDistributionSpecificationNum(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : new BigDecimal(storeOrderGoods.getDistributionSpecification().getQpc()));
        ordDirDeliveryDetail.setSmallSort(storeOrderGoods.getSort());
        ordDirDeliveryDetail.setSortName(storeOrderGoods.getSortName());
        ordDirDeliveryDetail.setDistributionType(DistributionWaysEnum.getTypeByName(storeOrderGoods.getDistributionWay()));
        ordDirDeliveryDetail.setDistributionTypeValue(storeOrderGoods.getDistributionWay());
        ordDirDeliveryDetail.setIsGift(NumberUtil.INTEGER_ZERO);
        ordDirDeliveryDetail.setVendorCode(storeOrderGoods.getVendorCode());
        BigDecimal stockStorePrice = warehouseServer.getStockPrice(deliveryIn.getStoreCode(), deliveryIn.getGoodsCode(), deliveryIn.getBizOrgCode());
        ordDirDeliveryDetail.setStoreStockPrice(Objects.isNull(stockStorePrice) ? BigDecimal.ZERO : stockStorePrice);
        BigDecimal stockWarehousePrice = warehouseServer.getWarehousePrice(deliveryIn.getWrhCode(), deliveryIn.getStockCode(),
                deliveryIn.getGoodsCode(), centerStockBizOrgCode, ordDirDeliveryDetail.getVendorCode());
        ordDirDeliveryDetail.setWrhPrice(Objects.isNull(stockWarehousePrice) ? BigDecimal.ZERO : stockWarehousePrice);
        ordDirDeliveryDetail.setInvoiceType(storeOrderGoods.getInvoiceType());
        ordDirDeliveryDetail.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(ordDirDeliveryDetail.getInvoiceType()));
        return ordDirDeliveryDetail;
    }

    @Override
    public Response<List<DirDeliveryOrderDetailsOut>> importDeliveryDetail(ImportDeliveryIn importDeliveryIn, String centerStockBizOrgCode) {
        byte[] bytes = fileService.getFileBytesByFileId(importDeliveryIn.getFileId(), SystemConstant.SYSTEM_CODE);
        if (null == bytes) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DirDetailImportListener listener = new DirDetailImportListener(this, importDeliveryIn, centerStockBizOrgCode);
        ExcelReader excelReader = EasyExcelFactory.read(inputStream, ImportDirDeliveryDetailsOrder.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcelFactory.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        List<DirDeliveryOrderDetailsOut> deliveryOrderDetailsOuts = listener.getDeliveryOrderDetailsOuts();
        // 查询商品在采购中效期码
        List<QueryPurchaseIn> queryPurchaseInList = deliveryOrderDetailsOuts.stream().filter(detail -> StringUtils.isNotBlank(detail.getPurchaseNo())).map(item -> {
            QueryPurchaseIn queryPurchaseIn = new QueryPurchaseIn();
            queryPurchaseIn.setPurchaseNo(item.getPurchaseNo());
            queryPurchaseIn.setGoodsCode(item.getGoodsCode());
            return queryPurchaseIn;
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queryPurchaseInList)) {
            return Response.data(deliveryOrderDetailsOuts, listener.getImportErrorMessage(totalErrorMap));
        }
        Response<List<QueryPurchaseOut>> purResponse = purchaseOrderClient.findValidityInfo(queryPurchaseInList);
        if (!purResponse.isSuccess()) {
            return Response.error(purResponse.getMessage());
        }
        Map<String, QueryPurchaseOut> purchaseGoodsExpiryMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(purResponse.getData())) {
            purchaseGoodsExpiryMap = purResponse.getData().stream()
                    .collect(Collectors.toMap(queryPurchaseOut -> queryPurchaseOut.getPurchaseNo() + SystemConstant.SHORT_LINE + queryPurchaseOut.getGoodsCode(), Function.identity()));
        }
        List<DirDeliveryOrderDetailsOut> resultDetailList = Lists.newArrayList();
        Map<String, QueryPurchaseOut> finalPurchaseGoodsExpiryMap = purchaseGoodsExpiryMap;
        deliveryOrderDetailsOuts.forEach(item -> {
            if (StringUtils.isBlank(item.getPurchaseNo())) {
                resultDetailList.add(item);
                return;
            }
            QueryPurchaseOut queryPurchaseOut = finalPurchaseGoodsExpiryMap.get(item.getPurchaseNo() + SystemConstant.SHORT_LINE + item.getGoodsCode());
            if (Objects.isNull(queryPurchaseOut)) {
                String mapKey = "第【" + item.getImportIndex() + "】行：";
                StringJoiner errorJoiner = totalErrorMap.get(mapKey);
                if (Objects.isNull(errorJoiner)) {
                    errorJoiner = new StringJoiner(SystemConstant.COMMA);
                }
                errorJoiner.add("采购单下此商品不存在或者采购单未审核");
                totalErrorMap.put(mapKey, errorJoiner);
                return;
            }
            if (StringUtils.isNotBlank(queryPurchaseOut.getValidityCode())) {
                item.setExpiry(queryPurchaseOut.getValidityCode().substring(8, queryPurchaseOut.getValidityCode().length() - 1));
            }
            resultDetailList.add(item);
        });
        return Response.data(resultDetailList, listener.getImportErrorMessage(totalErrorMap));
    }

    @Override
    public Page<DirDeliveryOrderOut> findDeliveryOrdersByPage(DirDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = deliveryOrderIn.getBizOrgCode();
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(loginBizOrgCode);
        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !loginBizOrgCode.equals(stockInfoOut.getBizOrgCode())).findFirst();
        if (!anyOneOptional.isPresent()) {
            deliveryOrderIn.setBizOrgCode("");
        }
        if (CollectionUtils.isEmpty(deliveryOrderIn.getStockCodeList())) {
            List<String> stockCodeList = authOrgStockMap.keySet().stream().collect(Collectors.toList());
            deliveryOrderIn.setStockCodeList(stockCodeList);
        } else {
            // 校验业务仓位是否允许操作
            storeChannelHandle.checkStockCodeListISAuth(deliveryOrderIn.getStockCodeList(), authOrgStockMap);
        }
        if (StringUtils.isNotEmpty(deliveryOrderIn.getStoreArea())) {
            List<String> storeCodeList = new ArrayList<>();
            List<StoreInfoOut> storeList = storeCenterService.getStoreInfoByCode(new StoreInfoIn(deliveryOrderIn.getStoreArea()));
            if (CollectionUtils.isEmpty(storeList)) {
                return new Page<>(deliveryOrderIn);
            }
            storeList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            deliveryOrderIn.setStoreCodeList(storeCodeList);
        }
        if (StringUtils.isNotBlank(deliveryOrderIn.getDifferenceOrderNo())) {
            String deliveryNo = ordDirDelivDifferenceService.getDeliveryNoByDiffNo(deliveryOrderIn.getDifferenceOrderNo(), null);
            if (StringUtils.isBlank(deliveryNo)) {
                return new Page<>(deliveryOrderIn);
            }
            deliveryOrderIn.setDeliveryOrderNo(deliveryNo);
        }
        List<DirDeliveryOrderOut> deliveryOrderOutList = ordDirDeliveryMapper.findDeliveryOrdersByPage(deliveryOrderIn);
        for (DirDeliveryOrderOut item : deliveryOrderOutList) {
            item.setDifferenceOrderNo(ordDirDelivDifferenceService.getDifferenceNoByDeliveryNo(item.getDeliveryOrderNo(), item.getBizOrgCode()));
            StockInfoOut stockInfoOut = authOrgStockMap.get(item.getStockCode());
            if (Objects.isNull(stockInfoOut)) {
                throw new BusinessException(item.getStockCode() + "无此仓位信息");
            }
//            OrdDirDeliveryDetail ordDirDeliveryDetail = ordDirDeliveryDetailService.getDetail(item.getId());
            List<OrdDirDeliveryDetail> deliveryOrderDetails = ordDirDeliveryDetailService.findDeliveryOrderDetails(item.getId());
            if (CollectionUtils.isNotEmpty(deliveryOrderDetails)) {
                // 仓位中文
                item.setStockName(stockInfoOut.getStockName());
                item.setWrhCodeName(stockInfoOut.getWarehouseName());
                item.setOrdDirDeliveryDetailList(deliveryOrderDetails);
            }
            //状态
            item.setDeliveryStatusValue(DeliveryOrderEnum.getValueByKey(item.getDeliveryStatusCode()));
            //是否能匹配订货订单
            //配送方式中文
            item.setDistributionTypeValue(systemDictService.getSystemDictName(StringUtils.isNotBlank(item.getDistributionType()) ? item.getDistributionType() : ""));
            // 如果是已收货状态
            if (item.getDeliveryStatusCode().equals(DeliveryOrderEnum.SHIPPED.getKey()) && NumberUtils.INTEGER_ZERO.equals(item.getIsReversal()) && NumberUtils.INTEGER_ZERO.equals(item.getIsReversalOrder())) {
                // 判断是否已签收，未签收的不能进行收货
                OrdDirDeliveryOrderSigning deliveryOrderSigning = ordDirDeliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(item.getId(), item.getBizOrgCode());
                if (Objects.isNull(deliveryOrderSigning)) {
                    item.setIsSigning(0);
                    item.setIsCanTake(0);
                } else {
                    item.setIsSigning(1);
                    // 已签收，判断是否有人正在收货
                    if (DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey().equals(item.getReceiveProgress())) {
                        item.setIsCanTake(1);
                    } else {
                        item.setIsCanTake(0);
                    }
                }
            } else {
                item.setIsSigning(-1);
                item.setIsCanTake(-1);
            }
            //是否能释放订货订单金额
        }
        Page<DirDeliveryOrderOut> resultPage = new Page<>(deliveryOrderIn);
        resultPage.setList(deliveryOrderOutList);
        return resultPage;
    }

    @Override
    public Page<DirDeliveryOrderDetailsOut> findDeliveryOrderDetailForPage(DirDeliveryOrderDetailsIn deliveryOrderDetailsIn, String loginBizOrgCode) {
        OrdDirDelivery ordDirDelivery = ordDirDeliveryMapper.selectByPrimaryKey(deliveryOrderDetailsIn.getDeliveryOrderId());
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirDelivery.getStockCode(), loginBizOrgCode, "操作配货单明细");
        List<DirDeliveryOrderDetailsOut> dirDeliveryOrderDetailsOuts = ordDirDeliveryDetailService.findDeliveryOrderDetailsForPage(deliveryOrderDetailsIn);
        dirDeliveryOrderDetailsOuts.forEach(item -> {
            //直营配货方式
            item.setDistributionTypeValue(DistributionWaysEnum.getNameByType(ordDirDelivery.getDistributionType()));
            item.setDistributionType(ordDirDelivery.getDistributionType());
            //品类名称
            OrgSortOut orgSortOut = orderGoodsServer.getByCode(item.getSmallSort(), ordDirDelivery.getBizOrgCode());
            item.setSortName(Objects.nonNull(orgSortOut) ? orgSortOut.getSortName() : "");
            //实收金额
            BigDecimal arrivalAmount = BigDecimal.ZERO;
            if (Objects.nonNull(item.getArrivalQuantity()) && Objects.nonNull(item.getOrderUnitPrice())) {
                arrivalAmount = item.getArrivalQuantity().multiply(item.getOrderUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP);
            }
            item.setArrivalAmount(arrivalAmount);
            BigDecimal deliveryAmount = BigDecimal.ZERO;
            if (Objects.nonNull(item.getDeliveryQuantity()) && Objects.nonNull(item.getOrderUnitPrice())) {
                deliveryAmount = item.getDeliveryQuantity().multiply(item.getOrderUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP);
            }
            //实配金额
            item.setDeliveryAmount(deliveryAmount);
            item.setPositionName(stockInfoOut.getStockName());
            item.setPosition(ordDirDelivery.getStoreCode());
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
            item.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(item.getInvoiceType()));
        });
        Page<DirDeliveryOrderDetailsOut> resultPage = new Page<>(deliveryOrderDetailsIn);
        resultPage.setList(dirDeliveryOrderDetailsOuts);
        return resultPage;
    }

    @Override
    public DirDeliveryOrderOut getDeliveryOrderOutByDeliveryOrderId(Long deliveryOrderId) {
        OrdDirDelivery ordDirDelivery = this.selectByPrimaryKey(deliveryOrderId);
        DirDeliveryOrderOut dirDeliveryOrderOut = new DirDeliveryOrderOut();
        BeanUtils.copy(ordDirDelivery, dirDeliveryOrderOut);
        WarehouseInfoOut warehouseInfoOut = warehouseServer.getWarehouseInfoByCode(ordDirDelivery.getWrhCode());
        if (Objects.isNull(warehouseInfoOut)) {
            log.info("配货单{}获取详情--仓储代码{}不存在", ordDirDelivery.getDeliveryOrderNo(), ordDirDelivery.getWrhCode());
            throw new RuntimeException("仓储代码不存在");
        }
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirDelivery.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("无此仓位信息");
        }
        dirDeliveryOrderOut.setStockName(stockInfoOut.getStockName());
        dirDeliveryOrderOut.setWrhCodeName(stockInfoOut.getWarehouseName());
        dirDeliveryOrderOut.setDistributionTypeValue(DistributionWaysEnum.getNameByType(dirDeliveryOrderOut.getDistributionType()));
        OrdDirDelivRequest ordDirDelivRequest = ordDirDeliveryMapper.getRequestOrder(ordDirDelivery.getId(), ordDirDelivery.getBizOrgCode());
        if (Objects.nonNull(ordDirDelivRequest)) {
            OrdDirOrderCycle ordDirOrderCycle = ordDirDeliveryMapper.getOrderCycleById(ordDirDelivRequest.getOrderCycleId(), ordDirDelivery.getBizOrgCode());
            dirDeliveryOrderOut.setShortOrderType(Objects.isNull(ordDirOrderCycle) ? "" : ordDirOrderCycle.getShortOrderType());
            dirDeliveryOrderOut.setRequestOrderNo(ordDirDelivRequest.getRequestOrderNo());
        }
        dirDeliveryOrderOut.setDeliveryStatusValue(DeliveryOrderEnum.getValueByKey(ordDirDelivery.getDeliveryStatusCode()));
        // 差异单号
        dirDeliveryOrderOut.setDifferenceOrderNo(ordDirDelivDifferenceService.getDifferenceNoByDeliveryNo(ordDirDelivery.getDeliveryOrderNo(), ordDirDelivery.getBizOrgCode()));
        // 红冲单
        if (NumberUtils.INTEGER_ONE.equals(ordDirDelivery.getIsReversal())) {
            OrdDirDelivery reversalDeliveryOrder = ordDirDeliveryMapper.selectOne(OrdDirDelivery.builder().sourceNo(ordDirDelivery.getDeliveryOrderNo()).bizOrgCode(ordDirDelivery.getBizOrgCode()).isDelete(ModelConst.DELETE.NO).build());
            dirDeliveryOrderOut.setReversalOrderId(reversalDeliveryOrder.getId());
            dirDeliveryOrderOut.setReversalOrderNo(reversalDeliveryOrder.getDeliveryOrderNo());
        }
        if (NumberUtils.INTEGER_ONE.equals(ordDirDelivery.getIsReversalOrder())) {
            OrdDirDelivery sourceDeliveryOrder = ordDirDeliveryMapper.selectOne(OrdDirDelivery.builder().deliveryOrderNo(ordDirDelivery.getSourceNo()).bizOrgCode(ordDirDelivery.getBizOrgCode()).isDelete(ModelConst.DELETE.NO).build());
            dirDeliveryOrderOut.setSourceOrderId(sourceDeliveryOrder.getId());
        }
        //根据配销单id查询配销单明细 统计实收数量和实收金额
        Map<String, BigDecimal> sumMap = this.countQuantityAndAmount(deliveryOrderId);
        //添加总实收数量
        dirDeliveryOrderOut.setTotalArrivalQuantity(sumMap.get(OrdDisMapKeyConstant.TOTAL_QUANTITY));
        //添加总实收金额
        dirDeliveryOrderOut.setTotalArrivalAmount(sumMap.get(OrdDisMapKeyConstant.TOTAL_AMOUNT));

        return dirDeliveryOrderOut;
    }

    /**
     * 导出直营配货单明细信息
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    @Override
    public String exportDeliveryOrderDetails(DirDeliveryOrderDetailsIn deliveryOrderDetailsIn, String loginBizOrgCode) {
        deliveryOrderDetailsIn.setPageNum(0);
        deliveryOrderDetailsIn.setPageSize(0);
        Page<DirDeliveryOrderDetailsOut> resultPage = this.findDeliveryOrderDetailForPage(deliveryOrderDetailsIn, loginBizOrgCode);
        List<ExcelDeliveryOrderDetails> excelDeliveryOrderDetails = parseDataToExcel(resultPage.getList());
        log.info("导出直营配货单明细列表集合大小是--{}", excelDeliveryOrderDetails.size());
        String title = "导出直营配货单明细";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelDeliveryOrderDetails,
                title, title, ExcelDeliveryOrderDetails.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    /**
     * 导出配货单列表
     *
     * @param deliveryOrderIn
     * @return
     */
    @Override
    public String exportDeliveryOrder(DirDeliveryOrderIn deliveryOrderIn) {
        // 设置每次查询条数
        deliveryOrderIn.setPageSize(10000);
        String title = "配货单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配货单列表",
                        // 导出模板实体
                        ExcelDirDeliveryOrder.class,
                        // 分页查询对象
                        deliveryOrderIn,
                        // 分页查询方法
                        page -> {
                            Page<DirDeliveryOrderOut> deliveryOrdersByPage = this.findDeliveryOrdersByPage(deliveryOrderIn);
                            List<ExcelDirDeliveryOrder> excelDeliveryOrder = parseOrderDataToExcel(deliveryOrdersByPage.getList());
                            log.info("导出配货单列表集合大小是--{}", excelDeliveryOrder.size());
                            return excelDeliveryOrder;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    @Override
    public String exportDeliveryOrderDetailByOrder(DirDeliveryOrderIn deliveryOrderIn) {
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(deliveryOrderIn.getBizOrgCode());
        if (CollectionUtils.isEmpty(deliveryOrderIn.getStockCodeList())) {
            List<String> stockCodeList = authOrgStockMap.keySet().stream().collect(Collectors.toList());
            deliveryOrderIn.setStockCodeList(stockCodeList);
        } else {
            // 校验业务仓位是否允许操作
            storeChannelHandle.checkStockCodeListISAuth(deliveryOrderIn.getStockCodeList(), authOrgStockMap);
        }
        if (StringUtils.isNotEmpty(deliveryOrderIn.getStoreArea())) {
            List<String> storeCodeList = new ArrayList<>();
            List<StoreInfoOut> storeList = storeCenterService.getStoreInfoByCode(new StoreInfoIn(deliveryOrderIn.getStoreArea()));
            if (CollectionUtils.isEmpty(storeList)) {
                throw new BusinessException("无可供导出的数据");
            }
            storeList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            deliveryOrderIn.setStoreCodeList(storeCodeList);
        }
        if (StringUtils.isNotBlank(deliveryOrderIn.getDifferenceOrderNo())) {
            String deliveryNo = ordDirDelivDifferenceService.getDeliveryNoByDiffNo(deliveryOrderIn.getDifferenceOrderNo(), null);
            if (StringUtils.isBlank(deliveryNo)) {
                throw new BusinessException("无可供导出的数据");
            }
            deliveryOrderIn.setDeliveryOrderNo(deliveryNo);
        }
        LocalDateTime now = LocalDateTime.now();
//        deliveryOrderIn.setCreateEndTime(DateUtil.formatLocalDateTime(now));
//        deliveryOrderIn.setCreateStartTime(DateUtil.formatLocalDateTime(now.plusDays(-2)));
        // 设置每次查询条数
        deliveryOrderIn.setPageNum(1);
        deliveryOrderIn.setPageSize(5000);
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = "配货单明细".concat(DateUtil.format(now, DatePattern.PURE_DATETIME_MS_PATTERN)).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配货单明细",
                        // 导出模板实体
                        AsyncExcelDeliveryOrderDetail.class,
                        // 分页查询对象
                        deliveryOrderIn,
                        // 分页查询方法
                        page -> this.findListForAsyncExportPage(deliveryOrderIn))
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    @Override
    public List<DirNoSalvageDeliveryOrderOut> findNoSalvageDeliveryOrderList(List<Long> idList, String bizOrgCode) {
        return ordDirDeliveryMapper.findNoSalvageDeliveryOrderList(idList, DeliveryOrderEnum.PENDING.getKey(), bizOrgCode);
    }

    @Override
    public OrdDirDeliveryOut getDeliveryOrderOutByNo(String deliveryOrderNo) {
        OrdDirDelivery order = new OrdDirDelivery();
        order.setDeliveryOrderNo(deliveryOrderNo);
        order.setIsDelete(0);
        OrdDirDelivery entity = ordDirDeliveryMapper.selectOne(order);
        if (null == entity) {
            return null;
        }
        OrdDirDeliveryOut ordDirDeliveryOut = new OrdDirDeliveryOut();
        BeanUtils.copy(entity, ordDirDeliveryOut);
        List<OrdDirDeliveryDetail> details = ordDirDeliveryDetailService.findDeliveryOrderDetails(entity.getId());
        ordDirDeliveryOut.setDetailList(details);
        return ordDirDeliveryOut;
    }

    @Override
    public List<String> findNotAuditDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(DeliveryOrderEnum.PENDING.getKey());
        orderStatusCodeList.add(DeliveryOrderEnum.PREVIEWAPPROVED.getKey());
        return ordDirDeliveryMapper.findNotAuditDeliveryOrderList(createTimeBegin, createTimeEnd, orderStatusCodeList, bizOrgCode);
    }

    @Override
    public TakeDeliveryInfoOut getTakeDeliveryInfoByDeliveryOrderId(Long dirDeliveryOrderId) {
        OrdDirDelivery ordDirDelivery = ordDirDeliveryMapper.selectByPrimaryKey(dirDeliveryOrderId);
        if (Objects.isNull(ordDirDelivery)) {
            throw new BusinessException("不存在的配货单");
        }

        List<OrdDirDeliveryOrderAttachment> attachmentList = ordDirDeliveryOrderAttachmentService.findAllByDeliveryOrderIdAndBizOrgCode(dirDeliveryOrderId, ordDirDelivery.getBizOrgCode());
        List<String> signingAttachmentUrlList = Lists.newArrayList();
        List<String> takeAttachmentUrlList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            signingAttachmentUrlList = attachmentList.stream().filter(deliveryOrderAttachment -> NumberUtil.INTEGER_ONE.equals(deliveryOrderAttachment.getAttachmentType())).map(OrdDirDeliveryOrderAttachment::getAttachmentUrl).collect(Collectors.toList());
            takeAttachmentUrlList = attachmentList.stream().filter(deliveryOrderAttachment -> NumberUtil.INTEGER_TWO.equals(deliveryOrderAttachment.getAttachmentType())).map(OrdDirDeliveryOrderAttachment::getAttachmentUrl).collect(Collectors.toList());
        }

        DirDeliveryOrderSigningOut dirDeliveryOrderSigningOut = new DirDeliveryOrderSigningOut();
        OrdDirDeliveryOrderSigning ordDirDeliveryOrderSigning = ordDirDeliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(dirDeliveryOrderId, ordDirDelivery.getBizOrgCode());
        if (Objects.nonNull(ordDirDeliveryOrderSigning)) {
            BeanUtils.copy(ordDirDeliveryOrderSigning, dirDeliveryOrderSigningOut);
        }
        dirDeliveryOrderSigningOut.setSigningAttachmentUrlList(signingAttachmentUrlList);
        TakeDeliveryInfoOut takeDeliveryInfoOut = new TakeDeliveryInfoOut();
        takeDeliveryInfoOut.setDirDeliveryOrderSigningOut(dirDeliveryOrderSigningOut);
        takeDeliveryInfoOut.setTakeRemark(ordDirDelivery.getTakeRemark());
        takeDeliveryInfoOut.setTakeAttachmentUrlList(takeAttachmentUrlList);
        return takeDeliveryInfoOut;
    }

    /**
     * 冲销配货单
     *
     * @param chargeDirDeliveryOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response chargeDeliveryOrder(ChargeDirDeliveryOrderIn chargeDirDeliveryOrderIn) {
        OrdDirDelivery ordDirDelivery = ordDirDeliveryMapper.selectByPrimaryKey(chargeDirDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(ordDirDelivery)) {
            throw new BusinessException("此配货单不存在");
        }
        String loginBizOrgCode = chargeDirDeliveryOrderIn.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDirDelivery.getStockCode(), loginBizOrgCode, "冲销配货单");
        //校验已收货的配货差异单是否冲销
        OrdDirDelivDifference query = new OrdDirDelivDifference();
        query.setDeliveryOrderNo(ordDirDelivery.getDeliveryOrderNo());
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        query.setIsReversal(NumberUtil.INTEGER_ZERO);
        query.setIsReversalOrder(NumberUtils.INTEGER_ZERO);
        List<OrdDirDelivDifference> queryList = ordDirDelivDifferenceService.list(query);
        if (CollectionUtils.isNotEmpty(queryList)) {
            Optional<OrdDirDelivDifference> anyOptional = queryList.stream().filter(ordDirDelivDifference -> !DifferenceOrderStatusEnum.INVALID.getCode().equals(ordDirDelivDifference.getDifferenceStatus())).findAny();
            if (anyOptional.isPresent()) {
                throw new BusinessException("有关联的配货差异单还未冲销，请先处理差异单");
            }
        }
        if (!DeliveryOrderEnum.RECEIVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.SHIPPED.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
            throw new BusinessException("只有已收货和已发货状态可冲销");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDirDelivery.getIsReversal())) {
            throw new BusinessException("此单据已被红冲不能重复生成红冲单");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDirDelivery.getIsReversalOrder())) {
            throw new BusinessException("红冲单不能被红冲");
        }
        //校验门店库存
        String message = this.checkInvRed(ordDirDelivery, chargeDirDeliveryOrderIn.getDetailList());
        if (StringUtil.isNotEmpty(message)) {
            throw new BusinessException(message);
        }

        // 保存红冲配货单
        OrdDirDelivery dirDelivery = new OrdDirDelivery();
        BeanUtils.copy(ordDirDelivery, dirDelivery);
        dirDelivery.setOrderQuantity(Objects.isNull(ordDirDelivery.getOrderQuantity()) ? BigDecimal.ZERO : ordDirDelivery.getOrderQuantity().negate());
        dirDelivery.setOrderAmount(Objects.isNull(ordDirDelivery.getOrderAmount()) ? BigDecimal.ZERO : ordDirDelivery.getOrderAmount().negate());
        dirDelivery.setDistributionQuantity(Objects.isNull(ordDirDelivery.getDistributionQuantity()) ? BigDecimal.ZERO : ordDirDelivery.getDistributionQuantity().negate());
        dirDelivery.setDistributionAmount(Objects.isNull(ordDirDelivery.getDistributionAmount()) ? BigDecimal.ZERO : ordDirDelivery.getDistributionAmount().negate());
        dirDelivery.setDeliveryQuantity(Objects.isNull(ordDirDelivery.getDeliveryQuantity()) ? BigDecimal.ZERO : ordDirDelivery.getDeliveryQuantity().negate());
        dirDelivery.setDeliveryAmount(Objects.isNull(ordDirDelivery.getDeliveryAmount()) ? BigDecimal.ZERO : ordDirDelivery.getDeliveryAmount().negate());

        dirDelivery.setId(null);
        dirDelivery.setIsReversal(NumberUtil.INTEGER_ZERO);
        dirDelivery.setIsReversalOrder(NumberUtil.INTEGER_ONE);
        dirDelivery.setDeliveryTime(LocalDateTime.now());
        dirDelivery.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
        dirDelivery.setUpdateTime(LocalDateTime.now());
        dirDelivery.setCreateTime(LocalDateTime.now());
        // 配货红冲单号
        dirDelivery.setDeliveryOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PH.getCode(), ordDirDelivery.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR));
        //新增配货红冲单
        int count = this.saveChargeDeliveryOrder(dirDelivery, chargeDirDeliveryOrderIn.getDetailList());

        //库存调整，修改bug 冲销单发库存传冲销单的业务单号
        this.adjustInvRedFlush(dirDelivery, chargeDirDeliveryOrderIn.getDetailList(), stockInfoOut);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(),
                String.valueOf(dirDelivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(),
                OperateLogTypeEnum.SAVE.getName(), new Date(),
                dirDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        //修改原配货单
        this.updateDeliverOrder(ordDirDelivery);
        return Response.data(count);
    }

    @Override
    public void checkDeliveryAmountSimilarity(String createTimeBegin, String createTimeEnd, String bizOrgCode) {
        Example example = new Example(OrdDirDelivery.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andBetween("createTime", createTimeBegin, createTimeEnd);
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(DeliveryOrderEnum.PENDING.getKey());
        orderStatusCodeList.add(DeliveryOrderEnum.PREVIEWAPPROVED.getKey());
        orderStatusCodeList.add(DeliveryOrderEnum.APPROVED.getKey());
        criteria.andIn("deliveryStatusCode", orderStatusCodeList);
        criteria.andEqualTo("bizOrgCode", bizOrgCode);
        criteria.andEqualTo("isDelete", ModelConst.DELETE.NO);
        List<OrdDirDelivery> ordDirDeliveryList = ordDirDeliveryMapper.selectByExample(example);
        for (OrdDirDelivery ordDisDelivery : ordDirDeliveryList) {
            StoreUnit storeUnit = storeCenterService.getStoreUnitByStoreCode(ordDisDelivery.getStoreCode(), bizOrgCode);
            String unitStr = "";
            if (storeUnit != null) {
                unitStr = "【" + storeUnit.getUnitCode() + "】" + storeUnit.getUnitName() + SystemConstant.SHORT_LINE;
            }
            WarningResultOut skuSimilarityWarningResultOut = this.checkOrderAmount(ordDisDelivery, unitStr, OrderWarningTypeEnum.DELIVERY_ORDER_LIMIT_AMOUNT.getErrorMessage());
            if (Boolean.TRUE.equals(skuSimilarityWarningResultOut.getCheckFlag())) {
                warningService.pushWarningMessage(WarningBusinessTypeEnum.DIR_DELIVERY_ORDER.getBusinessType(), OrderWarningTypeEnum.DELIVERY_ORDER_LIMIT_AMOUNT.getType(),
                        skuSimilarityWarningResultOut.getErrorMessage(), null, null, bizOrgCode);
            }
        }
    }

    @Override
    public List<DirNoAuditDeliveryOrderInfoOut> findNoAuditDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(DeliveryOrderEnum.PENDING.getKey());
        orderStatusCodeList.add(DeliveryOrderEnum.PREVIEWAPPROVED.getKey());
        return ordDirDeliveryMapper.findNoAuditDeliveryOrderList(createTimeBegin, createTimeEnd, orderStatusCodeList, bizOrgCode);
    }

    @Override
    public List<DirDeliveryOrderPrintOut> findPrintDataByIds(List<Long> ids) {
        // 仓位
        Map<String, StockInfoOut> stockMap = stockServer.findAll(UserUtil.getBizOrgCode());
        List<DirDeliveryOrderPrintOut> printOutList = new ArrayList<>();
        for (Long id : ids) {
            DirDeliveryOrderPrintOut orderPrintOut = new DirDeliveryOrderPrintOut();
            OrdDirDelivery ordDirDelivery = selectByPrimaryKey(id);
            if (ordDirDelivery == null) {
                throw new BusinessException("无效的配销单id！");
            }
            BeanUtils.copy(ordDirDelivery, orderPrintOut);
            StockInfoOut stockInfoOut = stockMap.get(orderPrintOut.getStockCode());
            if (stockInfoOut != null) {
                orderPrintOut.setStockName(stockInfoOut.getStockName());
            }
            orderPrintOut.setDeliveryQuantity(Objects.isNull(orderPrintOut.getDeliveryQuantity()) ? BigDecimal.ZERO : orderPrintOut.getDeliveryQuantity());
            orderPrintOut.setDeliveryStatusName(systemDictService.getSystemDictName(orderPrintOut.getDeliveryStatusCode()));
            List<DirDeliveryOrderDtlPrintOut> dtlPrintOutList = ordDirDeliveryDetailService.findPrintDtlByDeliveryId(id);
            // 实配包装数合计
//            BigDecimal deliveryPackQuantity = dtlPrintOutList.stream().map(item -> Objects.isNull(item.getDeliveryPackageQuantity()) ? BigDecimal.ZERO : item.getDeliveryPackageQuantity()).reduce(BigDecimal.ZERO, BigDecimal::add);
            AtomicReference<BigDecimal> orderQuantity = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> orderPackQuantity = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> deliveryPackQuantity = new AtomicReference<>(BigDecimal.ZERO);
            dtlPrintOutList.forEach(detail -> {
                if (Objects.nonNull(detail.getOrderPackageQuantity())) {
                    orderPackQuantity.getAndSet(orderPackQuantity.get().add(detail.getOrderPackageQuantity()));
                }
                if (Objects.nonNull(detail.getDeliveryPackageQuantity())) {
                    orderQuantity.getAndSet(orderQuantity.get().add(detail.getOrderQuantity()));
                    deliveryPackQuantity.getAndSet(deliveryPackQuantity.get().add(detail.getDeliveryPackageQuantity()));
                }
            });
            orderPrintOut.setDeliveryPackQuantity(deliveryPackQuantity.get());
            orderPrintOut.setOrderQuantity(orderQuantity.get());
            orderPrintOut.setOrderPackQuantity(orderPackQuantity.get());
            orderPrintOut.setDtlPrintOuts(dtlPrintOutList);
            OrdDirDeliveryOrderSigning orderSigning = ordDirDeliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(id, ordDirDelivery.getBizOrgCode());
            if (Objects.nonNull(orderSigning)) {
                orderPrintOut.setSignature(orderSigning.getSignature());
            }
            printOutList.add(orderPrintOut);
        }
        return printOutList;
    }

    @Override
    public Response<String> asyncImportDelivery(String fileId, String loginUsername, String loginBizOrgCode) {
        String key = DirSystemConstant.CHECK_DIR_ORDER_DELIVERY_IMPORT_ONLY_ONE + loginBizOrgCode;
        if (!redisService.setIfAbsent(key, loginUsername, 10L, TimeUnit.MINUTES)) {
            return Response.error("当前有用户正在异步上传配货单，请稍后尝试");
        }
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            redisService.del(key);
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DirDeliveryAsyncImportListener listener = new DirDeliveryAsyncImportListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportDirDeliveryOrder.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > 0) {
            redisService.del(key);
            return Response.error(listener.getImportErrorMessage(totalErrorMap));
        }
        dirDeliveryImportHandle.handleDeliveryListAsyncImport(listener.getDeliveryOrders(), loginUsername, loginBizOrgCode, "", key);
        return Response.success("开始导入，请耐心等待，切勿重复操作！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveAsyncImportDeliveryOrder(List<OrdDirDeliveryIn> ordDirDeliveryInList) {
        List<Long> deliveryOrderIdList = Lists.newArrayList();
        ordDirDeliveryInList.forEach(ordDirDeliveryIn -> {
            Long orderId = this.saveOrUpdate(ordDirDeliveryIn);
            deliveryOrderIdList.add(orderId);
        });
        return deliveryOrderIdList;
    }

    @Override
    public List<OrdDirDeliveryIn> initDeliveryOrderAndDetailForAsyncImport(List<ImportDirDeliveryOrder> deliveryOrderList,
                                                                           Map<String, StoreInfo> storeInfoMap, String loginUsername) {
        Map<String, List<ImportDirDeliveryOrder>> map = deliveryOrderList.stream().collect(Collectors.groupingBy(item -> item.getWrhCode() + "_" + item.getStockCode() + "_" + item.getStoreCode()));
        List<OrdDirDeliveryIn> ordDirDeliveryInList = Lists.newArrayList();
        map.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            OrdDirDeliveryIn ordDisDeliveryIn = new OrdDirDeliveryIn();
            ImportDirDeliveryOrder importDirDeliveryOrder = v.get(NumberUtil.INTEGER_ZERO);
            StoreInfo storeInfo = storeInfoMap.get(importDirDeliveryOrder.getStoreCode());
            String bizOrgCode = storeInfo.getBizOrgCode();
            ordDisDeliveryIn.setStoreCode(importDirDeliveryOrder.getStoreCode());
            ordDisDeliveryIn.setStoreName(Objects.isNull(storeInfo) ? "" : storeInfo.getStoreName());
            ordDisDeliveryIn.setStockCode(importDirDeliveryOrder.getStockCode());
            ordDisDeliveryIn.setWrhCode(importDirDeliveryOrder.getWrhCode());
            ordDisDeliveryIn.setBizOrgCode(bizOrgCode);
            ordDisDeliveryIn.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(bizOrgCode));
            ordDisDeliveryIn.setDistributionType(importDirDeliveryOrder.getDistributionType());
            ordDisDeliveryIn.setRemark(importDirDeliveryOrder.getRemark());
            ordDisDeliveryIn.setCreator(loginUsername);
            ordDisDeliveryIn.setUpdater(loginUsername);
            List<OrdDirDeliveryDetail> ordDisDeliveryDetails = new ArrayList<>();
            for (ImportDirDeliveryOrder disDeliveryOrder : v) {
                OrdDirDeliveryDetail ordDisDeliveryDetail = this.initOrdDirDeliveryDetailByAsyncImport(bizOrgCode, disDeliveryOrder, importDirDeliveryOrder.getDistributionType());
                if (Objects.isNull(ordDisDeliveryDetail)) {
                    continue;
                }
                ordDisDeliveryDetails.add(ordDisDeliveryDetail);
            }
            ordDisDeliveryIn.setDetailList(ordDisDeliveryDetails);
            ordDisDeliveryIn.setSourceCode(DeliveryOrderSourceCodeEnum.MANUAL.getType());
            ordDisDeliveryIn.setAuditType(SalvageAuditTypeEnum.WAIT_MANUAL_AUDIT.getCode());
            ordDirDeliveryInList.add(ordDisDeliveryIn);
        });
        return ordDirDeliveryInList;
    }

    private OrdDirDeliveryDetail initOrdDirDeliveryDetailByAsyncImport(String bizOrgCode, ImportDirDeliveryOrder disDeliveryOrder, String distributionType) {
        OrdDirDeliveryDetail ordDisDeliveryDetail = new OrdDirDeliveryDetail();
        DeliveryIn deliveryIn = new DeliveryIn();
        deliveryIn.setGoodsCode(disDeliveryOrder.getGoodsCode());
        deliveryIn.setBizOrgCode(bizOrgCode);
        deliveryIn.setStockCode(disDeliveryOrder.getStockCode());
        deliveryIn.setStoreCode(disDeliveryOrder.getStoreCode());
        deliveryIn.setWrhCode(disDeliveryOrder.getWrhCode());
        deliveryIn.setDistributionType(distributionType);
        OrdDirDeliveryDetailOut detailOut = this.checkOrderGoodsByAsyncImport(deliveryIn, disDeliveryOrder.getCenterStockBizOrgCode());
        if (Objects.isNull(detailOut)) {
            return null;
        }
        BeanUtils.copy(detailOut, ordDisDeliveryDetail);
        ordDisDeliveryDetail.setGoodsCode(disDeliveryOrder.getGoodsCode());
        ordDisDeliveryDetail.setOrderUnitPrice(ordDisDeliveryDetail.getDistributionUnitPrice());
        ordDisDeliveryDetail.setOrderQuantity(new BigDecimal(disDeliveryOrder.getDeliveryQuantity()));
        BigDecimal packageQty = ordDisDeliveryDetail.getOrderQuantity().divide(ordDisDeliveryDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_ZERO, RoundingMode.DOWN);
        ordDisDeliveryDetail.setOrderPackageQuantity(packageQty);
        ordDisDeliveryDetail.setOrderAmount(ordDisDeliveryDetail.getOrderUnitPrice().multiply(ordDisDeliveryDetail.getOrderQuantity()));

        BigDecimal sellTax = null == ordDisDeliveryDetail.getSellTax() ? BigDecimal.ZERO : ordDisDeliveryDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
        BigDecimal tax = sellTax.add(BigDecimal.ONE);
        ordDisDeliveryDetail.setDistributionExceptTaxAmount(ordDisDeliveryDetail.getOrderAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        ordDisDeliveryDetail.setDistributionTaxAmount(ordDisDeliveryDetail.getOrderAmount().subtract(ordDisDeliveryDetail.getDistributionExceptTaxAmount()));
        ordDisDeliveryDetail.setWrhCostAmount(ordDisDeliveryDetail.getWrhPrice().multiply(ordDisDeliveryDetail.getOrderQuantity()));
        ordDisDeliveryDetail.setWrhExceptTaxAmount(ordDisDeliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        ordDisDeliveryDetail.setWrhTaxAmount(ordDisDeliveryDetail.getWrhCostAmount().subtract(ordDisDeliveryDetail.getWrhExceptTaxAmount()));
        ordDisDeliveryDetail.setStoreCostAmount(ordDisDeliveryDetail.getStoreStockPrice().multiply(ordDisDeliveryDetail.getOrderQuantity()));
        ordDisDeliveryDetail.setStoreExceptTaxAmount(ordDisDeliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
        ordDisDeliveryDetail.setStoreTaxAmount(ordDisDeliveryDetail.getStoreCostAmount().subtract(ordDisDeliveryDetail.getStoreExceptTaxAmount()));
        ordDisDeliveryDetail.setExpiry(disDeliveryOrder.getExpiry());
        ordDisDeliveryDetail.setPurchaseNo(disDeliveryOrder.getPurchaseNo());
        return ordDisDeliveryDetail;
    }

    private OrdDirDeliveryDetailOut checkOrderGoodsByAsyncImport(DeliveryIn deliveryIn, String centerStockBizOrgCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBizOrgCode(deliveryIn.getBizOrgCode());
        orderGoodsIn.setGoodsCode(deliveryIn.getGoodsCode());
        orderGoodsIn.setStoreCode(deliveryIn.getStoreCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DISTRIBUTION_BILL.getType());
        OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(storeOrderGoods)) {
            log.error("门店{}配货单异步导入明细{}不存在", deliveryIn.getStoreCode(), deliveryIn.getGoodsCode());
            return null;
        }
        OrdDirDeliveryDetailOut ordDirDeliveryDetail = new OrdDirDeliveryDetailOut();
        ordDirDeliveryDetail.setGoodsCode(storeOrderGoods.getGoodsCode());
        ordDirDeliveryDetail.setGoodsName(storeOrderGoods.getGoodsName());
        ordDirDeliveryDetail.setBarCode(storeOrderGoods.getBarCode());
        ordDirDeliveryDetail.setGoodsType(storeOrderGoods.getGoodsType());
        ordDirDeliveryDetail.setDistributionSpecification(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? "" : storeOrderGoods.getDistributionSpecification().getQpcStr());
        ordDirDeliveryDetail.setDistributionSpecificationUnit(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? "" : storeOrderGoods.getDistributionSpecification().getUnitName());
        ordDirDeliveryDetail.setDistributionSpecificationNum(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : BigDecimal.valueOf(storeOrderGoods.getDistributionSpecification().getQpc()));
        ordDirDeliveryDetail.setSellTax(storeOrderGoods.getOutTax());
        ordDirDeliveryDetail.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(ordDirDeliveryDetail.getGoodsType()));
        ordDirDeliveryDetail.setDistributionPrice(Objects.isNull(storeOrderGoods.getDistributionPrice()) ? BigDecimal.ZERO : storeOrderGoods.getDistributionPrice());
        ordDirDeliveryDetail.setDistributionUnitPrice(Objects.isNull(storeOrderGoods.getDistributionUnitPrice()) ? BigDecimal.ZERO : storeOrderGoods.getDistributionUnitPrice());
        ordDirDeliveryDetail.setDistributionSpecificationNum(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : new BigDecimal(storeOrderGoods.getDistributionSpecification().getQpc()));
        ordDirDeliveryDetail.setSmallSort(storeOrderGoods.getSort());
        ordDirDeliveryDetail.setSortName(storeOrderGoods.getSortName());
        ordDirDeliveryDetail.setDistributionType(DistributionWaysEnum.getTypeByName(storeOrderGoods.getDistributionWay()));
        ordDirDeliveryDetail.setDistributionTypeValue(storeOrderGoods.getDistributionWay());
        ordDirDeliveryDetail.setIsGift(NumberUtil.INTEGER_ZERO);
        ordDirDeliveryDetail.setVendorCode(storeOrderGoods.getVendorCode());
        BigDecimal stockStorePrice = warehouseServer.getStockPrice(deliveryIn.getStoreCode(), deliveryIn.getGoodsCode(), deliveryIn.getBizOrgCode());
        ordDirDeliveryDetail.setStoreStockPrice(Objects.isNull(stockStorePrice) ? BigDecimal.ZERO : stockStorePrice);
        BigDecimal stockWarehousePrice = warehouseServer.getWarehousePrice(deliveryIn.getWrhCode(), deliveryIn.getStockCode(), deliveryIn.getGoodsCode(),
                centerStockBizOrgCode, ordDirDeliveryDetail.getVendorCode());
        ordDirDeliveryDetail.setWrhPrice(Objects.isNull(stockWarehousePrice) ? BigDecimal.ZERO : stockWarehousePrice);
        ordDirDeliveryDetail.setInvoiceType(storeOrderGoods.getInvoiceType());
        ordDirDeliveryDetail.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(ordDirDeliveryDetail.getInvoiceType()));
        return ordDirDeliveryDetail;
    }


    /**
     * 校验配货单金额
     *
     * @param targetOrder
     * @param unitStr
     * @param msgTemplate
     * @return
     */
    private WarningResultOut checkOrderAmount(OrdDirDelivery targetOrder, String unitStr, String msgTemplate) {
        boolean flag = false;
        WarningResultOut warningResultOut = new WarningResultOut();
        if (targetOrder.getOrderAmount().compareTo(new BigDecimal("5000")) < 0) {
            warningResultOut.setCheckFlag(flag);
            return warningResultOut;
        }
        // 对比该门店配货单金额超当天前30笔订单（所有有效订单（不含已作废状态）平均金额的1倍
        List<BigDecimal> requestOrderTotalAmountList = this.findThirtyDaysDeliveryOrderTotalAmountByStoreCode(targetOrder.getStoreCode(), targetOrder.getBizOrgCode(), targetOrder.getStockCode());
        String errorMessage = null;
        if (CollectionUtils.isNotEmpty(requestOrderTotalAmountList)) {
            BigDecimal sumPaidAmount = requestOrderTotalAmountList.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            // 金额对比值
            BigDecimal contrastAmount = sumPaidAmount.divide(new BigDecimal(requestOrderTotalAmountList.size()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN).multiply(BigDecimal.valueOf(NumberUtils.INTEGER_TWO));
            if (targetOrder.getOrderAmount().compareTo(contrastAmount) >= 0) {
                errorMessage = MessageFormat.format(msgTemplate, StoreOrderWarningTypeEnum.DIRECTLY.getName(), WarningBusinessTypeEnum.DIR_DELIVERY_ORDER.getName(), unitStr + targetOrder.getStoreCode()
                        , targetOrder.getDeliveryOrderNo(), targetOrder.getOrderAmount());
                log.info(errorMessage);
                log.info("门店{}配货单单号：{}即将发送金额过大预警", targetOrder.getStoreCode(), targetOrder.getDeliveryOrderNo());
                flag = true;
            }
        }
        warningResultOut.setCheckFlag(flag);
        warningResultOut.setErrorMessage(errorMessage);
        return warningResultOut;
    }

    private List<BigDecimal> findThirtyDaysDeliveryOrderTotalAmountByStoreCode(String storeCode, String bizOrgCode, String stockCode) {
        return ordDirDeliveryMapper.findThirtyDaysDeliveryOrderTotalAmountByStoreCode(storeCode, bizOrgCode, stockCode, DeliveryOrderEnum.INVALID.getKey());
    }

    private String checkInvRed(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> detailList) {
        StringJoiner error = new StringJoiner(",");
        //校验门店库存
        this.checkInvCharge(detailList, error, ordDirDelivery.getBizOrgCode(), ordDirDelivery.getStoreCode());
        if (error.length() == NumberUtil.INTEGER_ZERO) {
            return "";
        }
        return error.toString();
    }

    /**
     * 冲销调整库存
     *
     * @param ordDirDelivery
     * @param detailList
     */
    private void adjustInvRedFlush(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> detailList, StockInfoOut stockInfoOut) {
        //门店库存调整
        StockFlowIn storeFlowIn = this.initStoreStockRedFlush(ordDirDelivery, detailList, stockInfoOut);
        //异步调仓储库存
        StockFlowIn wareFlowIn = this.initWarehouseStockRedFlush(ordDirDelivery, detailList, stockInfoOut);
        List<StockFlowIn> stockFlowInList = Arrays.asList(storeFlowIn, wareFlowIn);
        Response response = stockFlowService.checkStockFlow(stockFlowInList);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
    }

    /**
     * 冲销 仓储库存调整
     *
     * @param ordDirDelivery
     * @param detailList
     * @return
     */
    private StockFlowIn initWarehouseStockRedFlush(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> detailList, StockInfoOut stockInfoOut) {
        //作废配货单，对总部业务占用库存进行冲单，取配货数量字段
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_OUT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_OUT.getName());
        stockFlowIn.setFlowDate(ordDirDelivery.getDeliveryTime());
        stockFlowIn.setCreator(ordDirDelivery.getCreator());
        stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        stockFlowIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        detailList.forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity())) {
                return;
            }
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            //配货单-冲销仓储库存调整发生价取原单仓储库存价
            stockFlowGoodsIn.setPrice(item.getWrhPrice());
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDirDelivery.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            // 实际增/减
            stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.WAREHOUSE.getCode());
            // 实际数
            stockFlowGoodsIn.setActualQty(item.getDeliveryQuantity().abs());
            //成本含税金额
            stockFlowGoodsIn.setCostTaxAmount(item.getWrhCostAmount().abs());
            //成本不含税金额
            stockFlowGoodsIn.setCostNonTaxAmount(item.getWrhExceptTaxAmount().abs());
            //成本税额
            stockFlowGoodsIn.setCostTax(item.getWrhTaxAmount().abs());
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(item.getDistributionExceptTaxAmount().abs());
            //含税金额
            stockFlowGoodsIn.setTaxAmount(item.getDeliveryAmount().abs());
            // 税额
            stockFlowGoodsIn.setTax(item.getDistributionTaxAmount().abs());
            //单号
            stockFlowGoodsIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
            stockFlowGoodsIns.add(stockFlowGoodsIn);

        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    /**
     * 冲销 门店库存调整
     *
     * @param ordDirDelivery
     * @param detailList
     * @return
     */
    private StockFlowIn initStoreStockRedFlush(OrdDirDelivery ordDirDelivery, List<OrdDirDeliveryDetail> detailList, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.ALLOCATION_OUT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.ALLOCATION_OUT.getName());
        stockFlowIn.setBizOrgCode(ordDirDelivery.getBizOrgCode());
        stockFlowIn.setFlowDate(ordDirDelivery.getDeliveryTime());
        stockFlowIn.setCreator(ordDirDelivery.getCreator());
        stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        stockFlowIn.setOrgCode(ordDirDelivery.getOrgCode());
        stockFlowIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        detailList.forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity())) {
                return;
            }
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            //收货后冲销，门店库存减少
            if (DeliveryOrderEnum.RECEIVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
                //实际数
                stockFlowGoodsIn.setActualQty(item.getDeliveryQuantity().abs());
                //实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            }
            //发货后冲销 门店库存占用，
            if (DeliveryOrderEnum.SHIPPED.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
                //实际数
                stockFlowGoodsIn.setActualQty(item.getDeliveryQuantity().abs());
                //实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
                //申请数
                stockFlowGoodsIn.setApplyQty(item.getDeliveryQuantity().abs());
                //申请增/减
                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
            }
            if (StringUtils.isNotBlank(item.getExpiry())) {
                stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(item.getExpiry(), stockFlowGoodsIn.getActualQty())));
            }
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDirDelivery.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            stockFlowGoodsIn.setStoreCode(ordDirDelivery.getStoreCode());
            stockFlowGoodsIn.setStoreName(ordDirDelivery.getStoreName());
            //配货单-冲销门店库存调整发生价取原单门店配销价
            stockFlowGoodsIn.setPrice(item.getDistributionUnitPrice());
            //库存发生位置
            stockFlowGoodsIn.setPosition(StockHappenLieEnum.STORE.getCode());
            //成本含税金额
            stockFlowGoodsIn.setCostTaxAmount(item.getStoreCostAmount());
            //成本不含税金额
            stockFlowGoodsIn.setCostNonTaxAmount(item.getStoreExceptTaxAmount());
            //成本税额
            stockFlowGoodsIn.setCostTax(item.getStoreTaxAmount());
            //不含税金额
            stockFlowGoodsIn.setNonTaxAmount(item.getDistributionExceptTaxAmount());
            //含税金额
            stockFlowGoodsIn.setTaxAmount(item.getDeliveryAmount());
            // 税额
            stockFlowGoodsIn.setTax(item.getDistributionTaxAmount());
            //单号
            stockFlowGoodsIn.setSourceNo(ordDirDelivery.getDeliveryOrderNo());
            stockFlowGoodsIns.add(stockFlowGoodsIn);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    @Override
    public DirDeliveryOrderOut getDeliveryOrderTotal(DirDeliveryOrderIn deliveryOrderIn) {
        DirDeliveryOrderOut dirDeliveryOrderOut = new DirDeliveryOrderOut();
        //如果为空 默认给0
        dirDeliveryOrderOut.setDeliveryAmount(BigDecimal.ZERO);
        dirDeliveryOrderOut.setDeliveryQuantity(BigDecimal.ZERO);
        dirDeliveryOrderOut.setDistributionAmount(BigDecimal.ZERO);
        dirDeliveryOrderOut.setDistributionQuantity(BigDecimal.ZERO);
        dirDeliveryOrderOut.setTotalArrivalQuantity(BigDecimal.ZERO);
        dirDeliveryOrderOut.setTotalArrivalAmount(BigDecimal.ZERO);
        String loginBizOrgCode = deliveryOrderIn.getBizOrgCode();
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(loginBizOrgCode);
        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !loginBizOrgCode.equals(stockInfoOut.getBizOrgCode())).findFirst();
        if (!anyOneOptional.isPresent()) {
            deliveryOrderIn.setBizOrgCode("");
        }
        if (CollectionUtils.isEmpty(deliveryOrderIn.getStockCodeList())) {
            List<String> stockCodeList = authOrgStockMap.keySet().stream().collect(Collectors.toList());
            deliveryOrderIn.setStockCodeList(stockCodeList);
        } else {
            // 校验业务仓位是否允许操作
            storeChannelHandle.checkStockCodeListISAuth(deliveryOrderIn.getStockCodeList(), authOrgStockMap);
        }
        if (StringUtils.isNotBlank(deliveryOrderIn.getStoreArea())) {
            List<String> storeCodeList = new ArrayList<>();
            List<StoreInfoOut> storeList = storeCenterService.getStoreInfoByCode(new StoreInfoIn(deliveryOrderIn.getStoreArea()));
            if (CollectionUtils.isEmpty(storeList)) {
                return dirDeliveryOrderOut;
            }
            storeList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            deliveryOrderIn.setStoreCodeList(storeCodeList);
        }
        if (StringUtils.isNotBlank(deliveryOrderIn.getDifferenceOrderNo())) {
            String deliveryNo = ordDirDelivDifferenceService.getDeliveryNoByDiffNo(deliveryOrderIn.getDifferenceOrderNo(), null);
            if (StringUtils.isBlank(deliveryNo)) {
                return dirDeliveryOrderOut;
            }
            deliveryOrderIn.setDeliveryOrderNo(deliveryNo);
        }
        //结果集
//        DirDeliveryOrderOut result = ordDirDeliveryMapper.getDeliveryOrderTotal(deliveryOrderIn);
//        if (Objects.isNull(result)) {
//            return dirDeliveryOrderOut;
//        }
        List<Long> sumDeliveryDataIdList = ordDirDeliveryMapper.findSumDeliveryDataIdList(deliveryOrderIn);
        if (CollectionUtils.isNotEmpty(sumDeliveryDataIdList)) {
            SumDirDeliveryOrderDataOut sumDisDeliveryOrderDataOut = ordDirDeliveryMapper.sumDeliveryData(deliveryOrderIn);
            if (Objects.nonNull(sumDisDeliveryOrderDataOut)) {
                dirDeliveryOrderOut.setOrderAmount(sumDisDeliveryOrderDataOut.getOrderAmount());
                dirDeliveryOrderOut.setOrderQuantity(sumDisDeliveryOrderDataOut.getOrderQuantity());
                dirDeliveryOrderOut.setDistributionAmount(sumDisDeliveryOrderDataOut.getDistributionAmount());
                dirDeliveryOrderOut.setDistributionQuantity(sumDisDeliveryOrderDataOut.getDistributionQuantity());
                dirDeliveryOrderOut.setDeliveryAmount(sumDisDeliveryOrderDataOut.getDeliveryAmount());
                dirDeliveryOrderOut.setDeliveryQuantity(sumDisDeliveryOrderDataOut.getDeliveryQuantity());
            }
            DirDeliveryOrderArrivalDataOut deliveryOrderArrivalDataOut = ordDirDeliveryDetailService.sumArrivalDataByDeliveryOrderIdList(sumDeliveryDataIdList);
            if (Objects.nonNull(deliveryOrderArrivalDataOut)) {
                dirDeliveryOrderOut.setTotalArrivalAmount(deliveryOrderArrivalDataOut.getTotalArrivalAmount());
                dirDeliveryOrderOut.setTotalArrivalQuantity(deliveryOrderArrivalDataOut.getTotalArrivalQuantity());
            }
        }
        return dirDeliveryOrderOut;
    }


    /**
     * 查询配货单列表(库存盘点)
     *
     * @param deliveryOrderIn
     * @return
     */
    @Override
    public List<DirDeliveryOrderOut> findDirDeliveryOrder(DirDeliveryOrderIn deliveryOrderIn) {
        StringJoiner sj = new StringJoiner(",");
        sj.add(DeliveryOrderEnum.APPROVED.getKey());
        sj.add(DeliveryOrderEnum.SHIPPED.getKey());
        deliveryOrderIn.setDeliveryStatusCode(sj.toString());
        List<DirDeliveryOrderOut> dirDeliveryOrderOutList = ordDirDeliveryMapper.findDirDeliveryOrder(deliveryOrderIn);
        dirDeliveryOrderOutList.forEach(item -> item.setDeliveryStatusValue(DeliveryOrderEnum.getValueByKey(item.getDeliveryStatusCode())));
        return dirDeliveryOrderOutList;
    }

    /**
     * 冲销修改
     *
     * @param ordDirDelivery
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliverOrder(OrdDirDelivery ordDirDelivery) {
        ordDirDelivery.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        ordDirDelivery.setIsReversal(NumberUtil.INTEGER_ONE);
        this.updateByPrimaryKey(ordDirDelivery);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(),
                String.valueOf(ordDirDelivery.getId()),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(),
                OrdLogTypeEnum.DIR_DELIVERY_ORDER_CHARGE.getName(), new Date(),
                ordDirDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 保存红冲单信息
     *
     * @param query
     * @param detailList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int saveChargeDeliveryOrder(OrdDirDelivery query, List<OrdDirDeliveryDetail> detailList) {
        String orderPriority = query.getOrderPriority();
        if (Objects.isNull(orderPriority)) {
            orderPriority = storeCenterService.getOrderPriorityByStoreCode(query.getStoreCode(), query.getBizOrgCode(), query.getStockCode(), query.getDistributionType());
            query.setOrderPriority(orderPriority);
        }
        int count = this.insertSelective(query);
        detailList.forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity())) {
                return;
            }
            item.setDeliveryOrderId(query.getId());
            item.setDeliveryQuantity(item.getDeliveryQuantity().negate());
            item.setArrivalQuantity(Objects.isNull(item.getArrivalQuantity()) ? BigDecimal.ZERO : item.getArrivalQuantity().negate());
            item.setOrderQuantity(Objects.isNull(item.getOrderQuantity()) ? BigDecimal.ZERO : item.getOrderQuantity().negate());
            item.setOrderAmount(Objects.isNull(item.getOrderAmount()) ? BigDecimal.ZERO : item.getOrderAmount().negate());
            item.setDistributionQuantity(Objects.isNull(item.getDistributionQuantity()) ? BigDecimal.ZERO : item.getDistributionQuantity().negate());
            item.setDistributionAmount(Objects.isNull(item.getDistributionAmount()) ? BigDecimal.ZERO : item.getDistributionAmount().negate());
            item.setDeliveryAmount(Objects.isNull(item.getDeliveryAmount()) ? BigDecimal.ZERO : item.getDeliveryAmount().negate());
            item.setArrivalAmount(Objects.isNull(item.getArrivalAmount()) ? BigDecimal.ZERO : item.getArrivalAmount().negate());
            item.setDistributionExceptTaxAmount(Objects.isNull(item.getDistributionExceptTaxAmount()) ? BigDecimal.ZERO : item.getDistributionExceptTaxAmount().negate());
            item.setDistributionTaxAmount(Objects.isNull(item.getDistributionTaxAmount()) ? BigDecimal.ZERO : item.getDistributionTaxAmount().negate());
            item.setWrhCostAmount(Objects.isNull(item.getWrhCostAmount()) ? BigDecimal.ZERO : item.getWrhCostAmount().negate());
            item.setWrhExceptTaxAmount(Objects.isNull(item.getWrhExceptTaxAmount()) ? BigDecimal.ZERO : item.getWrhExceptTaxAmount().negate());
            item.setWrhTaxAmount(Objects.isNull(item.getWrhTaxAmount()) ? BigDecimal.ZERO : item.getWrhTaxAmount().negate());
            item.setStoreTaxAmount(Objects.isNull(item.getStoreTaxAmount()) ? BigDecimal.ZERO : item.getStoreTaxAmount().negate());
            item.setStoreExceptTaxAmount(Objects.isNull(item.getStoreExceptTaxAmount()) ? BigDecimal.ZERO : item.getStoreExceptTaxAmount().negate());
            item.setStoreCostAmount(Objects.isNull(item.getStoreCostAmount()) ? BigDecimal.ZERO : item.getStoreCostAmount().negate());
            item.setCreateTime(LocalDateTime.now());
        });
        ordDirDeliveryDetailService.save(detailList, query, orderPriority);
        return count;
    }

    private List<ExcelDirDeliveryOrder> parseOrderDataToExcel(List<DirDeliveryOrderOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertOrderExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    private ExcelDirDeliveryOrder convertOrderExcel(DirDeliveryOrderOut dirDeliveryOrderOut, int index) {
        ExcelDirDeliveryOrder excelDeliveryOrder = new ExcelDirDeliveryOrder();
        BeanUtils.copy(dirDeliveryOrderOut, excelDeliveryOrder);
        excelDeliveryOrder.setIsReversal(NumberUtil.INTEGER_ZERO.equals(dirDeliveryOrderOut.getIsReversal()) ? "否" : "是");
        excelDeliveryOrder.setIsReversalOrder(NumberUtil.INTEGER_ZERO.equals(dirDeliveryOrderOut.getIsReversalOrder()) ? "否" : "是");
        // 仓位名称
        StockTransInfoOut stockOut = orderGoodsServer.getTransInfo(dirDeliveryOrderOut.getStockCode(), dirDeliveryOrderOut.getBizOrgCode());
        excelDeliveryOrder.setStockCode(Objects.nonNull(stockOut) ? stockOut.getStockName() + "【" + stockOut.getStockCode() + "】" : "");

        excelDeliveryOrder.setIndex(index + 1);
        return excelDeliveryOrder;
    }

    /**
     * 解析数据到excel
     *
     * @param list
     * @return
     */
    private List<ExcelDeliveryOrderDetails> parseDataToExcel(List<DirDeliveryOrderDetailsOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将DeliveryOrderDetailsOut转换为导出ExcelStoreSkuBaseValue
     *
     * @param dirDeliveryOrderDetailsOut
     * @return ExcelStoreSkuBaseValue
     */
    private ExcelDeliveryOrderDetails convertExcel(DirDeliveryOrderDetailsOut dirDeliveryOrderDetailsOut, int index) {
        ExcelDeliveryOrderDetails excelDeliveryOrderDetails = new ExcelDeliveryOrderDetails();
        BeanUtils.copy(dirDeliveryOrderDetailsOut, excelDeliveryOrderDetails);
        if (Objects.nonNull(dirDeliveryOrderDetailsOut.getArrivalQuantity()) && Objects.nonNull(dirDeliveryOrderDetailsOut.getOrderUnitPrice())) {
            excelDeliveryOrderDetails.setArrivalAmount(dirDeliveryOrderDetailsOut.getArrivalQuantity().multiply(dirDeliveryOrderDetailsOut.getOrderUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP));
        } else {
            excelDeliveryOrderDetails.setArrivalAmount(BigDecimal.ZERO);
        }
        if (Objects.nonNull(dirDeliveryOrderDetailsOut.getDeliveryQuantity()) && Objects.nonNull(dirDeliveryOrderDetailsOut.getOrderUnitPrice())) {
            dirDeliveryOrderDetailsOut.setDeliveryAmount(dirDeliveryOrderDetailsOut.getDeliveryQuantity().multiply(dirDeliveryOrderDetailsOut.getOrderUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP));
        } else {
            excelDeliveryOrderDetails.setDeliveryAmount(BigDecimal.ZERO);
        }
        excelDeliveryOrderDetails.setIsGiftStr(NumberUtil.INTEGER_ONE.equals(dirDeliveryOrderDetailsOut.getIsGift()) ? "是" : "否");
        excelDeliveryOrderDetails.setIndex(index + 1);
        return excelDeliveryOrderDetails;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResetTakeDirDeliveryByDeliveryOrderId(Long id, String bizOrgCode, String loginUsername) {
        UpdateResetTakeDirDeliveryIn updateResetTakeDeliveryIn = new UpdateResetTakeDirDeliveryIn();
        updateResetTakeDeliveryIn.setDirDeliveryOrderId(id);
        updateResetTakeDeliveryIn.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        updateResetTakeDeliveryIn.setReceiveProgress(DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey());
        updateResetTakeDeliveryIn.setUpdater(loginUsername);
        updateResetTakeDeliveryIn.setBizOrgCode(bizOrgCode);
        ordDirDeliveryMapper.updateResetTakeDirDeliveryById(updateResetTakeDeliveryIn);
        ordDirDeliveryOrderHeartRateMonitorService.deleteByDeliveryOrderId(id, bizOrgCode);
    }

    @Override
    public List<OrdDirDelivery> findNeedAutoTakeDirDeliveryOrderList(String nowTime, String deliveryStatusCode) {
        return ordDirDeliveryMapper.findNeedAutoTakeDirDeliveryOrderList(nowTime, deliveryStatusCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDirDeliveryTakeOngoingById(OrdDirDelivery deliveryOrder) {
        return ordDirDeliveryMapper.updateDirDeliveryTakeOngoingById(deliveryOrder);
    }

    /**
     * 校验门店库存
     *
     * @param detailList
     * @param error
     * @param bizOrgCode
     * @param storeCode
     */
    private void checkInvCharge(List<OrdDirDeliveryDetail> detailList, StringJoiner error, String bizOrgCode, String storeCode) {
        if (stockStoreService.checkStockIsAllowNegative(storeCode, bizOrgCode)) {
            return;
        }
        Optional.ofNullable(detailList).orElse(new ArrayList<>()).forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity()) || BigDecimal.ZERO.compareTo(item.getDeliveryQuantity()) == 0) {
                return;
            }
            BigDecimal bigDecimal = warehouseServer.getStockNum(storeCode, item.getGoodsCode(), bizOrgCode);
            BigDecimal bigDecimalTar = Objects.isNull(bigDecimal) ? BigDecimal.ZERO : bigDecimal;
            if (NumberUtil.INTEGER_ZERO > bigDecimalTar.compareTo(item.getDeliveryQuantity())) {
                error.add("商品【" + item.getGoodsCode() + "】" + item.getGoodsName() + "门店可用库存不足");
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateDeliverOrder(OrdDirDeliveryIn ordDirDeliveryIn) {
        OrdDirDelivery ordDirDelivery = new OrdDirDelivery();
        BeanUtils.copy(ordDirDeliveryIn, ordDirDelivery);
        List<OrdDirDeliveryDetail> detailList = ordDirDeliveryIn.getDetailList();
        ordDirDelivery.setSkuCount(detailList.size());
        ordDirDelivery.setIsReversal(NumberUtil.INTEGER_ZERO);
        ordDirDelivery.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        // 要货数量 要货金额 品类属性
        BigDecimal orderQuantity = BigDecimal.ZERO;
        BigDecimal orderAmount = BigDecimal.ZERO;
        Map<String, String> goodsTypes = new HashMap<>();
        StringJoiner joiner = new StringJoiner(SystemConstant.COMMA);
        for (OrdDirDeliveryDetail detail : detailList) {
            if (Objects.isNull(detail.getOrderQuantity())) {
                throw new BusinessException("要货数量不能为空");
            }
            orderQuantity = orderQuantity.add(detail.getOrderQuantity());
            orderAmount = orderAmount.add(Objects.nonNull(detail.getOrderAmount()) ? detail.getOrderAmount() : BigDecimal.ZERO);
            if (!goodsTypes.containsKey(detail.getGoodsType())) {
                goodsTypes.put(detail.getGoodsType(), detail.getGoodsType());
                joiner.add(detail.getGoodsType());
            }
        }
        ordDirDelivery.setOrderQuantity(orderQuantity);
        ordDirDelivery.setOrderAmount(orderAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDirDelivery.setGoodsType(joiner.toString());
        //配销数量
        ordDirDelivery.setDistributionQuantity(BigDecimal.ZERO);
        //配销金额
        ordDirDelivery.setDistributionAmount(BigDecimal.ZERO);
        //实配数量
        ordDirDelivery.setDeliveryQuantity(BigDecimal.ZERO);
        //实配金额
        ordDirDelivery.setDeliveryAmount(BigDecimal.ZERO);
        String orderPriority = ordDirDeliveryIn.getOrderPriority();
        if (StringUtils.isBlank(orderPriority)) {
            orderPriority = storeCenterService.getOrderPriorityByStoreCode(ordDirDeliveryIn.getStoreCode(), ordDirDeliveryIn.getBizOrgCode(), ordDirDelivery.getStockCode(), ordDirDelivery.getDistributionType());
            ordDirDelivery.setOrderPriority(orderPriority);
        }
        //修改
        boolean isDelete = false;
        if (Objects.nonNull(ordDirDelivery.getId())) {
            OrdDirDelivery dirDelivery = ordDirDeliveryMapper.selectByPrimaryKey(ordDirDelivery.getId());
            if (Objects.isNull(dirDelivery)) {
                throw new BusinessException("此配货单不存在！");
            }
            if (!DeliveryOrderEnum.PENDING.getKey().equals(ordDirDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDirDelivery.getDeliveryStatusCode())) {
                throw new BusinessException("此配货单状态为，" + DeliveryOrderEnum.getValueByKey(ordDirDelivery.getDeliveryStatusCode()) + "不可修改");
            }
            ordDirDeliveryMapper.updateByPrimaryKeySelective(ordDirDelivery);
            isDelete = true;
        } else {
            //新增,生成单号
            ordDirDelivery.setDeliveryOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PH.getCode(), ordDirDelivery.getBizOrgCode(), uniqueUtils, 4));
            ordDirDeliveryMapper.insertSelective(ordDirDelivery);
            // 非订单流配置创建捞单池明细
            if (!DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDirDelivery.getSourceCode())
                    && DistributionWaysEnum.UNIFIEDDIS.getType().equals(ordDirDelivery.getDistributionType())) {
                dirDeliveryOrderSalvageHandle.saveForManualCreateDeliveryOrder(ordDirDelivery, ordDirDeliveryIn.getAuditType());
            }
            String content = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_CREATE.getKey(), ordDirDelivery.getDeliveryOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDirDelivery.getId()),
                    OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(),
                    content, new Date(),
                    ordDirDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        //删除旧数据，重新保存
        if (isDelete) {
            OrdDirDeliveryDetail ordDirDeliveryDetail = new OrdDirDeliveryDetail();
            ordDirDeliveryDetail.setDeliveryOrderId(ordDirDelivery.getId());
            ordDirDeliveryDetailService.delete(ordDirDeliveryDetail);
        }
        ordDirDeliveryDetailService.save(detailList, ordDirDelivery, orderPriority);
        return ordDirDelivery.getId();
    }

    /**
     * 查询在单量
     *
     * @param storeCode
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public List<InOneQtyVO> findInDeliveryOrder(String storeCode, String beginTime, String endTime) {
        return ordDirDeliveryMapper.findInDeliveryOrder(storeCode, beginTime, endTime);
    }


    /**
     * 将明细实体转为出参对象并封装进表头出参对象
     *
     * @param out
     * @param detailList
     */
    private void initOutDetailOutList(OrdDirDeliveryOut out, List<OrdDirDeliveryDetail> detailList) {
        List<OrdDirDeliveryDetailOut> dtlOutList = detailList.stream().map(item -> {
            OrdDirDeliveryDetailOut orderDetailOut = new OrdDirDeliveryDetailOut();
            com.edc.plugins.utils.bean.BeanUtils.copy(item, orderDetailOut);
            return orderDetailOut;
        }).collect(Collectors.toList());
        out.setDetailOutList(dtlOutList);
    }

    /**
     * 采购订单回传直营配货单
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String dirPurchaseOrderToErp(List<TransferNoticePurchaseVO> transferNoticePurchaseVOList) {
        List<OrdDirDeliveryDetail> ordDirDeliveryDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(transferNoticePurchaseVOList)) {
            List<OrdDirDeliveryDetail> allDetails = new ArrayList<>();
            log.info("采购回传采购单数量集合长度为--{}", transferNoticePurchaseVOList.size());
            for (TransferNoticePurchaseVO transferNoticePurchaseVO : transferNoticePurchaseVOList) {
                OrdDeliveryDetailIn ordDeliveryDetailIn = OrdDeliveryDetailIn.builder().vendorCode(transferNoticePurchaseVO.getVendorCode())
                        .orderPriority(transferNoticePurchaseVO.getOrderPriority()).distributionType(DistributionWaysEnum.TRANSFER.getType())
                        .deliveryStatusCode(DeliveryOrderEnum.PREVIEWAPPROVED.getKey()).carryForwardCycle(transferNoticePurchaseVO.getCarryForwardCycle()).build();
                List<OrdDirDeliveryDetail> details = ordDirDeliveryDetailService.findOrdDirDeliveryDetail(ordDeliveryDetailIn);
                // 幂等
                if(CollectionUtils.isEmpty(details)){
                    continue;
                }
                log.info("采购回传订单方匹配直营配货单明细数量集合长度为--{}", details.size());
                List<GoodsDtlsVO> goodsDtls = transferNoticePurchaseVO.getGoodsDtls();

                //获取所有待匹配明细(不过滤订单方)
                OrdDeliveryDetailIn deliveryDetailIn = OrdDeliveryDetailIn.builder()
                        .orderPriority(transferNoticePurchaseVO.getOrderPriority()).distributionType(DistributionWaysEnum.TRANSFER.getType())
                        .deliveryStatusCode(DeliveryOrderEnum.PREVIEWAPPROVED.getKey()).carryForwardCycle(transferNoticePurchaseVO.getCarryForwardCycle()).build();
                List<OrdDirDeliveryDetail> ordDisDeliveryDetail = ordDirDeliveryDetailService.findOrdDirDeliveryDetail(deliveryDetailIn);
                log.info("采购回传不过滤订单方匹配直营配货单明细数量集合长度为--{}", ordDisDeliveryDetail.size());
                allDetails.addAll(ordDisDeliveryDetail);
                //处理匹配到采购单明细
                for (GoodsDtlsVO goodsDtl : goodsDtls) {
                    //匹配到采购订单号
                    List<OrdDirDeliveryDetail> itemDetails = details.stream().filter(item -> goodsDtl.getGoodsCode().equals(item.getGoodsCode())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(itemDetails)) {
                        itemDetails.forEach(e -> {
                            e.setPurchaseNo(transferNoticePurchaseVO.getPurchaseOrderNo());
                            e.setDistributionQuantity(e.getOrderQuantity());
                            e.setDistributionPackageQuantity(e.getDistributionQuantity()
                                    .divide(e.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                            e.setDistributionAmount(e.getOrderQuantity().multiply(e.getOrderUnitPrice()));
                            if (StringUtils.isNotBlank(goodsDtl.getValidityCode())) {
                                e.setExpiry(goodsDtl.getValidityCode().substring(8, goodsDtl.getValidityCode().length() - 1));
                            }
                        });
                        ordDirDeliveryDetails.addAll(itemDetails);
                    }
                }
            }
            List<Long> detailIds = ordDirDeliveryDetails.stream().map(OrdDirDeliveryDetail::getId).collect(Collectors.toList());
            List<OrdDirDeliveryDetail> deliveryDetails = allDetails.stream().filter(item -> !detailIds.contains(item.getId())).distinct().collect(Collectors.toList());
            for (OrdDirDeliveryDetail deliveryDetail : deliveryDetails) {
                deliveryDetail.setDistributionQuantity(BigDecimal.ZERO);
                deliveryDetail.setDistributionPackageQuantity(BigDecimal.ZERO);
                deliveryDetail.setDistributionAmount(BigDecimal.ZERO);
                ordDirDeliveryDetails.add(deliveryDetail);
            }
        }
        log.info("采购回传需要更新数量的直营配货单明细数量集合长度为--{}", ordDirDeliveryDetails.size());
        for (OrdDirDeliveryDetail deliveryDetail : ordDirDeliveryDetails) {
            BigDecimal sellTax = null == deliveryDetail.getSellTax() ? BigDecimal.ZERO : deliveryDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tax = sellTax.add(BigDecimal.ONE);
            deliveryDetail.setDistributionExceptTaxAmount(deliveryDetail.getDistributionAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            deliveryDetail.setDistributionTaxAmount(deliveryDetail.getDistributionAmount().subtract(deliveryDetail.getDistributionExceptTaxAmount()));
            deliveryDetail.setWrhCostAmount(deliveryDetail.getWrhPrice().multiply(deliveryDetail.getDistributionQuantity()));
            deliveryDetail.setWrhExceptTaxAmount(deliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            deliveryDetail.setWrhTaxAmount(deliveryDetail.getWrhCostAmount().subtract(deliveryDetail.getWrhExceptTaxAmount()));
            deliveryDetail.setStoreCostAmount(deliveryDetail.getDistributionUnitPrice().multiply(deliveryDetail.getDistributionQuantity()));
            deliveryDetail.setStoreExceptTaxAmount(deliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            deliveryDetail.setStoreTaxAmount(deliveryDetail.getStoreCostAmount().subtract(deliveryDetail.getStoreExceptTaxAmount()));
        }
        int pageSize = 500;
        int pages = ordDirDeliveryDetails.size() % pageSize == 0 ? ordDirDeliveryDetails.size() / pageSize : ordDirDeliveryDetails.size() / pageSize + 1;
        for (int i = 0; i < pages; i++) {
            ordDirDeliveryDetailService.batchUpdate(ordDirDeliveryDetails.subList(i * pageSize, i == pages - 1 ? ordDirDeliveryDetails.size() : (i + 1) * pageSize));
        }
        //修改配货单状态为已审核
        List<Long> deliveryOrderIdList = ordDirDeliveryDetails.stream().map(OrdDirDeliveryDetail::getDeliveryOrderId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deliveryOrderIdList)) {
            log.info("采购回传需要更新直营配货单状态集合长度为--{}", deliveryOrderIdList.size());
            for (Long deliveryOrderId : deliveryOrderIdList) {
                //配销总数
                BigDecimal distributionQuantity = ordDirDeliveryDetails.stream().distinct().filter(item -> item.getDeliveryOrderId().equals(deliveryOrderId))
                        .map(OrdDirDeliveryDetail::getDistributionQuantity).reduce(BigDecimal::add).get();
                //配销金额
                BigDecimal distributionAmount = ordDirDeliveryDetails.stream().distinct().filter(item -> item.getDeliveryOrderId().equals(deliveryOrderId))
                        .map(OrdDirDeliveryDetail::getDistributionAmount).reduce(BigDecimal::add).get();
                UpdateOrdDirDeliveryIn updateOrdDirDeliveryIn = UpdateOrdDirDeliveryIn.builder().deliveryStatusCode(DeliveryOrderEnum.PREVIEWAPPROVED.getKey())
                        .distributionType(DistributionWaysEnum.TRANSFER.getType()).deliveryStatusCodeUpdate(DeliveryOrderEnum.APPROVED.getKey())
                        .deliveryOrderId(deliveryOrderId).distributionQuantity(distributionQuantity).distributionAmount(distributionAmount).build();
                int updateCount = ordDirDeliveryMapper.updateOrdDirDeliveryStatus(updateOrdDirDeliveryIn);
                OrdDirDelivery ordDirDelivery = ordDirDeliveryMapper.selectByPrimaryKey(deliveryOrderId);
                if (updateCount == 0) {
                    log.error("配货单{}采购回传已更新", ordDirDelivery.getDeliveryOrderNo());
                }
                String content = MessageFormat.format(DeliveryOrderLogEnum.DIR_DELIVERY_ORDER_STATUS_UPDATE.getKey(), ordDirDelivery.getDeliveryOrderNo(),
                        DeliveryOrderEnum.PREVIEWAPPROVED.getValue(), DeliveryOrderEnum.APPROVED.getValue());
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(ordDirDelivery.getId()),
                        OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
            }
            this.updateDirDelivery(deliveryOrderIdList);
            deliveryOrderIdList.forEach(id -> {
                String content = MessageFormat.format(DeliveryOrderLogEnum.TRANSFER_DIR_DELIVERY_ORDER_STATUS_UPDATE.getKey(),
                        DeliveryOrderEnum.getValueByKey(DeliveryOrderEnum.APPROVED.getKey()));
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_DELIVERY_ORDER.getName(), String.valueOf(id),
                        OrdLogTypeEnum.DIR_DELIVERY_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
            });
        }
        return "采购回传信息修改完成";
    }

    @Override
    public int countByStatusAndIdList(String deliveryOrderStatus, List<Long> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            return ordDirDeliveryMapper.countByStatusAndIdList(deliveryOrderStatus, idList);
        } else {
            return NumberUtil.INTEGER_ZERO;
        }
    }

    @Override
    public Page<DirDeliveryForReturnOut> findDeliveryOrderForReturn(DirDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = deliveryOrderIn.getBizOrgCode();
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(loginBizOrgCode);
        Optional<StockInfoOut> anyOneOptional = authOrgStockMap.values().stream().filter(stockInfoOut -> !loginBizOrgCode.equals(stockInfoOut.getBizOrgCode())).findFirst();
        if (!anyOneOptional.isPresent()) {
            deliveryOrderIn.setBizOrgCode("");
        }
        if (CollectionUtils.isEmpty(deliveryOrderIn.getStockCodeList())) {
            List<String> stockCodeList = authOrgStockMap.keySet().stream().collect(Collectors.toList());
            deliveryOrderIn.setStockCodeList(stockCodeList);
        } else {
            // 校验业务仓位是否允许操作
            storeChannelHandle.checkStockCodeListISAuth(deliveryOrderIn.getStockCodeList(), authOrgStockMap);
        }
        deliveryOrderIn.setDeliveryStatusCode(DeliveryOrderEnum.RECEIVED.getKey());
        List<DirDeliveryForReturnOut> deliveryOrderOutList = ordDirDeliveryMapper.findDeliveryOrderForReturnByPage(deliveryOrderIn);
        deliveryOrderOutList.forEach(dirDeliveryForReturnOut -> {
            StockInfoOut stockInfoOut = authOrgStockMap.get(dirDeliveryForReturnOut.getStockCode());
            if (Objects.isNull(stockInfoOut)) {
                throw new BusinessException(dirDeliveryForReturnOut.getStockCode() + "无此仓位信息");
            }
            dirDeliveryForReturnOut.setStockNameStr(stockInfoOut.getStockName() + "[" + stockInfoOut.getStockCode() + "]");
            dirDeliveryForReturnOut.setReceiveTimeStr(DateUtils.format(dirDeliveryForReturnOut.getReceiveTime()));
        });
        Page<DirDeliveryForReturnOut> page = new Page<>(deliveryOrderIn);
        page.setList(deliveryOrderOutList);
        return page;
    }

    @Override
    public List<DirStoreDeliveryNoInfoForAppOut> findStoreDeliveryInfoList(String storeCode) {
        List<DirStoreDeliveryNoInfoForAppOut> deliveryOrderList = ordDirDeliveryMapper.findStoreDeliveryForAppReturnList(storeCode, LocalDateTime.now().minusHours(72));
        if (CollectionUtils.isEmpty(deliveryOrderList)) {
            return null;
        }
        deliveryOrderList.forEach(item -> {
            List<OrdDirDeliveryDetail> detailList = ordDirDeliveryDetailService.findDeliveryOrderDetails(item.getId());
            item.setDetailList(detailList);
        });
        return deliveryOrderList;
    }

    /**
     * 采购回传库存后下发dts
     *
     * @param deliveryOrderIdList
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDirDelivery(List<Long> deliveryOrderIdList) {
        deliveryOrderIdList.forEach(item -> {
            OrdDirDeliveryOut ordDirDeliveryOut = new OrdDirDeliveryOut();
            OrdDirDelivery ordDirDelivery = ordDirDeliveryMapper.selectByPrimaryKey(item);
            List<OrdDirDeliveryDetail> detailList = ordDirDeliveryDetailService.list(OrdDirDeliveryDetail.builder().deliveryOrderId(item).isDelete(NumberUtil.INTEGER_ZERO).build());
            BeanUtils.copy(ordDirDelivery, ordDirDeliveryOut);
            initOutDetailOutList(ordDirDeliveryOut, detailList);
            ordDirDeliveryOut.setDetailList(detailList);
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDirDelivery.getStoreCode());
            BigDecimal distributionQuantity = ordDirDelivery.getDistributionQuantity();
            if (Objects.isNull(distributionQuantity) || BigDecimal.ZERO.compareTo(distributionQuantity) == NumberUtil.INTEGER_ZERO) {
                //整单无法匹配采购单，作废处理
                this.invalidTransferOrder(ordDirDelivery, detailList);
            } else {
                List<OrdDirDeliveryDetail> detailListOut = ordDirDeliveryOut.getDetailList();
                //过滤需要发dts的明细
                List<OrdDirDeliveryDetail> details = detailListOut.stream().filter(detail -> BigDecimal.ZERO.compareTo(detail.getDistributionQuantity()) != NumberUtil.INTEGER_ZERO).collect(Collectors.toList());
                //下发dts
                StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDirDelivery.getStockCode());
                if (stockServer.isSendWms(ordDirDelivery.getStockCode(), stockInfoOut.getBizOrgCode())) {
                    dirDeliveryOrderSalvageHandle.initUnificationBill(ordDirDeliveryOut, details, storeOut.getStoreId(), stockInfoOut.getBizOrgCode());
                }
            }
        });
    }


    /**
     * 根据配货单id 统计配货单明细实收数量和实收金额
     *
     * @param deliveryOrderId 配销单id
     * @return
     */
    private Map<String, BigDecimal> countQuantityAndAmount(Long deliveryOrderId) {
        Map<String, BigDecimal> sumMap = new HashMap<>();
        //实收数量
        BigDecimal arrivalQuantity = BigDecimal.ZERO;
        //实收金额
        BigDecimal arrivalAmount = BigDecimal.ZERO;
        //查询配货单明细
        List<OrdDirDeliveryDetail> deliveryOrderDetails = ordDirDeliveryDetailService.findDeliveryOrderDetails(deliveryOrderId);
        //明细为空 则数量和金额为0 不为空则统计实际数量和金额
        if (CollectionUtils.isNotEmpty(deliveryOrderDetails)) {
            arrivalQuantity = deliveryOrderDetails.stream().filter(d -> Objects.nonNull(d.getArrivalQuantity()))
                    .map(OrdDirDeliveryDetail::getArrivalQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

            arrivalAmount = deliveryOrderDetails.stream().filter(d -> Objects.nonNull(d.getArrivalAmount()))
                    .map(OrdDirDeliveryDetail::getArrivalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        //添加实收数量和实收金额
        sumMap.put(OrdDisMapKeyConstant.TOTAL_QUANTITY, arrivalQuantity);
        sumMap.put(OrdDisMapKeyConstant.TOTAL_AMOUNT, arrivalAmount);

        return sumMap;
    }

    /**
     * 为异步导出查询配货单明细方法
     *
     * @param deliveryOrderIn
     * @return
     */
    private List<AsyncExcelDeliveryOrderDetail> findListForAsyncExportPage(DirDeliveryOrderIn deliveryOrderIn) {
        List<AsyncExcelDeliveryOrderDetail> asyncExcelDeliveryOrderDetails = ordDirDeliveryMapper.findListForAsyncExportByPage(deliveryOrderIn);
        if (CollectionUtils.isEmpty(asyncExcelDeliveryOrderDetails)) {
            return asyncExcelDeliveryOrderDetails;
        }
        asyncExcelDeliveryOrderDetails.forEach(item -> {
            item.setDistributionTypeValue(systemDictService.getSystemDictName(item.getDistributionType()));
            item.setDeliveryStatusValue(DeliveryOrderEnum.getValueByKey(item.getDeliveryStatusCode()));
            item.setIsGiftStr(ModelConst.DELETE.NO.equals(item.getIsGift()) ? "否" : "是");
            item.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(item.getInvoiceType()));
        });
        return asyncExcelDeliveryOrderDetails;
    }

    /**
     * 根据配销单获取关联要货单订货周期
     *
     * @param deliveryOrder
     */
    private void setAutoTakeTime(OrdDirDelivery deliveryOrder) {
        OrdDirDelivRequest dirDelivRequest = requestOrderHandle.getRequestOrderByDeliveryOrderIdAndBizOrgCode(deliveryOrder.getId(), deliveryOrder.getBizOrgCode());
        if (Objects.nonNull(dirDelivRequest)) {
            Integer orderCycleId = dirDelivRequest.getOrderCycleId();
            if (!DeliveryOrderEnum.SHIPPED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
                throw new BusinessException("配货单" + deliveryOrder.getDeliveryOrderNo() + "设置自动收货时间时状态不正确");
            }
            DirOrderProcessConfigItem autoTakeDeliveryRuleItem = dirDeliveryOrderConfigHandle.getAutoTakeDeliveryRule(orderCycleId, deliveryOrder.getBizOrgCode());
            if (Objects.isNull(autoTakeDeliveryRuleItem)) {
                OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, deliveryOrder.getBizOrgCode());
                log.info("配货单：{}，门店" + orderCycle.getStoreCode() + "的" + orderCycle.getShortOrderType() + "（" + orderCycle.getTruncationDateTime() + "）" + "未找到配货单是否自动收货配置项", deliveryOrder.getDeliveryOrderNo());
                return;
            }
            // 如果是非自动收货
            if (OrderCycleProcessConfigItemCodeEnum.MANUAL_RECEIVE.getCode().equals(autoTakeDeliveryRuleItem.getItemCode())) {
                return;
            }
            log.info("配货单{}即将设置自动收货时间", deliveryOrder.getDeliveryOrderNo());
            if (StringUtils.isNotBlank(autoTakeDeliveryRuleItem.getItemValue())) {
                Integer value = Integer.parseInt(autoTakeDeliveryRuleItem.getItemValue());
                LocalDateTime autoTakeDeliveryTime = deliveryOrder.getDeliveryTime().plusHours(value);
                deliveryOrder.setAutoTakeDeliveryTime(autoTakeDeliveryTime);
                log.info("配货单{}自动收货时间1------------------设置为{}", deliveryOrder.getDeliveryOrderNo(), deliveryOrder.getAutoTakeDeliveryTime());
            }
        }
    }


}
