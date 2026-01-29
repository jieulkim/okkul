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
                <h4>💪 강점</h4>
                <p>{{ examResult.summary.strengths }}</p>
              </div>
              <div class="weakness-box">
                <h4>📝 개선 필요</h4>
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
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.exam-result-page {
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

.loading-screen p {
  font-size: 16px;
  color: #64748b;
  font-weight: 600;
}

/* 결과 컨테이너 */
.result-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 헤더 */
.result-header {
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--bg-secondary);
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 15px;
  box-shadow: var(--shadow-sm);
}

.back-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

.dark-mode .back-btn {
  background: var(--bg-tertiary);
  border-color: #FFFFFF;
  color: var(--text-primary);
}

.header-content {
  flex: 1;
}

.result-title {
  font-size: 36px;
  font-weight: 900;
  margin-bottom: 8px;
  color: var(--primary-color);
  -webkit-text-fill-color: initial;
}

.exam-date {
  color: #64748b;
  font-size: 16px;
  font-weight: 500;
}

/* 종합 피드백 */
.summary-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 48px;
}

.score-card,
.radar-chart-card,
.comment-card {
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  padding: 32px;
  border: var(--border-primary);
  box-shadow: var(--shadow-md);
  transition: all 0.3s;
}

.dark-mode .score-card,
.dark-mode .radar-chart-card,
.dark-mode .comment-card {
  background: var(--bg-secondary);
  border-color: #FFFFFF;
}

.score-card h3,
.radar-chart-card h3,
.comment-card h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 20px;
  color: var(--text-primary);
}

.dark-mode .score-card h3,
.dark-mode .radar-chart-card h3,
.dark-mode .comment-card h3 {
  color: #f1f5f9;
}

.grade-display {
  text-align: center;
}

.grade {
  font-size: 64px;
  font-weight: 900;
  color: #FFD700;
  margin-bottom: 12px;
  text-shadow: 2px 2px 4px rgba(255, 215, 0, 0.3);
}

.score {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.dark-mode .score {
  color: #f1f5f9;
}

/* 오각형 차트 */
.radar-chart {
  width: 100%;
  max-width: 300px;
  margin: 0 auto 32px;
  display: block;
}

.chart-label {
  font-size: 13px;
  font-weight: 700;
  fill: var(--text-primary);
}

.dark-label {
  fill: #f1f5f9;
}

.score-text {
  font-size: 11px;
  fill: #64748b;
  font-weight: 600;
}

/* 점수 목록 */
.scores-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-item .score-label {
  min-width: 90px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.dark-mode .score-item .score-label {
  color: #f1f5f9;
}

.score-bar {
  flex: 1;
  height: 10px;
  background: #e2e8f0;
  border-radius: 5px;
  overflow: hidden;
}

.dark-mode .score-bar {
  background: #334155;
}

.score-fill {
  height: 100%;
  background: var(--primary-color);
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 5px;
  border-right: 1px solid #000;
}

.score-value {
  min-width: 45px;
  text-align: right;
  font-weight: 700;
  color: #FFD700;
  font-size: 15px;
}

/* 총평 */
.comment-text {
  line-height: 1.8;
  color: var(--text-primary);
  margin-bottom: 24px;
  font-size: 15px;
}

.dark-mode .comment-text {
  color: #e2e8f0;
}

.strengths-weakness {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 768px) {
  .strengths-weakness {
    grid-template-columns: 1fr;
  }
}

.strength-box,
.weakness-box {
  padding: 20px;
  border-radius: 12px;
  border: 2px solid;
}

.strength-box {
  background: #f0fdf4;
  border-color: #10b981;
}

.dark-mode .strength-box {
  background: #064e3b;
  border-color: #10b981;
}

.weakness-box {
  background: #fef2f2;
  border-color: #ef4444;
}

.dark-mode .weakness-box {
  background: #450a0a;
  border-color: #ef4444;
}

.strength-box h4,
.weakness-box h4 {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.dark-mode .strength-box h4,
.dark-mode .weakness-box h4 {
  color: #f1f5f9;
}

.strength-box p,
.weakness-box p {
  line-height: 1.6;
  color: #374151;
  font-size: 14px;
}

.dark-mode .strength-box p,
.dark-mode .weakness-box p {
  color: #d1d5db;
}

/* 문항별 피드백 */
.section-title {
  font-size: 28px;
  font-weight: 900;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.dark-mode .section-title {
  color: #f1f5f9;
}

.questions-grid {
  display: grid;
  gap: 16px;
}

.question-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  padding: 24px;
  border: var(--border-primary);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.question-card:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

.dark-mode .question-card {
  background: var(--bg-secondary);
  border-color: #FFFFFF;
}

.dark-mode .question-card:hover {
  border-color: #FFD700;
}

.question-number {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-color);
  color: #000000;
  font-weight: 900;
  font-size: 18px;
  border-radius: 50%;
  border: var(--border-secondary);
  box-shadow: var(--shadow-sm);
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