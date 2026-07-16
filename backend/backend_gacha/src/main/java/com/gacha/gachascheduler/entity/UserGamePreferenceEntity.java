package com.gacha.gachascheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_game_preferences")
@IdClass(UserGamePreferenceId.class)
@Data
public class UserGamePreferenceEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "game_code")
    private String gameCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
