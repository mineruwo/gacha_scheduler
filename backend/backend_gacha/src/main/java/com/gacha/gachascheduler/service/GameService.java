package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    @Transactional
    public GameEntity createGame(GameEntity game) {
        return gameRepository.save(game);
    }

    @Transactional(readOnly = true)
    public List<GameEntity> getAllGames() {
        return gameRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<GameEntity> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    @Transactional
    public GameEntity updateGame(Long id, GameEntity updatedGame) {
        return gameRepository.findById(id).map(game -> {
            game.setTitle(updatedGame.getTitle());
            game.setGameCode(updatedGame.getGameCode());
            game.setHasGacha(updatedGame.getHasGacha());
            game.setHasPass(updatedGame.getHasPass());
            game.setCanRecord(updatedGame.getCanRecord());
            game.setIsService(updatedGame.getIsService());
            game.setComment(updatedGame.getComment());
            game.setCanManageSchedule(updatedGame.getCanManageSchedule());
            game.setCanTrackCurrency(updatedGame.getCanTrackCurrency());
            return gameRepository.save(game);
        }).orElseThrow(() -> new RuntimeException("Game not found with id " + id));
    }

    @Transactional
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }
}
