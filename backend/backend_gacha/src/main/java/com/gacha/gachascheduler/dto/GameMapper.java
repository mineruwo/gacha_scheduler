package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.GameEntity;

public final class GameMapper {

    private GameMapper() {
    }

    public static GameResponseDto toDto(GameEntity entity) {
        GameResponseDto dto = new GameResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setGameCode(entity.getGameCode());
        dto.setHasGacha(entity.getHasGacha());
        dto.setHasPass(entity.getHasPass());
        dto.setCanRecord(entity.getCanRecord());
        dto.setIsService(entity.getIsService());
        dto.setComment(entity.getComment());
        dto.setCanManageSchedule(entity.getCanManageSchedule());
        dto.setCanTrackCurrency(entity.getCanTrackCurrency());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
