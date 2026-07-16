package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gacha.gachascheduler.dto.ScheduleEventRequestDto;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.ScheduleCategory;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({IcsCalendarService.class, UserService.class, UserGamePreferenceService.class, ScheduleEventService.class})
class IcsCalendarServiceTest {

    @Autowired
    private IcsCalendarService icsCalendarService;

    @Autowired
    private UserGamePreferenceService userGamePreferenceService;

    @Autowired
    private ScheduleEventService scheduleEventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void unknownUserCodeThrows() {
        assertThatThrownBy(() -> icsCalendarService.generateIcsForUserCode("no-such-code"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void generatesValidCalendarWrapperWithNoEvents() {
        UserEntity user = persistUser("empty-code");

        String ics = icsCalendarService.generateIcsForUserCode(user.getUserCode());

        assertThat(ics).startsWith("BEGIN:VCALENDAR\r\n");
        assertThat(ics).contains("VERSION:2.0\r\n");
        assertThat(ics).endsWith("END:VCALENDAR\r\n");
        assertThat(ics).doesNotContain("BEGIN:VEVENT");
    }

    @Test
    void filtersEventsByUserGamePreferenceAndIncludesGameTitleInSummary() {
        UserEntity user = persistUser("filtered-code");
        GameEntity followedGame = persistGame("followed-game", "Followed Game");
        GameEntity otherGame = persistGame("other-game", "Other Game");
        userGamePreferenceService.addPreference(user.getId(), followedGame.getGameCode());

        OffsetDateTime now = OffsetDateTime.now();
        scheduleEventService.createSchedule(
                scheduleRequest(followedGame.getGameCode(), "Followed Update", now.plusDays(1), now.plusDays(2)));
        scheduleEventService.createSchedule(
                scheduleRequest(otherGame.getGameCode(), "Other Update", now.plusDays(1), now.plusDays(2)));

        String ics = icsCalendarService.generateIcsForUserCode(user.getUserCode());

        assertThat(ics).contains("SUMMARY:[Followed Game] Followed Update");
        assertThat(ics).doesNotContain("Other Update");
    }

    @Test
    void noPreferencesMeansAllGamesIncluded() {
        UserEntity user = persistUser("all-games-code");
        GameEntity game = persistGame("any-game", "Any Game");
        OffsetDateTime now = OffsetDateTime.now();
        scheduleEventService.createSchedule(
                scheduleRequest(game.getGameCode(), "Any Update", now.plusDays(1), now.plusDays(2)));

        String ics = icsCalendarService.generateIcsForUserCode(user.getUserCode());

        assertThat(ics).contains("SUMMARY:[Any Game] Any Update");
    }

    private UserEntity persistUser(String userCode) {
        UserEntity user = new UserEntity();
        user.setEmail(userCode + "@example.com");
        user.setName("Test User");
        user.setGoogleId("google-" + UUID.randomUUID());
        user.setUserCode(userCode);
        return userRepository.save(user);
    }

    private GameEntity persistGame(String gameCode, String title) {
        GameEntity game = new GameEntity();
        game.setTitle(title);
        game.setGameCode(gameCode);
        return gameRepository.save(game);
    }

    private ScheduleEventRequestDto scheduleRequest(
            String gameCode, String title, OffsetDateTime start, OffsetDateTime end) {
        ScheduleEventRequestDto request = new ScheduleEventRequestDto();
        request.setGameCode(gameCode);
        request.setTitle(title);
        request.setCategory(ScheduleCategory.EVENT);
        request.setStartAt(start);
        request.setEndAt(end);
        return request;
    }
}
