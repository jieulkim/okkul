# 1. 파이썬 3.12.10 (slim 버전) 사용
FROM python:3.12.10-slim

# 2. 컨테이너 내 작업 폴더 설정
WORKDIR /app

# 3. 의존성 설치 (중요: requirements.txt가 opic-agent 안에 있음!)
# 로컬의 ai/opic-agent/requirements.txt를 -> 컨테이너의 /app/requirements.txt로 복사
COPY opic-agent/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. 소스코드 전체 복사 (STT 폴더 등 형제 폴더도 다 같이 복사됨)
COPY . .

# 5. ★핵심 수정★: 실행할 위치를 opic-agent 안으로 변경
# (이걸 안 하면 uvicorn이 main.py를 못 찾습니다)
WORKDIR /app/opic-agent

# 6. 실행 (이제 main.py가 바로 옆에 있으니 실행됨)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]