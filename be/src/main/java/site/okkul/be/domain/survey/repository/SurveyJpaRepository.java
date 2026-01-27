package site.okkul.be.domain.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.okkul.be.domain.survey.entity.Survey;

import java.util.List;
import java.util.Optional;

public interface SurveyJpaRepository extends JpaRepository<Survey, Long> {

    List<Survey> findAllByUserId(Long userId);
    Optional<Survey> findBySurveyIdAndUserId(Long surveyId, Long userId);

}