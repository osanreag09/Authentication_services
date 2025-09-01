package co.com.crediya.api.mappers;

import co.com.crediya.api.dtos.LoginResponseDTO;
import co.com.crediya.model.LoginResponse;

public class LoginMapper {

    public static LoginResponseDTO toLoginResponse(LoginResponse response) {
        return LoginResponseDTO.builder()
                .token(response.getToken())
                .email(response.getEmail())
                .role(response.getRole())
                .fullName(response.getFullName())
                .build();
    }
}
