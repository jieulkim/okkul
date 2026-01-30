package site.okkul.be.domain.practice.entity;

public enum FeedbackStatus {
    /**
     * 요청이 생성되었으나 아직 전송되지 않은 대기 상태
     */
    PENDING,
    /**
     * AI 모델에 요청을 보내고 결과를 기다리는 중인 상태
     */
    PROCESSING,
    /**
     * AI로부터 유효한 응답을 받아 처리가 완료된 상태
     */
    COMPLETED,
    /**
     * 네트워크 오류, 타임아웃, AI 모델 에러 등으로 중단된 상태
     */
    FAILED
}
