package com.stickman;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 火柴人探险回合制网页游戏 - 启动类
 */
@SpringBootApplication
@MapperScan("com.stickman.mapper")
public class StickmanAdventureApplication {

    public static void main(String[] args) {
        SpringApplication.run(StickmanAdventureApplication.class, args);
    }
}
