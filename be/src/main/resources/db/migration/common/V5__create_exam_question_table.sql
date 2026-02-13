-- V4: Exam과 Question 간의 다대다 매핑을 위한 조인 테이블 생성

CREATE TABLE exam_question
(
    exam_id        BIGINT  NOT NULL,
    question_id    BIGINT  NOT NULL,
    question_order INTEGER NOT NULL,

    CONSTRAINT pk_exam_question PRIMARY KEY (exam_id, question_order),
    CONSTRAINT fk_exam_question_exam FOREIGN KEY (exam_id) REFERENCES exam (exam_id),
    CONSTRAINT fk_exam_question_question FOREIGN KEY (question_id) REFERENCES question_bank (question_id)
);

-- exam_question_set 테이블에 question_set_order 컬럼 추가
ALTER TABLE exam_question_set
    RENAME COLUMN question_order TO question_set_order;

-- exam_sentence_feedback 테이블 생성
-- 1. 기본키(PK) 제약 조건 삭제
ALTER TABLE exam_sentence_feedback DROP CONSTRAINT IF EXISTS pk_exam_sentence_feedback;

-- 2. 더 이상 필요 없는 컬럼 삭제 (feedback_id, updated_at)
ALTER TABLE exam_sentence_feedback DROP COLUMN IF EXISTS feedback_id;
ALTER TABLE exam_sentence_feedback DROP COLUMN IF EXISTS updated_at;

-- 3. FK 컬럼을 NOT NULL로 변경 (값 타입 컬렉션은 FK가 필수임)
-- 만약 기존에 데이터가 있다면, NULL인 데이터가 없는지 먼저 확인해야 합니다.
ALTER TABLE exam_sentence_feedback ALTER COLUMN exam_answer_exam_id SET NOT NULL;
ALTER TABLE exam_sentence_feedback ALTER COLUMN exam_answer_question_order SET NOT NULL;

-- 4. 외래키(FK) 제약 조건 추가 (이미 존재한다면 생략 가능)
-- 기존에 제약조건이 있었다면 삭제 후 재생성하는 것이 깔끔합니다.
ALTER TABLE exam_sentence_feedback DROP CONSTRAINT IF EXISTS fk_exam_sentence_feedback_exam_answer;

ALTER TABLE exam_sentence_feedback
    ADD CONSTRAINT fk_exam_sentence_feedback_exam_answer
        FOREIGN KEY (exam_answer_exam_id, exam_answer_question_order)
            REFERENCES exam_answer (exam_id, question_order)
            ON DELETE CASCADE; -- 부모 삭제 시 함께 삭제되도록 설정 권장

ALTER TABLE exam_report
ALTER COLUMN strength_type TYPE TEXT USING strength_type::TEXT;
ALTER TABLE exam_report
ALTER COLUMN weakness_type TYPE TEXT USING weakness_type::TEXT;