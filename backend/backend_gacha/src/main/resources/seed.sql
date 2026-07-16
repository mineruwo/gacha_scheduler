-- Gacha Scheduler 로컬 개발 및 테스트용 데이터 시드 스크립트 (PostgreSQL)
-- 주의: Spring Boot 애플리케이션을 최소 1회 기동하여 DDL(ddl-auto=update)에 의해 테이블이 생성된 후 실행하십시오.

-- 기존 데이터 초기화 (선택 사항)
TRUNCATE TABLE announcements CASCADE;
TRUNCATE TABLE banner_characters CASCADE;
TRUNCATE TABLE banners CASCADE;
TRUNCATE TABLE characters CASCADE;
TRUNCATE TABLE schedule_events CASCADE;
TRUNCATE TABLE user_game_preferences CASCADE;
TRUNCATE TABLE games CASCADE;
TRUNCATE TABLE users CASCADE;

-- 1. 게임 등록 (games)
INSERT INTO games (id, title, game_code, has_gacha, has_pass, can_record, is_service, comment, can_manage_schedule, can_track_currency, created_at, updated_at) VALUES
(1, '원신', 'genshin', true, true, true, true, '원신(Genshin Impact) 로컬 테스트용 게임 데이터', true, true, NOW(), NOW()),
(2, '붕괴: 스타레일', 'hsr', true, true, true, true, '붕괴: 스타레일(Honkai Rail) 로컬 테스트용 게임 데이터', true, true, NOW(), NOW());

-- PostgreSQL Sequence 값 갱신 (ID 충돌 방지)
SELECT setval(pg_get_serial_sequence('games', 'id'), 2);

-- 2. 게임 업데이트/이벤트 일정 등록 (schedule_events)
INSERT INTO schedule_events (game_code, title, category, start_at, end_at, description, created_at, updated_at) VALUES
('genshin', '원신 5.4 버전 업데이트', 'UPDATE', NOW() - INTERVAL '1 day', NOW() + INTERVAL '40 days', '신규 지역 개방 및 다양한 캐릭터 복각 업데이트', NOW(), NOW()),
('genshin', '해등절 축제 이벤트', 'EVENT', NOW() + INTERVAL '3 days', NOW() + INTERVAL '18 days', '리월 해등절 축제 이벤트 진행 및 무료 뽑기권 증정', NOW(), NOW()),
('genshin', '5.4 서버 정기 점검', 'MAINTENANCE', NOW() - INTERVAL '1 day', NOW() - INTERVAL '19 hours', '버전 업데이트를 위한 점검 (5시간 소요)', NOW(), NOW()),
('hsr', '스타레일 3.1 버전 업데이트', 'UPDATE', NOW() - INTERVAL '3 days', NOW() + INTERVAL '38 days', '페나코니 에필로그 및 신규 5성 캐릭터 등장', NOW(), NOW()),
('hsr', '시뮬레이션 우주: 무한의 차원', 'EVENT', NOW() + INTERVAL '5 days', NOW() + INTERVAL '25 days', '새로운 기믹의 시뮬레이션 우주 이벤트', NOW(), NOW());

