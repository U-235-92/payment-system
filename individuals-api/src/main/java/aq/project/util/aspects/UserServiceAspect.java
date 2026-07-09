package aq.project.util.aspects;

import aq.project.dto.*;
import aq.project.util.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.function.Supplier;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class UserServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.UserService.createUser(..)) && args(createUserDTO)")
    public Mono<ResponseTokenDTO> createUser(
            ProceedingJoinPoint pjp,
            @Valid CreateUserDTO createUserDTO
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-user";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String createUserEmail = createUserDTO.getIndividualData().getEmail();
        String preMainLogicLogMessage = String.format("Received request to create user with email: %s",
                createUserEmail);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to create user with email: %s",
                createUserEmail);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to create user with email: %s",
                createUserEmail);;
//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                checkConstraints(createUserDTO)
        );
    }

    private Supplier<Mono<Void>> checkConstraints(CreateUserDTO createUserDTO) {
        return () -> {
            if (isBlank(createUserDTO.getUsername()))
                throw new IllegalArgumentException("Error occurred during registration: username must no blank.");

            if (isBlank(createUserDTO.getPassword()))
                throw new IllegalArgumentException("Error occurred during registration: password must no blank.");

            if (isBlank(createUserDTO.getIndividualData().getEmail()))
                throw new IllegalArgumentException("Error occurred during registration: email must no blank.");

            if (!createUserDTO.getPassword().equals(createUserDTO.getConfirmPassword()))
                throw new IllegalArgumentException("Input password and it's confirmation do not match.");

            return Mono.empty();
        };
    }

    private boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    @Around("execution(* aq.project.services.UserService.loginUser(..)) && args(loginUserDTO)")
    public Mono<ResponseTokenDTO> loginUser(
            ProceedingJoinPoint pjp,
            @Valid LoginUserDTO loginUserDTO
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "login-user";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String loginUserEmail = loginUserDTO.getEmail();
        String preMainLogicLogMessage = String.format("Received request to login user with email: %s",
                loginUserEmail);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to login user with email: %s",
                loginUserEmail);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to login user with email: %s",
                loginUserEmail);;
//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    @Around("execution(* aq.project.services.UserService.updateUser(..)) && args(updateUserDTO)")
    public Mono<Void> updateUser(
            ProceedingJoinPoint pjp,
            @Valid UpdateUserDTO updateUserDTO
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "update-user";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String updateUserKeycloakId = updateUserDTO.getKeycloakUserId();
        String preMainLogicLogMessage = String.format("Received request to update user with id: %s",
                updateUserKeycloakId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to update user with id: %s",
                updateUserKeycloakId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to update user with id: %s",
                updateUserKeycloakId);;
//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    @Around("execution(* aq.project.services.UserService.deleteUserByKeycloakId(..)) && args(keycloakId)")
    public Mono<Void> deleteUserByKeycloakId(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "delete-user";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to delete user with id: %s",
                keycloakId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to delete user with id: %s",
                keycloakId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to delete user with id: %s",
                keycloakId);;
//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    @Around("execution(* aq.project.services.UserService.getUserInfoResponseDTO(..)) && args(authentication)")
    public Mono<UserInfoResponseDTO> getUserInfo(
            ProceedingJoinPoint pjp,
            Authentication authentication
    ) throws Throwable {
//        Check for constraints
        checkConstraints(authentication);
//        Prepare handler metadata
        String actionName = "get-user-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String email = ((Jwt) authentication).getClaim("email");
        String preMainLogicLogMessage = String.format("Received request to get user info with email: %s",
                email);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to get user info with email: %s",
                email);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to get user info with email: %s",
                email);;
//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    private void checkConstraints(Authentication authentication) {
        if(authentication == null) {
            throw new IllegalArgumentException("Access denied. Received request hasn't got valid access token");
        }
        if(authentication.getPrincipal() instanceof Jwt jwt) {
            if(Instant.now().isAfter(jwt.getExpiresAt())) {
                throw new IllegalArgumentException("Access denied. Access token was expired.");
            }
        } else {
            throw new IllegalArgumentException("Access denied. Only valid JWT access token required.");
        }
    }
}
