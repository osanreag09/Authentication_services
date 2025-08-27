package co.com.crediya.api.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "User registration request")
public class UserRequestDTO {
    private Long id;

    @NotBlank(message = "Names cannot be empty")
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "User's birth date in YYYY-MM-DD format", example = "1990-01-01")
    private LocalDate bornDate;
    @Schema(description = "User's address", example = "123 Main St, City, Country")
    private String address;
    @Schema(description = "User's phone number", example = "+1234567890")
    private String phone;

    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must have a valid domain")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @NotNull(message = "Base salary cannot be null")
    @Schema(description = "User's base salary", example = "50000.00")
    private Double baseSalary;
}
