# 02. 인증/공통 기반

상태: 백엔드·프론트 연동 완료 / 실동작(e2e) 검증 대기
선행 조건: [01. 보안 강화](01-security-hardening.md)

## 배경

프론트엔드는 이미 `@react-oauth/google`로 구글 로그인 UI(`LoginPage`)와 `AuthContext`(localStorage 기반 user/token/role 저장), `ProtectedRoute`를 갖추고 있다. `LoginPage`는 구글 ID 토큰을 `POST /api/auth/google`로 보내고 응답을 받아 로그인 처리하도록 되어 있지만, JWT가 `'dummy-jwt-token'` 문자열로 하드코딩되어 있었다. 이후 모든 기능(스케줄러 개인화, 게시판 작성, 관리자 페이지)이 이 인증 위에서 동작하므로 가장 먼저 완결시켰다.

착수 시점에 `backend/backend_gacha/src/main/java/com/gacha/gachascheduler/` 하위에 이미 `UserEntity`(googleId, email, role 등), `Role` enum(`USER`/`SUB_ADMIN`/`MAIN_ADMIN`), `UserService.findOrCreateUser`, `AuthController`(Google ID 토큰 검증)가 구현되어 있었음을 뒤늦게 확인했다. 아래 체크리스트는 새로 만든 것이 아니라 **기존 코드를 완결시킨 내역**이다. (SecurityConfig에 미완성 상태로 남아있던 Spring 서버 리다이렉트형 `oauth2Login`/`CustomOAuth2User`는 프론트가 쓰지 않는 경로라 제거했다.)

### 백엔드

- [x] `User` 엔티티 — 기존 `UserEntity`(email, name, googleId, role, userCode 등) 재사용
- [x] `POST /api/auth/google`: 기존 `AuthController`가 이미 Google ID 토큰 검증(`google-api-client`) → `UserService.findOrCreateUser`로 유저 생성/조회까지 구현되어 있었음. 여기에 자체 JWT 발급(`UserResponseDto.token`)을 추가
- [x] JWT 발급/검증: `com.gacha.gachascheduler.security.JwtProvider` 신규 추가 (jjwt 0.11.5, HS256, 서명 키는 `JWT_SECRET` 환경변수)
- [x] Spring Security 설정: `com.gacha.gachascheduler.config.SecurityConfig`를 stateless JWT 필터 기반으로 재구성. `/api/auth/google`, `/api/hello`는 permitAll, 나머지는 인증 필요. `@EnableMethodSecurity` 추가로 `GameController`의 `@PreAuthorize`가 실제로 동작하도록 수정 (이전에는 애노테이션만 있고 미적용 상태였음)
- [x] Role 기반 접근 제어: 기존 `Role`(`USER`/`SUB_ADMIN`/`MAIN_ADMIN`) 그대로 사용. 별도 `ADMIN` 단일 role은 만들지 않음 — 이후 파트의 관리자 보호도 이 enum 기준으로 작성할 것
- [ ] 공통 예외 처리(`@ControllerAdvice`)와 공통 응답 포맷 — 아직 미착수. 현재는 컨트롤러별로 개별 처리 중이라 파트 03 이후 도메인 API를 추가하며 필요성이 커지면 별도로 진행

### 프론트엔드

- [x] `LoginPage`의 `login(userData, 'dummy-jwt-token')`을 백엔드가 실제 발급한 JWT로 교체 (backend 세션이 반영)
- [x] API 요청 공통 클라이언트에 `Authorization: Bearer <token>` 자동 첨부 — `src/api/apiClient.js`의 `apiFetch` (이후 모든 백엔드 호출은 이 함수 사용)
- [x] 토큰 만료/401 처리 시 공통 로그아웃 로직 (현재 `apiFetch` 내부에서 처리)
- [x] `role` 기반 UI 분기 — 기존 `Sidebar`에 구현됨
- [ ] 로그인/최초 가입 시 **이용약관(ToS) 및 커뮤니티 법적 책임 동의 체크 UI** 기획 및 추가 (Phase 2)

## 설계 메모

- JWT 서명 키(`JWT_SECRET`)는 [01. 보안 강화](01-security-hardening.md)와 동일한 방식으로 환경변수 관리
- localStorage에 JWT를 저장하는 현재 방식은 XSS 위험이 있으나 초기 버전에서는 유지하고, 추후 httpOnly 쿠키 전환은 별도 검토 항목으로 남김

## DoD

- [x] 백엔드: 구글 로그인 → ID 토큰 검증 → 자체 JWT 발급까지 동작 (`SecurityConfigTest`, `JwtProviderTest`, `UserServiceTest`로 검증. 실제 Google 토큰을 이용한 수동 e2e 확인은 아직 안 함 — 프론트 연동 시 함께 확인)
- [x] 보호된 API가 토큰 없이는 401을 반환 (`SecurityConfigTest`)
- [x] 관리자 전용 API(`GET /api/admin/games`)가 `USER` role 토큰으로는 403, `SUB_ADMIN`/`MAIN_ADMIN` role 토큰으로는 통과함을 확인 (`SecurityConfigTest`)
- [ ] 프론트: `LoginPage`가 `'dummy-jwt-token'` 대신 백엔드가 반환한 `token`을 사용하도록 수정
- [ ] 프론트: API 클라이언트에 `Authorization: Bearer <token>` 자동 첨부 + 401 처리
