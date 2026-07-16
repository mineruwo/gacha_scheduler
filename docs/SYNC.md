# AI 작업 동기화 문서 (SYNC)

여러 AI 세션(백엔드/웹/모바일)이 병렬로 작업할 때 서로의 최신 작업을 공유하는 문서.

> **처음 합류한 세션이라면**: 이 로그를 전부 읽기 전에 [HANDOVER.md](HANDOVER.md)부터 보면 5분 안에 현재 상태·꼭 알아야 할 함정들을 파악할 수 있다. 이 문서는 시간순 상세 기록, HANDOVER.md는 항상 최신으로 갱신되는 스냅샷.

## 규칙

1. **작업 시작 전** 이 문서를 먼저 읽고, 자신의 영역에 영향을 주는 변경/요청이 있는지 확인한다.
2. **작업 종료 시** 아래 "작업 로그"의 **맨 위에** 새 항목을 추가한다 (최신이 위). 기존 항목은 수정하지 않는다. **기록 시 반드시 [AI 모델] / [페르소나 (기획자, 개발자(프론트, 백엔드 등))] / [작업 내용]을 포함하여 남겨야 한다.**
3. 다른 영역과의 **인터페이스(API 계약, DTO 형식, 환경변수 이름 등)를 만들거나 바꿀 때는** 반드시 "인터페이스 계약" 섹션을 갱신하고, 로그에 "다른 영역 요청사항"으로 명시한다.
4. 상세 계획/체크리스트는 [docs/plans/](plans/00-overview.md)에 유지하고, 이 문서에는 **세션 간 전달이 필요한 것만** 적는다.

## 영역별 최신 상태 (요약)

| 영역 | 최종 갱신 | 상태 한 줄 요약 |
|---|---|---|
| planner & designer | 2026-07-15 | 기획 및 디자인 전문 세션 — 기능 기획, UI/UX 시안 작성, E2E 시나리오 수립 담당 (개발/코드 변경에 일체 참여하지 않음) |
| backend | 2026-07-16 | 파트 07·08·09(배포 준비물) 완료 + 로컬 PostgreSQL 지속 기동 중 + 실 Google Client ID 반영 + **제품 완성도 점검 후 우선순위 높은 항목 처리**(ErrorBoundary/404 페이지/게시판 검색, 아래 로그) + **seed.sql 5성 확률 밸런스 버그 수정**(아래 로그) — 이미지 업로드는 보류 결정. 상세는 아래 로그 |
| frontend (Web/React) | 2026-07-16 | 파트 02~06 프론트 전부 완료 + 잔존 버그 수정 + 모바일 하단 탭바 정정 + **로그인 화면에 이메일/비밀번호 로그인·회원가입 탭 추가**(구글 로그인과 병행) + **가챠 배너/드랍테이블 관리자 페이지 신규**(아래 로그 참고) |
| frontend_mobile (Flutter) | 2026-07-16 | **🔴 모바일 전략 재전환(2차) — 이제 이게 유일한 모바일 앱이다.** 웹(`frontend`)에 있던 화면 전부 이식 완료: 가챠 시뮬레이터, 로그인/회원가입, 스케줄표(간트차트), 공략 게시판, **유저 프로필(실 데이터 조회/수정)**. 전 화면 라이트 파스텔 디자인 적용. 전부 실기기 e2e 검증 완료. 남은 것: 유저관리(관리자) 화면, 구글 로그인, 401 자동 로그아웃 — 아래 로그 참고 |
| mobile_shell (Capacitor) | 2026-07-16 | **🔴 중단(deprecated). 신규 작업 금지** — 사용자가 모바일 전략을 다시 Flutter 네이티브로 전환하기로 결정(아래 로그 참고). 오늘 추가된 Google 로그인 브릿지/하단탭바/아이콘 등은 유지만 하고 더 이상 발전시키지 않음. 코드는 삭제하지 않고 보관 |

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
- **주의(자연 확률 계산)**: `BannerService.pull()`의 자연 확률은 `해당 등급 가중치 합 / 풀 전체 가중치 합`으로 계산된다. `weight`는 절대 확률처럼 보이도록 설계됐으므로(개별 5성 0.006 = 0.6%) **풀 전체 가중치 합이 1.0에 근접해야 의도한 확률이 나온다.** 하위 등급(3성 등)을 빼먹으면 분모가 작아져 상위 등급 확률이 비정상적으로 뛴다(2026-07-16 실제 발생 버그, 아래 로그 참고). 새 배너/게임을 시딩할 때 반드시 하위 등급까지 채워서 합계를 확인할 것 — 관리자 페이지(`GachaBannerManagementPage`)의 확률 미리보기로 바로 확인 가능
- `BannerCharacterResponseDto`(공개 `GET /api/banners/{id}/characters`)에는 `isPickup` 필드가 없다 — 픽업 여부가 필요하면 `BannerResponseDto.pickupCharacterIds`(배너 목록/상세 응답에 포함)와 캐릭터 id를 대조해서 판단할 것(`GachaBannerManagementPage`가 이 방식으로 구현돼 있음, 새 DTO 필드 추가 안 함)

### 인증 API (파트 02 확장) — 아이디/비밀번호 로그인·회원가입 (2026-07-16 신규 확정)

- `POST /api/auth/signup` — 요청 `{ email, password(8자 이상), name }`, 응답은 `POST /api/auth/google`과 동일한 `UserResponseDto`(토큰 포함, 가입 즉시 로그인 처리). **이메일이 이미 구글 계정으로 가입돼 있으면 그 계정에 비밀번호만 추가**(같은 계정으로 통합 — 사용자 결정 사항), 이미 비밀번호가 있는 이메일이면 `409 Conflict`
- `POST /api/auth/login` — 요청 `{ email, password }`, 성공 시 `UserResponseDto`, 실패(이메일 없음/비밀번호 틀림/구글 전용 계정) 시 `401`
- `UserEntity.passwordHash`는 응답 DTO에 절대 포함 안 됨(구글 전용 계정은 null). 비밀번호 재설정(이메일 인증) 기능은 아직 없음 — 나중에 필요해지면 별도 파트로
- 이 계약 변경 시 수정 필요한 곳: backend `AuthController`/`UserService`, 웹 `frontend/src/pages/LoginPage.jsx`, **모바일 `frontend_mobile/lib/repositories/auth_repository.dart`(2026-07-16부터 이 API 신규 소비, 아래 로그 참고)**

### 환경/설정

- (보관: Flutter) 모바일 API 베이스 URL: `--dart-define=API_BASE_URL=...` (기본 `http://localhost:8080`, Android 에뮬레이터 `http://10.0.2.2:8080`)
- (보관: Flutter) 모바일 Mock↔실API 전환: `--dart-define=USE_MOCK_GACHA=true`일 때만 Mock (2026-07-15 기본값을 실 API로 전환)
- 웹: `frontend/.env`의 `VITE_API_BASE_URL`
- 웹 Mock↔실API 전환: `VITE_USE_MOCK_GACHA=true`일 때만 Mock — **기본은 실 API** (2026-07-15 기본값 전환, 백엔드 가챠 API 구현 완료에 따름)
- 웹 공통 API 클라이언트: `frontend/src/api/apiClient.js`의 `apiFetch` — localStorage `token`을 `Authorization: Bearer`로 자동 첨부, 401 응답 시 인증 정보 삭제 후 `/login` 리다이렉트, 2xx가 아니면 `ApiError` throw, JSON 파싱해 반환. **이후 웹에서 백엔드 호출 시 이 함수를 사용할 것**

## 다른 영역에 대한 요청 (미해결)

- [ ] **🔴 → 모든 세션(특히 frontend/mobile_shell 담당)**: **모바일 전략이 다시 바뀌었다 — `mobile_shell`(Capacitor) 개발 중단, `frontend_mobile`(Flutter)이 유일한 모바일 앱으로 재확정됨(2026-07-16, 사용자 결정).** 아래 로그의 상세 항목 참고. `mobile_shell`에 새 기능/버그 수정 투입하지 말 것 — 이미 만든 건 보관만 하고 냅둠. 프론트(웹) 세션은 계속 PC 브라우저 대응만 신경 쓰면 되고, 모바일 대응은 이제 `frontend_mobile` Flutter 쪽에서 전담
- [ ] **→ 사용자**: 실제 Google 계정으로 로그인 버튼을 직접 눌러 e2e 확인 — **데스크톱/모바일 웹 브라우저**에서만 유효(위 전략 변경으로 `mobile_shell`의 외부 브라우저 위임 로그인은 더 이상 이어가지 않음)
- [ ] **🔴 → backend 세션(AuthController/UserService 담당)**: 회원가입의 "구글 계정에 비밀번호 병합" 로직에 이메일 소유권 검증 없는 계정 탈취 취약점 있음 — 상세는 [09-deployment.md](plans/09-deployment.md) "발견한 보안 이슈" 섹션, 아래 2026-07-16 배포 로그 참고. 이메일 인증 절차 추가 또는 병합 로직 재설계 필요
- [ ] **→ 사용자**: 도메인 준비되면 알려줄 것 — certbot으로 TLS 발급 진행 (지금은 `http://101.79.26.52`로만 접근 가능)

해결됨:

- ~~**→ 사용자**: 실제 AWS RDS/EC2 계정 준비~~ → AWS 프리티어 신규 가입 불가로 **NCP로 배포 대상 전환**(사용자 결정), Server+PostgreSQL 직접 설치로 실제 프로비저닝 및 백엔드 배포 완료(2026-07-16, 아래 로그 참고). `http://101.79.26.52`로 접근 가능
- ~~**→ 사용자/planner**: `mobile_shell` Google 로그인 방향 결정(외부 브라우저 위임 vs 네이티브 SDK)~~ → 사용자가 "외부 브라우저로 위임"을 선택, 구현·실기기 검증 완료(아래 로그 참고)
- ~~**→ 사용자**: 실 Google OAuth Client ID 필요~~ → 사용자가 Firebase 콘솔에서 발급받은 Web Client ID(`435890288277-pj54fjvqlr1bjct6nsuqhk4da9pl7285.apps.googleusercontent.com`) 확인, 백엔드 `application-local.properties`와 프론트 `frontend/.env`의 `GOOGLE_CLIENT_ID`/`VITE_GOOGLE_CLIENT_ID`에 동일하게 반영 완료. 백엔드 재기동해서 값 반영 확인(`/api/hello`, `/api/games` 200 응답)

- ~~**→ backend**: 가챠 API 계약 확정~~ → "파트 04(가챠 시뮬레이터) 백엔드 완료" 로그에서 계약 그대로 구현 완료
- ~~**→ backend**: CORS에 Capacitor origin(`https://localhost`) 추가~~ → 같은 로그에서 반영 완료, frontend #4에서 프리플라이트 실동작 확인
- ~~**→ frontend/frontend_mobile**: Mock 끄고 실제 연동 확인~~ → frontend #4 로그 참고. 웹·모바일 모두 기본값을 실 API로 전환했고, 가챠/스케줄러 API는 H2 인메모리로 서버를 띄워 e2e 검증 완료 (frontend_mobile은 보관 상태라 코드 기본값만 정합성 맞춤)
- ~~**→ 사용자/아무 세션**: 서버 띄워 가챠 API e2e~~ → frontend #4에서 H2로 수행 (구글 로그인 부분만 위 미해결로 남김)

## 작업 로그 (최신이 위)

### 2026-07-16 — Claude Sonnet 5 / 개발자(모바일) / frontend_mobile 실서버(NCP) e2e 스모크 테스트 + 401 자동 로그아웃 버그 수정

- **사용자 요청**: "실제 서버로 테스트 해볼래?" — 백엔드가 NCP에 배포한 실서버(`http://101.79.26.52`, 위 로그 참고)로 모바일 앱을 붙여서 검증
  - `flutter build apk --debug --dart-define=API_BASE_URL=http://101.79.26.52`로 실서버를 가리키게 빌드. **디버그 전용** `network_security_config.xml`에 `101.79.26.52` 도메인 추가(TLS 붙기 전까지 cleartext 허용 — 실서버에 도메인/인증서 적용되면 이 항목 제거할 것)
  - 실기기(Galaxy Z Fold)에서 홈/스케줄표(실 시드 일정)/가챠(실 배너 "산드로네 픽업", 실제 10연 뽑기)/게시판(채널 0개라 빈 상태 정상 렌더링 — `curl`로 `GET /api/channels` → `[]` 확인, 버그 아니라 시드 데이터 차이)/회원가입→자동 로그인→프로필 조회까지 전부 확인
