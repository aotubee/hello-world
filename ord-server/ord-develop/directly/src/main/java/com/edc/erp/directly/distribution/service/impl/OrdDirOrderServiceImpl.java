package com.edc.erp.directly.distribution.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.warning.LogisticsMessageQueryIn;
import com.edc.erp.common.model.in.warning.QueryPaidTransferOrderIn;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.stock.StockTransInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.model.out.warning.LogisticsMessageOrderOut;
import com.edc.erp.common.model.out.warning.WarningResultOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.distribution.handle.OrderDetailHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDetailMapper;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDistributionMapper;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderMapper;
import com.edc.erp.directly.distribution.model.excel.ExcelDirOrderOut;
import com.edc.erp.directly.distribution.model.in.BatchInvalidOrderIn;
import com.edc.erp.directly.distribution.model.in.DirOrderQueryIn;
import com.edc.erp.directly.distribution.model.in.OrderDetailIn;
import com.edc.erp.directly.distribution.model.in.OrderIn;
import com.edc.erp.directly.distribution.model.out.DirOrderOut;
import com.edc.erp.directly.distribution.model.out.OrderDetailInfoOut;
import com.edc.erp.directly.distribution.model.out.OrderDetailOut;
import com.edc.erp.directly.distribution.model.out.OrderSummaryOut;
import com.edc.erp.directly.distribution.service.OrdDirOrderService;
import com.edc.erp.directly.distribution.service.OrdDirOrderTrackService;
import com.edc.erp.directly.enumeration.*;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.export.AsyncExportExecutor;
import com.edc.plugins.export.ExportExcelByPage;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单表(OrdDirOrder)表服务实现类
 *
 * @author wanglidong
 * @since 2022-11-15 16:23:14
 */
@Slf4j
@Service
public class OrdDirOrderServiceImpl extends BaseServiceImpl<OrdDirOrder> implements OrdDirOrderService {

    @Autowired
    private AsyncLogService asyncLogService;
    @Autowired
    private OrderHandle orderHandle;
    @Autowired
    private OrdDirOrderMapper ordDirOrderMapper;
    @Autowired
    private StoreCenterService storeCenterService;
    @Autowired
    private SystemDictService systemDictService;
    @Autowired
    private OrderDetailHandle orderDetailHandle;
    @Autowired
    private OrdDirOrderTrackService ordDirOrderTrackService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OrderGoodsServer orderGoodsServer;
    @Autowired
    private OrdDirOrderDetailMapper ordDirOrderDetailMapper;
    @Autowired
    private AsyncExportExecutor asyncExportExecutor;
    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;
    @Autowired
    private OrdDirOrderDistributionMapper ordDirOrderDistributionMapper;


