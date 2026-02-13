-- 1. DEV (개발용) 환경 설정
CREATE USER dev_user WITH PASSWORD 'dev_pass_1234';
CREATE SCHEMA dev_schema AUTHORIZATION dev_user;
-- dev_user에게 dev_schema에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON SCHEMA dev_schema TO dev_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA dev_schema TO dev_user;

-- 2. PROD (운영용) 환경 설정
CREATE USER prod_user WITH PASSWORD 'prod_pass_secure';
CREATE SCHEMA prod_schema AUTHORIZATION prod_user;
-- prod_user에게 prod_schema에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON SCHEMA prod_schema TO prod_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA prod_schema TO prod_user;

-- 3. 기본 DB에 대한 접속 권한 부여
GRANT CONNECT ON DATABASE main_db TO dev_user;
GRANT CONNECT ON DATABASE main_db TO prod_user;