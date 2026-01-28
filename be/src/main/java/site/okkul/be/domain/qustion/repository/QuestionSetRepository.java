package site.okkul.be.domain.qustion.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.entity.QuestionType;

/**
 * @author 김남주
 */
@Repository
public interface QuestionSetRepository extends JpaRepository<QuestionSet, Long> {

	@Query(value = """
			SELECT qs.*
			FROM question_set qs
			JOIN question_type qt ON qs.type_id = qt.type_id
			WHERE qs.level = :level
			  AND qs.topic_id = :topicId
			  AND qt.type_code = :typeCode
			ORDER BY random()
			LIMIT 1
			""", nativeQuery = true)
	Optional<QuestionSet> findRandomByLevelAndTopicIdAndTypeCode(
			@Param("level") Integer level,
			@Param("topicId") Long topicId,
			@Param("typeCode") QuestionType type
	);

	@Query(value = """
			SELECT qs.*
			FROM question_set qs
			JOIN question_type qt ON qs.type_id = qt.type_id
			WHERE qt.type_code = :typeCode
			ORDER BY random()
			LIMIT 1
			""", nativeQuery = true)
	Optional<QuestionSet> findRandomByTypeCode(@Param("typeCode") String typeCode);


	@Query("""
			SELECT q from QuestionSet q
			WHERE q.level = ?1 and
					q.topic.id = ?2 and
					q.questionType = ?3
			ORDER BY RANDOM()
			LIMIT 1
			""")
	Optional<QuestionSet> findRandomByLevelAndTopic(Integer level, Long topicId, QuestionType questionType);
}
