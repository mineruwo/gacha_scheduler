# Gacha Scheduler 프로젝트

가챠 게임 유저들을 위한 스케줄러, 가챠 시뮬레이터, 공략 게시판을 제공하는 종합 플랫폼입니다.

## ✨ 핵심 기능

### 1. 게임 업데이트 스케줄러 (주요 플랫폼: Web/React)

*   서브컬쳐 게임의 업데이트 및 이벤트 일정을 간트 차트 형식으로 제공합니다.
*   **로그인 기반 개인화:**
    *   사용자는 관심 있는 게임을 선택하여 자신만의 스케줄 뷰를 필터링할 수 있습니다.
    *   필터링 정보는 DB에 저장되어 어느 기기에서 접속하든 동일한 환경을 제공합니다.

### 2. 가챠 시뮬레이터 (플랫폼: Web/React, Mobile/Flutter)

*   실제 게임과 유사한 경험을 제공하는 가챠 시뮬레이터입니다.
*   **상세 로직:**
    *   캐릭터/아이템별 가중치를 적용합니다.
    *   '확정 천장' 시스템을 구현하여 일정 횟수 이상 시도 시 확정 보상을 제공합니다.
    *   사용자가 필터링한 게임에 맞춰 시뮬레이션 가능한 캐릭터 목록이 동적으로 변경됩니다.

### 3. 공략 게시판 (주요 플랫폼: Web/React)

*   게임 공략 및 정보 공유를 위한 커뮤니티 게시판입니다.
*   **주요 특징:**
    *   '채널' 형식으로 게임별 게시판을 구분합니다.
    *   DCinside, Arca.live 등 커뮤니티 사이트의 장점을 참고한 글 작성 템플릿을 제공하여 작성 편의성을 높입니다.

## 🛠️ 기술 스택

*   **Web Frontend:** React (`frontend/`) — PC/모바일 브라우저 반응형 대응
*   **Mobile App:** Capacitor 하이브리드 셸 (`mobile_shell/`) — 위 React 웹을 WebView로 감싸 스토어 배포. 웹을 재배포하면 앱 컨텐츠도 함께 갱신되는 구조
*   **Backend:** Spring Boot, PostgreSQL

> 모바일 전략 변경 (2026-07-15): 기존 Flutter 앱(`frontend_mobile/`)은 별도 모바일 화면 유지 부담으로 하이브리드 셸 방식으로 전환. Flutter 코드는 추후 네이티브 전환 대비용으로 보관한다.

## 📋 작업 계획

파트별 구현 계획은 [docs/plans](docs/plans/00-overview.md)에서 확인할 수 있습니다.

> **AI 병렬 작업 시 필독**: 여러 AI 세션이 동시에 작업 중입니다. 처음 합류했다면 [docs/HANDOVER.md](docs/HANDOVER.md)로 현재 상태를 빠르게 파악한 뒤, 작업 시작 전 [docs/SYNC.md](docs/SYNC.md)를 읽고, 작업 종료 시 로그를 남겨 서로의 최신 작업을 공유하세요.

## 🔐 로컬 개발 환경 설정

민감한 값(DB 접속 정보, OAuth 시크릿)은 저장소에 커밋하지 않고 환경변수/로컬 override 파일로 관리합니다.

**Backend**

```bash
cp backend/backend_gacha/src/main/resources/application-local.properties.example \
   backend/backend_gacha/src/main/resources/application-local.properties
# application-local.properties 를 열어 DB_URL, DB_USERNAME, DB_PASSWORD,
# GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET 실제 값을 채운다.
```

**Frontend**

```bash
cp frontend/.env.example frontend/.env
# VITE_API_BASE_URL, VITE_GOOGLE_CLIENT_ID 값을 채운다.
```

두 파일 모두 `.gitignore`에 등록되어 있어 커밋되지 않습니다.