package com.stickman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 怪物配置
 */
@Data
@TableName("monster")
public class Monster {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private Integer level;

    private Integer hp;

    private Integer attack;

    private Integer defense;

    private Integer speed;

    private Integer expReward;

    private Integer goldReward;

    private String skillName;

    private Integer skillDamage;

    private String color;

    private String icon;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
