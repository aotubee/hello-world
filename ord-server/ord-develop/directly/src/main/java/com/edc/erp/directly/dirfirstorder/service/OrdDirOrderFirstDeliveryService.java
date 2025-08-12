package com.edc.erp.directly.dirfirstorder.service;

import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirstDelivery;
import com.edc.plugins.common.model.page.Page;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;
import java.util.Map;



/**
 * 配货单与铺货单关联表(OrdDirOrderFirstDelivery)表服务接口
 *
 * @author weichao
 * @since 2022-11-10 14:08:50
 */
public interface OrdDirOrderFirstDeliveryService extends BaseService<OrdDirOrderFirstDelivery> {

    /**
     * 批量保存配货单与铺货单关联表
     * @param deliveryOrders
     * @param firstOrderId
     * @param userName
     */
    void batchSave(List<Long> deliveryOrders, Long firstOrderId, String userName);
}
