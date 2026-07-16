package com.gacha.gachascheduler.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.gacha.gachascheduler.enums.Role;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private final JwtProvider jwtProvider =
            new JwtProvider("unit-test-secret-key-must-be-at-least-32-bytes-long", 60);

    @Test
    void createsAndParsesTokenWithUserIdAndRole() {
        String token = jwtProvider.createToken(42L, Role.MAIN_ADMIN);

        assertThat(jwtProvider.isValid(token)).isTrue();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(42L);
        assertThat(jwtProvider.getRole(token)).isEqualTo(Role.MAIN_ADMIN);
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtProvider.createToken(1L, Role.USER);
        int mid = token.length() / 2;
        char flipped = token.charAt(mid) == 'a' ? 'b' : 'a';
        String tampered = token.substring(0, mid) + flipped + token.substring(mid + 1);

        assertThat(jwtProvider.isValid(tampered)).isFalse();
    }

    @Test
    void rejectsExpiredToken() {
        JwtProvider shortLived = new JwtProvider("unit-test-secret-key-must-be-at-least-32-bytes-long", -1);
        String token = shortLived.createToken(1L, Role.USER);

        assertThat(shortLived.isValid(token)).isFalse();
    }
}
