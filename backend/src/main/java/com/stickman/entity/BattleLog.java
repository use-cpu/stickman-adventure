package com.stickman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 战斗日志
 */
@Data
@TableName("battle_log")
public class BattleLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long saveId;

    private Long userId;

    private String battleType;

    private String enemyName;

    private String result;

    private Integer rounds;

    private String detail;

    private Integer expGained;

    private Integer goldGained;

    private LocalDateTime createTime;
}
