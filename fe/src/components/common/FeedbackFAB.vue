<template>
  <button class="feedback-fab" @click="$emit('toggle')" aria-label="피드백 보내기">
    <img src="@/assets/images/okkul.png" alt="오꿀쌤" class="fab-icon" />
  </button>
</template>

<script setup>
defineEmits(['toggle']);
</script>

<style scoped>
.feedback-fab {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 70px;
  height: 70px;
  background: var(--primary-gradient);
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  transition: all 0.3s var(--ease-smooth);
  z-index: 999;
  animation: pulse 2.5s infinite;
}

.feedback-fab:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
  animation-play-state: paused;
}

.fab-icon {
  width: 90%;
  height: 90%;
  object-fit: contain;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(253, 216, 53, 0.7);
  }
  70% {
    transform: scale(1);
    box-shadow: 0 0 0 15px rgba(253, 216, 53, 0);
  }
  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(253, 216, 53, 0);
  }
}

/* 다크모드에서는 그림자를 좀 더 부드럽게 */
.dark-mode .feedback-fab {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.4);
}
.dark-mode .feedback-fab:hover {
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.5);
}
</style>
