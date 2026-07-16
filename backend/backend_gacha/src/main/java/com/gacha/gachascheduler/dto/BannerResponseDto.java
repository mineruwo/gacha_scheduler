package com.gacha.gachascheduler.dto;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Data;

@Data
public class BannerResponseDto {
    private Long id;
    private Long gameId;
    private String gameName;
    private String name;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private List<Long> pickupCharacterIds;
    private Integer pityThreshold;
    private Double rateUpRate;
}
