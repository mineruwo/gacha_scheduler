# AI 세션 인계 문서

**이 문서의 성격**: [SYNC.md](SYNC.md)는 시간순 append-only 로그라 지금 상태를 파악하려면 수십 개 항목을 다 읽어야 한다. 이 문서는 반대로 **항상 최신 상태로 덮어써서 유지하는 스냅샷**이다 — 새 세션은 이 문서 하나로 5분 안에 감을 잡고, 상세 근거/재현 절차가 필요하면 SYNC.md나 `docs/plans/*.md`로 넘어간다.

**갱신 규칙**: 상태가 크게 바뀌면(파트 완료, 구조적 제약 발견, 인프라 전환 등) 이 문서도 SYNC.md 로그와 함께 갱신할 것 — 로그처럼 추가하지 말고 해당 섹션을 덮어쓸 것.

## 이 프로젝트

가챠 게임 유저용 스케줄러(간트차트) + 가챠 시뮬레이터 + 공략 게시판 플랫폼. 상세 기능 설명은 루트 [README.md](../README.md), 파트별 계획은 [docs/plans/00-overview.md](plans/00-overview.md) 참고.

> **🔴 모바일 전략 2026-07-16 재전환**: 한 번 하이브리드(Capacitor)로 갔다가 다시 네이티브 Flutter로 되돌아왔다. **`mobile_shell`(Capacitor)은 중단(deprecated) — 신규 작업 절대 넣지 말 것.** `frontend_mobile`(Flutter)이 유일한 모바일 앱. 왜 두 번 바뀌었는지, 뭐가 이미 있는지는 아래 표/SYNC.md 로그 참고

- **backend**: Spring Boot 3.5.5 (Java 21) + JPA + Spring Security(stateless JWT) + PostgreSQL — `backend/backend_gacha`
- **frontend**: React 19 + Vite + react-router-dom 7 — `frontend` (PC 브라우저 대응이 주 목적, 모바일 반응형은 유지되지만 우선순위 아님)
- **frontend_mobile**: Flutter — **모바일 유일 앱(재확정)**. 웹에 있던 화면(가챠 시뮬레이터/로그인·회원가입/스케줄표/공략 게시판/유저 프로필) 전부 이식 완료, 전 화면 라이트 파스텔 테마 적용됨. 유저관리(관리자) 화면만 아직 없음. 상세는 아래 "모바일 전략" 섹션
- **mobile_shell**: Capacitor 7 — **중단(deprecated), 코드만 보관**. 참고용으로만 남겨둠, 더 이상 손대지 말 것

## 지금 로컬 개발 환경 켜는 법

```bash
# 1) PostgreSQL (이미 설치·기동돼 있을 수 있음, 아래로 확인)
brew services list | grep postgresql   # 안 떠 있으면:
brew services start postgresql@16      # (이 저장소 세션 샌드박스에선 launchd 문제로 실패할 수 있음 —
                                        #  그럴 땐 postgres -D /opt/homebrew/var/postgresql@16 & 로 직접 기동)

# 2) 백엔드 (application-local.properties에 로컬 DB/시크릿 이미 채워져 있음, git엔 없음)
cd backend/backend_gacha && ./gradlew bootRun
curl http://localhost:8080/api/games   # 시드 데이터 200 나오면 정상

# 3) 프론트
cd frontend && npm run dev   # frontend/.env 이미 존재(VITE_API_BASE_URL=http://localhost:8080)
```

DB/계정: `gacha_scheduler` DB, `gacha_app` 계정(로컬 전용, RDS 아님). 시드는 `backend/backend_gacha/src/main/resources/seed.sql`(PostgreSQL 문법, H2에선 안 돌아감).

## 모바일 전략 (두 번 바뀜 — 헷갈리지 말 것)

1. **최초**: Flutter 네이티브 앱(`frontend_mobile`)으로 시작, 파트 04(가챠 시뮬레이터) 화면만 Mock으로 구현
2. **1차 전환(2026-07-15)**: "화면 유지 부담"으로 하이브리드(Capacitor `mobile_shell`, 웹을 WebView로 감쌈)로 전환. Flutter는 보관
3. **2차 전환(2026-07-16, 지금)**: 사용자가 "웹뷰로 감싸지 말고 앱이 API를 직접 호출했어야 했다"며 다시 번복 → **Flutter 네이티브로 최종 확정**. `mobile_shell`은 중단(코드는 남아있지만 개발 안 함)

