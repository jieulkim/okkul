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
      component: MyPageView
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
      component: PracticeFeedbackView
    },
    {
      path: '/exam/feedback',
      name: 'exam-feedback',
      component: ExamFeedbackView
    }

  ]
})

export default router