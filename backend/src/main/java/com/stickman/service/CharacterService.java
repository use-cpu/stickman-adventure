package com.stickman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stickman.common.BusinessException;
import com.stickman.entity.Character;
import com.stickman.mapper.CharacterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 角色属性管理服务
 */
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterMapper characterMapper;
    private final GameSaveService gameSaveService;

    /**
     * 根据存档ID获取角色
     */
    public Character getBySaveId(Long saveId) {
        gameSaveService.getById(saveId);
        Character ch = characterMapper.selectOne(
                new LambdaQueryWrapper<Character>().eq(Character::getSaveId, saveId));
        if (ch == null) {
            throw BusinessException.of("角色不存在");
        }
        return ch;
    }

    /**
     * 升级属性分配: 消耗技能点提升属性
     * @param attr ATTACK / DEFENSE / SPEED / MAX_HP / MAX_MP
     */
    public Character allocateStat(Long saveId, String attr) {
        Character ch = getBySaveId(saveId);
        if (ch.getSkillPoints() <= 0) {
            throw BusinessException.of("技能点不足");
        }
        switch (attr.toUpperCase()) {
            case "ATTACK":
                ch.setAttack(ch.getAttack() + 3);
                break;
            case "DEFENSE":
                ch.setDefense(ch.getDefense() + 2);
                break;
            case "SPEED":
                ch.setSpeed(ch.getSpeed() + 2);
                break;
            case "MAX_HP":
                ch.setMaxHp(ch.getMaxHp() + 20);
                ch.setHp(ch.getHp() + 20);
                break;
            case "MAX_MP":
                ch.setMaxMp(ch.getMaxMp() + 10);
                ch.setMp(ch.getMp() + 10);
                break;
            default:
                throw BusinessException.of("未知属性: " + attr);
        }
        ch.setSkillPoints(ch.getSkillPoints() - 1);
        characterMapper.updateById(ch);
        return ch;
    }

    /**
     * 战斗结束后同步角色数据
     */
    public void saveCharacter(Character ch) {
        characterMapper.updateById(ch);
    }
}
