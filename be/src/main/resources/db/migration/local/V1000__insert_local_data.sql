-- ========================================================
-- [필수] 기초 데이터(Seed Data) 삽입
-- ========================================================

-- -- (1) 문제 유형 (기본값)
-- -- 7가지 OPIc 문제 유형 적재
-- INSERT INTO question_type(type_code, description)
-- VALUES ('INTRODUCTION', '자기소개'),
--        ('COMBO2', '두문제 콤보 - order 1 묘사 > order 2 묘사, 루틴, 비교 중 하나'),
--        ('COMBO3', '세문제 콤보 - order 1 묘사 > order 2 묘사, 루틴, 비교 중 하나, order 3 과거 경험'),
--        ('RP1', '롤플레이 한문제 - 정보 요청'),
--        ('RP2', '롤플레이 두문제 - order 1 묘사 > order 2 이전 질문 관련 eva에게 질문하기'),
--        ('RP3', '롤플레이 세문제 - order 1 정보 요청 > order 2 대안 제시 > order 3 관련 과거 경험'),
--        ('AD2', '어드밴스 두문제 - order 1 비교, 묘사, 루틴 > order 2 이전 관련 이슈, 뉴스, 의견 등');


-- ============================================================
-- 1. 중분류 (topic_category) 데이터 적재
-- ============================================================
INSERT INTO topic_category(category_id, category_code, category_name)
VALUES
-- [Part 4] 선택형 설문 (기존 데이터)
(1, 'LEISURE_ACTIVITY', '여가 활동'),
(2, 'HOBBY_INTEREST', '취미나 관심사'),
(3, 'SPORTS', '운동'),
(4, 'TRAVEL_VACATION', '휴가 및 출장'),
-- [Part 1~3] 공통 필수 설문 (추가된 데이터)
(5, 'OCCUPATION', '신분/직업'),
(6, 'EDUCATION', '학생 여부'),
(7, 'RESIDENCE', '거주지');


-- ============================================================
-- 2. 소분류 (Topic) 데이터 적재
-- ============================================================
INSERT INTO topic (topic_id, topic_code, topic_name, category_id)
VALUES
-- ------------------------------------------------------------
-- Category 1: 여가 활동 (100번대)
-- ------------------------------------------------------------
(101, 'WATCH_MOVIE', '영화보기', 1),
(102, 'GO_TO_CLUB', '클럽/나이트클럽 가기', 1),
(103, 'PERFORMANCE', '공연보기', 1),
(104, 'CONCERT', '콘서트보기', 1),
(105, 'MUSEUM', '박물관가기', 1),
(106, 'PARK', '공원가기', 1),
(107, 'CAMPING', '캠핑하기', 1),
(108, 'BEACH', '해변가기', 1),
(109, 'WATCH_SPORTS_GAME', '스포츠 관람', 1),
(110, 'HOME_IMPROVEMENT', '주거 개선', 1),

-- ------------------------------------------------------------
-- Category 2: 취미나 관심사 (200번대)
-- ------------------------------------------------------------
(201, 'READ_TO_CHILD', '아이에게 책 읽어주기', 2),
(202, 'LISTEN_MUSIC', '음악 감상하기', 2),
(203, 'PLAY_INSTRUMENT', '악기 연주하기', 2),
(204, 'SINGING', '혼자 노래부르거나 합창하기', 2),
(205, 'DANCING', '춤추기', 2),
(206, 'WRITING', '글쓰기(편지, 단문, 시 등)', 2),
(207, 'PAINTING', '그림 그리기', 2),
(208, 'COOKING', '요리하기', 2),
(209, 'RAISE_PET', '애완동물 기르기', 2),

-- ------------------------------------------------------------
-- Category 3: 운동 (300번대)
-- ------------------------------------------------------------
(301, 'BASKETBALL', '농구', 3),
(302, 'BASEBALL', '야구/소프트볼', 3),
(303, 'SOCCER', '축구', 3),
(304, 'FOOTBALL', '미식축구', 3),
(305, 'HOCKEY', '하키', 3),
(306, 'CRICKET', '크리켓', 3),
(307, 'GOLF', '골프', 3),
(308, 'VOLLEYBALL', '배구', 3),
(309, 'TENNIS', '테니스', 3),
(310, 'BADMINTON', '배드민턴', 3),
(311, 'PING_PONG', '탁구', 3),
(312, 'SWIMMING', '수영', 3),
(313, 'BICYCLE', '자전거', 3),
(314, 'SKI_SNOWBOARD', '스키/스노우보드', 3),
(315, 'ICE_SKATE', '아이스 스케이트', 3),
(316, 'JOGGING', '조깅', 3),
(317, 'WALKING', '걷기', 3),
(318, 'YOGA', '요가', 3),
(319, 'HIKING', '하이킹/트레킹', 3),
(320, 'FISHING', '낚시', 3),
(321, 'HEALTH_FITNESS', '헬스', 3),
(322, 'NO_EXERCISE', '운동을 전혀 하지 않음', 3),

