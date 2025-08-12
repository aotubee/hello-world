package com.edc.erp.directly.dirdeliveryorder.job;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.DeliveryOrderEnum;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.SalvageDelivPondStatusEnum;
import com.edc.erp.common.enumeration.SalvageDeliveryOrderWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.WarningBusinessTypeEnum;
import com.edc.erp.common.service.WarningService;
import com.edc.erp.common.util.NumberUtil;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirSalvageDelivPond;
import com.edc.erp.directly.dirdeliveryorder.handle.DirDeliveryOrderSalvageHandle;
import com.edc.erp.directly.dirdeliveryorder.model.in.ExecuteDirSalvageDeliveryOrderIn;
import com.edc.erp.directly.dirdeliveryorder.model.out.DirNoSalvageDeliveryOrderOut;
import com.edc.erp.directly.dirdeliveryorder.model.out.OrdDirSalvageDelivPondDetailOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirSalvageDelivPondDetailService;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirSalvageDelivPondService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.utils.DateUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 配销捞单定时器
 */
@Component
@Slf4j
public class DirSalvageDeliveryOrderScheduler {

    @Autowired
    RedisService redisService;

    @Autowired
    TaskLockUtils taskLockUtils;

    /**
     * 关闭截单定时器的值
     */
    private final static String CLOSE_CUT_ORDER_KEY = "OPEN";

    @Autowired
    private OrdDirSalvageDelivPondDetailService ordDirSalvageDelivPondDetailService;

    @Autowired
    private OrdDirSalvageDelivPondService ordDirSalvageDelivPondService;

    @Autowired
    private DirDeliveryOrderSalvageHandle dirDeliveryOrderSalvageHandle;

    @Autowired
    private OrdDirDeliveryService ordDirDeliveryService;

    @Autowired
    private WarningService warningService;


