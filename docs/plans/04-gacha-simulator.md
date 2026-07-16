# 04. 가챠 시뮬레이터

상태: 완료 — 백엔드 구현 + 웹 실 API 전환 및 e2e 검증 완료 (2026-07-15). **모바일(Flutter) 재확정 이후 실 API 전환 + 실기기 e2e 검증도 완료 (2026-07-16)**
선행 조건: [02. 인증/공통 기반](02-auth-foundation.md), [03. 스케줄러](03-scheduler.md) (게임/캐릭터가 게임 목록에 종속)

## 기능 요구사항 (README 기준)

- 실제 게임과 유사한 가챠 경험 제공
- 캐릭터/아이템별 가중치 적용
- '확정 천장' 시스템: 일정 횟수 이상 시도 시 확정 보상
- 사용자가 필터링한 게임에 맞춰 시뮬레이션 가능한 캐릭터 목록이 동적으로 변경
- 플랫폼: Web(React) + Mobile(Flutter)

## 작업 범위

### 백엔드

착수 시점에 웹·모바일 세션이 이미 Mock으로 구현해둔 계약(SYNC.md)이 있어, 그 계약 그대로 구현했다. 다른 파트와 달리 `Game`과의 연결은 `gameCode`가 아니라 `gameId`(games.id)를 그대로 쓴다 — 계약이 `gameId` 정수를 이미 가정하고 있었기 때문.

- [x] `Banner` 엔티티(`BannerEntity`): gameId, name, startAt, endAt, pityThreshold, rateUpRate
- [x] `Character` 엔티티(`CharacterEntity`): gameId, name, rarity, iconUrl (가중치는 게임 전역이 아니라 배너별로 다르므로 여기 두지 않음)
- [x] `BannerCharacterEntity`(배너-캐릭터 풀 join, 복합키): weight, isPickup — `pickupCharacterIds`와 배너별 가중치는 여기서 파생
- [x] 확률/천장 로직(`BannerService.pull`): 별도 `PityPolicy` 엔티티로 분리하지 않고 배너의 `pityThreshold`/`rateUpRate` 필드로 단순화 — 매 뽑기 pity+1, 천장 도달 시 확정, 확정이 아니어도 최고 등급 가중치 비율만큼 자연 확률로 최고 등급 적중, 최고 등급 적중 시 rateUpRate 확률로 픽업 우선. 자세한 규칙은 `BannerService` 클래스 주석 참고
- [x] `PullHistory`(서버 측 영속 천장 카운트)는 **미구현으로 보류** — 아래 "설계 메모" 참고
- [x] API
  - [x] `GET /api/games/{gameId}/banners`, `GET /api/banners` — 배너 목록 (`PublicBannerController`, 비로그인 접근 가능)
  - [x] `GET /api/banners/{bannerId}/characters` — 뽑기 대상 캐릭터/가중치
  - [x] `POST /api/banners/{bannerId}/pull` — 1회/10연 뽑기, 결과와 갱신된 천장 카운트 반환 (비로그인 접근 가능)
  - [x] `POST/PUT/DELETE /api/admin/banners`, `/api/admin/characters`, `PUT/DELETE /api/admin/banners/{bannerId}/characters/{characterId}` — 관리자 CRUD + 풀 구성

**밸런스 버그 수정 (2026-07-16, 사용자 리포트)**: 자연 확률은 `최고 등급 가중치 합 / 풀 전체 가중치 합`으로 계산되는데, 시드 데이터에 3성이 없어 분모가 작아서 5성 자연 확률이 19%까지 뛰는 문제가 있었다. `seed.sql`에 3성 캐릭터를 추가해 풀 합계를 ~1.0으로 맞춰 정상화(5성 1.2%, 4성 5.1%대). 상세는 SYNC.md 로그 참고, 이 계산 방식과 주의사항은 SYNC.md "가챠 API" 계약 섹션에 명시함.

**중요한 구현 이슈 (다른 파트에도 영향)**: `BannerCharacterEntity`(복합키 `@IdClass`)의 `@ManyToOne` 섀도우 연관관계(`character`/`banner`)가 Hibernate에서 항상 `null`로 조회되는 문제를 발견했다. 같은 패턴을 쓰던 `ScheduleEventEntity.game`(참조 컬럼이 `games`의 PK가 아니라 `game_code`라 프록시 생성 불가)도 실제로 테스트해보니 동일하게 깨져 있었다(파트 03에서 만들 때는 발견 못 했던 버그). 그래서 `BannerEntity`/`CharacterEntity`/`ScheduleEventEntity`/`UserGamePreferenceEntity`(파트 03, 기존 코드)의 이런 섀도우 연관관계 필드를 전부 제거하고, 서비스 레이어에서 `GameRepository`/`CharacterRepository`로 명시적으로 배치 조회해 DTO를 조립하는 방식으로 통일했다. 앞으로 새 엔티티를 추가할 때도 이 패턴(참조 컬럼이 대상 테이블의 PK가 아니거나 `@IdClass` 복합키인 경우, 지연 로딩 연관관계에 의존하지 말고 명시적으로 조회)을 따를 것.

### 프론트엔드 (Web)

