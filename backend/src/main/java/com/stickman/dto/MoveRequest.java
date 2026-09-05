package com.stickman.dto;

import lombok.Data;

/**
 * 玩家移动请求
 */
@Data
public class MoveRequest {

    private Long saveId;

    /** 方向: UP/DOWN/LEFT/RIGHT */
    private String direction;
}
