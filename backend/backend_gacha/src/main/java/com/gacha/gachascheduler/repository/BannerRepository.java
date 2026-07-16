package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.BannerEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<BannerEntity, Long> {
    List<BannerEntity> findByGameId(Long gameId);
}
