package aq.project.aspects;

import aq.project.dto.CreateUserDTO;
import aq.project.dto.LoginUserDTO;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.UpdateUserDTO;
import aq.project.exceptions.InvalidAccessTokenException;
import aq.project.exceptions.InvalidIndividualsDataException;
import aq.project.exceptions.InvalidPasswordConfirmException;
import aq.project.exceptions.InvalidUserRegistrationEventException;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import static aq.project.util.telemetry.TelemetryUtils.*;

@Slf4j
@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class GatewayUserRestControllerValidationAspect {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    private final Validator validator;

    @Before("execution(* aq.project.controllers.GatewayUserRestController.createUser(..)) && args(createUserDTO)")
    public void checkCreateUserViolations(CreateUserDTO createUserDTO) throws InvalidPasswordConfirmException, InvalidUserRegistrationEventException, InvalidIndividualsDataException {
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(createUserDTO);
        if(!violations.isEmpty()) {
            String description = "Attempt of create user with wrong data: ";
            ConstraintViolationException exception = new ConstraintViolationException(violations);
            logException(description, exception);
            throw exception;
        }
        if(createUserDTO.getIndividualData() == null) {
            InvalidIndividualsDataException exception = new InvalidIndividualsDataException("Error during registration: individual data of user is null");
            logException("", exception);
            throw exception;
        }
        if(isBlank(createUserDTO.getPassword()) || isBlank(createUserDTO.getUsername()) || isBlank(createUserDTO.getIndividualData().getEmail())) {
            InvalidUserRegistrationEventException exception = new InvalidUserRegistrationEventException("Error during registration: email, username, password must no blank");
            logException("", exception);
            throw exception;
        }
        if(!createUserDTO.getPassword().equals(createUserDTO.getConfirmPassword())) {
            String description = "Error during registration: input password and it's confirm do not match";
            ConstraintViolationException exception = new ConstraintViolationException(violations);
            logException(description, exception);
            throw exception;
        }
    }

    private boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.loginUser(..)) && args(loginUserDTO)")
    public void checkLoginUserViolations(LoginUserDTO loginUserDTO) {
        Set<ConstraintViolation<LoginUserDTO>> violations = validator.validate(loginUserDTO);
        if(!violations.isEmpty()) {
            String description = "Attempt of login user with wrong data: ";
            ConstraintViolationException exception = new ConstraintViolationException(violations);
            logException(description, exception);
            throw exception;
        }
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.updateUser(..)) && args(updateUserDTO)")
    public void checkUpdateUserViolations(UpdateUserDTO updateUserDTO) {
        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);
        if(!violations.isEmpty()) {
            String description = "Attempt of update user with wrong user data: ";
            ConstraintViolationException exception = new ConstraintViolationException(violations);
            logException(description, exception);
            throw exception;
        }
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.deleteUserByKeycloakId(..)) && args(keycloakId)")
    public void checkDeleteUserByKeycloakIdViolations(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.refreshToken(..)) && args(refreshTokenDTO)")
    public void checkRefreshTokenViolations(RefreshTokenDTO refreshTokenDTO) {
        Set<ConstraintViolation<RefreshTokenDTO>> violations = validator.validate(refreshTokenDTO);
        if(!violations.isEmpty()) {
            String description = "Attempt of refresh token with wrong data: ";
            ConstraintViolationException exception = new ConstraintViolationException(violations);
            logException(description, exception);
            throw exception;
        }
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.getUserInfo(..)) && args(authentication)")
    public void checkGetUserInfoViolations(Authentication authentication) throws InvalidAccessTokenException {
        if(authentication == null) {
            InvalidAccessTokenException exception = new InvalidAccessTokenException("Access denied. Empty Authorization header. " +
                    "The request must include [Authorization] header with [Bearer [access_token]] value");
            logException("", exception);
            throw exception;
        }
        if(!(authentication.getPrincipal() instanceof Jwt)) {
            InvalidAccessTokenException exception = new InvalidAccessTokenException("Access denied. Valid access token required");
            logException("", exception);
            throw exception;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        if(Instant.now().isAfter(jwt.getExpiresAt())) {
            InvalidAccessTokenException exception = new InvalidAccessTokenException("Access denied. Expired access token");
            logException("", exception);
            throw exception;
        }
    }

    private String getConstrainViolationDetails(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString() + " = " + violation.getInvalidValue())
                .collect(Collectors.joining("; "));
    }

    private void logException(String exceptionDescription, Exception exception) {
        if(exceptionDescription == null) exceptionDescription = "";
        Tracer tracer = getTracer(applicationName, "gateway-user-rest-controller-validation-aspect-tracer", openTelemetry);
        Span span = getSpan(tracer, "exception-span");
        String traceId = getTraceId(span);
        String spanId = getSpanId(span);
        span.recordException(exception);
        String logMessage = String.format("[%s-%s] %s occurred: %s %s", traceId, spanId, exception.getClass().getName(), exceptionDescription, exception.getMessage());
        log.warn(logMessage);
        span.end();
    }
}
