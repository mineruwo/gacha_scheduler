package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserProfileResponseDto toProfileDto(UserEntity entity) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setProfilePictureUrl(entity.getProfilePictureUrl());
        dto.setUserCode(entity.getUserCode());
        dto.setRole(entity.getRole().name());
        dto.setIsDeleted(entity.getIsDeleted());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
