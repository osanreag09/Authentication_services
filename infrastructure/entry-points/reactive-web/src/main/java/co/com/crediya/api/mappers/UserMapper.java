package co.com.crediya.api.mappers;


import co.com.crediya.api.dtos.UserRequestDTO;
import co.com.crediya.api.dtos.UserResponseDTO;
import co.com.crediya.model.user.User;

public class UserMapper {

    public UserMapper() {

    }

    public static User toDomain(UserRequestDTO dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .bornDate(dto.getBornDate())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .baseSalary(dto.getBaseSalary())
                .build();
    }

    public static UserResponseDTO toResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
}
