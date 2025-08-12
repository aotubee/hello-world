package com.edc.erp.common.service;


import com.edc.erp.common.model.out.fl.DeliveryNumberVO;
import com.edc.plugins.common.response.Response;

/**
 * 物流信息业务接口
 * @author lishaobo
 * @since 2023-01-02 14:56:21
 */
public interface WmsService {

    /**
     * 获取物流箱数信息
     * @param deliveryNo
     * @return
     */
    Response<DeliveryNumberVO> getDeliveryNumberByDeliveryNo(String deliveryNo);
}
