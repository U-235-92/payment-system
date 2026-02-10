package aq.project.aspects;

import aq.project.dto.TokenRefreshRequest;
import aq.project.dto.UserLoginRequest;
import aq.project.dto.UserRegistrationRequest;
import aq.project.exceptions.InvalidPasswordConfirmException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class InputArgsAuthControllerAspect {

    private final Validator validator;

    @Before("execution(* aq.project.controllers.AuthController.createUser(..)) && args(request)")
    public void checkUserRegistrationRequestViolations(UserRegistrationRequest request) throws InvalidPasswordConfirmException {
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
        if(!violations.isEmpty()) {
            String msg = "Attempt of registration an user with wrong user data: ";
            String details = getConstrainViolationDetails(violations);
            String warn = msg + details;
            logWarn(warn);
            throw new ConstraintViolationException(violations);
        }
        if(!request.getPassword().equals(request.getConfirmPassword())) {
            String msg = "Error during registration: input password and it's confirm do not match";
            logWarn(msg);
            throw new InvalidPasswordConfirmException(msg);
        }
    }

    @Before("execution(* aq.project.controllers.AuthController.requestToken(..)) && args(request)")
    public void checkUserLoginRequestViolations(UserLoginRequest request) {
        Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(request);
        if(!violations.isEmpty()) {
            String msg = "Attempt of login an user with wrong user data: ";
            String details = getConstrainViolationDetails(violations);
            String warn = msg + details;
            logWarn(warn);
            throw new ConstraintViolationException(violations);
        }
    }

    @Before("execution(* aq.project.controllers.AuthController.updateToken(..)) && args(request)")
    public void checkRefreshTokenRequestViolations(TokenRefreshRequest request) {
        Set<ConstraintViolation<TokenRefreshRequest>> violations = validator.validate(request);
        if(!violations.isEmpty()) {
            String msg = "Attempt of getting new tokens with wrong refresh token input data: ";
            String details = getConstrainViolationDetails(violations);
            String warn = msg + details;
            logWarn(warn);
            throw new ConstraintViolationException(violations);
        }
    }

    private String getConstrainViolationDetails(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString() + " = " + violation.getInvalidValue())
                .collect(Collectors.joining("; "));
    }

    private void logWarn(String msg) {
        log.warn(msg);
    }
}
