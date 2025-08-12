package com.edc.erp.common.rpc;

import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.plugins.common.response.Response;
import com.edc.plugins.feignclient.config.GlobalFeignErrorDecoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author fxw
 * @description: app用户远程调用中心
 * @since 2022/10/20 14:27
 */
@FeignClient(name ="mdm",contextId ="AppUserClient", configuration = GlobalFeignErrorDecoderConfiguration.class)
public interface AppUserClient {

    /**
     * 根據用戶编码查询用户信息
     *
     * @param userCode
     * @return
     */
    @GetMapping("/opt/optAppUser/getAppUserByUserCode")
    Response<AppUserOut> getAppUserByUserCode(@RequestParam String userCode);
}
