package com.gacha.gachascheduler.security;

import com.gacha.gachascheduler.enums.Role;

public record AuthenticatedUser(Long userId, Role role) {
}
