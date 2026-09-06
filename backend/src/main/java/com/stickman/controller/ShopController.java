package com.stickman.controller;

import com.stickman.common.Result;
import com.stickman.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商店接口 - 购买装备和药水
 */
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /** 获取商店商品列表(含购买状态) */
    @GetMapping("/items")
    public Result<Map<String, Object>> listItems(@RequestParam Long saveId) {
        return Result.success(shopService.listItems(saveId));
    }

    /** 获取该存档已购买的物品(背包) */
    @GetMapping("/inventory")
    public Result<List<Map<String, Object>>> getInventory(@RequestParam Long saveId) {
        return Result.success(shopService.getInventory(saveId));
    }

    /** 购买商品 */
    @PostMapping("/buy")
    public Result<Map<String, Object>> buy(@RequestParam Long saveId, @RequestParam Long itemId) {
        return Result.success(shopService.buy(saveId, itemId));
    }
}
