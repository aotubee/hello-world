package com.edc.erp.directly.dirdeliveryorder.mapper;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryOrderHeartRateMonitor;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配货单收货心跳检测(OrdDirDeliveryOrderHeartRateMonitor)表数据库访问层
 *
 * @author weichao
 * @since 2022-11-21 11:15:37
 */
@Repository
public interface OrdDirDeliveryOrderHeartRateMonitorMapper extends BaseMapper<OrdDirDeliveryOrderHeartRateMonitor> {


    int updateTakeDeliveryHeartRateByDirDeliveryOrderId(OrdDirDeliveryOrderHeartRateMonitor deliveryOrderHeartRateMonitor);

    List<OrdDirDeliveryOrderHeartRateMonitor> findNeedResetHeartList(@Param("heartRate") Integer heartRate);

}
