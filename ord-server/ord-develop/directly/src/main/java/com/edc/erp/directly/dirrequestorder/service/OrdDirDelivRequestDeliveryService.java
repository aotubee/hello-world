package com.edc.erp.directly.dirrequestorder.service;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirrequestorder.entity.OrdDirDelivRequestDelivery;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;



/**
 * 要货单与配货单关联表(OrdDirDelivRequestDelivery)表服务接口
 *
 * @author weichao
 * @since 2022-11-15 12:27:14
 */
public interface OrdDirDelivRequestDeliveryService extends BaseService<OrdDirDelivRequestDelivery> {

    /**
     * 批量新增
     *
     * @param deliveryOrderList
     * @param requestOrderId
     */
    void batchSave(List<OrdDirDelivery> deliveryOrderList, Long requestOrderId);
}
