<template>
  <div class="game-page">
    <NavBar />
    <div class="game-content">
      <!-- 左侧: 角色面板 -->
      <div class="panel side-panel">
        <h3 class="title">📜 角色信息</h3>
        <div v-if="character" class="char-info">
          <div class="char-name">{{ character.name }} <span class="lv">Lv.{{ character.level }}</span></div>
          <div class="stat-row"><span>HP</span><el-progress :percentage="hpPct" :color="'#e74c3c'" :show-text="false" /></div>
          <div class="stat-text">{{ character.hp }} / {{ character.maxHp }}</div>
          <div class="stat-row"><span>MP</span><el-progress :percentage="mpPct" :color="'#9b59b6'" :show-text="false" /></div>
          <div class="stat-text">{{ character.mp }} / {{ character.maxMp }}</div>
          <div class="stat-grid">
            <div>攻击: {{ character.attack }}</div>
            <div>防御: {{ character.defense }}</div>
            <div>速度: {{ character.speed }}</div>
            <div>金币: {{ character.gold }}</div>
            <div>经验: {{ character.exp }}/{{ character.expToNext }}</div>
            <div>技能点: {{ character.skillPoints }}</div>
          </div>
          <div v-if="character.skillPoints > 0" class="alloc">
            <el-button size="small" type="warning" @click="alloc('ATTACK')">+攻击</el-button>
            <el-button size="small" type="warning" @click="alloc('DEFENSE')">+防御</el-button>
            <el-button size="small" type="warning" @click="alloc('SPEED')">+速度</el-button>
            <el-button size="small" type="warning" @click="alloc('MAX_HP')">+生命</el-button>
            <el-button size="small" type="warning" @click="alloc('MAX_MP')">+魔法</el-button>
          </div>
        </div>
        <el-divider />
        <div class="map-info">
          <div>关卡: 第 {{ save?.currentLevel }} 层</div>
          <div>坐标: ({{ save?.playerX }}, {{ save?.playerY }})</div>
          <div class="legend">
            <div><span class="lg player"></span>玩家</div>
            <div><span class="lg monster"></span>怪物</div>
            <div><span class="lg treasure"></span>宝箱</div>
            <div><span class="lg boss"></span>Boss</div>
            <div><span class="lg wall"></span>墙壁</div>
            <div><span class="lg shop"></span>商店</div>
          </div>
        </div>
      </div>

      <!-- 中间: 地图 / 战斗 -->
      <div class="panel main-panel">
        <!-- 战斗模式 -->
        <div v-if="inBattle" class="battle-scene">
          <h3 class="title">⚔️ {{ battle.battleType === 'BOSS' ? 'Boss 战斗' : '遭遇战斗' }}</h3>
          <div class="fighters">
            <div class="fighter" :style="{ background: rgbaColor('#3498db') }">
              <div class="emoji">🤺</div>
              <div>{{ battle.player?.name }}</div>
              <div class="hp-bar"><div class="fill" :style="{ width: pHPct + '%' }"></div><div class="text">{{ battle.player?.hp }}/{{ battle.player?.maxHp }}</div></div>
              <div class="mp-text">MP {{ battle.player?.mp }}/{{ battle.player?.maxMp }}</div>
            </div>
            <div class="vs">VS</div>
            <div class="fighter" :style="{ background: rgbaColor(battle.enemy?.color) }">
              <div class="emoji">{{ battle.enemy?.icon }}</div>
              <div>{{ battle.enemy?.name }} <span v-if="battle.bossPhase2" class="rage">狂暴!</span></div>
              <div class="hp-bar"><div class="fill" :style="{ width: eHPct + '%' }"></div><div class="text">{{ battle.enemy?.hp }}/{{ battle.enemy?.maxHp }}</div></div>
            </div>
          </div>
          <div class="battle-log" ref="logBox">{{ battleLog }}</div>
          <div v-if="!battle.finished" class="actions">
            <button class="action-btn attack" @click="act('ATTACK')">⚔️ 普通攻击</button>
            <button class="action-btn skill" @click="act('SKILL')" :disabled="(battle.player?.mp||0) < 10">✨ 技能</button>
            <button class="action-btn defend" @click="act('DEFEND')">🛡️ 防御</button>
            <button class="action-btn flee" @click="act('FLEE')" v-if="battle.battleType !== 'BOSS'">🏃 逃跑</button>
          </div>
          <div v-else class="result-box">
            <h2 :class="resultClass">{{ resultTitle }}</h2>
            <p v-if="battle.expGained">获得 {{ battle.expGained }} 经验, {{ battle.goldGained }} 金币</p>
            <el-button type="primary" @click="endBattle">返回探险</el-button>
          </div>
        </div>

        <!-- 地图模式 -->
        <div v-else class="map-view">
          <h3 class="title">🗺️ 第 {{ save?.currentLevel }} 层地图</h3>
          <div class="map-grid" :style="{ gridTemplateColumns: `repeat(${mapSize}, 48px)` }">
            <div
              v-for="(cell, idx) in mapCells"
              :key="idx"
              :class="cellClass(cell, idx)"
              @click="onCellClick(idx)"
            >{{ cellIcon(cell, idx) }}</div>
          </div>
          <div class="move-pad">
            <div></div>
            <button class="btn-primary" @click="move('UP')">⬆️</button>
            <div></div>
            <button class="btn-primary" @click="move('LEFT')">⬅️</button>
            <button class="btn-primary" @click="move('DOWN')">⬇️</button>
            <button class="btn-primary" @click="move('RIGHT')">➡️</button>
          </div>
          <div class="msg">{{ message }}</div>
        </div>
      </div>
    </div>

    <!-- 商店弹窗 -->
    <el-dialog v-model="showShop" title="🏪 冒险商店" width="760px">
      <div class="shop-head">
        <span>当前金币: <b class="gold">💰 {{ shopGold }}</b></span>
        <el-input v-model="shopFilter" placeholder="搜索商品..." size="small" style="width:160px" clearable />
      </div>
      <!-- 角色属性面板 -->
      <div v-if="shopCharacter" class="char-panel">
        <div class="char-panel-title">🧙 {{ shopCharacter.name }} <span class="lv">Lv.{{ shopCharacter.level }}</span></div>
        <div class="char-stats">
          <span>❤️ {{ shopCharacter.hp }}/{{ shopCharacter.maxHp }}</span>
          <span>🔵 {{ shopCharacter.mp }}/{{ shopCharacter.maxMp }}</span>
          <span>⚔️ 攻击 {{ shopCharacter.attack }}</span>
          <span>🛡️ 防御 {{ shopCharacter.defense }}</span>
          <span>💨 速度 {{ shopCharacter.speed }}</span>
          <span>✨ 技能加成 +{{ shopCharacter.skillBonus || 0 }}</span>
        </div>
      </div>
      <div class="shop-tabs">
        <el-radio-group v-model="shopCategory" size="small">
          <el-radio-button label="">全部商品</el-radio-button>
          <el-radio-button label="INVENTORY">🎒 背包 ({{ shopInventory.length }})</el-radio-button>
          <el-radio-button label="WEAPON">武器</el-radio-button>
          <el-radio-button label="ARMOR">防具</el-radio-button>
          <el-radio-button label="BOOTS">鞋子</el-radio-button>
          <el-radio-button label="ACCESSORY">饰品</el-radio-button>
          <el-radio-button label="POTION">药水</el-radio-button>
          <el-radio-button label="SKILL">技能书</el-radio-button>
        </el-radio-group>
      </div>
      <div class="shop-grid">
        <div v-for="item in filteredItems" :key="item.id" class="shop-item" :class="{ sold: item.purchased }">
          <div class="item-icon">{{ item.icon }}</div>
          <div class="item-name">{{ item.name }}</div>
          <div class="item-desc">{{ item.description }}</div>
          <div class="item-foot">
            <span v-if="!isInventoryView" class="item-price">💰 {{ item.price }}</span>
            <span v-else class="item-price owned">已拥有</span>
            <el-button
              v-if="!isInventoryView"
              :type="item.purchased ? 'info' : (shopGold >= item.price ? 'primary' : 'info')"
              size="small"
              :disabled="item.purchased || shopGold < item.price"
              @click="buyItem(item)">
              {{ item.purchased ? '已拥有' : (shopGold >= item.price ? '购买' : '金币不足') }}
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="filteredItems.length === 0" :description="isInventoryView ? '背包空空如也,快去购买装备吧!' : '没有符合条件的商品'" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { saveApi, characterApi, mapApi, combatApi, shopApi } from '../api'

