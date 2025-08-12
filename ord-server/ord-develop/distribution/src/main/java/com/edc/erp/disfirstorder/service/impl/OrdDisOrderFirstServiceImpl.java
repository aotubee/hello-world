package com.edc.erp.disfirstorder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.edc.erp.common.async.handel.SyncOrdDisOrderHandle;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.handle.AsyncExportHandle;
import com.edc.erp.common.model.in.fund.ForeignAccountFundIn;
import com.edc.erp.common.model.in.fund.FrozenOrderIn;
import com.edc.erp.common.model.in.fund.StoreFrozenIn;
import com.edc.erp.common.model.in.fund.UnFrozenIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.out.fund.ForeignAccountFundOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.goods.OrgSortOut;
import com.edc.erp.common.model.out.stock.StockInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.*;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import com.edc.erp.disdeliveryorder.model.in.OrdDisDeliveryIn;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDelivery;
import com.edc.erp.disfirstorder.enumeration.FirstOrderFreezeEnum;
import com.edc.erp.disfirstorder.handle.DisFirstOrderAsyncImportHandle;
import com.edc.erp.disfirstorder.handle.DisFirstOrderAuditHandle;
import com.edc.erp.disfirstorder.listener.DisFirstDirOrderAsyncListener;
import com.edc.erp.disfirstorder.mapper.OrdDisOrderFirstMapper;
import com.edc.erp.disfirstorder.model.excel.ImportFirstOrderDetail;
import com.edc.erp.disfirstorder.model.in.OrdDisFirstOrderAuditIn;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstDetailIn;
import com.edc.erp.disfirstorder.model.in.OrdDisOrderFirstIn;
import com.edc.erp.disfirstorder.model.out.FirstOrderConfigOut;
import com.edc.erp.disfirstorder.model.out.FirstOrderSortOut;
import com.edc.erp.disfirstorder.model.out.OrdDerDisOrderFirstOut;
import com.edc.erp.disfirstorder.model.out.OrdDisOrderFirstDetailOut;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDeliveryService;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDetailService;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import com.edc.uc.authority.util.UserUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 配销铺货单(OrdDisOrderFirst)表服务实现类
 *
 * @author weichao
 * @since 2022-10-10 16:18:11
 */
@Slf4j
@Service
public class OrdDisOrderFirstServiceImpl extends BaseServiceImpl<OrdDisOrderFirst> implements OrdDisOrderFirstService {
    @Autowired
    private OrdDisOrderFirstMapper ordDisOrderFirstMapper;
    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;
    @Autowired
    private OrdDisOrderFirstDeliveryService ordDisOrderFirstDeliveryService;
    @Autowired
    private StoreCenterService storeCenterService;
    @Autowired
    private OrdDisOrderFirstDetailService ordDisOrderFirstDetailService;
    @Autowired
    private OrderGoodsServer orderGoodsServer;
    @Autowired
    private UniqueUtils uniqueUtils;
    @Autowired
    private FundServer fundServer;
    @Autowired
    private AsyncPushTaskService asyncPushTaskService;
    @Autowired
    private SyncOrdDisOrderHandle syncOrdDisOrderHandle;
    @Autowired
    private WarehouseServer warehouseServer;
    @Autowired
    private StockServer stockServer;

    @Autowired
    private AsyncLogService asyncLogService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DisFirstOrderAsyncImportHandle disFirstOrderAsyncImportHandle;

    @Autowired
    private AsyncExportHandle asyncExportHandle;

    @Autowired
    private RedisService redisService;

    @Autowired
    private DisFirstOrderAuditHandle disFirstOrderAuditHandle;


    @Override
    public Page<OrdDerDisOrderFirstOut> findFirstOrderPage(OrdDisOrderFirstIn ordDisOrderFirstIn) {

        List<OrdDerDisOrderFirstOut> ordDerDisOrderFirstOuts = ordDisOrderFirstMapper.findFirstOrderByPage(ordDisOrderFirstIn);
        ordDerDisOrderFirstOuts.forEach(item -> {
            item.setFirstOrderStatusStr(FirstOrderStatusEnum.getNameByCode(item.getFirstOrderStatus()));
            item.setTotalAmount(item.getTotalAmount().setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP));
        });
        Page<OrdDerDisOrderFirstOut> outPage = new Page<>(ordDisOrderFirstIn);
        outPage.setList(ordDerDisOrderFirstOuts);
        return outPage;
    }

    @Override
    public OrdDerDisOrderFirstOut getFirstOrderOut(Long ordDisOrderFirstId) {
        OrdDerDisOrderFirstOut orderDisOrderFirstOut = new OrdDerDisOrderFirstOut();
        OrdDisOrderFirst orderFirst = ordDisOrderFirstMapper.selectByPrimaryKey(ordDisOrderFirstId);
        if (Objects.isNull(orderFirst)) {
            throw new BusinessException("此铺货单不存在");
        }
        BeanUtils.copy(orderFirst, orderDisOrderFirstOut);
        orderDisOrderFirstOut.setFirstOrderStatusStr(FirstOrderStatusEnum.getNameByCode(orderDisOrderFirstOut.getFirstOrderStatus()));
        // 配货单关联
        OrdDisOrderFirstDelivery orderFirstDelivery = new OrdDisOrderFirstDelivery();
        orderFirstDelivery.setFirstOrderId(orderFirst.getId());

        List<OrdDisOrderFirstDelivery> orderFirstDeliveries = ordDisOrderFirstDeliveryService.list(orderFirstDelivery);
        List<OrdDisDelivery> ordDisDeliveries = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orderFirstDeliveries)) {

            for (OrdDisOrderFirstDelivery ordDisOrderFirstDelivery : orderFirstDeliveries) {
                // 配货单
                OrdDisDelivery ordDisDelivery = ordDisDeliveryService.selectByPrimaryKey(ordDisOrderFirstDelivery.getDeliveryOrderId());
                if (Objects.isNull(ordDisDelivery)) {
                    continue;
                }
                ordDisDeliveries.add(ordDisDelivery);
            }
        }
        orderDisOrderFirstOut.setDeliveryOrders(ordDisDeliveries);
        return orderDisOrderFirstOut;

    }

