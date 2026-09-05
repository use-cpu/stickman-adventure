package com.stickman.controller;

import com.stickman.common.Result;
import com.stickman.dto.SaveCreateRequest;
import com.stickman.entity.GameSave;
import com.stickman.service.GameSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 游戏存档接口
 */
@RestController
@RequestMapping("/saves")
@RequiredArgsConstructor
public class GameSaveController {

    private final GameSaveService gameSaveService;

    /** 我的存档列表 */
    @GetMapping
    public Result<List<GameSave>> mySaves() {
        return Result.success(gameSaveService.listMySaves());
    }

    /** 存档详情(含角色) */
    @GetMapping("/{id}")
    public Result<GameSave> detail(@PathVariable Long id) {
        return Result.success(gameSaveService.getDetail(id));
    }

    /** 创建存档 */
    @PostMapping
    public Result<GameSave> create(@Valid @RequestBody SaveCreateRequest req) {
        return Result.success(gameSaveService.createSave(req));
    }

    /** 删除存档 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        gameSaveService.deleteSave(id);
        return Result.success();
    }
}
