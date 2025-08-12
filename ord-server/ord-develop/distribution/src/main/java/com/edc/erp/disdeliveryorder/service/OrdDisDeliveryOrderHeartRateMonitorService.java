package com.edc.erp.disdeliveryorder.service;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderHeartRateMonitor;
import com.edc.erp.disdeliveryorder.model.in.UpdateDisDeliveryOrderHeartRateIn;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 配销单收货心跳检测(OrdDisDeliveryOrderHeartRateMonitor)表服务接口
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
public interface OrdDisDeliveryOrderHeartRateMonitorService extends BaseService<OrdDisDeliveryOrderHeartRateMonitor> {

    /**
     * 根据配销单id删除心跳检测记录
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    int deleteByDeliveryOrderId(Long id, String bizOrgCode);

    /**
     * 更新收货最近一次心跳时间
     *
     * @param updateDisDeliveryOrderHeartRateIn
     */
    void updateTakeDeliveryHeartRate(UpdateDisDeliveryOrderHeartRateIn updateDisDeliveryOrderHeartRateIn);

    List<OrdDisDeliveryOrderHeartRateMonitor> findNeedResetHeartList(Integer heartRate);

    int saveDisDeliveryOrderHeartRateMonitor(OrdDisDeliveryOrderHeartRateMonitor deliveryOrderHeartRateMonitor);

}
