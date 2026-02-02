package site.okkul.be.domain.qustion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.qustion.dto.QuestionDetailResponse;
import site.okkul.be.domain.qustion.dto.QuestionRequest;
import site.okkul.be.domain.qustion.entity.Question;
import site.okkul.be.domain.qustion.entity.QuestionErrorCode;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.repository.QuestionRepository;
import site.okkul.be.domain.qustion.repository.QuestionSetRepository;
import site.okkul.be.global.exception.BusinessException;

/**
 * @author 김남주
 */
@Service
@RequiredArgsConstructor
public class QuestionService {
	private final QuestionRepository questionRepository;
	private final QuestionSetRepository questionSetRepository;

	@Transactional
	public QuestionDetailResponse addQuestion(Long setId, QuestionRequest request) {
		QuestionSet questionSet = questionSetRepository.findById(setId)
				.orElseThrow(() -> new BusinessException(QuestionErrorCode.QUESTION_SET_NOT_FOUND));

		// Question 엔티티 생성 및 연관관계 편의 메서드 활용
		Question question = Question.builder()
				.questionText(request.questionText())
				.audioUrl(request.audioUrl())
				.order(request.order())
				.questionSet(questionSet) // 부모 연결
				.build();

		return QuestionDetailResponse.from(questionRepository.save(question));
	}

	@Transactional
	public QuestionDetailResponse updateQuestion(Long questionId, QuestionRequest request) {
		Question question = questionRepository.findById(questionId)
				.orElseThrow(() -> new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND));

		// 더티 체킹을 통한 수정
		question.update(request.questionText(), request.audioUrl(), request.order());
		return QuestionDetailResponse.from(question);
	}

	@Transactional
	public void deleteQuestion(Long questionId) {
		questionRepository.deleteById(questionId);
	}
}
