package co.com.crediya.usecase.registeruser;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.registeruser.exception.InvalidUserDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    private User validUser;
    private User userWithHighSalary;
    private User userWithLowSalary;
    private User existingEmailUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .baseSalary(50000.0)
                .build();

        userWithHighSalary = validUser.toBuilder()
                .baseSalary(16000000.0)
                .build();

        userWithLowSalary = validUser.toBuilder()
                .baseSalary(-1000.0)
                .build();

        existingEmailUser = validUser.toBuilder()
                .email("existing@example.com")
                .build();
    }

    @Test
    void saveUser_WithValidData_ShouldRegisterUser() {
        // Arrange
        when(userRepository.existByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.saveUser(validUser)).thenReturn(Mono.just(validUser));

        // Act & Assert
        StepVerifier.create(registerUserUseCase.registerUser(validUser))
                .expectNext(validUser)
                .verifyComplete();

        verify(userRepository).existByEmail(validUser.getEmail());
        verify(userRepository).saveUser(validUser);
    }

    @Test
    void registerUser_WithExistingEmail_ShouldReturnError() {
        // Arrange
        when(userRepository.existByEmail(existingEmailUser.getEmail())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(registerUserUseCase.registerUser(existingEmailUser))
                .expectErrorMatches(throwable -> 
                    throwable instanceof InvalidUserDataException &&
                    throwable.getMessage().equals("The email is already in use")
                )
                .verify();

        verify(userRepository).existByEmail(existingEmailUser.getEmail());
        verify(userRepository, never()).saveUser(any());
    }

    @Test
    void registerUser_WithHighSalary_ShouldReturnError() {
        // Act & Assert
        StepVerifier.create(registerUserUseCase.registerUser(userWithHighSalary))
                .expectErrorMatches(throwable ->
                    throwable instanceof InvalidUserDataException &&
                    throwable.getMessage().equals("The base salary must be between 0 and 15000000")
                )
                .verify();

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_WithNegativeSalary_ShouldReturnError() {
        // Act & Assert
        StepVerifier.create(registerUserUseCase.registerUser(userWithLowSalary))
                .expectErrorMatches(throwable ->
                    throwable instanceof InvalidUserDataException &&
                    throwable.getMessage().equals("The base salary must be between 0 and 15000000")
                )
                .verify();

        verifyNoInteractions(userRepository);
    }
}
