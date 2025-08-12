package com.edc.erp.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.edc.erp.common.constant.SystemConstant;
import com.edc.erp.common.model.in.warning.WarningIn;
import com.edc.erp.common.service.WarningService;
import com.edc.plugins.redis.RedisDataBaseService;
import com.edc.plugins.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-01-27 09:27
 */
@Service
public class WarningServiceImpl implements WarningService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisDataBaseService redisDataBaseService;

    @Value("${warningRedisDB}")
    private Integer warningRedisDB;

    @Value("${spring.redis.database}")
    private Integer defaultIndex;

    @Value("${redisMq.erpOrdTopic}")
    private String erpOrdTopic;

    @Override
    public void pushWarningMessage(String businessType, String errorCode, String placeholderErrorMessage, String originalException, String businessParam, String bizOrgCode) {
        WarningIn warningIn = initWarningIn(businessType, errorCode, placeholderErrorMessage, originalException, businessParam, bizOrgCode);

        this.getRedisTemplate(warningRedisDB).convertAndSend(erpOrdTopic, JSON.toJSONString(warningIn));
    }

    @Override
    public void pushDBIndexWarningMessage(Integer dbIndex, String topic, String businessType, String errorCode, String placeholderErrorMessage,
                                          String originalException, String businessParam, String bizOrgCode) {
        WarningIn warningIn = initWarningIn(businessType, errorCode, placeholderErrorMessage, originalException, businessParam, bizOrgCode);
        this.getRedisTemplate(dbIndex).convertAndSend(topic, JSON.toJSONString(warningIn));
    }

    private static WarningIn initWarningIn(String businessType, String errorCode, String placeholderErrorMessage,
                                           String originalException, String businessParam, String bizOrgCode) {
        WarningIn warningIn = new WarningIn();
        warningIn.setSystemNo(SystemConstant.SYSTEM_CODE);
        warningIn.setBusinessType(businessType);
        warningIn.setErrorCode(errorCode);
        warningIn.setPlaceholderErrorMessage(placeholderErrorMessage);
        warningIn.setOriginalException(originalException);
        warningIn.setBusinessParam(businessParam);
        warningIn.setBizOrgCode(bizOrgCode);
        return warningIn;
    }

    /**
     * 获取redisTemplate
     * @param dbIndex
     * @return
     */
    private RedisTemplate getRedisTemplate(int dbIndex) {
        if (dbIndex == defaultIndex) {
            return redisService.getRedisTemplate();
        }
        return redisDataBaseService.getRedisTemplate(dbIndex);
    }

}
