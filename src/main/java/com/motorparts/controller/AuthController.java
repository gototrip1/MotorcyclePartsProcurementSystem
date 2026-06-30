package com.motorparts.controller;

import com.motorparts.common.Result;
import com.motorparts.dto.LoginRequest;
import com.motorparts.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 登录认证控制器
 */
@Tag(name = "登录认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    /**
     * 登出
     */
    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(
            @Parameter(description = "登录令牌") @RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return Result.success();
    }
}
