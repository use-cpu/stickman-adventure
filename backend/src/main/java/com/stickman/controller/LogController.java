package com.stickman.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stickman.common.Constants;
import com.stickman.common.BusinessException;
import com.stickman.common.Result;
import com.stickman.common.UserContext;
import com.stickman.entity.BattleLog;
import com.stickman.entity.UserLog;
import com.stickman.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日志接口
 *  - 玩家: 查看自己的战斗日志
 *  - 管理员: 查看所有战斗日志、用户操作日志
 */
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /** 我的战斗日志 */
    @GetMapping("/battle/mine")
    public Result<List<BattleLog>> myBattleLogs(@RequestParam(required = false) Long saveId) {
        return Result.success(logService.listMyBattleLogs(saveId));
    }

    /** 所有战斗日志(管理员) */
    @GetMapping("/battle/all")
    public Result<Page<BattleLog>> allBattleLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        checkAdmin();
        return Result.success(logService.listAllBattleLogs(page, size));
    }

    /** 用户操作日志(管理员) */
    @GetMapping("/user")
    public Result<Page<UserLog>> userLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        checkAdmin();
        return Result.success(logService.listUserLogs(page, size));
    }

    private void checkAdmin() {
        if (!Constants.ROLE_ADMIN.equals(UserContext.getRole())) {
            throw BusinessException.forbidden("仅管理员可操作");
        }
    }
}
