package com.gacha.gachascheduler.dto;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class ChannelResponseDto {
    private Long id;
    private Long gameId;
    private String gameName;
    private String name;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
