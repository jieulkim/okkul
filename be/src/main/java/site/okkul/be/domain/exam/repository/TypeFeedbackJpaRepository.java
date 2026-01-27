package site.okkul.be.domain.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.okkul.be.domain.exam.entity.TypeFeedback;

import java.util.List;

public interface TypeFeedbackJpaRepository extends JpaRepository<TypeFeedback, Long> {

    @Query("SELECT tf FROM TypeFeedback tf " +
            "JOIN FETCH tf.examAnswer ea " +
            "JOIN FETCH ea.question q " +
            "JOIN FETCH q.questionSet qs " +
            "JOIN FETCH qs.questionType qt " +
            "WHERE ea.examId = :examId")
    List<TypeFeedback> findAllByExamIdWithAnswerAndQuestion(@Param("examId") Long examId);
}