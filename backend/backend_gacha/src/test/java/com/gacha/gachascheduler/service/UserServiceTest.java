package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(UserService.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void firstLoginCreatesNewUser() {
        UserEntity created = userService.findOrCreateUser(
                "test@example.com", "Tester", "https://example.com/avatar.png", "google-sub-123");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getUserCode()).isNotBlank();
        assertThat(userRepository.findByGoogleId("google-sub-123")).isPresent();
    }

    @Test
    void secondLoginWithSameGoogleIdReusesExistingUser() {
        UserEntity first = userService.findOrCreateUser(
                "repeat@example.com", "Repeat User", null, "google-sub-456");
        UserEntity second = userService.findOrCreateUser(
                "repeat@example.com", "Repeat User Updated", null, "google-sub-456");

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(userRepository.count()).isEqualTo(1);
        // 재로그인해도 구글 프로필 이름으로 덮어쓰지 않는다 (PUT /api/users/me 수정값 보존)
        assertThat(second.getName()).isEqualTo("Repeat User");
    }

    @Test
    void signupCreatesNewPasswordAccount() {
        UserEntity created = userService.signup("newpw@example.com", "password123", "New Password User");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPasswordHash()).isNotBlank();
        assertThat(created.getGoogleId()).isNull();
        assertThat(userService.authenticateWithPassword("newpw@example.com", "password123")).isPresent();
    }

    @Test
    void signupMergesPasswordIntoExistingGoogleOnlyAccount() {
        UserEntity googleUser = userService.findOrCreateUser(
                "merge@example.com", "Merge User", null, "google-sub-merge");

        UserEntity merged = userService.signup("merge@example.com", "password123", "Merge User");

        assertThat(merged.getId()).isEqualTo(googleUser.getId());
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userService.authenticateWithPassword("merge@example.com", "password123")).isPresent();
    }

    @Test
    void signupRejectsEmailAlreadyRegisteredWithPassword() {
        userService.signup("dup@example.com", "password123", "Dup User");

        assertThatThrownBy(() -> userService.signup("dup@example.com", "anotherPassword", "Dup User 2"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void authenticateWithPasswordRejectsWrongPassword() {
        userService.signup("wrongpw@example.com", "password123", "Wrong PW User");

        assertThat(userService.authenticateWithPassword("wrongpw@example.com", "incorrect")).isEmpty();
    }

    @Test
    void authenticateWithPasswordRejectsGoogleOnlyAccount() {
        userService.findOrCreateUser("googleonly@example.com", "Google Only User", null, "google-sub-only");

        assertThat(userService.authenticateWithPassword("googleonly@example.com", "anyPassword")).isEmpty();
    }
}
