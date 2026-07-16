package com.gacha.gachascheduler.dto;

import java.util.List;
import lombok.Data;

@Data
public class UpdateGamePreferencesRequestDto {
    private List<String> gameCodes;
}