//    @Override
//    public Response<OrdDisOrderFirst> auditFirstOrder(InsertFirstOrderDetailIn insertFirstOrderDetailIn) {
//        Response<OrdDisOrderFirst> response = ordDisOrderFirstDetailService.saveFirstOrderDetail(insertFirstOrderDetailIn);
//        if (!response.isSuccess()) {
//            return response;
//        }
//        return this.audit(response);
//    }
//
//    /**
//     * 审核
//     *
//     * @param response
//     * @return
//     */
//    @Transactional(rollbackFor = Exception.class)
//    public Response<OrdDisOrderFirst> audit(Response<OrdDisOrderFirst> response) {
//        OrdDisOrderFirst ordFirstOrder = response.getData();
//        OrdDisOrderFirst firstOrder = this.selectByPrimaryKey(ordFirstOrder.getId());
//        // 校验余额是否充足，不足则作废
//        boolean enough = this.checkAvailableBalance(firstOrder.getId());
//        if (!enough) {
//            throw new BusinessException("门店" + firstOrder.getStoreCode() + "可用资金不足");
//        }
//        // 更改审核状态
//        if (NumberUtil.INTEGER_ONE.equals(firstOrder.getIsEffectiveImmediately())) {
//            // 若设置生效时间为立即生效
//            firstOrder.setEffectiveTime(LocalDateTime.now());
//            firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.EXECUTED.getCode());
//        } else {
//            firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.APPROVED.getCode());
//        }
//        firstOrder.setApprovalTime(LocalDateTime.now());
//        firstOrder.setApprover(UserUtil.getUserName());
//        int update = ordDisOrderFirstMapper.updateByPrimaryKeySelective(firstOrder);
//        if (NumberUtil.INTEGER_ZERO.equals(update)) {
//            return Response.error("铺货单审核失败");
//        }
//        // 如果为生效状态，则拆分为配货单
//        if (FirstOrderStatusEnum.EXECUTED.getCode().equals(firstOrder.getFirstOrderStatus())) {
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_FIRST_TO_DELIVERY, JSONObject.toJSONString(firstOrder), firstOrder.getBizOrgCode());
//            //生效日志
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
//                    String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_EXECUTED.getName(), new Date(), firstOrder.getCreator());
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        }
//        //扣减资金
//        toFund(firstOrder);
//        String payContent = MessageFormat.format(OrdLogTypeEnum.DIS_FIRST_ORDER_GOODS_AUDIT.getName(), firstOrder.getTotalAmount());
//        //审核日志
//        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
//                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), payContent, new Date(), firstOrder.getCreator());
//        asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        return Response.data(firstOrder);
//    }

    @Override
    public void toFund(OrdDisOrderFirst firstOrder) {
//        if (BigDecimal.ZERO.compareTo(firstOrder.getTotalAmount()) == 0) {
//            return;
//        }
//        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
//        rechargeLiquidationIn.setBizOrgCode(firstOrder.getBizOrgCode());
//        rechargeLiquidationIn.setPayOrPrincipalCode(firstOrder.getStoreCode());
//        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
//        rechargeLiquidationIn.setRecipientPrincipalCode(firstOrder.getBizOrgCode());
//        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
//        rechargeLiquidationIn.setBusinessNo(firstOrder.getFirstOrderNo());
//        rechargeLiquidationIn.setBusinessType(FundTypeEnum.FIRST_ORDER.getCode());
//        rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
//        rechargeLiquidationIn.setLiquidationAmount(firstOrder.getTotalAmount().abs());
//        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
//        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
//        if (!response.isSuccess()) {
//            throw new BusinessException(response.getMessage());
//        }
        StoreFrozenIn storeFrozenIn = new StoreFrozenIn();
        storeFrozenIn.setPrincipalCode(firstOrder.getStoreCode());
        storeFrozenIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
        storeFrozenIn.setBizOrgCode(firstOrder.getBizOrgCode());
        FrozenOrderIn frozenOrderIn = new FrozenOrderIn();
        frozenOrderIn.setAmount(firstOrder.getTotalAmount().abs());
        frozenOrderIn.setBusinessNo(firstOrder.getFirstOrderNo());
        frozenOrderIn.setBusinessType(FundTypeEnum.FIRST_ORDER.getCode());
        List<FrozenOrderIn> frozenOrders = Collections.singletonList(frozenOrderIn);
        storeFrozenIn.setFrozenOrders(frozenOrders);
        Response response = syncOrdDisOrderHandle.syncToFrozen(storeFrozenIn);
        if (!response.isSuccess()) {
            log.error("配销铺货单{}冻结失败:{}", firstOrder.getFirstOrderNo(), response.getMessage());
            throw new BusinessException(response.getMessage());
        }
    }

    @Override
    public OrdDisOrderFirst getOrdDisFirstByDeliveryOrderId(Long deliveryOrderId) {
        return ordDisOrderFirstMapper.getOrdDisFirstByDeliveryOrderId(deliveryOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidFirstOrder(OrdDisOrderFirst firstOrder) {
        if (FirstOrderStatusEnum.EXECUTED.getCode().equals(firstOrder.getFirstOrderStatus())) {
            throw new BusinessException("已生效的铺货单不可作废");
        }
        if (FirstOrderStatusEnum.INVALID.getCode().equals(firstOrder.getFirstOrderStatus())) {
            throw new BusinessException("已作废的铺货单不可作废");
        }
        String content = OrdLogTypeEnum.FIRST_ORDER_GOODS_INVALID.getName();
        // 已审核,退款整单
        if (FirstOrderStatusEnum.APPROVED.getCode().equals(firstOrder.getFirstOrderStatus())) {
            this.returnFundForSplitFirstOrder(firstOrder, firstOrder.getTotalAmount(), FundReturnTypeEnum.DIS_FIRST_ORDER_INVALID.getName());
            content = MessageFormat.format(OrdLogTypeEnum.FIRST_ORDER_GOODS_INVALID_AND_RETURN_UNFREEZE.getName(), firstOrder.getTotalAmount());
        }
        firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.INVALID.getCode());
        firstOrder.setFreezeStatus(FirstOrderFreezeEnum.RELEASE.getKey());
        int count = ordDisOrderFirstMapper.updateByPrimaryKeySelective(firstOrder);
        // 作废日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), content,
                new Date(), firstOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return count;
    }

    /**
     * 铺货单校验资金
     *
     * @param firstOrderId
     * @return
     */
    @Override
    public boolean checkAvailableBalance(Long firstOrderId) {
        OrdDisOrderFirst query = new OrdDisOrderFirst();
        query.setId(firstOrderId);
        query.setIsDelete(NumberUtil.INTEGER_ZERO);
        OrdDisOrderFirst firstOrder = this.selectOne(query);
        if (firstOrder == null) {
            throw new BusinessException("此铺货单不存在");
        }
        StoreOut inStoreOut = storeCenterService.getStoreInfoByErpStoreCode(firstOrder.getStoreCode());
        if (Objects.isNull(inStoreOut) || Objects.isNull(inStoreOut.getStoreId())) {
            throw new BusinessException("铺货单门店获取失败，请重试");
        }
        //资金校验
        ForeignAccountFundIn foreignAccountFundIn = new ForeignAccountFundIn();
        foreignAccountFundIn.setBizOrgCode(firstOrder.getBizOrgCode());
        foreignAccountFundIn.setPrincipalCode(firstOrder.getStoreCode());
        foreignAccountFundIn.setPrincipalType(PrincipalTypeEnum.STORE.getCode());
        ForeignAccountFundOut foreignAccountFundOut = fundServer.getAvailableAmount(foreignAccountFundIn);
        if (Objects.isNull(foreignAccountFundOut) || foreignAccountFundOut.getAvailableAmount().add(foreignAccountFundOut.getCredit()).compareTo(firstOrder.getTotalAmount()) < NumberUtil.INTEGER_ZERO) {
            log.info("门店" + firstOrder.getStoreCode() + "可用资金不足");
            return false;
        }
        return true;

    }

    /**
     * 添加铺货单
     *
     * @param firstOrder
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertFirstOrder(OrdDisOrderFirst firstOrder) {
        firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.PENDING.getCode());
        String orgOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XP.getCode(), UserUtil.getBizOrgCode(), uniqueUtils, NumberUtil.INTEGER_FOUR);
        firstOrder.setFirstOrderNo(orgOrderNo);
        firstOrder.setCreateTime(LocalDateTime.now());
        firstOrder.setUpdateTime(LocalDateTime.now());
        firstOrder.setIsDelete(0);
        insertSelective(firstOrder);
        //  保存日志
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_SAVE.getName(), new Date(), firstOrder.getCreator());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);

        return firstOrder.getId();
    }

    /**
     * 分割品类名称
     *
     * @param firstOrderSortOut
     * @param bizOrgCode
     */
    private void splitSort(FirstOrderSortOut firstOrderSortOut, String bizOrgCode) {
        StringJoiner categoryName = new StringJoiner(",");
        String[] split = firstOrderSortOut.getSortCode().split(",");
        for (String sortCode : split) {
            OrgSortOut orgSortOut = orderGoodsServer.getByCode(sortCode, bizOrgCode);
            categoryName.add(Objects.nonNull(orgSortOut) ? orgSortOut.getSortName() : "");
        }
        firstOrderSortOut.setSortName(categoryName.toString());
    }

    /**
     * 根据铺货拆单配置拆分配销单
     *
     * @param firstOrder
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<List<OrdDisDeliveryIn>> spiltFirstOrderByConfig(OrdDisOrderFirst firstOrder) {
        String bizOrgCode = firstOrder.getBizOrgCode();
        //获取铺货单拆单配置
        FirstOrderConfigOut firstOrderConfigOut = getFirstOrderConfigOut(bizOrgCode);
        // 商品明细
        OrdDisOrderFirstDetailIn query = new OrdDisOrderFirstDetailIn();
        query.setFirstOrderId(firstOrder.getId());
        Page<OrdDisOrderFirstDetailOut> detailPage = ordDisOrderFirstDetailService.findOrdDisOrderFirstDetailPage(query, bizOrgCode);
        List<OrdDisOrderFirstDetailOut> orderDetailList = detailPage.getList();
        if (CollectionUtils.isEmpty(detailPage.getList())) {
            throw new BusinessException("没有需要拆单的明细");
        }
        Map<String, OrdDisOrderFirstDetailOut> skuOrderDetailMap = orderDetailList.stream().collect(Collectors.toMap(OrdDisOrderFirstDetailOut::getGoodsCode, Function.identity()));
        List<String> skuCodes = orderDetailList.stream().map(OrdDisOrderFirstDetailOut::getGoodsCode).collect(Collectors.toList());
        // 筛出符合配货商品范围的商品集合
        List<String> legalOrderCodeList = checkGoods(firstOrder, bizOrgCode, skuCodes);
        // 合规的商品集合
        List<OrdDisOrderFirstDetailOut> legalOrderDetails = new ArrayList<>();
        if (CollectionUtils.isEmpty(legalOrderCodeList)) {
            log.info("配销配销铺货单{}没有需要拆单的符合商品范围的明细", firstOrder.getFirstOrderNo());
            this.returnFundForSplitFirstOrder(firstOrder, firstOrder.getTotalAmount(), FundReturnTypeEnum.DIS_FIRST_ORDER_RETURN.getName());
            firstOrder.setFreezeStatus(FirstOrderFreezeEnum.RELEASE.getKey());
            ordDisOrderFirstMapper.updateByPrimaryKeySelective(firstOrder);
            String content = MessageFormat.format(OrdLogTypeEnum.DIS_FIRST_ORDER_EMPTY_UNFREEZE.getName(), firstOrder.getFirstOrderNo());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                    String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), content, new Date(), firstOrder.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.data(null, "没有可拆分的商品");
        }
        for (String legalGoodsCode : legalOrderCodeList) {
            if (skuOrderDetailMap.containsKey(legalGoodsCode)) {
                legalOrderDetails.add(skuOrderDetailMap.get(legalGoodsCode));
            }
        }
        // 拆分的明细集合
        Map<String, List<OrdDisOrderFirstDetailOut>> firstOrderDetailMap = new TreeMap<>();
        for (OrdDisOrderFirstDetailOut orderDetailOut : legalOrderDetails) {
            if (Objects.isNull(orderDetailOut.getDistributionPrice())) {
                log.error(orderDetailOut.getGoodsCode() + "此商品无配销价，该条明细作废");
                continue;
            }
            StringJoiner keys = new StringJoiner("-");
            // 增加仓位
            if (NumberUtil.INTEGER_ONE.equals(firstOrderConfigOut.getIsStock())) {
                keys.add(orderDetailOut.getStockCode());
            }
            // 增加配送方式
            if (NumberUtil.INTEGER_ONE.equals(firstOrderConfigOut.getIsDistributionMode())) {
                keys.add(orderDetailOut.getDistributionType());
            }
            // 增加分类
            if (NumberUtil.INTEGER_ONE.equals(firstOrderConfigOut.getIsSort())) {
                splitSort(firstOrderConfigOut, orderDetailOut, keys);
            }
            String key = keys.toString();
            List<OrdDisOrderFirstDetailOut> firstOrderDetailOuts = firstOrderDetailMap.get(key);
            if (CollectionUtils.isEmpty(firstOrderDetailOuts)) {
                firstOrderDetailOuts = Lists.newArrayList();
            }
            firstOrderDetailOuts.add(orderDetailOut);
            // 拆分好的明细集合
            firstOrderDetailMap.put(key, firstOrderDetailOuts);
        }
        List<OrdDisDeliveryIn> deliveryOrderList = Lists.newArrayList();
        firstOrderDetailMap.entrySet().forEach(entry -> {
            // 初始化配销单
            this.initOrdDisDeliveryIn(firstOrder, bizOrgCode, deliveryOrderList, entry);
            log.info("铺货单{}拆单配销单集合数量是--{}", firstOrder.getFirstOrderNo(), deliveryOrderList.size());
        });
        ArrayList<Long> ordDisDeliveryIds = new ArrayList<>();
        for (OrdDisDeliveryIn ordDisDelivery : deliveryOrderList) {
            ordDisDelivery.setSourceCode(DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType());
            ordDisDelivery.setAuditType(SalvageAuditTypeEnum.NOW_AUTO_AUDIT.getCode());
            Long orderId = ordDisDeliveryService.saveOrUpdate(ordDisDelivery);
            ordDisDelivery.setId(orderId);
            ordDisDeliveryIds.add(orderId);
        }
        ordDisOrderFirstDeliveryService.batchSave(ordDisDeliveryIds, firstOrder.getId(), firstOrder.getUpdater());
        firstOrder.setFirstOrderStatus(FirstOrderStatusEnum.EXECUTED.getCode());
        firstOrder.setUpdateTime(LocalDateTime.now());
        ordDisOrderFirstMapper.updateByPrimaryKeySelective(firstOrder);
        List<BusinessLog> businessLogList = Lists.newArrayList();
        //生效日志
        BusinessLog executeBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_EXECUTED.getName(), new Date(),
                firstOrder.getUpdater());
        businessLogList.add(executeBusinessLog);
        // 不合规的通过定时任务 解冻原单冻结新单逻辑  代替返款逻辑
        StringJoiner illegalGoodsJoiner = new StringJoiner(",");
        Map<String, OrdDisOrderFirstDetailOut> legalSkuOrderDetailMap = legalOrderDetails.stream().collect(Collectors.toMap(OrdDisOrderFirstDetailOut::getGoodsCode, Function.identity()));
        orderDetailList.forEach(detail -> optIllegalGoods(detail.getGoodsCode(), legalSkuOrderDetailMap, illegalGoodsJoiner));
        if (illegalGoodsJoiner.length() > NumberUtil.INTEGER_ZERO) {
            String content = MessageFormat.format(OrdLogTypeEnum.FIRST_ORDER_ILLEGAL_GOODS.getName(), illegalGoodsJoiner.toString());
            // 作废日志
            BusinessLog illegalGoodsBusinessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                    String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), content, new Date(), firstOrder.getCreator());
            businessLogList.add(illegalGoodsBusinessLog);
        }
        // 不合规铺货商品返款金额
//        BigDecimal illegalDetailReturnAmount = orderDetailList.stream().filter(detail -> optIllegalGoods(detail.getGoodsCode(), legalSkuOrderDetailMap, illegalGoodsJoiner))
//                .map(OrdDisOrderFirstDetail::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        // 判断是否需要返款
//        if (illegalDetailReturnAmount.compareTo(BigDecimal.ZERO) == NumberUtil.INTEGER_ONE) {
//            this.returnFundForSplitFirstOrder(firstOrder, illegalDetailReturnAmount, FundReturnTypeEnum.DIS_FIRST_ORDER_RETURN.getName());
//            String content = MessageFormat.format(OrdLogTypeEnum.FIRST_ORDER_GOODS_RETURN_AMOUNT.getName(), illegalGoodsJoiner.toString(), illegalDetailReturnAmount);
//            String content = MessageFormat.format(OrdLogTypeEnum.FIRST_ORDER_ILLEGAL_GOODS.getName(), illegalGoodsJoiner.toString());
//            // 作废日志
//            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
//                    String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), content, new Date(), firstOrder.getCreator());
//            asyncLogService.sendAsyncSaveLogByMq(businessLog);
//        }
        businessLogList.forEach(businessLog -> asyncLogService.sendAsyncSaveLogByMq(businessLog));
        return Response.data(deliveryOrderList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrdDisOrderFirst getOrdDisOrderFirstForImport(Long firstOrderId, StoreOut storeOut, String loginUsername, Integer isEffectiveImmediately, LocalDateTime effectiveTime) {
        OrdDisOrderFirst ordDisOrderFirst = new OrdDerDisOrderFirstOut();
        ordDisOrderFirst.setUpdater(loginUsername);
        ordDisOrderFirst.setIsEffectiveImmediately(isEffectiveImmediately);
        ordDisOrderFirst.setEffectiveTime(effectiveTime);
        if (Objects.isNull(firstOrderId)) {
            ordDisOrderFirst.setStoreCode(storeOut.getStoreCode());
            ordDisOrderFirst.setStoreName(storeOut.getStoreName());
            ordDisOrderFirst.setTotalNum(NumberUtil.INTEGER_ZERO);
            ordDisOrderFirst.setTotalAmount(BigDecimal.ZERO);
            ordDisOrderFirst.setGoodsNum(NumberUtil.INTEGER_ZERO);
            ordDisOrderFirst.setOrgCode(UserUtil.getOrgCode());
            ordDisOrderFirst.setBizOrgCode(UserUtil.getBizOrgCode());
            ordDisOrderFirst.setCreator(loginUsername);
            String firstOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XP.getCode(), ordDisOrderFirst.getBizOrgCode(), uniqueUtils, 4);
            ordDisOrderFirst.setFirstOrderNo(firstOrderNo);
            ordDisOrderFirst.setFirstOrderStatus(FirstOrderStatusEnum.PENDING.getCode());
            ordDisOrderFirst.setIsDelete(ModelConst.DELETE.NO);
            ordDisOrderFirst.setCreateTime(LocalDateTime.now());
            ordDisOrderFirstMapper.insert(ordDisOrderFirst);
            // 添加日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                    String.valueOf(ordDisOrderFirst.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                    OrdLogTypeEnum.FIRST_ORDER_GOODS_SAVE.getName(), new Date(), ordDisOrderFirst.getCreator());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        } else {
            ordDisOrderFirst = ordDisOrderFirstMapper.selectByPrimaryKey(firstOrderId);
            ordDisOrderFirstMapper.updateByPrimaryKeySelective(ordDisOrderFirst);
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                    String.valueOf(ordDisOrderFirst.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
                    OrdLogTypeEnum.FIRST_ORDER_GOODS_UPDATE.getName(), new Date(), ordDisOrderFirst.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
        }
        return ordDisOrderFirst;
    }

    @Override
    public Response<Long> asyncImportFirstOrderDetail(String fileId, OrdDisOrderFirst ordDisOrderFirst, String loginUsername) {
        byte[] bytes = fileService.getFileBytesByFileId(fileId, SystemConstant.SYSTEM_CODE);
        if (null == bytes) {
            return Response.error("无效的Excel模板");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DisFirstDirOrderAsyncListener listener = new DisFirstDirOrderAsyncListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, ImportFirstOrderDetail.class, listener).headRowNumber(1).build();
        ReadSheet readSheet = EasyExcel.readSheet(NumberUtil.INTEGER_ZERO).build();
        excelReader.read(readSheet).finish();
        Map<String, StringJoiner> totalErrorMap = listener.totalErrorMap;
        if (totalErrorMap.size() > 0) {
            return Response.data(ordDisOrderFirst.getId(), "导入失败：" + listener.getImportErrorMessage(totalErrorMap));
        }
        List<ImportFirstOrderDetail> importFirstOrderDetailList = listener.getOutDetails();
        disFirstOrderAsyncImportHandle.handleAsyncFirstOrder(ordDisOrderFirst.getId(), importFirstOrderDetailList, loginUsername);
        return Response.data(ordDisOrderFirst.getId(), "文件导入中，稍后刷新查看！");
    }

    @Override
    public Response<String> auditFirstOrder(OrdDisFirstOrderAuditIn ordDisFirstOrderAuditIn, String loginUsername) {
        OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstMapper.selectByPrimaryKey(ordDisFirstOrderAuditIn.getFirstOrderId());
        if (Objects.isNull(ordDisOrderFirst)) {
            return Response.error("配销铺货单不存在");
        }
        if (FirstOrderStatusEnum.INVALID.getCode().equals(ordDisOrderFirst.getFirstOrderStatus())) {
            return Response.error("铺货单已作废");
        }
        if (FirstOrderStatusEnum.APPROVED.getCode().equals(ordDisOrderFirst.getFirstOrderStatus())) {
            return Response.error("请勿重复审核");
        }
        int detailCount = ordDisOrderFirstDetailService.countByFirstOrderId(ordDisFirstOrderAuditIn.getFirstOrderId());
        if (detailCount == NumberUtil.INTEGER_ZERO) {
            return Response.error("配销铺货单明细为空");
        }
        String key = SystemConstant.ORD_DIS_FIRST_ORDER_AUDIT + SystemConstant.COLON + ordDisOrderFirst.getBizOrgCode()
                + SystemConstant.COLON + ordDisOrderFirst.getFirstOrderNo();
        if (!redisService.setIfAbsent(key, ordDisOrderFirst.getFirstOrderNo(), 20l, TimeUnit.MINUTES)) {
            return Response.error("审核中，请勿重复审核");
        }
        // 校验余额是否充足，不足则作废
        boolean enough = this.checkAvailableBalance(ordDisOrderFirst.getId());
        if (!enough) {
            redisService.del(key);
            return Response.error("可用资金不足");
        }
        disFirstOrderAuditHandle.asyncCheckByAuditFirstOrder(ordDisFirstOrderAuditIn, loginUsername, key);
        return Response.success("审核中，请稍后查看");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDirFirstOrder(OrdDisOrderFirst ordDisOrderFirst) {
        String firstOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.XP.getCode(), ordDisOrderFirst.getBizOrgCode(), uniqueUtils, 4);
        ordDisOrderFirst.setFirstOrderNo(firstOrderNo);
        ordDisOrderFirst.setFirstOrderStatus(FirstOrderStatusEnum.PENDING.getCode());
        ordDisOrderFirst.setIsDelete(ModelConst.DELETE.NO);
        ordDisOrderFirst.setOrgCode(UserUtil.getOrgCode());
        ordDisOrderFirst.setCreateTime(LocalDateTime.now());
        ordDisOrderFirst.setUpdateTime(LocalDateTime.now());
        ordDisOrderFirstMapper.insert(ordDisOrderFirst);
        return ordDisOrderFirst.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unFreezeDisFirstOrder(OrdDisOrderFirst ordDisOrderFirst) {
        return ordDisOrderFirstMapper.unFreezeDisFirstOrder(ordDisOrderFirst);
    }

    @Override
    public OrdDisOrderFirst getNeedUnFreezeOne(Long id) {
        OrdDisOrderFirst ordDisOrderFirst = new OrdDerDisOrderFirstOut();
        ordDisOrderFirst.setId(id);
        ordDisOrderFirst.setFreezeStatus(FirstOrderFreezeEnum.FREEZE.getKey());
        ordDisOrderFirst.setIsDelete(ModelConst.DELETE.NO);
        return ordDisOrderFirstMapper.selectOne(ordDisOrderFirst);
    }


    private boolean optIllegalGoods(String goodsCode, Map<String, OrdDisOrderFirstDetailOut> legalSkuOrderDetailMap, StringJoiner illegalGoodsJoiner) {
        if (legalSkuOrderDetailMap.containsKey(goodsCode)) {
            return false;
        }
        illegalGoodsJoiner.add(goodsCode);
        return true;
    }

    /**
     * 退还首单铺货不合格商品支付金额
     *
     * @param firstOrder
     * @param illegalDetailReturnAmount
     */
    private void returnFundForSplitFirstOrder(OrdDisOrderFirst firstOrder, BigDecimal illegalDetailReturnAmount, String fundReturnType) {
//        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
//        rechargeLiquidationIn.setRecipientPrincipalCode(firstOrder.getStoreCode());
//        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
//        rechargeLiquidationIn.setPayOrPrincipalCode(firstOrder.getBizOrgCode());
//        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
//        rechargeLiquidationIn.setBizOrgCode(firstOrder.getBizOrgCode());
//        rechargeLiquidationIn.setBusinessNo(firstOrder.getFirstOrderNo());
//        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DIS_FIRST_ORDER_RETURN.getCode());
//        rechargeLiquidationIn.setLiquidationAmount(illegalDetailReturnAmount);
//        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
//        rechargeLiquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
//        rechargeLiquidationIn.setOriginalBusinessNo(firstOrder.getFirstOrderNo());
//        rechargeLiquidationIn.setRemark(fundReturnType);
//        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
        UnFrozenIn unFrozenIn = new UnFrozenIn();
        unFrozenIn.setUnFrozenBusinessNos(Collections.singletonList(firstOrder.getFirstOrderNo()));
        Response response = syncOrdDisOrderHandle.syncToUnFrozen(unFrozenIn);
        if (!response.isSuccess()) {
//            log.error("配销收单铺货{}退还不合格商品支付金额异常：{}", firstOrder.getFirstOrderNo(), response.getMessage());
            log.error("配销首单铺货{}解冻不合格商品支付金额异常：{}", firstOrder.getFirstOrderNo(), response.getMessage());
            throw new BusinessException("配销收单铺货" + firstOrder.getFirstOrderNo() + "退还不合格商品支付金额异常:" + response.getMessage());
        }
//        String content = MessageFormat.format(OrdLogTypeEnum.FIRST_ORDER_GOODS_EXECUTED.getName(), illegalDetailReturnAmount);
//        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(),
//                String.valueOf(firstOrder.getId()), OrdLogTypeEnum.FIRST_ORDER_GOODS.getCode(), content, new Date(), firstOrder.getCreator());
//        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    private FirstOrderConfigOut getFirstOrderConfigOut(String bizOrgCode) {
        FirstOrderConfigOut firstOrderConfigOut = ordDisOrderFirstMapper.getOrderFirstConfig(bizOrgCode);
        if (Objects.isNull(firstOrderConfigOut)) {
            throw new BusinessException("未配置拆单规则");
        }
        if (firstOrderConfigOut.getIsSort().equals(NumberUtil.INTEGER_ONE)) {
            List<FirstOrderSortOut> firstOrderSortOuts = ordDisOrderFirstMapper.findOrderFirstConfigSort(firstOrderConfigOut.getId());
            firstOrderSortOuts.forEach(firstOrderSortOut -> {
                splitSort(firstOrderSortOut, bizOrgCode);
            });
            firstOrderConfigOut.setFirstOrderSortOuts(firstOrderSortOuts);
        }
        return firstOrderConfigOut;
    }

    private List<String> checkGoods(OrdDisOrderFirst firstOrder, String bizOrgCode, List<String> skuCodes) {
        //校验是否可铺货
        OrderGoodsIn orderGoodsInIsFirst = new OrderGoodsIn();
        orderGoodsInIsFirst.setStoreCode(firstOrder.getStoreCode());
        orderGoodsInIsFirst.setGoodsCodeList(skuCodes);
        orderGoodsInIsFirst.setBizOrgCode(bizOrgCode);
        orderGoodsInIsFirst.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
        List<String> checkFirstlegalOrderCodeList = orderGoodsServer.findBusinessGoodsCodeList(orderGoodsInIsFirst);
        //所有商品不满足铺货条件
        if (CollectionUtils.isEmpty(checkFirstlegalOrderCodeList)) {
            return null;
        }
        //校验配销
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setStoreCode(firstOrder.getStoreCode());
        orderGoodsIn.setGoodsCodeList(skuCodes);
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.DEALER_BILL.getType());
        List<String> legalOrderCodeList = orderGoodsServer.findBusinessGoodsCodeList(orderGoodsIn);
        //所有商品不能生成配销单
        if (CollectionUtils.isEmpty(legalOrderCodeList)) {
            return null;
        }
        //取交集
        List<String> codeList = checkFirstlegalOrderCodeList.stream().filter(legalOrderCodeList::contains).collect(Collectors.toList());
        return codeList;
    }

    /**
     * 初始化配销单
     *
     * @param firstOrder
     * @param bizOrgCode
     * @param deliveryOrderList
     * @param entry
     * @return
     */
    private List<OrdDisDeliveryDetail> initOrdDisDeliveryIn(OrdDisOrderFirst firstOrder, String bizOrgCode, List<OrdDisDeliveryIn> deliveryOrderList,
                                                            Map.Entry<String, List<OrdDisOrderFirstDetailOut>> entry) {
        String[] splits = entry.getKey().split(SystemConstant.SHORT_LINE);
        OrdDisDeliveryIn deliveryOrder = new OrdDisDeliveryIn();
        deliveryOrder.setDeliveryOrderNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PX.getCode(), bizOrgCode, uniqueUtils, NumberUtil.INTEGER_FOUR));
        String type = DistributionWaysEnum.getTypeByName(splits[1]);
        deliveryOrder.setDistributionType(type);
        deliveryOrder.setStockCode(splits[0]);
        deliveryOrder.setOrgCode(firstOrder.getOrgCode());
        StockInfoOut stockInfoOut = stockServer.getTransInfo(deliveryOrder.getStockCode());
        if (Objects.isNull(stockInfoOut) || StringUtil.isEmpty(stockInfoOut.getWarehouseCode())) {
            throw new BusinessException("仓储不存在");
        }
        deliveryOrder.setWrhCode(stockInfoOut.getWarehouseCode());
        deliveryOrder.setBizOrgCode(bizOrgCode);
        deliveryOrder.setStoreCode(firstOrder.getStoreCode());
        deliveryOrder.setStoreName(firstOrder.getStoreName());
        deliveryOrder.setDeliveryStatusCode(DeliveryOrderEnum.PENDING.getKey());
        deliveryOrder.setUpdater(firstOrder.getUpdater());
        deliveryOrder.setCreator(firstOrder.getCreator());
        deliveryOrder.setOrderQuantity(BigDecimal.valueOf(firstOrder.getTotalNum()));
        deliveryOrder.setOrderAmount(firstOrder.getTotalAmount());
