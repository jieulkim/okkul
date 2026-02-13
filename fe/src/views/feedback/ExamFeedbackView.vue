<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { examApi, historyApi } from '@/api';
import okkulSvg from '@/assets/images/okkul.svg';

const router = useRouter();
const route = useRoute();

const examResult = ref(null);
const isLoading = ref(true);
let pollingInterval = null; // pollingInterval ref 추가

// 피드백 완료 여부를 폴링하는 함수
const pollForFeedbackCompletion = async (examId) => {
  try {
    const response = await examApi.getExamInfo(examId);
    if (response.data.examStatus?.code === 'COMPLETED') {
      console.log('Feedback COMPLETED, stopping polling.');
      if (pollingInterval) clearInterval(pollingInterval);
      await loadExamResult(); // 피드백 완료 후 결과 로드
    } else {
      console.log('Feedback not yet COMPLETED, polling again in 3 seconds...');
    }
  } catch (error) {
    console.error('Polling failed:', error);
    if (pollingInterval) clearInterval(pollingInterval);
    // 에러 처리, 예를 들어 사용자에게 알림 또는 다른 페이지로 리디렉션
    alert('피드백 상태를 확인하는 중 오류가 발생했습니다.');
    router.push('/exam'); // 에러 발생 시 시험 목록으로 이동
  }
};

// 시험 결과 로드 (API 연동 버전)
const loadExamResult = async () => {
  try {
    const examId = parseInt(route.query.examId);
    
    // 1. 시험 히스토리(리포트) 조회
    const historyResponse = await historyApi.getExamHistoryDetail(examId);
    const report = historyResponse.data.examReport;

    // 2. 시험 문제 목록 조회 (질문 텍스트 등)
    const infoResponse = await examApi.getExamInfo(examId);
    console.log("Info Response questions:", infoResponse.data.questions);

    // 2.5. 각 문항별 상세 점수 (문법, 어휘, 논리) 조회
    const questionDetailsPromises = infoResponse.data.questions?.map(async q => {
      try {
        const detailResponse = await historyApi.getExamAnswerDetail(examId, q.order);
        const latestFeedback = detailResponse.data.sentenceFeedbacks?.[0]; // 첫 번째 세부 피드백에서 점수 추출
        return {
          questionOrder: q.order,
          questionText: q.questionText,
          sttScript: detailResponse.data.sttScript || '',
          enhancedScript: detailResponse.data.improvedAnswer || '',
          grammar: { score: latestFeedback?.grammarScore !== undefined ? latestFeedback.grammarScore : 0 },
          vocabulary: { score: latestFeedback?.vocabScore !== undefined ? latestFeedback.vocabScore : 0 },
          logic: { score: latestFeedback?.logicScore !== undefined ? latestFeedback.logicScore : 0 },
        };
      } catch (detailError) {
        console.warn(`Failed to fetch detail for question ${q.order}:`, detailError);
        return {
          questionOrder: q.order,
          questionText: q.questionText,
          sttScript: '',
          enhancedScript: '',
          grammar: { score: 0 },
          vocabulary: { score: 0 },
          logic: { score: 0 },
        };
      }
    }) || [];

    const questionResultsWithScores = await Promise.all(questionDetailsPromises);

    // 3. 데이터 매핑
    const difficulty = infoResponse.data.initialDifficulty || infoResponse.data.adjustedDifficulty || 1;
    const date = new Date(historyResponse.data.createdAt).toLocaleDateString('ko-KR', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit' 
    }).replace(/\. /g, '.').replace(/\.$/, '');
    
    examResult.value = {
      difficulty,
      formattedDate: date,
      createdAt: historyResponse.data.createdAt || new Date().toISOString(),
      summary: {
        grade: report?.grade || '정보없음',
        totalScore: report?.totalScore || 0,
        categoryScores: {
          grammar: report?.avgGrammar || 0,
          vocabulary: report?.avgVocab || 0,
          logic: report?.avgLogic || 0,
          fluency: report?.avgFluency || 0,
          relevance: report?.avgRelevance || 0
        },
        comment: report?.comment || '총평이 없습니다.',
        strengths: report?.strengthTypes?.join(', ') || '없음',
        weakness: report?.weaknessTypes?.join(', ') || '없음'
      },
      // 질문 목록은 infoResponse에서 가져옴. 
      questionResults: questionResultsWithScores
    };
    
  } catch (error) {
    console.error('시험 결과 로드 실패:', error);
    alert('시험 결과를 불러오는데 실패했습니다.');
    router.push('/exam');
  } finally {
    isLoading.value = false;
  }
};

