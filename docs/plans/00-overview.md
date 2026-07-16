# Gacha Scheduler 작업 계획 개요

작성일: 2026-07-15
목적: 스캐폴딩 단계인 프로젝트를 실제 기능이 동작하는 상태까지 끌어올리기 위한 파트별 작업 계획.

## 현재 상태 스냅샷 (2026-07-15 기준)

| 영역 | 스택 | 상태 |
|---|---|---|
| backend | Spring Boot 3.5.5 (Java 21) + JPA + Security + PostgreSQL(AWS RDS) | `com.gacha.gachascheduler` 패키지에 User/Game/UserGamePreference/ScheduleEvent/Banner/Character/Channel/Post/Comment/PendingSchedule/ServerCostSetting 엔티티 + 인증(JWT)/게임/스케줄/가챠/게시판/유저관리 API 완료(01~06 파트), 파트 07 중 iCal 구독·AI 스케줄 승인 워크플로 완료, 파트 08 중 서버비 프로그레스 바 API 완료 |
| frontend | React 19 + Vite + react-router-dom 7 + `@react-oauth/google` | 파트 02~06 화면 전부 구현 완료(스텁 페이지 없음) — 스케줄러/시뮬레이터/게임관리/게시판/글쓰기/채널관리/기록/프로필/유저관리. H2 서버로 전체 e2e 검증 완료 |
| frontend_mobile | Flutter + Riverpod + Firebase(Auth/Firestore/Crashlytics/Analytics/Messaging) | **🔴 재확정 — 유일한 모바일 앱(2026-07-16)**. 가챠 시뮬레이터 화면 실 API 전환 + 실기기 e2e 검증 완료. 나머지 화면(스케줄러/게시판/로그인·프로필/유저관리)은 신규 제작 필요 |
| mobile_shell | Capacitor 7 + Android(WebView) | **🔴 중단(deprecated, 2026-07-16)** — 코드는 보관, 신규 작업 없음 |

> **모바일 전략 변경 1차(2026-07-15)**: 모바일은 별도 화면(Flutter) 대신 **하이브리드(Capacitor 셸 + 반응형 웹)** 로 전환했었음.
>
> **모바일 전략 변경 2차(2026-07-16, 사용자 결정, 번복)**: "웹뷰로 웹을 감싸는 대신 앱이 API를 직접 호출했어야 했다"는 판단으로 **다시 Flutter 네이티브로 최종 확정**. `mobile_shell`은 중단, `frontend_mobile`이 유일한 모바일 앱. 이후 파트의 "모바일 연동" 항목은 Flutter 네이티브 화면 구현을 의미함(반응형 웹 대응 아님). 상세: [../SYNC.md](../SYNC.md) 작업 로그, [../HANDOVER.md](../HANDOVER.md)의 "모바일 전략" 섹션

## 진행 순서 (파트)

선행 조건인 보안 조치를 먼저 처리한 뒤, 인증 공통 기반 → 기능 3종을 순서대로 엔드투엔드(백엔드→프론트→모바일)로 완성한다. 각 파트는 이전 파트에 의존하므로 순서를 건너뛰지 않는다.

1. **[01. 보안 강화](01-security-hardening.md)** — DB 자격증명 하드코딩 제거, 환경변수 전환, 자격증명 로테이션 안내 (선행 필수)
2. **[02. 인증/공통 기반](02-auth-foundation.md)** — Google OAuth 로그인 백엔드 구현, JWT 발급, User 엔티티, 공통 응답/예외 처리
3. **[03. 게임 업데이트 스케줄러](03-scheduler.md)** — Game/Schedule 엔티티, 간트 차트 API, 개인화 필터링, `SchedulerPage`/`GameManagementPage`
4. **[04. 가챠 시뮬레이터](04-gacha-simulator.md)** — 확률/천장 로직, Banner/Character 엔티티, `SimulatorPage`(웹) + Flutter 시뮬레이터 화면
5. **[05. 공략 게시판](05-strategy-board.md)** — Channel/Post/Comment 엔티티, 글쓰기 템플릿, `StrategyBoardPage`/`ChannelManagementPage`/`NoticeCreationPage`
6. **[06. 관리자/유저 관리](06-admin-user-management.md)** — `UserManagementPage`/`UserProfilePage`/`HistoryPage`, 권한(Role) 기반 접근 제어
7. **[07. 확장 기능 기획](07-extended-features-planning.md)** — 몬테카를로 가챠 계산기, iCal 구독 연동, 공유 카드 생성, AI 스케줄 파이프라인 기획
8. **[08. 수익화 전략](08-monetization-strategy.md)** — 네이티브 광고(Phase 1~2) 및 커뮤니티 포인트 연동 보상형 광고(Phase 3) 기획
9. **[09. 배포](09-deployment.md)** — NCP Server+systemd+nginx+Cloud DB for PostgreSQL 배포 아키텍처(AWS→NCP로 전환, 2026-07-16), 운영 설정/systemd/nginx 템플릿, 프로비저닝 런북
10. **[10. 푸시 알림 시스템](10-push-notifications.md)** — FCM 서버 연동, 스케줄/게시글 구독 알림 푸시, 모바일 앱 푸시 수신 로직
11. **[11. 텍스트 RPG 미니게임](11-text-rpg-minigame.md)** — 가상 인벤토리, 방치형 원정대 파견, 커뮤니티 활동 포인트 기반 스탯 성장 기획
12. **[12. 가챠 확률 제어 UI]** (06번 문서 Phase 2 통합) — 관리자가 확률을 실시간으로 확인하고 계산할 수 있는 UI
13. **[13. 캐릭터 육성 재화 계산기](13-build-planner.md)** — 구간별 소모 재화 계산 및 공략 게시판 위젯 연동 기획

