package site.okkul.be.domain.practice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.practice.entity.Practice;

import java.util.Optional;

@Repository
public interface PracticeJpaRepository extends JpaRepository<Practice, Long> {
    Optional<Practice> findByPracticeIdAndUserId(Long practiceId, Long userId);

    @Query("SELECT p FROM Practice p JOIN FETCH p.questionIds WHERE p.practiceId = :practiceId AND p.user.id = :userId")
    Optional<Practice> findByIdAndUserIdWithQuestionIds(@Param("practiceId") Long practiceId, @Param("userId") Long userId);

    Page<Practice> findAllByUserId(Long userId, Pageable pageable);
}
