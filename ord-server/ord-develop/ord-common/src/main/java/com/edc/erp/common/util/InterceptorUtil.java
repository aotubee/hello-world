package com.edc.erp.common.util;

import com.edc.plugins.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-12-11 14:47
 */
public class InterceptorUtil {

    public static boolean checkSystemMaintenance(String redisSystemMaintenanceStr) {
        if (Objects.isNull(redisSystemMaintenanceStr)) {
            return true;
        }
        String[] checkRuleArray = redisSystemMaintenanceStr.split("-");
        for (String rule : checkRuleArray) {
            LocalDateTime beginTime = DateUtils.parseTime(LocalDate.now() + " " + rule.split("~")[0]);
            LocalDateTime endTime = DateUtils.parseTime(LocalDate.now() + " " + rule.split("~")[1]);
            LocalDateTime nowTime = LocalDateTime.now();
            if (nowTime.isAfter(beginTime) && nowTime.isBefore(endTime)) {
                return false;
            }
        }
        return true;
    }
}
