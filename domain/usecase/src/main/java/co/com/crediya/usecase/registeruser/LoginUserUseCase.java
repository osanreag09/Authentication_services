package co.com.crediya.usecase.registeruser;

import co.com.crediya.model.LoginResponse;
import co.com.crediya.model.gateways.PasswordEncoderGateway;
import co.com.crediya.model.user.gateways.RoleRepository;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.registeruser.exception.InvalidUserDataException;
import co.com.crediya.usecase.registeruser.gateways.LoginUser;
import co.com.crediya.usecase.registeruser.gateways.TokenGenerator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoginUserUseCase implements LoginUser {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenGenerator tokenGenerator;
    private final PasswordEncoderGateway passwordEncoder;

    @Override
    public Mono<LoginResponse> loginUser(String email, String password) {
        return userRepository.getUserByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .switchIfEmpty(Mono.error(new InvalidUserDataException("Invalid credentials")))
                .flatMap(user -> {
                    return roleRepository.getRoleById(user.getRol().getId())
                            .flatMap(role -> {
                                String token = tokenGenerator.generateToken(
                                        user.getEmail(),
                                        role.getName()
                                );

                                return Mono.just(LoginResponse.builder()
                                        .token(token)
                                        .email(user.getEmail())
                                        .role(role.getName())
                                        .fullName(user.getFirstName() + " " + user.getLastName())
                                        .build());
                            });
                });
    }
}
