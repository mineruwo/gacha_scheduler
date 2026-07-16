package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.enums.ScheduleCategory;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class PendingScheduleRequestDto {
    private String gameCode;
    private String title;
    private ScheduleCategory category;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String description;
    private String sourceNote;
}
