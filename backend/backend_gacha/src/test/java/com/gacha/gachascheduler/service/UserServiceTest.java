package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(second.getName()).isEqualTo("Repeat User Updated");
    }
}
