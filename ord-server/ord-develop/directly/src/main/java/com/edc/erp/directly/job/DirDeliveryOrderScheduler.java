package com.edc.erp.directly.job;


import com.edc.erp.common.util.InterceptorUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.handle.TakeDirDeliveryHandle;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 配货单定时器
 */
@Component
@Slf4j
public class DirDeliveryOrderScheduler {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TaskLockUtils taskLockUtils;

    @Autowired
    private TakeDirDeliveryHandle takeDirDeliveryHandle;


    /**
     * 关闭自动收货定时器开关值
     */
    private final static String CLOSE_AUTO_TAKE_DELIVERY_KEY = "OPEN";


    /**
     * 自动收货定时器
     */
//    @Scheduled(cron = "0 0 14,15,16,17,18 * * ? ")
    @Scheduled(cron = "0 0/20 * * * ?")
    public void autoTakeDelivery() {
        String closeAutoTakeDeliverySwitch = redisService.get(DirSystemConstant.DIR_CLOSE_AUTO_TAKE_DELIVERY_SWITCH_KEY);
        if (CLOSE_AUTO_TAKE_DELIVERY_KEY.equals(closeAutoTakeDeliverySwitch)) {
            log.info("自动收货开关未打开......");
            return;
        }
        log.info("开始执行自动收货任务");
        if (taskLockUtils.lock(DirSystemConstant.DIR_AUTO_TAKE_DELIVERY_SWITCH, 60 * 15 * 1000)) {
            try {
                boolean flag = InterceptorUtil.checkSystemMaintenance(redisService.get(com.edc.erp.common.constant.SystemConstant.SYSTEM_MAINTENANCE));
                if (!flag) {
                    return;
                }
                takeDirDeliveryHandle.autoTakeDisDelivery();
            } catch (Exception e) {
                log.error("执行自动收货任务异常：", e);
            } finally {
                taskLockUtils.unlock(DirSystemConstant.DIR_AUTO_TAKE_DELIVERY_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", DirSystemConstant.DIR_AUTO_TAKE_DELIVERY_SWITCH);
        }
        log.info("自动收货任务执行结束");
    }


}
