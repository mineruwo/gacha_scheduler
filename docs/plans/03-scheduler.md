# 03. 게임 업데이트 스케줄러

상태: 백엔드·프론트(웹) 완료 / 실동작(e2e) 검증 대기 — 모바일은 범위 아님
선행 조건: [02. 인증/공통 기반](02-auth-foundation.md)

## 기능 요구사항 (README 기준)

- 서브컬쳐 게임의 업데이트/이벤트 일정을 간트 차트 형식으로 제공
- 로그인 사용자는 관심 게임을 선택해 자신만의 스케줄 뷰로 필터링 가능
- 필터링 정보는 DB에 저장되어 기기 간 동일하게 유지

## 작업 범위

### 백엔드

착수 시점에 `Game` 엔티티(`GameEntity`)와 관리자 CRUD(`GameController` → `/api/admin/games`), `UserGamePreferenceEntity`/`Service`/`Repository`(다대다 매핑)가 이미 구현되어 있었다. `Game`은 `id` 대신 `gameCode`(unique)를 다른 도메인과의 연결 키로 사용하는 기존 컨벤션을 그대로 따랐다.

- [x] `Game` 엔티티 — 기존 `GameEntity` 재사용 (title, gameCode, hasGacha, hasPass, canManageSchedule 등)
- [x] `ScheduleEvent` 엔티티 신규 추가 (`ScheduleEventEntity`: id, gameCode, title, category(`UPDATE`/`EVENT`/`MAINTENANCE`), startAt, endAt, description)
- [x] `UserGameFilter` — 기존 `UserGamePreferenceEntity`(userId + gameCode 복합키) 재사용, 전체 교체용 `UserGamePreferenceService.replacePreferences` 추가
- [x] API
  - [x] `GET /api/games` — 전체 게임 목록 (신규 `PublicGameController`, 비로그인 접근 가능)
  - [x] `GET /api/schedules?gameCodes=...&from=&to=` — 기간/게임 필터 조회 (계획 초안의 `gameIds`를 도메인 키인 `gameCodes`로 변경, 비로그인 접근 가능)
  - [x] `GET /api/users/me/game-preferences` / `PUT /api/users/me/game-preferences` — 개인화 필터 조회/전체교체 (계획 초안의 `game-filters`에서 기존 서비스명에 맞춰 `game-preferences`로 명명, 로그인 필요)
  - [x] `POST/PUT/DELETE /api/admin/schedules` — 관리자 CRUD (`ScheduleEventController`). `/api/admin/games` CRUD는 기존 `GameController`가 이미 보유
- [x] 관리자 권한 검증 — 기존 `Role`(`SUB_ADMIN`/`MAIN_ADMIN`) + `@PreAuthorize` 사용 (02 파트에서 `@EnableMethodSecurity` 추가로 실제 동작하게 됨)
- [x] **버그 수정 (파트 04 작업 중 발견)**: `ScheduleEventEntity.game`(지연 로딩 섀도우 연관관계)이 항상 `null`을 반환해 응답의 `gameTitle`이 계속 빈 값이었다. `game_code`가 `games`의 PK가 아니라 Hibernate가 프록시를 만들지 못하는 게 원인. 연관관계 필드를 제거하고 `ScheduleEventController`가 `GameService`로 gameCode를 배치 조회해 명시적으로 채우도록 수정 — API 응답 형태(JSON 필드)는 그대로이므로 프론트 수정 불필요, 값만 정상적으로 채워짐

### 프론트엔드

- [x] `SchedulerPage`: 간트 차트 렌더링 — 의존성 추가 없이 자체 구현(CSS 절대 배치, 월 눈금/오늘 표시선/카테고리 색상), 게임 필터 UI(체크박스), 기간 이동(◀▶/오늘, 기본 -1개월~+3개월)
- [x] 로그인 상태에 따라 필터 저장 분기 — 비로그인: localStorage(`scheduler_game_filter`), 로그인: `PUT /api/users/me/game-preferences`
- [x] `GameManagementPage` (관리자): 게임 등록/수정/삭제 + 일정 등록/수정/삭제 폼 (`src/api/schedulerApi.js`의 `adminApi` 사용)
- 구현 메모 (2026-07-15): 일정은 범위 전체를 한 번에 받아 클라이언트에서 게임 필터링(토글 시 재요청 없음). `scheduler` 라우트의 `ProtectedRoute`를 제거해 비로그인 열람 허용(DoD 반영). 일정 조회 범위는 관리 페이지 기준 -1개월~+6개월

### 모바일

- 스케줄러는 README상 "주요 플랫폼: Web/React"로 명시되어 있어 모바일 구현 범위 아님 (필요 시 별도 파트로 분리)

## 설계 메모

- 간트 차트 라이브러리는 프론트 작업 착수 시 라이선스/번들 크기 비교 후 확정
- 비로그인 사용자의 필터는 프론트 로컬 상태로만 유지, 로그인 시 서버 값과 병합
- `GET /api/schedules`의 `from`/`to`를 생략하면 서버가 기본값(현재 기준 -1개월 ~ +3개월)으로 채운다
- 일정 조회는 겹침(overlap) 기준으로 필터링한다: `startAt <= to AND (endAt IS NULL OR endAt >= from)`

## DoD

- [x] 백엔드: 관리자 API로 게임/일정을 등록하면 공개 조회 API(`GET /api/schedules`)에 즉시 반영됨을 서비스 테스트로 확인 (`ScheduleEventServiceTest`)
- [x] 백엔드: 로그인 유저가 관심 게임 필터를 저장(`PUT /api/users/me/game-preferences`)하면 재조회 시 동일하게 유지됨을 서비스 테스트로 확인 (`UserGamePreferenceServiceTest`)
- [x] 백엔드: `GET /api/games`, `GET /api/schedules`는 토큰 없이도 200 반환 확인 (`SecurityConfigTest`)
- [ ] 프론트: 관리자 페이지에서 게임/일정을 등록하면 스케줄러 페이지에 즉시 반영 — 구현 완료, 실동작 확인은 백엔드 기동 필요(로컬 DB 설정/Java 21 부재로 미검증)
- [ ] 프론트: 비로그인 상태에서도 전체 일정 열람 가능 (웹 UI 기준) — 라우트 보호 제거로 구현 완료, 실동작 확인 대기
