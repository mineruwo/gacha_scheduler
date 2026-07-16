package com.gacha.gachascheduler.dto;

import lombok.Data;

@Data
public class ChannelRequestDto {
    private Long gameId;
    private String name;
    private String description;
}
