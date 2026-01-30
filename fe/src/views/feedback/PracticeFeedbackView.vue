<script setup>
import { ref, computed, onMounted, inject } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const isDarkMode = inject('isDarkMode', ref(false));

// 피드백 데이터 (API에서 받은 데이터 - localStorage 또는 props로 전달받음)
const feedbackData = ref(null);
const selectedCorrectionIndex = ref(null);

// 초기화
onMounted(() => {
  loadFeedback();
});

const loadFeedback = () => {
  // localStorage에서 피드백 데이터 로드
  const practiceId = route.query.practiceId;
  const questionId = route.query.questionId;
  
  if (practiceId && questionId) {
    const savedData = localStorage.getItem(`practice_feedback_${practiceId}_${questionId}`);
    if (savedData) {
      feedbackData.value = JSON.parse(savedData);
    } else {
      alert('피드백 데이터를 찾을 수 없습니다.');
      router.push('/practice');
    }
  }
};

// 개선된 스크립트 (하이라이트 처리)
const enhancedScript = computed(() => {
  if (!feedbackData.value?.feedbackResult?.scriptCorrections) return '';
  
  let script = feedbackData.value.userEnglishScript || '';
  const corrections = feedbackData.value.feedbackResult.scriptCorrections;
  
  // 모든 교정 항목을 적용한 스크립트 생성
  corrections.forEach(correction => {
    script = script.replace(correction.originalSegment, correction.correctedSegment);
  });
  
  return script;
});

// 하이라이트된 단어 배열
const highlightedWords = computed(() => {
  if (!feedbackData.value?.feedbackResult?.scriptCorrections) return [];
  
  const original = feedbackData.value.userEnglishScript || '';
  const enhanced = enhancedScript.value;
  const corrections = feedbackData.value.feedbackResult.scriptCorrections;
  
  const words = enhanced.split(' ');
  
  return words.map(word => {
    const isHighlighted = corrections.some(c => 
      c.correctedSegment.includes(word) && !c.originalSegment.includes(word)
    );
    
    return {
      text: word,
      highlighted: isHighlighted
    };
  });
});

// 교정 항목 선택
const selectCorrection = (index) => {
  selectedCorrectionIndex.value = index;
};
</script>

<template>
  <div class="practice-feedback-page" :class="{ 'dark-mode': isDarkMode }">
    <div v-if="!feedbackData" class="loading-screen">
      <div class="spinner"></div>
      <p>피드백을 불러오는 중...</p>
    </div>

    <template v-else>
      <div class="feedback-container">
        <!-- 헤더 -->
        <div class="feedback-header">
          <button @click="router.push('/practice')" class="back-btn">
            <span class="material-icons">arrow_back</span>
            연습으로 돌아가기
          </button>
          <h1 class="feedback-title">유형별 연습 피드백</h1>
        </div>

        <!-- 개선된 스크립트 섹션 -->
        <div class="enhanced-section">
          <div class="section-card">
            <h2 class="section-title">
              <span class="material-icons">auto_fix_high</span>
              오꿀쌤의 교정 스크립트
            </h2>
            <p class="section-description">노란색 표시는 개선된 부분입니다. 단어를 클릭하면 상세 설명을 볼 수 있습니다.</p>
            
            <div class="script-box enhanced-script">
              <span 
                v-for="(word, idx) in highlightedWords" 
                :key="idx"
                :class="{ 'highlighted-word': word.highlighted }"
                class="script-word"
              >
                {{ word.text }}
              </span>
            </div>
          </div>

          <!-- 원본 스크립트 비교 -->
          <div class="section-card">
            <h3 class="subsection-title">내 원본 답변</h3>
            <div class="script-box original-script">
              <p>{{ feedbackData.userEnglishScript }}</p>
            </div>
          </div>
        </div>

        <!-- 교정 항목 상세 -->
        <div class="corrections-section">
          <h2 class="section-title">
            <span class="material-icons">edit_note</span>
            문법 및 어휘 교정
          </h2>
          
          <div v-if="feedbackData.feedbackResult?.scriptCorrections?.length > 0" class="corrections-list">
            <div 
              v-for="(correction, index) in feedbackData.feedbackResult.scriptCorrections"
              :key="index"
              class="correction-item"
              :class="{ 'selected': selectedCorrectionIndex === index }"
              @click="selectCorrection(index)"
            >
              <div class="correction-number">{{ index + 1 }}</div>
              <div class="correction-content">
                <div class="correction-comparison">
                  <div class="before">
                    <span class="label">수정 전</span>
                    <span class="text original">{{ correction.originalSegment }}</span>
                  </div>
                  <span class="material-icons arrow">arrow_forward</span>
                  <div class="after">
                    <span class="label">수정 후</span>
                    <span class="text corrected">{{ correction.correctedSegment }}</span>
                  </div>
                </div>
                <div class="correction-comment">
                  <span class="material-icons">lightbulb</span>
                  {{ correction.comment }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-corrections">
            <span class="material-icons">check_circle</span>
            <p>완벽합니다! 교정이 필요한 부분이 없습니다.</p>
          </div>
        </div>

        <!-- 종합 피드백 -->
        <div class="overall-section">
          <div class="section-card overall-card">
            <h2 class="section-title">
              <span class="material-icons">rate_review</span>
              종합 평가
            </h2>
            <div class="overall-content">
              <p class="overall-comment">{{ feedbackData.feedbackResult?.overallComment || '종합 평가가 제공되지 않았습니다.' }}</p>
            </div>
          </div>
        </div>

        <!-- 액션 버튼 -->
        <div class="action-buttons">
          <button @click="router.push('/practice')" class="action-btn retry-btn">
            <span class="material-icons">refresh</span>
            다시 연습하기
          </button>
          <button @click="router.push('/')" class="action-btn home-btn">
            <span class="material-icons">home</span>
            홈으로
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.practice-feedback-page {
  min-height: 100vh;
  background: var(--bg-primary);
  font-family: 'Noto Sans KR', sans-serif;
  padding: 40px 20px;
}

.dark-mode {
  background: var(--bg-primary);
  color: var(--text-primary);
}

/* 로딩 */
.loading-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  gap: 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top-color: #FFD700;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 컨테이너 */
.feedback-container {
  max-width: 1000px;
  margin: 0 auto;
}

/* 헤더 */
.feedback-header {
  margin-bottom: 40px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--bg-secondary);
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}

