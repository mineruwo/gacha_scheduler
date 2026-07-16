package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.Role;
import com.gacha.gachascheduler.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(UserService.class)
class UserManagementServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void updateRoleChangesRole() {
        Long userId = createUser("role-target@example.com", "Role Target");

        UserEntity updated = userService.updateRole(userId, Role.SUB_ADMIN);

        assertThat(updated.getRole()).isEqualTo(Role.SUB_ADMIN);
        assertThat(userRepository.findById(userId).orElseThrow().getRole()).isEqualTo(Role.SUB_ADMIN);
    }

    @Test
    void updateProfileChangesNameAndPicture() {
        Long userId = createUser("profile-target@example.com", "Old Name");

        UserEntity updated = userService.updateProfile(userId, "New Name", "https://example.com/avatar.png");

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getProfilePictureUrl()).isEqualTo("https://example.com/avatar.png");
    }

    @Test
    void searchUsersFindsByPartialEmailOrName() {
        createUser("uniqueaddr@example.com", "Someone Else");
        createUser("other@example.com", "DistinctNickname");
        createUser("unrelated@example.com", "Unrelated");

        List<UserEntity> byEmail = userService.searchUsers("uniqueaddr");
        List<UserEntity> byName = userService.searchUsers("DistinctNickname");

        assertThat(byEmail).hasSize(1);
        assertThat(byName).hasSize(1);
    }

    @Test
    void searchUsersWithBlankQueryReturnsAll() {
        createUser("a@example.com", "A");
        createUser("b@example.com", "B");

        assertThat(userService.searchUsers(null)).hasSize(2);
        assertThat(userService.searchUsers("  ")).hasSize(2);
    }

    private Long createUser(String email, String name) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setName(name);
        user.setGoogleId(UUID.randomUUID().toString());
        user.setUserCode("01_" + UUID.randomUUID());
        return userRepository.save(user).getId();
    }
}
