package com.edc.erp.disdeliveryorder.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
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
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.fund.UnFrozenAndPayIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.purchase.FindTransferOrderIn;
import com.edc.erp.common.model.in.purchase.OrdDeliveryDetailIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.model.out.QueryPurchaseOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.OrgSortOut;
import com.edc.erp.common.model.out.purchase.GoodsDtlsVO;
import com.edc.erp.common.model.out.purchase.OrderDeliverRequestOut;
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
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.*;
import com.edc.erp.disdeliveryorder.handle.DisDeliveryImportHandle;
import com.edc.erp.disdeliveryorder.handle.DisDeliveryInvalidUnFreezeHandle;
import com.edc.erp.disdeliveryorder.handle.DisDeliveryOrderSalvageHandle;
import com.edc.erp.disdeliveryorder.listener.DisDeliveryAsyncImportListener;
import com.edc.erp.disdeliveryorder.listener.DisDetailImportListener;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryMapper;
import com.edc.erp.disdeliveryorder.model.in.*;
import com.edc.erp.disdeliveryorder.model.out.*;
import com.edc.erp.disdeliveryorder.service.*;
import com.edc.erp.disdifferenceorder.entity.OrdDisDelivDifference;
import com.edc.erp.disdifferenceorder.service.OrdDisDelivDifferenceService;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequest;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.enumeration.DeliveryOrderFreezeEnum;
import com.edc.erp.enumeration.DeliveryOrderReceiveProgressEnum;
import com.edc.erp.enumeration.OrderCycleProcessConfigItemCodeEnum;
import com.edc.erp.handle.DisDeliveryOrderConfigHandle;
import com.edc.erp.handle.DisRequestOrderHandle;
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
import org.springframework.beans.factory.annotation.Value;
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
 * 配销单(OrdDisDelivery)表服务实现类
 *
 * @author weichao
 * @since 2022-10-10 19:48:29
 */
@Slf4j
@Service
public class OrdDisDeliveryServiceImpl extends BaseServiceImpl<OrdDisDelivery> implements OrdDisDeliveryService {
    @Autowired
    private OrdDisDeliveryMapper ordDisDeliveryMapper;
    @Autowired
    private StoreCenterService storeCenterService;
    @Autowired
    private OrdDisDeliveryDetailService ordDisDeliveryDetailService;
    @Autowired
    private OrderGoodsServer orderGoodsServer;
    @Autowired
    private OrdDisDeliveryOrderAttachmentService deliveryOrderAttachmentService;
    @Autowired
    private OrdDisDeliveryOrderSigningService ordDisDeliveryOrderSigningService;
    @Autowired
    private FileService fileService;
    @Autowired
    private UniqueUtils uniqueUtils;
    @Autowired
    private OrdDisDelivDifferenceService ordDisDelivDifferenceService;
    @Autowired
//    private AsyncTaskItemService asyncTaskItemService;
    private AsyncLogService asyncLogService;
    @Autowired
    private StockServer stockServer;
    @Autowired
    private SystemDictService systemDictService;

    @Autowired
    private OrdDisDeliveryOrderHeartRateMonitorService ordDisDeliveryOrderHeartRateMonitorService;

    @Autowired
    private WarehouseServer warehouseServer;

    @Autowired
    private OrdDisSalvageDelivPondDetailService ordDisSalvageDelivPondDetailService;

    @Autowired
    private DisDeliveryOrderSalvageHandle disDeliveryOrderSalvageHandle;

    @Autowired
    private StockStoreService stockStoreService;

    @Autowired
    private OrdDisDeliveryPayService ordDisDeliveryPayService;

    @Autowired
    private StockFlowService stockFlowService;

    @Autowired
    private DisRequestOrderHandle requestOrderHandle;

    @Autowired
    private DisDeliveryOrderConfigHandle disDeliveryOrderConfigHandle;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private WarningService warningService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private DisDeliveryImportHandle disDeliveryImportHandle;

    @Autowired
    private StoreChannelHandle storeChannelHandle;

    @Autowired
    private PurchaseOrderClient purchaseOrderClient;

    @Autowired
    private FundServer fundServer;

    @Value("${warningRedisDB}")
    private Integer warningRedisDB;

    @Autowired
    private DisDeliveryInvalidUnFreezeHandle disDeliveryInvalidUnFreezeHandle;

    @Value("${redisMq.erpOrdTopic}")
    private String erpOrdTopic;


    /**
     * 分页查询配销单信息
     *
     * @param deliveryOrderIn
     * @return
     */
    @Override
    public Page<DisDeliveryOrderOut> findDeliveryOrdersByPage(DisDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = deliveryOrderIn.getBizOrgCode();
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(deliveryOrderIn.getBizOrgCode());
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
            String deliveryNo = ordDisDelivDifferenceService.getDeliveryNoByDiffNo(deliveryOrderIn.getDifferenceOrderNo(), null);
            if (StringUtils.isBlank(deliveryNo)) {
                return new Page<>(deliveryOrderIn);
            }
            deliveryOrderIn.setDeliveryOrderNo(deliveryNo);
        }
        List<DisDeliveryOrderOut> deliveryOrderOutList = ordDisDeliveryMapper.findDeliveryOrdersByPage(deliveryOrderIn);
        for (DisDeliveryOrderOut item : deliveryOrderOutList) {
            item.setDifferenceOrderNo(ordDisDelivDifferenceService.getDifferenceNoByDeliveryNo(item.getDeliveryOrderNo(), item.getBizOrgCode()));
            StockInfoOut stockInfoOut = authOrgStockMap.get(item.getStockCode());
            if (Objects.isNull(stockInfoOut)) {
                throw new BusinessException(item.getStockCode() + "无此仓位信息");
            }
            List<OrdDisDeliveryDetail> deliveryOrderDetails = ordDisDeliveryDetailService.findDeliveryOrderDetails(item.getId());
            if (CollectionUtils.isNotEmpty(deliveryOrderDetails)) {
                // 仓位中文
                item.setStockName(stockInfoOut.getStockName());
                item.setWrhCodeName(stockInfoOut.getWarehouseName());
                item.setOrdDisDeliveryDetailList(deliveryOrderDetails);
            }
            //状态
            item.setDeliveryStatusValue(DeliveryOrderEnum.getValueByKey(item.getDeliveryStatusCode()));
            //是否能匹配订货订单
            //配送方式中文
            item.setDistributionTypeValue(systemDictService.getSystemDictName(StringUtils.isNotBlank(item.getDistributionType()) ? item.getDistributionType() : ""));
            // 如果是已收货状态
            if (item.getDeliveryStatusCode().equals(DeliveryOrderEnum.SHIPPED.getKey()) && NumberUtils.INTEGER_ZERO.equals(item.getIsReversal()) && NumberUtils.INTEGER_ZERO.equals(item.getIsReversalOrder())) {
                // 判断是否已签收，未签收的不能进行收货
                OrdDisDeliveryOrderSigning deliveryOrderSigning = ordDisDeliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(item.getId(), item.getBizOrgCode());
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
        Page<DisDeliveryOrderOut> resultPage = new Page<>(deliveryOrderIn);
        resultPage.setList(deliveryOrderOutList);
        return resultPage;
    }

    /**
     * 分页查询配销单明细信息
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    @Override
    public Page<DisDeliveryOrderDetailsOut> findDeliveryOrderDetailForPage(DisDeliveryOrderDetailsIn deliveryOrderDetailsIn, String loginBizOrgCode) {
        OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(deliveryOrderDetailsIn.getDeliveryOrderId());
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDisDelivery.getStockCode(), loginBizOrgCode, "操作配销单明细");
        List<DisDeliveryOrderDetailsOut> disDeliveryOrderDetailsOuts = ordDisDeliveryDetailService.findDeliveryOrderDetailsForPage(deliveryOrderDetailsIn);
        disDeliveryOrderDetailsOuts.forEach(item -> {
            //配销方式
            item.setDistributionTypeValue(DistributionWaysEnum.getNameByType(ordDisDelivery.getDistributionType()));
            item.setDistributionType(ordDisDelivery.getDistributionType());
            //品类名称
            OrgSortOut orgSortOut = orderGoodsServer.getByCode(item.getSmallSort(), ordDisDelivery.getBizOrgCode());
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
            item.setPosition(ordDisDelivery.getStoreCode());
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
            item.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(item.getInvoiceType()));
        });
        Page<DisDeliveryOrderDetailsOut> resultPage = new Page<>(deliveryOrderDetailsIn);
        resultPage.setList(disDeliveryOrderDetailsOuts);
        return resultPage;
    }

    @Autowired
    private AsyncExportExecutor asyncExportExecutor;

    /**
     * 导出配销单明细信息
     *
     * @param deliveryOrderDetailsIn
     * @return
     */
    /*@Override
    public String exportDeliveryOrderDetails(DisDeliveryOrderDetailsIn deliveryOrderDetailsIn) {
        deliveryOrderDetailsIn.setPageNum(0);
        deliveryOrderDetailsIn.setPageSize(0);
        Page<DisDeliveryOrderDetailsOut> resultPage = findDeliveryOrderDetailForPage(deliveryOrderDetailsIn);
        List<ExcelDeliveryOrderDetails> excelDeliveryOrderDetails = parseDataToExcel(resultPage.getList());
        log.info("导出配销单明细列表集合大小是--{}", excelDeliveryOrderDetails.size());
        String title = "导出配销单明细";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelDeliveryOrderDetails,
                title, title, ExcelDeliveryOrderDetails.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }*/
    @Override
    public String exportDeliveryOrderDetails(DisDeliveryOrderDetailsIn deliveryOrderDetailsIn, String loginBizOrgCode) {
        deliveryOrderDetailsIn.setPageSize(10000);
        String title = "导出配销单明细";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配销单列表",
                        // 导出模板实体
                        ExcelDeliveryOrderDetails.class,
                        // 分页查询对象
                        deliveryOrderDetailsIn,
                        // 分页查询方法
                        page -> {
                            Page<DisDeliveryOrderDetailsOut> resultPage = this.findDeliveryOrderDetailForPage(deliveryOrderDetailsIn, loginBizOrgCode);
                            List<ExcelDeliveryOrderDetails> excelDeliveryOrderDetails = parseDataToExcel(resultPage.getList());
                            log.info("导出配销单明细列表集合大小是--{}", excelDeliveryOrderDetails.size());
                            return excelDeliveryOrderDetails;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    /**
     * 新增或修改配销单信息
     *
     * @param ordDisDeliveryIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdate(OrdDisDeliveryIn ordDisDeliveryIn) {
        ordDisDeliveryIn.setDeliveryStatusCode(DistributionWaysEnum.TRANSFER.getType().equals(ordDisDeliveryIn.getDistributionType()) ? DeliveryOrderEnum.PREVIEWAPPROVED.getKey() : DeliveryOrderEnum.PENDING.getKey());
        return this.saveOrUpdateDeliverOrder(ordDisDeliveryIn);
    }

    /**
     * 运营端查询配货单收货信息
     *
     * @param disDeliveryOrderId
     * @return
     */
    @Override
    public TakeDeliveryInfoOut getTakeDeliveryInfoByDeliveryOrderId(Long disDeliveryOrderId) {
        OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(disDeliveryOrderId);
        if (Objects.isNull(ordDisDelivery)) {
            throw new BusinessException("不存在的配销单");
        }
        List<OrdDisDeliveryOrderAttachment> attachmentList = deliveryOrderAttachmentService.findAllByDeliveryOrderIdAndBizOrgCode(disDeliveryOrderId, ordDisDelivery.getBizOrgCode());
        List<String> signingAttachmentUrlList = Lists.newArrayList();
        List<String> takeAttachmentUrlList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            signingAttachmentUrlList = attachmentList.stream().filter(deliveryOrderAttachment -> NumberUtil.INTEGER_ONE.equals(deliveryOrderAttachment.getAttachmentType())).map(OrdDisDeliveryOrderAttachment::getAttachmentUrl).collect(Collectors.toList());
            takeAttachmentUrlList = attachmentList.stream().filter(deliveryOrderAttachment -> NumberUtil.INTEGER_TWO.equals(deliveryOrderAttachment.getAttachmentType())).map(OrdDisDeliveryOrderAttachment::getAttachmentUrl).collect(Collectors.toList());
        }

        DisDeliveryOrderSigningOut disDeliveryOrderSigningOut = new DisDeliveryOrderSigningOut();
        OrdDisDeliveryOrderSigning ordDisDeliveryOrderSigning = ordDisDeliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(disDeliveryOrderId, ordDisDelivery.getBizOrgCode());
        if (Objects.nonNull(ordDisDeliveryOrderSigning)) {
            BeanUtils.copy(ordDisDeliveryOrderSigning, disDeliveryOrderSigningOut);
        }
        disDeliveryOrderSigningOut.setSigningAttachmentUrlList(signingAttachmentUrlList);
        TakeDeliveryInfoOut takeDeliveryInfoOut = new TakeDeliveryInfoOut();
        takeDeliveryInfoOut.setDisDeliveryOrderSigningOut(disDeliveryOrderSigningOut);
        takeDeliveryInfoOut.setTakeRemark(ordDisDelivery.getTakeRemark());
        takeDeliveryInfoOut.setTakeAttachmentUrlList(takeAttachmentUrlList);
        return takeDeliveryInfoOut;
    }

    /**
     * 批量新增配销单
     *
     * @param oddDisDelivers
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<OrdDisDelivery> oddDisDelivers) {
        return ordDisDeliveryMapper.batchInsert(oddDisDelivers);
    }

    /**
     * 红冲配销单
     *
     * @param chargeDisDeliveryOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response chargeDeliveryOrder(ChargeDisDeliveryOrderIn chargeDisDeliveryOrderIn, String username) {
        OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(chargeDisDeliveryOrderIn.getDeliveryOrderId());
        if (Objects.isNull(ordDisDelivery)) {
            throw new BusinessException("此配销单不存在");
        }
        String loginBizOrgCode = chargeDisDeliveryOrderIn.getBizOrgCode();
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDisDelivery.getStockCode(), loginBizOrgCode, "冲销配销单");
        //校验已收货的配销差异单是否冲销
        OrdDisDelivDifference query = new OrdDisDelivDifference();
        query.setDeliveryOrderNo(ordDisDelivery.getDeliveryOrderNo());
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        query.setIsReversal(NumberUtil.INTEGER_ZERO);
        query.setIsReversalOrder(NumberUtils.INTEGER_ZERO);
        List<OrdDisDelivDifference> queryList = ordDisDelivDifferenceService.list(query);
        if (CollectionUtils.isNotEmpty(queryList)) {
            Optional<OrdDisDelivDifference> anyOptional = queryList.stream().filter(ordDisDelivDifference -> !DifferenceOrderStatusEnum.INVALID.getCode().equals(ordDisDelivDifference.getDifferenceStatus())).findAny();
            if (anyOptional.isPresent()) {
                throw new BusinessException("有关联的配销差异单还未冲销，请先处理差异单");
            }
        }
        if (!DeliveryOrderEnum.RECEIVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.SHIPPED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
            throw new BusinessException("只有已收货和已发货状态可冲销");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDisDelivery.getIsReversal())) {
            throw new BusinessException("此单据已被红冲不能重复生成红冲单");
        }
        if (NumberUtil.INTEGER_ONE.equals(ordDisDelivery.getIsReversalOrder())) {
            throw new BusinessException("红冲单不能被红冲");
        }
        //校验门店库存
        if (DeliveryOrderEnum.RECEIVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
            String message = this.checkInvRed(ordDisDelivery, chargeDisDeliveryOrderIn.getDetailList());
            if (StringUtil.isNotEmpty(message)) {
                throw new BusinessException(message);
            }
        }

        // 保存红冲配销单
        OrdDisDelivery disDelivery = new OrdDisDelivery();
        BeanUtils.copy(ordDisDelivery, disDelivery);
        disDelivery.setOrderQuantity(Objects.isNull(ordDisDelivery.getOrderQuantity()) ? BigDecimal.ZERO : ordDisDelivery.getOrderQuantity().negate());
        disDelivery.setOrderAmount(Objects.isNull(ordDisDelivery.getOrderAmount()) ? BigDecimal.ZERO : ordDisDelivery.getOrderAmount().negate());
        disDelivery.setDistributionQuantity(Objects.isNull(ordDisDelivery.getDistributionQuantity()) ? BigDecimal.ZERO : ordDisDelivery.getDistributionQuantity().negate());
        disDelivery.setDistributionAmount(Objects.isNull(ordDisDelivery.getDistributionAmount()) ? BigDecimal.ZERO : ordDisDelivery.getDistributionAmount().negate());
        disDelivery.setDeliveryQuantity(Objects.isNull(ordDisDelivery.getDeliveryQuantity()) ? BigDecimal.ZERO : ordDisDelivery.getDeliveryQuantity().negate());
        disDelivery.setDeliveryAmount(Objects.isNull(ordDisDelivery.getDeliveryAmount()) ? BigDecimal.ZERO : ordDisDelivery.getDeliveryAmount().negate());
        disDelivery.setId(null);
        disDelivery.setIsReversal(NumberUtil.INTEGER_ZERO);
        disDelivery.setIsReversalOrder(NumberUtil.INTEGER_ONE);
        disDelivery.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
        disDelivery.setDeliveryTime(LocalDateTime.now());
        disDelivery.setCreator(username);
        disDelivery.setUpdater(username);
        disDelivery.setCreateTime(LocalDateTime.now());
        disDelivery.setUpdateTime(LocalDateTime.now());
        // 配销红冲单号
        disDelivery.setDeliveryOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PX.getCode(), ordDisDelivery.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR));
        //新增配销红冲单
        int count = this.saveChargeDeliveryOrder(disDelivery, chargeDisDeliveryOrderIn.getDetailList());

        //库存调整,修改bug 冲销单发库存传冲销单的业务单号
        this.adjustInvRedFlush(disDelivery, chargeDisDeliveryOrderIn.getDetailList(), stockInfoOut);
        //资金返款
        Response response = ordDisDeliveryPayService.returnAmountByDeliveryOrder(disDelivery, ordDisDelivery.getDeliveryOrderNo(),
                FundReturnTypeEnum.DIS_DELIVERY_ORDER_CHARGE.getName(), ordDisDelivery.getDeliveryAmount(), ordDisDelivery.getId());
        if (!response.isSuccess()) {
            log.error("配货单{}冲销调整资金账户异常{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
            throw new BusinessException(response.getMessage());
        }
        String content = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_CREATE_CHARGE.getKey(), ordDisDelivery.getDeliveryOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                String.valueOf(disDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                content, new Date(),
                disDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        //修改原配销单
        this.updateDeliverOrder(ordDisDelivery);
        return Response.data(count);
    }

    private String checkInvRed(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList) {
        StringJoiner error = new StringJoiner(",");
        //校验门店库存
        this.checkInvCharge(detailList, error, ordDisDelivery.getBizOrgCode(), ordDisDelivery.getStoreCode());
        if (error.length() == NumberUtil.INTEGER_ZERO) {
            return "";
        }
        return error.toString();
    }

    /**
     * 门店库存调整
     *
     * @param ordDisDelivery
     * @param detailList
     */
    private void adjustInvRedFlush(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList, StockInfoOut stockInfoOut) {
        List<StockFlowIn> stockFlowInList = Lists.newArrayList();
        //门店库存调整
        StockFlowIn storeFlowIn = this.initStoreStockRedFlush(ordDisDelivery, detailList, stockInfoOut);
        stockFlowInList.add(storeFlowIn);
        // 天岁接入ERP，不再对接中科接口
//        boolean isAbutmentWms = stockServer.isAbutmentWms(ordDisDelivery.getStockCode(), ordDisDelivery.getBizOrgCode());
//        if (isAbutmentWms) {
        //异步调仓储库存
        StockFlowIn wareFlowIn = this.initWarehouseStockRedFlush(ordDisDelivery, detailList, stockInfoOut);
        stockFlowInList.add(wareFlowIn);
//        }
        Response response = stockFlowService.checkStockFlow(stockFlowInList);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getMessage());
        }
    }

