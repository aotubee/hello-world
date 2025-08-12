package com.edc.erp.common.service;

import com.edc.erp.common.model.in.warning.RobotMarkDownMessageIn;
import com.edc.plugins.common.response.Response;

/**
 * 钉钉服务RPC
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-08-25 11:46
 */
public interface DingTalkService {

    /**
     * 获取token
     *
     * @param appKey    appKey
     * @param appSecret appSecret
     * @return
     */
    String getAccessToken(String appKey, String appSecret);

    /**
     * 发送MarkDown格式钉钉机器人预警消息
     *
     * @param robotMarkDownMessageIn 钉钉机器人预警消息入参
     * @return
     */
    Response sendDingTalkRobotMarkDownMessage(RobotMarkDownMessageIn robotMarkDownMessageIn);

}
