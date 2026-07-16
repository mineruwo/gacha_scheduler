package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.CharacterEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends JpaRepository<CharacterEntity, Long> {
    List<CharacterEntity> findByGameId(Long gameId);
}
