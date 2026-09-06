package com.stickman.game;

import com.stickman.common.Constants;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 网格地图生成器
 * 基于 seed + level 生成确定性的 10x10 地图,保证同一存档重新进入地图布局一致。
 *
 * 格子类型:
 *  0 空地  1 普通怪物  2 宝箱  3 Boss  4 出口  5 墙壁
 */
@Component
public class GameMapGenerator {

    public static class GeneratedMap {
        public int[][] tiles;
        public int size;
    }

    /**
     * 生成地图
     * @param seed  随机种子
     * @param level 当前关卡(1开始)
     */
    public GeneratedMap generate(long seed, int level) {
        GeneratedMap map = new GeneratedMap();
        map.size = Constants.MAP_SIZE;
        map.tiles = new int[map.size][map.size];

        Random rng = new Random(seed + level * 31L);

        // 初始化为空地
        for (int y = 0; y < map.size; y++) {
            for (int x = 0; x < map.size; x++) {
                map.tiles[y][x] = Constants.TILE_EMPTY;
            }
        }

        // 周边放少量墙壁
        int wallCount = 6 + level;
        for (int i = 0; i < wallCount; i++) {
            int wx = rng.nextInt(map.size);
            int wy = rng.nextInt(map.size);
            // 不在起点放墙
            if (!(wx == 0 && wy == 0)) {
                map.tiles[wy][wx] = Constants.TILE_WALL;
            }
        }

        // 普通怪物 (随关卡递增)
        int monsterCount = 4 + level;
        placeRandomTile(map, rng, monsterCount, Constants.TILE_MONSTER, 0, 0);

        // 宝箱
        int treasureCount = 2 + level / 2;
        placeRandomTile(map, rng, treasureCount, Constants.TILE_TREASURE, 0, 0);

        // 商店 (每层1个)
        placeRandomTile(map, rng, 1, Constants.TILE_SHOP, 0, 0);

        // Boss 放在右下角区域
        map.tiles[map.size - 1][map.size - 1] = Constants.TILE_BOSS;

        // 出口(Boss被击败后激活,这里预置)
        // 出口设在与Boss对称的另一角附近,但默认隐藏,通关后由逻辑处理

        return map;
    }

    private void placeRandomTile(GeneratedMap map, Random rng, int count, int tileType, int avoidX, int avoidY) {
        int placed = 0;
        int attempts = 0;
        while (placed < count && attempts < 200) {
            attempts++;
            int x = rng.nextInt(map.size);
            int y = rng.nextInt(map.size);
            // 避开起点和Boss位置
            if (x == avoidX && y == avoidY) continue;
            if (x == map.size - 1 && y == map.size - 1) continue;
            if (map.tiles[y][x] != Constants.TILE_EMPTY) continue;
            map.tiles[y][x] = tileType;
            placed++;
        }
    }

    /**
     * 检查移动是否合法
     */
    public boolean canMove(int[][] tiles, int x, int y) {
        int size = tiles.length;
        if (x < 0 || x >= size || y < 0 || y >= size) {
            return false;
        }
        return tiles[y][x] != Constants.TILE_WALL;
    }
}
