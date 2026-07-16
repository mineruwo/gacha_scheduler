package com.gacha.gachascheduler.dto;

import java.util.List;
import lombok.Data;

@Data
public class PullResponseDto {
    private List<BannerCharacterResponseDto> results;
    private Integer pityCount;
}
