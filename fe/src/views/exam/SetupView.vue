<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isPlaying = ref(false)
const isRecording = ref(false)
const hasRecording = ref(false)

const audio = new Audio('https://opickoreademo.multicampus.com/Audio/EN/0.mp3')

const togglePlay = () => {
  if (isPlaying.value) {
    audio.pause()
    audio.currentTime = 0
  } else {
    audio.play()
    audio.onended = () => isPlaying.value = false
  }
  isPlaying.value = !isPlaying.value
}

const startRec = () => {
  isRecording.value = true
  setTimeout(() => {
    isRecording.value = false
    hasRecording.value = true
  }, 3000) // 가상 녹음 3초
}

onUnmounted(() => audio.pause())
</script>

<template>
  <div class="assessment-page">
    <header class="assessment-header">
      <nav class="step-progress">
        <div class="step completed">Step 1</div>
        <div class="step completed">Step 2</div>
        <div class="step active">Step 3 (Setup)</div>
        <div class="step">Step 4</div>
        <div class="step last">Step 5</div>
      </nav>
      <h1 class="page-title">Device Setup</h1>
    </header>

    <main class="assessment-main">
      <div class="setup-grid">
        <div class="character-card">
          <div class="okkul-mini-container">
            <div class="platypus-body">
              <div class="platypus-hat"></div>
              <div class="platypus-eye left"></div>
              <div class="platypus-eye right"></div>
              <div class="platypus-bill"></div>
              <div class="platypus-arm-right" :class="{ 'wave': isPlaying }"></div>
            </div>
          </div>
          <button @click="togglePlay" class="play-btn" :class="{ active: isPlaying }">
            <span class="material-icons">{{ isPlaying ? 'stop' : 'play_arrow' }}</span>
            {{ isPlaying ? 'Stop' : 'Test Audio' }}
          </button>
        </div>

        <div class="control-card">
          <ul class="steps">
            <li>1. 소리가 잘 들리는지 버튼을 눌러 확인하세요.</li>
            <li>2. 마이크 녹음 버튼을 눌러 목소리를 점검하세요.</li>
          </ul>
          <div class="btns">
            <button @click="startRec" class="rec-btn" :disabled="isRecording">
              {{ isRecording ? 'Recording...' : 'Start Recording' }}
            </button>
            <button v-if="hasRecording" class="check-btn">Play Recorded Voice</button>
          </div>
        </div>
      </div>
    </main>

    <footer class="assessment-footer">
      <button @click="router.back()" class="nav-btn back-btn">Back</button>
      <button @click="router.push('/exam/question')" class="nav-btn next-btn" :disabled="!hasRecording">Next</button>
    </footer>
  </div>
</template>

<style scoped>
.assessment-page { min-height: 100vh; padding-bottom: 100px; }
.assessment-header { max-width: 1280px; margin: 0 auto; width: 100%; padding: 32px 16px; }
.step-progress { display: flex; height: 50px; margin-bottom: 30px; }
.step { flex: 1; display: flex; align-items: center; justify-content: center; background: #f8f9fa; color: #94a3b8; font-size: 12px; clip-path: polygon(0% 0%, 90% 0%, 100% 50%, 90% 100%, 0% 100%, 10% 50%); }
.step.active { background: #FFD700; color: #1e293b; font-weight: bold; }
.step.completed { background: #e2e8f0; }

.setup-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; max-width: 1000px; margin: 0 auto; }
.character-card { background: #f8f9fa; border-radius: 20px; padding: 60px; text-align: center; }
.play-btn { margin-top: 30px; background: #FFD700; border: none; padding: 12px 24px; border-radius: 30px; cursor: pointer; display: flex; align-items: center; gap: 8px; margin-inline: auto; }

/* 오꿀이 스타일 */
.okkul-mini-container { width: 65px; height: 65px; position: relative; animation: float 3s infinite ease-in-out; margin: 0 auto; }
.platypus-body { position: relative; width: 65px; height: 65px; background: #C59358; border: 3px solid #000; border-radius: 50%; }
.platypus-hat { position: absolute; top: -10px; left: 50%; transform: translateX(-50%); width: 28px; height: 12px; background: #333; border: 2.5px solid #000; border-radius: 4px; }
.platypus-eye { position: absolute; top: 26px; width: 6px; height: 6px; background: #000; border-radius: 50%; }
.platypus-eye.left { left: 18px; }
.platypus-eye.right { right: 18px; }
.platypus-bill { position: absolute; top: 34px; left: 50%; transform: translateX(-50%); width: 34px; height: 12px; background: #333; border: 2.5px solid #000; border-radius: 12px; }
.platypus-arm-right { position: absolute; right: -20px; top: 32px; width: 20px; height: 9px; background: #C59358; border: 2.5px solid #000; border-radius: 10px; transform-origin: left center; }
.wave { animation: wave-motion 0.8s infinite alternate ease-in-out; }
@keyframes wave-motion { from { transform: rotate(10deg); } to { transform: rotate(-50deg); } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }

.assessment-footer { position: fixed; bottom: 0; left: 0; right: 0; background: white; border-top: 1px solid #eee; padding: 20px 40px; display: flex; justify-content: space-between; }
.nav-btn { padding: 10px 30px; border-radius: 10px; border: none; font-weight: bold; cursor: pointer; }
.next-btn { background: #FFD700; }
</style>