.back-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

.dark-mode .back-btn {
  background: var(--bg-tertiary);
  border-color: #334155;
  color: #f1f5f9;
}

.feedback-title {
  font-size: 36px;
  font-weight: 900;
  color: var(--primary-color);
  -webkit-text-fill-color: initial;
}

/* 섹션 */
.enhanced-section,
.corrections-section,
.overall-section {
  margin-bottom: 40px;
}

.section-card {
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  padding: 32px;
  box-shadow: var(--shadow-md);
  border: var(--border-primary);
  margin-bottom: 24px;
}

.dark-mode .section-card {
  background: var(--bg-secondary);
  border-color: #FFFFFF;
}

.section-title {
  font-size: 24px;
  font-weight: 900;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-primary);
}

.dark-mode .section-title {
  color: #f1f5f9;
}

.section-title .material-icons {
  color: var(--primary-color);
  font-size: 28px;
}

.section-description {
  color: #64748b;
  margin-bottom: 20px;
  font-size: 14px;
  line-height: 1.6;
}

.subsection-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.dark-mode .subsection-title {
  color: #f1f5f9;
}

/* 스크립트 박스 */
.script-box {
  padding: 24px;
  border-radius: 12px;
  line-height: 2;
  font-size: 16px;
}

.enhanced-script {
  background: var(--bg-tertiary);
  border: var(--border-secondary);
}

.dark-mode .enhanced-script {
  background: var(--bg-primary);
}

.original-script {
  background: var(--bg-tertiary);
  border: var(--border-thin);
}

.dark-mode .original-script {
  background: var(--bg-tertiary);
  border-color: #FFFFFF;
}

.script-word {
  margin-right: 6px;
  display: inline-block;
  transition: all 0.2s;
}

.highlighted-word {
  background: var(--primary-color);
  color: #000000;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 900;
  border: 1px solid #000;
  cursor: pointer;
  display: inline-block;
  margin: 2px;
}

.dark-mode .highlighted-word {
  background: var(--primary-color);
  color: #000000;
  border-color: #FFFFFF;
}

/* 교정 목록 */
.corrections-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.correction-item {
  display: flex;
  gap: 20px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  padding: 24px;
  border: var(--border-primary);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.correction-item:hover,
.correction-item.selected {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
  background: var(--primary-color);
}

.dark-mode .correction-item {
  background: var(--bg-primary);
  border-color: #334155;
}

.correction-number {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-color);
  color: #000000;
  font-weight: 900;
  font-size: 16px;
  border-radius: 50%;
  border: var(--border-secondary);
}

.correction-content {
  flex: 1;
}

.correction-comparison {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.before,
.after {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
}

.text {
  font-size: 16px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 8px;
}

.original {
  background: #fee2e2;
  color: #dc2626;
  text-decoration: line-through;
}

.corrected {
  background: #dcfce7;
  color: #16a34a;
}

.arrow {
  color: #94a3b8;
  font-size: 24px;
}

.correction-comment {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: var(--border-radius);
  border-left: 4px solid var(--primary-color);
  line-height: 1.6;
  color: var(--text-secondary);
}

.dark-mode .correction-comment {
  background: var(--bg-tertiary);
  color: #cbd5e1;
}

.correction-comment .material-icons {
  color: #FFD700;
  font-size: 20px;
  margin-top: 2px;
}

.no-corrections {
  text-align: center;
  padding: 60px 20px;
  color: #10b981;
}

.no-corrections .material-icons {
  font-size: 64px;
  margin-bottom: 16px;
}

.no-corrections p {
  font-size: 18px;
  font-weight: 600;
}

/* 종합 평가 */
.overall-card {
  background: var(--primary-color);
  border-color: #000000;
}

.dark-mode .overall-card {
  background: linear-gradient(135deg, #422006 0%, #78350f 100%);
}

.overall-content {
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  border: var(--border-thin);
}

.dark-mode .overall-content {
  background: var(--bg-primary);
}

.overall-comment {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-primary);
}

.dark-mode .overall-comment {
  color: #f1f5f9;
}

/* 액션 버튼 */
.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 48px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 32px;
  border-radius: var(--border-radius);
  border: var(--border-secondary);
  font-weight: 900;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.retry-btn {
  background: var(--primary-color);
  color: #000000;
}

.retry-btn:hover {
  transform: translate(-0.05em, -0.05em);
  box-shadow: var(--shadow-lg);
}

.home-btn {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.home-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

.dark-mode .home-btn {
  background: var(--bg-tertiary);
  border-color: #334155;
  color: #f1f5f9;
}

/* 반응형 */
@media (max-width: 768px) {
  .correction-comparison {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .arrow {
    transform: rotate(90deg);
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>