    /**
     * 冲销 调整仓储库存
     *
     * @param ordDisDelivery
     * @param detailList
     * @return
     */
    private StockFlowIn initWarehouseStockRedFlush(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getName());
        stockFlowIn.setFlowDate(ordDisDelivery.getDeliveryTime());
        stockFlowIn.setCreator(ordDisDelivery.getCreator());
        stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        stockFlowIn.setOrgCode(stockInfoOut.getOrgCode());
        stockFlowIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        detailList.forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity())) {
                return;
            }

            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            //配销单-冲销仓储库存调整发生价取原单仓储库存价
            stockFlowGoodsIn.setPrice(item.getWrhPrice());
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDisDelivery.getStockCode());
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
            stockFlowGoodsIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
            stockFlowGoodsIns.add(stockFlowGoodsIn);

        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    /**
     * 冲销 门店库存调整
     *
     * @param ordDisDelivery
     * @param detailList
     * @return
     */
    private StockFlowIn initStoreStockRedFlush(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList, StockInfoOut stockInfoOut) {
        // 门店库存调整
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getName());
        stockFlowIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
        stockFlowIn.setFlowDate(ordDisDelivery.getDeliveryTime());
        stockFlowIn.setCreator(ordDisDelivery.getCreator());
        stockFlowIn.setOperationType(OrderTypeEnum.COVER.getCode());
        stockFlowIn.setOrgCode(ordDisDelivery.getOrgCode());
        stockFlowIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
        stockFlowIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
        detailList.forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity())) {
                return;
            }
            StockFlowGoodsIn stockFlowGoodsIn = new StockFlowGoodsIn();
            BeanUtils.copy(item, stockFlowGoodsIn);
            //收货后冲销，门店库存减少
            if (DeliveryOrderEnum.RECEIVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
                //实际数
                stockFlowGoodsIn.setActualQty(item.getDeliveryQuantity().abs());
                //实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            }
            //发货后冲销 门店库存占用，
            if (DeliveryOrderEnum.SHIPPED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
                //实际数
                stockFlowGoodsIn.setActualQty(item.getDeliveryQuantity().abs());
                //实际增/减
                stockFlowGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
                //申请数
                stockFlowGoodsIn.setApplyQty(item.getDeliveryQuantity().abs());
                //申请增/减
                stockFlowGoodsIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
                //是否改变可用库存 Y是N否-统配出/配销出
                stockFlowGoodsIn.setIsBusinessQty("N");
            }
            if (StringUtils.isNotBlank(item.getExpiry())) {
                stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(item.getExpiry(), stockFlowGoodsIn.getActualQty())));
            }
            // 仓储
            stockFlowGoodsIn.setStockCode(ordDisDelivery.getStockCode());
            stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
            stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
            stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
            stockFlowGoodsIn.setStoreCode(ordDisDelivery.getStoreCode());
            stockFlowGoodsIn.setStoreName(ordDisDelivery.getStoreName());
            //配销单-冲销门店库存调整发生价取原单门店配销价
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
            stockFlowGoodsIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
            // 效期码
            stockFlowGoodsIns.add(stockFlowGoodsIn);
        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return stockFlowIn;
    }

    /**
     * 根据配销单id查询配销单出参信息
     *
     * @param deliveryOrderId
     * @return
     */
    @Override
    public DisDeliveryOrderOut getDeliveryOrderOutByDeliveryOrderId(Long deliveryOrderId) {
        OrdDisDelivery ordDisDelivery = this.selectByPrimaryKey(deliveryOrderId);
        WarehouseInfoOut warehouseInfoOut = warehouseServer.getWarehouseInfoByCode(ordDisDelivery.getWrhCode());
        if (Objects.isNull(warehouseInfoOut)) {
            log.info("配销单{}获取详情--仓储代码{}不存在", ordDisDelivery.getDeliveryOrderNo(), ordDisDelivery.getWrhCode());
            throw new RuntimeException("仓储代码不存在");
        }
        DisDeliveryOrderOut disDeliveryOrderOut = new DisDeliveryOrderOut();
        BeanUtils.copy(ordDisDelivery, disDeliveryOrderOut);
        StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisDelivery.getStockCode());
        if (Objects.isNull(stockInfoOut)) {
            throw new BusinessException("无此仓位信息");
        }
        disDeliveryOrderOut.setStockName(stockInfoOut.getStockName());
        disDeliveryOrderOut.setWrhCodeName(stockInfoOut.getWarehouseName());
        disDeliveryOrderOut.setDistributionTypeValue(DistributionWaysEnum.getNameByType(disDeliveryOrderOut.getDistributionType()));
        OrdDisDelivRequest ordDisDelivRequest = ordDisDeliveryMapper.getRequestOrder(ordDisDelivery.getId(), ordDisDelivery.getBizOrgCode());
        if (Objects.nonNull(ordDisDelivRequest)) {
            OrdDisOrderCycle ordDisOrderCycle = ordDisDeliveryMapper.getOrderCycleById(ordDisDelivRequest.getOrderCycleId(), ordDisDelivery.getBizOrgCode());
            disDeliveryOrderOut.setShortOrderType(Objects.isNull(ordDisOrderCycle) ? "" : ordDisOrderCycle.getShortOrderType());
            disDeliveryOrderOut.setRequestOrderNo(ordDisDelivRequest.getRequestOrderNo());
        }
        disDeliveryOrderOut.setDeliveryStatusValue(DeliveryOrderEnum.getValueByKey(ordDisDelivery.getDeliveryStatusCode()));
        // 差异单号
        disDeliveryOrderOut.setDifferenceOrderNo(ordDisDelivDifferenceService.getDifferenceNoByDeliveryNo(ordDisDelivery.getDeliveryOrderNo(), ordDisDelivery.getBizOrgCode()));
        // 红冲单
        if (NumberUtils.INTEGER_ONE.equals(ordDisDelivery.getIsReversal())) {
            OrdDisDelivery reversalDeliveryOrder = ordDisDeliveryMapper.selectOne(OrdDisDelivery.builder().sourceNo(ordDisDelivery.getDeliveryOrderNo()).bizOrgCode(ordDisDelivery.getBizOrgCode()).isDelete(ModelConst.DELETE.NO).build());
            disDeliveryOrderOut.setReversalOrderId(reversalDeliveryOrder.getId());
            disDeliveryOrderOut.setReversalOrderNo(reversalDeliveryOrder.getDeliveryOrderNo());
        }
        if (NumberUtils.INTEGER_ONE.equals(ordDisDelivery.getIsReversalOrder())) {
            OrdDisDelivery sourceDeliveryOrder = ordDisDeliveryMapper.selectOne(OrdDisDelivery.builder().deliveryOrderNo(ordDisDelivery.getSourceNo()).bizOrgCode(ordDisDelivery.getBizOrgCode()).isDelete(ModelConst.DELETE.NO).build());
            disDeliveryOrderOut.setSourceOrderId(sourceDeliveryOrder.getId());
        }
        //根据配销单id查询配销单明细 统计实收数量和实收金额
        Map<String, BigDecimal> sumMap = this.countQuantityAndAmount(deliveryOrderId);
        //添加总实收数量
        disDeliveryOrderOut.setTotalArrivalQuantity(sumMap.get(OrdDisMapKeyConstant.TOTAL_QUANTITY));
        //添加总实收金额
        disDeliveryOrderOut.setTotalArrivalAmount(sumMap.get(OrdDisMapKeyConstant.TOTAL_AMOUNT));
        return disDeliveryOrderOut;
    }


    /**
     * 根据配销单id 统计配销单明细实收数量和实收金额
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
        //查询配销单明细
        List<OrdDisDeliveryDetail> deliveryOrderDetails = ordDisDeliveryDetailService.findDeliveryOrderDetails(deliveryOrderId);
        //明细为空 则数量和金额为0 不为空则统计实际数量和金额
        if (CollectionUtils.isNotEmpty(deliveryOrderDetails)) {
            arrivalQuantity = deliveryOrderDetails.stream().filter(d -> Objects.nonNull(d.getArrivalQuantity()))
                    .map(OrdDisDeliveryDetail::getArrivalQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

            arrivalAmount = deliveryOrderDetails.stream().filter(d -> Objects.nonNull(d.getArrivalAmount()))
                    .map(OrdDisDeliveryDetail::getArrivalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        //添加实收数量和实收金额
        sumMap.put(OrdDisMapKeyConstant.TOTAL_QUANTITY, arrivalQuantity);
        sumMap.put(OrdDisMapKeyConstant.TOTAL_AMOUNT, arrivalAmount);

        return sumMap;
    }


    /**
     * 统计所有配销单明细实收数量和实收金额
     *
     * @param bizOrgCode 业务组织
     * @return
     */
    private Map<String, BigDecimal> countTotalQuantityAndAmount(String bizOrgCode) {
        Map<String, BigDecimal> totalSumMap = new HashMap<>();
        //实收数量
        BigDecimal totalArrivalQuantity = BigDecimal.ZERO;
        //实收金额
        BigDecimal totalArrivalAmount = BigDecimal.ZERO;
        //统计实收数量和实收金额
        OrdDisDeliveryDetail ordDisDeliveryDetail = ordDisDeliveryDetailService.countTotalQuantityAndAmount(bizOrgCode);
        //不为空 则统计实际数量和金额
        if (Objects.nonNull(ordDisDeliveryDetail)) {
            //实收数量
            totalArrivalQuantity = ordDisDeliveryDetail.getArrivalQuantity();
            //实收金额
            totalArrivalAmount = ordDisDeliveryDetail.getArrivalAmount();
        }
        //添加实收数量和实收金额
        totalSumMap.put(OrdDisMapKeyConstant.TOTAL_QUANTITY, totalArrivalQuantity);
        totalSumMap.put(OrdDisMapKeyConstant.TOTAL_AMOUNT, totalArrivalAmount);

        return totalSumMap;
    }


    /**
     * 校验商品代码
     *
     * @param deliveryIn
     * @return
     */
    @Override
    public OrdDisDeliveryDetailOut checkOrderGoods(DeliveryIn deliveryIn, String centerStockBizOrgCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBizOrgCode(deliveryIn.getBizOrgCode());
        orderGoodsIn.setGoodsCode(deliveryIn.getGoodsCode());
        orderGoodsIn.setStoreCode(deliveryIn.getStoreCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
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
        OrdDisDeliveryDetailOut ordDisDeliveryDetail = new OrdDisDeliveryDetailOut();
        ordDisDeliveryDetail.setGoodsCode(storeOrderGoods.getGoodsCode());
        ordDisDeliveryDetail.setGoodsName(storeOrderGoods.getGoodsName());
        ordDisDeliveryDetail.setBarCode(storeOrderGoods.getBarCode());
        ordDisDeliveryDetail.setGoodsType(storeOrderGoods.getGoodsType());
        ordDisDeliveryDetail.setDistributionSpecification(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? "" : storeOrderGoods.getDistributionSpecification().getQpcStr());
        ordDisDeliveryDetail.setDistributionSpecificationUnit(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? "" : storeOrderGoods.getDistributionSpecification().getUnitName());
        ordDisDeliveryDetail.setDistributionSpecificationNum(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : BigDecimal.valueOf(storeOrderGoods.getDistributionSpecification().getQpc()));
        ordDisDeliveryDetail.setSellTax(storeOrderGoods.getOutTax());
        ordDisDeliveryDetail.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(ordDisDeliveryDetail.getGoodsType()));
        ordDisDeliveryDetail.setDistributionPrice(Objects.isNull(storeOrderGoods.getDistributionPrice()) ? BigDecimal.ZERO : storeOrderGoods.getDistributionUnitPrice());
        ordDisDeliveryDetail.setDistributionUnitPrice(Objects.isNull(storeOrderGoods.getDistributionUnitPrice()) ? BigDecimal.ZERO : storeOrderGoods.getDistributionUnitPrice());
        ordDisDeliveryDetail.setDistributionSpecificationNum(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : new BigDecimal(storeOrderGoods.getDistributionSpecification().getQpc()));
        ordDisDeliveryDetail.setSmallSort(storeOrderGoods.getSort());
        ordDisDeliveryDetail.setSortName(storeOrderGoods.getSortName());
        ordDisDeliveryDetail.setDistributionType(storeOrderGoods.getDistributionWay());
        ordDisDeliveryDetail.setVendorCode(storeOrderGoods.getVendorCode());
        ordDisDeliveryDetail.setIsGift(NumberUtil.INTEGER_ZERO);
        BigDecimal stockStorePrice = this.getStockPrice(deliveryIn.getStoreCode(), deliveryIn.getGoodsCode(), deliveryIn.getBizOrgCode());
        ordDisDeliveryDetail.setStoreStockPrice(Objects.isNull(stockStorePrice) ? BigDecimal.ZERO : stockStorePrice);
        BigDecimal stockWarehousePrice = this.getStockWarehousePrice(deliveryIn.getWrhCode(), deliveryIn.getStockCode(),
                deliveryIn.getGoodsCode(), centerStockBizOrgCode);
        ordDisDeliveryDetail.setWrhPrice(Objects.isNull(stockWarehousePrice) ? BigDecimal.ZERO : stockWarehousePrice);
        ordDisDeliveryDetail.setInvoiceType(storeOrderGoods.getInvoiceType());
        ordDisDeliveryDetail.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(ordDisDeliveryDetail.getInvoiceType()));
        return ordDisDeliveryDetail;
    }

    @Override
    public BigDecimal getStockPrice(String storeCode, String goodsCode, String bizOrgCode) {
        return ordDisDeliveryMapper.getStockPrice(storeCode, goodsCode, bizOrgCode);
    }

    @Override
    public BigDecimal getStockWarehousePrice(String wrhCode, String stockCode, String goodsCode, String bizOrgCode) {
        return ordDisDeliveryMapper.getStockWarehousePrice(wrhCode, stockCode, goodsCode, bizOrgCode);
    }


    /**
     * 保存红冲单信息
     *
     * @param query
     * @param detailList
     * @return
     */
    private int saveChargeDeliveryOrder(OrdDisDelivery query, List<OrdDisDeliveryDetail> detailList) {
        String orderPriority = query.getOrderPriority();
        if (StringUtils.isBlank(orderPriority)) {
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
        ordDisDeliveryDetailService.save(detailList, query, orderPriority);
        return count;
    }

    /**
     * 配销单红冲修改
     *
     * @param ordDisDelivery
     */
    private void updateDeliverOrder(OrdDisDelivery ordDisDelivery) {
        ordDisDelivery.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        ordDisDelivery.setIsReversal(NumberUtil.INTEGER_ONE);
        this.updateByPrimaryKey(ordDisDelivery);
        String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ORDER_CHARGE.getKey(), ordDisDelivery.getDeliveryOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                content, new Date(),
                ordDisDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResetTakeDisDeliveryByDeliveryOrderId(Long id, String bizOrgCode, String loginUsername) {
        UpdateResetTakeDisDeliveryIn updateResetTakeDeliveryIn = new UpdateResetTakeDisDeliveryIn();
        updateResetTakeDeliveryIn.setDisDeliveryOrderId(id);
        updateResetTakeDeliveryIn.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        updateResetTakeDeliveryIn.setReceiveProgress(DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey());
        updateResetTakeDeliveryIn.setUpdater(loginUsername);
        updateResetTakeDeliveryIn.setBizOrgCode(bizOrgCode);
        ordDisDeliveryMapper.updateResetTakeDisDeliveryById(updateResetTakeDeliveryIn);
        ordDisDeliveryOrderHeartRateMonitorService.deleteByDeliveryOrderId(id, bizOrgCode);
    }

    @Override
    public List<OrdDisDelivery> findNeedAutoTakeDisDeliveryOrderList(String nowTime, String deliveryStatusCode) {
        return ordDisDeliveryMapper.findNeedAutoTakeDisDeliveryOrderList(nowTime, deliveryStatusCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDisDeliveryTakeOngoingById(OrdDisDelivery deliveryOrder) {
        return ordDisDeliveryMapper.updateDisDeliveryTakeOngoingById(deliveryOrder);
    }

    /**
     * 校验仓储库存
     *
     * @param detailList
     * @param error
     * @param bizOrgCode
     * @param stockCode
     */
    private void chargeInvStock(List<OrdDisDeliveryDetail> detailList, StringJoiner error, String bizOrgCode, String stockCode, String wrhCode) {
        Optional.ofNullable(detailList).orElse(new ArrayList<>()).forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity()) || BigDecimal.ZERO.compareTo(item.getDeliveryQuantity()) == 0) {
                return;
            }
            BigDecimal bigDecimal = this.warehouseServer.getWarehouseNum(wrhCode, stockCode, item.getGoodsCode(), bizOrgCode);
            BigDecimal bigDecimalTar = Objects.isNull(bigDecimal) ? BigDecimal.ZERO : bigDecimal;
            if (NumberUtil.INTEGER_ZERO > bigDecimalTar.compareTo(item.getDeliveryQuantity())) {
                error.add("商品【" + item.getGoodsCode() + "】" + item.getGoodsName() + "仓储可用库存不足");
            }
        });
    }

    /**
     * 校验剩余库存
     *
     * @param detailList
     * @param error
     * @param bizOrgCode
     * @param storeCode
     */
    private void checkInvCharge(List<OrdDisDeliveryDetail> detailList, StringJoiner error, String bizOrgCode, String storeCode) {
        if (stockStoreService.checkStockIsAllowNegative(storeCode, bizOrgCode)) {
            return;
        }
        Optional.ofNullable(detailList).orElse(new ArrayList<>()).forEach(item -> {
            if (Objects.isNull(item.getDeliveryQuantity()) || BigDecimal.ZERO.compareTo(item.getDeliveryQuantity()) == 0) {
                return;
            }
            BigDecimal bigDecimal = ordDisDelivDifferenceService.checkStoreInv(bizOrgCode, item.getGoodsCode(), storeCode);
            BigDecimal bigDecimalTar = Objects.isNull(bigDecimal) ? BigDecimal.ZERO : bigDecimal;
            if (NumberUtil.INTEGER_ZERO > bigDecimalTar.compareTo(item.getDeliveryQuantity())) {
                error.add("商品【" + item.getGoodsCode() + "】" + item.getGoodsName() + "门店可用库存不足");
            }
        });
    }

    /**
     * 解析数据到excel
     *
     * @param list
     * @return
     */
    private List<ExcelDeliveryOrderDetails> parseDataToExcel(List<DisDeliveryOrderDetailsOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将DeliveryOrderDetailsOut转换为导出ExcelStoreSkuBaseValue
     *
     * @param deliveryOrderDetailsOut
     * @return ExcelStoreSkuBaseValue
     */
    private ExcelDeliveryOrderDetails convertExcel(DisDeliveryOrderDetailsOut deliveryOrderDetailsOut, int index) {
        ExcelDeliveryOrderDetails excelDeliveryOrderDetails = new ExcelDeliveryOrderDetails();
        BeanUtils.copy(deliveryOrderDetailsOut, excelDeliveryOrderDetails);
        if (Objects.nonNull(deliveryOrderDetailsOut.getArrivalQuantity()) && Objects.nonNull(deliveryOrderDetailsOut.getOrderUnitPrice())) {
            excelDeliveryOrderDetails.setArrivalAmount(deliveryOrderDetailsOut.getArrivalQuantity().multiply(deliveryOrderDetailsOut.getOrderUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP));
        } else {
            excelDeliveryOrderDetails.setArrivalAmount(BigDecimal.ZERO);
        }
        if (Objects.nonNull(deliveryOrderDetailsOut.getDeliveryQuantity()) && Objects.nonNull(deliveryOrderDetailsOut.getOrderUnitPrice())) {
            deliveryOrderDetailsOut.setDeliveryAmount(deliveryOrderDetailsOut.getDeliveryQuantity().multiply(deliveryOrderDetailsOut.getOrderUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP));
        } else {
            excelDeliveryOrderDetails.setDeliveryAmount(BigDecimal.ZERO);
        }
        excelDeliveryOrderDetails.setIsGiftStr(NumberUtil.INTEGER_ONE.equals(deliveryOrderDetailsOut.getIsGift()) ? "是" : "否");
        excelDeliveryOrderDetails.setIndex(index + 1);
        return excelDeliveryOrderDetails;
    }


    /**
     * 作废配销单
     *
     * @param deliveryOrderId
     * @param isReturnAmount
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidDisDeliverOrder(Long deliveryOrderId, String userName, boolean isReturnAmount) {
        OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(deliveryOrderId);
        if (Objects.isNull(ordDisDelivery)) {
            throw new BusinessException("配销单不存在，无法作废！");
        }
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDisDelivery.getStockCode(), UserUtil.getBizOrgCode(), "作废配销单");
        // 23-02-24 产品要求中转自主随意作废
//        if (DistributionWaysEnum.TRANSFER.getType().equals(ordDisDelivery.getDistributionType())) {
//            throw new BusinessException("中转配销单不能作废！");
//        }
        String beforeStatusCode = ordDisDelivery.getDeliveryStatusCode();
        boolean isInvalid = DeliveryOrderEnum.PENDING.getKey().equals(beforeStatusCode) || DeliveryOrderEnum.APPROVED.getKey().equals(beforeStatusCode)
                || (DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(beforeStatusCode) && DeliveryOrderSourceCodeEnum.MANUAL.getType().equals(ordDisDelivery.getSourceCode()));
        if (!isInvalid) {
            throw new BusinessException("配销单状态不允许作废");
        }
        // 处理解冻
        List<BusinessLog> businessLogList = disDeliveryInvalidUnFreezeHandle.deliveryFreezeForBusinessOrder(ordDisDelivery.getId(), userName);
        // 23-02-24 产品要求中转自主随意作废
//        if (DistributionWaysEnum.TRANSFER.getType().equals(ordDisDelivery.getDistributionType())) {
//            throw new BusinessException("中转配销单不能作废！");
//        }

        ordDisDelivery.setUpdater(userName);
        ordDisDelivery.setUpdateTime(LocalDateTime.now());
        ordDisDelivery.setDeliveryStatusCode(DeliveryOrderEnum.INVALID.getKey());
        if (DeliveryOrderFreezeEnum.FREEZE.getKey().equals(ordDisDelivery.getFreezeStatus())) {
            ordDisDelivery.setFreezeStatus(DeliveryOrderFreezeEnum.RELEASE.getKey());
        }
        int count = ordDisDeliveryMapper.updateByPrimaryKeySelective(ordDisDelivery);
        List<OrdDisDeliveryDetail> deliveryOrderDetails = ordDisDeliveryDetailService.findDeliveryOrderDetails(deliveryOrderId);
        if (DeliveryOrderEnum.APPROVED.getKey().equals(beforeStatusCode) && DistributionWaysEnum.UNIFIEDDIS.getType().equals(ordDisDelivery.getDistributionType())) {
            // 天岁接入ERP，不再对接中科接口
//            boolean isAbutmentWms = stockServer.isAbutmentWms(ordDisDelivery.getStockCode(), ordDisDelivery.getBizOrgCode());
//            if (isAbutmentWms) {
            //仓储库存释放
            List<StockFlowIn> stockFlowIns = this.initWarehouseStockInvalid(ordDisDelivery, deliveryOrderDetails, stockInfoOut);
            Response response = stockFlowService.checkStockFlow(stockFlowIns);
            if (!response.isSuccess()) {
                throw new BusinessException(response.getMessage());
            }
//            }
        }
        // 订单流冻结金额注释返款
//        BigDecimal returnAmount = ordDisDeliveryPayService.calculationInvalidDisDeliverAmount(ordDisDelivery, beforeStatusCode);
//        if (isReturnAmount) {
//            //作废反还资金
//            Response returnResponse = ordDisDeliveryPayService.returnAmountByDeliveryOrder(ordDisDelivery, ordDisDelivery.getDeliveryOrderNo(),
//                    FundReturnTypeEnum.DIS_DELIVERY_ORDER_INVALID.getName(), returnAmount, ordDisDelivery.getId());
//            if (!returnResponse.isSuccess()) {
//                log.error("作废配销单{}退款失败，{}", ordDisDelivery.getDeliveryOrderNo(), returnResponse.getMessage());
//                throw new BusinessException(returnResponse.getMessage());
//            }
//        }
        String content = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_STATUS_UPDATE.getKey(), ordDisDelivery.getDeliveryOrderNo(),
                DeliveryOrderEnum.getValueByKey(beforeStatusCode), DeliveryOrderEnum.getValueByKey(DeliveryOrderEnum.INVALID.getKey()));
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), userName);
        businessLogList.add(businessLog);
        if (CollectionUtils.isNotEmpty(businessLogList)) {
            businessLogList.forEach(bl -> asyncLogService.sendAsyncSaveLogByMq(bl));
        }
//        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

    /**
     * 中转配销单作废
     *
     * @param delivery
     * @param deliveryDetails
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidTransferOrder(OrdDisDelivery delivery, List<OrdDisDeliveryDetail> deliveryDetails) {
        String beforeStatusCode = delivery.getDeliveryStatusCode();
        delivery.setDeliveryStatusCode(DeliveryOrderEnum.INVALID.getKey());
        int count = ordDisDeliveryMapper.updateByPrimaryKeySelective(delivery);
        //作废反还资金
        Response response = ordDisDeliveryPayService.returnAmountByDeliveryOrder(delivery, delivery.getDeliveryOrderNo(),
                FundReturnTypeEnum.DIS_DELIVERY_ORDER_INVALID.getName(), delivery.getOrderAmount(), delivery.getId());
        if (!response.isSuccess()) {
            log.error("中转配销单{}作废退款异常:{}", delivery.getDeliveryOrderNo(), response.getMessage());
            throw new BusinessException("中转配销单" + delivery.getDeliveryOrderNo() + "作废退款异常:" + response.getMessage());
        }
        String content = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_STATUS_UPDATE.getKey(), delivery.getDeliveryOrderNo(),
                DeliveryOrderEnum.getValueByKey(beforeStatusCode), DeliveryOrderEnum.getValueByKey(DeliveryOrderEnum.INVALID.getKey()));
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(delivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), delivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

    /**
     * 仓储库存调整初始化入参
     *
     * @param ordDisDelivery
     * @param detailList
     * @return
     */
    private List<StockFlowIn> initWarehouseStockInvalid(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList, StockInfoOut stockInfoOut) {
        StockFlowIn stockFlowIn = new StockFlowIn();
        stockFlowIn.setBizOrgCode(stockInfoOut.getBizOrgCode());
        stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getCode());
        stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getName());
        stockFlowIn.setFlowDate(LocalDateTime.now());
        stockFlowIn.setCreator(ordDisDelivery.getCreator());
        stockFlowIn.setOperationType(OrderTypeEnum.INVALID.getCode());
        stockFlowIn.setOrgCode(ordDisDelivery.getOrgCode());
        stockFlowIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
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
            stockFlowGoodsIn.setStockCode(ordDisDelivery.getStockCode());
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
            stockFlowGoodsIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
            stockFlowGoodsIns.add(stockFlowGoodsIn);

        });
        stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
        return Collections.singletonList(stockFlowIn);
    }

    /**
     * 审核配销单
     *
     * @param stockInfoOut
     * @param ordDisDelivery
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<OrdDisDeliveryOut> audit(StockInfoOut stockInfoOut, OrdDisDelivery ordDisDelivery) {
        String key = DisSystemConstant.CHECK_DIS_DELIVERY_ORDER_AUDIT + ordDisDelivery.getBizOrgCode() +
                SystemConstant.COLON + ordDisDelivery.getStoreCode() + SystemConstant.WAIT + ordDisDelivery.getDeliveryOrderNo();
        if (!redisService.setIfAbsent(key, ordDisDelivery.getDeliveryOrderNo(), 1L, TimeUnit.MINUTES)) {
            return Response.error("配销单短时间内请勿重复审核");
        }
        if (!DeliveryOrderEnum.PENDING.getKey().equals(ordDisDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
            throw new BusinessException("配销单状态不允许审核");
        }
        // 天岁接入ERP，不再对接中科接口
//        if (OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(ordDisDelivery.getBizOrgCode())) {
//            boolean isAbutmentWms = stockServer.isAbutmentWms(ordDisDelivery.getStockCode(), ordDisDelivery.getBizOrgCode());
//            if (!isAbutmentWms) {
//                DisDeliveryTaskIn disDeliveryTaskIn = new DisDeliveryTaskIn();
//                disDeliveryTaskIn.setDeliveryOrderNo(ordDisDelivery.getDeliveryOrderNo());
//                disDeliveryTaskIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
//                disDeliveryTaskIn.setIsNeedPay(DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDisDelivery.getSourceCode()) ? false : true);
//                disDeliveryTaskIn.setLoginUsername(ordDisDelivery.getCreator());
//                asyncTaskItemService.sendDeliveryOrderToZk(JSONObject.toJSONString(disDeliveryTaskIn));
//                return Response.success("提交审核成功");
//            } else {
//                log.error("天岁仓位{}允许下发大仓，请检查仓位配置", ordDisDelivery.getStockCode());
//                throw new BusinessException("请检查天岁仓位" + ordDisDelivery.getStockCode() + "是否下发ERP大仓");
//            }
//        }
        int executionCount;
        // 审核手动创建的非天岁中转配销单
        if (DistributionWaysEnum.TRANSFER.getType().equals(ordDisDelivery.getDistributionType())) {
            executionCount = disDeliveryOrderSalvageHandle.handleDeliveryOrderByManualTransfer(ordDisDelivery, stockInfoOut.getBizOrgCode());
        } else {
            OrdDisSalvageDelivPondDetail ordDisSalvageDelivPondDetail = ordDisSalvageDelivPondDetailService.getOneByDeliveryOrderId(ordDisDelivery.getId());
            if (Objects.isNull(ordDisSalvageDelivPondDetail)) {
                return Response.error("配销单未找到对应的配货池明细");
            }
            // 占库存
            executionCount = disDeliveryOrderSalvageHandle.handleSalvageAfterAuditDeliveryOrder(ordDisDelivery, ordDisSalvageDelivPondDetail, stockInfoOut);
        }
        if (executionCount == 0) {
            return Response.error("配销单未审核中占库存失败");
        }
        OrdDisDeliveryOut ordDisDeliveryOut = new OrdDisDeliveryOut();
        BeanUtils.copy(ordDisDelivery, ordDisDeliveryOut);
        return Response.data(ordDisDeliveryOut, "审核并占库存成功");
    }

    /**
     * 发货
     *
     * @param ordDisDelivery
     * @param updateDisDeliveryDetailInList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDisDeliveryInfo(OrdDisDelivery ordDisDelivery, List<UpdateDisDeliveryDetailIn> updateDisDeliveryDetailInList, String loginBizOrgCode) {
        StockInfoOut stockInfoOut = stockServer.getAndCheckStockInfo(ordDisDelivery.getStockCode(), loginBizOrgCode, "配销单发货");
        List<OrdDisDeliveryDetail> ordDisDeliveryDetails = Lists.newArrayList();
        updateDisDeliveryDetailInList.forEach(updateDeliveryDetailIn -> {
            OrdDisDeliveryDetail disDeliveryDetail = ordDisDeliveryDetailService.getOneByIdAndDeliveryOrderId(updateDeliveryDetailIn.getId(), ordDisDelivery.getId());
            if (Objects.isNull(disDeliveryDetail)) {
                throw new BusinessException("未找到指定配销单明细");
            }
            if (Objects.isNull(disDeliveryDetail.getDistributionQuantity()) || 0 == BigDecimal.ZERO.compareTo(disDeliveryDetail.getDistributionQuantity())) {
                log.info("配货单{}商品{}没有审核数，故不处理发货数", ordDisDelivery.getDeliveryOrderNo(), disDeliveryDetail.getGoodsCode());
                ordDisDeliveryDetails.add(disDeliveryDetail);
                return;
            }
            disDeliveryDetail.setDeliveryQuantity(updateDeliveryDetailIn.getDeliveryQuantity());
            disDeliveryDetail.setDeliveryPackageQuantity(disDeliveryDetail.getDeliveryQuantity().divide(disDeliveryDetail.getDistributionSpecificationNum(), NumberUtil.INTEGER_ZERO, RoundingMode.UP));
            disDeliveryDetail.setDeliveryAmount(disDeliveryDetail.getOrderUnitPrice().multiply(disDeliveryDetail.getDeliveryQuantity()));
            disDeliveryDetail.setStockoutQuantity(disDeliveryDetail.getDistributionQuantity().subtract(disDeliveryDetail.getDeliveryQuantity()));
            disDeliveryDetail.setUpdateTime(LocalDateTime.now());
            disDeliveryDetail.setUpdater(ordDisDelivery.getUpdater());

            //税额
            BigDecimal sellTax = Objects.isNull(disDeliveryDetail.getSellTax()) ? BigDecimal.ZERO : disDeliveryDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tax = sellTax.add(BigDecimal.ONE);
            //最新仓储库存价
            BigDecimal warehousePrice = warehouseServer.getWarehousePrice(ordDisDelivery.getWrhCode(), ordDisDelivery.getStockCode(),
                    disDeliveryDetail.getGoodsCode(), stockInfoOut.getBizOrgCode(), disDeliveryDetail.getVendorCode());
            disDeliveryDetail.setWrhPrice(warehousePrice);
            //配销出货配货单发货仓储减库存，仓储成本相关为负值
            disDeliveryDetail.setWrhCostAmount(warehousePrice.multiply(updateDeliveryDetailIn.getDeliveryQuantity()).negate());
            disDeliveryDetail.setWrhExceptTaxAmount(disDeliveryDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            disDeliveryDetail.setWrhTaxAmount(disDeliveryDetail.getWrhCostAmount().subtract(disDeliveryDetail.getWrhExceptTaxAmount()));

            //配销出货配货单发货门店加库存，门店成本为正值
            disDeliveryDetail.setStoreCostAmount(disDeliveryDetail.getDeliveryAmount());
            disDeliveryDetail.setStoreExceptTaxAmount(disDeliveryDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            disDeliveryDetail.setStoreTaxAmount(disDeliveryDetail.getStoreCostAmount().subtract(disDeliveryDetail.getStoreExceptTaxAmount()));
            ordDisDeliveryDetails.add(disDeliveryDetail);
            ordDisDelivery.setDeliveryAmount(Objects.isNull(ordDisDelivery.getDeliveryAmount()) ? disDeliveryDetail.getDeliveryAmount() : ordDisDelivery.getDeliveryAmount().add(disDeliveryDetail.getDeliveryAmount()));
            ordDisDelivery.setDeliveryQuantity(Objects.isNull(ordDisDelivery.getDeliveryQuantity()) ? disDeliveryDetail.getDeliveryQuantity() : ordDisDelivery.getDeliveryQuantity().add(disDeliveryDetail.getDeliveryQuantity()));
        });
        ordDisDeliveryDetailService.batchUpdateDeliveryInfo(ordDisDeliveryDetails);
        ordDisDelivery.setDeliveryAmount(ordDisDelivery.getDeliveryAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.CEILING));
        ordDisDelivery.setReceiveProgress(DeliveryOrderReceiveProgressEnum.CAN_BEGIN.getKey());
        ordDisDelivery.setDeliveryTime(ordDisDelivery.getUpdateTime());
        ordDisDelivery.setDeliveryStatusCode(DeliveryOrderEnum.SHIPPED.getKey());
        ordDisDelivery.setUpdateTime(LocalDateTime.now());

        // 设置自动收货时间
        this.setAutoTakeTime(ordDisDelivery);

        if (Objects.isNull(ordDisDelivery.getFreezeStatus())) {
            this.shipmentsOrdDisDeliveryFund(ordDisDelivery, ordDisDeliveryDetails);
        } else {
            ordDisDelivery.setFreezeStatus(DeliveryOrderFreezeEnum.RELEASE.getKey());
            // 发货前释放+实扣
            Response response = this.payBeforeShipments(ordDisDelivery.getId(), ordDisDelivery.getDeliveryAmount());
            if (!response.isSuccess()) {
                throw new BusinessException(response.getMessage());
            }
        }

        ordDisDeliveryMapper.updateByPrimaryKeySelective(ordDisDelivery);
        this.optInvForDeliveryInfo(ordDisDelivery, ordDisDeliveryDetails, stockInfoOut);
        String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ORDER_DELIVERED.getKey(), ordDisDelivery.getDeliveryOrderNo(), ordDisDelivery.getDeliveryAmount().abs());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                content, new Date(),
                ordDisDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 配销单库存操作
     *
     * @param ordDisDelivery
     * @param ordDisDeliveryDetails
     */
    @Override
    public void optInvForDeliveryInfo(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> ordDisDeliveryDetails, StockInfoOut stockInfoOut) {
        List<OperatorInvIn> operatorInvIns = Lists.newArrayList();
        // 天岁接入ERP，不再对接中科接口
//        boolean isAbutmentWms = stockServer.isAbutmentWms(ordDisDelivery.getStockCode(), ordDisDelivery.getBizOrgCode());
        // 大库操作
//        if (isAbutmentWms) {
        OperatorInvIn wmsOperatorInvIn = new OperatorInvIn();
        wmsOperatorInvIn.setOccurrenceLocation(StockHappenLieEnum.WAREHOUSE.getCode());
        wmsOperatorInvIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
        if (!DistributionWaysEnum.TRANSFER.getType().equals(ordDisDelivery.getDistributionType())) {
            wmsOperatorInvIn.setApplyLowering(AdjustTypeEnum.REDUCE.getCode());
        }
        wmsOperatorInvIn.setIsBusinessQty("Y");
        operatorInvIns.add(wmsOperatorInvIn);
//        }
        // 门店操作
        OperatorInvIn storeOperatorInvIn = new OperatorInvIn();
        storeOperatorInvIn.setOccurrenceLocation(StockHappenLieEnum.STORE.getCode());
        storeOperatorInvIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
        storeOperatorInvIn.setApplyLowering(AdjustTypeEnum.ADD.getCode());
        storeOperatorInvIn.setIsBusinessQty("N");
        operatorInvIns.add(storeOperatorInvIn);
        List<StockFlowIn> stockFlowInList = this.disDeliveryOrderOptInv(ordDisDelivery, ordDisDeliveryDetails, operatorInvIns, stockInfoOut);
        //调用库存rpc调整库存
        Response response = stockFlowService.checkStockFlow(stockFlowInList);
        if (!response.isSuccess()) {
            log.error("配货单{}发货调整库存异常{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
            throw new BusinessException(response.getMessage());
        }
    }

    /**
     * 配销单资金调整
     *
     * @param ordDisDelivery
     * @param detailList
     */
    @Override
    public void shipmentsOrdDisDeliveryFund(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList) {
        BigDecimal returnAmount = BigDecimal.ZERO;
        // 如果是运营端手动创建
        if (DeliveryOrderSourceCodeEnum.MANUAL.getType().equals(ordDisDelivery.getSourceCode())) {
            returnAmount = calculateManualReturnAmount(ordDisDelivery, detailList, returnAmount);
        }
        // 如果是铺货单或者订单流创建
        if (DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDisDelivery.getSourceCode())
                || DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDisDelivery.getSourceCode())) {
            returnAmount = calculateOrder(ordDisDelivery, detailList, returnAmount);
        }
        if (returnAmount.compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO) {
            return;
        }
        if (returnAmount.compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ONE) {
            returnAmount = returnAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP);
        }
