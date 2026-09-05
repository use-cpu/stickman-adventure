import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截: 携带 token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截: 统一处理业务码与错误
http.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(res)
  },
  (error) => {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期,请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    } else {
      const msg = error.response?.data?.message || error.message || '网络错误'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

// 认证
export const authApi = {
  login: (data) => http.post('/auth/login', data),
  register: (data) => http.post('/auth/register', data),
  me: () => http.get('/auth/me')
}

// 存档
export const saveApi = {
  list: () => http.get('/saves'),
  detail: (id) => http.get(`/saves/${id}`),
  create: (data) => http.post('/saves', data),
  remove: (id) => http.delete(`/saves/${id}`)
}

// 角色
export const characterApi = {
  getBySave: (saveId) => http.get(`/character/save/${saveId}`),
  allocate: (saveId, attr) => http.post(`/character/save/${saveId}/allocate`, null, { params: { attr } })
}

// 地图
export const mapApi = {
  get: (saveId) => http.get(`/map/save/${saveId}`),
  move: (saveId, direction) => http.post(`/map/save/${saveId}/move`, null, { params: { direction } }),
  treasure: (saveId) => http.post(`/map/save/${saveId}/treasure`)
}

// 战斗
export const combatApi = {
  start: (data) => http.post('/combat/start', data),
  action: (saveId, action) => http.post(`/combat/save/${saveId}/action`, null, { params: { action } }),
  status: (saveId) => http.get(`/combat/save/${saveId}/status`),
  forfeit: (saveId) => http.post(`/combat/save/${saveId}/forfeit`)
}

// 怪物配置(管理员)
export const monsterApi = {
  list: (type) => http.get('/monsters', { params: { type } }),
  detail: (id) => http.get(`/monsters/${id}`),
  create: (data) => http.post('/monsters', data),
  update: (data) => http.put('/monsters', data),
  remove: (id) => http.delete(`/monsters/${id}`)
}

// Boss配置(管理员)
export const bossApi = {
  list: () => http.get('/bosses'),
  detail: (id) => http.get(`/bosses/${id}`),
  create: (data) => http.post('/bosses', data),
  update: (data) => http.put('/bosses', data),
  remove: (id) => http.delete(`/bosses/${id}`)
}

// 日志
export const logApi = {
  myBattleLogs: (saveId) => http.get('/logs/battle/mine', { params: { saveId } }),
  allBattleLogs: (page, size) => http.get('/logs/battle/all', { params: { page, size } }),
  userLogs: (page, size) => http.get('/logs/user', { params: { page, size } })
}

// 管理员
export const adminApi = {
  users: (page, size, username) => http.get('/admin/users', { params: { page, size, username } }),
  updateStatus: (id, status) => http.put(`/admin/users/${id}/status`, null, { params: { status } }),
  resetPassword: (id, newPassword) => http.put(`/admin/users/${id}/password`, null, { params: { newPassword } }),
  allSaves: () => http.get('/admin/saves')
}

export default http
