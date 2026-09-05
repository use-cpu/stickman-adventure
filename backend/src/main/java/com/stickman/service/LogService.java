package com.stickman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stickman.common.Constants;
import com.stickman.common.UserContext;
import com.stickman.entity.BattleLog;
import com.stickman.entity.UserLog;
import com.stickman.mapper.BattleLogMapper;
import com.stickman.mapper.UserLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 日志服务: 战斗日志 + 用户操作日志
 */
@Service
@RequiredArgsConstructor
public class LogService {

    private final BattleLogMapper battleLogMapper;
    private final UserLogMapper userLogMapper;

    /**
     * 查询当前用户的战斗日志
     */
    public List<BattleLog> listMyBattleLogs(Long saveId) {
        LambdaQueryWrapper<BattleLog> wrapper = new LambdaQueryWrapper<BattleLog>()
                .eq(BattleLog::getUserId, UserContext.getUserId())
                .orderByDesc(BattleLog::getCreateTime);
        if (saveId != null) {
            wrapper.eq(BattleLog::getSaveId, saveId);
        }
        return battleLogMapper.selectList(wrapper);
    }

    /**
     * 查询所有战斗日志(管理员)
     */
    public Page<BattleLog> listAllBattleLogs(int page, int size) {
        return battleLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<BattleLog>().orderByDesc(BattleLog::getCreateTime));
    }

    /**
     * 查询所有用户操作日志(管理员)
     */
    public Page<UserLog> listUserLogs(int page, int size) {
        return userLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserLog>().orderByDesc(UserLog::getCreateTime));
    }

    /**
     * 记录一条战斗日志
     */
    public void recordBattleLog(BattleLog log) {
        battleLogMapper.insert(log);
    }

    /**
     * 记录一条用户操作日志
     */
    public void recordUserLog(String action, String detail) {
        UserLog log = new UserLog();
        log.setUserId(UserContext.getUserId());
        log.setAction(action);
        log.setDetail(detail);
        userLogMapper.insert(log);
    }
}
