package com.stickman.game;

import com.stickman.common.Constants;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 回合制战斗引擎
 *
 * 处理玩家动作 → 敌人动作 → 结算, 产出每回合结果。
 * 战斗会话保存在 BattleManager 内存中。
 */
@Component
public class CombatEngine {

    private final Random rng = new Random();

    /**
     * 执行玩家本回合动作
     * @param session  当前战斗会话
     * @param action    ATTACK / SKILL / DEFEND / FLEE
     * @return 本回合的简要战报文本
     */
    public String executePlayerAction(BattleSession session, String action) {
        if (session.isFinished()) {
            return "战斗已结束";
        }

        session.setRound(session.getRound() + 1);
        session.getPlayer().resetDefend();
        session.getEnemy().resetDefend();

        StringBuilder sb = new StringBuilder();
        sb.append("—— 第 ").append(session.getRound()).append(" 回合 ——\n");

        // 玩家先手则玩家先动,否则敌人先动
        if (session.isPlayerFirst()) {
            if (!doPlayerAction(session, action, sb)) {
                return sb.toString();
            }
            doEnemyAction(session, sb);
        } else {
            doEnemyAction(session, sb);
            doPlayerAction(session, action, sb);
        }

        // 结算
        checkEnd(session);
        return sb.toString();
    }

    /**
     * 执行玩家动作,返回 false 表示玩家逃跑(战斗直接结束)
     */
    private boolean doPlayerAction(BattleSession session, String action, StringBuilder sb) {
        Combatant player = session.getPlayer();
        Combatant enemy = session.getEnemy();

        if (!player.isAlive()) {
            return true;
        }

        if ("FLEE".equalsIgnoreCase(action)) {
            // 普通战斗 50% 概率逃跑, Boss战不可逃跑
            if (Constants.BATTLE_BOSS.equals(session.getBattleType())) {
                sb.append("Boss战无法逃跑!\n");
                session.logAction("尝试逃跑失败(Boss战)");
                return true;
            }
            if (rng.nextInt(100) < 50) {
                sb.append("逃跑成功!\n");
                session.setFinished(true);
                session.setResult(Constants.RESULT_FLEE);
                session.logAction("逃跑成功");
                return false;
            } else {
                sb.append("逃跑失败, 浪费了这一回合!\n");
                session.logAction("逃跑失败");
                return true;
            }
        }

        if ("DEFEND".equalsIgnoreCase(action)) {
            player.setDefending(true);
            sb.append(player.getName()).append(" 进入防御姿态, 本回合受到的伤害减半。\n");
            session.logAction(player.getName() + " 防御");
            return true;
        }

        if ("SKILL".equalsIgnoreCase(action)) {
            if (player.getMp() < player.getSkillMpCost()) {
                sb.append("MP不足, 技能释放失败, 转为普通攻击!\n");
                session.logAction("MP不足, 技能失败");
                action = "ATTACK";
            } else {
                player.setMp(player.getMp() - player.getSkillMpCost());
                int baseDmg = player.getSkillDamage() > 0
                        ? player.getSkillDamage() + player.getAttack() / 2
                        : player.getAttack() * 2;
                int fluctuation = rng.nextInt(Math.max(1, baseDmg / 10)) - Math.max(1, baseDmg / 20);
                int damage = enemy.takeDamage(baseDmg + fluctuation);
                sb.append(player.getName()).append(" 释放技能 [")
                        .append(player.getSkillName() == null ? "重击" : player.getSkillName())
                        .append("], 对 ").append(enemy.getName())
                        .append(" 造成 ").append(damage).append(" 点伤害!\n");
                session.logAction(player.getName() + " 释放技能造成 " + damage + " 伤害");
                return true;
            }
        }

        // 默认普通攻击 ATTACK
        int baseDmg = player.getAttack();
        int fluctuation = rng.nextInt(Math.max(1, baseDmg / 10)) - Math.max(1, baseDmg / 20);
        // 10% 暴击
        boolean crit = rng.nextInt(100) < 10;
        if (crit) {
            baseDmg = (int) (baseDmg * 1.5);
        }
        int damage = enemy.takeDamage(baseDmg + fluctuation);
        sb.append(player.getName()).append(" 攻击 ").append(enemy.getName())
                .append(crit ? "(暴击!)" : "")
                .append(", 造成 ").append(damage).append(" 点伤害。\n");
        session.logAction(player.getName() + " 普攻造成 " + damage + " 伤害" + (crit ? "(暴击)" : ""));
        return true;
    }

    /**
     * 敌人回合
     */
    private void doEnemyAction(BattleSession session, StringBuilder sb) {
        Combatant enemy = session.getEnemy();
        Combatant player = session.getPlayer();

        if (!enemy.isAlive()) {
            return;
        }

        // Boss 进入二阶段(血量低于阈值)且尚未触发
        if (Constants.BATTLE_BOSS.equals(session.getBattleType()) && !session.isBossPhase2()) {
            int threshold = enemy.getMaxHp() * 40 / 100;
            // 这里使用通用 40% 阈值简化(Boss表有 phase2HpThreshold 字段,实际在 CombatService 设置)
            if (enemy.getHp() <= threshold) {
                session.setBossPhase2(true);
                enemy.setAttack((int) (enemy.getAttack() * 1.3));
                sb.append("⚠ ").append(enemy.getName()).append(" 进入狂暴状态, 攻击力提升!\n");
                session.logAction(enemy.getName() + " 进入二阶段狂暴");
            }
        }

        // 30% 概率使用技能(MP足够时)
        boolean useSkill = enemy.getSkillDamage() > 0 && rng.nextInt(100) < 30;
        if (useSkill) {
            int baseDmg = enemy.getSkillDamage() + enemy.getAttack() / 2;
            int fluctuation = rng.nextInt(Math.max(1, baseDmg / 10)) - Math.max(1, baseDmg / 20);
            int damage = player.takeDamage(baseDmg + fluctuation);
            sb.append(enemy.getName()).append(" 释放技能 [")
                    .append(enemy.getSkillName() == null ? "特殊攻击" : enemy.getSkillName())
                    .append("], 对 ").append(player.getName())
                    .append(" 造成 ").append(damage).append(" 点伤害!\n");
            session.logAction(enemy.getName() + " 释放技能造成 " + damage + " 伤害");
        } else {
            int baseDmg = enemy.getAttack();
            int fluctuation = rng.nextInt(Math.max(1, baseDmg / 10)) - Math.max(1, baseDmg / 20);
            int damage = player.takeDamage(baseDmg + fluctuation);
            sb.append(enemy.getName()).append(" 攻击 ").append(player.getName())
                    .append(", 造成 ").append(damage).append(" 点伤害。\n");
            session.logAction(enemy.getName() + " 攻击造成 " + damage + " 伤害");
        }
    }

    /**
     * 检查战斗是否结束
     */
    private void checkEnd(BattleSession session) {
        if (session.getEnemy() == null || !session.getEnemy().isAlive()) {
            session.setFinished(true);
            session.setResult(Constants.RESULT_WIN);
        } else if (!session.getPlayer().isAlive()) {
            session.setFinished(true);
            session.setResult(Constants.RESULT_LOSE);
        }
    }
}
