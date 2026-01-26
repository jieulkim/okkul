package site.okkul.be.domain.qustion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.qustion.entity.Question;

/**
 * @author 김남주
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
}
