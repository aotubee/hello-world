package com.edc.erp.disdeliveryorder.service.impl;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryMapper;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderExtendService;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdDisDeliveryOrderExtendServiceImpl implements OrdDisDeliveryOrderExtendService {

    private final OrdDisDeliveryMapper ordDisDeliveryMapper;


    @Override
    public OrdDisDelivery getOneByIdAndBizOrgCode(Long disDeliveryOrderId, String bizOrgCode) {
        OrdDisDelivery ordDisDelivery = new OrdDisDelivery();
        ordDisDelivery.setId(disDeliveryOrderId);
        ordDisDelivery.setBizOrgCode(bizOrgCode);
        ordDisDelivery.setIsDelete(ModelConst.DELETE.NO);
        return ordDisDeliveryMapper.selectOne(ordDisDelivery);
    }

    @Override
    public OrdDisDelivery getOneByDeliveryOrderNo(String deliveryOrderNo, String deliveryOrderStatus, String bizOrgCode) {
        OrdDisDelivery ordDisDelivery = new OrdDisDelivery();
        ordDisDelivery.setDeliveryOrderNo(deliveryOrderNo);
        ordDisDelivery.setDeliveryStatusCode(deliveryOrderStatus);
        ordDisDelivery.setBizOrgCode(bizOrgCode);
        ordDisDelivery.setIsDelete(ModelConst.DELETE.NO);
        return ordDisDeliveryMapper.selectOne(ordDisDelivery);
    }

}