const route = useRoute()
const router = useRouter()
const saveId = Number(route.params.saveId)

const save = ref(null)
const character = ref(null)
const mapSize = ref(10)
const tiles = ref([])
const playerPos = reactive({ x: 0, y: 0 })
const message = ref('使用方向键或下方按钮移动,探索地图击败Boss通关!')

const inBattle = ref(false)
const battle = reactive({})
const battleLog = ref('')
const logBox = ref(null)

// 商店
const showShop = ref(false)
const shopItems = ref([])
const shopInventory = ref([])
const shopGold = ref(0)
const shopCategory = ref('')
const shopFilter = ref('')
const shopCharacter = ref(null)

const isInventoryView = computed(() => shopCategory.value === 'INVENTORY')

const filteredItems = computed(() => {
  const list = isInventoryView.value ? shopInventory.value : shopItems.value
  return list.filter(item => {
    if (!isInventoryView.value && shopCategory.value && item.category !== shopCategory.value) return false
    if (shopFilter.value && !item.name.includes(shopFilter.value) && !item.description.includes(shopFilter.value)) return false
    return true
  })
})

const mapCells = computed(() => {
  const arr = []
  for (let y = 0; y < mapSize.value; y++) {
    for (let x = 0; x < mapSize.value; x++) {
      arr.push(tiles.value[y]?.[x] ?? -1)
    }
  }
  return arr
})