- **버그 발견 및 수정**: 로컬 백엔드로 로그인했던 예전 토큰이 기기에 캐시된 채로 실서버(다른 DB)에 붙이면, `GET /api/users/me`가 401을 반환하는데 **로그아웃 버튼도 없는 채로 "프로필을 불러오지 못했습니다" 에러 화면에 갇히는 버그** 발견(이전 로그의 "401 자동 로그아웃 처리 없음" 항목이 실제로 재현됨). `lib/providers/user_profile_providers.dart`의 `userProfileProvider`에서 `UserApiException(401)`을 잡아 `authControllerProvider.notifier.logout()`을 호출하도록 수정 — 이제 토큰이 무효화되면 자동으로 로그인 필요 화면으로 돌아감. 재빌드 후 실기기에서 확인 완료
- **다른 영역 요청사항**: 없음. 코드/설정 변경은 전부 `frontend_mobile` 안에 있고, 백엔드 API는 손대지 않음
- **남은 것**: 이 401 처리는 지금 `userProfileProvider`에만 적용됨 — `schedule_repository.dart`/`board_repository.dart`/`auth_repository.dart` 등 다른 인증 필요 호출에도 같은 패턴을 적용하면 더 견고해짐(다음에 손볼 후보). 게시판 콘텐츠(채널/글)는 실서버 시드에 아직 없음 — 필요하면 백엔드 세션에 채널 시드 추가 요청할 것

### 2026-07-16 — Claude Sonnet 5 / 개발자(모바일) / frontend_mobile 유저 프로필(실 데이터) 화면 신규 이식 — 웹 화면 전체 이식 완료

- **작업 내용**: 웹 `UserProfilePage.jsx`의 프로필 조회/수정 부분을 Flutter로 이식(관리자 메뉴 링크·다크모드 토글·뽑기 기록 링크는 해당 화면이 모바일에 아직 없어 제외)
  - `lib/models/user_profile_model.dart`, `lib/repositories/user_repository.dart`(`GET/PUT /api/users/me`), `lib/providers/user_profile_providers.dart`(`userProfileProvider` — 로그인 토큰이 바뀔 때마다 자동 재조회)
  - `lib/screens/my_info_screen.dart`를 **디버그(키/값 저장) 화면에서 실제 프로필 화면으로 교체**(PM 노트에서부터 예정돼 있던 개편) — 이름/이메일/역할/가입일 표시 + "정보 수정"(이름/프로필 사진 URL 인라인 폼) + 로그아웃. 디버그 도구는 완전히 제거
- 실기기(Galaxy Z Fold)에서 프로필 조회, 수정→저장→즉시 반영까지 확인
- **다른 영역 요청사항**: 없음. 기존 `/api/users/me` 계약 그대로 소비
- **의미**: 이것으로 웹(`frontend`)에 있던 로그인/스케줄러/가챠/게시판/프로필 5개 화면이 `frontend_mobile`에도 전부 이식됨. 남은 건 유저관리(관리자) 화면(모바일에 관리자 기능 자체가 아직 없음), 구글 로그인, 401 자동 로그아웃(어제 로그 참고)
- **참고(다른 세션 로그에서 확인)**: 바로 아래 백엔드 로그에 **회원가입 계정 탈취 취약점**이 보고돼 있음 — 모바일의 `/api/auth/signup` 호출부(`auth_repository.dart`)는 이 취약점과 직접 관련은 없지만(클라이언트는 백엔드 로직을 그대로 호출할 뿐), 백엔드가 이 로직을 수정하면 회원가입 실패 케이스(이메일 인증 등)가 늘어날 수 있어 모바일 쪽 에러 메시지 처리도 같이 재검토가 필요할 수 있음

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드/인프라) / NCP 서버 실제 프로비저닝 + 백엔드 배포 완료, 계정 탈취 취약점 발견

- **작업 내용**: 사용자가 NCP 콘솔에서 VPC/Subnet/ACG/Server(mi1-g3 Micro, 1년 무료, RAM 1GB, 공인 IP)를 만든 뒤 접속 정보를 전달, 세션이 SSH로 직접 접속해 나머지 프로비저닝 진행
  - SSH 하드닝: 전용 키 생성 → 서버에 등록 → **로컬 macOS의 `~/.ssh/gacha-scheduler/`에 안전하게 백업 확인 후** 비밀번호 로그인 비활성화(자동 승인 게이트가 "키 백업 없이 비밀번호 로그인부터 끄려는" 첫 시도를 막아서, 백업부터 하고 재시도함). `~/.ssh/config`에 `gacha-server` 별칭 등록
  - Java 21 / nginx / fail2ban / PostgreSQL 설치, `gacha` 전용 유저 생성, jar 업로드, DB/롤 생성, `backend.env` 서버에 직접 작성(로컬에 평문 저장 안 함)
  - **RAM 1GB 이슈**: JVM+Postgres 동시 구동이 빡빡해서(가용 메모리 100MB 안팎) 2GB 스왑 추가, JVM 힙 `-Xmx384m`으로 하향(템플릿 기본 512m에서), Postgres `max_connections=20`으로 하향
  - **nginx IPv6 이슈**: 이 서버가 IPv6 미지원인데 기본 사이트가 `listen [::]:80`을 갖고 있어 기동 실패 — 기본 사이트 제거, `gacha-scheduler` 전용 사이트만 사용
  - seed.sql 적용(게임/캐릭터/배너/공지) 후 **테스트 유저(admin@example.com 등)는 배포 직후 삭제** — 아래 보안 이슈 참고
  - 검증: `http://101.79.26.52/actuator/health` → `{"status":"UP"}`, `http://101.79.26.52/api/games` → 시드 게임 목록 정상 응답
- **🔴 배포 중 발견한 보안 이슈(코드 미수정, 별도 조치 필요)**: `UserService` 회원가입의 "구글 계정에 비밀번호 병합" 로직이 **이메일 소유권 검증 없이 계정 탈취를 허용**함 — 공격자가 기존 구글 전용 계정의 이메일만 알면 `POST /api/auth/signup`으로 비밀번호를 붙여서 그 계정에 로그인 가능. 상세 및 권장 수정 방향은 [09-deployment.md](plans/09-deployment.md) "발견한 보안 이슈" 섹션 참고. **다른 세션이 이 로직을 다시 볼 때 반드시 확인할 것**
- **다른 영역 요청사항**: 위 보안 이슈는 `AuthController`/`UserService`를 만든 세션(또는 이어받는 세션)이 이메일 인증 절차 추가 등으로 수정 필요
- **다음 단계**: 도메인 준비되면 certbot으로 TLS 발급, 프론트 배포 + `VITE_API_BASE_URL`/CORS 반영

### 2026-07-16 — Claude Sonnet 5 / 개발자(모바일) / frontend_mobile 공략 게시판 화면 신규 이식 (사용자 요청, 스케줄표 다음 순서)

- **작업 내용**: 웹 `StrategyBoardPage.jsx`(채널 탭/글 목록/검색·페이징/상세/댓글·대댓글/글쓰기)를 Flutter로 이식
  - `lib/models/channel_model.dart`, `lib/models/post_model.dart`(+`PostPageModel`, Spring `Page` JSON 그대로 파싱), `lib/models/comment_model.dart` 신규
  - `lib/repositories/board_repository.dart`(`BoardRepository`/`ApiBoardRepository`) — `GET /api/channels`, `GET /api/channels/{id}/posts`(검색어 포함), `POST/GET/DELETE /api/posts`, `GET/POST /api/posts/{id}/comments`, `DELETE /api/comments/{id}`. 인증 필요한 호출은 `AuthState.user.token`을 직접 `Authorization: Bearer`로 전달(스케줄표에서 쓴 패턴 재사용)
  - `lib/providers/board_providers.dart` — `channelsProvider`, `postsProvider`(family, `PostsQuery{channelId,page,query}` 키), `postDetailProvider`/`commentsProvider`(postId로 family). 글쓰기/댓글/삭제 후 `ref.invalidate(...)`로 목록·댓글 갱신
  - `lib/screens/board_screen.dart` 전면 재작성 — 채널 칩 탭, 검색+페이징 목록, 게시글 상세(조회수 자동 증가), 댓글/대댓글(1단계) 작성·삭제(본인 글/댓글이거나 SUB_ADMIN/MAIN_ADMIN이면 삭제 가능, 웹의 `canModify`와 동일 로직), 비로그인 시 로그인 유도 문구
  - `lib/screens/post_editor_screen.dart` 신규(글쓰기 — 템플릿/제목/내용, 웹의 `/admin/notice`에 해당하나 이름과 달리 일반 로그인 유저면 누구나 사용 가능한 것을 API 권한으로 확인 후 그대로 이식)
- **실기기 e2e 중 발견한 것(버그 아님, 환경 이슈)**: 로컬 백엔드 DB가 세션 사이 초기화되면서 이전에 만든 테스트 계정이 사라졌는데, 앱에는 예전 JWT가 로컬에 캐시돼 있어 "로그인된 것처럼" 보이다가 댓글 작성 시 401로 실패 — 새 계정으로 재로그인해서 해결. **모바일 앱은 아직 401 응답 시 자동 로그아웃 처리가 없음**(웹의 `apiClient.js`의 `handleUnauthorized`에 해당하는 게 없음) — 로컬 DB를 자주 초기화하는 여러 세션 병렬 개발 환경에서는 계속 헷갈릴 수 있는 부분이라 다음에 손볼 후보로 남겨둠(HANDOVER.md에도 기록)
- 실기기(Galaxy Z Fold)에서 채널 전환, 검색, 글쓰기(작성 후 목록 즉시 갱신 확인), 게시글 상세+조회수 증가, 댓글 작성/답글/삭제(확인 다이얼로그 포함) 전부 확인
- **다른 영역 요청사항**: 없음. 기존 게시판 API 계약(`/api/channels`, `/api/channels/{id}/posts`, `/api/posts/**`, `/api/comments/**`)을 그대로 소비만 함
- **남은 것**: 유저 프로필(실 데이터, `UserProfilePage` 이식) 미착수. 모바일 앱의 401 자동 로그아웃 처리도 아직 없음(위 참고)

### 2026-07-16 — Claude Sonnet 5 / 개발자(모바일) / frontend_mobile 스케줄표(간트차트) 화면 신규 이식 (사용자 요청 "이제 각 화면에 맞게 기능 이식이 필요해")

- **순서 결정**: 로그인 이식 완료 후 다음 대상(게시판 vs 스케줄표)을 사용자에게 다시 묻지 않고 판단 — 앱 이름 자체가 "가챠 스케줄러"이고 하단 탭 1번이라 스케줄표를 먼저 진행
- **작업 내용**: 웹 `SchedulerPage.jsx`(간트차트 스타일 일정표)를 Flutter로 이식
  - `lib/models/game_model.dart`, `lib/models/schedule_event_model.dart` 신규
  - `lib/repositories/schedule_repository.dart`(`ScheduleRepository`/`ApiScheduleRepository`) — `GET /api/games`, `GET /api/schedules?from&to`(둘 다 비로그인 가능), `GET/PUT /api/users/me/game-preferences`(로그인 필요, `Authorization: Bearer` 직접 첨부)
  - `lib/providers/schedule_providers.dart` — `gamesProvider`/`schedulesProvider`(Riverpod `FutureProvider`), `ScheduleRangeController`(기간 이동, 기본 -1개월~+3개월), `GameFilterController`(관심 게임 필터 — **로그인 시 서버에 저장, 비로그인 시 `shared_preferences`에 로컬 저장**, 웹과 동일한 정책. `authControllerProvider` 상태 변화를 `ref.listen`으로 구독해 로그인/로그아웃 시 자동 재로드)
  - `lib/screens/schedule_screen.dart` 전면 재작성 — 기간 툴바(이전달/다음달/오늘), 게임 필터 칩(`FilterChip`), 카테고리 범례, 커스텀 간트차트(`LayoutBuilder`+`Stack`/`Positioned`로 날짜를 퍼센트 위치로 환산해 이벤트 바 렌더링, 오늘 표시선 포함). 카테고리 색상은 `AppColors.categoryUpdate`/`categoryEvent`/`categoryMaintenance` 신규 추가(파스텔 톤으로 웹의 파랑/초록/빨강 컨셉 유지)
- **버그 발견 및 수정**: Dart `DateTime.toIso8601String()`은 로컬 시간일 때 오프셋/Z가 없는 문자열을 만드는데, 백엔드 `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)`은 오프셋이 없으면 400을 반환함(`curl`로 직접 재현 확인) — `schedule_repository.dart`에서 `from.toUtc().toIso8601String()`으로 수정. **다른 화면에서도 Dart `DateTime`을 쿼리 파라미터로 백엔드에 보낼 때는 항상 `.toUtc()`부터 거칠 것** (다음 세션도 참고)
- 실기기(Galaxy Z Fold)에서 툴바 이전/다음 달 이동, 게임 필터 토글, 간트차트 렌더링(카테고리별 색상, 오늘 표시선) 전부 확인
- **다른 영역 요청사항**: 없음. 기존 스케줄러 API 계약(`/api/games`, `/api/schedules`, `/api/users/me/game-preferences`)을 그대로 소비만 함, 변경 없음
- **남은 것**: 게시판(`StrategyBoardPage`)·유저 프로필(실 데이터, `UserProfilePage`) 화면은 아직 미착수

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드) / 배포 대상 클라우드를 AWS→NCP로 전환 (사용자 결정)

