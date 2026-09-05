<template>
  <div class="auth-wrap">
    <div class="auth-box panel">
      <h1 class="title">🤺 火柴人探险</h1>
      <p class="subtitle">回合制探险 · 网格地图 · Boss对战</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password prefix-icon="Lock" @keyup.enter="onLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="onLogin">登 录</el-button>
      </el-form>
      <div class="tip">
        还没有账号? <router-link to="/register">立即注册</router-link>
      </div>
      <div class="demo-tip">
        演示账号: admin/admin123 (管理员) · player/123456 (玩家)
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onLogin = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (e) {
      // 错误已由拦截器提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
}
.auth-box {
  width: 380px;
  padding: 32px;
}
.title { font-size: 32px; margin-bottom: 4px; }
.subtitle { text-align: center; color: #95a5a6; margin-bottom: 24px; }
.tip { text-align: center; margin-top: 16px; color: #bdc3c7; }
.tip a { color: #409eff; }
.demo-tip {
  margin-top: 12px;
  text-align: center;
  font-size: 12px;
  color: #7f8c8d;
  background: rgba(0,0,0,0.25);
  padding: 8px;
  border-radius: 6px;
}
</style>
