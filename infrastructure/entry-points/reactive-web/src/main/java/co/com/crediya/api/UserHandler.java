package co.com.crediya.api;

import co.com.crediya.api.dtos.UserRequestDTO;
import co.com.crediya.api.mappers.UserMapper;
import co.com.crediya.usecase.registeruser.RegisterUserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserHandler {

private final RegisterUserUseCase registerUser;
private final Validator validator;

    public Mono<ServerResponse> registerUser(ServerRequest request) {
        return request.bodyToMono(UserRequestDTO.class)
                .flatMap(dto -> {
                    var violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        String errorMsg = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("Validation error");
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error", errorMsg));
                    }
                    return Mono.just(dto)
                            .map(UserMapper::toDomain)
                            .flatMap(registerUser::saveUser)
                            .map(UserMapper::toResponse)
                            .flatMap(dtoResp -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(dtoResp));
                })
                .onErrorResume(e -> ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("error", e.getMessage())));
    }
}