-- 3. 가챠 시뮬레이터용 캐릭터 등록 (characters)
-- 5성 픽업 라인업은 실제 게임의 현재(2026-07-16 기준) 진행 배너를 반영함(웹 검색으로 확인, 아래 배너 섹션 참고)
INSERT INTO characters (id, game_id, name, rarity, icon_url, created_at, updated_at) VALUES
-- 원신 캐릭터 (game_id = 1) — 6.7 버전(산드로네 배너, 2026-07-01~07-21)
(1, 1, '산드로네', 5, 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(2, 1, '시틀랄리', 5, 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(3, 1, '피슬', 4, 'https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(4, 1, '베넷', 4, 'https://images.unsplash.com/photo-1563089145-599997674d42?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(5, 1, '향릉', 4, 'https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
-- 스타레일 캐릭터 (game_id = 2) — 4.4 버전(히메코·노바 배너, 2026-07-15~08-25)
(6, 2, '히메코·노바', 5, 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(7, 2, '어벤츄린', 5, 'https://images.unsplash.com/photo-1614850523459-c2f4c699c52e?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(8, 2, '갤러거', 4, 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(9, 2, '페라', 4, 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
-- 3성 캐릭터 (하위 풀을 채워 5성/4성 자연 확률을 현실적인 수준으로 낮추는 역할, 아래 배너 가중치 설명 참고)
(10, 1, '앰버', 3, 'https://images.unsplash.com/photo-1520975916090-3105956dac38?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(11, 1, '케이아', 3, 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(12, 1, '리사', 3, 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(13, 2, '나타샤', 3, 'https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(14, 2, '아를란', 3, 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=60', NOW(), NOW());

SELECT setval(pg_get_serial_sequence('characters', 'id'), 14);

-- 4. 가챠 시뮬레이터용 배너 등록 (banners)
-- 실제 게임의 현재 진행 배너를 반영(2026-07-16 기준 웹 검색으로 확인, docs/SYNC.md 로그 참고)
INSERT INTO banners (id, game_id, name, pity_threshold, rate_up_rate, start_at, end_at, created_at, updated_at) VALUES
(1, 1, '루나 Ⅷ - 산드로네 픽업', 90, 0.5, '2026-07-01 00:00:00+09', '2026-07-21 18:59:00+09', NOW(), NOW()),
(2, 2, '히메코·노바 워프', 80, 0.56, '2026-07-15 00:00:00+09', '2026-08-25 23:59:00+09', NOW(), NOW());

SELECT setval(pg_get_serial_sequence('banners', 'id'), 2);

-- 5. 배너별 소속 캐릭터 매핑 및 확률 설정 (banner_characters)
-- BannerService.pull()의 자연 확률 계산은 "최고 등급(rarity 최댓값) 가중치 합 / 풀 전체 가중치 합"이다.
-- weight는 절대 확률처럼 보이도록 설계했으므로(개별 5성 0.006 = 0.6%) 풀 전체 가중치 합이 1.0에
-- 근접해야 그 의도대로 동작한다. 3성 캐릭터가 없던 이전 버전은 풀 합계가 0.063(4성까지)에 불과해
-- 5성 자연 확률이 0.012/0.063 ≈ 19%까지 뛰는 버그가 있었다(사용자 리포트, 2026-07-16) — 3성을
-- 추가해 풀 합계를 ~1.0으로 맞춰서 5성/4성 비율은 그대로 유지한 채 확률만 정상화했다.
-- 원신 산드로네 배너 (banner_id = 1)
-- 5성 픽업 캐릭터 (산드로네, 픽업)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES (1, 1, 0.006, true);
-- 5성 동시 등장 캐릭터 (시틀랄리, 비픽업)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES (1, 2, 0.006, false);
-- 4성 픽업 캐릭터들 (피슬, 베넷, 향릉)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES
(1, 3, 0.017, true),
(1, 4, 0.017, true),
(1, 5, 0.017, true);
-- 3성 상시 캐릭터들 (앰버, 케이아, 리사) — 풀 합계를 ~1.0으로 채워 위 5성/4성 확률을 정상화
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES
(1, 10, 0.312, false),
(1, 11, 0.312, false),
(1, 12, 0.312, false);

-- 스타레일 히메코·노바 배너 (banner_id = 2)
-- 5성 픽업 캐릭터 (히메코·노바, 픽업)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES (2, 6, 0.006, true);
-- 5성 상시 캐릭터 (어벤츄린, 비픽업)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES (2, 7, 0.006, false);
-- 4성 픽업 캐릭터들 (갤러거, 페라)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES
(2, 8, 0.025, true),
(2, 9, 0.025, true);
-- 3성 상시 캐릭터들 (나타샤, 아를란) — 풀 합계를 ~1.0으로 채워 위 5성/4성 확률을 정상화
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES
(2, 13, 0.469, false),
(2, 14, 0.469, false);

-- 6. 공지사항/팝업 배너 예시 (announcements, 파트 06 Phase 2)
INSERT INTO announcements (id, type, title, content, image_url, link_url, start_at, end_at, is_active, created_at, updated_at) VALUES
(1, 'NOTICE', '서버 점검 안내', '7/20 새벽 2시~4시 정기 점검이 진행됩니다.', NULL, NULL, NOW() - INTERVAL '1 day', NOW() + INTERVAL '9 days', true, NOW(), NOW()),
(2, 'POPUP', '7월 출석 이벤트', NULL, 'https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=400', '/board', NOW() - INTERVAL '1 day', NOW() + INTERVAL '15 days', true, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('announcements', 'id'), 2);

-- 7. 테스트용 유저 등록 (users)
-- 구글 로그인 우회 테스트 시 유용하게 사용 가능 (USER, MAIN_ADMIN 권한 예제)
INSERT INTO users (id, email, name, google_id, user_code, role, is_deleted, created_at, updated_at) VALUES
(1, 'admin@example.com', '테스트 관리자', 'google-test-admin-12345', 'UC-ADMIN-9999', 'MAIN_ADMIN', false, NOW(), NOW()),
(2, 'user@example.com', '일반 사용자', 'google-test-user-12345', 'UC-USER-0001', 'USER', false, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('users', 'id'), 2);
