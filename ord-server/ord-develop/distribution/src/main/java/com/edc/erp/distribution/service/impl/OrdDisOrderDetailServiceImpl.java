package com.edc.erp.distribution.service.impl;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.stock.StockTransInfoOut;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.distribution.entity.OrdDisOrder;
import com.edc.erp.distribution.entity.OrdDisOrderCycle;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.distribution.handle.DisOrderCycleHandle;
import com.edc.erp.distribution.handle.DisOrderHandle;
import com.edc.erp.distribution.mapper.OrdDisOrderDetailMapper;
import com.edc.erp.distribution.mapper.OrdDisOrderDistributionMapper;
import com.edc.erp.distribution.model.in.OrderDetailIn;
import com.edc.erp.distribution.model.out.*;
import com.edc.erp.distribution.service.OrdDisOrderDetailService;
import com.edc.erp.distribution.util.FileExportUtil;
import com.edc.erp.enumeration.OrderStatusEnum;
import com.edc.erp.handle.DisOrderConfigHandle;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 配销订货单详细表(OrdDisOrderDetail)表服务实现类
 *
 * @author fxw
 * @since 2022-10-17 15:55:14
 */
@Service
@Slf4j
public class OrdDisOrderDetailServiceImpl extends BaseServiceImpl<OrdDisOrderDetail> implements OrdDisOrderDetailService {

    @Autowired
    private OrdDisOrderDetailMapper ordDisOrderDetailMapper;

    @Autowired
    private DisOrderCycleHandle disOrderCycleHandle;

    @Autowired
    private RedisService redisService;

    @Autowired
    private DisOrderHandle orderHandle;

    @Autowired
    private FundServer fundServer;

    @Autowired
    private OrderGoodsServer orderGoodsServer;

    @Autowired
    private DisOrderConfigHandle orderConfigHandle;

    @Autowired
    private FileService fileService;

    @Autowired
    private OrdDisOrderDistributionMapper ordDisOrderDistributionMapper;

    /**
     * 删除存在的sku信息
     *
     * @param order
     */
    @Override
    public void removeSkuForExistsQuantity(OrdDisOrder order) {
        OrdDisOrderCycle orderCycle = disOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(order.getOrderCycleId(), order.getOrgCode());
        if (Objects.isNull(orderCycle)) {
            return;
        }
        List<OrdDisOrderDetail> orderDetailList = this.findOrderDetailListByOrderId(order.getId());
        if (CollectionUtils.isEmpty(orderDetailList)) {
            return;
        }
        LocalDateTime truncationTime = orderCycle.getTruncationDateTime();
        String redisTruncationTimeStr = DateUtils.format(truncationTime, "yyyy-MM-dd/HH-mm-ss");
        orderDetailList.forEach(orderDetail -> {
            String key = "storeTruncationDateTimeSku:" + order.getBizOrgCode() + ":" + order.getStoreCode() + redisTruncationTimeStr + ":" + orderDetail.getGoodsCode();
            redisService.del(key);
        });
    }

    /**
     * 根据订单id查询明细信息
     *
     * @param orderId
     * @return
     */
    @Override
    public List<OrdDisOrderDetail> findOrderDetailListByOrderId(Long orderId) {
        OrdDisOrderDetail ordDisOrderDetail = new OrdDisOrderDetail();
        ordDisOrderDetail.setOrderId(orderId);
        ordDisOrderDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderDetailMapper.select(ordDisOrderDetail);
    }

