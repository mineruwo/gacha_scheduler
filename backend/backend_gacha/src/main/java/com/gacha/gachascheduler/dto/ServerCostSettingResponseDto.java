package com.gacha.gachascheduler.dto;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class ServerCostSettingResponseDto {
    private Long targetAmount;
    private Long currentAmount;
    /** 0~100 범위로 반올림한 달성률. targetAmount가 0이면 0. */
    private Integer percentage;
    private OffsetDateTime updatedAt;
}
