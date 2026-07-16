package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.Role;
import com.gacha.gachascheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    // 이미 초기화돼 있어 Lombok @RequiredArgsConstructor 생성자 파라미터에서 제외됨(다른 곳에 별도 빈 필요 없음)
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserEntity findOrCreateUser(String email, String name, String profilePictureUrl, String googleId) {
        Optional<UserEntity> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            // email만 구글 값으로 동기화한다. name/profilePictureUrl은 유저가
            // PUT /api/users/me 로 수정할 수 있는 값이라 재로그인 때 덮어쓰면 원복된다.
            user.setEmail(email);
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

    @Transactional(readOnly = true)
    public Optional<UserEntity> findUserByUserCode(String userCode) {
        return userRepository.findByUserCode(userCode);
    }

    /** userCode는 iCal 구독 URL의 비밀값으로도 쓰인다. 유출 시 이 메서드로 재발급해 기존 구독 URL을 무효화한다. */
    @Transactional
    public UserEntity resetUserCode(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        user.setUserCode(generateUserCode());
        return userRepository.save(user);
    }

    /**
     * 이메일이 이미 구글 계정으로 가입돼 있으면 비밀번호만 추가해서 같은 계정으로 통합하고,
     * 이미 비밀번호가 설정된 계정이면 거부한다(중복 가입).
     */
    @Transactional
    public UserEntity signup(String email, String rawPassword, String name) {
        Optional<UserEntity> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            UserEntity user = existing.get();
            if (user.getPasswordHash() != null) {
                throw new IllegalStateException("Email already registered: " + email);
            }
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            return userRepository.save(user);
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        newUser.setUserCode(generateUserCode());
        return userRepository.save(newUser);
    }

    /** 구글 전용 계정(passwordHash 없음)이거나 비밀번호가 틀리면 empty를 반환한다. */
    @Transactional(readOnly = true)
    public Optional<UserEntity> authenticateWithPassword(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPasswordHash() != null)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }
}
