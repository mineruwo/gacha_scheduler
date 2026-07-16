package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.entity.UserGamePreferenceEntity;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(UserGamePreferenceService.class)
class UserGamePreferenceServiceTest {

    @Autowired
    private UserGamePreferenceService userGamePreferenceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void replacePreferencesAddsAndRemovesToMatchTargetSet() {
        UserEntity user = persistUser();
        persistGame("game-a");
        persistGame("game-b");
        persistGame("game-c");

        userGamePreferenceService.replacePreferences(user.getId(), List.of("game-a", "game-b"));
        List<UserGamePreferenceEntity> firstPass = userGamePreferenceService.getUserPreferences(user.getId());
        assertThat(firstPass).extracting(UserGamePreferenceEntity::getGameCode)
                .containsExactlyInAnyOrder("game-a", "game-b");

        userGamePreferenceService.replacePreferences(user.getId(), List.of("game-b", "game-c"));
        List<UserGamePreferenceEntity> secondPass = userGamePreferenceService.getUserPreferences(user.getId());
        assertThat(secondPass).extracting(UserGamePreferenceEntity::getGameCode)
                .containsExactlyInAnyOrder("game-b", "game-c");
    }

    @Test
    void replacePreferencesWithEmptyListClearsAllPreferences() {
        UserEntity user = persistUser();
        persistGame("game-a");
        userGamePreferenceService.replacePreferences(user.getId(), List.of("game-a"));

        userGamePreferenceService.replacePreferences(user.getId(), List.of());

        assertThat(userGamePreferenceService.getUserPreferences(user.getId())).isEmpty();
    }

    private UserEntity persistUser() {
        UserEntity user = new UserEntity();
        user.setEmail("pref-test@example.com");
        user.setName("Pref Tester");
        user.setGoogleId("google-pref-tester");
        user.setUserCode("01_" + java.util.UUID.randomUUID());
        return userRepository.save(user);
    }

    private void persistGame(String gameCode) {
        GameEntity game = new GameEntity();
        game.setTitle(gameCode);
        game.setGameCode(gameCode);
        gameRepository.save(game);
    }
}
