package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gacha.gachascheduler.dto.ScheduleEventRequestDto;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.ScheduleEventEntity;
import com.gacha.gachascheduler.enums.ScheduleCategory;
import com.gacha.gachascheduler.repository.GameRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(ScheduleEventService.class)
class ScheduleEventServiceTest {

    @Autowired
    private ScheduleEventService scheduleEventService;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void createScheduleFailsWhenGameDoesNotExist() {
        ScheduleEventRequestDto request = requestFor("no-such-game", OffsetDateTime.now(), OffsetDateTime.now().plusDays(1));

        assertThatThrownBy(() -> scheduleEventService.createSchedule(request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void createAndQuerySchedulesWithinRange() {
        GameEntity game = persistGame("test-game");
        OffsetDateTime now = OffsetDateTime.now();

        scheduleEventService.createSchedule(requestFor(game.getGameCode(), now.plusDays(1), now.plusDays(3)));
        scheduleEventService.createSchedule(requestFor(game.getGameCode(), now.plusDays(30), now.plusDays(31)));

        List<ScheduleEventEntity> inRange = scheduleEventService.getSchedules(
                List.of(game.getGameCode()), now, now.plusDays(7));

        assertThat(inRange).hasSize(1);
        assertThat(inRange.get(0).getGameCode()).isEqualTo(game.getGameCode());
    }

    @Test
    void deletedScheduleNoLongerReturnedByQuery() {
        GameEntity game = persistGame("delete-me-game");
        OffsetDateTime now = OffsetDateTime.now();
        ScheduleEventEntity created = scheduleEventService.createSchedule(
                requestFor(game.getGameCode(), now, now.plusDays(1)));

        scheduleEventService.deleteSchedule(created.getId());

        List<ScheduleEventEntity> inRange = scheduleEventService.getSchedules(
                null, now.minusDays(1), now.plusDays(2));
        assertThat(inRange).isEmpty();
    }

    private GameEntity persistGame(String gameCode) {
        GameEntity game = new GameEntity();
        game.setTitle("Test Game");
        game.setGameCode(gameCode);
        return gameRepository.save(game);
    }

    private ScheduleEventRequestDto requestFor(String gameCode, OffsetDateTime start, OffsetDateTime end) {
        ScheduleEventRequestDto request = new ScheduleEventRequestDto();
        request.setGameCode(gameCode);
        request.setTitle("Test Schedule");
        request.setCategory(ScheduleCategory.EVENT);
        request.setStartAt(start);
        request.setEndAt(end);
        return request;
    }
}
