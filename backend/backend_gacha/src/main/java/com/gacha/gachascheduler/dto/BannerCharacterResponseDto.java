package com.gacha.gachascheduler.dto;

import lombok.Data;

/** 배너 풀 안에서의 캐릭터 표현 (SYNC.md 계약의 Character 응답 형태: id/gameId/name/rarity/iconUrl/weight). */
@Data
public class BannerCharacterResponseDto {
    private Long id;
    private Long gameId;
    private String name;
    private Integer rarity;
    private String iconUrl;
    private Double weight;
}
