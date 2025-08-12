package com.edc.erp.common.service.impl;

import com.edc.erp.common.model.out.ucmanager.SysUserOut;
import com.edc.erp.common.rpc.UcManagerClient;
import com.edc.erp.common.service.UserService;
import com.edc.plugins.common.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户接口实现
 * @author lx
 * @since 2022-11-23 14:56:21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UcManagerClient ucManagerClient;

    @Override
    public List<SysUserOut> findUserByUsernameList(List<String> usernameList) {
        Response<List<SysUserOut>> response = ucManagerClient.findUserByUsernameList(usernameList);
        if (!response.isSuccess() || null == response.getData()) {
            log.error(response.getMessage());
            return null;
        }
        return response.getData();
    }

    @Override
    public SysUserOut getUserByUsername(String username) {
        Response<SysUserOut> response = ucManagerClient.getUserByUsername(username);
        return response.isSuccess() ? response.getData() : null;
    }
}
