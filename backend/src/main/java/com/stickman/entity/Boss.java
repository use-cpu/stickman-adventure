package com.stickman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Boss配置
 */
@Data
@TableName("boss")
public class Boss {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer level;

    private Integer hp;

    private Integer maxHp;

    private Integer attack;

    private Integer defense;

    private Integer speed;

    private Integer expReward;

    private Integer goldReward;

    private String skillName;

    private Integer skillDamage;

    private Integer phase2HpThreshold;

    private String color;

    private String icon;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
