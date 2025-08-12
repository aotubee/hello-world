package com.edc.erp.directly.job;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.enumeration.OrgCodeConvertEnum;
import com.edc.erp.common.enumeration.warning.OrderWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.StoreOrderWarningTypeEnum;
import com.edc.erp.common.enumeration.warning.WarningBusinessTypeEnum;
import com.edc.erp.common.service.WarningService;
import com.edc.erp.directly.handle.DirRequestOrderHandle;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.lock.TaskLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * 要货单未拆配销单提醒定时器
 */
@Component
@Slf4j
public class DirCheckNotDeliveryOrderScheduler {

    @Autowired
    RedisService redisService;

    @Autowired
    TaskLockUtils taskLockUtils;
    /**
     * 关闭查询未正常拆单定时器的值
     */
    private final static String CLOSE_TO_DELIVERY_ORDER_ERROR_KEY = "OPEN";

    @Autowired
    private DirRequestOrderHandle requestOrderHandle;

    @Autowired
    private WarningService warningService;

    /**
     * 06:30未正常拆单定时器
     */
    @Scheduled(cron = "0 10,20 7 * * ?")
    public void sendMessageForNoTransferOrder() {
        String closeOrderSwitch = redisService.get(SystemConstant.CLOSE_TO_DELIVERY_ORDER_ERROR_SWITCH_KEY);
        if (CLOSE_TO_DELIVERY_ORDER_ERROR_KEY.equals(closeOrderSwitch)) {
            log.info("查询未正常拆单门店定时器开关未打开......");
            return;
        }
        log.info("开始执行查询合作经营06:30未正常拆单任务");
        String key = SystemConstant.TO_DELIVERY_ORDER_ERROR_SWITCH + StoreOrderWarningTypeEnum.DIRECTLY.getCode();
        if (taskLockUtils.lock(key, 60 * 3 * 1000)) {
            try {
                String truncationDateTime = LocalDate.now() + " 06:30:00";
                String createTimeBegin = LocalDate.now() + " 00:00:00";
                String createTimeEnd = LocalDate.now() + " 23:59:59";
                for (String bizOrgCode : SystemConstant.BIZORGCODES) {
                    // 查询到达截单时间未拆单的要货单号；
                    List<String> orderNoList = requestOrderHandle.findNotToDeliveryOrderList(createTimeBegin, createTimeEnd, bizOrgCode);
                    if (CollectionUtils.isEmpty(orderNoList)) {
                        log.info("{}合作经营未正常拆单定时器未查到数据", OrgCodeConvertEnum.getNameByBizOrgCode(bizOrgCode));
                        continue;
                    }
                    String msg = MessageFormat.format(OrderWarningTypeEnum.NOT_TO_DELIVERY_ORDER.getErrorMessage(), StoreOrderWarningTypeEnum.DIRECTLY.getName(), WarningBusinessTypeEnum.DIR_REQUEST_ORDER.getName(),
                            WarningBusinessTypeEnum.DIR_REQUEST_ORDER.getName(), truncationDateTime, String.join(SystemConstant.COMMA, orderNoList));
                    warningService.pushWarningMessage(WarningBusinessTypeEnum.DIR_REQUEST_ORDER.getBusinessType(), OrderWarningTypeEnum.NOT_TO_DELIVERY_ORDER.getType(),
                            msg, null, null, bizOrgCode);
                }
            } catch (Exception e) {
                log.error("执行查询合作经营06:30未正常拆单任务异常：", e);
            } finally {
                taskLockUtils.unlock(key);
            }
        } else {
            log.info("查询合作经营06:30未正常拆单定时器定时器锁还未释放：{}", key);
        }
        log.info("查询合作经营06:30未正常拆单任务执行结束");
    }
}
