package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.ChannelEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {
    List<ChannelEntity> findByGameId(Long gameId);
}
