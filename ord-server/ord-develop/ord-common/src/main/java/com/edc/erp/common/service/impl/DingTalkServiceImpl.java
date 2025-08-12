package com.edc.erp.common.service.impl;

import com.edc.erp.common.model.in.warning.RobotMarkDownMessageIn;
import com.edc.erp.common.rpc.UcManagerClient;
import com.edc.erp.common.service.DingTalkService;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-08-25 16:19
 */
@Slf4j
@Service
public class DingTalkServiceImpl implements DingTalkService {

//    @Autowired
//    private DingTalkClient dingTalkClient;

    @Autowired
    private UcManagerClient ucManagerClient;

    @Override
    public String getAccessToken(String appKey, String appSecret) {
        Response<String> response = ucManagerClient.getAccessToken(appKey, appSecret);
        if (null != response && response.isSuccess()) {
            return response.getData();
        }
        log.error("获取钉钉AccessToken错误" + (null != response ? response.getMessage() : "-"));
        return null;
    }

    @Override
    public Response sendDingTalkRobotMarkDownMessage(RobotMarkDownMessageIn robotMarkDownMessageIn) {
        Response response = ucManagerClient.sendDingTalkRobotMarkDownMessage(robotMarkDownMessageIn);
        if (null != response && response.isSuccess()) {
            return Response.success("发送成功");
        }
        log.error("发送预警消息异常：" + (null != response ? response.getMessage() : "-"));
        return Response.error("发送失败");
    }


}
