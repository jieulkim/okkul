package site.okkul.be.domain.qustion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.qustion.dto.QuestionSetRequest;
import site.okkul.be.domain.qustion.dto.QuestionSetResponse;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.entity.QuestionType;
import site.okkul.be.domain.qustion.repository.QuestionSetRepository;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.topic.repository.TopicRepository;

/**
 * @author 김남주
 */
@Service
@RequiredArgsConstructor

public class QuestionSetService {
	private final QuestionSetRepository questionSetRepository;
	private final TopicRepository topicRepository;
//	private final QuestionTypeRepository questionTypeRepository;

	@Transactional(readOnly = true)
	public Page<QuestionSetResponse> findAll(Pageable pageable) {
		return questionSetRepository.findAll(pageable)
				.map(QuestionSetResponse::from);
	}

	@Transactional(readOnly = true)
	public QuestionSetResponse findById(Long id) {
		return QuestionSetResponse.from(
				questionSetRepository.findById(id)
						.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문항 세트입니다. ID: " + id))
		);
	}

	@Transactional
	public QuestionSetResponse create(QuestionSetRequest request) {
		return QuestionSetResponse.from(
				questionSetRepository.save(QuestionSet.builder()
						.level(request.level())
						.topic(topicRepository.findById(request.topicId())
								.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주제입니다. ID: " + request.topicId())))
						.questionType(QuestionType.fromId(request.typeId()))
						.build())
		);
	}

	@Transactional
	public QuestionSetResponse update(Long id, QuestionSetRequest request) {
		QuestionSet questionSet = questionSetRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문항 세트입니다. ID: " + id));

		QuestionType type = QuestionType.fromId(request.typeId());

		Topic topic = topicRepository.findById(request.topicId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주제입니다. ID: " + request.topicId()));

		questionSet.update(request.level(), topic, type);

		return QuestionSetResponse.from(questionSet);
	}

	@Transactional
	public void delete(Long id) {
		questionSetRepository.deleteById(id);
	}
}
