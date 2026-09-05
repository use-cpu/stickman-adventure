package com.stickman.dto;

import lombok.Data;

/**
 * 开始战斗请求
 */
@Data
public class StartBattleRequest {

    private Long saveId;

    /** 战斗类型: NORMAL / BOSS */
    private String battleType;

    /** 怪物ID (普通战斗) */
    private Long monsterId;

    /** Boss ID (Boss战) */
    private Long bossId;
}
