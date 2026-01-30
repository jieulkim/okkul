package site.okkul.be.domain.exam.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.exam.entity.Exam;

/**
 * 모의고사 리포지토리
 */
@Repository
public interface ExamJpaRepository extends JpaRepository<Exam, Long> {

	Optional<Exam> findByIdAndUserId(Long id, Long userId);

	Page<Exam> findByUserId(Long userId, Pageable pageable);
}
