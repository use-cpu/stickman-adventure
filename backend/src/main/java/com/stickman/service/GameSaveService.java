package com.stickman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stickman.common.BusinessException;
import com.stickman.common.Constants;
import com.stickman.common.UserContext;
import com.stickman.dto.SaveCreateRequest;
import com.stickman.entity.Character;
import com.stickman.entity.GameSave;
import com.stickman.mapper.CharacterMapper;
import com.stickman.mapper.GameSaveMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 游戏存档服务 - 多用户存档管理
 */
@Service
@RequiredArgsConstructor
public class GameSaveService {

    private final GameSaveMapper saveMapper;
    private final CharacterMapper characterMapper;

    /**
     * 查询当前用户的所有存档
     */
    public List<GameSave> listMySaves() {
        Long userId = UserContext.getUserId();
        return saveMapper.selectList(
                new LambdaQueryWrapper<GameSave>()
                        .eq(GameSave::getUserId, userId)
                        .orderByDesc(GameSave::getUpdateTime));
    }

    /**
     * 查询所有用户存档(管理员)
     */
    public List<GameSave> listAllSaves() {
        return saveMapper.selectList(
                new LambdaQueryWrapper<GameSave>().orderByDesc(GameSave::getUpdateTime));
    }

    /**
     * 获取存档详情(含角色)
     */
    public GameSave getDetail(Long saveId) {
        GameSave save = saveMapper.selectById(saveId);
        if (save == null) {
            throw BusinessException.of("存档不存在");
        }
        checkOwnership(save);
        return save;
    }

    /**
     * 创建新存档并初始化角色
     */
    @Transactional
    public GameSave createSave(SaveCreateRequest req) {
        Long userId = UserContext.getUserId();

        GameSave save = new GameSave();
        save.setUserId(userId);
        save.setSaveName(req.getSaveName());
        save.setCurrentLevel(1);
        save.setPlayerX(0);
        save.setPlayerY(0);
        save.setMapSeed(System.currentTimeMillis());
        save.setExploredTiles("[\"0_0\"]");
        save.setIsFinished(0);
        saveMapper.insert(save);

        // 初始化角色
        Character ch = new Character();
        ch.setSaveId(save.getId());
        ch.setName(req.getCharacterName() == null ? "火柴人" : req.getCharacterName());
        ch.setLevel(1);
        ch.setHp(100);
        ch.setMaxHp(100);
        ch.setMp(30);
        ch.setMaxMp(30);
        ch.setAttack(15);
        ch.setDefense(8);
        ch.setSpeed(10);
        ch.setExp(0);
        ch.setExpToNext(100);
        ch.setGold(0);
        ch.setSkillPoints(0);
        characterMapper.insert(ch);

        return save;
    }

    /**
     * 删除存档(同时删除角色)
     */
    @Transactional
    public void deleteSave(Long saveId) {
        GameSave save = saveMapper.selectById(saveId);
        if (save == null) {
            throw BusinessException.of("存档不存在");
        }
        checkOwnership(save);
        characterMapper.delete(
                new LambdaQueryWrapper<Character>().eq(Character::getSaveId, saveId));
        saveMapper.deleteById(saveId);
    }

    /**
     * 更新存档(角色坐标、关卡、探索状态等)
     */
    public void updateSave(GameSave save) {
        saveMapper.updateById(save);
    }

    /**
     * 校验存档归属权
     */
    public void checkOwnership(GameSave save) {
        if (UserContext.isAdmin()) {
            return;
        }
        if (!save.getUserId().equals(UserContext.getUserId())) {
            throw BusinessException.forbidden("无权操作他人存档");
        }
    }

    public GameSave getById(Long saveId) {
        GameSave save = saveMapper.selectById(saveId);
        if (save == null) {
            throw BusinessException.of("存档不存在");
        }
        checkOwnership(save);
        return save;
    }
}
