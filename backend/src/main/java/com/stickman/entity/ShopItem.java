package com.stickman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商店商品
 */
@Data
@TableName("shop_item")
public class ShopItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 分类: WEAPON/ARMOR/BOOTS/ACCESSORY/POTION */
    private String category;

    private Integer price;

    private String icon;

    private String description;

    /** 属性: ATTACK/DEFENSE/SPEED/MAX_HP/MAX_MP/HP/MP */
    private String statType;

    private Integer statValue;

    /** 是否唯一(只能买一次) */
    private Integer isUnique;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
