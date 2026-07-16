package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.GamePreferenceDto;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.UserGamePreferenceEntity;
import com.gacha.gachascheduler.entity.UserGamePreferenceId;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.UserGamePreferenceRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGamePreferenceService {

    private final UserGamePreferenceRepository userGamePreferenceRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Transactional
    public UserGamePreferenceEntity addPreference(Long userId, String gameCode) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        UserGamePreferenceEntity preference = new UserGamePreferenceEntity();
        preference.setUserId(userId);
        preference.setGameCode(gameCode);

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

    /**
     * 유저의 관심 게임 목록을 주어진 gameCode 집합으로 완전히 교체한다.
     * (기존 필터에는 없던 게임을 추가하고, 새 목록에 없는 기존 필터는 제거)
     */
    @Transactional
    public List<UserGamePreferenceEntity> replacePreferences(Long userId, List<String> gameCodes) {
        List<UserGamePreferenceEntity> existing = userGamePreferenceRepository.findByUserId(userId);
        Set<String> existingCodes = existing.stream()
                .map(UserGamePreferenceEntity::getGameCode)
                .collect(Collectors.toSet());
        Set<String> targetCodes = new HashSet<>(gameCodes);

        existing.stream()
                .filter(preference -> !targetCodes.contains(preference.getGameCode()))
                .forEach(preference -> userGamePreferenceRepository.deleteById(
                        new UserGamePreferenceId(userId, preference.getGameCode())));

        targetCodes.stream()
                .filter(code -> !existingCodes.contains(code))
                .forEach(code -> addPreference(userId, code));

        return userGamePreferenceRepository.findByUserId(userId);
    }

    /**
     * UserGamePreferenceEntity는 game 연관관계를 지연 로딩으로 들고 있지 않으므로(참조 컬럼이 games의 PK가
     * 아니라 game_code인 복합키 엔티티라 프록시 생성이 안 됨), gameCode로 게임을 배치 조회해 gameTitle을 채운다.
     */
    @Transactional(readOnly = true)
    public List<GamePreferenceDto> getUserPreferenceDtos(Long userId) {
        List<UserGamePreferenceEntity> preferences = getUserPreferences(userId);
        Map<String, GameEntity> gamesByCode = gameRepository.findByGameCodeIn(
                        preferences.stream().map(UserGamePreferenceEntity::getGameCode).distinct().toList())
                .stream()
                .collect(Collectors.toMap(GameEntity::getGameCode, Function.identity()));

        return preferences.stream().map(preference -> {
            GamePreferenceDto dto = new GamePreferenceDto();
            dto.setGameCode(preference.getGameCode());
            GameEntity game = gamesByCode.get(preference.getGameCode());
            dto.setGameTitle(game != null ? game.getTitle() : null);
            return dto;
        }).toList();
    }
}
