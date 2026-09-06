package com.stickman.common;

/**
 * 系统常量
 */
public class Constants {

    /** 角色类型 */
    public static final String ROLE_PLAYER = "PLAYER";
    public static final String ROLE_ADMIN = "ADMIN";

    /** 怪物类型 */
    public static final String MONSTER_NORMAL = "NORMAL";
    public static final String MONSTER_ELITE = "ELITE";
    public static final String MONSTER_BOSS = "BOSS";

    /** 战斗类型 */
    public static final String BATTLE_NORMAL = "NORMAL";
    public static final String BATTLE_BOSS = "BOSS";

    /** 战斗结果 */
    public static final String RESULT_WIN = "WIN";
    public static final String RESULT_LOSE = "LOSE";
    public static final String RESULT_FLEE = "FLEE";

    /** 地图配置 */
    public static final int MAP_SIZE = 10;          // 地图大小 10x10
    public static final int TILE_EMPTY = 0;         // 空地
    public static final int TILE_MONSTER = 1;       // 普通怪物
    public static final int TILE_TREASURE = 2;      // 宝箱
    public static final int TILE_BOSS = 3;          // Boss
    public static final int TILE_EXIT = 4;          // 出口
    public static final int TILE_WALL = 5;          // 墙壁
    public static final int TILE_SHOP = 6;          // 商店

    /** 上下文键 */
    public static final String CTX_USER_ID = "userId";
    public static final String CTX_USERNAME = "username";
    public static final String CTX_ROLE = "role";
}
