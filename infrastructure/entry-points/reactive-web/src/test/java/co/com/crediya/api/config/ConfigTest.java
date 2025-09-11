package co.com.crediya.api.config;

import co.com.crediya.api.RouterRestTest;
import co.com.crediya.api.util.JwtUtil;
import co.com.crediya.usecase.registeruser.gateways.LoginUser;
import co.com.crediya.usecase.registeruser.gateways.RegisterUser;
import co.com.crediya.usecase.registeruser.gateways.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({
    SecurityHeadersConfig.class,
    CorsConfig.class
})
@ContextConfiguration(classes = {
    SecurityConfig.class,
    TestController.class,
    RouterRestTest.TestConfig.class
})
@ActiveProfiles("test")
class ConfigTest {

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private RegisterUser registerUser;

    @MockitoBean
    private UserInfo userInfo;

    @MockitoBean
    private Validator validator;

    @MockitoBean
    private LoginUser loginUser;

    @MockitoBean
    private JwtUtil jwtUtil;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        // Configure JwtUtil mock to return a specific role for the token
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("ADMIN");
        
        Claims claims = Jwts.claims()
                .setSubject("test@example.com")
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .setIssuedAt(new Date())
                .setIssuer("test-issuer")
                .setId("test-id");
        claims.put("role", "ADMIN");
        
        when(jwtUtil.validateToken(anyString())).thenReturn(Mono.just(claims));

        this.webTestClient = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer valid.token.here")
                .build();
    }


    @Test
    @WithMockUser
    void securityHeadersAreApplied() {
        webTestClient.get()
                .uri("/test/headers")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }
}