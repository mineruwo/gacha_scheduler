package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.ServerCostSettingMapper;
import com.gacha.gachascheduler.dto.ServerCostSettingResponseDto;
import com.gacha.gachascheduler.service.ServerCostSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 파트 08 — 메인/사이드바 서버비 달성률 게이지용 공개 조회. 등록/수정은 {@link AdminSettingsController}. */
@RestController
@RequiredArgsConstructor
public class PublicSettingsController {

    private final ServerCostSettingService serverCostSettingService;

    @GetMapping("/api/settings/server-cost")
    public ResponseEntity<ServerCostSettingResponseDto> getServerCostSettings() {
        return ResponseEntity.ok(ServerCostSettingMapper.toDto(serverCostSettingService.getSettings()));
    }
}
