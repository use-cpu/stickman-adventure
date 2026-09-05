package com.stickman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stickman.common.BusinessException;
import com.stickman.entity.Monster;
import com.stickman.mapper.MonsterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 怪物配置管理服务(管理员可增删改查)
 */
@Service
@RequiredArgsConstructor
public class MonsterService {

    private final MonsterMapper monsterMapper;

    public List<Monster> list(String type) {
        LambdaQueryWrapper<Monster> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Monster::getType, type);
        }
        return monsterMapper.selectList(wrapper.orderByAsc(Monster::getLevel));
    }

    public Monster getById(Long id) {
        Monster m = monsterMapper.selectById(id);
        if (m == null) {
            throw BusinessException.of("怪物不存在");
        }
        return m;
    }

    public Monster create(Monster m) {
        monsterMapper.insert(m);
        return m;
    }

    public Monster update(Monster m) {
        if (m.getId() == null) {
            throw BusinessException.of("ID不能为空");
        }
        monsterMapper.updateById(m);
        return m;
    }

    public void delete(Long id) {
        monsterMapper.deleteById(id);
    }
}
