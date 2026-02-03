import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/main/HomeView.vue'
import PracticeView from '../views/practice/PracticeView.vue'
import PracticeQuestionView from '../views/practice/PracticeQuestionView.vue'
import ExamView from '../views/exam/ExamView.vue'
import SurveyView from '../views/survey/SurveyView.vue'
import SurveyLevelView from '../views/survey/SurveyLevelView.vue'
import SetupView from '../views/exam/SetupView.vue'
import ExamQuestionView from '../views/exam/ExamQuestionView.vue'
import MyPageView from '../views/mypage/MyPageView.vue'
import LoginView from '../views/auth/LoginView.vue'
import OAuth2Redirect from '../views/auth/OAuth2Redirect.vue'
import ExamFeedbackView from '../views/feedback/ExamFeedbackView.vue'
import PracticeFeedbackView from '../views/feedback/PracticeFeedbackView.vue'
import FeedbackListView from '../views/feedback/FeedbackListView.vue'

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
      component: PracticeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/practice/question',
      name: 'practice-question',
      component: PracticeQuestionView,
      meta: { requiresAuth: true }
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: MyPageView,
      meta: { requiresAuth: true }
    },
    {
      path: '/exam',
      name: 'exam',
      component: ExamView,
      meta: { requiresAuth: true }
    },
    {
      path: '/exam/setup',
      name: 'exam-setup',
      component: SetupView,
      meta: { requiresAuth: true }
    },
    {
      path: '/exam/question',
      name: 'exam-question',
      component: ExamQuestionView,
      meta: { requiresAuth: true }
    },
    {
      path: '/survey',
      name: 'survey',
      component: SurveyView,
      meta: { requiresAuth: true }
    },
    {
      path: '/survey/level',
      name: 'survey-level',
      component: SurveyLevelView,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/oauth2/redirect',
      name: 'oauth2-redirect',
      component: OAuth2Redirect
    },
    {
      path: '/auth/level',
      name: 'level-setting',
      component: () => import('../views/auth/LevelSettingView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/practice/feedback',
      name: 'practice-feedback',
      component: PracticeFeedbackView,
      meta: { requiresAuth: true }
    },
    {
      path: '/exam/feedback',
      name: 'exam-feedback',
      component: ExamFeedbackView,
      meta: { requiresAuth: true }
    },
    {
      path: '/exam/feedback/detail',
      name: 'exam-question-feedback',
      component: () => import('../views/feedback/ExamQuestionFeedbackView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/feedback',
      name: 'feedback-list',
      component: FeedbackListView,
      meta: { requiresAuth: true }
    }

  ]
})

import { useAuthStore } from '@/stores/auth'

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 인증이 필요한 페이지 접근 시
  if (to.meta.requiresAuth) {
    // 토큰은 있는데 유저 정보가 없으면 가져오기
    if (!authStore.isAuthenticated && localStorage.getItem('accessToken')) {
      await authStore.fetchUser()
    }

    // 로그인 되어 있는 경우
    if (authStore.isAuthenticated) {
      // 1. 신규 유저(목표 등급 없음)인 경우 목표 설정 페이지로 강제 이동
      // 무한 루프 방지: 현재 가려는 페이지가 목표 설정 페이지인 경우는 제외
      if (!authStore.user?.targetLevel && to.name !== 'level-setting') {
        console.log('[Router] Missing targetLevel. Redirecting to Level Setting...');
        return next({ name: 'level-setting' })
      }
      
      // 2. 이미 목표 등급이 있는데 목표 설정 페이지로 가려는 경우 홈으로 (선택 사항)
      if (authStore.user?.targetLevel && to.name === 'level-setting') {
        return next({ name: 'home' })
      }

      return next()
    }

    // 로그인 안되어 있으면 로그인 페이지로
    if (confirm('로그인이 필요한 서비스입니다. 로그인 하시겠습니까?')) {
        return next('/login')
    } else {
        return next(false)
    }
  }

  next()
})

export default router