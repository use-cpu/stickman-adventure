<template>
  <div class="admin-page">
    <NavBar />
    <div class="content panel">
      <h2 class="title">⚙️ 管理后台</h2>
      <el-tabs v-model="activeTab">
        <!-- 怪物配置 -->
        <el-tab-pane label="怪物配置" name="monster">
          <div class="tab-head">
            <el-select v-model="monsterFilter" placeholder="筛选类型" clearable style="width:140px" @change="loadMonsters">
              <el-option label="普通" value="NORMAL" />
              <el-option label="精英" value="ELITE" />
              <el-option label="Boss" value="BOSS" />
            </el-select>
            <el-button type="primary" @click="openMonster(null)">＋ 新增怪物</el-button>
          </div>
          <table class="data-table">
            <thead>
              <tr><th>ID</th><th>图标</th><th>名称</th><th>类型</th><th>等级</th><th>HP</th><th>攻击</th><th>防御</th><th>速度</th><th>经验</th><th>金币</th><th>技能</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="m in monsters" :key="m.id">
                <td>{{ m.id }}</td>
                <td>{{ m.icon }}</td>
                <td>{{ m.name }}</td>
                <td>{{ m.type }}</td>
                <td>{{ m.level }}</td>
                <td>{{ m.hp }}</td>
                <td>{{ m.attack }}</td>
                <td>{{ m.defense }}</td>
                <td>{{ m.speed }}</td>
                <td>{{ m.expReward }}</td>
                <td>{{ m.goldReward }}</td>
                <td>{{ m.skillName }}({{ m.skillDamage }})</td>
                <td>
                  <el-button size="small" @click="openMonster(m)">编辑</el-button>
                  <el-button size="small" type="danger" @click="delMonster(m)">删除</el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </el-tab-pane>

        <!-- Boss配置 -->
        <el-tab-pane label="Boss配置" name="boss">
          <div class="tab-head"><span></span><el-button type="primary" @click="openBoss(null)">＋ 新增Boss</el-button></div>
          <table class="data-table">
            <thead>
              <tr><th>ID</th><th>图标</th><th>名称</th><th>关卡</th><th>HP</th><th>攻击</th><th>防御</th><th>速度</th><th>经验</th><th>金币</th><th>技能</th><th>二阶段阈值</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="b in bosses" :key="b.id">
                <td>{{ b.id }}</td>
                <td>{{ b.icon }}</td>
                <td>{{ b.name }}</td>
                <td>第{{ b.level }}层</td>
                <td>{{ b.hp }}</td>
                <td>{{ b.attack }}</td>
                <td>{{ b.defense }}</td>
                <td>{{ b.speed }}</td>
                <td>{{ b.expReward }}</td>
                <td>{{ b.goldReward }}</td>
                <td>{{ b.skillName }}({{ b.skillDamage }})</td>
                <td>{{ b.phase2HpThreshold }}%</td>
                <td>
                  <el-button size="small" @click="openBoss(b)">编辑</el-button>
                  <el-button size="small" type="danger" @click="delBoss(b)">删除</el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </el-tab-pane>

        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="user">
          <div class="tab-head">
            <el-input v-model="userSearch" placeholder="搜索用户名" style="width:200px" clearable @keyup.enter="loadUsers" />
            <el-button @click="loadUsers">搜索</el-button>
          </div>
          <table class="data-table">
            <thead>
              <tr><th>ID</th><th>用户名</th><th>昵称</th><th>角色</th><th>状态</th><th>注册时间</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id">
                <td>{{ u.id }}</td>
                <td>{{ u.username }}</td>
                <td>{{ u.nickname }}</td>
                <td><el-tag :type="u.role==='ADMIN'?'danger':'primary'">{{ u.role==='ADMIN'?'管理员':'玩家' }}</el-tag></td>
                <td><el-tag :type="u.status===1?'success':'info'">{{ u.status===1?'启用':'禁用' }}</el-tag></td>
                <td>{{ u.createTime }}</td>
                <td>
                  <el-button size="small" :type="u.status===1?'warning':'success'" @click="toggleStatus(u)">{{ u.status===1?'禁用':'启用' }}</el-button>
                  <el-button size="small" @click="resetPwd(u)">重置密码</el-button>
                </td>
              </tr>
            </tbody>
          </table>
          <el-pagination
            v-model:current-page="userPage" :page-size="20" :total="userTotal"
            layout="prev, pager, next" @current-change="loadUsers" style="margin-top:16px;justify-content:center"
          />
        </el-tab-pane>

        <!-- 战斗日志 -->
        <el-tab-pane label="战斗日志" name="battleLog">
          <table class="data-table">
            <thead>
              <tr><th>ID</th><th>用户ID</th><th>存档ID</th><th>类型</th><th>敌人</th><th>结果</th><th>回合</th><th>经验</th><th>金币</th><th>时间</th></tr>
            </thead>
            <tbody>
              <tr v-for="l in battleLogs" :key="l.id">
                <td>{{ l.id }}</td>
                <td>{{ l.userId }}</td>
                <td>{{ l.saveId }}</td>
                <td>{{ l.battleType==='BOSS'?'Boss战':'普通' }}</td>
                <td>{{ l.enemyName }}</td>
                <td><el-tag :type="resultType(l.result)">{{ resultText(l.result) }}</el-tag></td>
                <td>{{ l.rounds }}</td>
                <td>{{ l.expGained }}</td>
                <td>{{ l.goldGained }}</td>
                <td>{{ l.createTime }}</td>
              </tr>
            </tbody>
          </table>
          <el-pagination
            v-model:current-page="blogPage" :page-size="20" :total="blogTotal"
            layout="prev, pager, next" @current-change="loadBattleLogs" style="margin-top:16px;justify-content:center"
          />
        </el-tab-pane>

        <!-- 操作日志 -->
        <el-tab-pane label="操作日志" name="userLog">
          <table class="data-table">
            <thead>
              <tr><th>ID</th><th>用户ID</th><th>操作</th><th>详情</th><th>IP</th><th>时间</th></tr>
            </thead>
            <tbody>
              <tr v-for="l in userLogs" :key="l.id">
                <td>{{ l.id }}</td>
                <td>{{ l.userId }}</td>
                <td>{{ l.action }}</td>
                <td>{{ l.detail }}</td>
                <td>{{ l.ip }}</td>
                <td>{{ l.createTime }}</td>
              </tr>
            </tbody>
          </table>
          <el-pagination
            v-model:current-page="ulogPage" :page-size="20" :total="ulogTotal"
            layout="prev, pager, next" @current-change="loadUserLogs" style="margin-top:16px;justify-content:center"
          />
        </el-tab-pane>

        <!-- 全部存档 -->
        <el-tab-pane label="全部存档" name="saves">
          <table class="data-table">
            <thead>
              <tr><th>ID</th><th>用户ID</th><th>存档名</th><th>关卡</th><th>坐标</th><th>状态</th><th>更新时间</th></tr>
            </thead>
            <tbody>
              <tr v-for="s in allSaves" :key="s.id">
                <td>{{ s.id }}</td>
                <td>{{ s.userId }}</td>
                <td>{{ s.saveName }}</td>
                <td>第{{ s.currentLevel }}层</td>
                <td>({{ s.playerX }},{{ s.playerY }})</td>
                <td><el-tag :type="s.isFinished?'success':'info'">{{ s.isFinished?'通关':'进行中' }}</el-tag></td>
                <td>{{ s.updateTime }}</td>
              </tr>
            </tbody>
          </table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 怪物编辑弹窗 -->
    <el-dialog v-model="showMonsterDlg" :title="monsterForm.id ? '编辑怪物' : '新增怪物'" width="520px">
      <el-form :model="monsterForm" label-width="90px">
        <el-form-item label="名称"><el-input v-model="monsterForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="monsterForm.type" style="width:100%">
            <el-option label="普通" value="NORMAL" /><el-option label="精英" value="ELITE" /><el-option label="Boss" value="BOSS" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标"><el-input v-model="monsterForm.icon" placeholder="emoji,如 👹" /></el-form-item>
        <el-form-item label="颜色"><el-color-picker v-model="monsterForm.color" /></el-form-item>
        <el-form-item label="等级"><el-input-number v-model="monsterForm.level" :min="1" /></el-form-item>
        <el-form-item label="生命"><el-input-number v-model="monsterForm.hp" :min="1" /></el-form-item>
        <el-form-item label="攻击"><el-input-number v-model="monsterForm.attack" :min="0" /></el-form-item>
        <el-form-item label="防御"><el-input-number v-model="monsterForm.defense" :min="0" /></el-form-item>
        <el-form-item label="速度"><el-input-number v-model="monsterForm.speed" :min="1" /></el-form-item>
        <el-form-item label="经验奖励"><el-input-number v-model="monsterForm.expReward" :min="0" /></el-form-item>
        <el-form-item label="金币奖励"><el-input-number v-model="monsterForm.goldReward" :min="0" /></el-form-item>
        <el-form-item label="技能名"><el-input v-model="monsterForm.skillName" /></el-form-item>
        <el-form-item label="技能伤害"><el-input-number v-model="monsterForm.skillDamage" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMonsterDlg = false">取消</el-button>
        <el-button type="primary" @click="saveMonster">保存</el-button>
      </template>
    </el-dialog>

    <!-- Boss编辑弹窗 -->
    <el-dialog v-model="showBossDlg" :title="bossForm.id ? '编辑Boss' : '新增Boss'" width="520px">
      <el-form :model="bossForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="bossForm.name" /></el-form-item>
        <el-form-item label="出现关卡"><el-input-number v-model="bossForm.level" :min="1" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="bossForm.icon" placeholder="emoji" /></el-form-item>
        <el-form-item label="颜色"><el-color-picker v-model="bossForm.color" /></el-form-item>
        <el-form-item label="生命"><el-input-number v-model="bossForm.hp" :min="1" /></el-form-item>
        <el-form-item label="最大生命"><el-input-number v-model="bossForm.maxHp" :min="1" /></el-form-item>
        <el-form-item label="攻击"><el-input-number v-model="bossForm.attack" :min="0" /></el-form-item>
        <el-form-item label="防御"><el-input-number v-model="bossForm.defense" :min="0" /></el-form-item>
        <el-form-item label="速度"><el-input-number v-model="bossForm.speed" :min="1" /></el-form-item>
        <el-form-item label="经验奖励"><el-input-number v-model="bossForm.expReward" :min="0" /></el-form-item>
        <el-form-item label="金币奖励"><el-input-number v-model="bossForm.goldReward" :min="0" /></el-form-item>
        <el-form-item label="技能名"><el-input v-model="bossForm.skillName" /></el-form-item>
        <el-form-item label="技能伤害"><el-input-number v-model="bossForm.skillDamage" :min="0" /></el-form-item>
        <el-form-item label="二阶段阈值%"><el-input-number v-model="bossForm.phase2HpThreshold" :min="1" :max="100" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBossDlg = false">取消</el-button>
        <el-button type="primary" @click="saveBoss">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { monsterApi, bossApi, adminApi, logApi } from '../api'

