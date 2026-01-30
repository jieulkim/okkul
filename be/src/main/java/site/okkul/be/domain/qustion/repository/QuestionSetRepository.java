package site.okkul.be.domain.qustion.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.qustion.entity.QuestionSet;
import site.okkul.be.domain.qustion.entity.QuestionType;

import java.util.Optional;

import java.util.Optional;

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

	Page<QuestionSet> findByLevel(Integer level, Pageable pageable);

    /**
     * 난이도, 소재, 문제유형에 따른 세트문제 1개 랜덤 조회
     * @param level    난이도
     * @param topicId  토픽 ID
     * @param typeId   문제 유형 ID
     * @return QuestionSet (Optional)
     *
     * MySQL: RAND()
     * PostgreSQL: RANDOM()
     */
    @Query(value = "SELECT * FROM question_set qs " +
            "WHERE qs.level = :level " +
            "AND qs.topic_id = :topicId " +
            "AND qs.type_id = :typeId " +
            "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<QuestionSet> findRandomByLevelAndTopicAndType(@Param("level") int level, @Param("topicId") long topicId, @Param("typeId") long typeId);

}
