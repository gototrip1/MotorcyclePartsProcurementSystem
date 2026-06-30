package com.motorparts.service.impl;

import com.motorparts.common.enums.ResultCode;
import com.motorparts.common.exception.BusinessException;
import com.motorparts.common.util.PasswordUtil;
import com.motorparts.dto.LoginRequest;
import com.motorparts.entity.User;
import com.motorparts.service.AuthService;
import com.motorparts.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 登录认证服务实现。
 *
 * <p>项目未引入 Spring Security，密码使用 Hutool BCrypt 加密存储、登录时用
 * {@link PasswordUtil#matches} 校验；令牌为简单的 UUID。若后续需要更完整的安全能力，
 * 可接入正式的会话/JWT 机制。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = userService.getUserByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "用户不存在");
        }
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", UUID.randomUUID().toString().replace("-", ""));
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("department", user.getDepartment());
        return result;
    }

    @Override
    public void logout(String token) {
        // 无状态实现：无服务端会话需要清理，留作语义占位
    }
}
