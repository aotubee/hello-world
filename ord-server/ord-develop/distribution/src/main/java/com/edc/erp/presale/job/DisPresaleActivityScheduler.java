package com.edc.erp.presale.job;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.presale.entity.OrdDisPresaleActivity;
import com.edc.erp.presale.service.OrdDisPresaleActivityService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配货单定时器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DisPresaleActivityScheduler {

    private final RedisService redisService;

    private final TaskLockUtils taskLockUtils;

    private final OrdDisPresaleActivityService ordDisPresaleActivityService;

    /**
     * 关闭自动收货定时器开关值
     */
    private final static String CLOSE_AUTO_TAKE_DELIVERY_KEY = "OPEN";


    /**
     * 自动收货定时器
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoExecutedPresaleActivity() {
        String closeAutoExecutedPresaleActivitySwitch = redisService.get(DisSystemConstant.DIS_CLOSE_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH_KEY);
        if (CLOSE_AUTO_TAKE_DELIVERY_KEY.equals(closeAutoExecutedPresaleActivitySwitch)) {
            log.info("自动生效加盟预售活动开关未打开......");
            return;
        }
        log.info("开始执行自动生效加盟预售活动任务");
        if (taskLockUtils.lock(DisSystemConstant.DIS_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH, 2 * 60 * 1000)) {
            try {
                while (true) {
                    taskLockUtils.unlock(DisSystemConstant.DIS_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH);
                    taskLockUtils.lock(DisSystemConstant.DIS_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH, 2 * 60 * 1000);
                    List<Long> needExecutedActivityIdList = ordDisPresaleActivityService.findNeedExecutePresaleActivityIdList();
                    if (CollectionUtils.isEmpty(needExecutedActivityIdList)) {
                        Thread.sleep(60 * 1000);
                        continue;
                    }
                    needExecutedActivityIdList.forEach(id -> {
                        try {
                            ordDisPresaleActivityService.executedPresaleActivity(id, SystemConstant.SYSTEM_NAME);
                        } catch (Exception e) {
                            log.error("生效预售活动{}异常", id, e);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("执行自动生效加盟预售活动任务异常：", e);
            } finally {
                taskLockUtils.unlock(DisSystemConstant.DIS_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", DisSystemConstant.DIS_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH);
        }
        log.info("自动生效加盟预售活动任务执行结束");
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoStopPresaleActivity() {
        String closeAutoExecutedPresaleActivitySwitch = redisService.get(DisSystemConstant.DIS_CLOSE_AUTO_STOP_PRESALE_ACTIVITY_SWITCH_KEY);
        if (CLOSE_AUTO_TAKE_DELIVERY_KEY.equals(closeAutoExecutedPresaleActivitySwitch)) {
            log.info("自动中止加盟预售活动开关未打开......");
            return;
        }
        log.info("开始执行自动中止加盟预售活动任务");
        if (taskLockUtils.lock(DisSystemConstant.DIS_AUTO_STOP_PRESALE_ACTIVITY_SWITCH, 2 * 60 * 1000)) {
            try {
                while (true) {
                    taskLockUtils.unlock(DisSystemConstant.DIS_AUTO_STOP_PRESALE_ACTIVITY_SWITCH);
                    taskLockUtils.lock(DisSystemConstant.DIS_AUTO_STOP_PRESALE_ACTIVITY_SWITCH, 2 * 60 * 1000);
                    List<Long> needExecutedActivityIdList = ordDisPresaleActivityService.findNeedStopPresaleActivityIdList();
                    if (CollectionUtils.isEmpty(needExecutedActivityIdList)) {
                        Thread.sleep(60 * 1000);
                        continue;
                    }
                    needExecutedActivityIdList.forEach(id -> {
                        try {
                            OrdDisPresaleActivity presaleActivity = ordDisPresaleActivityService.getOneById(id);
                            presaleActivity.setUpdater(SystemConstant.SYSTEM_USER);
                            presaleActivity.setUpdateTime(LocalDateTime.now());
                            ordDisPresaleActivityService.terminatePresaleActivity(presaleActivity);
                        } catch (Exception e) {
                            log.error("中止预售活动{}异常", id, e);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("执行自动中止加盟预售活动任务异常：", e);
            } finally {
                taskLockUtils.unlock(DisSystemConstant.DIS_AUTO_STOP_PRESALE_ACTIVITY_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", DisSystemConstant.DIS_AUTO_EXECUTE_PRESALE_ACTIVITY_SWITCH);
        }
        log.info("自动中止加盟预售活动任务执行结束");
    }
}
