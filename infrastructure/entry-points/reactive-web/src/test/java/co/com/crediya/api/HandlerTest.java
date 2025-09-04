package co.com.crediya.api;

import co.com.crediya.api.dtos.UserRequestDTO;
import co.com.crediya.model.user.RolUser;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.registeruser.gateways.LoginUser;
import co.com.crediya.usecase.registeruser.gateways.RegisterUser;
import co.com.crediya.usecase.registeruser.gateways.UserInfo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
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
class HandlerTest {

    @Mock
    private RegisterUser registerUserUseCase;

    @Mock
    private UserInfo userInfo;

    @Mock
    private Validator validator;

    @Mock
    private ServerRequest serverRequest;

    @Mock
    private LoginUser loginUser;

    @InjectMocks
    private Handler handler;

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
                .password("password")
                .rol(1L)
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
                .password("password")
                .rol(RolUser.builder().name("ADMIN").build())
                .build();
    }

    @Test
    void registerUser_WithValidData_ReturnsCreated() {
        // Arrange
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(registerUserUseCase.registerUser(any(User.class))).thenReturn(Mono.just(savedUser));
        when(serverRequest.bodyToMono(UserRequestDTO.class))
                .thenReturn(Mono.just(validUserRequest));

        // Act & Assert
        StepVerifier.create(handler.registerUser(serverRequest))
                .assertNext(serverResponse -> {
                    assertEquals(200, serverResponse.statusCode().value());
                })
                .verifyComplete();

        verify(registerUserUseCase).registerUser(any(User.class));
        verify(validator).validate(any(UserRequestDTO.class));
    }

    @Test
    void registerUser_WithInvalidData_ReturnsBadRequest() {
        // Arrange
        UserRequestDTO invalidRequest = UserRequestDTO.builder()
                .email("invalid-email")
                .build();

        // Create a constraint violation for the invalid email
        ConstraintViolation<UserRequestDTO> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Email must have a valid domain");
        Set<ConstraintViolation<UserRequestDTO>> violations = Set.of(violation);

        // Mock the request body and validator
        when(serverRequest.bodyToMono(UserRequestDTO.class))
                .thenReturn(Mono.just(invalidRequest));
        when(validator.validate(any(UserRequestDTO.class))).thenReturn(violations);

        // Act
        Mono<ServerResponse> responseMono = handler.registerUser(serverRequest);

        // Assert
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof ResponseStatusException) {
                        ResponseStatusException ex = (ResponseStatusException) throwable;
                        return ex.getStatusCode() == HttpStatus.BAD_REQUEST &&
                               ex.getReason().contains("Email must have a valid domain");
                    }
                    return false;
                })
                .verify();

        verify(validator).validate(any(UserRequestDTO.class));
        verify(registerUserUseCase, never()).registerUser(any(User.class));
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

    @Test
    void getUserByEmail_WhenUserExists_ReturnsUser() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .rol(RolUser.builder().name("ADMIN").build())
                .password("password")
                .build();

        when(serverRequest.pathVariable("email")).thenReturn(email);
        when(userInfo.getUserByEmail(email)).thenReturn(Mono.just(user));

        // Act & Assert
        StepVerifier.create(handler.getUserByEmail(serverRequest))
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                })
                .verifyComplete();

        verify(userInfo).getUserByEmail(email);
    }

    @Test
    void getUserByEmail_WhenUserNotFound_ReturnsNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(serverRequest.pathVariable("email")).thenReturn(email);
        when(userInfo.getUserByEmail(email)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(handler.getUserByEmail(serverRequest))
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof ResponseStatusException) {
                        ResponseStatusException ex = (ResponseStatusException) throwable;
                        return ex.getStatusCode() == HttpStatus.NOT_FOUND &&
                               ex.getReason().equals("User not found: " + email);
                    }
                    return false;
                })
                .verify();

        verify(userInfo).getUserByEmail(email);
    }
}