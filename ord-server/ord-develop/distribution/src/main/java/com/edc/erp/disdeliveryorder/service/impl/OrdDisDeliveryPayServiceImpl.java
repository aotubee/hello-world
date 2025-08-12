package com.edc.erp.disdeliveryorder.service.impl;

import com.edc.erp.common.async.handel.SyncOrdDisOrderHandle;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.*;
import com.edc.erp.common.model.in.fund.RechargeLiquidationIn;
import com.edc.erp.common.util.CreateCodeUtil;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryPay;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryPayMapper;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryPayService;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstService;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.bean.ModelConst;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrdDisDeliveryPayServiceImpl extends BaseServiceImpl<OrdDisDeliveryPay> implements OrdDisDeliveryPayService {

    private final OrdDisDeliveryPayMapper ordDisDeliveryPayMapper;
    private final SyncOrdDisOrderHandle syncOrdDisOrderHandle;
    private final UniqueUtils uniqueUtils;
    private final AsyncLogService asyncLogService;
    private final OrdDisOrderFirstService ordDisOrderFirstService;

    @Override
    public OrdDisDeliveryPay getOrdDisDeliveryPayBy(String deliveryOrderNo, String bizOrgCode) {
        OrdDisDeliveryPay ordDisDeliveryPay = new OrdDisDeliveryPay();
        ordDisDeliveryPay.setDeliveryOrderNo(deliveryOrderNo);
        ordDisDeliveryPay.setBizOrgCode(bizOrgCode);
        ordDisDeliveryPay.setIsDelete(ModelConst.DELETE.NO);
        return ordDisDeliveryPayMapper.selectOne(ordDisDeliveryPay);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<String> payDeliveryOrder(OrdDisDelivery ordDisDelivery, BigDecimal payAmount) {
        // 资管余额支付
        if (BigDecimal.ZERO.compareTo(ordDisDelivery.getOrderAmount()) == 0) {
            return Response.success();
        }
        OrdDisDeliveryPay ordDisDeliveryPay = new OrdDisDeliveryPay();
        ordDisDeliveryPay.setDeliveryOrderNo(ordDisDelivery.getDeliveryOrderNo());
        ordDisDeliveryPay.setOriginalAmount(payAmount);
        ordDisDeliveryPay.setActualAmount(payAmount);
        ordDisDeliveryPay.setBizOrgCode(ordDisDelivery.getBizOrgCode());
        ordDisDeliveryPay.setOrgCode(ordDisDelivery.getOrgCode());
        ordDisDeliveryPay.setTransactionStatus(DisDeliveryPayStatusEnum.PAID.getCode());
        ordDisDeliveryPay.setCreator(ordDisDelivery.getCreator());
        ordDisDeliveryPay.setUpdater(ordDisDelivery.getUpdater());
        ordDisDeliveryPay.setPayNo(CreateCodeUtil.getOrgOrderNo(OrderNoPreEnum.PXPAY.getCode(), ordDisDelivery.getBizOrgCode(), uniqueUtils, 4));
        ordDisDeliveryPayMapper.insertSelective(ordDisDeliveryPay);

        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        rechargeLiquidationIn.setPayOrPrincipalCode(ordDisDelivery.getStoreCode());
        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.STORE.getCode());
        rechargeLiquidationIn.setRecipientPrincipalCode(ordDisDelivery.getBizOrgCode());
        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
        rechargeLiquidationIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
        rechargeLiquidationIn.setBusinessNo(ordDisDelivery.getDeliveryOrderNo());
        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DISTRIBUTION_SUPPLEMENT.getCode());
        rechargeLiquidationIn.setLiquidationAmount(ordDisDeliveryPay.getActualAmount());
        rechargeLiquidationIn.setDirection(FundDirectionEnum.PAY.getCode());
        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
        if (!response.isSuccess()) {
            log.error("创建配销单支付异常:{}", response.getMessage());
            return response;
        } else {
            String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ORDER_PAY.getKey(), ordDisDelivery.getDeliveryOrderNo(), ordDisDeliveryPay.getActualAmount());
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                    String.valueOf(ordDisDelivery.getId()),
                    OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                    content, new Date(),
                    ordDisDelivery.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            return Response.success();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response returnAmountByDeliveryOrder(OrdDisDelivery ordDisDelivery, String originalBusinessNo, String fundReturnType,
                                                BigDecimal returnAmount, Long deliveryId) {
        if (BigDecimal.ZERO.compareTo(returnAmount) == 0) {
            return Response.success();
        }
        Response response = null;
        if (DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDisDelivery.getSourceCode())) {
            response = this.returnAmountByFirstOrder(ordDisDelivery, returnAmount, fundReturnType, deliveryId);
        }
        if (DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDisDelivery.getSourceCode())) {
            response = this.sendReturnAmountRequest(ordDisDelivery, returnAmount, originalBusinessNo, fundReturnType);
        }
        if (DeliveryOrderSourceCodeEnum.MANUAL.getType().equals(ordDisDelivery.getSourceCode())) {
            response = this.sendReturnAmountRequest(ordDisDelivery, returnAmount, originalBusinessNo, fundReturnType);
//            response = this.returnAmountByManual(ordDisDelivery, originalBusinessNo, returnAmount, fundReturnType); 冻结不在入配销单支付记录表
        }
        if (Objects.isNull(response)) {
            return Response.error("未知类型来源退款");
        }
        String content = MessageFormat.format(DeliveryOrderLogEnum.DIS_DELIVERY_ORDER_RETURN.getKey(), ordDisDelivery.getDeliveryOrderNo(), fundReturnType, returnAmount);
        BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE,
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getName(),
                String.valueOf(ordDisDelivery.getId()),
                OrdLogTypeEnum.DIS_DELIVERY_ORDER.getCode(),
                content, new Date(),
                ordDisDelivery.getUpdater());
        asyncLogService.sendAsyncSaveLogByMq(businessLog);
        return response;
    }


    /**
     * 计算作废配销单返款金额
     *
     * @param ordDisDelivery   配销单
     * @param beforeStatusCode 当前配销单状态
     * @return
     */
    @Override
    public BigDecimal calculationInvalidDisDeliverAmount(OrdDisDelivery ordDisDelivery, String beforeStatusCode) {
        BigDecimal returnAmount = BigDecimal.ZERO;
        // 订单流和收单铺货
        if (DeliveryOrderSourceCodeEnum.ORDER_CONFIG.getType().equals(ordDisDelivery.getSourceCode())
                || DeliveryOrderSourceCodeEnum.FIRST_ORDER.getType().equals(ordDisDelivery.getSourceCode())) {
            if (DeliveryOrderEnum.PENDING.getKey().equals(beforeStatusCode) || DeliveryOrderEnum.APPROVED.getKey().equals(beforeStatusCode)) {
                returnAmount = ordDisDelivery.getOrderAmount();
            }
        }
        // 运营端手动创建
        if (DeliveryOrderSourceCodeEnum.MANUAL.getType().equals(ordDisDelivery.getSourceCode())) {
            if (DeliveryOrderEnum.APPROVED.getKey().equals(beforeStatusCode)) {
                returnAmount = ordDisDelivery.getDistributionAmount();
            }
        }
        return returnAmount;
    }

    private Response returnAmountByFirstOrder(OrdDisDelivery ordDisDelivery, BigDecimal returnAmount, String fundReturnType,
                                              Long deliveryId) {
        OrdDisOrderFirst ordDisOrderFirst = ordDisOrderFirstService.getOrdDisFirstByDeliveryOrderId(deliveryId);
        if (Objects.isNull(ordDisOrderFirst)) {
            log.error("配销单{}未查询到铺货单", ordDisDelivery.getDeliveryOrderNo());
            throw new BusinessException("配销单" + ordDisDelivery.getDeliveryOrderNo() + "未查询到铺货单");
        }
        String originalBusinessNo = ordDisOrderFirst.getFirstOrderNo();
        Response response = sendReturnAmountRequest(ordDisDelivery, returnAmount, originalBusinessNo, fundReturnType);
        if (!response.isSuccess()) {
            log.error("铺货单{}拆分配销单{}退款异常:{}", originalBusinessNo, ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
        }
        return response;
    }

    private Response returnAmountByManual(OrdDisDelivery ordDisDelivery, String originalBusinessNo, BigDecimal returnAmount, String fundReturnType) {
        OrdDisDeliveryPay ordDisDeliveryPay = this.getOrdDisDeliveryPayBy(originalBusinessNo, ordDisDelivery.getBizOrgCode());
        if (Objects.isNull(ordDisDeliveryPay)) {
            return Response.error("配销单" + originalBusinessNo + "不存在支付记录");
        }
        if (DisDeliveryPayStatusEnum.REFUNDED.getTagName().equals(ordDisDeliveryPay.getTransactionStatus())) {
            return Response.error("配销单" + originalBusinessNo + "已退款");
        }
        if (returnAmount.compareTo(ordDisDeliveryPay.getActualAmount()) == NumberUtil.INTEGER_ONE) {
            return Response.error("配销单" + originalBusinessNo + "退款金额大于原支付金额");
        }
        Response response = this.sendReturnAmountRequest(ordDisDelivery, returnAmount, originalBusinessNo, fundReturnType);
        if (!response.isSuccess()) {
            log.error("运营端手动创建配销单{}退款异常:{}", ordDisDelivery.getDeliveryOrderNo(), response.getMessage());
            return response;
        }
        else {
            OrdDisDeliveryPay updateOrdDisDeliveryPay = new OrdDisDeliveryPay();
            updateOrdDisDeliveryPay.setId(ordDisDeliveryPay.getId());
            updateOrdDisDeliveryPay.setTransactionStatus(DisDeliveryPayStatusEnum.REFUNDED.getCode());
            updateOrdDisDeliveryPay.setUpdater(ordDisDelivery.getUpdater());
            updateOrdDisDeliveryPay.setUpdateTime(LocalDateTime.now());
            ordDisDeliveryPayMapper.updateByPrimaryKeySelective(updateOrdDisDeliveryPay);
            return Response.success();
        }
    }

    private Response sendReturnAmountRequest(OrdDisDelivery ordDisDelivery, BigDecimal returnAmount, String originalBusinessNo, String fundReturnType) {
        RechargeLiquidationIn rechargeLiquidationIn = new RechargeLiquidationIn();
        rechargeLiquidationIn.setRecipientPrincipalCode(ordDisDelivery.getStoreCode());
        rechargeLiquidationIn.setRecipientPrincipalType(PrincipalTypeEnum.STORE.getCode());
        rechargeLiquidationIn.setPayOrPrincipalCode(ordDisDelivery.getBizOrgCode());
        rechargeLiquidationIn.setPayOrPrincipalType(PrincipalTypeEnum.ORGANIZATION.getCode());
        rechargeLiquidationIn.setBizOrgCode(ordDisDelivery.getBizOrgCode());
        rechargeLiquidationIn.setBusinessNo(ordDisDelivery.getDeliveryOrderNo());
        rechargeLiquidationIn.setBusinessType(FundTypeEnum.DIS_DELIVERY_ORDER_RETURN.getCode());
        rechargeLiquidationIn.setLiquidationAmount(returnAmount);
        rechargeLiquidationIn.setBusinessModule(BusinessModuleEnum.INVENTORY_CENTER.getCode());
        rechargeLiquidationIn.setDirection(FundDirectionEnum.RETURN.getCode());
        rechargeLiquidationIn.setOriginalBusinessNo(originalBusinessNo);
        rechargeLiquidationIn.setRemark(fundReturnType);
        Response response = syncOrdDisOrderHandle.syncOrderToFund(rechargeLiquidationIn);
        return response;
    }

}