    /**
     * 保存配销订货单明细信息
     *
     * @param orderDetail
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OrdDisOrderDetail orderDetail) {
        ordDisOrderDetailMapper.insert(orderDetail);
    }

    /**
     * 退还订单金额
     *
     * @param order
     * @return
     */
    @Override
    public Response releaseOrderAmount(OrdDisOrder order) {
        if (BigDecimal.ZERO.compareTo(order.getOrderAmount()) == 0) {
            return Response.success();
        }
//        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
//        rechargeLiquidationIn.setRecipientPrincipalCode(order.getStoreCode());
//        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
//        rechargeLiquidationIn.setPayOrPrincipalCode(order.getBizOrgCode());
//        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
//        rechargeLiquidationIn.setBizOrgCode(order.getBizOrgCode());
//        rechargeLiquidationIn.setBusinessNo(order.getOrderNo());
//        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DIS_ORDER_RETURN.getCode());
//        rechargeLiquidationIn.setLiquidationAmount(order.getOrderAmount().abs());
//        rechargeLiquidationIn.setOriginalBusinessNo(order.getOrderNo());
//        rechargeLiquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
//        rechargeLiquidationIn.setRemark(FundReturnTypeEnum.DIS_ORDER_INVALID.getName());
//        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
//        Response response = fundServer.settlement(rechargeLiquidationIn);
//        if (!response.isSuccess()) {
//            log.error("订货单{}支付异常：{}", order.getOrderNo(), response.getMessage());
//            return Response.error(response.getMessage());
//        }
        UnFrozenIn unFrozenIn = new UnFrozenIn();
        unFrozenIn.setUnFrozenBusinessNos(Collections.singletonList(order.getOrderNo()));
        Response response = fundServer.unFrozen(unFrozenIn);
        if (null != response && !response.isSuccess()) {
            log.error("截单订货单{}不足起订额调用资管解冻异常{}", order.getOrderNo(), response.getMessage());
            throw new BusinessException("截单订货单" + order.getOrderNo() + "不足起订额调用资管解冻异常" + response.getMessage());
        }
        return response;
    }

    /**
     * 查询订货单明细分页列表
     *
     * @param orderDetailIn
     * @param bizOrgCode
     * @return
     */
    @Override
    public Page<OrderDetailOut> findOrderDetailOutPage(OrderDetailIn orderDetailIn, String bizOrgCode) {
        orderDetailIn.setIsDelete(ModelConst.DELETE.NO);
        List<OrderDetailOut> orderDetailOuts = ordDisOrderDetailMapper.findOrderDetailOutByPage(orderDetailIn);
        this.initBaseAndGiftOrderDetails(orderDetailOuts, bizOrgCode);

        Page resPage = new Page(orderDetailIn);
        resPage.setList(orderDetailOuts);
        return resPage;
    }

