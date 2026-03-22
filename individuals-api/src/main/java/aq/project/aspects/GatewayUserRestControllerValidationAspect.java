package aq.project.aspects;

import aq.project.dto.CreateUserEvent;
import aq.project.dto.LoginUserEvent;
import aq.project.dto.RefreshTokenDTO;
import aq.project.dto.UpdateUserEvent;
import aq.project.exceptions.InvalidPasswordConfirmException;
import aq.project.exceptions.InvalidUserRegistrationEventException;
import aq.project.exceptions.LackIndividualsDataException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class GatewayUserRestControllerValidationAspect {

    private final Validator validator;

    @Before("execution(* aq.project.controllers.GatewayUserRestController.createUser(..)) && args(createUserEvent)")
    public void checkCreateUserViolations(CreateUserEvent createUserEvent) throws InvalidPasswordConfirmException, InvalidUserRegistrationEventException, LackIndividualsDataException {
        Set<ConstraintViolation<CreateUserEvent>> violations = validator.validate(createUserEvent);
        if(!violations.isEmpty()) {
            String msg = "Attempt of create user with wrong data: ";
            String details = getConstrainViolationDetails(violations);
            String warn = msg + details;
            logWarn(warn);
            throw new ConstraintViolationException(violations);
        }
        if(createUserEvent.getIndividualData() == null) {
            throw new LackIndividualsDataException("Error during registration: individual data of user is null");
        }
        if(isBlank(createUserEvent.getPassword()) || isBlank(createUserEvent.getUsername()) || isBlank(createUserEvent.getIndividualData().getEmail())) {
            throw new InvalidUserRegistrationEventException("Error during registration: email, username, password must no blank");
        }
        if(!createUserEvent.getPassword().equals(createUserEvent.getConfirmPassword())) {
            String msg = "Error during registration: input password and it's confirm do not match";
            logWarn(msg);
            throw new InvalidPasswordConfirmException(msg);
        }
    }

    private boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.loginUser(..)) && args(loginUserEvent)")
    public void checkLoginUserViolations(LoginUserEvent loginUserEvent) {
        Set<ConstraintViolation<LoginUserEvent>> violations = validator.validate(loginUserEvent);
        if(!violations.isEmpty()) {
            String msg = "Attempt of login user with wrong data: ";
            String details = getConstrainViolationDetails(violations);
            String warn = msg + details;
            logWarn(warn);
            throw new ConstraintViolationException(violations);
        }
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.updateUser(..)) && args(updateUserEvent)")
    public void checkUpdateUserViolations(UpdateUserEvent updateUserEvent) {
        Set<ConstraintViolation<UpdateUserEvent>> violations = validator.validate(updateUserEvent);
        if(!violations.isEmpty()) {
            String msg = "Attempt of update user with wrong user data: ";
            String details = getConstrainViolationDetails(violations);
            String warn = msg + details;
            logWarn(warn);
            throw new ConstraintViolationException(violations);
        }
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.deleteUserByKeycloakId(..)) && args(keycloakId)")
    public void checkDeleteUserByKeycloakIdViolations(@Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String keycloakId) {
    }

    @Before("execution(* aq.project.controllers.GatewayUserRestController.refreshToken(..)) && args(refreshTokenDTO)")
    public void checkRefreshTokenViolations(RefreshTokenDTO refreshTokenDTO) {
        Set<ConstraintViolation<RefreshTokenDTO>> violations = validator.validate(refreshTokenDTO);
        if(!violations.isEmpty()) {
            String msg = "Attempt of refresh token with wrong data: ";
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