**지금부터 모바일 작업은 전부 `frontend_mobile`(Flutter/Dart, Riverpod)에서.** **가챠 시뮬레이터 화면은 실 API 연동 + 실기기 e2e 검증까지 완료됨**(`lib/screens/gacha_screen.dart`, `lib/repositories/gacha_repository.dart`의 `ApiGachaRepository`, `AppConfig.useMockGacha` 기본값 `false`) — 1회/10연 뽑기 전부 실기기(Samsung SM-F966N, Android 16)에서 실제 백엔드로 확인함. **전 화면 라이트 파스텔 디자인 적용 완료**(`lib/theme/app_theme.dart`, `docs/design_assets/ui_concept_light_pastel_*.jpg` 참고). **이메일/비밀번호 로그인·회원가입 + 세션 영속화도 실기기 e2e 검증 완료**(`lib/providers/auth_providers.dart`, `lib/screens/login_screen.dart`, `lib/screens/my_info_screen.dart`) — 구글 로그인은 아직 없음(추후 `google_sign_in`/`firebase_auth`로, 의존성은 이미 있음). **스케줄표(간트차트) 화면도 이식 완료**(`lib/screens/schedule_screen.dart`, `lib/providers/schedule_providers.dart`, `lib/repositories/schedule_repository.dart`) — 기간 이동/게임 필터(로그인 시 서버 저장, 비로그인 시 로컬 저장)까지 실기기 확인. **공략 게시판도 이식 완료**(`lib/screens/board_screen.dart`, `lib/screens/post_editor_screen.dart`, `lib/providers/board_providers.dart`, `lib/repositories/board_repository.dart`) — 채널 전환/검색/페이징/글쓰기/댓글·대댓글(삭제 권한 검증 포함) 전부 실기기 확인. **유저 프로필(실 데이터)도 이식 완료**(`lib/screens/my_info_screen.dart`를 디버그 화면에서 교체, `lib/providers/user_profile_providers.dart`, `lib/repositories/user_repository.dart`) — 조회/수정(이름·프로필 사진 URL) 실기기 확인. **이걸로 웹의 5개 유저용 화면이 모바일에 전부 이식 완료됨.** 아직 없는 것: 유저관리(관리자) 화면(모바일에 관리자 기능 자체가 없음), 구글 로그인(추후 `google_sign_in`/`firebase_auth`로, 의존성은 이미 있음), 401 자동 로그아웃(아래 16번 참고). API 계약은 위 백엔드가 이미 다 구현해뒀으니 그대로 호출하면 됨(웹이 쓰는 `frontend/src/api/*.js`들을 Dart로 그대로 옮기는 셈).

**Flutter 로컬 개발 환경**: `brew install --cask flutter`로 설치됨(경로 `/opt/homebrew/share/flutter/bin`, PATH에 이미 추가). 실기기 빌드/설치 절차:
```bash
export PATH="/opt/homebrew/share/flutter/bin:$PATH"
cd frontend_mobile && flutter pub get && flutter build apk --debug
adb reverse tcp:8080 tcp:8080   # 실기기가 로컬 백엔드에 붙게
adb install -r build/app/outputs/flutter-apk/app-debug.apk
```
**네이티브 UI 자동화 팁**: 스크린샷을 눈으로 보고 탭 좌표를 추정하지 말 것 — 배율 계산이 자꾸 틀린다. `adb shell uiautomator dump /sdcard/d.xml && adb pull /sdcard/d.xml`로 정확한 `bounds`를 뽑아서 중심 좌표를 계산할 것.

## 전체 진행 상태 (요약, 상세는 [00-overview.md](plans/00-overview.md))

| 파트 | 상태 |
|---|---|
| 01~06 (보안/인증/스케줄러/가챠/게시판/유저관리) | 백엔드+프론트 전부 완료, H2/로컬 Postgres로 e2e 검증 |
| 07 (확장 기능) | iCal 구독 + AI 스케줄 승인 워크플로 백엔드 완료. 실 스크래퍼/LLM 파서·계산기·공유카드는 미착수 |
| 08 (수익화) | 서버비 프로그레스 바 API 완료. 광고/포인트 시스템은 기획 더 필요 |
| 09 (배포) | **NCP 실제 프로비저닝 + 백엔드 배포 완료(2026-07-16)**. `http://101.79.26.52`에서 API 응답 중(도메인/TLS는 아직). RAM 1GB라 스왑+힙 축소로 튜닝함. **회원가입 계정 탈취 취약점 발견함(아래 참고), 미해결로 남음** |

