-- ============================================================
-- 火柴人探险回合制网页游戏 - 数据库设计
-- ============================================================

DROP DATABASE IF EXISTS stickman_adventure;
CREATE DATABASE stickman_adventure DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE stickman_adventure;

-- ----------------------------
-- 1. 用户表 (玩家 + 管理员)
-- ----------------------------
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
  `nickname`    VARCHAR(50)           COMMENT '昵称',
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'PLAYER' COMMENT '角色: PLAYER/ADMIN',
  `avatar`      VARCHAR(255)          COMMENT '头像URL',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ----------------------------
-- 2. 游戏存档表 (多用户存档管理)
-- ----------------------------
CREATE TABLE `game_save` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT      NOT NULL COMMENT '所属用户',
  `save_name`       VARCHAR(50) NOT NULL COMMENT '存档名称',
  `current_level`   INT         NOT NULL DEFAULT 1  COMMENT '当前关卡',
  `player_x`        INT         NOT NULL DEFAULT 0  COMMENT '玩家在地图上的X坐标',
  `player_y`        INT         NOT NULL DEFAULT 0  COMMENT '玩家在地图上的Y坐标',
  `map_seed`        BIGINT      NOT NULL DEFAULT 0  COMMENT '地图随机种子',
  `explored_tiles`  TEXT                 COMMENT '已探索格子的JSON数组',
  `defeated_monsters` TEXT               COMMENT '已击败小怪的JSON: {"x_y": 击败时间戳}',
  `is_finished`     TINYINT     NOT NULL DEFAULT 0  COMMENT '是否通关 0否 1是',
  `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏存档';

-- ----------------------------
-- 3. 角色属性表 (每个存档一个角色)
-- ----------------------------
CREATE TABLE `game_character` (
  `id`             BIGINT     NOT NULL AUTO_INCREMENT,
  `save_id`        BIGINT     NOT NULL COMMENT '关联存档',
  `name`           VARCHAR(50) NOT NULL COMMENT '角色名',
  `level`          INT        NOT NULL DEFAULT 1  COMMENT '等级',
  `hp`             INT        NOT NULL DEFAULT 100 COMMENT '当前生命值',
  `max_hp`         INT        NOT NULL DEFAULT 100 COMMENT '最大生命值',
  `mp`             INT        NOT NULL DEFAULT 30  COMMENT '当前魔法值',
  `max_mp`         INT        NOT NULL DEFAULT 30  COMMENT '最大魔法值',
  `attack`         INT        NOT NULL DEFAULT 15  COMMENT '攻击力',
  `defense`        INT        NOT NULL DEFAULT 8   COMMENT '防御力',
  `speed`          INT        NOT NULL DEFAULT 10  COMMENT '速度(决定出手顺序)',
  `exp`            INT        NOT NULL DEFAULT 0   COMMENT '经验值',
  `exp_to_next`    INT        NOT NULL DEFAULT 100 COMMENT '升级所需经验',
  `gold`           INT        NOT NULL DEFAULT 0   COMMENT '金币',
  `skill_points`   INT        NOT NULL DEFAULT 0   COMMENT '技能点',
  `skill_bonus`    INT        NOT NULL DEFAULT 0   COMMENT '技能伤害加成',
  `create_time`    DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_save_id` (`save_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色属性';

-- ----------------------------
-- 4. 怪物配置表 (管理员可管理)
-- ----------------------------
CREATE TABLE `monster` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50) NOT NULL COMMENT '怪物名',
  `type`        VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '类型: NORMAL/ELITE/BOSS',
  `level`       INT         NOT NULL DEFAULT 1  COMMENT '等级',
  `hp`          INT         NOT NULL DEFAULT 50 COMMENT '生命值',
  `attack`      INT         NOT NULL DEFAULT 10 COMMENT '攻击力',
  `defense`     INT         NOT NULL DEFAULT 5  COMMENT '防御力',
  `speed`       INT         NOT NULL DEFAULT 8  COMMENT '速度',
  `exp_reward`  INT         NOT NULL DEFAULT 20 COMMENT '经验奖励',
  `gold_reward` INT         NOT NULL DEFAULT 10 COMMENT '金币奖励',
  `skill_name`  VARCHAR(50)          COMMENT '技能名',
  `skill_damage` INT                  COMMENT '技能伤害',
  `color`       VARCHAR(20) NOT NULL DEFAULT '#e74c3c' COMMENT '颜色',
  `icon`        VARCHAR(10) NOT NULL DEFAULT '👹' COMMENT '图标emoji',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='怪物配置';

-- ----------------------------
-- 5. Boss配置表
-- ----------------------------
CREATE TABLE `boss` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(50) NOT NULL COMMENT 'Boss名',
  `level`         INT         NOT NULL COMMENT '出现关卡',
  `hp`            INT         NOT NULL COMMENT '生命值',
  `max_hp`        INT         NOT NULL COMMENT '最大生命值',
  `attack`        INT         NOT NULL COMMENT '攻击力',
  `defense`       INT         NOT NULL COMMENT '防御力',
  `speed`         INT         NOT NULL COMMENT '速度',
  `exp_reward`    INT         NOT NULL COMMENT '经验奖励',
  `gold_reward`   INT         NOT NULL COMMENT '金币奖励',
  `skill_name`    VARCHAR(50)          COMMENT '技能名',
  `skill_damage`  INT                  COMMENT '技能伤害',
  `phase2_hp_threshold` INT  NOT NULL DEFAULT 50 COMMENT '二阶段血量百分比阈值',
  `color`         VARCHAR(20) NOT NULL DEFAULT '#8e44ad',
  `icon`          VARCHAR(10) NOT NULL DEFAULT '🐲',
  `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Boss配置';

-- ----------------------------
-- 6. 战斗日志表
-- ----------------------------
CREATE TABLE `battle_log` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
  `save_id`      BIGINT      NOT NULL COMMENT '关联存档',
  `user_id`      BIGINT      NOT NULL COMMENT '用户',
  `battle_type`  VARCHAR(20) NOT NULL COMMENT '战斗类型: NORMAL/BOSS',
  `enemy_name`   VARCHAR(50) NOT NULL COMMENT '敌人名',
  `result`       VARCHAR(20) NOT NULL COMMENT '结果: WIN/LOSE/FLEE',
  `rounds`       INT         NOT NULL DEFAULT 0 COMMENT '回合数',
  `detail`       TEXT                 COMMENT '战斗详情JSON',
  `exp_gained`   INT         NOT NULL DEFAULT 0,
  `gold_gained`  INT         NOT NULL DEFAULT 0,
  `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_save_id` (`save_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战斗日志';

-- ----------------------------
-- 7. 用户操作日志表
-- ----------------------------
CREATE TABLE `user_log` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL COMMENT '用户',
  `action`      VARCHAR(50) NOT NULL COMMENT '操作类型',
  `detail`      VARCHAR(255)         COMMENT '操作详情',
  `ip`          VARCHAR(50)          COMMENT 'IP地址',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作日志';

-- ----------------------------
-- 8. 商店商品表
-- ----------------------------
CREATE TABLE `shop_item` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL COMMENT '商品名',
  `category`    VARCHAR(20)  NOT NULL COMMENT '分类: WEAPON/ARMOR/BOOTS/ACCESSORY/POTION',
  `price`       INT          NOT NULL DEFAULT 0 COMMENT '价格(金币)',
  `icon`        VARCHAR(10)           COMMENT '图标',
  `description` VARCHAR(200)          COMMENT '描述',
  `stat_type`   VARCHAR(20)  NOT NULL COMMENT '属性: ATTACK/DEFENSE/SPEED/MAX_HP/MAX_MP/HP/MP',
  `stat_value`  INT          NOT NULL DEFAULT 0 COMMENT '属性数值',
  `is_unique`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否唯一(只能买一次)',
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店商品';

-- ----------------------------
-- 9. 角色已购买商品表
-- ----------------------------
CREATE TABLE `character_item` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `save_id`     BIGINT   NOT NULL COMMENT '存档ID',
  `item_id`     BIGINT   NOT NULL COMMENT '商品ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_save_item` (`save_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色已购买商品';

-- ============================================================
-- 初始数据
-- ============================================================

-- 默认管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`username`,`password`,`nickname`,`role`) VALUES
('admin','$2a$10$bf6J.9aj24YzVfww8Lfwge4.wGzoom73EY4/LKdBLOZd.nxjvrWP6','超级管理员','ADMIN');

-- 默认玩家账号 (密码: 123456)
INSERT INTO `sys_user` (`username`,`password`,`nickname`,`role`) VALUES
('player','$2a$10$/bh0hb8ICk3zmgUH2SZXwO73AQLQkc.yBNeuQrWwZw0M0spC17yWe','测试玩家','PLAYER');

-- 普通怪物配置
INSERT INTO `monster` (`name`,`type`,`level`,`hp`,`attack`,`defense`,`speed`,`exp_reward`,`gold_reward`,`skill_name`,`skill_damage`,`color`,`icon`) VALUES
('史莱姆','NORMAL',1,40,8,2,6,15,8,'粘液喷射',12,'#2ecc71','🟢'),
('哥布林','NORMAL',2,60,12,4,10,25,15,'突袭',18,'#27ae60','👺'),
('骷髅兵','NORMAL',3,80,16,6,9,35,22,'骨刺',22,'#bdc3c7','💀'),
('蝙蝠','NORMAL',2,45,14,3,15,20,12,'音波',16,'#34495e','🦇'),
('狼','NORMAL',4,100,20,8,14,45,30,'撕咬',28,'#7f8c8d','🐺'),
('暗影刺客','ELITE',5,140,26,10,18,70,50,'背刺',40,'#2c3e50','🥷'),
('石巨人','ELITE',6,200,22,18,5,90,60,'岩崩',35,'#95a5a6','🗿');

-- Boss配置
INSERT INTO `boss` (`name`,`level`,`hp`,`max_hp`,`attack`,`defense`,`speed`,`exp_reward`,`gold_reward`,`skill_name`,`skill_damage`,`phase2_hp_threshold`,`color`,`icon`) VALUES
('森林守护者',1,300,300,25,12,12,150,100,'藤蔓缠绕',40,50,'#27ae60','🌳'),
('火焰领主',2,500,500,38,18,15,250,180,'烈焰风暴',60,50,'#e67e22','🔥'),
('冰霜女皇',3,800,800,52,25,18,400,300,'绝对零度',90,40,'#3498db','❄️'),
('暗影君王',4,1200,1200,70,35,20,700,500,'虚空吞噬',130,30,'#8e44ad','👑');

-- 商店商品
INSERT INTO `shop_item` (`name`,`category`,`price`,`icon`,`description`,`stat_type`,`stat_value`,`is_unique`,`sort_order`) VALUES
('铁剑','WEAPON',50,'⚔️','锋利的铁剑, 攻击+5','ATTACK',5,1,1),
('钢剑','WEAPON',150,'🗡️','精钢打造, 攻击+12','ATTACK',12,1,2),
('烈焰之刃','WEAPON',400,'🔥','附魔烈焰, 攻击+25','ATTACK',25,1,3),
('皮甲','ARMOR',60,'🥋','轻便皮甲, 防御+4','DEFENSE',4,1,4),
('锁子甲','ARMOR',180,'🛡️','坚固锁甲, 防御+10','DEFENSE',10,1,5),
('龙鳞铠','ARMOR',450,'🐉','龙鳞锻造, 防御+20','DEFENSE',20,1,6),
('疾风靴','BOOTS',80,'👟','风之靴, 速度+5','SPEED',5,1,7),
('风行之靴','BOOTS',220,'🥾','风行加持, 速度+12','SPEED',12,1,8),
('生命护符','ACCESSORY',100,'❤️','生命护符, 最大生命+40','MAX_HP',40,1,9),
('魔力项链','ACCESSORY',120,'💎','魔力项链, 最大魔法+25','MAX_MP',25,1,10),
('小型生命药水','POTION',20,'🧪','恢复50点生命值','HP',50,0,11),
('大型生命药水','POTION',60,'🍷','恢复150点生命值','HP',150,0,12),
('魔法药水','POTION',30,'🔮','恢复40点魔法值','MP',40,0,13),
('大型魔法药水','POTION',80,'🧴','恢复100点魔法值','MP',100,0,14),
('初级技能书','SKILL',100,'📖','技能伤害+5','SKILL_DAMAGE',5,1,15),
('中级技能书','SKILL',250,'📚','技能伤害+15','SKILL_DAMAGE',15,1,16),
('高级技能书','SKILL',500,'📕','技能伤害+30','SKILL_DAMAGE',30,1,17);
