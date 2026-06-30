package com.motorparts.service;

import com.motorparts.dto.LoginRequest;

import java.util.Map;

/**
 * 登录认证服务接口
 */
public interface AuthService {

    /**
     * 登录：校验账号密码、检查账号未禁用，返回 token 与用户信息。
     *
     * @param request 登录参数
     * @return 含 token、用户基本信息（id/username/realName/role/department）的 Map
     */
    Map<String, Object> login(LoginRequest request);

    /**
     * 登出（清除登录态）。当前为无状态实现，仅作语义占位。
     *
     * @param token 登录令牌
     */
    void logout(String token);
}
