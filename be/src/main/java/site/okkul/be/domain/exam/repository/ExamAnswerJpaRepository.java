package site.okkul.be.domain.exam.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.okkul.be.domain.exam.entity.ExamAnswer;

/**
 * 모의고사 답변 레포지토리
 */
public interface ExamAnswerJpaRepository extends JpaRepository<ExamAnswer, ExamAnswer.ExamAnswerId> {
	List<ExamAnswer> findAllByExamId(Long examId);
}
