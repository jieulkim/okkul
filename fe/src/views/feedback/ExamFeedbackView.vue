<script setup>
import { ref, computed, onMounted, inject } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { examApi } from '@/api';

const router = useRouter();
const route = useRoute();
const isDarkMode = inject('isDarkMode', ref(false));

const examResult = ref(null);
const selectedQuestionIndex = ref(null);
const isLoading = ref(true);
const showDetailModal = ref(false);

// 시험 결과 로드
const loadExamResult = async () => {
  try {
    isLoading.value = true;
    // const examApi = new Exam(); // Removed - using shared instance
    const examId = parseInt(route.query.examId);
    
    if (!examId) {
      alert('시험 ID가 없습니다.');
      router.push('/exam');
      return;
    }
    
    // Mock Mode Check
    let response;
    if (import.meta.env.VITE_USE_MOCK_DATA === 'true') {
        const mockQuestions = Array.from({ length: 15 }, (_, i) => ({
            questionOrder: i + 1,
            questionText: `[Mock] Feedback Question ${i + 1}`,
            sttScript: "This is a mock answer script provided for testing purposes.",
            enhancedScript: "This is a mock answer script provided for testing purposes.",
            grammar: { score: 85, feedback: "Good grammar." },
            vocabulary: { score: 80, feedback: "Good vocabulary." },
            logic: { score: 75, feedback: "Logical flow is clear." },
            fluency: { score: 90, feedback: "Very fluent." },
            relevance: { score: 88, feedback: "Relevant answer." }
        }));

        response = {
            data: {
                createdAt: new Date().toISOString(),
                summary: {
                    grade: 'IM2',
                    totalScore: 85,
                    categoryScores: {
                        grammar: 85,
                        vocabulary: 80,
                        logic: 75,
                        fluency: 90,
                        relevance: 88
                    },
                    comment: "This is a mock summary comment. Use this to verify the UI layout and components.",
                    strengths: "Mock strength: Good delivery.",
                    weakness: "Mock weakness: Needs more complex sentence structures."
                },
                questionResults: mockQuestions
            }
        };
        examResult.value = response.data;
    } else {
        const response = await examApi.getExamResult(examId);
        examResult.value = response.data;
    }
    
  } catch (error) {
    console.error('시험 결과 로드 실패:', error);
    alert('시험 결과를 불러오는데 실패했습니다.');
    router.push('/exam');
  } finally {
    isLoading.value = false;
  }
};

// 오각형 차트 데이터
const radarData = computed(() => {
  if (!examResult.value?.summary?.categoryScores) return [];
  
  const scores = examResult.value.summary.categoryScores;
  return [
    { label: '문법', value: scores.grammar || 0, key: 'grammar' },
    { label: '어휘', value: scores.vocabulary || 0, key: 'vocabulary' },
    { label: '논리', value: scores.logic || 0, key: 'logic' },
    { label: '유창성', value: scores.fluency || 0, key: 'fluency' },
    { label: '주제적합성', value: scores.relevance || 0, key: 'relevance' }
  ];
});

// 오각형 차트 포인트 계산
const radarPoints = computed(() => {
  const centerX = 150;
  const centerY = 150;
  const maxRadius = 110;
  
  return radarData.value.map((item, index) => {
    const angle = (Math.PI * 2 * index) / 5 - Math.PI / 2;
    const radius = (item.value / 100) * maxRadius;
    return {
      x: centerX + radius * Math.cos(angle),
      y: centerY + radius * Math.sin(angle),
      label: item.label,
      value: item.value
    };
  });
});

const radarPointsString = computed(() => {
  return radarPoints.value.map(p => `${p.x},${p.y}`).join(' ');
});

// 라벨 위치 계산
const getLabelPosition = (index) => {
  const centerX = 150;
  const centerY = 150;
  const labelRadius = 135;
  const angle = (Math.PI * 2 * index) / 5 - Math.PI / 2;
  
  return {
    x: centerX + labelRadius * Math.cos(angle),
    y: centerY + labelRadius * Math.sin(angle)
  };
};

// 문항 클릭
const selectQuestion = (index) => {
  selectedQuestionIndex.value = index;
  showDetailModal.value = true;
};

// 선택된 문항
const selectedQuestion = computed(() => {
  if (selectedQuestionIndex.value === null) return null;
  return examResult.value?.questionResults?.[selectedQuestionIndex.value];
});

