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
