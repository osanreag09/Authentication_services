package co.com.crediya.usecase.registeruser;

import co.com.crediya.model.gateways.PasswordEncoderGateway;
import co.com.crediya.model.user.RolUser;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.RoleRepository;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.registeruser.exception.InvalidUserDataException;
import co.com.crediya.usecase.registeruser.gateways.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private PasswordEncoderGateway passwordEncoder;

    @InjectMocks
    private LoginUserUseCase loginUserUseCase;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPassword";
    private static final String TEST_TOKEN = "test.token.123";
    private static final Long ROLE_ID = 1L;
    private static final String ROLE_NAME = "USER";

    private User testUser;
    private RolUser testRole;

    @BeforeEach
    void setUp() {
        testRole = RolUser.builder()
                .id(ROLE_ID)
                .name(ROLE_NAME)
                .build();

        testUser = User.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .firstName("Test")
                .lastName("User")
                .rol(testRole)
                .build();
    }

    @Test
    void loginUser_WithValidCredentials_ReturnsToken() {
        // Arrange
        when(userRepository.getUserByEmail(TEST_EMAIL)).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(roleRepository.getRoleById(ROLE_ID)).thenReturn(Mono.just(testRole));
        when(tokenGenerator.generateToken(TEST_EMAIL, ROLE_NAME)).thenReturn(TEST_TOKEN);

        // Act & Assert
        StepVerifier.create(loginUserUseCase.loginUser(TEST_EMAIL, TEST_PASSWORD))
                .expectNextMatches(loginResponse ->
                        TEST_TOKEN.equals(loginResponse.getToken()) &&
                                TEST_EMAIL.equals(loginResponse.getEmail()) &&
                                ROLE_NAME.equals(loginResponse.getRole()) &&
                                "Test User".equals(loginResponse.getFullName())
                )
                .verifyComplete();

        verify(userRepository).getUserByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        verify(roleRepository).getRoleById(ROLE_ID);
        verify(tokenGenerator).generateToken(TEST_EMAIL, ROLE_NAME);
    }

    @Test
    void loginUser_WithInvalidEmail_ThrowsException() {
        // Arrange
        when(userRepository.getUserByEmail(TEST_EMAIL)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loginUserUseCase.loginUser(TEST_EMAIL, TEST_PASSWORD))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidUserDataException &&
                                "Invalid credentials".equals(throwable.getMessage())
                )
                .verify();

        verify(userRepository).getUserByEmail(TEST_EMAIL);
        verifyNoInteractions(passwordEncoder, roleRepository, tokenGenerator);
    }

    @Test
    void loginUser_WithInvalidPassword_ThrowsException() {
        // Arrange
        when(userRepository.getUserByEmail(TEST_EMAIL)).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        StepVerifier.create(loginUserUseCase.loginUser(TEST_EMAIL, TEST_PASSWORD))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidUserDataException &&
                                "Invalid credentials".equals(throwable.getMessage())
                )
                .verify();

        verify(userRepository).getUserByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        verifyNoInteractions(roleRepository, tokenGenerator);
    }
}