package com.gacha.gachascheduler.dto;

import lombok.Data;

@Data
public class CharacterRequestDto {
    private Long gameId;
    private String name;
    private Integer rarity;
    private String iconUrl;
}
