<script setup>
import { ref, computed } from 'vue'

const currentIdx = ref(0);
const showRelevelModal = ref(false);

const goNext = () => {
  if (currentIdx.value === 6) { // 7번째 문제(index 6) 종료 시
    showRelevelModal.value = true;
  } else {
    currentIdx.value++;
    // 다음 문제 로드 로직
  }
};

const setRelevel = (choice) => {
  console.log(`난이도 선택: ${choice}`);
  showRelevelModal.value = false;
  currentIdx.value++; // 8번 문제로 진행
};
</script>

<template>
  <div class="exam-page" :class="{ 'dark-mode': isDarkMode }">
    <div class="exam-header">
      <div class="status-left">
        <span class="question-number">Question {{ currentIdx + 1 }} of 15</span>
      </div>
      <div class="time-limit">
        <span class="timer">Time Left: {{ formattedTime }}</span>
      </div>
    </div>

    <div class="main-layout">
      <div class="character-section">
        <div class="character-box">
          <div class="platypus-character">
             </div>
          <button @click="togglePlay" class="play-btn">
            {{ isPlaying ? '■ Stop' : '▶ Listen' }}
          </button>
        </div>
      </div>

      <div class="control-section">
        <div class="recording-status">
          <div v-if="isRecording" class="rec-indicator">● RECORDING</div>
          <div v-else class="ready-indicator">READY</div>
        </div>
        
        <div class="volume-meter">
          <div class="bar" v-for="n in 10" :key="n" :class="{ active: volumeLevel >= n }"></div>
        </div>

        <div class="nav-buttons">
          <button @click="goNext" class="next-btn">NEXT</button>
        </div>
      </div>
    </div>

    <div v-if="showRelevelModal" class="modal-overlay relevel-modal">
      <div class="modal-card">
        <h3>난이도 재조정</h3>
        <p>전반부 문제를 풀어보셨습니다.<br>남은 문제의 난이도를 어떻게 하시겠습니까?</p>
        <div class="relevel-options">
          <button @click="setRelevel('easy')">쉬운 질문</button>
          <button @click="setRelevel('same')" class="active">비슷한 질문</button>
          <button @click="setRelevel('hard')">어려운 질문</button>
        </div>
      </div>
    </div>
  </div>
</template>

