# 10. 푸시 알림 시스템 (Push Notifications)

상태: 기획 완료 / 개발 대기
선행 조건: [02. 인증/공통 기반](02-auth-foundation.md), [03. 게임/배너 데이터 관리](03-game-banner-management.md), 모바일 앱 환경(Flutter) 셋업 완료

## 배경

사용자의 앱 리텐션(재접속률)을 높이기 위해, 관심 있는 게임의 중요 일정(이벤트 시작 등)을 하루 전이나 당일에 알려주는 네이티브 푸시 알림(FCM) 시스템을 구축한다. 
스팸성 알림을 방지하기 위해 유저가 구독한 카테고리(선호 게임)에 대해서만 발송하며, 알림 발송 여부는 관리자가 일정을 등록할 때 수동으로 제어(옵션)한다.

## 작업 범위

### 백엔드 (Spring Boot)

1.  **Firebase 인프라 구성:** Firebase Admin SDK를 의존성에 추가하고, 프로젝트의 `ServiceAccountKey.json`을 로드하여 FCM 발송 객체(`FirebaseMessaging`)를 빈(Bean)으로 등록한다.
2.  **기기 토큰 관리 (`UserDeviceToken`):**
    *   `userId`, `deviceToken`, `lastUpdatedAt`을 저장하는 테이블 생성 (한 유저당 여러 기기 지원).
    *   모바일 앱 구동 시 호출할 `POST /api/users/me/device-token` 엔드포인트 구현 (토큰 신규 등록 및 갱신 멱등성 보장).
3.  **일정 발송 옵션 추가:**
    *   `ScheduleEvent` 엔티티 및 DTO에 `notifyBeforeOneDay`(하루 전 알림), `notifyOnStartDay`(당일 알림) boolean 필드 추가.
4.  **스케줄러 크론 잡 (`@Scheduled`):**
    *   매일 일정 시간(예: 오전 9시 또는 자정)에 도는 백그라운드 스레드 구성.
    *   로직: 시작일이 내일이고 `notifyBeforeOneDay == true`인 일정, 또는 시작일이 오늘이고 `notifyOnStartDay == true`인 일정을 조회.
    *   해당 일정의 `gameId`를 `UserGamePreference`로 등록해 둔 유저들의 `deviceToken` 목록을 추출.
    *   FCM `sendMulticast` API를 통해 알림 일괄 발송 (알림 제목/내용 템플릿 적용).

### 프론트엔드 (React Web - Admin)

1.  **관리자 폼 수정 (`GameManagementPage`):** 일정을 등록/수정하는 모달 폼에 "알림 발송 옵션 (하루 전 / 당일)" 체크박스를 추가하여 서버로 전송.

### 모바일 앱 (Flutter)

1.  **FCM 연동:** `firebase_core`, `firebase_messaging` 패키지 설정.
2.  **권한 요청:** 앱 최초 구동 시 유저에게 푸시 알림 권한(`requestPermission`) 요구.
3.  **토큰 동기화:** 권한 승인 시 FCM 토큰(`getToken`)을 얻어 백엔드의 `POST /api/users/me/device-token`으로 즉시 전송.
4.  **딥링크 연동 (옵션):** 푸시 알림을 클릭하고 앱으로 진입했을 때, 해당 일정 상세 정보나 스케줄러 탭으로 바로 이동하도록 라우팅 처리.

## 개발 참고 사항

- 백엔드 로컬 개발/테스트 시 Firebase 프로젝트 연동이 필요하므로, 사용자로부터 서비스 어카운트 키를 제공받거나 로컬 모의(Mock) 객체로 알림 로그만 찍히도록 분기 처리해야 함.
- 만료된 기기 토큰으로 인해 FCM 발송 에러가 발생할 경우, 백엔드에서 해당 토큰 레코드를 DB에서 삭제(Soft/Hard Delete)하는 에러 핸들링 로직 권장.
