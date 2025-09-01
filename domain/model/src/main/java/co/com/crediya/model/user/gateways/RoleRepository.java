package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.RolUser;
import reactor.core.publisher.Mono;

public interface RoleRepository {
    Mono<RolUser> findByName(String name);

    Mono<RolUser> getRoleById(Long id);
}
