package site.okkul.be.domain.qustion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.okkul.be.domain.qustion.repository.QuestionSetRepository;

/**
 * @author 김남주
 */
@Service
@RequiredArgsConstructor
public class QuestionSetService {
	private final QuestionSetRepository questionSetRepository;
}
