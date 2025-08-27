package co.com.crediya.r2dbc.mapper;

import co.com.crediya.model.user.User;
import co.com.crediya.r2dbc.entity.UserEntity;

public class DataMapper {

    public DataMapper() {

    }

    public static UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bornDate(user.getBornDate())
                .address(user.getAddress())
                .phone(user.getPhone())
                .email(user.getEmail())
                .baseSalary(user.getBaseSalary())
                .build();
    }

    public static User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .bornDate(entity.getBornDate())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .baseSalary(entity.getBaseSalary())
                .build();
    }

}
