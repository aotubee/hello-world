package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.warning.RobotMarkDownMessageIn;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 钉钉服务API
 *
 * @author : zhangyao@tseveryday.com
 * @date 2019年08月28日 18:56
 */
@FeignClient(name = "edc-server-dingtalk", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface DingTalkClient {

    /**
     * 获取accessToken
     *
     * @param appKey
     * @param appSecret
     * @return
     */
    @GetMapping(value = "/dingtalk/index/getAccessToken")
    Response<String> getAccessToken(@RequestParam(value = "appKey") String appKey,
                                    @RequestParam(value = "appSecret") String appSecret);


    /**
     * 发送MarkDown格式钉钉机器人预警消息
     *
     * @param robotMarkDownMessageIn 预警消息入参
     * @return
     */
    @PostMapping(value = "/dingtalk/message/sendDingTalkRobotMarkDownMessage")
    Response sendDingTalkRobotMarkDownMessage(@RequestBody RobotMarkDownMessageIn robotMarkDownMessageIn);

}
