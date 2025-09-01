package co.com.crediya.api.filters;

import co.com.crediya.api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getPath().toString().startsWith("/api/v1/auth/")) {
            return chain.filter(exchange);
        }

        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .map(token -> token.substring(7))
                .flatMap(token -> jwtUtil.validateToken(token)
                        .map(claims -> new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + claims.get("role")))
                        )))
                .map(auth -> {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    return exchange;
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }
}