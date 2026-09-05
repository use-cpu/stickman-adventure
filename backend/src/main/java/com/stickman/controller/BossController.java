package com.stickman.controller;

import com.stickman.common.BusinessException;
import com.stickman.common.Constants;
import com.stickman.common.Result;
import com.stickman.common.UserContext;
import com.stickman.entity.Boss;
import com.stickman.service.BossService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Boss配置管理接口(管理员)
 */
@RestController
@RequestMapping("/bosses")
@RequiredArgsConstructor
public class BossController {

    private final BossService bossService;

    @GetMapping
    public Result<List<Boss>> list() {
        return Result.success(bossService.list());
    }

    @GetMapping("/{id}")
    public Result<Boss> detail(@PathVariable Long id) {
        return Result.success(bossService.getById(id));
    }

    @PostMapping
    public Result<Boss> create(@RequestBody Boss boss) {
        checkAdmin();
        return Result.success(bossService.create(boss));
    }

    @PutMapping
    public Result<Boss> update(@RequestBody Boss boss) {
        checkAdmin();
        return Result.success(bossService.update(boss));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        checkAdmin();
        bossService.delete(id);
        return Result.success();
    }

    private void checkAdmin() {
        if (!Constants.ROLE_ADMIN.equals(UserContext.getRole())) {
            throw BusinessException.forbidden("仅管理员可操作");
        }
    }
}
