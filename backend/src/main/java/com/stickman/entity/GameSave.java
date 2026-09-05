package com.stickman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 游戏存档
 */
@Data
@TableName("game_save")
public class GameSave {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String saveName;

    private Integer currentLevel;

    private Integer playerX;

    private Integer playerY;

    private Long mapSeed;

    private String exploredTiles;

    private Integer isFinished;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
