package site.okkul.be.domain.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.okkul.be.domain.practice.entity.PracticeAnswer;

import java.util.Optional;

public interface PracticeAnswerJpaRepository extends JpaRepository<PracticeAnswer, Long> {
    Optional<PracticeAnswer> findByPracticeAnswerIdAndPractice_User_Id(Long practiceAnswerId, Long userId);
}
