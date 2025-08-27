package co.com.crediya.api.exceptions;

import co.com.crediya.usecase.registeruser.exception.InvalidUserDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleAllExceptions(Exception ex, ServerWebExchange exchange) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("codigo", "ERROR_INTERNO");
        errorResponse.put("mensaje", "Ha ocurrido un error inesperado. Por favor, intente nuevamente más tarde.");
        errorResponse.put("timestamp", Instant.now().toString());
        
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }
    
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationExceptions(WebExchangeBindException ex) {
        log.warn("Error de validación: {}", ex.getMessage());
        
        String errorMessage = ex.getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
                
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("codigo", "ERROR_VALIDACION");
        errorResponse.put("mensaje", errorMessage);
        errorResponse.put("timestamp", Instant.now().toString());
        
        return Mono.just(ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleInvalidUserData(InvalidUserDataException ex) {
        log.warn("Error de datos de usuario: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("codigo", "ERROR_DATOS_USUARIO");
        errorResponse.put("mensaje", ex.getMessage());
        errorResponse.put("timestamp", Instant.now().toString());
        
        return Mono.just(ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }
}
