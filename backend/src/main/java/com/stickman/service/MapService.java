package com.stickman.service;

import com.stickman.common.Constants;
import com.stickman.common.BusinessException;
import com.stickman.entity.GameSave;
import com.stickman.game.GameMapGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 网格地图探险服务
 *
 * 负责地图生成、玩家移动、探索状态管理。
 * 地图基于存档的 mapSeed + 关卡 确定性生成,保证同一存档地图一致。
 */
@Service
@RequiredArgsConstructor
public class MapService {

    /** 小怪刷新时间: 击败后 1 分钟 (60秒) 重新出现 */
    private static final long MONSTER_RESPAWN_MS = 60_000L;

    private final GameSaveService gameSaveService;
    private final GameMapGenerator mapGenerator;

    /**
     * 获取存档当前地图及玩家位置、已探索格子
     */
    public Map<String, Object> getMap(Long saveId) {
        GameSave save = gameSaveService.getById(saveId);

        GameMapGenerator.GeneratedMap generated = mapGenerator.generate(save.getMapSeed(), save.getCurrentLevel());
        int[][] tiles = generated.tiles;

        // 应用已击败小怪遮罩(1分钟内击败的小怪显示为空地)
        boolean changed = applyDefeatedOverlay(tiles, save);
        if (changed) {
            gameSaveService.updateSave(save);
        }

        // 解析已探索格子
        Set<String> explored = parseExplored(save.getExploredTiles());
        // 自动探索玩家周围(九宫格可见)
        revealAround(tiles, save.getPlayerX(), save.getPlayerY(), explored);

        // 构建前端可见地图(未探索的隐藏为 -1,即战争迷雾)
        int size = tiles.length;
        int[][] visible = new int[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                String key = x + "_" + y;
                if (explored.contains(key)) {
                    visible[y][x] = tiles[y][x];
                } else {
                    visible[y][x] = -1; // 战争迷雾
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("size", size);
        result.put("tiles", visible);
        result.put("playerX", save.getPlayerX());
        result.put("playerY", save.getPlayerY());
        result.put("currentLevel", save.getCurrentLevel());
        result.put("explored", explored);
        result.put("isFinished", save.getIsFinished());
        // 持久化探索状态(把九宫格探索结果存回去)
        save.setExploredTiles(toJsonArray(explored));
        gameSaveService.updateSave(save);
        return result;
    }

    /**
     * 玩家移动一格
     * @return 移动结果: 是否可移动、移动后踩到的格子类型(可能触发战斗/宝箱)
     */
    public Map<String, Object> move(Long saveId, String direction) {
        GameSave save = gameSaveService.getById(saveId);

        GameMapGenerator.GeneratedMap generated = mapGenerator.generate(save.getMapSeed(), save.getCurrentLevel());
        int[][] tiles = generated.tiles;

        // 应用已击败小怪遮罩(1分钟内击败的小怪视为空地, 不会触发战斗)
        applyDefeatedOverlay(tiles, save);

        int nx = save.getPlayerX();
        int ny = save.getPlayerY();
        switch (direction.toUpperCase()) {
            case "UP":    ny -= 1; break;
            case "DOWN":  ny += 1; break;
            case "LEFT":  nx -= 1; break;
            case "RIGHT": nx += 1; break;
            default: throw BusinessException.of("未知方向: " + direction);
        }

        Map<String, Object> result = new HashMap<>();
        if (!mapGenerator.canMove(tiles, nx, ny)) {
            result.put("moved", false);
            result.put("message", "前方无法通行(边界或墙壁)");
            return result;
        }

        save.setPlayerX(nx);
        save.setPlayerY(ny);

        // 探索新位置
        Set<String> explored = parseExplored(save.getExploredTiles());
        revealAround(tiles, nx, ny, explored);
        save.setExploredTiles(toJsonArray(explored));

        int tileType = tiles[ny][nx];
        gameSaveService.updateSave(save);

        result.put("moved", true);
        result.put("playerX", nx);
        result.put("playerY", ny);
        result.put("tileType", tileType);
        result.put("tileName", tileName(tileType));
        // 踩到怪物 → 提示前端触发战斗 (已击败且未刷新的小怪已被遮罩为空地, 不会触发)
        result.put("triggerBattle", tileType == Constants.TILE_MONSTER || tileType == Constants.TILE_BOSS);
        result.put("battleType", tileType == Constants.TILE_BOSS ? Constants.BATTLE_BOSS : Constants.BATTLE_NORMAL);
        // 踩到商店 → 提示前端打开商店弹窗
        result.put("triggerShop", tileType == Constants.TILE_SHOP);
        return result;
    }

    /**
     * 记录击败一只小怪(普通怪), 写入存档的 defeatedMonsters
     * Boss 不记录 (Boss 击败后进入下一关, 地图整体刷新)
     */
    public void recordMonsterDefeat(Long saveId) {
        GameSave save = gameSaveService.getById(saveId);
        Map<String, Long> defeated = parseDefeated(save.getDefeatedMonsters());
        String key = save.getPlayerX() + "_" + save.getPlayerY();
        defeated.put(key, System.currentTimeMillis());
        save.setDefeatedMonsters(toDefeatedJson(defeated));
        gameSaveService.updateSave(save);
    }

    /**
     * 拾取宝箱: 加金币,并把格子改为空地
     */
    public Map<String, Object> openTreasure(Long saveId) {
        GameSave save = gameSaveService.getById(saveId);
        GameMapGenerator.GeneratedMap generated = mapGenerator.generate(save.getMapSeed(), save.getCurrentLevel());
        int[][] tiles = generated.tiles;
        int x = save.getPlayerX();
        int y = save.getPlayerY();

        Map<String, Object> result = new HashMap<>();
        if (tiles[y][x] != Constants.TILE_TREASURE) {
            result.put("opened", false);
            result.put("message", "当前位置没有宝箱");
            return result;
        }
        // 宝箱奖励: 随机金币 + 概率回血
        Random rng = new Random(save.getMapSeed() + x * 7L + y * 13L);
        int gold = 10 + rng.nextInt(40);
        int heal = rng.nextInt(30);
        tiles[y][x] = Constants.TILE_EMPTY; // 标记已开(本会话内)
        result.put("opened", true);
        result.put("gold", gold);
        result.put("heal", heal);
        result.put("message", "获得 " + gold + " 金币, 回复 " + heal + " 点生命值");
        return result;
    }

    // ====== 私有辅助 ======

    /**
     * 应用已击败小怪遮罩:
     * - 击败时间在 1 分钟内的小怪格子 → 改为空地
     * - 超过 1 分钟的 → 从记录中移除(小怪已刷新)
     * @return 是否有数据变更(需要持久化)
     */
    private boolean applyDefeatedOverlay(int[][] tiles, GameSave save) {
        Map<String, Long> defeated = parseDefeated(save.getDefeatedMonsters());
        if (defeated.isEmpty()) return false;

        long now = System.currentTimeMillis();
        boolean changed = false;
        Iterator<Map.Entry<String, Long>> it = defeated.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            String key = e.getKey();
            long defeatTime = e.getValue();
            if (now - defeatTime >= MONSTER_RESPAWN_MS) {
                // 超过刷新时间, 小怪复活, 移除记录
                it.remove();
                changed = true;
            } else {
                // 仍在冷却期, 该格子显示为空地
                String[] parts = key.split("_");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                if (y >= 0 && y < tiles.length && x >= 0 && x < tiles[0].length) {
                    tiles[y][x] = Constants.TILE_EMPTY;
                }
            }
        }
        if (changed) {
            save.setDefeatedMonsters(toDefeatedJson(defeated));
        }
        return changed;
    }

    /**
     * 解析已击败小怪JSON: {"x_y": 时间戳, ...}
     */
    private Map<String, Long> parseDefeated(String json) {
        Map<String, Long> map = new HashMap<>();
        if (json == null || json.isEmpty() || "{}".equals(json)) {
            return map;
        }
        // 简单解析 {"x_y":timestamp,...} 格式
        String content = json.replace("{", "").replace("}", "").replace("\"", "");
        for (String pair : content.split(",")) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) continue;
            String[] kv = trimmed.split(":");
            if (kv.length == 2) {
                try {
                    map.put(kv[0].trim(), Long.parseLong(kv[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    private String toDefeatedJson(Map<String, Long> map) {
        if (map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Long> e : map.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    private void revealAround(int[][] tiles, int px, int py, Set<String> explored) {
        int size = tiles.length;
        // 视野半径: 以玩家为中心向外扩展 2 格, 形成 5x5 的可见区域
        int radius = 2;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int x = px + dx;
                int y = py + dy;
                if (x >= 0 && x < size && y >= 0 && y < size) {
                    explored.add(x + "_" + y);
                }
            }
        }
    }

    private Set<String> parseExplored(String json) {
        if (json == null || json.isEmpty()) {
            return new HashSet<>();
        }
        // 简单解析 ["x_y", ...] 格式
        String content = json.replace("[", "").replace("]", "").replace("\"", "");
        Set<String> set = new HashSet<>();
        for (String s : content.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }

    private String toJsonArray(Set<String> set) {
        if (set.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (String s : set) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(s).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private String tileName(int type) {
        switch (type) {
            case Constants.TILE_EMPTY: return "空地";
            case Constants.TILE_MONSTER: return "怪物";
            case Constants.TILE_TREASURE: return "宝箱";
            case Constants.TILE_BOSS: return "Boss";
            case Constants.TILE_EXIT: return "出口";
            case Constants.TILE_WALL: return "墙壁";
            case Constants.TILE_SHOP: return "商店";
            default: return "未知";
        }
    }
}
