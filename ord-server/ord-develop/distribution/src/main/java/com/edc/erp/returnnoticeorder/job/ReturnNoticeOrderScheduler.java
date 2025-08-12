package com.edc.erp.returnnoticeorder.job;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.returnnoticeorder.entity.OrdDisReturnNotice;
import com.edc.erp.returnnoticeorder.enumeration.OrdDisReturnNoticeStatusEnum;
import com.edc.erp.returnnoticeorder.mapper.OrdDisReturnNoticeMapper;
import com.edc.plugins.redis.lock.TaskLockUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 退货通知单定时器
 * @author lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnNoticeOrderScheduler {

    private final OrdDisReturnNoticeMapper ordDisReturnNoticeMapper;

    private final TaskLockUtils taskLockUtils;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void autoReturnNoticeIsEffectiveJob(){
        log.info("开始执行配销退货通知单生效定时任务");
        if (taskLockUtils.lock(SystemConstant.DIS_RETURN_NOTICE_ORDER_SWITCH, SystemConstant.LOCK_TIME_OUT)) {
            try {
        this.autoReturnNoticeIsEffective();
            } catch (Exception e) {
                log.error("执行配销退货通知单任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIS_RETURN_NOTICE_ORDER_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", SystemConstant.DIS_RETURN_NOTICE_ORDER_SWITCH);
        }
        log.info("配销退货通知单生效定时任务结束");

    }

    public void autoReturnNoticeIsEffective() {
        OrdDisReturnNotice returnNotice = new OrdDisReturnNotice();
        returnNotice.setIsDelete(NumberUtil.INTEGER_ZERO);
        returnNotice.setStatus(OrdDisReturnNoticeStatusEnum.APPROVED.getKey());
        returnNotice.setTakeEffectTime(LocalDateTime.now());
        List<OrdDisReturnNotice> ordDisReturnNotices = ordDisReturnNoticeMapper.selectTakeEffectTime(returnNotice);
        Optional.ofNullable(ordDisReturnNotices).orElse(new ArrayList<>()).forEach(item->{
           /* if (isBetween(item.getTakeEffectTime(), item.getReturnDeadline())) {*/
                item.setStatus(OrdDisReturnNoticeStatusEnum.PROCESSED.getKey());
                ordDisReturnNoticeMapper.updateByPrimaryKey(item);
                log.info("{}配销退货通知单已生效",item.getReturnNoticeOrderNo());
            //}
        });
    }

    /**
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return true在时间段内，false不在时间段内
     */
    private boolean isBetween(LocalDateTime beginTime, LocalDateTime endTime) {
        //获取当前时间
        LocalDateTime now = LocalDateTime.now();
        boolean flag = false;
        if (now.isAfter(beginTime) && now.isBefore(endTime)) {
            flag = true;
        }
        return flag;
    }
}
