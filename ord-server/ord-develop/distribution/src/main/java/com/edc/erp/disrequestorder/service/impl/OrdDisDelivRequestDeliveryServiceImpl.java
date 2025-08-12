package com.edc.erp.disrequestorder.service.impl;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDelivery;
import com.edc.erp.disrequestorder.mapper.OrdDisDelivRequestDeliveryMapper;
import com.edc.erp.disrequestorder.service.OrdDisDelivRequestDeliveryService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 集货单与配销单关联表(OrdDisDelivRequestDelivery)表服务实现类
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
@Service
@RequiredArgsConstructor
public class OrdDisDelivRequestDeliveryServiceImpl extends BaseServiceImpl<OrdDisDelivRequestDelivery> implements OrdDisDelivRequestDeliveryService {

    private final OrdDisDelivRequestDeliveryMapper ordDisDelivRequestDeliveryMapper;


    /**
     * 批量关联集货单与配销单关系
     *
     * @param deliveryOrderList
     * @param requestOrderId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<OrdDisDelivery> deliveryOrderList, Long requestOrderId) {
        for (OrdDisDelivery ordDisDelivery : deliveryOrderList) {
            OrdDisDelivRequestDelivery ordDisDelivRequestDelivery = new OrdDisDelivRequestDelivery();
            ordDisDelivRequestDelivery.setDeliveryOrderId(ordDisDelivery.getId());
            ordDisDelivRequestDelivery.setRequestOrderId(requestOrderId);
            ordDisDelivRequestDelivery.setCreator(ordDisDelivery.getCreator());
            ordDisDelivRequestDeliveryMapper.insertSelective(ordDisDelivRequestDelivery);
        }
    }
}
