<script setup>
import { ref, computed, onMounted, inject } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { historyApi } from '@/api';

const router = useRouter();
const route = useRoute();

// 피드백 데이터 (API에서 받은 데이터 - localStorage 또는 props로 전달받음)
const errorMessage = ref(''); // 에러 메시지 상태 추가
const feedbackData = ref(null);
const selectedCorrectionIndex = ref(null);
const isLoading = ref(false);

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  loadFeedback();
});

const loadFeedback = async () => {
  const practiceId = route.query.practiceId;
  // questionId 파싱 안전하게 처리
  let qParam = route.query.questionId;
  let questionId = qParam ? parseInt(qParam) : null;
  
  if (!practiceId) {
    errorMessage.value = '잘못된 접근입니다. 연습 ID가 필요합니다.';
    return;
  }

  try {
    isLoading.value = true;
    errorMessage.value = '';

    // Mock Mode Support
    if (import.meta.env.MODE === 'mock') {
         console.warn('Mock Mode: Using dummy practice feedback data');
         await new Promise(resolve => setTimeout(resolve, 500));
         feedbackData.value = {
             userEnglishScript: "I like watching movies. My favorite movie is Interstellar. It is very interesting.",
             feedbackResult: {
                 overallComment: "전반적으로 자연스러운 답변입니다. 다만, 형용사 사용을 더 다양하게 하면 좋겠습니다.",
                 scriptCorrections: [
                     {
                         originalSegment: "It is very interesting",
                         correctedSegment: "It is absolutely fascinating",
                         comment: "'Interesting'보다 'fascinating'이 더 강렬한 인상을 줍니다."
                     }
                 ]
             }
         };
         return;
    }

    // 1. 연습 히스토리 상세 조회
    console.log('[PracticeFeedback] Fetching detail for practiceId:', practiceId);
    const { data } = await historyApi.getPracticeHistoryDetail(practiceId);
    console.log('[PracticeFeedback] API Response:', data);
    
    if (!data || !data.questions || data.questions.length === 0) {
        throw new Error('이 연습에 대한 질문 데이터가 존재하지 않습니다.');
    }

    // questionId가 없으면 첫 번째 질문 사용
    if (!questionId) {
        questionId = data.questions[0].questionId;
        console.log('[PracticeFeedback] No questionId provided, defaulting to:', questionId);
        // URL 업데이트 (replace로 history stack 오염 방지)
        router.replace({ query: { ...route.query, questionId } });
    }

    // 2. 해당 질문 찾기
    const targetQuestion = data.questions.find(q => q.questionId === questionId);
    if (!targetQuestion) {
      console.error('[PracticeFeedback] Target question not found. Search ID:', questionId, 'Available:', data.questions.map(q => q.questionId));
      throw new Error('해당 질문을 찾을 수 없습니다.');
    }

    // 3. 마지막 시도 찾기 (또는 특정 시도)
    console.log('[PracticeFeedback] Target Question:', targetQuestion);
    // attempts가 없을 수도 있으므로 안전하게 접근
    const attempts = targetQuestion.attempts || [];
    if (attempts.length === 0) {
         throw new Error('이 질문에 대한 답변 시도 기록이 없습니다.');
    }
    
    const lastAttempt = attempts[attempts.length - 1];
    
    if (!lastAttempt || !lastAttempt.feedback) {
      console.error('[PracticeFeedback] No feedback found in last attempt:', lastAttempt);
      throw new Error('아직 피드백이 생성되지 않았거나, 데이터를 불러올 수 없습니다.');
    }

    // status 체크
    if (lastAttempt.feedback.status && lastAttempt.feedback.status !== 'COMPLETED') {
         console.warn('Feedback status is:', lastAttempt.feedback.status);
    }

    // 4. 데이터 매핑
    feedbackData.value = {
      userEnglishScript: lastAttempt.userAnswer?.englishScript || '',
      feedbackResult: {
        overallComment: [
          lastAttempt.feedback.fluencyFeedback,
          lastAttempt.feedback.logicFeedback,
          lastAttempt.feedback.relevanceFeedback
        ].filter(Boolean).join('\n\n') || '종합 평가가 아직 준비되지 않았습니다.',
        scriptCorrections: lastAttempt.feedback.sentenceDetails?.map(detail => ({
            originalSegment: detail.targetSegment,
            correctedSegment: detail.correctedSegment,
            comment: detail.comment
        })) || []
      }
    };
    console.log('[PracticeFeedback] Mapped Data:', feedbackData.value);

  } catch (error) {
    console.error('피드백 로드 실패:', error);
    errorMessage.value = error.message || '피드백 데이터를 불러오는 중 오류가 발생했습니다.';
  } finally {
      isLoading.value = false;
  }
};
</script>

