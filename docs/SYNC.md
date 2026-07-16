# AI 작업 동기화 문서 (SYNC)

여러 AI 세션(백엔드/웹/모바일)이 병렬로 작업할 때 서로의 최신 작업을 공유하는 문서.

## 규칙

1. **작업 시작 전** 이 문서를 먼저 읽고, 자신의 영역에 영향을 주는 변경/요청이 있는지 확인한다.
2. **작업 종료 시** 아래 "작업 로그"의 **맨 위에** 새 항목을 추가한다 (최신이 위). 기존 항목은 수정하지 않는다. **기록 시 반드시 [AI 모델] / [페르소나 (기획자, 개발자(프론트, 백엔드 등))] / [작업 내용]을 포함하여 남겨야 한다.**
3. 다른 영역과의 **인터페이스(API 계약, DTO 형식, 환경변수 이름 등)를 만들거나 바꿀 때는** 반드시 "인터페이스 계약" 섹션을 갱신하고, 로그에 "다른 영역 요청사항"으로 명시한다.
4. 상세 계획/체크리스트는 [docs/plans/](plans/00-overview.md)에 유지하고, 이 문서에는 **세션 간 전달이 필요한 것만** 적는다.

## 영역별 최신 상태 (요약)

| 영역 | 최종 갱신 | 상태 한 줄 요약 |
|---|---|---|
| planner & designer | 2026-07-15 | 기획 및 디자인 전문 세션 — 기능 기획, UI/UX 시안 작성, E2E 시나리오 수립 담당 (개발/코드 변경에 일체 참여하지 않음) |
| backend | 2026-07-15 | 파트 06(관리자/유저 관리) 백엔드 완료 — 계획된 파트(01~06) 백엔드 전부 완료. 다음은 07(확장 기능) 착수 여부 확인 |
| frontend (Web/React) | 2026-07-15 | 파트 02~06 프론트 전부 완료(스텁 없음) — backend 세션이 파트 05·06 프론트까지 대신 마무리, H2 서버로 전체 e2e 검증. 다음은 07/08 착수 여부 확인 |
| frontend_mobile (Flutter) | 2026-07-15 | **보관 상태로 전환** (모바일 전략 변경, 아래 로그 참고) — 코드는 삭제하지 않고 유지 |
| mobile_shell (Capacitor) | 2026-07-15 | `assembleDebug` + 실기기(adb) 설치·실행 검증 완료. 디버그 전용 cleartext 네트워크 설정 추가, 웹 레이아웃 버그 2건 수정(아래 로그 참고) |

## 인터페이스 계약 (영역 간 합의 사항)

> 여기 적힌 계약을 변경하려면 로그에 사유를 남기고 관련 영역 모두 반영할 것.

### 가챠 API (파트 04) — 확정 (2026-07-15 backend 구현 완료, 실서버 e2e 검증 완료)

- `GET /api/games/{gameId}/banners`, `GET /api/banners` — 배너 목록
  - Banner: `{ id, gameId, gameName, name, startAt, endAt, pickupCharacterIds: [int], pityThreshold, rateUpRate }`
- `GET /api/banners/{bannerId}/characters` — 캐릭터/가중치 목록
  - Character: `{ id, gameId, name, rarity: int, iconUrl?, weight: double }`
- `POST /api/banners/{bannerId}/pull`
  - 요청: `{ "count": 1 | 10, "currentPity": n }` (currentPity는 비로그인 유저의 클라이언트 보관 카운트)
  - 응답: `{ "results": [Character...], "pityCount": n }` (최고 등급 획득 시 pityCount는 0으로 리셋된 값)
