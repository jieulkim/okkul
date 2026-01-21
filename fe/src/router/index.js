import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/main/HomeView.vue'
import PracticeView from '../views/practice/PracticeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/practice',
      name: 'practice',
      component: PracticeView
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/mypage/MyPageView.vue')
    }
    // reports 라우트는 나중에 추가
    // {
    //   path: '/reports',
    //   name: 'reports',
    //   component: () => import('../views/reports/ReportsView.vue')
    // }
  ]
})

export default router