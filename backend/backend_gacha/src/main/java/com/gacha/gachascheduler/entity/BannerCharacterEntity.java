package com.gacha.gachascheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "banner_characters")
@IdClass(BannerCharacterId.class)
@Data
public class BannerCharacterEntity {

    @Id
    @Column(name = "banner_id")
    private Long bannerId;

    @Id
    @Column(name = "character_id")
    private Long characterId;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "is_pickup", nullable = false)
    private Boolean isPickup = false;
}
