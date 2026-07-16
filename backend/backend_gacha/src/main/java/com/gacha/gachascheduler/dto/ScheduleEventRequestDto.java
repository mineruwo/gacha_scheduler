package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.enums.ScheduleCategory;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ScheduleEventRequestDto {
    private String gameCode;
    private String title;
    private ScheduleCategory category;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String description;
}
