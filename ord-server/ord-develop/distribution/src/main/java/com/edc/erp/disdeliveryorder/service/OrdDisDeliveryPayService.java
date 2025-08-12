package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryPay;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface OrdDisDeliveryPayService extends BaseService<OrdDisDeliveryPay> {

    OrdDisDeliveryPay getOrdDisDeliveryPayBy(String deliveryOrderNo, String bizOrgCode);

    Response<String> payDeliveryOrder(OrdDisDelivery ordDisDelivery, BigDecimal payAmount);

    Response returnAmountByDeliveryOrder(OrdDisDelivery ordDisDelivery, String originalBusinessNo, String fundReturnType, BigDecimal returnAmount, Long deliveryId);

    BigDecimal calculationInvalidDisDeliverAmount(OrdDisDelivery ordDisDelivery, String beforeStatusCode);
}
