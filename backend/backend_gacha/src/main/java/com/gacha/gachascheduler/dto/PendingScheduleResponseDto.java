package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.enums.PendingScheduleStatus;
import com.gacha.gachascheduler.enums.ScheduleCategory;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class PendingScheduleResponseDto {
    private Long id;
    private String gameCode;
    private String gameTitle;
    private String title;
    private ScheduleCategory category;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String description;
    private String sourceNote;
    private PendingScheduleStatus status;
    private Long approvedScheduleId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