const hpPct = computed(() => character.value ? Math.round(character.value.hp / character.value.maxHp * 100) : 0)
const mpPct = computed(() => character.value ? Math.round(character.value.mp / character.value.maxMp * 100) : 0)
const pHPct = computed(() => battle.player ? Math.max(0, Math.round(battle.player.hp / battle.player.maxHp * 100)) : 0)
const eHPct = computed(() => battle.enemy ? Math.max(0, Math.round(battle.enemy.hp / battle.enemy.maxHp * 100)) : 0)
const resultTitle = computed(() => ({ WIN: '🏆 胜利!', LOSE: '💀 战败...', FLEE: '🏃 逃跑成功' }[battle.result] || ''))
const resultClass = computed(() => ({ WIN: 'win', LOSE: 'lose', FLEE: 'flee' }[battle.result] || ''))

const rgbaColor = (hex) => {
  if (!hex) return 'rgba(52,152,219,0.25)'
  const h = hex.replace('#', '')
  const r = parseInt(h.substring(0, 2), 16)
  const g = parseInt(h.substring(2, 4), 16)
  const b = parseInt(h.substring(4, 6), 16)
  return `rgba(${r},${g},${b},0.25)`
}

const cellClass = (cell, idx) => {
  const x = idx % mapSize.value
  const y = Math.floor(idx / mapSize.value)
  if (x === playerPos.x && y === playerPos.y) return 'map-cell player'
  if (cell === -1) return 'map-cell fog'
  const names = { 0: 'empty', 5: 'wall', 1: 'monster', 2: 'treasure', 3: 'boss', 4: 'empty', 6: 'shop' }
  return 'map-cell ' + (names[cell] || 'empty')
}

const cellIcon = (cell, idx) => {
  const x = idx % mapSize.value
  const y = Math.floor(idx / mapSize.value)
  if (x === playerPos.x && y === playerPos.y) return '🤺'
  if (cell === -1) return ''
  if (cell === 1) return '👹'
  if (cell === 2) return '💰'
  if (cell === 3) return '🐲'
  if (cell === 5) return '🧱'
  if (cell === 6) return '🏪'
  return ''
}

const loadData = async () => {
  const [sv, ch, mp] = await Promise.all([
    saveApi.detail(saveId),
    characterApi.getBySave(saveId),
    mapApi.get(saveId)
  ])
  save.value = sv.data
  character.value = ch.data
  tiles.value = mp.data.tiles
  mapSize.value = mp.data.size
  playerPos.x = mp.data.playerX
  playerPos.y = mp.data.playerY
}

