package site.okkul.be.domain.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.okkul.be.domain.exam.entity.ExamAnswer;

import java.util.List;

public interface ExamAnswerJpaRepository extends JpaRepository<ExamAnswer, Long> {
    List<ExamAnswer> findAllByExamId(Long examId);
}