    /**
     * 根据订货单id查询订货单明细信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrderDetailInfoOut getOrderDetailInfoOut(Long orderId, String bizOrgCode) {
        OrderDetailInfoOut orderDetailInfoOut = new OrderDetailInfoOut();
        OrdDirOrder ordDirOrder = this.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
        if (Objects.isNull(ordDirOrder)) {
            throw new BusinessException("不存在的订货单");
        }
        BeanUtils.copy(ordDirOrder, orderDetailInfoOut);
        OrderIn orderIn = new OrderIn();
        orderIn.setId(Math.toIntExact(orderId));
        orderIn.setBizOrgCode(bizOrgCode);
        Page<DirOrderOut> orderOutPage = this.findOrderOutPage(orderIn);
        DirOrderOut dirOrderOut = orderOutPage.getList().get(0);
        orderDetailInfoOut.setCutOffTime(dirOrderOut.getTruncationDateTime());
        orderDetailInfoOut.setOrderTypeCode(dirOrderOut.getOrderTypeCode());
        orderDetailInfoOut.setOrderTypeName(dirOrderOut.getOrderTypeName());
        orderDetailInfoOut.setStoreName(dirOrderOut.getStoreName());
        orderDetailInfoOut.setRequestOrderNo(dirOrderOut.getRequestOrderNo());
        //订货单状态转中文
        orderDetailInfoOut.setOrderStatusCodeStr(OrderStatusEnum.getValueByKey(orderDetailInfoOut.getOrderStatusCode()));
        //订货类型转中文
        orderDetailInfoOut.setSourceCodeStr(SourceTypeEnum.getValueByKey(orderDetailInfoOut.getSourceCode()));
        OrderDetailIn orderDetailIn = new OrderDetailIn();
        orderDetailIn.setOrderId(orderId);
        List<OrderDetailOut> OrderDetailOuts = ordDirOrderDetailMapper.findOrderDetailOutByPage(orderDetailIn);
        this.initBaseAndGiftOrderDetails(OrderDetailOuts, bizOrgCode);
        orderDetailInfoOut.setOrderDetailList(OrderDetailOuts);
        String distributionOrderNo = ordDirOrderDistributionMapper.getDistributionOrderNoByOrderId(orderId, bizOrgCode);
        orderDetailInfoOut.setDistributionOrderNo(distributionOrderNo);
        return orderDetailInfoOut;
    }

    /**
     * 中文翻译
     *
     * @param orderDetailOut
     * @param bizOrgCode
     */
    void transOrderDetailOut(OrderDetailOut orderDetailOut, String bizOrgCode) {
        StockTransInfoOut stockOut = orderGoodsServer.getTransInfo(orderDetailOut.getPosition(), bizOrgCode);
        // 仓位
        orderDetailOut.setPositionName(Objects.nonNull(stockOut) ? stockOut.getStockName() : "");
        // 配货金额
        BigDecimal originalUnitTotalPrice = orderDetailOut.getOriginalPrice() == null ? BigDecimal.ZERO : orderDetailOut.getOriginalPrice().multiply(orderDetailOut.getQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP);
        orderDetailOut.setOriginalUnitTotalPrice(originalUnitTotalPrice);
        // 支付金额
        BigDecimal payUnitTotalPrice = orderDetailOut.getOrderUnitPrice() == null ? BigDecimal.ZERO : orderDetailOut.getOrderUnitPrice().multiply(orderDetailOut.getQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP);
        orderDetailOut.setPayUnitTotalPrice(payUnitTotalPrice);
        // 实付金额
        BigDecimal realUnitTotalPrice = orderDetailOut.getRealUnitPrice() == null ? BigDecimal.ZERO : orderDetailOut.getRealUnitPrice().multiply(orderDetailOut.getQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP);
        orderDetailOut.setRealUnitTotalPrice(realUnitTotalPrice);

        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setGoodsCode(orderDetailOut.getGoodsCode());
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        OrderGoodsOut orderGoodsOut = orderGoodsServer.getOrderGoods(orderGoodsIn);
        if (Objects.nonNull(orderGoodsOut)) {
            // 分类
            String category = orderGoodsOut.getSortName();
            orderDetailOut.setSmallSortStr(category);
        }
        orderDetailOut.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(orderDetailOut.getInvoiceType()));
        orderDetailOut.setActivityCode(orderDetailOut.getActivityNo());
        orderDetailOut.setActivityType(ActivityTypeEnum.getTagNameByCode(orderDetailOut.getActivityType()));
    }

    /**
     * 查询明细赠品信息
     *
     * @param orderDetailOuts
     * @param bizOrgCode
     * @return
     */
    void initBaseAndGiftOrderDetails(List<OrderDetailOut> orderDetailOuts, String bizOrgCode) {
        Map<String, List<OrderDetailOut>> giftMap = orderDetailOuts.stream().filter(dtl -> NumberUtils.INTEGER_ONE.equals(dtl.getIsGift())).collect(Collectors.groupingBy(OrderDetailOut::getBaseGoodsCode));
        for (OrderDetailOut item : orderDetailOuts) {
            this.transOrderDetailOut(item, bizOrgCode);
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
            //配送方式转中文
            item.setDistributionTypeStr(DistributionWaysEnum.getNameByType(item.getDistributionType()));
            if (NumberUtils.INTEGER_ZERO.equals(item.getIsGift())) {
                if (giftMap.containsKey(item.getGoodsCode())) {
                    List<OrderDetailOut> ordDisOrderDetails = giftMap.get(item.getGoodsCode());
                    List<OrderDetailOut> detailOuts = new ArrayList<>();
                    for (OrdDirOrderDetail detail : ordDisOrderDetails) {
                        OrderDetailOut detailOut = new OrderDetailOut();
                        BeanUtils.copy(detail, detailOut);
                        this.transOrderDetailOut(detailOut, bizOrgCode);
                        detailOuts.add(detailOut);
                    }
                    item.setGiftOrderDetails(detailOuts);
                }
            }
        }
    }

    /**
     * 直营订货单分页查询
     *
     * @param orderIn
     * @return
     */
    @Override
    public Page<DirOrderOut> findOrderOutPage(OrderIn orderIn) {
        // 当门店名称，门店区域等不为空时，先得到所有的门店代码集合
        if (StringUtils.isNotEmpty(orderIn.getStoreArea()) || StringUtils.isNotEmpty(orderIn.getStoreName())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(orderIn.getStoreArea(),
                    orderIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return new Page<>(orderIn);
            }
            orderIn.setStoreCodeList(storeCodeList);
        }
        List<DirOrderOut> orderOuts = ordDirOrderMapper.findDirOrderByPage(orderIn);
        orderOuts.stream().forEach(orderOut -> {
            this.transChineseName(orderOut);
        });
        Page<DirOrderOut> resPage = new Page(orderIn);
        resPage.setList(orderOuts);
        return resPage;
    }

    /**
     * 根据id、业务组织代码获取直营订货单信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrdDirOrder getOrderByIdAndBizOrgCode(Long orderId, String bizOrgCode) {
        return ordDirOrderMapper.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
    }

    /**
     * 导出直营订货单
     *
     * @param orderIn
     * @return
     */
    @Override
    public String exportOrdDirOrder(OrderIn orderIn) {
        // 设置每次查询条数
        orderIn.setPageSize(10000);
        String title = "直营订货单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "直营订货单列表",
                        // 导出模板实体
                        ExcelDirOrderOut.class,
                        // 分页查询对象
                        orderIn,
                        // 分页查询方法
                        page -> {
                            Page<DirOrderOut> outPage = this.findOrderOutPage(orderIn);
                            List<DirOrderOut> orderOuts = outPage.getList();
                            if (CollectionUtil.isEmpty(orderOuts)) {
                                return null;
                            }
                            List<ExcelDirOrderOut> excelDirOrderOutList = new ArrayList<>();
                            for (int i = 0; i < orderOuts.size(); i++) {
                                ExcelDirOrderOut excelDirOrderOut = new ExcelDirOrderOut();
                                excelDirOrderOut.setNo(i + 1);
                                BeanUtil.copyProperties(orderOuts.get(i), excelDirOrderOut);
                                excelDirOrderOut.setOrderTypeName(excelDirOrderOut.getOrderTypeName() + "【" + orderOuts.get(i).getOrderTypeCode() + "】");
                                excelDirOrderOutList.add(excelDirOrderOut);
                            }
                            return excelDirOrderOutList;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    /**
     * 修改直营订货单状态
     *
     * @param id
     * @param bizOrgCode
     * @param orderStatusCode
     * @param updater
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long id, String bizOrgCode, String orderStatusCode, String updater) {
        ordDirOrderMapper.updateOrderStatus(id, bizOrgCode, orderStatusCode, updater);
    }

    /**
     * 截单未支付
     *
     * @param order
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateForTruncationOrderByNoPay(OrdDirOrder order) {
        return ordDirOrderMapper.updateForTruncationOrderByNoPay(order);
    }

    /**
     * 根据截单时间批量作废
     *
     * @param orderList
     * @param orderStatusCode
     * @param updater
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInvalidForCutOrder(List<OrdDirOrder> orderList, String orderStatusCode, String updater) {
        this.batchUpdateOrder(orderList, orderStatusCode, updater);
    }

    /**
     * 批量修改订单
     *
     * @param orderList
     * @param orderStatusCode
     * @param updater
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateOrder(List<OrdDirOrder> orderList, String orderStatusCode, String updater) {
        List<BusinessLog> logContentList = Lists.newArrayList();
        orderList.stream().forEach(order -> {
            order.setUpdater(updater);
            String beforeOrderStatusCode = order.getOrderStatusCode();
            order.setOrderStatusCode(orderStatusCode);
            String content = MessageFormat.format(OrderLogEnum.CUT_LESS_MIN_AMOUNT_INVALID_SINGLE_MODEL_MARGE.getKey(), order.getOrderNo(),
                    OrderStatusEnum.getValueByKey(beforeOrderStatusCode), OrderStatusEnum.getValueByKey(orderStatusCode));
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), String.valueOf(order.getId()),
                    OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), updater);
            logContentList.add(businessLog);
            // 不足起订额整单作废推送订单追踪日志
            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.CUT_LESS_MIN_AMOUNT_INVALID_SINGLE_MODEL_MARGE.getTemplate(), order.getOrderNo());
            ordDirOrderTrackService.pushRedisOrderTrackMessage(order.getOrderNo(), order.getStoreCode(),
                    OrderTrackStatusEnum.INVALID_ORDER.getName(), trackLog, order.getBizOrgCode(), order.getCreator(), order.getCreateTime());
        });
        ordDirOrderMapper.batchUpdateOrder(orderList);
        logContentList.forEach(businessLog -> asyncLogService.sendAsyncSaveLogByMq(businessLog));
    }

    /**
     * 中文转化
     *
     * @param orderOut
     */
    void transChineseName(DirOrderOut orderOut) {
        orderOut.setOrderStatusCodeStr(systemDictService.getSystemDictName(orderOut.getOrderStatusCode()));
        orderOut.setSourceCodeStr(systemDictService.getSystemDictName(orderOut.getSourceCode()));
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(orderOut.getStoreCode());
        if (storeOut != null) {
            orderOut.setStoreName(storeOut.getStoreName());
            orderOut.setStoreAreaStr(storeOut.getArea());
        }
    }

    /**
     * 修改订货单
     *
     * @param order
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OrdDirOrder order) {
        ordDirOrderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 保存订货单
     *
     * @param ordDirOrder
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OrdDirOrder ordDirOrder) {
        ordDirOrderMapper.insertSelective(ordDirOrder);
        String content = MessageFormat.format(OrderLogEnum.ORDER_CREATE.getKey(), ordDirOrder.getOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), ordDirOrder.getId().toString(),
                OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), ordDirOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 批量修改要货单订货单关联关系
     *
     * @param orderList
     * @param id
     * @param systemUser
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateOrderRequestRel(List<OrdDirOrder> orderList, Long id, String systemUser) {
        for (OrdDirOrder ordDirOrder : orderList) {
            ordDirOrder.setRequestOrderId(id);
            ordDirOrder.setUpdater(systemUser);
        }
        ordDirOrderMapper.batchUpdateOrderRequestRel(orderList);
    }

    /**
     * 根据要货单id查询关联的订货单列表
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public List<OrdDirOrder> findOrderByRequestOrderIdAndBizOrgCode(Long requestOrderId, String bizOrgCode) {
        OrdDirOrder ordDirOrder = new OrdDirOrder();
        ordDirOrder.setRequestOrderId(requestOrderId);
        ordDirOrder.setBizOrgCode(bizOrgCode);
        ordDirOrder.setIsDelete(ModelConst.DELETE.NO);

        List<OrdDirOrder> list = ordDirOrderMapper.select(ordDirOrder);
        return list;
    }

    /**
     * 直营订货单汇总
     *
     * @param orderIn
     * @return
     */
    @Override
    public OrderSummaryOut orderSummary(OrderIn orderIn) {
        orderIn.setIsDelete(ModelConst.DELETE.NO);
        // 当门店名称，门店区域等不为空时，先得到所有的门店代码集合
        if (StringUtils.isNotEmpty(orderIn.getStoreArea()) || StringUtils.isNotEmpty(orderIn.getStoreName())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(orderIn.getStoreArea(),
                    orderIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return null;
            }
            orderIn.setStoreCodeList(storeCodeList);
        }
        return ordDirOrderMapper.orderSummary(orderIn);
    }

    /**
     * 批量作废直营订货单
     *
     * @param batchInvalidOrderIn
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response batchInvalidOrder(BatchInvalidOrderIn batchInvalidOrderIn) {
        StringJoiner repeatMsg = new StringJoiner(",");
        StringJoiner statusErrorMsg = new StringJoiner(",");
        for (Long orderId : batchInvalidOrderIn.getOrderIdList()) {
            OrdDirOrder ordDisOrder = this.getOrderByIdAndBizOrgCode(orderId, batchInvalidOrderIn.getBizOrgCode());
            if (Objects.isNull(ordDisOrder)) {
                continue;
            }
            if (OrderStatusEnum.TO_REQUEST_ORDER.getKey().equals(ordDisOrder.getOrderStatusCode())) {
                statusErrorMsg.add(ordDisOrder.getOrderNo());
                continue;
            }
            if (OrderStatusEnum.INVALID.getKey().equals(ordDisOrder.getOrderStatusCode())) {
                repeatMsg.add(ordDisOrder.getOrderNo());
                continue;
            }
            orderHandle.invalidOrder(ordDisOrder, batchInvalidOrderIn.getUsername(), batchInvalidOrderIn.getBizOrgCode());
        }

        StringJoiner errorMsg = new StringJoiner(",");
        if (StringUtils.isNotBlank(repeatMsg.toString())) {
            errorMsg.add(repeatMsg + "订货单状态为" + OrderStatusEnum.INVALID.getValue() + "，请勿重复作废");
        }
        if (StringUtils.isNotBlank(statusErrorMsg.toString())) {
            errorMsg.add(statusErrorMsg + "订货单状态为" + OrderStatusEnum.TO_REQUEST_ORDER.getValue() + "，不能作废");
        }
        String content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_ORDER_JOIN_INVALID.getKey(), errorMsg.toString());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER_DISTRIBUTION_ORDER.getName(), String.valueOf(batchInvalidOrderIn.getDistributionOrderId()),
                OrdLogTypeEnum.DIR_ORDER_DISTRIBUTION_ORDER.getCode(), content, new Date(), batchInvalidOrderIn.getUsername());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success(errorMsg.toString());
    }

    /**
     * 根据id查询订货单出参信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public DirOrderOut getOrderOutById(Long orderId, String bizOrgCode) {
        DirOrderOut orderOut = ordDirOrderMapper.getByIdAndOrg(orderId, bizOrgCode);
        if (orderOut != null) {
            this.transChineseName(orderOut);
        }
        return orderOut;
    }

    /**
     * 根据要货单主键获取实付单价
     *
     * @param requestOrderId
     * @return
     */
    @Override
    public BigDecimal getRealUnitPrice(Long requestOrderId) {
        List<BigDecimal> realUnitPrices = ordDirOrderMapper.findRealUnitPrice(requestOrderId);
        if (CollectionUtils.isNotEmpty(realUnitPrices)) {
            BigDecimal min = Collections.min(realUnitPrices);
            return min;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<OrdDirOrder> findNeedCutOrderListByParameter(DirOrderQueryIn dirOrderQueryIn) {
        return ordDirOrderMapper.findNeedCutOrderListByParameter(dirOrderQueryIn);
    }

    /**
     * 门店上下限跑货订单金额超当天前30笔订单
     *
     * @param storeCode
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    @Override
    public List<BigDecimal> findThirtyDaysRequestOrderTotalAmountByStoreCodeAndOrderTypeConfigId(String storeCode, Integer orderTypeConfigId, String bizOrgCode) {
        return ordDirOrderMapper.findThirtyDaysRequestOrderTotalAmountByStoreCodeAndOrderTypeConfigId(storeCode, orderTypeConfigId, bizOrgCode);
    }

    /**
     * 校验订货清单商品相似度
     *
     * @param targetOrder
     * @param unitStr
     * @param msgTemplate
     * @return
     */
    @Override
    public WarningResultOut checkOrderGoodsSimilarity(OrdDirOrder targetOrder, String unitStr, String msgTemplate) {
        boolean flag = false;
        // 对比goodsCode
        List<OrdDirOrderDetail> targetOrderGoodsDetailsList = orderDetailHandle.findOrderDetailListByOrderId(targetOrder.getId());
        Map<String, BigDecimal> skuQuantityMap = targetOrderGoodsDetailsList.stream().filter(detail -> NumberUtil.INTEGER_ZERO.equals(detail.getIsGift())).collect(Collectors.toMap(OrdDirOrderDetail::getGoodsCode, OrdDirOrderDetail::getQuantity));
        OrdDirOrder lastOrder = orderHandle.getLastUpAndDownOrder(targetOrder);
        String errorMessage = null;
        if (Objects.nonNull(lastOrder)) {
            List<OrdDirOrderDetail> parentOrderDetailsList = orderDetailHandle.findOrderDetailListByOrderId(lastOrder.getId());
            if (CollectionUtils.isNotEmpty(parentOrderDetailsList)) {
                int i = 0;
                for (OrdDirOrderDetail orderDetail : parentOrderDetailsList) {
                    BigDecimal quantity = skuQuantityMap.get(orderDetail.getGoodsCode());
                    if (null != quantity && quantity.compareTo(orderDetail.getQuantity()) == 0) {
                        i++;
                    }
                }
                // 计算相似度
                BigDecimal similarity = new BigDecimal(i).divide(new BigDecimal(parentOrderDetailsList.size()), NumberUtil.INTEGER_FOUR, RoundingMode.DOWN).multiply(new BigDecimal("100"));
                String targetRedisSimilarity = redisService.get("targetSimilarity");
                BigDecimal targetSimilarity;
                if (StringUtils.isNotEmpty(targetRedisSimilarity)) {
                    targetSimilarity = new BigDecimal(targetRedisSimilarity);
                } else {
                    targetSimilarity = new BigDecimal("80");
                }
                if (similarity.compareTo(targetSimilarity) >= 0) {
                    errorMessage = MessageFormat.format(msgTemplate, unitStr + targetOrder.getStoreCode(),
                            targetOrder.getOrderNo(), lastOrder.getOrderNo(), similarity);
                    log.info(errorMessage);
                    log.info("门店{}跑货订货单单号：{}即将发送商品明细相似度超高预警", targetOrder.getStoreCode(), targetOrder.getOrderNo());
                    flag = true;
                }
            }
        }
        WarningResultOut warningResultOut = new WarningResultOut();
        warningResultOut.setCheckFlag(flag);
        warningResultOut.setErrorMessage(errorMessage);
        return warningResultOut;
    }

    /**
     * 根据条件查询订货单
     *
     * @param targetOrder
     * @return
     */
    @Override
    public OrdDirOrder getLastUpAndDownOrder(OrdDirOrder targetOrder) {
        return ordDirOrderMapper.getLastUpAndDownOrder(targetOrder);
    }

    @Override
    public BigDecimal getTotalDistributionAndUpDownOrderAmount(String storeCode, String truncationDateTime, Integer orderTypeConfigId, String bizOrgCode) {
        List<OrdDirOrder> distributionAndUpDownOrderList = this.findDistributionAndUpDownOrderList(storeCode, truncationDateTime, orderTypeConfigId, bizOrgCode);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (CollectionUtils.isEmpty(distributionAndUpDownOrderList)) {
            return totalAmount;
        }
        for (OrdDirOrder order : distributionAndUpDownOrderList) {
            totalAmount = totalAmount.add(order.getOrderAmount());
        }
        return totalAmount;
    }

    @Override
    public List<OrdDirOrder> findDistributionAndUpDownOrderList(String storeCode, String truncationDateTime, Integer orderTypeConfigId, String bizOrgCode) {
        DirOrderQueryIn orderQueryIn = new DirOrderQueryIn();
        orderQueryIn.setStoreCode(storeCode);
        orderQueryIn.setTruncationDateTime(truncationDateTime);
        orderQueryIn.setOrderTypeConfigId(orderTypeConfigId);
        orderQueryIn.setBizOrgCode(bizOrgCode);
        List<String> sourceCodeList = Lists.newArrayList();
        sourceCodeList.add(SourceTypeEnum.DISTRIBUTION.getKey());
        sourceCodeList.add(SourceTypeEnum.UPLOWDOWN.getKey());
        orderQueryIn.setSourceCodeList(sourceCodeList);
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.SUBMIT.getKey());
        orderQueryIn.setOrderStatusCodeList(orderStatusCodeList);
        return ordDirOrderMapper.findOrderListByOrderQueryIn(orderQueryIn);
    }

    /**
     * 查找未下发ERP未作废的订货单
     *
     * @param storeCode
     * @param truncationDateTime
     * @param orderTypeId
     * @param bizOrgCode
     * @return
     */
    @Override
    public List<OrdDirOrder> findNoInvalidOrderListByParameter(String storeCode, String truncationDateTime, Integer orderTypeId, String bizOrgCode) {
        DirOrderQueryIn orderQueryIn = new DirOrderQueryIn();
        orderQueryIn.setStoreCode(storeCode);
        orderQueryIn.setTruncationDateTime(truncationDateTime);
        orderQueryIn.setOrderTypeConfigId(orderTypeId);
        orderQueryIn.setBizOrgCode(bizOrgCode);
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.SUBMIT.getKey());
        orderQueryIn.setOrderStatusCodeList(orderStatusCodeList);
        return ordDirOrderMapper.findOrderListByOrderQueryIn(orderQueryIn);
    }

    @Override
    public BigDecimal sumOrderAmount(String storeCode, String bizOrgCode, List<Long> orderIdList) {
        BigDecimal totalPayAmount = ordDirOrderMapper.sumOrderAmount(storeCode, bizOrgCode, orderIdList, OrderStatusEnum.SUBMIT.getKey());
        return null != totalPayAmount ? totalPayAmount : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getCycleOrderedGoodsQty(String storeCode, LocalDateTime truncationDateTime, Integer orderTypeConfigId, String goodsCode, String bizOrgCode) {
        OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycle(storeCode, orderTypeConfigId, truncationDateTime, bizOrgCode);
        if (Objects.isNull(orderCycle)) {
            return BigDecimal.ZERO;
        }
        return ordDirOrderDetailMapper.getCycleOrderedGoodsQty(orderCycle.getId(), goodsCode);
    }

    @Override
    public List<String> findPaidTransferOrderStoreCodeList(QueryPaidTransferOrderIn queryPaidTransferOrderIn) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.PAID.getKey());
        orderStatusCodeList.add(OrderStatusEnum.SUBMIT.getKey());
        queryPaidTransferOrderIn.setOrderStatusCodeList(orderStatusCodeList);
        return ordDirOrderMapper.findPaidTransferOrderStoreCodeList(queryPaidTransferOrderIn);
    }

    @Override
    public List<LogisticsMessageOrderOut> findOrderByTruncationDateTimeAndOrderStatus(LogisticsMessageQueryIn logisticsMessageQueryIn) {
        return ordDirOrderMapper.findOrderByTruncationDateTimeAndOrderStatus(logisticsMessageQueryIn);
    }

    @Override
    public List<String> findNotToRequestOrderList(String truncationDateTime, String bizOrgCode) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.PAID.getKey());
        orderStatusCodeList.add(OrderStatusEnum.SUBMIT.getKey());
        return ordDirOrderMapper.findNotToRequestOrderList(truncationDateTime, orderStatusCodeList, bizOrgCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderSourceCode(List<Long> orderIdList, String sourceCode) {
        ordDirOrderMapper.updateOrderSourceCode(orderIdList, sourceCode);
    }

    @Override
    public int getCountByParameter(String storeCode, String sourceCode, LocalDateTime truncationDateTime) {
        return ordDirOrderMapper.getCountByParameter(storeCode, sourceCode, truncationDateTime);
    }
}
