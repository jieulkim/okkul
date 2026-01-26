package site.okkul.be.domain.exam.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.exam.docs.ExamControllerDocs;
import site.okkul.be.domain.exam.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/exam")
public class ExamController implements ExamControllerDocs {

    @Override
    @PostMapping("/start")
    public ResponseEntity<ExamStartResponse> startExam(@RequestBody ExamStartRequest request) {
        // 1~7번 문항 생성 (더미 데이터)
        List<QuestionResponse> firstBatch = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> QuestionResponse.builder()
                        .answerId(100L + i)
                        .questionOrder(i)
                        .questionText("Question text for number " + i)
                        .audioUrl("https://storage.com/q" + i + ".mp3")
                        .build())
                .toList();

        ExamStartResponse dummy = ExamStartResponse.builder()
                .examId(1L)
                .totalQuestions(15)
                .questions(firstBatch) // 리스트로 전달
                .build();

        return ResponseEntity.ok(dummy);
    }

    @Override
    @PostMapping("/{examId}/questions/current")
    public ResponseEntity<List<QuestionResponse>> getRemainingQuestions(@PathVariable Long examId, @RequestParam int adjustedDifficulty) {
        // 1. Service를 호출하여 DB의 adjusted_difficulty를 확인하고
        // 2. 그 난이도에 맞는 8~15번 문항을 조회해오는 로직이 필요합니다.

        // 7번 문항 답변 이후, 난이도가 조정된 8~15번 문항 생성 (더미 데이터)
        List<QuestionResponse> remainingBatch = IntStream.rangeClosed(8, 15)
                .mapToObj(i -> QuestionResponse.builder()
                        .answerId(100L + i)
                        .questionOrder(i)
                        .questionText("Adjusted Difficulty Question " + i)
                        .audioUrl("https://storage.com/q" + i + ".mp3")
                        .build())
                .toList();

        return ResponseEntity.ok(remainingBatch);
    }

    @Override
    @PostMapping(
            value = "/exam/{examId}/questions/{answerId}/answer", // 경로 변수명을 answerId로 통일 권장
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> submitAnswer(
            @PathVariable Long examId,
            @PathVariable Long answerId, // 인터페이스와 이름을 맞추세요!
            @RequestPart("file") MultipartFile file
    ) {
        // 1. 파일이 비어있는지 체크 (더미 로직)
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 2. 실제로는 여기서 S3 업로드 + AI 분석 비동기 호출이 일어납니다.
        System.out.println("시험 ID: " + examId);
        System.out.println("답변 PK: " + answerId);
        System.out.println("파일명: " + file.getOriginalFilename());

        // 3. 성공 시 200 OK 반환
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/exam/{examId}/complete")
    public ResponseEntity<Void> completeExam(@PathVariable Long examId) {

        // 1. 실제로는 DB에서 해당 examId를 찾아 status를 'COMPLETED'로 변경합니다.
        System.out.println("시험 종료 처리 중... Exam ID: " + examId);

        // 2. AI 분석 엔진에게 "이제 모든 답변이 들어왔으니 최종 리포트를 생성해줘"라고
        // 신호를 보내는 비즈니스 로직이 여기서 시작될 수 있습니다.

        // 3. 성공 시 200 OK 반환 (데이터 없이 헤더만 보냄)
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/exam/{examId}/status")
    public ResponseEntity<ExamStatusResponse> getExamStatus(@PathVariable Long examId) {

        // 더미 데이터 생성
        ExamStatusResponse dummy = ExamStatusResponse.builder()
                .completedQuestions(12)      // 현재까지 분석 완료된 문항 수
                .totalQuestions(15)          // 전체 문항 수
                .isAllAnalyzed(false)        // 아직 분석 중임을 표시
                .estimatedRemainingSeconds(15) // 예상 남은 시간 (초)
                .build();

        return ResponseEntity.ok(dummy);
    }

    @Override
    @GetMapping("/exam/{examId}/result")
    public ResponseEntity<ExamResultResponse> getExamResult(@PathVariable Long examId) {

        // 1. 총평 및 카테고리별 점수 (오각형 차트용)
        ExamSummaryResponse summary = ExamSummaryResponse.builder()
                .totalScore(82.5)
                .grade("IH")
                .categoryScores(CategoryScoresResponse.builder() // 오각형 차트의 5개 축 데이터
                        .grammar(80)
                        .vocabulary(75)
                        .logic(90)
                        .fluency(85)
                        .relevance(88)
                        .build())
                .comment("전반적으로 우수한 실력이지만, 돌발 상황 대처 능력을 요구하는 롤플레이에서 주춤하는 모습이 보입니다.")
                .strengths("인물 및 장소 묘사") // 오픽 1, 2유형 (Description)
                .weakness("롤플레이 및 대안 제시") // 오픽 11, 12, 13유형 (Role-play)
                .build();

        // 2. 문항별 상세 결과 (리스트 형식)
        List<QuestionResultDetailResponse> details = List.of(
                QuestionResultDetailResponse.builder()
                        .questionOrder(1)
                        .questionText("Please introduce yourself.")
                        .sttScript("Hello, my name is Minsoo and I living in Seoul.")
                        .enhancedScript("Hello, my name is Minsoo and I live in Seoul.") // AI가 교정한 문장
                        // FeedbackSet을 사용하여 각 영역별 결과 담기
                        .grammar(new FeedbackSetResponse(3, "현재진행형보다는 일반현재시제가 적절합니다."))
                        .vocabulary(new FeedbackSetResponse(4, "표현이 평이하지만 정확합니다."))
                        .logic(new FeedbackSetResponse(5, "자기소개 구성이 매우 논리적입니다."))
                        .fluency(new FeedbackSetResponse(5, "발화량이 충분하여 자신감이 느껴집니다."))
                        .relevance(new FeedbackSetResponse(5, "질문에 완벽히 부합하는 답변입니다."))
                        .build(),
                QuestionResultDetailResponse.builder()
                        .questionOrder(2)
                        .questionText("Tell me about your favorite park.")
                        .sttScript("I like Hangang park because it is very beautiful at night.")
                        .enhancedScript("I love Hangang Park because the night view is absolutely stunning.")
                        .grammar(new FeedbackSetResponse(4, "문장이 간결하고 정확합니다."))
                        .vocabulary(new FeedbackSetResponse(3, "beautiful 대신 stunning, breathtaking 같은 표현을 써보세요."))
                        .logic(new FeedbackSetResponse(5, "이유 설명이 명확합니다."))
                        .fluency(new FeedbackSetResponse(2, "답변 길이가 다소 짧습니다. 구체적인 묘사를 더해 발화량을 늘려보세요."))
                        .relevance(new FeedbackSetResponse(5, "공원 묘사 주제에 충실합니다."))
                        .build()
        );

        // 3. 전체 응답 조립
        ExamResultResponse dummy = ExamResultResponse.builder()
                .examId(examId)
                .title("실전 대비 모의고사 1회")
                .createdAt(LocalDateTime.now())
                .summary(summary)
                .questionResults(details)
                .build();

        return ResponseEntity.ok(dummy);
    }
}