-- ------------------------------------------------------------
-- Category 4: 휴가 및 출장 (400번대)
-- ------------------------------------------------------------
(401, 'BIZ_TRIP_DOMESTIC', '국내출장', 4),
(402, 'BIZ_TRIP_OVERSEAS', '해외출장', 4),
(403, 'VACATION_HOME', '집에서 보내는 휴가', 4),
(404, 'TRIP_DOMESTIC', '국내 여행', 4),
(405, 'TRIP_OVERSEAS', '해외 여행', 4),

-- ------------------------------------------------------------
-- Category 5: 신분/직업 (500번대) - [NEW]
-- ------------------------------------------------------------
(501, 'JOB_MANAGER', '직장인 (관리직)', 5),
(502, 'JOB_WORKER', '직장인 (일반/비관리직)', 5),
(503, 'NO_JOB', '비구직/무직', 5),

-- ------------------------------------------------------------
-- Category 6: 학생 여부 (600번대) - [NEW]
-- ------------------------------------------------------------
(601, 'STUDENT', '학생', 6),
(602, 'NOT_STUDENT', '학생 아님', 6),

-- ------------------------------------------------------------
-- Category 7: 거주지 (700번대) - [NEW]
-- ------------------------------------------------------------
(701, 'LIVE_ALONE', '홀로 거주', 7),
(702, 'LIVE_WITH_FRIEND', '친구/룸메이트와 함께 거주', 7),
(703, 'LIVE_WITH_FAMILY', '가족과 함께 거주', 7),
(704, 'DORMITORY', '학교 기숙사', 7),
(705, 'MILITARY_BASE', '군대 막사', 7);

-- ============================================================
-- 3. 질문 세트 (QuestionSet) 예제 데이터 적재
-- ============================================================
INSERT INTO question_set (set_id, level, question_cnt, topic_id, type_id, created_at, updated_at) VALUES
(1, 4, 3, 101, 1, NOW(), NOW()),
(2, 4, 3, 101, 2, NOW(), NOW()),
(3, 4, 3, 101, 3, NOW(), NOW()),
(4, 4, 3, 102, 1, NOW(), NOW()),
(5, 4, 3, 102, 2, NOW(), NOW()),
(6, 4, 3, 102, 3, NOW(), NOW()),
(7, 4, 3, 201, 1, NOW(), NOW()),
(8, 4, 3, 201, 2, NOW(), NOW()),
(9, 5, 3, 201, 3, NOW(), NOW()),
(10, 5, 3, 202, 1, NOW(), NOW()),
(11, 5, 3, 202, 2, NOW(), NOW()),
(12, 5, 3, 202, 3, NOW(), NOW()),
(13, 5, 3, 301, 1, NOW(), NOW()),
(14, 5, 3, 301, 2, NOW(), NOW()),
(15, 5, 3, 301, 3, NOW(), NOW()),
(16, 5, 3, 101, 1, NOW(), NOW()),
(17, 5, 3, 101, 2, NOW(), NOW()),
(18, 6, 3, 101, 3, NOW(), NOW()),
(19, 6, 3, 102, 1, NOW(), NOW()),
(20, 6, 3, 102, 2, NOW(), NOW()),
(21, 6, 3, 102, 3, NOW(), NOW()),
(22, 6, 3, 201, 1, NOW(), NOW()),
(23, 6, 3, 201, 2, NOW(), NOW()),
(24, 6, 3, 201, 3, NOW(), NOW()),
(25, 6, 3, 202, 1, NOW(), NOW()),
(26, 6, 3, 202, 2, NOW(), NOW()),
(27, 6, 3, 202, 3, NOW(), NOW()),
(28, 6, 3, 301, 1, NOW(), NOW()),
(29, 5, 3, 301, 2, NOW(), NOW()),
(30, 6, 3, 301, 3, NOW(), NOW());

ALTER SEQUENCE question_set_set_id_seq RESTART WITH 31;

