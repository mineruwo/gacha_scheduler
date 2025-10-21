package com.gacha.gachascheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "games")
@Data
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "game_code", unique = true, nullable = false)
    private String gameCode;

    @Column(name = "has_gacha", nullable = false)
    private Boolean hasGacha = false;

    @Column(name = "has_pass", nullable = false)
    private Boolean hasPass = false;

    @Column(name = "can_record", nullable = false)
    private Boolean canRecord = false;

    @Column(name = "is_service", nullable = false)
    private Boolean isService = false;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "can_manage_schedule", nullable = false)
    private Boolean canManageSchedule = false;

    @Column(name = "can_track_currency", nullable = false)
    private Boolean canTrackCurrency = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
