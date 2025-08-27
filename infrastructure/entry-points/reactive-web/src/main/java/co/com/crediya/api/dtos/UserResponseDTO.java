package co.com.crediya.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "User response data")
public class UserResponseDTO {
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;
    @Schema(description = "User's first name", example = "John")
    private String firstName;
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
}
