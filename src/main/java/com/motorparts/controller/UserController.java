package com.motorparts.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.motorparts.common.PageResult;
import com.motorparts.common.Result;
import com.motorparts.common.enums.ResultCode;
import com.motorparts.common.util.PasswordUtil;
import com.motorparts.dto.ChangePasswordRequest;
import com.motorparts.entity.User;
import com.motorparts.service.UserService;
import org.springframework.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

/**
 * 用户/员工管理控制器（管理员维护账号、分配角色）。
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户")
    @PostMapping("/create")
    public Result<User> create(@Valid @RequestBody User user) {
        if (userService.existsUsername(user.getUsername(), null)) {
            return Result.error(ResultCode.DATA_EXISTS);
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 正常
        }
        // 密码加密存储
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        boolean success = userService.save(user);
        if (!success) {
            return Result.error();
        }
        return Result.success(user);
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户")
    @PutMapping("/update/{id}")
    public Result<User> update(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody User user) {
        user.setId(id);
        if (userService.existsUsername(user.getUsername(), id)) {
            return Result.error(ResultCode.DATA_EXISTS);
        }
        // 传了新密码才加密更新；否则置空，避免把密码列覆盖为明文/空串
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        user.setUpdateTime(LocalDateTime.now());
        boolean success = userService.updateById(user);
        if (!success) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        }
        return Result.success(user);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        boolean success = userService.removeById(id);
        if (!success) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        }
        return Result.success();
    }

    /**
     * 获取单个用户
     */
    @Operation(summary = "获取单个用户")
    @GetMapping("/get/{id}")
    public Result<User> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        }
        user.setPassword(null); // 不回传密码
        return Result.success(user);
    }

    /**
     * 分页查询用户
     */
    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<User>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "部门") @RequestParam(required = false) String department) {
        Page<User> page = new Page<>(current, size);
        Page<User> result = userService.pageUsers(page, username, role, department);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(PageResult.from(result));
    }

    /**
     * 分配角色
     */
    @Operation(summary = "分配角色")
    @PatchMapping("/assign-role/{id}")
    public Result<Void> assignRole(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "角色 admin/purchase/requisition") @RequestParam String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        boolean success = userService.updateById(user);
        if (!success) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        }
        return Result.success();
    }

    /**
     * 启用账号
     */
    @Operation(summary = "启用账号")
    @PatchMapping("/enable/{id}")
    public Result<Void> enable(@Parameter(description = "用户ID") @PathVariable Long id) {
        boolean success = userService.updateStatus(id, 1);
        return success ? Result.success() : Result.error(ResultCode.DATA_NOT_EXISTS);
    }

    /**
     * 禁用账号
     */
    @Operation(summary = "禁用账号")
    @PatchMapping("/disable/{id}")
    public Result<Void> disable(@Parameter(description = "用户ID") @PathVariable Long id) {
        boolean success = userService.updateStatus(id, 2);
        return success ? Result.success() : Result.error(ResultCode.DATA_NOT_EXISTS);
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码")
    @PatchMapping("/reset-password/{id}")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String password) {
        User user = new User();
        user.setId(id);
        user.setPassword(PasswordUtil.encode(password));
        user.setUpdateTime(LocalDateTime.now());
        boolean success = userService.updateById(user);
        return success ? Result.success() : Result.error(ResultCode.DATA_NOT_EXISTS);
    }

    /**
     * 修改密码（用户修改自己的密码，需校验原密码）
     */
    @Operation(summary = "修改密码")
    @PatchMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        boolean success = userService.changePassword(
                request.getId(), request.getOldPassword(), request.getNewPassword());
        return success ? Result.success() : Result.error();
    }
}