-- ============================================================
-- 4. 질문 (Question) 예제 데이터 적재
-- ============================================================
INSERT INTO question_bank (question_text, audio_url, order_index, set_id, created_at, updated_at) VALUES
('Question 1 for Set: L1-T101-Q1', 'http://example.com/L1-T101-Q1-1.mp3', 1, 1, NOW(), NOW()),
('Question 2 for Set: L1-T101-Q1', 'http://example.com/L1-T101-Q1-2.mp3', 2, 1, NOW(), NOW()),
('Question 3 for Set: L1-T101-Q1', 'http://example.com/L1-T101-Q1-3.mp3', 3, 1, NOW(), NOW()),
('Question 1 for Set: L1-T101-Q2', 'http://example.com/L1-T101-Q2-1.mp3', 1, 2, NOW(), NOW()),
('Question 2 for Set: L1-T101-Q2', 'http://example.com/L1-T101-Q2-2.mp3', 2, 2, NOW(), NOW()),
('Question 3 for Set: L1-T101-Q2', 'http://example.com/L1-T101-Q2-3.mp3', 3, 2, NOW(), NOW()),
('Question 1 for Set: L1-T101-Q3', 'http://example.com/L1-T101-Q3-1.mp3', 1, 3, NOW(), NOW()),
('Question 2 for Set: L1-T101-Q3', 'http://example.com/L1-T101-Q3-2.mp3', 2, 3, NOW(), NOW()),
('Question 3 for Set: L1-T101-Q3', 'http://example.com/L1-T101-Q3-3.mp3', 3, 3, NOW(), NOW()),
('Question 1 for Set: L1-T102-Q1', 'http://example.com/L1-T102-Q1-1.mp3', 1, 4, NOW(), NOW()),
('Question 2 for Set: L1-T102-Q1', 'http://example.com/L1-T102-Q1-2.mp3', 2, 4, NOW(), NOW()),
('Question 3 for Set: L1-T102-Q1', 'http://example.com/L1-T102-Q1-3.mp3', 3, 4, NOW(), NOW()),
('Question 1 for Set: L1-T102-Q2', 'http://example.com/L1-T102-Q2-1.mp3', 1, 5, NOW(), NOW()),
('Question 2 for Set: L1-T102-Q2', 'http://example.com/L1-T102-Q2-2.mp3', 2, 5, NOW(), NOW()),
('Question 3 for Set: L1-T102-Q2', 'http://example.com/L1-T102-Q2-3.mp3', 3, 5, NOW(), NOW()),
('Question 1 for Set: L1-T102-Q3', 'http://example.com/L1-T102-Q3-1.mp3', 1, 6, NOW(), NOW()),
('Question 2 for Set: L1-T102-Q3', 'http://example.com/L1-T102-Q3-2.mp3', 2, 6, NOW(), NOW()),
('Question 3 for Set: L1-T102-Q3', 'http://example.com/L1-T102-Q3-3.mp3', 3, 6, NOW(), NOW()),
('Question 1 for Set: L1-T201-Q1', 'http://example.com/L1-T201-Q1-1.mp3', 1, 7, NOW(), NOW()),
('Question 2 for Set: L1-T201-Q1', 'http://example.com/L1-T201-Q1-2.mp3', 2, 7, NOW(), NOW()),
('Question 3 for Set: L1-T201-Q1', 'http://example.com/L1-T201-Q1-3.mp3', 3, 7, NOW(), NOW()),
('Question 1 for Set: L1-T201-Q2', 'http://example.com/L1-T201-Q2-1.mp3', 1, 8, NOW(), NOW()),
('Question 2 for Set: L1-T201-Q2', 'http://example.com/L1-T201-Q2-2.mp3', 2, 8, NOW(), NOW()),
('Question 3 for Set: L1-T201-Q2', 'http://example.com/L1-T201-Q2-3.mp3', 3, 8, NOW(), NOW()),
('Question 1 for Set: L1-T201-Q3', 'http://example.com/L1-T201-Q3-1.mp3', 1, 9, NOW(), NOW()),
('Question 2 for Set: L1-T201-Q3', 'http://example.com/L1-T201-Q3-2.mp3', 2, 9, NOW(), NOW()),
('Question 3 for Set: L1-T201-Q3', 'http://example.com/L1-T201-Q3-3.mp3', 3, 9, NOW(), NOW()),
('Question 1 for Set: L1-T202-Q1', 'http://example.com/L1-T202-Q1-1.mp3', 1, 10, NOW(), NOW()),
('Question 2 for Set: L1-T202-Q1', 'http://example.com/L1-T202-Q1-2.mp3', 2, 10, NOW(), NOW()),
('Question 3 for Set: L1-T202-Q1', 'http://example.com/L1-T202-Q1-3.mp3', 3, 10, NOW(), NOW()),
('Question 1 for Set: L1-T202-Q2', 'http://example.com/L1-T202-Q2-1.mp3', 1, 11, NOW(), NOW()),
('Question 2 for Set: L1-T202-Q2', 'http://example.com/L1-T202-Q2-2.mp3', 2, 11, NOW(), NOW()),
('Question 3 for Set: L1-T202-Q2', 'http://example.com/L1-T202-Q2-3.mp3', 3, 11, NOW(), NOW()),
('Question 1 for Set: L1-T202-Q3', 'http://example.com/L1-T202-Q3-1.mp3', 1, 12, NOW(), NOW()),
('Question 2 for Set: L1-T202-Q3', 'http://example.com/L1-T202-Q3-2.mp3', 2, 12, NOW(), NOW()),
('Question 3 for Set: L1-T202-Q3', 'http://example.com/L1-T202-Q3-3.mp3', 3, 12, NOW(), NOW()),
('Question 1 for Set: L1-T301-Q1', 'http://example.com/L1-T301-Q1-1.mp3', 1, 13, NOW(), NOW()),
('Question 2 for Set: L1-T301-Q1', 'http://example.com/L1-T301-Q1-2.mp3', 2, 13, NOW(), NOW()),
('Question 3 for Set: L1-T301-Q1', 'http://example.com/L1-T301-Q1-3.mp3', 3, 13, NOW(), NOW()),
('Question 1 for Set: L1-T301-Q2', 'http://example.com/L1-T301-Q2-1.mp3', 1, 14, NOW(), NOW()),
('Question 2 for Set: L1-T301-Q2', 'http://example.com/L1-T301-Q2-2.mp3', 2, 14, NOW(), NOW()),
('Question 3 for Set: L1-T301-Q2', 'http://example.com/L1-T301-Q2-3.mp3', 3, 14, NOW(), NOW()),
('Question 1 for Set: L1-T301-Q3', 'http://example.com/L1-T301-Q3-1.mp3', 1, 15, NOW(), NOW()),
('Question 2 for Set: L1-T301-Q3', 'http://example.com/L1-T301-Q3-2.mp3', 2, 15, NOW(), NOW()),
('Question 3 for Set: L1-T301-Q3', 'http://example.com/L1-T301-Q3-3.mp3', 3, 15, NOW(), NOW()),
('Question 1 for Set: L2-T101-Q1', 'http://example.com/L2-T101-Q1-1.mp3', 1, 16, NOW(), NOW()),
('Question 2 for Set: L2-T101-Q1', 'http://example.com/L2-T101-Q1-2.mp3', 2, 16, NOW(), NOW()),
('Question 3 for Set: L2-T101-Q1', 'http://example.com/L2-T101-Q1-3.mp3', 3, 16, NOW(), NOW()),
('Question 1 for Set: L2-T101-Q2', 'http://example.com/L2-T101-Q2-1.mp3', 1, 17, NOW(), NOW()),
('Question 2 for Set: L2-T101-Q2', 'http://example.com/L2-T101-Q2-2.mp3', 2, 17, NOW(), NOW()),
('Question 3 for Set: L2-T101-Q2', 'http://example.com/L2-T101-Q2-3.mp3', 3, 17, NOW(), NOW()),
('Question 1 for Set: L2-T101-Q3', 'http://example.com/L2-T101-Q3-1.mp3', 1, 18, NOW(), NOW()),
('Question 2 for Set: L2-T101-Q3', 'http://example.com/L2-T101-Q3-2.mp3', 2, 18, NOW(), NOW()),
('Question 3 for Set: L2-T101-Q3', 'http://example.com/L2-T101-Q3-3.mp3', 3, 18, NOW(), NOW()),
('Question 1 for Set: L2-T102-Q1', 'http://example.com/L2-T102-Q1-1.mp3', 1, 19, NOW(), NOW()),
('Question 2 for Set: L2-T102-Q1', 'http://example.com/L2-T102-Q1-2.mp3', 2, 19, NOW(), NOW()),
('Question 3 for Set: L2-T102-Q1', 'http://example.com/L2-T102-Q1-3.mp3', 3, 19, NOW(), NOW()),
('Question 1 for Set: L2-T102-Q2', 'http://example.com/L2-T102-Q2-1.mp3', 1, 20, NOW(), NOW()),
('Question 2 for Set: L2-T102-Q2', 'http://example.com/L2-T102-Q2-2.mp3', 2, 20, NOW(), NOW()),
('Question 3 for Set: L2-T102-Q2', 'http://example.com/L2-T102-Q2-3.mp3', 3, 20, NOW(), NOW()),
('Question 1 for Set: L2-T102-Q3', 'http://example.com/L2-T102-Q3-1.mp3', 1, 21, NOW(), NOW()),
('Question 2 for Set: L2-T102-Q3', 'http://example.com/L2-T102-Q3-2.mp3', 2, 21, NOW(), NOW()),
('Question 3 for Set: L2-T102-Q3', 'http://example.com/L2-T102-Q3-3.mp3', 3, 21, NOW(), NOW()),
('Question 1 for Set: L2-T201-Q1', 'http://example.com/L2-T201-Q1-1.mp3', 1, 22, NOW(), NOW()),
('Question 2 for Set: L2-T201-Q1', 'http://example.com/L2-T201-Q1-2.mp3', 2, 22, NOW(), NOW()),
('Question 3 for Set: L2-T201-Q1', 'http://example.com/L2-T201-Q1-3.mp3', 3, 22, NOW(), NOW()),
('Question 1 for Set: L2-T201-Q2', 'http://example.com/L2-T201-Q2-1.mp3', 1, 23, NOW(), NOW()),
('Question 2 for Set: L2-T201-Q2', 'http://example.com/L2-T201-Q2-2.mp3', 2, 23, NOW(), NOW()),
('Question 3 for Set: L2-T201-Q2', 'http://example.com/L2-T201-Q2-3.mp3', 3, 23, NOW(), NOW()),
('Question 1 for Set: L2-T201-Q3', 'http://example.com/L2-T201-Q3-1.mp3', 1, 24, NOW(), NOW()),
('Question 2 for Set: L2-T201-Q3', 'http://example.com/L2-T201-Q3-2.mp3', 2, 24, NOW(), NOW()),
('Question 3 for Set: L2-T201-Q3', 'http://example.com/L2-T201-Q3-3.mp3', 3, 24, NOW(), NOW()),
('Question 1 for Set: L2-T202-Q1', 'http://example.com/L2-T202-Q1-1.mp3', 1, 25, NOW(), NOW()),
('Question 2 for Set: L2-T202-Q1', 'http://example.com/L2-T202-Q1-2.mp3', 2, 25, NOW(), NOW()),
('Question 3 for Set: L2-T202-Q1', 'http://example.com/L2-T202-Q1-3.mp3', 3, 25, NOW(), NOW()),
('Question 1 for Set: L2-T202-Q2', 'http://example.com/L2-T202-Q2-1.mp3', 1, 26, NOW(), NOW()),
('Question 2 for Set: L2-T202-Q2', 'http://example.com/L2-T202-Q2-2.mp3', 2, 26, NOW(), NOW()),
('Question 3 for Set: L2-T202-Q2', 'http://example.com/L2-T202-Q2-3.mp3', 3, 26, NOW(), NOW()),
('Question 1 for Set: L2-T202-Q3', 'http://example.com/L2-T202-Q3-1.mp3', 1, 27, NOW(), NOW()),
('Question 2 for Set: L2-T202-Q3', 'http://example.com/L2-T202-Q3-2.mp3', 2, 27, NOW(), NOW()),
('Question 3 for Set: L2-T202-Q3', 'http://example.com/L2-T202-Q3-3.mp3', 3, 27, NOW(), NOW()),
('Question 1 for Set: L2-T301-Q1', 'http://example.com/L2-T301-Q1-1.mp3', 1, 28, NOW(), NOW()),
('Question 2 for Set: L2-T301-Q1', 'http://example.com/L2-T301-Q1-2.mp3', 2, 28, NOW(), NOW()),
('Question 3 for Set: L2-T301-Q1', 'http://example.com/L2-T301-Q1-3.mp3', 3, 28, NOW(), NOW()),
('Question 1 for Set: L2-T301-Q2', 'http://example.com/L2-T301-Q2-1.mp3', 1, 29, NOW(), NOW()),
('Question 2 for Set: L2-T301-Q2', 'http://example.com/L2-T301-Q2-2.mp3', 2, 29, NOW(), NOW()),
('Question 3 for Set: L2-T301-Q2', 'http://example.com/L2-T301-Q2-3.mp3', 3, 29, NOW(), NOW()),
('Question 1 for Set: L2-T301-Q3', 'http://example.com/L2-T301-Q3-1.mp3', 1, 30, NOW(), NOW()),
('Question 2 for Set: L2-T301-Q3', 'http://example.com/L2-T301-Q3-2.mp3', 2, 30, NOW(), NOW()),
('Question 3 for Set: L2-T301-Q3', 'http://example.com/L2-T301-Q3-3.mp3', 3, 30, NOW(), NOW());

