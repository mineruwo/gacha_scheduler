package com.gacha.gachascheduler.entity;

import com.gacha.gachascheduler.enums.PendingScheduleStatus;
import com.gacha.gachascheduler.enums.ScheduleCategory;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 파트 07 — AI 스케줄 자동 파싱 파이프라인의 "승인 대기" 스테이징 테이블.
 * docs/plans/07-extended-features-planning.md 4번 항목. 실제 스크래퍼/LLM 파서(외부 API 자격증명 필요)는
 * 이번 범위에 포함하지 않고, 파싱된 결과를 검토/승인/반려하는 어드민 워크플로만 구현한다.
 */
@Entity
@Table(name = "pending_schedules")
@Data
public class PendingScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_code", nullable = false)
    private String gameCode;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleCategory category;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 파서 출처 메모(예: "HoYoLAB 공지" 등). 실제 자동 수집 전까지는 수동/임시 입력용. */
    @Column(name = "source_note")
    private String sourceNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PendingScheduleStatus status = PendingScheduleStatus.PENDING;

    /** 승인 시 실제로 생성된 ScheduleEvent의 id (감사 추적용, 반려 시 null 유지) */
    @Column(name = "approved_schedule_id")
    private Long approvedScheduleId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
