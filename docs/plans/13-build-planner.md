# 13. 캐릭터 육성 재화 계산기 (Build Planner)

상태: 기획 완료 / 개발 대기
선행 조건: [03. 게임/배너 데이터 관리](03-game-banner-management.md), [05. 공략 커뮤니티 게시판](05-strategy-board.md)

## 배경

사용자가 가챠 시뮬레이터에서 획득하거나 실제로 보유한 캐릭터를 성장시키기 위해 필요한 재화량을 정확히 계산해 주는 편의 기능이다. 
계산 결과를 단순히 개인 확인용으로 끝내지 않고, 커뮤니티 게시판에 위젯 형태로 첨부할 수 있게 하여 양질의 공략글 생성을 유도한다.

## 핵심 기획

1. **레벨업 재화 계산 로직:**
   - 유저가 현재 레벨과 목표 레벨을 입력하면, 해당 구간 돌파에 필요한 아이템(모라, 경험치 책, 보스 재료 등)의 종류와 총합 갯수를 실시간으로 계산한다.
   - **(MVP 스펙)** 초기 릴리즈에는 '캐릭터 레벨업'만 지원하며, 향후 무기와 스킬(특성) 레벨업 계산까지 확장할 수 있도록 DB를 유연하게 설계한다.
2. **초기 데이터 적재 방식:**
   - 크롤링보다 빠르고 안정적인 MVP 론칭을 위해, 인기 캐릭터 10~20종의 데이터만 관리자가 엑셀이나 어드민 API를 통해 직접 밀어 넣을 수 있도록 한다.
3. **공략 게시판(Community) 연동:**
   - 게시글을 작성할 때, 본문 에디터 툴바에 `[육성 계산기 첨부]` 기능을 넣는다.
   - 자신이 세팅한 목표 레벨과 재화 목록이 담긴 깔끔한 'UI 카드'가 본문에 삽입되어 다른 유저들에게 직관적인 공략 정보를 제공한다.

## 작업 범위

### 백엔드 (Spring Boot)
1. **DB 엔티티 설계:** 
   - `ItemEntity` (id, gameId, name, iconUrl, rarity)
   - `CharacterProgressionEntity` (id, characterId, startLevel, endLevel, itemId, quantity)
2. **조회 로직 (`GET /api/characters/{id}/materials`):** 
   - 쿼리 파라미터 `currentLevel`, `targetLevel`을 받아, 해당 구간(`startLevel >= currentLevel && endLevel <= targetLevel`)에 포함되는 모든 레코드의 `quantity`를 `itemId` 기준으로 합산(Group By)하여 반환.
3. **관리자용 입력 API:** 
   - `POST /api/admin/items` 및 `POST /api/admin/progression` (Bulk Insert 가능하도록 DTO 설계).

### 프론트엔드 / 모바일 (Flutter & React)
1. **계산기 모달/화면 UI:** 
   - 캐릭터 정보 페이지에 '현재 레벨'과 '목표 레벨'을 설정할 수 있는 슬라이더 배치.
   - 하단에 필요 재화 아이콘과 숫자를 그리드 형태로 출력.
2. **게시글 에디터 플러그인 연동:** 
   - 글 작성 화면에서 계산기를 호출하고, [결과 본문에 삽입]을 누르면 특수한 JSON 숏코드(Shortcode)나 HTML 블록 형태로 본문에 추가.
   - 글 읽기 화면에서는 해당 숏코드를 인식하여 읽기 전용(Read-only)의 예쁜 재화 카드 위젯으로 렌더링.
