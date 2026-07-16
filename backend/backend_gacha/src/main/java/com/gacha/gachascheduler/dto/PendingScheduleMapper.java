package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.PendingScheduleEntity;

public final class PendingScheduleMapper {

    private PendingScheduleMapper() {
    }

    public static PendingScheduleResponseDto toDto(PendingScheduleEntity entity, GameEntity game) {
        PendingScheduleResponseDto dto = new PendingScheduleResponseDto();
        dto.setId(entity.getId());
        dto.setGameCode(entity.getGameCode());
        dto.setGameTitle(game != null ? game.getTitle() : null);
        dto.setTitle(entity.getTitle());
        dto.setCategory(entity.getCategory());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setDescription(entity.getDescription());
        dto.setSourceNote(entity.getSourceNote());
        dto.setStatus(entity.getStatus());
        dto.setApprovedScheduleId(entity.getApprovedScheduleId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
