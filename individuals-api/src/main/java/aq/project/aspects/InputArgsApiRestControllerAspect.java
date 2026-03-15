package aq.project.aspects;

import aq.project.dto.CreateUserRequest;
import aq.project.dto.LoginUserRequest;
import aq.project.dto.RefreshTokenRequest;
import aq.project.exceptions.InvalidPasswordConfirmException;
import aq.project.exceptions.InvalidUserRegistrationRequestException;
import aq.project.exceptions.LackIndividualsDataException;
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
public class InputArgsApiRestControllerAspect {

    private final Validator validator;

    @Before("execution(* aq.project.controllers.ApiRestController.createUser(..)) && args(request)")
    public void checkUserRegistrationRequestViolations(CreateUserRequest createUserRequest) throws InvalidPasswordConfirmException, InvalidUserRegistrationRequestException, LackIndividualsDataException {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(createUserRequest);
        if(!violations.isEmpty()) {
            String msg = "Attempt of registration an user with wrong user data: ";
            String details = getConstrainViolationDetails(violations);
            String warn = msg + details;
            logWarn(warn);
            throw new ConstraintViolationException(violations);
        }
        if(createUserRequest.getIndividualData() == null) {
            throw new LackIndividualsDataException("Error during registration: individual data of user is null");
        }
        if(isBlank(createUserRequest.getPassword()) || isBlank(createUserRequest.getUsername()) || isBlank(createUserRequest.getIndividualData().getEmail())) {
            throw new InvalidUserRegistrationRequestException("Error during registration: email, username, password must no blank");
        }
        if(!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())) {
            String msg = "Error during registration: input password and it's confirm do not match";
            logWarn(msg);
            throw new InvalidPasswordConfirmException(msg);
        }
    }

    private boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    @Before("execution(* aq.project.controllers.ApiRestController.loginUser(..)) && args(request)")
    public void checkUserLoginRequestViolations(LoginUserRequest loginUserRequest) {
        Set<ConstraintViolation<LoginUserRequest>> violations = validator.validate(loginUserRequest);
        if(!violations.isEmpty()) {
            String msg = "Attempt of loginUser an user with wrong user data: ";
            String details = getConstrainViolationDetails(violations);
            String warn = msg + details;
            logWarn(warn);
            throw new ConstraintViolationException(violations);
        }
    }

    @Before("execution(* aq.project.controllers.ApiRestController.updateToken(..)) && args(request)")
    public void checkRefreshTokenRequestViolations(RefreshTokenRequest refreshTokenRequest) {
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(refreshTokenRequest);
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