// 하이라이트 처리 로직
const buildHighlightedText = (stt, enhanced) => {
  if (!stt || !enhanced) return enhanced || stt || '';
  
  const sttWords = stt.toLowerCase().split(/\s+/);
  const enhancedWords = enhanced.split(/\s+/);
  
  return enhancedWords.map((word, idx) => {
    const cleanWord = word.replace(/[.,!?;:]/g, '');
    const cleanSTT = sttWords[idx]?.replace(/[.,!?;:]/g, '') || '';
    const isDifferent = cleanWord.toLowerCase() !== cleanSTT;
    
    return {
      text: word,
      highlighted: isDifferent
    };
  });
};

const highlightedScript = computed(() => {
  if (!selectedQuestion.value) return [];
  return buildHighlightedText(
    selectedQuestion.value.sttScript,
    selectedQuestion.value.enhancedScript
  );
});

onMounted(() => {
  loadExamResult();
});
</script>

<template>
  <div class="exam-result-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 로딩 -->
    <div v-if="isLoading" class="loading-screen">
      <div class="spinner"></div>
      <p>결과를 불러오는 중...</p>
    </div>

    <!-- 결과 화면 -->
    <template v-else-if="examResult">
      <div class="result-container">
        <!-- 헤더 -->
        <div class="result-header">
          <button @click="router.push('/')" class="back-btn">
            <span class="material-icons">arrow_back</span>
            홈으로
          </button>
          <div class="header-content">
            <h1 class="result-title">모의고사 결과</h1>
            <p class="exam-date">{{ new Date(examResult.createdAt).toLocaleDateString('ko-KR') }}</p>
          </div>
        </div>

        <!-- 종합 피드백 섹션 -->
        <div class="summary-section">
          <!-- 점수 카드 -->
          <div class="score-card">
            <h3>예상 등급</h3>
            <div class="grade-display">
              <div class="grade">{{ examResult.summary.grade }}</div>
              <div class="score">{{ Math.round(examResult.summary.totalScore) }}점</div>
            </div>
          </div>

          <!-- 오각형 차트 -->
          <div class="radar-chart-card">
            <h3>영역별 분석</h3>
            <svg viewBox="0 0 300 300" class="radar-chart">
              <!-- 배경 그리드 -->
              <g class="grid">
                <polygon 
                  v-for="i in 5" 
                  :key="`grid-${i}`"
                  :points="radarData.map((_, idx) => {
                    const angle = (Math.PI * 2 * idx) / 5 - Math.PI / 2;
                    const radius = (i * 110) / 5;
                    return `${150 + radius * Math.cos(angle)},${150 + radius * Math.sin(angle)}`;
                  }).join(' ')"
                  fill="none"
                  :stroke="isDarkMode ? '#334155' : '#e2e8f0'"
                  stroke-width="1"
                />
              </g>
              
              <!-- 축선 -->
              <g class="axes">
                <line 
                  v-for="(item, index) in radarData"
                  :key="`axis-${index}`"
                  x1="150" y1="150"
                  :x2="150 + 110 * Math.cos((Math.PI * 2 * index) / 5 - Math.PI / 2)"
                  :y2="150 + 110 * Math.sin((Math.PI * 2 * index) / 5 - Math.PI / 2)"
                  :stroke="isDarkMode ? '#475569' : '#cbd5e1'"
                  stroke-width="1"
                />
              </g>

              <!-- 데이터 폴리곤 -->
              <polygon
                :points="radarPointsString"
                fill="rgba(255, 215, 0, 0.3)"
                stroke="#FFD700"
                stroke-width="2"
              />

              <!-- 포인트 -->
              <circle
                v-for="(point, index) in radarPoints"
                :key="`point-${index}`"
                :cx="point.x"
                :cy="point.y"
                r="4"
                fill="#FFD700"
              />

              <!-- 라벨 -->
              <g class="labels">
                <text 
                  v-for="(item, index) in radarData"
                  :key="`label-${index}`"
                  :x="getLabelPosition(index).x"
                  :y="getLabelPosition(index).y"
                  text-anchor="middle"
                  :class="{ 'dark-label': isDarkMode }"
                  class="chart-label"
                >
                  <tspan>{{ item.label }}</tspan>
                  <tspan :x="getLabelPosition(index).x" dy="15" class="score-text">{{ item.value }}</tspan>
                </text>
              </g>
            </svg>

            <!-- 영역별 점수 목록 -->
            <div class="scores-list">
              <div v-for="item in radarData" :key="item.label" class="score-item">
                <span class="score-label">{{ item.label }}</span>
                <div class="score-bar">
                  <div class="score-fill" :style="{ width: item.value + '%' }"></div>
                </div>
                <span class="score-value">{{ item.value }}</span>
              </div>
            </div>
          </div>

          <!-- 총평 -->
          <div class="comment-card">
            <h3>총평</h3>
            <p class="comment-text">{{ examResult.summary.comment }}</p>
            
            <div class="strengths-weakness">
              <div class="strength-box">
                <h4>강점</h4>
                <p>{{ examResult.summary.strengths }}</p>
              </div>
              <div class="weakness-box">
                <h4>개선 필요</h4>
                <p>{{ examResult.summary.weakness }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 문항별 세부 피드백 -->
        <div class="questions-section">
          <h2 class="section-title">문항별 세부 피드백</h2>
          
          <div class="questions-grid">
            <div 
              v-for="(question, index) in examResult.questionResults"
              :key="index"
              class="question-card"
              @click="selectQuestion(index)"
            >
              <div class="question-number">Q{{ question.questionOrder }}</div>
              <div class="question-preview">
                <p class="question-text">{{ question.questionText }}</p>
                <div class="scores-mini">
                  <span class="mini-score">문법 {{ question.grammar?.score || 0 }}</span>
                  <span class="mini-score">어휘 {{ question.vocabulary?.score || 0 }}</span>
                  <span class="mini-score">논리 {{ question.logic?.score || 0 }}</span>
                </div>
              </div>
              <span class="material-icons">chevron_right</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 문항 상세 모달 -->
      <transition name="modal">
        <div v-if="showDetailModal && selectedQuestion" class="modal-overlay" @click="showDetailModal = false">
          <div class="modal-card" @click.stop>
            <div class="modal-header">
              <h3>Question {{ selectedQuestion.questionOrder }}</h3>
              <button @click="showDetailModal = false" class="close-btn">
                <span class="material-icons">close</span>
              </button>
            </div>

            <div class="modal-body">
              <!-- 질문 -->
              <div class="detail-section">
                <h4>질문</h4>
                <p class="detail-text">{{ selectedQuestion.questionText }}</p>
              </div>

              <!-- 내 답변 -->
              <div class="detail-section">
                <h4>내 답변 (STT)</h4>
                <div class="script-box">
                  <p class="detail-text">{{ selectedQuestion.sttScript }}</p>
                </div>
              </div>

              <!-- 개선된 답변 (하이라이트) -->
              <div class="detail-section">
                <h4>오꿀쌤의 교정 스크립트</h4>
                <div class="highlighted-script">
                  <span 
                    v-for="(word, idx) in highlightedScript" 
                    :key="idx"
                    :class="{ 'highlighted-word': word.highlighted }"
                    class="script-word"
                  >
                    {{ word.text }}
                  </span>
                </div>
                <p class="highlight-guide">노란색 표시는 개선된 부분입니다</p>
              </div>

              <!-- 영역별 피드백 -->
              <div class="detail-section">
                <h4>영역별 피드백</h4>
                <div class="feedback-areas">
                  <div class="feedback-item">
                    <div class="feedback-header">
                      <span class="feedback-label">문법</span>
                      <span class="feedback-score">{{ selectedQuestion.grammar?.score || 0 }}점</span>
                    </div>
                    <p class="feedback-text">{{ selectedQuestion.grammar?.feedback || '피드백 없음' }}</p>
                  </div>
                  <div class="feedback-item">
                    <div class="feedback-header">
                      <span class="feedback-label">어휘</span>
                      <span class="feedback-score">{{ selectedQuestion.vocabulary?.score || 0 }}점</span>
                    </div>
                    <p class="feedback-text">{{ selectedQuestion.vocabulary?.feedback || '피드백 없음' }}</p>
                  </div>
                  <div class="feedback-item">
                    <div class="feedback-header">
                      <span class="feedback-label">논리</span>
                      <span class="feedback-score">{{ selectedQuestion.logic?.score || 0 }}점</span>
                    </div>
                    <p class="feedback-text">{{ selectedQuestion.logic?.feedback || '피드백 없음' }}</p>
                  </div>
                  <div class="feedback-item">
                    <div class="feedback-header">
                      <span class="feedback-label">유창성</span>
                      <span class="feedback-score">{{ selectedQuestion.fluency?.score || 0 }}점</span>
                    </div>
                    <p class="feedback-text">{{ selectedQuestion.fluency?.feedback || '피드백 없음' }}</p>
                  </div>
                  <div class="feedback-item">
                    <div class="feedback-header">
                      <span class="feedback-label">주제 적합성</span>
                      <span class="feedback-score">{{ selectedQuestion.relevance?.score || 0 }}점</span>
                    </div>
                    <p class="feedback-text">{{ selectedQuestion.relevance?.feedback || '피드백 없음' }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </template>
  </div>
</template>

<style scoped>
.exam-result-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 48px 24px;
}

/* 로딩 */
.loading-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  gap: 24px;
}

