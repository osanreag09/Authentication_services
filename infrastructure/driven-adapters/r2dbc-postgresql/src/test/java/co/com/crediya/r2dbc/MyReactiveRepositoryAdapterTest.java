package co.com.crediya.r2dbc;

import co.com.crediya.model.user.User;
import co.com.crediya.r2dbc.entity.UserEntity;
import co.com.crediya.r2dbc.mapper.DataMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyReactiveRepositoryAdapterTest {

    @Mock
    private MyReactiveRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MyReactiveRepositoryAdapter repositoryAdapter;

    private User testUser;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .bornDate(LocalDate.of(1990, 1, 1))
                .address("123 Test St")
                .phone("1234567890")
                .baseSalary(3000.0)
                .build();

        testUserEntity = DataMapper.toEntity(testUser);
    }

    @Test
    void saveUser_ShouldSaveAndReturnUser() {
        // Arrange
        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(testUserEntity));

        // Act
        Mono<User> result = repositoryAdapter.saveUser(testUser);

        // Assert
        StepVerifier.create(result)
                .assertNext(savedUser -> {
                    assertNotNull(savedUser);
                    assertEquals(testUser.getEmail(), savedUser.getEmail());
                    assertEquals(testUser.getFirstName(), savedUser.getFirstName());
                    assertEquals(testUser.getLastName(), savedUser.getLastName());
                })
                .verifyComplete();

        verify(repository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void existByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        String testEmail = "john.doe@example.com";
        when(repository.existsByEmail(testEmail)).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = repositoryAdapter.existByEmail(testEmail);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(repository, times(1)).existsByEmail(testEmail);
    }

    @Test
    void existByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        when(repository.existsByEmail(nonExistentEmail)).thenReturn(Mono.just(false));

        // Act
        Mono<Boolean> result = repositoryAdapter.existByEmail(nonExistentEmail);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(repository, times(1)).existsByEmail(nonExistentEmail);
    }

    @Test
    void saveUser_ShouldHandleError_WhenSaveFails() {
        // Arrange
        when(repository.save(any(UserEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert
        StepVerifier.create(repositoryAdapter.saveUser(testUser))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).save(any(UserEntity.class));
    }
}
