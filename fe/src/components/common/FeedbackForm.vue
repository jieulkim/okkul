<template>
  <div class="feedback-form-container card">
    <h3 class="feedback-title">서비스에 대한 의견을 자유롭게 남겨주세요</h3>
    <p class="feedback-description">
      저희 서비스를 개선하는 데 귀하의 소중한 의견이 큰 도움이 됩니다. 감사합니다!
    </p>
    
    <form @submit.prevent="submitFeedback" class="feedback-form">
      <div class="rating-section">
        <h4 class="rating-title">서비스 만족도</h4>
        <div class="stars-container" @mouseleave="resetHover">
          <span
            v-for="star in 5"
            :key="star"
            class="material-icons star-icon"
            @mouseover="hoverStar(star)"
            @click="setRating(star)"
          >
            {{ (hoverRating || rating) >= star ? 'star' : 'star_border' }}
          </span>
        </div>
      </div>

      <textarea
        v-model="feedbackText"
        class="textarea"
        rows="5"
        placeholder="여기에 의견을 입력해주세요..."
        aria-label="서비스 피드백 입력"
        required
        :disabled="isSubmitting"
      ></textarea>
      <button type="submit" class="btn btn-primary feedback-submit-btn" :disabled="isSubmitting">
        {{ isSubmitting ? '보내는 중...' : '의견 보내기' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { db } from '@/firebaseConfig'; // Firebase 설정 파일에서 db 인스턴스 가져오기
import { collection, addDoc } from 'firebase/firestore';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router'; // useRouter 임포트

const feedbackText = ref('');
const rating = ref(0);
const hoverRating = ref(0);
const isSubmitting = ref(false);
const authStore = useAuthStore();
const router = useRouter(); // useRouter 인스턴스 생성

const setRating = (star) => {
  rating.value = star;
};

const hoverStar = (star) => {
  hoverRating.value = star;
};

const resetHover = () => {
  hoverRating.value = 0;
};

const submitFeedback = async () => {
  if (rating.value === 0) {
    alert('서비스 만족도 별점을 선택해주세요.');
    return;
  }
  if (feedbackText.value.trim() === '') {
    alert('의견을 입력해주세요.');
    return;
  }

  isSubmitting.value = true;

  try {
    const user = authStore.user;
    const currentPath = router.currentRoute.value.fullPath; // 현재 페이지 경로 가져오기

    const docRef = await addDoc(collection(db, "feedback"), {
      message: feedbackText.value.trim(),
      rating: rating.value, // 별점 추가
      userId: user ? user.id : 'anonymous',
      userEmail: user ? user.email : 'anonymous',
      timestamp: new Date(),
      environment: import.meta.env.MODE,
      pagePath: currentPath // 현재 페이지 경로 추가
    });
    console.log("Feedback document written with ID: ", docRef.id);
    
    feedbackText.value = ''; // 입력 필드 초기화
    rating.value = 0; // 별점 초기화
    alert('소중한 의견 감사합니다!');
  } catch (e) {
    console.error("Error adding document: ", e);
    alert('의견을 제출하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.feedback-form-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  text-align: center;
}

.feedback-title {
  font-size: 1.8rem;
  color: var(--text-main);
  margin-bottom: 5px;
}

.feedback-description {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 15px;
}

.feedback-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rating-section {
  margin-bottom: 10px;
  padding: 10px;
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
}

.rating-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.stars-container {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.star-icon {
  font-size: 2.5rem;
  color: var(--honey-400);
  cursor: pointer;
  transition: transform 0.1s ease-in-out;
}

.star-icon:hover {
  transform: scale(1.2);
}

.textarea {
  resize: vertical;
  min-height: 120px;
  font-size: 1rem;
  line-height: 1.6;
  color: var(--text-main);
  background: var(--bg-subtle);
  border-color: var(--card-border);
  transition: background-color 0.2s, border-color 0.2s;
}

.textarea:focus {
  background: var(--card-bg);
}

.textarea:disabled {
  background-color: #f0f0f0;
  cursor: not-allowed;
}

.feedback-submit-btn {
  align-self: flex-end;
  min-width: 150px;
  padding: 12px 30px;
}

.feedback-submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* Dark mode adjustments */
.dark-mode .feedback-form-container {
  border-color: var(--card-border);
}

.dark-mode .rating-section {
  background: #2C2825; /* Use a dark subtle background */
}

.dark-mode .textarea {
  background: var(--bg-subtle);
  border-color: var(--card-border);
}
.dark-mode .textarea:focus {
  background: var(--card-bg);
}

.dark-mode .textarea:disabled {
  background-color: #333;
}
</style>
