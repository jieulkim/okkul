package site.okkul.be.domain.survey.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 설문조사 엔티티
 *
 * @author 김남주
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long surveyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Q1-1. 현재 귀하는 어느 분야에 종사하고 계십니까?
    @Enumerated(EnumType.STRING)
    @Column(name = "occupation")
    private OccupationType occupation;

    // Q1-2. 현재 귀하는 어디에서 학생을 가르치십니까?
    @Enumerated(EnumType.STRING)
    @Column(name = "teach_at")
    private TeachingLevel teachAt;

    // Q1-3. 현재 귀하는 직업이 있으십니까?
    @Column(name = "has_job")
    private Boolean hasJob;

    // Q1-4. 귀하의 근무 기간은 얼마나 되십니까?
    @Enumerated(EnumType.STRING)
    @Column(name = "work_period")
    private WorkPeriod workPeriod;

    // Q1-5. 귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?
    @Column(name = "is_manager")
    private Boolean isManager;

    // Q2-1. 현재 당신은 어디에 살고 계십니까?
    @Column(name = "is_student")
    private Boolean isStudent;

    // Q2-2. 현재 당신은 어디에 살고 계십니까?
    @Enumerated(EnumType.STRING)
    @Column(name = "class_type")
    private ClassType classType;

    // Q3-1. 현재 당신은 어디에 살고 계십니까?
    @Enumerated(EnumType.STRING)
    @Column(name = "residence")
    private ResidenceType residence;

    @Column(name = "level")
    private Integer level; // 1~6

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "survey_topics", joinColumns = @JoinColumn(name = "survey_id"))
    @Column(name = "topic_id", nullable = false)
    private Set<Long> topicIds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
