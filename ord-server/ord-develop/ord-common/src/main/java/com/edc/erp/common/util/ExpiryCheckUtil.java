package com.edc.erp.common.util;

import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @ClassName ExpiryCheckUtil
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2025/3/26 15:20
 **/
@Slf4j
public class ExpiryCheckUtil {

    public static Response<LocalDateTime> checkExpiry(String expiry) {
        if (StringUtils.isBlank(expiry)) {
            return Response.error("效期码为空");
        }
        try {
            expiry = String.valueOf(LocalDate.now().getYear()).substring(0, 2) + expiry;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
            LocalDateTime localDateTime = LocalDateTime.parse(expiry, formatter);
            long passMonths = ChronoUnit.MONTHS.between(localDateTime, LocalDateTime.now());
            long futureMonths = ChronoUnit.MONTHS.between(LocalDateTime.now(), localDateTime);
            if (passMonths > 6 || futureMonths > 6) {
                return Response.error("效期时间大于6个月");
            }
            return Response.data(localDateTime);
        } catch (Exception e) {
            log.error("效期码{}转LocalDateTime异常", expiry, e);
            return Response.error("效期码格式不正确");
        }
    }
}
