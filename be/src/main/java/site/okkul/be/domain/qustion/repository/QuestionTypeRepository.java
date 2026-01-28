package site.okkul.be.domain.qustion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.qustion.entity.QuestionType;

/**
 * @author 김남주
 */
@Repository
public interface QuestionTypeRepository extends JpaRepository<QuestionType, Long> {
}
