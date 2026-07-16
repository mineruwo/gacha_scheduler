package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.AnnouncementEntity;

public final class AnnouncementMapper {

    private AnnouncementMapper() {
    }

    public static AnnouncementResponseDto toDto(AnnouncementEntity entity) {
        AnnouncementResponseDto dto = new AnnouncementResponseDto();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setImageUrl(entity.getImageUrl());
        dto.setLinkUrl(entity.getLinkUrl());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
