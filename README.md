# 🤺 火柴人探险 · 回合制网页游戏

基于 **SpringBoot + Vue3 + MySQL + MyBatis-Plus** 的火柴人探险回合制网页小游戏。玩家在网格地图上探索、遭遇怪物进行回合制战斗、击败各层 Boss 通关；管理员可配置怪物/Boss、管理用户与查看日志。

## ✨ 功能特性

| 模块 | 说明 |
| --- | --- |
| 注册登录 | JWT 鉴权，BCrypt 密码加密，区分玩家/管理员角色 |
| 多用户存档管理 | 每个用户可创建多个独立存档，含角色、坐标、关卡、地图种子 |
| 网格地图探险 | 10×10 程序化生成地图，战争迷雾，方向键移动，宝箱拾取 |
| 角色属性管理 | 等级/HP/MP/攻防速/经验/金币/技能点，升级自动提升并可分配技能点 |
| 回合制战斗系统 | 速度决定先手，普攻/技能/防御/逃跑，10% 暴击，伤害浮动 |
| Boss 对战 | 各层专属 Boss，血量低于阈值进入二阶段狂暴，击败通关进入下一层 |
| 日志查看 | 玩家查看自身战斗日志；管理员查看全部战斗日志与用户操作日志 |
| 怪物配置管理 | 管理员对怪物/Boss 进行增删改查 |

## 🧱 技术栈

- **后端**：SpringBoot 2.7.18 · MyBatis-Plus 3.5.3 · MySQL 8 · JWT · BCrypt · Lombok · Hutool
- **前端**：Vue 3.4 · Vite 5 · Vue Router 4 · Pinia 2 · Element Plus · Axios

## 📂 目录结构

```
stickman-adventure/
├── backend/                      # SpringBoot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/stickman/
│       │   ├── StickmanAdventureApplication.java
│       │   ├── config/           # MyBatis-Plus / CORS / Web 拦截器配置
│       │   ├── common/          # Result / JwtUtil / 拦截器 / 异常 / 上下文
│       │   ├── entity/           # 7 个实体类
│       │   ├── mapper/          # MyBatis-Plus Mapper
│       │   ├── dto/             # 请求 DTO
│       │   ├── game/            # 战斗引擎 / 战斗会话 / 地图生成器
│       │   ├── service/         # 业务服务层
│       │   └── controller/      # REST 控制器
│       └── resources/application.yml
├── frontend/                     # Vue3 前端
│   ├── package.json  vite.config.js  index.html
│   └── src/
│       ├── main.js  App.vue  router/  store/  api/
│       ├── assets/main.css
│       ├── components/NavBar.vue
│       └── views/  Login.vue  Register.vue  Home.vue  Game.vue  Admin.vue
└── db/schema.sql                 # 数据库脚本(含初始数据)
```

## 🚀 快速开始

### 1. 准备环境
- JDK 17+、Maven 3.8+、Node 18+、MySQL 8+

### 2. 初始化数据库
```bash
mysql -uroot -p < db/schema.sql
```
脚本会创建 `stickman_adventure` 库并写入 7 张表与初始数据（怪物、Boss、管理员/玩家账号）。

> 默认数据库连接：`root/root`，端口 `3306`。如需修改，编辑 `backend/src/main/resources/application.yml`。

### 3. 启动后端
```bash
cd backend
mvn spring-boot:run
# 或打包后运行: mvn package -DskipTests && java -jar target/stickman-adventure-1.0.0.jar
```
后端默认运行在 `http://localhost:8080/api`。

### 4. 启动前端
```bash
cd frontend
npm install
npm run dev
```
前端默认运行在 `http://localhost:5173`（已配置 `/api` 代理到后端）。

### 5. 演示账号
| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin123` |
| 玩家 | `player` | `123456` |

`schema.sql` 中已内置上述账号的真实 BCrypt 密码哈希，导入后即可直接登录。

## 🎮 玩法说明

1. 登录后在「游戏大厅」新建存档并进入。
2. 地图使用方向按钮（或将来支持键盘）移动火柴人，九宫格自动揭开迷雾。
3. 踩到怪物格子触发战斗：普攻 / 技能（消耗 MP）/ 防御（减半受击）/ 逃跑。
4. 击败敌人获得经验金币，升级解锁技能点可分配属性。
5. 击败每层右下角的 Boss 即通关，进入下一层，血蓝回满。
6. 战败则回到起点，损失一半金币。

## 🔌 主要 API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/register` | 注册 |
| GET  | `/api/auth/me` | 当前用户 |
| GET/POST/DELETE | `/api/saves` | 存档增删查 |
| GET/POST | `/api/character/save/{saveId}` | 角色属性 / 分配技能点 |
| GET/POST | `/api/map/save/{saveId}` | 地图 / 移动 / 宝箱 |
| POST | `/api/combat/start` | 开始战斗 |
| POST | `/api/combat/save/{saveId}/action` | 回合动作 |
| GET/PUT/DELETE | `/api/monsters`、`/api/bosses` | 怪物/Boss 配置(管理员) |
| GET | `/api/logs/battle/mine`、`/api/logs/battle/all`、`/api/logs/user` | 日志 |
| GET/PUT | `/api/admin/users` | 用户管理(管理员) |

## 📝 备注

- 战斗会话保存在后端内存（`BattleManager`），每个存档同时仅允许一场进行中的战斗。
- 地图基于存档 `mapSeed + 关卡` 确定性生成，保证同一存档地图布局一致。
- 前端生产构建：`npm run build`，产物在 `frontend/dist`，可由后端或 Nginx 静态托管。
