package co.com.crediya.api.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.Instant;

@Builder
public record ErrorResponse(
        @Schema(description = "Error code", example = "DATA_USER_INVALID")
        String code,

        @Schema(description = "Error message", example = "Validation error")
        String message,

        @Schema(description = "Timestamp of the error", example = "2025-08-29T01:33:31.308870200Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant timestamp
) {}