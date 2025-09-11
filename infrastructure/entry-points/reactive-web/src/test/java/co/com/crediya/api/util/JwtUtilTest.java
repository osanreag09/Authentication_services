package co.com.crediya.api.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private final String SECRET_KEY = "testSecretKey1234567890123456789012345678901234567890";
    private final long EXPIRATION_TIME = 3600;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_TIME);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Arrange
        String email = "test@example.com";
        String role = "USER";

        // Act
        String token = jwtUtil.generateToken(email, role);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify token can be parsed
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(email, claims.getSubject());
        assertEquals(role, claims.get("role", String.class));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnClaims() {
        // Arrange
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        // Act & Assert
        StepVerifier.create(jwtUtil.validateToken(token))
                .assertNext(claims -> {
                    assertEquals(email, claims.getSubject());
                    assertEquals(role, claims.get("role", String.class));
                })
                .verifyComplete();
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnError() {
        // Act & Assert
        StepVerifier.create(jwtUtil.validateToken(null))
                .verifyErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof IllegalArgumentException);
                    assertEquals("Token cannot be null or empty", throwable.getMessage());
                });
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnError() {
        // Act & Assert
        StepVerifier.create(jwtUtil.validateToken("   "))
                .verifyErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof IllegalArgumentException);
                    assertEquals("Token cannot be null or empty", throwable.getMessage());
                });
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnError() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        StepVerifier.create(jwtUtil.validateToken(invalidToken))
                .verifyErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof IllegalArgumentException);
                    assertTrue(throwable.getMessage().contains("Invalid token: "));
                });
    }

    @Test
    void getUsernameFromToken_ShouldReturnUsername() {
        // Arrange
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, "USER");

        // Act
        String username = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertEquals(email, username);
    }

    @Test
    void getRoleFromToken_ShouldReturnRole() {
        // Arrange
        String role = "ADMIN";
        String token = jwtUtil.generateToken("test@example.com", role);

        // Act
        String roleFromToken = jwtUtil.getRoleFromToken(token);

        // Assert
        assertEquals(role, roleFromToken);
    }

    @Test
    void tokenExpiration_ShouldWorkCorrectly() {
        // Arrange
        String email = "test@example.com";
        String role = "USER";

        // Set very short expiration (1 second) for testing
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        String token = jwtUtil.generateToken(email, role);

        // Wait for token to expire
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThrows(ExpiredJwtException.class,
                () -> jwtUtil.getUsernameFromToken(token));
    }
}