const activeTab = ref('monster')

// 怪物
const monsters = ref([])
const monsterFilter = ref('')
const showMonsterDlg = ref(false)
const monsterForm = reactive({})

const loadMonsters = async () => {
  const res = await monsterApi.list(monsterFilter.value)
  monsters.value = res.data
}
const openMonster = (m) => {
  Object.keys(monsterForm).forEach(k => delete monsterForm[k])
  Object.assign(monsterForm, m || {
    name: '', type: 'NORMAL', level: 1, hp: 50, attack: 10, defense: 5, speed: 8,
    expReward: 20, goldReward: 10, skillName: '', skillDamage: 0, color: '#e74c3c', icon: '👹'
  })
  showMonsterDlg.value = true
}
const saveMonster = async () => {
  if (!monsterForm.name) { ElMessage.warning('请输入名称'); return }
  if (monsterForm.id) await monsterApi.update({ ...monsterForm })
  else await monsterApi.create({ ...monsterForm })
  ElMessage.success('保存成功')
  showMonsterDlg.value = false
  loadMonsters()
}
const delMonster = (m) => {
  ElMessageBox.confirm(`确认删除怪物「${m.name}」?`, '确认', { type: 'warning' })
    .then(async () => { await monsterApi.remove(m.id); ElMessage.success('已删除'); loadMonsters() })
    .catch(() => {})
}