const move = async (dir) => {
  if (inBattle.value) return
  const res = await mapApi.move(saveId, dir)
  const d = res.data
  if (!d.moved) {
    message.value = d.message
    ElMessage.info(d.message)
    return
  }
  playerPos.x = d.playerX
  playerPos.y = d.playerY
  message.value = `移动到 (${d.playerX},${d.playerY})` + (d.tileName ? ` · ${d.tileName}` : '')

  if (d.tileType === 2) {
    // 宝箱
    const tr = await mapApi.treasure(saveId)
    ElMessage.success(tr.data.message)
    await loadData()
  } else if (d.triggerBattle) {
    await startBattle(d.battleType)
  } else if (d.triggerShop) {
    // 踩到商店瓦片, 打开商店
    await openShop()
  } else {
    // 普通移动: 刷新地图以显示新解锁的迷雾范围
    await loadData()
  }
}

const openShop = async () => {
  shopCategory.value = ''
  shopFilter.value = ''
  await loadShop()
  showShop.value = true
}

const loadShop = async () => {
  const [itemsRes, invRes, charRes] = await Promise.all([
    shopApi.items(saveId),
    shopApi.inventory(saveId),
    characterApi.getBySave(saveId)
  ])
  shopItems.value = itemsRes.data.items
  shopInventory.value = invRes.data
  shopGold.value = itemsRes.data.gold
  shopCharacter.value = charRes.data
}

const buyItem = async (item) => {
  if (item.purchased) return
  if (shopGold.value < item.price) {
    ElMessage.warning('金币不足')
    return
  }
  const res = await shopApi.buy(saveId, item.id)
  ElMessage.success(res.data.message)
  shopGold.value = res.data.gold
  // 同步角色面板的属性
  if (res.data.character) {
    shopCharacter.value = res.data.character
  }
  await loadShop()
}

const startBattle = async (battleType) => {
  // 重置上一场战斗的残留状态, 否则 finished=true 会导致直接显示结算
  Object.keys(battle).forEach(k => delete battle[k])
  try {
    const res = await combatApi.start({ saveId, battleType })
    Object.assign(battle, res.data)
    battle.finished = false
    battleLog.value = `${battle.enemyName} 出现了!${battle.playerFirst ? '你先手出击!' : '敌人速度更快,先发制人!'}\n`
    inBattle.value = true
  } catch (e) {
    // 后端报"已有进行中的战斗" → 残留会话, 清理后重试一次
    await combatApi.forfeit(saveId)
    const res = await combatApi.start({ saveId, battleType })
    Object.assign(battle, res.data)
    battle.finished = false
    battleLog.value = `${battle.enemyName} 出现了!${battle.playerFirst ? '你先手出击!' : '敌人速度更快,先发制人!'}\n`
    inBattle.value = true
  }
}

const act = async (action) => {
  const res = await combatApi.action(saveId, action)
  const d = res.data
  battleLog.value += (d.report || '') + '\n'
  if (d.player) Object.assign(battle, { player: { ...battle.player, ...d.player } })
  if (d.enemy) Object.assign(battle, { enemy: { ...battle.enemy, ...d.enemy } })
  await nextTick()
  if (logBox.value) logBox.value.scrollTop = logBox.value.scrollHeight
  if (d.finished) {
    battle.finished = true
    battle.result = d.result
    if (d.expGained !== undefined) {
      battle.expGained = d.expGained
      battle.goldGained = d.goldGained
    }
    if (battle.bossPhase2 === undefined) battle.bossPhase2 = false
    // 同步角色
    const ch = await characterApi.getBySave(saveId)
    character.value = ch.data
    const sv = await saveApi.detail(saveId)
    save.value = sv.data
  }
}

const endBattle = async () => {
  inBattle.value = false
  await loadData()
  message.value = '继续探索地图,寻找Boss!'
}

const alloc = async (attr) => {
  const res = await characterApi.allocate(saveId, attr)
  character.value = res.data
  ElMessage.success('属性提升!')
}