- **경위**: AWS 프리티어 신규 가입이 막혀서 대안을 검토. Oracle Cloud(영구 무료지만 ARM 재고 이슈)/GCP(Always Free가 미국 리전 한정이라 한국 서비스엔 지연시간 불리) 순서로 논의하다, 사용자가 **네이버클라우드플랫폼(NCP)** 선택 — 서울 리전 + 신규가입 크레딧 + 결제 편의성이 결정 이유. 계정 생성 및 크레딧 확인 완료(사용자)
- **작업 내용**: [09-deployment.md](plans/09-deployment.md)를 AWS 용어(EC2/RDS/보안그룹) → NCP 용어(Server/Cloud DB for PostgreSQL/ACG)로 전면 수정. 새 계정은 VPC 환경(옛 Classic 포트포워딩 방식과 다름, 공인 IP를 서버에 직접 연결)이라 그 기준으로 런북 작성. `deploy/backend.env.example`/`deploy/gacha-scheduler-backend.service`의 AWS 전용 문구(RDS/t4g.micro 등)도 프로바이더 중립적으로 수정. **DB는 Cloud DB for PostgreSQL(관리형)과 Server에 직접 설치(로컬 개발과 동일 구성, 더 저렴) 두 가지 옵션을 모두 제시** — 크레딧 소진 속도 걱정되면 후자 권장
- **다른 영역 요청사항**: 없음. 이 프로젝트는 프로바이더 전용 관리형 서비스를 안 쓰고 순수 VM+표준 오픈소스(Postgres/systemd/nginx) 구성이라 프로바이더를 바꿔도 코드/설정 변경이 거의 없음(env var 값만 교체) — 이 판단 근거도 문서에 남김
- **다음 단계**: 사용자가 NCP 콘솔에서 런북대로 VPC/ACG/Server/DB 생성 진행 예정

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드+프론트) / 공지사항/팝업 배너 관리 기능 신규 (06번 문서 Phase 2, 사용자 요청)

- **작업 내용**: "서버쪽 추가 작업" 질문에 남은 백엔드 항목으로 안내했던 06-admin-user-management.md Phase 2 "공지사항/팝업 배너 관리 엔드포인트"를 구현
  - 백엔드: `AnnouncementEntity`(테이블 `announcements`) — NOTICE(공지사항)/POPUP(팝업 배너) 공용 엔티티, `type`/`title`/`content`(NOTICE용)/`imageUrl`/`linkUrl`(POPUP용, linkUrl은 공용)/`startAt`/`endAt`(null 허용, 종료일 없음)/`isActive`. 공개 `GET /api/announcements?type=`는 `isActive=true AND startAt<=now<=endAt(또는 endAt null)`인 것만 반환(`AnnouncementRepository.findCurrentlyActive`), 관리자 `GET/POST/PUT/DELETE /api/admin/announcements`는 활성 여부 무관하게 전체 관리. `SecurityConfig`에 `/api/announcements/**` GET permitAll 추가
  - 프론트: `HomePage`에 활성 NOTICE 목록 표시(placeholder였던 `background:blue` 인라인 스타일도 정리해 `HomePage.css`로 이동). `MainLayout`에 전역 `PopupBannerModal` 마운트 — 활성 POPUP 중 오늘 아직 안 본 것 1개를 모달로 띄우고, "오늘 하루 보지 않기" 클릭 시 `localStorage`(`dismissedPopups: {id: 'YYYY-MM-DD'}`)에 기록해 그날은 다시 안 뜸(그냥 닫기는 세션에서만 숨김, 새로고침하면 다시 뜸 — 의도된 동작). 관리자 페이지 `AnnouncementManagementPage`(`/admin/announcements`) — 종류 선택에 따라 NOTICE는 내용 textarea, POPUP은 이미지 URL 입력으로 폼이 바뀜, CRUD 테이블
  - `frontend/src/api/announcementApi.js` 신규(`announcementApi`/`adminAnnouncementApi`, 다른 파트의 `xxxApi`/`adminXxxApi` 컨벤션과 동일)
- **검증**: `AnnouncementServiceTest`(날짜 범위/isActive 필터링/CRUD) + `SecurityConfigTest`(공개 GET permitAll, 관리자 CRUD 401/403/200) 추가, `./gradlew test` 전체 통과. eslint/`npm run build` 통과. 로컬 Postgres+Vite로 브라우저 e2e — 홈페이지 공지 노출, 팝업 표시 및 "오늘 하루 보지 않기" 후 재방문 시 미노출, 관리자 페이지에서 활성화 토글 껐다 켰을 때 공개 API 응답이 즉시 반영되는 것까지 확인
- **다른 영역 요청사항**: 없음. `seed.sql`에 예시 데이터(공지 1건, 팝업 1건) 추가함 — 새로 시딩하는 세션은 참고
- **06-admin-user-management.md Phase 2 체크리스트 정리**: 이 작업을 하면서 "가챠 확률 제어 UI"(사실상 이전 세션에서 만든 `GachaBannerManagementPage`와 동일 항목)와 "유저 정지"(기존 `isDeleted` 재사용으로 이미 구현됨)도 미체크 상태였던 걸 발견해 함께 체크 정리함

### 2026-07-16 — Claude Sonnet 5 / 개발자(모바일) / frontend_mobile 라이트 파스텔 디자인 적용 + 로그인·회원가입 기능 이식 (사용자 요청)

- **사용자 요청 1**: "그 어느정도 퍼블리싱한 디자인을 한게 있는데 그거 참고해서 앱 디자인 수정 가능하겠어?" → `docs/design_assets/ui_concept_light_pastel_*.jpg`(클린 파스텔 컨셉)를 참고해 `frontend_mobile` 전 화면(홈/가챠/스케줄표/게시판/내 정보)에 라이트 파스텔 테마 적용
  - `lib/theme/app_theme.dart` 신규 — 오프화이트 배경 + 파스텔 블루/핑크/민트/피치/퍼플 팔레트, 공통 `ThemeData`(카드 20px 라운드, 필/아웃라인 버튼, 인풋 데코레이션, 바텀 네비게이션 테마) + `AppTheme.softCard()` 헬퍼(은은한 그림자 카드)
  - 하단 네비게이션 흰 배경+그림자로 리스킨, 가챠 결과 카드 색상을 희귀도별 파스텔로 교체(4성=연보라/5성=연골드), 스케줄표·게시판은 "준비 중이에요" 파스텔 카드로, 내 정보는 카드형 레이아웃으로 정리
  - 실기기(Galaxy Z Fold, `adb`)에서 5개 탭 전부 스크린샷으로 확인 완료
- **사용자 요청 2**: "이제 각 화면에 맞게 기능 이식이 필요해" → `AskUserQuestion`으로 이식 순서를 물어 **로그인부터** 진행하기로 결정(게시판/내 정보 모두 로그인이 선행 조건이라 순서상 타당)
  - `lib/models/user_model.dart`, `lib/repositories/auth_repository.dart`(`AuthRepository`/`ApiAuthRepository`, 위 인증 API 계약 그대로 소비), `lib/providers/auth_providers.dart`(`AuthState`/`AuthController` — Riverpod `StateNotifier`, 로그인/회원가입/로그아웃 + `shared_preferences`로 세션 영속화) 신규
  - `lib/managers/app_bootstrap_manager.dart`에 `Ref` 주입 후 `USER_DATA` 초기화 단계에서 `authControllerProvider.notifier.restoreSession()` 호출 — 스플래시 단계에서 저장된 세션 자동 복원
  - `lib/screens/login_screen.dart` 신규(로그인/회원가입 탭 UI, 웹 `LoginPage.jsx`와 동일한 컨셉이나 Flutter 위젯으로 재구현). 구글 로그인은 이번 범위에서 제외(추후 필요 시 `google_sign_in`/`firebase_auth`로 별도 진행 — 이미 의존성은 있음)
  - `lib/screens/my_info_screen.dart`에 계정 섹션 추가 — 비로그인 시 "로그인이 필요합니다" CTA 카드(→ `LoginScreen` push), 로그인 시 이름/이메일 요약 카드 + 로그아웃 버튼. 기존 디버그(키/값 저장) 도구는 그대로 하단에 유지
  - l10n 키 추가(`ko.json`/`en.json`): `login_*`, `my_info_login_*`, `my_info_logout`
  - 실기기 e2e로 회원가입→자동 로그인→앱 완전 종료 후 재실행 시 세션 복원→로그아웃→재로그인까지 전부 확인(백엔드는 `adb reverse tcp:8080 tcp:8080`로 로컬 서버 연결)
- **다른 영역 요청사항**: 없음. 인증 API 계약 자체는 변경하지 않고 기존 계약을 그대로 소비만 함(위 인터페이스 계약 섹션에 소비처 추가 표기)
- **남은 것**: 게시판/스케줄표/내 정보(실제 프로필) 화면은 아직 각각 웹의 `StrategyBoardPage`/`SchedulerPage`/`UserProfilePage` 기능 이식이 필요 — 다음 세션에서 이어갈 것

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드가 프론트 일부 대행) / seed.sql 배너를 실제 게임 픽업 정보로 교체 (사용자 요청)

- **사용자 요청**: "지금 게임 내에서 실제 픽업 내용을 검색해서 내용 수정해줄래? 저건 뭔 맞지도 않고 픽업도 마찬가지로" — 기존 시드의 배너명/기간/픽업 캐릭터가 실제 게임과 무관한 예시 데이터였음
- **조사**: WebSearch로 2026-07-16 기준 실제 진행 배너 확인 — 원신 6.7 「산드로네」 픽업(2026-07-01~07-21, 5성 신캐 산드로네 + 5성 시틀랄리 동시 등장, 4성 라인업은 리크 소스에서도 미확정이라 기존 4성/3성 풀은 유지), 붕괴: 스타레일 4.4 「히메코·노바」 워프(2026-07-15~08-25, 5성 히메코·노바 픽업). 출처는 리크/추측 기반 블로그 종합이라 100% 확정 정보는 아님(4성 세부 라인업 등은 "미정"으로 표기된 부분 다수)
- **수정**: `seed.sql`의 캐릭터 id 1(푸리나→산드로네)/2(느비예트→시틀랄리)/6(아케론→히메코·노바)를 실제 이름으로 교체(id/rarity/gameId는 유지, 이름만 갱신). `banners` 테이블의 name/start_at/end_at을 실제 배너명·기간으로 교체. 4성/3성 풀은 리크 정보가 불확실해 기존 그대로 유지. 로컬 Postgres에 재적용 후 `GET /api/banners`·시뮬레이터 화면에서 새 배너명 반영 확인
- **다른 영역 요청사항**: 없음. 캐릭터 id 1/2/6의 **이름만** 바뀌었고 id/구조는 그대로라 기존 코드 영향 없음. 다만 이 seed 데이터는 리크/추측 정보 기반이라 시간이 지나면(다음 버전 배너로 넘어가면) 다시 구식이 됨 — 정기적으로 최신화 필요하면 이 로그를 참고해 웹 검색으로 갱신할 것

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드가 프론트 일부 대행) / 가챠 5성 확률 밸런스 버그 수정 + 가챠 배너/드랍테이블 관리자 페이지 신규 (사용자 리포트)

