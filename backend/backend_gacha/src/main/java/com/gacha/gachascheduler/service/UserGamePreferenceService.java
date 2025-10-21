package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.entity.UserGamePreferenceEntity;
import com.gacha.gachascheduler.entity.UserGamePreferenceId;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.UserGamePreferenceRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserGamePreferenceService {

    private final UserGamePreferenceRepository userGamePreferenceRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Transactional
    public UserGamePreferenceEntity addPreference(Long userId, String gameCode) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GameEntity game = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        UserGamePreferenceEntity preference = new UserGamePreferenceEntity();
        preference.setUserId(userId);
        preference.setGameCode(gameCode);
        preference.setUser(user);
        preference.setGame(game);

        return userGamePreferenceRepository.save(preference);
    }

    @Transactional
    public void removePreference(Long userId, String gameCode) {
        UserGamePreferenceId id = new UserGamePreferenceId(userId, gameCode);
        userGamePreferenceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserGamePreferenceEntity> getUserPreferences(Long userId) {
        return userGamePreferenceRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean hasPreference(Long userId, String gameCode) {
        UserGamePreferenceId id = new UserGamePreferenceId(userId, gameCode);
        return userGamePreferenceRepository.existsById(id);
    }
}
