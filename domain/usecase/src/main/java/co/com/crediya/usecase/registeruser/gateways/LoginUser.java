package co.com.crediya.usecase.registeruser.gateways;

import co.com.crediya.model.LoginResponse;
import co.com.crediya.model.user.User;
import reactor.core.publisher.Mono;

public interface LoginUser {
    Mono<LoginResponse> loginUser(String email, String password);
}
