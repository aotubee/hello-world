package com.edc.erp.disfirstorder.job;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.AsyncTaskConstant;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.FirstOrderStatusEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.constant.DisSystemConstant;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import com.edc.erp.disfirstorder.mapper.OrdDisOrderFirstMapper;
import com.edc.plugins.jms.MessageSender;
import com.edc.plugins.jms.entity.SendResponse;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.sdk.log.dto.BusinessLog;
import com.edc.sdk.log.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 配销铺货单定时器
 *
 * @author weichao
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DisFirstOrderScheduler {

    private final OrdDisOrderFirstMapper ordDisOrderFirstMapper;

    private final TaskLockUtils taskLockUtils;

    private final AsyncPushTaskService asyncPushTaskService;

    private final AsyncLogService asyncLogService;

    @Qualifier("disFirstToDeliverySender")
    private final MessageSender disFirstToDeliverySender;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoDisFirstOrderIsEffectiveJob() {
        log.info("开始执行配销铺货单生效定时任务");
        if (taskLockUtils.lock(SystemConstant.DIS_FIRST_ORDER_SWITCH, SystemConstant.FIRST_LOCK_TIME_OUT)) {
            try {
                this.autoDisFirstOrderIsEffective();
            } catch (Exception e) {
                log.error("执行配销铺货单任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIS_FIRST_ORDER_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", SystemConstant.DIS_FIRST_ORDER_SWITCH);
        }
        log.info("配销铺货单生效定时任务结束");

    }

    @Transactional(rollbackFor = Exception.class)
    public void autoDisFirstOrderIsEffective() {
        OrdDisOrderFirst ordDisOrderFirst = new OrdDisOrderFirst();
        ordDisOrderFirst.setIsDelete(NumberUtil.INTEGER_ZERO);
        ordDisOrderFirst.setFirstOrderStatus(FirstOrderStatusEnum.APPROVED.getCode());
        ordDisOrderFirst.setEffectiveTime(LocalDateTime.now());
        List<OrdDisOrderFirst> ordDisOrderFirsts = ordDisOrderFirstMapper.selectByEffectiveTime(ordDisOrderFirst);
        Optional.ofNullable(ordDisOrderFirsts).orElse(new ArrayList<>()).forEach(item -> {
            item = ordDisOrderFirstMapper.selectByPrimaryKey(item.getId());
            if (!FirstOrderStatusEnum.APPROVED.getCode().equals(item.getFirstOrderStatus())) {
                return;
            }
            item.setUpdater(SystemConstant.SYSTEM_USER);
//            item.setFirstOrderStatus(FirstOrderStatusEnum.EXECUTED.getCode());
//            ordDisOrderFirstMapper.updateByPrimaryKey(item);
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIS_FIRST_TO_DELIVERY, JSONObject.toJSONString(item), item.getBizOrgCode(), item.getFirstOrderNo());
            SendResponse sendResponse = disFirstToDeliverySender.sendSync(JSONObject.toJSONString(item).getBytes(), System.currentTimeMillis() + DisSystemConstant.MQ_DELAY_TIME);
            log.info("铺货单转配销单{}消息ID---{}", item.getFirstOrderNo(), sendResponse.getMessageId());
//            log.info("{}配销铺货单已生效", item.getFirstOrderNo());
        });
    }

}
