package com.gacha.gachascheduler.dto;

import com.gacha.gachascheduler.entity.BannerCharacterEntity;
import com.gacha.gachascheduler.entity.BannerEntity;
import com.gacha.gachascheduler.entity.CharacterEntity;
import com.gacha.gachascheduler.entity.GameEntity;
import java.util.List;

/**
 * BannerEntity/CharacterEntity는 game 연관관계를 지연 로딩으로 들고 있지 않으므로(참조 컬럼이 games의
 * PK가 아니거나, 복합키 엔티티라 프록시 생성이 안 됨), 호출 측에서 GameRepository/CharacterRepository로
 * 미리 조회한 엔티티를 명시적으로 전달해야 한다.
 */
public final class BannerMapper {

    private BannerMapper() {
    }

    public static BannerResponseDto toDto(BannerEntity entity, GameEntity game, List<BannerCharacterEntity> pool) {
        BannerResponseDto dto = new BannerResponseDto();
        dto.setId(entity.getId());
        dto.setGameId(entity.getGameId());
        dto.setGameName(game != null ? game.getTitle() : null);
        dto.setName(entity.getName());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setPityThreshold(entity.getPityThreshold());
        dto.setRateUpRate(entity.getRateUpRate());
        dto.setPickupCharacterIds(pool.stream()
                .filter(BannerCharacterEntity::getIsPickup)
                .map(BannerCharacterEntity::getCharacterId)
                .toList());
        return dto;
    }

    public static BannerCharacterResponseDto toCharacterDto(BannerCharacterEntity entity, CharacterEntity character) {
        BannerCharacterResponseDto dto = new BannerCharacterResponseDto();
        dto.setId(character.getId());
        dto.setGameId(character.getGameId());
        dto.setName(character.getName());
        dto.setRarity(character.getRarity());
        dto.setIconUrl(character.getIconUrl());
        dto.setWeight(entity.getWeight());
        return dto;
    }
}
