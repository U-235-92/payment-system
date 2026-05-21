package aq.project.aspects;

import aq.project.dto.CreateWalletRequestDTO;
import jakarta.validation.Valid;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Component
@Validated
public class WalletRestControllerValidationAspect {

    @Before("execution(* aq.project.controllers.WalletRestController.createWallet(..)) && args(createWalletRequestDTO)")
    public void validateCreateWalletArguments(@Valid CreateWalletRequestDTO createWalletRequestDTO) {}
}