// Boss
const bosses = ref([])
const showBossDlg = ref(false)
const bossForm = reactive({})
const loadBosses = async () => { const res = await bossApi.list(); bosses.value = res.data }
const openBoss = (b) => {
  Object.keys(bossForm).forEach(k => delete bossForm[k])
  Object.assign(bossForm, b || {
    name: '', level: 1, hp: 300, maxHp: 300, attack: 25, defense: 12, speed: 12,
    expReward: 150, goldReward: 100, skillName: '', skillDamage: 40,
    phase2HpThreshold: 50, color: '#8e44ad', icon: '🐲'
  })
  showBossDlg.value = true
}
const saveBoss = async () => {
  if (!bossForm.name) { ElMessage.warning('请输入名称'); return }
  if (bossForm.id) await bossApi.update({ ...bossForm })
  else await bossApi.create({ ...bossForm })
  ElMessage.success('保存成功')
  showBossDlg.value = false
  loadBosses()
}
const delBoss = (b) => {
  ElMessageBox.confirm(`确认删除Boss「${b.name}」?`, '确认', { type: 'warning' })
    .then(async () => { await bossApi.remove(b.id); ElMessage.success('已删除'); loadBosses() })
    .catch(() => {})
}

// 用户
const users = ref([])
const userSearch = ref('')
const userPage = ref(1)
const userTotal = ref(0)
const loadUsers = async () => {
  const res = await adminApi.users(userPage.value, 20, userSearch.value)
  users.value = res.data.records
  userTotal.value = Number(res.data.total)
}
const toggleStatus = async (u) => {
  await adminApi.updateStatus(u.id, u.status === 1 ? 0 : 1)
  ElMessage.success('已更新')
  loadUsers()
}
const resetPwd = (u) => {
  ElMessageBox.prompt(`为用户「${u.username}」设置新密码`, '重置密码', { inputPattern: /.{6,}/, inputErrorMessage: '至少6位' })
    .then(async ({ value }) => { await adminApi.resetPassword(u.id, value); ElMessage.success('密码已重置') })
    .catch(() => {})
}

