package com.edc.erp.directly.job;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.warning.OrderWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.StoreOrderWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.WarningBusinessTypeEnum;
import com.edc.erp.common.service.WarningService;
import com.edc.erp.directly.dirdeliveryorder.model.out.DirNoAuditDeliveryOrderInfoOut;
import com.edc.erp.directly.dirdeliveryorder.service.OrdDirDeliveryService;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import com.edc.plugins.utils.DateUtils;
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
import java.util.stream.Collectors;

/**
 * 配货单未审核提醒定时器
 *
 * @author lishaobo
 */
@Component
@Slf4j
public class DirCheckNotAuditDeliveryScheduler {

    @Autowired
    RedisService redisService;

    @Autowired
    TaskLockUtils taskLockUtils;
    /**
     * 关闭查询未正常审核定时器的值
     */
    private final static String CLOSE_AUDIT_DELIVERY_ERROR_KEY = "OPEN";

    @Autowired
    private OrdDirDeliveryService deliveryService;

    @Autowired
    private WarningService warningService;

    /**
     * 06:30未正常审核定时器
     */
//    @Scheduled(cron = "0 00,10 8 * * ?")
//    public void sendMessageForNoTransferOrder() {
//        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_AUDIT_DELIVERY_ERROR_SWITCH_KEY);
//        if (CLOSE_AUDIT_DELIVERY_ERROR_KEY.equals(closeOrderSwitch)) {
//            log.info("查询未正常审核门店定时器开关未打开......");
//            return;
//        }
//        log.info("开始执行查询合作经营06:30未正常审核任务");
//        String key = SystemConstant.AUDIT_DELIVERY_ERROR_SWITCH + StoreOrderWarningTypeEnum.DIRECTLY.getCode();
//        if (taskLockUtils.lock(key, 60 * 3 * 1000)) {
//            try {
//                for (String bizOrgCode : SystemConstant.BIZORGCODES) {
//                    String truncationDateTime = LocalDate.now() + " 07:30:00";
//                    String createTimeBegin = LocalDate.now() + " 00:00:00";
//                    String createTimeEnd = LocalDate.now() + " 23:59:59";
//                    // 查询到达审核时间未审核的配货单号；
//                    List<String> orderNoList = deliveryService.findNotAuditDeliveryOrderList(createTimeBegin, createTimeEnd, bizOrgCode);
//                    if (CollectionUtils.isEmpty(orderNoList)) {
//                        log.info("{}合作经营未正常审核定时器未查到数据", OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode));
//                        continue;
//                    }
//                    String msg = MessageFormat.format(OrderWarningTypeEnum.DELIVERY_ORDER_NOT_AUDIT.getErrorMessage(), StoreOrderWarningTypeEnum.DIRECTLY.getName(), WarningBusinessTypeEnum.DIR_DELIVERY_ORDER.getName(),
//                            WarningBusinessTypeEnum.DIR_DELIVERY_ORDER.getName(), truncationDateTime, String.join(SystemConstant.COMMA, orderNoList));
//                    warningService.pushWarningMessage(erpOrdTopic, WarningBusinessTypeEnum.DIR_DELIVERY_ORDER.getBusinessType(), OrderWarningTypeEnum.DELIVERY_ORDER_NOT_AUDIT.getType(),
//                            msg, null, null, bizOrgCode);
//                }
//            } catch (Exception e) {
//                log.error("执行查询合作经营06:30未正常审核任务异常：", e);
//            } finally {
//                taskLockUtils.unlock(key);
//            }
//        } else {
//            log.info("查询合作经营06:30未正常审核定时器定时器锁还未释放：{}", key);
//        }
//        log.info("查询合作经营06:30未正常审核任务执行结束");
//    }
    @Scheduled(cron = "0 0/10 * * * ?")
    public void sendMessageForNoTransferOrder() {
        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_AUDIT_DELIVERY_ERROR_SWITCH_KEY);
        if (CLOSE_AUDIT_DELIVERY_ERROR_KEY.equals(closeOrderSwitch)) {
            log.info("直营配货单未正常审核定时器开关未打开.......");
            return;
        }
        log.info("开始执行直营配货单未正常审核任务");
        String key = SystemConstant.AUDIT_DELIVERY_ERROR_SWITCH + StoreOrderWarningTypeEnum.DIRECTLY.getCode();
        if (taskLockUtils.lock(key, 60 * 8 * 1000)) {
            try {
                for (String bizOrgCode : SystemConstant.BIZORGCODES) {
                    try {
                        String createTimeBegin = LocalDate.now() + " 00:00:00";
                        String createTimeEnd = LocalDate.now() + " 23:59:59";
                        // 查询到达审核时间未审核的配货单号；
                        List<DirNoAuditDeliveryOrderInfoOut> orderNoList = deliveryService.findNoAuditDeliveryOrderList(createTimeBegin, createTimeEnd, bizOrgCode);
                        if (CollectionUtils.isEmpty(orderNoList)) {
                            log.info("{}合作经营未正常审核定时器未查到数据", OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode));
                            continue;
                        }
                        Map<String, List<DirNoAuditDeliveryOrderInfoOut>> truncationDeliveryOrderMap = orderNoList.stream()
                                .collect(Collectors.groupingBy(DirNoAuditDeliveryOrderInfoOut::getTruncationDateTime));
                        truncationDeliveryOrderMap.forEach((truncationDateTime, noAuditDeliveryOrderInfoOuts) -> {
                            try {
                                Duration duration = Duration.between(DateUtils.parseTime(truncationDateTime), LocalDateTime.now());
                                if (duration.toMinutes() > SystemConstant.DIR_DELIVERY_NO_AUDIT_MAX_OVER_TIME) {
                                    String deliveryOrderStr = noAuditDeliveryOrderInfoOuts.stream().map(DirNoAuditDeliveryOrderInfoOut::getDeliveryOrderNo).collect(Collectors.joining(SystemConstant.COMMA));
                                    String msg = MessageFormat.format(OrderWarningTypeEnum.DELIVERY_ORDER_NOT_AUDIT.getErrorMessage(),
                                            StoreOrderWarningTypeEnum.DIRECTLY.getName(), WarningBusinessTypeEnum.DIR_DELIVERY_ORDER.getName(),
                                            WarningBusinessTypeEnum.DIR_DELIVERY_ORDER.getName(), truncationDateTime, String.join(SystemConstant.COMMA, deliveryOrderStr));
                                    warningService.pushWarningMessage(WarningBusinessTypeEnum.DIR_DELIVERY_ORDER.getBusinessType(), OrderWarningTypeEnum.DELIVERY_ORDER_NOT_AUDIT.getType(),
                                            msg, null, null, bizOrgCode);
                                }
                            } catch (Exception e) {
                                log.error("执行组织{}截单时间{}直营配货单未正常审核任务异常：", bizOrgCode, truncationDateTime, e);
                            }
                        });
                    } catch (Exception e) {
                        log.error("执行组织{}直营配货单未正常审核任务异常：", bizOrgCode, e);
                    }
                }
            } catch (Exception e) {
                log.error("执行直营配货单未正常审核任务异常：", e);
            } finally {
                taskLockUtils.unlock(key);
            }
        } else {
            log.info("直营配货单未正常审核定时器定时器锁还未释放：{}", key);
        }
        log.info("直营配货单未正常审核任务执行结束");
    }

}
