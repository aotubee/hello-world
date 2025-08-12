package com.edc.erp.directly.distribution.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.warning.LogisticsMessageOrderQuantityIn;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.stock.StockTransInfoOut;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StandardSortService;
import com.edc.erp.common.service.StockServer;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.distribution.entity.OrdDirOrder;
import com.edc.erp.directly.distribution.entity.OrdDirOrderCycle;
import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import com.edc.erp.directly.distribution.handle.DirOrderCycleHandle;
import com.edc.erp.directly.distribution.handle.OrderHandle;
import com.edc.erp.directly.distribution.mapper.OrdDirOrderDetailMapper;
import com.edc.erp.directly.distribution.model.excel.ExcelOrdDirOrderDetail;
import com.edc.erp.directly.distribution.model.in.OrderDetailIn;
import com.edc.erp.directly.distribution.model.out.*;
import com.edc.erp.directly.distribution.service.OrdDirOrderDetailService;
import com.edc.erp.directly.entity.DirOrderProcessConfigItem;
import com.edc.erp.directly.enumeration.OrderLogEnum;
import com.edc.erp.directly.enumeration.OrderStatusEnum;
import com.edc.erp.directly.handle.DirOrderConfigHandle;
import com.edc.erp.directly.handle.DirRequestOrderHandle;
import com.edc.erp.directly.util.FileExportUtil;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订货单详细表(OrdDirOrderDetail)表服务实现类
 *
 * @author wanglidong
 * @since 2022-11-15 18:09:28
 */
@Service
@Slf4j
public class OrdDirOrderDetailServiceImpl extends BaseServiceImpl<OrdDirOrderDetail> implements OrdDirOrderDetailService {

    @Autowired
    private AsyncLogService asyncLogService;
    @Autowired
    private StockServer stockServer;
    @Autowired
    private FileService fileService;
    @Autowired
    private OrderHandle orderHandle;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OrderGoodsServer orderGoodsServer;
    @Autowired
    private DirOrderConfigHandle orderConfigHandle;
    @Autowired
    private DirRequestOrderHandle requestOrderHandle;
    @Autowired
    private DirOrderCycleHandle dirOrderCycleHandle;
    @Autowired
    private StandardSortService standardSortService;
    @Autowired
    private OrdDirOrderDetailMapper ordDirOrderDetailMapper;

    /**
     * 根据订单id查询明细信息
     *
     * @param orderId
     * @return
     */
    @Override
    public List<OrdDirOrderDetail> findOrderDetailListByOrderId(Long orderId) {
        OrdDirOrderDetail ordDisOrderDetail = new OrdDirOrderDetail();
        ordDisOrderDetail.setOrderId(orderId);
        ordDisOrderDetail.setIsDelete(ModelConst.DELETE.NO);
        return ordDirOrderDetailMapper.select(ordDisOrderDetail);
    }

    /**
     * 导出直营订货单详情
     *
     * @param orderDetailIn
     * @return
     */
    @Override
    public String exportOrdDirOrderDetail(OrderDetailIn orderDetailIn) {
        orderDetailIn.setPageNum(NumberUtil.INTEGER_ZERO);
        orderDetailIn.setPageSize(NumberUtil.INTEGER_ZERO);
        Page<OrderDetailOut> detailOutPage = this.findOrderDetailOutPage(orderDetailIn, UserUtil.getBizOrgCode());
        List<ExcelOrdDirOrderDetail> excelOrdDirOrderDetailList = this.parseDataToExcel(detailOutPage.getList());
        byte[] bytes = FileExportUtil.getFileBytesByData(excelOrdDirOrderDetailList, "直营订货单详情", "直营订货单详情", ExcelOrdDirOrderDetail.class, true);
        return fileService.uploadFile("直营订货单详情" + ".xlsx", bytes, OrdSystemConstant.SYSTEM_CODE, OrdSystemConstant.SYSTEM_NAME);
    }

