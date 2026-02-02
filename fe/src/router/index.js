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
    // 1. 이미 로그인 되어 있으면 통과
    if (authStore.isAuthenticated) {
      return next()
    }

    // 2. 토큰이 있다면 정보 가져오기 시도
    if (authStore.token) {
      await authStore.fetchUser()
      if (authStore.isAuthenticated) {
        return next()
      }
    }

    // 3. 로그인 안되어 있고 토큰도 유효하지 않으면 로그인 페이지로
    if (confirm('로그인이 필요한 서비스입니다. 로그인 하시겠습니까?')) {
        return next('/login')
    } else {
        return next(false)
    }
  }

  next()
})

export default router