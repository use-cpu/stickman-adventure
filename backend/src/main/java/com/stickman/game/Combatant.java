package com.stickman.game;

import lombok.Data;

/**
 * 战斗参与者(玩家或敌人的运行时快照)
 */
@Data
public class Combatant {

    private String name;
    private String icon;
    private String color;

    private int hp;
    private int maxHp;
    private int mp;
    private int maxMp;
    private int attack;
    private int defense;
    private int speed;

    private String skillName;
    private int skillDamage;
    /** 技能消耗的MP */
    private int skillMpCost;

    /** 当前回合是否处于防御状态 */
    private boolean defending;

    public Combatant() {
        this.skillMpCost = 10;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    /** 造成伤害并返回实际伤害值 */
    public int takeDamage(int rawDamage) {
        // 防御状态下减半受到的伤害
        int def = defending ? defense * 2 : defense;
        int dmg = Math.max(1, rawDamage - def / 2);
        hp = Math.max(0, hp - dmg);
        return dmg;
    }

    public void resetDefend() {
        this.defending = false;
    }
}
