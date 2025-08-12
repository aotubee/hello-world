package com.edc.erp.directly.returnnoticeorder.job;

import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.returnnoticeorder.entity.OrdDirReturnNotice;
import com.edc.erp.directly.returnnoticeorder.enumeration.OrdReturnNoticeStatusEnum;
import com.edc.erp.directly.returnnoticeorder.mapper.OrdDirReturnNoticeMapper;
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
 * @return: 退货通知单定时器
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DirReturnNoticeOrderScheduler {

    private final OrdDirReturnNoticeMapper ordDisReturnNoticeMapper;

    private final TaskLockUtils taskLockUtils;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void autoReturnNoticeIsEffectiveJob() {
        log.info("开始执行直营退货通知单生效定时任务");
        if (taskLockUtils.lock(SystemConstant.DIR_RETURN_NOTICE_ORDER_SWITCH, SystemConstant.LOCK_TIME_OUT)) {
            try {
                this.autoReturnNoticeIsEffective();
            } catch (Exception e) {
                log.error("执行直营退货通知单任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIR_RETURN_NOTICE_ORDER_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", SystemConstant.DIR_RETURN_NOTICE_ORDER_SWITCH);
        }
        log.info("直营退货通知单生效定时任务结束");

    }

    public void autoReturnNoticeIsEffective() {
        OrdDirReturnNotice returnNotice = new OrdDirReturnNotice();
        returnNotice.setIsDelete(NumberUtil.INTEGER_ZERO);
        returnNotice.setStatus(OrdReturnNoticeStatusEnum.APPROVED.getKey());
        returnNotice.setTakeEffectTime(LocalDateTime.now());
        List<OrdDirReturnNotice> ordDirReturnNotices = ordDisReturnNoticeMapper.selectTakeEffectTime(returnNotice);
        Optional.ofNullable(ordDirReturnNotices).orElse(new ArrayList<>()).forEach(item -> {
            /*if (isBetween(item.getTakeEffectTime(), item.getReturnDeadline())) {*/
            item.setStatus(OrdReturnNoticeStatusEnum.PROCESSED.getKey());
            ordDisReturnNoticeMapper.updateByPrimaryKey(item);
            log.info("{}直营退货通知单已生效", item.getReturnNoticeOrderNo());
            // }
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
