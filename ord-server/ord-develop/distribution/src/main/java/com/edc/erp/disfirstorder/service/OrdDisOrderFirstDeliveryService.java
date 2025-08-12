package com.edc.erp.disfirstorder.service;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.model.in.OrdDisDeliveryIn;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirstDelivery;
import com.edc.plugins.mybatis.service.BaseService;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 配销配货单与配销铺货单关联表(OrdDisOrderFirstDelivery)表服务接口
 *
 * @author weichao
 * @since 2022-10-10 16:17:48
 */
public interface OrdDisOrderFirstDeliveryService extends BaseService<OrdDisOrderFirstDelivery> {
    /**
     * 批量保存配销配货单与配销铺货单关联表
     * @param deliveryOrders
     * @param firstOrderId
     * @param userName
     */
    void batchSave(List<Long> deliveryOrders, Long firstOrderId, String userName);

    Long getFirstOrderIdByDeliveryOrderId(Long deliveryOrderId);
}
