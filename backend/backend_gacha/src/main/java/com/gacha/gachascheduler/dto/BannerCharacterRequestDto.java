package com.gacha.gachascheduler.dto;

import lombok.Data;

/** 관리자가 배너 풀에 캐릭터를 추가/수정할 때 사용 (가중치, 픽업 여부 설정). */
@Data
public class BannerCharacterRequestDto {
    private Long characterId;
    private Double weight;
    private Boolean isPickup;
}
