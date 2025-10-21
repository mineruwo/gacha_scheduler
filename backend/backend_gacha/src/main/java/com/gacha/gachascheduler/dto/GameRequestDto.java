package com.gacha.gachascheduler.dto;

import lombok.Data;

@Data
public class GameRequestDto {
    private String title;
    private String gameCode;
    private Boolean hasGacha;
    private Boolean hasPass;
    private Boolean canRecord;
    private Boolean isService;
    private String comment;
    private Boolean canManageSchedule;
    private Boolean canTrackCurrency;
}
