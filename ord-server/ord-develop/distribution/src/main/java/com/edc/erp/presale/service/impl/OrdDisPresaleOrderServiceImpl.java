package com.edc.erp.presale.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.model.in.goods.OrderGoodsIn;
import com.edc.erp.common.model.in.store.StoreInfoIn;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.model.out.goods.OrderGoodsOut;
import com.edc.erp.common.model.out.store.StoreInfoOut;
import com.edc.erp.common.model.out.store.StoreOut;
import com.edc.erp.common.service.FundServer;
import com.edc.erp.common.service.OrderGoodsServer;
import com.edc.erp.common.service.StoreCenterService;
import com.edc.erp.common.service.impl.FileService;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.FileExportUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import com.edc.erp.enumeration.OrderIdentificationEnum;
import com.edc.erp.enumeration.OrderLogEnum;
import com.edc.erp.enumeration.OrderStatusEnum;
import com.edc.erp.model.excel.ExcelPresaleOrderOut;
import com.edc.erp.model.excel.ExcelStorePresaleOrderDetailInfoOut;
import com.edc.erp.model.in.QueryStorePresaleOrderForAppIn;
import com.edc.erp.model.in.QueryStorePresaleOrderIn;
import com.edc.erp.model.out.DisStorePresaleOrderGetForAppOut;
import com.edc.erp.model.out.DisStorePresaleOrderInfoOut;
import com.edc.erp.model.out.DisStorePresaleOrderPageForAppOut;
import com.edc.erp.model.out.StorePresaleOrderDetailInfoOut;
import com.edc.erp.presale.entity.*;
import com.edc.erp.presale.enumeration.OrdDisPresaleActivityStatusEnum;
import com.edc.erp.presale.enumeration.OrdDisPresaleFlowBusinessTypeEnum;
import com.edc.erp.presale.enumeration.OrdDisPresaleOrderStatusEnum;
import com.edc.erp.presale.mapper.OrdDisPresaleOrderDetailMapper;
import com.edc.erp.presale.mapper.OrdDisPresaleOrderMapper;
import com.edc.erp.presale.model.in.*;
import com.edc.erp.presale.model.out.CreatePresaleOrderDataOut;
import com.edc.erp.presale.service.*;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.export.AsyncExportExecutor;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import com.edc.plugins.utils.bean.BeanUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @ClassName OrdDisPresaleOrderServiceImpl
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/8/21 18:40
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdDisPresaleOrderServiceImpl implements OrdDisPresaleOrderService {
    private final OrdDisPresaleOrderMapper ordDisPresaleOrderMapper;
    private final OrdDisPresaleActivityService ordDisPresaleActivityService;
    private final OrderGoodsServer orderGoodsServer;
    private final OrdDisPresaleActivityGoodsService ordDisPresaleActivityGoodsService;
    private final UniqueUtils uniqueUtils;
    private final OrdDisPresaleOrderDetailMapper ordDisPresaleOrderDetailMapper;
    private final AsyncLogService asyncLogService;
    private final FundServer fundServer;
    private final StoreCenterService storeCenterService;
    private final AsyncExportExecutor asyncExportExecutor;
    private final FileService fileService;
    private final OrdDisPresaleAssetsService ordDisPresaleAssetsService;
    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;
    @Qualifier("updatePresaleOrderPaidSender")
    private final MessageSender updatePresaleOrderPaidSender;

    @Override
    public CreatePresaleOrderDataOut handleDisPresaleOrderData(SubmitPresaleOrderIn submitPresaleOrderIn) {
        String storeCode = submitPresaleOrderIn.getStoreCode();
        StoreOut storeOut = storeCenterService.getStoreInfoByErpStoreCode(storeCode);
        String storeName = storeOut.getStoreName();
        String bizOrgCode = submitPresaleOrderIn.getBizOrgCode();
        submitPresaleOrderIn.setOrgCode(storeOut.getOrgCode());
        List<OrdDisPresaleOrderDetail> orderDetailList = Lists.newArrayList();
        Set<String> skuSet = new HashSet<>();
        AtomicReference<BigDecimal> goodsSumQtyAtomic = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalOrderAmountAtomic = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalPayAmountAtomic = new AtomicReference<>(BigDecimal.ZERO);
        submitPresaleOrderIn.getPresaleOrderActivityInList().forEach(presaleOrderActivity -> {
            String presaleActivityNo = presaleOrderActivity.getPresaleActivityNo();
            // 预售活动
            OrdDisPresaleActivity ordDisPresaleActivity = ordDisPresaleActivityService.getPresaleActivityIsExecute(presaleOrderActivity.getPresaleActivityId());
            if (Objects.isNull(ordDisPresaleActivity)) {
                throw new BusinessException("活动" + presaleActivityNo + "已过期");
            }
            // 是否限购
            Integer isLimitBuy = ordDisPresaleActivity.getIsLimitBuy();
            // 限购次数
            Integer limitBuyTime = ordDisPresaleActivity.getLimitBuyTime();
            // 购买数量
            BigDecimal presaleActivityQty = presaleOrderActivity.getPresaleActivityQty();
            List<SubmitPresaleOrderGoodsIn> presaleOrderDetailInList = presaleOrderActivity.getPresaleOrderDetailInList();
            // 封装预售订单明细
            presaleOrderDetailInList.stream().forEach(presaleOrderGoods -> {
                // 预售活动该商品信息
                OrdDisPresaleActivityGoods ordDisPresaleActivityGoods = ordDisPresaleActivityGoodsService.getOneByIdAndActivityId(presaleOrderGoods.getId(), ordDisPresaleActivity.getId());
                // 查询该活动下该商品已下单包装数量（主商品）
                BigDecimal sumExistPackageQuantity = ordDisPresaleOrderDetailMapper.sumBasePackageQuantityByActivityIdAndGoodsCode(presaleOrderActivity.getPresaleActivityId(),
                        presaleOrderGoods.getGoodsCode(), storeCode);
                sumExistPackageQuantity = Objects.isNull(sumExistPackageQuantity) ? BigDecimal.ZERO : sumExistPackageQuantity;
                // 本次购买包装数
                BigDecimal packageQuantityNow = ordDisPresaleActivityGoods.getPackageQuantity().multiply(presaleActivityQty);
                if (isLimitBuy.equals(NumberUtil.INTEGER_ONE) && NumberUtil.INTEGER_ZERO.equals(ordDisPresaleActivityGoods.getIsGift())) {
                    // 计算该商品本次最大购买量
                    BigDecimal maxPackageQuantity = new BigDecimal(limitBuyTime).multiply(ordDisPresaleActivityGoods.getPackageQuantity());
                    if (sumExistPackageQuantity.add(packageQuantityNow).compareTo(maxPackageQuantity) == NumberUtil.INTEGER_ONE) {
                        throw new BusinessException("活动" + presaleActivityNo + "商品" + presaleOrderGoods.getGoodsCode() + "限购包装数不得超过" + maxPackageQuantity.intValue() + "已购" + sumExistPackageQuantity);
                    }
                }
                skuSet.add(presaleOrderGoods.getGoodsCode());
                OrdDisPresaleOrderDetail ordDisPresaleOrderDetail = new OrdDisPresaleOrderDetail();

                BeanUtils.copy(ordDisPresaleActivityGoods, ordDisPresaleOrderDetail);
                OrderGoodsIn orderGoodsIn = new OrderGoodsIn();
                orderGoodsIn.setStoreCode(storeCode);
                orderGoodsIn.setGoodsCode(presaleOrderGoods.getGoodsCode());
                orderGoodsIn.setBizOrgCode(bizOrgCode);
                orderGoodsIn.setBusinessType(BusinessTypeColumnEnum.getTypeBySourceCode(SourceTypeEnum.INITIATIVE.getKey()));
                OrderGoodsOut orderGoodsOut = orderGoodsServer.getStoreOrderGoods(orderGoodsIn);
                if (Objects.isNull(orderGoodsOut)) {
                    throw new BusinessException("商品" + presaleOrderGoods.getGoodsCode() + "不存在");
                }
                ordDisPresaleOrderDetail.setDistributionPrice(orderGoodsOut.getDistributionPrice());
                ordDisPresaleOrderDetail.setPackageQuantity(packageQuantityNow);
                // 商品总数量
                BigDecimal goodsTotalQty = ordDisPresaleOrderDetail.getPackageQuantity().multiply(ordDisPresaleOrderDetail.getPackageSpecificationNum());
                ordDisPresaleOrderDetail.setGoodsQuantity(goodsTotalQty);
                BigDecimal distributionAmount = ordDisPresaleOrderDetail.getPackageQuantity().multiply(orderGoodsOut.getDistributionPrice()).setScale(NumberUtil.INTEGER_TWO, RoundingMode.HALF_UP);
                ordDisPresaleOrderDetail.setDistributionAmount(distributionAmount);
                ordDisPresaleOrderDetail.setPresaleActivityId(ordDisPresaleActivity.getId());
                ordDisPresaleOrderDetail.setPayAmount(ordDisPresaleOrderDetail.getIsGift().compareTo(NumberUtil.INTEGER_ZERO) == 0 ? distributionAmount : BigDecimal.ZERO);
                ordDisPresaleOrderDetail.setPresaleActivityNo(ordDisPresaleActivity.getPresaleActivityNo());
                orderDetailList.add(ordDisPresaleOrderDetail);
                goodsSumQtyAtomic.getAndSet(goodsSumQtyAtomic.get().add(goodsTotalQty));
                totalOrderAmountAtomic.getAndSet(totalOrderAmountAtomic.get().add(distributionAmount));
            });
            totalPayAmountAtomic.getAndSet(totalPayAmountAtomic.get().add(ordDisPresaleActivity.getPromotionalPrice().multiply(presaleActivityQty)));
        });
        String loginUsername = submitPresaleOrderIn.getLoginUsername();
        OrdDisPresaleOrder ordDisPresaleOrder = new OrdDisPresaleOrder();
        String presaleOrderNo = CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.KYD.getCode(), bizOrgCode, uniqueUtils, 4);
        ordDisPresaleOrder.setPresaleOrderNo(presaleOrderNo);
        ordDisPresaleOrder.setStatus(OrdDisPresaleOrderStatusEnum.WAIT_PAYMENT.getKey());
        ordDisPresaleOrder.setStoreCode(storeCode);
        ordDisPresaleOrder.setStoreName(storeName);
        ordDisPresaleOrder.setTotalSkuQty(skuSet.size());
        ordDisPresaleOrder.setTotalGoodsQty(goodsSumQtyAtomic.get());
        ordDisPresaleOrder.setTotalOrderAmount(totalOrderAmountAtomic.get());
        ordDisPresaleOrder.setTotalPayAmount(totalPayAmountAtomic.get());
        ordDisPresaleOrder.setTotalDiscountAmount(ordDisPresaleOrder.getTotalOrderAmount().subtract(ordDisPresaleOrder.getTotalPayAmount()));
        ordDisPresaleOrder.setCreator(loginUsername);
        ordDisPresaleOrder.setCreateTime(LocalDateTime.now());
        ordDisPresaleOrder.setUpdater(loginUsername);
        ordDisPresaleOrder.setUpdateTime(LocalDateTime.now());
        ordDisPresaleOrder.setBizOrgCode(bizOrgCode);
        ordDisPresaleOrder.setOrgCode(submitPresaleOrderIn.getOrgCode());
        ordDisPresaleOrder.setIsDelete(ModelConst.DELETE.NO);
        CreatePresaleOrderDataOut createPresaleOrderDataOut = new CreatePresaleOrderDataOut();
        createPresaleOrderDataOut.setOrdDisPresaleOrder(ordDisPresaleOrder);
        createPresaleOrderDataOut.setOrderDetailList(orderDetailList);
        return createPresaleOrderDataOut;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> saveDisPresaleOrder(CreatePresaleOrderDataOut createPresaleOrderDataOut) {
        OrdDisPresaleOrder ordDisPresaleOrder = createPresaleOrderDataOut.getOrdDisPresaleOrder();
        ordDisPresaleOrderMapper.insert(ordDisPresaleOrder);
        createPresaleOrderDataOut.getOrderDetailList().forEach(ordDisPresaleOrderDetail -> ordDisPresaleOrderDetail.setPresaleOrderId(ordDisPresaleOrder.getId()));
        ordDisPresaleOrderDetailMapper.batchSaveDisPresaleOrderDetailList(createPresaleOrderDataOut.getOrderDetailList());
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER_SAVE.getName(),
                String.valueOf(ordDisPresaleOrder.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER.getCode(),
                OperateLogTypeEnum.SAVE.getName(),
                new Date(), ordDisPresaleOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.data(ordDisPresaleOrder.getId());
    }

    @Override
    public Response<String> payPresaleOrder(Long id, AppUserOut appUserOut, String loginUsername) {
        OrdDisPresaleOrder ordDisPresaleOrder = ordDisPresaleOrderMapper.selectByPrimaryKey(id);
        if (Objects.isNull(ordDisPresaleOrder)) {
            return Response.error("不存在的预售订单");
        }
        if (!OrdDisPresaleOrderStatusEnum.WAIT_PAYMENT.getKey().equals(ordDisPresaleOrder.getStatus())
                && !OrdDisPresaleOrderStatusEnum.PAYING.getKey().equals(ordDisPresaleOrder.getStatus())) {
            return Response.error("预售订单状态不正确");
        }
        if (BigDecimal.ZERO.compareTo(ordDisPresaleOrder.getTotalPayAmount()) == 0) {
            return Response.error("预售订单支付金额为0");
        }
        List<OrdDisPresaleOrderDetail> presaleOrderDetailList = ordDisPresaleOrderDetailMapper.findListByOrderId(id);
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.SEMICOLON);
        Set<String> errorSet = new HashSet<>();
        presaleOrderDetailList.forEach(detail -> {
            OrdDisPresaleActivity ordDisPresaleActivity = ordDisPresaleActivityService.getOneById(detail.getPresaleActivityId());
            if (!OrdDisPresaleActivityStatusEnum.EXECUTED.getKey().equals(ordDisPresaleActivity.getStatus())) {
                if (errorSet.contains(ordDisPresaleActivity.getPresaleActivityNo())) {
                    return;
                }
                errorJoiner.add(ordDisPresaleActivity.getPresaleActivityNo());
                errorSet.add(ordDisPresaleActivity.getPresaleActivityNo());
            }
        });
        if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
            throw new BusinessException("预售订单支付时检验以下预售活动状态不正确：" + errorJoiner);
        }
        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        rechargeLiquidationIn.setPayOrPrincipalCode(appUserOut.getStoreCode());
        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
        rechargeLiquidationIn.setRecipientPrincipalCode(appUserOut.getBizOrgCode());
        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
        rechargeLiquidationIn.setBizOrgCode(appUserOut.getBizOrgCode());
        rechargeLiquidationIn.setBusinessNo(ordDisPresaleOrder.getPresaleOrderNo());
        rechargeLiquidationIn.setBusinessType(FundTypeEnum.PAY_PRESALE_ORDER.getCode());
        rechargeLiquidationIn.setLiquidationAmount(ordDisPresaleOrder.getTotalPayAmount());
        rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        try {
            Response response = fundServer.settlement(rechargeLiquidationIn);
            if (!response.isSuccess()) {
                log.error("预售订单{}支付失败:{}", ordDisPresaleOrder.getPresaleOrderNo(), response.getMessage());
                return Response.error("预售订单支付失败");
            } else {
                String beforeStatusCode = ordDisPresaleOrder.getStatus();
                ordDisPresaleOrder.setStatus(OrdDisPresaleOrderStatusEnum.PAYING.getKey());
                ordDisPresaleOrder.setUpdater(appUserOut.getName());
                ordDisPresaleOrder.setUpdateTime(LocalDateTime.now());
                ordDisPresaleOrderMapper.updateByPrimaryKey(ordDisPresaleOrder);

                UpdatePresaleOrderPaidForMqIn updatePresaleOrderPaidForMqIn = new UpdatePresaleOrderPaidForMqIn();
                updatePresaleOrderPaidForMqIn.setPresaleOrderId(id);
                updatePresaleOrderPaidForMqIn.setLoginUsername(loginUsername);
                SendResponse sendResponse = updatePresaleOrderPaidSender.sendSync(JSONObject.toJSONString(updatePresaleOrderPaidForMqIn).getBytes());
                if (Objects.nonNull(sendResponse)) {
                    log.info("预售订单支付成功更新状态发送消息ID----->{}", sendResponse.getMessageId());
                }
                String content = MessageFormat.format(OrderLogEnum.DIS_PRESALE_ORDER.getKey(),
                        OrdDisPresaleOrderStatusEnum.getValueByKey(beforeStatusCode), OrdDisPresaleOrderStatusEnum.getValueByKey(OrdDisPresaleOrderStatusEnum.PAYING.getKey()));
                BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER_PAY.getName(),
                        String.valueOf(ordDisPresaleOrder.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER.getCode(),
                        content, new Date(), ordDisPresaleOrder.getUpdater());
                asyncLogService.sendAsyncSaveLogByMq(businessLog);
//                this.updatePresaleOrderAfterPaidSuccess(id, appUserOut, loginUsername, ordDisPresaleOrder);
                return Response.success();
            }
        } catch (Exception e) {
            log.error("预售订单{}支付异常{}", ordDisPresaleOrder.getPresaleOrderNo(), e);
        }
        return Response.error("支付失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePresaleOrderAfterPaidSuccess(Long id, String loginUsername) {
        OrdDisPresaleOrder ordDisPresaleOrder = ordDisPresaleOrderMapper.selectByPrimaryKey(id);
        if (Objects.isNull(ordDisPresaleOrder)) {
            log.info("预售活动ID{}不存在", id);
            return;
        }
        if (!(OrdDisPresaleOrderStatusEnum.PAYING.getKey().equals(ordDisPresaleOrder.getStatus())
                || OrdDisPresaleOrderStatusEnum.WAIT_PAYMENT.getKey().equals(ordDisPresaleOrder.getStatus()))) {
            log.info("预售订单{}状态不正确", ordDisPresaleOrder.getPresaleOrderNo());
            return;
        }
        UpdateDisPresaleAssetsIn updateDisPresaleAssetsIn = new UpdateDisPresaleAssetsIn();
        List<OrdDisPresaleOrderDetail> presaleOrderDetailList = ordDisPresaleOrderDetailMapper.findListByOrderId(id);
        OrdDisPresaleAssets ordDisPresaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(ordDisPresaleOrder.getStoreCode());
        if (Objects.nonNull(ordDisPresaleAssets)) {
            updateDisPresaleAssetsIn.setAssetsId(ordDisPresaleAssets.getId());
        } else {
//            BigDecimal totalSurplusGoodsQty = presaleOrderDetailList.stream().map(OrdDisPresaleOrderDetail::getGoodsQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            ordDisPresaleAssets = new OrdDisPresaleAssets();
            ordDisPresaleAssets.setStoreCode(ordDisPresaleOrder.getStoreCode());
            ordDisPresaleAssets.setStoreName(ordDisPresaleOrder.getStoreName());
            // ordDisPresaleAssets.setTotalSkuQty(presaleOrderDetailList.size());
            // ordDisPresaleAssets.setTotalSurplusGoodsQty(totalSurplusGoodsQty);
            ordDisPresaleAssets.setOrgCode(ordDisPresaleOrder.getOrgCode());
            ordDisPresaleAssets.setBizOrgCode(ordDisPresaleOrder.getBizOrgCode());
            ordDisPresaleAssets.setCreator(loginUsername);
            ordDisPresaleAssets.setUpdater(loginUsername);
            ordDisPresaleAssets.setIsDelete(ModelConst.DELETE.NO);
            ordDisPresaleAssetsService.saveOrdDisPresaleAssets(ordDisPresaleAssets);
            updateDisPresaleAssetsIn.setAssetsId(ordDisPresaleAssets.getId());
        }
        updateDisPresaleAssetsIn.setBizOrgCode(ordDisPresaleOrder.getBizOrgCode());
        updateDisPresaleAssetsIn.setLoginUsername(loginUsername);
        updateDisPresaleAssetsIn.setBusinessType(OrdDisPresaleFlowBusinessTypeEnum.PRESALE_ORDER.getKey());
        updateDisPresaleAssetsIn.setSourceNo(ordDisPresaleOrder.getPresaleOrderNo());

        OrdDisPresaleAssets finalOrdDisPresaleAssets = ordDisPresaleAssets;
        Map<String, List<OrdDisPresaleOrderDetail>> goodsPresaleDetailsMap = presaleOrderDetailList.stream().collect(Collectors.groupingBy(OrdDisPresaleOrderDetail::getGoodsCode));
        List<UpdateDisPresaleAssetsGoodsIn> assetsGoodsInList = goodsPresaleDetailsMap.entrySet().stream().map(entry -> {
            OrdDisPresaleOrderDetail detail = entry.getValue().get(NumberUtil.INTEGER_ZERO);
            UpdateDisPresaleAssetsGoodsIn updateDisPresaleAssetsGoodsIn = new UpdateDisPresaleAssetsGoodsIn();
            BeanUtils.copy(detail, updateDisPresaleAssetsGoodsIn);
            updateDisPresaleAssetsGoodsIn.setStoreCode(ordDisPresaleOrder.getStoreCode());
            updateDisPresaleAssetsGoodsIn.setStoreName(ordDisPresaleOrder.getStoreName());
            updateDisPresaleAssetsGoodsIn.setActualLowering(AdjustTypeEnum.ADD.getCode());
            updateDisPresaleAssetsGoodsIn.setOrderQuantity(BigDecimal.ZERO);
            BigDecimal updateSurplusQuantity = entry.getValue().stream().map(OrdDisPresaleOrderDetail::getGoodsQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            updateDisPresaleAssetsGoodsIn.setUpdateSurplusQuantity(updateSurplusQuantity);
            updateDisPresaleAssetsGoodsIn.setLoginUsername(loginUsername);
            updateDisPresaleAssetsGoodsIn.setAssetsId(finalOrdDisPresaleAssets.getId());
            return updateDisPresaleAssetsGoodsIn;
        }).collect(Collectors.toList());
        updateDisPresaleAssetsIn.setAssetsGoodsInList(assetsGoodsInList);
        ordDisPresaleAssetsService.updatePresaleAssets(updateDisPresaleAssetsIn);

        String beforeStatusCode = ordDisPresaleOrder.getStatus();
        ordDisPresaleOrder.setStatus(OrdDisPresaleOrderStatusEnum.PAID.getKey());
        ordDisPresaleOrder.setPayTime(LocalDateTime.now());
        ordDisPresaleOrder.setUpdater(loginUsername);
        ordDisPresaleOrder.setUpdateTime(LocalDateTime.now());
        ordDisPresaleOrderMapper.updateByPrimaryKey(ordDisPresaleOrder);
        String content = MessageFormat.format(OrderLogEnum.DIS_PRESALE_ORDER.getKey(),
                OrdDisPresaleOrderStatusEnum.getValueByKey(beforeStatusCode), OrdDisPresaleOrderStatusEnum.getValueByKey(OrdDisPresaleOrderStatusEnum.PAID.getKey()));
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER_PAY.getName(),
                String.valueOf(ordDisPresaleOrder.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER.getCode(),
                content, new Date(), ordDisPresaleOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
    }

    @Override
    public Page<DisStorePresaleOrderPageForAppOut> findStorePresaleOrderForApp(QueryStorePresaleOrderForAppIn queryStorePresaleOrderForAppIn) {
        List<DisStorePresaleOrderPageForAppOut> resultList = ordDisPresaleOrderMapper.findStorePresaleOrderForAppByPage(queryStorePresaleOrderForAppIn);
        resultList.forEach(item -> item.setStatusStr(OrdDisPresaleOrderStatusEnum.getValueByKey(item.getStatus())));
        Page<DisStorePresaleOrderPageForAppOut> page = new Page<>(queryStorePresaleOrderForAppIn);
        page.setList(resultList);
        return page;
    }

    @Override
    public DisStorePresaleOrderGetForAppOut getStorePresaleOrderInfoForApp(Long id) {
        OrdDisPresaleOrder ordDisPresaleOrder = ordDisPresaleOrderMapper.selectByPrimaryKey(id);
        if (Objects.isNull(ordDisPresaleOrder)) {
            throw new BusinessException("不存在的预售订单");
        }
        List<OrdDisPresaleOrderDetail> detailList = ordDisPresaleOrderDetailMapper.findListByOrderId(id);
        // 过滤出赠品数
        Integer giftQty = detailList.stream().filter(item -> item.getIsGift() == 1).map(OrdDisPresaleOrderDetail::getGoodsQuantity).mapToInt(BigDecimal::intValue).sum();
        Map<String, List<OrdDisPresaleOrderDetail>> detailListMap = detailList.stream().collect(Collectors.groupingBy(OrdDisPresaleOrderDetail::getPresaleActivityNo));
        List<DisStorePresaleOrderGetForAppOut.StorePresaleActivity> presaleActivityList = new ArrayList<>();
        for (String key : detailListMap.keySet()) {
            List<StorePresaleOrderDetailInfoOut> presaleOrderDetailList = detailListMap.get(key).stream().map(ordDisPresaleOrderDetail -> {
                StorePresaleOrderDetailInfoOut storePresaleOrderDetailInfoOut = new StorePresaleOrderDetailInfoOut();
                BeanUtils.copy(ordDisPresaleOrderDetail, storePresaleOrderDetailInfoOut);
                return storePresaleOrderDetailInfoOut;
            }).collect(Collectors.toList());
            DisStorePresaleOrderGetForAppOut.StorePresaleActivity activity = DisStorePresaleOrderGetForAppOut.StorePresaleActivity.builder()
                    .presaleActivityNo(key)
                    .presaleOrderDetailList(presaleOrderDetailList)
                    .build();
            presaleActivityList.add(activity);
        }
        DisStorePresaleOrderGetForAppOut disStorePresaleOrderInfoForAppOut = new DisStorePresaleOrderGetForAppOut();
        BeanUtils.copy(ordDisPresaleOrder, disStorePresaleOrderInfoForAppOut);
        disStorePresaleOrderInfoForAppOut.setStatusStr(OrdDisPresaleOrderStatusEnum.getValueByKey(ordDisPresaleOrder.getStatus()));
        disStorePresaleOrderInfoForAppOut.setPresaleActivityList(presaleActivityList);
        disStorePresaleOrderInfoForAppOut.setGiftQty(giftQty);
        disStorePresaleOrderInfoForAppOut.setGoodsQty(ordDisPresaleOrder.getTotalGoodsQty().intValue() - giftQty);
        return disStorePresaleOrderInfoForAppOut;
    }

    @Override
    public Page<DisStorePresaleOrderInfoOut> findStorePresaleOrderByPage(QueryStorePresaleOrderIn queryStorePresaleOrderIn) {
        if (StringUtils.isNotEmpty(queryStorePresaleOrderIn.getStoreArea())) {
            List<String> storeCodeList = new ArrayList<>();
            List<StoreInfoOut> storeList = storeCenterService.getStoreInfoByCode(new StoreInfoIn(queryStorePresaleOrderIn.getStoreArea()));
            if (CollectionUtils.isEmpty(storeList)) {
                return new Page<>(queryStorePresaleOrderIn);
            }
            storeList.forEach(storeInfoOut -> storeCodeList.add(storeInfoOut.getErpStoreCode()));
            queryStorePresaleOrderIn.setStoreCodeList(storeCodeList);
        }
        List<DisStorePresaleOrderInfoOut> resultList = ordDisPresaleOrderMapper.findStorePresaleOrderByPage(queryStorePresaleOrderIn);
        for (DisStorePresaleOrderInfoOut item : resultList) {
            item.setStatusStr(OrdDisPresaleOrderStatusEnum.getValueByKey(item.getStatus()));
            // StoreInfo storeInfo = storeCenterService.getStoreByCode(item.getStoreCode(), item.getBizOrgCode());
            StoreOut storeInfo = storeCenterService.getStoreInfoByErpStoreCode(item.getStoreCode());
            if (Objects.nonNull(storeInfo)) {
                item.setBelongArea(storeInfo.getArea());
            }
        }
        Page<DisStorePresaleOrderInfoOut> page = new Page<>(queryStorePresaleOrderIn);
        page.setList(resultList);
        return page;
    }

    @Override
    public DisStorePresaleOrderInfoOut getStorePresaleOrderInfo(Long id) {
        OrdDisPresaleOrder ordDisPresaleOrder = ordDisPresaleOrderMapper.selectByPrimaryKey(id);
        if (Objects.isNull(ordDisPresaleOrder)) {
            throw new BusinessException("不存在的预售订单");
        }
        List<OrdDisPresaleOrderDetail> detailList = ordDisPresaleOrderDetailMapper.findListByOrderId(id);
        List<StorePresaleOrderDetailInfoOut> presaleOrderDetailList = detailList.stream().map(ordDisPresaleOrderDetail -> {
            StorePresaleOrderDetailInfoOut storePresaleOrderDetailInfoOut = new StorePresaleOrderDetailInfoOut();
            BeanUtils.copy(ordDisPresaleOrderDetail, storePresaleOrderDetailInfoOut);
            storePresaleOrderDetailInfoOut.setGiftStr(storePresaleOrderDetailInfoOut.getIsGift().equals(NumberUtil.INTEGER_ZERO) ? "主商品" : "赠品");
            return storePresaleOrderDetailInfoOut;
        }).collect(Collectors.toList());
        DisStorePresaleOrderInfoOut storePresaleOrderInfoOut = new DisStorePresaleOrderInfoOut();
        BeanUtils.copy(ordDisPresaleOrder, storePresaleOrderInfoOut);
        storePresaleOrderInfoOut.setStatusStr(OrdDisPresaleOrderStatusEnum.getValueByKey(ordDisPresaleOrder.getStatus()));
        storePresaleOrderInfoOut.setDetailInfoOutList(presaleOrderDetailList);
        return storePresaleOrderInfoOut;
    }

    @Override
    public String exportPresaleOrderList(QueryStorePresaleOrderIn queryStorePresaleOrderIn) {
        String title = "导出预售订单列表";
        // 文件名：fileName+yyyyMMddHHmmss
        String fileName = title.concat(DateUtils.format(new Date(), "yyyyMMddHHmmss")).concat(".xlsx");
        // asyncExportExecutor.export(
        //         UserUtil.getUserName(),
        //         fileName,
        //         () -> ExportExcelByPage.exportExcelBytesByPage(
        //                 // sheet页名称
        //                 "预售订单列表",
        //                 // 导出模板实体
        //                 ExcelPresaleOrderOut.class,
        //                 // 分页查询对象
        //                 queryStorePresaleOrderIn,
        //                 // 分页查询方法
        //                 page -> {
        //                     Page<DisStorePresaleOrderInfoOut> resultPage = this.findStorePresaleOrderByPage(queryStorePresaleOrderIn);
        //                     List<ExcelPresaleOrderOut> excelDeliveryOrderDetails = parsePresaleOrderListDataToExcel(resultPage.getList());
        //                     log.info("导出预售订单列表集合大小是--{}", excelDeliveryOrderDetails.size());
        //                     return excelDeliveryOrderDetails;
        //                 })
        // );
        // return AsyncExportExecutor.DOWNLOADING;

        Page<DisStorePresaleOrderInfoOut> resultPage = this.findStorePresaleOrderByPage(queryStorePresaleOrderIn);
        List<ExcelPresaleOrderOut> excelDeliveryOrderDetails = parsePresaleOrderListDataToExcel(resultPage.getList());
        byte[] bytes = FileExportUtil.getFileBytesByData(excelDeliveryOrderDetails, title, title, ExcelPresaleOrderOut.class, true);
        return fileService.uploadFile(fileName, bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public String exportPresaleOrderDetailList(Long id) {
        DisStorePresaleOrderInfoOut storePresaleOrderInfo = this.getStorePresaleOrderInfo(id);
        List<ExcelStorePresaleOrderDetailInfoOut> excelRequestOrderDtlOuts = parsePresaleOrderDetailDataToExcel(storePresaleOrderInfo.getDetailInfoOutList());
        String title = "预售订单明细信息";
        byte[] bytes = FileExportUtil.getFileBytesByData(excelRequestOrderDtlOuts,
                title, title, ExcelStorePresaleOrderDetailInfoOut.class, true);
        return fileService.uploadFile(title + ".xlsx", bytes, SystemConstant.SYSTEM_CODE, SystemConstant.SYSTEM_NAME);
    }

    @Override
    public OrdDisPresaleOrder getOrdDisPresaleOrder(Long id) {
        return ordDisPresaleOrderMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean cancelStorePresaleOrder(Long id) {
        /**
         * 1、待付款状态，可操作取消订单；
         * 2、取消订单后，单据状态变为已取消状态；
         * 3、取消成功，吐司提示：预售订单取消成功！
         */
        OrdDisPresaleOrder ordDisPresaleOrder = ordDisPresaleOrderMapper.selectByPrimaryKey(id);
        if (Objects.isNull(ordDisPresaleOrder)) {
            throw new BusinessException("预售订单不存在");
        }
        if (!OrdDisPresaleOrderStatusEnum.WAIT_PAYMENT.getKey().equals(ordDisPresaleOrder.getStatus())) {
            throw new BusinessException("预售订单状态异常，无法取消");
        }
        String beforeStatusCode = ordDisPresaleOrder.getStatus();
        ordDisPresaleOrder.setStatus(OrdDisPresaleOrderStatusEnum.CANCEL.getKey());
        boolean resultFlag = ordDisPresaleOrderMapper.updateByPrimaryKeySelective(ordDisPresaleOrder) > 0;

        String content = MessageFormat.format(OrderLogEnum.DIS_PRESALE_ORDER.getKey(),
                OrdDisPresaleOrderStatusEnum.getValueByKey(beforeStatusCode), OrdDisPresaleOrderStatusEnum.getValueByKey(OrdDisPresaleOrderStatusEnum.CANCEL.getKey()));
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER_PAY.getName(),
                String.valueOf(ordDisPresaleOrder.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER.getCode(),
                content, new Date(), ordDisPresaleOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return resultFlag;
    }

    @Override
    public Response<String> checkIsCanRefund(OrdDisPresaleOrder ordDisPresaleOrder) {
        OrdDisPresaleAssets presaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(ordDisPresaleOrder.getStoreCode());
        if (Objects.isNull(presaleAssets)) {
            return Response.error("门店资产不存在");
        }
        List<OrdDisPresaleOrderDetail> detailList = ordDisPresaleOrderDetailMapper.findListByOrderId(ordDisPresaleOrder.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            return Response.error("明细不存在");
        }
        // 因为有赠品，按商品代码数量合并
        List<OrdDisPresaleOrderDetail> margePresaleOrderGoodsList = detailList.stream().collect(Collectors.toMap(OrdDisPresaleOrderDetail::getGoodsCode, a -> a, (o1, o2) -> {
            o1.setGoodsQuantity(o1.getGoodsQuantity().add(o2.getGoodsQuantity()));
            o1.setPackageQuantity(o1.getPackageQuantity().add(o2.getPackageQuantity()));
            return o1;
        })).values().stream().collect(Collectors.toList());
        StringJoiner errorJoiner = new StringJoiner(SystemConstant.COMMA);
        margePresaleOrderGoodsList.forEach(targetOrderDetail -> {
            OrdDisPresaleAssetsDetail ordDisPresaleAssetsDetail = new OrdDisPresaleAssetsDetail();
            ordDisPresaleAssetsDetail.setAssetsId(presaleAssets.getId());
            ordDisPresaleAssetsDetail.setGoodsCode(targetOrderDetail.getGoodsCode());
            ordDisPresaleAssetsDetail.setPresaleActivityId(targetOrderDetail.getPresaleActivityId());
            ordDisPresaleAssetsDetail = ordDisPresaleAssetsDetailService.selectOne(ordDisPresaleAssetsDetail);
            if (Objects.isNull(ordDisPresaleAssetsDetail)) {
                throw new BusinessException("商品" + targetOrderDetail.getGoodsCode() + "资产中不存在");
            }
            if (ordDisPresaleAssetsDetail.getSurplusQuantity().compareTo(targetOrderDetail.getGoodsQuantity()) == -1) {
                errorJoiner.add(targetOrderDetail.getGoodsCode());
            }
        });
        if (errorJoiner.length() > NumberUtil.INTEGER_ZERO) {
            return Response.error("门店预售资产中商品：" + errorJoiner + "数量不够预售订单退款");
        }
        return Response.success();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> refundPresaleOrder(OrdDisPresaleOrder ordDisPresaleOrder, String loginUsername) {
        //通知资管中心回退金额
        RechargeLiquidationIn liquidationIn = new RechargeLiquidationIn();
        liquidationIn.setRecipientPrincipalCode(ordDisPresaleOrder.getStoreCode());
        liquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
        liquidationIn.setPayOrPrincipalCode(ordDisPresaleOrder.getBizOrgCode());
        liquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
        liquidationIn.setBizOrgCode(ordDisPresaleOrder.getBizOrgCode());
        liquidationIn.setBusinessNo(ordDisPresaleOrder.getPresaleOrderNo());
        liquidationIn.setBusinessType(FundTypeEnum.RETURN_PRESALE_ORDER.getCode());
        liquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        liquidationIn.setLiquidationAmount(ordDisPresaleOrder.getTotalPayAmount().abs());
        liquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
        liquidationIn.setOriginalBusinessNo(ordDisPresaleOrder.getPresaleOrderNo());
        liquidationIn.setRemark(FundReturnTypeEnum.RETURN_PRESALE_ORDER.getName());
        Response response = fundServer.settlement(liquidationIn);
        if (!response.isSuccess()) {
            log.error("预售订单{}退款失败:{}", ordDisPresaleOrder.getPresaleOrderNo(), response.getMessage());
            return Response.error("预售订单退款失败");
        }
        // 预售，退资产
        UpdateDisPresaleAssetsIn updateDisPresaleAssetsIn = new UpdateDisPresaleAssetsIn();
        updateDisPresaleAssetsIn.setBizOrgCode(ordDisPresaleOrder.getBizOrgCode());
        updateDisPresaleAssetsIn.setLoginUsername(loginUsername);
        updateDisPresaleAssetsIn.setBusinessType(OrdDisPresaleFlowBusinessTypeEnum.PRESALE_ORDER_REFUND.getKey());
        updateDisPresaleAssetsIn.setSourceNo(ordDisPresaleOrder.getPresaleOrderNo());
        OrdDisPresaleAssets ordDisPresaleAssets = ordDisPresaleAssetsService.getOrdDisPresaleAssetsByStoreCode(ordDisPresaleOrder.getStoreCode());
        if (Objects.nonNull(ordDisPresaleAssets)) {
            updateDisPresaleAssetsIn.setAssetsId(ordDisPresaleAssets.getId());
        }

        List<OrdDisPresaleOrderDetail> presaleOrderDetailList = ordDisPresaleOrderDetailMapper.findListByOrderId(ordDisPresaleOrder.getId());
        OrdDisPresaleAssets finalOrdDisPresaleAssets = ordDisPresaleAssets;
        Map<String, List<OrdDisPresaleOrderDetail>> goodsPresaleDetailsMap = presaleOrderDetailList.stream().collect(Collectors.groupingBy(OrdDisPresaleOrderDetail::getGoodsCode));
        List<UpdateDisPresaleAssetsGoodsIn> assetsGoodsInList = goodsPresaleDetailsMap.entrySet().stream().map(entry -> {
            OrdDisPresaleOrderDetail detail = entry.getValue().get(NumberUtil.INTEGER_ZERO);
            UpdateDisPresaleAssetsGoodsIn updateDisPresaleAssetsGoodsIn = new UpdateDisPresaleAssetsGoodsIn();
            BeanUtils.copy(detail, updateDisPresaleAssetsGoodsIn);
            updateDisPresaleAssetsGoodsIn.setStoreCode(ordDisPresaleOrder.getStoreCode());
            updateDisPresaleAssetsGoodsIn.setStoreName(ordDisPresaleOrder.getStoreName());
            updateDisPresaleAssetsGoodsIn.setActualLowering(AdjustTypeEnum.REDUCE.getCode());
            updateDisPresaleAssetsGoodsIn.setOrderQuantity(BigDecimal.ZERO);
            BigDecimal updateSurplusQuantity = entry.getValue().stream().map(OrdDisPresaleOrderDetail::getGoodsQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            updateDisPresaleAssetsGoodsIn.setUpdateSurplusQuantity(updateSurplusQuantity.negate());
            updateDisPresaleAssetsGoodsIn.setLoginUsername(loginUsername);
            updateDisPresaleAssetsGoodsIn.setAssetsId(finalOrdDisPresaleAssets.getId());
            return updateDisPresaleAssetsGoodsIn;
        }).collect(Collectors.toList());
        updateDisPresaleAssetsIn.setAssetsGoodsInList(assetsGoodsInList);
        ordDisPresaleAssetsService.updatePresaleAssets(updateDisPresaleAssetsIn);
        // 更新预售订单为已退款
        String beforeStatusCode = ordDisPresaleOrder.getStatus();
        ordDisPresaleOrder.setStatus(OrdDisPresaleOrderStatusEnum.REFUND.getKey());
        ordDisPresaleOrder.setUpdater(loginUsername);
        ordDisPresaleOrder.setUpdateTime(LocalDateTime.now());
        ordDisPresaleOrderMapper.updateByPrimaryKey(ordDisPresaleOrder);
        String content = MessageFormat.format(OrderLogEnum.DIS_PRESALE_ORDER.getKey(),
                OrdDisPresaleOrderStatusEnum.getValueByKey(beforeStatusCode), OrdDisPresaleOrderStatusEnum.getValueByKey(OrdDisPresaleOrderStatusEnum.REFUND.getKey()));
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER_PAY.getName(),
                String.valueOf(ordDisPresaleOrder.getId()), OrdLogTypeEnum.ORD_DIS_PRESALE_ORDER.getCode(),
                content, new Date(), ordDisPresaleOrder.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return Response.success("预售订单退款成功");
    }

    @Override
    public OrdDisPresaleOrder getOneByIdAndBizOrgCode(Long id, String bizOrgCode) {
        OrdDisPresaleOrder ordDisPresaleOrder = new OrdDisPresaleOrder();
        ordDisPresaleOrder.setId(id);
        ordDisPresaleOrder.setBizOrgCode(bizOrgCode);
        ordDisPresaleOrder.setIsDelete(ModelConst.DELETE.NO);
        return ordDisPresaleOrderMapper.selectOne(ordDisPresaleOrder);
    }

    private List<ExcelPresaleOrderOut> parsePresaleOrderListDataToExcel(List<DisStorePresaleOrderInfoOut> list) {
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : IntStream.range(0, list.size())
                .mapToObj(i -> convertPresaleOrderListExcel(list.get(i), i))
                .collect(Collectors.toList());
    }

    /**
     * 将DeliveryOrderDetailsOut转换为导出ExcelStoreSkuBaseValue
     *
     * @param disStorePresaleOrderInfoOut
     * @return ExcelStoreSkuBaseValue
     */
    private ExcelPresaleOrderOut convertPresaleOrderListExcel(DisStorePresaleOrderInfoOut disStorePresaleOrderInfoOut, int index) {
        ExcelPresaleOrderOut excelPresaleOrderOut = new ExcelPresaleOrderOut();
        BeanUtils.copy(disStorePresaleOrderInfoOut, excelPresaleOrderOut);
        excelPresaleOrderOut.setCreateTimeStr(DateUtils.format(disStorePresaleOrderInfoOut.getCreateTime()));
        excelPresaleOrderOut.setIndex(index + 1);
        return excelPresaleOrderOut;
    }

    private List<ExcelStorePresaleOrderDetailInfoOut> parsePresaleOrderDetailDataToExcel(List<StorePresaleOrderDetailInfoOut> detailInfoOutList) {
        return CollectionUtils.isEmpty(detailInfoOutList) ? Collections.emptyList() : IntStream.range(0, detailInfoOutList.size())
                .mapToObj(i -> convertPresaleOrderDetailExcel(detailInfoOutList.get(i), i))
                .collect(Collectors.toList());
    }

    private ExcelStorePresaleOrderDetailInfoOut convertPresaleOrderDetailExcel(StorePresaleOrderDetailInfoOut storePresaleOrderDetailInfoOut, int index) {
        ExcelStorePresaleOrderDetailInfoOut orderDetailInfoOut = new ExcelStorePresaleOrderDetailInfoOut();
        BeanUtils.copy(storePresaleOrderDetailInfoOut, orderDetailInfoOut);
        orderDetailInfoOut.setGoodsQuantity(storePresaleOrderDetailInfoOut.getGoodsQuantity().intValue());
        orderDetailInfoOut.setIndex(index + 1);
        return orderDetailInfoOut;
    }
}
