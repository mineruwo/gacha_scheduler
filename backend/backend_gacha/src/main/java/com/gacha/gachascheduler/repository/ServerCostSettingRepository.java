package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.ServerCostSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerCostSettingRepository extends JpaRepository<ServerCostSettingEntity, Long> {
}
