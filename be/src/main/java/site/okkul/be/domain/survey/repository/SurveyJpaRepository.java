package site.okkul.be.domain.survey.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.okkul.be.domain.survey.entity.Survey;

public interface SurveyJpaRepository extends JpaRepository<Survey, Long> {

	/**
	 * 유저별 설문조사 목록 조회
	 *
	 * @param userId 유저 ID
	 * @return 유저별 설문조사 목록
	 */
	List<Survey> findAllByUserId(Long userId);

	/**
	 * 유저 + 설문조사 ID를 통한 설문조사 조회
	 *
	 * @param surveyId 설문조사 ID
	 * @param userId   유저 ID
	 * @return 설문조사
	 */
	Optional<Survey> findBySurveyIdAndUserId(Long surveyId, Long userId);
}
