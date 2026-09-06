package com.stickman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色已购买商品
 */
@Data
@TableName("character_item")
public class CharacterItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long saveId;

    private Long itemId;

    private LocalDateTime createTime;
}
