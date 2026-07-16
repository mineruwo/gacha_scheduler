package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.ServerCostSettingMapper;
import com.gacha.gachascheduler.dto.ServerCostSettingRequestDto;
import com.gacha.gachascheduler.dto.ServerCostSettingResponseDto;
import com.gacha.gachascheduler.service.ServerCostSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 파트 08 — 관리자가 이번 달 서버 유지비 목표액/달성액을 수동으로 입력하는 설정 화면용 API. */
@RestController
@RequestMapping("/api/admin/settings/server-cost")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
public class AdminSettingsController {

    private final ServerCostSettingService serverCostSettingService;

    @GetMapping
    public ResponseEntity<ServerCostSettingResponseDto> getServerCostSettings() {
        return ResponseEntity.ok(ServerCostSettingMapper.toDto(serverCostSettingService.getSettings()));
    }

    @PutMapping
    public ResponseEntity<ServerCostSettingResponseDto> updateServerCostSettings(
            @RequestBody ServerCostSettingRequestDto request) {
        return ResponseEntity.ok(ServerCostSettingMapper.toDto(
                serverCostSettingService.updateSettings(request.getTargetAmount(), request.getCurrentAmount())));
    }
}
