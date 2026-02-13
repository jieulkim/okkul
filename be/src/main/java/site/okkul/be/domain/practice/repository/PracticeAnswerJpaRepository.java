package site.okkul.be.domain.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import site.okkul.be.domain.practice.entity.PracticeAnswer;

import java.util.List;
import java.util.Optional;

public interface PracticeAnswerJpaRepository extends JpaRepository<PracticeAnswer, Long> {
    Optional<PracticeAnswer> findByPracticeAnswerIdAndPractice_User_Id(Long practiceAnswerId, Long userId);

    @Query("SELECT pa FROM PracticeAnswer pa LEFT JOIN pa.feedbacks WHERE pa.practice.practiceId = :practiceId")
    List<PracticeAnswer> findAllByPracticeIdWithFeedbacks(Long practiceId);
}
