package com.stickman.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建存档请求
 */
@Data
public class SaveCreateRequest {

    @NotBlank(message = "存档名不能为空")
    private String saveName;

    private String characterName;
}
