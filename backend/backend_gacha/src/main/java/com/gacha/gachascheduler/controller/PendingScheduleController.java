package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.PendingScheduleMapper;
import com.gacha.gachascheduler.dto.PendingScheduleRequestDto;
import com.gacha.gachascheduler.dto.PendingScheduleResponseDto;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.PendingScheduleEntity;
import com.gacha.gachascheduler.enums.PendingScheduleStatus;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.service.PendingScheduleService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 파트 07 — AI 스케줄 파이프라인의 어드민 승인 대시보드용 API. 실제 자동 스크래핑/LLM 파싱은 아직 없고,
 * 지금은 관리자(또는 향후 파이프라인)가 후보를 등록하면 검토 후 승인/반려하는 흐름만 제공한다.
 */
@RestController
@RequestMapping("/api/admin/pending-schedules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
public class PendingScheduleController {

    private final PendingScheduleService pendingScheduleService;
    private final GameRepository gameRepository;

    @GetMapping
    public ResponseEntity<List<PendingScheduleResponseDto>> getPendingSchedules(
            @RequestParam(required = false) PendingScheduleStatus status) {
        List<PendingScheduleEntity> pendingSchedules = pendingScheduleService.getByStatus(status);

        Map<String, GameEntity> gamesByCode = gameRepository
                .findByGameCodeIn(pendingSchedules.stream()
                        .map(PendingScheduleEntity::getGameCode)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(GameEntity::getGameCode, Function.identity()));

        List<PendingScheduleResponseDto> response = pendingSchedules.stream()
                .map(p -> PendingScheduleMapper.toDto(p, gamesByCode.get(p.getGameCode())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PendingScheduleResponseDto> createPendingSchedule(
            @RequestBody PendingScheduleRequestDto request) {
        PendingScheduleEntity created = pendingScheduleService.createPending(request);
        GameEntity game = gameRepository.findByGameCode(created.getGameCode()).orElse(null);
        return new ResponseEntity<>(PendingScheduleMapper.toDto(created, game), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<PendingScheduleResponseDto> approve(@PathVariable Long id) {
        PendingScheduleEntity approved = pendingScheduleService.approve(id);
        GameEntity game = gameRepository.findByGameCode(approved.getGameCode()).orElse(null);
        return ResponseEntity.ok(PendingScheduleMapper.toDto(approved, game));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PendingScheduleResponseDto> reject(@PathVariable Long id) {
        PendingScheduleEntity rejected = pendingScheduleService.reject(id);
        GameEntity game = gameRepository.findByGameCode(rejected.getGameCode()).orElse(null);
        return ResponseEntity.ok(PendingScheduleMapper.toDto(rejected, game));
    }
}