.spinner {
  width: 56px;
  height: 56px;
  border: 4px solid var(--bg-tertiary);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-screen p {
  font-size: 1.125rem;
  color: var(--text-secondary);
  font-weight: 600;
}

/* 결과 컨테이너 */
.result-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 헤더 */
.result-header {
  margin-bottom: 48px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.95rem;
  box-shadow: var(--shadow-sm);
  color: var(--text-secondary);
}

.back-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  transform: translateY(-2px);
}

.header-content {
  flex: 1;
}

.result-title {
  font-size: 2.25rem;
  font-weight: 800;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.exam-date {
  color: var(--text-tertiary);
  font-size: 1rem;
  font-weight: 500;
}

/* 종합 피드백 */
.summary-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 32px;
  margin-bottom: 64px;
}

.score-card,
.radar-chart-card,
.comment-card {
  background: var(--bg-secondary);
  border-radius: 24px;
  padding: 40px;
  border: var(--border-primary);
  box-shadow: var(--shadow-md);
}

.score-card h3,
.radar-chart-card h3,
.comment-card h3 {
  font-size: 1.25rem;
  font-weight: 800;
  margin-bottom: 32px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-card h3::before,
.radar-chart-card h3::before,
.comment-card h3::before {
  content: "";
  display: block;
  width: 4px;
  height: 20px;
  background: var(--primary-color);
  border-radius: 2px;
}

.grade-display {
  text-align: center;
  padding: 24px 0;
}

.grade {
  font-size: 5rem;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 16px;
  line-height: 1;
}

.score {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-secondary);
}

