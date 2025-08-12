package com.edc.erp.common.util;


import com.edc.erp.common.constant.SystemConstant;
import com.edc.plugins.common.exception.BusinessException;
import com.edc.plugins.redis.RedisService;
import com.edc.plugins.redis.unique.UniqueUtils;
import com.edc.plugins.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @description: 生成编码工具
 * @author fxw
 * @since 2022/09/17
 */
@Slf4j
public class CreateCodeUtil {


    /**
     * 获取业务编码
     *
     * @param typeCode    业务类型
     * @param uniqueUtils redis唯一自增序列号工具类
     * @return
     */
    public static String getCode(String typeCode, UniqueUtils uniqueUtils) {
        String prefixKey = "uniqueOrderNo:" + typeCode;
        Long redisIndex = uniqueUtils.getUniqueId(prefixKey);
        DecimalFormat decimalFormat = new DecimalFormat("0000");
        return decimalFormat.format(redisIndex);
    }

    /**
     * 获取业务单号
     *
     * @param typeCode      业务单类型
     * @param bizOrgCode       组织代码
     * @param uniqueUtils   redis唯一自增序列号工具类
     * @return
     */
    public static String getOrgOrderNo(String typeCode, String bizOrgCode, UniqueUtils uniqueUtils, int digits) {
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalDate nowLocalDate = nowDateTime.toLocalDate();
        String prefixKey = "uniqueOrderNo:" + typeCode + bizOrgCode + DateUtils.format(nowLocalDate, "yyMMdd");
        String todayEndTime = nowLocalDate + " 23:59:59";
        long minutesLeft = LocalDateTime.now().until(DateUtils.parseTime(todayEndTime), ChronoUnit.MINUTES);
        Long redisIndex = uniqueUtils.getUniqueIdExpire(prefixKey, Math.toIntExact(minutesLeft) + 10, TimeUnit.MINUTES);
        return typeCode + bizOrgCode + DateUtils.format(nowLocalDate, "yyMMdd") + String.format("%0" + digits + "d", redisIndex);
    }

    /**
     * 获取业务代码
     *
     * @param typeCode      业务类型
     * @param bizOrgCode       组织代码
     * @param uniqueUtils   redis唯一自增序列号工具类
     * @return
     */
    public static String getOrgBusinessCodeNo(String typeCode, String bizOrgCode, UniqueUtils uniqueUtils, int digits) {
        String prefixKey = "uniqueCodeNo:" + typeCode + bizOrgCode;
        Long redisIndex = uniqueUtils.getUniqueId(prefixKey);
        return typeCode + bizOrgCode + String.format("%0" + digits + "d", redisIndex);
    }

    /**
     * 获取业务代码(不加类型的流水号)
     *
     * @param typeCode      业务类型
     * @param bizOrgCode       组织代码
     * @param uniqueUtils   redis唯一自增序列号工具类
     * @return
     */
    public static String getFlowNoByTypeAndOrg(String typeCode, String bizOrgCode, UniqueUtils uniqueUtils, int digits) {
        String prefixKey = "uniqueCodeNo:" + typeCode + bizOrgCode;
        Long redisIndex = uniqueUtils.getUniqueId(prefixKey);
        return String.format("%0" + digits + "d", redisIndex);
    }

    /**
     * 根据中类代码及类型生成商品代码
     *
     * @param typeCode 类型（标准或组织）
     * @param bizOrgCode 组织代码
     * @param prefix 中类代码
     * @return code
     */
    public static String getGoodsCodeBySubSort(String typeCode, String bizOrgCode, String prefix, RedisService redisService) {

        String key = bizOrgCode == null ? typeCode : typeCode + "_" + bizOrgCode;
        Object codeObj = redisService.hGet(key, prefix);
        log.info("从redis中,key是{},小key是{},取出的数据是--", key, prefix, codeObj);
        int code;
        if (Objects.nonNull(codeObj)) {
            code = Integer.valueOf(codeObj.toString());
        } else {
            code = 0;
        }
        code++;

        redisService.hSet(key, prefix, code + "");
        String mnemonicCode = prefix + String.format("%0" + 4 + "d", code);
        int mix = 8;
        if (mnemonicCode.length() > mix) {
            throw new BusinessException("生成的商品代码位数已超限");
        }

        return mnemonicCode;
    }

    public static String createCode(String lastCode, Integer fillDigits) {
        String str = lastCode.substring(lastCode.length() - fillDigits);
        String prefix = lastCode.substring(0, lastCode.length() - fillDigits);
        String code = String.format("%0" + fillDigits + "d", Integer.valueOf(str) + 1);
        if (code.length() > fillDigits) {
            throw new BusinessException("生成的代码位数已超限");
        }
        return prefix + code;
    }


    /**
     * 获取配销订货单多笔支付
     * @param businessOrderNo
     * @param uniqueUtils
     * @param digits
     * @return
     */
    public static String getMultipleBusinessOrderPaySerialNo(String businessOrderNo, UniqueUtils uniqueUtils, int digits) {
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalDate nowLocalDate = nowDateTime.toLocalDate();
        String prefixKey = SystemConstant.BUSINESS_ORDER_PAY_SERIAL_NO_KEY + SystemConstant.COLON + businessOrderNo;
        String todayEndTime = nowLocalDate + " 23:59:59";
        long minutesLeft = LocalDateTime.now().until(DateUtils.parseTime(todayEndTime), ChronoUnit.MINUTES);
        Long redisIndex = uniqueUtils.getUniqueIdExpire(prefixKey, Math.toIntExact(minutesLeft) + 10, TimeUnit.MINUTES);
        return businessOrderNo + SystemConstant.SHORT_LINE + String.format("%0" + digits + "d", redisIndex);
    }

    public static void main(String[] args) {
        System.out.println(createCode("020101", 4));
    }
}
