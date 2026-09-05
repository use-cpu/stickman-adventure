<template>
  <div class="auth-wrap">
    <div class="auth-box panel">
      <h1 class="title">📝 注册账号</h1>
      <p class="subtitle">创建你的火柴人探险账号</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名 (3-20字符)" size="large" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称 (选填)" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码 (6-30字符)" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirm">
          <el-input v-model="form.confirm" type="password" placeholder="确认密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="onRegister">注 册</el-button>
      </el-form>
      <div class="tip">
        已有账号? <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', nickname: '', password: '', confirm: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度3-20字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '长度6-30字符', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const onRegister = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authApi.register({
        username: form.username,
        password: form.password,
        nickname: form.nickname
      })
      ElMessage.success('注册成功,请登录')
      router.push('/login')
    } catch (e) {
      // 错误由拦截器处理
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
.auth-box { width: 380px; padding: 32px; }
.title { font-size: 28px; margin-bottom: 4px; }
.subtitle { text-align: center; color: #95a5a6; margin-bottom: 24px; }
.tip { text-align: center; margin-top: 16px; color: #bdc3c7; }
.tip a { color: #409eff; }
</style>