<template>
  <div class="practice-feedback-page">
    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>피드백을 불러오는 중입니다...</p>
    </div>

    <div v-else-if="feedbackData" class="feedback-container">
        <div class="feedback-header">
          <button @click="router.push('/practice')" class="back-btn">
            <span class="material-icons">arrow_back</span>
            연습으로 돌아가기
          </button>
          <h1 class="feedback-title">유형별 연습 피드백</h1>
        </div>

        <div class="enhanced-section">
             <!-- ... existing ... -->
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
            다른 유형 연습하기
          </button>
          <button @click="router.push('/')" class="action-btn home-btn">
            <span class="material-icons">home</span>
            홈으로
          </button>
        </div>
    </div>

    <!-- 에러 화면 -->
    <div v-else class="error-screen">
      <span class="material-icons error-icon">error_outline</span>
      <p class="error-message">{{ errorMessage || '피드백 데이터를 불러올 수 없습니다.' }}</p>
      <button @click="router.push('/practice')" class="back-link btn-secondary">돌아가기</button>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

/* 에러 화면 스타일 */
.error-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  gap: 24px;
  text-align: center;
  color: var(--text-secondary);
}

.error-icon {
  font-size: 64px;
  color: #ef4444; /* Error red */
  margin-bottom: 8px;
}

.error-message {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  max-width: 400px;
  line-height: 1.6;
}

.btn-secondary {
    padding: 12px 24px;
    border-radius: 12px;
    background: #FFFFFF;
    border: 1px solid var(--border-primary);
    cursor: pointer;
    font-weight: 600;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.practice-feedback-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 48px 24px;
}

@keyframes slideUpFade {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 컨테이너 */
.feedback-container {
  max-width: 1000px;
  margin: 0 auto;
  animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

/* 헤더 */
.feedback-header {
  margin-bottom: 48px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
  color: var(--text-secondary);
  align-self: flex-start;
}

.back-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  transform: translateY(-2px);
}

.feedback-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-primary);
}

/* 섹션 */
.enhanced-section,
.corrections-section,
.overall-section {
  margin-bottom: 48px;
}

.section-card {
  background: var(--bg-secondary);
  border-radius: 24px;
  padding: 40px;
  box-shadow: var(--shadow-md);
  border: var(--border-primary);
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 800;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-primary);
}

.section-title .material-icons {
  color: var(--primary-color);
  font-size: 28px;
}

.section-description {
  color: var(--text-secondary);
  margin-bottom: 24px;
  font-size: 0.95rem;
  line-height: 1.6;
}

.subsection-title {
  font-size: 1.125rem;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--text-primary);
}

/* 스크립트 박스 */
.script-box {
  padding: 32px;
  border-radius: 16px;
  line-height: 1.8;
  font-size: 1.125rem;
}

.enhanced-script {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
}

.original-script {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
}

.script-word {
  margin-right: 6px;
  display: inline-block;
  transition: all 0.2s;
}

.highlighted-word {
  background: var(--primary-light);
  color: #E65100;
  padding: 2px 6px;
  border-radius: 6px;
  font-weight: 700;
  cursor: pointer;
  display: inline-block;
  margin: 2px;
}

/* 교정 목록 */
.corrections-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.correction-item {
  display: flex;
  gap: 24px;
  background: var(--bg-secondary);
  border-radius: 20px;
  padding: 32px;
  border: 1px solid var(--border-primary);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
}

.correction-item:hover {
  background: var(--bg-tertiary);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.correction-item.selected {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.correction-number {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-color);
  color: #212529;
  font-weight: 700;
  font-size: 1.125rem;
  border-radius: 50%;
}

.correction-content {
  flex: 1;
}

.correction-comparison {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.before,
.after {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.label {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.text {
  font-size: 1.125rem;
  font-weight: 600;
  padding: 10px 20px;
  border-radius: 10px;
}

.original {
  background: #fee2e2;
  color: #ef4444;
  text-decoration: line-through;
}

.corrected {
  background: #dcfce7;
  color: #16a34a;
}

.arrow {
  color: var(--text-tertiary);
  font-size: 24px;
}

.correction-comment {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
  border-left: 4px solid var(--primary-color);
  line-height: 1.6;
  color: var(--text-secondary);
  font-size: 0.95rem;
}

.correction-comment .material-icons {
  color: var(--primary-color);
  font-size: 20px;
  margin-top: 2px;
}

.no-corrections {
  text-align: center;
  padding: 64px 24px;
  color: #10b981;
}

.no-corrections .material-icons {
  font-size: 64px;
  margin-bottom: 20px;
}

.no-corrections p {
  font-size: 1.25rem;
  font-weight: 700;
}

/* 종합 평가 */
.overall-card {
  border-top: 4px solid var(--primary-color);
}

.overall-content {
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  border: var(--border-thin);
}

.overall-comment {
  font-size: 1.125rem;
  line-height: 1.8;
  color: var(--text-primary);
}

/* 액션 버튼 */
.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 64px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 40px;
  border-radius: 16px;
  font-weight: 700;
  font-size: 1.125rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
}

.retry-btn {
  background: var(--primary-color);
  color: #212529;
  border: none;
}

.retry-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.home-btn {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-primary);
}

.home-btn:hover {
  background: var(--bg-tertiary);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
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

/* 로딩 스타일 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  gap: 16px;
  color: var(--text-secondary);
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--border-primary);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>