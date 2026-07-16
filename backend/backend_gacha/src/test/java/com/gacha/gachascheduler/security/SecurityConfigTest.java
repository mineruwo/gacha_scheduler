package com.gacha.gachascheduler.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gacha.gachascheduler.dto.BannerResponseDto;
import com.gacha.gachascheduler.entity.BannerEntity;
import com.gacha.gachascheduler.entity.CharacterEntity;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.Role;
import com.gacha.gachascheduler.repository.CharacterRepository;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import com.gacha.gachascheduler.service.BannerService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void actuatorHealthEndpointIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void publicEndpointIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/hello")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/some-protected-resource")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminGamesEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/games")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminGamesEndpointRejectsUserRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.USER);

        mockMvc.perform(get("/api/admin/games").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminGamesEndpointAllowsSubAdminRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.SUB_ADMIN);

        mockMvc.perform(get("/api/admin/games").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void adminGamesEndpointAllowsMainAdminRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.MAIN_ADMIN);

        mockMvc.perform(get("/api/admin/games").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void publicGamesListIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/games")).andExpect(status().isOk());
    }

    @Test
    void publicSchedulesListIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/schedules")).andExpect(status().isOk());
    }

    @Test
    void adminSchedulesEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/schedules/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void gamePreferencesEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me/game-preferences")).andExpect(status().isUnauthorized());
    }

    @Test
    void gamePreferencesEndpointAllowsAuthenticatedUser() throws Exception {
        String token = jwtProvider.createToken(1L, Role.USER);

        mockMvc.perform(get("/api/users/me/game-preferences").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void publicBannersListIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/banners")).andExpect(status().isOk());
    }

    @Test
    void pullEndpointIsAccessibleWithoutToken() throws Exception {
        GameEntity game = new GameEntity();
        game.setTitle("Security Test Game");
        game.setGameCode("security-test-game");
        game = gameRepository.save(game);

        CharacterEntity character = new CharacterEntity();
        character.setGameId(game.getId());
        character.setName("Security Test Character");
        character.setRarity(5);
        character = characterRepository.save(character);

        BannerEntity banner = new BannerEntity();
        banner.setGameId(game.getId());
        banner.setName("Security Test Banner");
        banner.setStartAt(OffsetDateTime.now());
        banner.setPityThreshold(10);
        banner.setRateUpRate(0.5);
        BannerResponseDto createdBanner = bannerService.createBanner(banner);
        bannerService.upsertPoolCharacter(createdBanner.getId(), character.getId(), 1.0, true);

        mockMvc.perform(post("/api/banners/" + createdBanner.getId() + "/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":1,\"currentPity\":0}"))
                .andExpect(status().isOk());
    }

    @Test
    void adminBannersEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/banners")).andExpect(status().isUnauthorized());
    }

    @Test
    void publicChannelsListIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/channels")).andExpect(status().isOk());
    }

    @Test
    void adminChannelsEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/channels")).andExpect(status().isUnauthorized());
    }

    @Test
    void publicPostCommentsListIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/posts/999/comments")).andExpect(status().isOk());
    }

    @Test
    void createPostRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/channels/1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"content\":\"c\",\"templateType\":\"FREE\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCommentRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"c\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminUsersEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminUsersEndpointRejectsUserRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.USER);

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUsersEndpointAllowsMainAdminRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.MAIN_ADMIN);

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void myProfileEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void myHistoryEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me/history")).andExpect(status().isUnauthorized());
    }

    @Test
    void calendarIcsFeedIsAccessibleWithoutToken() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("calendar-security-test@example.com");
        user.setName("Calendar Security Test");
        user.setGoogleId("google-calendar-security-test-" + UUID.randomUUID());
        user.setUserCode("calendar-security-test-" + UUID.randomUUID());
        userRepository.save(user);

        mockMvc.perform(get("/api/users/" + user.getUserCode() + "/calendar.ics"))
                .andExpect(status().isOk());
    }

    @Test
    void calendarResetEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/users/me/calendar/reset")).andExpect(status().isUnauthorized());
    }

    @Test
    void pendingSchedulesEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/pending-schedules")).andExpect(status().isUnauthorized());
    }

    @Test
    void pendingSchedulesEndpointRejectsUserRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.USER);

        mockMvc.perform(get("/api/admin/pending-schedules").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void pendingSchedulesEndpointAllowsSubAdminRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.SUB_ADMIN);

        mockMvc.perform(get("/api/admin/pending-schedules").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void signupEndpointIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"security-signup-test@example.com\",\"password\":\"password123\",\"name\":\"Signup Test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void loginEndpointIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nonexistent@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void serverCostSettingsPublicEndpointIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/settings/server-cost")).andExpect(status().isOk());
    }

    @Test
    void adminServerCostSettingsEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/settings/server-cost")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminServerCostSettingsEndpointRejectsUserRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.USER);

        mockMvc.perform(get("/api/admin/settings/server-cost").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminServerCostSettingsEndpointAllowsSubAdminRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.SUB_ADMIN);

        mockMvc.perform(get("/api/admin/settings/server-cost").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void announcementsPublicEndpointIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/announcements")).andExpect(status().isOk());
    }

    @Test
    void adminAnnouncementsEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/announcements")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminAnnouncementsEndpointRejectsUserRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.USER);

        mockMvc.perform(get("/api/admin/announcements").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAnnouncementsEndpointAllowsSubAdminRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.SUB_ADMIN);

        mockMvc.perform(get("/api/admin/announcements").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void adminFileUploadEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/admin/files/upload"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminFileUploadEndpointRejectsUserRoleToken() throws Exception {
        String token = jwtProvider.createToken(1L, Role.USER);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/admin/files/upload")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
