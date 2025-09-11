package co.com.crediya.r2dbc;

import co.com.crediya.model.user.RolUser;
import co.com.crediya.r2dbc.entity.RolEntity;
import co.com.crediya.r2dbc.entity.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RolRepository extends ReactiveCrudRepository<RolEntity, Long>, ReactiveQueryByExampleExecutor<RolEntity> {

    Mono<RolUser> findByName(String name);

    Mono<RolUser> getRoleById(Long id);
}