    /**
     * 捞单定时器
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void salvageDeliveryOrder() {
        String closeCutOrderSwitch = redisService.get(SystemConstant.DIR_CLOSE_SALVAGE_DELIV_POND_SWITCH_KEY);
        if (CLOSE_CUT_ORDER_KEY.equals(closeCutOrderSwitch)) {
            log.info("直营配货单捞单定时器开关未打开......");
            return;
        }
        if (taskLockUtils.lock(SystemConstant.DIR_CLOSE_SALVAGE_DELIV_POND_SWITCH, 60 * 3 * 1000)) {
            log.info("开始执行直营配货单捞单任务");
            try {
                LocalDateTime nowTime = LocalDateTime.now();
                String beginTime = LocalDate.now() + " 00:00:00";
                String endTime = LocalDate.now() + " 23:59:59";
                ExecuteDirSalvageDeliveryOrderIn executeDirSalvageDeliveryOrderIn = new ExecuteDirSalvageDeliveryOrderIn();
                executeDirSalvageDeliveryOrderIn.setBeginTruncationDateTime(beginTime);
                executeDirSalvageDeliveryOrderIn.setEndTruncationDateTime(endTime);
                executeDirSalvageDeliveryOrderIn.setExecuteTime(nowTime);
                List<String> salvageStatusList = Lists.newArrayList();
                salvageStatusList.add(SalvageDelivPondStatusEnum.WAIT_EXECUTION.getKey());
                salvageStatusList.add(SalvageDelivPondStatusEnum.EXECUTION_ING.getKey());
                executeDirSalvageDeliveryOrderIn.setSalvageStatusList(salvageStatusList);
                List<OrdDirSalvageDelivPond> dirSalvageDeliveryPondList = ordDirSalvageDelivPondService.findListForAutoSalvage(executeDirSalvageDeliveryOrderIn);
                if (CollectionUtils.isEmpty(dirSalvageDeliveryPondList)) {
                    return;
                }
                Map<String, List<OrdDirSalvageDelivPond>> map = dirSalvageDeliveryPondList.stream().collect(Collectors.groupingBy(OrdDirSalvageDelivPond::getBizOrgCode));
                map.forEach((bizOrgCode, ordDirSalvageDelivPonds) -> {
                    ordDirSalvageDelivPonds.forEach(ordDirSalvageDelivPond -> {
                        // 根据捞单池id查询捞单池明细
                        List<OrdDirSalvageDelivPondDetailOut> ordDirSalvageDelivPondDetailOutList = ordDirSalvageDelivPondDetailService.findListBySalvagePondId(ordDirSalvageDelivPond.getId());
                        List<Long> deliveryOrderIdList = ordDirSalvageDelivPondDetailOutList.stream().map(OrdDirSalvageDelivPondDetailOut::getDeliveryOrderId).collect(Collectors.toList());
                        // 本次捞单待执行的配货单数量
                        int pendingStatusCount = ordDirDeliveryService.countByStatusAndIdList(DeliveryOrderEnum.PENDING.getKey(), deliveryOrderIdList);
                        // 本次捞单已经作废的数量
                        int invalidStatusCount = ordDirDeliveryService.countByStatusAndIdList(DeliveryOrderEnum.INVALID.getKey(), deliveryOrderIdList);
                        // 本捞单记录需要执行的总数量
                        int maxCount = ordDirSalvageDelivPondDetailOutList.size() - invalidStatusCount;
                        // 已执行占库存总数
                        int executedCount = maxCount - pendingStatusCount;
                        // 更新占库存的配销单，并记录日志，返回本次定时器执行成功的数量
                        int newExecutionTotal = pendingStatusCount == 0 ? pendingStatusCount : dirDeliveryOrderSalvageHandle.handleSalvageDelivPondDetailList(bizOrgCode, SystemConstant.SYSTEM_USER, true, ordDirSalvageDelivPondDetailOutList);
                        // 等待执行占库存总数
                        int waitExecutionTotal = maxCount - (newExecutionTotal + executedCount);
                        OrdDirSalvageDelivPond updateOrdDirSalvageDelivPond = this.initUpdateOrdDirSalvageDelivPond(ordDirSalvageDelivPond,
                                newExecutionTotal + executedCount, waitExecutionTotal);
                        ordDirSalvageDelivPondService.updateStatus(updateOrdDirSalvageDelivPond);
                    });
                });
            } catch (Exception e) {
                log.error("执行直营配货单捞单任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIR_CLOSE_SALVAGE_DELIV_POND_SWITCH);
            }
        } else {
            log.info("直营配货单捞单定时器定时器锁还未释放：{}", SystemConstant.DIR_CLOSE_SALVAGE_DELIV_POND_SWITCH);
        }
        log.info("直营配货单捞单任务执行结束");
    }

    private OrdDirSalvageDelivPond initUpdateOrdDirSalvageDelivPond(OrdDirSalvageDelivPond ordDirSalvageDelivPond, int executionTotal, int waitExecutionTotal) {
        String salvageStatus = 0 == waitExecutionTotal ? SalvageDelivPondStatusEnum.COMPLETED.getKey() : SalvageDelivPondStatusEnum.EXECUTION_ING.getKey();
        OrdDirSalvageDelivPond updateOrdDirSalvageDelivPond = new OrdDirSalvageDelivPond();
        updateOrdDirSalvageDelivPond.setId(ordDirSalvageDelivPond.getId());
        updateOrdDirSalvageDelivPond.setWaitExecutionTotal(waitExecutionTotal);
        updateOrdDirSalvageDelivPond.setExecutionTotal(executionTotal);
        updateOrdDirSalvageDelivPond.setSalvageStatus(salvageStatus);
        updateOrdDirSalvageDelivPond.setUpdater(SystemConstant.SYSTEM_USER);
        updateOrdDirSalvageDelivPond.setUpdateTime(LocalDateTime.now());
        return updateOrdDirSalvageDelivPond;
    }

    @Scheduled(cron = "0 0/30 * * * ?")
    public void warningDirNoAutoSalvageDeliveryOrder() {
        String closeCutOrderSwitch = redisService.get(SystemConstant.DIR_CLOSE_WARNING_NO_AUTO_SALVAGE_SWITCH_KEY);
        if (CLOSE_CUT_ORDER_KEY.equals(closeCutOrderSwitch)) {
            log.info("直营预警未执行捞单定时器开关未打开......");
            return;
        }
        if (taskLockUtils.lock(SystemConstant.DIR_WARNING_NO_AUTO_SALVAGE_SWITCH, 60 * 3 * 1000)) {
            log.info("开始执行直营预警未执行捞单任务");
            try {
                for (String bizOrgCode : SystemConstant.BIZORGCODES) {
                    String beginTime = LocalDate.now() + " 00:00:00";
                    List<OrdDirSalvageDelivPond> salvageList = ordDirSalvageDelivPondService.findNeedRepeatExecuteSalvageList(SalvageDelivPondStatusEnum.COMPLETED.getKey(), beginTime, bizOrgCode);
                    if (CollectionUtils.isEmpty(salvageList)) {
                        continue;
                    }
                    salvageList.forEach(ordDisSalvageDelivPond -> {
                        String warningCountKey = SystemConstant.WARNING_COUNT_KEY + bizOrgCode + SystemConstant.COLON
                                + ordDisSalvageDelivPond.getOrderTypeConfigId() + SystemConstant.WAIT + ordDisSalvageDelivPond.getTruncationDateTime();
                        Integer warningCount = NumberUtil.INTEGER_ONE;
                        if (redisService.hasKey(warningCountKey)) {
                            warningCount = Integer.valueOf(redisService.get(warningCountKey));
                        }
                        if (warningCount.compareTo(NumberUtil.INTEGER_FIVE) != -1) {
                            return;
                        }
                        List<Long> deliveryOrderIdList = ordDirSalvageDelivPondDetailService.findDeliveryOrderIdListBySalvagePondId(ordDisSalvageDelivPond.getId());
                        List<DirNoSalvageDeliveryOrderOut> noSalvageDeliveryOrderList = ordDirDeliveryService.findNoSalvageDeliveryOrderList(deliveryOrderIdList, bizOrgCode);
                        if (CollectionUtils.isEmpty(noSalvageDeliveryOrderList)) {
                            return;
                        }
                        String warningContent = noSalvageDeliveryOrderList.stream().map(DirNoSalvageDeliveryOrderOut::getDeliveryOrderNo).collect(Collectors.joining(SystemConstant.COMMA));
                        // 发预警消息
                        Duration duration = Duration.between(ordDisSalvageDelivPond.getTruncationDateTime(), LocalDateTime.now());
                        long minutes = duration.toMinutes();
                        String timeDifference = duration.toHours() < NumberUtil.INTEGER_ONE ? duration.toMinutes() + "分钟" : minutes / 60 + "小时" + minutes % 60 + "分钟";
                        String msg = MessageFormat.format(SalvageDeliveryOrderWarningTypeEnum.NO_AUTO_SALVAGE.getErrorMessage(),
                                OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode), warningContent, DateUtils.format(ordDisSalvageDelivPond.getTruncationDateTime()),
                                timeDifference, warningCount);
                        warningService.pushWarningMessage(WarningBusinessTypeEnum.NO_AUTO_SALVAGE.getBusinessType(), SalvageDeliveryOrderWarningTypeEnum.NO_AUTO_SALVAGE.getType(),
                                msg, null, null, bizOrgCode);
                        warningCount++;
                        redisService.set(warningCountKey, warningCount, NumberUtil.INTEGER_ONE, TimeUnit.DAYS);
                    });
                }
            } catch (Exception e) {
                log.error("执行直营预警未执行捞单任务异常：", e);
            } finally {
                taskLockUtils.unlock(SystemConstant.DIR_WARNING_NO_AUTO_SALVAGE_SWITCH);
            }
        } else {
            log.info("直营预警未执行捞单定时器定时器锁还未释放：{}", SystemConstant.DIR_WARNING_NO_AUTO_SALVAGE_SWITCH);
        }
        log.info("直营预警未执行捞单任务执行结束");
    }
}
