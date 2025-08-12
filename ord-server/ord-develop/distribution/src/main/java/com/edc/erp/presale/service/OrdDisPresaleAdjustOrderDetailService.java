package com.edc.erp.presale.service;

import com.edc.erp.presale.entity.OrdDisPresaleAdjustOrderDetail;
import com.edc.erp.presale.model.in.PresaleAdjustOrderSaveIn;
import com.edc.plugins.mybatis.service.BaseService;

public interface OrdDisPresaleAdjustOrderDetailService extends BaseService<OrdDisPresaleAdjustOrderDetail> {
    /**
     * 根据调整单id删除明细
     * @param adjustOrderId
     */
    void deleteByAdjustOrderId(Long adjustOrderId);

    /**
     * 保存调整单明细
     * @param adjustOrderId
     * @param orderSaveIn
     */
    void savePresaleAdjustOrderDetail(Long adjustOrderId, PresaleAdjustOrderSaveIn orderSaveIn);
}
