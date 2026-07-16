package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.UserProfileResponseDto;
import com.gacha.gachascheduler.dto.RoleUpdateRequestDto;
import com.gacha.gachascheduler.dto.UserMapper;
import com.gacha.gachascheduler.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserProfileResponseDto>> getUsers(@RequestParam(required = false) String query) {
        List<UserProfileResponseDto> users = userService.searchUsers(query).stream()
                .map(UserMapper::toProfileDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserProfileResponseDto> updateRole(
            @PathVariable Long id, @RequestBody RoleUpdateRequestDto request) {
        return ResponseEntity.ok(UserMapper.toProfileDto(userService.updateRole(id, request.getRole())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> suspendUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
