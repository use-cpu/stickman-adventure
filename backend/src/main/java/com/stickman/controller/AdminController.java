package com.stickman.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stickman.common.Constants;
import com.stickman.common.BusinessException;
import com.stickman.common.Result;
import com.stickman.common.UserContext;
import com.stickman.entity.GameSave;
import com.stickman.entity.SysUser;
import com.stickman.mapper.SysUserMapper;
import com.stickman.service.GameSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员接口: 用户管理、全局存档查看
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SysUserMapper userMapper;
    private final GameSaveService gameSaveService;

    /** 用户列表(分页) */
    @GetMapping("/users")
    public Result<Page<SysUser>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username) {
        checkAdmin();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysUser::getUsername, username);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return Result.success(userMapper.selectPage(new Page<>(page, size), wrapper));
    }

    /** 修改用户状态(启用/禁用) */
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        checkAdmin();
        SysUser u = new SysUser();
        u.setId(id);
        u.setStatus(status);
        userMapper.updateById(u);
        return Result.success();
    }

    /** 重置用户密码 */
    @PutMapping("/users/{id}/password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        checkAdmin();
        SysUser u = new SysUser();
        u.setId(id);
        u.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userMapper.updateById(u);
        return Result.success();
    }

    /** 所有存档列表(管理员查看) */
    @GetMapping("/saves")
    public Result<?> allSaves() {
        checkAdmin();
        return Result.success(gameSaveService.listAllSaves());
    }

    private void checkAdmin() {
        if (!Constants.ROLE_ADMIN.equals(UserContext.getRole())) {
            throw BusinessException.forbidden("仅管理员可操作");
        }
    }
}
