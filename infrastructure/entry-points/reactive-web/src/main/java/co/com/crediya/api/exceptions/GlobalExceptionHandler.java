package co.com.crediya.api.exceptions;

import co.com.crediya.usecase.registeruser.exception.InvalidUserDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationExceptions(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        // Get the first error message
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("DATA_USER_INVALID")
                .message(errorMessage)
                .timestamp(Instant.now())
                .build();

        return Mono.just(ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidUserData(
            InvalidUserDataException ex,
            ServerWebExchange exchange) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("DATA_USER_INVALID")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();

        return Mono.just(ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAllUncaughtException(
            Exception ex,
            ServerWebExchange exchange) {

        log.error("Unhandled error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .timestamp(Instant.now())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(
            ResponseStatusException ex,
            ServerWebExchange exchange) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("ERROR_DATOS_USUARIO")
                .message(ex.getReason() != null ? ex.getReason() : "Validation error")
                .timestamp(Instant.now())
                .build();

        return Mono.just(ResponseEntity
                .status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }
}
