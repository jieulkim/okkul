package site.okkul.be.domain.survey.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.survey.entity.Survey;

@Repository
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

	/**
	 * 가잔 최신에 생성된 설문조사 3개 조회
	 *
	 * @param userId
	 * @return 설문조사 Top 3
	 */
	@Query("SELECT s FROM Survey s " +
			"WHERE s.userId =:userId " +
			"ORDER BY s.createdAt DESC LIMIT 3")
	List<Survey> findTop3SurveyIdAndUserId(@Param("userId") Long userId);

}