// 오각형 차트 데이터
const parseFeedbackList = (feedbackString) => {
  if (!feedbackString || feedbackString === '없음') {
    return [];
  }
  // 대괄호 제거
  let cleanedString = feedbackString.startsWith('[') && feedbackString.endsWith(']')
    ? feedbackString.slice(1, -1)
    : feedbackString;

  // 온점과 쉼표가 같이 나올 경우를 기준으로 분리 (. ,)
  // 정규식: 마침표, 선택적 공백, 쉼표, 선택적 공백
  return cleanedString.split(/\.\s*,\s*/).map(item => {
    item = item.trim();
    if (item.length > 0 && !item.endsWith('.')) {
      item += '.'; // 마침표로 끝나지 않으면 추가
    }
    return item;
  }).filter(item => item.length > 0);
};

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

const radarPoints = computed(() => {
  const centerX = 240; // Shifted center X for larger space
  const centerY = 160;
  const maxRadius = 135; // Increased radius
  
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
  const centerX = 240;
  const centerY = 160;
  const labelRadius = 162; // 라벨을 바깥으로 더 밀어냄
  const angle = (Math.PI * 2 * index) / 5 - Math.PI / 2;
  
  const x = centerX + labelRadius * Math.cos(angle);
  const y = centerY + labelRadius * Math.sin(angle);
  
  let anchor = 'middle';
  if (x < centerX - 40) anchor = 'end';
  else if (x > centerX + 40) anchor = 'start';
  
  return { x, y, anchor };
};

// 문항 클릭
const selectQuestion = (index) => {
  const question = examResult.value.questionResults[index];
  if (!question) return;

  router.push({
    path: '/exam/feedback/detail',
    query: {
      examId: route.query.examId,
      questionOrder: question.questionOrder,
      num: route.query.num
    }
  });
};

// 선택된 문항 관련 로직 제거 (상세 페이지로 이동하므로)

const handlePopState = () => {
  window.history.pushState(null, '', window.location.href);
};

onMounted(() => {
  const rawExamId = route.query.examId;
  const examId = parseInt(rawExamId);
  
  if (!rawExamId || isNaN(examId)) {
    console.error('[ExamFeedbackView] 유효하지 않은 시험 ID:', rawExamId);
    alert('유효하지 않은 시험 정보입니다.');
    router.push('/exam');
    return;
  }

  // 브라우저 뒤로가기 처리
  window.history.pushState(null, '', window.location.href);
  window.addEventListener('popstate', handlePopState);

  // 로딩 상태 시작
  isLoading.value = true;
  // 초기 즉시 실행 후 폴링 시작
  pollForFeedbackCompletion(examId); // 첫 실행
  pollingInterval = setInterval(() => pollForFeedbackCompletion(examId), 3000); // 3초마다 폴링
});

// 컴포넌트 언마운트 시 인터벌 정리
onBeforeUnmount(() => {
  window.removeEventListener('popstate', handlePopState);
  if (pollingInterval) {
    clearInterval(pollingInterval);
  }
});
</script>

<template>
  <div class="exam-result-page">
    <!-- 로딩 -->
    <div v-if="isLoading" class="loading-screen">
      <div class="loader-container">
        <div class="okkul-loading-wrapper">
          <img :src="okkulSvg" alt="오꿀쌤 로딩" class="loading-okkul" />
          <div class="glow-ring"></div>
        </div>
        <div class="loading-text-group">
          <p class="loading-title">결과를 불러오는 중...</p>
          <div class="loading-dots">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 결과 화면 -->
    <template v-else-if="examResult">
      <div class="result-container fade-in">
        <!-- 헤더 -->
        <div class="feedback-header">
          <button @click="router.push('/feedback?category=EXAM')" class="back-btn">
            <span class="material-icons">arrow_back</span>
            목록으로
          </button>
          <div class="header-text-group">
            <h1 class="feedback-title">난이도 {{ examResult.difficulty }} 모의고사 ({{ examResult.formattedDate }})</h1>
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
            
            <h3 style="margin-top: 40px;">총평</h3>
            <p class="comment-text">{{ examResult.summary.comment }}</p>
            
            <div class="strengths-weakness">
              <div class="strength-box">
                <h4>강점</h4>
                <ul v-if="parseFeedbackList(examResult.summary.strengths).length > 0">
                  <li v-for="(item, i) in parseFeedbackList(examResult.summary.strengths)" :key="`strength-${i}`">
                    {{ item }}
                  </li>
                </ul>
                <p v-else>없음</p>
              </div>
              <div class="weakness-box">
                <h4>개선 필요</h4>
                <ul v-if="parseFeedbackList(examResult.summary.weakness).length > 0">
                  <li v-for="(item, i) in parseFeedbackList(examResult.summary.weakness)" :key="`weakness-${i}`">
                    {{ item }}
                  </li>
                </ul>
                <p v-else>없음</p>
              </div>
            </div>
          </div>

          <div class="radar-chart-card">
            <h3>영역별 분석</h3>
            <svg viewBox="0 0 480 340" class="radar-chart">
              <!-- 배경 그리드 -->
              <g class="grid">
                <polygon 
                  v-for="i in 5" 
                  :key="`grid-${i}`"
                  :points="radarData.map((_, idx) => {
                    const angle = (Math.PI * 2 * idx) / 5 - Math.PI / 2;
                    const radius = (i * 135) / 5;
                    return `${240 + radius * Math.cos(angle)},${160 + radius * Math.sin(angle)}`;
                  }).join(' ')"
                  fill="none"
                  stroke="#e2e8f0"
                  stroke-width="1"
                />
              </g>
              
              <!-- 축선 -->
              <g class="axes">
                <line 
                  v-for="(item, index) in radarData"
                  :key="`axis-${index}`"
                  x1="240" y1="160"
                  :x2="240 + 135 * Math.cos((Math.PI * 2 * index) / 5 - Math.PI / 2)"
                  :y2="160 + 135 * Math.sin((Math.PI * 2 * index) / 5 - Math.PI / 2)"
                  stroke="#cbd5e1"
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
                  :text-anchor="getLabelPosition(index).anchor"
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
        </div>

        <!-- 문항별 세부 피드백 -->
        <div class="questions-section">
          <h2 class="section-title">문항별 세부 피드백</h2>
          
          <div v-if="examResult.questionResults.length > 0" class="questions-grid">
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
                  <span class="mini-score">문법 {{ question.grammar?.score }}</span>
                  <span class="mini-score">어휘 {{ question.vocabulary?.score }}</span>
                  <span class="mini-score">논리 {{ question.logic?.score }}</span>
                </div>
              </div>
              <span class="material-icons">chevron_right</span>
            </div>
          </div>
          <div v-else class="empty-state">
            <span class="material-icons">feedback</span>
            <p>문항별 피드백 정보가 없습니다.</p>
          </div>
        </div>
      </div>

    </template>
  </div>
