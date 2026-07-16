package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.AnnouncementEntity;
import com.gacha.gachascheduler.enums.AnnouncementType;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

    @Query("SELECT a FROM AnnouncementEntity a WHERE (:type IS NULL OR a.type = :type) ORDER BY a.startAt DESC")
    List<AnnouncementEntity> findAllByOptionalType(@Param("type") AnnouncementType type);

    @Query("SELECT a FROM AnnouncementEntity a WHERE a.isActive = true "
            + "AND a.startAt <= :now AND (a.endAt IS NULL OR a.endAt >= :now) "
            + "AND (:type IS NULL OR a.type = :type) ORDER BY a.startAt DESC")
    List<AnnouncementEntity> findCurrentlyActive(@Param("type") AnnouncementType type, @Param("now") OffsetDateTime now);
}
