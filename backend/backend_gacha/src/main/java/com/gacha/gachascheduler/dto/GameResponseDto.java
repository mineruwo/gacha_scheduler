package com.gacha.gachascheduler.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class GameResponseDto {
    private Long id;
    private String title;
    private String gameCode;
    private Boolean hasGacha;
    private Boolean hasPass;
    private Boolean canRecord;
    private Boolean isService;
    private String comment;
    private Boolean canManageSchedule;
    private Boolean canTrackCurrency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