</template>

<style scoped>
.exam-result-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 40px 100px;
}

/* 로딩 */
.loading-screen {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  background: #FDFBF5;
}

.loader-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
}

.okkul-loading-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-okkul {
  width: 80px;
  height: 80px;
  z-index: 2;
  animation: okkulFloat 2s ease-in-out infinite;
}

.glow-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-top-color: #FFD700;
  border-bottom-color: #FFD700;
  border-radius: 50%;
  animation: spinRing 2s linear infinite;
  z-index: 1;
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.2);
}

.loading-text-group {
  text-align: center;
}

.loading-title {
  font-size: 1.25rem;
  color: var(--text-primary);
  font-weight: 800;
  margin-bottom: 12px;
  letter-spacing: -0.01em;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: #FFD700;
  border-radius: 50%;
  animation: bounceDot 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes spinRing {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes okkulFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes bounceDot {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1.0); }
}

/* 결과 컨테이너 */
.result-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

.fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 헤더 */
.feedback-header {
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-text-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
}

.back-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  transform: translateY(-2px);
}

.feedback-title {
  font-size: 2rem;
  font-weight: 900;
  color: var(--text-primary);
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
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 40px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
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
  max-width: 400px;
  margin: 0 auto 60px;
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
.questions-section {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 40px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

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

.result-header {
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 40px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
}

.back-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  transform: translateY(-2px);
}

.question-card {
  display: flex;
  align-items: center;
  gap: 24px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  padding: 24px 32px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.question-card:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: var(--primary-color);
  transform: translateX(8px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
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
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  max-width: 900px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.2);
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
  color: #E65100;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 900;
  border: 1px solid #E65100;
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

/* 빈 상태 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  background: var(--bg-secondary);
  border-radius: 20px;
  border: 1px dashed var(--border-primary);
  color: var(--text-tertiary);
  gap: 16px;
}

.empty-state .material-icons {
  font-size: 48px;
}

.empty-state p {
  font-size: 1.1rem;
  font-weight: 600;
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