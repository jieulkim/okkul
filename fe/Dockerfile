# --------------------------------------------------------
# 1단계: Build Stage (Node.js로 빌드)
# --------------------------------------------------------
FROM node:20-alpine as build-stage

# 작업 디렉토리 설정
WORKDIR /app

# 패키지 파일 먼저 복사 (캐시 활용을 위해)
COPY package*.json ./

# 의존성 설치
RUN npm install

# 소스 코드 복사
COPY . .

# 빌드 인자(Argument) 설정 (기본값: production)
ARG PROFILE=production

# GitLab CI/CD 등 외부에서 주입받을 환경 변수 ARG로 선언
ARG VITE_FIREBASE_API_KEY
ARG VITE_FIREBASE_AUTH_DOMAIN
ARG VITE_FIREBASE_PROJECT_ID
ARG VITE_FIREBASE_STORAGE_BUCKET
ARG VITE_FIREBASE_MESSAGING_SENDER_ID
ARG VITE_FIREBASE_APP_ID
ARG VITE_FIREBASE_MEASUREMENT_ID

# 빌드 실행 (브랜치명을 mode로 전달: cicd, develop, master)
RUN npm run build -- --mode ${PROFILE}

# --------------------------------------------------------
# 2단계: Production Stage (Nginx로 배포)
# --------------------------------------------------------
FROM nginx:alpine as production-stage

# 1단계에서 빌드된 결과물(dist 폴더)만 Nginx의 서빙 폴더로 복사
COPY --from=build-stage /app/dist /usr/share/nginx/html

# SPA(Single Page App) 라우팅 처리를 위한 설정 파일 복사 (아래 2번에서 생성)
COPY default.conf /etc/nginx/conf.d/default.conf

# 80 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]
