package site.okkul.be.domain.practice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.entity.QuestionType;
import site.okkul.be.domain.question.entity.converter.QuestionTypeConverter;
import site.okkul.be.domain.topic.entity.Topic;
import site.okkul.be.domain.user.entity.User;

import java.time.Instant;
import java.util.Set;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "practice")
public class Practice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "practice_id")
    private Long practiceId;

    @CreationTimestamp
    @Column(name = "started_at")
    private Instant startedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Convert(converter = QuestionTypeConverter.class)
    private QuestionType questionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private QuestionSet questionSet;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "practice_question",
            joinColumns = @JoinColumn(name = "practice_id")
    )
    @Column(name = "question_id", nullable = false)
    private Set<Long> questionIds;
}