import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/main/HomeView.vue'
import PracticeView from '../views/practice/PracticeView.vue'
import PracticeQuestionView from '../views/practice/PracticeQuestionView.vue'
import ExamView from '../views/exam/ExamView.vue'
import SurveyView from '../views/survey/SurveyView.vue'
import SurveyLevelView from '../views/survey/SurveyLevelView.vue'
import SetupView from '../views/exam/SetupView.vue'
import ExamQuestionView from '../views/exam/ExamQuestionView.vue'

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
      path: '/practice/question',
      name: 'practice-question',
      component: PracticeQuestionView
    },

    // 나중에 만들 예정
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/mypage/MyPageView.vue')
    },
    {
      path: '/exam',
      name: 'exam',
      component: ExamView
    },
    {
      path: '/exam/setup',
      name: 'exam-setup',
      component: SetupView
    },
    {
      path: '/exam/question',
      name: 'exam-question',
      component: ExamQuestionView
    },
    {
      path: '/survey',
      name: 'survey',
      component: SurveyView
    },
    {
      path: '/survey/level',
      name: 'survey-level',
      component: SurveyLevelView
    },
    // reports 라우트는 나중에 추가
    // {
    //   path: '/reports',
    //   name: 'reports',
    //   component: () => import('../views/reports/ReportsView.vue')
    // }
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/auth/LoginView.vue')
    }
  ]
})

export default router