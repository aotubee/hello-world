package com.edc.erp.common.service;


import com.edc.erp.common.model.out.ucmanager.SysUserOut;
import java.util.List;


/**
 * 用户业务接口
 * @author lx
 * @since 2022-11-23 14:56:21
 */
public interface UserService {

    /**
     * 根据用户名查用户信息
     *
     * @param usernameList 用户名集合
     * @return 关联的用户，否则返回空集合
     */
    List<SysUserOut> findUserByUsernameList(List<String> usernameList);

    /**
     * 根据用户名查询用户信息
     *
     * @author: lishaobo
     * @date: 2020-02-10 12:24
     * @param username 用户
     * @return String
     */
    SysUserOut getUserByUsername(String username);
}
