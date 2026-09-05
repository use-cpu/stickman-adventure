import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') },
  { path: '/', name: 'home', component: () => import('../views/Home.vue'), meta: { auth: true } },
  { path: '/game/:saveId', name: 'game', component: () => import('../views/Game.vue'), meta: { auth: true } },
  { path: '/admin', name: 'admin', component: () => import('../views/Admin.vue'), meta: { auth: true, admin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.auth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.admin && !userStore.isAdmin) {
    next('/')
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
