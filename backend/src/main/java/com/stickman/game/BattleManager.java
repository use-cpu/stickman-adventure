package com.stickman.game;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战斗会话管理器 - 在内存中维护进行中的战斗
 */
@Component
public class BattleManager {

    /** key = saveId, 每个存档同时只能有一场进行中的战斗 */
    private final Map<Long, BattleSession> sessions = new ConcurrentHashMap<>();

    public void start(BattleSession session) {
        sessions.put(session.getSaveId(), session);
    }

    public BattleSession get(Long saveId) {
        return sessions.get(saveId);
    }

    public BattleSession getOrFail(Long saveId) {
        BattleSession s = sessions.get(saveId);
        if (s == null) {
            throw new IllegalStateException("当前没有进行中的战斗");
        }
        return s;
    }

    public void remove(Long saveId) {
        sessions.remove(saveId);
    }

    public boolean hasBattle(Long saveId) {
        return sessions.containsKey(saveId);
    }
}
