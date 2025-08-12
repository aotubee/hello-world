package com.edc.erp.disrequestorder.service;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disrequestorder.entity.OrdDisDelivRequestDelivery;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;



/**
 * 集货单与配销单关联表(OrdDisDelivRequestDelivery)表服务接口
 *
 * @author weichao
 * @since 2022-10-20 14:54:48
 */
public interface OrdDisDelivRequestDeliveryService extends BaseService<OrdDisDelivRequestDelivery> {

    /**
     * 批量关联集货单与配销单关系
     * @param deliveryOrderList
     * @param requestOrderId
     */
    void batchSave(List<OrdDisDelivery> deliveryOrderList, Long requestOrderId);
}
