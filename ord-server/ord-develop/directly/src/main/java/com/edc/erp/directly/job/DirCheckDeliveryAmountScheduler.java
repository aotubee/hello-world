package com.edc.erp.directly.job;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.warning.StoreOrderWarningTypeEnum;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 配货单金额过大提醒定时器
 * @author lishaobo
 */
@Component
@Slf4j
public class DirCheckDeliveryAmountScheduler {

    @Autowired
    RedisService redisService;

    @Autowired
    TaskLockUtils taskLockUtils;
    /**
     * 关闭查询配货单金额过大定时器的值
     */
    private final static String CLOSE_DELIVERY_AMOUNT_SIMILARITY_KEY = "OPEN";

    @Autowired
    private OrdDirDeliveryService ordDirDeliveryService;

    /**
     * 06:30未正常拆单定时器
     */
    @Scheduled(cron = "0 10 7 * * ?")
    public void sendMessageForNoTransferOrder() {
        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_DELIVERY_AMOUNT_SIMILARITY_SWITCH_KEY);
        if (CLOSE_DELIVERY_AMOUNT_SIMILARITY_KEY.equals(closeOrderSwitch)) {
            log.info("查询配货单金额过大定时器开关未打开......");
            return;
        }
        log.info("开始执行查询合作经营06:30配货单金额过大任务");
        String key = SystemConstant.DELIVERY_AMOUNT_SIMILARITY_SWITCH + StoreOrderWarningTypeEnum.DIRECTLY.getCode();
        if (taskLockUtils.lock(key, 60 * 3 * 1000)) {
            try {
                for (String bizOrgCode : SystemConstant.BIZORGCODES) {
                    String createTimeBegin = LocalDate.now() + " 00:00:00";
                    String createTimeEnd = LocalDate.now() + " 23:59:59";
                    ordDirDeliveryService.checkDeliveryAmountSimilarity(createTimeBegin, createTimeEnd, bizOrgCode);
                }
            } catch (Exception e) {
                log.error("执行查询合作经营06:30配货单金额过大异常：", e);
            } finally {
                taskLockUtils.unlock(key);
            }
        } else {
            log.info("查询合作经营06:30配货单金额过大定时器定时器锁还未释放：{}", key);
        }
        log.info("查询合作经营06:30配货单金额过大任务执行结束");
    }
}