## 꼭 알아야 할 것 (실제로 시간 날린 것들 — 재발견하지 말 것)

1. **Hibernate 지연 로딩 연관관계가 조용히 null을 반환하는 버그 패턴**: 참조 컬럼이 대상 엔티티의 실제 PK가 아니거나(`gameCode`로 연결 등), `@IdClass` 복합키 엔티티일 때 `@ManyToOne(fetch=LAZY)`가 프록시를 못 만들어서 항상 `null`. **원칙: 이런 연관관계는 아예 두지 말고, 서비스 레이어에서 항상 명시적으로 배치 조회(`findByXxxIn`)해서 DTO를 조립할 것.** 이미 있는 모든 엔티티가 이 패턴을 따름 — 새 엔티티 추가할 때도 지연 연관관계 유혹에 넘어가지 말 것. (04-gacha-simulator.md에 상세 원인)
2. **`mobile_shell`(Capacitor WebView) 안에서는 Google 로그인 버튼을 직접 렌더링할 수 없음** — Google이 서버 단에서 User-Agent의 `; wv)`(임베디드 WebView) 마커를 감지해서 GIS 스크립트/OAuth 자체를 403으로 차단함(2021년부터 시행 중인 공식 정책, 우리 쪽 설정 문제 아님). **해결·구현 완료**: `@capacitor/browser`로 외부 브라우저(Custom Tabs)에 로그인을 위임하고, 완료되면 커스텀 스킴 `gachascheduler://auth-callback`(AndroidManifest의 intent-filter + `@capacitor/app`의 `appUrlOpen` 리스너, `App.jsx`의 `NativeAuthBridge`)으로 토큰을 앱에 돌려받아 로그인 처리 + 브라우저 탭 자동 종료. **주의**: `window.Capacitor.Plugins.<Plugin>.addListener(...)`가 항상 Promise를 반환한다고 가정하면 안 됨(런타임에 따라 아닐 수 있어 `c.then is not a function`으로 React가 통째로 크래시했었음) — 반드시 `Promise.resolve(...)`로 감쌀 것. 브라우저 브릿지 URL은 `VITE_OAUTH_BRIDGE_URL`(로컬은 `http://localhost:5173`, 실기기는 `adb reverse tcp:5173 tcp:5173` 필요, 배포 시 실 도메인으로 교체)
3. **JWT role staleness**: role을 바꾸면 DB엔 즉시 반영되지만, 이미 발급된 JWT는 만료 전까지 발급 시점 role 그대로 동작(stateless JWT의 구조적 한계, 의도적으로 미조치)
4. **가챠 뽑기 기록은 서버에 영속 저장 안 함**(의도된 스코프 축소) — `currentPity`는 클라이언트가 들고 있는 값을 매번 신뢰/갱신. 히스토리 API에는 글/댓글만 있음
5. **`userCode`가 이중 용도**: 프로필에 노출되는 일반 식별자이면서 동시에(파트 07부터) iCal 구독 URL의 비밀값 역할도 함. 재발급 API(`POST /api/users/me/calendar/reset`) 있음
6. **CSS 중첩 주석은 조용히 다음 규칙을 통째로 깨뜨림**: `/* ... /* 한줄 주석 */ ... */` 형태로 주석이 중첩되면 CSS는 중첩을 지원 안 해서 바깥 주석이 조기 종료되고, 그 뒤 죽은 코드가 살아나 문법이 깨짐 — Android WebView는 이 시점에서 복구 못 하고 바로 다음 규칙 전체를 드롭함(데스크톱 Chrome은 복구해서 안 보였음). `index.css`에서 이미 한 번 발생해 수정됨 — CSS에 주석 추가할 때 중첩 주의
7. **Android 15(targetSdk 35)는 엣지투엣지가 강제**라 `safe-area-inset-*` 패딩 없이는 헤더가 상태바랑 겹침(수정됨), **cleartext HTTP도 기본 차단**이라 실기기/에뮬레이터에서 로컬 백엔드 붙이려면 디버그 빌드 전용 network_security_config가 필요(이미 `mobile_shell/android/app/src/debug/`에 추가됨, 릴리스엔 영향 없음)
8. **`findOrCreateUser`는 재로그인 시 email만 동기화**하고 name/profilePictureUrl은 덮어쓰지 않음(원래는 덮어썼다가 버그로 발견돼 수정됨) — `PUT /api/users/me`로 바꾼 값이 유지되는 게 의도된 동작
9. **로그인 수단이 구글 하나가 아니게 됨**: `POST /api/auth/signup`/`POST /api/auth/login`으로 이메일+비밀번호 가입/로그인 추가(2026-07-16). `UserEntity.passwordHash`는 nullable(구글 전용 계정은 null). **같은 이메일로 구글 가입 후 비밀번호 가입을 시도하면 거부하지 않고 그 계정에 비밀번호만 추가해서 통합**(사용자 결정) — DTO엔 passwordHash 절대 노출 안 함. `LoginPage.jsx`에 로그인/회원가입 탭 + Google 버튼이 같이 있음. 비밀번호 재설정 기능은 아직 없음
10. **`frontend_mobile`도 mobile_shell과 똑같은 Android 15+ 함정 2개를 그대로 가지고 있었음**: (a) Flutter 기본 템플릿은 디버그용 INTERNET 권한만 `android/app/src/debug/AndroidManifest.xml`에 넣어두고 메인 매니페스트엔 없음 — release 빌드에선 API 호출 자체가 막힘, 메인 매니페스트에도 추가해야 함(수정 완료). (b) Android 15+ cleartext HTTP 기본 차단 — `android/app/src/debug/res/xml/network_security_config.xml` + `debug/AndroidManifest.xml`의 `networkSecurityConfig` 참조로 해결(디버그 전용, mobile_shell과 동일 패턴). **새 Flutter 프로젝트/화면을 실기기에 처음 빌드할 때마다 이 두 가지부터 확인할 것**
11. **Flutter `DropdownButtonFormField`(및 `DropdownButton`)에 `isExpanded: true`를 안 주면 조용히 오버플로우함** — `DropdownMenuItem`의 `Text`에 `overflow: TextOverflow.ellipsis`를 줘도 소용없음(선택된 값을 보여주는 영역 자체가 내용 크기에 맞춰 늘어나려다 넘침). 텍스트 길이가 가변적인 드롭다운을 만들 때는 항상 `isExpanded: true`부터 넣을 것
12. **가챠 자연 확률은 `등급 가중치 합 / 풀 전체 가중치 합`이라 하위 등급을 빼먹으면 상위 등급 확률이 폭등함**(2026-07-16 실제 발생 — 3성 없이 5성/4성만 있던 시드 데이터가 5성 19%를 냈음). `weight`는 절대 확률처럼 설계됐으므로(0.006=0.6%) 풀 전체 합이 1.0에 가까워야 함. 새 배너 시딩/수정 시 `GachaBannerManagementPage`의 확률 미리보기로 항상 확인할 것
13. **`frontend_mobile`의 실기기 e2e 테스트에서 `mobile_shell`과 패키지명을 헷갈리기 쉬움**: `mobile_shell`은 `com.gacha.scheduler`, `frontend_mobile`은 `com.example.frontend_mobile`(기본 템플릿 값 그대로, 아직 안 바꿈) — 둘 다 같은 실기기에 설치돼 있으면 `adb shell monkey -p <패키지명>`을 잘못 지정해도 에러 없이 조용히 다른 앱이 실행되니 항상 패키지명부터 확인할 것. 로컬 백엔드 붙일 땐 `adb reverse tcp:8080 tcp:8080` 필수(에뮬레이터의 `10.0.2.2`와 달리 실기기는 `localhost`가 기기 자신을 가리킴)
14. **폴더블 기기(Galaxy Z Fold 등)는 `adb shell screencap`/`input tap`이 기본적으로 잘못된 디스플레이(꺼진 커버 화면 등)를 대상으로 잡을 수 있음** — `adb shell dumpsys display`의 `mViewports`에서 `isActive=true`인 `uniqueId`(physicalDisplayId)를 확인해서 `screencap -d <id>`로 지정할 것. `input tap`은 디스플레이 지정 옵션이 없어 기본 활성 디스플레이를 쓰므로 보통은 그냥 되지만, 좌표는 반드시 `adb shell uiautomator dump`의 실제 `bounds`로 계산할 것(스크린샷 눈대중 금지 — 이미 11번 항목에도 있음)
15. **Dart `DateTime.toIso8601String()`을 백엔드 쿼리 파라미터로 그대로 보내면 안 됨** — 로컬 `DateTime`은 오프셋/`Z` 없는 문자열(`2026-07-16T00:00:00.000`)을 만드는데, 백엔드의 `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)`은 이걸 400으로 거부함(스케줄표 화면 이식 중 실제 발생, `curl`로 재현 확인). **항상 `.toUtc().toIso8601String()`로 변환해서 보낼 것** — `frontend_mobile/lib/repositories/schedule_repository.dart`에서 수정된 패턴 참고, 앞으로 Dart에서 날짜를 쿼리 파라미터로 보내는 곳마다 동일하게 주의
16. **`frontend_mobile`의 401 자동 로그아웃은 `userProfileProvider`에만 적용돼 있음(부분 수정, 2026-07-16)** — 실서버(NCP)로 테스트하다가 로컬 DB 기준 토큰이 캐시된 채 남아있어서 "로그인된 것처럼 보이지만 모든 요청이 401로 실패"하고 **로그아웃 버튼도 없는 막다른 화면에 갇히는 버그**를 실제로 겪음. `lib/providers/user_profile_providers.dart`의 `userProfileProvider`에서 `UserApiException(401)`을 잡아 `authControllerProvider.notifier.logout()`을 호출하도록 고쳐서 이 경로는 해결됨. **하지만 `board_repository.dart`/`schedule_repository.dart`/`auth_repository.dart`의 다른 인증 호출들은 아직 이 처리가 없음** — 같은 패턴으로 넓혀서 공통 처리(예: 401 감지 헬퍼)로 만드는 게 다음 후보
17. **🔴 보안 취약점 — 회원가입의 "구글 계정에 비밀번호 병합" 로직이 이메일 소유권 검증 없이 계정 탈취를 허용함**(2026-07-16 배포 중 발견). 구글로 이미 가입된 이메일(비밀번호 없음)을 공격자가 알면 `POST /api/auth/signup`으로 자기 비밀번호를 붙여서 그 계정에 로그인 가능. 이메일 인증 절차 없이는 못 막음 — `AuthController`/`UserService` 다음에 만질 때 반드시 고칠 것. 상세는 [09-deployment.md](plans/09-deployment.md) "발견한 보안 이슈" 참고
18. **NCP mi1-g3(RAM 1GB)에서 JVM+Postgres 동시 구동은 여유 메모리가 100MB 안팎으로 빡빡함** — 2GB 스왑 + JVM 힙 `-Xmx384m`(템플릿 기본 512m에서 하향) + Postgres `max_connections=20`으로 안정화했음. 트래픽 늘면 인스턴스 업그레이드 재검토
19. **NCP 서버는 IPv6 미지원인데 nginx 기본 사이트가 `listen [::]:80`을 갖고 있어 설치 직후 기동 실패함** — 기본 사이트(`/etc/nginx/sites-enabled/default`) 삭제하고 전용 사이트만 쓸 것
20. **NCP는 AWS와 달리 `.pem` 키를 SSH 로그인에 직접 안 씀** — 콘솔에서 받은 키는 "관리자 비밀번호 확인" 기능으로 초기 비밀번호를 복호화하는 용도. 실제 배포에서는 별도 SSH 키를 생성해 서버에 등록하고 비밀번호 로그인은 껐음 — 키는 `~/.ssh/gacha-scheduler/`(로컬 macOS)에 있고 `~/.ssh/config`에 `gacha-server` 별칭 등록됨(`ssh gacha-server`로 바로 접속)
21. **`frontend_mobile`을 실서버(NCP, `http://101.79.26.52`)로 테스트하려면 두 가지가 필요**: (a) `flutter build apk --debug --dart-define=API_BASE_URL=http://101.79.26.52`로 API 베이스 URL 교체, (b) 아직 TLS가 없는 cleartext HTTP라서 `android/app/src/debug/res/xml/network_security_config.xml`의 `domain-config`에 `101.79.26.52` 도메인을 추가해야 함(디버그 전용이라 릴리스에는 영향 없음, 이미 2026-07-16에 추가해둠) — **실서버에 도메인/TLS 인증서가 적용되면 이 도메인 항목은 제거할 것**(더 이상 cleartext 예외가 필요 없어짐)

