package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByGoogleId(String googleId);
    Optional<UserEntity> findByUserCode(String userCode);
    List<UserEntity> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String email, String name);
}
