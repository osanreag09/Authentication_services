package co.com.crediya.r2dbc.config;

import co.com.crediya.r2dbc.config.security.BCryptPasswordEncoderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BCryptPasswordEncoderServiceTest {

    private BCryptPasswordEncoderService passwordEncoderService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        passwordEncoderService = new BCryptPasswordEncoderService(passwordEncoder);
    }

    @Test
    void encode_ShouldReturnEncodedPassword() {
        // Arrange
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword = passwordEncoderService.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));
    }

    @Test
    void matches_WhenPasswordsMatch_ShouldReturnTrue() {
        // Arrange
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean result = passwordEncoderService.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    void matches_WhenPasswordsDoNotMatch_ShouldReturnFalse() {
        // Arrange
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean result = passwordEncoderService.matches(wrongPassword, encodedPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    void matches_WhenEncodedPasswordIsNull_ShouldReturnFalse() {
        // Arrange
        String rawPassword = "testPassword123";
        String nullEncodedPassword = null;

        // Act
        boolean result = passwordEncoderService.matches(rawPassword, nullEncodedPassword);

        // Assert
        assertFalse(result);
    }
}
