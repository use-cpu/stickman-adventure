package com.stickman.controller;

import com.stickman.common.Result;
import com.stickman.entity.Character;
import com.stickman.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色属性接口
 */
@RestController
@RequestMapping("/character")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    /** 获取存档对应的角色 */
    @GetMapping("/save/{saveId}")
    public Result<Character> getBySave(@PathVariable Long saveId) {
        return Result.success(characterService.getBySaveId(saveId));
    }

    /** 分配技能点提升属性: attr = ATTACK/DEFENSE/SPEED/MAX_HP/MAX_MP */
    @PostMapping("/save/{saveId}/allocate")
    public Result<Character> allocate(@PathVariable Long saveId, @RequestParam String attr) {
        return Result.success(characterService.allocateStat(saveId, attr));
    }
}
