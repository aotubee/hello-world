package com.edc.erp.job;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.warning.StoreOrderWarningTypeEnum;
import com.edc.erp.disdeliveryorder.service.OrdDisDeliveryService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 配销单金额过大提醒定时器
 * @author lishaobo
 */
@Component
@Slf4j
public class CheckDeliveryAmountScheduler {

    @Autowired
    RedisService redisService;

    @Autowired
    TaskLockUtils taskLockUtils;
    /**
     * 关闭查询配销单金额过大定时器的值
     */
    private final static String CLOSE_DELIVERY_AMOUNT_SIMILARITY_KEY = "OPEN";

    @Autowired
    private OrdDisDeliveryService ordDisDeliveryService;

    /**
     * 06:30配销单金额过大定时器
     */
    @Scheduled(cron = "0 10 7 * * ?")
//    @Scheduled(cron = "0 10,20,30 * * * ?")
    public void sendMessageForNoTransferOrder() {
        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_DELIVERY_AMOUNT_SIMILARITY_SWITCH_KEY);
        if (CLOSE_DELIVERY_AMOUNT_SIMILARITY_KEY.equals(closeOrderSwitch)) {
            log.info("查询配销单金额过大定时器开关未打开......");
            return;
        }
        log.info("开始执行查询特许加盟06:30配销单金额过大任务");
        String key = SystemConstant.DELIVERY_AMOUNT_SIMILARITY_SWITCH + StoreOrderWarningTypeEnum.FRANCHISE.getCode();
        if (taskLockUtils.lock(key, 60 * 3 * 1000)) {
            try {
                for (String bizOrgCode : SystemConstant.BIZORGCODES) {
                    String createTimeBegin = LocalDate.now() + " 00:00:00";
                    String createTimeEnd = LocalDate.now() + " 23:59:59";
                    ordDisDeliveryService.checkDeliveryAmountSimilarity(createTimeBegin, createTimeEnd, bizOrgCode);
                }
            } catch (Exception e) {
                log.error("执行查询特许加盟06:30配销单金额过大任务异常：", e);
            } finally {
                taskLockUtils.unlock(key);
            }
        } else {
            log.info("查询特许加盟06:30配销单金额过大定时器定时器锁还未释放：{}", key);
        }
        log.info("查询特许加盟06:30配销单金额过大任务执行结束");
    }
}
