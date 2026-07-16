package com.gacha.gachascheduler.dto;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
}
