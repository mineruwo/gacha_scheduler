package com.gacha.gachascheduler.dto;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class BannerRequestDto {
    private Long gameId;
    private String name;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private Integer pityThreshold;
    private Double rateUpRate;
}
