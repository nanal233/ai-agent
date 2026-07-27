import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import EcAppChat from '../views/EcAppChat.vue'
import ManusChat from '../views/ManusChat.vue'

const routes = [
  { path: '/', name: 'home', component: Home },
  { path: '/ec-app', name: 'ec-app', component: EcAppChat },
  { path: '/manus', name: 'manus', component: ManusChat },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
