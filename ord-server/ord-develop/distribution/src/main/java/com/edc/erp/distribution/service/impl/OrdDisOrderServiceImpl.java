package com.edc.erp.distribution.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.enumeration.SalvageAuditTypeEnum;
import com.edc.erp.common.model.in.warning.QueryPaidTransferOrderIn;
import com.edc.erp.common.model.out.OrderProcessConfigItemOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.model.out.warning.WarningResultOut;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.distribution.handle.DisOrderDetailHandle;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.mapper.OrdDisOrderDetailMapper;
import com.edc.erp.distribution.mapper.OrdDisOrderMapper;
import com.edc.erp.distribution.model.in.*;
import com.edc.erp.distribution.model.out.*;
import com.edc.erp.distribution.service.OrdDisOrderService;
import com.edc.erp.distribution.service.OrdDisOrderTrackService;
import com.edc.erp.distribution.service.OrderCreateService;
import com.edc.erp.enumeration.*;
import com.edc.erp.handle.DisOrderConfigHandle;
import com.edc.erp.handle.DisRequestOrderHandle;
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
import com.edc.sdk.dictionary.entity.SystemDict;
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
 * 配销订单表(OrdDisOrder)表服务实现类
 *
 * @author fxw
 * @since 2022-10-17 15:55:14
 */
@Service
@Slf4j
public class OrdDisOrderServiceImpl extends BaseServiceImpl<OrdDisOrder> implements OrdDisOrderService {
    @Autowired
    private OrdDisOrderMapper ordDisOrderMapper;

    @Autowired
    private StoreCenterService storeCenterService;

    @Autowired
    private SystemDictService systemDictService;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private DisOrderHandle orderHandle;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private DisOrderConfigHandle orderConfigHandle;

    @Autowired
    private DisRequestOrderHandle requestOrderHandle;

    @Autowired
    private DisOrderDetailHandle orderDetailHandle;

    @Autowired
    private OrdDisOrderDetailMapper ordDisOrderDetailMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderCreateService orderCreateService;

    @Autowired
    private OrdDisOrderTrackService ordDisOrderTrackService;

    @Autowired
    private AsyncExportExecutor asyncExportExecutor;


    /**
     * 分页查询配销订货单列表
     *
     * @param orderIn
     * @return
     */
    @Override
    public Page<DisOrderOut> findOrderOutPage(OrderIn orderIn) {
        // 当门店名称，门店区域等不为空时，先得到所有的门店代码集合
        if (StringUtils.isNotEmpty(orderIn.getStoreArea()) || StringUtils.isNotEmpty(orderIn.getStoreName())) {
            List<String> storeCodeList = storeCenterService.findStoreCodeListByStoreAreaOrName(orderIn.getStoreArea(),
                    orderIn.getStoreName());
            if (CollectionUtils.isEmpty(storeCodeList)) {
                return new Page<>(orderIn);
            }
            orderIn.setStoreCodeList(storeCodeList);
        }
        List<DisOrderOut> orderOuts = ordDisOrderMapper.findDisOrderByPage(orderIn);
        orderOuts.stream().forEach(orderOut -> {
            this.transChineseName(orderOut);
        });
        Page<DisOrderOut> resPage = new Page(orderIn);
        resPage.setList(orderOuts);
        return resPage;
    }

    /**
     * 保存订货单
     *
     * @param ordDisOrder
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OrdDisOrder ordDisOrder) {
        ordDisOrderMapper.insert(ordDisOrder);
        String content = MessageFormat.format(OrderLogEnum.ORDER_CREATE.getKey(), SourceTypeEnum.getValueByKey(ordDisOrder.getSourceCode()), ordDisOrder.getOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), ordDisOrder.getId().toString(),
                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), ordDisOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 修改订货单
     *
     * @param ordDisOrder
     */
    @Override
    public void update(OrdDisOrder ordDisOrder) {
        ordDisOrderMapper.updateByPrimaryKeySelective(ordDisOrder);
    }

