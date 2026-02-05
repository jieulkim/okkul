import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './assets/global-styles.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Google Analytics에 환경 정보 전송
// GA4에서 'environment'라는 이름의 커스텀 측정기준(custom dimension)을 먼저 설정해야 합니다.
if (typeof window.gtag === 'function') {
  window.gtag('set', {
    'environment': import.meta.env.MODE
  });
  console.log(`Google Analytics environment set to: ${import.meta.env.MODE}`);
}

app.mount('#app')
