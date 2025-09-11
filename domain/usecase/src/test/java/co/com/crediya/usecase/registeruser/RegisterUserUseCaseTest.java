package co.com.crediya.usecase.registeruser;

import co.com.crediya.model.gateways.PasswordEncoderGateway;
import co.com.crediya.model.user.RolUser;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderGateway passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    private User validUser;
    private User userWithHighSalary;
    private User userWithLowSalary;
    private User existingEmailUser;

    @BeforeEach
    void setUp() {
        userWithHighSalary = new User(1L,
                "John",
                "Doe",
                LocalDate.now(),
                "Calle siempre vida"
                ,"1234567890"
                ,"john.doe@example.com",
                16000000L,
                "12345",
                RolUser.builder().name("CLIENT").build()
                );

        userWithLowSalary = new User(2L,
                "Jane",
                "Doe",
                LocalDate.now(),
                "calle siempre vida",
                "1234567890",
                "jane.doe@example.com",
                -1000L,
                "12345",
                RolUser.builder().name("CLIENT").build());

        validUser = User.builder()
                .id(1L)
                .firstName("Valid")
                .lastName("User")
                .email("valid@example.com")
                .baseSalary(100000.0)
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
        when(passwordEncoder.encode(validUser.getPassword())).thenReturn("encodedPassword");
        when(userRepository.saveUser(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            return Mono.just(userToSave);
        });

        // Act
        Mono<User> result = registerUserUseCase.registerUser(validUser);

        // Assert
        StepVerifier.create(result)
                .assertNext(savedUser -> {
                    assertNotNull(savedUser);
                    assertEquals(validUser.getEmail(), savedUser.getEmail());
                    assertEquals("encodedPassword", savedUser.getPassword());
                })
                .verifyComplete();

        verify(userRepository, times(1)).existByEmail(validUser.getEmail());
        verify(userRepository, times(1)).saveUser(any(User.class));
        verify(passwordEncoder, times(1)).encode(validUser.getPassword());
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
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof InvalidUserDataException);
                    assertEquals("The base salary must be between 0 and 15000000", throwable.getMessage());
                })
                .verify();

        // Verify no repository interactions occurred since validation fails first
        verify(userRepository, never()).existByEmail(anyString());
        verify(userRepository, never()).saveUser(any());
    }

    @Test
    void registerUser_WithNegativeSalary_ShouldReturnError() {
        // Act & Assert
        StepVerifier.create(registerUserUseCase.registerUser(userWithLowSalary))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof InvalidUserDataException);
                    assertEquals("The base salary must be between 0 and 15000000", throwable.getMessage());
                })
                .verify();

        // Verify no repository interactions occurred since validation fails first
        verify(userRepository, never()).existByEmail(anyString());
        verify(userRepository, never()).saveUser(any());
    }
}