## 원칙

- **여러 AI 세션이 병렬로 작업 중** — 작업 시작 전 [../SYNC.md](../SYNC.md)를 읽고, 종료 시 작업 로그를 남긴다. 영역 간 인터페이스(API 계약 등) 변경은 반드시 SYNC.md에 기록한다.
- 각 파트는 "백엔드 엔티티/API → 프론트 연동 → (해당 시) 모바일 연동" 순으로 세로로 완결시킨다. 여러 파트를 동시에 벌리지 않는다. (2차 모바일 전략 변경 이후 "모바일 연동" = `frontend_mobile`(Flutter)에 해당 화면을 네이티브로 구현하고 실 API 연결하는 것)
- 파트 완료 기준(DoD)은 각 문서 하단에 체크리스트로 명시한다.
- DB 스키마는 `spring.jpa.hibernate.ddl-auto=update`로 자동 반영되는 현재 설정을 유지하되, 엔티티 설계는 각 파트 문서에 먼저 기록한 뒤 구현한다.
- 시크릿(비밀번호, 클라이언트 시크릿, JWT 서명 키)은 코드/문서 어디에도 평문으로 남기지 않는다.

## 파트별 상태 추적

| 파트 | 상태 |
|---|---|
| 01. 보안 강화 | 코드 변경 완료 / 사용자 조치 대기 |
| 02. 인증/공통 기반 | 백엔드·프론트 연동 완료 / 실동작(e2e) 검증 대기 |
| 03. 스케줄러 | 백엔드·프론트(웹) 완료 / 실동작(e2e) 검증 대기 |
| 04. 가챠 시뮬레이터 | 완료 — 웹 실 API 전환 + e2e 검증 완료. **Flutter(모바일)도 실 API 전환 + 실기기 e2e 검증 완료(2026-07-16)**. **5성 확률 밸런스 버그 수정 + 배너/드랍테이블 관리자 페이지 신규(2026-07-16)** (Google 로그인 e2e만 사용자 확인 대기) |
| 05. 공략 게시판 | 완료 — 백엔드+프론트, H2 e2e 검증 완료 |
| 06. 관리자/유저 관리 | 완료 — 백엔드+프론트, H2 e2e 검증 완료. **Phase 2: 가챠 확률 제어 UI + 공지사항/팝업 배너 관리 완료(2026-07-16)** |
| 07. 확장 기능 | 백엔드 일부 완료 — iCal 구독 API 완료, AI 스케줄 승인 워크플로 완료(실 스크래퍼/LLM 파서는 제외). 계산기/공유카드는 프론트 전용이라 대기 |
| 08. 수익화 전략 | 백엔드 일부 완료 — 서버비 프로그레스 바 API 완료. 광고/포인트 시스템은 상세 기획 필요해 대기 |
| 09. 배포 | **NCP 실제 프로비저닝 + 백엔드 배포 완료(2026-07-16)** — `http://101.79.26.52`에서 API 응답 중. 도메인/TLS는 아직. 배포 중 회원가입 계정 탈취 취약점 발견(미해결, HANDOVER.md 17번 참고) |

각 파트 작업이 끝나면 이 표와 해당 문서의 상태를 갱신한다.
