package co.com.crediya.api.filters;

import co.com.crediya.api.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class AuthorizationFilter implements WebFilter {
    private final JwtUtil jwtUtil;

    public AuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Skip filter for login endpoint
        if (exchange.getRequest().getPath().toString().equals("/api/v1/login")) {
            return chain.filter(exchange);
        }

        String token = getTokenFromRequest(exchange.getRequest());
        if (token == null) {
            log.warn("Missing token");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return jwtUtil.validateToken(token)
                .flatMap(claims -> {
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);

                    if (username == null || role == null) {
                        log.warn("Invalid token: missing username or role");
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    // Set up authentication with the single role
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    // Set the authentication in the context
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                })
                .onErrorResume(e -> {
                    log.warn("Invalid token: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        try {
            // Check for token in Authorization header (Bearer token)
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7).trim();
            }
            
            // Check for token in custom 'token' header
            String tokenHeader = request.getHeaders().getFirst("token");
            if (tokenHeader != null && !tokenHeader.isEmpty()) {
                return tokenHeader.trim();
            }
            
            return null;
        } catch (Exception e) {
            log.warn("Error extracting token from request: {}", e.getMessage());
            return null;
        }
    }
}