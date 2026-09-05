package com.stickman.service;

import com.stickman.common.Constants;
import com.stickman.common.BusinessException;
import com.stickman.entity.BattleLog;
import com.stickman.entity.Boss;
import com.stickman.entity.Character;
import com.stickman.entity.GameSave;
import com.stickman.entity.Monster;
import com.stickman.game.BattleManager;
import com.stickman.game.BattleSession;
import com.stickman.game.CombatEngine;
import com.stickman.game.Combatant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 战斗编排服务
 *
 * 流程: 开始战斗 → 玩家每回合提交动作 → 结算(奖励/日志) → 清理会话
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private final BattleManager battleManager;
    private final CombatEngine combatEngine;
    private final GameSaveService gameSaveService;
    private final CharacterService characterService;
    private final MonsterService monsterService;
    private final BossService bossService;
    private final LogService logService;

    /**
     * 开始一场战斗
     */
    public Map<String, Object> startBattle(Long saveId, String battleType, Long monsterId, Long bossId) {
        if (battleManager.hasBattle(saveId)) {
            throw BusinessException.of("该存档已有进行中的战斗");
        }

        GameSave save = gameSaveService.getById(saveId);
        Character ch = characterService.getBySaveId(saveId);

        Combatant player = toCombatant(ch);
        Combatant enemy = new Combatant();
        Long enemyId;

        if (Constants.BATTLE_BOSS.equals(battleType)) {
            Boss boss;
            if (bossId != null) {
                boss = bossService.getById(bossId);
            } else {
                boss = bossService.getByLevel(save.getCurrentLevel());
                if (boss == null) {
                    throw BusinessException.of("当前关卡没有配置Boss");
                }
            }
            enemyId = boss.getId();
            buildEnemyFromBoss(enemy, boss);
        } else {
            Monster monster;
            if (monsterId != null) {
                monster = monsterService.getById(monsterId);
            } else {
                // 根据关卡随机挑一个普通怪
                monster = monsterService.list(Constants.MONSTER_NORMAL).stream()
                        .filter(m -> Math.abs(m.getLevel() - save.getCurrentLevel()) <= 1)
                        .findFirst()
                        .orElseThrow(() -> new BusinessException("没有可用怪物"));
            }
            enemyId = monster.getId();
            buildEnemyFromMonster(enemy, monster);
        }

        BattleSession session = new BattleSession();
        session.setSaveId(saveId);
        session.setUserId(save.getUserId());
        session.setBattleType(battleType == null ? Constants.BATTLE_NORMAL : battleType);
        session.setPlayer(player);
        session.setEnemy(enemy);
        session.setEnemyId(enemyId);
        session.setRound(0);
        session.determineOrder();

        battleManager.start(session);

        Map<String, Object> result = new HashMap<>();
        result.put("battleType", session.getBattleType());
        result.put("player", snapshot(player, true));
        result.put("enemy", snapshot(enemy, true));
        result.put("playerFirst", session.isPlayerFirst());
        result.put("enemyId", enemyId);
        result.put("enemyName", enemy.getName());
        result.put("enemyIcon", enemy.getIcon());
        result.put("round", 0);
        result.put("finished", false);
        return result;
    }

    /**
     * 玩家执行一个动作
     */
    @Transactional
    public Map<String, Object> doAction(Long saveId, String action) {
        BattleSession session = battleManager.getOrFail(saveId);
        String report = combatEngine.executePlayerAction(session, action);

        boolean ended = session.isFinished();
        Map<String, Object> result = new HashMap<>();
        result.put("round", session.getRound());
        result.put("report", report);
        result.put("player", snapshot(session.getPlayer(), false));
        result.put("enemy", snapshot(session.getEnemy(), false));
        result.put("finished", ended);

        if (ended) {
            // 先清理内存会话, 确保不残留 (即使后续数据库结算失败, 也不会卡住后续战斗)
            battleManager.remove(saveId);
            try {
                // 同步角色数据到数据库(战斗中HP/MP会变化)
                GameSave save = gameSaveService.getById(saveId);
                Character ch = characterService.getBySaveId(saveId);
                ch.setHp(session.getPlayer().getHp());
                ch.setMp(session.getPlayer().getMp());

                String resultType = session.getResult();
                int expGain = 0;
                int goldGain = 0;

                if (Constants.RESULT_WIN.equals(resultType)) {
                    expGain = session.getExpGained() > 0 ? session.getExpGained()
                            : (Constants.BATTLE_BOSS.equals(session.getBattleType())
                                    ? session.getEnemy() != null ? 150 : 0 : 25);
                    goldGain = session.getGoldGained() > 0 ? session.getGoldGained()
                            : (Constants.BATTLE_BOSS.equals(session.getBattleType()) ? 100 : 15);
                    applyRewards(ch, expGain, goldGain);
                    result.put("expGained", expGain);
                    result.put("goldGained", goldGain);

                    // 击败Boss通关: 进入下一关
                    if (Constants.BATTLE_BOSS.equals(session.getBattleType())) {
                        save.setCurrentLevel(save.getCurrentLevel() + 1);
                        // 回满血蓝
                        ch.setHp(ch.getMaxHp());
                        ch.setMp(ch.getMaxMp());
                    }
                } else if (Constants.RESULT_LOSE.equals(resultType)) {
                    // 失败: 角色复活,扣一半金币,血量回满(在起点)
                    ch.setHp(ch.getMaxHp());
                    ch.setMp(ch.getMaxMp());
                    ch.setGold(ch.getGold() / 2);
                    save.setPlayerX(0);
                    save.setPlayerY(0);
                }

                characterService.saveCharacter(ch);
                gameSaveService.updateSave(save);

                // 记录战斗日志
                BattleLog battleLog = new BattleLog();
                battleLog.setSaveId(saveId);
                battleLog.setUserId(session.getUserId());
                battleLog.setBattleType(session.getBattleType());
                battleLog.setEnemyName(session.getEnemy().getName());
                battleLog.setResult(resultType);
                battleLog.setRounds(session.getRound());
                battleLog.setDetail(String.join(" | ", session.getActionLog()));
                battleLog.setExpGained(expGain);
                battleLog.setGoldGained(goldGain);
                logService.recordBattleLog(battleLog);

                result.put("result", resultType);
            } catch (Exception e) {
                log.error("战斗结算数据库操作失败, saveId={}, 会话已清理, 战斗结果可能未持久化", saveId, e);
                result.put("result", session.getResult());
            }
        }

        return result;
    }

    /**
     * 查询当前战斗状态
     */
    public Map<String, Object> currentBattle(Long saveId) {
        if (!battleManager.hasBattle(saveId)) {
            Map<String, Object> r = new HashMap<>();
            r.put("inBattle", false);
            return r;
        }
        BattleSession session = battleManager.get(saveId);
        Map<String, Object> result = new HashMap<>();
        result.put("inBattle", true);
        result.put("battleType", session.getBattleType());
        result.put("player", snapshot(session.getPlayer(), false));
        result.put("enemy", snapshot(session.getEnemy(), false));
        result.put("round", session.getRound());
        result.put("playerFirst", session.isPlayerFirst());
        result.put("bossPhase2", session.isBossPhase2());
        return result;
    }

    /**
     * 逃跑/放弃当前战斗
     */
    public void forfeit(Long saveId) {
        battleManager.remove(saveId);
    }

    // ====== 私有辅助 ======

    private void applyRewards(Character ch, int exp, int gold) {
        ch.setExp(ch.getExp() + exp);
        ch.setGold(ch.getGold() + gold);
        // 升级判定
        while (ch.getExp() >= ch.getExpToNext()) {
            ch.setExp(ch.getExp() - ch.getExpToNext());
            ch.setLevel(ch.getLevel() + 1);
            ch.setExpToNext((int) (ch.getExpToNext() * 1.5));
            ch.setMaxHp(ch.getMaxHp() + 20);
            ch.setHp(ch.getMaxHp());
            ch.setMaxMp(ch.getMaxMp() + 10);
            ch.setMp(ch.getMaxMp());
            ch.setAttack(ch.getAttack() + 3);
            ch.setDefense(ch.getDefense() + 2);
            ch.setSpeed(ch.getSpeed() + 1);
            ch.setSkillPoints(ch.getSkillPoints() + 2);
        }
    }

    private Combatant toCombatant(Character ch) {
        Combatant c = new Combatant();
        c.setName(ch.getName());
        c.setIcon("🤺");
        c.setColor("#3498db");
        c.setHp(ch.getHp());
        c.setMaxHp(ch.getMaxHp());
        c.setMp(ch.getMp());
        c.setMaxMp(ch.getMaxMp());
        c.setAttack(ch.getAttack());
        c.setDefense(ch.getDefense());
        c.setSpeed(ch.getSpeed());
        c.setSkillName("重击");
        c.setSkillDamage(ch.getAttack() + 10);
        c.setSkillMpCost(10);
        return c;
    }

    private void buildEnemyFromMonster(Combatant enemy, Monster m) {
        enemy.setName(m.getName());
        enemy.setIcon(m.getIcon());
        enemy.setColor(m.getColor());
        enemy.setHp(m.getHp());
        enemy.setMaxHp(m.getHp());
        enemy.setMp(50);
        enemy.setMaxMp(50);
        enemy.setAttack(m.getAttack());
        enemy.setDefense(m.getDefense());
        enemy.setSpeed(m.getSpeed());
        enemy.setSkillName(m.getSkillName());
        enemy.setSkillDamage(m.getSkillDamage() == null ? 0 : m.getSkillDamage());
    }

    private void buildEnemyFromBoss(Combatant enemy, Boss b) {
        enemy.setName(b.getName());
        enemy.setIcon(b.getIcon());
        enemy.setColor(b.getColor());
        enemy.setHp(b.getHp());
        enemy.setMaxHp(b.getMaxHp());
        enemy.setMp(100);
        enemy.setMaxMp(100);
        enemy.setAttack(b.getAttack());
        enemy.setDefense(b.getDefense());
        enemy.setSpeed(b.getSpeed());
        enemy.setSkillName(b.getSkillName());
        enemy.setSkillDamage(b.getSkillDamage() == null ? 0 : b.getSkillDamage());
    }

    private Map<String, Object> snapshot(Combatant c, boolean full) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", c.getName());
        m.put("icon", c.getIcon());
        m.put("color", c.getColor());
        m.put("hp", c.getHp());
        m.put("maxHp", c.getMaxHp());
        m.put("mp", c.getMp());
        m.put("maxMp", c.getMaxMp());
        m.put("alive", c.isAlive());
        if (full) {
            m.put("attack", c.getAttack());
            m.put("defense", c.getDefense());
            m.put("speed", c.getSpeed());
            m.put("skillName", c.getSkillName());
        }
        return m;
    }
}
