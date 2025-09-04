package co.com.crediya.api;

import co.com.crediya.api.util.JwtUtil;
import co.com.crediya.model.user.RolUser;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.registeruser.gateways.LoginUser;
import co.com.crediya.usecase.registeruser.gateways.RegisterUser;
import co.com.crediya.usecase.registeruser.gateways.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebFluxTest
@ContextConfiguration(classes = {
        RouterRest.class,
        Handler.class,
        RouterRestTest.TestConfig.class,
        RouterRestTest.TestSecurityConfig.class
})
public class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RegisterUser registerUserUseCase;

    @MockitoBean
    private UserInfo userInfo;

    @MockitoBean
    private LoginUser loginUser;

    @MockitoBean
    private JwtUtil jwtUtil;

    @EnableWebFluxSecurity
    @TestConfiguration
    public static class TestSecurityConfig {
        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
            return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                    .pathMatchers("/api/v1/usuarios").permitAll()
                    .anyExchange().authenticated()
                )
                .build();
        }
    }

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public RegisterUser registerUserUseCase() {
            return Mockito.mock(RegisterUser.class);
        }

        @Bean
        public UserInfo userInfo() {
            return Mockito.mock(UserInfo.class);
        }

        @Bean
        public LoginUser loginUser() {
            return Mockito.mock(LoginUser.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return Mockito.mock(JwtUtil.class);
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

    @BeforeEach
    void setUp() {
        // Configure JwtUtil mock
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("ADMIN");
        Claims mockClaims = Jwts.claims().setSubject("test@example.com");
        mockClaims.put("role", "ADMIN");
        when(jwtUtil.validateToken(anyString())).thenReturn(Mono.just(mockClaims));

        // Create a mock user with all required fields
        RolUser mockRole = RolUser.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        User mockUser = User.builder()
                .id(123L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .rol(mockRole)  // Make sure to set the role
                .build();

        when(registerUserUseCase.registerUser(any(User.class))).thenReturn(Mono.just(mockUser));
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
          "baseSalary": 2500.0,
          "password": "password",
          "rol": 1
        }
        """;

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid.token.here")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(123)
                .jsonPath("$.email").isEqualTo("test@example.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid.token.here")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }
}