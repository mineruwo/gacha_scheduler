package com.gacha.gachascheduler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BannerCharacterId implements Serializable {
    private Long bannerId;
    private Long characterId;
}
