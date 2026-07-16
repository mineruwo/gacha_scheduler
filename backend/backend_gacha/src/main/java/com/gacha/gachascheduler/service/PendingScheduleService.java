package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.PendingScheduleRequestDto;
import com.gacha.gachascheduler.dto.ScheduleEventRequestDto;
import com.gacha.gachascheduler.entity.PendingScheduleEntity;
import com.gacha.gachascheduler.entity.ScheduleEventEntity;
import com.gacha.gachascheduler.enums.PendingScheduleStatus;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.PendingScheduleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파트 07 — AI 스케줄 파이프라인의 "관리자 검토 및 승인" 단계만 구현한다.
 * 실제 웹 스크래퍼/LLM 파서는 외부 API 자격증명(및 각 게임사 약관 검토)이 필요해 이번 범위 밖 —
 * docs/plans/07-extended-features-planning.md 4번 항목 설계 메모 참고.
 * 지금은 관리자가 직접(또는 향후 파이프라인이 대신) 후보를 등록하면, 검토 후 승인/반려하는 흐름만 제공한다.
 */
@Service
@RequiredArgsConstructor
public class PendingScheduleService {

    private final PendingScheduleRepository pendingScheduleRepository;
    private final GameRepository gameRepository;
    private final ScheduleEventService scheduleEventService;

    @Transactional
    public PendingScheduleEntity createPending(PendingScheduleRequestDto request) {
        requireExistingGame(request.getGameCode());

        PendingScheduleEntity entity = new PendingScheduleEntity();
        entity.setGameCode(request.getGameCode());
        entity.setTitle(request.getTitle());
        entity.setCategory(request.getCategory());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setDescription(request.getDescription());
        entity.setSourceNote(request.getSourceNote());
        entity.setStatus(PendingScheduleStatus.PENDING);
        return pendingScheduleRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<PendingScheduleEntity> getByStatus(PendingScheduleStatus status) {
        PendingScheduleStatus target = status != null ? status : PendingScheduleStatus.PENDING;
        return pendingScheduleRepository.findByStatusOrderByCreatedAtDesc(target);
    }

    /** 승인: 실제 ScheduleEvent를 생성하고, 대기 항목은 감사 추적을 위해 삭제하지 않고 APPROVED로 표시한다. */
    @Transactional
    public PendingScheduleEntity approve(Long id) {
        PendingScheduleEntity pending = requirePending(id);

        ScheduleEventRequestDto request = new ScheduleEventRequestDto();
        request.setGameCode(pending.getGameCode());
        request.setTitle(pending.getTitle());
        request.setCategory(pending.getCategory());
        request.setStartAt(pending.getStartAt());
        request.setEndAt(pending.getEndAt());
        request.setDescription(pending.getDescription());
        ScheduleEventEntity created = scheduleEventService.createSchedule(request);

        pending.setStatus(PendingScheduleStatus.APPROVED);
        pending.setApprovedScheduleId(created.getId());
        return pendingScheduleRepository.save(pending);
    }

    @Transactional
    public PendingScheduleEntity reject(Long id) {
        PendingScheduleEntity pending = requirePending(id);
        pending.setStatus(PendingScheduleStatus.REJECTED);
        return pendingScheduleRepository.save(pending);
    }

    private PendingScheduleEntity requirePending(Long id) {
        PendingScheduleEntity pending = pendingScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pending schedule not found with id " + id));
        if (pending.getStatus() != PendingScheduleStatus.PENDING) {
            throw new IllegalStateException("Pending schedule " + id + " already resolved: " + pending.getStatus());
        }
        return pending;
    }

    private void requireExistingGame(String gameCode) {
        gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new RuntimeException("Game not found with gameCode " + gameCode));
    }
}
