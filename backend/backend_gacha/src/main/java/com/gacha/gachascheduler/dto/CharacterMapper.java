package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.CharacterEntity;

public final class CharacterMapper {

    private CharacterMapper() {
    }

    public static CharacterResponseDto toDto(CharacterEntity entity) {
        CharacterResponseDto dto = new CharacterResponseDto();
        dto.setId(entity.getId());
        dto.setGameId(entity.getGameId());
        dto.setName(entity.getName());
        dto.setRarity(entity.getRarity());
        dto.setIconUrl(entity.getIconUrl());
        return dto;
    }
}
