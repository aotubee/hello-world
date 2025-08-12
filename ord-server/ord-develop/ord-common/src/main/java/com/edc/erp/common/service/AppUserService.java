package com.edc.erp.common.service;

import com.edc.erp.common.model.out.appuser.AppUserOut;

/**
 * @author fxw
 * @description: app用户rpc接口
 * @since 2022/10/20 14:42
 */
public interface AppUserService {

    /**
     * 根据用户代码获取用户信息
     *
     * @param userCode
     * @return
     */
    AppUserOut getAppUserByUserCode(String userCode);
}