- **사용자 리포트**: "가챠 드랍테이블을 관리자페이지에서 배너로 수정 가능하게 해야할 것 같은데 지금 5성이 너무 잘 나와"
- **버그 원인**: `BannerService.pull()`의 자연 확률은 `최고 등급 가중치 합 / 풀 전체 가중치 합`. 시드 데이터(`seed.sql`)에 3성 캐릭터가 없어서 풀 합계가 4성까지(0.063)밖에 안 됐고, 그 결과 5성 자연 확률이 0.012/0.063 ≈ **19%**로 계산되고 있었음(의도한 값은 개별 0.006 = 절대 확률 0.6%였을 것으로 보임, 위 "가챠 API" 계약 섹션에 이 계산 방식 주석 추가함)
- **수정**: `seed.sql`에 3성 캐릭터 5명 추가(원신: 앰버/케이아/리사, 스타레일: 나타샤/아를란)하고 배너 풀 가중치를 채워서 각 배너 풀 합계를 ~1.0으로 정상화. 결과: 5성 자연 확률 1.2%, 4성 5.1~5.11%, 3성 나머지(원신 93.7%/스타레일 93.8%) — 5성/4성 가중치 자체는 건드리지 않았고 3성으로 분모만 채움. 로컬 Postgres에 재적용 후 SQL로 등급별 가중치 합 검증 완료
- **관리자 페이지 신규**: `frontend/src/pages/GachaBannerManagementPage.jsx`(+ css) — 게임 선택 → 배너 CRUD(이름/기간/천장/픽업확률) → 캐릭터 CRUD(이름/등급/아이콘) → 배너별 드랍테이블(풀) 관리(가중치 인라인 수정/픽업 토글/추가·제외) → **현재 가중치 기준 등급별 자연 확률 미리보기**(위 버그 같은 밸런스 문제를 관리자가 저장 전에 바로 확인 가능). 백엔드는 기존 admin API(`BannerController`/`CharacterController`, 파트 04에서 이미 구현 완료)를 그대로 사용 — 백엔드 코드 변경 없음. `frontend/src/api/gachaApi.js`에 `adminGachaApi` export 추가(스케줄러/게시판의 `adminApi`/`adminBoardApi`와 동일 컨벤션). 라우트 `/admin/gacha-banner` 추가, `Sidebar.jsx`/`UserProfilePage.jsx`의 SUB_ADMIN 메뉴에 링크 추가
- **검증**: eslint 통과, `npm run build` 성공. 로컬 백엔드+Vite 개발서버로 브라우저 e2e — 테스트 관리자 계정(회원가입 API로 생성 후 SQL로 `MAIN_ADMIN` 부여, 테스트 후 삭제)으로 로그인해 배너/캐릭터 목록 로드 확인, 확률 미리보기가 시드 수정값(★5 1.20%/★4 5.11%/★3 93.69%)과 일치함을 확인, 푸리나 가중치를 0.006→0.02로 변경해 PUT 200 응답과 미리보기 갱신(1.20%→2.57%) 확인 후 원복
- **다른 영역 요청사항**: 없음(백엔드 API 변경 없음). 모바일(Flutter)에는 아직 관리자 화면이 없음 — 필요해지면 별도 요청으로

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드가 모바일 대행) / Flutter 가챠 화면 실기기 e2e 검증 + 버그 수정

- **배경**: 바로 위 로그에서 모바일 전략을 Flutter 네이티브로 재확정한 뒤, 사용자가 지정한 우선순위대로 "가챠 시뮬레이터 화면 Mock→실 API 전환"부터 착수
- **발견**: `ApiGachaRepository`(`lib/repositories/gacha_repository.dart`)와 `AppConfig.useMockGacha`(기본값 `false`)는 **이미 실 API를 호출하도록 정확하게 구현돼 있었음** — 파트 04 초기 작업 당시 계약대로 잘 맞춰져 있어서 코드 자체를 고칠 필요는 없었고, 실제로 남은 일은 "정말 동작하는지 실기기로 검증"이었음
- **환경 구축**: 이 머신에 Flutter SDK가 없어서(`brew install --cask flutter`, 3.44.6) 처음 설치. `flutter pub get` 성공, `flutter analyze` 결과 가챠 관련 파일은 이슈 0건(기존 다른 파일들의 사소한 lint info/warning만 20건)
- **실기기 빌드 중 발견·수정한 버그 2건**:
  1. **AndroidManifest.xml에 release용 INTERNET 권한이 없었음** — Flutter 기본 템플릿은 `android/app/src/debug/AndroidManifest.xml`에만 디버그용 INTERNET 권한을 넣어두는데(핫리로드용), 메인 매니페스트엔 없어서 릴리스 빌드에서는 API 호출 자체가 불가능한 상태였음. 메인 `AndroidManifest.xml`에 권한 추가
  2. **Android 15+ cleartext HTTP 차단 (mobile_shell 때와 동일 이슈)** — 실기기가 Android 16(API 36)이라 확정. `android/app/src/debug/res/xml/network_security_config.xml` 신규(localhost/127.0.0.1/10.0.2.2 cleartext 허용) + `debug/AndroidManifest.xml`에 `networkSecurityConfig` 참조 추가(디버그 전용, 릴리스엔 영향 없음) — mobile_shell에서 이미 검증된 동일 패턴 재사용
- **실기기 e2e 검증 결과 (Samsung SM-F966N, Android 16)**: `adb reverse tcp:8080`으로 로컬 Postgres 백엔드에 연결 → 가챠 화면이 **seed.sql의 실제 데이터**(`[원신] 정토의 시선 - 푸리나 픽업` 배너, 베넷/느비예트/향릉/피슬 캐릭터)를 정상 표시 → 1회 뽑기(천장 0→1) → 10연 뽑기(천장 1→11, 캐릭터 10개 실데이터) 전부 실제 버튼 탭으로 확인
- **부가로 발견·수정한 UI 버그**: 배너 이름이 길면(`[원신] 정토의 시선 - 푸리나 픽업`) `DropdownButtonFormField`가 "RIGHT OVERFLOWED BY 6.7 PIXELS" 오버플로우 — `isExpanded: true` 누락이 원인(Flutter의 잘 알려진 함정: 이 옵션 없이는 선택된 값 표시 영역이 내용에 맞춰 늘어나다 오버플로우함). 추가해서 해결, 재빌드 후 실기기로 재확인
- **팁**: 네이티브 앱 UI 좌표는 스크린샷을 눈으로 보고 추정하지 말고 `adb shell uiautomator dump`로 정확한 `bounds`를 뽑아서 탭할 것 — 이번에 스크린샷 눈대중으로 하단 탭바 y좌표를 완전히 잘못 짚어서(약 1160 vs 실제 2318) 몇 번 허탕침
- **검증**: `flutter analyze` 가챠 관련 이슈 0건 유지. 코드 리뷰용 자동 테스트는 이 프로젝트에 아직 없음(frontend_mobile 전체에 테스트 파일 없음, 향후 과제)
- **다른 영역 요청사항**: 없음(API 계약 변경 없음, 새로 발견한 두 버그 모두 Android 프로젝트 설정 파일만 수정)
- **다음 예정**: 없음(이번 우선순위 항목 완료). 남은 화면(스케줄러/게시판/로그인·프로필/유저관리)은 전부 Flutter로 신규 제작 필요 — 사용자 확인 후 진행

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드가 모바일 대행) / 🔴 모바일 전략 재전환 — Capacitor 하이브리드 폐기, Flutter 네이티브로 복귀 (사용자 결정)

- **배경**: 사용자가 "생각을 잘못했다"며 2026-07-15의 하이브리드(Capacitor) 전환 결정을 번복. "기존 프론트(웹) 페이지에서 API 호출하는 걸 여기서(모바일) 기능을 따로 만들었어야 했다"는 판단 — 즉 웹뷰로 웹을 감싸는 대신, Flutter 네이티브 앱이 백엔드 API를 직접 호출하는 구조로 되돌아감
- **결정 사항 (사용자 확인)**:
  1. **전환 범위**: 완전 전환. `mobile_shell`(Capacitor)은 중단(deprecated) — 코드는 삭제하지 않고 보관만 하되 신규 작업 없음. `frontend_mobile`(Flutter)이 유일한 모바일 앱으로 재확정. 웹(`frontend`)은 이제 PC 브라우저 대응만 신경 쓰면 됨(모바일 브라우저 반응형은 계속 유지해도 되지만 우선순위 아님)
  2. **작업 순서**: 가챠 시뮬레이터 화면(이미 존재, Mock으로 동작 중)을 실 API로 전환하는 것부터 우선 착수. 이후 스케줄러/게시판/로그인·프로필·유저관리 화면을 Flutter로 신규 제작 예정(이번 세션엔 미착수)
- **다른 세션 영향**: `mobile_shell`에 오늘 투입된 작업(Google 로그인 외부 브라우저 위임+딥링크, Flutter 스타일 하단탭바, 앱 아이콘/스플래시, cleartext 네트워크 설정 등)은 전부 유지만 되고 더 이상 발전 안 시킴 — **frontend/mobile_shell 담당 세션은 이 로그를 보는 즉시 mobile_shell 작업을 중단할 것**. 위 "다른 영역에 대한 요청"에 굵게 표시해둠
- **다음 예정**: 이 세션이 이어서 `frontend_mobile/lib/repositories/`의 가챠 Mock 저장소를 실제 백엔드 API 호출로 전환 착수 (아래 다음 로그 참고)

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드·프론트) / 이메일/비밀번호 로그인·회원가입 추가 (사용자 요청)

- **배경**: 사용자가 Google 로그인 도메인 얘기를 하다가 "아이디/비밀번호 로그인, 회원가입, 구글 로그인 이렇게 세 트랙으로 나눌 수 있다"고 제안. 스코프 확정을 위해 3가지를 물어봤고 전부 추천안으로 확정: (1) 로그인 아이디는 이메일 (2) 구글로 이미 가입된 이메일로 비밀번호 가입 시도하면 **같은 계정에 비밀번호만 추가**(계정 통합) (3) 비밀번호 재설정 기능은 나중에
- **백엔드 작업 내용**:
  - `UserEntity`에 nullable `passwordHash` 컬럼 추가(구글 전용 계정은 null, 응답 DTO에는 절대 노출 안 함 — `UserMapper`/`UserResponseDto`가 필드별로 명시 매핑이라 원천적으로 안전)
  - `UserService.signup(email, rawPassword, name)`: 이메일이 없으면 신규 생성, 있는데 `passwordHash`가 null(구글 전용)이면 비밀번호만 추가해 병합, 이미 `passwordHash`가 있으면 `IllegalStateException`(컨트롤러에서 409로 변환). `UserService.authenticateWithPassword(email, rawPassword)`: `passwordHash`가 없거나 불일치하면 empty 반환. `BCryptPasswordEncoder`는 `UserService` 필드에서 바로 `new`로 초기화(이미 초기화돼 있어 Lombok `@RequiredArgsConstructor` 생성자 파라미터에서 제외됨 — 별도 `PasswordEncoder` 빈을 여기저기 안 만들어도 됨, 기존 `@DataJpaTest` 슬라이스 테스트와도 충돌 없음)
  - `AuthController`에 `POST /api/auth/signup`(이메일/비밀번호 8자 이상/이름 검증, 성공 시 `POST /api/auth/google`과 동일한 형태로 토큰 포함 응답 — 가입 즉시 로그인), `POST /api/auth/login`(실패 시 401) 추가. 기존 googleLogin의 DTO 조립 로직을 `toResponseDto()`로 추출해 3개 엔드포인트가 공유
  - `SecurityConfig`의 permitAll 목록에 `/api/auth/signup`, `/api/auth/login` 추가
- **프론트 작업 내용**: `LoginPage.jsx`를 로그인/회원가입 탭 + 이메일·비밀번호 폼 + 기존 Google 버튼(또는 앱 셸의 외부 브라우저 위임 버튼)을 한 화면에 구성하도록 재작성. 성공 처리 로직(`completeLogin`)을 공유해서 브릿지 탭(외부 브라우저)에서 완료됐을 때의 딥링크 리다이렉트가 구글/이메일 로그인/회원가입 셋 다 동일하게 동작. 인라인 스타일을 `LoginPage.css`로 정리
- **검증**: 백엔드 `./gradlew test` 전체 통과(92개, `UserServiceTest`에 signup/병합/중복거부/비밀번호오류/구글전용계정 케이스 5개, `AuthControllerTest` 신규(signup 성공/짧은 비밀번호 거부/중복 409/login 성공/오답/구글전용계정), `SecurityConfigTest`에 permitAll 케이스 2개 추가). curl로 실 로컬 Postgres에 대해 회원가입→로그인→오답→중복가입 4가지 직접 확인. 프론트 `npm run lint`/`build` 통과(CSS 경고 없음)
- **실기기 검증(SM-F966N)**: 새 로그인 화면 렌더링(로그인/회원가입 탭, Google 버튼 공존) 스크린샷 확인. CDP로 폼에 입력·제출해 **실제 이메일/비밀번호 로그인 성공**(헤더에 가입 시 이름 표시, 홈 이동) 확인, 오답 비밀번호 시 에러 문구("이메일 또는 비밀번호가 올바르지 않습니다.") 렌더링 확인. 앱 셸 전용 Google 버튼(외부 브라우저 위임)은 이전 로그에서 이미 검증된 경로라 이번엔 회귀만 확인
- **다른 영역 요청사항**: 없음(신규 API 추가만, 기존 `/api/auth/google` 계약 변경 없음) — 인터페이스 계약 섹션에 신규 추가함
- **다음 예정**: 없음(이번 요청 범위 완료). 비밀번호 재설정(이메일 발송)은 사용자가 나중에 필요하다고 하면 별도 파트로

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드가 프론트 일부 대행) / 제품 완성도 점검 + 우선순위 높은 항목 3건 구현

