package co.com.crediya.usecase.registeruser;

import co.com.crediya.model.user.RolUser;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInfoUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserInfoUseCase userInfoUseCase;

    private User testUser;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        RolUser testRole = RolUser.builder()
                .id(1L)
                .name("ROLE_USER")
                .description("Regular user role")
                .build();

        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email(testEmail)
                .password("encodedPassword")
                .bornDate(LocalDate.of(1990, 1, 1))
                .address("123 Test St")
                .phone("1234567890")
                .baseSalary(30000.0)
                .rol(testRole)
                .build();
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.getUserByEmail(testEmail))
                .thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(userInfoUseCase.getUserByEmail(testEmail))
                .expectNextMatches(user ->
                        user.getId().equals(1L) &&
                                user.getEmail().equals(testEmail) &&
                                user.getRol().getName().equals("ROLE_USER")
                )
                .verifyComplete();
    }

    @Test
    void getUserByEmail_WhenUserNotExists_ShouldReturnEmptyMono() {
        // Arrange
        when(userRepository.getUserByEmail(anyString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(userInfoUseCase.getUserByEmail("nonexistent@example.com"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getUserByEmail_WhenRepositoryThrowsError_ShouldPropagateError() {
        // Arrange
        String errorMessage = "Database connection failed";
        when(userRepository.getUserByEmail(testEmail))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        // Act & Assert
        StepVerifier.create(userInfoUseCase.getUserByEmail(testEmail))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals(errorMessage)
                )
                .verify();
    }
}