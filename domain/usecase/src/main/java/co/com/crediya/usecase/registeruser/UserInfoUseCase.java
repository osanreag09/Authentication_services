package co.com.crediya.usecase.registeruser;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.registeruser.exception.InvalidUserDataException;
import co.com.crediya.usecase.registeruser.gateways.RegisterUser;
import co.com.crediya.usecase.registeruser.gateways.UserInfo;
import reactor.core.publisher.Mono;

public class UserInfoUseCase implements UserInfo {
    private static final Long MAX_BASE_SALARY = 15000000L;
    private static final Long MIN_BASE_SALARY = 0L;

    private final UserRepository userRepository;

    public UserInfoUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }
}
