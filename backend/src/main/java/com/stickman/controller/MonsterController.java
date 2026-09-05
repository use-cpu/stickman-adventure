package com.stickman.controller;

import com.stickman.common.BusinessException;
import com.stickman.common.Constants;
import com.stickman.common.Result;
import com.stickman.entity.Monster;
import com.stickman.service.MonsterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 怪物配置管理接口(管理员)
 */
@RestController
@RequestMapping("/monsters")
@RequiredArgsConstructor
public class MonsterController {

    private final MonsterService monsterService;

    /** 查询怪物列表 */
    @GetMapping
    public Result<List<Monster>> list(@RequestParam(required = false) String type) {
        return Result.success(monsterService.list(type));
    }

    @GetMapping("/{id}")
    public Result<Monster> detail(@PathVariable Long id) {
        return Result.success(monsterService.getById(id));
    }

    @PostMapping
    public Result<Monster> create(@RequestBody Monster monster) {
        checkAdmin();
        return Result.success(monsterService.create(monster));
    }

    @PutMapping
    public Result<Monster> update(@RequestBody Monster monster) {
        checkAdmin();
        return Result.success(monsterService.update(monster));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        checkAdmin();
        monsterService.delete(id);
        return Result.success();
    }

    private void checkAdmin() {
        if (!Constants.ROLE_ADMIN.equals(com.stickman.common.UserContext.getRole())) {
            throw BusinessException.forbidden("仅管理员可操作");
        }
    }
}
