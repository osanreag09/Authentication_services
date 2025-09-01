package co.com.crediya.api;

import co.com.crediya.api.dtos.UserLoginRequestDTO;
import co.com.crediya.api.dtos.UserRequestDTO;
import co.com.crediya.api.mappers.LoginMapper;
import co.com.crediya.api.mappers.UserMapper;
import co.com.crediya.api.util.ValidationUtil;
import co.com.crediya.usecase.registeruser.exception.InvalidUserDataException;
import co.com.crediya.usecase.registeruser.gateways.LoginUser;
import co.com.crediya.usecase.registeruser.gateways.RegisterUser;
import co.com.crediya.usecase.registeruser.gateways.UserInfo;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
    private final Validator validator;
    private final RegisterUser registerUser;
    private final UserInfo userInfo;
    private final LoginUser loginUser;

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

    public Mono<ServerResponse> getUserByEmail(ServerRequest request) {
        String email = request.pathVariable("email");
        
        return userInfo.getUserByEmail(email)
                .map(UserMapper::toResponse)
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found: " + email
                )));
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(UserLoginRequestDTO.class)
                .flatMap(login -> loginUser.loginUser(login.getEmail(), login.getPassword())
                        .map(LoginMapper::toLoginResponse)
                        .flatMap(loginResponse -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginResponse)
                        )
                        .onErrorResume(InvalidUserDataException.class, e ->
                                ServerResponse.status(HttpStatus.UNAUTHORIZED).build()
                        )
                );
    }
}
