package co.com.crediya.usecase.registeruser.gateways;

import co.com.crediya.model.user.User;
import reactor.core.publisher.Mono;

public interface RegisterUser {
    public Mono<User> registerUser(User user);
}
