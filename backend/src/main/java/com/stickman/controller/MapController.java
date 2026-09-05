package com.stickman.controller;

import com.stickman.common.Result;
import com.stickman.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 网格地图探险接口
 */
@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    /** 获取地图 */
    @GetMapping("/save/{saveId}")
    public Result<Map<String, Object>> getMap(@PathVariable Long saveId) {
        return Result.success(mapService.getMap(saveId));
    }

    /** 移动: direction = UP/DOWN/LEFT/RIGHT */
    @PostMapping("/save/{saveId}/move")
    public Result<Map<String, Object>> move(@PathVariable Long saveId, @RequestParam String direction) {
        return Result.success(mapService.move(saveId, direction));
    }

    /** 拾取宝箱 */
    @PostMapping("/save/{saveId}/treasure")
    public Result<Map<String, Object>> openTreasure(@PathVariable Long saveId) {
        return Result.success(mapService.openTreasure(saveId));
    }
}
