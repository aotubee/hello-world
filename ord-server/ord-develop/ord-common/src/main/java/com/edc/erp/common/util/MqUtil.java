package com.edc.erp.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edc.plugins.jms.entity.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-06-19 14:14
 */
@Slf4j
public class MqUtil {

    public static JSONObject initJSONObject(MqMessage mqMessage, String topic, String tags) {
        log.info("消息ID:------------->" + mqMessage.getMessageId());
        log.info("消息Topic:------------->" + mqMessage.getTopic() + "==============配置文件中Topic：------------>" + topic);
        log.info("消息Tag:------------->" + mqMessage.getTag() + "==============配置文件中Topic：------------>" + tags);
        String resultStr = new String(mqMessage.getBody());
        if (StringUtils.isEmpty(resultStr)) {
            log.info("消息内容为空");
            return null;
        }
        if (mqMessage.getTopic().equals(topic) && mqMessage.getTag().equals(tags)) {
            JSONObject jsonObject = JSON.parseObject(resultStr);
            return jsonObject;
        }
        return null;
    }

    public static JSONObject initJsonObject(MqMessage mqMessage, String topic, String tags) {
        String resultStr = new String(mqMessage.getBody());
        if (StringUtils.isEmpty(resultStr)) {
            log.info("消息内容为空");
            return null;
        }
        if (mqMessage.getTopic().equals(topic) && mqMessage.getTag().equals(tags)) {
            return JSON.parseObject(resultStr);
        }
        return null;
    }

    public static String initMessage(MqMessage mqMessage, String topic, String tags) {
        if (mqMessage.getTopic().equals(topic) && mqMessage.getTag().equals(tags)) {
            return new String(mqMessage.getBody());
        }
        return null;
    }
}
