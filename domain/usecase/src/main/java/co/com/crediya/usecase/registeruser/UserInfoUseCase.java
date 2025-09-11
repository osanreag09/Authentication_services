package co.com.crediya.usecase.registeruser;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.registeruser.gateways.UserInfo;
import reactor.core.publisher.Mono;

public class UserInfoUseCase implements UserInfo {

    private final UserRepository userRepository;

    public UserInfoUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }
}