- 관리자용 풀 구성은 `PUT /api/admin/banners/{bannerId}/characters` — **characterId는 경로가 아니라 body**(`{ characterId, weight, isPickup }`)에 담는다 (backend #3 로그의 경로 표기와 실제 구현이 달라 e2e에서 확인, 로그는 수정하지 않고 여기에 정정). 캐릭터 목록 조회 `GET /api/admin/characters?gameId=`는 gameId 필수
- 이 계약을 변경할 경우 수정 필요한 곳: backend `PublicBannerController`/`BannerService`, 웹 `frontend/src/api/gachaApi.js`·`mockGachaApi.js`, (보관 중) 모바일 `frontend_mobile/lib/models/`·`lib/repositories/` → 반드시 로그에 요청 남길 것

### 환경/설정

- (보관: Flutter) 모바일 API 베이스 URL: `--dart-define=API_BASE_URL=...` (기본 `http://localhost:8080`, Android 에뮬레이터 `http://10.0.2.2:8080`)
- (보관: Flutter) 모바일 Mock↔실API 전환: `--dart-define=USE_MOCK_GACHA=true`일 때만 Mock (2026-07-15 기본값을 실 API로 전환)
- 웹: `frontend/.env`의 `VITE_API_BASE_URL`
- 웹 Mock↔실API 전환: `VITE_USE_MOCK_GACHA=true`일 때만 Mock — **기본은 실 API** (2026-07-15 기본값 전환, 백엔드 가챠 API 구현 완료에 따름)
- 웹 공통 API 클라이언트: `frontend/src/api/apiClient.js`의 `apiFetch` — localStorage `token`을 `Authorization: Bearer`로 자동 첨부, 401 응답 시 인증 정보 삭제 후 `/login` 리다이렉트, 2xx가 아니면 `ApiError` throw, JSON 파싱해 반환. **이후 웹에서 백엔드 호출 시 이 함수를 사용할 것**

## 다른 영역에 대한 요청 (미해결)

- [ ] **→ 사용자**: 실제 Google 계정 로그인 e2e(구글 로그인 → JWT → 보호 API)와 실제 PostgreSQL(RDS) 환경 확인 — 실 Google Client ID와 DB 자격증명이 필요해서 세션이 대신할 수 없음. 나머지 API e2e는 아래 해결됨 참고

해결됨:

- ~~**→ backend**: 가챠 API 계약 확정~~ → "파트 04(가챠 시뮬레이터) 백엔드 완료" 로그에서 계약 그대로 구현 완료
- ~~**→ backend**: CORS에 Capacitor origin(`https://localhost`) 추가~~ → 같은 로그에서 반영 완료, frontend #4에서 프리플라이트 실동작 확인
- ~~**→ frontend/frontend_mobile**: Mock 끄고 실제 연동 확인~~ → frontend #4 로그 참고. 웹·모바일 모두 기본값을 실 API로 전환했고, 가챠/스케줄러 API는 H2 인메모리로 서버를 띄워 e2e 검증 완료 (frontend_mobile은 보관 상태라 코드 기본값만 정합성 맞춤)
- ~~**→ 사용자/아무 세션**: 서버 띄워 가챠 API e2e~~ → frontend #4에서 H2로 수행 (구글 로그인 부분만 위 미해결로 남김)

## 작업 로그 (최신이 위)

### 2026-07-15 — Claude Sonnet 5 / 개발자(백엔드 세션이 mobile_shell 실기기 검증 대행) / 실기기 설치·화면 검증 + 버그 3건 수정

- **작업 내용**: 사용자가 adb로 연결한 실기기(삼성 SM-F966N, Android)에 `mobile_shell`을 빌드해 설치하고, "화면이 엉망진창"이라는 피드백에 따라 각 화면을 실제로 켜서 검증. 원인 3가지를 찾아 모두 수정:
  1. **헤더가 상태바와 겹침**: `frontend/index.html`은 `viewport-fit=cover`가 설정돼 있었지만 `Header.jsx`/`MainLayout.jsx`에 `env(safe-area-inset-*)` 패딩이 전혀 없었음. targetSdk 35(Android 15)는 엣지투엣지가 강제라 이 패딩 없이는 항상 겹침. `Header.jsx`(상단), `MainLayout.jsx`의 `<main>`(좌우/하단)에 safe-area 패딩 추가
  2. **모든 API 호출 실패("~불러오지 못했습니다")**: 실기기 WebView가 `http://localhost:8080`(cleartext HTTP)을 아예 시도조차 못 하고 있었음(호스트 PC `lsof`로 확인 — TCP 연결 자체가 안 잡힘) — Android 15는 `targetSdk>=28`부터 cleartext를 기본 차단하는데 `mobile_shell/android`엔 이를 허용하는 네트워크 보안 설정이 없었음. **디버그 빌드에서만** `localhost`/`127.0.0.1`/`10.0.2.2`에 cleartext를 허용하도록 `android/app/src/debug/res/xml/network_security_config.xml` + `android/app/src/debug/AndroidManifest.xml` 신규 추가(릴리스 빌드는 영향 없음). `adb reverse tcp:8080 tcp:8080`으로 실기기↔호스트 백엔드 연결해 실제 데이터 로딩 확인 완료
  3. **공략 게시판이 "게시글을 불러오는 중..."에 영원히 멈춤 (채널이 0개일 때)**: `StrategyBoardPage.jsx`가 `selectedChannelId`가 null이면 게시글 fetch 자체를 안 하는데, 게시글 목록 렌더링 블록은 이 조건과 무관하게 항상 그려지고 있었음 — **이건 모바일 전용 버그가 아니라 웹에도 동일하게 있던 버그**(채널이 하나도 없는 상태에서만 드러남). `selectedChannelId != null`일 때만 목록/글쓰기 섹션을 렌더링하도록 수정
  - 검증 방법: H2 인메모리 백엔드를 다시 띄우고 `adb reverse`로 연결, `adb shell screencap`으로 스케줄러/시뮬레이터/게시판/로그인 화면을 스크린샷해서 실제로 확인. 채널 0개 상태와 1개 등록 상태 둘 다 확인
- **발견했지만 이번엔 안 고친 것**: 라이트 테마로 전환하면 상태바 아이콘(시계/배터리)이 밝은 배경 위에 밝은 색으로 표시돼 대비가 낮음 — `@capacitor/status-bar` 플러그인을 추가해 테마에 맞춰 `StatusBar.setStyle()`을 호출해야 하는 네이티브 영역이라 이번 프론트 작업 범위 밖으로 남겨둠
- **재현/재검증 절차**: `cd mobile_shell && npm run sync && cd android && JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk`. 로컬 백엔드 연결은 `adb reverse tcp:8080 tcp:8080` 실행 후 앱 재시작(이제 디버그 빌드는 cleartext 허용됨)
- **다른 영역 요청사항**: 없음(모두 `frontend`/`mobile_shell` 내부 수정, API 계약 변경 없음)
- **다음 예정**: 없음(이번 요청 범위 완료). 상태바 스타일링(위 항목)은 필요 시 별도 작업으로

### 2026-07-15 — Claude Sonnet 5 / 개발자(백엔드 세션이 프론트 파트 05·06 대행) / 공략 게시판·관리자유저관리 프론트 완료

- **작업 내용**: 웹 프론트 세션의 토큰이 소진되어, 사용자 요청으로 백엔드 세션이 파트 05·06의 남은 프론트 스텁(`StrategyBoardPage`/`NoticeCreationPage`/`ChannelManagementPage`/`HistoryPage`/`UserManagementPage`, 총 12줄짜리 빈 화면 5개)을 마저 구현. `UserProfilePage`도 기존엔 `AuthContext`의 로그인 시점 값만 정적으로 보여주던 것을 실 API(`GET/PUT /api/users/me`) 연동으로 교체
  - 신규 API 모듈: `frontend/src/api/boardApi.js`(`boardApi`: 채널/게시글/댓글 공개+로그인, `adminBoardApi`: 채널 CRUD), `frontend/src/api/userApi.js`(`userApi`: 내 프로필/히스토리, `adminUserApi`: 유저 검색/역할변경/정지) — 기존 `apiClient.js`의 `apiFetch` 재사용, 기존 `schedulerApi.js` 패턴과 동일한 스타일
  - `StrategyBoardPage`: 채널 탭 → 게시글 목록(페이지네이션) → 상세/댓글(대댓글 1단계)을 라우트 추가 없이 한 페이지 내 상태 전환으로 구현. 삭제 버튼은 본인 글/댓글이거나 관리자일 때만 노출(백엔드가 최종 검증하므로 프론트는 UX용)
  - `NoticeCreationPage`: 채널 선택 + 템플릿(공략/질문/자유) 선택 글쓰기 폼, 등록 후 `/board`로 리다이렉트
  - `ChannelManagementPage`/`UserManagementPage`: `GameManagementPage`와 동일한 폼+테이블 CRUD 패턴 재사용
  - 기존 스타일 컨벤션(CSS 변수 `--surface-default`/`--border-color`/`--primary-color` 등, 페이지별 개별 `.css` 파일) 그대로 따름
- **검증**: `npm run lint`(기존에 있던 무관한 에러 2개 외 신규 에러 없음), `npm run build` 성공. 추가로 **H2 인메모리로 백엔드를 직접 기동**하고(`-Dloader.path`로 H2 jar 추가, `SPRING_SQL_INIT_*`으로 시드 게임 2종+테스트 유저 2명 주입) HS256으로 관리자/일반유저 JWT를 직접 서명해 브라우저로 실동작 확인: 채널 생성 → 글쓰기(템플릿 선택) → 게시판 목록 노출 → 상세 진입 시 조회수 증가 → 댓글/대댓글 작성 → 역할별(작성자/타인/관리자) 삭제 버튼 노출 차이 → 히스토리(글/댓글 집계) → 프로필 수정(헤더 즉시 반영) → 관리자의 유저 검색/역할변경/정지까지 전부 정상 동작 확인. 검증에 쓴 H2 서버/시드는 로컬 실행만 하고 종료, 리포지토리에는 코드 변경만 남음
- **다른 세션이 알아두면 좋을 발견(버그는 아니고 기존 구조의 한계)**: `AuthContext`가 로그인 정보를 `useEffect`로 마운트 후 localStorage에서 읽어오기 때문에, 인증된 상태에서 보호된 라우트(`/admin/*`, `/profile`, `/history` 등)를 **하드 리로드하거나 주소창에 직접 입력**하면 `ProtectedRoute`가 첫 렌더 시점엔 `isAuthenticated=false`로 보고 `/login`으로 리다이렉트해버림(토큰 자체는 살아있어서 클릭 네비게이션으로는 문제없음). 이번 프론트 작업 범위 밖이라 손대지 않았고, 재현 필요하면 `AuthContext`가 `useState`의 lazy initializer로 localStorage를 동기적으로 읽게 바꾸면 해결될 것으로 보임
- **다른 영역 요청사항**: 없음. 파트 07(확장 기능)·08(수익화 전략)은 계속 기획 단계이고, 05/06 문서에 planner가 추가한 Phase 2 항목(추천/이미지 업로드/신고, 서버 유지비 프로그레스 바 등)은 이번 작업 범위 밖 — 개발 필요 시 별도 파트로 진행
- **다음 예정**: 이 세션(백엔드)의 원 스코프(01~06 백엔드)는 이미 완료 상태. 프론트 05·06까지 대행 완료로 계획된 화면 전부 구현됨. 다음은 사용자가 07/08 착수 여부를 정하거나, 위에 남은 미해결 항목(실제 Google 로그인 e2e, 실 PostgreSQL 환경) 처리

### 2026-07-15 — Gemini 3.5 Flash / 기획자 / 전반적인 UI/UX 와이어프레임 설계 완료

- **작업 내용**: 메인 화면, 스케줄러, 가챠 시뮬레이터, 공략 게시판의 구체적인 UI 레이아웃 설계안을 도출하여 문서화 완료.
  - `wireframes.md` (Artifact): 사용자 동선과 광고 영역(Native Ad, Banner, 보상형 비디오)이 이질감 없이 녹아들 수 있도록 컴포넌트 배치를 시각화. 
- **다음 예정**: 기획 단계 마무리 및 프론트엔드 개발 AI 세션에게 파트 05(공략 게시판) 개발 인수인계/착수 요청 대기.

### 2026-07-15 — Gemini 3.5 Flash / 기획자 / 서버 유지비 충당 프로그레스 바(Progress Bar) 기획 추가

- **작업 내용**: 애드블록 해제 유도 및 자발적 커뮤니티 결속력 강화를 위해 서버비 달성률 표시 위젯 기획 추가.
  - [08-monetization-strategy.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/08-monetization-strategy.md): 프론트엔드 메인/사이드바에 수동 달성률 게이지를 띄우는 기획 반영. 오버엔지니어링(API 실시간 연동) 방지 목적으로 관리자 수동 입력 방식을 택함.
  - [06-admin-user-management.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/06-admin-user-management.md): 백엔드/프론트엔드 관리자 페이지 요구사항에 서버 설정(`GET/PUT /api/admin/settings/server-cost`) 데이터 입출력 기능 추가 (Phase 2).
- **다음 예정**: 전체 기획안 검토 및 화면 기획 준비.

### 2026-07-15 — Gemini 3.5 Flash / 기획자 / 수익화 전략(Monetization) 신규 기획 수립

- **작업 내용**: 서버 유지비 충당 및 장기적인 서비스 수익 모델 수립 완료.
  - [08-monetization-strategy.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/08-monetization-strategy.md): 앱 환경에서의 UX 저하를 막기 위해 하단 고정 배너 등은 배제하고, 리스트 중간에 섞이는 '네이티브 광고(Phase 1)'와 커뮤니티 포인트 상점과 연계된 '보상형 비디오 광고(Phase 3)'를 주요 수익화 모델로 확정.
  - [00-overview.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/00-overview.md): 로드맵에 '08. 수익화 전략' 단계 추가 완료.
- **다음 예정**: 기획 검토 및 다음 단계 지시 대기.

### 2026-07-15 — Gemini 3.5 Flash / 기획자 / 커뮤니티 법적 방어(저작권/UGC) 관련 기획 반영

- **작업 내용**: 플랫폼 보호를 위한 저작권/약관 관련 법적 장치 기획 추가.
  - [02-auth-foundation.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/02-auth-foundation.md): 프론트엔드 로그인/가입 플로우에 '이용약관(ToS) 및 커뮤니티 법적 책임 동의 체크 UI' 추가.
  - [05-strategy-board.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/05-strategy-board.md): 서비스 하단 저작권 침해(DMCA) 신고 이메일 명시, 미공개 유출(스포일러) 데이터 정책 마련 등 게임사와의 마찰 방어 체계 반영.
- **다음 예정**: 기획 검토 및 다음 파트 설계.

### 2026-07-15 — Gemini 3.5 Flash / 기획자 / 공략 게시판(Phase 2) 고도화 기획 반영

- **작업 내용**: 실제 서브컬처/게임 커뮤니티(디시인사이드, 아카라이브 등) 분석을 바탕으로, 공략 게시판(파트 05) 기획을 보완 및 확장(Phase 2) 기획에 반영 완료.
  - [05-strategy-board.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/05-strategy-board.md): 추천/베스트 게시글 시스템, 이미지/미디어 첨부, 세부 카테고리(말머리) 및 태그, 통합 검색, 사용자 신고 및 블라인드 시스템을 Phase 2 요구사항으로 추가.
- **다음 예정**: 전체적인 Phase 2 백로그 정리 및 프론트/백엔드 후속 개발팀에게 인수인계 / 우선순위 정리.

### 2026-07-15 — Gemini 3.5 Flash / 기획자 & 디자이너 / 역할 범위 제약조건 추가 (개발 배제)

- **작업 내용**: 기획자 및 디자이너 페르소나 적용에 따른 개발 코드 변경 배제 규칙 명세.
  - 이 세션은 기획(기능 정의, 일정 관리, E2E 시나리오 설계) 및 UI/UX 디자인 시안 도출만 수행하며, 코드베이스의 소스코드 구현/수정(개발) 작업에는 일절 관여하지 않습니다.
  - 본 제약사항을 `SYNC.md` 요약 테이블 및 본 작업 로그에 공식 명시하여 후속 개발 AI 세션들과의 업무 범위를 정비했습니다.
- **다음 예정**: 기획 및 디자인 관점의 고도화 작업 및 피드백 대응.

### 2026-07-15 — Claude Sonnet 5 / 개발자(백엔드) / 파트 06(관리자/유저 관리) 백엔드 완료

- **작업 내용**: 파트 06(관리자/유저 관리) 백엔드 완료 — 계획서(docs/plans/00-overview.md)의 01~06 파트 백엔드가 전부 끝났다
  - `GET /api/admin/users?query=`(이메일/닉네임 부분일치 검색, `AdminUserController`), `PUT /api/admin/users/{id}/role`(역할 변경), `DELETE /api/admin/users/{id}`(정지 — 새 `status` 필드 대신 기존 `UserEntity.isDeleted`/`deletedAt` 재사용)
  - `GET/PUT /api/users/me`(내 프로필 조회/수정, `UserProfileController`), `GET /api/users/me/history`(내가 쓴 글+댓글 통합 조회, `UserHistoryService`) — 가챠 뽑기 기록은 파트 04에서 서버에 영속 저장을 안 하기로 했으므로 히스토리에 없음
- **다른 파트에 영향 줄 수 있는 발견**: `UserService.findOrCreateUser`(파트 02)가 재로그인 때마다 구글 프로필의 name/profilePictureUrl로 덮어써서, `PUT /api/users/me`로 바꾼 닉네임이 다음 구글 로그인 시 원복될 수 있음. 이미 동작 중인 파트 02 로직이라 이번엔 안 건드렸고, 문제되면 별도로 고칠 것(`findOrCreateUser`가 최초 생성 시에만 name을 설정하도록)
- **알아둘 점**: role 변경은 DB에는 즉시 반영되지만, 이미 발급된 JWT는 발급 시점 role을 담고 있어 그 토큰이 만료되기 전까진 예전 권한으로 동작함(재로그인해야 새 role 반영) — stateless JWT의 구조적 한계, 이번 파트에서 별도 조치 안 함
- **테스트**: `UserManagementServiceTest`(검색/역할변경/프로필수정), `UserHistoryServiceTest`(본인 글/댓글만 조회), `SecurityConfigTest`에 관리자/본인 프로필 케이스 추가. `./gradlew test` 전체 통과(52개)
- **다른 영역 요청사항**: 없음
- **다음 예정**: 계획된 백엔드 파트 없음(01~06 전부 완료). 프론트(웹)는 05(공략 게시판)·06(관리자/유저 관리) 화면이 아직 없고, 파트 07(확장 기능 기획)은 기획만 완료된 상태 — 사용자 방향 확인 필요

### 2026-07-15 — Claude Fable 5 / 개발자(프론트) / Mock→실API 전환 + 실서버 e2e 검증 (frontend #4)

- **작업 내용**: 백엔드의 "Mock 끄고 실제 연동 확인" 요청 수행 + 실서버 e2e 검증
  - Mock 기본값을 실 API로 전환: 웹 `gachaApi.js`(`VITE_USE_MOCK_GACHA=true`일 때만 Mock), 모바일 `app_config.dart`(보관 상태지만 정합성 위해 동일 전환), `.env.example` 갱신
  - 로컬에 PostgreSQL이 없어 **H2 인메모리로 백엔드 실서버 기동** 후 e2e: bootJar를 `PropertiesLauncher` + `-Dloader.path`(H2 jar)로 실행, `SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver`/`DB_URL=jdbc:h2:mem:...;MODE=PostgreSQL` 등 환경변수 오버라이드 — 코드/설정 파일 수정 없음. 재현 절차는 이 방식 그대로
- **e2e 검증 결과 (전부 통과)**:
  - 관리자 JWT(HS256 직접 발급)로 게임/캐릭터 6종/배너/풀 시드 등록
  - 가챠 계약 필드 전부 일치(`gameName` 포함 — backend #3의 배치 조회 버그픽스 실동작 확인), 뽑기 500회 동안 천장(90) 초과 없음·5성 시 pity 리셋·pity 89 → 10연 첫 결과 확정 5성
  - 스케줄러 `GET /api/schedules`의 `gameTitle` 정상(버그픽스 확인), 무토큰 관리자 API 401, Capacitor origin(`https://localhost`) CORS 프리플라이트 200
- **인터페이스 계약 정정**: 관리자 풀 구성 PUT의 characterId가 경로가 아니라 body에 들어감을 확인 — 기존 로그는 수정하지 않고 "인터페이스 계약" 섹션에 정정 기록 (규칙 3)
- **문서 정리**: 계약 섹션 상태를 "확정"으로 갱신, 환경/설정의 Mock 기본값·Flutter 보관 표기 반영, 해결된 요청 4건 체크오프
- **미검증**: 실제 Google 로그인 e2e와 실제 PostgreSQL 환경 — 사용자 조치 필요 (위 미해결 요청)

### 2026-07-15 — Gemini 3.5 Flash / 기획자 / 확장 기능(Phase 4-5) 상세 기획서 수립 완료

- **작업 내용**: 서비스 스케일업 및 잔존율 강화를 위한 고도화 기능 4종 상세 기획서 작성 완료.
  - [07-extended-features-planning.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/07-extended-features-planning.md): 몬테카를로 기반 가챠 성공률 계산기, iCal/ICS 캘린더 연동, 가챠 결과 이미지 공유 카드 생성기, AI 기반 스케줄 자동 수집 파이프라인 기획서 추가.
  - [00-overview.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/00-overview.md): 로드맵에 파트 07(확장 기능 기획, 상태: 기획 수립 완료/개발 대기) 추가 반영.
- **다음 예정**: 기획 기반의 Phase 2 공략 게시판(05) 프론트엔드 연동 개발 또는 추가 설계 조율.

### 2026-07-15 — Gemini 3.5 Flash / 기획자 / E2E 검증 환경 구축 및 가이드 완료

- **작업 내용**: Phase 1 E2E 수동 검증을 위한 로컬 개발 환경 가이드라인 구축 및 DB 시드 준비.
  - [e2e_verification_guide.md](file:///Users/somminwoo-m1/.gemini/antigravity/brain/1a0a7cbc-3274-4d56-99a7-a78b19955393/e2e_verification_guide.md): 로컬 DB 기동(Docker PostgreSQL) 및 백엔드/프론트엔드 연동 수동 검증 시나리오 작성 완료.
  - [seed.sql](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/backend/backend_gacha/src/main/resources/seed.sql): 테스트를 위한 원신/붕괴 스타레일 게임 메타, 가챠 시뮬레이터 픽업 배너, 캐릭터 풀, 일정 데이터, 테스트 어드민/유저 시드 추가.
  - **검증**: 로컬 OpenJDK 26 런타임 환경에서 백엔드 프로젝트 빌드 및 29개 단위 테스트(가챠 확률/천장, SecurityConfig 역할 제어) 전체 통과 확인.
- **다음 예정**: Phase 2 공략 게시판(05) 프론트엔드 개발 및 E2E 실동작 검증 (backend #5에 의해 공략 게시판 백엔드 구현 완료된 상태 확인).

### 2026-07-15 — Claude Sonnet 5 / 개발자(백엔드) / 파트 05(공략 게시판) 백엔드 완료

- **작업 내용**: 파트 05(공략 게시판) 백엔드 완료
  - `ChannelEntity`(gameId, name, description), `PostEntity`(channelId, authorId, title, content, templateType, viewCount), `CommentEntity`(postId, authorId, content, parentCommentId — 대댓글 1단계) 신규 추가. `Game`과의 연결은 04 파트와 같은 컨벤션으로 `gameId`(Long) 사용
  - 글쓰기 템플릿은 계획서의 별도 `PostTemplate` 엔티티 대신 `PostTemplateType` enum(`GUIDE`/`QUESTION`/`FREE`)으로 단순화 — 동적 스키마가 실제로 필요해지기 전까지는 이 정도로 충분하다고 판단
  - 공개 API: `GET /api/channels?gameId=`, `GET /api/channels/{channelId}/posts`(Spring Data `Page` 그대로 직렬화, `?page=&size=&sort=`), `GET /api/posts/{postId}`(조회 시 viewCount+1), `GET /api/posts/{postId}/comments` — 전부 비로그인 접근 가능
  - 인증 필요 API: `POST /api/channels/{channelId}/posts`, `PUT/DELETE /api/posts/{postId}`, `POST /api/posts/{postId}/comments`, `DELETE /api/comments/{commentId}` — 수정/삭제는 본인 글/댓글이거나 관리자(`SUB_ADMIN`/`MAIN_ADMIN`)만 가능, 위반 시 Spring Security의 `AccessDeniedException`을 서비스에서 직접 던져서 기존 403 핸들러 재사용(새 예외 처리 인프라 안 만듦)
  - 관리자 API: `/api/admin/channels` CRUD
- **테스트**: `PostServiceTest`(조회수 증가, 본인/타인/관리자 권한 검증, 채널별 격리), `CommentServiceTest`(대댓글, 삭제 권한), `SecurityConfigTest`에 채널/게시글/댓글 케이스 추가. `./gradlew test` 전체 통과(42개)
- **다른 영역 요청사항**: 없음 — 프론트(웹) 쪽 `StrategyBoardPage`/`NoticeCreationPage`/`ChannelManagementPage`는 아직 미구현 상태로 확인됨(파트 03 로그 이후 웹 세션이 이 파트를 건드린 흔적 없음)
- **다음 예정**: 파트 06(관리자/유저 관리) 백엔드 착수

### 2026-07-15 — Claude Sonnet 5 / 개발자(백엔드) / 파트 05(공략 게시판) 착수, 이전 파트 요약

- **지금까지 완료**: 파트 01(보안 강화, DB 자격증명 환경변수화) → 파트 02(인증, JWT 발급 + SecurityConfig 정리) → 파트 03(스케줄러, Game/Schedule 공개 API + 관리자 CRUD + 유저 필터) → 파트 04(가챠 시뮬레이터, Banner/Character/뽑기 로직, 웹·모바일 계약대로 구현). 상세 내역은 바로 아래의 이전 로그 항목들 참고. `./gradlew test` 29개 전체 통과 상태에서 이어감
- **지금부터 착수**: 파트 05(공략 게시판) 백엔드 — `docs/plans/05-strategy-board.md` 기준 Channel/Post/Comment 엔티티 + API. 다른 영역에 영향 주는 변경이 생기면 이 로그를 갱신할 예정

### 2026-07-15 — Claude Sonnet 5 / 개발자(백엔드) / 파트 04(가챠 시뮬레이터) 백엔드 완료

- **작업 내용**: 파트 04(가챠 시뮬레이터) 백엔드 완료 — 위 "가챠 API 계약" 섹션(웹/모바일이 이미 Mock으로 구현해둔 형태) 그대로 구현
  - `BannerEntity`(gameId, name, startAt, endAt, pityThreshold, rateUpRate), `CharacterEntity`(gameId, name, rarity, iconUrl), `BannerCharacterEntity`(배너-캐릭터 풀 join, weight/isPickup) 신규 추가. `Game`과의 연결은 이 파트만 `gameCode`가 아니라 계약대로 `gameId`(정수) 사용
  - 공개 API: `GET /api/games/{gameId}/banners`, `GET /api/banners`, `GET /api/banners/{bannerId}/characters`, `POST /api/banners/{bannerId}/pull` — 전부 비로그인 접근 가능
  - 관리자 API: `/api/admin/banners`, `/api/admin/characters` CRUD + 풀 구성(`PUT/DELETE /api/admin/banners/{bannerId}/characters/{characterId}`)
  - 뽑기 로직(`BannerService.pull`): 매 뽑기 pity+1, 천장 도달 시 확정, 확정 아니어도 최고 등급 가중치 비율만큼 자연 확률 적중, 최고 등급 적중 시 rateUpRate 확률로 픽업 우선. 별도 `PityPolicy` 테이블 없이 배너 필드로 단순화
  - CORS: mobile_shell 요청 반영 — `https://localhost`(Capacitor WebView)를 기본 허용 origin에 추가. `cors.allowed-origins` 환경변수로 배포 시 origin 추가 가능(콤마 구분, 기본값 `http://localhost:5173,https://localhost`)
- **범위에서 뺀 것**: 로그인 유저의 서버 측 영속 천장 카운트(`PullHistory` 등)는 구현하지 않음. 현재는 로그인 여부와 무관하게 요청의 `currentPity`를 그대로 신뢰/갱신한다(웹/모바일 Mock과 동일 동작이라 계약 위반 아님). 기기 간 천장 동기화가 필요해지면 별도 작업 필요 — docs/plans/04-gacha-simulator.md 설계 메모 참고
- **중요 — 다른 세션도 알아둘 버그 수정**: `ScheduleEventEntity.game`(파트 03에서 만든 지연 로딩 연관관계)이 실제로는 항상 `null`을 반환하고 있었음(참조 컬럼이 games의 PK가 아니라 Hibernate가 프록시를 못 만듦). 즉 지금까지 `GET /api/schedules` 등의 응답에서 `gameTitle`이 계속 빈 값이었을 것. 이번에 발견해서 고쳤다 — 응답 JSON 형태는 동일하고 값만 정상적으로 채워지므로 프론트 수정은 필요 없음. 같은 문제가 있던 기존 `UserGamePreferenceEntity.user`/`.game`(파트 03 이전부터 있던 코드, `/api/users/me/game-preferences`의 `gameTitle`)도 같이 고쳤음. **앞으로 이런 지연 로딩 연관관계(참조 컬럼이 대상 테이블 PK가 아니거나 `@IdClass` 복합키인 경우)는 믿지 말고 항상 명시적으로 배치 조회할 것** — 상세 원인은 docs/plans/04-gacha-simulator.md 참고
- **테스트**: `BannerServiceTest`(뽑기 확률/천장 검증, 200/30회 반복 통계 테스트), `SecurityConfigTest`에 배너/뽑기/관리자 케이스 추가. `./gradlew test` 전체 통과(29개)
- **미검증**: 실제 서버 기동 후 웹/모바일에서 Mock 끄고 붙여보는 e2e — 위 "다른 영역에 대한 요청" 참고
- **다음 예정**: 파트 05(공략 게시판) 백엔드 착수

### 2026-07-15 — Claude Fable 5 / 개발자(프론트·모바일) / mobile_shell 신설 — 모바일 전략 변경(사용자 결정)

- **결정 사항**: 모바일은 별도 화면을 만들지 않고 **하이브리드(Capacitor 셸 + 반응형 웹)** 로 전환. 웹을 재배포하면 앱 컨텐츠가 함께 갱신되는 "컨텐츠 재배포" 모델. **기존 Flutter 앱(`frontend_mobile/`)은 삭제하지 않고 보관** (추후 네이티브 전환 대비, 사용자 지시)
- **작업 내용**: `mobile_shell/` 신설 — Capacitor 7 + Android 플랫폼. `npm run sync`로 웹 빌드를 셸에 동기화, 상세 워크플로/원격 URL 모드 전환법은 `mobile_shell/README.md` 참고. `frontend/index.html` 정비(제목/lang/파비콘/viewport-fit)
- **검증**: `gradlew assembleDebug` 성공(app-debug.apk 생성, React 웹 번들 포함). 에뮬레이터 실행은 Android Studio에서 확인 필요. 빌드 시 JDK 21 필요 — Android Studio 내장 JDK 사용(README에 기재)
- **다른 영역 영향**:
  - **backend**: CORS 허용 요청 (위 미해결 요청 참고)
  - **frontend(웹)**: 이제 모바일 브라우저+앱 셸까지 커버하므로 새 화면은 반응형 필수. 기존 `useMediaQuery`/`MainLayout` 분기 활용
  - **frontend_mobile(Flutter)**: 신규 작업 중단, 보관. 계획서의 "모바일 연동" 항목들은 반응형 웹으로 대체됨
  - **향후 파트 04 모바일**: Flutter 시뮬레이터 화면 대신 웹 `SimulatorPage`가 앱에서도 그대로 사용됨

### 2026-07-15 — Claude Fable 5 / 개발자(프론트) / 파트 03 프론트(웹) 완료 (frontend #3)

- **작업 내용**: 파트 03 프론트(웹) 완료 — backend #2 로그에서 확정한 계약(`gameCodes`/`game-preferences`) 그대로 사용
  - `src/api/schedulerApi.js`: 공개 조회(`fetchGames`/`fetchSchedules`) + 개인화 필터(`fetchMyGamePreferences`/`updateMyGamePreferences`) + 관리자 CRUD(`adminApi`) — 전부 `apiFetch` 사용
  - `SchedulerPage`: 간트 차트 자체 구현(의존성 추가 없음), 게임 필터 체크박스(비로그인 localStorage / 로그인 서버 저장 분기), 기간 이동(-1개월~+3개월 기본). `App.jsx`에서 scheduler 라우트의 `ProtectedRoute` 제거(비로그인 열람 DoD 반영)
  - `GameManagementPage`: 게임 등록/수정/삭제 + 일정 등록/수정/삭제 폼과 목록
- **검증**: eslint 통과, `npm run build` 성공. **백엔드 실기동 e2e는 미검증** — 이 머신에 `application-local.properties`(DB 자격증명)가 없고 Java 17뿐(백엔드는 21 필요). 백엔드 세션 또는 사용자가 서버 기동 후 스케줄러/관리자 화면 확인 필요
- **다른 영역 요청사항**: 없음. 가챠 API 계약 검토(backend, 파트 04 착수 시)는 그대로 대기

### 2026-07-15 — Claude Sonnet 5 / 개발자(백엔드) / 파트 03(스케줄러) 백엔드 완료

- **작업 내용**: 파트 03(게임 업데이트 스케줄러) 백엔드 완료
  - `ScheduleEventEntity`(gameCode, title, category(`UPDATE`/`EVENT`/`MAINTENANCE`), startAt, endAt, description) 신규 추가. 기존 컨벤션대로 `id`가 아니라 `gameCode`로 `Game`과 연결
  - 공개 조회 API 신규: `GET /api/games`(`PublicGameController`), `GET /api/schedules?gameCodes=&from=&to=` — 둘 다 비로그인 접근 가능(SecurityConfig에 GET permitAll 매처 추가)
  - 관리자 CRUD 신규: `POST/PUT/DELETE /api/admin/schedules` (`ScheduleEventController`, `SUB_ADMIN`/`MAIN_ADMIN`만)
  - 개인화 필터 API 신규: `GET/PUT /api/users/me/game-preferences` (로그인 필요) — 기존 `UserGamePreferenceService`에 `replacePreferences`(전체 교체) 추가해서 사용
  - 기존 `GameController`의 DTO 변환 중복을 `GameMapper`로 추출해 재사용(신규 `PublicGameController`와 공유)
- **인터페이스 계약 변경**: 계획서 초안의 `gameIds`/`game-filters`를 실제 구현에서 `gameCodes`/`game-preferences`로 명명 — 위 목록이 확정된 이름. 프론트(웹)에서 아직 이 API들을 호출하는 코드는 없어 보여서 지금 반영해도 영향 없음
- **테스트**: `ScheduleEventServiceTest`, `UserGamePreferenceServiceTest` 추가, `SecurityConfigTest`에 공개 API/관리자 API/필터 API 케이스 추가. `./gradlew test` 전체 통과(22개)
- **다음 예정**: 파트 04(가챠 시뮬레이터) 착수 — 위 "가챠 API 계약" 섹션을 웹·모바일이 이미 구현한 Mock 기준으로 검토하고, 필요하면 조정안을 이 문서에 남길 예정

### 2026-07-15 — Claude Fable 5 / 개발자(프론트) / 파트 02 프론트 연동 + 파트 04 SimulatorPage 웹 구현 (frontend #2)

- **작업 내용**:
  - 파트 02 프론트 연동 마무리: backend 세션이 만든 `src/api/apiClient.js`를 확장 통합(Bearer 자동 첨부 + 401 자동 로그아웃/`/login` 리다이렉트 + 비2xx `ApiError` throw + JSON 반환). `LoginPage` dummy 토큰 교체는 backend 세션이 이미 반영, role 기반 UI는 기존 `Sidebar`에 이미 구현돼 있어 추가 작업 없음
  - 파트 04 웹 범위: `SimulatorPage` 구현(배너 선택/천장 게이지/1회·10연 뽑기/결과 그리드) — `src/api/gachaApi.js`(SYNC 계약 준수, `apiFetch` 사용) + `src/api/mockGachaApi.js`(개발용 Mock, 모바일 Mock과 동일 데이터/로직). `VITE_USE_MOCK_GACHA=false`로 실 API 전환
- **검증**: eslint 통과, `npm run build` 성공. Mock 천장 로직은 node로 5000회 뽑기 시뮬레이션 검증(천장 초과 없음, 최고 등급 시 pity 리셋, 천장 도달 시 확정 등급)
- **다른 영역 요청사항**: backend의 "파트 03 이후 가챠 API 계약 검토" 일정 확인함. 웹/모바일 모두 Mock으로 대기, 계약 확정 시 플래그 전환만 하면 됨
- **미검증**: 구글 로그인 → JWT 저장 → 보호 API 호출의 실제 엔드투엔드 동작(백엔드 기동 + 실제 Google Client ID 필요, backend 로그의 미검증 항목과 동일)

### 2026-07-15 — Claude Sonnet 5 / 개발자(백엔드) / 파트 01(보안 강화) + 파트 02(인증) 백엔드 완료

- **작업 내용**: 파트 01(보안 강화) + 파트 02(인증/공통 기반) 백엔드 완료
  - 파트 01: `application.properties`의 DB 자격증명 하드코딩을 환경변수(`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`)로 교체, 로컬 override용 `application-local.properties.example` 추가, 루트 `.gitignore` 정비. **RDS 비밀번호 로테이션은 사용자가 AWS 콘솔에서 직접 처리해야 함(미완료, 코드로 대행 불가)**
  - 파트 02: 착수 시점에 `com.gacha.gachascheduler` 하위에 `UserEntity`/`Role`(`USER`/`SUB_ADMIN`/`MAIN_ADMIN`)/`UserService`/`AuthController`(Google ID 토큰 검증)가 이미 구현돼 있는 것을 확인. 여기에 `security.JwtProvider`/`JwtAuthenticationFilter`를 추가해 `POST /api/auth/google` 응답(`UserResponseDto.token`)에 자체 JWT를 담아 발급하도록 완결. `SecurityConfig`는 stateless JWT 필터 기반으로 재구성하고 `@EnableMethodSecurity`를 추가해 `GameController`의 `@PreAuthorize`가 실제로 동작하도록 수정(이전엔 애노테이션만 있고 미적용 상태였음)
- **제거한 것**: 프론트가 쓰지 않는 Spring 서버 리다이렉트형 `oauth2Login`/`CustomOAuth2User`/`spring-boot-starter-oauth2-client` 의존성 — ID 토큰 POST 방식과 두 인증 경로가 공존해 혼란스러웠음
- **다른 영역에 살짝 손댄 부분**: `frontend/src/pages/LoginPage.jsx`에서 `login(userData, 'dummy-jwt-token')` → `login(userData, userData.token)` 한 줄만 교체(백엔드가 이제 진짜 토큰을 내려주므로), `frontend/src/api/apiClient.js` 신규 추가(Authorization 헤더 자동 첨부 + 401 시 로그아웃하는 fetch 래퍼, 아직 어디서도 사용 안 함). 이 세션은 지금부터 백엔드만 작업하기로 해서 프론트 쪽은 더 건드리지 않음 — 필요하면 frontend 세션에서 이어서 사용/수정해도 됨
- **테스트**: `JwtProviderTest`, `SecurityConfigTest`(401/403/200 케이스), `UserServiceTest` 추가, `./gradlew test` 전체 통과(12개)
- **미검증**: `POST /api/auth/google`의 실제 Google ID 토큰 검증 e2e는 수동으로 못 해봄(실제 Google 자격증명 필요) — 프론트 연동 시 함께 확인 필요
- **다른 영역 요청사항 확인**: 모바일의 "가챠 API 계약" 확정 요청 확인함. 계획 순서상 파트 03(스케줄러)을 먼저 끝낸 뒤 파트 04에서 이 계약을 검토/확정할 예정. 그때까지는 모바일의 Mock 동작 유지 권장

### 2026-07-15 — Claude Fable 5 / 개발자(모바일) / 파트 04 모바일(Flutter) 시뮬레이터 화면 (frontend_mobile #1)

- **작업 내용**: 파트 04 모바일 범위 완료 — 가챠 시뮬레이터 화면(`lib/screens/gacha_screen.dart`), Riverpod 상태 관리(`lib/providers/gacha_providers.dart`), API 저장소 + 개발용 Mock(`lib/repositories/`), 모델(`lib/models/`), 설정(`lib/config/app_config.dart`), l10n ko/en 문자열 추가
- **동작 방식**: 백엔드 API 부재로 기본 Mock 동작(가중치/천장/픽업 로컬 시뮬레이션). 백엔드 완성 시 `USE_MOCK_GACHA=false`로 전환만 하면 됨. 확률 로직은 최종적으로 서버 책임(계획서 원칙)
- **다른 영역 요청사항**: 위 "가챠 API 계약" 확정 요청 (backend)
- **미검증**: 이 머신에 Flutter SDK가 없어 `flutter analyze`/실행 검증 못 함. SDK 있는 환경에서 확인 필요
