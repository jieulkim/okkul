package site.okkul.be.domain.qustion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.qustion.entity.QuestionSet;

import java.util.Optional;

/**
 * @author 김남주
 */
@Repository
public interface QuestionSetRepository extends JpaRepository<QuestionSet, Long> {

    @Query(value = """
        SELECT qs.*
        FROM question_set qs
        JOIN questiontype qt ON qs.type_id = qt.type_id
        WHERE qs.level = :level
          AND qs.topic_id = :topicId
          AND qt.type_code = :typeCode
        ORDER BY random()
        LIMIT 1
        """, nativeQuery = true)
    Optional<QuestionSet> findRandomByLevelAndTopicIdAndTypeCode(
            @Param("level") Integer level,
            @Param("topicId") Long topicId,
            @Param("typeCode") String typeCode
    );

    @Query(value = """
    SELECT qs.*
    FROM question_set qs
    JOIN questiontype qt ON qs.type_id = qt.type_id
    WHERE qt.type_code = :typeCode
    ORDER BY random()
    LIMIT 1
    """, nativeQuery = true)
    Optional<QuestionSet> findRandomByTypeCode(@Param("typeCode") String typeCode);

}