- **배경**: 사용자가 "백엔드 말고 이 앱의 중요 요소 중 비어있는 게 뭔지 체크해달라"고 요청. 프론트 페이지 전체/자산/테스트/법적 페이지를 훑어서 정리한 뒤, "우선순위 높은 것부터, 정책은 후순위로" 지시받아 아래 3건 구현
  1. **React ErrorBoundary** — `components/ErrorBoundary.jsx` 신규, `main.jsx`에서 `AuthProvider` 바깥을 감쌈. 컴포넌트 크래시 시 흰 화면 대신 "홈으로 이동" 버튼이 있는 안내 화면 노출
  2. **404 페이지** — `pages/NotFoundPage.jsx` 신규, `App.jsx`의 `MainLayout` 자식 라우트 끝에 `<Route path="*" .../>` 추가
  3. **게시판 검색** — 백엔드: `PostRepository.searchByChannelId`(JPQL, 제목/내용 대소문자 무시 LIKE), `PostService.getPostsByChannel(channelId, query, pageable)`로 시그니처 변경(query가 비어있으면 기존 동작), `PostController`에 `?query=` 파라미터 추가. 프론트: `boardApi.fetchPosts`에 query 파라미터 추가, `StrategyBoardPage`에 검색창+초기화 버튼 추가(채널 전환 시 검색어 리셋)
  - **점검 결과 중 이번엔 안 고친 것(사용자가 정책 항목은 후순위로 미룸)**: 이용약관/개인정보처리방침/DMCA 연락처 페이지 없음, 앱 아이콘이 아직 Capacitor 기본 아이콘, PWA manifest.json 없음, OG 메타 태그 없음, 프론트 테스트 파일 0개, **이미지 업로드 기능 전무**(배너/캐릭터/프로필 사진 전부 URL 텍스트 입력뿐 — S3/Cloudinary/로컬디스크 중 저장소 선택 필요해 사용자에게 물어봤고 "지금은 보류"로 결정됨, 필요해지면 이 결정부터 다시 해야 함)
- **검증**: 백엔드 `./gradlew test` 전체 통과(92개 — 이 세션에서 다른 세션이 동시에 추가 중이던 이메일/비밀번호 회원가입 기능 테스트까지 포함해서 전부 통과 확인). 프론트 `npm run lint`/`npm run build` 통과(기존 무관 에러 1건 외 없음). 로컬 Postgres에 채널/게시글 시드해서 브라우저로 검색→필터링→초기화까지 실제 클릭 테스트 확인
- **동시 작업 주의사항 발견**: 이 작업 도중 다른 세션이 `SecurityConfig.java`/`UserService.java`에 이메일/비밀번호 인증(`/api/auth/signup`, `/api/auth/login`, BCrypt)을 동시에 추가하고 있었음 — 같은 로컬 백엔드 디렉터리에서 `./gradlew test`를 동시에 두 세션이 돌리면 `build/test-results`에 XML 쓰기 충돌이 발생할 수 있음(재시도하면 해결됨, 코드 문제 아님). 여러 세션이 동시에 로컬 백엔드를 만질 땐 이런 일시적 충돌이 있을 수 있다는 것 참고
- **다른 영역 요청사항**: 없음(API 계약은 `?query=` 파라미터 추가뿐, 기존 응답 형태 변경 없음)
- **다음 예정**: 없음(이번 요청 범위 완료). 이미지 업로드는 저장소 결정 시 재논의

### 2026-07-16 — Claude Sonnet 5 / 개발자(프론트·모바일) / `mobile_shell` Google 로그인을 외부 브라우저 위임 방식으로 구현 (사용자 결정에 따름)

- **배경**: 직전 로그(서버 DB·로그인 테스트)에서 발견한 "Google이 임베디드 WebView를 서버 단에서 차단"하는 문제에 대해 사용자에게 방향을 물었고, "외부 브라우저로 위임(추천)"을 선택 — Chrome Custom Tabs로 로그인시키고 완료되면 앱으로 돌아오는 방식
- **작업 내용**:
  - `mobile_shell/package.json`에 `@capacitor/browser@^7`, `@capacitor/app@^7` 추가(기존 `StatusBar`처럼 `window.Capacitor.Plugins.<Plugin>` 전역 프록시로 사용 — `frontend/package.json`엔 아무것도 추가 안 함, 프론트 코드는 항상 이 전역 접근 패턴을 씀)
  - `mobile_shell/android/app/src/main/AndroidManifest.xml`: 커스텀 스킴 `gachascheduler://auth-callback`용 `intent-filter` 추가(기존 `launchMode="singleTask"`라 별도 변경 불필요)
  - `frontend/src/pages/LoginPage.jsx`: `window.Capacitor?.isNativePlatform?.()`로 앱 셸 여부 판별 → 앱 셸이면 기존 `GoogleLogin` 위젯 대신 "Google 계정으로 로그인" 버튼을 두고, 클릭 시 `Browser.open({ url: `${VITE_OAUTH_BRIDGE_URL}/login?bridge=1` })`으로 시스템 브라우저를 띄움. 같은 `LoginPage` 컴포넌트가 그 브라우저 탭에서 `?bridge=1`로 로드되면(일반 브라우저라 `isNativePlatform()`이 false라 정상적으로 `GoogleLogin` 위젯이 렌더링됨) 로그인 성공 시 백엔드 JWT를 받은 뒤 `location.href`를 `gachascheduler://auth-callback?token=...&user=...`로 리다이렉트
  - `frontend/src/App.jsx`: `NativeAuthBridge` 컴포넌트 신규 — `window.Capacitor.Plugins.App.addListener('appUrlOpen', ...)`로 위 딥링크를 받아 `login()` 호출 + `navigate('/')` + `Browser.close()`로 탭 자동 종료
  - `frontend/.env`/`.env.example`에 `VITE_OAUTH_BRIDGE_URL`(기본 `http://localhost:5173`) 추가 — 배포 시엔 실 도메인으로 교체 필요
- **버그를 하나 만들었다가 바로 잡음**: 처음엔 `appPlugin.addListener(...)`가 `Promise<PluginListenerHandle>`를 반환한다고 가정하고 `listenerPromise.then(...)`으로 cleanup을 짰는데, 실기기에서 `TypeError: c.then is not a function`으로 **React 전체가 크래시**(화면이 완전히 빈 채로 나옴)하는 걸 실기기 CDP로 잡아냄 — React StrictMode가 effect를 mount→cleanup→mount로 한 번 더 돌리는 시점에 터짐. `Promise.resolve(appPlugin.addListener(...)).then(...)`으로 감싸서 반환 타입에 안전하게 대응하도록 수정, 재검증 후 정상 동작 확인. **다른 세션이 알아두면 좋을 점**: `window.Capacitor.Plugins.*`의 네이티브 프록시 메서드가 항상 Promise를 반환한다고 가정하지 말 것(이번처럼 `Browser.close()`도 `Promise.resolve(...).catch(...)`로 방어적으로 감쌌음)
- **검증(실기기, SM-F966N)**: eslint/`npm run build` 통과. 로컬 Vite dev server(`localhost:5173`)를 띄우고 `adb reverse tcp:5173`·`tcp:8080`으로 연결 후:
  1. CDP로 로그인 화면의 "Google 계정으로 로그인" 버튼을 클릭 → **Chrome Custom Tab이 실제로 열리고, 그 안에서 진짜 Google 로그인 버튼(로고 포함)이 정상 렌더링됨을 스크린샷으로 확인**(임베디드 웹뷰에서는 403으로 아예 안 뜨던 것과 대조)
  2. 실제 Google 계정 로그인은 세션이 대행할 수 없어서, 대신 `adb shell am start -a android.intent.action.VIEW -d "gachascheduler://auth-callback?token=...&user=..."`로 콜백 딥링크를 직접 발사해 "로그인 성공 후"의 앱 쪽 처리를 검증 — 앱이 포그라운드로 복귀하고, 헤더에 로그인된 사용자명이 뜨고, 홈으로 이동하는 것까지 스크린샷으로 확인(브라우저 탭은 자동으로 닫혀서 화면에 안 보임)
- **한계/알아둘 점**: 브릿지 페이지(`/login?bridge=1`)가 실제로 접속 가능한 주소여야 함 — 로컬 개발 중엔 `adb reverse tcp:5173 tcp:5173`가 항상 필요(잊으면 Custom Tab이 빈 페이지/연결 실패를 보여줌). 프로덕션 배포 시엔 `VITE_OAUTH_BRIDGE_URL`을 실 도메인(예: `https://gacha-scheduler.example.com`)으로 바꿔야 함
- **다른 영역 요청사항**: 없음(프론트/모바일 전용, 백엔드 `/api/auth/google` 계약은 그대로 재사용). `docs/HANDOVER.md`의 "실제로 시간 날린 것들" 2번 항목도 "미해결"에서 "해결 완료"로 갱신함

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드) / 신규 인계 문서(HANDOVER.md) 작성 (사용자 요청)

- **작업 내용**: 새로 합류하는 AI 세션이 이 SYNC.md 로그 60여 개를 처음부터 다 안 읽어도 되도록, **항상 최신 상태로 덮어쓰는 스냅샷** `docs/HANDOVER.md` 신규 작성. 로그 전체를 훑어서 다음을 정리: 5분 로컬 환경 기동법, 파트별 진행 상태 요약, 시크릿 파일 위치, 그리고 가장 중요한 **"실제로 시간 날렸던 것들" 8개 항목**(Hibernate 지연 로딩 함정, mobile_shell Google 로그인 구조적 불가, JWT role staleness, 가챠 뽑기 기록 미영속, userCode 이중 용도, CSS 중첩 주석 버그, Android 15 엣지투엣지/cleartext 차단, findOrCreateUser 동작)을 한 곳에 압축
- **다른 문서 연결**: `docs/SYNC.md` 상단과 루트 `README.md`의 "AI 병렬 작업 시 필독"에 HANDOVER.md 링크 추가(새 세션이 SYNC.md를 다 읽기 전에 먼저 보도록 유도)
- **갱신 원칙**: 이 문서는 SYNC.md처럼 append하지 않고, 상태가 바뀔 때마다 해당 섹션을 덮어쓰는 방식으로 유지한다 — 다음 세션들도 이 원칙을 지켜줄 것
- **다른 영역 요청사항**: 없음(문서만 추가, 코드/계약 변경 없음)

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드) / 파트 09(배포) 준비물 — 운영 설정 + systemd/nginx 템플릿 + 프로비저닝 런북

- **배경**: 기능 파트(01~08) 백엔드가 사실상 다 끝나서 "남은 건 데브옵스"라는 데 합의. 사용자가 새 AWS 계정을 만드는 동안(예전 계정은 프리티어 소진) 계정 없이 먼저 할 수 있는 준비물을 요청받음
- **배포 아키텍처 결정**: 트래픽이 거의 없는 초기 단계라 EC2 t4g.micro/t3.micro 1대 + systemd(컨테이너/ECS 없음) + nginx(TLS 종료) + RDS(같은 리전) 조합으로 결정 — ECS Fargate+ALB보다 저렴하고 지금 규모엔 이거면 충분. 상세 근거는 `docs/plans/09-deployment.md` 참고
- **작업 내용**:
  - `application-prod.properties`(`SPRING_PROFILES_ACTIVE=prod`) 신규 — 에러 응답 스택트레이스/내부 메시지 숨김, `spring.jpa.open-in-view=false`(이 프로젝트는 애초에 지연 연관관계를 안 쓰는 컨벤션이라 꺼도 안전), SQL 로그 끔, actuator는 `/actuator/health`만 노출
  - `build.gradle`에 `spring-boot-starter-actuator` 추가, `SecurityConfig`에 `/actuator/health` permitAll 추가(로드밸런서 헬스체크가 토큰 없이 접근해야 함)
  - `backend/backend_gacha/deploy/`에 3개 템플릿 신규: `gacha-scheduler-backend.service`(systemd, 전용 유저로 실행·힙 512m 제한·실패 시 자동 재시작), `backend.env.example`(운영 환경변수 템플릿), `nginx-gacha-scheduler.conf.example`(리버스 프록시, certbot으로 TLS 붙이는 법 주석 포함)
  - `docs/plans/09-deployment.md` 신규 — 위 아키텍처 결정 근거 + RDS/EC2 프로비저닝 순서를 단계별 런북으로 정리(AWS 계정 준비되면 그대로 따라가면 됨)
- **검증**: `SecurityConfigTest`에 `/actuator/health` 공개 접근 케이스 추가, `./gradlew test` 전체 통과(78개, 기존 77 + 신규 1)
- **다른 영역 요청사항**: 없음(신규 파일/설정만 추가, 기존 API 계약 변경 없음). 프론트 세션에 참고용으로 남기는 것: 배포 후 `frontend/.env`의 `VITE_API_BASE_URL`을 배포 도메인으로, `mobile_shell`은 원격 URL 모드 전환 필요 — 둘 다 09번 문서에 적어둠
- **다음 예정**: 없음(이번 요청 범위 완료). 사용자가 AWS 계정 만들고 나면 09번 문서의 런북대로 RDS/EC2 프로비저닝 진행

