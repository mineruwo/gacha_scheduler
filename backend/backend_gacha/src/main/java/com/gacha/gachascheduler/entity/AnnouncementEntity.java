package com.gacha.gachascheduler.entity;

import com.gacha.gachascheduler.enums.AnnouncementType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 파트 06 Phase 2 — 사이트 공지사항(NOTICE) / 앱 내 팝업 배너(POPUP) 공통 엔티티.
 * NOTICE는 title/content(텍스트), POPUP은 title/imageUrl(+선택적 linkUrl)을 주로 사용하지만
 * 두 용도가 "관리자가 켜고 끌 수 있는 활성 기간이 있는 공지"라는 점에서 동일해 하나의 테이블로 통합했다.
 */
@Entity
@Table(name = "announcements")
@Data
public class AnnouncementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    /** null이면 종료일 없이 계속 노출(수동으로 isActive를 꺼야 내려감) */
    @Column(name = "end_at")
    private OffsetDateTime endAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
