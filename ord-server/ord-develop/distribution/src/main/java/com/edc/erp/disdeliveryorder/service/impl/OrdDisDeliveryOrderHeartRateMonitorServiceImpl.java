package com.edc.erp.disdeliveryorder.service.impl;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderHeartRateMonitor;
import com.edc.erp.disdeliveryorder.mapper.OrdDisDeliveryOrderHeartRateMonitorMapper;
import com.edc.erp.disdeliveryorder.model.in.UpdateDisDeliveryOrderHeartRateIn;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderHeartRateMonitorService;
import com.edc.plugins.mybatis.service.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * 配销单收货心跳检测(OrdDisDeliveryOrderHeartRateMonitor)表服务实现类
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Service
@RequiredArgsConstructor
public class OrdDisDeliveryOrderHeartRateMonitorServiceImpl extends BaseServiceImpl<OrdDisDeliveryOrderHeartRateMonitor> implements OrdDisDeliveryOrderHeartRateMonitorService {

    private final OrdDisDeliveryOrderHeartRateMonitorMapper ordDisDeliveryOrderHeartRateMonitorMapper;

    /**
     * 根据配销单id删除心跳检测记录
     *
     * @param id
     * @param bizOrgCode
     * @return
     */
    @Override
    public int deleteByDeliveryOrderId(Long id, String bizOrgCode) {
        OrdDisDeliveryOrderHeartRateMonitor monitor = new OrdDisDeliveryOrderHeartRateMonitor();
        monitor.setBizOrgCode(bizOrgCode);
        monitor.setDisDeliveryOrderId(id);
        int delete = ordDisDeliveryOrderHeartRateMonitorMapper.delete(monitor);
        return delete;
    }

    /**
     * 更新收货最近一次心跳时间
     *
     * @param updateDisDeliveryOrderHeartRateIn
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTakeDeliveryHeartRate(UpdateDisDeliveryOrderHeartRateIn updateDisDeliveryOrderHeartRateIn) {
        OrdDisDeliveryOrderHeartRateMonitor monitor = new OrdDisDeliveryOrderHeartRateMonitor();
        monitor.setBizOrgCode(updateDisDeliveryOrderHeartRateIn.getBizOrgCode());
        monitor.setDisDeliveryOrderId(updateDisDeliveryOrderHeartRateIn.getDeliveryOrderId());
        OrdDisDeliveryOrderHeartRateMonitor rateMonitor = ordDisDeliveryOrderHeartRateMonitorMapper.selectOne(monitor);
        if (Objects.nonNull(rateMonitor)) {
            rateMonitor.setLastHeartbeatTime(LocalDateTime.now());
            ordDisDeliveryOrderHeartRateMonitorMapper.updateByPrimaryKeySelective(rateMonitor);
        }
    }

    @Override
    public List<OrdDisDeliveryOrderHeartRateMonitor> findNeedResetHeartList(Integer heartRate) {
        return ordDisDeliveryOrderHeartRateMonitorMapper.findNeedResetHeartList(heartRate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveDisDeliveryOrderHeartRateMonitor(OrdDisDeliveryOrderHeartRateMonitor deliveryOrderHeartRateMonitor) {
        return ordDisDeliveryOrderHeartRateMonitorMapper.insert(deliveryOrderHeartRateMonitor);
    }
}
