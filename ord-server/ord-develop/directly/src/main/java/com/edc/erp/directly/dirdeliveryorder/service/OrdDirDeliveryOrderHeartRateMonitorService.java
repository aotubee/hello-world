package com.edc.erp.directly.dirdeliveryorder.service;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderHeartRateMonitor;
import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateDeliveryOrderHeartRateIn;
import com.edc.plugins.mybatis.service.BaseService;

import java.util.List;


/**
 * 配货单收货心跳检测(OrdDirDeliveryOrderHeartRateMonitor)表服务接口
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
public interface OrdDirDeliveryOrderHeartRateMonitorService extends BaseService<OrdDirDeliveryOrderHeartRateMonitor> {

    /**
     * 更新收货最近一次心跳时间
     *
     * @param updateDeliveryOrderHeartRateIn
     */
    void updateTakeDeliveryHeartRate(UpdateDeliveryOrderHeartRateIn updateDeliveryOrderHeartRateIn);


    /**
     * 根据配货单id删除心跳检测记录
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    int deleteByDeliveryOrderId(Long id, String bizOrgCode);


    int saveDirDeliveryOrderHeartRateMonitor(OrdDirDeliveryOrderHeartRateMonitor deliveryOrderHeartRateMonitor);

    List<OrdDirDeliveryOrderHeartRateMonitor> findNeedResetHeartList(Integer heartRate);
}
