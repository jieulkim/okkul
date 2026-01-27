package site.okkul.be.domain.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.okkul.be.domain.exam.entity.ExamReport;

public interface ExamReportJpaRepository extends JpaRepository<ExamReport, Long> {
}
