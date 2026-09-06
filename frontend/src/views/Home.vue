<template>
  <div class="home-page">
    <NavBar />
    <div class="content">
      <div class="panel section">
        <div class="section-head">
          <h2>🎮 我的存档</h2>
          <el-button type="primary" @click="showCreate = true">＋ 新建存档</el-button>
        </div>
        <p v-if="saves.length === 0" class="empty">暂无存档,点击右上角创建一个开始冒险吧!</p>
        <div class="save-list">
          <div v-for="s in saves" :key="s.id" class="save-card">
            <div class="save-info">
              <div class="save-name">{{ s.saveName }}</div>
              <div class="save-meta">
                关卡 {{ s.currentLevel }} · 坐标({{ s.playerX }},{{ s.playerY }})
                <span v-if="s.isFinished" class="tag-finish">已通关</span>
              </div>
            </div>
            <div class="save-ops">
              <el-button type="success" size="small" @click="enter(s.id)">进入游戏</el-button>
              <el-button type="warning" size="small" @click="openShop(s.id)">🏪 商店</el-button>
              <el-button size="small" @click="viewLogs(s.id)">战斗日志</el-button>
              <el-button type="danger" size="small" @click="onDelete(s)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 战斗日志弹窗 -->
      <el-dialog v-model="showLogs" title="战斗日志" width="700px">
        <el-empty v-if="logs.length === 0" description="暂无战斗记录" />
        <el-table v-else :data="logs" size="small" max-height="420">
          <el-table-column prop="createTime" label="时间" width="160" />
          <el-table-column prop="enemyName" label="敌人" />
          <el-table-column prop="battleType" label="类型">
            <template #default="{ row }">{{ row.battleType === 'BOSS' ? 'Boss战' : '普通' }}</template>
          </el-table-column>
          <el-table-column prop="result" label="结果">
            <template #default="{ row }">
              <el-tag :type="resultType(row.result)">{{ resultText(row.result) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rounds" label="回合" width="70" />
          <el-table-column prop="expGained" label="经验" width="70" />
          <el-table-column prop="goldGained" label="金币" width="70" />
        </el-table>
      </el-dialog>

      <!-- 新建存档弹窗 -->
      <el-dialog v-model="showCreate" title="新建存档" width="420px">
        <el-form :model="createForm" label-width="80px">
          <el-form-item label="存档名称">
            <el-input v-model="createForm.saveName" placeholder="如: 勇者之路" />
          </el-form-item>
          <el-form-item label="角色名称">
            <el-input v-model="createForm.characterName" placeholder="如: 火柴人" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreate = false">取消</el-button>
          <el-button type="primary" @click="onCreate">创建并进入</el-button>
        </template>
      </el-dialog>

      <!-- 商店弹窗 -->
      <el-dialog v-model="showShop" title="🏪 冒险商店" width="760px" @open="loadShop">
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { saveApi, logApi, shopApi, characterApi } from '../api'

const router = useRouter()
const saves = ref([])
const showCreate = ref(false)
const createForm = reactive({ saveName: '', characterName: '' })
const showLogs = ref(false)
const logs = ref([])

// 商店
const showShop = ref(false)
const shopSaveId = ref(null)
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

const openShop = (id) => {
  shopSaveId.value = id
  shopCategory.value = ''
  shopFilter.value = ''
  showShop.value = true
}

const loadShop = async () => {
  const [itemsRes, invRes, charRes] = await Promise.all([
    shopApi.items(shopSaveId.value),
    shopApi.inventory(shopSaveId.value),
    characterApi.getBySave(shopSaveId.value)
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
  const res = await shopApi.buy(shopSaveId.value, item.id)
  ElMessage.success(res.data.message)
  shopGold.value = res.data.gold
  if (res.data.character) {
    shopCharacter.value = res.data.character
  }
  // 刷新列表
  await loadShop()
}

const loadSaves = async () => {
  const res = await saveApi.list()
  saves.value = res.data
}

const enter = (id) => router.push(`/game/${id}`)

const viewLogs = async (id) => {
  const res = await logApi.myBattleLogs(id)
  logs.value = res.data
  showLogs.value = true
}

const onCreate = async () => {
  if (!createForm.saveName.trim()) {
    ElMessage.warning('请输入存档名称')
    return
  }
  const res = await saveApi.create(createForm)
  ElMessage.success('存档创建成功')
  showCreate.value = false
  createForm.saveName = ''
  createForm.characterName = ''
  router.push(`/game/${res.data.id}`)
}

const onDelete = (s) => {
  ElMessageBox.confirm(`确认删除存档「${s.saveName}」? 角色数据将一并删除。`, '确认', {
    type: 'warning'
  }).then(async () => {
    await saveApi.remove(s.id)
    ElMessage.success('已删除')
    loadSaves()
  }).catch(() => {})
}

const resultText = (r) => ({ WIN: '胜利', LOSE: '失败', FLEE: '逃跑' }[r] || r)
const resultType = (r) => ({ WIN: 'success', LOSE: 'danger', FLEE: 'info' }[r] || '')

onMounted(loadSaves)
</script>

<style scoped>
.content { max-width: 960px; margin: 24px auto; padding: 0 16px; }
.section { padding: 24px; }
.section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-head h2 { color: #f1c40f; }
.empty { text-align: center; color: #95a5a6; padding: 30px; }
.save-list { display: flex; flex-direction: column; gap: 12px; }
.save-card {
  display: flex; justify-content: space-between; align-items: center;
  background: rgba(0,0,0,0.25); border-radius: 8px; padding: 14px 18px;
  border-left: 3px solid #409eff;
}
.save-name { font-size: 16px; font-weight: bold; color: #ecf0f1; }
.save-meta { font-size: 13px; color: #95a5a6; margin-top: 4px; }
.tag-finish { color: #2ecc71; margin-left: 8px; }
.save-ops { display: flex; gap: 8px; }

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
.shop-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; max-height: 480px; overflow-y: auto; padding: 4px; }
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
