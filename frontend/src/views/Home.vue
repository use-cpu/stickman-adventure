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
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { saveApi, logApi } from '../api'

const router = useRouter()
const saves = ref([])
const showCreate = ref(false)
const createForm = reactive({ saveName: '', characterName: '' })
const showLogs = ref(false)
const logs = ref([])

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
</style>
