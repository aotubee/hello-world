package com.edc.erp.directly.dirdeliveryorder.service.impl;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderHeartRateMonitor;
import com.edc.erp.directly.dirdeliveryorder.mapper.OrdDirDeliveryOrderHeartRateMonitorMapper;
import com.edc.erp.directly.dirdeliveryorder.model.in.UpdateDeliveryOrderHeartRateIn;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryOrderHeartRateMonitorService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 配货单收货心跳检测(OrdDirDeliveryOrderHeartRateMonitor)表服务实现类
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
@Service
@RequiredArgsConstructor
public class OrdDirDeliveryOrderHeartRateMonitorServiceImpl extends BaseServiceImpl<OrdDirDeliveryOrderHeartRateMonitor> implements OrdDirDeliveryOrderHeartRateMonitorService {

    private final OrdDirDeliveryOrderHeartRateMonitorMapper ordDirDeliveryOrderHeartRateMonitorMapper;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTakeDeliveryHeartRate(UpdateDeliveryOrderHeartRateIn updateDeliveryOrderHeartRateIn) {
        OrdDirDeliveryOrderHeartRateMonitor deliveryOrderHeartRateMonitor = new OrdDirDeliveryOrderHeartRateMonitor();
        deliveryOrderHeartRateMonitor.setDirDeliveryOrderId(updateDeliveryOrderHeartRateIn.getDeliveryOrderId());
        deliveryOrderHeartRateMonitor.setBizOrgCode(updateDeliveryOrderHeartRateIn.getBizOrgCode());
        deliveryOrderHeartRateMonitor.setLastHeartbeatTime(LocalDateTime.now());
        ordDirDeliveryOrderHeartRateMonitorMapper.updateTakeDeliveryHeartRateByDirDeliveryOrderId(deliveryOrderHeartRateMonitor);
    }


    /**
     * 根据配货单id删除心跳检测记录
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByDeliveryOrderId(Long id, String bizOrgCode) {
        OrdDirDeliveryOrderHeartRateMonitor monitor = new OrdDirDeliveryOrderHeartRateMonitor();
        monitor.setBizOrgCode(bizOrgCode);
        monitor.setDirDeliveryOrderId(id);
        int delete = ordDirDeliveryOrderHeartRateMonitorMapper.delete(monitor);
        return delete;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveDirDeliveryOrderHeartRateMonitor(OrdDirDeliveryOrderHeartRateMonitor deliveryOrderHeartRateMonitor) {
        return ordDirDeliveryOrderHeartRateMonitorMapper.insert(deliveryOrderHeartRateMonitor);
    }

    @Override
    public List<OrdDirDeliveryOrderHeartRateMonitor> findNeedResetHeartList(Integer heartRate) {
        return ordDirDeliveryOrderHeartRateMonitorMapper.findNeedResetHeartList(heartRate);
    }
}
