# 06. 관리자/유저 관리

상태: 완료 (백엔드 + 프론트, H2 e2e 검증) — Phase 2 중 서버 유지비 프로그레스 바 API·가챠 확률 제어 UI·공지사항/팝업배너 관리는 완료
선행 조건: [02. 인증/공통 기반](02-auth-foundation.md), [03~05 파트] (히스토리가 각 기능 활동을 참조)

## 배경

`UserManagementPage`(관리자), `UserProfilePage`, `HistoryPage`는 앞선 파트들이 만든 데이터(게시글, 뽑기 기록, 필터 등)를 조회/관리하는 화면이므로 다른 기능 파트들이 최소 1개 이상 완료된 뒤 진행한다.

## 작업 범위

### 백엔드

- [x] `GET /api/admin/users?query=` — 유저 목록/검색(이메일·닉네임 부분일치, `query` 생략 시 전체), `PUT /api/admin/users/{id}/role` — 역할 변경
- [x] `GET /api/users/me` — 내 프로필 조회, `PUT /api/users/me` — 닉네임/프로필 이미지 수정 (`UserProfileController`)
- [x] `GET /api/users/me/history` — 내가 작성한 글 + 댓글 통합 조회 (`UserHistoryService`). **가챠 뽑기 기록은 파트 04에서 서버 측 영속 저장을 하지 않기로 했으므로 포함하지 않음** — docs/plans/04-gacha-simulator.md 설계 메모 참고
- [x] 유저 정지: 새 `status` 필드를 추가하지 않고 기존 `UserEntity.isDeleted`/`deletedAt`(이미 있던 소프트 삭제 필드)를 재사용 — `DELETE /api/admin/users/{id}`가 `UserService.deleteUser`(기존 메서드) 호출
- [x] 서버 유지비 프로그레스 바 데이터 저장/조회 API (`GET/PUT /api/admin/settings/server-cost`, 공개 조회는 `GET /api/settings/server-cost`) — 상세는 [08-monetization-strategy.md](08-monetization-strategy.md) "백엔드 구현 현황" 참고

## Phase 2: 추가 관리 기능

- [x] **공지사항(Notification) / 앱 내 팝업 배너(Banner) 관리 엔드포인트** (2026-07-16) — `AnnouncementEntity`(NOTICE/POPUP 공용, `type`/`title`/`content`/`imageUrl`/`linkUrl`/`startAt`/`endAt`/`isActive`) 신규. 공개 조회 `GET /api/announcements?type=`(활성 기간+isActive인 것만), 관리자 CRUD `POST/PUT/DELETE /api/admin/announcements`(전체 목록 무관하게 관리). 프론트: `HomePage`에 활성 NOTICE 목록 표시, `MainLayout`에 전역 `PopupBannerModal`(활성 POPUP 중 오늘 안 본 것 1개, localStorage로 "오늘 하루 보지 않기" 기억), 관리자 페이지 `AnnouncementManagementPage`(`/admin/announcements`). `AnnouncementServiceTest`/`SecurityConfigTest` 추가, 브라우저 e2e로 등록/수정/비활성화/재활성화까지 확인. 상세는 SYNC.md 로그 참고
- [ ] 유저 정지 / 해제 (Banned) 플래그 및 블랙리스트 관리 — 기본 정지는 위 `isDeleted` 재사용으로 이미 되지만, 기간제 정지/사유 기록 등 더 세밀한 관리가 필요하면 별도 작업
- [ ] 서버 유지비 후원 진척도(`GET/PUT /api/admin/settings/server-cost`) 데이터 관리 UI
- [x] **가챠 확률 제어 UI (Probability Management)** (2026-07-16, 사용자 리포트로 착수) — `frontend/src/pages/GachaBannerManagementPage.jsx`(`/admin/gacha-banner`)로 구현 완료
  - [x] 배너 생성 시 `pityThreshold`(천장) 및 `rateUpRate`(픽업 확률) 직접 입력란 제공.
  - [x] 배너 내 캐릭터 풀(Pool)의 가중치(Weight)를 실시간으로 조정할 수 있는 테이블 제공(가중치 인라인 수정 + 픽업 토글 + 추가/제외).
  - [x] 가중치를 입력하면 즉시 **등급별 최종 등장 확률(%)**이 실시간 계산되어 화면에 표시되는 헬퍼 UI 구현. 상세는 04-gacha-simulator.md, SYNC.md 로그 참고 — 백엔드는 기존 admin API(파트 04) 그대로 사용, 신규 백엔드 코드 없음

### 알아둘 점 (다른 파트에 영향)

- `UserService.findOrCreateUser`(파트 02, 구글 로그인 시 유저 생성/조회)는 재로그인 때마다 구글 프로필의 `name`/`profilePictureUrl`로 덮어쓴다. 이번 파트에서 추가한 `PUT /api/users/me`로 닉네임을 바꿔도, 사용자가 구글 로그인을 다시 하면 구글 이름으로 되돌아간다. 기존에 이미 동작 중이고 프론트가 의존할 수 있는 로직이라 이번 파트에서는 건드리지 않았다 — 실제로 문제가 되면 `findOrCreateUser`가 기존 유저의 name/profilePictureUrl을 최초 생성 시에만 설정하도록 바꿔야 한다.

### 프론트엔드

- [x] `UserManagementPage`: 유저 목록/검색 테이블, 역할 변경(드롭다운), 정지 처리(버튼, 이미 정지된 유저는 비활성화)
- [x] `UserProfilePage`: 내 정보 표시/수정 — 실 API(`GET/PUT /api/users/me`)로 연동(기존엔 `AuthContext`의 로그인 시점 값만 정적으로 표시했음). 수정 성공 시 `AuthContext`/localStorage도 함께 갱신해 헤더에 즉시 반영
- [x] `HistoryPage`: 내가 작성한 글/댓글을 탭으로 구분해 조회 (뽑기 기록은 API에 없다는 안내 문구 포함)
- [ ] `SiteSettingsPage` (또는 AdminDashboard 내 탭): 서버 유지비 프로그레스 바 수동 설정(목표액, 현재 달성액) 컴포넌트 추가 (신규 기획 - Phase 2, 이번 작업 범위 밖)
- `frontend/src/api/userApi.js`(`userApi`: 내 프로필/히스토리, `adminUserApi`: 유저 검색/역할변경/정지) 신규 작성

## DoD

- [x] 관리자가 유저 role을 변경하면 해당 유저의 접근 권한이 즉시 반영 (`UserManagementServiceTest`, 다음 요청부터 새 JWT가 새 role을 담아 발급되므로 즉시 반영되려면 재로그인 필요 — 기존 JWT는 발급 시점 role을 그대로 담고 있어 만료 전까지는 예전 권한으로 동작함, 알아둘 점)
- [x] 일반 유저가 자신의 프로필을 수정할 수 있음 (`UserManagementServiceTest.updateProfileChangesNameAndPicture`)
- [x] 히스토리 페이지에 실제 활동 데이터가 파트별로 표시됨 — 글/댓글만 (`UserHistoryServiceTest`)
- [x] 프론트: 위 화면 3종 구현 및 실동작 확인 — H2 인메모리 서버로 관리자 role 변경/정지, 본인 프로필 수정(헤더 즉시 반영), 히스토리 글/댓글 집계까지 브라우저로 직접 검증 완료
