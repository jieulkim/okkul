package site.okkul.be.domain.qustion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.okkul.be.domain.qustion.dto.QuestionTypeRequest;
import site.okkul.be.domain.qustion.entity.QuestionType;
import site.okkul.be.domain.qustion.repository.QuestionTypeRepository;

/**
 * @author 김남주
 */
@Service
@RequiredArgsConstructor
public class QuestionTypeService {
	private final QuestionTypeRepository questionTypeRepository;

	@Transactional(readOnly = true)
	public Page<QuestionType> findAll(Pageable pageable) {
		return questionTypeRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public QuestionType findById(Long id) {
		return questionTypeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 유형을 찾을 수 없습니다. ID: " + id));
	}

	@Transactional
	public QuestionType create(QuestionType questionType) {
		return questionTypeRepository.save(questionType);
	}

	@Transactional
	public QuestionType update(Long id, QuestionTypeRequest questionTypeRequest) {
		QuestionType type = findById(id);
		type.update(
				questionTypeRequest.typeCode(),
				questionTypeRequest.description()
		);
		return type;
	}

	@Transactional
	public void delete(Long id) {
		questionTypeRepository.deleteById(id);
	}
}
