import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/main/HomeView.vue'
import PracticeView from '../views/practice/PracticeView.vue'
import PracticeQuestionView from '../views/practice/PracticeQuestionView.vue'
import ExamView from '../views/exam/ExamView.vue'
import SurveyView from '../views/survey/SurveyView.vue'
import SurveyLevelView from '../views/survey/SurveyLevelView.vue'

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
      path: '/survey',
      name: 'survey',
      component: SurveyView
    },
    {
      path: '/survey/level',
      name: 'survey-level',
      component: SurveyLevelView
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