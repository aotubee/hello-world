package com.edc.erp.job;


import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryOrderHeartRateMonitor;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryOrderHeartRateMonitorService;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 配货单收货心跳监测定时器
 */
@Component
@Slf4j
public class DisDeliveryOrderHeartRateMonitorScheduler {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TaskLockUtils taskLockUtils;

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    @Autowired
    private OrdDisDeliveryOrderHeartRateMonitorService ordDisDeliveryOrderHeartRateMonitorService;


    /**
     * 关闭自动收货定时器开关值
     */
    private final static String CLOSE_RESET_TAKE_DELIVERY_SWITCH_KEY = "OPEN";


    /**
     * 重置收货定时器
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void resetTakeDelivery() {
        String closeAutoTakeDeliverySwitch = redisService.get(DisSystemConstant.DIS_CLOSE_RESET_TAKE_DELIVERY_SWITCH_KEY);
        if (CLOSE_RESET_TAKE_DELIVERY_SWITCH_KEY.equals(closeAutoTakeDeliverySwitch)) {
            log.info("重置超时未完成收货开关未打开......");
            return;
        }
        log.info("开始执行重置超时未完成收货任务");
        if (taskLockUtils.lock(DisSystemConstant.DIS_RESET_TAKE_DELIVERY_SWITCH, 60 * 5 * 1000)) {
            try {
                List<OrdDisDeliveryOrderHeartRateMonitor> needResetHeartList = ordDisDeliveryOrderHeartRateMonitorService.findNeedResetHeartList(DisSystemConstant.DIS_TAKE_DELIVERY_HEART_RATE_TIME);
                if (CollectionUtils.isEmpty(needResetHeartList)) {
                    return;
                }
                needResetHeartList.forEach(deliveryOrderHeartRateMonitor -> {
                    try {
                        ordDisDeliveryService.updateResetTakeDisDeliveryByDeliveryOrderId(deliveryOrderHeartRateMonitor.getDisDeliveryOrderId(), deliveryOrderHeartRateMonitor.getBizOrgCode(), com.edc.erp.common.constant.SystemConstant.SYSTEM_USER);
                    } catch (Exception e) {
                        log.error("配销单主键{}重置收货异常", deliveryOrderHeartRateMonitor.getDisDeliveryOrderId(), e);
                    }
                });
            } catch (Exception e) {
                log.error("执行重置超时未完成收货任务异常：", e);
            } finally {
                taskLockUtils.unlock(DisSystemConstant.DIS_RESET_TAKE_DELIVERY_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", DisSystemConstant.DIS_RESET_TAKE_DELIVERY_SWITCH);
        }
        log.info("重置超时未完成收货任务执行结束");
    }

}
