package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;

public interface OrdDisDeliveryOrderExtendService {

    OrdDisDelivery getOneByIdAndBizOrgCode(Long disDeliveryOrderId, String bizOrgCode);

    OrdDisDelivery getOneByDeliveryOrderNo(String deliveryOrderNo, String deliveryOrderStatus, String bizOrgCode);


}
