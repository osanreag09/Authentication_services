package co.com.crediya.api;

import co.com.crediya.api.dtos.UserRequestDTO;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.registeruser.RegisterUserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private Validator validator;

    @Mock
    private ServerRequest serverRequest;

    @InjectMocks
    private UserHandler userHandler;

    private UserRequestDTO validUserRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validUserRequest = UserRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .bornDate(LocalDate.of(1990, 1, 1))
                .address("123 Test St")
                .phone("1234567890")
                .baseSalary(3000.0)
                .build();

        savedUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .bornDate(LocalDate.of(1990, 1, 1))
                .address("123 Test St")
                .phone("1234567890")
                .baseSalary(3000.0)
                .build();
    }

    @Test
    void registerUser_WithValidData_ReturnsCreated() {
        // Arrange
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(registerUserUseCase.saveUser(any(User.class))).thenReturn(Mono.just(savedUser));
        when(serverRequest.bodyToMono(UserRequestDTO.class))
                .thenReturn(Mono.just(validUserRequest));

        // Act & Assert
        StepVerifier.create(userHandler.registerUser(serverRequest))
                .assertNext(serverResponse -> {
                    assertEquals(200, serverResponse.statusCode().value());
                })
                .verifyComplete();

        verify(registerUserUseCase).saveUser(any(User.class));
        verify(validator).validate(any(UserRequestDTO.class));
    }

    @Test
    void registerUser_WithInvalidData_ReturnsBadRequest() {
        // Arrange
        ConstraintViolation<UserRequestDTO> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Email is required");

        Set<ConstraintViolation<UserRequestDTO>> violations = Set.of(violation);
        when(validator.validate(any(UserRequestDTO.class))).thenReturn(violations);

        UserRequestDTO invalidRequest = UserRequestDTO.builder()
                .email("invalid-email")
                .build();

        // Mock the request body
        when(serverRequest.bodyToMono(UserRequestDTO.class))
                .thenReturn(Mono.just(invalidRequest));

        // Act
        Mono<ServerResponse> responseMono = userHandler.registerUser(serverRequest);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(serverResponse -> {
                    assertEquals(400, serverResponse.statusCode().value());
                })
                .verifyComplete();

        verify(validator).validate(any(UserRequestDTO.class));
        verify(registerUserUseCase, never()).saveUser(any(User.class));
    }

    @Test
    void testUserRequestDTOValidation() {
        Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(validUserRequest);
        if (!violations.isEmpty()) {
            System.out.println("Validation errors in test data:");
            violations.forEach(v ->
                    System.out.println(v.getPropertyPath() + " " + v.getMessage())
            );
        }
        assertTrue(violations.isEmpty(), "There are validation errors in the test data");
    }
}