const onCellClick = () => {}

onMounted(async () => {
  // 进入游戏页时, 检查后端是否有残留的战斗会话(刷新/异常退出导致), 自动清理
  try {
    const st = await combatApi.status(saveId)
    if (st.data?.inBattle) {
      await combatApi.forfeit(saveId)
    }
  } catch (e) { /* 忽略, 继续加载地图 */ }
  await loadData()
})
</script>

<style scoped>
.game-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 16px;
}
.side-panel { padding: 20px; }
.char-name { font-size: 18px; font-weight: bold; margin-bottom: 12px; }
.lv { color: #f1c40f; font-size: 14px; margin-left: 6px; }
.stat-row { display: flex; align-items: center; gap: 8px; margin-top: 8px; font-size: 13px; }
.stat-text { font-size: 12px; color: #95a5a6; margin-bottom: 6px; }
.stat-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; font-size: 13px; margin-top: 12px; padding-top: 12px; border-top: 1px solid #3a4a5a; }
.alloc { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 6px; }
.map-info { font-size: 13px; color: #bdc3c7; line-height: 1.8; }
.legend { margin-top: 10px; display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.lg { display: inline-block; width: 14px; height: 14px; border-radius: 3px; margin-right: 6px; vertical-align: middle; }
.lg.player { background: #3498db; }
.lg.monster { background: #6b2c2c; }
.lg.treasure { background: #7d5a1a; }
.lg.boss { background: #4a1a4a; }
.lg.wall { background: #4a3520; }
.lg.shop { background: #d4a017; }

.main-panel { padding: 20px; min-height: 560px; display: flex; flex-direction: column; }
.map-view { display: flex; flex-direction: column; align-items: center; gap: 16px; }
.move-pad {
  display: grid;
  grid-template-columns: repeat(3, 56px);
  gap: 8px;
  margin-top: 8px;
}
.move-pad button { font-size: 20px; padding: 8px; }
.msg { color: #95a5a6; font-size: 13px; margin-top: 8px; }

.battle-scene { padding: 10px; }
.fighters { display: flex; align-items: center; justify-content: center; gap: 24px; margin: 16px 0; }
.vs { font-size: 22px; font-weight: bold; color: #e74c3c; }
.mp-text { font-size: 12px; color: #bdc3c7; margin-top: 4px; }
.rage { color: #e74c3c; font-size: 12px; }
.actions { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }
.result-box { text-align: center; padding: 20px; }
.result-box .win { color: #f1c40f; }
.result-box .lose { color: #e74c3c; }
.result-box .flee { color: #95a5a6; }

/* 商店 */
.shop-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.shop-head .gold { color: #f1c40f; font-size: 18px; }
.char-panel {
  background: rgba(52,152,219,0.12); border: 1px solid rgba(52,152,219,0.3);
  border-radius: 8px; padding: 10px 14px; margin-bottom: 14px;
}
.char-panel-title { font-weight: bold; color: #ecf0f1; margin-bottom: 6px; font-size: 14px; }
.char-panel-title .lv { color: #f1c40f; font-size: 12px; margin-left: 6px; }
.char-stats { display: flex; flex-wrap: wrap; gap: 14px; font-size: 13px; color: #bdc3c7; }
.char-stats span { white-space: nowrap; }
.shop-tabs { margin-bottom: 16px; }
.shop-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; max-height: 420px; overflow-y: auto; padding: 4px; }
.shop-item {
  background: rgba(0,0,0,0.25); border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px; padding: 14px; text-align: center; transition: all 0.2s;
}
.shop-item:hover { border-color: #f1c40f; transform: translateY(-2px); }
.shop-item.sold { opacity: 0.55; }
.item-icon { font-size: 36px; margin-bottom: 6px; }
.item-name { font-weight: bold; color: #ecf0f1; margin-bottom: 4px; }
.item-desc { font-size: 12px; color: #95a5a6; min-height: 32px; margin-bottom: 8px; }
.item-foot { display: flex; justify-content: space-between; align-items: center; }
.item-price { color: #f1c40f; font-weight: bold; }
.item-price.owned { color: #2ecc71; }
</style>
