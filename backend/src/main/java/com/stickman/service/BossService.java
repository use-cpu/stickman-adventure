package com.stickman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stickman.common.BusinessException;
import com.stickman.entity.Boss;
import com.stickman.mapper.BossMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Boss配置管理服务
 */
@Service
@RequiredArgsConstructor
public class BossService {

    private final BossMapper bossMapper;

    public List<Boss> list() {
        return bossMapper.selectList(
                new LambdaQueryWrapper<Boss>().orderByAsc(Boss::getLevel));
    }

    public Boss getById(Long id) {
        Boss b = bossMapper.selectById(id);
        if (b == null) {
            throw BusinessException.of("Boss不存在");
        }
        return b;
    }

    /**
     * 根据关卡获取该关Boss
     */
    public Boss getByLevel(Integer level) {
        Boss b = bossMapper.selectOne(
                new LambdaQueryWrapper<Boss>().eq(Boss::getLevel, level).last("LIMIT 1"));
        return b;
    }

    public Boss create(Boss b) {
        bossMapper.insert(b);
        return b;
    }

    public Boss update(Boss b) {
        if (b.getId() == null) {
            throw BusinessException.of("ID不能为空");
        }
        bossMapper.updateById(b);
        return b;
    }

    public void delete(Long id) {
        bossMapper.deleteById(id);
    }
}
