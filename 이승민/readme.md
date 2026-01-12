1. Git과 GitHub, 무엇이 다른가?
Git (Version Control System)
정의: 프로그램의 버전 관리를 위한 툴.

특징:

시간의 축: 버전의 변화(v1 → v2)를 기록.

차원의 축: 하나의 프로젝트를 여러 형상(원본 vs 테스트본)으로 관리.

GitHub
정의: Git으로 관리하는 프로젝트를 온라인에 공유하여 협업을 돕는 서비스.

역할:

교통 정리: 팀원 A와 팀원 B가 각자 작업한 내용을 커밋 단위로 주고받을 수 있게 한다.

충돌 방지: 원격 저장소에 올리기 전, 반드시 최신 내역을 내 컴퓨터로 받아오게(Pull) 강제함으로써 작업 간 Loss를 방지한다.

2. 환경 설정 (Windows 기준)
가장 먼저 VS Code와 Git Bash를 연동하고, 유저 정보를 설정해야 한다.

설치: Git & Git Bash, Sourcetree(GUI), VS Code 설치

터미널 설정: VS Code에서 Ctrl + `로 터미널 열기 → Ctrl + Shift + P → Select Default Profile → Git Bash 선택

Git 최초 설정 (User Config)
누가 코드를 수정했는지 식별하기 위해 필수적이다.

Bash

# 1. 유저 이름 및 이메일 설정
git config --global user.name "본인영문이름"
git config --global user.email "본인이메일@gmail.com"

# 2. 설정 확인
git config --global user.name
git config --global user.email

# 3. 기본 브랜치명 변경 (master -> main)
git config --global init.defaultBranch main
3. 프로젝트 생성 및 관리 시작
프로젝트 초기화
Bash

# 해당 폴더를 Git이 관리하도록 설정 (최초 1회)
git init

# 현재 Git 상태 확인 (어떤 파일이 변경되었는지 등)
git status
.gitignore 설정
보안상 중요한 파일이나 불필요한 파일은 Git 관리 대상에서 제외해야 한다. .gitignore 파일을 생성하여 관리한다. (참고: gitignore.io)

코드 스니펫

# 모든 file.c 무시
file.c

# 최상위 폴더의 file.c만 무시
/file.c

# 모든 .c 확장자 파일 무시
*.c
4. 버전 만들기 (Add & Commit)
작업한 내용을 저장소에 '박제'하는 과정이다.

Bash

# 1. 파일을 스테이징 영역(Staging Area)에 담기
git add filename.py  # 특정 파일
git add .            # 변경된 모든 파일

# 2. 버전에 설명(메시지)을 달아 저장하기 (Commit)
git commit -m "작업 내용에 대한 코멘트"

# (옵션) Add와 Commit 동시에 하기 (새로 생성된 파일이 없을 때만 가능)
git commit -am "작업 내용 코멘트"

# 3. 커밋 이력 확인
git log
Tip: 파일의 변경 사항을 자세히 보고 싶다면 git diff를 사용한다. (종료 시 :q)

5. 시간 여행 (Reset vs Revert)
실수를 했을 때 이전 버전으로 돌아가는 방법은 크게 두 가지가 있다. 협업 시에는 Revert를 사용하는 것이 안전하다.

Reset: 시간을 되돌리기
과거의 특정 시점으로 돌아가고, 그 이후의 기록은 지워버린다.

Bash

# 돌아갈 시점의 커밋 해시를 복사해서 입력
git reset --hard (돌아갈 커밋 해시)
Revert: 취소했다는 기록 남기기
현재 버전을 그대로 두고, 특정 커밋의 내용을 '취소'하는 새로운 커밋을 생성한다. 이미 공유된(Push된) 커밋을 되돌릴 때는 반드시 Revert를 써야 한다.

Bash

# 1. 취소할 커밋의 해시를 찾아 되돌리기
git revert (되돌릴 커밋 해시)

# 2. (충돌 발생 시) 코드 수정 후 계속 진행
git add .
git revert --continue
6. 차원 나누기 (Branch)
분석 마트를 구성할 때 '실배포용', '테스트용', '개인 작업용' 등 차원을 나누어 관리할 때 사용한다.

브랜치 기본 명령어
Bash

# 브랜치 생성
git branch (신규 브랜치명)

# 브랜치 이동
git switch (이동할 브랜치명)

# 생성과 동시에 이동
git switch -c (신규 브랜치명)

# 브랜치 삭제 (병합되지 않은 내용이 있다면 -D로 강제 삭제)
git branch -d (삭제할 브랜치명)
브랜치 합치기: Merge vs Rebase
두 갈래로 나뉜 작업을 하나로 합치는 방식이다.

Merge (병합): 두 브랜치의 역사를 그대로 남기며 합친다. (안전함)

Bash

git switch main        # 메인 브랜치로 이동
git merge add-coach    # 서브 브랜치를 메인에 합침
Rebase (재배치): 브랜치의 베이스를 옮겨 한 줄로 깔끔하게 정리한다. (이미 공유된 커밋에는 사용 자제)

Bash

git switch new-teams   # 합쳐질 브랜치로 이동
git rebase main        # main의 끝에 이어 붙임

git switch main        # 다시 메인으로 와서
git merge new-teams    # Fast-forward로 마무리
🚨 충돌(Conflict)이 발생하면? 같은 파일의 같은 위치를 수정했다면 충돌이 발생한다.

git status로 충돌 파일 확인

코드를 직접 수정하여 충돌 해결

git add . → git commit (Merge) 또는 git rebase --continue (Rebase)

7. GitHub 활용 및 협업 (Remote)
로컬 저장소를 온라인 원격 저장소(Remote Repository)와 연결한다.

원격 저장소 연결 및 Push
GitHub에서 레포지토리 생성 후, 로컬 프로젝트와 연결한다.

Bash

# 원격 저장소 연결 (origin이라는 이름으로 주소 등록)
git remote add origin (https://github.com/아이디/프로젝트.git)

# 로컬의 커밋 내역을 원격으로 업로드 (Push)
# -u 옵션은 처음 한 번만 사용 (이후엔 git push만 해도 됨)
git push -u origin main
협업의 기본: Pull & Push
협업할 때는 **'일단 당겨오고(Pull), 그다음에 올린다(Push)'**를 기억해야 한다.

Bash

# 1. 원격 저장소의 최신 내역 가져오기 (Pull)
git pull

# (참고) Pull 할 때 Rebase 방식으로 가져오기 (커밋 내역 깔끔하게 유지)
git pull --rebase

# 2. 내 작업 내역 올리기 (Push)
git push
Tip: git push가 거절(rejected)당했다면? 누군가 먼저 새 버전을 올린 것이다. git pull을 먼저 해서 합친 뒤에 다시 Push 해야 한다.

원격 브랜치 다루기
Bash

# 로컬 브랜치를 원격에 올릴 때 (Upstream 설정)
git push --set-upstream origin (브랜치명)

# 원격의 브랜치 목록 확인
git branch -a

# 원격 브랜치를 로컬로 가져오기 (추적 설정)
git switch -t origin/(원격브랜치명)