package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gacha.gachascheduler.dto.PendingScheduleRequestDto;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.PendingScheduleEntity;
import com.gacha.gachascheduler.entity.ScheduleEventEntity;
import com.gacha.gachascheduler.enums.PendingScheduleStatus;
import com.gacha.gachascheduler.enums.ScheduleCategory;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.ScheduleEventRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({PendingScheduleService.class, ScheduleEventService.class})
class PendingScheduleServiceTest {

    @Autowired
    private PendingScheduleService pendingScheduleService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ScheduleEventRepository scheduleEventRepository;

    @Test
    void createPendingFailsWhenGameDoesNotExist() {
        PendingScheduleRequestDto request = requestFor("no-such-game");

        assertThatThrownBy(() -> pendingScheduleService.createPending(request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void newPendingScheduleDefaultsToPendingStatusAndIsListedByStatus() {
        persistGame("pending-game");

        PendingScheduleEntity created = pendingScheduleService.createPending(requestFor("pending-game"));

        assertThat(created.getStatus()).isEqualTo(PendingScheduleStatus.PENDING);
        assertThat(pendingScheduleService.getByStatus(PendingScheduleStatus.PENDING))
                .extracting(PendingScheduleEntity::getId)
                .contains(created.getId());
    }

    @Test
    void approvingCreatesRealScheduleEventAndMarksApproved() {
        persistGame("approve-game");
        PendingScheduleEntity created = pendingScheduleService.createPending(requestFor("approve-game"));

        PendingScheduleEntity approved = pendingScheduleService.approve(created.getId());

        assertThat(approved.getStatus()).isEqualTo(PendingScheduleStatus.APPROVED);
        assertThat(approved.getApprovedScheduleId()).isNotNull();
        ScheduleEventEntity createdEvent = scheduleEventRepository.findById(approved.getApprovedScheduleId())
                .orElseThrow();
        assertThat(createdEvent.getGameCode()).isEqualTo("approve-game");
        assertThat(createdEvent.getTitle()).isEqualTo("Test Pending Schedule");
    }

    @Test
    void rejectingMarksRejectedWithoutCreatingScheduleEvent() {
        persistGame("reject-game");
        PendingScheduleEntity created = pendingScheduleService.createPending(requestFor("reject-game"));

        PendingScheduleEntity rejected = pendingScheduleService.reject(created.getId());

        assertThat(rejected.getStatus()).isEqualTo(PendingScheduleStatus.REJECTED);
        assertThat(rejected.getApprovedScheduleId()).isNull();
        assertThat(scheduleEventRepository.findAll()).isEmpty();
    }

    @Test
    void approvingAlreadyResolvedEntryThrows() {
        persistGame("double-approve-game");
        PendingScheduleEntity created = pendingScheduleService.createPending(requestFor("double-approve-game"));
        pendingScheduleService.approve(created.getId());

        assertThatThrownBy(() -> pendingScheduleService.approve(created.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void persistGame(String gameCode) {
        GameEntity game = new GameEntity();
        game.setTitle(gameCode);
        game.setGameCode(gameCode);
        gameRepository.save(game);
    }

    private PendingScheduleRequestDto requestFor(String gameCode) {
        PendingScheduleRequestDto request = new PendingScheduleRequestDto();
        request.setGameCode(gameCode);
        request.setTitle("Test Pending Schedule");
        request.setCategory(ScheduleCategory.MAINTENANCE);
        request.setStartAt(OffsetDateTime.now().plusDays(1));
        request.setEndAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        request.setSourceNote("unit-test");
        return request;
    }
}
