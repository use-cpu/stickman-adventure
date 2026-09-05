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

    private final GameSaveService gameSaveService;
    private final GameMapGenerator mapGenerator;

    /**
     * 获取存档当前地图及玩家位置、已探索格子
     */
    public Map<String, Object> getMap(Long saveId) {
        GameSave save = gameSaveService.getById(saveId);

        GameMapGenerator.GeneratedMap generated = mapGenerator.generate(save.getMapSeed(), save.getCurrentLevel());
        int[][] tiles = generated.tiles;

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
        // 踩到怪物 → 提示前端触发战斗
        result.put("triggerBattle", tileType == Constants.TILE_MONSTER || tileType == Constants.TILE_BOSS);
        result.put("battleType", tileType == Constants.TILE_BOSS ? Constants.BATTLE_BOSS : Constants.BATTLE_NORMAL);
        return result;
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
            default: return "未知";
        }
    }
}