## 시크릿/설정 파일 (전부 gitignore 대상, 로컬에만 존재)

- `backend/backend_gacha/src/main/resources/application-local.properties` — DB_URL/DB_USERNAME/DB_PASSWORD(로컬 Postgres)/GOOGLE_CLIENT_ID(실 값 반영됨)/JWT_SECRET
- `frontend/.env` — VITE_API_BASE_URL/VITE_GOOGLE_CLIENT_ID(백엔드와 동일 값)/VITE_USE_MOCK_GACHA
- 운영 배포용 값은 `backend/backend_gacha/deploy/backend.env.example` 템플릿 참고(실제 값은 서버의 `/etc/gacha-scheduler/backend.env`에만)

## 제품 완성도 갭 (2026-07-16 점검)

"백엔드 말고 이 앱에서 비어있는 중요 요소"를 훑어서 나온 목록. 우선순위 높은 것부터 처리, 정책 관련은 사용자 지시로 후순위.

- [x] React ErrorBoundary (`components/ErrorBoundary.jsx`) — 컴포넌트 크래시 시 흰 화면 방지
- [x] 404 페이지 (`pages/NotFoundPage.jsx`, `App.jsx`의 `path="*"`)
- [x] 게시판 검색 (제목/내용, 채널 내 검색 — `PostRepository.searchByChannelId`, `?query=`)
- [x] 가챠 배너/드랍테이블 관리자 페이지 (`GachaBannerManagementPage`, `/admin/gacha-banner`) — 배너/캐릭터 CRUD + 풀 가중치·픽업 관리 + 등급별 확률 미리보기. 겸사겸사 5성 확률 밸런스 버그도 수정(위 12번 항목)
- [x] 공지사항/팝업 배너 관리 (`AnnouncementManagementPage`, `/admin/announcements`) — `AnnouncementEntity`(NOTICE/POPUP 공용), 홈페이지 공지 목록 + 전역 팝업 모달(`MainLayout`의 `PopupBannerModal`, "오늘 하루 보지 않기"는 localStorage `dismissedPopups`로 기억)
- [ ] **이미지 업로드 기능 전무** — 배너/캐릭터 아이콘, 프로필 사진 전부 URL 텍스트 입력뿐, 실제 파일 업로드/호스팅 없음. **저장소(S3/Cloudinary/로컬디스크) 결정을 사용자에게 물었고 "지금은 보류"로 결정됨** — 다시 논의할 때 이 결정부터 시작할 것
- [ ] 이용약관/개인정보처리방침/DMCA 연락처 페이지 — 계획 문서(02, 05번)엔 있는데 실제 화면 없음 (정책, 후순위로 보류)
- [ ] 앱 아이콘이 아직 Capacitor 기본 아이콘(파란 X자), PWA manifest.json 없음, Open Graph 메타 태그 없음 (브랜딩/공유 미리보기)
- [ ] 프론트 테스트 파일 0개 (백엔드는 92개)

