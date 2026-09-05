package com.stickman.controller;

import com.stickman.common.Result;
import com.stickman.dto.StartBattleRequest;
import com.stickman.service.CombatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 回合制战斗接口
 */
@RestController
@RequestMapping("/combat")
@RequiredArgsConstructor
public class CombatController {

    private final CombatService combatService;

    /** 开始战斗: battleType=NORMAL/BOSS, 可选 monsterId / bossId */
    @PostMapping("/start")
    public Result<Map<String, Object>> startBattle(@RequestBody StartBattleRequest req) {
        return Result.success(combatService.startBattle(
                req.getSaveId(), req.getBattleType(), req.getMonsterId(), req.getBossId()));
    }

    /** 执行本回合动作: action=ATTACK/SKILL/DEFEND/FLEE */
    @PostMapping("/save/{saveId}/action")
    public Result<Map<String, Object>> action(@PathVariable Long saveId, @RequestParam String action) {
        return Result.success(combatService.doAction(saveId, action));
    }

    /** 查询当前战斗状态 */
    @GetMapping("/save/{saveId}/status")
    public Result<Map<String, Object>> status(@PathVariable Long saveId) {
        return Result.success(combatService.currentBattle(saveId));
    }

    /** 放弃当前战斗 */
    @PostMapping("/save/{saveId}/forfeit")
    public Result<?> forfeit(@PathVariable Long saveId) {
        combatService.forfeit(saveId);
        return Result.success();
    }
}
