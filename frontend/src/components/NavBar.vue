<template>
  <div class="navbar">
    <span class="brand">🤺 火柴人探险</span>
    <div class="actions">
      <span class="user-info">{{ user?.nickname || user?.username }} ({{ user?.role === 'ADMIN' ? '管理员' : '玩家' }})</span>
      <router-link v-if="user?.role === 'ADMIN'" to="/admin" class="nav-link">管理后台</router-link>
      <router-link to="/" class="nav-link">游戏大厅</router-link>
      <el-button size="small" @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 2px solid #409eff;
}
.brand { font-size: 22px; font-weight: bold; color: #f1c40f; }
.actions { display: flex; align-items: center; gap: 16px; }
.user-info { color: #bdc3c7; font-size: 14px; }
.nav-link { color: #409eff; text-decoration: none; font-size: 14px; }
.nav-link:hover { text-decoration: underline; }
</style>
