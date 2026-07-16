package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.ScheduleEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ScheduleEventRepository extends JpaRepository<ScheduleEventEntity, Long> {

    @Query("SELECT s FROM ScheduleEventEntity s "
            + "WHERE s.gameCode IN :gameCodes "
            + "AND s.startAt <= :to AND (s.endAt IS NULL OR s.endAt >= :from) "
            + "ORDER BY s.startAt")
    List<ScheduleEventEntity> findByGameCodesAndRange(
            @Param("gameCodes") List<String> gameCodes,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("SELECT s FROM ScheduleEventEntity s "
            + "WHERE s.startAt <= :to AND (s.endAt IS NULL OR s.endAt >= :from) "
            + "ORDER BY s.startAt")
    List<ScheduleEventEntity> findAllInRange(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}
