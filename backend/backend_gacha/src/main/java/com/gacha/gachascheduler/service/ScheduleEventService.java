package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.ScheduleEventRequestDto;
import com.gacha.gachascheduler.entity.ScheduleEventEntity;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.ScheduleEventRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleEventService {

    private final ScheduleEventRepository scheduleEventRepository;
    private final GameRepository gameRepository;

    @Transactional
    public ScheduleEventEntity createSchedule(ScheduleEventRequestDto request) {
        requireExistingGame(request.getGameCode());

        ScheduleEventEntity entity = new ScheduleEventEntity();
        applyRequest(entity, request);
        return scheduleEventRepository.save(entity);
    }

    @Transactional
    public ScheduleEventEntity updateSchedule(Long id, ScheduleEventRequestDto request) {
        ScheduleEventEntity entity = scheduleEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id " + id));
        requireExistingGame(request.getGameCode());

        applyRequest(entity, request);
        return scheduleEventRepository.save(entity);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        scheduleEventRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ScheduleEventEntity> getSchedules(List<String> gameCodes, OffsetDateTime from, OffsetDateTime to) {
        if (gameCodes == null || gameCodes.isEmpty()) {
            return scheduleEventRepository.findAllInRange(from, to);
        }
        return scheduleEventRepository.findByGameCodesAndRange(gameCodes, from, to);
    }

    private void requireExistingGame(String gameCode) {
        gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new RuntimeException("Game not found with gameCode " + gameCode));
    }

    private void applyRequest(ScheduleEventEntity entity, ScheduleEventRequestDto request) {
        entity.setGameCode(request.getGameCode());
        entity.setTitle(request.getTitle());
        entity.setCategory(request.getCategory());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setDescription(request.getDescription());
    }
}
