package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.GamePreferenceDto;
import com.gacha.gachascheduler.dto.UpdateGamePreferencesRequestDto;
import com.gacha.gachascheduler.security.AuthenticatedUser;
import com.gacha.gachascheduler.service.UserGamePreferenceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인한 유저의 관심 게임 필터(스케줄러 개인화). 로그인이 필요하므로 SecurityConfig의
 * 기본 anyRequest().authenticated() 규칙으로 보호된다(별도 permitAll 없음).
 */
@RestController
@RequestMapping("/api/users/me/game-preferences")
@RequiredArgsConstructor
public class UserGamePreferenceController {

    private final UserGamePreferenceService userGamePreferenceService;

    @GetMapping
    public ResponseEntity<List<GamePreferenceDto>> getMyGamePreferences(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(userGamePreferenceService.getUserPreferenceDtos(principal.userId()));
    }

    @PutMapping
    public ResponseEntity<List<GamePreferenceDto>> updateMyGamePreferences(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody UpdateGamePreferencesRequestDto request) {
        userGamePreferenceService.replacePreferences(principal.userId(), request.getGameCodes());
        return ResponseEntity.ok(userGamePreferenceService.getUserPreferenceDtos(principal.userId()));
    }
}
