package site.okkul.be.domain.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.practice.entity.PracticeSentenceFeedback;

@Repository
public interface PracticeSentenceFeedbackJpaRepository extends JpaRepository<PracticeSentenceFeedback, Long> {

}