//        log.info("配销单{}退款金额{}", ordDisDelivery.getDeliveryOrderNo(), returnAmount);
        Response response = ordDisDeliveryPayService.returnAmountByDeliveryOrder(ordDisDelivery, ordDisDelivery.getDeliveryOrderNo(), FundReturnTypeEnum.DIS_DELIVERY_ORDER_STOCK_OUT.getName(), returnAmount, ordDisDelivery.getId());

        String error = "该笔业务单已经清算成功，不允许重复清算";
        if (!response.isSuccess()) {
            log.error("配货单{}发货调整资金账户异常{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
            if (StringUtils.isNotBlank(response.getMessage()) && !response.getMessage().contains(error)) {
                throw new BusinessException(response.getMessage());
            }
        }
    }

    private static BigDecimal calculateOrder(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList, BigDecimal returnAmount) {
        for (OrdDisDeliveryDetail item : detailList) {
            // 过滤审核数为空默认为占库存失败（无库存，不下发物流没有发货数）和发货数为空的
            if (Objects.isNull(item.getDeliveryQuantity())) {
                log.info("配货单{}商品{}没有实配数，故按数量为0计算退款金额", ordDisDelivery.getDeliveryOrderNo(), item.getDistributionQuantity());
                item.setDeliveryQuantity(BigDecimal.ZERO);
            }
            BigDecimal differNum = BigDecimal.ZERO;
            if ((DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDisDelivery.getSourceCode())
                    || DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDisDelivery.getSourceCode()))
                    && item.getDeliveryQuantity().compareTo(item.getOrderQuantity()) < NumberUtil.INTEGER_ZERO) {
                differNum = item.getOrderQuantity().subtract(item.getDeliveryQuantity());
            }
            returnAmount = returnAmount.add(differNum.multiply(item.getOrderUnitPrice()));
        }
        return returnAmount;
    }

    private static BigDecimal calculateManualReturnAmount(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList, BigDecimal returnAmount) {
        for (OrdDisDeliveryDetail item : detailList) {
            // 过滤审核数为空默认为占库存失败（无库存，不下发物流没有发货数）和发货数为空的
            if (Objects.isNull(item.getDistributionQuantity()) || Objects.isNull(item.getDeliveryQuantity())) {
                log.info("配货单{}商品{}没有审核数，故不参与资金调整计算", ordDisDelivery.getDeliveryOrderNo(), item.getGoodsCode(), item.getDistributionQuantity());
                continue;
            }
            BigDecimal differNum = BigDecimal.ZERO;
            if (item.getDeliveryQuantity().compareTo(item.getDistributionQuantity()) < NumberUtil.INTEGER_ZERO) {
                differNum = item.getDistributionQuantity().subtract(item.getDeliveryQuantity());
            }
            returnAmount = returnAmount.add(differNum.multiply(item.getOrderUnitPrice()));
        }
        return returnAmount;
    }

    /**
     * 批量审批
     *
     * @param bizOrgCode
     * @param loginUsername
     * @param ordDisDeliveryIns
     * @return
     */
    @Override
    public Response bachAudit(String bizOrgCode, String loginUsername, List<OrdDisDeliveryIn> ordDisDeliveryIns) {
        AtomicInteger successTotal = new AtomicInteger();
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        ordDisDeliveryIns.forEach(ordDisDeliveryIn -> {
            try {
                OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(ordDisDeliveryIn.getId());
                if (Objects.isNull(ordDisDelivery)) {
                    throw new BusinessException("配销单不存在");
                }
                StockInfoOut stockInfoOut;
                if (DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDisDelivery.getSourceCode())) {
                    stockInfoOut = stockServer.getTransInfo(ordDisDelivery.getStockCode());
                } else {
                    stockInfoOut = stockServer.getAndCheckStockInfo(ordDisDelivery.getStockCode(), bizOrgCode, "批量审核配销单");
                }
                Response<OrdDisDeliveryOut> response = this.audit(stockInfoOut, ordDisDelivery);
                if (response.isSuccess()) {
                    successTotal.getAndIncrement();
                }
            } catch (Exception e) {
                log.error("配销单{}审核异常{}", ordDisDeliveryIn.getDeliveryOrderNo(), e);
                errorJoiner.add("配销单" + ordDisDeliveryIn.getDeliveryOrderNo() + e.getMessage());
            }
        });
        return Response.success("成功审核" + successTotal.get() + "笔配销单;" + errorJoiner);
    }

    /**
     * 配销单异步导出测试
     *
     * @param deliveryOrderIn
     * @return
     */
    @Override
    public String exportDeliveryOrder(DisDeliveryOrderIn deliveryOrderIn) {
        // 设置每次查询条数
        deliveryOrderIn.setPageSize(10000);
        String title = "配销单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配销单列表",
                        // 导出模板实体
                        ExcelDeliveryOrder.class,
                        // 分页查询对象
                        deliveryOrderIn,
                        // 分页查询方法
                        page -> {
                            Page<DisDeliveryOrderOut> deliveryOrdersByPage = this.findDeliveryOrdersByPage(deliveryOrderIn);
                            List<ExcelDeliveryOrder> excelDeliveryOrder = parseOrderDataToExcel(deliveryOrdersByPage.getList());
                            log.info("导出配销单列表集合大小是--{}", excelDeliveryOrder.size());
                            return excelDeliveryOrder;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    @Override
    public DisDeliveryOrderOut getDeliveryOrderTotal(DisDeliveryOrderIn deliveryOrderIn) {
        DisDeliveryOrderOut disDeliveryOrderOut = new DisDeliveryOrderOut();
        //如果为空 默认给0
        disDeliveryOrderOut.setDeliveryAmount(BigDecimal.ZERO);
        disDeliveryOrderOut.setDeliveryQuantity(BigDecimal.ZERO);
        disDeliveryOrderOut.setDistributionAmount(BigDecimal.ZERO);
        disDeliveryOrderOut.setDistributionQuantity(BigDecimal.ZERO);
        disDeliveryOrderOut.setTotalArrivalQuantity(BigDecimal.ZERO);
        disDeliveryOrderOut.setTotalArrivalAmount(BigDecimal.ZERO);
        String loginBizOrgCode = deliveryOrderIn.getBizOrgCode();
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(deliveryOrderIn.getBizOrgCode());
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
                return disDeliveryOrderOut;
            }
            storeList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            deliveryOrderIn.setStoreCodeList(storeCodeList);
        }
        if (StringUtils.isNotBlank(deliveryOrderIn.getDifferenceOrderNo())) {
            String deliveryNo = ordDisDelivDifferenceService.getDeliveryNoByDiffNo(deliveryOrderIn.getDifferenceOrderNo(), null);
            if (StringUtils.isBlank(deliveryNo)) {
                return disDeliveryOrderOut;
            }
            deliveryOrderIn.setDeliveryOrderNo(deliveryNo);
        }
        //结果集
//        DisDeliveryOrderOut result = ordDisDeliveryMapper.getDeliveryOrderTotal(deliveryOrderIn);
//        if (Objects.isNull(result)) {
//            return disDeliveryOrderOut;
//        }
//        deliveryOrderIn.setPageSize(0);
//        deliveryOrderIn.setPageNum(0);

        List<Long> sumDeliveryDataIdList = ordDisDeliveryMapper.findSumDeliveryDataIdList(deliveryOrderIn);
        if (CollectionUtils.isNotEmpty(sumDeliveryDataIdList)) {
            SumDisDeliveryOrderDataOut sumDisDeliveryOrderDataOut = ordDisDeliveryMapper.sumDeliveryData(deliveryOrderIn);
            if (Objects.nonNull(sumDisDeliveryOrderDataOut)) {
                disDeliveryOrderOut.setOrderAmount(sumDisDeliveryOrderDataOut.getOrderAmount());
                disDeliveryOrderOut.setOrderQuantity(sumDisDeliveryOrderDataOut.getOrderQuantity());
                disDeliveryOrderOut.setDistributionAmount(sumDisDeliveryOrderDataOut.getDistributionAmount());
                disDeliveryOrderOut.setDistributionQuantity(sumDisDeliveryOrderDataOut.getDistributionQuantity());
                disDeliveryOrderOut.setDeliveryAmount(sumDisDeliveryOrderDataOut.getDeliveryAmount());
                disDeliveryOrderOut.setDeliveryQuantity(sumDisDeliveryOrderDataOut.getDeliveryQuantity());
            }
            DisDeliveryOrderArrivalDataOut deliveryOrderArrivalDataOut = ordDisDeliveryDetailService.sumArrivalDataByDeliveryOrderIdList(sumDeliveryDataIdList);
            if (Objects.nonNull(deliveryOrderArrivalDataOut)) {
                disDeliveryOrderOut.setTotalArrivalAmount(deliveryOrderArrivalDataOut.getTotalArrivalAmount());
                disDeliveryOrderOut.setTotalArrivalQuantity(deliveryOrderArrivalDataOut.getTotalArrivalQuantity());
            }
        }
        return disDeliveryOrderOut;
    }


    @Override
    public Response<List<DisDeliveryOrderDetailsOut>> importDeliveryDetail(ImportDeliveryIn importDeliveryIn, String centerStockBizOrgCode) {
        byte[] bytes = fileService.getFileBytesByFileId(importDeliveryIn.getFileId(), SystemConstant.SYSTEM_CODE);
        if (null == bytes) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DisDetailImportListener listener = new DisDetailImportListener(this, importDeliveryIn, centerStockBizOrgCode);
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportDisDeliveryDetailsOrder.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        List<DisDeliveryOrderDetailsOut> deliveryOrderDetailsOutList = listener.getDeliveryOrderDetailsOuts();
        // 查询商品在采购中效期码
        List<QueryPurchaseIn> queryPurchaseInList = deliveryOrderDetailsOutList.stream().filter(detail -> StringUtils.isNotBlank(detail.getPurchaseNo())).map(item -> {
            QueryPurchaseIn queryPurchaseIn = new QueryPurchaseIn();
            queryPurchaseIn.setPurchaseNo(item.getPurchaseNo());
            queryPurchaseIn.setGoodsCode(item.getGoodsCode());
            return queryPurchaseIn;
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queryPurchaseInList)) {
            return Response.data(deliveryOrderDetailsOutList, listener.getImportErrorMessage(totalErrorMap));
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
        Map<String, QueryPurchaseOut> finalPurchaseGoodsExpiryMap = purchaseGoodsExpiryMap;
        List<DisDeliveryOrderDetailsOut> resultDetailList = Lists.newArrayList();
        deliveryOrderDetailsOutList.forEach(item -> {
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

    /**
     * 查询配销单列表(库存盘点)
     *
     * @param deliveryOrderIn
     * @return
     */
    @Override
    public List<DisDeliveryOrderOut> findDisDeliveryOrder(DisDeliveryOrderIn deliveryOrderIn) {
        StringJoiner sj = new StringJoiner(",");
        sj.add(DeliveryOrderEnum.APPROVED.getKey());
        sj.add(DeliveryOrderEnum.SHIPPED.getKey());
        deliveryOrderIn.setDeliveryStatusCode(sj.toString());
        List<DisDeliveryOrderOut> disDeliveryOrderOutList = ordDisDeliveryMapper.findDisDeliveryOrder(deliveryOrderIn);
        disDeliveryOrderOutList.forEach(item -> {
            item.setDeliveryStatusValue(DeliveryOrderEnum.getValueByKey(item.getDeliveryStatusCode()));
        });
        return disDeliveryOrderOutList;
    }

    /**
     * 根据结转周期查询中转配销单
     *
     * @param findTransferOrderIn
     * @return
     */
    @Override
    public List<OrdDisDelivery> findTransferOrderByCarryForwardCycle(FindTransferOrderIn findTransferOrderIn) {
        return ordDisDeliveryMapper.findTransferOrderByCarryForwardCycle(findTransferOrderIn);
    }

    /**
     * 查询在单量
     *
     * @param storeCode
     * @return
     */
    @Override
    public List<InOneQtyVO> findInDeliveryOrder(String storeCode, String beginTime, String endTime) {
        return ordDisDeliveryMapper.findInDeliveryOrder(storeCode, beginTime, endTime);
    }


    private List<ExcelDeliveryOrder> parseOrderDataToExcel(List<DisDeliveryOrderOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertOrderExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    private ExcelDeliveryOrder convertOrderExcel(DisDeliveryOrderOut disDeliveryOrderOut, int index) {
        ExcelDeliveryOrder excelDeliveryOrder = new ExcelDeliveryOrder();
        BeanUtils.copy(disDeliveryOrderOut, excelDeliveryOrder);
        excelDeliveryOrder.setIsReversal(NumberUtil.INTEGER_ZERO.equals(disDeliveryOrderOut.getIsReversal()) ? "否" : "是");
        excelDeliveryOrder.setIsReversalOrder(NumberUtil.INTEGER_ZERO.equals(disDeliveryOrderOut.getIsReversalOrder()) ? "否" : "是");
        // 仓位名称
        StockTransInfoOut stockOut = orderGoodsServer.getTransInfo(disDeliveryOrderOut.getStockCode(), disDeliveryOrderOut.getBizOrgCode());
        excelDeliveryOrder.setStockCode(Objects.nonNull(stockOut) ? stockOut.getStockName() + "【" + stockOut.getStockCode() + "】" : "");

        excelDeliveryOrder.setIndex(index + 1);
        return excelDeliveryOrder;
    }


    private OrdDisDeliveryDetail initOrdDisDeliveryDetailByAsyncImport(String bizOrgCode, ImportDisDeliveryOrder disDeliveryOrder, String distributionType) {
        OrdDisDeliveryDetail ordDisDeliveryDetail = new OrdDisDeliveryDetail();
        DeliveryIn deliveryIn = new DeliveryIn();
        deliveryIn.setGoodsCode(disDeliveryOrder.getGoodsCode());
        deliveryIn.setBizOrgCode(bizOrgCode);
        deliveryIn.setStockCode(disDeliveryOrder.getStockCode());
        deliveryIn.setStoreCode(disDeliveryOrder.getStoreCode());
        deliveryIn.setWrhCode(disDeliveryOrder.getWrhCode());
        deliveryIn.setDistributionType(distributionType);
        OrdDisDeliveryDetailOut detailOut = this.checkOrderGoodsByAsyncImport(deliveryIn, disDeliveryOrder.getCenterStockBizOrgCode());
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

    public OrdDisDeliveryDetailOut checkOrderGoodsByAsyncImport(DeliveryIn deliveryIn, String centerStockBizOrgCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBizOrgCode(deliveryIn.getBizOrgCode());
        orderGoodsIn.setGoodsCode(deliveryIn.getGoodsCode());
        orderGoodsIn.setStoreCode(deliveryIn.getStoreCode());
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
        OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.isNull(storeOrderGoods)) {
            log.error("门店{}配销单异步导入明细{}不存在", deliveryIn.getStoreCode(), deliveryIn.getGoodsCode());
            return null;
        }
        OrdDisDeliveryDetailOut ordDisDeliveryDetail = new OrdDisDeliveryDetailOut();
        ordDisDeliveryDetail.setGoodsCode(storeOrderGoods.getGoodsCode());
        ordDisDeliveryDetail.setGoodsName(storeOrderGoods.getGoodsName());
        ordDisDeliveryDetail.setBarCode(storeOrderGoods.getBarCode());
        ordDisDeliveryDetail.setGoodsType(storeOrderGoods.getGoodsType());
        ordDisDeliveryDetail.setDistributionSpecification(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? "" : storeOrderGoods.getDistributionSpecification().getQpcStr());
        ordDisDeliveryDetail.setDistributionSpecificationUnit(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? "" : storeOrderGoods.getDistributionSpecification().getUnitName());
        ordDisDeliveryDetail.setDistributionSpecificationNum(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : BigDecimal.valueOf(storeOrderGoods.getDistributionSpecification().getQpc()));
        ordDisDeliveryDetail.setSellTax(storeOrderGoods.getOutTax());
        ordDisDeliveryDetail.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(ordDisDeliveryDetail.getGoodsType()));
        ordDisDeliveryDetail.setDistributionPrice(Objects.isNull(storeOrderGoods.getDistributionPrice()) ? BigDecimal.ZERO : storeOrderGoods.getDistributionUnitPrice());
        ordDisDeliveryDetail.setDistributionUnitPrice(Objects.isNull(storeOrderGoods.getDistributionUnitPrice()) ? BigDecimal.ZERO : storeOrderGoods.getDistributionUnitPrice());
        ordDisDeliveryDetail.setDistributionSpecificationNum(Objects.isNull(storeOrderGoods.getDistributionSpecification()) ? BigDecimal.ZERO : new BigDecimal(storeOrderGoods.getDistributionSpecification().getQpc()));
        ordDisDeliveryDetail.setSmallSort(storeOrderGoods.getSort());
        ordDisDeliveryDetail.setSortName(storeOrderGoods.getSortName());
        ordDisDeliveryDetail.setDistributionType(storeOrderGoods.getDistributionWay());
        ordDisDeliveryDetail.setVendorCode(storeOrderGoods.getVendorCode());
        ordDisDeliveryDetail.setIsGift(NumberUtil.INTEGER_ZERO);
        BigDecimal stockStorePrice = this.getStockPrice(deliveryIn.getStoreCode(), deliveryIn.getGoodsCode(), deliveryIn.getBizOrgCode());
        ordDisDeliveryDetail.setStoreStockPrice(Objects.isNull(stockStorePrice) ? BigDecimal.ZERO : stockStorePrice);
        BigDecimal stockWarehousePrice = this.getStockWarehousePrice(deliveryIn.getWrhCode(), deliveryIn.getStockCode(),
                deliveryIn.getGoodsCode(), centerStockBizOrgCode);
        ordDisDeliveryDetail.setWrhPrice(Objects.isNull(stockWarehousePrice) ? BigDecimal.ZERO : stockWarehousePrice);
        ordDisDeliveryDetail.setInvoiceType(storeOrderGoods.getInvoiceType());
        ordDisDeliveryDetail.setInvoiceTypeStr(ordDisDeliveryDetail.getInvoiceType());
        return ordDisDeliveryDetail;
    }


    /**
     * 新增或修改入库原子能力
     *
     * @param ordDisDeliveryIn
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateDeliverOrder(OrdDisDeliveryIn ordDisDeliveryIn) {
        OrdDisDelivery ordDisDelivery = new OrdDisDelivery();
        BeanUtils.copy(ordDisDeliveryIn, ordDisDelivery);
        List<OrdDisDeliveryDetail> detailList = ordDisDeliveryIn.getDetailList();
        ordDisDelivery.setSkuCount(detailList.size());
        ordDisDelivery.setIsReversal(NumberUtil.INTEGER_ZERO);
        ordDisDelivery.setIsReversalOrder(NumberUtil.INTEGER_ZERO);
        //集货数量 要货金额 品类属性
        BigDecimal orderQuantity = BigDecimal.ZERO;
        BigDecimal orderAmount = BigDecimal.ZERO;
        Map<String, String> goodsTypes = new HashMap<>();
        StringJoiner joiner = new StringJoiner(SystemConstant.COMMA);
        for (OrdDisDeliveryDetail detail : detailList) {
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
        ordDisDelivery.setOrderQuantity(orderQuantity);
        ordDisDelivery.setOrderAmount(orderAmount.setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        ordDisDelivery.setGoodsType(joiner.toString());

        //配销数量
        ordDisDelivery.setDistributionQuantity(BigDecimal.ZERO);
        //配销金额
        ordDisDelivery.setDistributionAmount(BigDecimal.ZERO);
        //实配数量
        ordDisDelivery.setDeliveryQuantity(BigDecimal.ZERO);
        //实配金额
        ordDisDelivery.setDeliveryAmount(BigDecimal.ZERO);
        String orderPriority = ordDisDeliveryIn.getOrderPriority();
        if (StringUtils.isBlank(orderPriority)) {
            orderPriority = storeCenterService.getOrderPriorityByStoreCode(ordDisDeliveryIn.getStoreCode(), ordDisDeliveryIn.getBizOrgCode(),
                    ordDisDelivery.getStockCode(), ordDisDelivery.getDistributionType());
            ordDisDelivery.setOrderPriority(orderPriority);
        }
        //修改
        boolean isDelete = false;
        if (Objects.nonNull(ordDisDelivery.getId())) {
            OrdDisDelivery disDelivery = ordDisDeliveryMapper.selectByPrimaryKey(ordDisDelivery.getId());
            if (Objects.isNull(disDelivery)) {
                throw new BusinessException("此配销单不存在！");
            }
            if (!DeliveryOrderEnum.PENDING.getKey().equals(ordDisDelivery.getDeliveryStatusCode()) && !DeliveryOrderEnum.PREVIEWAPPROVED.getKey().equals(ordDisDelivery.getDeliveryStatusCode())) {
                throw new BusinessException("此配销单状态为，" + DeliveryOrderEnum.getValueByKey(ordDisDelivery.getDeliveryStatusCode()) + "不可修改");
            }
            ordDisDeliveryMapper.updateByPrimaryKeySelective(ordDisDelivery);
            isDelete = true;
        } else {
            //新增,生成单号
            ordDisDelivery.setDeliveryOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PX.getCode(), ordDisDelivery.getBizOrgCode(), uniqueUtils, 4));
            ordDisDelivery.setFreezeStatus(DeliveryOrderFreezeEnum.UN_FREEZE.getKey());
            ordDisDeliveryMapper.insertSelective(ordDisDelivery);
            // 非订单流配置创建捞单池明细
            // 天岁接入ERP，不再对接中科接口
            if (!DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDisDelivery.getSourceCode())
//            if (!OrgCodeConvertEnum.TS_MYT.getBizOrgCode().equals(ordDisDeliveryIn.getBizOrgCode()) && !DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDisDelivery.getSourceCode())
                    && DistributionWaysEnum.UNIFIEDDIS.getType().equals(ordDisDelivery.getDistributionType())) {
                disDeliveryOrderSalvageHandle.saveForManualCreateDeliveryOrder(ordDisDelivery, ordDisDeliveryIn.getAuditType());
            }
            String content = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_CREATE.getKey(), ordDisDelivery.getDeliveryOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDisDelivery.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                    content, new Date(),
                    ordDisDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        //删除旧数据，重新保存
        if (isDelete) {
            OrdDisDeliveryDetail ordDisDeliveryDetail = new OrdDisDeliveryDetail();
            ordDisDeliveryDetail.setDeliveryOrderId(ordDisDelivery.getId());
            ordDisDeliveryDetailService.delete(ordDisDeliveryDetail);
        }
        ordDisDeliveryDetailService.save(detailList, ordDisDelivery, orderPriority);

        return ordDisDelivery.getId();
    }

    /**
     * 将明细实体转为出参对象并封装进表头出参对象
     *
     * @param out
     * @param detailList
     */
    private void initOutDetailOutList(OrdDisDeliveryOut out, List<OrdDisDeliveryDetail> detailList) {
        List<OrdDisDeliveryDetailOut> dtlOutList = detailList.stream().map(item -> {
            OrdDisDeliveryDetailOut orderDetailOut = new OrdDisDeliveryDetailOut();
            com.edc.plugins.utils.bean.BeanUtils.copy(item, orderDetailOut);
            return orderDetailOut;
        }).collect(Collectors.toList());
        out.setDetailOutList(dtlOutList);
    }


    /**
     * 采购订单回传配销单
     *
     * @param transferNoticePurchaseVOList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String disPurchaseOrderToErp(List<TransferNoticePurchaseVO> transferNoticePurchaseVOList) {
        List<OrdDisDeliveryDetail> ordDisDeliveryDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(transferNoticePurchaseVOList)) {
            log.info("采购回传采购单数量集合长度为--{}", transferNoticePurchaseVOList.size());
            List<OrdDisDeliveryDetail> allDetails = new ArrayList<>();
            for (TransferNoticePurchaseVO transferNoticePurchaseVO : transferNoticePurchaseVOList) {
                //匹配到采购单明细
                OrdDeliveryDetailIn ordDeliveryDetailIn = OrdDeliveryDetailIn.builder().vendorCode(transferNoticePurchaseVO.getVendorCode())
                        .orderPriority(transferNoticePurchaseVO.getOrderPriority()).distributionType(DistributionWaysEnum.TRANSFER.getType())
                        .deliveryStatusCode(DeliveryOrderEnum.PREVIEWAPPROVED.getKey()).carryForwardCycle(transferNoticePurchaseVO.getCarryForwardCycle()).build();
                List<OrdDisDeliveryDetail> details = ordDisDeliveryDetailService.findOrdDisDeliveryDetail(ordDeliveryDetailIn);
                log.info("采购回传订单方匹配加盟配销单明细数量集合长度为--{}", details.size());
                List<GoodsDtlsVO> goodsDtls = transferNoticePurchaseVO.getGoodsDtls();

                //获取所有待匹配明细(不过滤订单方)
                OrdDeliveryDetailIn deliveryDetailIn = OrdDeliveryDetailIn.builder()
                        .orderPriority(transferNoticePurchaseVO.getOrderPriority()).distributionType(DistributionWaysEnum.TRANSFER.getType())
                        .deliveryStatusCode(DeliveryOrderEnum.PREVIEWAPPROVED.getKey()).carryForwardCycle(transferNoticePurchaseVO.getCarryForwardCycle()).build();
                List<OrdDisDeliveryDetail> ordDisDeliveryDetail = ordDisDeliveryDetailService.findOrdDisDeliveryDetail(deliveryDetailIn);
                log.info("采购回传不过滤订单方匹配加盟配销单明细数量集合长度为--{}", ordDisDeliveryDetail.size());
                allDetails.addAll(ordDisDeliveryDetail);
                //处理匹配到采购单明细
                for (GoodsDtlsVO goodsDtl : goodsDtls) {
                    List<OrdDisDeliveryDetail> itemDetails = details.stream().filter(item -> goodsDtl.getGoodsCode().equals(item.getGoodsCode())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(itemDetails)) {
                        continue;
                    }
                    itemDetails.forEach(e -> {
                        e.setPurchaseNo(transferNoticePurchaseVO.getPurchaseOrderNo());
                        e.setDistributionQuantity(e.getOrderQuantity());
                        e.setDistributionPackageQuantity(e.getDistributionQuantity()
                                .divide(e.getDistributionSpecificationNum(), NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
                        e.setDistributionAmount(e.getOrderQuantity().multiply(e.getOrderUnitPrice()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP));
                        if (StringUtils.isNotBlank(goodsDtl.getValidityCode())) {
                            e.setExpiry(goodsDtl.getValidityCode().substring(8, goodsDtl.getValidityCode().length() - 1));
                        }
                    });
                    ordDisDeliveryDetails.addAll(itemDetails);
                }
            }
            List<Long> detailIds = ordDisDeliveryDetails.stream().map(OrdDisDeliveryDetail::getId).collect(Collectors.toList());
            List<OrdDisDeliveryDetail> deliveryDetails = allDetails.stream().filter(item -> !detailIds.contains(item.getId())).distinct().collect(Collectors.toList());
            for (OrdDisDeliveryDetail deliveryDetail : deliveryDetails) {
                deliveryDetail.setDistributionQuantity(BigDecimal.ZERO);
                deliveryDetail.setDistributionPackageQuantity(BigDecimal.ZERO);
                deliveryDetail.setDistributionAmount(BigDecimal.ZERO);
                ordDisDeliveryDetails.add(deliveryDetail);
            }
        }
        log.info("采购回传需要更新数量的加盟配销单明细数量集合长度为--{}", ordDisDeliveryDetails.size());
        for (OrdDisDeliveryDetail deliveryDetail : ordDisDeliveryDetails) {
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
        int pages = ordDisDeliveryDetails.size() % pageSize == 0 ? ordDisDeliveryDetails.size() / pageSize : ordDisDeliveryDetails.size() / pageSize + 1;
        for (int i = 0; i < pages; i++) {
            ordDisDeliveryDetailService.batchUpdate(ordDisDeliveryDetails.subList(i * pageSize, i == pages - 1 ? ordDisDeliveryDetails.size() : (i + 1) * pageSize));
        }
        //修改配销单状态为已审核
        List<Long> deliveryOrderIdList = ordDisDeliveryDetails.stream().map(OrdDisDeliveryDetail::getDeliveryOrderId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deliveryOrderIdList)) {
            log.info("采购回传需要更新加盟配销单状态集合长度为--{}", deliveryOrderIdList.size());
            for (Long deliveryOrderId : deliveryOrderIdList) {
                //配销总数
                BigDecimal distributionQuantity = ordDisDeliveryDetails.stream().distinct().filter(item -> item.getDeliveryOrderId().equals(deliveryOrderId))
                        .map(OrdDisDeliveryDetail::getDistributionQuantity).reduce(BigDecimal::add).get();
                //配销金额
                BigDecimal distributionAmount = ordDisDeliveryDetails.stream().distinct().filter(item -> item.getDeliveryOrderId().equals(deliveryOrderId))
                        .map(OrdDisDeliveryDetail::getDistributionAmount).reduce(BigDecimal::add).get();
                UpdateOrdDisDeliveryIn updateOrdDisDeliveryIn = UpdateOrdDisDeliveryIn.builder().deliveryStatusCode(DeliveryOrderEnum.PREVIEWAPPROVED.getKey())
                        .distributionType(DistributionWaysEnum.TRANSFER.getType()).deliveryStatusCodeUpdate(DeliveryOrderEnum.APPROVED.getKey())
                        .deliveryOrderId(deliveryOrderId).distributionQuantity(distributionQuantity).distributionAmount(distributionAmount).build();
                int updateCount = ordDisDeliveryMapper.updateOrdDisDeliveryStatus(updateOrdDisDeliveryIn);
                OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(deliveryOrderId);
                if (updateCount == 0) {
                    log.error("配销单{}采购回传已更新", ordDisDelivery.getDeliveryOrderNo());
                }
                String content = MessageFormat.format(DeliveryOrderLogEnum.DELIVERY_ORDER_STATUS_UPDATE.getKey(), ordDisDelivery.getDeliveryOrderNo(),
                        DeliveryOrderEnum.PREVIEWAPPROVED.getValue(), DeliveryOrderEnum.APPROVED.getValue());
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                        String.valueOf(ordDisDelivery.getId()), OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), ordDisDelivery.getUpdater());
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
            }
            this.updateDisDelivery(deliveryOrderIdList);
            deliveryOrderIdList.forEach(id -> {
                String content = MessageFormat.format(DeliveryOrderLogEnum.TRANSFER_DELIVERY_ORDER_STATUS_UPDATE.getKey(),
                        DeliveryOrderEnum.getValueByKey(DeliveryOrderEnum.APPROVED.getKey()));
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(), String.valueOf(id),
                        OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(), content, new Date(), SystemConstant.SYSTEM_USER);
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
            });
        }
        return "采购回传信息修改完成";
    }

    /**
     * 采购回传库存后下发dts
     *
     * @param deliveryOrderIdList
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDisDelivery(List<Long> deliveryOrderIdList) {
        deliveryOrderIdList.forEach(item -> {
            OrdDisDeliveryOut ordDisDeliveryOut = new OrdDisDeliveryOut();
            OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(item);
            List<OrdDisDeliveryDetail> detailList = ordDisDeliveryDetailService.list(OrdDisDeliveryDetail.builder().deliveryOrderId(item).isDelete(NumberUtil.INTEGER_ZERO).build());
            BeanUtils.copy(ordDisDelivery, ordDisDeliveryOut);
            initOutDetailOutList(ordDisDeliveryOut, detailList);
            ordDisDeliveryOut.setDetailList(detailList);
            StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(ordDisDelivery.getStoreCode());
            if (Objects.isNull(storeOut)) {
                log.error("采购回传DTS，门店{}找不到", ordDisDelivery.getStoreCode());
            }
            BigDecimal distributionQuantity = ordDisDelivery.getDistributionQuantity();
            if (Objects.isNull(distributionQuantity) || BigDecimal.ZERO.compareTo(distributionQuantity) == NumberUtil.INTEGER_ZERO) {
                //整单无法匹配采购单，作废处理
                this.invalidTransferOrder(ordDisDelivery, detailList);
            } else {
//                //无法匹配配销单的明细做返款处理  2023-05-13中转单在已审核时不做缺货返款操作，在已发货时统一处理
//                disDeliveryOrderSalvageHandle.deliveryOrderStockOutRefundFund(ordDisDelivery);
                List<OrdDisDeliveryDetail> detailListOut = ordDisDeliveryOut.getDetailList();
                //过滤需要发dts的明细
                List<OrdDisDeliveryDetail> details = detailListOut.stream().filter(detail -> BigDecimal.ZERO.compareTo(detail.getDistributionQuantity()) != NumberUtil.INTEGER_ZERO).collect(Collectors.toList());
                //下发dts
                StockInfoOut stockInfoOut = stockServer.getTransInfo(ordDisDelivery.getStockCode());
                if (stockServer.isSendWms(ordDisDelivery.getStockCode(), stockInfoOut.getBizOrgCode())) {
                    disDeliveryOrderSalvageHandle.initUnificationBill(ordDisDeliveryOut, details, storeOut.getStoreId(), stockInfoOut.getBizOrgCode());
                }
            }
        });
    }

    @Override
    public List<OrderDeliverRequestOut> findTruncationDateTime(String startTime, String endTime, String distributionType, String bizOrgCode) {
        return ordDisDeliveryMapper.findTruncationDateTime(startTime, endTime, distributionType, bizOrgCode);
    }

    /**
     * 初始化库存调整操作入参(发货)
     *
     * @param ordDisDelivery
     * @param detailList
     * @param operatorInvIns
     * @return
     */
    @Override
    public List<StockFlowIn> disDeliveryOrderOptInv(OrdDisDelivery ordDisDelivery, List<OrdDisDeliveryDetail> detailList,
                                                    List<OperatorInvIn> operatorInvIns, StockInfoOut stockInfoOut) {
        // 门店库存调整
        List<StockFlowIn> stockFlowInList = Lists.newArrayList();
        for (OperatorInvIn operatorInvIn : operatorInvIns) {
            StockFlowIn stockFlowIn = new StockFlowIn();
            stockFlowIn.setBusinessType(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getCode());
            stockFlowIn.setSourceName(InvBusinessTypeEnum.DISTRIBUTIVE_OUT.getName());
            stockFlowIn.setOccurrenceLocation(operatorInvIn.getOccurrenceLocation());
            stockFlowIn.setFlowDate(ordDisDelivery.getDeliveryTime());
            stockFlowIn.setCreator(ordDisDelivery.getCreator());
            stockFlowIn.setOperationType(OrderTypeEnum.SHIPPED.getCode());
            stockFlowIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
            List<StockFlowGoodsIn> stockFlowGoodsIns = new ArrayList<>();
            for (OrdDisDeliveryDetail item : detailList) {
                if (Objects.isNull(item.getDeliveryQuantity())) {
                    continue;
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
                    //配销出-发货门店库存调整发生价取最新门店配销价 2023-06-09改为继承价
                    stockFlowGoodsIn.setPrice(item.getOrderUnitPrice());
                    stockFlowIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
                    stockFlowIn.setOrgCode(ordDisDelivery.getOrgCode());
                    if (StringUtils.isNotBlank(item.getExpiry())) {
                        stockFlowGoodsIn.setGoodsExpiryFlowList(Lists.newArrayList(new StockFlowGoodsExpiryIn(item.getExpiry(), applyQty)));
                    }
                }
                if (StockHappenLieEnum.WAREHOUSE.getCode().equals(stockFlowIn.getOccurrenceLocation())) {
                    applyQty = item.getDistributionQuantity().abs();
                    //配销出-发货大库库存调整发生价取最新仓储库存价
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
                stockFlowGoodsIn.setStockCode(ordDisDelivery.getStockCode());
                stockFlowGoodsIn.setStockName(stockInfoOut.getStockName());
                stockFlowGoodsIn.setWarehouseName(stockInfoOut.getWarehouseName());
                stockFlowGoodsIn.setWarehouseCode(stockInfoOut.getWarehouseCode());
                stockFlowGoodsIn.setStoreCode(ordDisDelivery.getStoreCode());
                stockFlowGoodsIn.setStoreName(ordDisDelivery.getStoreName());
                //库存发生位置
                stockFlowGoodsIn.setPosition(operatorInvIn.getOccurrenceLocation());
                //成本含税金额
                BigDecimal costTaxAmount = BigDecimal.ZERO;
                if (StockHappenLieEnum.STORE.getCode().equals(operatorInvIn.getOccurrenceLocation())) {
                    //门店成本税额为正值
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
                stockFlowGoodsIn.setSourceNo(ordDisDelivery.getDeliveryOrderNo());
                stockFlowGoodsIns.add(stockFlowGoodsIn);
            }
            stockFlowIn.setStockStoreFlowGoodsIn(stockFlowGoodsIns);
            stockFlowInList.add(stockFlowIn);
        }
        return stockFlowInList;
    }

    @Override
    public int countByStatusAndIdList(String deliveryOrderStatus, List<Long> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            return ordDisDeliveryMapper.countByStatusAndIdList(deliveryOrderStatus, idList);
        } else {
            return NumberUtil.INTEGER_ZERO;
        }
    }

    @Override
    public String exportDeliveryOrderDetailByOrder(DisDeliveryOrderIn deliveryOrderIn) {
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
            String deliveryNo = ordDisDelivDifferenceService.getDeliveryNoByDiffNo(deliveryOrderIn.getDifferenceOrderNo(), null);
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
        String fileName = "配销单明细".concat(DateUtil.format(now, DatePattern.PURE_DATETIME_MS_PATTERN)).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配销单明细",
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
    public List<DisNoSalvageDeliveryOrderOut> findNoSalvageDeliveryOrderList(List<Long> idList, String bizOrgCode) {
        return ordDisDeliveryMapper.findNoSalvageDeliveryOrderList(idList, DeliveryOrderEnum.PENDING.getKey(), bizOrgCode);
    }

    @Override
    public OrdDisDeliveryOut getDeliveryOrderOutByNo(String deliveryOrderNo) {
        OrdDisDelivery order = new OrdDisDelivery();
        order.setDeliveryOrderNo(deliveryOrderNo);
        order.setIsDelete(0);
        OrdDisDelivery entity = ordDisDeliveryMapper.selectOne(order);
        if (null == entity) {
            return null;
        }
        List<OrdDisDeliveryDetail> details = ordDisDeliveryDetailService.findDeliveryOrderDetails(entity.getId());
        OrdDisDeliveryOut ordDirDeliveryOut = new OrdDisDeliveryOut();
        BeanUtils.copy(entity, ordDirDeliveryOut);
        ordDirDeliveryOut.setDetailList(details);
        return ordDirDeliveryOut;
    }

    @Override
    public void checkDeliveryAmountSimilarity(String createTimeBegin, String createTimeEnd, String bizOrgCode) {
        Example example = new Example(OrdDisDelivery.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andBetween("createTime", createTimeBegin, createTimeEnd);
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(DeliveryOrderEnum.PENDING.getKey());
        orderStatusCodeList.add(DeliveryOrderEnum.PREVIEWAPPROVED.getKey());
        orderStatusCodeList.add(DeliveryOrderEnum.APPROVED.getKey());
        criteria.andIn("deliveryStatusCode", orderStatusCodeList);
        criteria.andEqualTo("bizOrgCode", bizOrgCode);
        criteria.andEqualTo("isDelete", ModelConst.DELETE.NO);
        List<OrdDisDelivery> ordDisDeliveryList = ordDisDeliveryMapper.selectByExample(example);
        for (OrdDisDelivery ordDisDelivery : ordDisDeliveryList) {
            StoreUnit storeUnit = storeCenterService.getStoreUnitByStoreCode(ordDisDelivery.getStoreCode(), bizOrgCode);
            String unitStr = "";
            if (storeUnit != null) {
                unitStr = "【" + storeUnit.getUnitCode() + "】" + storeUnit.getUnitName() + SystemConstant.SHORT_LINE;
            }
            WarningResultOut skuSimilarityWarningResultOut = this.checkOrderAmount(ordDisDelivery, unitStr, OrderWarningTypeEnum.DELIVERY_ORDER_LIMIT_AMOUNT.getErrorMessage());
            if (Boolean.TRUE.equals(skuSimilarityWarningResultOut.getCheckFlag())) {
                warningService.pushWarningMessage(WarningBusinessTypeEnum.DIS_DELIVERY_ORDER.getBusinessType(), OrderWarningTypeEnum.DELIVERY_ORDER_LIMIT_AMOUNT.getType(),
                        skuSimilarityWarningResultOut.getErrorMessage(), null, null, bizOrgCode);
            }
        }
    }

    @Override
    public List<OrdDisDelivery> findNeedFreezeDeliveryOrderList(String handleTime, String bizOrgCode) {
        return ordDisDeliveryMapper.findNeedFreezeDeliveryOrderList(handleTime, bizOrgCode);
    }

    @Override
    public List<OrderConfigFreezeDeliveryOrderOut> findOrderConfigDeliveryOrderOut(List<OrdDisDelivery> disDeliveryList, String bizOrgCode) {
        return ordDisDeliveryMapper.findOrderConfigDeliveryOrderOut(disDeliveryList, bizOrgCode);
    }

    @Override
    public List<FirstOrderFreezeDeliveryOrderOut> findFirstOrderDeliveryOrderOut(List<OrdDisDelivery> disDeliveryList, String bizOrgCode) {
        return ordDisDeliveryMapper.findFirstOrderDeliveryOrderOut(disDeliveryList, bizOrgCode);
    }

    @Override
    public Integer countNoInvalidByCycleIdAndNonDeliveryOrderId(Long orderCycleId, Long deliveryOrderId, String bizOrgCode) {
        return ordDisDeliveryMapper.countNoInvalidByCycleIdAndNonDeliveryOrderId(orderCycleId, deliveryOrderId, bizOrgCode);
    }

    @Override
    public Response payBeforeShipments(Long id, BigDecimal deliveryAmount) {
        OrdDisDelivery ordDisDelivery = ordDisDeliveryMapper.selectByPrimaryKey(id);
        if (Objects.isNull(ordDisDelivery)) {
            return Response.error("配销单不存在");
        }
        if (ordDisDelivery.getDistributionAmount().compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ZERO) {
            log.info("配销单{}配销金额{}不需要解冻", ordDisDelivery.getDeliveryOrderNo(), ordDisDelivery.getDistributionAmount());
            return Response.success("冻结金额为0，不需要解冻");
        }
        RechargeLiquidationIn rechargeLiquidationIn = null;
        if(deliveryAmount.abs().compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ONE){
            rechargeLiquidationIn = new RechargeLiquidationIn();
            rechargeLiquidationIn.setPayOrPrincipalCode(ordDisDelivery.getStoreCode());
            rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
            rechargeLiquidationIn.setRecipientPrincipalCode(ordDisDelivery.getBizOrgCode());
            rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
            rechargeLiquidationIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
            rechargeLiquidationIn.setBusinessNo(ordDisDelivery.getDeliveryOrderNo());
            rechargeLiquidationIn.setBusinessType(FundTypeEnum.DISTRIBUTION_SHIPMENTS.getCode());
            rechargeLiquidationIn.setLiquidationAmount(deliveryAmount.abs());
            rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
            rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
            rechargeLiquidationIn.setRemark(FundTypeEnum.DISTRIBUTION_SHIPMENTS.getName());
        }
        UnFrozenAndPayIn unFrozenAndPayIn = new UnFrozenAndPayIn();
        unFrozenAndPayIn.setRechargeLiquidationIn(rechargeLiquidationIn);
        unFrozenAndPayIn.setUnFrozenBusinessNo(ordDisDelivery.getDeliveryOrderNo());
        Response response = fundServer.unFrozenAndSettlement(unFrozenAndPayIn);
        if (Objects.nonNull(response) && response.isSuccess()) {
            return response;
        }
        log.error("配销单{}发货实扣异常{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
        // 发送钉钉
        String msg = MessageFormat.format(OrderWarningTypeEnum.PAY_BEFORE_DELIVERY_SHIPMENTS_FAIL.getErrorMessage(), ordDisDelivery.getDeliveryOrderNo(), ordDisDelivery.getStoreCode());
        warningService.pushDBIndexWarningMessage(warningRedisDB, erpOrdTopic, WarningBusinessTypeEnum.DIS_DELIVERY_ORDER.getBusinessType(),
                OrderWarningTypeEnum.PAY_BEFORE_DELIVERY_SHIPMENTS_FAIL.getType(),
                msg, null, null, ordDisDelivery.getBizOrgCode());
        return Response.error("账户实扣失败");
    }

    @Override
    public Integer countNoInvalidByFirstOrderAndNonDeliveryOrderId(Long firstOrderId, Long deliveryOrderId, String bizOrgCode) {
        return ordDisDeliveryMapper.countNoInvalidByFirstOrderAndNonDeliveryOrderId(firstOrderId, deliveryOrderId, bizOrgCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateFreeze(List<Long> idList, String loginUsername, String bizOrgCode) {
        return ordDisDeliveryMapper.batchUpdateFreeze(idList, loginUsername, bizOrgCode);
    }

    @Override
    public List<DisNoAuditDeliveryOrderInfoOut> findNoAuditDeliveryOrderList(String createTimeBegin, String createTimeEnd, String bizOrgCode) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(DeliveryOrderEnum.PENDING.getKey());
        orderStatusCodeList.add(DeliveryOrderEnum.PREVIEWAPPROVED.getKey());
        return ordDisDeliveryMapper.findNoAuditDeliveryOrderList(createTimeBegin, createTimeEnd, orderStatusCodeList, bizOrgCode);
    }

    @Override
    public OrdDisDelivery getOneByDeliveryOrderNoAndBizOrgCode(String deliveryOrderNo, String bizOrgCode) {
        OrdDisDelivery ordDisDelivery = new OrdDisDelivery();
        ordDisDelivery.setDeliveryOrderNo(deliveryOrderNo);
        ordDisDelivery.setBizOrgCode(bizOrgCode);
        ordDisDelivery.setIsDelete(ModelConst.DELETE.NO);
        return ordDisDeliveryMapper.selectOne(ordDisDelivery);
    }

    @Override
    public List<DisDeliveryOrderPrintOut> findPrintDataByIds(List<Long> ids) {
        // 仓位
        Map<String, StockInfoOut> stockMap = stockServer.findAll(UserUtil.getBizOrgCode());
        List<DisDeliveryOrderPrintOut> printOutList = new ArrayList<>();
        for (Long id : ids) {
            DisDeliveryOrderPrintOut orderPrintOut = new DisDeliveryOrderPrintOut();
            OrdDisDelivery ordDisDelivery = selectByPrimaryKey(id);
            if (ordDisDelivery == null) {
                throw new BusinessException("无效的配货单id！");
            }
            BeanUtils.copy(ordDisDelivery, orderPrintOut);
            StockInfoOut stockInfoOut = stockMap.get(orderPrintOut.getStockCode());
            if (stockInfoOut != null) {
                orderPrintOut.setStockName(stockInfoOut.getStockName());
            }
            orderPrintOut.setDeliveryQuantity(Objects.isNull(orderPrintOut.getDeliveryQuantity()) ? BigDecimal.ZERO : orderPrintOut.getDeliveryQuantity());
            orderPrintOut.setDeliveryStatusName(systemDictService.getSystemDictName(orderPrintOut.getDeliveryStatusCode()));
            List<DisDeliveryOrderDtlPrintOut> dtlPrintOutList = ordDisDeliveryDetailService.findPrintDtlByDeliveryId(id);
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
            OrdDisDeliveryOrderSigning orderSigning = ordDisDeliveryOrderSigningService.getDeliveryOrderSigningByDeliveryOrderId(id, ordDisDelivery.getBizOrgCode());
            if (Objects.nonNull(orderSigning)) {
                orderPrintOut.setSignature(orderSigning.getSignature());
            }
            printOutList.add(orderPrintOut);
        }
        return printOutList;
    }

//    /**
//     * @param deliveryOrder:
//     * @param goodsDetailList:
//     * @param deliveryTime:
//     * @Description: 中科发货回传
//     * @Author: ZhangYao
//     * @Date: 2023/8/14 14:55
//     * @return: com.edc.plugins.common.response.Response<java.lang.String>
//     **/
//    @Transactional(rollbackFor = Exception.class)
//    public Response<String> zkShippedToHd(OrdDisDelivery deliveryOrder,
//                                          List<HandleDeliveryOrderDetailIn> goodsDetailList,
//                                          LocalDateTime deliveryTime) {
//        List<OrdDisDeliveryDetail> disDeliveryDetailList = ordDisDeliveryDetailService.findDeliveryOrderDetails(deliveryOrder.getId());
//        if (CollectionUtils.isEmpty(disDeliveryDetailList)) {
//            return Response.error("配销单{}没有明细", deliveryOrder.getDeliveryOrderNo());
//        }
//        Map<String, BigDecimal> deliveryQuantityMap;
//        if (CollectionUtils.isEmpty(goodsDetailList)) {
//            deliveryQuantityMap = new HashMap<>();
//        } else {
//            deliveryQuantityMap = goodsDetailList.stream().collect(Collectors.toMap(HandleDeliveryOrderDetailIn::getGoodsCode, HandleDeliveryOrderDetailIn::getDeliveryQuantity, (s1, s2) -> s1));
//        }
//        Map<String, BigDecimal> finalDeliveryQuantityMap = deliveryQuantityMap;
//        List<UpdateDisDeliveryDetailIn> updateDeliveryDetailInList = disDeliveryDetailList.stream().map(disDeliveryDetail -> {
//            UpdateDisDeliveryDetailIn updateDisDeliveryDetailIn = new UpdateDisDeliveryDetailIn();
//            updateDisDeliveryDetailIn.setId(disDeliveryDetail.getId());
//            BigDecimal deliveryQuantity = finalDeliveryQuantityMap.get(disDeliveryDetail.getGoodsCode());
//            if (null == deliveryQuantity) {
//                deliveryQuantity = BigDecimal.ZERO;
//            }
//            updateDisDeliveryDetailIn.setDeliveryQuantity(deliveryQuantity);
//            return updateDisDeliveryDetailIn;
//        }).collect(Collectors.toList());
//        deliveryOrder.setUpdater(SystemConstant.SYSTEM_USER);
//        deliveryOrder.setUpdateTime(deliveryTime);
//        ordDisDeliveryService.updateDisDeliveryInfo(deliveryOrder, updateDeliveryDetailInList, false);
//        List<TakeDisDeliveryOrderGoodsIn> list = ordDisDeliveryDetailService.findTakeDisDeliveryOrderGoodsListByDeliveryOrderId(deliveryOrder.getId());
//        TakeDisDeliveryOrderIn takeDisDeliveryOrderIn = new TakeDisDeliveryOrderIn();
//        takeDisDeliveryOrderIn.setDeliveryOrderId(deliveryOrder.getId());
//        takeDisDeliveryOrderIn.setTakeDeliveryOrderGoodsInList(list);
//        takeDisDeliveryOrderIn.setTakeRemark("中科发货系统自动收货");
//        takeDisDeliveryOrderIn.setLoginUsername(SystemConstant.SYSTEM_USER);
//        takeDisDeliveryHandle.takeDelivery(takeDisDeliveryOrderIn, deliveryOrder);
//        return Response.success();
//    }
//


    @Override
    public Response<String> asyncImportDelivery(String fileId, String loginUsername, String loginBizOrgCode) {
        String key = DisSystemConstant.CHECK_DIS_ORDER_DELIVERY_IMPORT_ONLY_ONE + loginBizOrgCode;
        if (!redisService.setIfAbsent(key, loginUsername, 10L, TimeUnit.MINUTES)) {
            return Response.error("当前有用户正在异步上传配销单，请稍后尝试");
        }
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (bytes == null) {
            redisService.del(key);
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DisDeliveryAsyncImportListener listener = new DisDeliveryAsyncImportListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportDisDeliveryOrder.class, listener).headRowNumber(NumberUtil.INTEGER_ONE).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > NumberUtil.INTEGER_ZERO) {
            redisService.del(key);
            return Response.error(listener.getImportErrorMessage(totalErrorMap));
        }
        disDeliveryImportHandle.handleDeliveryListAsyncImport(listener.getDeliveryOrders(), loginUsername, loginBizOrgCode, "", key);
        return Response.success("开始导入，请耐心等待，切勿重复操作！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveAsyncImportDeliveryOrder(List<OrdDisDeliveryIn> ordDisDeliveryInList) {
        List<Long> deliveryOrderIdList = Lists.newArrayList();
        ordDisDeliveryInList.forEach(ordDisDeliveryIn -> {
            Long orderId = this.saveOrUpdate(ordDisDeliveryIn);
            deliveryOrderIdList.add(orderId);
        });
        return deliveryOrderIdList;
    }

    @Override
    public List<OrdDisDeliveryIn> initDeliveryOrderAndDetailForAsyncImport(List<ImportDisDeliveryOrder> deliveryOrderList,
                                                                           Map<String, StoreInfo> storeInfoMap, String loginUsername) {
        List<OrdDisDeliveryIn> ordDisDeliveryInList = Lists.newArrayList();
        Map<String, List<ImportDisDeliveryOrder>> map = deliveryOrderList.stream().collect(Collectors.groupingBy(item -> item.getWrhCode() + "_" + item.getStockCode() + "_" + item.getStoreCode()));
        map.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            OrdDisDeliveryIn ordDisDeliveryIn = new OrdDisDeliveryIn();
            ImportDisDeliveryOrder importDisDeliveryOrder = v.get(NumberUtil.INTEGER_ZERO);
            StoreInfo storeInfo = storeInfoMap.get(importDisDeliveryOrder.getStoreCode());
            String bizOrgCode = storeInfo.getBizOrgCode();
            ordDisDeliveryIn.setStoreCode(importDisDeliveryOrder.getStoreCode());
            ordDisDeliveryIn.setStoreName(Objects.isNull(storeInfo) ? "" : storeInfo.getStoreName());
            ordDisDeliveryIn.setStockCode(importDisDeliveryOrder.getStockCode());
            ordDisDeliveryIn.setWrhCode(importDisDeliveryOrder.getWrhCode());
            ordDisDeliveryIn.setBizOrgCode(bizOrgCode);
            ordDisDeliveryIn.setOrgCode(OrgCodeConvertEnum.getOrgCodeByBizOrgCode(bizOrgCode));
            ordDisDeliveryIn.setDistributionType(importDisDeliveryOrder.getDistributionType());
            ordDisDeliveryIn.setRemark(importDisDeliveryOrder.getRemark());
            ordDisDeliveryIn.setCreator(loginUsername);
            ordDisDeliveryIn.setUpdater(loginUsername);
            List<OrdDisDeliveryDetail> ordDisDeliveryDetails = new ArrayList<>();
            for (ImportDisDeliveryOrder disDeliveryOrder : v) {
                OrdDisDeliveryDetail ordDisDeliveryDetail = this.initOrdDisDeliveryDetailByAsyncImport(bizOrgCode, disDeliveryOrder, importDisDeliveryOrder.getDistributionType());
                if (Objects.isNull(ordDisDeliveryDetail)) {
                    continue;
                }
                ordDisDeliveryDetails.add(ordDisDeliveryDetail);
            }
            ordDisDeliveryIn.setDetailList(ordDisDeliveryDetails);
            ordDisDeliveryIn.setSourceCode(DeliveryOrderSourceCodeEnum.MANUAL.getType());
            ordDisDeliveryIn.setAuditType(SalvageAuditTypeEnum.WAIT_MANUAL_AUDIT.getCode());
            ordDisDeliveryInList.add(ordDisDeliveryIn);

        });
        return ordDisDeliveryInList;
    }

    @Override
    public Page<QueryDisDeliveryForReturnOut> findDeliveryOrderForReturn(DisDeliveryOrderIn deliveryOrderIn) {
        String loginBizOrgCode = deliveryOrderIn.getBizOrgCode();
        Map<String, StockInfoOut> authOrgStockMap = stockServer.findByAuthOrg(deliveryOrderIn.getBizOrgCode());
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
        List<QueryDisDeliveryForReturnOut> deliveryOrderOutList = ordDisDeliveryMapper.findDeliveryOrderForReturnByPage(deliveryOrderIn);
        deliveryOrderOutList.forEach(queryDirDeliveryForReturnOut -> {
            StockInfoOut stockInfoOut = authOrgStockMap.get(queryDirDeliveryForReturnOut.getStockCode());
            if (Objects.isNull(stockInfoOut)) {
                throw new BusinessException(queryDirDeliveryForReturnOut.getStockCode() + "无此仓位信息");
            }
            queryDirDeliveryForReturnOut.setStockNameStr(stockInfoOut.getStockName() + "[" + stockInfoOut.getStockCode() + "]");
            queryDirDeliveryForReturnOut.setReceiveTimeStr(DateUtils.format(queryDirDeliveryForReturnOut.getReceiveTime()));
        });
        Page<QueryDisDeliveryForReturnOut> page = new Page<>(deliveryOrderIn);
        page.setList(deliveryOrderOutList);
        return page;
    }

    @Override
    public List<DisStoreDeliveryNoInfoForAppOut> findStoreDeliveryInfoList(String storeCode) {
        List<DisStoreDeliveryNoInfoForAppOut> deliveryOrderList = ordDisDeliveryMapper.findStoreDeliveryForAppReturnList(storeCode, LocalDateTime.now().minusHours(72));
        if (CollectionUtils.isEmpty(deliveryOrderList)) {
            return null;
        }
        deliveryOrderList.forEach(item -> {
            List<OrdDisDeliveryDetail> detailList = ordDisDeliveryDetailService.findDeliveryOrderDetails(item.getId());
            item.setDetailList(detailList);
        });
        return deliveryOrderList;
    }


    /**
     * 校验配销单金额
     *
     * @param targetOrder
     * @param unitStr
     * @param msgTemplate
     * @return
     */
    private WarningResultOut checkOrderAmount(OrdDisDelivery targetOrder, String unitStr, String msgTemplate) {
        boolean flag = false;
        WarningResultOut warningResultOut = new WarningResultOut();
        if (targetOrder.getOrderAmount().compareTo(new BigDecimal("5000")) < 0) {
            warningResultOut.setCheckFlag(flag);
            return warningResultOut;
        }
        // 对比该门店配销单金额超当天前30笔订单（所有有效订单（不含已作废状态）平均金额的1倍
        List<BigDecimal> requestOrderTotalAmountList = this.findThirtyDaysDeliveryOrderTotalAmountByStoreCode(targetOrder.getStoreCode(), targetOrder.getBizOrgCode(), targetOrder.getStockCode());
        String errorMessage = null;
        if (CollectionUtils.isNotEmpty(requestOrderTotalAmountList)) {
            BigDecimal sumPaidAmount = requestOrderTotalAmountList.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            // 金额对比值
            BigDecimal contrastAmount = sumPaidAmount.divide(new BigDecimal(requestOrderTotalAmountList.size()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN).multiply(BigDecimal.valueOf(NumberUtils.INTEGER_TWO));
            if (targetOrder.getOrderAmount().compareTo(contrastAmount) >= 0) {
                errorMessage = MessageFormat.format(msgTemplate, StoreOrderWarningTypeEnum.FRANCHISE.getName(), WarningBusinessTypeEnum.DIS_DELIVERY_ORDER.getName(), unitStr + targetOrder.getStoreCode()
                        , targetOrder.getDeliveryOrderNo(), targetOrder.getOrderAmount());
                log.info(errorMessage);
                log.info("门店{}配销单单号：{}即将发送金额过大预警", targetOrder.getStoreCode(), targetOrder.getDeliveryOrderNo());
                flag = true;
            }
        }
        warningResultOut.setCheckFlag(flag);
        warningResultOut.setErrorMessage(errorMessage);
        return warningResultOut;
    }

    private List<BigDecimal> findThirtyDaysDeliveryOrderTotalAmountByStoreCode(String storeCode, String bizOrgCode, String stockCode) {
        return ordDisDeliveryMapper.findThirtyDaysDeliveryOrderTotalAmountByStoreCode(storeCode, bizOrgCode, stockCode, DeliveryOrderEnum.INVALID.getKey());
    }


    /**
     * 为异步导出查询配货单明细方法
     *
     * @param deliveryOrderIn
     * @return
     */
    private List<AsyncExcelDeliveryOrderDetail> findListForAsyncExportPage(DisDeliveryOrderIn deliveryOrderIn) {
        List<AsyncExcelDeliveryOrderDetail> asyncExcelDeliveryOrderDetails = ordDisDeliveryMapper.findListForAsyncExportByPage(deliveryOrderIn);
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

    private void setAutoTakeTime(OrdDisDelivery deliveryOrder) {
        OrdDisDelivRequest disDelivRequest = requestOrderHandle.getRequestOrderByDeliveryOrderIdAndBizOrgCode(deliveryOrder.getId(), deliveryOrder.getBizOrgCode());
        if (Objects.nonNull(disDelivRequest)) {
            Integer orderCycleId = disDelivRequest.getOrderCycleId();
            if (!DeliveryOrderEnum.SHIPPED.getKey().equals(deliveryOrder.getDeliveryStatusCode())) {
                throw new BusinessException("配销单" + deliveryOrder.getDeliveryOrderNo() + "设置自动收货时间时状态不正确");
            }
            OrderProcessConfigItemOut autoTakeDeliveryRuleItem = disDeliveryOrderConfigHandle.getAutoTakeDeliveryRule(orderCycleId, deliveryOrder.getBizOrgCode());
            if (Objects.isNull(autoTakeDeliveryRuleItem)) {
                OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(orderCycleId, deliveryOrder.getBizOrgCode());
                log.info("配销单：{}，门店" + orderCycle.getStoreCode() + "的" + orderCycle.getShortOrderType() + "（" + orderCycle.getTruncationDateTime() + "）" + "未找到配销单是否自动收货配置项", deliveryOrder.getDeliveryOrderNo());
                return;
            }
            // 如果是非自动收货
            if (OrderCycleProcessConfigItemCodeEnum.MANUAL_RECEIVE.getCode().equals(autoTakeDeliveryRuleItem.getItemCode())) {
                return;
            }
            log.info("配销单{}即将设置自动收货时间", deliveryOrder.getDeliveryOrderNo());
            if (StringUtils.isNotBlank(autoTakeDeliveryRuleItem.getItemValue())) {
                Integer value = Integer.parseInt(autoTakeDeliveryRuleItem.getItemValue());
                LocalDateTime autoTakeDeliveryTime = deliveryOrder.getDeliveryTime().plusHours(value);
                deliveryOrder.setAutoTakeDeliveryTime(autoTakeDeliveryTime);
                log.info("配销单{}自动收货时间1------------------设置为{}", deliveryOrder.getDeliveryOrderNo(), deliveryOrder.getAutoTakeDeliveryTime());
            }
        }
    }
}