// 战斗日志
const battleLogs = ref([])
const blogPage = ref(1)
const blogTotal = ref(0)
const loadBattleLogs = async () => {
  const res = await logApi.allBattleLogs(blogPage.value, 20)
  battleLogs.value = res.data.records
  blogTotal.value = Number(res.data.total)
}

// 操作日志
const userLogs = ref([])
const ulogPage = ref(1)
const ulogTotal = ref(0)
const loadUserLogs = async () => {
  const res = await logApi.userLogs(ulogPage.value, 20)
  userLogs.value = res.data.records
  ulogTotal.value = Number(res.data.total)
}

// 全部存档
const allSaves = ref([])
const loadAllSaves = async () => { const res = await adminApi.allSaves(); allSaves.value = res.data }

const resultText = (r) => ({ WIN: '胜利', LOSE: '失败', FLEE: '逃跑' }[r] || r)
const resultType = (r) => ({ WIN: 'success', LOSE: 'danger', FLEE: 'info' }[r] || '')

// 按需加载 tab 数据
watch(activeTab, (tab) => {
  if (tab === 'user' && users.value.length === 0) loadUsers()
  if (tab === 'battleLog' && battleLogs.value.length === 0) loadBattleLogs()
  if (tab === 'userLog' && userLogs.value.length === 0) loadUserLogs()
  if (tab === 'saves' && allSaves.value.length === 0) loadAllSaves()
})

onMounted(() => {
  loadMonsters()
  loadBosses()
})
</script>

<style scoped>
.content { max-width: 1200px; margin: 20px auto; padding: 24px; }
.tab-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
</style>
