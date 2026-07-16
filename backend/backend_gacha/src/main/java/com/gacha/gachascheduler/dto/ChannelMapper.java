package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.entity.GameEntity;

/** ChannelEntity는 game 연관관계를 지연 로딩으로 들고 있지 않으므로 호출 측이 GameEntity를 명시적으로 전달한다. */
public final class ChannelMapper {

    private ChannelMapper() {
    }

    public static ChannelResponseDto toDto(ChannelEntity entity, GameEntity game) {
        ChannelResponseDto dto = new ChannelResponseDto();
        dto.setId(entity.getId());
        dto.setGameId(entity.getGameId());
        dto.setGameName(game != null ? game.getTitle() : null);
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
