# Gacha Scheduler Database ERD

본 문서는 가챠 스케줄러 앱의 전체 데이터베이스 스키마와 엔티티 간의 관계를 나타내는 ERD입니다. (최근에 추가된 텍스트 RPG, 푸시 알림 엔티티 포함)

```mermaid
erDiagram
    %% Core Entities
    User ||--o{ UserGamePreference : "has preferences"
    User ||--o{ UserDeviceToken : "owns devices (FCM)"
    User ||--o{ Post : "writes"
    User ||--o{ Comment : "writes"
    User ||--|| UserRpgStats : "has stats (Text RPG)"
    User ||--o{ UserInventory : "owns characters (RPG)"
    User ||--o{ Expedition : "starts (RPG)"

    Game ||--o{ Character : "has"
    Game ||--o{ Banner : "has"
    Game ||--o{ ScheduleEvent : "has"
    Game ||--o{ UserGamePreference : "is preferred by"
    Game ||--o{ Post : "is tagged in"

    %% Gacha System Entities
    Banner ||--o{ BannerCharacter : "contains pool"
    Character ||--o{ BannerCharacter : "is in pool"

    %% Community Entities
    Post ||--o{ Comment : "has"

    %% Text RPG Entities
    Character ||--o{ UserInventory : "is stored as"

    User {
        bigint id PK
        string email
        string name
        string profilePictureUrl
        string role "ADMIN / USER"
        string provider "GOOGLE / EMAIL"
        boolean isDeleted
        timestamp deletedAt
    }

    Game {
        bigint id PK
        string name
        string iconUrl
    }

    Character {
        bigint id PK
        bigint gameId FK
        string name
        int rarity
        string element
        string imageUrl
    }

    Banner {
        bigint id PK
        bigint gameId FK
        string name
        timestamp startAt
        timestamp endAt
        int pityThreshold
        float rateUpRate
    }

    BannerCharacter {
        bigint bannerId PK, FK
        bigint characterId PK, FK
        float weight
        boolean isPickup
    }

    ScheduleEvent {
        bigint id PK
        bigint gameId FK
        string title
        text description
        timestamp startAt
        timestamp endAt
        string url
        boolean notifyBeforeOneDay "Push Option"
        boolean notifyOnStartDay "Push Option"
    }

    UserGamePreference {
        bigint userId PK, FK
        bigint gameId PK, FK
    }

    Post {
        bigint id PK
        bigint userId FK
        bigint gameId FK
        string title
        text content
        int views
        string status "PUBLISHED, BANNED"
        timestamp createdAt
        timestamp updatedAt
    }

    Comment {
        bigint id PK
        bigint postId FK
        bigint userId FK
        text content
        boolean isDeleted
        timestamp createdAt
    }

    UserDeviceToken {
        bigint userId FK
        string deviceToken PK
        timestamp lastUpdatedAt
    }

    UserRpgStats {
        bigint userId PK, FK
        int points "Community Points"
        int level
        int experience
        int attackPower
    }

    UserInventory {
        bigint id PK
        bigint userId FK
        bigint characterId FK
        timestamp acquiredAt
    }

    Expedition {
        bigint id PK
        bigint userId FK
        string status "ONGOING, COMPLETED"
        timestamp startTime
        timestamp endTime
    }
```

## 스키마 주요 설명

1. **Gacha System (가챠 시스템):** `Banner`와 `Character`는 M:N 관계이며, 이를 `BannerCharacter` 엔티티가 매핑합니다. 이때 `BannerCharacter` 테이블에 가중치(weight)와 픽업 여부(isPickup)가 저장되어 유동적인 확률 계산이 가능합니다.
2. **Push Notifications (푸시 알림):** `UserGamePreference`를 구독 카테고리로 활용하며, 매일 스케줄러가 `ScheduleEvent`의 시작일을 체크하여 `UserDeviceToken`을 통해 알림을 발송합니다.
3. **Text RPG (게이미피케이션):** 시뮬레이터에서 뽑은 캐릭터는 `UserInventory`에 쌓이며, 이를 기반으로 `Expedition` 테이블을 생성하여 방치형 탐험 데이터를 추적합니다. 커뮤니티 활동(Post, Comment) 시 `UserRpgStats`의 points(재화)가 증가합니다.
