package com.edc.erp.disdeliveryorder.mapper;

import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderHeartRateMonitor;
import com.edc.plugins.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 配销单收货心跳检测(OrdDisDeliveryOrderHeartRateMonitor)表数据库访问层
 *
 * @author fxw
 * @since 2022-10-26 18:06:47
 */
@Repository
public interface OrdDisDeliveryOrderHeartRateMonitorMapper extends BaseMapper<OrdDisDeliveryOrderHeartRateMonitor> {

    List<OrdDisDeliveryOrderHeartRateMonitor> findNeedResetHeartList(@Param("heartRate") Integer heartRate);
}
