package com.stickman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 角色属性
 */
@Data
@TableName("game_character")
@Alias("PlayerCharacter")
public class Character {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long saveId;

    private String name;

    private Integer level;

    private Integer hp;

    private Integer maxHp;

    private Integer mp;

    private Integer maxMp;

    private Integer attack;

    private Integer defense;

    private Integer speed;

    private Integer exp;

    private Integer expToNext;

    private Integer gold;

    private Integer skillPoints;

    /** 技能伤害加成 (通过技能书获得) */
    private Integer skillBonus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
