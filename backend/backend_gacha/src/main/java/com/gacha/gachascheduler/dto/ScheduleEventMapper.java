package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.ScheduleEventEntity;

public final class ScheduleEventMapper {

    private ScheduleEventMapper() {
    }

    /**
     * game은 ScheduleEventEntity가 지연 로딩 연관관계로 들고 있지 않으므로(참조 컬럼이 PK가 아니라
     * lazy proxy를 만들 수 없음) 호출 측에서 gameCode로 미리 조회해 명시적으로 전달해야 한다.
     */
    public static ScheduleEventResponseDto toDto(ScheduleEventEntity entity, GameEntity game) {
        ScheduleEventResponseDto dto = new ScheduleEventResponseDto();
        dto.setId(entity.getId());
        dto.setGameCode(entity.getGameCode());
        dto.setGameTitle(game != null ? game.getTitle() : null);
        dto.setTitle(entity.getTitle());
        dto.setCategory(entity.getCategory());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