    /**
     * 根据id、业务组织代码获取订货单信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public OrdDisOrder getOrderByIdAndBizOrgCode(Long orderId, String bizOrgCode) {
        return ordDisOrderMapper.getOrderByIdAndBizOrgCode(orderId, bizOrgCode);
    }

    /**
     * @param id:
     * @param bizOrgCode:
     * @param orderStatusCode:
     * @param updater:
     * @param freezeStatus:
     * @Description: 修改订单状态
     * @Author: ZhangYao
     * @Date: 2023/7/19 10:42
     * @return: void
     **/
    @Override
    public void updateOrderStatus(Long id, String bizOrgCode, String orderStatusCode, String updater, String freezeStatus) {
        ordDisOrderMapper.updateOrderStatus(id, bizOrgCode, orderStatusCode, updater, freezeStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response batchInvalidOrder(BatchInvalidOrderIn batchInvalidOrderIn) {
        StringJoiner repeatMsg = new StringJoiner(",");
        StringJoiner statusErrorMsg = new StringJoiner(",");

        for (Long orderId : batchInvalidOrderIn.getOrderIdList()) {
            OrdDisOrder ordDisOrder = this.getOrderByIdAndBizOrgCode(orderId, batchInvalidOrderIn.getBizOrgCode());
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
            errorMsg.add(repeatMsg.toString() + "订货单状态为" + OrderStatusEnum.INVALID.getValue() + "，请勿重复作废");
        }
        if (StringUtils.isNotBlank(statusErrorMsg.toString())) {
            errorMsg.add(statusErrorMsg.toString() + "订货单状态为" + OrderStatusEnum.TO_REQUEST_ORDER.getValue() + "，不能作废");
        }

        String content = MessageFormat.format(DistributionOrderLogEnum.DISTRIBUTION_ORDER_JOIN_INVALID.getKey(), errorMsg.toString());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER_DISTRIBUTION_ORDER.getName(), String.valueOf(batchInvalidOrderIn.getDistributionOrderId()),
                OrdLogTypeEnum.DIS_ORDER_DISTRIBUTION_ORDER.getCode(), content, new Date(), batchInvalidOrderIn.getUsername());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success(errorMsg.toString());
    }

    @Override
    public BigDecimal getRealUnitPrice(Long requestOrderId) {
        List<BigDecimal> realUnitPrices = ordDisOrderMapper.findRealUnitPrice(requestOrderId);
        if (CollectionUtils.isNotEmpty(realUnitPrices)) {
            BigDecimal min = Collections.min(realUnitPrices);
            return min;
        }
        return BigDecimal.ZERO;
    }

    /**
     * 加推订货单
     *
     * @param ordDisOrder
     * @param userName
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> addPushOrder(OrdDisOrder ordDisOrder, String userName) {
        OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(ordDisOrder.getOrderCycleId(), ordDisOrder.getBizOrgCode());
        boolean havePayProcess = orderConfigHandle.isHavePayProcess(orderCycle.getId(), ordDisOrder.getStoreCode(), ordDisOrder.getBizOrgCode());
        if (havePayProcess) {
            if (!OrderStatusEnum.PAID.getKey().equals(ordDisOrder.getOrderStatusCode())) {
                return Response.error("该订货单未付款，不能加推");
            }
        }

        OrderProcessConfigItemOut minAmountItem = orderConfigHandle.minAmountCheckType(orderCycle.getId(), orderCycle.getBizOrgCode());
        if (Objects.isNull(minAmountItem)) {
            throw new BusinessException("截单时起订额校验规则查询为空");
        }
        List<OrdDisOrder> orderList = Lists.newArrayList(ordDisOrder);
        String content = MessageFormat.format(OrderLogEnum.ADD_PUSH_ORDER.getKey(), ordDisOrder.getOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), String.valueOf(ordDisOrder.getId()),
                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), userName);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        requestOrderHandle.mergeDisOrder(orderCycle, minAmountItem.getItemCode(), orderList, userName, false,
                true, SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode());
        return Response.success();
    }

    /**
     * 根据截单时间批量作废
     *
     * @param orderList
     * @param orderStatusCode
     * @param updater
     */
    @Override
    public void batchInvalidForCutOrder(List<OrdDisOrder> orderList, String orderStatusCode, String updater) {
        List<BusinessLog> businessLogList = this.batchUpdateOrder(orderList, orderStatusCode, updater);
        orderList.forEach(order -> {
            Response response = orderDetailHandle.releaseOrderAmount(order);
            if (!response.isSuccess()) {
                throw new BusinessException("订货单退还金额失败" + response.getMessage());
            }
        });
        businessLogList.forEach(businessLog -> asyncLogService.sendAsyncSaveLogByMq(businessLog));
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
    public List<BusinessLog> batchUpdateOrder(List<OrdDisOrder> orderList, String orderStatusCode, String updater) {
        List<BusinessLog> logContentList = Lists.newArrayList();
        orderList.stream().forEach(order -> {
            order.setUpdater(updater);
            String beforeOrderStatusCode = order.getOrderStatusCode();
            order.setOrderStatusCode(orderStatusCode);
            String content = MessageFormat.format(OrderLogEnum.CUT_LESS_MIN_AMOUNT_INVALID_SINGLE_MODEL_MARGE.getKey(), order.getOrderNo(),
                    OrderStatusEnum.getValueByKey(beforeOrderStatusCode), OrderStatusEnum.getValueByKey(orderStatusCode));
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
                    OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), updater);
            logContentList.add(businessLog);
            // 预售，退资产
            if (OrderIdentificationEnum.PRESALE_ORDER.getCode().equals(order.getOrderIdentification())) {
                orderHandle.handleReturnBackStorePresaleAssets(order, updater);
            }
            // 不足起订额整单作废推送订单追踪日志
            String trackLog = MessageFormat.format(OrderTrackLogTemplateEnum.CUT_LESS_MIN_AMOUNT_INVALID_SINGLE_MODEL_MARGE.getTemplate(), order.getOrderNo());
            ordDisOrderTrackService.pushRedisOrderTrackMessage(order.getOrderNo(), order.getStoreCode(),
                    OrderTrackStatusEnum.INVALID_ORDER.getName(), trackLog, order.getBizOrgCode(), order.getCreator(), order.getCreateTime());
        });
        ordDisOrderMapper.batchUpdateOrder(orderList);
        return logContentList;
    }