## 미해결 (최신 목록은 항상 [SYNC.md](SYNC.md)의 "다른 영역에 대한 요청" 섹션이 원본)

- 실제 Google 계정으로 로그인 e2e (사용자 몫 — 데스크톱/모바일 웹은 그냥 되고, 앱 셸은 위 2번 항목의 외부 브라우저 버튼을 눌러 진행. 설정 자체는 완료)
- **회원가입 계정 탈취 취약점 수정** (위 17번 항목, 이메일 인증 절차 추가 필요)
- 도메인 연결 + certbot TLS 발급 (지금은 `http://101.79.26.52`로만 접근 가능)
- 프론트/모바일을 배포된 API 도메인으로 전환 + CORS 반영

## 여러 세션이 로컬 백엔드를 동시에 만질 때

- 같은 `backend/backend_gacha` 디렉터리에서 두 세션이 동시에 `./gradlew test`를 돌리면 `build/test-results`에 XML 쓰기 충돌로 빌드가 실패할 수 있다 — **코드 문제 아님, 그냥 재시도하면 해결됨**
- 백엔드 소스에 다른 세션이 실시간으로 변경을 넣고 있을 수 있다(예: 이메일/비밀번호 회원가입 `/api/auth/signup`·`/api/auth/login` 추가 등). 내가 파일을 Read한 시점과 실제 저장된 내용이 다를 수 있으니, Edit 전에는 최신 내용으로 다시 Read할 것

## 협업 규칙 리마인더

여러 AI 세션이 병렬 작업 중 — **작업 시작 전 SYNC.md를 읽고**(이 문서보다 최신 세부사항이 있을 수 있음), **끝나면 SYNC.md에 로그를 남기고**(형식: `[AI 모델] / [페르소나] / [작업 내용]`), **이 문서(HANDOVER.md)도 상태가 바뀌었으면 같이 갱신**할 것.
