package com.edc.erp.presale.job;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.presale.entity.OrdDisPresaleAssetsDetail;
import com.edc.erp.presale.enumeration.OrdDisPresaleAssetsStatusEnum;
import com.edc.erp.presale.service.OrdDisPresaleAssetsDetailService;
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
public class DisPresaleAssetsScheduler {

    private final RedisService redisService;

    private final TaskLockUtils taskLockUtils;

    private final OrdDisPresaleAssetsDetailService ordDisPresaleAssetsDetailService;

    /**
     * 关闭自动收货定时器开关值
     */
    private final static String CLOSE_AUTO_TAKE_DELIVERY_KEY = "OPEN";


    /**
     * 自动收货定时器
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoExecutedPresaleActivity() {
        String closeAutoExecutedPresaleActivitySwitch = redisService.get(DisSystemConstant.DIS_CLOSE_PRESALE_ASSETS_SWITCH_KEY);
        if (CLOSE_AUTO_TAKE_DELIVERY_KEY.equals(closeAutoExecutedPresaleActivitySwitch)) {
            log.info("预售资产状态变更开关未打开......");
            return;
        }
        log.info("开始执行预售资产状态变更任务");
        if (taskLockUtils.lock(DisSystemConstant.DIS_PRESALE_ASSETS_SWITCH, 2 * 60 * 1000)) {
            try {
                while (true) {
                    taskLockUtils.unlock(DisSystemConstant.DIS_PRESALE_ASSETS_SWITCH);
                    taskLockUtils.lock(DisSystemConstant.DIS_PRESALE_ASSETS_SWITCH, 2 * 60 * 1000);
                    List<Long> needUpdateIdList = ordDisPresaleAssetsDetailService.findNeedUpdateStatusAssetsDetailList();
                    if (CollectionUtils.isEmpty(needUpdateIdList)) {
                        Thread.sleep(60 * 1000);
                        continue;
                    }
                    needUpdateIdList.forEach(id -> {
                        try {
                            OrdDisPresaleAssetsDetail assetsDetail = ordDisPresaleAssetsDetailService.getOneById(id);
                            String status;
                            if (LocalDateTime.now().isBefore(assetsDetail.getBeginOrderDate())) {
                                status = OrdDisPresaleAssetsStatusEnum.NOT_STARTED.getKey();
                            } else if (LocalDateTime.now().isAfter(assetsDetail.getEndOrderDate())) {
                                status = OrdDisPresaleAssetsStatusEnum.EXPIRED.getKey();
                            } else {
                                status = OrdDisPresaleAssetsStatusEnum.ORDERING.getKey();
                            }
                            ordDisPresaleAssetsDetailService.updateStatus(id, status, assetsDetail.getStatus(), SystemConstant.SYSTEM_USER);
                        } catch (Exception e) {
                            log.error("预售资产状态变更{}异常", id, e);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("执行预售资产状态变更任务异常：", e);
            } finally {
                taskLockUtils.unlock(DisSystemConstant.DIS_PRESALE_ASSETS_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", DisSystemConstant.DIS_PRESALE_ASSETS_SWITCH);
        }
        log.info("预售资产状态变更任务执行结束");
    }

}
