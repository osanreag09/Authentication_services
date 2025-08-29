package co.com.crediya.usecase.registeruser;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.registeruser.exception.InvalidUserDataException;
import co.com.crediya.usecase.registeruser.gateways.RegisterUser;
import reactor.core.publisher.Mono;

public class RegisterUserUseCase implements RegisterUser {
    private static final Long MAX_BASE_SALARY = 15000000L;
    private static final Long MIN_BASE_SALARY = 0L;

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<User> registerUser(User user) {
        return  validateUserSalary(user)
                .then(userRepository.existByEmail(user.getEmail()))
                .flatMap(exist -> {
                    if(exist) {
                        return Mono.error(new InvalidUserDataException("The email is already in use"));
                    } else {
                        return userRepository.saveUser(user);
                    }
                });
    }

    private static Mono<Void> validateUserSalary(User user) {
        if(user.getBaseSalary() < MIN_BASE_SALARY || user.getBaseSalary() > MAX_BASE_SALARY) {
            return Mono.error(new InvalidUserDataException("The base salary must be between 0 and 15000000"));
        }
        return Mono.empty();
    }
}
