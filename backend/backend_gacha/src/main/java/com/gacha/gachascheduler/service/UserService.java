package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.Role;
import com.gacha.gachascheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity findOrCreateUser(String email, String name, String profilePictureUrl, String googleId) {
        Optional<UserEntity> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            // Update user details if necessary
            user.setEmail(email);
            user.setName(name);
            user.setProfilePictureUrl(profilePictureUrl);
            user.setUpdatedAt(OffsetDateTime.now());
            return userRepository.save(user);
        } else {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setProfilePictureUrl(profilePictureUrl);
            newUser.setGoogleId(googleId);
            newUser.setUserCode(generateUserCode()); // Generate user code
            return userRepository.save(newUser);
        }
    }

    private String generateUserCode() {
        // For now, a simple UUID with a '01_' prefix. Can be extended to dynamic XX_UUID format later.
        return "01_" + UUID.randomUUID().toString();
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsDeleted(true);
            user.setDeletedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public List<UserEntity> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    @Transactional
    public UserEntity updateRole(Long userId, Role role) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public UserEntity updateProfile(Long userId, String name, String profilePictureUrl) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        user.setName(name);
        user.setProfilePictureUrl(profilePictureUrl);
        return userRepository.save(user);
    }
}
