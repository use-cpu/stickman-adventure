package com.stickman.game;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗会话(进行中的一场战斗,保存在内存中)
 */
@Data
public class BattleSession {

    private Long saveId;
    private Long userId;

    /** 战斗类型 NORMAL / BOSS */
    private String battleType;

    /** 玩家 */
    private Combatant player;

    /** 敌人 */
    private Combatant enemy;

    /** 敌人ID(怪物ID或Boss ID) */
    private Long enemyId;

    /** 当前回合数 */
    private int round;

    /** 是否由玩家先手 */
    private boolean playerFirst;

    /** 战斗结束标记 */
    private boolean finished;

    /** 战斗结果 WIN / LOSE / FLEE */
    private String result;

    /** 战斗详情(回合动作记录) */
    private List<String> actionLog = new ArrayList<>();

    /** 累计获得经验 */
    private int expGained;

    /** 累计获得金币 */
    private int goldGained;

    /** Boss二阶段标记 */
    private boolean bossPhase2;

    public void logAction(String msg) {
        this.actionLog.add(msg);
    }

    /**
     * 判断谁先手: 速度高者先手, 相同则玩家先手
     */
    public void determineOrder() {
        this.playerFirst = player.getSpeed() >= enemy.getSpeed();
    }
}
