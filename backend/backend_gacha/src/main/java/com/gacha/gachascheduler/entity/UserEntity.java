package com.gacha.gachascheduler.entity;

import com.gacha.gachascheduler.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "google_id", unique = true)
    private String googleId;

    // 아이디/비밀번호 회원가입 계정만 값이 있음 (구글 전용 계정은 null) — BCrypt 해시만 저장
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "user_code", unique = true, nullable = false)
    private String userCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