### 2026-07-16 — Claude Sonnet 5 / 개발자(프론트·백엔드) / 서버 DB 연결·로그인 검증 — **`mobile_shell`(Capacitor 앱 셸)에서 Google 로그인이 구조적으로 불가능함을 발견**

- **배경**: 사용자가 "서버 DB 연결 테스트 및 로그인 테스트"를 요청. SYNC.md 확인 결과 백엔드가 로컬 PostgreSQL로 상시 기동 중이고 실 Google Client ID도 최근 반영된 상태였어서, 둘 다 실제로 검증
- **DB 연결 테스트 — 정상**: `ps aux`로 `bootRun`/`postgres` 프로세스 확인, `psql`로 테이블 12개 전부 존재·유저 2명(관리자/일반) 시드 확인, `curl /api/games`가 실 DB의 원신/스타레일 데이터를 200으로 반환. `/api/auth/google`에 깨진 idToken을 보내면 500(정상 에러 처리), JWT 서명·만료를 이 세션의 로컬 `jwt.secret`으로 위조해서 `/api/users/me`를 찔러보니 실제 DB의 관리자 레코드(`admin@example.com`)를 200으로 정확히 반환 — JWT 인증 파이프라인이 로컬 Postgres와 완전히 정상 동작함을 확인. 위조/누락 토큰은 둘 다 401로 정상 거부
- **로그인 테스트 — 치명적 발견**: `frontend/.env`가 마지막 프론트 빌드(12:30)보다 늦게(12:32) 생성된 걸 발견해서 재빌드 → 실 Client ID가 번들에 반영된 걸 확인 후 `mobile_shell`에 재배포. 실기기 CDP로 `/login` 진입 후 Google 로그인 버튼(iframe)이 **전혀 렌더링되지 않음**을 확인. 네트워크 로그에 `net::ERR_BLOCKED_BY_ORB`가 찍혀서 원인 추적:
  - 기기 UA를 확인해보니 `...Chrome/150.0.7871.46 Mobile Safari/537.36` 안에 **`; wv)`**(Android WebView 마커)가 포함돼 있음
  - 호스트에서 동일 UA로 `curl -A "<그 UA>" https://accounts.google.com/gsi/client`를 실행해보니 **HTTP 403 + Google 표준 에러 페이지**가 응답. 반면 일반 데스크톱 Chrome UA로 같은 요청을 보내면 200 + `application/javascript` 정상 응답
  - 즉 **Google이 서버 단에서 User-Agent의 `; wv)`(임베디드 WebView) 마커를 감지해서 GIS 스크립트/OAuth 엔드포인트 자체를 차단**하는 것(2021년부터 시행 중인 Google의 공식 보안 정책 — 임베디드 웹뷰를 통한 자격증명 피싱 방지 목적). 우리 쪽 CSP/CORS/네트워크 설정 문제가 전혀 아니고, **Client ID를 아무리 올바르게 설정해도 Capacitor WebView 안에서는 절대 해결 불가능**한 구조적 제약
- **영향 범위**: `mobile_shell`(하이브리드 앱)의 Google 로그인만 불가능. **데스크톱 웹/모바일 브라우저(일반 Chrome/Safari 등)는 전혀 영향 없음**(위 curl 테스트로 확인) — `frontend`(웹) 자체의 로그인 기능은 정상
- **다른 영역 요청사항**: 위 "미해결" 섹션에 추가함 — 사용자/planner가 방향(외부 브라우저로 로그인 위임 vs 네이티브 SDK 플러그인 vs 로그인만 웹으로 유도) 결정 필요. 결정 전까지는 **앱 셸에서 Google 로그인 관련 추가 디버깅에 시간 쓰지 말 것**(우리 코드 문제가 아님이 확정됐음)
- **참고**: 재현 방법은 실기기/에뮬레이터의 `navigator.userAgent`를 확인해 `; wv)` 포함 여부를 보거나, 그 UA로 직접 `curl -A "<UA>" https://accounts.google.com/gsi/client`를 쳐서 403 여부로 바로 확인 가능(브라우저/CDP 없이도 검증됨)

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드) / 실 Google OAuth Client ID 반영 (사용자 제공)

- **작업 내용**: 사용자가 Firebase 콘솔(`gacha-scheduler` 프로젝트)에서 Google 로그인을 활성화해 재발급받은 `google-services.json`에서 Web 타입(`client_type: 3`) OAuth 클라이언트 ID를 확인 — `435890288277-pj54fjvqlr1bjct6nsuqhk4da9pl7285.apps.googleusercontent.com`. 백엔드 `application-local.properties`의 `GOOGLE_CLIENT_ID` 플레이스홀더를 이 값으로 교체하고, 프론트 `frontend/.env`(기존에 없어서 `.env.example` 기준으로 신규 생성)의 `VITE_GOOGLE_CLIENT_ID`에도 동일 값 반영. 백엔드 재기동해서 반영 확인
- **여전히 필요**: 실제 Google 계정으로 로그인 버튼을 눌러보는 e2e는 세션이 대행 못 함(위 미해결 요청 참고) — 안 되면 GCP Console에서 해당 OAuth 클라이언트의 "승인된 JavaScript 원본"에 `http://localhost:5173` 등록 여부부터 확인할 것
- **다른 영역 요청사항**: 없음(로컬 설정 파일만 갱신, API 계약 변경 없음)

### 2026-07-16 — Claude Sonnet 5 / 개발자(프론트) / 내정보 테마 토글을 해/달 애니메이션 버튼에서 표준 스위치로 교체 (사용자 요청)

- **작업 내용**: 사용자가 "테마 변경 토글을 그냥 일반적인 토글로 바꿔달라"고 요청. 기존 `ThemeToggleButton`(해/달 아이콘이 회전하는 커스텀 버튼)을 제거하고, `UserProfilePage.jsx`의 "설정" 섹션에 `input[type=checkbox]` 기반의 표준 pill 스위치(`.toggle-switch`)를 직접 구현. `ThemeToggleButton`이 이제 어디서도 쓰이지 않아 `components/ThemeToggleButton.jsx`·`.css` 파일 자체를 삭제(헤더에서도 이미 지난 로그에서 제거된 상태였음)
- **검증**: eslint(기존 무관 에러 1건 외 신규 없음, 오히려 삭제로 인해 기존에 있던 `ThemeToggleButton`의 미사용 `theme` prop lint 에러 1건이 함께 사라짐)/`npm run build`(CSS 경고 없음) 통과. 실기기 재배포(force-stop 후 재실행) 후 CDP로 토글 클릭 → 다크모드 정상 전환 확인, 스크린샷으로 표준 스위치 UI(off: 회색 트랙, on: 파란 트랙+우측 정렬) 확인
- **다른 영역 요청사항**: 없음(프론트 전용 UI 교체)

### 2026-07-16 — Claude Sonnet 5 / 개발자(프론트) / 버그 수정 — 하단 탭바가 실기기에서 `position:fixed`를 잃고 화면 하단에서 깨져 보이던 문제

- **배경**: 사용자가 "네비게이션 바가 깨져있다"고 지적. 직전 로그(헤더 정리) 배포 후 실기기에서 하단 탭바가 고정되지 않고 문서 흐름 맨 아래에 5개 탭이 세로로 240px 높이만큼 뭉개져 나타남(디바이스 CDP로 `getComputedStyle(nav).position`이 `fixed`가 아니라 `static`으로 나오는 것까지 직접 확인)
- **원인**: `frontend/src/index.css`에 CSS 주석이 중첩되어 있었음 — "Remove media query..." 주석(103행)이 여는 `/*` 바로 다음 줄에 `/* Default padding for smaller screens */`라는 **한 줄짜리 완결된 주석**이 끼어 있어, CSS는 주석 중첩을 지원하지 않으므로 그 지점에서 바깥 주석이 조기 종료됨. 그 결과 뒤에 있던, 원래는 죽은 코드였어야 할 vite 기본 템플릿 잔재(`color:#213547; background-color:#ffffff; } a:hover{...} button{...} } */`)가 실제 라이브 CSS로 파싱되면서 문법이 깨진 채로 남아있었음(빌드 시 esbuild가 "Unexpected \"}\"" 경고를 계속 띄우고 있었는데 지금까지는 무시됨). 이번에 `UserProfilePage.css`에 새 규칙을 추가하면서 번들 내 청크 배치가 바뀌어, 이 깨진 조각이 하필 `.mobile-bottom-nav{...}` 규칙 바로 앞에 위치하게 됐고, 안드로이드 WebView의 CSS 파서가 데스크톱 Chrome과 달리 이 시점에서 복구하지 못하고 `.mobile-bottom-nav` 규칙 자체를 통째로 드롭해버림(`.mobile-bottom-nav-item`류는 그 뒤에 있어서 영향 없었음) — 그래서 겉보기엔 하단 탭바가 `position:fixed`를 완전히 잃고 일반 블록처럼 문서 맨 끝에 쌓여 렌더링된 것
- **작업 내용**: `index.css`의 깨진 중첩 주석과 죽은 코드(vite 기본 템플릿 잔재) 전체를 제거. 실제 쓰이던 `@media (min-width:768px/1024px)` 반응형 `--main-padding` 규칙 2개만 남김
- **검증**: `npm run build` 시 esbuild CSS 경고가 완전히 사라짐(이전엔 항상 4건 발생 중이었음 — 이번에 처음 알아챔). 실기기에 재배포 후 **완전히 강제 종료(force-stop) 후 재실행**해 CDP로 `getComputedStyle(nav).position === 'fixed'`, `bottom: '0px'`, 높이 ~109px(정상)을 확인, 콘텐츠가 긴 내정보 화면에서도 동일하게 고정됨을 확인, 스크린샷으로도 5탭 모두 화면 하단에 정상 고정된 것 확인
- **다른 영역 요청사항**: 없음(CSS 전용 수정, 죽은 코드 제거라 API/레이아웃 계약 변경 없음)
- **다른 세션이 알아두면 좋을 점**: 이번처럼 "빌드는 성공하지만 esbuild가 CSS 경고를 띄우는" 상태는 프로덕션 번들에서 인접 규칙을 통째로 깨뜨릴 수 있는 잠재 버그이니, 향후 CSS를 추가하는 세션은 `npm run build` 로그의 `[WARNING] ... css-syntax-error`를 무시하지 말고 그때그때 원인 CSS 파일을 정리할 것

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드) / 로컬 PostgreSQL로 전환 + 백엔드 지속 기동 (사용자 요청)

- **배경**: 프론트/모바일에서 일정·채널 목록이 안 뜬다는 문의 → 확인해보니 원인은 "DB 연결 실패"가 아니라 **백엔드 자체가 아예 안 떠 있었음**(지금까지는 검증할 때만 H2로 잠깐 띄웠다가 세션 끝나면 종료했음). 사용자에게 로컬 Postgres/실제 RDS/H2 지속 중 선택지를 물었고, "지금은 로컬 Postgres로 쓰고 나중에 RDS로 마이그레이션 가능하게" 하기로 결정
- **작업 내용**:
  - `brew install postgresql@16`, DB `gacha_scheduler` + 전용 계정 `gacha_app` 생성(비밀번호는 `openssl rand`로 생성, 어디에도 평문 커밋 안 함)
  - `backend/backend_gacha/src/main/resources/application-local.properties`(기존부터 gitignore 대상) 신규 작성 — `DB_URL=jdbc:postgresql://localhost:5432/gacha_scheduler`, `DB_USERNAME=gacha_app`, `DB_PASSWORD=<생성값>`, `JWT_SECRET=<생성값>`. `GOOGLE_CLIENT_ID`는 아직 플레이스홀더(실 구글 로그인은 여전히 사용자 조치 필요 항목, 미해결 요청 참고)
  - `./gradlew bootRun`으로 기동 확인(Java 21 toolchain 자동 사용) → Hibernate `ddl-auto=update`가 12개 테이블 전부 생성(PendingSchedule/ServerCostSetting 포함) → `seed.sql`을 로컬 DB에 적용해 원신/스타레일 게임·일정·배너·캐릭터·테스트 유저 시드
  - **RDS로 나중에 옮길 때**: 코드/드라이버/dialect 변경 전혀 필요 없음. `application-local.properties`(또는 배포 환경의 실제 env var)의 `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` 세 값만 RDS 엔드포인트/계정으로 바꾸면 끝 — 애초에 파트 01에서 이 셋을 env var로 분리해둔 이유가 이거였음. 상세는 `docs/plans/01-security-hardening.md`의 "로컬 개발 DB" 섹션 참고
