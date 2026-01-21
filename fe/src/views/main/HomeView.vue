<script setup>
const userName = "OOO"
</script>

<template>
  <div class="home-container">
    <header class="main-header">
      <div class="header-content">
        <div class="logo">🍯 오픽꿀잼</div>
        <nav class="main-nav">
          <a href="#" class="active">실전 모의고사</a>
          <router-link to="/practice">유형별 연습</router-link>
          <a href="#">피드백 리포트</a>
        </nav>
        <div class="user-profile">
          <div class="avatar">OO</div>
          <span>{{ userName }} 님</span>
        </div>
      </div>
    </header>

    <main class="main-content">
      <div class="dashboard-grid">
        <div class="left-column">
          <section class="welcome-banner">
            <h1>오늘도 <span class="highlight">꿀</span> 떨어지게 연습해볼까요? 🐝</h1>
            <p>목표 등급 AL 달성까지 단 2단계 남았습니다.</p>
            <div class="action-buttons">
              <button class="btn-primary">
                <span class="material-icons-outlined">play_circle_filled</span>
                <div class="btn-text">
                  <span class="title">실전 모의고사 시작</span>
                  <span class="sub">15문항, 약 40분 소요</span>
                </div>
              </button>
              <router-link to="/practice" class="btn-secondary">
                <span class="material-icons-outlined">category</span>
                <div class="btn-text">
                  <span class="title">유형별 집중 연습</span>
                  <span class="sub">약점 보완 테마별 학습</span>
                </div>
              </router-link>
            </div>
          </section>

          <section class="stats-card">
            <div class="card-header">
              <h3>최근 성적 추이</h3>
              <span>최근 7일 기준</span>
            </div>
            <div class="chart-area">
              <div class="bar" style="height: 40%"><div class="fill"></div></div>
              <div class="bar" style="height: 55%"><div class="fill"></div></div>
              <div class="bar" style="height: 45%"><div class="fill"></div></div>
              <div class="bar" style="height: 70%"><div class="fill"></div></div>
              <div class="bar" style="height: 85%"><div class="fill active"></div></div>
            </div>
            <div class="chart-labels">
              <span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>오늘</span>
            </div>
          </section>
        </div>

        <div class="right-column">
          <section class="ai-analysis-card">
            <h3>AI 등급 분석</h3>
            <div class="grade-circle">
              <svg viewBox="0 0 100 100">
                <circle class="bg" cx="50" cy="50" r="45"></circle>
                <circle class="fg" cx="50" cy="50" r="45" style="stroke-dashoffset: 70"></circle>
              </svg>
              <div class="grade-text">
                <span class="label">AI Predicted</span>
                <span class="grade">IH</span>
                <span class="percent">상위 15%</span>
              </div>
            </div>
            <div class="grade-stats">
              <div class="stat">
                <p>현재 예상</p>
                <strong>IH</strong>
              </div>
              <div class="divider"></div>
              <div class="stat">
                <p>목표 등급</p>
                <strong class="target">AL</strong>
              </div>
            </div>
            <button class="detail-btn">상세 리포트 보기</button>
          </section>

          <section class="premium-card">
            <div class="premium-header">
              <span class="material-icons-outlined">workspace_premium</span>
              <h4>프리미엄 멤버십</h4>
            </div>
            <p>무제한 AI 피드백과 실전 모의고사 20회분이 남았습니다.</p>
            <button class="upgrade-btn">이용권 연장하기</button>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.home-container { min-height: 100vh; background-color: #f8f9fa; }

/* 헤더 스타일 */
.main-header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo { font-size: 24px; font-weight: 900; color: #FFD600; }
.main-nav { display: flex; gap: 30px; }
.main-nav a { 
  text-decoration: none; 
  color: #64748b; 
  font-weight: 500;
  padding-bottom: 5px;
}
.main-nav a.active { color: #FFD600; border-bottom: 2px solid #FFD600; font-weight: 700; }

/* 레이아웃 */
.main-content { max-width: 1200px; margin: 0 auto; padding: 30px 20px; }
.dashboard-grid { display: grid; grid-template-cols: 2fr 1fr; gap: 24px; }

/* 카드 공통 스타일 */
section { background: white; border-radius: 1rem; border: 1px solid #f1f5f9; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }

/* 배너 스타일 */
.welcome-banner h1 { font-size: 28px; font-weight: 900; margin-bottom: 8px; }
.highlight { color: #FFD600; }
.action-buttons { display: grid; grid-template-cols: 1fr 1fr; gap: 16px; margin-top: 30px; }

.btn-primary, .btn-secondary {
  display: flex; align-items: center; justify-content: center; gap: 12px;
  padding: 20px; border-radius: 1rem; cursor: pointer; border: none; transition: 0.2s;
  text-decoration: none; color: inherit;
}
.btn-primary { background: #FFD600; }
.btn-secondary { background: white; border: 2px solid #FFD600; }

.btn-text { text-align: left; display: flex; flex-direction: column; }
.btn-text .title { font-weight: bold; font-size: 16px; }
.btn-text .sub { font-size: 12px; opacity: 0.7; }

/* 차트 스타일 */
.chart-area { height: 180px; display: flex; align-items: flex-end; justify-content: space-between; padding: 0 20px; }
.bar { width: 40px; background: #f1f5f9; border-radius: 8px 8px 0 0; position: relative; }
.fill { position: absolute; bottom: 0; width: 100%; height: 100%; background: #FFD600; opacity: 0.3; border-radius: inherit; }
.fill.active { opacity: 1; }
.chart-labels { display: flex; justify-content: space-between; margin-top: 10px; color: #94a3b8; font-size: 12px; }

/* AI 분석 원형 그래프 */
.grade-circle { position: relative; width: 180px; height: 180px; margin: 20px auto; }
.grade-circle svg { transform: rotate(-90deg); }
.grade-circle circle { fill: none; stroke-width: 8; stroke-linecap: round; }
.grade-circle .bg { stroke: #f1f5f9; }
.grade-circle .fg { stroke: #FFD600; stroke-dasharray: 283; }

.grade-text { 
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  display: flex; flex-direction: column; align-items: center;
}
.grade { font-size: 40px; font-weight: 900; }

/* 사이드바 프리미엄 */
.premium-card { background: #FFD600; }
.upgrade-btn { width: 100%; padding: 10px; background: white; border: none; border-radius: 8px; font-weight: bold; margin-top: 10px; }
</style>