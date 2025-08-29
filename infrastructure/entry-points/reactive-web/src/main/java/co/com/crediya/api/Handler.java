package co.com.crediya.api;

import co.com.crediya.api.dtos.UserRequestDTO;
import co.com.crediya.api.mappers.UserMapper;
import co.com.crediya.api.util.ValidationUtil;
import co.com.crediya.usecase.registeruser.gateways.RegisterUser;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
private final Validator validator;
private final RegisterUser registerUser;

    public Mono<ServerResponse> registerUser(ServerRequest request) {
        return request.bodyToMono(UserRequestDTO.class)
                .flatMap(dto -> ValidationUtil.validate(dto, validator))
                .map(UserMapper::toDomain)
                .flatMap(registerUser::registerUser)
                .map(UserMapper::toResponse)
                .flatMap(dtoResp -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dtoResp));
    }
}
