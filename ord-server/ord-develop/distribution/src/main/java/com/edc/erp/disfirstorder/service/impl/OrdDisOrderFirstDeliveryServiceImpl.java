package com.edc.erp.disfirstorder.service.impl;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.model.in.OrdDisDeliveryIn;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDelivery;
import com.edc.erp.disfirstorder.service.OrdDisOrderFirstDeliveryService;
import com.edc.erp.disfirstorder.mapper.OrdDisOrderFirstDeliveryMapper;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 配销配货单与配销铺货单关联表(OrdDisOrderFirstDelivery)表服务实现类
 *
 * @author weichao
 * @since 2022-10-10 16:17:51
 */
@Service
@RequiredArgsConstructor
public class OrdDisOrderFirstDeliveryServiceImpl extends BaseServiceImpl<OrdDisOrderFirstDelivery> implements OrdDisOrderFirstDeliveryService {

    private final OrdDisOrderFirstDeliveryMapper ordDisOrderFirstDeliveryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<Long> deliveryOrders, Long firstOrderId, String userName) {
        if (CollectionUtils.isEmpty(deliveryOrders)) {
            return;
        }
        ordDisOrderFirstDeliveryMapper.batchSave(deliveryOrders, firstOrderId, userName);
    }

    @Override
    public Long getFirstOrderIdByDeliveryOrderId(Long deliveryOrderId) {
        return ordDisOrderFirstDeliveryMapper.getFirstOrderIdByDeliveryOrderId(deliveryOrderId);
    }
}
