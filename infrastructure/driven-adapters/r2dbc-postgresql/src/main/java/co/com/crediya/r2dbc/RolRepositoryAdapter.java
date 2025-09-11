package co.com.crediya.r2dbc;

import co.com.crediya.model.user.RolUser;
import co.com.crediya.model.user.gateways.RoleRepository;
import co.com.crediya.r2dbc.entity.RolEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import co.com.crediya.r2dbc.mapper.RolDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class RolRepositoryAdapter extends ReactiveAdapterOperations<
        RolUser,
        RolEntity,
        Long,
        RolRepository
> implements RoleRepository {
    public RolRepositoryAdapter(RolRepository repository, ObjectMapper mapper, ReactiveTransactionManager transactionManager) {
        super(repository, mapper, d -> mapper.map(d, RolUser.class), transactionManager);
    }

    @Override
    public Mono<RolUser> findByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Mono<RolUser> getRoleById(Long id) {
        log.info("Getting role by id: {}", id);
        return repository.findById(id)
                .map(RolDataMapper::toDomain);
    }

}
