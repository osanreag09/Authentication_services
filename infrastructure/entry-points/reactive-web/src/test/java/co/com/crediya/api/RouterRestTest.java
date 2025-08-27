package co.com.crediya.api;

import co.com.crediya.model.user.User;
import co.com.crediya.r2dbc.adapter.RegisterUserAdapter;
import co.com.crediya.usecase.registeruser.RegisterUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, Handler.class, RouterRestTest.TestConfig.class})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Mock
    private static RegisterUserAdapter registerUserUseCase;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RegisterUserAdapter registerUserUseCase() {
            return registerUserUseCase;
        }
        
        @Bean
        public Validator validator() {
            return Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
        }
    }

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void registerUser_ShouldReturnCreatedStatus_WhenValidRequest() {
        // Given
        String requestBody = """
                {
                  "firstName": "Cosme",
                  "lastName": "Pérez",
                  "bornDate": "1990-05-15",
                  "address": "Calle 123",
                  "phone": "3001234567",
                  "email": "juan1.perez@gmail.com",
                  "baseSalary": 2500.0
                }
                """;

        User mockUser = User.builder()
                .id(123L)
                .email("test@example.com")
                .build();

        when(registerUserUseCase.registerUser(any(User.class))).thenReturn(Mono.just(mockUser));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("123")
                .jsonPath("$.email").isEqualTo("test@example.com");
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenInvalidRequest() {
        // Given
        String invalidRequestBody = """
                {
                    "email": "",
                    "password": ""
                }
                """;

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
