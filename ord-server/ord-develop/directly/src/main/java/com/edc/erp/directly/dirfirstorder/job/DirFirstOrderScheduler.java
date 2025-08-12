package com.edc.erp.directly.dirfirstorder.job;

import com.alibaba.fastjson.JSONObject;
import com.edc.erp.common.async.service.AsyncPushTaskService;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.FirstOrderStatusEnum;
import com.edc.erp.common.enumeration.OrdLogTypeEnum;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.constant.DirSystemConstant;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import com.edc.erp.directly.dirfirstorder.mapper.OrdDirOrderFirstMapper;
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
 * 直营铺货单定时器
 *
 * @author weichao
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DirFirstOrderScheduler {

    private final OrdDirOrderFirstMapper ordDirOrderFirstMapper;

    private final TaskLockUtils taskLockUtils;

    private final AsyncPushTaskService asyncPushTaskService;

    private final AsyncLogService asyncLogService;

    @Qualifier("dirFirstToDeliverySender")
    private final MessageSender dirFirstToDeliverySender;


    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoDirFirstOrderIsEffectiveJob() {
        log.info("开始执行直营铺货单生效定时任务");
        if (taskLockUtils.lock(SystemConstant.DIR_FIRST_ORDER_SWITCH, SystemConstant.FIRST_LOCK_TIME_OUT)) {
            try {
                this.autoDirFirstOrderIsEffective();
            } catch (Exception e) {
                log.error("执行直营铺货单任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIR_FIRST_ORDER_SWITCH);
            }
        } else {
            log.info("定时器锁还未释放：{}", SystemConstant.DIR_FIRST_ORDER_SWITCH);
        }
        log.info("直营铺货单生效定时任务结束");

    }

    @Transactional(rollbackFor = Exception.class)
    public void autoDirFirstOrderIsEffective() {
        OrdDirOrderFirst ordDirOrderFirst = new OrdDirOrderFirst();
        ordDirOrderFirst.setIsDelete(NumberUtil.INTEGER_ZERO);
        ordDirOrderFirst.setFirstOrderStatus(FirstOrderStatusEnum.APPROVED.getCode());
        ordDirOrderFirst.setEffectiveTime(LocalDateTime.now());
        List<OrdDirOrderFirst> ordDirOrderFirsts = ordDirOrderFirstMapper.selectByEffectiveTime(ordDirOrderFirst);
        Optional.ofNullable(ordDirOrderFirsts).orElse(new ArrayList<>()).forEach(item -> {
            item = ordDirOrderFirstMapper.selectByPrimaryKey(item.getId());
            if (!FirstOrderStatusEnum.APPROVED.getCode().equals(item.getFirstOrderStatus())) {
                return;
            }
            item.setUpdater(SystemConstant.SYSTEM_USER);
//            item.setFirstOrderStatus(FirstOrderStatusEnum.EXECUTED.getCode());
//            ordDirOrderFirstMapper.updateByPrimaryKey(item);
//            asyncPushTaskService.submit(AsyncTaskConstant.Type.DIR_FIRST_TO_DELIVERY, JSONObject.toJSONString(item), item.getBizOrgCode(), item.getFirstOrderNo());
            SendResponse sendResponse = dirFirstToDeliverySender.sendSync(JSONObject.toJSONString(item).getBytes(),System.currentTimeMillis() + DirSystemConstant.MQ_DELAY_TIME);
            log.info("直营铺货单转配货单{}消息ID---{}", item.getFirstOrderNo(), sendResponse.getMessageId());
            //生效日志
            BusinessLog businessLog = new BusinessLog(SystemConstant.SYSTEM_CODE, OrdLogTypeEnum.FIRST_ORDER_GOODS_EXECUTED.getCode(),
                    String.valueOf(item.getId()), OrdLogTypeEnum.DIR_FIRST_ORDER_GOODS.getCode(), OrdLogTypeEnum.FIRST_ORDER_GOODS_EXECUTED.getName(), new Date(),
                    item.getUpdater());
            asyncLogService.sendAsyncSaveLogByMq(businessLog);
            log.info("{}直营铺货单已生效", item.getFirstOrderNo());
        });
    }

}