    /**
     * 直营订货单明细列表查询
     *
     * @param orderDetailIn
     * @param bizOrgCode
     * @return
     */
    @Override
    public Page<OrderDetailOut> findOrderDetailOutPage(OrderDetailIn orderDetailIn, String bizOrgCode) {
        orderDetailIn.setIsDelete(ModelConst.DELETE.NO);
        List<OrderDetailOut> orderDetailOuts = ordDirOrderDetailMapper.findOrderDetailOutByPage(orderDetailIn);
        orderDetailOuts.stream().forEach(orderDetailOut -> {
            this.transOrderDetailOut(orderDetailOut, bizOrgCode);
            orderDetailOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(orderDetailOut.getGoodsType()));
            //配送方式转中文
            orderDetailOut.setDistributionTypeStr(DistributionWaysEnum.getNameByType(orderDetailOut.getDistributionType()));
            //赠品
            List<OrderDetailOut> giftOrderDetails = this.findGiftOrderDetails(orderDetailOut.getId(), orderDetailOut.getActivityType(), orderDetailOut.getActivityCode(), bizOrgCode);
            orderDetailOut.setGiftOrderDetails(giftOrderDetails);
        });
        Page resPage = new Page(orderDetailIn);
        resPage.setList(orderDetailOuts);
        return resPage;
    }

    /**
     * 删除存在的sku信息
     *
     * @param ordDirOrder
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSkuForExistsQuantity(OrdDirOrder ordDirOrder) {
        OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(ordDirOrder.getOrderCycleId(), ordDirOrder.getOrgCode());
        if (Objects.isNull(orderCycle)) {
            return;
        }
        List<OrdDirOrderDetail> orderDetailList = this.findOrderDetailListByOrderId(ordDirOrder.getId());
        if (CollectionUtils.isEmpty(orderDetailList)) {
            return;
        }
        LocalDateTime truncationTime = orderCycle.getTruncationDateTime();
        String redisTruncationTimeStr = DateUtils.format(truncationTime, "yyyy-MM-dd/HH-mm-ss");
        orderDetailList.forEach(orderDetail -> {
            String key = "storeTruncationDateTimeSku:" + ordDirOrder.getBizOrgCode() + ":" + ordDirOrder.getStoreCode() + redisTruncationTimeStr + ":" + orderDetail.getGoodsCode();
            redisService.del(key);
        });
    }

    public void removeGoodsForExistsQuantity(OrdDirOrder ordDirOrder, OrdDirOrderCycle orderCycle, List<OrdDirOrderDetail> orderDetailList) {
        LocalDateTime truncationTime = orderCycle.getTruncationDateTime();
        String redisTruncationTimeStr = DateUtils.format(truncationTime, "yyyy-MM-dd/HH-mm-ss");
        orderDetailList.forEach(orderDetail -> {
            String key = "storeTruncationDateTimeSku:" + ordDirOrder.getBizOrgCode() + ":" + ordDirOrder.getStoreCode() + redisTruncationTimeStr + ":" + orderDetail.getGoodsCode();
            redisService.del(key);
        });
    }

    /**
     * 作废直营订货单
     *
     * @param ordDirOrder
     * @param userName
     * @param bizOrgCode
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidOrder(OrdDirOrder ordDirOrder, String userName, String bizOrgCode) {
        String beforeStatusCode = ordDirOrder.getOrderStatusCode();
        String content = MessageFormat.format(OrderLogEnum.ORDER_STATUS_UPDATE.getKey(), ordDirOrder.getOrderNo(),
                OrderStatusEnum.getValueByKey(beforeStatusCode), OrderStatusEnum.getValueByKey(OrderStatusEnum.INVALID.getKey()));
        orderHandle.updateOrderStatus(ordDirOrder.getId(), ordDirOrder.getBizOrgCode(), OrderStatusEnum.INVALID.getKey(), SystemConstant.SYSTEM_USER);
        // 删除存在的sku信息
        this.removeSkuForExistsQuantity(ordDirOrder);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), ordDirOrder.getOrderNo(),
                OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), userName);
        String key = SystemConstant.DSS_STORE_ORDER_CREATED + bizOrgCode + SystemConstant.COLON + DateUtils.format(ordDirOrder.getCreateTime().toLocalDate())
                + SystemConstant.COLON + ordDirOrder.getStoreCode();
        redisService.del(key);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    /**
     * 加推直营订货单
     *
     * @param ordDirOrder
     * @param userName
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response addPushOrder(OrdDirOrder ordDirOrder, String userName) {
        OrdDirOrderCycle orderCycle = dirOrderCycleHandle.getOrderCycleByIdAndBizOrgCode(ordDirOrder.getOrderCycleId(), ordDirOrder.getBizOrgCode());
        if (!OrderStatusEnum.SUBMIT.getKey().equals(ordDirOrder.getOrderStatusCode())) {
            return Response.error("该订货单状态不能够加推");
        }
        DirOrderProcessConfigItem minAmountItem = orderConfigHandle.minAmountCheckType(orderCycle.getId(), orderCycle.getBizOrgCode());
        if (Objects.isNull(minAmountItem)) {
            throw new BusinessException("截单时起订额校验规则查询为空");
        }
        List<OrdDirOrder> orderList = Lists.newArrayList(ordDirOrder);
        String content = MessageFormat.format(OrderLogEnum.ADD_PUSH_ORDER.getKey(), ordDirOrder.getOrderNo());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.DIR_ORDER.getName(), String.valueOf(ordDirOrder.getId()),
                OrdLogTypeEnum.DIR_ORDER.getCode(), content, new Date(), userName);
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        requestOrderHandle.mergeDirOrder(orderCycle, minAmountItem.getItemCode(), orderList, userName, false, true, SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode());
        return Response.success();
    }

    /**
     * 保存订货单详情
     *
     * @param orderDetail
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OrdDirOrderDetail orderDetail) {
        ordDirOrderDetailMapper.insert(orderDetail);
    }

    /**
     * 查询直营订货单明细表头
     *
     * @param orderId
     * @param bizOrgCode
     * @return
     */
    @Override
    public BackHeaderOrderDetailOut getBackHeaderOrderDetailOut(Long orderId, String bizOrgCode) {
        BackHeaderOrderDetailOut backHeaderOrderDetailOut = new BackHeaderOrderDetailOut();
        DirOrderOut orderOut = orderHandle.getOrderOutById(orderId, bizOrgCode);
        if (orderOut == null) {
            return null;
        }
        BeanUtil.copyProperties(orderOut, backHeaderOrderDetailOut);
        // 支付金额 = 应付金额
        //backHeaderOrderDetailOut.setPayableTotalAmount(orderOut.getOrderAmount());
        // 配货金额合计 = 订单金额
        backHeaderOrderDetailOut.setOriginalUnitTotalPrice(orderOut.getOrderAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        // 明细
        OrdDirOrderDetail detail = new OrdDirOrderDetail();
        detail.setIsDelete(ModelConst.DELETE.NO);
        detail.setOrderId(orderOut.getId());
        List<OrdDirOrderDetail> orderDetails = ordDirOrderDetailMapper.select(detail);
//        // 配货金额
//        BigDecimal originalUnitTotalPrice = orderDetails.stream().map(m -> m.getOriginalUnitPrice().multiply(m.getPackageQuantity()))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        backHeaderOrderDetailOut.setOriginalUnitTotalPrice(originalUnitTotalPrice);
        // 商品总数
        BigDecimal totalQuantity = orderDetails.stream().map(OrdDirOrderDetail::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        backHeaderOrderDetailOut.setTotalQuantity(totalQuantity.intValue());
        return backHeaderOrderDetailOut;
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
        BigDecimal realUnitTotalPrice = orderDetailOut.getOrderUnitPrice() == null ? BigDecimal.ZERO : orderDetailOut.getOrderUnitPrice().multiply(orderDetailOut.getQuantity()).setScale(NumberUtil.INTEGER_FOUR, RoundingMode.UP);
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
     * 查询明细赠品信息
     *
     * @param orderDetailId
     * @param activityType
     * @param activityCode
     * @param bizOrgCode
     * @return
     */
    List<OrderDetailOut> findGiftOrderDetails(Long orderDetailId, String activityType, String activityCode, String bizOrgCode) {
        List<OrderDetailOut> giftOrderDetails = new ArrayList<>();
        OrdDirOrderDetail orderDetail = new OrdDirOrderDetail();
        orderDetail.setIsDelete(ModelConst.DELETE.NO);
        orderDetail.setId(orderDetailId);
        List<OrdDirOrderDetail> orderDetails = ordDirOrderDetailMapper.select(orderDetail);
        for (OrdDirOrderDetail detail : orderDetails) {
            OrderDetailOut detailOut = new OrderDetailOut();
            BeanUtils.copy(detail, detailOut);
            detailOut.setActivityCode(activityCode);
            detailOut.setActivityType(activityType);
            this.transOrderDetailOut(detailOut, bizOrgCode);

            giftOrderDetails.add(detailOut);
        }
        return giftOrderDetails;
    }

    /**
     * 导出数据转化
     *
     * @param list
     * @return
     */
    private List<ExcelOrdDirOrderDetail> parseDataToExcel(List<OrderDetailOut> list) {
        List<ExcelOrdDirOrderDetail> excelOrderDetailOuts = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ExcelOrdDirOrderDetail excelOrderDetailOut = new ExcelOrdDirOrderDetail();
            OrderDetailOut orderDetailOut = list.get(i);
            BeanUtils.copy(orderDetailOut, excelOrderDetailOut);
            excelOrderDetailOut.setNo(i + 1);
            if (orderDetailOut.getIsGift() != null) {
                excelOrderDetailOut.setIsGiftStr(orderDetailOut.getIsGift() == 1 ? "赠品" : "主商品");
            }
            if (orderDetailOut.getAllowDistributionReturn() != null) {
                excelOrderDetailOut.setAllowDistributionReturnStr(orderDetailOut.getAllowDistributionReturn() == 1 ? "是" : "否");
            }
            //商品品类属性转中文
            excelOrderDetailOut.setGoodsTypeStr(GoodsTypeEnum.getNameByCode(orderDetailOut.getGoodsType()));
            //配送方式转中文
            excelOrderDetailOut.setDistributionTypeStr(DistributionWaysEnum.getNameByType(orderDetailOut.getDistributionType()));
            //仓位代码转中文
            excelOrderDetailOut.setPositionStr(orderDetailOut.getPositionName() + "【" + orderDetailOut.getPosition() + "】");
            //商品小分类转中文
            excelOrderDetailOut.setSmallSortStr(orderDetailOut.getSmallSortStr() + "【" + orderDetailOut.getSmallSort() + "】");
            excelOrderDetailOuts.add(excelOrderDetailOut);
        }
        return excelOrderDetailOuts;
    }

    @Override
    public BigDecimal sumPackageByOrderIdList(String skuCode, List<Long> orderIdList, AppUserOut appUserOut, String truncationTimeStr) {
        if (CollectionUtils.isEmpty(orderIdList)) {
            return BigDecimal.ZERO;
        }
        // 优先redis中获取
        LocalDateTime truncationTime = DateUtils.parseTime(truncationTimeStr);
        String redisTruncationTimeStr = DateUtils.format(truncationTime, "yyyy-MM-dd/HH-mm-ss");
        String key = DirSystemConstant.DIR_STORE_TRUNCATION_DATE_TIME_SKU + appUserOut.getBizOrgCode() + ":" + appUserOut.getStoreCode() + redisTruncationTimeStr + ":" + skuCode;
        String sumQuantityObj = redisService.get(key);
        BigDecimal sumQuantity;
        if (StringUtils.isEmpty(sumQuantityObj)) {
            sumQuantity = ordDirOrderDetailMapper.sumExistsTotalPackageQuantityBySkuCodeAndOrderIdList(skuCode, orderIdList);
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
    public List<AppTopDirOrderGoodsOut> findTopOrderDetailListByOrderId(Long orderId, Integer topNum) {
        return ordDirOrderDetailMapper.findTopOrderDetailListByOrderId(orderId, topNum);
    }

    @Override
    public OrdDirOrderDetail getOrderDetailByIdAndOrderId(Long id, Long orderId) {
        OrdDirOrderDetail orderDetail = new OrdDirOrderDetail();
        orderDetail.setId(id);
        orderDetail.setOrderId(orderId);
        orderDetail.setIsDelete(0);
        return ordDirOrderDetailMapper.selectOne(orderDetail);
    }

    @Override
    public BigDecimal sumOrderSkuQuantity(LogisticsMessageOrderQuantityIn logisticsMessageOrderQuantityIn) {
        BigDecimal quantity = ordDirOrderDetailMapper.sumOrderSkuQuantity(logisticsMessageOrderQuantityIn);
        return Objects.nonNull(quantity) ? quantity : BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveDirOrderDetail(List<OrdDirOrderDetail> ordDirOrderDetailList) {
        ordDirOrderDetailMapper.batchSaveDirOrderDetail(ordDirOrderDetailList);
    }

    @Override
    public int countByOrderId(Long id) {
        OrdDirOrderDetail orderDetail = new OrdDirOrderDetail();
        orderDetail.setOrderId(id);
        orderDetail.setIsDelete(0);
        return ordDirOrderDetailMapper.selectCount(orderDetail);
    }

    @Override
    public Map<String, BigDecimal> findStoreOrderQuantityByTruncationTime(LocalDateTime truncationTime, String storeCode) {
        List<StoreOrderGoodsOut> list = ordDirOrderDetailMapper.findStoreOrderQuantityByTruncationTime(truncationTime, storeCode);
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.toMap(StoreOrderGoodsOut::getGoodsCode, StoreOrderGoodsOut::getTotalQuantity));
    }
}
