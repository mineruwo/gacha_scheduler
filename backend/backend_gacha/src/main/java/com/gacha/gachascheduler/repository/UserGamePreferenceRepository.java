package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.UserGamePreferenceEntity;
import com.gacha.gachascheduler.entity.UserGamePreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGamePreferenceRepository extends JpaRepository<UserGamePreferenceEntity, UserGamePreferenceId> {
    List<UserGamePreferenceEntity> findByUserId(Long userId);
}
