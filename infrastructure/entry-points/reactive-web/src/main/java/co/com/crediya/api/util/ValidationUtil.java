package co.com.crediya.api.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtil {

    public static <T> Mono<T> validate(T dto, Validator validator) {
        if (dto == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body cannot be null"));
        }

        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage));
        }
        return Mono.just(dto);
    }
}