    /**
     * 截单未支付
     *
     * @param order
     * @return
     */
    @Override
    public int updateForTruncationOrderByNoPay(OrdDisOrder order) {
        return ordDisOrderMapper.updateForTruncationOrderByNoPay(order);
    }

    /**
     * 批量修改集货单订货单关联关系
     *
     * @param orderList
     * @param id
     * @param systemUser
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateOrderRequestRel(List<OrdDisOrder> orderList, Long id, String systemUser) {
        for (OrdDisOrder ordDisOrder : orderList) {
            ordDisOrder.setRequestOrderId(id);
            ordDisOrder.setUpdater(systemUser);
        }
        ordDisOrderMapper.batchUpdateOrderRequestRel(orderList);
    }

    /**
     * 根据集货单id查询关联的订货单列表
     *
     * @param requestOrderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public List<OrdDisOrder> findOrderByRequestOrderIdAndBizOrgCode(Long requestOrderId, String bizOrgCode) {
        OrdDisOrder ordDisOrder = new OrdDisOrder();
        ordDisOrder.setRequestOrderId(requestOrderId);
        ordDisOrder.setBizOrgCode(bizOrgCode);
        ordDisOrder.setIsDelete(ModelConst.DELETE.NO);

        List<OrdDisOrder> list = ordDisOrderMapper.select(ordDisOrder);
        return list;
    }

    /**
     * 订货单汇总
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
        return ordDisOrderMapper.orderSummary(orderIn);
    }

    /**
     * 根据id查询订货单出参信息
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public DisOrderOut getOrderOutById(Long orderId, String bizOrgCode) {
        DisOrderOut orderOut = ordDisOrderMapper.getByIdAndOrg(orderId, bizOrgCode);
        if (orderOut != null) {
            this.transChineseName(orderOut);
        }
        return orderOut;
    }

    /**
     * 导出订货单列表
     *
     * @param orderIn
     * @return
     */
    @Override
    public String exportOrderList(OrderIn orderIn) {
        // 设置每次查询条数
        orderIn.setPageSize(10000);
        String title = "配销订货单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        asyncExportExecutor.export(
                UserUtil.getUserName(),
                fileName,
                () -> ExportExcelByPage.exportExcelBytesByPage(
                        // sheet页名称
                        "配销订货单列表",
                        // 导出模板实体
                        ExcelOrderOut.class,
                        // 分页查询对象
                        orderIn,
                        // 分页查询方法
                        page -> {
                            Page<DisOrderOut> outPage = this.findOrderOutPage(orderIn);
                            List<ExcelOrderOut> excelOrderOuts = this.parseDataToExcel(outPage.getList());
                            return excelOrderOuts;
                        })
        );
        return AsyncExportExecutor.DOWNLOADING;
    }

    /**
     * 获取门店某个订货周期下指定订单类型下已付款跑货和分货金额
     *
     * @param storeCode
     * @param truncationDateTime
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    @Override
    public BigDecimal getTotalDistributionAndUpDownOrderAmount(String storeCode, String truncationDateTime, Integer orderTypeConfigId, String bizOrgCode) {
        List<OrdDisOrder> distributionAndUpDownOrderList = this.findDistributionAndUpDownOrderList(storeCode, truncationDateTime, orderTypeConfigId, bizOrgCode);
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (CollectionUtils.isEmpty(distributionAndUpDownOrderList)) {
            return totalAmount;
        }
        for (OrdDisOrder order : distributionAndUpDownOrderList) {
            totalAmount = totalAmount.add(order.getOrderAmount());
        }
        return totalAmount;
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
    public List<OrdDisOrder> findNoInvalidOrderListByParameter(String storeCode, String truncationDateTime, Integer orderTypeId, String bizOrgCode) {
        OrderQueryIn orderQueryIn = new OrderQueryIn();
        orderQueryIn.setStoreCode(storeCode);
        orderQueryIn.setTruncationDateTime(truncationDateTime);
        orderQueryIn.setOrderTypeConfigId(orderTypeId);
        orderQueryIn.setBizOrgCode(bizOrgCode);
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.WAIT_PAYMENT.getKey());
        orderStatusCodeList.add(OrderStatusEnum.PAID.getKey());
        orderStatusCodeList.add(OrderStatusEnum.PAYING.getKey());
        orderQueryIn.setOrderStatusCodeList(orderStatusCodeList);
        return ordDisOrderMapper.findOrderListByOrderQueryIn(orderQueryIn);
    }

    /**
     * 中文转化
     *
     * @param orderOut
     */
    void transChineseName(DisOrderOut orderOut) {
        orderOut.setOrderStatusCodeStr(systemDictService.getSystemDictName(orderOut.getOrderStatusCode()));
        orderOut.setSourceCodeStr(systemDictService.getSystemDictName(orderOut.getSourceCode()));
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(orderOut.getStoreCode());
        if (storeOut != null) {
            orderOut.setStoreName(storeOut.getStoreName());
            orderOut.setStoreAreaStr(storeOut.getArea());
        }
        // 订货标识
        orderOut.setOrderIdentificationStr(OrderIdentificationEnum.getNameByCode(orderOut.getOrderIdentification()));
    }

    /**
     * 解析数据
     *
     * @param list
     * @return
     */
    private List<ExcelOrderOut> parseDataToExcel(List<DisOrderOut> list) {
        List<ExcelOrderOut> excelOrderOuts = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ExcelOrderOut excelOrderOut = new ExcelOrderOut();
            BeanUtils.copy(list.get(i), excelOrderOut);
            excelOrderOut.setIndex(i + 1);
            excelOrderOut.setOrderTypeName(excelOrderOut.getOrderTypeName() + "【" + list.get(i).getOrderTypeCode() + "】");

            excelOrderOuts.add(excelOrderOut);
        }
        return excelOrderOuts;
    }

    /**
     * 查找未作废的分货和跑货订单
     *
     * @param storeCode
     * @param truncationDateTime
     * @param orderTypeConfigId
     * @param bizOrgCode
     * @return
     */
    @Override
    public List<OrdDisOrder> findDistributionAndUpDownOrderList(String storeCode, String truncationDateTime, Integer orderTypeConfigId, String bizOrgCode) {
        OrderQueryIn orderQueryIn = new OrderQueryIn();
        orderQueryIn.setStoreCode(storeCode);
        orderQueryIn.setTruncationDateTime(truncationDateTime);
        orderQueryIn.setOrderTypeConfigId(orderTypeConfigId);
        orderQueryIn.setBizOrgCode(bizOrgCode);
        List<String> sourceCodeList = Lists.newArrayList();
        sourceCodeList.add(SourceTypeEnum.DISTRIBUTION.getKey());
        sourceCodeList.add(SourceTypeEnum.UPLOWDOWN.getKey());
        orderQueryIn.setSourceCodeList(sourceCodeList);
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.PAID.getKey());
        orderQueryIn.setOrderStatusCodeList(orderStatusCodeList);
        return ordDisOrderMapper.findOrderListByOrderQueryIn(orderQueryIn);
    }

    /**
     * APP根据单号查询直营订货单
     *
     * @param orderNo
     * @param bizOrgCode
     * @return
     */
    @Override
    public DisOrderOut getOrderOut(String orderNo, String bizOrgCode) {
        OrdDisOrder query = new OrdDisOrder();
        query.setBizOrgCode(bizOrgCode);
        query.setOrderNo(orderNo);
        OrdDisOrder ordDisOrder = this.selectOne(query);
        if (Objects.isNull(ordDisOrder)) {
            return null;
        }
        DisOrderOut disOrderOut = new DisOrderOut();
        BeanUtils.copy(ordDisOrder, disOrderOut);
        this.transChineseName(disOrderOut);
        return disOrderOut;
    }

    @Override
    public List<OrdDisOrder> findNeedCutOrderListByParameter(OrderQueryIn orderQueryIn) {
        return ordDisOrderMapper.findNeedCutOrderListByParameter(orderQueryIn);
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
        return ordDisOrderMapper.findThirtyDaysRequestOrderTotalAmountByStoreCodeAndOrderTypeConfigId(storeCode, orderTypeConfigId, bizOrgCode);
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
    public WarningResultOut checkOrderGoodsSimilarity(OrdDisOrder targetOrder, String unitStr, String msgTemplate) {
        boolean flag = false;
        // 对比goodsCode
        List<OrdDisOrderDetail> targetOrderGoodsDetailsList = orderDetailHandle.findOrderDetailListByOrderId(targetOrder.getId());
        Map<String, BigDecimal> skuQuantityMap = targetOrderGoodsDetailsList.stream().filter(detail -> NumberUtil.INTEGER_ZERO.equals(detail.getIsGift())).collect(Collectors.toMap(OrdDisOrderDetail::getGoodsCode, OrdDisOrderDetail::getQuantity));
        OrdDisOrder lastOrder = orderHandle.getLastUpAndDownOrder(targetOrder);
        String errorMessage = null;
        if (Objects.nonNull(lastOrder)) {
            List<OrdDisOrderDetail> parentOrderDetailsList = orderDetailHandle.findOrderDetailListByOrderId(lastOrder.getId());
            if (CollectionUtils.isNotEmpty(parentOrderDetailsList)) {
                int i = 0;
                for (OrdDisOrderDetail orderDetail : parentOrderDetailsList) {
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
    public OrdDisOrder getLastUpAndDownOrder(OrdDisOrder targetOrder) {
        return ordDisOrderMapper.getLastUpAndDownOrder(targetOrder);
    }

    @Override
    public BigDecimal sumNeedPayAmount(String storeCode, String bizOrgCode, List<Long> orderIdList) {
        BigDecimal totalPayAmount = ordDisOrderMapper.sumOrderAmount(storeCode, bizOrgCode, orderIdList, OrderStatusEnum.WAIT_PAYMENT.getKey());
        return null != totalPayAmount ? totalPayAmount : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal sumPaidAmount(String storeCode, String bizOrgCode, List<Long> orderIdList) {
        BigDecimal totalPayAmount = ordDisOrderMapper.sumOrderAmount(storeCode, bizOrgCode, orderIdList, OrderStatusEnum.PAID.getKey());
        return null != totalPayAmount ? totalPayAmount : BigDecimal.ZERO;
    }

    @Override
    public List<AppDisOrderCycleOut> findAppOrderList(AppQueryDisOrderIn appQueryOrderIn) {
        List<DisOrderCycleOrderOut> orderCycleOrderOutList = disOrderCycleHandle.findOrderCycleOrderListByAppQueryOrderIn(appQueryOrderIn);
        if (CollectionUtils.isEmpty(orderCycleOrderOutList)) {
            return Lists.newArrayList();
        }
        Map<Integer, List<AppDisOrderOut>> orderMap = new LinkedHashMap<>();
        Map<Integer, AppDisOrderCycleOut> cycleOutMap = new LinkedHashMap<>();
        orderCycleOrderOutList.forEach(orderCycleOrderOut -> {
            this.initCycleMap(cycleOutMap, orderCycleOrderOut);
            this.initOrderMap(orderMap, orderCycleOrderOut);
        });
        List<AppDisOrderCycleOut> appOrderCycleOutList = Lists.newArrayList();
        cycleOutMap.entrySet().forEach(entry -> {
            Integer orderCycleId = entry.getKey();
            AppDisOrderCycleOut appOrderCycleOut = entry.getValue();
            List<AppDisOrderOut> appOrderOuts = orderMap.get(orderCycleId);
            BigDecimal totalAmount = this.getTotalAmount(appQueryOrderIn, orderCycleId, appOrderOuts);
            appOrderCycleOut.setTotalAmount(totalAmount);
            this.initAppOrderOutOtherData(appOrderOuts, orderCycleId, appQueryOrderIn.getBizOrgCode());
            appOrderCycleOut.setAppOrderOutList(appOrderOuts);
            appOrderCycleOutList.add(appOrderCycleOut);
        });
        return appOrderCycleOutList;
    }

    @Override
    public int countDisOrderByOrderCycleId(Integer orderCycleId, String storeCode, String orderStatusCode) {
        OrdDisOrder ordDisOrder = new OrdDisOrder();
        ordDisOrder.setOrderCycleId(orderCycleId);
        ordDisOrder.setStoreCode(storeCode);
        ordDisOrder.setOrderStatusCode(orderStatusCode);
        ordDisOrder.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderMapper.selectCount(ordDisOrder);
    }

    @Override
    public BigDecimal getCycleOrderedGoodsQty(String storeCode, LocalDateTime truncationDateTime, Integer orderTypeConfigId, String goodsCode, String bizOrgCode) {
        OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycle(storeCode, orderTypeConfigId, truncationDateTime, bizOrgCode);
        if (Objects.isNull(orderCycle)) {
            return BigDecimal.ZERO;
        }
        return ordDisOrderDetailMapper.getCycleOrderedGoodsQty(orderCycle.getId(), goodsCode);
    }

    @Override
    public List<String> findPaidTransferOrderStoreCodeList(QueryPaidTransferOrderIn queryPaidTransferOrderIn) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.PAID.getKey());
        orderStatusCodeList.add(OrderStatusEnum.WAIT_PAYMENT.getKey());
        queryPaidTransferOrderIn.setOrderStatusCodeList(orderStatusCodeList);
        return ordDisOrderMapper.findPaidTransferOrderStoreCodeList(queryPaidTransferOrderIn);
    }

    @Override
    public List<String> findNotToRequestOrderList(String truncationDateTime, String bizOrgCode) {
        List<String> orderStatusCodeList = Lists.newArrayList();
        orderStatusCodeList.add(OrderStatusEnum.PAID.getKey());
        orderStatusCodeList.add(OrderStatusEnum.WAIT_PAYMENT.getKey());
        return ordDisOrderMapper.findNotToRequestOrderList(truncationDateTime, orderStatusCodeList, bizOrgCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderSourceCode(List<Long> orderIdList, String sourceCode) {
        ordDisOrderMapper.updateOrderSourceCode(orderIdList, sourceCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderOrderIdentification(List<Long> orderIdList, String orderIdentification) {
        ordDisOrderMapper.updateOrderOrderIdentification(orderIdList, orderIdentification);
    }

    @Override
    public List<OrdDisOrder> findNeedUnfreezeOrderNo(Long orderCycleId) {
        return ordDisOrderMapper.findNeedUnfreezeOrderNo(orderCycleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchReleaseOrderList(List<OrdDisOrder> ordDisOrderList) {
        return ordDisOrderMapper.batchReleaseOrderList(ordDisOrderList);
    }

//    @Transactional(rollbackFor = Exception.class)
//    public void updateNoNeedPayOrderPaid(OrdDisOrder order) {
//        order.setUpdater(SystemConstant.SYSTEM_USER);
//        String beforeStatusCode = order.getOrderStatusCode();
//        orderHandle.updateOrderStatus(order.getId(), order.getBizOrgCode(), OrderStatusEnum.PAID.getKey(), order.getUpdater());
//        String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE_FOR_NO_NEED_PAY.getKey(), order.getOrderNo(),
//                OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.PAID.getKey()));
//        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIS_ORDER.getName(), order.getId().toString(),
//                OrdLogTypeEnum.DIS_ORDER.getCode(), content, new Date(), order.getUpdater());
//        logService.saveLog(businessLog);
//        log.info("订货订单号：" + order.getOrderNo() + "由于订货额为0， 无需进行支付，状态直接修改为已付款");
//    }

    /**
     * 封装7日订货单订货周期map
     *
     * @param cycleOutMap
     * @param orderCycleOrderOut
     */
    private void initCycleMap(Map<Integer, AppDisOrderCycleOut> cycleOutMap, DisOrderCycleOrderOut orderCycleOrderOut) {
        AppDisOrderCycleOut appOrderCycleOut = cycleOutMap.get(orderCycleOrderOut.getOrderCycleId());
        if (Objects.isNull(appOrderCycleOut)) {
            appOrderCycleOut = new AppDisOrderCycleOut();
            BeanUtils.copy(orderCycleOrderOut, appOrderCycleOut);
            cycleOutMap.put(orderCycleOrderOut.getOrderCycleId(), appOrderCycleOut);
        }
    }

    /**
     * 封装7日订货单订货单map
     *
     * @param orderMap
     * @param orderCycleOrderOut
     */
    private void initOrderMap(Map<Integer, List<AppDisOrderOut>> orderMap, DisOrderCycleOrderOut orderCycleOrderOut) {
        List<AppDisOrderOut> appOrderOuts = orderMap.get(orderCycleOrderOut.getOrderCycleId());
        if (CollectionUtils.isEmpty(appOrderOuts)) {
            appOrderOuts = Lists.newArrayList();
        }
        AppDisOrderOut appOrderOut = new AppDisOrderOut();
        BeanUtils.copy(orderCycleOrderOut, appOrderOut);
        appOrderOut.setOrderId(orderCycleOrderOut.getOrderId());
        appOrderOuts.add(appOrderOut);
        orderMap.put(orderCycleOrderOut.getOrderCycleId(), appOrderOuts);
    }

    /**
     * 计算7日订单中订货周期的总额
     *
     * @param appQueryOrderIn
     * @param orderCycleId
     * @param appOrderOuts
     * @return
     */
    private BigDecimal getTotalAmount(AppQueryDisOrderIn appQueryOrderIn, Integer orderCycleId, List<AppDisOrderOut> appOrderOuts) {
        // 查找此订货周期是否含有支付流程
        boolean isHavePayFlag = orderConfigHandle.isHavePayProcess(orderCycleId, appQueryOrderIn.getStoreCode(), appQueryOrderIn.getBizOrgCode());
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (isHavePayFlag) {
            List<Long> orderIdList = appOrderOuts.stream().map(AppDisOrderOut::getOrderId).collect(Collectors.toList());
            totalAmount = this.sumPaidAmount(appQueryOrderIn.getStoreCode(), appQueryOrderIn.getBizOrgCode(), orderIdList);
        }
        return totalAmount;
    }

    private void initAppOrderOutOtherData(List<AppDisOrderOut> appOrderOutList, Integer orderCycleId, String bizOrgCode) {
        appOrderOutList.forEach(appOrderOut -> {
            SystemDict orderTypeSystemDict = systemDictService.getSystemDict(appOrderOut.getOrderStatusCode());
            if (null != orderTypeSystemDict) {
                appOrderOut.setOrderStatusCodeStr(orderTypeSystemDict.getDictValueName());
            }
            SystemDict sourceCodeSystemDict = systemDictService.getSystemDict(appOrderOut.getSourceCode());
            if (null != sourceCodeSystemDict) {
                appOrderOut.setSourceCodeStr(sourceCodeSystemDict.getDictValueName());
            }
            // top商品
            List<AppTopDisOrderGoodsOut> appTopOrderGoodsOutList = orderDetailHandle.findTopOrderDetailListByOrderId(appOrderOut.getOrderId(), 3);
            appOrderOut.setAppTopOrderGoodsOutList(appTopOrderGoodsOutList);
            // 是否能编辑
            OrdDisOrder order = new OrdDisOrder();
            order.setId(appOrderOut.getOrderId());
            order.setOrderCycleId(orderCycleId);
            order.setOrderStatusCode(appOrderOut.getOrderStatusCode());
            order.setStoreCode(appOrderOut.getStoreCode());
            order.setSourceCode(appOrderOut.getSourceCode());
            order.setBizOrgCode(bizOrgCode);
            boolean activityMatchFlag = orderConfigHandle.isCanEditOrder(order);
            appOrderOut.setIsCanEdit(activityMatchFlag ? 1 : 0);
            if (appOrderOut.getOrderStatusCode().equals(OrderStatusEnum.TO_REQUEST_ORDER.getKey())
                    || appOrderOut.getOrderStatusCode().equals(OrderStatusEnum.INVALID.getKey())
//                    || appOrderOut.getOrderStatusCode().equals(OrderStatusEnum.PAYING.getKey())
                    || SourceTypeEnum.DISTRIBUTION.getKey().equals(order.getSourceCode())) {
                appOrderOut.setIsCanInvalid(0);
            } else {
                appOrderOut.setIsCanInvalid(1);
            }
            appOrderOut.setOrderIdentificationStr(OrderIdentificationEnum.getNameByCode(appOrderOut.getOrderIdentification()));
        });
    }

    public AppDisOrderDetailInfoOut getAppOrderDetailInfoOut(Long orderId, String bizOrgCode) {
        AppDisOrderDetailInfoOut appOrderDetailInfoOut = new AppDisOrderDetailInfoOut();
        AppDisOrderHeaderOut appOrderHeader = new AppDisOrderHeaderOut();
        OrdDisOrder orderOut = this.getOrderOutById(orderId, bizOrgCode);
        if (Objects.isNull(orderOut)) {
            throw new BusinessException("不存在的订货单");
        }
        BeanUtils.copy(orderOut, appOrderHeader);
        appOrderDetailInfoOut.setAppOrderHeader(appOrderHeader);
        List<OrdDisOrderDetail> orderDetailList = orderDetailHandle.findOrderDetailListByOrderId(orderId);
        Map<String, List<OrdDisOrderDetail>> giftMap = orderDetailList.stream().filter(dtl -> NumberUtils.INTEGER_ONE.equals(dtl.getIsGift())).collect(Collectors.groupingBy(OrdDisOrderDetail::getBaseGoodsCode));
        List<AppOrderDetailOut> detailOutList = new ArrayList<>();
        for (OrdDisOrderDetail item : orderDetailList) {
            if (NumberUtils.INTEGER_ZERO.equals(item.getIsGift())) {
                AppOrderDetailOut detailOut = new AppOrderDetailOut();
                BeanUtils.copy(item, detailOut);
                if (giftMap.containsKey(item.getGoodsCode())) {
                    detailOut.setGiftOutList(giftMap.get(item.getGoodsCode()));
                }
                detailOutList.add(detailOut);
            }
        }
        appOrderDetailInfoOut.setOrderDetailList(detailOutList);
        return appOrderDetailInfoOut;
    }

    /**
     * 更新订货单商品数量
     *
     * @param updateOrderIn
     * @param loginUsername
     */
    public Response<String> updateOrderSkuPackageQuantity(UpdateDisOrderIn updateOrderIn, String loginUsername) {
        OrdDisOrder order = this.getOrderByIdAndBizOrgCode(updateOrderIn.getOrderId(), updateOrderIn.getBizOrgCode());
        if (Objects.isNull(order)) {
            return Response.error("不存在的订货单");
        }
        boolean activityMatchFlag = orderConfigHandle.isCanEditOrder(order);
        if (activityMatchFlag) {
            orderCreateService.updateDisOrderGoods(updateOrderIn.getUpdateOrderGoodsInList(), order, loginUsername);
            return Response.success();
        } else {
            return Response.error("该订货单不能编辑");
        }
    }


}
