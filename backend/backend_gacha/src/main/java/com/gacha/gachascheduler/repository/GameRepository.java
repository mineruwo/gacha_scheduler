package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {
    Optional<GameEntity> findByGameCode(String gameCode);
    List<GameEntity> findByGameCodeIn(List<String> gameCodes);
}
