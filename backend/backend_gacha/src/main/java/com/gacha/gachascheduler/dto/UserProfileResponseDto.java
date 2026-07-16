package com.gacha.gachascheduler.dto;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class UserProfileResponseDto {
    private Long id;
    private String email;
    private String name;
    private String profilePictureUrl;
    private String userCode;
    private String role;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
