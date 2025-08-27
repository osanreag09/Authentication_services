package co.com.crediya.r2dbc.adapter;

import co.com.crediya.model.user.User;
import co.com.crediya.usecase.registeruser.RegisterUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserAdapterTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private RegisterUserAdapter registerUserAdapter;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(registerUserUseCase.saveUser(any(User.class)))
                .thenReturn(Mono.just(testUser));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(registerUserAdapter.registerUser(testUser))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void registerUser_WithError_ReturnsError() {
        // Arrange
        String errorMessage = "Database error";
        when(registerUserUseCase.saveUser(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        StepVerifier.create(registerUserAdapter.registerUser(testUser))
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException && 
                    throwable.getMessage().equals(errorMessage))
                .verify();
    }

    @Test
    void registerUser_WithNullUser_ReturnsError() {
        // Act & Assert
        StepVerifier.create(registerUserAdapter.registerUser(null))
                .expectError(IllegalArgumentException.class)
                .verify();
        
        // Verify no interactions with mocks
        verifyNoInteractions(registerUserUseCase);
        verifyNoInteractions(transactionalOperator);
    }
}