- **검증**: `curl localhost:8080/api/games`·`/api/channels`·`/api/schedules`·`/api/banners`가 전부 200 + 시드된 실데이터 반환 확인. `psql -c '\dt'`로 12개 테이블 전부 생성 확인
- **알아둘 점(재시작 방법)**: `brew services start postgresql@16`이 이 세션의 샌드박스 환경에서는 launchd 문제로 실패해서, 지금은 `postgres` 프로세스를 직접 백그라운드로 띄운 상태(이 세션이 끝나면 같이 죽을 수 있음). 사용자의 실제 터미널에서는 `brew services start postgresql@16`이 정상 동작할 가능성이 높음 — 재부팅 후에도 자동 시작을 원하면 그쪽으로 재시도해볼 것. 백엔드는 `cd backend/backend_gacha && ./gradlew bootRun`으로 아무 세션에서나 재기동 가능(로컬 Postgres만 떠있으면 됨)
- **다른 영역 요청사항**: 없음(로컬 인프라 구성, API 계약 변경 없음). 프론트/모바일 세션은 이제 `http://localhost:8080`(에뮬레이터는 `http://10.0.2.2:8080`, 실기기는 `adb reverse tcp:8080 tcp:8080`)으로 언제든 실제 데이터 붙여서 확인 가능
- **여전히 미해결**: 실제 Google 로그인 e2e(플레이스홀더 Client ID라 여전히 안 됨), 실제 AWS RDS 전환(사용자가 RDS 준비 후 값만 교체하면 됨, 위 참고)

### 2026-07-16 — Claude Sonnet 5 / 개발자(프론트) / 헤더 정리 — 로그아웃·테마 토글을 내정보 화면 안으로 이동 (사용자 요청)

- **작업 내용**: 사용자가 "헤더의 로그인 아이디를 누르면 내정보로 이동(기존 그대로 유지), 로그아웃 버튼과 테마 토글은 내정보 안쪽으로 옮겨달라"고 요청. `Header.jsx`에서 `ThemeToggleButton`과 로그아웃 버튼을 제거(이제 사용자 이름 링크만 남음, 클릭 시 기존처럼 `/profile`로 이동 — 변경 없음). `theme`/`toggleTheme`은 `MainLayout`의 `<Outlet context={{ theme, toggleTheme }} />`로 전달해 `UserProfilePage`에서 `useOutletContext()`로 받아 쓰도록 배선. `UserProfilePage`에 "설정" 섹션을 새로 추가해 기존 `ThemeToggleButton` 컴포넌트(재사용)와 로그아웃 버튼을 배치 — 데스크톱/모바일 모두 동일하게 적용(모바일 전용 분기 아님, 이제 테마/로그아웃 접근 경로가 하나로 통일됨)
- **검증**: eslint/`npm run build` 통과(기존에 있던 무관한 경고 2건 외 신규 에러 없음). 실기기(SM-F966N)에 재배포 후 CDP로 확인 — 헤더에 "테스터"만 남고 로그아웃/테마 버튼 사라짐, 내정보 진입 시 "설정" 섹션에서 테마 토글 클릭 시 전체 페이지가 즉시 다크모드로 전환되는 것과 로그아웃 버튼 노출을 스크린샷으로 확인
- **다른 영역 요청사항**: 없음(프론트 전용, API 계약 변경 없음). 다만 `UserProfilePage`가 이제 `useOutletContext()`에 의존하므로, 향후 이 페이지를 `MainLayout` 바깥의 다른 라우트 트리에서 재사용할 경우 theme/toggleTheme을 별도로 넘겨줘야 함

### 2026-07-16 — Claude Sonnet 5 / 개발자(프론트·모바일) / 모바일 하단 탭바 구성 정정 — '홈' 탭 누락 수정 (사용자 지적)

- **배경**: 바로 아래 로그(Claude Fable 5)에서 하단 탭바를 스케줄러/시뮬레이터/채널/기록/내정보 5개로 구성했는데, 사용자가 "원래 홈 화면이 있었는데 없어진 것 같다"고 지적. 확인해보니 Flutter 원본(`frontend_mobile/lib/screens/home_screen.dart`)의 실제 탭 구성은 일정/가챠/**홈(중앙, 기본 선택)**/게시판/내정보였고, `기록`은 Flutter에 아예 존재하지 않는 개념이었음 — 바로 아래 로그의 "Flutter가 쓰던 것과 동일한 아이콘 재사용"이라는 서술은 아이콘 스타일만 참고했을 뿐 탭 구성 자체는 Flutter와 다르게 짜여 있었던 것이 이번에 드러난 불일치임
- **작업 내용**: `MobileBottomNav.jsx`의 탭 구성을 Flutter와 동일한 순서(스케줄러/시뮬레이터/**홈**/채널/내정보)로 정정. `기록` 탭은 제거하고, 대신 `UserProfilePage`(내 정보)에 "내 활동 > 뽑기 기록" 링크를 추가해 로그인한 모바일 사용자가 계속 접근할 수 있게 함(데스크톱은 기존 Sidebar에 `기록`이 그대로 있어 변경 없음). `HomePage.jsx`의 안내 문구도 더 이상 존재하지 않는 "왼쪽 사이드바" 언급을 "하단(모바일)/좌측(PC) 메뉴"로 수정
- **검증**: eslint/`npm run build` 통과. 실기기(SM-F966N)에 재배포 후 확인 — 앱 실행 시 '홈' 탭이 기본 활성 상태로 표시(스크린샷), CDP로 "내 정보" 탭 이동 시 "내 활동 > 뽑기 기록" 링크와 기존 관리자 메뉴가 함께 정상 노출
- **다른 영역 요청사항**: 없음(프론트 전용 수정, API 계약 변경 없음)

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드) / 파트 08(수익화 전략) 백엔드 — 서버비 프로그레스 바 API

- **작업 내용**: 사용자가 "나머지 백엔드 부분"을 이어서 하라고 해서, `docs/plans/06-admin-user-management.md`에 Phase 2로 예고돼 있던 `GET/PUT /api/admin/settings/server-cost`를 구현. `docs/plans/08-monetization-strategy.md`의 나머지 항목(네이티브 광고, 포인트 시스템+보상형 비디오)은 백엔드 대상이 아니거나(광고는 전부 프론트+퍼블리셔 계정 설정) 구체적인 데이터 모델이 기획서에 아직 없어서(포인트 원장/상점 아이템 종류/광고 SDK 미확정) 손대지 않음 — 임의로 스키마를 먼저 짜면 기획 확정 후 갈아엎을 위험이 커서 보류
  - `ServerCostSettingEntity`(id=1 고정 싱글톤 행: targetAmount/currentAmount) 신규. `GET /api/settings/server-cost`(비로그인 공개 — 메인/사이드바 게이지가 로그인 여부와 무관하게 항상 떠야 하므로) 응답에 targetAmount/currentAmount/percentage(0~100, 목표 초과 달성 시 100에서 클램프하되 원본 금액은 그대로 노출) 포함
  - `GET/PUT /api/admin/settings/server-cost`(관리자, 06번 문서가 예고한 API 이름 그대로 유지)로 관리자가 목표액/달성액을 수동 입력
  - `SecurityConfig`에 `/api/settings/**` GET permitAll 추가 (`/api/admin/settings/**`와는 경로가 달라 충돌 없음)
- **테스트**: `ServerCostSettingServiceTest`(싱글톤 행 최초 생성/멱등성/갱신), `ServerCostSettingMapperTest`(퍼센트 계산 — 정상/target=0/초과달성 클램프), `SecurityConfigTest`에 공개·관리자 케이스 추가. `./gradlew test` 전체 통과(77개, 기존 66 + 신규 11)
- **다른 영역 요청사항**: 없음(신규 API 추가만, 기존 계약 변경 없음). 프론트 연동 필요 시: 메인/사이드바 게이지(`GET /api/settings/server-cost`의 `percentage` 사용), 관리자 설정 화면(`GET`으로 프리필 → `PUT`으로 저장, 기존 `GameManagementPage`류 단일 리소스 폼 패턴 재사용 가능)
- **다음 예정**: 없음(이번 요청 범위 완료). 포인트 시스템/광고 SDK 연동은 기획이 더 구체화되면 별도 파트로

### 2026-07-16 — Claude Sonnet 5 / 개발자(백엔드) / 파트 07(확장 기능) 백엔드 — iCal 구독 + AI 스케줄 승인 워크플로

- **작업 내용**: 사용자가 프론트 세션 복귀를 확인해줘서 백엔드 스코프로 복귀, `docs/plans/07-extended-features-planning.md`의 4개 항목 중 백엔드가 필요한 2개를 구현. 나머지 2개(몬테카를로 계산기는 클라이언트 Web Worker가 기본, 공유 카드는 html2canvas/Capacitor Share API로 전부 프론트)는 백엔드 대상이 아니라고 판단해 손대지 않음
  1. **iCal/ICS 구독** (`IcsCalendarService`, `CalendarController`): `GET /api/users/{userCode}/calendar.ics`(비로그인 공개 — `userCode`가 추측 불가능한 비밀값 역할, 캘린더 앱이 세션 없이 주기적으로 직접 호출해야 하므로 인증을 요구할 수 없음). 유저의 `UserGamePreference` 필터에 해당하는 일정만 RFC 5545 텍스트로 변환(필터 비어있으면 스케줄러 페이지와 동일 컨벤션으로 전체 게임 포함), 조회 범위는 -3개월~+12개월로 고정. `POST /api/users/me/calendar/reset`(로그인 필요)로 `userCode` 재발급 — 유출 시 기존 구독 URL 무효화
  2. **AI 스케줄 자동 파싱 파이프라인 — 승인 워크플로만** (`PendingScheduleEntity`, `PendingScheduleService`, `PendingScheduleController` → `/api/admin/pending-schedules`): 목록(`GET ?status=`), 등록(`POST`), 승인(`POST /{id}/approve` → 실제 `ScheduleEvent` 생성), 반려(`POST /{id}/reject`). **실제 웹 스크래퍼/LLM 파서는 구현하지 않음** — LLM API 키, 크롤링 대상 사이트 ToS 검토, cron 스케줄링 인프라가 필요해 이 세션 단독으로 착수하기엔 범위가 큼. 지금은 관리자가 후보를 수동 등록하는 것으로 대체 가능하고, 추후 스크래퍼/LLM이 결정되면 같은 엔드포인트에 자동으로 밀어넣기만 하면 되도록 설계(계약 변경 불필요)
  - `SecurityConfig`에 `/api/users/*/calendar.ics` permitAll 추가, `UserRepository.findByUserCode`/`UserService.findUserByUserCode`·`resetUserCode` 추가
- **다른 세션이 알아두면 좋을 점**: `UserService.findOrCreateUser`가 frontend #(2026-07-16) 세션에서 이미 수정된 상태(name/profilePictureUrl 재로그인 시 덮어쓰지 않음)를 확인하고 그 위에서 작업함 — 충돌 없음, 전체 테스트로 확인
- **주의 사항**: `userCode`가 이제 캘린더 구독 URL의 비밀값 역할도 겸함 — 프로필 화면에 이미 노출되는 범용 식별자라 재발급하면 다른 용도로 확장 시 영향 있을 수 있음(현재는 FK로 참조되는 곳 없음을 확인함). 상세 설계 메모는 `docs/plans/07-extended-features-planning.md` "백엔드 구현 현황" 섹션 참고
- **테스트**: `IcsCalendarServiceTest`(빈 캘린더 wrapper, 필터별 일정 포함/제외, 필터 없을 때 전체 포함, 알 수 없는 userCode 예외), `PendingScheduleServiceTest`(생성/승인 시 실제 ScheduleEvent 생성/반려/중복승인 방지), `SecurityConfigTest`에 캘린더·승인 API 케이스 추가. `./gradlew test` 전체 통과(66개, 기존 52 + 신규 14)
- **다른 영역 요청사항**: 없음(API 계약 신규 추가만 있고 기존 계약 변경 없음). 프론트 연동 필요 시: 마이페이지에 구독 URL(`GET /api/users/me` 응답의 `userCode`로 조립) 노출/복사/재발급 버튼, 관리자 대시보드에 "승인 대기 스케줄" 목록+승인/반려 버튼
- **다음 예정**: 없음(이번 요청 범위 완료). 계산기/공유카드/실 스크래퍼는 프론트 세션 또는 사용자 방향 확인 후 별도 진행

### 2026-07-16 — Claude Fable 5 / 개발자(프론트·모바일) / 모바일 내비게이션을 하단 고정 탭바로 전면 교체 (사용자 요청)

