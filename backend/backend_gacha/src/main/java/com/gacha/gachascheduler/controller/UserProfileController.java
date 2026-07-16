package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.UserProfileResponseDto;
import com.gacha.gachascheduler.dto.ProfileUpdateRequestDto;
import com.gacha.gachascheduler.dto.UserHistoryResponseDto;
import com.gacha.gachascheduler.dto.UserMapper;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.security.AuthenticatedUser;
import com.gacha.gachascheduler.service.UserHistoryService;
import com.gacha.gachascheduler.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 로그인한 유저 본인의 프로필/활동 이력. anyRequest().authenticated() 기본 규칙으로 보호된다. */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final UserHistoryService userHistoryService;

    @GetMapping
    public ResponseEntity<UserProfileResponseDto> getMyProfile(@AuthenticationPrincipal AuthenticatedUser principal) {
        UserEntity user = userService.findUserById(principal.userId())
                .orElseThrow(() -> new RuntimeException("User not found with id " + principal.userId()));
        return ResponseEntity.ok(UserMapper.toProfileDto(user));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponseDto> updateMyProfile(
            @AuthenticationPrincipal AuthenticatedUser principal, @RequestBody ProfileUpdateRequestDto request) {
        UserEntity updated = userService.updateProfile(
                principal.userId(), request.getName(), request.getProfilePictureUrl());
        return ResponseEntity.ok(UserMapper.toProfileDto(updated));
    }

    @GetMapping("/history")
    public ResponseEntity<UserHistoryResponseDto> getMyHistory(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(userHistoryService.getHistory(principal.userId()));
    }

    /** 캘린더 구독 URL(userCode 포함)이 유출됐을 때 재발급해 기존 URL을 무효화한다. */
    @PostMapping("/calendar/reset")
    public ResponseEntity<UserProfileResponseDto> resetCalendarCode(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        UserEntity updated = userService.resetUserCode(principal.userId());
        return ResponseEntity.ok(UserMapper.toProfileDto(updated));
    }
}
