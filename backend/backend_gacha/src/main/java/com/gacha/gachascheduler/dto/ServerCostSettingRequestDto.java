package com.gacha.gachascheduler.dto;

import lombok.Data;

@Data
public class ServerCostSettingRequestDto {
    private Long targetAmount;
    private Long currentAmount;
}