    /**
     * 运营端查询订货单明细表头
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public BackHeaderOrderDetailOut getBackHeaderOrderDetailOut(Long orderId, String bizOrgCode) {
        BackHeaderOrderDetailOut backHeaderOrderDetailOut = new BackHeaderOrderDetailOut();
        DisOrderOut orderOut = orderHandle.getOrderOutById(orderId, bizOrgCode);
        if (orderOut == null) {
            return null;
        }
        BeanUtils.copy(orderOut, backHeaderOrderDetailOut);
        // 支付金额 = 应付金额
        backHeaderOrderDetailOut.setPayableTotalAmount(orderOut.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        // 配货金额合计 = 订单金额
        backHeaderOrderDetailOut.setOriginalUnitTotalPrice(orderOut.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        // 明细
        OrdDisOrderDetail detail = new OrdDisOrderDetail();
        detail.setIsDelete(ModelConst.DELETE.NO);
        detail.setOrderId(orderOut.getId());
        List<OrdDisOrderDetail> orderDetails = ordDisOrderDetailMapper.select(detail);

//        // 配货金额
//        BigDecimal originalUnitTotalPrice = orderDetails.stream().map(m -> m.getOriginalUnitPrice().multiply(m.getPackageQuantity()))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        backHeaderOrderDetailOut.setOriginalUnitTotalPrice(originalUnitTotalPrice);
        // 商品总数
        BigDecimal totalQuantity = orderDetails.stream().map(OrdDisOrderDetail::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        backHeaderOrderDetailOut.setTotalQuantity(totalQuantity.intValue());
        // 是否需要释放金额
        boolean havePayProcess = orderConfigHandle.isHavePayProcess(orderOut.getOrderCycleId(), orderOut.getStoreCode(), orderOut.getOrgCode());
        boolean isShowReleaseAmount = false;
        if (havePayProcess) {
            // 已付款和已转单状态可释放金额
            if (OrderStatusEnum.PAID.getKey().equals(orderOut.getOrderStatusCode())
                    || OrderStatusEnum.TO_REQUEST_ORDER.getKey().equals(orderOut.getOrderStatusCode())) {
                isShowReleaseAmount = true;
            }
        }
        backHeaderOrderDetailOut.setShowReleaseAmount(isShowReleaseAmount);
        String distributionOrderNo = ordDisOrderDistributionMapper.getDistributionOrderNoByOrderId(orderId, bizOrgCode);
        backHeaderOrderDetailOut.setDistributionOrderNo(distributionOrderNo);
        return backHeaderOrderDetailOut;
    }

    /**
     * 订货单明细列表导出
     *
     * @param orderDetailIn
     * @param bizOrgCode
     * @return
     */
    @Override
    public String exportOrderDetailOut(OrderDetailIn orderDetailIn, String bizOrgCode) {
        orderDetailIn.setPageNum(NumberUtil.INTEGER_ZERO);
        orderDetailIn.setPageSize(NumberUtil.INTEGER_ZERO);
        Page<OrderDetailOut> detailOutPage = this.findOrderDetailOutPage(orderDetailIn, bizOrgCode);

        List<ExcelOrderDetailOut> excelOrderOuts = this.parseDataToExcel(detailOutPage.getList());
        byte[] bytes = FileExportUtil.getFileBytesByData(excelOrderOuts,
                "订货订单明细", "订货订单明细", ExcelOrderDetailOut.class, true);
        return fileService.uploadFile("订货订单明细" + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    /**
     * 根据订单id查询包装商品数量
     *
     * @param skuCode
     * @param orderIdList
     * @param appUserOut
     * @param truncationTimeStr
     * @return
     */
    @Override
    public BigDecimal sumPackageByOrderIdList(String skuCode, List<Long> orderIdList, AppUserOut appUserOut, String truncationTimeStr) {
        if (CollectionUtils.isEmpty(orderIdList)) {
            return BigDecimal.ZERO;
        }
        // 优先redis中获取
        LocalDateTime truncationTime = DateUtils.parseTime(truncationTimeStr);
        String redisTruncationTimeStr = DateUtils.format(truncationTime, "yyyy-MM-dd/HH-mm-ss");
        String key = DisSystemConstant.DIS_STORE_TRUNCATION_DATE_TIME_SKU + appUserOut.getBizOrgCode() + ":" + appUserOut.getStoreCode() + redisTruncationTimeStr + ":" + skuCode;
        String sumQuantityObj = redisService.get(key);
        BigDecimal sumQuantity;
        if (StringUtils.isEmpty(sumQuantityObj)) {
            sumQuantity = ordDisOrderDetailMapper.sumExistsTotalPackageQuantityBySkuCodeAndOrderIdList(skuCode, orderIdList);
            if (null == sumQuantity) {
                sumQuantity = BigDecimal.ZERO;
            }
            redisService.set(key, sumQuantity, 10, TimeUnit.MINUTES);
        } else {
            sumQuantity = new BigDecimal(sumQuantityObj);
        }
        return null == sumQuantity ? BigDecimal.ZERO : sumQuantity;
    }

    @Override
    public int getIsNeedOrderQuantityWarning(BigDecimal orderQuantity, BigDecimal monthlySales) {
        monthlySales = monthlySales.divide(new BigDecimal("4"), 0, RoundingMode.DOWN);
        if (orderQuantity.compareTo(monthlySales) >= 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public List<AppTopDisOrderGoodsOut> findTopOrderDetailListByOrderId(Long orderId, Integer topNum) {
        return ordDisOrderDetailMapper.findTopOrderDetailListByOrderId(orderId, topNum);
    }

    @Override
    public OrdDisOrderDetail getOrderDetailByIdAndOrderId(Long id, Long orderId) {
        OrdDisOrderDetail orderDetail = new OrdDisOrderDetail();
        orderDetail.setId(id);
        orderDetail.setOrderId(orderId);
        orderDetail.setIsDelete(0);
        return ordDisOrderDetailMapper.selectOne(orderDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveDisOrderDetail(List<OrdDisOrderDetail> ordDisOrderDetailList) {
        ordDisOrderDetailMapper.batchSaveDisOrderDetail(ordDisOrderDetailList);
    }

    /**
     * 查询明细赠品信息
     *
     * @param orderDetailOuts
     * @param bizOrgCode
     * @return
     */
    void initBaseAndGiftOrderDetails(List<OrderDetailOut> orderDetailOuts, String bizOrgCode) {
        Map<String, List<OrdDisOrderDetail>> giftMap = orderDetailOuts.stream().filter(dtl -> NumberUtils.INTEGER_ONE.equals(dtl.getIsGift())).collect(Collectors.groupingBy(OrdDisOrderDetail::getBaseGoodsCode));
        for (OrderDetailOut item : orderDetailOuts) {
            this.transOrderDetailOut(item, bizOrgCode);
            item.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(item.getGoodsType()));
            //配送方式转中文
            item.setDistributionTypeStr(DistributionWaysEnum.getNameByType(item.getDistributionType()));
            if (NumberUtils.INTEGER_ZERO.equals(item.getIsGift())) {
                if (giftMap.containsKey(item.getGoodsCode())) {
                    List<OrdDisOrderDetail> ordDisOrderDetails = giftMap.get(item.getGoodsCode());
                    List<OrderDetailOut> detailOuts = new ArrayList<>();
                    for (OrdDisOrderDetail detail : ordDisOrderDetails) {
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

        orderDetailOut.setActivityCode(orderDetailOut.getActivityNo());
        orderDetailOut.setActivityType(ActivityTypeEnum.getTagNameByCode(orderDetailOut.getActivityType()));
        orderDetailOut.setInvoiceTypeStr(InvoiceTypeEnum.getNameByCode(orderDetailOut.getInvoiceType()));
    }

    /**
     * 导出数据转化
     *
     * @param list
     * @return
     */
    private List<ExcelOrderDetailOut> parseDataToExcel(List<OrderDetailOut> list) {
        List<ExcelOrderDetailOut> excelOrderDetailOuts = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ExcelOrderDetailOut excelOrderDetailOut = new ExcelOrderDetailOut();
            OrderDetailOut orderDetailOut = list.get(i);

            BeanUtils.copy(orderDetailOut, excelOrderDetailOut);
            excelOrderDetailOut.setIndex(i + 1);
            if (orderDetailOut.getIsGift() != null) {
                excelOrderDetailOut.setIsGiftStr(orderDetailOut.getIsGift() == 1 ? "赠品" : "主商品");
            }
            if (orderDetailOut.getAllowDistributionReturn() != null) {
                excelOrderDetailOut.setAllowDistributionReturnStr(orderDetailOut.getAllowDistributionReturn() == 1 ? "是" : "否");
            }
            excelOrderDetailOut.setDistributionType(DistributionWaysEnum.getNameByType(orderDetailOut.getDistributionType()));
            excelOrderDetailOut.setSmallSortStr(orderDetailOut.getSmallSortStr() + "【" + orderDetailOut.getSmallSort() + "】");
            excelOrderDetailOut.setPositionName(orderDetailOut.getPositionName() + "【" + orderDetailOut.getPosition() + "】");
            excelOrderDetailOuts.add(excelOrderDetailOut);
        }
        return excelOrderDetailOuts;
    }
}
