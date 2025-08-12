package com.edc.erp.common.rpc;

import com.edc.erp.common.entity.OptAppNotification;
import com.edc.erp.common.model.in.unipush.OptAppNotificationIn;
import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author lishaobo
 * @description: app个推远程调用中心
 * @since 2023/1/7 14:27
 */
@RequestMapping(value = "/opt/appNotification")
@FeignClient(name ="mdm",contextId ="AppNotificationClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface AppNotificationClient {

    /**
     * 添加通知信息
     *
     * @param optAppNotification 消息
     * @return 添加通知信息
     */
    @PostMapping("/saveNotification")
    Response<String> saveNotification(@RequestBody OptAppNotification optAppNotification);

    /**
     * 发送通知消息给指定人员
     *
     * @param optAppNotificationIn 消息内容
     */
    @ApiOperation(value = "发送通知消息给指定人员", notes = "发送通知消息给指定人员", httpMethod = "POST")
    @PostMapping("/sendNotificationToSingle")
    Response<String> sendMessageToSingle(@RequestBody @Valid OptAppNotificationIn optAppNotificationIn);
}
