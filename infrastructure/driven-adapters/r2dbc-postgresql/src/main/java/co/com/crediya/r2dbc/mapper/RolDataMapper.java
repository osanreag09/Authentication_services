package co.com.crediya.r2dbc.mapper;

import co.com.crediya.model.user.RolUser;
import co.com.crediya.r2dbc.entity.RolEntity;

public class RolDataMapper {

    public static RolEntity toEntity(RolUser rolUser) {
        return RolEntity.builder()
                .id(rolUser.getId())
                .name(rolUser.getName())
                .build();
    }

    public static RolUser toDomain(RolEntity entity) {
        return RolUser.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
