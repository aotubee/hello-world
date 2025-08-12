package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;

public interface OrdDirDeliveryOrderExtendService {
    OrdDirDelivery getOneByIdAndBizOrgCode(Long dirDeliveryOrderId, String bizOrgCode);

    OrdDirDelivery getOneByDeliveryOrderNo(String deliveryOrderNo, String deliveryOrderStatus, String bizOrgCode);

}
