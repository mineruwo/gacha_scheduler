# 05. 공략 게시판

상태: 완료 (백엔드 + 프론트, H2 e2e 검증)
선행 조건: [02. 인증/공통 기반](02-auth-foundation.md), [03. 스케줄러](03-scheduler.md) (채널이 게임에 종속)

## 기능 요구사항 (README 기준)

- 게임 공략/정보 공유 커뮤니티 게시판
- '채널' 형식으로 게임별 게시판 구분
- DCinside, Arca.live류 커뮤니티의 글 작성 템플릿 제공 및 필수 커뮤니티 기능 추가 (Phase 2 확장)
  - 추천/비추천 및 인기글(개념글) 필터링 시스템
  - 이미지/미디어 첨부 및 리치 텍스트 에디터 연동
  - 세부 카테고리(말머리/태그) 및 통합 검색 기능
  - 유저 신고 및 제재(블라인드) 시스템

## 작업 범위

### 백엔드

`Game`과의 연결은 04 파트(가챠)와 같은 컨벤션으로 `gameId`(Long, games.id)를 사용했다. 지연 로딩 연관관계는 이전 파트들에서 겪은 문제(04 파트 참고) 때문에 아예 두지 않고, 서비스 레이어에서 `GameRepository`/`ChannelRepository`/`UserRepository`로 명시적으로 배치 조회해 DTO를 조립한다.

- [x] `Channel` 엔티티(`ChannelEntity`): gameId, name, description — 관리자 CRUD(`ChannelController` → `/api/admin/channels`)
- [x] `Post` 엔티티(`PostEntity`): channelId, authorId, title, content, templateType, viewCount
- [x] `Comment` 엔티티(`CommentEntity`): postId, authorId, content, parentCommentId(대댓글)
- [x] `PostTemplate` — 별도 엔티티/테이블 대신 `PostTemplateType` enum(`GUIDE`/`QUESTION`/`FREE`)으로 단순화. 동적 스키마가 필요해지면 별도 테이블로 승격
- [x] API
  - [x] `GET /api/channels?gameId=`(비로그인 가능, `PublicChannelController`), 관리자 `POST/PUT/DELETE /api/admin/channels`
  - [x] `GET /api/channels/{channelId}/posts`(페이지네이션, `Pageable` 기반, 비로그인 가능), `POST /api/channels/{channelId}/posts`(로그인 필요)
  - [x] `GET /api/posts/{postId}`(비로그인 가능, 조회 시 viewCount +1), `PUT/DELETE /api/posts/{postId}`(로그인 + 본인 글 또는 관리자)
  - [x] `GET /api/posts/{postId}/comments`(비로그인 가능), `POST /api/posts/{postId}/comments`(로그인 필요), `DELETE /api/comments/{commentId}`(로그인 + 본인 댓글 또는 관리자)
  - [x] 작성/수정/삭제는 로그인 + 본인 글만 허용(관리자는 예외) — `PostService`/`CommentService`에서 `Spring Security`의 `AccessDeniedException`을 직접 던져 기존 403 핸들러(`SecurityConfig`)를 재사용

### 프론트엔드

- [x] `StrategyBoardPage`: 채널 탭 → 채널별 게시글 목록(페이지네이션) → 상세/댓글(대댓글 1단계, 답글/삭제는 본인 또는 관리자만 노출)을 한 페이지 내 상태 전환으로 구현. 별도 라우트 없이 목록↔상세를 토글
- [x] `NoticeCreationPage`: 채널 선택 + 템플릿(공략/질문/자유) 선택형 글쓰기 폼, 등록 후 게시판으로 리다이렉트
- [x] `ChannelManagementPage`(관리자): 채널 등록/수정/삭제 — `GameManagementPage`와 동일한 폼+테이블 CRUD 패턴 재사용
- `frontend/src/api/boardApi.js`(채널/게시글/댓글 공개+로그인 API), `adminBoardApi`(채널 CRUD) 신규 작성

## 설계 메모

- **[Phase 1] 초기 버전 (현재 완료)**: 백엔드는 `content`를 순수 문자열(TEXT)로 저장하며, 초기 프론트는 텍스트 또는 마크다운 외부 링크 이미지 삽입만 허용.
- **[Phase 2] 커뮤니티 고도화 (추후 과제)**:
  - **이미지 업로드**: AWS S3 등 클라우드 스토리지 연동 및 본문 내 이미지 첨부 지원.
  - **추천/베스트 시스템**: `Post`에 추천/비추천 컬럼(또는 별도 Like 테이블)을 추가하여, 일정 추천 수 이상 게시글을 '베스트(개념글)'로 분류.
  - **검색 및 태그**: 제목, 내용 검색 API 추가 및 동적 태그(말머리) 시스템 도입.
  - **신고 시스템**: `Report` 테이블을 신설하여 부적절한 게시물/댓글 신고 접수 및 누적 시 자동 블라인드 처리 기획.
  - **법적 방어 및 저작권 대응 (신규)**: 
    - 하단(Footer)에 **저작권 침해 신고 채널(이메일 등)** 명시 및 관리자 즉각 블라인드 대응 프로세스 구축.
    - 미공개 데이터(유출/스포일러) 업로드 방지를 위한 운영 정책 마련 또는 '유출 전용 탭(비검색 노출)' 신설 등 게임사 저작권 마찰 방어 구조 설계.
- `GET /api/channels/{channelId}/posts`는 Spring Data의 `Page<PostResponseDto>`를 그대로 직렬화해서 반환한다(`content`/`totalElements`/`totalPages`/`number` 등 포함). 페이지 파라미터는 `?page=&size=&sort=` 쿼리스트링 (향후 검색 파라미터 `&query=` 및 베스트 필터 `&isBest=true` 추가 고려).
- 댓글은 대댓글 1단계만 지원(`parentCommentId`로 부모 표시, 트리 렌더링은 프론트 책임)

## DoD

- [x] 게임별 채널이 분리되어 게시글이 채널 단위로 격리됨 (`PostServiceTest.getPostsByChannelReturnsOnlyThatChannelsPostsNewestFirst`)
- [x] 로그인 유저가 템플릿을 선택해 글을 작성/수정/삭제할 수 있음, 본인 아니면 403 (`PostServiceTest`)
- [x] 댓글 작성/조회 동작, 본인 아니면 삭제 403 (`CommentServiceTest`)
- [x] 관리자가 채널을 생성/삭제할 수 있음 (`SecurityConfigTest`로 인증/권한 확인, CRUD 자체는 GameController와 동일 패턴이라 별도 서비스 테스트는 생략)
- [x] 프론트: 위 화면 3종 구현 및 실동작 확인 — H2 인메모리 서버로 채널 생성→글쓰기→목록 노출→상세(조회수 증가)→댓글/대댓글 작성→권한별(작성자/타인/관리자) 삭제 버튼 노출 차이까지 브라우저로 직접 검증 완료. Phase 2(추천/이미지 업로드/신고 등)는 범위 밖
