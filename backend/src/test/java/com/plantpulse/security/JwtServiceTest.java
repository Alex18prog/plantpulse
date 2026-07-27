package com.plantpulse.security;

import com.plantpulse.domain.User;
import com.plantpulse.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-only-secret-key-at-least-32-bytes-long-1234567890";

    private JwtService jwtService;
    private User adminUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);

        adminUser = User.builder()
                .id(1L)
                .email("admin@plantpulse.dev")
                .passwordHash("irrelevant-hash")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void generateToken_thenExtractEmail_roundTrips() {
        String token = jwtService.generateToken(adminUser);

        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@plantpulse.dev");
    }

    @Test
    void isTokenValid_matchingPrincipal_returnsTrue() {
        String token = jwtService.generateToken(adminUser);
        UserDetails principal = new AppUserPrincipal(adminUser);

        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }

    @Test
    void isTokenValid_differentUsername_returnsFalse() {
        String token = jwtService.generateToken(adminUser);
        User otherUser = User.builder().email("someone-else@plantpulse.dev").passwordHash("x").role(Role.TECHNICIAN).build();
        UserDetails otherPrincipal = new AppUserPrincipal(otherUser);

        assertThat(jwtService.isTokenValid(token, otherPrincipal)).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String expiredToken = jwtService.generateToken(adminUser);
        UserDetails principal = new AppUserPrincipal(adminUser);

        assertThat(jwtService.isTokenValid(expiredToken, principal)).isFalse();
    }

    @Test
    void isTokenValid_malformedToken_returnsFalse() {
        UserDetails principal = new AppUserPrincipal(adminUser);

        assertThat(jwtService.isTokenValid("not-a-real-jwt", principal)).isFalse();
    }
}
