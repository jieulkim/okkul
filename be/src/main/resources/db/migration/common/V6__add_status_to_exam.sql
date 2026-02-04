-- 1. status 컬럼 추가
ALTER TABLE exam ADD COLUMN status VARCHAR(20);

-- 2. 기존 데이터 마이그레이션 (기존 데이터가 있을 경우를 대비)
-- 종료 시간이 있으면 완료 상태, 없으면 진행 중 상태로 초기화
UPDATE exam SET status = 'COMPLETED' WHERE end_at IS NOT NULL;
UPDATE exam SET status = 'IN_PROGRESS' WHERE end_at IS NULL;

-- 3. NOT NULL 제약조건 추가
ALTER TABLE exam ALTER COLUMN status SET NOT NULL;
