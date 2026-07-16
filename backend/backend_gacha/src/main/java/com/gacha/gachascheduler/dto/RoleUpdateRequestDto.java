package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.enums.Role;
import lombok.Data;

@Data
public class RoleUpdateRequestDto {
    private Role role;
}
