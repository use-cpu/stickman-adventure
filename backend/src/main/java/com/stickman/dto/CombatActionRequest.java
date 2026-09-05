package com.stickman.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 战斗动作请求 (玩家回合操作)
 */
@Data
public class CombatActionRequest {

    @NotNull(message = "存档ID不能为空")
    private Long saveId;

    /** 动作类型: ATTACK 普通攻击 / SKILL 技能 / DEFEND 防御 / FLEE 逃跑 */
    private String action;
}
