package co.com.crediya.api;

import co.com.crediya.api.dtos.UserRequestDTO;
import co.com.crediya.api.dtos.UserResponseDTO;
import co.com.crediya.api.exceptions.ErrorResponse;
import co.com.crediya.api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import java.util.Arrays;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "User", description = "User management APIs")
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    method = {org.springframework.web.bind.annotation.RequestMethod.POST},
                    beanClass = Handler.class,
                    beanMethod = "registerUser",
                    operation = @Operation(
                            operationId = "registerUser",
                            summary = "Register a new user",
                            description = "Creates a new user with the provided information",
                            tags = {"User"},
                            requestBody = @RequestBody(
                                    description = "User details to register",
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = UserRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "User registered successfully",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = UserResponseDTO.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid input data",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponse.class)
                                            )
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios/{email}",
                    method = {org.springframework.web.bind.annotation.RequestMethod.GET},
                    beanClass = Handler.class,
                    beanMethod = "getUserByEmail",
                    operation = @Operation(
                            operationId = "getUserByEmail",
                            summary = "Get user by email",
                            description = "Retrieves a user's details by their email address",
                            tags = {"User"},
                            parameters = {
                                    @io.swagger.v3.oas.annotations.Parameter(
                                            name = "email",
                                            description = "Email address of the user to retrieve",
                                            required = true,
                                            in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "User found and returned successfully",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = UserResponseDTO.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "User not found",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponse.class)
                                            )
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler, JwtUtil jwtUtil) {
        return route(POST("/api/v1/usuarios"),
                request -> hasAnyRole(request, jwtUtil, "ADMIN", "ASSESSOR", "CLIENT")
                        .flatMap(hasAccess -> {
                            if (hasAccess) {
                                return handler.registerUser(request);
                            } else {
                                return ServerResponse.status(FORBIDDEN).build();
                            }
                        }))
                .andRoute(GET("/api/v1/usuarios/{email}"),
                        request -> hasAnyRole(request, jwtUtil, "ADMIN")
                                .flatMap(hasAccess -> {
                                    if (hasAccess) {
                                        return handler.getUserByEmail(request);
                                    } else {
                                        return ServerResponse.status(FORBIDDEN).build();
                                    }
                                }))
                .andRoute(POST("/api/v1/login"), handler::login);
    }

    private Mono<Boolean> hasAnyRole(ServerRequest request, JwtUtil jwtUtil, String... roles) {
        return Mono.justOrEmpty(request.headers().firstHeader(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .flatMap(token -> {
                    try {
                        String userRole = jwtUtil.getRoleFromToken(token);
                        return Mono.just(Arrays.asList(roles).contains(userRole));
                    } catch (Exception e) {
                        return Mono.just(false);
                    }
                })
                .defaultIfEmpty(false);
    }
}
