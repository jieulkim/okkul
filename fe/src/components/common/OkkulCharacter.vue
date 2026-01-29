<template>
  <div class="okkul-character" :class="{ mini: size === 'mini', waving: wave, static: !animated }">
    <div class="platypus-body">
      <div class="platypus-hat"></div>
      <div class="platypus-eye left"></div>
      <div class="platypus-eye right"></div>
      <div class="platypus-bill"></div>
      <div class="platypus-arm-left"></div>
      <div class="platypus-arm-right" :class="{ wave: wave }"></div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  size: {
    type: String,
    default: 'normal' // 'mini', 'normal', 'large'
  },
  wave: {
    type: Boolean,
    default: false
  },
  animated: {
    type: Boolean,
    default: true // false로 설정하면 float 애니메이션 제거
  }
})
</script>

<style scoped>
.okkul-character {
  width: 120px;
  height: 120px;
  position: relative;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
  flex-shrink: 0; /* 찌그러짐 방지 */
}

/* 애니메이션 있는 경우만 float 적용 */
.okkul-character:not(.static) {
  animation: float 3s infinite ease-in-out;
}

.okkul-character.mini {
  width: 70px;
  height: 70px;
}

/* 몸체 */
.platypus-body {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #d4a574 0%, #c59358 100%);
  border: 3px solid #2d2d2d;
  border-radius: 50%;
  box-shadow: inset -5px -5px 10px rgba(0, 0, 0, 0.1);
}

.okkul-character.mini .platypus-body {
  border-width: 2.5px;
}

/* 모자 */
.platypus-hat {
  position: absolute;
  top: -15%;
  left: 50%;
  transform: translateX(-50%);
  width: 35%;
  height: 15%;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border: 2.5px solid #2d2d2d;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.okkul-character.mini .platypus-hat {
  border-width: 2px;
  top: -12px;
  width: 32px;
  height: 14px;
}

/* 모자 리본 */
.platypus-hat::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 25%;
  height: 25%;
  background: #fff;
  border: 2px solid #2d2d2d;
  border-radius: 50%;
}

.okkul-character.mini .platypus-hat::after {
  width: 8px;
  height: 8px;
}

/* 눈 */
.platypus-eye {
  position: absolute;
  top: 30%;
  width: 10%;
  height: 10%;
  background: #000;
  border-radius: 50%;
  box-shadow: 0 0 0 2px #fff;
}

.okkul-character.mini .platypus-eye {
  top: 24px;
  width: 8px;
  height: 8px;
}

.platypus-eye.left {
  left: 22%;
  animation: blink 4s infinite;
}

.okkul-character.mini .platypus-eye.left {
  left: 17px;
}

.platypus-eye.right {
  right: 22%;
  animation: blink 4s 0.1s infinite;
}

.okkul-character.mini .platypus-eye.right {
  right: 17px;
}

/* 눈 하이라이트 */
.platypus-eye::after {
  content: '';
  position: absolute;
  top: 15%;
  right: 15%;
  width: 35%;
  height: 35%;
  background: #fff;
  border-radius: 50%;
}

.okkul-character.mini .platypus-eye::after {
  width: 3px;
  height: 3px;
}

/* 눈 깜빡임 */
@keyframes blink {
  0%, 96%, 100% {
    transform: scaleY(1);
  }
  98% {
    transform: scaleY(0.1);
  }
}

/* 볼터치 */
.platypus-body::before {
  content: '';
  position: absolute;
  left: 10%;
  top: 40%;
  width: 15%;
  height: 10%;
  background: rgba(255, 150, 150, 0.4);
  border-radius: 50%;
  filter: blur(2px);
}

.platypus-body::after {
  content: '';
  position: absolute;
  right: 10%;
  top: 40%;
  width: 15%;
  height: 10%;
  background: rgba(255, 150, 150, 0.4);
  border-radius: 50%;
  filter: blur(2px);
}

/* 부리 */
.platypus-bill {
  position: absolute;
  top: 45%;
  left: 50%;
  transform: translateX(-50%);
  width: 42%;
  height: 16%;
  background: linear-gradient(135deg, #444 0%, #2d2d2d 100%);
  border: 2.5px solid #2d2d2d;
  border-radius: 14px;
  box-shadow: inset 0 -2px 4px rgba(0, 0, 0, 0.3);
}

.okkul-character.mini .platypus-bill {
  top: 36px;
  width: 36px;
  height: 14px;
}

/* 부리 미소 */
.platypus-bill::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 45%;
  height: 40%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: none;
  border-radius: 0 0 8px 8px;
}

/* 왼팔 */
.platypus-arm-left {
  position: absolute;
  left: -15%;
  top: 40%;
  width: 23%;
  height: 12%;
  background: linear-gradient(90deg, #d4a574 0%, #c59358 100%);
  border: 2.5px solid #2d2d2d;
  border-radius: 10px;
  transform-origin: right center;
  transform: rotate(-20deg);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.okkul-character.mini .platypus-arm-left {
  left: -18px;
  top: 32px;
  width: 20px;
  height: 9px;
}

/* 오른팔 */
.platypus-arm-right {
  position: absolute;
  right: -18%;
  top: 38%;
  width: 25%;
  height: 13%;
  background: linear-gradient(90deg, #c59358 0%, #d4a574 100%);
  border: 2.5px solid #2d2d2d;
  border-radius: 10px;
  transform-origin: left center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.okkul-character.mini .platypus-arm-right {
  right: -22px;
  top: 30px;
  width: 22px;
  height: 10px;
}

/* 흔드는 애니메이션 */
.platypus-arm-right.wave {
  animation: wave-motion 1s infinite ease-in-out;
}

@keyframes wave-motion {
  0%, 100% {
    transform: rotate(10deg);
  }
  50% {
    transform: rotate(-45deg);
  }
}

/* 떠있는 애니메이션 */
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-6px) rotate(-2deg);
  }
  50% {
    transform: translateY(-10px) rotate(0deg);
  }
  75% {
    transform: translateY(-6px) rotate(2deg);
  }
}

/* 호버 효과 - 애니메이션 있을 때만 */
.okkul-character:not(.static):hover {
  animation: bounce 0.6s ease;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-15px) scale(1.05);
  }
}
</style>