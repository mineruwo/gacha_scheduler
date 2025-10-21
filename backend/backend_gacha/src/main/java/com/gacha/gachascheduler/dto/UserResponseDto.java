package com.gacha.gachascheduler.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private String profilePictureUrl;
    private String userCode;
    private String role;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
