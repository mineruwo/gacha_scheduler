package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.BannerCharacterRequestDto;
import com.gacha.gachascheduler.dto.BannerCharacterResponseDto;
import com.gacha.gachascheduler.dto.BannerRequestDto;
import com.gacha.gachascheduler.dto.BannerResponseDto;
import com.gacha.gachascheduler.entity.BannerEntity;
import com.gacha.gachascheduler.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
public class BannerController {

    private final BannerService bannerService;

    @PostMapping
    public ResponseEntity<BannerResponseDto> createBanner(@RequestBody BannerRequestDto request) {
        BannerEntity entity = new BannerEntity();
        entity.setGameId(request.getGameId());
        entity.setName(request.getName());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setPityThreshold(request.getPityThreshold());
        entity.setRateUpRate(request.getRateUpRate());

        BannerResponseDto created = bannerService.createBanner(entity);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BannerResponseDto> updateBanner(
            @PathVariable Long id, @RequestBody BannerRequestDto request) {
        BannerEntity entity = new BannerEntity();
        entity.setName(request.getName());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setPityThreshold(request.getPityThreshold());
        entity.setRateUpRate(request.getRateUpRate());

        return ResponseEntity.ok(bannerService.updateBanner(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{bannerId}/characters")
    public ResponseEntity<BannerCharacterResponseDto> setPoolCharacter(
            @PathVariable Long bannerId, @RequestBody BannerCharacterRequestDto request) {
        BannerCharacterResponseDto entity = bannerService.upsertPoolCharacter(
                bannerId, request.getCharacterId(), request.getWeight(), Boolean.TRUE.equals(request.getIsPickup()));
        return ResponseEntity.ok(entity);
    }

    @DeleteMapping("/{bannerId}/characters/{characterId}")
    public ResponseEntity<Void> removePoolCharacter(
            @PathVariable Long bannerId, @PathVariable Long characterId) {
        bannerService.removePoolCharacter(bannerId, characterId);
        return ResponseEntity.noContent().build();
    }
}
