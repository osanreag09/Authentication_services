package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;
import reactor.core.publisher.Mono;

public interface LoginUserRepository {
    Mono<User> saveUser(User user); //TODO: Add all fields needed and modify this.
}