//        deliveryOrder.setDeliveryQuantity(BigDecimal.valueOf(firstOrder.getTotalNum()));
//        deliveryOrder.setDeliveryAmount(firstOrder.getTotalAmount());
        deliveryOrder.setIsDelete(NumberUtil.INTEGER_ZERO);
//        deliveryOrder.setIsFirstOrderSource(NumberUtil.INTEGER_ONE);
        // 配货单明细保存
        List<OrdDisDeliveryDetail> deliveryOrderDetailsList = Lists.newArrayList();
        List<OrdDisOrderFirstDetailOut> value = entry.getValue();
        value.forEach(firstOrderDetailOut -> {
            OrdDisDeliveryDetail deliveryOrderDetail = new OrdDisDeliveryDetail();
            BeanUtils.copy(firstOrderDetailOut, deliveryOrderDetail);
            OrderGoodsOut orderGoods = getOrderGoodsOut(bizOrgCode, firstOrderDetailOut);
            BigDecimal warehousePrice = warehouseServer.getWarehousePrice(deliveryOrder.getWrhCode(), deliveryOrder.getStockCode(),
                    deliveryOrderDetail.getGoodsCode(), stockInfoOut.getBizOrgCode(), orderGoods.getVendorCode());
            deliveryOrderDetail.setWrhPrice(Objects.isNull(warehousePrice) ? BigDecimal.ZERO : warehousePrice);
            BigDecimal stockPrice = warehouseServer.getStockPrice(deliveryOrder.getStoreCode(), deliveryOrderDetail.getGoodsCode(), bizOrgCode);
            deliveryOrderDetail.setStoreStockPrice(Objects.isNull(stockPrice) ? BigDecimal.ZERO : stockPrice);
            deliveryOrderDetail.setSellTax(orderGoods.getOutTax());
            deliveryOrderDetail.setDeliveryOrderId(deliveryOrder.getId());
//            deliveryOrderDetail.setDeliveryQuantity(BigDecimal.valueOf(firstOrderDetailOut.getNum()));
//            deliveryOrderDetail.setDistributionPackageQuantity(firstOrderDetailOut.getDistributionPackageNum());
            deliveryOrderDetail.setDistributionUnitPrice(firstOrderDetailOut.getDistributionPrice());
//            deliveryOrderDetail.setDistributionAmount(firstOrderDetailOut.getAmount());
            deliveryOrderDetail.setDistributionSpecification(firstOrderDetailOut.getQpcStr());
            deliveryOrderDetail.setDistributionSpecificationNum(firstOrderDetailOut.getDistributionSpecificationNum());
            deliveryOrderDetail.setDistributionSpecificationUnit(firstOrderDetailOut.getDistributionSpecificationUnit());
            deliveryOrderDetail.setOrderQuantity(BigDecimal.valueOf(firstOrderDetailOut.getNum()));
            deliveryOrderDetail.setOrderPackageQuantity(firstOrderDetailOut.getDistributionPackageNum());
            deliveryOrderDetail.setOrderAmount(firstOrderDetailOut.getAmount());
            deliveryOrderDetail.setOrderUnitPrice(firstOrderDetailOut.getDistributionPrice());
//            deliveryOrderDetail.setDeliveryQuantity(BigDecimal.ZERO);
//            deliveryOrderDetail.setDeliveryPackageQuantity(BigDecimal.ZERO);
//            deliveryOrderDetail.setDeliveryAmount(BigDecimal.ZERO);
//            deliveryOrderDetail.setDistributionQuantity(BigDecimal.valueOf(firstOrderDetailOut.getNum()));
//            deliveryOrderDetail.setArrivalAmount(BigDecimal.ZERO);
//            deliveryOrderDetail.setArrivalPackageQuantity(BigDecimal.ZERO);
//            deliveryOrderDetail.setArrivalQuantity(BigDecimal.ZERO);
            deliveryOrderDetail.setSmallSort(firstOrderDetailOut.getSort());
            BigDecimal sellTax = Objects.isNull(deliveryOrderDetail.getSellTax()) ? BigDecimal.ZERO : deliveryOrderDetail.getSellTax().divide(new BigDecimal(SystemConstant.PERCENTAGE));
            BigDecimal tax = sellTax.add(BigDecimal.ONE);
            //配销去金额
            deliveryOrderDetail.setDistributionExceptTaxAmount(deliveryOrderDetail.getOrderAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            deliveryOrderDetail.setDistributionTaxAmount(deliveryOrderDetail.getOrderAmount().subtract(deliveryOrderDetail.getDistributionExceptTaxAmount()));
            deliveryOrderDetail.setWrhCostAmount(deliveryOrderDetail.getWrhPrice().multiply(deliveryOrderDetail.getOrderQuantity()));
            deliveryOrderDetail.setWrhExceptTaxAmount(deliveryOrderDetail.getWrhCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            deliveryOrderDetail.setWrhTaxAmount(deliveryOrderDetail.getWrhCostAmount().subtract(deliveryOrderDetail.getWrhExceptTaxAmount()));
            deliveryOrderDetail.setStoreCostAmount(deliveryOrderDetail.getStoreStockPrice().multiply(deliveryOrderDetail.getOrderQuantity()));
            deliveryOrderDetail.setStoreExceptTaxAmount(deliveryOrderDetail.getStoreCostAmount().divide(tax, NumberUtil.INTEGER_FOUR, RoundingMode.HALF_UP));
            deliveryOrderDetail.setStoreTaxAmount(deliveryOrderDetail.getStoreCostAmount().subtract(deliveryOrderDetail.getStoreExceptTaxAmount()));
            deliveryOrderDetail.setGoodsType(orderGoods.getGoodsType());
//            if (Objects.nonNull(orderGoods.getDistributionSpecification())) {
//                StandardSpecTransInfoOut distributionSpecification = orderGoods.getDistributionSpecification();
//                deliveryOrderDetail.setDistributionSpecification(distributionSpecification.getQpcStr());
//            }
            // 铺货单不参加活动
            deliveryOrderDetail.setIsGift(NumberUtil.INTEGER_ZERO);
            deliveryOrderDetail.setCreator(SystemConstant.SYSTEM_USER);
            deliveryOrderDetail.setUpdater(SystemConstant.SYSTEM_USER);
            deliveryOrderDetail.setIsDelete(NumberUtil.INTEGER_ZERO);
            deliveryOrderDetail.setInvoiceType(orderGoods.getInvoiceType());
            deliveryOrderDetailsList.add(deliveryOrderDetail);
            deliveryOrder.setDetailList(deliveryOrderDetailsList);
        });
        deliveryOrderList.add(deliveryOrder);
        return deliveryOrderDetailsList;
    }

    /**
     * 获取商品信息
     *
     * @param bizOrgCode
     * @param firstOrderDetailOut
     * @return
     */
    private OrderGoodsOut getOrderGoodsOut(String bizOrgCode, OrdDisOrderFirstDetailOut firstOrderDetailOut) {
        OrderGoodsIn orderGoodsInTar = new OrderGoodsIn();
        orderGoodsInTar.setGoodsCode(firstOrderDetailOut.getGoodsCode());
        orderGoodsInTar.setBizOrgCode(bizOrgCode);
        OrderGoodsOut orderGoods = orderGoodsServer.getOrderGoods(orderGoodsInTar);
        if (Objects.isNull(orderGoods)) {
            throw new BusinessException(firstOrderDetailOut.getGoodsCode() + "商品信息不存在");
        }
        return orderGoods;
    }

    /**
     * 根据运营品类拆分
     *
     * @param firstOrderConfigOut
     * @param orderDetailOut
     * @param keys
     */
    private void splitSort(FirstOrderConfigOut firstOrderConfigOut, OrdDisOrderFirstDetailOut orderDetailOut, StringJoiner keys) {
        boolean isOther = true;
        // 配置分类的集合
        for (FirstOrderSortOut firstOrderSortOut : firstOrderConfigOut.getFirstOrderSortOuts()) {
            String[] split = firstOrderSortOut.getSortCode().split(",");
            // 多个分类情况
            if (split.length > 1) {
                for (int i = 0; i < split.length; i++) {
                    if (orderDetailOut.getSort().startsWith(split[i])) {
                        keys.add("group").add(String.valueOf(firstOrderSortOut.getId()));
                        isOther = false;
                        break;
                    }
                }
            } else if (orderDetailOut.getSort().startsWith(firstOrderSortOut.getSortCode())) {
                keys.add(firstOrderSortOut.getSortCode());
                isOther = false;
            }
        }
        // 其他分类
        if (isOther) {
            keys.add("other");
        }
    }

    /**
     * 校验订单商品信息
     *
     * @param goodsCode
     * @param storeCode
     * @param bizOrgCode
     * @return
     */
    @Override
    public Response<OrderGoodsOut> checkOrderGoods(String goodsCode, String storeCode, String bizOrgCode) {
        OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
        orderGoodsIn.setBizOrgCode(bizOrgCode);
        orderGoodsIn.setGoodsCode(goodsCode);
        orderGoodsIn.setStoreCode(storeCode);
        orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.FIRST_ORDER.getType());
        OrderGoodsOut storeOrderGoods = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
        if (Objects.nonNull(storeOrderGoods)) {
            if (Objects.isNull(storeOrderGoods.getDistributionPrice()) || BigDecimal.ZERO.compareTo(storeOrderGoods.getDistributionPrice()) == NumberUtil.INTEGER_ZERO) {
                return Response.error("此商品无配销价");
            }
            return Response.data(storeOrderGoods);
        }
        return Response.error("此商品不可铺货");
    }


}
