package co.com.crediya.api.filters;

import co.com.crediya.api.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private WebFilterChain chain;

    @InjectMocks
    private AuthorizationFilter authorizationFilter;

    private ServerWebExchange exchange;
    private static final String TEST_TOKEN = "test.token.123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ROLE = "USER";

    @BeforeEach
    void setUp() {
        // Default exchange setup - can be overridden in specific tests
        exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/secure")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN)
        );
    }

    @Test
    void filter_WhenPublicEndpoint_ShouldNotAuthenticate() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(authorizationFilter.filter(exchange, chain))
                .verifyComplete();

        // Verify no authentication was set
        StepVerifier.create(ReactiveSecurityContextHolder.getContext())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void filter_WithMissingToken_ShouldReturnUnauthorized() {
        // Arrange
        exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/secure")
        );

        // Act & Assert
        StepVerifier.create(authorizationFilter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_WithValidToken_ShouldSetAuthentication() {
        // Arrange
        Claims claims = Jwts.claims()
                .setSubject(TEST_EMAIL)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); // 1 hour
        claims.put("role", TEST_ROLE);

        when(jwtUtil.validateToken(TEST_TOKEN)).thenReturn(Mono.just(claims));
        when(chain.filter(any())).thenAnswer(invocation -> {
            // Verify the authentication was set in the context
            return ReactiveSecurityContextHolder.getContext()
                    .map(ctx -> ctx.getAuthentication())
                    .cast(UsernamePasswordAuthenticationToken.class)
                    .doOnNext(auth -> {
                        assertEquals(TEST_EMAIL, auth.getPrincipal());
                        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + TEST_ROLE)));
                    })
                    .then(Mono.empty());
        });

        // Act & Assert
        StepVerifier.create(authorizationFilter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_WithInsufficientPermissions_ShouldReturnForbidden() {
        // Arrange
        exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN)
        );

        Claims claims = Jwts.claims()
                .setSubject(TEST_EMAIL)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); // 1 hour
        claims.put("role", TEST_ROLE);

        when(jwtUtil.validateToken(TEST_TOKEN)).thenReturn(Mono.just(claims));

        // Act & Assert
        StepVerifier.create(authorizationFilter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }
}