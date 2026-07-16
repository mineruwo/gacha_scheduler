# 01. 보안 강화

상태: 코드 변경 완료 / 사용자 조치 대기 (RDS 비밀번호 로테이션 등)
선행 조건: 없음 (모든 파트의 선행 조건)

## 배경

`backend/backend_gacha/src/main/resources/application.properties`에 AWS RDS PostgreSQL 접속 정보(호스트, 계정, 비밀번호)가 평문으로 커밋되어 있고, 이 저장소는 GitHub(`mineruwo/gacha_scheduler`)에 푸시되어 있다. 과거 `frontend/.env`도 한 차례 커밋되었다가 삭제된 이력이 있다(커밋 `acc4eaa`, `5d39ccf`).

## 작업 범위

### 코드 변경 (Claude가 수행)

- [ ] `application.properties`의 `spring.datasource.username`, `spring.datasource.password`, `spring.datasource.url`을 환경변수 참조(`${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`)로 교체
- [ ] 로컬 개발용 `application-local.properties.example` (또는 `.env.example`) 템플릿 추가 — 실제 값은 없이 키 이름만
- [ ] 루트 `.gitignore`에 `application-local.properties`, `*.env`, `.env*` (예외: `.env.example`) 패턴 추가
- [ ] `frontend/.env.example` 추가 (`VITE_API_BASE_URL`, `VITE_GOOGLE_CLIENT_ID` 키 이름만)
- [ ] README에 로컬 실행 시 환경변수 설정 방법 안내 추가

### 사용자가 직접 수행해야 하는 작업 (Claude는 대행 불가)

시스템/보안 설정 변경 및 인프라 조작은 대행하지 않는 원칙에 따라 아래는 사용자가 직접 처리해야 한다.

- [ ] **AWS RDS 마스터 비밀번호 로테이션** — 이미 공개 저장소 히스토리에 노출되었으므로 반드시 교체. AWS 콘솔 → RDS → `gacha-database-1` → 수정에서 변경
- [ ] **Google OAuth Client Secret 재발급 여부 검토** — `.env` 노출 이력이 있어 Google Cloud Console에서 클라이언트 보안 검토 권장
- [ ] **Git 히스토리 정리(선택)** — `git filter-repo` 또는 BFG로 과거 커밋의 비밀번호/시크릿 제거. 히스토리 재작성은 파괴적 작업이라 브랜치 백업 후 진행 권장. 이미 GitHub에 반영된 이력은 캐시/포크에 남을 수 있으므로 로테이션이 근본 대책이며 히스토리 정리는 보조 수단
- [ ] 로컬 환경변수 실제 값 설정 (`.env`, `application-local.properties` 등 — gitignore 대상이므로 직접 작성)

## DoD (완료 기준)

- [ ] 저장소 내 어떤 파일에도 실제 비밀번호/시크릿 값이 평문으로 존재하지 않음
- [ ] `application.properties`가 환경변수 미설정 시에도 애플리케이션 기동 실패 메시지가 명확함(값 누락을 바로 알 수 있음)
- [ ] 사용자가 RDS 비밀번호를 교체했음을 확인

## 로컬 개발 DB (2026-07-16 추가)

프론트/모바일이 "실제로 붙어서 검증할" 지속적인 백엔드가 필요해져서, RDS 대신 **로컬 PostgreSQL**을 우선 구성했다(RDS 자격증명은 여전히 사용자만 다룰 수 있는 영역이라 세션이 대행 불가 — 위 미해결 항목 그대로).

- `brew install postgresql@16` 설치, DB `gacha_scheduler` + 계정 `gacha_app` 생성, `backend/backend_gacha/src/main/resources/application-local.properties`(gitignore 대상)에 로컬 접속 정보 채워둠. `GOOGLE_CLIENT_ID`는 아직 플레이스홀더라 구글 로그인은 여전히 안 됨(공개 API인 게임/일정/채널/가챠는 문제없이 동작)
- `seed.sql`을 로컬 DB에 적용해 원신/스타레일 게임·일정·배너·캐릭터·테스트 유저 시드 완료
- **RDS로 전환할 때**: 코드 변경 없이 `application-local.properties`의 `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`만 RDS 값으로 교체하면 됨(드라이버/dialect 모두 이미 PostgreSQL로 고정돼 있어 로컬 Postgres와 RDS는 접속 정보만 다름)
- **재시작 방법**: `brew services start postgresql@16`이 이 세션(샌드박스) 환경에서는 launchd 문제로 실패해서 지금은 `postgres -D /opt/homebrew/var/postgresql@16`를 직접 백그라운드로 띄운 상태 — 사용자의 실제 터미널에서는 `brew services start postgresql@16`이 정상 동작할 가능성이 높으니 재부팅 후 자동 시작을 원하면 그쪽으로 시도해볼 것. 백엔드 서버는 `cd backend/backend_gacha && ./gradlew bootRun`
