package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.BannerCharacterEntity;
import com.gacha.gachascheduler.entity.BannerCharacterId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerCharacterRepository extends JpaRepository<BannerCharacterEntity, BannerCharacterId> {
    List<BannerCharacterEntity> findByBannerId(Long bannerId);
}