- **배경**: 사용자가 "원래 Flutter 앱은 하단 네비게이션 바였는데 왜 지금은 다르냐"고 지적. 하이브리드 전환 후 모바일 폭에서는 PC용 Header/Sidebar를 반응형으로 눕혀 상단 가로 스크롤 탭으로 재활용하고 있었는데(바로 위 로그의 버그 수정도 이 구조 안에서의 땜빵이었음), 이건 Flutter의 `BottomNavigationBar`(스케줄표/가챠/홈/게시판/내정보 5탭, `frontend_mobile/lib/screens/home_screen.dart`)와 근본적으로 다른 구조였음을 인정하고 방향을 재확인한 뒤 진행
- **작업 내용**: 모바일 폭(앱 셸 포함, `(max-width: 768px)`) 전용 `frontend/src/layouts/MobileBottomNav.jsx`(+css) 신규 작성
  - 5개 탭: 스케줄러/시뮬레이터/채널/기록/내 정보 — Flutter가 쓰던 것과 동일한 Material 아이콘(calendar_today/casino/article/history/person) 재사용해 시각적 연속성 유지. "내 정보" 탭은 로그인 여부에 따라 `/profile` 또는 `/login`으로 분기
  - `MainLayout.jsx`: `isMobile`일 때 기존 `Sidebar` 렌더링을 끄고 `MobileBottomNav`를 하단 고정으로 렌더링, `<main>`의 `paddingBottom`에 탭바 높이(64px)만큼 여유를 더해 콘텐츠 가림 방지(데스크톱은 기존 그대로 무변경)
  - 관리자 메뉴(게임 관리/공지사항 작성/채널 관리/유저 정보 관리)는 모바일에서 항상 보이는 탭바에 넣기엔 항목이 너무 많아 **`UserProfilePage`(내 정보) 안의 별도 섹션으로 이동** — `isMobile && (SUB_ADMIN || MAIN_ADMIN)`일 때만 노출(데스크톱은 기존 Sidebar에 이미 있어 중복 방지). 프로필 API가 실패해도 관리자 메뉴는 항상 뜨도록 렌더링 순서 조정(관리자가 자기 프로필 조회 실패와 무관하게 관리 화면엔 진입할 수 있어야 하므로)
- **검증**: eslint/`npm run build` 통과. 실기기(SM-F966N)에 재배포해 확인 — 하단 탭바 렌더링, 탭별 아이콘 활성 강조(파란색), 실제 터치로 탭 전환 시 콘텐츠가 탭바에 가리지 않음(스크린샷으로 확인), "내 정보" 진입 시 관리자 메뉴 4개 링크 정상 노출(프로필 조회 실패 상태에서도)
- **다른 영역 요청사항**: 없음(프론트 전용 레이아웃 변경, API 계약 변경 없음). 데스크톱 웹은 기존 Header/Sidebar 그대로라 영향 없음

### 2026-07-16 — Claude Fable 5 / 개발자(프론트·모바일) / 실기기 adb 조작 테스트 + 신규 레이아웃 버그 2건 발견·수정

- **작업 내용**: 사용자 요청으로 실기기(SM-F966N)에 앱을 설치해 화면을 캡처하며 기능을 직접 조작 테스트. H2 인메모리로 백엔드를 띄우고 게임 2종/배너·캐릭터/일정 4건/채널·글·댓글을 시드한 뒤, `adb reverse tcp:8080`으로 앱을 실제 API에 연결해 검증
- **정상 확인된 것**: 스케줄러(간트 차트 렌더링, 게임 필터 토글, 실 API 데이터), 가챠 시뮬레이터(배너 로드, 10연 뽑기, 천장 카운트 갱신), 게시판(글 목록/상세/조회수 증가/댓글, 비로그인 안내 문구), `/history` 등 보호 라우트의 비로그인 시 `/login` 리다이렉트
- **직전 세션이 수정한 버그 3건을 실기기에서 재검증**: Google 로그인 없이 Chrome DevTools Protocol(`webview_devtools_remote_<pid>` 소켓)로 앱 WebView에 직접 붙어 `localStorage`에 유효 서명된 테스트 JWT를 주입한 뒤 `Page.navigate`로 `/profile`·`/admin/game`에 **하드 네비게이션**(SPA 클라이언트 라우팅이 아니라 실제 페이지 로드)함
  - **AuthContext 하드 리로드 수정 확인**: 하드 네비게이션 후에도 `/login`으로 튕기지 않고 대상 페이지에 그대로 머묾 (수정 전이었다면 재현됐을 버그)
  - **role 기반 UI 확인**: 헤더에 사용자명/로그아웃, 사이드바에 관리자 메뉴(게임 관리/공지사항 작성/채널 관리/유저 정보 관리) 전부 정상 노출
  - **상태바 테마 연동 확인**: 라이트 테마로 전환 시 실기기 상태바 아이콘이 어두운 색으로 바뀌어 밝은 배경과 대비가 유지됨(스크린샷으로 확인)
  - **재로그인 닉네임 원복 수정**은 실제 Google 로그인이 필요해 이번에도 미검증 (백엔드 단위 테스트로만 확인된 상태 유지)
- **신규 발견·수정 버그 2건** (항목이 적을 땐 안 드러나다가, role별 메뉴가 늘어나며 실사용 중 발견됨):
  1. **`Sidebar.jsx` 모바일 상단 탭바 텍스트가 글자 단위로 세로 줄바꿈**: flex row에 `white-space:nowrap`/`overflow-x` 없이 `justify-content:space-around`로 항목을 욱여넣어서, 관리자 메뉴까지 늘어나면 "스케줄러"가 "스/케/줄/러"처럼 깨짐. `overflow-x:auto` 가로 스크롤 + `white-space:nowrap`으로 수정, 구분선도 세로 바 형태로 교체
  2. **`GameManagementPage`/`ChannelManagementPage`/`UserManagementPage`의 관리자 테이블도 동일한 원인으로 헤더/셀 텍스트가 세로로 깨짐**: 3개 파일 모두 `.table-scroll` 래퍼(가로 스크롤) + `white-space:nowrap` + `min-width`로 동일하게 수정
- **검증**: eslint 통과, `npm run build` 성공, 수정 각각을 실기기에 재배포해 스크린샷으로 재확인(수정 전/후 비교). 백엔드 코드는 변경 없어 `./gradlew test` 재실행 안 함
- **다른 영역 요청사항**: 없음 (프론트 전용 CSS/레이아웃 수정, API 계약 변경 없음)
- **테스트 방법 공유**: 실기기(또는 에뮬레이터) 디버그 빌드는 `webview_devtools_remote_<pid>` 추상 소켓을 노출한다(`adb shell cat /proc/net/unix | grep webview_devtools`). `adb forward tcp:9333 localabstract:webview_devtools_remote_<pid>` 후 `http://localhost:9333/json`으로 `webSocketDebuggerUrl`을 얻으면, Node의 네이티브 `WebSocket`으로 CDP `Runtime.evaluate`(localStorage 조작 등)·`Page.navigate`(하드 네비게이션)를 실행해 실제 Google 로그인 없이도 인증된 화면을 재현/검증할 수 있음
- **여전히 미해결**: 실제 Google 계정 로그인 e2e(사용자 조치 필요 항목, 위 목록 참고)

### 2026-07-16 — Claude Fable 5 / 개발자(프론트·백엔드 소수정) / 이전 세션들이 남긴 잔존 이슈 3건 수정

- **작업 내용**: 다른 세션들이 "발견만 하고 범위 밖으로 남긴" 이슈 3건을 모두 수정
  1. **하드 리로드 시 강제 로그아웃** (backend의 05·06 대행 로그에서 발견 보고): `AuthContext`가 `useEffect`로 localStorage를 읽어 첫 렌더에 `isAuthenticated=false`였던 것을 `useState` lazy initializer 동기 로딩으로 교체 — 보호 라우트 새로고침/직접 진입 시 `/login`으로 튕기던 문제 해결. 손상된 저장값 방어(try/catch)도 추가
  2. **재로그인 시 닉네임/프로필사진 원복** (backend의 파트 06 로그에서 발견 보고): `UserService.findOrCreateUser`가 기존 유저의 name/profilePictureUrl을 구글 프로필로 덮어쓰지 않도록 수정(email만 동기화). `UserServiceTest`의 해당 단언도 새 동작 기준으로 갱신 — **행동 변경**: 이제 구글 프로필 이름을 바꿔도 서비스 닉네임에 반영되지 않음(의도된 동작, `PUT /api/users/me` 수정값 보존이 우선)
  3. **앱 상태바 아이콘 대비 낮음** (mobile_shell 실기기 검증 로그에서 발견 보고): `mobile_shell`에 `@capacitor/status-bar@7` 추가(8.x는 Capacitor 7과 비호환이라 7.x 고정), 웹 `useTheme` 훅에서 `window.Capacitor?.Plugins?.StatusBar` 가드로 테마 전환 시 `setStyle` 호출 — 일반 브라우저에선 no-op라 웹 동작 영향 없음
- **검증**: frontend `npm run build` 성공(AuthContext의 `react-refresh/only-export-components` lint 에러는 기존부터 있던 것), backend `./gradlew test` 전체 통과, mobile_shell `npm run sync` + `assembleDebug` 성공(status-bar 플러그인 포함 APK 생성). 상태바 실기기 확인은 `adb install` 후 라이트/다크 토글로 확인 필요
- **다른 영역 요청사항**: 없음 (API 계약 변경 없음. 2번은 백엔드 행동 변경이지만 응답 형태 동일)

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

### 2026-07-16 — Gemini 3.5 Flash / 기획자 / 캐릭터 육성 재화 계산기(Build Planner) 기획

- **작업 내용**: 유저 편의성과 커뮤니티 공략글 품질 향상을 위한 '캐릭터 육성 재화 계산기' 설계 완료.
  - `docs/plans/13-build-planner.md` 기획서 추가.
  - 캐릭터의 현재 레벨과 목표 레벨을 지정하면 필요한 재화량(경험치 책, 모라 등)을 산출해 주는 백엔드 로직(`ItemEntity`, `CharacterProgressionEntity`) 기획.
  - 가장 중요한 기능으로, 도출된 계산 결과를 **공략 게시판 글 작성 시 'UI 위젯/이미지 숏코드' 형태로 첨부**할 수 있도록 연동하여 양질의 커뮤니티 글쓰기를 유도.
  - MVP 릴리즈를 위해 복잡한 외부 크롤러 도입보다는 관리자가 필수 인기 캐릭터만 직접 입력하는 방식 채택.

### 2026-07-16 — Gemini 3.5 Flash / 기획자 / 관리자 가챠 확률 제어 UI 기획

- **작업 내용**: 관리자용 웹 페이지에서 가챠 확률을 유연하고 직관적으로 관리할 수 있는 UI 기획안 작성.
  - `docs/plans/06-admin-user-management.md` 문서 Phase 2 범위에 '가챠 확률 제어 UI (Probability Management)' 항목 추가.
  - 배너 설정 시 천장(`pityThreshold`)과 픽업 확률(`rateUpRate`)을 직접 입력하는 폼 설계.
  - 캐릭터별 가중치(Weight)를 실시간으로 조정하는 엑셀 그리드 테이블 제안. 가중치 수정 시 백엔드 지식이 없는 운영자도 퍼센트(%)를 바로 확인할 수 있도록 '실시간 최종 확률 자동 계산 헬퍼 UI' 기획.

### 2026-07-16 — Gemini 3.5 Flash / 기획자 / 텍스트 RPG 미니게임(게이미피케이션) 기획 수립

- **작업 내용**: 체류 시간 극대화를 위한 '텍스트 RPG 방치형 원정대' 시스템 기획 완료.
  - `docs/plans/11-text-rpg-minigame.md` 기획서 추가.
  - 가챠 시뮬레이터 결과를 저장하는 '가상 인벤토리' 시스템 도입.
  - 인벤토리 캐릭터들을 파견(Idle) 보내고 푸시 알림(FCM)과 연계하여 앱 재방문을 유도하도록 설계.
  - 커뮤니티(게시판) 글/댓글 작성으로 얻는 포인트를 RPG 스탯 업그레이드에 쓰도록 연결하여 커뮤니티 활성화 유도.

### 2026-07-16 — Gemini 3.5 Flash / 기획자 / 푸시 알림 시스템(Push Notification) 기획 수립

- **작업 내용**: 서버 유지비 충당 및 장기적인 서비스 수익 모델 수립 완료.
  - [08-monetization-strategy.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/08-monetization-strategy.md): 앱 환경에서의 UX 저하를 막기 위해 하단 고정 배너 등은 배제하고, 리스트 중간에 섞이는 '네이티브 광고(Phase 1)'와 커뮤니티 포인트 상점과 연계된 '보상형 비디오 광고(Phase 3)'를 주요 수익화 모델로 확정.
  - [00-overview.md](file:///Users/somminwoo-m1/Documents/projects/personal/gacha_scheduler/docs/plans/00-overview.md): 로드맵에 '08. 수익화 전략' 단계 추가 완료.
- **다음 예정**: 기획 검토 및 다음 단계 지시 대기.

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
