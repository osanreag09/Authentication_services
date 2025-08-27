package co.com.crediya.r2dbc.adapter;

import co.com.crediya.model.user.User;
import co.com.crediya.usecase.registeruser.RegisterUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserAdapter {

    private final RegisterUserUseCase useCase;
    private final TransactionalOperator tx;

    public Mono<User> registerUser(User user) {
        if (user == null) {
            return Mono.error(new IllegalArgumentException("User cannot be null"));
        }
        return useCase.saveUser(user)
                .as(tx::transactional)
                .doOnSuccess(result -> log.info("Transaction complete successfully"));
    }
}
