package com.stickman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户操作日志
 */
@Data
@TableName("user_log")
public class UserLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String action;

    private String detail;

    private String ip;

    private LocalDateTime createTime;
}
