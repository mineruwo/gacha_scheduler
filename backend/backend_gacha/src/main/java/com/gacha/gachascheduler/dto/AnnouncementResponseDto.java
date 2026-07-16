package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.enums.AnnouncementType;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class AnnouncementResponseDto {
    private Long id;
    private AnnouncementType type;
    private String title;
    private String content;
    private String imageUrl;
    private String linkUrl;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
