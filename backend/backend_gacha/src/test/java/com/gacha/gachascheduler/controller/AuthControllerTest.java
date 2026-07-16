package com.gacha.gachascheduler.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gacha.gachascheduler.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void signupReturnsTokenAndUserInfo() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"signup-flow@example.com\",\"password\":\"password123\",\"name\":\"Signup Flow\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("signup-flow@example.com"))
                .andExpect(jsonPath("$.name").value("Signup Flow"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void signupRejectsTooShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"short-pw@example.com\",\"password\":\"short\",\"name\":\"Short PW\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signupRejectsDuplicateEmailAlreadyHavingPassword() throws Exception {
        userService.signup("duplicate-flow@example.com", "password123", "Duplicate Flow");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"duplicate-flow@example.com\",\"password\":\"password456\",\"name\":\"Duplicate Flow 2\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void loginSucceedsWithCorrectPassword() throws Exception {
        userService.signup("login-flow@example.com", "password123", "Login Flow");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login-flow@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        userService.signup("wrong-flow@example.com", "password123", "Wrong Flow");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong-flow@example.com\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsGoogleOnlyAccount() throws Exception {
        userService.findOrCreateUser("google-flow@example.com", "Google Flow", null, "google-sub-flow");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"google-flow@example.com\",\"password\":\"anyPassword\"}"))
                .andExpect(status().isUnauthorized());
    }
}
