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
import java.util.Map;
import java.util.Set;

@Slf4j
public class AuthorizationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    private final Map<String, Set<String>> securedPaths = Map.of(
            "/api/v1/usuarios", Set.of("ADMIN", "ASSESSOR")
    );

    public AuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String token = getTokenFromRequest(exchange.getRequest());
        if (token == null) {
            log.warn("Missing token in request to {}", path);
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

                    // Special handling for user detail endpoint
                    if (path.matches("/api/v1/usuarios/[^/]+")) {
                        String requestedEmail = path.substring(path.lastIndexOf('/') + 1);

                        // Allow access if user is ADMIN/ASSESSOR or if it's their own email
                        if (!"ADMIN".equals(role) && !"ASSESSOR".equals(role) && !username.equals(requestedEmail)) {
                            log.warn("Access denied for user {} with role {} to path {}",
                                    username, role, path);
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }
                    }
                    // For all other endpoints, use the existing role-based access control
                    else if (!hasAccess(path, role)) {
                        log.warn("Access denied for user {} with role {} to path {}",
                                username, role, path);
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                })
                .onErrorResume(e -> {
                    log.warn("Token validation failed: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/v1/login") ||
                path.equals("/api/v1/health") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/webjars") ||
                path.startsWith("/swagger-resources") ||
                path.endsWith(".html") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".json") ||
                path.equals("/favicon.ico");
    }

    private boolean hasAccess(String path, String userRole) {
        String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        for (Map.Entry<String, Set<String>> entry : securedPaths.entrySet()) {
            String pattern = entry.getKey();

            String regex = pattern
                    .replace("/", "\\/")
                    .replace("{", "(?<")
                    .replace("}", ">[^\\/]+)") + "/?$";

            if (normalizedPath.matches(regex)) {
                return entry.getValue().stream()
                        .anyMatch(role -> role.equals(userRole));
            }
        }
        return true;
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