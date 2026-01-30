package site.okkul.be.domain.practice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.entity.Practice;
import site.okkul.be.domain.practice.entity.PracticeAnswer;
import site.okkul.be.domain.practice.mapper.PracticeAnswerMapper;
import site.okkul.be.domain.practice.repository.PracticeAnswerJpaRepository;
import site.okkul.be.domain.practice.repository.PracticeJpaRepository;
import site.okkul.be.domain.qustion.entity.Question;
import site.okkul.be.domain.qustion.repository.QuestionRepository;
import site.okkul.be.infra.storage.FileStorageService;

@Component
@RequiredArgsConstructor
public class PracticeAnswerCreator {

    private final PracticeJpaRepository practiceJpaRepository;
    private final QuestionRepository questionRepository;
    private final PracticeAnswerJpaRepository practiceAnswerRepository;
    private final FileStorageService fileStorageService;
    private final PracticeAnswerMapper practiceAnswerMapper;

    /**
     * 답변을 생성하고 저장하는 책임을 가지며, 쓰기 가능한 새 트랜잭션에서 실행된다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createAndSaveAnswer(Long practiceId, PracticeFeedbackRequest request, MultipartFile audioFile) {
        Practice practice = practiceJpaRepository.findById(practiceId)
                .orElseThrow(() -> new IllegalArgumentException("Practice not found"));
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        String audioUrl = fileStorageService.upload(audioFile, "answer");

        PracticeAnswer practiceAnswer = practiceAnswerMapper.toEntity(request, audioUrl, practice, question);

        PracticeAnswer saved = practiceAnswerRepository.save(practiceAnswer);
        return saved.getPracticeAnswerId();
    }
}
