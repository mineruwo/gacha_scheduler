-- Gacha Scheduler 로컬 개발 및 테스트용 데이터 시드 스크립트 (PostgreSQL)
-- 주의: Spring Boot 애플리케이션을 최소 1회 기동하여 DDL(ddl-auto=update)에 의해 테이블이 생성된 후 실행하십시오.

-- 기존 데이터 초기화 (선택 사항)
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
INSERT INTO characters (id, game_id, name, rarity, icon_url, created_at, updated_at) VALUES
-- 원신 캐릭터 (game_id = 1)
(1, 1, '푸리나', 5, 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(2, 1, '느비예트', 5, 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(3, 1, '피슬', 4, 'https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(4, 1, '베넷', 4, 'https://images.unsplash.com/photo-1563089145-599997674d42?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(5, 1, '향릉', 4, 'https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
-- 스타레일 캐릭터 (game_id = 2)
(6, 2, '아케론', 5, 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(7, 2, '어벤츄린', 5, 'https://images.unsplash.com/photo-1614850523459-c2f4c699c52e?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(8, 2, '갤러거', 4, 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=150&auto=format&fit=crop&q=60', NOW(), NOW()),
(9, 2, '페라', 4, 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=150&auto=format&fit=crop&q=60', NOW(), NOW());

SELECT setval(pg_get_serial_sequence('characters', 'id'), 9);

-- 4. 가챠 시뮬레이터용 배너 등록 (banners)
INSERT INTO banners (id, game_id, name, pity_threshold, rate_up_rate, start_at, end_at, created_at, updated_at) VALUES
(1, 1, '정토의 시선 - 푸리나 픽업', 90, 0.5, NOW() - INTERVAL '1 day', NOW() + INTERVAL '15 days', NOW(), NOW()),
(2, 2, '고요한 연옥의 불꽃 - 아케론 픽업', 80, 0.56, NOW() - INTERVAL '2 days', NOW() + INTERVAL '20 days', NOW(), NOW());

SELECT setval(pg_get_serial_sequence('banners', 'id'), 2);

-- 5. 배너별 소속 캐릭터 매핑 및 확률 설정 (banner_characters)
-- 원신 푸리나 배너 (banner_id = 1)
-- 5성 픽업 캐릭터 (푸리나, 픽업)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES (1, 1, 0.006, true);
-- 5성 상시 캐릭터 (느비예트, 비픽업)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES (1, 2, 0.006, false);
-- 4성 픽업 캐릭터들 (피슬, 베넷, 향릉)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES
(1, 3, 0.017, true),
(1, 4, 0.017, true),
(1, 5, 0.017, true);

-- 스타레일 아케론 배너 (banner_id = 2)
-- 5성 픽업 캐릭터 (아케론, 픽업)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES (2, 6, 0.006, true);
-- 5성 상시 캐릭터 (어벤츄린, 비픽업)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES (2, 7, 0.006, false);
-- 4성 픽업 캐릭터들 (갤러거, 페라)
INSERT INTO banner_characters (banner_id, character_id, weight, is_pickup) VALUES
(2, 8, 0.025, true),
(2, 9, 0.025, true);

-- 6. 테스트용 유저 등록 (users)
-- 구글 로그인 우회 테스트 시 유용하게 사용 가능 (USER, MAIN_ADMIN 권한 예제)
INSERT INTO users (id, email, name, google_id, user_code, role, is_deleted, created_at, updated_at) VALUES
(1, 'admin@example.com', '테스트 관리자', 'google-test-admin-12345', 'UC-ADMIN-9999', 'MAIN_ADMIN', false, NOW(), NOW()),
(2, 'user@example.com', '일반 사용자', 'google-test-user-12345', 'UC-USER-0001', 'USER', false, NOW(), NOW());

SELECT setval(pg_get_serial_sequence('users', 'id'), 2);