- [x] `SimulatorPage`: 배너 선택 → 1회/10연 뽑기 UI, 천장 카운터(진행 게이지) 표시 — 연출 애니메이션은 추후 보강
- [x] 비로그인 시 천장 카운트를 프론트 상태로만 유지(배너별 localStorage `gacha_pity_{bannerId}`), 로그인 시 서버 값 사용은 백엔드 구현 후 연동
- 구현 메모 (2026-07-15): `src/api/gachaApi.js`(SYNC 계약 준수) + `src/api/mockGachaApi.js`(개발용 Mock, 모바일과 동일 데이터/로직). 기본은 실 API — 백엔드 없이 UI만 볼 때 `VITE_USE_MOCK_GACHA=true`로 Mock 전환 (백엔드 구현 완료에 따라 기본값 반전)
- [x] `GachaBannerManagementPage`(관리자, 2026-07-16): 배너/캐릭터 CRUD + 배너별 드랍테이블(가중치/픽업) 관리 + 등급별 자연 확률 미리보기. `src/api/gachaApi.js`의 `adminGachaApi`로 기존 admin API(`/api/admin/banners`, `/api/admin/characters`) 호출. 라우트 `/admin/gacha-banner`, SUB_ADMIN 이상만 접근

### 모바일 (Flutter)

- [x] 동일 API를 사용하는 시뮬레이터 화면 신규 작성 (`lib/screens/gacha_screen.dart`)
- [x] Riverpod으로 상태 관리(`lib/providers/gacha_providers.dart`), `http` 패키지로 API 연동(`lib/repositories/gacha_repository.dart`)
- [x] 웹과 동일한 천장/가중치 로직을 서버에 위임하므로 클라이언트는 결과 표시에 집중
- 구현 메모 (2026-07-15): 백엔드 API가 아직 없어 기본값은 Mock(`lib/repositories/mock_gacha_repository.dart`)으로 동작 — API 베이스 URL은 `--dart-define=API_BASE_URL=...`(기본 `http://localhost:8080`, 에뮬레이터는 `http://10.0.2.2:8080`), 비로그인 천장 카운트는 배너별로 `shared_preferences`에 보관(`gacha_pity_{bannerId}`)
- **실 API 전환 + 실기기 e2e 검증 완료 (2026-07-16)**: `AppConfig.useMockGacha` 기본값을 `false`로, `ApiGachaRepository`가 계약대로 정확히 구현돼 있어 코드 변경 없이 그대로 동작 확인. Flutter SDK 신규 설치(`brew install --cask flutter`) 후 실기기(Samsung SM-F966N, Android 16)에 디버그 APK 빌드/설치, `adb reverse tcp:8080`으로 로컬 Postgres 백엔드 연결 → seed.sql 실데이터(원신 배너/캐릭터)로 1회·10연 뽑기 전부 확인(천장 카운트 0→1→11 정상 증가)
  - 이 과정에서 실기기 빌드 시 버그 2건 발견·수정: (1) release용 AndroidManifest.xml에 INTERNET 권한 누락(디버그 매니페스트에만 있었음) → 메인 매니페스트에 추가, (2) Android 15+ cleartext HTTP 차단(mobile_shell과 동일 이슈) → 디버그 전용 `network_security_config.xml` 추가
  - 부가로 발견한 UI 버그: 배너 이름이 길면 `DropdownButtonFormField`가 오버플로우 — `isExpanded: true` 누락이 원인, 추가해서 해결
  - 상세는 SYNC.md의 "Flutter 가챠 화면 실기기 e2e 검증 + 버그 수정" 로그 참고

## 설계 메모

- 확률 로직은 반드시 서버에서 계산(클라이언트 조작 방지). 클라이언트는 결과만 표시 — 구현 완료
- 가중치/천장 정책은 별도 `PityPolicy` 테이블 대신 `BannerEntity`의 `pityThreshold`/`rateUpRate` 필드로 단순화했다(게임마다 배너 단위로 값만 다르게 설정하면 되므로 별도 정책 테이블 없이도 요구사항 충족)
- **로그인 유저의 서버 측 영속 천장 카운트는 이번 파트에서 구현하지 않았다.** 현재는 로그인 여부와 무관하게 항상 요청의 `currentPity`를 신뢰하고 그대로 갱신해서 돌려준다(웹/모바일이 이미 이 방식의 Mock으로 구현해뒀던 것과 동일). 로그인 유저에게 기기 간 천장 동기화가 필요해지면 `PullHistory`(또는 배너별 유저 천장 카운트 테이블)를 추가하는 별도 작업으로 진행할 것
- 배너 풀이 비어있으면(`BannerCharacterEntity` 미등록) `pull` 호출 시 예외 발생 — 관리자가 반드시 배너 생성 후 캐릭터를 등록해야 함

## DoD

- [x] 특정 게임의 배너에서 1회/10연 뽑기가 가중치에 따라 랜덤 결과를 반환 (`BannerServiceTest`)
- [x] 설정된 천장 횟수 도달 시 확정 등급(최고 rarity) 이상이 보장됨 — 200회 연속 뽑기로 천장 미초과 검증 + 천장 직전 뽑기가 확정으로 최고 등급 반환하는지 검증 (`BannerServiceTest`)
- [ ] 로그인 유저는 천장 카운트가 세션 간 유지됨 — 위 설계 메모대로 이번 파트 범위에서 제외, 별도 작업으로 이월
- [x] 웹에서 동일 API로 동작 확인 — H2 인메모리로 실서버 기동 후 웹이 쓰는 전 엔드포인트 e2e 통과(계약 필드/천장/리셋/확정, SYNC.md frontend #4 로그 참고)
- [x] 모바일(Flutter)에서 동일 API로 동작 확인 — 실기기(Android 16)로 1회/10연 뽑기 e2e 검증 완료, 2026-07-16 (모바일 전략이 다시 Flutter 네이티브로 재확정된 이후)
