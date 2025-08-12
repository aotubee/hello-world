package com.edc.erp.common.rpc;

import com.edc.erp.common.model.in.warning.RobotMarkDownMessageIn;
import com.edc.erp.common.model.out.ucmanager.SysUserOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 查询用户信息rpc
 * @author lx
 * @since 2022-11-23 14:56:21
 */
@FeignClient(value = "uc-manager", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface UcManagerClient {


    /**
     * 根据用户名查用户信息
     *
     * @param usernameList 用户名集合
     * @return 如果远程调用成功则返回查询结果
     */
    @GetMapping("/user/findUserByUsernameList")
    Response<List<SysUserOut>> findUserByUsernameList(@RequestParam("usernameList") List<String> usernameList);

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return
     */
    @GetMapping(value = "/user/loadUserByUsername")
    @ResponseBody
    Response<SysUserOut> getUserByUsername(@RequestParam("username") String username);

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
