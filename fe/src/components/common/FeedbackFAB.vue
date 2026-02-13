<template>
  <div class="fab-wrapper">
    <div class="speech-bubble" @click="$emit('toggle')">
      의견 보내기
    </div>
    <button class="feedback-fab" @click="$emit('toggle')" aria-label="피드백 보내기">
      <img src="@/assets/images/default-profile.png" alt="오꿀" class="fab-icon" />
    </button>
  </div>
</template>

<script setup>
defineEmits(['toggle']);
</script>

<style scoped>
.fab-wrapper {
  position: fixed;
  bottom: 0;
  right: 0;
  z-index: 999;
  /* Pointer events none wrapper so it doesn't block clicks in the corner area generally, 
     but children will have pointer-events auto */
  pointer-events: none;
  width: 200px; /* Area for bubble and button */
  height: 150px;
}

.feedback-fab {
  position: absolute;
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
  animation: pulse 2.5s infinite;
  pointer-events: auto; /* Enable clicks */
}

.speech-bubble {
  position: absolute;
  bottom: 110px; /* Positioned above the button */
  right: 25px;
  background: white;
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  font-weight: 700;
  color: var(--text-primary);
  font-size: 0.9rem;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.3s;
  border: 1px solid var(--primary-color);
  animation: float 3s ease-in-out infinite;
  white-space: nowrap;
}

/* Arrow for speech bubble */
.speech-bubble::after {
  content: '';
  position: absolute;
  bottom: -6px;
  right: 28px;
  width: 12px;
  height: 12px;
  background: white;
  border-bottom: 1px solid var(--primary-color);
  border-right: 1px solid var(--primary-color);
  transform: rotate(45deg);
}

.fab-wrapper:hover .speech-bubble {
  transform: translateY(-5px);
}

.feedback-fab:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
  animation-play-state: paused;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
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
</style>