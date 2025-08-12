package com.edc.erp.directly.dirdeliveryorder.service.impl;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryMapper;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderExtendService;
import com.edc.plugins.utils.bean.ModelConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdDirDeliveryOrderExtendServiceImpl implements OrdDirDeliveryOrderExtendService {

    private final OrdDirDeliveryMapper ordDirDeliveryMapper;


    @Override
    public OrdDirDelivery getOneByIdAndBizOrgCode(Long dirDeliveryOrderId, String bizOrgCode) {
        OrdDirDelivery ordDirDelivery = new OrdDirDelivery();
        ordDirDelivery.setId(dirDeliveryOrderId);
        ordDirDelivery.setBizOrgCode(bizOrgCode);
        ordDirDelivery.setIsDelete(ModelConst.DELETE.NO);
        return ordDirDeliveryMapper.selectOne(ordDirDelivery);
    }

    @Override
    public OrdDirDelivery getOneByDeliveryOrderNo(String deliveryOrderNo, String deliveryOrderStatus, String bizOrgCode) {
        OrdDirDelivery ordDirDelivery = new OrdDirDelivery();
        ordDirDelivery.setDeliveryOrderNo(deliveryOrderNo);
        ordDirDelivery.setDeliveryStatusCode(deliveryOrderStatus);
        ordDirDelivery.setBizOrgCode(bizOrgCode);
        ordDirDelivery.setIsDelete(ModelConst.DELETE.NO);
        return ordDirDeliveryMapper.selectOne(ordDirDelivery);
    }

}
