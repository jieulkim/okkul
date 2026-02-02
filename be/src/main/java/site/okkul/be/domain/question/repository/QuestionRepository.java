package site.okkul.be.domain.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.question.entity.Question;

/**
 * @author 김남주
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
}
