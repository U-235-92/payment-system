package aq.project.aspects;

import aq.project.aspects.handlers.AbstractAspectHandler;
import aq.project.dto.*;
import aq.project.util.metrics.ApplicationMetricsRegistry;
import io.micrometer.core.annotation.Timed;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserRestControllerAspect {

    @Value("${spring.application.name}")
    private String tracerName;

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    @Timed(value = "individuals_api.create_user_time")
    @Around("execution(* aq.project.controllers.UserRestController.createUser(..)) && args(createUserDTO)")
    public Mono<ResponseEntity<ResponseTokenDTO>> createUser(ProceedingJoinPoint pjp, CreateUserDTO createUserDTO) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Create user dto is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return "Received create user request";
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return "Handle of create user dto completed successfully";
            }

            @Override
            protected void handleCustomViolations() {
                if(isBlank(createUserDTO.getUsername())) {
                    throw new IllegalArgumentException("Error occurred during registration: username must no blank");
                }
                if(isBlank(createUserDTO.getPassword())) {
                    throw new IllegalArgumentException("Error occurred during registration: password must no blank");
                }
                if(isBlank(createUserDTO.getIndividualData().getEmail())) {
                    throw new IllegalArgumentException("Error occurred during registration: email must no blank");
                }
                if(!createUserDTO.getPassword().equals(createUserDTO.getConfirmPassword())) {
                    throw new IllegalArgumentException("Input password and it's confirmation do not match");
                }
            }

            private boolean isBlank(String string) {
                return string == null || string.trim().isEmpty();
            }
        };
        String spanName = "create_user";
        return handler.handleAspect(
                pjp,
                createUserDTO,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessCreateUserCounter,
                ApplicationMetricsRegistry::incrementFailCreateUserCounter
        );
    }

    @Timed(value = "individuals_api.login_user_time")
    @Around("execution(* aq.project.controllers.UserRestController.loginUser(..)) && args(loginUserDTO)")
    public Mono<ResponseEntity<ResponseTokenDTO>> loginUser(ProceedingJoinPoint pjp, LoginUserDTO loginUserDTO) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Login user dto is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return String.format("Received login user request with email [%s]", loginUserDTO.getEmail());
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return String.format("Handle of login user with email [%s] completed successfully", loginUserDTO.getEmail());
            }
        };
        String spanName = "login_user";
        return handler.handleAspect(
                pjp,
                loginUserDTO,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessLoginUserCounter,
                ApplicationMetricsRegistry::incrementFailLoginUserCounter
        );
    }

    @Timed(value = "individuals_api.update_user_time")
    @Around("execution(* aq.project.controllers.UserRestController.updateUser(..)) && args(updateUserDTO)")
    public Mono<ResponseEntity<Void>> updateUser(ProceedingJoinPoint pjp, UpdateUserDTO updateUserDTO) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Update user dto is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return String.format("Received update user request for user with keycloak id [%s]",
                        updateUserDTO.getKeycloakUserId());
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return String.format("Handle of update user request for user with keycloak id [%s] completed successfully",
                        updateUserDTO.getKeycloakUserId());
            }
        };
        String spanName = "update_user";
        return handler.handleAspect(
                pjp,
                updateUserDTO,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessUpdateUserCounter,
                ApplicationMetricsRegistry::incrementFailUpdateUserCounter
        );
    }

    @Timed(value = "individuals_api.delete_user_time")
    @Around("execution(* aq.project.controllers.UserRestController.deleteUserByKeycloakId(..)) && args(keycloakId)")
    public Mono<ResponseEntity<Void>> deleteUserByKeycloakId(ProceedingJoinPoint pjp, String keycloakId) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Keycloak id is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return String.format("Received delete user request for user with keycloak id [%s]", keycloakId);
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return String.format("Handle of delete user request for user with keycloak id [%s] completed successfully", keycloakId);
            }
        };
        String spanName = "delete_user_by_keycloak_id";
        return handler.handleAspect(
                pjp,
                keycloakId,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessDeleteUserByKeycloakIdCounter,
                ApplicationMetricsRegistry::incrementFailDeleteUserByKeycloakIdCounter
        );
    }

    @Timed(value = "individuals_api.get_user_info_time")
    @Around("execution(* aq.project.controllers.UserRestController.getUserInfo(..)) && args(authentication)")
    public Mono<ResponseEntity<UserInfoResponseDTO>> getUserInfo(ProceedingJoinPoint pjp, Authentication authentication) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Authentication is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                if(authentication.getPrincipal() instanceof Jwt jwt) {
                    String email = jwt.getClaim("email");
                    return String.format("Received get user info request for user with email [%s]", email);
                }
                throw new IllegalArgumentException("Access denied. Only valid JWT access token required");
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                String email = ((Jwt) authentication).getClaim("email");
                return String.format("Handle of get user info request for user with email [%s] completed successfully", email);
            }

            @Override
            protected void handleCustomViolations() {
                if(authentication.getPrincipal() instanceof Jwt jwt) {
                    if(Instant.now().isAfter(jwt.getExpiresAt())) {
                        throw new IllegalArgumentException("Access denied. Access token was expired");
                    }
                }
                throw new IllegalArgumentException("Access denied. Only valid JWT access token required");
            }
        };
        String spanName = "get_user_info";
        return handler.handleAspect(
                pjp,
                authentication,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessGetUserInfoCounter,
                ApplicationMetricsRegistry::incrementFailGetUserInfoCounter
        );
    }
}
