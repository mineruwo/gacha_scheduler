package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.PendingScheduleEntity;
import com.gacha.gachascheduler.enums.PendingScheduleStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingScheduleRepository extends JpaRepository<PendingScheduleEntity, Long> {
    List<PendingScheduleEntity> findByStatusOrderByCreatedAtDesc(PendingScheduleStatus status);
}
