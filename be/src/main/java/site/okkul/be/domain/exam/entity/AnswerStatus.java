package site.okkul.be.domain.exam.entity;

public enum AnswerStatus {
    READY,       // 초기 생성 (사용자가 아직 녹음 버튼을 누르기 전)
    UPLOADED,    // 음성 파일(MinIO/S3) 업로드 완료
    STT_ONGOING, // STT 변환 중 (음성 -> 텍스트)
    ANALYZING,   // STT 완료 후 AI가 피드백 분석 중 (오꿀쌤 분석 중)
    COMPLETED,   // 피드백 저장까지 모든 과정 완료
    ANALYZING_FAILED       // 어느 단계에서든 오류 발생 시
}
