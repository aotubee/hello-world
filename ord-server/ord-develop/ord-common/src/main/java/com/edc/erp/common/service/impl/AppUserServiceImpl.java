package com.edc.erp.common.service.impl;

import com.edc.erp.common.model.out.appuser.AppUserOut;
import com.edc.erp.common.rpc.AppUserClient;
import com.edc.erp.common.service.AppUserService;
import com.edc.plugins.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author fxw
 * @description: app用户rpc调用实现类
 * @since 2022/10/20 14:45
 */
@Service
@Slf4j
public class AppUserServiceImpl implements AppUserService {

    @Resource
    private AppUserClient appUserClient;

    /**
     * 根据用户代码获取用户信息
     *
     * @param userCode
     * @return
     */
    @Override
    public AppUserOut getAppUserByUserCode(String userCode) {
        Response<AppUserOut> response = appUserClient.getAppUserByUserCode(userCode);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }
}