/* 오각형 차트 */
.radar-chart {
  width: 100%;
  max-width: 280px;
  margin: 0 auto 40px;
  display: block;
}

.chart-label {
  font-size: 12px;
  font-weight: 700;
  fill: var(--text-secondary);
}

.score-text {
  font-size: 10px;
  fill: var(--text-tertiary);
  font-weight: 600;
}

/* 점수 목록 */
.scores-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.score-item .score-label {
  min-width: 80px;
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.score-bar {
  flex: 1;
  height: 8px;
  background: var(--bg-tertiary);
  border-radius: 4px;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  background: var(--primary-color);
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 4px;
}

.score-value {
  min-width: 40px;
  text-align: right;
  font-weight: 700;
  color: var(--primary-color);
  font-size: 1rem;
}

/* 총평 */
.comment-text {
  line-height: 1.7;
  color: var(--text-primary);
  margin-bottom: 32px;
  font-size: 1rem;
}

.strengths-weakness {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 768px) {
  .strengths-weakness {
    grid-template-columns: 1fr;
  }
}

.strength-box,
.weakness-box {
  padding: 24px;
  border-radius: 16px;
  border: 1px solid transparent;
}

.strength-box {
  background: rgba(16, 185, 129, 0.05);
  border-color: rgba(16, 185, 129, 0.2);
}

.weakness-box {
  background: rgba(239, 68, 68, 0.05);
  border-color: rgba(239, 68, 68, 0.2);
}

.strength-box h4,
.weakness-box h4 {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.strength-box h4 { color: #059669; }
.weakness-box h4 { color: #dc2626; }

.strength-box p,
.weakness-box p {
  line-height: 1.6;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* 문항별 피드백 */
.section-title {
  font-size: 1.75rem;
  font-weight: 800;
  margin-bottom: 32px;
  color: var(--text-primary);
}

.questions-grid {
  display: grid;
  gap: 16px;
}

.question-card {
  display: flex;
  align-items: center;
  gap: 24px;
  background: var(--bg-secondary);
  border-radius: 20px;
  padding: 24px 32px;
  border: 1px solid var(--border-primary);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
}

.question-card:hover {
  border-color: var(--primary-color);
  transform: translateX(8px);
  box-shadow: var(--shadow-md);
}

.question-number {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-light);
  color: #8B7300;
  font-weight: 700;
  font-size: 1.125rem;
  border-radius: 12px;
}

.question-preview {
  flex: 1;
}

.question-text {
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.5;
}

.dark-mode .question-text {
  color: #f1f5f9;
}

.scores-mini {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.mini-score {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
  padding: 4px 12px;
  background: #f1f5f9;
  border-radius: 6px;
}

.dark-mode .mini-score {
  background: var(--bg-primary);
  color: #94a3b8;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  padding: 20px;
}

.modal-card {
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  max-width: 900px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  border: var(--border-primary);
  box-shadow: var(--shadow-lg);
}

.dark-mode .modal-card {
  background: var(--bg-secondary);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 32px;
  border-bottom: 2px solid #e2e8f0;
  position: sticky;
  top: 0;
  background: white;
  z-index: 1;
}

.dark-mode .modal-header {
  background: var(--bg-secondary);
  border-bottom-color: #FFFFFF;
}

.modal-header h3 {
  font-size: 24px;
  font-weight: 900;
  color: var(--text-primary);
}

.dark-mode .modal-header h3 {
  color: #f1f5f9;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #f1f5f9;
}

.dark-mode .close-btn:hover {
  background: #334155;
}

.close-btn .material-icons {
  color: #64748b;
  font-size: 24px;
}

.modal-body {
  padding: 32px;
}

.detail-section {
  margin-bottom: 32px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.dark-mode .detail-section h4 {
  color: #f1f5f9;
}

.detail-text {
  line-height: 1.8;
  color: var(--text-primary);
  font-size: 15px;
}

.dark-mode .detail-text {
  color: #e2e8f0;
}

.script-box {
  background: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
}

.dark-mode .script-box {
  background: var(--bg-tertiary);
  border-color: #FFFFFF;
}

/* 하이라이트 스크립트 */
.highlighted-script {
  line-height: 2;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: var(--border-radius);
  border: var(--border-secondary);
  margin-bottom: 12px;
}

.dark-mode .highlighted-script {
  background: var(--bg-primary);
}

.script-word {
  margin-right: 6px;
  display: inline-block;
}

.highlighted-word {
  background: var(--primary-color);
  color: #000000;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 900;
  border: 1px solid #000;
  cursor: help;
  display: inline-block;
  margin: 2px;
}

.dark-mode .highlighted-word {
  border-color: #FFFFFF;
  background: var(--primary-color); /* Added for dark mode consistency */
  color: #000000; /* Added for dark mode consistency */
}

.highlight-guide {
  font-size: 13px;
  color: #64748b;
  font-style: italic;
}

/* 영역별 피드백 */
.feedback-areas {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-item {
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border-left: 4px solid #FFD700;
  transition: all 0.2s;
}

.feedback-item:hover {
  background: #f1f5f9;
  transform: translateX(4px);
}

.dark-mode .feedback-item {
  background: var(--bg-tertiary);
  border-left-color: var(--primary-color);
}

.dark-mode .feedback-item:hover {
  background: var(--bg-secondary);
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.feedback-label {
  font-weight: 700;
  color: var(--text-primary);
  font-size: 16px;
}

.dark-mode .feedback-label {
  color: #f1f5f9;
}

.feedback-score {
  font-weight: 900;
  color: #FFD700;
  font-size: 18px;
}

.feedback-text {
  color: #64748b;
  line-height: 1.7;
  font-size: 14px;
}

.dark-mode .feedback-text {
  color: #94a3b8;
}

/* 모달 애니메이션 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-card,
.modal-leave-active .modal-card {
  transition: transform 0.3s ease;
}

.modal-enter-from .modal-card,
.modal-leave-to .modal-card {
  transform: scale(0.9);
}

/* 반응형 */
@media (max-width: 768px) {
  .result-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .summary-section {
    grid-template-columns: 1fr;
  }
  
  .modal-body {
    padding: 24px 20px;
  }
}
</style>