package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.ScheduleEventMapper;
import com.gacha.gachascheduler.dto.ScheduleEventRequestDto;
import com.gacha.gachascheduler.dto.ScheduleEventResponseDto;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.ScheduleEventEntity;
import com.gacha.gachascheduler.service.GameService;
import com.gacha.gachascheduler.service.ScheduleEventService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 조회(GET /api/schedules)는 비로그인 사용자도 가능하고, 등록/수정/삭제(/api/admin/schedules)는 관리자 전용이다.
 *
 * ScheduleEventEntity는 game 연관관계를 지연 로딩으로 들고 있지 않으므로(참조 컬럼이 games의 PK가 아니라
 * game_code라 프록시 생성이 안 됨), 여기서 gameCode로 게임을 별도 조회해 매퍼에 명시적으로 넘긴다.
 */
@RestController
@RequiredArgsConstructor
public class ScheduleEventController {

    private final ScheduleEventService scheduleEventService;
    private final GameService gameService;

    @GetMapping("/api/schedules")
    public ResponseEntity<List<ScheduleEventResponseDto>> getSchedules(
            @RequestParam(required = false) List<String> gameCodes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        OffsetDateTime rangeFrom = from != null ? from : OffsetDateTime.now().minusMonths(1);
        OffsetDateTime rangeTo = to != null ? to : OffsetDateTime.now().plusMonths(3);

        List<ScheduleEventEntity> schedules = scheduleEventService.getSchedules(gameCodes, rangeFrom, rangeTo);
        Map<String, GameEntity> gamesByCode = loadGamesByCode(schedules);

        List<ScheduleEventResponseDto> response = schedules.stream()
                .map(schedule -> ScheduleEventMapper.toDto(schedule, gamesByCode.get(schedule.getGameCode())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
    @PostMapping("/api/admin/schedules")
    public ResponseEntity<ScheduleEventResponseDto> createSchedule(@RequestBody ScheduleEventRequestDto request) {
        ScheduleEventEntity created = scheduleEventService.createSchedule(request);
        GameEntity game = gameService.getGameByCode(created.getGameCode()).orElse(null);
        return new ResponseEntity<>(ScheduleEventMapper.toDto(created, game), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
    @PutMapping("/api/admin/schedules/{id}")
    public ResponseEntity<ScheduleEventResponseDto> updateSchedule(
            @PathVariable Long id, @RequestBody ScheduleEventRequestDto request) {
        ScheduleEventEntity updated = scheduleEventService.updateSchedule(id, request);
        GameEntity game = gameService.getGameByCode(updated.getGameCode()).orElse(null);
        return ResponseEntity.ok(ScheduleEventMapper.toDto(updated, game));
    }

    @PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
    @DeleteMapping("/api/admin/schedules/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleEventService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, GameEntity> loadGamesByCode(List<ScheduleEventEntity> schedules) {
        List<String> gameCodes = schedules.stream()
                .map(ScheduleEventEntity::getGameCode)
                .distinct()
                .toList();
        return gameService.getGamesByCode(gameCodes).stream()
                .collect(Collectors.toMap(GameEntity::getGameCode, Function.identity()));
    }
}
